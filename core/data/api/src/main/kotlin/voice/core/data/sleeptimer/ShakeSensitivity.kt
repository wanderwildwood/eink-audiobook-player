package voice.core.data.sleeptimer

import kotlinx.serialization.Serializable

/**
 * How hard the device has to be shaken to reset the sleep timer.
 *
 * The value is a linear-acceleration threshold in m/s^2 - gravity is already subtracted by the
 * platform's sensor fusion, so a device at rest reads close to zero and these can be small
 * numbers. Higher sensitivity means a lower threshold, i.e. a gentler shake is enough.
 */
@Serializable
public enum class ShakeSensitivity(public val thresholdMetersPerSecondSquared: Float) {

  /** Takes a firm shake. Least likely to trip on rolling over or adjusting the covers. */
  Low(4.5f),

  /** A normal wrist flick. */
  Medium(3.0f),

  /** A small nudge is enough. */
  High(1.8f),
  ;

  public companion object {
    public val Default: ShakeSensitivity = Medium
  }
}
