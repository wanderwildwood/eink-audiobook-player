package voice.core.sleeptimer

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import voice.core.common.DispatcherProvider
import voice.core.logging.api.Logger
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * The acknowledgement for a shake that reset the sleep timer: without it there is no way to tell a
 * shake that registered from one that didn't, short of waiting to hear whether the book stops.
 *
 * The tone is synthesized rather than bundled as an asset so there is no audio file to ship, and so
 * its level can be fixed in the waveform itself - this plays in a dark room next to someone falling
 * asleep, and it should never be the loudest thing that happens.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ShakeChimeImpl(dispatcherProvider: DispatcherProvider) : ShakeChime {

  private val scope = CoroutineScope(dispatcherProvider.io)

  // Synthesized once and reused - it never changes, and building it costs an allocation per shake
  // otherwise.
  private val samples: ShortArray by lazy { synthesize() }

  override fun play() {
    scope.launch {
      runCatching { playBlocking() }
        .onFailure { Logger.w(it, "Could not play the shake chime") }
    }
  }

  private fun playBlocking() {
    val samples = samples
    val track = AudioTrack.Builder()
      .setAudioAttributes(
        AudioAttributes.Builder()
          // Follows the media volume the book is already playing at, so it stays in proportion
          // to whatever the listener chose for the night.
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build(),
      )
      .setAudioFormat(
        AudioFormat.Builder()
          .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
          .setSampleRate(SAMPLE_RATE)
          .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
          .build(),
      )
      .setBufferSizeInBytes(samples.size * Short.SIZE_BYTES)
      .setTransferMode(AudioTrack.MODE_STATIC)
      .build()

    // No audio focus request: this is a half-second tone over a book that is either playing or
    // just paused, and asking for focus would duck or pause that book to announce itself.
    track.write(samples, 0, samples.size)
    track.setNotificationMarkerPosition(samples.size)
    track.setPlaybackPositionUpdateListener(
      object : AudioTrack.OnPlaybackPositionUpdateListener {
        override fun onMarkerReached(track: AudioTrack) {
          track.release()
        }

        override fun onPeriodicNotification(track: AudioTrack) = Unit
      },
    )
    track.play()
  }

  /**
   * A struck-bell shape: a fundamental with a quieter fifth above it, both decaying exponentially.
   * The short attack ramp matters - starting a sine at full amplitude puts a step in the waveform,
   * which is audible as a click.
   */
  private fun synthesize(): ShortArray {
    val frameCount = (SAMPLE_RATE * DURATION_SECONDS).toInt()
    val attackFrames = (SAMPLE_RATE * ATTACK_SECONDS).toInt()
    return ShortArray(frameCount) { frame ->
      val t = frame.toDouble() / SAMPLE_RATE
      val decay = exp(-t / DECAY_TIME_CONSTANT_SECONDS)
      val attack = min(1.0, frame.toDouble() / attackFrames)
      val fundamental = sin(2 * PI * FUNDAMENTAL_HZ * t)
      val fifth = sin(2 * PI * FIFTH_HZ * t) * FIFTH_GAIN
      val value = (fundamental + fifth) / (1 + FIFTH_GAIN) * decay * attack * PEAK_AMPLITUDE
      (value * Short.MAX_VALUE).toInt().toShort()
    }
  }

  private companion object {
    const val SAMPLE_RATE = 44_100
    const val DURATION_SECONDS = 0.6
    const val ATTACK_SECONDS = 0.008
    const val DECAY_TIME_CONSTANT_SECONDS = 0.18

    // A' and the fifth above it. High enough to read as a chime rather than a thud, low enough
    // not to be piercing in a quiet room.
    const val FUNDAMENTAL_HZ = 880.0
    const val FIFTH_HZ = 1320.0
    const val FIFTH_GAIN = 0.35

    // Deliberately quiet. This is a confirmation, not an alert.
    const val PEAK_AMPLITUDE = 0.16
  }
}
