package com.shellsongs;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the plain-text song format used both for the built-in library and for user-defined songs.
 * <pre>
 * Title: E D C | E D C | C C C C D D D D | E D C
 * </pre>
 * Notes are separated by spaces. A {@code |} (or a comma) starts a new phrase, which is shown on
 * its own line in the panel. Tokens {@code -} and {@code _} are ignored so users can pad rhythm.
 * Blank lines and lines starting with {@code #} are ignored.
 */
public final class SongParser
{
	private SongParser()
	{
	}

	/** Thrown when a song line cannot be understood. The message is user-facing. */
	public static final class SongFormatException extends Exception
	{
		SongFormatException(String message)
		{
			super(message);
		}
	}

	/**
	 * Parses a single song line.
	 *
	 * @throws SongFormatException if the line has no title, has no notes, or contains an unknown note
	 */
	public static Song parseLine(String line, boolean custom) throws SongFormatException
	{
		if (line == null)
		{
			throw new SongFormatException("Empty line");
		}

		int colon = line.indexOf(':');
		if (colon < 0)
		{
			throw new SongFormatException("Missing ':' between the title and the notes");
		}

		String title = line.substring(0, colon).trim();
		if (title.isEmpty())
		{
			throw new SongFormatException("Missing song title before ':'");
		}

		String body = line.substring(colon + 1);
		List<List<ShellNote>> phrases = new ArrayList<>();
		List<ShellNote> current = new ArrayList<>();

		// Treat '|' and ',' as phrase separators even when glued to a note ("E|" or "C,").
		String spaced = body.replace("|", " | ").replace(",", " , ");
		for (String token : spaced.trim().split("\\s+"))
		{
			if (token.isEmpty())
			{
				continue;
			}
			if (token.equals("|") || token.equals(","))
			{
				if (!current.isEmpty())
				{
					phrases.add(current);
					current = new ArrayList<>();
				}
				continue;
			}
			if (token.equals("-") || token.equals("_"))
			{
				continue;
			}

			ShellNote note = ShellNote.parse(token);
			if (note == null)
			{
				throw new SongFormatException("'" + title + "': unknown note '" + token
					+ "' (use C D E F G A A#)");
			}
			current.add(note);
		}
		if (!current.isEmpty())
		{
			phrases.add(current);
		}

		if (phrases.isEmpty())
		{
			throw new SongFormatException("'" + title + "': no notes after ':'");
		}

		return new Song(title, phrases, custom);
	}

	/**
	 * Parses many song lines. Lines that fail are skipped and their error message (prefixed with the
	 * 1-based line number) is added to {@code errors}.
	 */
	public static List<Song> parseAll(String text, boolean custom, List<String> errors)
	{
		List<Song> songs = new ArrayList<>();
		if (text == null)
		{
			return songs;
		}

		String[] lines = text.split("\\r?\\n");
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i].trim();
			if (line.isEmpty() || line.startsWith("#"))
			{
				continue;
			}
			try
			{
				songs.add(parseLine(line, custom));
			}
			catch (SongFormatException ex)
			{
				errors.add("Line " + (i + 1) + ": " + ex.getMessage());
			}
		}
		return songs;
	}
}
