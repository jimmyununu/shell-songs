package com.shellsongs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The built-in song list.
 * <p>
 * The shells cover a single F-major scale without an upper octave: C D E F G A A#.
 * Every built-in song therefore fits inside that range; a few well-known tunes are transposed
 * (Old MacDonald, Au Clair de la Lune, Seven Nation Army) or shortened (marked in the title)
 * to make them fit.
 */
public final class SongLibrary
{
	private static final String[] BUILT_IN = {
		"Scale practice: C D E F G A A# | A# A G F E D C",
		"Hot Cross Buns: E D C | E D C | C C C C D D D D | E D C",
		"Mary Had a Little Lamb: E D C D E E E | D D D E G G | E D C D E E E | E D D E D C",
		"Twinkle, Twinkle, Little Star: C C G G A A G | F F E E D D C | G G F F E E D | G G F F E E D | C C G G A A G | F F E E D D C",
		"Baa Baa Black Sheep: C C G G A A A A G | F F E E D D C | G G G G F F F F E E E E D | G G G G F F F F E E E E D | C C G G A A A A G | F F E E D D C",
		"London Bridge Is Falling Down: G A G F E F G | D E F | E F G | G A G F E F G | D G E C",
		"Jingle Bells (chorus): E E E | E E E | E G C D E | F F F F F E E E E D D E D G | E E E | E E E | E G C D E | F F F F F E E E E G G F D C",
		"Ode to Joy (main theme): E E F G G F E D | C C D E E D D | E E F G G F E D | C C D E D C C",
		"When the Saints Go Marching In: C E F G | C E F G | C E F G E C E D | E E D C C E G G F | E F G E C D C",
		"Old MacDonald Had a Farm: F F F C D D C | A A G G F | C F F F C D D C | A A G G F | C C F F F | C C F F F | F F F F F F F F F F F F F | F F F C D D C | A A G G F",
		"This Old Man: G E G | G E G | A G F E D E F | E F G C C C C C D E F G | G D D F E D C",
		"Oh! Susanna: C D E G G A G E | C D E E D C D | C D E G G A G E | C D E E D D C | F F A A G G E C D | C D E G G A G E | C D E E D D C",
		"Itsy Bitsy Spider: C C C D E E | E D C D E C | E E F G | G F E F G E | C C D E | E D C D E C | C C C D E E | E D C D E C",
		"Au Clair de la Lune: F F F G A G | F A G G F | F F F G A G | F A G G F | G G G G D D | G F E D C | F F F G A G | F A G G F",
		"Lightly Row: G E E | F D D | C D E F G G G | G E E | F D D | C E G G E E E | D D D D D E F | E E E E E F G | G E E | F D D | C E G G E E E",
		"Camptown Races (verse): G G E G A G E | E D | E D | G G E G A G E | D E D C",
		"Frere Jacques (first four lines): C D E C | C D E C | E F G | E F G | G A G F E C | G A G F E C",
		"Rain, Rain, Go Away: G E G G E | G G E A G E | G G E A G E | G E G G E",
		"Baby Shark: D E G G G G G G G | D E G G G G G G G | D E G G G G G G G | D E G",
		"We Wish You a Merry Christmas (chorus): C F F G F E D D | D G G A G F E C | C A A A# A G F D | C C D G E F",
		"O Christmas Tree (first lines): C F F F G A A A | G A A# E G F | C F F F G A A A | G A A# E G F",
		"Seven Nation Army (riff): D D F D C A# A | D D F D C A# A",
	};

	private static final List<Song> BUILT_IN_SONGS;

	static
	{
		List<Song> songs = new ArrayList<>();
		for (String line : BUILT_IN)
		{
			try
			{
				songs.add(SongParser.parseLine(line, false));
			}
			catch (SongParser.SongFormatException ex)
			{
				// A typo in the table above is a programming error; fail loudly so tests catch it.
				throw new IllegalStateException("Built-in song is malformed: " + line, ex);
			}
		}
		BUILT_IN_SONGS = Collections.unmodifiableList(songs);
	}

	private SongLibrary()
	{
	}

	public static List<Song> builtIn()
	{
		return BUILT_IN_SONGS;
	}

	/**
	 * Builds the full song list: built-in songs followed by the user's custom songs.
	 *
	 * @param customSongsText the raw "Custom songs" config text, may be null
	 * @param errors          receives a human-readable message for every custom line that failed to parse
	 */
	public static List<Song> all(String customSongsText, List<String> errors)
	{
		List<Song> songs = new ArrayList<>(BUILT_IN_SONGS);
		songs.addAll(SongParser.parseAll(customSongsText, true, errors));
		return songs;
	}
}
