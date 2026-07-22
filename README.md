# eInk Audiobook Player

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE.md)

**A minimalistic audiobook player built for e-ink Android devices.**

This is a personal fork of [Voice](https://github.com/PaulWoitaschek/Voice) by Paul Woitaschek, reskinned with Mudita's [MMD](https://github.com/mudita/MMD) design system for the Mudita Kompakt (4.3", 800x480, grayscale e-ink). It keeps Voice's audio engine and feature set — chapter navigation, sleep timer, bookmarks, playback speed, silence skipping, auto-rewind — and adapts the UI and a few behaviors for e-ink hardware:

- **Zero-animation UI**: every screen transition, dialog, and button state change is an instant cut. E-ink ghosts on any animation, so none of the usual Compose motion is used.
- **Monochrome MMD theme**: pure black-on-white color scheme, no ripple effects.
- **Author-folder library browsing**: books are grouped by author folder with counts, instead of one flat alphabetical list.
- **Now Playing bar**: shows the currently loaded book instantly (not gated on a persisted playback position), pinned to the bottom of the library screens.
- **Simplified sleep timer**: a single default duration plus shake-to-extend, using the phone's real accelerometer.
- **No telemetry**: no analytics, no crash reporting, no remote config, no background network calls of any kind. The only network activity is an explicit, user-initiated cover art search ("cover from internet" on long-press).

## Download

Grab the latest APK from [Releases](https://github.com/wanderwildwood/eink-audiobook-player/releases) and sideload it with `adb install`. Every release is signed with the same fixed debug key (`app/debug.keystore`, checked into this repo — see [Releases workflow](.github/workflows/release.yml)), so installing a newer release over an older one works like a normal app update instead of requiring an uninstall first.

Or build it yourself with the bundled Gradle wrapper: `./gradlew assembleFreeDebug`.

## License

Licensed under [GNU GPLv3](LICENSE.md), the same as upstream Voice. See [Voice](https://github.com/PaulWoitaschek/Voice) and [MMD](https://github.com/mudita/MMD) for the original projects this is built on.
