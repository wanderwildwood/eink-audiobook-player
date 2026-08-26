package voice.core.playback.player

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import voice.core.logging.api.Logger
import voice.core.playback.di.PlaybackScope

/**
 * Keeps a phone call from restarting a book that was already paused.
 *
 * Hang up and something on the device asks the session to play again - an OEM telephony stack
 * replaying a media button, a headset sending AVRCP PLAY. The session obliges whatever the book
 * was doing beforehand, so one paused hours earlier starts talking the moment the call ends.
 *
 * Note this is NOT media3's audio focus handling. That only resumes a book it paused itself, and
 * a paused player holds no focus to regain - confirmed on device, where a paused book is absent
 * from the audio focus stack entirely. The resume arrives as an ordinary play request, which is
 * why it is caught here at the player rather than anywhere near focus.
 *
 * So: remember whether the book was actually running when the call began, and swallow the play if
 * it wasn't. Only requests arriving during a call, or in the few seconds after one, are touched -
 * a play pressed at any other time goes through untouched.
 *
 * This also stops the book at the first ring rather than leaving it talking over the ringtone
 * until the call is answered. Audio focus only takes the book away once a call actually connects,
 * which meant a page of narration ran on underneath the ringing.
 */
@Inject
@SingleIn(PlaybackScope::class)
class CallResumeGuard(private val application: Application) {

  private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

  @Volatile private var lastPlayWhenReady = false

  /** When [lastPlayWhenReady] last went true -> false, to see through a pause the call caused. */
  @Volatile private var pausedAtElapsed = Long.MIN_VALUE

  private var pauseForCall: (() -> Unit)? = null

  @Volatile private var inCall = false

  @Volatile private var playingWhenCallStarted = false

  @Volatile private var callEndedAtElapsed = Long.MIN_VALUE

  init {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      audioManager?.addOnModeChangedListener(application.mainExecutor) { mode ->
        onModeChanged(mode)
      }
    }
  }

  /** What to run when a call arrives, so the book stops at the ring rather than at the answer. */
  fun pauseOnIncomingCall(action: () -> Unit) {
    pauseForCall = action
  }

  fun notePlayWhenReady(playWhenReady: Boolean) {
    if (lastPlayWhenReady && !playWhenReady) {
      pausedAtElapsed = SystemClock.elapsedRealtime()
    }
    lastPlayWhenReady = playWhenReady
  }

  /** True while a play request should be ignored because it is the tail of a call. */
  fun shouldSuppressResume(): Boolean {
    // It was running when the call came in, so resuming afterwards is what was wanted.
    if (playingWhenCallStarted) return false

    if (inCall) return true

    // On API < 31 there is no mode listener, so fall back to asking whether the device is still
    // winding down from a call at the moment the request lands. Narrower, but better than nothing.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && audioManager?.mode.isCallMode()) return true

    val sinceCallEnded = SystemClock.elapsedRealtime() - callEndedAtElapsed
    return callEndedAtElapsed != Long.MIN_VALUE && sinceCallEnded in 0 until RESUME_WINDOW_MS
  }

  private fun onModeChanged(mode: Int) {
    val nowInCall = mode.isCallMode()
    if (nowInCall && !inCall) {
      // Snapshot before the call takes hold. An incoming call can pause us through audio focus a
      // beat before the mode changes, so a pause in the last moments still counts as "was playing"
      // - otherwise the real interruption case would stop resuming.
      playingWhenCallStarted = wasPlayingRecently()
      Logger.d("Call started, book was playing: $playingWhenCallStarted")
      // Snapshot first, then stop - otherwise this pause is the thing we would be reading back.
      if (playingWhenCallStarted) {
        pauseForCall?.invoke()
      }
    } else if (!nowInCall && inCall) {
      callEndedAtElapsed = SystemClock.elapsedRealtime()
      Logger.d("Call ended, resume ${if (playingWhenCallStarted) "allowed" else "suppressed"}")
    }
    inCall = nowInCall
  }

  private fun wasPlayingRecently(): Boolean {
    if (lastPlayWhenReady) return true
    if (pausedAtElapsed == Long.MIN_VALUE) return false
    return SystemClock.elapsedRealtime() - pausedAtElapsed < FOCUS_PAUSE_GRACE_MS
  }
}

private fun Int?.isCallMode(): Boolean = this == AudioManager.MODE_IN_CALL ||
  this == AudioManager.MODE_IN_COMMUNICATION ||
  this == AudioManager.MODE_RINGTONE

/** How long after a call a play request is still treated as the call's doing. */
private const val RESUME_WINDOW_MS = 10_000L

/** A pause this recent when a call starts is read as the call causing it, not the user. */
private const val FOCUS_PAUSE_GRACE_MS = 3_000L
