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

import java.awt.geom.*;
import java.io.*;

/**
 * View for a single byte.
 *
 * @author Gerrit Meinders
 */
public class Tile
{
	private final ViewModel _viewModel;

	private final DataModel _dataModel;

	private long _address = -1L;

	private int _column = 0;

	private int _row = 0;

	private int _value = 0;

	public Tile( final ViewModel viewModel, final DataModel dataModel )
	{
		_viewModel = viewModel;
		_dataModel = dataModel;
	}

	public long getAddress()
	{
		return _address;
	}

	public void setAddress( final long address )
	{
		_address = address;
		init();
	}

	private void init()
	{
		final long address = getAddress();

		final ViewModel viewModel = _viewModel;
		final long offset = address - viewModel.getFirstRowAddress();
		final int columns = viewModel.getColumns();
		_column = (int)( offset % (long)columns );
		_row = (int)( offset / (long)columns );

		try
		{
			_value = (int)_dataModel.getByte( address );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	public int getColumn()
	{
		return _column;
	}

	public int getRow()
	{
		return _row;
	}

	public boolean isPrintable()
	{
		return _value >= 32 && _value <= 127;
	}

	public String getHexadecimal()
	{
		return Tools.byteToHexString( _value );
	}

	public String getUnsignedDecimal()
	{
		return String.valueOf( _value & 0xff );
	}

	public char getCharacter()
	{
		return (char)( _value & 0xff );
	}

	public Rectangle2D.Float getBounds()
	{
		final float size = _viewModel.getTileSize();
		final float padding = _viewModel.getTilePadding();
		final float spacing = size + padding;
		return new Rectangle2D.Float( spacing * (float)getColumn(), spacing * (float)getRow(), size, size );
	}

	public boolean isSelected()
	{
		return _viewModel.isSelected( getAddress() );
	}

	public boolean isSelectionAnchor()
	{
		return !isSelected() && ( _viewModel.isSelected( getAddress() - 1L ) || _viewModel.isSelected( getAddress() + 1L ) );
	}

	@Override
	public int hashCode()
	{
		return (int)( _address >> 32 ) | (int)_address;
	}

	@Override
	public boolean equals( final Object other )
	{
		return other instanceof Tile && _address == ( (Tile)other )._address;
	}
}
