package audiobook.features.playbackScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import audiobook.core.common.DispatcherProvider
import audiobook.core.common.MainScope
import audiobook.core.data.Book
import audiobook.core.data.BookId
import audiobook.core.data.durationMs
import audiobook.core.data.markForPosition
import audiobook.core.data.repo.BookRepository
import audiobook.core.data.repo.BookmarkRepo
import audiobook.core.data.sleeptimer.SleepTimerPreference
import audiobook.core.data.store.CurrentBookStore
import audiobook.core.data.store.SeekTimeStore
import audiobook.core.data.store.SleepTimerPreferenceStore
import audiobook.core.featureflag.ExperimentalPlaybackPersistenceQualifier
import audiobook.core.featureflag.FeatureFlag
import audiobook.core.logging.api.Logger
import audiobook.core.playback.CurrentBookResolver
import audiobook.core.playback.PlayerController
import audiobook.core.playback.misc.Decibel
import audiobook.core.playback.overlay
import audiobook.core.playback.playstate.PlayStateManager
import audiobook.core.sleeptimer.SleepTimer
import audiobook.core.sleeptimer.SleepTimerMode
import audiobook.core.sleeptimer.SleepTimerState
import audiobook.core.ui.formatTime
import audiobook.features.playbackScreen.batteryOptimization.BatteryOptimization
import audiobook.navigation.Destination
import audiobook.navigation.Navigator
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@AssistedInject
class BookPlayViewModel(
  private val bookRepository: BookRepository,
  private val currentBookResolver: CurrentBookResolver,
  private val player: PlayerController,
  private val sleepTimer: SleepTimer,
  private val playStateManager: PlayStateManager,
  @CurrentBookStore
  private val currentBookStoreId: DataStore<BookId?>,
  @SleepTimerPreferenceStore
  private val sleepTimerPreferenceStore: DataStore<SleepTimerPreference>,
  @SeekTimeStore
  private val seekTimeStore: DataStore<Int>,
  private val navigator: Navigator,
  private val bookmarkRepository: BookmarkRepo,
  private val batteryOptimization: BatteryOptimization,
  dispatcherProvider: DispatcherProvider,
  @ExperimentalPlaybackPersistenceQualifier
  private val experimentalPlaybackPersistenceFeatureFlag: FeatureFlag<Boolean>,
  @Assisted
  private val bookId: BookId,
) {

  private val scope = MainScope(dispatcherProvider)

  internal val viewEffects: Flow<BookPlayViewEffect>
    field = MutableSharedFlow<BookPlayViewEffect>(extraBufferCapacity = 1)

  internal val dialogState: State<BookPlayDialogViewState?>
    field = mutableStateOf<BookPlayDialogViewState?>(null)

  // Deliberately not persisted: a lock is for the session you are in - waking up to a player
  // you cannot operate, with no memory of locking it, would be worse than the stray touch.
  private val locked = mutableStateOf(false)

  init {
    scope.launch {
      player.pauseIfCurrentBookDifferentFrom(bookId)
      currentBookStoreId.updateData { bookId }
    }
  }

  @Composable
  fun viewState(): BookPlayViewState? {
    val persistedBook = remember(bookId) {
      bookRepository.flow(bookId).filterNotNull()
    }.collectAsState(initial = null).value ?: return null

    val experimentalPlaybackPersistence = experimentalPlaybackPersistenceFeatureFlag.get()
    val livePlaybackState = if (experimentalPlaybackPersistence) {
      remember(bookId) { player.livePlaybackStateFlow(bookId) }
        .collectAsState(null).value
    } else {
      null
    }
    val managerPlayState by remember {
      playStateManager.playStateFlow
    }.collectAsState()

    val book = if (livePlaybackState != null) {
      persistedBook.overlay(livePlaybackState)
    } else {
      persistedBook
    }
    val isPlaying = livePlaybackState?.isPlaying ?: (managerPlayState == PlayStateManager.PlayState.Playing)

    val currentMark = book.currentChapter.markForPosition(book.content.positionInChapter)
    val positionInCurrentMark = if (isPlaying && currentMark.durationMs > 0) {
      val relativePosition = book.content.positionInChapter - currentMark.startMs
      relativePosition.coerceIn(0L, currentMark.durationMs)
    } else {
      book.content.positionInChapter - currentMark.startMs
    }

    val sleepTime = remember { sleepTimer.state }.collectAsState().value
    // The initial matches SeekTimeStore's own default, so the buttons never render a number
    // that contradicts what they'd actually do - it only applies for the first frame anyway.
    val seekTimeSeconds = remember { seekTimeStore.data }.collectAsState(initial = 20).value
    val hasMoreThanOneChapter = book.chapters.sumOf { it.chapterMarks.count() } > 1
    return BookPlayViewState(
      sleepTimerState = sleepTime.toViewState(),
      playing = isPlaying,
      title = book.content.name,
      showPreviousNextButtons = hasMoreThanOneChapter,
      chapterName = currentMark.name.takeIf { hasMoreThanOneChapter },
      duration = currentMark.durationMs.milliseconds,
      playedTime = positionInCurrentMark.milliseconds,
      author = book.content.author,
      skipSilence = book.content.skipSilence,
      seekTimeSeconds = seekTimeSeconds,
      playbackSpeed = book.content.playbackSpeed,
      locked = locked.value,
      volumeGain = Decibel(book.content.gain),
    )
  }
  fun dismissDialog() {
    Logger.d("dismissDialog")
    dialogState.value = null
  }

  fun toggleLock() {
    locked.value = !locked.value
  }

  fun onPlaybackSpeedChanged(speed: Float) {
    dialogState.value = BookPlayDialogViewState.SpeedDialog(speed)
    player.setSpeed(speed)
  }

  fun next() {
    player.next()
  }

  fun previous() {
    player.previous()
  }

  fun playPause() {
    if (playStateManager.playState != PlayStateManager.PlayState.Playing) {
      scope.launch {
        if (batteryOptimization.shouldRequest()) {
          viewEffects.tryEmit(BookPlayViewEffect.RequestIgnoreBatteryOptimization)
          batteryOptimization.onBatteryOptimizationsRequested()
        }
      }
    }
    player.playPause()
  }

  fun rewind() {
    player.rewind()
  }

  fun fastForward() {
    player.fastForward()
  }

  fun onCloseClick() {
    navigator.goBack()
  }

  fun onCurrentChapterClick() {
    scope.launch {
      val book = currentBook() ?: return@launch
      dialogState.value = BookPlayDialogViewState.SelectChapterDialog(
        items = book.chapters.flatMapIndexed { chapterIndex, chapter ->
          chapter.chapterMarks.mapIndexed { markIndex, chapterMark ->
            val previousChapters = book.chapters.take(chapterIndex)
            BookPlayDialogViewState.SelectChapterDialog.ItemViewState(
              number = previousChapters.sumOf { it.chapterMarks.count() } + markIndex + 1,
              name = chapterMark.name ?: "",
              active = chapterMark == book.currentMark && chapter == book.currentChapter,
              time = formatTime(previousChapters.sumOf { it.duration } + chapterMark.startMs),
            )
          }
        },
      )
    }
  }

  fun onChapterClick(number: Int) {
    scope.launch {
      val book = currentBook() ?: return@launch
      var currentIndex = -1
      book.chapters.forEach { chapter ->
        chapter.chapterMarks.forEach { mark ->
          currentIndex++
          if (currentIndex == number - 1) {
            player.setPosition(mark.startMs, chapter.id)
            dialogState.value = null
            return@launch
          }
        }
      }
    }
  }

  fun onPlaybackSpeedIconClick() {
    scope.launch {
      val playbackSpeed = currentBook()?.content?.playbackSpeed ?: return@launch
      dialogState.value = BookPlayDialogViewState.SpeedDialog(playbackSpeed)
    }
  }

  fun toggleVolumeBoost() {
    scope.launch {
      val content = currentBook()?.content ?: return@launch
      player.setGain(if (content.gain > 0f) Decibel.Zero else VolumeBoost)
    }
  }

  fun onBookmarkClick() {
    navigator.goTo(Destination.Bookmarks(bookId))
  }

  fun onBookmarkLongClick() {
    scope.launch {
      val book = currentBook() ?: return@launch
      bookmarkRepository.addBookmarkAtBookPosition(
        book = book,
        title = null,
        setBySleepTimer = false,
      )
      viewEffects.tryEmit(BookPlayViewEffect.BookmarkAdded)
    }
  }

  fun seekTo(position: Duration) {
    scope.launch {
      val book = currentBook() ?: return@launch
      val currentChapter = book.currentChapter
      val currentMark = currentChapter.markForPosition(book.content.positionInChapter)
      player.setPosition(currentMark.startMs + position.inWholeMilliseconds, currentChapter.id)
    }
  }

  fun toggleSleepTimer() {
    scope.launch {
      Logger.d("toggleSleepTimer while active=${sleepTimer.state.value}")
      if (sleepTimer.state.value.enabled) {
        sleepTimer.disable()
        rememberSleepTimerEnabled(false)
      } else {
        val book = currentBook()
        if (book != null) {
          scope.launch {
            bookmarkRepository.addBookmarkAtBookPosition(
              book = book,
              setBySleepTimer = true,
              title = null,
            )
          }
          sleepTimer.enable(SleepTimerMode.TimedWithDefault)
          rememberSleepTimerEnabled(true)
        }
      }
    }
  }

  /**
   * Remembers that the user wants the sleep timer on (or off), so it can be re-armed automatically
   * on the next playback start. Deliberately only written on an explicit toggle - the timer
   * disabling itself when it expires must not count as switching it off.
   */
  private suspend fun rememberSleepTimerEnabled(enabled: Boolean) {
    sleepTimerPreferenceStore.updateData { it.copy(enabledLastSession = enabled) }
  }

  fun onBatteryOptimizationRequested() {
    navigator.goTo(Destination.BatteryOptimization)
  }

  fun toggleSkipSilence() {
    scope.launch {
      val skipSilence = currentBook()?.content?.skipSilence ?: return@launch
      player.skipSilence(!skipSilence)
    }
  }

  private suspend fun currentBook(): Book? {
    return currentBookResolver.book(bookId)
  }

  @AssistedFactory
  interface Factory {
    fun create(bookId: BookId): BookPlayViewModel
  }
}

private fun SleepTimerState.toViewState(): BookPlayViewState.SleepTimerViewState = when (this) {
  SleepTimerState.Disabled -> BookPlayViewState.SleepTimerViewState.Disabled
  is SleepTimerState.Enabled.WithDuration -> BookPlayViewState.SleepTimerViewState.Enabled.WithDuration(this.leftDuration)
  SleepTimerState.Enabled.UntilChapterEnd -> BookPlayViewState.SleepTimerViewState.Enabled.UntilChapterEnd
}

/**
 * How much the volume boost lifts playback when it is on.
 *
 * It used to be a slider from 0 to 9 dB, which asked people to tune a number in decibels against
 * a narrator they were part way through listening to. There is one useful answer for a quiet
 * recording, so this is it: clearly louder, and short of the 9 dB the slider allowed, because
 * the limiter doing the lifting starts to pump against a wide dynamic range near the top.
 */
private val VolumeBoost = Decibel(6F)
