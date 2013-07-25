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
import java.nio.*;
import java.nio.channels.*;

/**
 * FIXME Need comment
 *
 * @author Gerrit Meinders
 */
public class DataModel
{
	private final FileChannel _channel;

	private long _offset = 0L;

	private byte[] _bytes;

	private int _windowSize;

	private URI _dataSource;

	/**
	 * Constructs a new instance.
	 *
	 * @param dataSource URI of the data source.
	 * @param channel    Channel that provides access to the data.
	 */
	public DataModel( final URI dataSource, final FileChannel channel )
	{
		_channel = channel;
		_dataSource = dataSource;
		_windowSize = 0x10000;
	}

	public long getOffset()
	{
		return _offset;
	}

	public void setOffset( final long offset )
	{
		if ( _offset != offset )
		{
			_offset = offset;
			_bytes = null;
		}
	}

	public int getWindowSize()
	{
		return _windowSize;
	}

	public void setWindowSize( final int windowSize )
	{
		if ( _windowSize != windowSize )
		{
			_windowSize = windowSize;
			_bytes = null;
		}
	}

	public byte[] getBytes()
	throws IOException
	{
		if ( _bytes == null )
		{
			_channel.position( _offset );
			final ByteBuffer buffer = ByteBuffer.allocateDirect( _windowSize );

			while ( buffer.hasRemaining() && _channel.read( buffer ) != -1 )
			{
			}

			buffer.flip();
			final byte[] bytes = new byte[ buffer.limit() ];
			buffer.get( bytes );
			_bytes = bytes;
		}

		return _bytes;
	}

	public URI getDataSource()
	{
		return _dataSource;
	}

	public void setDataSource( URI dataSource )
	{
		_dataSource = dataSource;
	}
}
