package com.shellsongs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Small on-screen box showing the selected song, the next note and the notes after it.
 */
class ShellSongsOverlay extends OverlayPanel
{
	private final ShellSongsPlugin plugin;
	private final ShellSongsConfig config;

	@Inject
	ShellSongsOverlay(ShellSongsPlugin plugin, ShellSongsConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay())
		{
			return null;
		}

		PlaybackState state = plugin.getState();
		if (!state.hasSong())
		{
			return null;
		}

		Song song = state.getSong();
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(song.getTitle())
			.color(Color.WHITE)
			.build());

		if (state.isFinished())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Song complete!")
				.leftColor(ColorScheme.PROGRESS_COMPLETE_COLOR)
				.build());
			return super.render(graphics);
		}

		ShellNote next = state.nextNote();
		int repeat = state.nextNoteRepeat();
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Next")
			.right(repeat > 1 ? next.getShellName() + "  x" + repeat : next.getShellName())
			.rightColor(ColorScheme.BRAND_ORANGE)
			.build());

		List<ShellNote> notes = song.getNotes();
		int from = state.getPosition() + repeat;
		int to = Math.min(notes.size(), from + config.overlayNoteCount());
		if (from < to)
		{
			StringBuilder upcoming = new StringBuilder();
			for (int i = from; i < to; i++)
			{
				if (upcoming.length() > 0)
				{
					upcoming.append(' ');
				}
				upcoming.append(notes.get(i).getLabel());
			}
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Then")
				.right(upcoming.toString())
				.build());
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Note")
			.right((state.getPosition() + 1) + " / " + song.size())
			.leftColor(ColorScheme.LIGHT_GRAY_COLOR)
			.rightColor(ColorScheme.LIGHT_GRAY_COLOR)
			.build());

		return super.render(graphics);
	}
}
