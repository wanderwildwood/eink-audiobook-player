package audiobook.core.playback.notification

import android.app.PendingIntent

interface MainActivityIntentProvider {
  fun toCurrentBook(): PendingIntent
}
