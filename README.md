# eInk Audiobook Player

An audiobook player for the [Mudita Kompakt](https://mudita.com/products/kompakt/).

Fork of [Voice](https://github.com/PaulWoitaschek/Voice) by Paul Woitaschek, with the UI
rebuilt on Mudita's [MMD](https://github.com/mudita/MMD) design system for the phone's 4.3",
800x480 grayscale panel. Voice's audio engine and features are all still here: chapter
navigation, sleep timer, bookmarks, playback speed, silence skipping, auto-rewind.

## What is different from Voice

Screens cut rather than animate. No transitions, no ripples, black on white — e-ink ghosts
on motion.

Books are shelved by author, by genre, or by status (current, not started, finished), chosen
under "Library view" in settings. Each shelf lists its books as text with durations. There is
no grid and no flat alphabetical list.

Cover art is not drawn, downloaded, or read out of the files. A 4.3" 16-level grayscale panel
renders it as mud, and dropping it removed the app's last reason to touch the network.

Playback speed, skip amount and auto-rewind are each set by a `−` and a `+` either side of
the value. A slider needs the panel to track a moving thumb, which e-ink cannot do.

Lock, bookmark, skip silence, playback speed, volume boost and the sleep timer are all
buttons in one toolbar row, so what is on is visible without opening a menu to look. Volume
boost is on or off at a fixed gain rather than a slider asking you to tune decibels against a
narrator you are part way through.

Flipping one of those puts a line under the toolbar for a couple of seconds — "Volume boost
on", "Controls locked" — and then clears. An icon does not say which way it was just flipped.
It reads the state rather than the tap, so a tap that changes nothing says nothing, and it
appears and clears outright rather than fading, because a fade here is a run of full redraws
that each leave a ghost.

The Now Playing bar shows the loaded book straight away, not gated on a persisted playback
position, pinned to the bottom of the library screens.

The sleep timer keeps one duration between sessions, and a shake resets it without touching
the screen — shake sensitivity is adjustable, and a soft chime confirms the shake registered.
It fades the volume down over the last minute, evenly in decibels, leaving time to notice.
Pausing resets the countdown.

The app does not hold the `INTERNET` permission at all. No analytics, no crash reporting, no
remote config, no background calls — there is no route off the phone.

## Installing

Download the APK from
[Releases](https://github.com/wanderwildwood/eink-audiobook-player/releases), check it against
the `.sha256` published beside it, and sideload it:

```sh
sha256sum -c eink-audiobook-player-<version>.apk.sha256
adb install eink-audiobook-player-<version>.apk
```

### Upgrading from an older copy needs an uninstall

Android will not install this over an older copy, and stops with
`INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Uninstall first:

```sh
adb uninstall com.wanderwildwood.einkaudiobookplayer
adb install eink-audiobook-player-<version>.apk
```

Uninstalling clears the app's data — playback positions, bookmarks, and which folder your
audiobooks are in. The audio files on your card are untouched. Updates after this one install
normally.

### Building it yourself

```sh
./gradlew :app:assembleFreeRelease
```

With no keystore present the build falls back to the checked-in debug key, so a self-built
APK will not upgrade an installed release either. To build one that does, put your own
keystore at `signing/signing.keystore` with a `signing/signing.properties` beside it holding
`STORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`; both are gitignored.

## Licence

GPLv3, the same as upstream Voice. See [LICENSE.md](LICENSE.md), and
[Voice](https://github.com/PaulWoitaschek/Voice) and [MMD](https://github.com/mudita/MMD) for
the original projects this is built on.
