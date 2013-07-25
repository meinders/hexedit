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
 * FIXME Need comment
 *
 * @author Gerrit Meinders
 */
public class Tile
{
	private final ViewModel _viewModel;

	private final DataModel _dataModel;

	private int _offset = -1;

	private int _column = 0;

	private int _row = 0;

	private String _hexadecimal;

	private String _unsignedDecimal;

	private boolean _printable;

	private char _character;

	public Tile( final ViewModel viewModel, final DataModel dataModel )
	{
		_viewModel = viewModel;
		_dataModel = dataModel;
	}

	public int getOffset()
	{
		return _offset;
	}

	public void setOffset( final int offset )
	{
		_offset = offset;
		init();
	}

	public boolean next()
	{
		if ( ++_offset < _dataModel.getWindowSize() )
		{
			init();
			return true;
		}
		else
		{
			_offset--;
			return false;
		}
	}

	private void init()
	{
		final int columns = _viewModel.getColumns();
		final int column = _offset % columns;
		final int row = _offset / columns;
//		final int rows = _viewModel.getRows();
//		final int block = row / rows;

		_column = column;// + ( columns + 2 ) * block;
		_row = row;// % rows;

		try
		{
			final byte[] bytes = _dataModel.getBytes();
			final int value = (int)bytes[ _offset ];
			final int unsignedValue = value & 0xff;

			_character = (char)unsignedValue;
			_printable = value >= 32 && value <= 127;

			_unsignedDecimal = String.valueOf( unsignedValue );
			_hexadecimal = Tools.byteToHexString( value );
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
		return _printable;
	}

	public String getHexadecimal()
	{
		return _hexadecimal;
	}

	public String getUnsignedDecimal()
	{
		return _unsignedDecimal;
	}

	public char getCharacter()
	{
		return _character;
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
		return _viewModel.isSelected( _offset );
	}

	public boolean isSelectionAnchor()
	{
		return !isSelected() && ( _viewModel.isSelected( _offset - 1 ) || _viewModel.isSelected( _offset + 1 ) );
	}

	@Override
	public int hashCode()
	{
		return _offset;
	}

	@Override
	public boolean equals( final Object other )
	{
		return other instanceof Tile && _offset == ( (Tile)other )._offset;
	}

	public long getAddress()
	{
		return (long)getOffset() + _dataModel.getOffset();
	}
}
