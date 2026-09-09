package com.shellsongs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

/**
 * Sidebar panel: pick a song, see the next shell to play, and follow the whole song note by note.
 * All methods must be called on the Swing event thread.
 */
class ShellSongsPanel extends PluginPanel
{
	private static final String NO_SONG = "-";
	private static final String HTML_WIDTH = "<html><body style='width: 190px'>";

	private final ShellSongsPlugin plugin;

	private final JComboBox<Song> songBox = new JComboBox<>();
	private final JLabel nextNoteLabel = new JLabel(NO_SONG, SwingConstants.CENTER);
	private final JLabel nextShellLabel = new JLabel("Pick a song", SwingConstants.CENTER);
	private final JLabel repeatLabel = new JLabel(" ", SwingConstants.CENTER);
	private final JLabel progressLabel = new JLabel(" ", SwingConstants.CENTER);
	private final JCheckBox loopBox = new JCheckBox("Loop song");
	private final JLabel feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
	private final JLabel errorsLabel = new JLabel();
	private final NoteSheetComponent sheet;

	/** Set while the combo box or loop box is being updated from code so their listeners do not fire. */
	private boolean updatingSongs;
	private boolean updatingLoop;

	ShellSongsPanel(ShellSongsPlugin plugin)
	{
		super();
		this.plugin = plugin;

		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout());

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Title
		JLabel title = new JLabel("Shell Songs");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(title);
		content.add(Box.createVerticalStrut(8));

		// Song picker
		songBox.setFocusable(false);
		songBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		songBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		songBox.setPreferredSize(new Dimension(PANEL_WIDTH - 2 * BORDER_OFFSET, 28));
		songBox.addActionListener(e ->
		{
			if (updatingSongs)
			{
				return;
			}
			plugin.selectSong((Song) songBox.getSelectedItem());
		});
		content.add(songBox);
		content.add(Box.createVerticalStrut(10));

		// Next-note card
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		card.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel nextCaption = new JLabel("Play next", SwingConstants.CENTER);
		nextCaption.setFont(FontManager.getRunescapeSmallFont());
		nextCaption.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		nextCaption.setAlignmentX(Component.CENTER_ALIGNMENT);
		card.add(nextCaption);

		nextNoteLabel.setFont(FontManager.getRunescapeBoldFont().deriveFont(40f));
		nextNoteLabel.setForeground(ColorScheme.BRAND_ORANGE);
		nextNoteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		card.add(nextNoteLabel);

		nextShellLabel.setFont(FontManager.getRunescapeFont());
		nextShellLabel.setForeground(Color.WHITE);
		nextShellLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		card.add(nextShellLabel);

		repeatLabel.setFont(FontManager.getRunescapeBoldFont());
		repeatLabel.setForeground(ColorScheme.BRAND_ORANGE);
		repeatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		card.add(repeatLabel);

		progressLabel.setFont(FontManager.getRunescapeSmallFont());
		progressLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		progressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		card.add(progressLabel);

		feedbackLabel.setFont(FontManager.getRunescapeSmallFont());
		feedbackLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		feedbackLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		card.add(feedbackLabel);

		content.add(card);
		content.add(Box.createVerticalStrut(8));

		// Transport buttons
		JPanel buttons = new JPanel(new GridLayout(1, 3, 4, 0));
		buttons.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
		buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
		buttons.add(makeButton("Restart", plugin::restartSong));
		buttons.add(makeButton("Back", plugin::previousNote));
		buttons.add(makeButton("Skip", plugin::nextNote));
		content.add(buttons);
		content.add(Box.createVerticalStrut(4));

		// Loop toggle (mirrors the "Loop song" setting)
		loopBox.setFont(FontManager.getRunescapeSmallFont());
		loopBox.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		loopBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
		loopBox.setFocusable(false);
		loopBox.setToolTipText("Go straight back to the first note after the last one");
		loopBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		loopBox.addActionListener(e ->
		{
			if (!updatingLoop)
			{
				plugin.setLoop(loopBox.isSelected());
			}
		});
		content.add(loopBox);
		content.add(Box.createVerticalStrut(6));

		// Whole-song sheet
		sheet = new NoteSheetComponent(plugin::jumpToNote);
		sheet.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(sheet);
		content.add(Box.createVerticalStrut(10));

		// Custom-song errors (hidden when there are none)
		errorsLabel.setFont(FontManager.getRunescapeSmallFont());
		errorsLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		errorsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		errorsLabel.setVisible(false);
		content.add(errorsLabel);

		// Help text
		JLabel help = new JLabel(HTML_WIDTH
			+ "Play the highlighted shell. The number on it is how many times in a row to play it. "
			+ "The panel moves on when you play the right shell. "
			+ "Click any note above to jump to it. "
			+ "Add your own songs in the plugin settings.</body></html>");
		help.setFont(FontManager.getRunescapeSmallFont());
		help.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		help.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(help);

		add(content, BorderLayout.NORTH);
	}

	private static JButton makeButton(String text, Runnable action)
	{
		JButton button = new JButton(text);
		button.setFont(FontManager.getRunescapeSmallFont());
		button.setFocusable(false);
		button.addActionListener(e -> action.run());
		return button;
	}

	/** Reflects the "Loop song" setting in the checkbox without firing its listener. */
	void setLoop(boolean loop)
	{
		updatingLoop = true;
		try
		{
			loopBox.setSelected(loop);
		}
		finally
		{
			updatingLoop = false;
		}
	}

	/** Replaces the song list, keeping {@code selected} selected if it is present. */
	void setSongs(List<Song> songs, Song selected, List<String> errors)
	{
		updatingSongs = true;
		try
		{
			songBox.removeAllItems();
			for (Song song : songs)
			{
				songBox.addItem(song);
			}
			if (selected == null)
			{
				songBox.setSelectedIndex(-1);
			}
			else
			{
				songBox.setSelectedItem(selected);
			}
		}
		finally
		{
			updatingSongs = false;
		}

		if (errors.isEmpty())
		{
			errorsLabel.setVisible(false);
			errorsLabel.setText("");
		}
		else
		{
			StringBuilder sb = new StringBuilder(HTML_WIDTH).append("<b>Custom songs:</b>");
			for (String error : errors)
			{
				sb.append("<br>").append(escapeHtml(error));
			}
			sb.append("</body></html>");
			errorsLabel.setText(sb.toString());
			errorsLabel.setVisible(true);
		}
	}

	/** Redraws everything from the current playback state. */
	void update(PlaybackState state)
	{
		Song song = state.getSong();

		if (song != songBox.getSelectedItem())
		{
			updatingSongs = true;
			try
			{
				if (song == null)
				{
					songBox.setSelectedIndex(-1);
				}
				else
				{
					songBox.setSelectedItem(song);
				}
			}
			finally
			{
				updatingSongs = false;
			}
		}

		if (!state.hasSong())
		{
			nextNoteLabel.setText(NO_SONG);
			nextNoteLabel.setForeground(ColorScheme.BRAND_ORANGE);
			nextShellLabel.setText("Pick a song");
			repeatLabel.setText(" ");
			progressLabel.setText(" ");
			feedbackLabel.setText(" ");
			sheet.setSong(null);
			revalidate();
			repaint();
			return;
		}

		sheet.setSong(song);
		sheet.setPosition(state.getPosition(), state.nextNoteRepeat());

		if (state.isFinished())
		{
			nextNoteLabel.setText("Done");
			nextNoteLabel.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			nextShellLabel.setText("Song complete!");
			repeatLabel.setText(" ");
			progressLabel.setText("Play any shell to start again");
		}
		else
		{
			ShellNote next = state.nextNote();
			int repeat = state.nextNoteRepeat();
			nextNoteLabel.setText(next.getLabel());
			nextNoteLabel.setForeground(ColorScheme.BRAND_ORANGE);
			nextShellLabel.setText(next.getShellName());
			repeatLabel.setText(repeat > 1 ? "Click " + repeat + " times in a row" : "Click once");
			progressLabel.setText("Note " + (state.getPosition() + 1) + " of " + song.size());
		}

		String feedback = state.getFeedback();
		feedbackLabel.setText(feedback == null ? " " : feedback);
		feedbackLabel.setForeground(state.isFeedbackError()
			? ColorScheme.PROGRESS_ERROR_COLOR
			: ColorScheme.LIGHT_GRAY_COLOR);

		revalidate();
		repaint();
	}

	private static String escapeHtml(String s)
	{
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
