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
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

/**
 * Displays the contents of a {@link DataModel} as a grid of {@link Tile}s,
 * each of which shows the value of a single byte.
 *
 * @author Gerrit Meinders
 */
public class View
extends JPanel
{
	/**
	 * Model backing the view.
	 */
	private ViewModel _viewModel;

	/**
	 * Currently active menu.
	 */
	private Menu _menu;

	/**
	 * Triggers an update of the view when its model changes.
	 */
	private Observer _viewModelObserver = new Observer()
	{
		@Override
		public void update( final Observable o, final Object arg )
		{
			repaint();
		}
	};

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
		if ( _viewModel != viewModel )
		{
			if ( _viewModel != null )
			{
				_viewModel.deleteObserver( _viewModelObserver );
			}
			_viewModel = viewModel;
			if ( viewModel != null )
			{
				viewModel.addObserver( _viewModelObserver );
			}
		}
	}

	public Menu getMenu()
	{
		return _menu;
	}

	public void setMenu( final Menu menu )
	{
		_menu = menu;
		repaint();
	}

	@Override
	protected void paintComponent( final Graphics g )
	{
		super.paintComponent( g );
		final Graphics2D g2 = (Graphics2D)g.create();

		g2.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON );
		g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		final ViewModel viewModel = _viewModel;

		final Graphics2D staticGraphics = (Graphics2D)g2.create();
		final AffineTransform transform = viewModel.getTransform();
		g2.transform( transform );

		final Font sourceFont = new Font( Font.SERIF, Font.ITALIC, 42 );
		final Font sourceDetailsFont = new Font( Font.SANS_SERIF, Font.PLAIN, 15 );
		final Font characterFont = new Font( Font.SERIF, Font.BOLD, 18 );
		final Font valueFont = new Font( Font.MONOSPACED, Font.PLAIN, 12 );
		final Font addressFont = new Font( Font.MONOSPACED, Font.PLAIN, 12 );

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
		final Color otherRecordBackground = new Color( 0xf8f8f8 );
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
		final float decimalY = viewModel.getTileSize() - (float)valueMetrics.getDescent();

		final Rectangle2D.Float viewBounds = Tools.inverseTransform( transform, new Rectangle2D.Float( 0.0f, 0.0f, (float)getWidth(), (float)getHeight() ) );

		final Record record = viewModel.getRecord();
		final Highlighter highlighter = viewModel.getHighlighter();

		final DefinitionMap definitions = record.getDefinitions();

		for ( final Tile tile : viewModel.getTiles() )
		{
			final Rectangle2D.Float bounds = tile.getBounds();
			if ( !viewBounds.intersects( bounds ) )
			{
				continue;
			}

			final long address = tile.getAddress();

			final Definition definition = definitions.get( address - record.getStart() );

			if ( tile.isSelected() )
			{
				g2.setColor( selectionBackground );
			}
			else if ( address >= record.getStart() && address <= record.getEnd() )
			{
				if ( definition != null && definition.isLink() )
				{
					g2.setColor( tipBackground );
				}
				else
				{
					final Color color = highlighter.getColor( tile.getAddress() );
					if ( color == null )
					{
						g2.setColor( charBackground );
					}
					else
					{
						g2.setColor( color );
					}
				}
			}
			else
			{
				g2.setColor( otherRecordBackground );
			}

			g2.fill( bounds );

			if ( address == record.getStart() )
			{
				g2.setColor( new Color( 0xbbdddd ) );
				g2.fill( createStartBracket( bounds ) );
			}
			else if ( definition != null && ( address == record.getStart() + definition.getAddress() ) )
			{
				g2.setColor( new Color( 0xcccccc ) );
				g2.fill( createStartBracket( bounds ) );
			}

			if ( address == record.getEnd() )
			{
				g2.setColor( new Color( 0xbbdddd ) );
				g2.fill( createEndBracket( bounds ) );
			}
			else if ( definition != null && ( address == record.getStart() + definition.getAddress() + (long)definition.getLength() - 1L ) )
			{
				g2.setColor( new Color( 0xcccccc ) );
				g2.fill( createEndBracket( bounds ) );
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
				final String addressLabel = Long.toString( address );
				final Rectangle2D stringBounds = addressMetrics.getStringBounds( addressLabel, g2 );
				g2.setColor( addressForeground );
				g2.setFont( addressFont );
				g2.drawString( addressLabel, -addressMargin -(float)stringBounds.getMaxX(), (float)bounds.getCenterY() - (float)stringBounds.getY() - (float)stringBounds.getHeight() / 2.0f );
			}
		}

		final long selectionEnd = viewModel.getSelectionEnd();
		if ( false && viewModel.getSelectionLength() > 0L )
		{
			final Tile startTile = viewModel.getTile( viewModel.getSelectionStart() );
			final Tile endTile = viewModel.getTile( selectionEnd );
			final Rectangle2D.Float startBounds = startTile.getBounds();
			final Rectangle2D.Float endBounds = endTile.getBounds();

			int column;
			float y = endBounds.y + endBounds.height;
			if ( startBounds.y == endBounds.y )
			{
				column = startTile.getColumn();
			}
			else
			{
				column = endTile.getColumn() - 7;
			}
			column = Math.max( 0, Math.min( viewModel.getColumns() - 8, column ) );
			float x = column * ( viewModel.getTileSize() + viewModel.getTilePadding() );

			g2.setColor( new Color( 0xcccccc ) );
			g2.fill( new Rectangle2D.Float( x, y, viewModel.getTileSize() * 8.0f + viewModel.getTilePadding() * 7.0f, viewModel.getTileSize() + viewModel.getTilePadding() ) );
			g2.setColor( new Color( 0x444444 ) );
			g2.setFont( sourceDetailsFont );
			drawString( g2, new Rectangle2D.Float( x, y + viewModel.getTilePadding(), viewModel.getTileSize() * 2.0f + viewModel.getTilePadding() * 1.0f, viewModel.getTileSize() ), 0.5f, 0.5f, "Integer" );
			drawString( g2, new Rectangle2D.Float( x + viewModel.getTileSize() * 2.0f + viewModel.getTilePadding() * 2.0f, y + viewModel.getTilePadding(), viewModel.getTileSize() * 2.0f + viewModel.getTilePadding() * 1.0f, viewModel.getTileSize() ), 0.5f, 0.5f, "Float" );

			g2.setColor( new Color( 0xaaaaaa ) );
			g2.fill( new Rectangle2D.Float( x, y + viewModel.getTileSize() + viewModel.getTilePadding(), viewModel.getTileSize() * 8.0f + viewModel.getTilePadding() * 7.0f, viewModel.getTileSize() + viewModel.getTilePadding() ) );
			g2.setColor( new Color( 0x444444 ) );
			g2.setFont( sourceDetailsFont );
			drawString( g2, new Rectangle2D.Float( x, y + viewModel.getTileSize() + 2.0f * viewModel.getTilePadding(), viewModel.getTileSize() * 2.0f + viewModel.getTilePadding() * 1.0f, viewModel.getTileSize() ), 0.5f, 0.5f, "Little-endian" );
			drawString( g2, new Rectangle2D.Float( x + viewModel.getTileSize() * 2.0f + viewModel.getTilePadding() * 2.0f, y + viewModel.getTileSize() + 2.0f * viewModel.getTilePadding(), viewModel.getTileSize() * 2.0f + viewModel.getTilePadding() * 1.0f, viewModel.getTileSize() ), 0.5f, 0.5f, "Big-endian" );
		}

		if ( viewBounds.getY() < 0.0 )
		{
			g2.setColor( headerBackground );
			g2.fill( new Rectangle2D.Float( viewBounds.x, Math.min( viewBounds.y, -viewBounds.height - viewModel.getTilePadding() ), viewBounds.width, viewBounds.height ) );

			final String dataSource = viewModel.getDataSourceName();
			g2.setColor( sourceForeground );
			g2.setFont( sourceFont );
			g2.drawString( dataSource, 0.0f, -viewModel.getTileSize() - viewModel.getTilePadding() );
		}

		if ( viewModel.getSelectionLength() > 0L )
		{
			final String selectionValue = viewModel.getSelectionValue();
			if ( selectionValue != null )
			{
				final Rectangle2D tipTextBounds = valueMetrics.getStringBounds( selectionValue, g2 );
				final Tile tile = viewModel.getTile( viewModel.getSelectionStart() );
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

		if ( _menu != null )
		{
			drawButtonBar( staticGraphics, _menu );
		}
	}

	private void drawButtonBar( final Graphics2D g, final Menu menu )
	{
		final ViewModel viewModel = _viewModel;
		final float barHeight = viewModel.getTileSize();

		final Rectangle2D.Float bar = new Rectangle2D.Float( 0.0f, getHeight() - 1.5f * barHeight, getWidth(), 1.5f * barHeight );
		g.setColor( menu.getBackground() );
		g.fill( bar );

		g.setColor( new Color( 0xffffff ) );
		g.setFont( new Font( Font.SANS_SERIF, Font.BOLD, 15 ) );
		drawString( g, new Rectangle2D.Float( 0.0f, getHeight() - 1.5f * barHeight, getWidth(), 0.5f * barHeight ), 0.5f, 0.5f, menu.getText() );

		int buttonColumn = 0;
		final String group = null;
		for ( final MenuItem menuItem : menu.getItems() )
		{
			final Rectangle2D.Float bounds = new Rectangle2D.Float( buttonColumn * ( viewModel.getTileSize() + viewModel.getTilePadding() ), bar.y + bar.height - viewModel.getTileSize(), menuItem.getWidth() * ( viewModel.getTileSize() + viewModel.getTilePadding() ) - viewModel.getTilePadding(), viewModel.getTileSize() );
			final Color buttonBackground = menuItem.getBackground();
			if ( buttonBackground != null )
			{
				g.setColor( buttonBackground );
				g.fill( bounds );
			}

			final Action action = menuItem.getAction();
			g.setColor( action != null && action.isEnabled() ? new Color( 0xffffff ) : new Color( 0x80ffffff, true ) );
			g.setFont( new Font( Font.SANS_SERIF, Font.BOLD, 15 ) );
			drawString( g, bounds, 0.5f, 0.5f, menuItem.getText() );

			buttonColumn += menuItem.getWidth();
		}
	}

	private void drawString( final Graphics2D g2, final Rectangle2D.Float bounds, final float horizontalAlignment, final float verticalAlignment, final String string )
	{
		final FontMetrics fontMetrics = g2.getFontMetrics();
		final float x = bounds.x + horizontalAlignment * ( bounds.width - fontMetrics.stringWidth( string ) );
		final float y = bounds.y + fontMetrics.getAscent() + verticalAlignment * ( bounds.height - fontMetrics.getHeight() );
		g2.drawString( string, x, y );
	}

	private Path2D.Float createStartBracket( final Rectangle2D.Float bounds )
	{
		final Path2D.Float startBracket = new Path2D.Float();
		startBracket.moveTo( bounds.x, bounds.y );
		startBracket.lineTo( bounds.x + _viewModel.getTilePadding(), bounds.y );
		startBracket.lineTo( bounds.x + _viewModel.getTilePadding(), bounds.y - _viewModel.getTilePadding() );
		startBracket.lineTo( bounds.x - _viewModel.getTilePadding(), bounds.y - _viewModel.getTilePadding() );
		startBracket.lineTo( bounds.x - _viewModel.getTilePadding(), bounds.y + bounds.height + _viewModel.getTilePadding() );
		startBracket.lineTo( bounds.x + _viewModel.getTilePadding(), bounds.y + bounds.height + _viewModel.getTilePadding() );
		startBracket.lineTo( bounds.x + _viewModel.getTilePadding(), bounds.y + bounds.height );
		startBracket.lineTo( bounds.x, bounds.y + bounds.height );
		startBracket.closePath();
		return startBracket;
	}

	private Path2D.Float createEndBracket( final Rectangle2D.Float bounds )
	{
		final Path2D.Float endBracket = new Path2D.Float();
		endBracket.moveTo( bounds.x + bounds.width, bounds.y );
		endBracket.lineTo( bounds.x + bounds.width - _viewModel.getTilePadding(), bounds.y );
		endBracket.lineTo( bounds.x + bounds.width - _viewModel.getTilePadding(), bounds.y - _viewModel.getTilePadding() );
		endBracket.lineTo( bounds.x + bounds.width + _viewModel.getTilePadding(), bounds.y - _viewModel.getTilePadding() );
		endBracket.lineTo( bounds.x + bounds.width + _viewModel.getTilePadding(), bounds.y + bounds.height + _viewModel.getTilePadding() );
		endBracket.lineTo( bounds.x + bounds.width - _viewModel.getTilePadding(), bounds.y + bounds.height + _viewModel.getTilePadding() );
		endBracket.lineTo( bounds.x + bounds.width - _viewModel.getTilePadding(), bounds.y + bounds.height );
		endBracket.lineTo( bounds.x + bounds.width, bounds.y + bounds.height );
		endBracket.closePath();
		return endBracket;
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

						if ( address == selectionStart )
						{
							_viewModel.setSelectionStart( address );
							_selectionStart = _viewModel.getSelectionEnd();
						}
						else if ( address == selectionEnd )
						{
							_selectionStart = _viewModel.getSelectionStart();
							_viewModel.setSelectionEnd( address );
						}
						else
						{
							_selectionStart = address;
							_viewModel.select( address, address );
						}
					}
				}
			} );
			_holdTimer.setRepeats( false );
		}

		@Override
		public void mousePressed( final MouseEvent e )
		{
			_dragStart = e.getPoint();
			if ( e.getY() < getHeight() - _viewModel.getTileSize() )
			{
				_holdTimer.restart();
			}
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
				}
			}
			else if ( _dragging || (float)_dragStart.distanceSq( e.getPoint() ) > _dragThreshold * _dragThreshold )
			{
				_dragging = true;
				final Point dragEnd = e.getPoint();
				_viewModel.moveView( _dragStart, dragEnd );
				_dragStart = dragEnd;
				_holdTimer.stop();
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
		public void mouseClicked( final MouseEvent e )
		{
			if ( _menu != null && e.getY() >= getHeight() - _viewModel.getTileSize() )
			{
				final int column = (int)( e.getX() / ( _viewModel.getTileSize() + _viewModel.getTilePadding() ) );
				final MenuItem item = _menu.getItem( column );
				if ( item != null )
				{
					final Action action = item.getAction();
					if ( action != null )
					{
						action.actionPerformed( null );
					}
				}
			}
			else
			{
				final Tile tile = _viewModel.getTile( e.getPoint() );
				if ( tile != null )
				{
					final Record record = _viewModel.getRecord();
					if ( record != null )
					{
						final DefinitionMap definitions = record.getDefinitions();
						final Definition definition = definitions.get( tile.getAddress() );
						if ( definition != null )
						{
							try
							{
								definition.use( View.this );
							}
							catch ( IOException e1 )
							{
								e1.printStackTrace(); // FIXME: Generated try-catch block.
							}
						}
					}
				}
			}
		}

		@Override
		public void mouseWheelMoved( final MouseWheelEvent e )
		{
			_viewModel.scale( e.getPoint(), (float)Math.pow( 0.5, e.getPreciseWheelRotation() / 3.0 ) );
		}
	}
}
