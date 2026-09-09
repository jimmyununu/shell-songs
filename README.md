# Shell Songs

A RuneLite plugin for the seven musical shells rewarded by **Crab Quest** (Old School RuneScape, 8 September 2026).

Pick a song in the sidebar and the plugin tells you which shell to play next. When you click **Sound** on the right shell it moves on to the next note, so you can play a tune for other players without memorising it.

![Shell Songs icon](icon.png)

## Features

- **Song book** with 22 built-in tunes that fit the seven notes (C D E F G A A#).
- **Follow along**: the panel advances when you play the correct shell and tells you which shell you hit when you play a wrong one.
- **Click counter**: when a note repeats, the panel says how many times in a row to click that shell and counts down as you go.
- **Loop**: tick "Loop song" in the panel (or the settings) to go straight back to the first note after the last, so short tunes keep playing.
- **Whole-song view**: every note of the song as a row of chips. Played notes dim, the next note glows. Click any chip to jump there.
- **Overlay**: a small on-screen box with the song title, the next shell, and the notes after it.
- **Inventory highlight**: an outline around the shell you should play next, with the number of times in a row to play it written on it.
- **Custom songs**: add your own in the plugin settings, one per line.
- **Transport buttons**: Restart, Back and Skip for stepping through manually.

## The shells

| Note | Item | Item ID |
| ---- | ---- | ------- |
| C | Shell C | 34591 |
| D | Shell D | 34592 |
| E | Shell E | 34593 |
| F | Shell F | 34594 |
| G | Shell G | 34595 |
| A | Shell A | 34596 |
| A# | Shell A# | 34597 |

All seven come from the **Shell collection** (item 34589), the Crab Quest reward. Unpack it to get the individual shells.

## Writing your own songs

Open the plugin's settings and type into **Custom songs**, one song per line:

```
Title: notes separated by spaces, with | between phrases
```

For example:

```
My Tune: E D C | E D C | C C C C D D D D | E D C
Another: F F F C D D C | A A G G F
```

- Notes are `C D E F G A A#`. Lower-case works, and `Bb` or `A♯` are accepted for A#.
- A `|` (or a comma) starts a new line in the panel. It does not affect playing.
- Lines starting with `#` are ignored, so you can leave notes to yourself.
- A line the plugin cannot read is listed in red in the panel with the reason.

The shells span a single F-major scale without a second octave, so a tune only works if it fits within C up to A# with no notes outside that range. Several of the built-in songs are transposed or shortened for that reason and say so in their title.

## Plugin Hub compliance

This plugin is written to pass RuneLite's Plugin Hub review:

- Pure Java 11, no Kotlin or other JVM languages.
- No reflection, no native code, no external processes, no network access, no runtime downloads.
- No third-party dependencies beyond what the standard build provides (RuneLite client, Lombok).
- It never plays a shell, sends input, or edits menu entries. The only game event it reads is the user's own click on **Sound** on a shell item, and it uses that to move a cursor in the sidebar.
- Item IDs are hard-coded in `ShellNote.java`. Nothing is user-supplied by ID.
- Resources are read with `getResourceAsStream` through `ImageUtil.loadImageResource`.

## Building and running locally

Requires JDK 11 or newer.

```
./gradlew build      # compiles and runs the unit tests
./gradlew run        # launches RuneLite with the plugin loaded (developer mode)
```

On Windows use `gradlew.bat` instead of `./gradlew`.

## Submitting to the Plugin Hub

1. Push this folder to a **public** GitHub repository.
2. Fork [runelite/plugin-hub](https://github.com/runelite/plugin-hub) and create a branch.
3. Add a file `plugins/shell-songs` to the fork containing:

   ```
   repository=https://github.com/<your-user>/shell-songs.git
   commit=<full 40-character commit hash>
   ```

4. Open a pull request and watch the build check.

To ship an update later, change the `commit=` line to the new hash.

## Project layout

```
src/main/java/com/shellsongs/
  ShellSongsPlugin.java      plugin entry point, playback state, event handling
  ShellSongsConfig.java      settings
  ShellSongsPanel.java       sidebar panel
  NoteSheetComponent.java    the wrapped note-chip view
  ShellSongsOverlay.java     on-screen next-note box
  ShellHighlightOverlay.java inventory outline
  ShellNote.java             the seven shells and their item IDs
  Song.java / SongParser.java / SongLibrary.java
                             song model, text format, built-in list
src/main/resources/com/shellsongs/icon.png   sidebar icon
src/test/java/com/shellsongs/  unit tests and the developer-mode launcher
```

## License

BSD 2-Clause. See [LICENSE](LICENSE).
