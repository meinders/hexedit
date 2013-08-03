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

import junit.framework.*;

/**
 * FIXME Need comment
 *
 * @author Gerrit Meinders
 */
public class TestDefinitionMap
extends TestCase
{
	public void testGet()
	{
		final DefinitionImpl definition1 = new DefinitionImpl( 10L, 4 );
		final DefinitionImpl definition2 = new DefinitionImpl( 15L, 2 );
		final DefinitionImpl definition3 = new DefinitionImpl( 17L, 10 );
		final List<Definition> definitions = Arrays.<Definition>asList( definition1, definition2, definition3 );
		final DefinitionMap definitionMap = new DefinitionMap( definitions );

		assertNull( definitionMap.get( 9L ) );
		assertSame( definition1, definitionMap.get( 10L ) );
		assertSame( definition1, definitionMap.get( 11L ) );
		assertSame( definition1, definitionMap.get( 13L ) );
		assertNull( definitionMap.get( 14L ) );
		assertSame( definition2, definitionMap.get( 15L ) );
		assertSame( definition2, definitionMap.get( 16L ) );
		assertSame( definition3, definitionMap.get( 17L ) );
		assertSame( definition3, definitionMap.get( 25L ) );
		assertSame( definition3, definitionMap.get( 26L ) );
		assertNull( definitionMap.get( 27L ) );
	}
}
