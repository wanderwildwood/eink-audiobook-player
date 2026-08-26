package audiobook.app.navigation

import android.content.Intent
import androidx.datastore.core.DataStore
import audiobook.app.MainActivity
import audiobook.core.data.BookId
import audiobook.core.data.folders.AudiobookFolders
import audiobook.core.data.store.CurrentBookStore
import audiobook.core.data.store.OnboardingCompletedStore
import audiobook.core.playback.PlayerController
import audiobook.navigation.Destination
import audiobook.navigation.Origin
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Inject
class StartDestinationProvider(
  @OnboardingCompletedStore
  private val onboardingCompletedStore: DataStore<Boolean>,
  private val audiobookFolders: AudiobookFolders,
  @CurrentBookStore
  private val currentBookStore: DataStore<BookId?>,
  private val playerController: PlayerController,
) {

  operator fun invoke(intent: Intent): List<Destination.Compose> {
    val showOnboarding = runBlocking { showOnboarding() }
    if (showOnboarding) {
      // There is nothing to greet or explain that the folder picker does not say better by
      // asking. First launch goes straight there.
      return listOf(Destination.AddContent(origin = Origin.Onboarding))
    }

    val goToBook = intent.getBooleanExtra(MainActivity.Companion.NI_GO_TO_BOOK, false)
    if (goToBook) {
      val bookId = runBlocking { currentBookStore.data.first() }
      if (bookId != null) {
        return listOf(Destination.BookOverview, Destination.Playback(bookId))
      }
    }

    if (intent.action == "playCurrent") {
      val bookId = runBlocking { currentBookStore.data.first() }
      if (bookId != null) {
        playerController.play()
        return listOf(Destination.BookOverview, Destination.Playback(bookId))
      }
    }
    return listOf(Destination.BookOverview)
  }

  private suspend fun showOnboarding(): Boolean {
    return when {
      onboardingCompletedStore.data.first() -> false
      audiobookFolders.hasAnyFolders() -> false
      else -> true
    }
  }
}
