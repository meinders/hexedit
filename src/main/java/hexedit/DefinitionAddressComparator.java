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
 * Compares definitions by address in ascending order.
 *
 * @author Gerrit Meinders
 */
public class DefinitionAddressComparator
implements Comparator<Definition>
{
	/**
	 * Shared instance of the comparator.
	 */
	public static final DefinitionAddressComparator INSTANCE = new DefinitionAddressComparator();

	@Override
	public int compare( final Definition o1, final Definition o2 )
	{
		return Long.compare( o1.getAddress(), o2.getAddress() );
	}
}
