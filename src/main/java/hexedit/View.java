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

		setOpaque( true );
		setBackground( new Color( 0xeeeeee ) );
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

		final Font sourceFont = new Font( Font.SERIF, Font.ITALIC, 28 );
		final Font sourceDetailsFont = new Font( Font.SANS_SERIF, Font.PLAIN, 10 );
		final Font characterFont = new Font( Font.SERIF, Font.BOLD, 12 );
		final Font valueFont = new Font( Font.MONOSPACED, Font.PLAIN, 8 );
		final Font addressFont = new Font( Font.MONOSPACED, Font.PLAIN, 8 );

		final FontMetrics sourceMetrics = g2.getFontMetrics( sourceFont );
		final FontMetrics sourceDetailsMetrics = g2.getFontMetrics( sourceDetailsFont );
		final FontMetrics characterMetrics = g2.getFontMetrics( characterFont );
		final FontMetrics valueMetrics = g2.getFontMetrics( valueFont );
		final FontMetrics addressMetrics = g2.getFontMetrics( addressFont );

		final Color headerBackground = new Color( 0xbbdddd );
		final Color sourceForeground = new Color( 0x444444 );
		final Color sourceDetailsForeground = new Color( 0x888888 );

		final Color charForeground = new Color( 0x000000 );
		final Color addressForeground = new Color( 0x444444 );
		final Color charBackground = new Color( 0xffffff );
		final Color valueForeground = new Color( 0x008080 );
		final Color nullValueForeground = new Color( 0xaaaaaa );
		final Color selectionBackground = new Color( 0xbbdddd );
		final Color selectionBorderColor = new Color( 0x88cccc );

		final Color tipBackground = new Color( 0xffffcc );
		final Color tipBorderColor = new Color( 0xeeeeee );
		final Color tipForeground = new Color( 0x000000 );

		final float tipPadding = 2.0f;
		final float addressMargin = 10.0f;

		final float characterY = (float)characterMetrics.getAscent();
		final float decimalY = _viewModel.getTileSize() - (float)valueMetrics.getDescent();

		final Rectangle2D.Float viewBounds = Tools.inverseTransform( transform, new Rectangle2D.Float( 0.0f, 0.0f, (float)getWidth(), (float)getHeight() ) );

		for ( final Tile tile : _viewModel.getTiles() )
		{
			final Rectangle2D.Float bounds = tile.getBounds();
			if ( !viewBounds.intersects( bounds ) )
			{
				continue;
			}

			if ( tile.isSelected() )
			{
				g2.setColor( selectionBackground );
			}
			else
			{
				g2.setColor( charBackground  );
			}
			g2.fill( bounds );

			if ( tile.isSelectionAnchor() )
			{
				g2.setColor( selectionBackground );
				g2.draw( bounds );
			}

			if ( tile.isPrintable() )
			{
				g2.setFont( characterFont );
				final char c = tile.getCharacter();
				final int charWidth = characterMetrics.charWidth( c );
				g2.setColor( charForeground );
				g2.drawString( String.valueOf( c ), (float)bounds.getCenterX() - (float)charWidth / 2.0f, bounds.y + characterY );
			}

			final String value = tile.getHexadecimal();
			g2.setFont( valueFont );
			final int valueWidth = valueMetrics.stringWidth( value );
			g2.setColor( tile.getCharacter() == '\0' ? nullValueForeground : valueForeground );
			g2.drawString( value, (float)bounds.getCenterX() - (float)valueWidth / 2.0f, bounds.y + decimalY );

			if ( tile.getColumn() == 0 )
			{
				{
					final long address = tile.getAddress();
					final String addressLabel = Long.toString( address );
					final Rectangle2D stringBounds = addressMetrics.getStringBounds( addressLabel, g2 );
					g2.setColor( addressForeground );
					g2.setFont( addressFont );
					g2.drawString( addressLabel, -addressMargin -(float)stringBounds.getMaxX(), (float)bounds.getCenterY() - (float)stringBounds.getY() - (float)stringBounds.getHeight() / 2.0f );
				}
			}
		}

		if ( viewBounds.getY() < 0.0 )
		{
			g2.setColor( headerBackground );
			g2.fill( new Rectangle2D.Float( viewBounds.x, Math.min( viewBounds.y, -viewBounds.height - _viewModel.getTilePadding() ), viewBounds.width, viewBounds.height ) );

			final String dataSource = _viewModel.getDataSourceName();
			g2.setColor( sourceForeground );
			g2.setFont( sourceFont );
			g2.drawString( dataSource, 0.0f, -_viewModel.getTileSize() );

/*
					final String details = _viewModel.getDataSourceDetails();
					if ( details != null )
					{
						g2.setColor( sourceDetailsForeground );
						g2.setFont( sourceDetailsFont );
						g2.drawString( details, 0.0f, (float)sourceMetrics.getDescent() + sourceDetailsMetrics.getAscent() - _viewModel.getTileSize() );
					}
*/
		}

		if ( _viewModel.getSelectionLength() > 0L )
		{
			final String selectionValue = _viewModel.getSelectionValue();
			if ( selectionValue != null )
			{
				final Rectangle2D tipTextBounds = valueMetrics.getStringBounds( selectionValue, g2 );
				final Tile tile = _viewModel.getTile( _viewModel.getSelectionStart() );
				final Rectangle2D.Float tileBounds = tile.getBounds();

				g2.setColor( tipBackground );
				final Rectangle2D.Float tipBounds = new Rectangle2D.Float( tileBounds.x + (float)tipTextBounds.getMinX(), tileBounds.y + (float)tipTextBounds.getMinY() - 2.0f * tipPadding, (float)tipTextBounds.getWidth() + 2.0f * tipPadding, (float)tipTextBounds.getHeight() + 2.0f * tipPadding );
				g2.fill( tipBounds );
				g2.setColor( tipBorderColor );
				g2.draw( tipBounds );
				g2.setColor( tipForeground );
				g2.setFont( valueFont );
				g2.drawString( selectionValue, tileBounds.x + tipPadding, tileBounds.y - (float)valueMetrics.getDescent() );
			}
		}
	}

	private class MouseListenerImpl
		extends MouseAdapter
	{
		private final float _dragThreshold = 10.0f;

		private Point _dragStart;

		private boolean _dragging;

		private Timer _holdTimer;

		private long _selectionStart;

		private boolean _selecting;

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
						final long address = tile.getAddress();

						final long selectionStart = _viewModel.getSelectionStart();
						final long selectionEnd = _viewModel.getSelectionEnd();

						_selecting = true;

						if ( address == selectionStart - 1L )
						{
							_viewModel.setSelectionStart( address );
							_selectionStart = _viewModel.getSelectionEnd();
						}
						else if ( address == selectionEnd + 1L )
						{
							_selectionStart = _viewModel.getSelectionStart();
							_viewModel.setSelectionEnd( address );
						}
						else
						{
							_selectionStart = address;
							_viewModel.select( address, address );
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
			_holdTimer.restart();
		}

		@Override
		public void mouseDragged( final MouseEvent e )
		{
			if ( _selecting )
			{
				final Tile tile = _viewModel.getTile( e.getPoint() );
				if ( tile != null )
				{
					_viewModel.select( Math.min( tile.getAddress(), _selectionStart ), Math.max( tile.getAddress(), _selectionStart ) );
					repaint();
				}
			}
			else if ( _dragging || (float)_dragStart.distanceSq( e.getPoint() ) > _dragThreshold * _dragThreshold )
			{
				_dragging = true;
				final Point dragEnd = e.getPoint();
				_viewModel.moveView( _dragStart, dragEnd );
				_dragStart = dragEnd;
				_holdTimer.stop();
				repaint();
			}
		}

		@Override
		public void mouseReleased( final MouseEvent e )
		{
			_dragging = false;
			_holdTimer.stop();
			_selecting = false;
		}

		@Override
		public void mouseWheelMoved( final MouseWheelEvent e )
		{
			_viewModel.scale( e.getPoint(), (float)Math.pow( 0.5, e.getPreciseWheelRotation() / 3.0 ) );
			repaint();
		}
	}
}