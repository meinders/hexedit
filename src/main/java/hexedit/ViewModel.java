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
import java.util.*;

/**
 * FIXME Need comment
 *
 * @author Gerrit Meinders
 */
public class ViewModel
extends Observable
{
	private DataModel _dataModel;

	private int _columns = 24;

	private long _firstRowAddress = 0L;

	private float _translateX = 0.0f;

	private float _translateY = 0.0f;

	private float _scale = 1.0f;

	private float _tileSize = 45.0f;

	private float _tilePadding = 3.0f;

	private AffineTransform _affineTransform;

	private long _selectionStart = 0L;

	private long _selectionEnd = -1L;

	public int getColumns()
	{
		return _columns;
	}

	public DataModel getDataModel()
	{
		return _dataModel;
	}

	public void setDataModel( final DataModel dataModel )
	{
		if ( _dataModel != dataModel )
		{
			_dataModel = dataModel;
			setChanged();
			notifyObservers();
		}
	}

	public AffineTransform getTransform()
	{
		if ( _affineTransform == null )
		{
			_affineTransform = new AffineTransform( _scale, 0.0f, 0.0f, _scale, _translateX, _translateY );
		}
		return _affineTransform;
	}

	public Tile getTile()
	{
		return new Tile( this, _dataModel );
	}

	public Tile getTile( final long address )
	{
		final Tile result = new Tile( this, _dataModel );
		result.setAddress( address );
		return result;
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
		result.setAddress( _firstRowAddress + (long)offset );
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
		final float dx = (float)( to.getX() - from.getX() );
		final float dy = (float)( to.getY() - from.getY() );
		if ( dx != 0.0f || dy != 0.0f )
		{
			_translateX += dx;
			_translateY += dy;
			_affineTransform = null;
			normalizePosition();
			setChanged();
			notifyObservers();
		}
	}

	public void scale( final Point2D center, final float factor )
	{
		final float oldScale = _scale;
		final float newScale = Math.max( 1.0f, Math.min( 8.0f, oldScale * factor ) );
		final float actualFactor = newScale / oldScale;
		if ( actualFactor != 1.0f )
		{
			final float centerX = (float)center.getX();
			final float centerY = (float)center.getY();
			_translateX = ( _translateX - centerX ) * factor + centerX;
			_translateY = ( _translateY - centerY ) * factor + centerY;
			_scale = newScale;
			_affineTransform = null;
			normalizePosition();
			setChanged();
			notifyObservers();
		}
	}

	private void normalizePosition()
	{
		final float tileDistance = ( _tileSize + _tilePadding ) * _scale;
		final float translateY = _translateY;

		int rows = (int)Math.ceil( (double)( translateY / tileDistance ) );

		if ( _firstRowAddress < (long)rows * (long)_columns )
		{
			rows = (int)( _firstRowAddress / (long)_columns );
		}

		if ( rows != 0 )
		{
			_firstRowAddress -= (long)rows * (long)_columns;
			_translateY -= (float)rows * tileDistance;
		}
	}

	public long getSelectionStart()
	{
		return _selectionStart;
	}

	public void setSelectionStart( final long selectionStart )
	{
		if ( _selectionStart != selectionStart )
		{
			_selectionStart = selectionStart;
			setChanged();
			notifyObservers();
		}
	}

	public long getSelectionEnd()
	{
		return _selectionEnd;
	}

	public void setSelectionEnd( final long selectionEnd )
	{
		if ( _selectionEnd != selectionEnd )
		{
			_selectionEnd = selectionEnd;
			setChanged();
			notifyObservers();
		}
	}

	public long getSelectionLength()
	{
		return _selectionEnd - _selectionStart + 1L;
	}

	public void select( final long start, final long end )
	{
		final long newStart = Math.min( start, end );
		if ( _selectionStart != newStart )
		{
			_selectionStart = newStart;
			setChanged();
		}

		final long newEnd = Math.max( start, end );
		if ( _selectionEnd != newEnd )
		{
			_selectionEnd = newEnd;
			setChanged();
		}

		notifyObservers();
	}

	public void clearSelection()
	{
		if ( getSelectionLength() > 0L )
		{
			_selectionStart = 0L;
			_selectionEnd = -1L;
			setChanged();
			notifyObservers();
		}
	}

	public boolean isSelected( final long address )
	{
		return _selectionStart <= address && address <= _selectionEnd;
	}

	public String getSelectionValue()
	{
		final long selectionLength = getSelectionLength();

		if ( selectionLength == 1L )
		{
			final Tile tile = getTile( getSelectionStart() );
			return tile.getUnsignedDecimal();
		}
		else
		{
			final long start = _selectionStart;
			if ( selectionLength >= 2L && selectionLength <= 8L )
			{
				try
				{
					final long bigEndian = _dataModel.getBigEndian( start, (int)selectionLength );
					final long littleEndian = _dataModel.getLittleEndian( start, (int)selectionLength );

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
				final long end = _selectionEnd;
				return "Length: " + selectionLength + ", start: " + ( _dataModel.getOffset() + start ) + ", end: " + ( _dataModel.getOffset() + end );
			}
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

	public long getFirstRowAddress()
	{
		return _firstRowAddress;
	}

	public Iterable<Tile> getTiles()
	{
		final long start = getFirstRowAddress();
		final long end = start + 800L;

		return new Iterable<Tile>()
		{
			@Override
			public Iterator<Tile> iterator()
			{
				return new TileIterator( getTile( start ), end );
			}
		};
	}

	private static class TileIterator
	implements Iterator<Tile>
	{
		private final Tile _tile;

		private long _current;

		private long _end;

		/**
		 * Constructs a new instance.
		 *
		 * @param tile First tile.
		 * @param end  Address of last tile.
		 */
		private TileIterator( final Tile tile, final long end )
		{
			_tile = tile;
			_current = tile.getAddress();
			_end = end;
		}

		@Override
		public boolean hasNext()
		{
			return _current <= _end;
		}

		@Override
		public Tile next()
		{
			if ( !hasNext() )
			{
				throw new NoSuchElementException();
			}
			_tile.setAddress( _current++ );
			return _tile;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
