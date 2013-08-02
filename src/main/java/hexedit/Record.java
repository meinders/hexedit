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
 * A record specifies part of a file containing related data. The meaning of the
 * record's contents can be defined using {@link Definition}s.
 *
 * <p>The same record definition may occur at multiple places in a file, e.g.
 * in the case of an array. The {@link #previous()} and {@link #next()} methods
 * provide a means to navigate between occurrences of the same record.
 *
 * @author Gerrit Meinders
 */
public abstract class Record
{
	private Record _parent;

	private SortedMap<Long, Definition> _definitions;

	protected Record()
	{
		_definitions = new TreeMap<Long, Definition>();
	}

	public Record getParent()
	{
		return _parent;
	}

	public void setParent( final Record parent )
	{
		_parent = parent;
	}

	public Definition getDefinition( final long address )
	{
		Definition result = _definitions.get( address );
		if ( result == null )
		{
			final SortedMap<Long, Definition> headMap = _definitions.headMap( address );
			if ( !headMap.isEmpty() )
			{
				final Long previousAddress = headMap.lastKey();
				final Definition previous = headMap.get( previousAddress );
				if ( address - previousAddress < (long)previous.getLength() )
				{
					result = previous;
				}
			}
		}
		return result;
	}

	public void addDefinition( final Definition definition )
	{
		_definitions.put( definition.getAddress(), definition );
	}

	public void removeDefinition( final Definition definition )
	{
		_definitions.remove( definition.getAddress() );
	}

	public abstract long getStart();

	public abstract long getEnd();

	public long getLength()
	{
		return getEnd() - getStart() + 1L;
	}

	public abstract boolean hasPrevious();

	public abstract boolean hasNext();

	public abstract void previous();

	public abstract void next();
}
