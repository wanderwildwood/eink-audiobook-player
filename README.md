# eInk Audiobook Player

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE.md)

**A minimalistic audiobook player built for e-ink Android devices.**

This is a personal fork of [Voice](https://github.com/PaulWoitaschek/Voice) by Paul Woitaschek, reskinned with Mudita's [MMD](https://github.com/mudita/MMD) design system for the Mudita Kompakt (4.3", 800x480, grayscale e-ink). It keeps Voice's audio engine and feature set — chapter navigation, sleep timer, bookmarks, playback speed, silence skipping, auto-rewind — and adapts the UI and a few behaviors for e-ink hardware:

- **Zero-animation UI**: every screen transition, dialog, and button state change is an instant cut. E-ink ghosts on any animation, so none of the usual Compose motion is used.
- **Monochrome MMD theme**: pure black-on-white color scheme, no ripple effects.
- **Three library views**: books are shelved by **Author**, by **Genre**, or by **Status** (current, not started, finished), chosen under "Library view" in settings. Each shelf lists its books as text with durations — there is no grid and no flat alphabetical list.
- **No cover art**: covers are not drawn, downloaded, or read out of the files. A 4.3" 16-level grayscale panel renders them as mud, and dropping them removed the app's last reason to touch the network.
- **Nothing is set by dragging**: playback speed, skip amount, and auto-rewind are each set by a `−` and a `+` either side of the value. A slider needs the panel to track a moving thumb, which e-ink cannot do.
- **Now Playing bar**: shows the currently loaded book instantly (not gated on a persisted playback position), pinned to the bottom of the library screens.
- **A sleep timer you can answer**: one duration, remembered between sessions, that a shake resets without touching the screen — with adjustable shake sensitivity and a soft chime to confirm the shake registered. It fades the volume down over the last minute, evenly in decibels, leaving time to notice. Pausing resets the countdown.
- **Fully offline**: no analytics, no crash reporting, no remote config, no background network calls. The app does not hold the `INTERNET` permission at all.

## Install

Grab the latest APK from [Releases](https://github.com/wanderwildwood/eink-audiobook-player/releases), check it against the published `.sha256`, and sideload it (assets are named `eink-audiobook-player-v1.6.0.apk` and so on):

```sh
sha256sum -c eink-audiobook-player-<version>.apk.sha256
adb install eink-audiobook-player-<version>.apk
```

### Upgrading from v1.5.1 or earlier requires an uninstall

Releases up to and including **v1.5.1** were signed with the debug keystore checked into this repo — a key whose private half is public, so anyone could have built an APK that upgraded your install in place. Every release from **v1.6.0** onward is signed with a private key held outside the repo.

Android refuses to replace an app with one signed by a different key, so installing across that boundary fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Uninstall first:

```sh
adb uninstall com.wanderwildwood.einkaudiobookplayer
adb install eink-audiobook-player-<version>.apk
```

**Uninstalling clears the app's data**, so playback positions, bookmarks, and settings do not survive it — the audio files on your card are untouched. This is a one-time break; upgrades after it work normally again.

### Building it yourself

```sh
./gradlew :app:assembleFreeRelease
```

With no keystore present the build falls back to the checked-in debug key, so a self-built APK will not upgrade an installed release either. To build one that does, put your own keystore at `signing/signing.keystore` with a `signing/signing.properties` beside it holding `STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`; both are gitignored.

## License

Licensed under [GNU GPLv3](LICENSE.md), the same as upstream Voice. See [Voice](https://github.com/PaulWoitaschek/Voice) and [MMD](https://github.com/mudita/MMD) for the original projects this is built on.
