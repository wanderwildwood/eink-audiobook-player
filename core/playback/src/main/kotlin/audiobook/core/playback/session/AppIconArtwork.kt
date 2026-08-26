package audiobook.core.playback.session

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.graphics.createBitmap
import audiobook.core.logging.api.Logger
import java.io.ByteArrayOutputStream

/**
 * The app's own launcher icon as PNG bytes, for anything that wants a picture of what is playing.
 *
 * Books have no covers here, but players still ask - a notification, the launcher's media control,
 * a car head unit - and whatever is left in the file's own tags answers if nothing else does. The
 * icon is a truthful answer that stays consistent across the whole library.
 */
internal fun appIconArtwork(context: Context): ByteArray? {
  return try {
    val icon = context.packageManager.getApplicationIcon(context.applicationInfo)
    val size = 384
    val bitmap = createBitmap(size, size)
    Canvas(bitmap).apply {
      drawColor(Color.WHITE)
      icon.setBounds(0, 0, size, size)
      icon.draw(this)
    }
    // An adaptive icon keeps its mark within the middle two thirds and leaves the rest as bleed
    // for a launcher's mask to crop. Handing that over whole would be mostly margin, but trimming
    // it back to the visible 72 leaves nothing for whoever shows the thumbnail to crop in turn -
    // and they do. Give away half the bleed and keep the rest as breathing room.
    val inset = size * 9 / 108
    val cropped = Bitmap.createBitmap(bitmap, inset, inset, size - inset * 2, size - inset * 2)
    ByteArrayOutputStream().use { out ->
      cropped.compress(Bitmap.CompressFormat.PNG, 100, out)
      out.toByteArray()
    }
  } catch (e: Exception) {
    Logger.w(e, "Could not render the app icon as artwork")
    null
  }
}
