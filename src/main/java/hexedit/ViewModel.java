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
import java.net.*;

/**
 * FIXME Need comment
 *
 * @author Gerrit Meinders
 */
public class ViewModel
{
	private DataModel _dataModel;

	private int _columns = 24;

	private int _rows = 32;

	private float _x = 0.0f;

	private float _y = 0.0f;

	private float _scaleX = 2.0f;

	private float _scaleY = 2.0f;

	private float _tileSize = 30.0f;

	private float _tilePadding = 2.0f;

	private AffineTransform _affineTransform;

	private Tile _selectionStart;

	private Tile _selectionEnd;

	public int getColumns()
	{
		return _columns;
	}

	public int getRows()
	{
		return _rows;
	}

	public DataModel getDataModel()
	{
		return _dataModel;
	}

	public void setDataModel( final DataModel dataModel )
	{
		_dataModel = dataModel;
	}

	public AffineTransform getTransform()
	{
		if ( _affineTransform == null )
		{
			_affineTransform = new AffineTransform( _scaleX, 0.0f, 0.0f, _scaleY, _x, _y );
		}
		return _affineTransform;
	}

	public Tile getTile()
	{
		return new Tile( this, _dataModel );
	}

	public Tile getTile( final Point2D point )
	{
		final AffineTransform transform = getTransform();
		final Point2D.Float modelPoint = Tools.inverseTransform( transform, point );

		if ( ( modelPoint.x < 0.0f ) || ( modelPoint.y < 0.0f ) )
		{
			return null;
		}

		final float spacing = _tileSize + _tilePadding;
		if ( ( modelPoint.x % spacing > _tileSize ) || ( modelPoint.y % spacing > _tileSize ) )
		{
			return null;
		}

		final int column = (int)( modelPoint.x / spacing );
		if ( column >= getColumns() )
		{
			return null;
		}

		final int row = (int)( modelPoint.y / spacing );
		final int offset = row * getColumns() + column;

		if ( offset >= _dataModel.getWindowSize() )
		{
			return null;
		}

		final Tile result = new Tile( this, _dataModel );
		result.setOffset( offset );
		return result;
	}

	public float getTileSize()
	{
		return _tileSize;
	}

	public float getTilePadding()
	{
		return _tilePadding;
	}

	public void moveView( final Point2D from, final Point2D to )
	{
		_x += (float)( to.getX() - from.getX() );
		_y += (float)( to.getY() - from.getY() );
		_affineTransform = null;
	}

	public void scale( final Point2D center, final float factor )
	{
		final float centerX = (float)center.getX();
		final float centerY = (float)center.getY();
		_x = ( _x - centerX ) * factor + centerX;
		_y = ( _y - centerY ) * factor + centerY;
		_scaleX *= factor;
		_scaleY *= factor;
		_affineTransform = null;
	}

	public Tile getSelectionStart()
	{
		return _selectionStart;
	}

	public void setSelectionStart( final Tile selectionStart )
	{
		_selectionStart = selectionStart;
	}

	public Tile getSelectionEnd()
	{
		return _selectionEnd;
	}

	public void setSelectionEnd( final Tile selectionEnd )
	{
		_selectionEnd = selectionEnd;
	}

	public void clearSelection()
	{
		setSelectionStart( null );
		setSelectionEnd( null );
	}

	public boolean isSelected( final int offset )
	{
		if ( _selectionStart != null )
		{
			if ( _selectionEnd != null )
			{
				return _selectionStart.getOffset() <= offset && _selectionEnd.getOffset() >= offset ||
				       _selectionStart.getOffset() >= offset && _selectionEnd.getOffset() <= offset;
			}
			else
			{
				return _selectionStart.getOffset() == offset;
			}
		}
		else
		{
			return false;
		}
	}

	public String getSelectionValue()
	{
		final int selectionLength = getSelectionLength();

		if ( selectionLength == 1 )
		{
			return _selectionStart.getUnsignedDecimal();
		}
		else
		{
			final int start = Math.min( _selectionStart.getOffset(), _selectionEnd.getOffset() );
			final int end = Math.max( _selectionStart.getOffset(), _selectionEnd.getOffset() );
			if ( selectionLength >= 2 && selectionLength <= 8 )
			{
				try
				{
					final byte[] bytes = _dataModel.getBytes();

					long bigEndian = 0L;
					for ( int offset = start; offset <= end; offset++ )
					{
						bigEndian = ( bigEndian << 8 ) | ( (long)bytes[ offset ] & 0xffL );
					}

					long littleEndian = 0L;
					for ( int offset = end; offset >= start; offset-- )
					{
						littleEndian = ( littleEndian << 8 ) | ( (long)bytes[ offset ] & 0xffL );
					}

					final StringBuilder builder = new StringBuilder();
					builder.append( "int: LE " );
					builder.append( littleEndian );
					builder.append( " / BE " );
					builder.append( bigEndian );
					if ( selectionLength == 4 )
					{
						builder.append( " -- float: LE " );
						builder.append( Float.intBitsToFloat( (int)littleEndian ) );
						builder.append( " / BE " );
						builder.append( Float.intBitsToFloat( (int)bigEndian ) );
					}
					else if ( selectionLength == 8 )
					{
						builder.append( " -- float: LE " );
						builder.append( Double.longBitsToDouble( littleEndian ) );
						builder.append( " / BE " );
						builder.append( Double.longBitsToDouble( bigEndian ) );
					}
					return builder.toString();
				}
				catch ( IOException e )
				{
					return e.toString();
				}
			}
			else
			{
				return "Length: " + selectionLength + ", start: " + ( _dataModel.getOffset() + start ) + ", end: " + ( _dataModel.getOffset() + end );
			}
		}
	}

	private int getSelectionLength()
	{
		if ( _selectionStart == null )
		{
			return 0;
		}
		else if ( _selectionEnd == null )
		{
			return 1;
		}
		else
		{
			return Math.abs( _selectionStart.getOffset() - _selectionEnd.getOffset() ) + 1;
		}
	}

	public String getDataSourceName()
	{
		final URI dataSource = _dataModel.getDataSource();
		if ( "file".equals( dataSource.getScheme() ) )
		{
			final File file = new File( dataSource );
			return file.getName();
		}
		else
		{
			return dataSource.toString();
		}
	}

	public String getDataSourceDetails()
	{
		final URI dataSource = _dataModel.getDataSource();
		if ( "file".equals( dataSource.getScheme() ) )
		{
			final File file = new File( dataSource );
			return file.getAbsolutePath();
		}
		else
		{
			return null;
		}
	}
}
