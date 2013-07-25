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
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * FIXME Need comment
 *
 * @author Gerrit Meinders
 */
public class View
extends JPanel
{
	private ViewModel _viewModel;

	public View()
	{
		final MouseListenerImpl mouseListener = new MouseListenerImpl();
		addMouseListener( mouseListener );
		addMouseMotionListener( mouseListener );
		addMouseWheelListener( mouseListener );
	}

	public ViewModel getViewModel()
	{
		return _viewModel;
	}

	public void setViewModel( final ViewModel viewModel )
	{
		_viewModel = viewModel;
	}

	@Override
	protected void paintComponent( final Graphics g )
	{
		super.paintComponent( g );
		final Graphics2D g2 = (Graphics2D)g.create();

		g2.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON );
		g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );

		final AffineTransform transform = _viewModel.getTransform();
		g2.setTransform( transform );

		final Font characterFont = new Font( Font.SERIF, Font.BOLD, 12 );
		final Font valueFont = new Font( Font.MONOSPACED, Font.PLAIN, 8 );

		final FontMetrics characterMetrics = g2.getFontMetrics( characterFont );
		final FontMetrics valueMetrics = g2.getFontMetrics( valueFont );

		final Color charForeground = Color.BLACK;
		final Color charBackground = Color.WHITE;
		final Color valueForeground = new Color( 0x008080 );
		final Color nullValueForeground = new Color( 0xaaaaaa );
		final Color selectionBackground = new Color( 0xbbdddd );
		final Color selectionBorderColor = new Color( 0x88cccc );
		final Color tipBackground = new Color( 0xffffcc );
		final Color tipBorderColor = getBackground();
		final Color tipForeground = Color.BLACK;

		final float tipPadding = 2.0f;

		final float characterY = (float)characterMetrics.getAscent();
		final float decimalY = _viewModel.getTileSize() - (float)valueMetrics.getDescent();

		final Rectangle2D.Float viewBounds = Tools.inverseTransform( transform, new Rectangle2D.Float( 0.0f, 0.0f, (float)getWidth(), (float)getHeight() ) );

		int rendered = 0;
		final Tile tile = _viewModel.getTile();
		while ( tile.next() )
		{
			final Rectangle2D.Float bounds = tile.getBounds();
			if ( !viewBounds.intersects( bounds ) )
			{
				continue;
			}

			rendered++;

			final boolean selected = tile.isSelected();

			g2.setColor( selected ? selectionBackground : charBackground );
			g2.fill( bounds );

			if ( tile.isPrintable() )
			{
				g2.setFont( characterFont );
				final char c = tile.getCharacter();
				final int charWidth = characterMetrics.charWidth( c );
				g2.setColor( charForeground );
				g2.drawString( String.valueOf( c ), (float)bounds.getCenterX() - (float)charWidth / 2.0f, bounds.y + characterY );
			}

			final String decimal = tile.getUnsignedDecimal();
			final String hexadecimal = tile.getHexadecimal();

			final String value = hexadecimal;
			g2.setFont( valueFont );
			final int valueWidth = valueMetrics.stringWidth( value );
			g2.setColor( tile.getCharacter() == '\0' ? nullValueForeground : valueForeground );
			g2.drawString( value, (float)bounds.getCenterX() - (float)valueWidth / 2.0f, bounds.y + decimalY );
		}

		final Tile selectionStart = _viewModel.getSelectionStart();
		if ( selectionStart != null )
		{
			final String selectionValue = _viewModel.getSelectionValue();
			if ( selectionValue != null )
			{
				final Rectangle2D tipTextBounds = valueMetrics.getStringBounds( selectionValue, g2 );
				final Rectangle2D.Float tileBounds = selectionStart.getBounds();

				g2.setColor( tipBackground );
				final Rectangle2D.Double tipBounds = new Rectangle2D.Double( tileBounds.x + tipTextBounds.getMinX(), tileBounds.y + tipTextBounds.getMinY() - 2.0f * tipPadding, tipTextBounds.getWidth() + 2.0f * tipPadding, tipTextBounds.getHeight() + 2.0f * tipPadding );
				g2.fill( tipBounds );
				g2.setColor( tipBorderColor );
				g2.draw( tipBounds );
				g2.setColor( tipForeground );
				g2.setFont( valueFont );
				g2.drawString( selectionValue, tileBounds.x + tipPadding, tileBounds.y - valueMetrics.getDescent() );
			}
		}
	}

	private class MouseListenerImpl
		extends MouseAdapter
	{
		private Point _dragStart;

		private Timer _holdTimer;

		private boolean _selectedBeforePress;

		private MouseListenerImpl()
		{
			_holdTimer = new Timer( 300, new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					final Tile tile = _viewModel.getTile( _dragStart );
					if ( tile != null )
					{
						_selectedBeforePress = false;
						System.out.println( " - tile = " + tile );
						System.out.println( " - _viewModel.getSelectionEnd() = " + _viewModel.getSelectionEnd() );
						if ( tile.equals( _viewModel.getSelectionStart() ) )
						{
							_viewModel.setSelectionStart( _viewModel.getSelectionEnd() );
							_viewModel.setSelectionEnd( tile );
						}
						else if ( !tile.equals( _viewModel.getSelectionEnd() ) )
						{
							_viewModel.clearSelection();
							_viewModel.setSelectionStart( tile );
						}
						repaint();
					}
				}
			} );
			_holdTimer.setRepeats( false );
		}

		@Override
		public void mousePressed( final MouseEvent e )
		{
			_dragStart = e.getPoint();
			_selectedBeforePress = _viewModel.getSelectionStart() != null;
//			_viewModel.clearSelection();
			_holdTimer.restart();
		}

		@Override
		public void mouseReleased( MouseEvent e )
		{
			_selectedBeforePress = false;
			_holdTimer.stop();
		}

		@Override
		public void mouseDragged( final MouseEvent e )
		{
			if ( !_selectedBeforePress && _viewModel.getSelectionStart() != null )
			{
				final Tile tile = _viewModel.getTile( e.getPoint() );
				if ( tile != null )
				{
					_viewModel.setSelectionEnd( tile );
					repaint();
				}
			}
			else
			{
				final Point dragEnd = e.getPoint();
				_viewModel.moveView( _dragStart, dragEnd );
				_dragStart = dragEnd;
				_holdTimer.stop();
				repaint();
			}
		}

		@Override
		public void mouseWheelMoved( final MouseWheelEvent e )
		{
			_viewModel.scale( e.getPoint(), (float)Math.pow( 0.5, e.getPreciseWheelRotation() / 3.0 ) );
			repaint();
		}
	}
}