package vocie.core.data.store

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.runner.RunWith
import voice.core.data.sleeptimer.ShakeSensitivity
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.data.store.VoiceDataStoreFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

/**
 * A setting removed from the app is not removed from the files already written on people's
 * devices. Reading one has to skip what it no longer knows and keep the rest, because the
 * serializer's only answer to a failed read is the whole default - which would take every
 * setting stored beside the removed one with it.
 */
@RunWith(AndroidJUnit4::class)
class DroppedFieldTest {

  @Test
  fun `a stored field the app no longer knows does not reset the ones it does`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Application>()
    val fileName = "droppedField"
    File(context.filesDir, "datastore").mkdirs()
    // What the automatic sleep timer used to write, alongside settings that are still current.
    File(context.filesDir, "datastore/$fileName").writeText(
      """
      {
        "duration": "PT45M",
        "autoSleepTimerEnabled": true,
        "autoSleepStartTime": "22:00",
        "autoSleepEndTime": "06:00",
        "endOfChapterEnabled": true,
        "enabledLastSession": true,
        "shakeSensitivity": "Low"
      }
      """.trimIndent(),
    )

    val store = VoiceDataStoreFactory(Json { ignoreUnknownKeys = true }, context)
      .create(SleepTimerPreference.serializer(), SleepTimerPreference.Default, fileName)
    val read = store.data.first()

    assertEquals(expected = 45.minutes, actual = read.duration)
    assertEquals(expected = true, actual = read.endOfChapterEnabled)
    assertEquals(expected = true, actual = read.enabledLastSession)
    assertEquals(expected = ShakeSensitivity.Low, actual = read.shakeSensitivity)
  }
}
