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

/**
* Trivial implementation of {@link Definition} interface used for testing.
*
* @author Gerrit Meinders
*/
class DefinitionImpl
implements Definition
{
	private String _label;

	private long _address;

	private int _length;

	public DefinitionImpl( final long address, final int length )
	{
		_address = address;
		_length = length;
	}

	public String getLabel()
	{
		return _label;
	}

	public void setLabel( final String label )
	{
		_label = label;
	}

	public long getAddress()
	{
		return _address;
	}

	public void setAddress( final long address )
	{
		_address = address;
	}

	public int getLength()
	{
		return _length;
	}

	public void setLength( final int length )
	{
		_length = length;
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
