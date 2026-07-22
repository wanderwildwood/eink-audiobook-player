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
      // Well below the library's SENSITIVITY_LIGHT (11) - a light shake should be enough to
      // reset the timer, since this is meant to be triggered half-asleep in bed.
      shakeDetector.setSensitivity(6)
      shakeDetector.start(sensorManager, SensorManager.SENSOR_DELAY_GAME)
      cont.invokeOnCancellation {
        shakeDetector.stop()
      }
    }
  }
}
