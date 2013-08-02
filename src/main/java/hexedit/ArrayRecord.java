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

import java.util.*;

/**
 * Tightly packed array of fixed-length records.
 *
 * @author Gerrit Meinders
 */
public class ArrayRecord
	extends Record
{
	/**
	 * Start of the first record.
	 */
	private long _start;

	/**
	 * Length of each record.
	 */
	private long _length = 0L;

	/**
	 * Number of records.
	 */
	private long _count = 0L;

	/**
	 * Index of the current record.
	 */
	private long _index;

	/**
	 * Start of the current record.
	 */
	private long _address;

	/**
	 * Constructs a new instance.
	 *
	 * @param start  Start of the first record.
	 * @param length Length of each record.
	 * @param count  Number of records.
	 */
	public ArrayRecord( final long start, final long length, final long count )
	{
		_start = start;
		setLength( length );
		setCount( count );

		_index = 0L;
		update( 0L );
	}

	@Override
	public boolean hasPrevious()
	{
		return _index > 0L;
	}

	@Override
	public boolean hasNext()
	{
		return _index < _count - 1L;
	}

	@Override
	public long getStart()
	{
		return _address;
	}

	public void setStart( final long start )
	{
		final long end = getEnd();
		_start = start;
		update( 0L );
		setEnd( end );
	}

	@Override
	public long getEnd()
	{
		return _address + _length - 1L;
	}

	public void setEnd( final long end )
	{
		setLength( end - getStart() + 1L );
	}

	@Override
	public long getLength()
	{
		return _length;
	}

	public void setLength( final long length )
	{
		if ( length <= 0L )
		{
			throw new IllegalArgumentException( "Record length must be greater than zero." );
		}
		_length = length;
	}

	public long getCount()
	{
		return _count;
	}

	public void setCount( final long count )
	{
		if ( count <= 0L )
		{
			throw new IllegalArgumentException( "Array must contain at least 1 record." );
		}
		_count = count;
	}

	@Override
	public void previous()
	{
		if ( !hasPrevious() )
		{
			throw new NoSuchElementException();
		}
		update( -1L );
	}

	@Override
	public void next()
	{
		if ( !hasNext() )
		{
			throw new NoSuchElementException();
		}
		update( 1L );
	}

	private void update( final long offset )
	{
		_index += offset;
		_address = _start + _index * _length;
	}
}
