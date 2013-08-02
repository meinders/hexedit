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
 * Shows the presence of a {@link Record} and provides access to it.
 *
 * @author Gerrit Meinders
 */
public class RecordDefinition
implements Definition
{
	private String _label;

	private Record _record;

	/**
	 * Constructs a new instance.
	 */
	public RecordDefinition( final String label, final Record record )
	{
		_label = label;
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
		return _record.getStart();
	}

	@Override
	public int getLength()
	{
		return (int)Math.min( (long)Integer.MAX_VALUE, _record.getLength() );
	}

	@Override
	public void use( final View view )
	throws IOException
	{
		final ViewModel viewModel = view.getViewModel();
		viewModel.setRecord( _record );
	}

	@Override
	public boolean isLink()
	{
		return true;
	}
}
