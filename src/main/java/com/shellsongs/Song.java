package com.shellsongs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An immutable song: a title plus an ordered list of phrases, each phrase being a list of notes.
 * Phrases only affect how the song is displayed (line breaks); playback is over the flat note list.
 */
public final class Song
{
	private final String title;
	private final boolean custom;
	private final List<List<ShellNote>> phrases;
	private final List<ShellNote> notes;

	public Song(String title, List<List<ShellNote>> phrases, boolean custom)
	{
		this.title = title;
		this.custom = custom;

		List<List<ShellNote>> phraseCopy = new ArrayList<>();
		List<ShellNote> flat = new ArrayList<>();
		for (List<ShellNote> phrase : phrases)
		{
			if (phrase.isEmpty())
			{
				continue;
			}
			phraseCopy.add(Collections.unmodifiableList(new ArrayList<>(phrase)));
			flat.addAll(phrase);
		}
		this.phrases = Collections.unmodifiableList(phraseCopy);
		this.notes = Collections.unmodifiableList(flat);
	}

	public String getTitle()
	{
		return title;
	}

	/** True when the song came from the user's "Custom songs" setting rather than the built-in list. */
	public boolean isCustom()
	{
		return custom;
	}

	public List<List<ShellNote>> getPhrases()
	{
		return phrases;
	}

	/** Every note of the song in playing order. */
	public List<ShellNote> getNotes()
	{
		return notes;
	}

	public int size()
	{
		return notes.size();
	}

	/**
	 * @return the note at the given position, or {@code null} if the position is past the end
	 */
	public ShellNote noteAt(int position)
	{
		if (position < 0 || position >= notes.size())
		{
			return null;
		}
		return notes.get(position);
	}

	/**
	 * Counts how many times in a row the note at {@code position} repeats, including itself.
	 * For "C C C D" at position 0 this is 3; at position 3 it is 1. Past the end it is 0.
	 */
	public int repeatCountAt(int position)
	{
		ShellNote note = noteAt(position);
		if (note == null)
		{
			return 0;
		}
		int count = 1;
		while (noteAt(position + count) == note)
		{
			count++;
		}
		return count;
	}

	@Override
	public String toString()
	{
		// Used by the song drop-down.
		return custom ? title + " (custom)" : title;
	}
}
