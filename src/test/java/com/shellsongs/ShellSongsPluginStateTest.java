package com.shellsongs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Exercises the follow-along state machine without RuneLite's injector; only the plain methods
 * that the panel and the menu-click handler call are used.
 */
public class ShellSongsPluginStateTest
{
	private ShellSongsPlugin plugin;
	private Song song;

	@Before
	public void setUp() throws Exception
	{
		plugin = new ShellSongsPlugin();
		song = SongParser.parseLine("Test: E D C", false);
		plugin.selectSong(song);
	}

	@Test
	public void startsAtFirstNote()
	{
		PlaybackState state = plugin.getState();
		assertEquals(song, state.getSong());
		assertEquals(0, state.getPosition());
		assertEquals(ShellNote.E, state.nextNote());
		assertNull(state.getFeedback());
	}

	@Test
	public void correctNotesAdvanceAndFinish()
	{
		plugin.onShellPlayed(ShellNote.E, false, false);
		assertEquals(ShellNote.D, plugin.getState().nextNote());

		plugin.onShellPlayed(ShellNote.D, false, false);
		plugin.onShellPlayed(ShellNote.C, false, false);

		PlaybackState done = plugin.getState();
		assertTrue(done.isFinished());
		assertFalse(done.isFeedbackError());
		assertTrue(done.getFeedback().contains("whole song"));
	}

	@Test
	public void wrongNoteDoesNotAdvanceByDefault()
	{
		plugin.onShellPlayed(ShellNote.A_SHARP, false, false);

		PlaybackState state = plugin.getState();
		assertEquals(0, state.getPosition());
		assertTrue(state.isFeedbackError());
		assertTrue(state.getFeedback().contains("A#"));
		assertTrue(state.getFeedback().contains("E"));
	}

	@Test
	public void wrongNoteAdvancesWhenConfigured()
	{
		plugin.onShellPlayed(ShellNote.A_SHARP, true, false);

		PlaybackState state = plugin.getState();
		assertEquals(1, state.getPosition());
		assertTrue(state.isFeedbackError());
	}

	@Test
	public void playingAfterFinishRestartsSong()
	{
		plugin.jumpToNote(song.size());
		assertTrue(plugin.getState().isFinished());

		plugin.onShellPlayed(ShellNote.E, false, false);
		assertEquals(1, plugin.getState().getPosition());
	}

	@Test
	public void transportButtonsClampToSong()
	{
		plugin.previousNote();
		assertEquals(0, plugin.getState().getPosition());

		plugin.nextNote();
		plugin.nextNote();
		plugin.nextNote();
		plugin.nextNote();
		assertEquals(song.size(), plugin.getState().getPosition());

		plugin.jumpToNote(99);
		assertEquals(song.size(), plugin.getState().getPosition());
		plugin.jumpToNote(-5);
		assertEquals(0, plugin.getState().getPosition());

		plugin.nextNote();
		plugin.restartSong();
		assertEquals(0, plugin.getState().getPosition());
	}

	@Test
	public void loopingJumpsBackToStartAfterLastNote()
	{
		plugin.onShellPlayed(ShellNote.E, false, true);
		plugin.onShellPlayed(ShellNote.D, false, true);
		plugin.onShellPlayed(ShellNote.C, false, true);

		PlaybackState state = plugin.getState();
		assertFalse(state.isFinished());
		assertEquals(0, state.getPosition());
		assertEquals(ShellNote.E, state.nextNote());
		assertFalse(state.isFeedbackError());
		assertTrue(state.getFeedback().contains("Looping"));
	}

	@Test
	public void repeatCountFollowsRunsOfTheSameShell() throws Exception
	{
		Song run = SongParser.parseLine("Run: C C C D", false);
		plugin.selectSong(run);

		assertEquals(3, plugin.getState().nextNoteRepeat());
		plugin.onShellPlayed(ShellNote.C, false, false);
		assertEquals(2, plugin.getState().nextNoteRepeat());
		plugin.onShellPlayed(ShellNote.C, false, false);
		assertEquals(1, plugin.getState().nextNoteRepeat());
		plugin.onShellPlayed(ShellNote.C, false, false);
		assertEquals(ShellNote.D, plugin.getState().nextNote());
		assertEquals(1, plugin.getState().nextNoteRepeat());
		plugin.onShellPlayed(ShellNote.D, false, false);
		assertEquals(0, plugin.getState().nextNoteRepeat());
	}

	@Test
	public void shellPlayedWithoutSongIsIgnored()
	{
		plugin.selectSong(null);
		plugin.onShellPlayed(ShellNote.C, false, false);
		assertFalse(plugin.getState().hasSong());
	}
}
