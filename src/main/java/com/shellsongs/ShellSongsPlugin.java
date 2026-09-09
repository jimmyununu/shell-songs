package com.shellsongs;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

/**
 * Shell Songs: a song book for the seven musical shells from Crab Quest.
 * <p>
 * The plugin only watches which shell the player clicks "Sound" on and moves a cursor through the
 * selected song. It never plays shells, sends input, or changes menus.
 */
@Slf4j
@PluginDescriptor(
	name = "Shell Songs",
	description = "Shows which musical shells from Crab Quest to play, note by note, for a library of songs",
	tags = {"shell", "shells", "music", "song", "songs", "notes", "crab", "crab quest", "instrument", "sailing"}
)
public class ShellSongsPlugin extends Plugin
{
	@Inject
	private ShellSongsConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ShellSongsOverlay overlay;

	@Inject
	private ShellHighlightOverlay highlightOverlay;

	private ShellSongsPanel panel;
	private NavigationButton navButton;

	// Playback state. Guarded by "this" because it is touched from the client thread
	// (menu clicks, overlay rendering) and from the Swing thread (panel buttons).
	private Song currentSong;
	private int position;
	private String feedback;
	private boolean feedbackIsError;

	@Override
	protected void startUp()
	{
		panel = new ShellSongsPanel(this);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Shell Songs")
			.icon(icon)
			.priority(8)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		overlayManager.add(overlay);
		overlayManager.add(highlightOverlay);

		reloadSongs();
		final boolean loop = config.loopSong();
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.setLoop(loop);
			}
		});
		log.debug("Shell Songs started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(highlightOverlay);
		clientToolbar.removeNavigation(navButton);
		navButton = null;
		panel = null;

		synchronized (this)
		{
			currentSong = null;
			position = 0;
			feedback = null;
			feedbackIsError = false;
		}
		log.debug("Shell Songs stopped");
	}

	@Provides
	ShellSongsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ShellSongsConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!ShellSongsConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}

		if (ShellSongsConfig.KEY_CUSTOM_SONGS.equals(event.getKey()))
		{
			reloadSongs();
		}
		else if (ShellSongsConfig.KEY_LOOP.equals(event.getKey()))
		{
			final boolean loop = config.loopSong();
			SwingUtilities.invokeLater(() ->
			{
				if (panel != null)
				{
					panel.setLoop(loop);
				}
			});
		}
	}

	/** Called by the panel's Loop checkbox. Writes the setting so it persists and stays in sync. */
	void setLoop(boolean loop)
	{
		configManager.setConfiguration(ShellSongsConfig.GROUP, ShellSongsConfig.KEY_LOOP, loop);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!config.autoAdvance() || !event.isItemOp())
		{
			return;
		}
		if (!ShellNote.SOUND_OPTION.equals(event.getMenuOption()))
		{
			return;
		}

		ShellNote played = ShellNote.fromItemId(event.getItemId());
		if (played == null)
		{
			return;
		}

		onShellPlayed(played, config.advanceOnAnyShell(), config.loopSong());
	}

	// ---- State changes. Safe to call from any thread. ----

	/** Rebuilds the song list from the built-in library plus the custom-songs setting. */
	private void reloadSongs()
	{
		final List<String> errors = new ArrayList<>();
		final List<Song> loaded = SongLibrary.all(config.customSongs(), errors);

		final Song selected;
		synchronized (this)
		{
			// Keep the current song selected if a song with the same title still exists.
			Song keep = null;
			if (currentSong != null)
			{
				for (Song song : loaded)
				{
					if (song.getTitle().equals(currentSong.getTitle()) && song.isCustom() == currentSong.isCustom())
					{
						keep = song;
						break;
					}
				}
			}

			if (keep == null)
			{
				currentSong = null;
				position = 0;
				feedback = null;
				feedbackIsError = false;
			}
			else if (keep != currentSong)
			{
				// Same title but the notes may have been edited; clamp the cursor.
				currentSong = keep;
				position = Math.min(position, keep.size());
			}
			selected = currentSong;
		}

		final PlaybackState state = getState();
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.setSongs(loaded, selected, errors);
				panel.update(state);
			}
		});
	}

	void selectSong(Song song)
	{
		synchronized (this)
		{
			currentSong = song;
			position = 0;
			feedback = null;
			feedbackIsError = false;
		}
		refreshPanel();
	}

	void restartSong()
	{
		synchronized (this)
		{
			position = 0;
			feedback = null;
			feedbackIsError = false;
		}
		refreshPanel();
	}

	void nextNote()
	{
		synchronized (this)
		{
			if (currentSong != null && position < currentSong.size())
			{
				position++;
			}
			feedback = null;
			feedbackIsError = false;
		}
		refreshPanel();
	}

	void previousNote()
	{
		synchronized (this)
		{
			if (position > 0)
			{
				position--;
			}
			feedback = null;
			feedbackIsError = false;
		}
		refreshPanel();
	}

	void jumpToNote(int index)
	{
		synchronized (this)
		{
			if (currentSong != null)
			{
				position = Math.max(0, Math.min(index, currentSong.size()));
			}
			feedback = null;
			feedbackIsError = false;
		}
		refreshPanel();
	}

	/**
	 * Called when the player clicks "Sound" on a musical shell.
	 *
	 * @param played            the shell that was played
	 * @param advanceOnAnyShell whether a wrong note should still move the cursor forward
	 * @param loop              whether finishing the song should jump straight back to the start
	 */
	void onShellPlayed(ShellNote played, boolean advanceOnAnyShell, boolean loop)
	{
		synchronized (this)
		{
			if (currentSong == null || currentSong.size() == 0)
			{
				return;
			}

			// After finishing a song, the next shell played starts it again.
			if (position >= currentSong.size())
			{
				position = 0;
			}

			ShellNote expected = currentSong.noteAt(position);
			if (played == expected || advanceOnAnyShell)
			{
				position++;
				if (played != expected)
				{
					feedback = "Played " + played.getLabel() + ", wanted " + expected.getLabel();
					feedbackIsError = true;
				}
				else if (position >= currentSong.size() && loop)
				{
					position = 0;
					feedback = "Whole song played. Looping from the top.";
					feedbackIsError = false;
				}
				else if (position >= currentSong.size())
				{
					feedback = "Nice! You played the whole song.";
					feedbackIsError = false;
				}
				else
				{
					feedback = null;
					feedbackIsError = false;
				}
			}
			else
			{
				feedback = "That was " + played.getLabel() + ", play " + expected.getLabel();
				feedbackIsError = true;
			}
		}
		refreshPanel();
	}

	/** @return an immutable snapshot of the playback state */
	synchronized PlaybackState getState()
	{
		return new PlaybackState(currentSong, position, feedback, feedbackIsError);
	}

	private void refreshPanel()
	{
		final PlaybackState state = getState();
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.update(state);
			}
		});
	}
}
