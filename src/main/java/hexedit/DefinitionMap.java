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
 * Provides fast access to definitions when performing many look-ups, at the
 * cost of an initial sort.
 *
 * <p>When definitions contained in the map are modified, {@link #update()} must
 * be called before using the map. This is needed because the address of a
 * definition have changed, which potentially changes the ordering of the map.
 *
 * @author Gerrit Meinders
 */
public class DefinitionMap
{
	/**
	 * Definitions in the map, sorted by address.
	 */
	private final List<Definition> _definitions;

	/**
	 * Reused key instance.
	 */
	private Key _key = new Key();

	/**
	 * Constructs a new instance for the given definitions.
	 *
	 * @param definitions Definitions to provide access to.
	 */
	public DefinitionMap( final Collection<Definition> definitions )
	{
		_definitions = new ArrayList<Definition>( definitions );
		update();
	}

	/**
	 * Updates the helper to any changes to definition addresses.
	 */
	public void update()
	{
		Collections.sort( _definitions, DefinitionAddressComparator.INSTANCE );
	}

	/**
	 * Returns the definition that applies to the given address, relative to
	 * the same record as the definitions in this map.
	 *
	 * @param address Address to look up.
	 *
	 * @return Definition for the given address, if any.
	 */
	public Definition get( final long address )
	{
		final Key key = _key;
		key.setAddress( address );

		final int index = Collections.binarySearch( _definitions, key, DefinitionAddressComparator.INSTANCE );
		if ( index >= 0 )
		{
			return _definitions.get( index );
		}
		else if ( index == -1 )
		{
			return null;
		}
		else
		{
			final int insertionPoint = -index - 1;
			final Definition previous = _definitions.get( insertionPoint - 1 );
			return address - previous.getAddress() < (long)previous.getLength() ? previous : null;
		}
	}

	/**
	 * Key object used to call {@link Collections#binarySearch}.
	 */
	private static class Key
	implements Definition
	{
		private long _address = 0L;

		@Override
		public String getLabel()
		{
			return null;
		}

		@Override
		public long getAddress()
		{
			return _address;
		}

		private void setAddress( final long address )
		{
			_address = address;
		}

		@Override
		public int getLength()
		{
			return 0;
		}

		@Override
		public void use( final View view )
		throws IOException
		{
		}

		@Override
		public boolean isLink()
		{
			return false;
		}
	}
}
