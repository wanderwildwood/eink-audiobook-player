package audiobook.core.playback.session

import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import audiobook.core.common.rootGraphAs
import audiobook.core.logging.api.Logger
import audiobook.core.playback.di.PlaybackGraph
import audiobook.core.playback.player.BookPlayer
import audiobook.core.playback.playstate.PositionUpdater
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

class PlaybackService : MediaLibraryService() {

  @Inject
  lateinit var session: MediaLibrarySession

  @Inject
  lateinit var scope: CoroutineScope

  @Inject
  lateinit var player: BookPlayer

  @Inject
  lateinit var positionUpdater: PositionUpdater

  @Inject
  lateinit var notificationProvider: BookNotificationProvider

  override fun onCreate() {
    super.onCreate()
    rootGraphAs<PlaybackGraph.Provider>()
      .playbackGraphFactory
      .create(this)
      .inject(this)
    setMediaNotificationProvider(notificationProvider)
  }

  private fun release() {
    runBlocking {
      positionUpdater.flushPositionNow()
    }
    positionUpdater.release()
    player.release()
    session.release()
    scope.cancel()
  }

  override fun onDestroy() {
    super.onDestroy()
    release()
  }

  override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
    return session.takeUnless { session ->
      session.invokeIsReleased
    }.also {
      if (it == null) {
        Logger.w("onGetSession returns null because the session is already released")
      }
    }
  }
}

private val MediaSession.invokeIsReleased: Boolean
  get() = try {
    // temporarily checked to debug
    // https://github.com/androidx/media/issues/422
    MediaSession::class.java.getDeclaredMethod("isReleased")
      .apply { isAccessible = true }
      .invoke(this) as Boolean
  } catch (e: Exception) {
    Logger.w(e, "Couldn't check if it's released")
    false
  }
