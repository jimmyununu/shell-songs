package com.shellsongs;

import java.util.Locale;

/**
 * The seven musical shells rewarded by Crab Quest.
 * <p>
 * Item IDs are taken from the Old School RuneScape Wiki (8 September 2026 update).
 * They are hard-coded here because the RuneLite {@code ItemID} constants did not yet
 * include these items when this plugin was written.
 */
public enum ShellNote
{
	C("C", 34591),
	D("D", 34592),
	E("E", 34593),
	F("F", 34594),
	G("G", 34595),
	A("A", 34596),
	A_SHARP("A#", 34597);

	/** The packed "Shell collection" item that unpacks into the seven shells. */
	public static final int SHELL_COLLECTION_ITEM_ID = 34589;

	/** The inventory menu option that plays the shell. */
	public static final String SOUND_OPTION = "Sound";

	private final String label;
	private final int itemId;

	ShellNote(String label, int itemId)
	{
		this.label = label;
		this.itemId = itemId;
	}

	/** Short musical name, e.g. {@code A#}. */
	public String getLabel()
	{
		return label;
	}

	/** In-game item name, e.g. {@code Shell A#}. */
	public String getShellName()
	{
		return "Shell " + label;
	}

	public int getItemId()
	{
		return itemId;
	}

	/**
	 * @return the note for the given item id, or {@code null} if the item is not a musical shell
	 */
	public static ShellNote fromItemId(int itemId)
	{
		for (ShellNote note : values())
		{
			if (note.itemId == itemId)
			{
				return note;
			}
		}
		return null;
	}

	/**
	 * Parses a note token such as {@code C}, {@code a}, {@code A#}, {@code A♯} or {@code Bb}.
	 *
	 * @return the matching note, or {@code null} if the token is not recognised
	 */
	public static ShellNote parse(String token)
	{
		if (token == null)
		{
			return null;
		}

		String t = token.trim().toUpperCase(Locale.ROOT)
			.replace('♯', '#')
			.replace('♭', 'b');

		switch (t)
		{
			case "C":
				return C;
			case "D":
				return D;
			case "E":
				return E;
			case "F":
				return F;
			case "G":
				return G;
			case "A":
				return A;
			case "A#":
			case "AS":
			case "ASHARP":
			case "A-SHARP":
			case "BB":
			case "Bb":
			case "BFLAT":
			case "B-FLAT":
				return A_SHARP;
			default:
				return null;
		}
	}

	@Override
	public String toString()
	{
		return label;
	}
}
