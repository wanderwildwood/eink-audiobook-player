package voice.core.sleeptimer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Detects a deliberate shake using [Sensor.TYPE_LINEAR_ACCELERATION] - gravity is already
 * subtracted by the platform's own sensor fusion, so a stationary device reads ~0 m/s^2
 * rather than ~9.8 m/s^2 the way raw [Sensor.TYPE_ACCELEROMETER] does.
 *
 * This used to wrap com.squareup.seismic.ShakeDetector, which reads raw accelerometer data.
 * Its sensitivity is a bare magnitude threshold with no gravity compensation, so any value low
 * enough to make a deliberate shake easy to trigger was *also* low enough that gravity alone
 * satisfied it - the detector fired constantly at complete rest, never letting the sleep timer
 * countdown actually count down. Raising the threshold enough to stop that (up to the library's
 * own "light" preset) made genuine shakes hard to trigger again - the exact complaint that led
 * to a low threshold in the first place. Switching to linear acceleration removes gravity from
 * the picture entirely, so those two goals are no longer in tension.
 */
@ContributesBinding(AppScope::class)
class ShakeDetectorImpl(private val context: Context) : ShakeDetector {

  override suspend fun detect() {
    suspendCancellableCoroutine { cont ->
      val sensorManager = context.getSystemService<SensorManager>()
        ?: return@suspendCancellableCoroutine
      val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        ?: return@suspendCancellableCoroutine

      val window = ArrayDeque<Sample>()

      val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
          val x = event.values[0]
          val y = event.values[1]
          val z = event.values[2]
          val magnitudeSquared = (x * x + y * y + z * z).toDouble()

          window.addLast(Sample(event.timestamp, magnitudeSquared > ACCELERATION_THRESHOLD_SQUARED))
          while (window.size > 1 && event.timestamp - window.first().timestampNanos > WINDOW_NANOS) {
            window.removeFirst()
          }

          val acceleratingCount = window.count { it.accelerating }
          val spansMinWindow = window.size > 1 &&
            window.last().timestampNanos - window.first().timestampNanos >= MIN_WINDOW_NANOS
          val majorityAccelerating = acceleratingCount >= (window.size * 3) / 4

          if (spansMinWindow && majorityAccelerating && !cont.isCompleted) {
            sensorManager.unregisterListener(this)
            cont.resume(Unit)
          }
        }

        override fun onAccuracyChanged(
          sensor: Sensor?,
          accuracy: Int,
        ) = Unit
      }

      sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
      cont.invokeOnCancellation {
        sensorManager.unregisterListener(listener)
      }
    }
  }

  private data class Sample(
    val timestampNanos: Long,
    val accelerating: Boolean,
  )

  private companion object {
    // With gravity already removed, true rest reads close to 0 m/s^2 - this can be a low,
    // easy-to-trigger value without also catching ambient vibration or sensor noise.
    const val ACCELERATION_THRESHOLD = 4.0
    const val ACCELERATION_THRESHOLD_SQUARED = ACCELERATION_THRESHOLD * ACCELERATION_THRESHOLD

    // Mirrors com.squareup.seismic.ShakeDetector's own window sizing (250-500ms, 75% of
    // samples in the window must be "accelerating") - a proven balance between reacting
    // quickly and not tripping on a single noisy sample.
    const val MIN_WINDOW_NANOS = 250_000_000L
    const val WINDOW_NANOS = 500_000_000L
  }
}
