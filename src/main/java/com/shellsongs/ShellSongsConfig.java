package com.shellsongs;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(ShellSongsConfig.GROUP)
public interface ShellSongsConfig extends Config
{
	String GROUP = "shellsongs";
	String KEY_CUSTOM_SONGS = "customSongs";
	String KEY_LOOP = "loopSong";

	@ConfigSection(
		name = "Playing",
		description = "How the plugin follows along while you play",
		position = 0
	)
	String playingSection = "playing";

	@ConfigSection(
		name = "Display",
		description = "On-screen overlay and inventory highlight",
		position = 1
	)
	String displaySection = "display";

	@ConfigSection(
		name = "Custom songs",
		description = "Add your own songs to the list",
		position = 2
	)
	String customSection = "custom";

	@ConfigItem(
		keyName = "autoAdvance",
		name = "Follow along automatically",
		description = "Move to the next note when you click 'Sound' on the correct shell",
		section = playingSection,
		position = 0
	)
	default boolean autoAdvance()
	{
		return true;
	}

	@ConfigItem(
		keyName = "advanceOnAnyShell",
		name = "Advance on wrong notes too",
		description = "Move to the next note even when you play a different shell than expected",
		section = playingSection,
		position = 1
	)
	default boolean advanceOnAnyShell()
	{
		return false;
	}

	@ConfigItem(
		keyName = KEY_LOOP,
		name = "Loop song",
		description = "Go straight back to the first note after the last one, so short songs keep playing",
		section = playingSection,
		position = 2
	)
	default boolean loopSong()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show next-note overlay",
		description = "Show the song title and the upcoming notes in an on-screen overlay",
		section = displaySection,
		position = 0
	)
	default boolean showOverlay()
	{
		return true;
	}

	@Range(min = 1, max = 16)
	@ConfigItem(
		keyName = "overlayNoteCount",
		name = "Upcoming notes in overlay",
		description = "How many notes after the next one to show in the overlay",
		section = displaySection,
		position = 1
	)
	default int overlayNoteCount()
	{
		return 6;
	}

	@ConfigItem(
		keyName = "highlightInventory",
		name = "Highlight next shell",
		description = "Draw an outline around the shell you should play next in your inventory",
		section = displaySection,
		position = 2
	)
	default boolean highlightInventory()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight colour",
		description = "Colour of the inventory outline",
		section = displaySection,
		position = 3
	)
	default Color highlightColor()
	{
		return new Color(220, 138, 0);
	}

	@ConfigItem(
		keyName = KEY_CUSTOM_SONGS,
		name = "Custom songs",
		description = "One song per line, for example:  My Song: E D C | E D C | C C C C D D D D | E D C"
			+ "  Notes are C D E F G A A# (Bb also works). Use | to start a new line in the panel.",
		section = customSection,
		position = 0
	)
	default String customSongs()
	{
		return "";
	}
}
