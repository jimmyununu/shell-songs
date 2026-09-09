package com.shellsongs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.JComponent;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

/**
 * Paints the whole song as rows of note "chips". Each phrase starts a new row and long phrases wrap.
 * Played notes are dimmed, the next note is highlighted, and clicking a chip jumps to that note.
 */
class NoteSheetComponent extends JComponent
{
	private static final int CHIP_SIZE = 22;
	private static final int CHIP_GAP = 3;
	private static final int ROW_GAP = 5;
	private static final int PHRASE_GAP = 9;
	private static final int ARC = 6;

	private static final Color PLAYED_BG = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color PLAYED_FG = ColorScheme.MEDIUM_GRAY_COLOR;
	private static final Color NEXT_BG = ColorScheme.BRAND_ORANGE;
	private static final Color NEXT_FG = Color.BLACK;
	/** The rest of a run of repeated notes: same shell, still to be clicked. */
	private static final Color RUN_BG = new Color(150, 95, 0);
	private static final Color RUN_FG = Color.WHITE;
	private static final Color UPCOMING_BG = ColorScheme.MEDIUM_GRAY_COLOR;
	private static final Color UPCOMING_FG = Color.WHITE;

	private Song song;
	private int position;
	/** Number of notes from {@code position} that are the same shell (the "click N times" run). */
	private int runLength = 1;
	private final IntConsumer onNoteClicked;

	/** Chip bounds from the last layout pass, indexed by note position. Used for click hit-testing. */
	private final List<Rectangle> chipBounds = new ArrayList<>();
	private int layoutWidth = -1;
	private int layoutHeight = 0;

	NoteSheetComponent(IntConsumer onNoteClicked)
	{
		this.onNoteClicked = onNoteClicked;
		setOpaque(false);
		setFont(FontManager.getRunescapeSmallFont());
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (song == null)
				{
					return;
				}
				for (int i = 0; i < chipBounds.size(); i++)
				{
					if (chipBounds.get(i).contains(e.getPoint()))
					{
						onNoteClicked.accept(i);
						return;
					}
				}
			}
		});
	}

	void setSong(Song song)
	{
		if (this.song == song)
		{
			return;
		}
		this.song = song;
		this.position = 0;
		layoutWidth = -1;
		revalidate();
		repaint();
	}

	void setPosition(int position, int runLength)
	{
		this.position = position;
		this.runLength = Math.max(1, runLength);
		repaint();
	}

	private int availableWidth()
	{
		int w = getWidth();
		if (w <= 0)
		{
			// Not laid out yet: assume the usual sidebar width minus PluginPanel padding.
			w = PluginPanel.PANEL_WIDTH - 2 * PluginPanel.BORDER_OFFSET;
		}
		return w;
	}

	/** Recomputes chip positions for the given width. Cheap, so it may run on every paint. */
	private void layoutChips(int width)
	{
		if (width == layoutWidth)
		{
			return;
		}
		layoutWidth = width;
		chipBounds.clear();

		if (song == null)
		{
			layoutHeight = 0;
			return;
		}

		int perRow = Math.max(1, (width + CHIP_GAP) / (CHIP_SIZE + CHIP_GAP));
		int y = 0;
		boolean firstPhrase = true;
		for (List<ShellNote> phrase : song.getPhrases())
		{
			if (!firstPhrase)
			{
				y += PHRASE_GAP;
			}
			firstPhrase = false;

			int col = 0;
			for (int i = 0; i < phrase.size(); i++)
			{
				if (col == perRow)
				{
					col = 0;
					y += CHIP_SIZE + ROW_GAP;
				}
				int x = col * (CHIP_SIZE + CHIP_GAP);
				chipBounds.add(new Rectangle(x, y, CHIP_SIZE, CHIP_SIZE));
				col++;
			}
			y += CHIP_SIZE + ROW_GAP;
		}
		layoutHeight = Math.max(0, y - ROW_GAP);
	}

	@Override
	public Dimension getPreferredSize()
	{
		int width = availableWidth();
		layoutChips(width);
		return new Dimension(width, layoutHeight);
	}

	@Override
	public Dimension getMaximumSize()
	{
		Dimension pref = getPreferredSize();
		return new Dimension(Integer.MAX_VALUE, pref.height);
	}

	@Override
	public void invalidate()
	{
		layoutWidth = -1;
		super.invalidate();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (song == null)
		{
			return;
		}

		layoutChips(availableWidth());

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Font font = getFont();
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics();
		List<ShellNote> notes = song.getNotes();

		for (int i = 0; i < chipBounds.size() && i < notes.size(); i++)
		{
			Rectangle r = chipBounds.get(i);
			Color bg;
			Color fg;
			if (i < position)
			{
				bg = PLAYED_BG;
				fg = PLAYED_FG;
			}
			else if (i == position)
			{
				bg = NEXT_BG;
				fg = NEXT_FG;
			}
			else if (i < position + runLength)
			{
				bg = RUN_BG;
				fg = RUN_FG;
			}
			else
			{
				bg = UPCOMING_BG;
				fg = UPCOMING_FG;
			}

			g2.setColor(bg);
			g2.fillRoundRect(r.x, r.y, r.width, r.height, ARC, ARC);

			String label = notes.get(i).getLabel();
			int tx = r.x + (r.width - fm.stringWidth(label)) / 2;
			int ty = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
			g2.setColor(fg);
			g2.drawString(label, tx, ty);
		}

		g2.dispose();
	}
}
