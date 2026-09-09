package com.shellsongs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

/**
 * Outlines the shell that should be played next in the inventory and writes on it how many
 * times in a row it has to be played.
 */
class ShellHighlightOverlay extends WidgetItemOverlay
{
	private static final Stroke OUTLINE = new BasicStroke(2f);
	private static final int BADGE_PADDING = 2;

	private final ShellSongsPlugin plugin;
	private final ShellSongsConfig config;

	@Inject
	ShellHighlightOverlay(ShellSongsPlugin plugin, ShellSongsConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (!config.highlightInventory())
		{
			return;
		}

		PlaybackState state = plugin.getState();
		ShellNote next = state.nextNote();
		if (next == null || next.getItemId() != itemId)
		{
			return;
		}

		Rectangle bounds = widgetItem.getCanvasBounds();
		if (bounds == null)
		{
			return;
		}

		Color color = config.highlightColor();
		Stroke oldStroke = graphics.getStroke();
		Font oldFont = graphics.getFont();
		Object oldAa = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Outline
		graphics.setStroke(OUTLINE);
		graphics.setColor(color);
		graphics.drawRoundRect(bounds.x - 1, bounds.y - 1, bounds.width + 1, bounds.height + 1, 6, 6);

		// Click-count badge in the bottom-right corner of the item
		int repeat = state.nextNoteRepeat();
		if (repeat > 0)
		{
			String text = "x" + repeat;
			graphics.setFont(FontManager.getRunescapeBoldFont());
			FontMetrics fm = graphics.getFontMetrics();
			int textWidth = fm.stringWidth(text);
			int badgeWidth = textWidth + BADGE_PADDING * 2;
			int badgeHeight = fm.getAscent() + BADGE_PADDING;
			int bx = bounds.x + bounds.width - badgeWidth;
			int by = bounds.y + bounds.height - badgeHeight;

			graphics.setColor(new Color(0, 0, 0, 190));
			graphics.fillRoundRect(bx, by, badgeWidth, badgeHeight, 4, 4);
			graphics.setColor(color);
			graphics.drawString(text, bx + BADGE_PADDING, by + fm.getAscent() - 1);
		}

		graphics.setFont(oldFont);
		graphics.setStroke(oldStroke);
		if (oldAa != null)
		{
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAa);
		}
	}
}
