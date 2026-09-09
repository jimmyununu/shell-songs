package com.shellsongs;

/**
 * An immutable snapshot of where the player is in the selected song. Produced on the client thread
 * or the Swing thread and safely readable from either.
 */
public final class PlaybackState
{
	public static final PlaybackState EMPTY = new PlaybackState(null, 0, null, false);

	private final Song song;
	private final int position;
	private final String feedback;
	private final boolean feedbackIsError;

	public PlaybackState(Song song, int position, String feedback, boolean feedbackIsError)
	{
		this.song = song;
		this.position = position;
		this.feedback = feedback;
		this.feedbackIsError = feedbackIsError;
	}

	/** The selected song, or null when none is selected. */
	public Song getSong()
	{
		return song;
	}

	/** Index of the next note to play. Equal to {@code song.size()} when the song is finished. */
	public int getPosition()
	{
		return position;
	}

	/** Short message to show under the next note, or null. */
	public String getFeedback()
	{
		return feedback;
	}

	public boolean isFeedbackError()
	{
		return feedbackIsError;
	}

	public boolean hasSong()
	{
		return song != null && song.size() > 0;
	}

	public boolean isFinished()
	{
		return hasSong() && position >= song.size();
	}

	/** The note to play next, or null when there is no song or the song is finished. */
	public ShellNote nextNote()
	{
		return hasSong() ? song.noteAt(position) : null;
	}

	/**
	 * How many times in a row the next shell must be clicked before the note changes.
	 * 0 when there is no next note.
	 */
	public int nextNoteRepeat()
	{
		return hasSong() ? song.repeatCountAt(position) : 0;
	}
}
