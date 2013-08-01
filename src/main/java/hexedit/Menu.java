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

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A menu from which the user can choose actions to perform.
 *
 * @author Gerrit Meinders
 */
public class Menu
extends MenuItem
{
	private List<MenuItem> _items = new ArrayList<MenuItem>();

	public Menu( final int width, final Color background )
	{
		super( null, width );
		setBackground( background );
	}

	public List<MenuItem> getItems()
	{
		return _items;
	}

	public void setItems( final List<MenuItem> items )
	{
		_items = items;
	}

	public MenuItem getItem( int column )
	{
		int start = 0;
		for ( final MenuItem item : _items )
		{
			start += item.getWidth();
			if ( start > column )
			{
				return item;
			}
		}
		return null;
	}
}
