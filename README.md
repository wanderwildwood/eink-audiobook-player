# eInk Audiobook Player

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE.md)

This is a personal fork of [Voice](https://github.com/PaulWoitaschek/Voice) by Paul Woitaschek, reskinned with Mudita's [MMD](https://github.com/mudita/MMD) design system for the Mudita Kompakt (4.3", 800x480, grayscale e-ink). It keeps Voice's audio engine and feature set — chapter navigation, sleep timer, bookmarks, playback speed, silence skipping, auto-rewind — and adapts the UI and a few behaviors for e-ink hardware:

- **No animation**: every screen transition, dialog, and button state change is an instant cut. E-ink ghosts on any animation, so none of the usual Compose motion is used.
- **Black on white**: pure black-on-white color scheme, no ripple effects.
- **Three library views**: books are shelved by **Author**, by **Genre**, or by **Status** (current, not started, finished), chosen under "Library view" in settings. Each shelf lists its books as text with durations — there is no grid and no flat alphabetical list.
- **No cover art**: covers are not drawn, downloaded, or read out of the files. A 4.3" 16-level grayscale panel renders them as mud, and dropping them removed the app's last reason to touch the network.
- **Nothing is set by dragging**: playback speed, skip amount, and auto-rewind are each set by a `−` and a `+` either side of the value. A slider needs the panel to track a moving thumb, which e-ink cannot do.
- **One row of playback controls**: lock, bookmark, skip silence, playback speed, volume boost and the sleep timer are all buttons in the toolbar, so what is on is visible without opening a menu to look. Volume boost is on or off at a fixed gain rather than a slider asking you to tune decibels against a narrator you are part way through.
- **A line that says what you just did**: lock, skip silence and volume boost are icons, and an icon does not say which way it was just flipped, so a line appears under the toolbar for a couple of seconds — "Volume boost on", "Controls locked" — and then clears. It reads the state rather than the tap, so a tap that changes nothing says nothing, and it appears and clears outright rather than fading, because a fade here is a run of full redraws that each leave a ghost.
- **Now Playing bar**: shows the currently loaded book instantly (not gated on a persisted playback position), pinned to the bottom of the library screens.
- **Sleep timer**: one duration, remembered between sessions, that a shake resets without touching the screen — with adjustable shake sensitivity and a soft chime to confirm the shake registered. It fades the volume down over the last minute, evenly in decibels, leaving time to notice. Pausing resets the countdown.
- **No network**: no analytics, no crash reporting, no remote config, no background network calls. The app does not hold the `INTERNET` permission at all.

## Install

Grab the latest APK from [Releases](https://github.com/wanderwildwood/eink-audiobook-player/releases), check it against the published `.sha256`, and sideload it (assets are named `eink-audiobook-player-v1.6.1.apk` and so on):

```sh
sha256sum -c eink-audiobook-player-<version>.apk.sha256
adb install eink-audiobook-player-<version>.apk
```

### Upgrading from an older copy needs an uninstall

Android will not install this over an older copy, and stops with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Uninstall first:

```sh
adb uninstall com.wanderwildwood.einkaudiobookplayer
adb install eink-audiobook-player-<version>.apk
```

**Uninstalling clears the app's data** — playback positions, bookmarks, and which folder your audiobooks are in. The audio files on your card are untouched. Updates after this one install normally.

### Building it yourself

```sh
./gradlew :app:assembleFreeRelease
```

With no keystore present the build falls back to the checked-in debug key, so a self-built APK will not upgrade an installed release either. To build one that does, put your own keystore at `signing/signing.keystore` with a `signing/signing.properties` beside it holding `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`; both are gitignored.

## License

Licensed under [GNU GPLv3](LICENSE.md), the same as upstream Voice. See [Voice](https://github.com/PaulWoitaschek/Voice) and [MMD](https://github.com/mudita/MMD) for the original projects this is built on.
