package com.shellsongs;

import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Builds the real sidebar panel headlessly and pushes every kind of state through it, so layout or
 * painting mistakes fail here instead of inside the client.
 */
public class ShellSongsPanelTest
{
	@BeforeClass
	public static void headless()
	{
		System.setProperty("java.awt.headless", "true");
	}

	@Test
	public void panelSurvivesEveryState() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			ShellSongsPlugin plugin = new ShellSongsPlugin();
			ShellSongsPanel panel = new ShellSongsPanel(plugin);
			List<Song> songs = SongLibrary.builtIn();
			Song first = songs.get(0);

			panel.setSongs(songs, first, Collections.emptyList());
			panel.setLoop(true);
			panel.setLoop(false);
			panel.update(new PlaybackState(first, 0, null, false));
			panel.setSize(225, 800);
			panel.doLayout();

			panel.update(new PlaybackState(first, 3, "That was C, play D", true));
			panel.update(new PlaybackState(first, first.size(), "Nice!", false));
			panel.update(PlaybackState.EMPTY);
			panel.setSongs(songs, null, Collections.singletonList("Line 2: 'Bad': unknown note 'B'"));

			// Switching to the longest song must not throw and must give the sheet real height.
			Song longest = songs.get(0);
			for (Song song : songs)
			{
				if (song.size() > longest.size())
				{
					longest = song;
				}
			}
			panel.update(new PlaybackState(longest, 1, null, false));
		});
	}

	@Test
	public void noteSheetWrapsLongPhrases() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			NoteSheetComponent sheet = new NoteSheetComponent(i ->
			{
			});
			Song song = null;
			try
			{
				// 20 notes in one phrase: wider than the sidebar, so it must wrap onto a second row.
				song = SongParser.parseLine("Long: C C C C C C C C C C C C C C C C C C C C", false);
			}
			catch (SongParser.SongFormatException e)
			{
				throw new AssertionError(e);
			}

			sheet.setSong(song);
			Dimension pref = sheet.getPreferredSize();
			assertEquals(213, pref.width);
			assertTrue("expected at least two rows, got height " + pref.height, pref.height > 22 * 2);

			sheet.setSong(null);
			assertEquals(0, sheet.getPreferredSize().height);
		});
	}
}
