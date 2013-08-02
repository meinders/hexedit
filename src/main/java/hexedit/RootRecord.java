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
import java.util.*;

/**
 * Special record that represents the entire file.
 *
 * @author Gerrit Meinders
 */
public class RootRecord
extends Record
{
	private final DataModel _dataModel;

	/**
	 * Constructs a new instance.
	 */
	public RootRecord( final DataModel dataModel )
	{
		_dataModel = dataModel;
	}

	@Override
	public boolean hasPrevious()
	{
		return false;
	}

	@Override
	public boolean hasNext()
	{
		return false;
	}

	@Override
	public long getStart()
	{
		return 0L;
	}

	@Override
	public long getEnd()
	{
		try
		{
			return _dataModel.getLength();
		}
		catch ( IOException ignored )
		{
			return 0L;
		}
	}

	@Override
	public void previous()
	{
		throw new NoSuchElementException();
	}

	@Override
	public void next()
	{
		throw new NoSuchElementException();
	}
}
