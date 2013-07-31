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
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Highlights based on the number of distinct values for a given address in two
 * or more data sources.
 *
 * @author Gerrit Meinders
 */
public class DifferenceHighlighter
implements Highlighter
{
	private List<DataModel> _dataModels;

	private byte[] _values;

	private Color[] _colors;

	/**
	 * Constructs a new instance.
	 *
	 * @param dataModels Data models to show differences between.
	 */
	public DifferenceHighlighter( final List<DataModel> dataModels )
	{
		if ( dataModels.size() < 2 )
		{
			throw new IllegalArgumentException( "At least two data models are needed." );
		}

		_dataModels = new ArrayList<DataModel>( dataModels );

		final int size = dataModels.size();
		_values = new byte[ size ];
		_colors = new Color[ size ];

		for ( int i = 0; i < size; i++ )
		{
			_colors[ i ] = new Color( 0xff - 0x44 * i / ( size - 1 ), 0xff - 0x22 * i / ( size - 1 ), 0xff - 0x44 * i / ( size - 1 ) );
		}
	}

	@Override
	public Color getColor( final long address )
	{
		int count = 0;
		models: for ( final DataModel dataModel : _dataModels )
		{
			byte value = (byte)0;
			try
			{
				value = dataModel.getByte( address );
			}
			catch ( IOException ignored )
			{
			}

			for ( int i = 0; i < count; i++ )
			{
				if ( _values[ i ] == value )
				{
					continue models;
				}
			}

			_values[ count++ ] = value;
		}

		return _colors[ count - 1 ];
	}
}
