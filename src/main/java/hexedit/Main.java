/*
 * Copyright 2013 Gerrit Meinders
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hexedit;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.channels.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * Main class that starts the hex editor to edit one or more files specified
 * on the command-line.
 *
 * @author Gerrit Meinders
 */
public class Main
{
	/**
	 * Run application.
	 *
	 * @param   args    Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		if ( args.length == 0 )
		{
			System.err.println( "Usage: java hexedit.Main <filename>..." );
			return;
		}

		final List<DataModel> dataModels = new ArrayList<DataModel>();
		for ( final String arg : args )
		{
			try
			{
				dataModels.add( createDataModel( arg ) );
			}
			catch ( IOException e )
			{
				System.err.println( "Failed to open file: " + arg );
				return;
			}
		}

		final ViewModel viewModel = new ViewModel();
		viewModel.setDataModel( dataModels.get( 0 ) );
		viewModel.setRecord( new RootRecord( viewModel.getDataModel() ) );
		if ( dataModels.size() > 1 )
		{
			viewModel.setHighlighter( new DifferenceHighlighter( dataModels ) );
		}

		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				final View view = new View();
				view.setViewModel( viewModel );
				view.setMenu( createMenu( view ) );

				final JFrame frame = new JFrame();
				frame.setContentPane( view );
				frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
				frame.setBounds( 400, 100, 1280, 800 );
				frame.setVisible( true );
			}
		} );
	}

	private static Menu createMenu( final View view )
	{
		final Menu navigateMenu = new Menu( 4, new Color( 0x6688aa ) );
		navigateMenu.setAction( new AbstractAction( "navigate" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				view.setMenu( navigateMenu );
			}
		} );

		final Menu structureMenu = new Menu( 4, new Color( 0x7fa1bb ) );
		structureMenu.setAction( new AbstractAction( "structure" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				view.setMenu( structureMenu );
			}
		} );

		final Menu analyzeMenu = new Menu( 4, new Color( 0x99bbcc ) );
		analyzeMenu.setAction( new AbstractAction( "analyze" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				view.setMenu( analyzeMenu );
			}
		} );

		final Action startOfFile = new AbstractAction( "start" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				viewModel.jumpTo( 0L );
			}
		};

		final Action jumpAbsolute = new AbstractAction( "jump" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				final long selectionLength = viewModel.getSelectionLength();
				if ( selectionLength >= 1L || selectionLength <= 8L )
				{
					final DataModel dataModel = viewModel.getDataModel();
					try
					{
						final long target = dataModel.getLittleEndian( viewModel.getSelectionStart(), (int)selectionLength );
						viewModel.jumpTo( target );
						viewModel.select( target, target );
					}
					catch ( IOException e1 )
					{
						e1.printStackTrace(); // FIXME: Generated try-catch block.
					}
				}
			}
		};

		final Action endOfFile = new AbstractAction( "end" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				final DataModel dataModel = viewModel.getDataModel();
				try
				{
					viewModel.jumpTo( dataModel.getLength() );
				}
				catch ( IOException e1 )
				{
					e1.printStackTrace(); // FIXME: Generated try-catch block.
				}
			}
		};

		final Action startOfRecord = new AbstractAction( "start" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				final Record record = viewModel.getRecord();
				if ( record != null )
				{
					viewModel.jumpTo( record.getStart() );
				}
			}
		};

		final Action jumpRelative = new AbstractAction( "jump" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				final long selectionLength = viewModel.getSelectionLength();
				if ( selectionLength == 2L || selectionLength == 4L )
				{
					final DataModel dataModel = viewModel.getDataModel();
					try
					{
						final Record record = viewModel.getRecord();
						final long target = record.getStart() + dataModel.getLittleEndian( viewModel.getSelectionStart(), (int)selectionLength );
						viewModel.jumpTo( target );
						viewModel.select( target, target );
					}
					catch ( IOException e1 )
					{
						e1.printStackTrace(); // FIXME: Generated try-catch block.
					}
				}
			}
		};

		final Action endOfRecord = new AbstractAction( "end" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				final Record record = viewModel.getRecord();
				if ( record != null )
				{
					viewModel.jumpTo( record.getEnd() );
				}
			}
		};

		final Action parentRecord = new AbstractAction( "parent" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				final Record record = viewModel.getRecord();
				if ( record != null )
				{
					final Record parent = record.getParent();
					if ( parent != null )
					{
						final Definition recordDefinition = record.getRecordDefinition();
						if ( recordDefinition != null )
						{
							viewModel.jumpTo( recordDefinition.getAddress() );
						}
						viewModel.setRecord( parent );
					}
				}
			}
		};

		final Action newRecord = new AbstractAction( "new" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				if ( !viewModel.isSelectionEmpty() )
				{
					final ArrayRecord record = new ArrayRecord( viewModel.getSelectionStart(), viewModel.getSelectionLength(), 1L );
					final Record parent = viewModel.getRecord();
					record.setParent( parent );
					final RecordDefinition definition = new RecordDefinition( "record", record );
					record.setRecordDefinition( definition );
					parent.addDefinition( definition );
					viewModel.setRecord( record );
					viewModel.clearSelection();
				}
			}
		};

		final Action newRecordFromPointer = new AbstractAction( "pointer" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				final long selectionLength = viewModel.getSelectionLength();
				if ( selectionLength >= 1L && selectionLength <= 8L )
				{
					final DataModel dataModel = viewModel.getDataModel();
					try
					{
						final long pointer = dataModel.getLittleEndian( viewModel.getSelectionStart(), (int)selectionLength );
						final ArrayRecord record = new ArrayRecord( pointer, 1L, 1L );
						final Record parent = viewModel.getRecord();
						record.setParent( parent );
						final long pointerAddress = viewModel.getSelectionStart() - parent.getStart();
						final PointerDefinition definition = new PointerDefinition( "pointer", pointerAddress, (int)selectionLength, record );
						record.setRecordDefinition( definition );
						parent.addDefinition( definition );
						viewModel.setRecord( record );
						viewModel.jumpTo( record.getStart() );
						viewModel.clearSelection();
					}
					catch ( IOException e1 )
					{
						e1.printStackTrace(); // FIXME: Generated try-catch block.
					}
				}
			}
		};

		final Action setRecordStart = new AbstractAction( "start" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				if ( !viewModel.isSelectionEmpty() )
				{
					final Record record = viewModel.getRecord();
					if ( record instanceof ArrayRecord )
					{
						final ArrayRecord arrayRecord = (ArrayRecord)record;
						arrayRecord.setStart( viewModel.getSelectionStart() );
						view.repaint();
					}
				}
			}
		};

		final Action setRecordEnd = new AbstractAction( "end" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				if ( !viewModel.isSelectionEmpty() )
				{
					final Record record = viewModel.getRecord();
					if ( record instanceof ArrayRecord )
					{
						final ArrayRecord arrayRecord = (ArrayRecord)record;
						arrayRecord.setEnd( viewModel.getSelectionEnd() );
						view.repaint();
					}
				}
			}
		};

		final Action setRecordLength = new AbstractAction( "length" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				final long selectionLength = viewModel.getSelectionLength();
				if ( selectionLength >= 1L && selectionLength <= 8L )
				{
					final DataModel dataModel = viewModel.getDataModel();
					final long length;
					try
					{
						length = dataModel.getLittleEndian( viewModel.getSelectionStart(), (int)selectionLength );

						final Record record = viewModel.getRecord();
						if ( record instanceof ArrayRecord )
						{
							final ArrayRecord arrayRecord = (ArrayRecord)record;
							arrayRecord.setLength( length );
							view.repaint();
						}
					}
					catch ( IOException e1 )
					{
						e1.printStackTrace(); // FIXME: Generated try-catch block.
					}
				}
			}
		};

		final Action setRecordCount = new AbstractAction( "count" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				final long selectionLength = viewModel.getSelectionLength();
				if ( selectionLength >= 1L && selectionLength <= 8L )
				{
					final DataModel dataModel = viewModel.getDataModel();
					final long length;
					try
					{
						length = dataModel.getLittleEndian( viewModel.getSelectionStart(), (int)selectionLength );

						final Record record = viewModel.getRecord();
						if ( record instanceof ArrayRecord )
						{
							final ArrayRecord arrayRecord = (ArrayRecord)record;
							arrayRecord.setCount( length );
							view.repaint();
						}
					}
					catch ( IOException e1 )
					{
						e1.printStackTrace(); // FIXME: Generated try-catch block.
					}
				}
			}
		};

		final Action deleteRecord = new AbstractAction( "delete" )
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final ViewModel viewModel = view.getViewModel();
				final Record record = viewModel.getRecord();
				final Record parent = record.getParent();
				if ( parent != null )
				{
					final Definition definition = record.getRecordDefinition();
					if ( definition != null )
					{
						parent.removeDefinition( definition );
						viewModel.jumpTo( definition.getAddress() );
					}
					viewModel.setRecord( parent );
				}
			}
		};

		{
			final List<MenuItem> items = new ArrayList<MenuItem>();
			items.add( new MenuItem( startOfFile, 2 ) );
			items.add( new MenuItem( jumpAbsolute, 2 ) );
			items.add( new MenuItem( endOfFile, 2 ) );
			items.add( new MenuItem( startOfRecord, 2 ) );
			items.add( new MenuItem( jumpRelative, 2 ) );
			items.add( new MenuItem( endOfRecord, 2 ) );
			items.add( new MenuItem( parentRecord, 2 ) );
			items.add( structureMenu );
			items.add( analyzeMenu );
			navigateMenu.setItems( items );
		}
		{
			final List<MenuItem> items = new ArrayList<MenuItem>();
			items.add( navigateMenu );
			items.add( new MenuItem( newRecord, 2 ) );
			items.add( new MenuItem( newRecordFromPointer, 2 ) );
			items.add( new MenuItem( setRecordStart, 2 ) );
			items.add( new MenuItem( setRecordEnd, 2 ) );
			items.add( new MenuItem( setRecordLength, 2 ) );
			items.add( new MenuItem( setRecordCount, 2 ) );
			items.add( new MenuItem( deleteRecord, 2 ) );
			items.add( new MenuItem( parentRecord, 2 ) );
			items.add( analyzeMenu );
			structureMenu.setItems( items );
		}

		{
			final List<MenuItem> items = new ArrayList<MenuItem>();
			items.add( navigateMenu );
			items.add( structureMenu );

/*
			items.add( new MenuItem( new AbstractAction( "little-endian" )
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
				}
			}, 2 ) );

			items.add( new MenuItem( new AbstractAction("big-endian")
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
				}
			}, 2 ) );
*/

			analyzeMenu.setItems( items );
		}

		final ViewModel viewModel = view.getViewModel();
		final Observer actionStateUpdater = new Observer()
		{
			@Override
			public void update( final Observable o, final Object arg )
			{
				final ViewModel viewModel = (ViewModel)o;
				final long selectionLength = viewModel.getSelectionLength();

				// If it *could* be a number of up to 64 bits.
				final boolean numberSelected = selectionLength >= 1L && selectionLength <= 8L;

				final Record record = viewModel.getRecord();
				final boolean rootRecord = record.getParent() == null;

				jumpAbsolute.setEnabled( numberSelected );
				jumpRelative.setEnabled( numberSelected );
				parentRecord.setEnabled( !rootRecord );

				setRecordStart.setEnabled( !rootRecord && numberSelected );
				setRecordEnd.setEnabled( !rootRecord && numberSelected );
				setRecordLength.setEnabled( !rootRecord && numberSelected );
				setRecordCount.setEnabled( !rootRecord && numberSelected );
				deleteRecord.setEnabled( !rootRecord );
			}
		};
		viewModel.addObserver( actionStateUpdater );
		actionStateUpdater.update( viewModel, null );

		return structureMenu;
	}

	private static DataModel createDataModel( final String filename )
	throws IOException
	{
		final File file = new File( filename );
		final FileChannel channel = FileChannel.open( file.toPath() );
		return new DataModel( file.toURI(), channel );
	}
}
