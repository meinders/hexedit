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

import java.awt.geom.*;

/**
 * FIXME Need comment
 *
 * @author Gerrit Meinders
 */
public class Tools
{
	private static final char[] _hexDigits = "0123456789abcdef".toCharArray();

	public static String byteToHexString( final int b )
	{
		return new String( new char[] {
		_hexDigits[ ( b >> 4 ) & 0xf ], _hexDigits[ b & 0xf ]
		} );
	}

	public static Rectangle2D.Float transform( AffineTransform transform, Rectangle2D.Float rectangle )
	{
		final float xx = (float)transform.getScaleX();
		final float yy = (float)transform.getScaleY();

		rectangle.x = rectangle.x * xx + (float)transform.getTranslateX();
		rectangle.y = rectangle.y * yy + (float)transform.getTranslateY();
		rectangle.width *= xx;
		rectangle.height *= yy;

		return rectangle;
	}

	public static Rectangle2D.Float inverseTransform( AffineTransform transform, Rectangle2D.Float rectangle )
	{
		final float xx = (float)transform.getScaleX();
		final float yy = (float)transform.getScaleY();

		rectangle.x = ( rectangle.x - (float)transform.getTranslateX() ) / xx;
		rectangle.y = ( rectangle.y - (float)transform.getTranslateY() ) / yy;
		rectangle.width  /= xx;
		rectangle.height /= yy;

		return rectangle;
	}

	public static Point2D.Float inverseTransform( AffineTransform transform, Point2D point )
	{
		try
		{
			final Point2D.Float result = new Point2D.Float();
			transform.inverseTransform( point, result );
			return result;
		}
		catch ( NoninvertibleTransformException e )
		{
			throw new RuntimeException( e );
		}
	}
}
