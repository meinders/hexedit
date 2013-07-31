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

import java.io.*;
import java.nio.channels.*;
import java.util.*;
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

				final JFrame frame = new JFrame();
				frame.setContentPane( view );
				frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
				frame.setBounds( 400, 100, 1280, 800 );
				frame.setVisible( true );
			}
		} );
	}

	private static DataModel createDataModel( final String filename )
	throws IOException
	{
		final File file = new File( filename );
		final FileChannel channel = FileChannel.open( file.toPath() );
		return new DataModel( file.toURI(), channel );
	}
}
