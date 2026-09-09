package com.shellsongs;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class SongParserTest
{
	@Test
	public void parsesTitleNotesAndPhrases() throws Exception
	{
		Song song = SongParser.parseLine("Hot Cross Buns: E D C | E D C | C C C C D D D D | E D C", false);

		assertEquals("Hot Cross Buns", song.getTitle());
		assertEquals(4, song.getPhrases().size());
		assertEquals(17, song.size());
		assertEquals(ShellNote.E, song.noteAt(0));
		assertEquals(ShellNote.C, song.noteAt(16));
		assertNull(song.noteAt(17));
		assertFalse(song.isCustom());
	}

	@Test
	public void acceptsNoteAliasesAndSeparators() throws Exception
	{
		Song song = SongParser.parseLine("Aliases: c d e f g a a# A♯ Bb B♭ , C| D - _", true);

		List<ShellNote> notes = song.getNotes();
		assertEquals(12, notes.size());
		assertEquals(ShellNote.A_SHARP, notes.get(6));
		assertEquals(ShellNote.A_SHARP, notes.get(7));
		assertEquals(ShellNote.A_SHARP, notes.get(8));
		assertEquals(ShellNote.A_SHARP, notes.get(9));
		assertEquals(3, song.getPhrases().size());
		assertTrue(song.isCustom());
	}

	@Test
	public void countsRepeatedNotes() throws Exception
	{
		Song song = SongParser.parseLine("Runs: C C C D | D E", false);

		assertEquals(3, song.repeatCountAt(0));
		assertEquals(2, song.repeatCountAt(1));
		assertEquals(1, song.repeatCountAt(2));
		// Runs continue across a phrase break, since the player just keeps clicking the same shell.
		assertEquals(2, song.repeatCountAt(3));
		assertEquals(1, song.repeatCountAt(5));
		assertEquals(0, song.repeatCountAt(6));
	}

	@Test
	public void rejectsUnknownNotes()
	{
		try
		{
			SongParser.parseLine("Bad: C D B", false);
			fail("expected a SongFormatException");
		}
		catch (SongParser.SongFormatException ex)
		{
			assertTrue(ex.getMessage().contains("'B'"));
		}
	}

	@Test
	public void rejectsMissingTitleOrNotes()
	{
		List<String> errors = new ArrayList<>();
		List<Song> songs = SongParser.parseAll("C D E\n: C D E\nTitle:\n\n# comment\nGood: C", true, errors);

		assertEquals(1, songs.size());
		assertEquals("Good", songs.get(0).getTitle());
		assertEquals(3, errors.size());
		assertTrue(errors.get(0).startsWith("Line 1:"));
		assertTrue(errors.get(1).startsWith("Line 2:"));
		assertTrue(errors.get(2).startsWith("Line 3:"));
	}

	@Test
	public void builtInLibraryIsValid()
	{
		List<Song> songs = SongLibrary.builtIn();
		assertFalse(songs.isEmpty());
		for (Song song : songs)
		{
			assertTrue(song.getTitle(), song.size() > 0);
			assertFalse(song.isCustom());
		}
	}

	@Test
	public void customSongsAreAppendedAfterBuiltIn()
	{
		List<String> errors = new ArrayList<>();
		List<Song> all = SongLibrary.all("Mine: A A# G", errors);

		assertTrue(errors.isEmpty());
		assertEquals(SongLibrary.builtIn().size() + 1, all.size());
		Song last = all.get(all.size() - 1);
		assertEquals("Mine", last.getTitle());
		assertTrue(last.isCustom());
		assertEquals("Mine (custom)", last.toString());
	}

	@Test
	public void shellNotesMapToItemIds()
	{
		assertSame(ShellNote.C, ShellNote.fromItemId(34591));
		assertSame(ShellNote.A_SHARP, ShellNote.fromItemId(34597));
		assertNull(ShellNote.fromItemId(ShellNote.SHELL_COLLECTION_ITEM_ID));
		assertEquals("Shell A#", ShellNote.A_SHARP.getShellName());
	}
}
