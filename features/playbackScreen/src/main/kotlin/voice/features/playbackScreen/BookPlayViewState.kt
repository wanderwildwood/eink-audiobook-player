package voice.features.playbackScreen

import androidx.compose.runtime.Immutable
import voice.core.playback.misc.Decibel
import kotlin.time.Duration

@Immutable
data class BookPlayViewState(
  val chapterName: String?,
  val showPreviousNextButtons: Boolean,
  val title: String,
  val sleepTimerState: SleepTimerViewState,
  val playedTime: Duration,
  val duration: Duration,
  val playing: Boolean,
  val author: String?,
  val skipSilence: Boolean,
  /** Seconds the rewind/fast-forward buttons move, shown on the buttons themselves. */
  val seekTimeSeconds: Int,
  /** Current playback speed. Surfaced under the speed icon when it is not 1x. */
  val playbackSpeed: Float,
  /** When true every control is inert except the lock itself, so a stray touch cannot seek. */
  val locked: Boolean,
) {

  sealed interface SleepTimerViewState {
    data object Disabled : SleepTimerViewState

    sealed interface Enabled : SleepTimerViewState {
      @JvmInline
      value class WithDuration(val leftDuration: Duration) : Enabled
      data object UntilChapterEnd : Enabled
    }
  }

  init {
    require(duration > Duration.ZERO) {
      "Duration must be positive in $this"
    }
  }
}

internal sealed interface BookPlayDialogViewState {
  data class SpeedDialog(val speed: Float) : BookPlayDialogViewState {

    val maxSpeed: Float get() = if (speed < 2F) 2F else 3.5F
  }

  data class VolumeGainDialog(
    val gain: Decibel,
    val valueFormatted: String,
    val maxGain: Decibel,
  ) : BookPlayDialogViewState

  data class SelectChapterDialog(val items: List<ItemViewState>) : BookPlayDialogViewState {

    data class ItemViewState(
      val number: Int,
      val name: String,
      val active: Boolean,
      val time: String,
    )
  }
}
