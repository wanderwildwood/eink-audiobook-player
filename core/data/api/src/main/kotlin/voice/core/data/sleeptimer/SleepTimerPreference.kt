package voice.core.data.sleeptimer

import kotlinx.serialization.Serializable
import voice.core.common.serialization.LocalTimeSerializer
import java.time.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
public data class SleepTimerPreference(
  /**
   * The custom sleep time duration. Ignored when [endOfChapterEnabled] is true.
   */
  val duration: Duration,
  /**
   * If the sleep timer should be automatically enabled between [autoSleepStartTime] and [autoSleepEndTime]
   */
  val autoSleepTimerEnabled: Boolean,
  @Serializable(with = LocalTimeSerializer::class)
  val autoSleepStartTime: LocalTime,
  @Serializable(with = LocalTimeSerializer::class)
  val autoSleepEndTime: LocalTime,
  /**
   * If true, the sleep timer (both the manual quick-tap button and the automatic nightly timer)
   * stops playback at the end of the current chapter instead of after [duration].
   */
  val endOfChapterEnabled: Boolean = false,
) {

  public companion object {
    public val Default: SleepTimerPreference = SleepTimerPreference(
      autoSleepTimerEnabled = false,
      autoSleepStartTime = LocalTime.of(22, 0),
      autoSleepEndTime = LocalTime.of(6, 0),
      duration = 10.minutes,
      endOfChapterEnabled = false,
    )
  }
}
