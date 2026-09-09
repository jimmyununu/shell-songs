package com.shellsongs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PlaybackStateTest
{
	@Test
	public void emptyStateHasNoSong()
	{
		assertFalse(PlaybackState.EMPTY.hasSong());
		assertFalse(PlaybackState.EMPTY.isFinished());
		assertNull(PlaybackState.EMPTY.nextNote());
	}

	@Test
	public void tracksNextNoteAndCompletion() throws Exception
	{
		Song song = SongParser.parseLine("Short: E D C", false);

		PlaybackState start = new PlaybackState(song, 0, null, false);
		assertTrue(start.hasSong());
		assertEquals(ShellNote.E, start.nextNote());
		assertFalse(start.isFinished());

		PlaybackState done = new PlaybackState(song, 3, "done", false);
		assertTrue(done.isFinished());
		assertNull(done.nextNote());
		assertEquals("done", done.getFeedback());
	}
}
