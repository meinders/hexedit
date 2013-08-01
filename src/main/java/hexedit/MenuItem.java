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
import javax.swing.*;

/**
 * A single item from a menu.
 *
 * @author Gerrit Meinders
 */
public class MenuItem
{
	private Action _action;

	private int _width;

	private Color _background;

	public MenuItem( final Action action, final int width )
	{
		_action = action;
		_width = width;
	}

	public Action getAction()
	{
		return _action;
	}

	public void setAction( Action action )
	{
		_action = action;
	}

	public String getText()
	{
		return (String)_action.getValue( Action.NAME );
	}

	public int getWidth()
	{
		return _width;
	}

	public Color getBackground()
	{
		return _background;
	}

	public void setBackground( Color background )
	{
		_background = background;
	}
}
