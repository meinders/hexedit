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
 * Assigns some meaning to a range of bytes.
 *
 * @author Gerrit Meinders
 */
public interface Definition
{
	/**
	 * Returns the label.
	 *
	 * @return Label.
	 */
	String getLabel();

	/**
	 * Returns the address where the definition starts, relative to the
	 * containing record.
	 *
	 * @return Address relative to the containing record.
	 */
	long getAddress();

	/**
	 * Returns the length of this definition.
	 *
	 * @return Length of the definition.
	 */
	int getLength();

	/**
	 * Returns whether this definition provides an implementation-specific
	 * action, i.e. whether the {@link #use} method does something.
	 *
	 * @return {@code true} if {@link #use} does something.
	 */
	boolean isLink();

	/**
	 * Performs an implementation-specific action.
	 *
	 * @param view View from which the action was triggered.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	void use( View view )
	throws IOException;
}
