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
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import javax.swing.*;

/**
 * FIXME Need comment
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
	throws IOException
	{
		if ( args.length == 0 )
		{
			System.err.println( "Usage: java hexedit.Main <filename>" );
			return;
		}

		final File file = new File( args[ 0 ] );
		final URI dataSource = file.toURI();
		final Path path = Paths.get( dataSource );
		final FileChannel channel = FileChannel.open( path );

		final DataModel dataModel = new DataModel( dataSource, channel );

		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				final ViewModel viewModel = new ViewModel();
				viewModel.setDataModel( dataModel );

				final View view = new View();
				view.setViewModel( viewModel );

				final JFrame frame = new JFrame();
				frame.setContentPane( view );
				frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
				frame.setBounds( 400, 100, 1280, 800 );
				frame.setVisible( true );

/*
				final Timer timer = new Timer( 50, new ActionListener()
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						panel.repaint();
					}
				} );
				timer.setRepeats( true );
				timer.start();
*/
			}
		} );
	}
}
