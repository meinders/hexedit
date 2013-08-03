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
 * Points to a record at another location.
 *
 * @author Gerrit Meinders
 */
public class PointerDefinition
implements Definition
{
	private String _label;

	private long _address;

	private int _length;

	private Record _record;

	/**
	 * Constructs a new instance.
	 *
	 * @param address Address of the pointer, relative to the containing record.
	 * @param length  Length of the pointer.
	 */
	public PointerDefinition( final String label, final long address, final int length, final Record record )
	{
		if ( length < 1 || length > 8 )
		{
			throw new IllegalArgumentException( "Length must be between 1 and 8 bytes: " + length );
		}

		_label = label;
		_address = address;
		_length = length;
		_record = record;
	}

	@Override
	public String getLabel()
	{
		return _label;
	}

	@Override
	public long getAddress()
	{
		return _address;
	}

	@Override
	public int getLength()
	{
		return _length;
	}

	@Override
	public void use( final View view )
	throws IOException
	{
		final ViewModel viewModel = view.getViewModel();
		viewModel.setRecord( _record );
		viewModel.jumpTo( _record.getStart() );
	}

	@Override
	public boolean isLink()
	{
		return true;
	}
}
