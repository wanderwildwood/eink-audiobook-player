# Privacy Policy

This is a personal fork of [Voice](https://github.com/PaulWoitaschek/Voice), built for a single device. It collects no data of any kind.

## What this app does not do

- No analytics, no crash reporting, no remote config, no telemetry of any kind
- No account, no sign-up, no login
- No location access
- No advertising, no ad ID
- No background network activity

## What this app does

- Reads and writes audiobook files from a folder you choose via Android's standard folder picker (Storage Access Framework). Nothing outside that folder is accessed.
- Plays audio in a foreground media-playback service, so playback continues and shows media controls while the app is in the background.
- Makes a network request only when you explicitly choose to search for cover art online (long-press a book → "Cover from internet"). This queries DuckDuckGo's image search directly from your device; no other server is involved.

## Permissions

- `INTERNET` / `ACCESS_NETWORK_STATE` — used solely for the on-demand cover art search above.
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK` — required for background audio playback with media controls.
- `WAKE_LOCK` — keeps playback running while the screen is off.
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` — lets you exempt the app from battery-saving restrictions that would otherwise interrupt long audiobook playback.

## Changes

If this policy changes, this file will be updated in place — there's no separate notification mechanism, since there's no way to reach users of a self-built app.
