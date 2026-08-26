package audiobook.app.misc

import android.app.PendingIntent
import android.content.Context
import audiobook.app.MainActivity
import audiobook.core.playback.notification.MainActivityIntentProvider
import dev.zacsweers.metro.Inject

@Inject
class MainActivityIntentProviderImpl(private val context: Context) : MainActivityIntentProvider {

  override fun toCurrentBook(): PendingIntent {
    val intent = MainActivity.goToBookIntent(context)
    return PendingIntent.getActivity(
      context,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }
}
