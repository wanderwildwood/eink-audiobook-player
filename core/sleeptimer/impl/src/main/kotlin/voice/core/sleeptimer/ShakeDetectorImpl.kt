package voice.core.sleeptimer

import android.content.Context
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.squareup.seismic.ShakeDetector as SeismicShakeDetector

@ContributesBinding(AppScope::class)
class ShakeDetectorImpl(private val context: Context) : ShakeDetector {

  override suspend fun detect() {
    suspendCancellableCoroutine { cont ->
      val sensorManager = context.getSystemService<SensorManager>()
        ?: return@suspendCancellableCoroutine
      lateinit var shakeDetector: SeismicShakeDetector
      val listener = SeismicShakeDetector.Listener {
        if (!cont.isCompleted) {
          // Unregister immediately - a normal resume() does not trigger invokeOnCancellation,
          // so without this the listener leaks on every detected shake and eventually hits
          // Android's per-app sensor listener cap.
          shakeDetector.stop()
          cont.resume(Unit)
        }
      }
      shakeDetector = SeismicShakeDetector(listener)
      // Seismic compares raw accelerometer magnitude (x^2+y^2+z^2), which is dominated by
      // gravity (~9.8 m/s^2) even at complete rest - it never subtracts a baseline. Any
      // sensitivity below ~9.8 therefore reads as "shaking" permanently, at rest, regardless
      // of real motion. That's what values of 6 and then 9 both did here (confirmed via
      // decompiling ShakeDetector$SampleQueue.isAccelerating - it's a bare magnitude^2 vs
      // threshold^2 comparison, no gravity compensation) - visible on a real device as a
      // "shake detected" log line every ~300ms while sitting untouched on a desk, which starved
      // the sleep timer countdown since it was being reset back to full duration constantly.
      // 10 is the least-sensitive-looking value that's still actually above gravity, i.e. the
      // most sensitive setting that isn't fundamentally broken - below the library's own
      // SENSITIVITY_LIGHT (11) preset, so still a bit easier to trigger deliberately than that.
      shakeDetector.setSensitivity(10)
      shakeDetector.start(sensorManager, SensorManager.SENSOR_DELAY_GAME)
      cont.invokeOnCancellation {
        shakeDetector.stop()
      }
    }
  }
}
