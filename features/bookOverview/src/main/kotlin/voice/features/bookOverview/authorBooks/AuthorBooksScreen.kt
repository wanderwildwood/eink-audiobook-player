package voice.features.bookOverview.authorBooks

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.data.BookId
import voice.core.ui.StaticBottomSheet
import voice.core.ui.icons.VoiceIcons
import voice.features.bookOverview.bottomSheet.BottomSheetContent
import voice.features.bookOverview.bottomSheet.BottomSheetItem
import voice.features.bookOverview.deleteBook.DeleteBookDialog
import voice.features.bookOverview.di.BookOverviewGraph
import voice.features.bookOverview.editTitle.EditBookTitleDialog
import voice.features.bookOverview.overview.BookOverviewItemViewState
import voice.features.bookOverview.overview.BookOverviewViewState
import voice.features.bookOverview.views.ListBookRow
import voice.features.bookOverview.views.NowPlayingBar
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.strings.R as StringsR

@ContributesTo(AppScope::class)
interface AuthorBooksGraph {
  val authorBooksViewModelFactory: AuthorBooksViewModel.Factory
}

@ContributesTo(AppScope::class)
interface AuthorBooksProvider {

  @Provides
  @IntoSet
  fun authorBooksNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.AuthorBooks> { key ->
    NavEntry(key) {
      AuthorBooksScreen(folderName = key.folderName)
    }
  }
}

@Composable
fun AuthorBooksScreen(folderName: String?) {
  val viewModel = retain(folderName) {
    rootGraphAs<AuthorBooksGraph>().authorBooksViewModelFactory.create(folderName)
  }
  val bookOverviewGraph = retain<BookOverviewGraph> {
    rootGraphAs<BookOverviewGraph.Factory.Provider>()
      .bookOverviewGraphProviderFactory.create()
  }
  val bottomSheetViewModel = bookOverviewGraph.bottomSheetViewModel
  val deleteBookViewModel = bookOverviewGraph.deleteBookViewModel
  val editBookTitleViewModel = bookOverviewGraph.editBookTitleViewModel
  val fileCoverViewModel = bookOverviewGraph.fileCoverViewModel
  val bookOverviewViewModel = bookOverviewGraph.bookOverviewViewModel

  val viewState = viewModel.state()
  val bookOverviewViewState = bookOverviewViewModel.state()

  val getContentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri ->
      if (uri != null) {
        fileCoverViewModel.onImagePicked(uri)
      }
    },
  )

  var showBottomSheet by remember { mutableStateOf(false) }

  AuthorBooksScreen(
    viewState = viewState,
    nowPlaying = bookOverviewViewState.nowPlaying?.value,
    nowPlayingActive = bookOverviewViewState.playButtonState == BookOverviewViewState.PlayButtonState.Playing,
    onBookClick = viewModel::onBookClick,
    onBookLongClick = { bookId ->
      bottomSheetViewModel.bookSelected(bookId)
      showBottomSheet = true
    },
    onBackClick = viewModel::onBackClick,
    onNowPlayingClick = viewModel::onBookClick,
    onNowPlayingPlayPauseClick = bookOverviewViewModel::playPause,
  )

  val deleteBookViewState = deleteBookViewModel.state.value
  if (deleteBookViewState != null) {
    DeleteBookDialog(
      viewState = deleteBookViewState,
      onDismiss = deleteBookViewModel::onDismiss,
      onConfirmDeletion = deleteBookViewModel::onConfirmDeletion,
      onDeleteCheckBoxCheck = deleteBookViewModel::onDeleteCheckBoxCheck,
    )
  }
  val editBookTitleState = editBookTitleViewModel.state.value
  if (editBookTitleState != null) {
    EditBookTitleDialog(
      onDismissEditTitleClick = editBookTitleViewModel::onDismissEditTitle,
      onConfirmEditTitle = editBookTitleViewModel::onConfirmEditTitle,
      viewState = editBookTitleState,
      onUpdateEditTitle = editBookTitleViewModel::onUpdateEditTitle,
    )
  }

  if (showBottomSheet) {
    StaticBottomSheet(
      onDismissRequest = {
        showBottomSheet = false
      },
    ) {
      BottomSheetContent(
        state = bottomSheetViewModel.state.value,
        onItemClick = { item ->
          if (item == BottomSheetItem.FileCover) {
            getContentLauncher.launch("image/*")
          }
          bottomSheetViewModel.onItemClick(item)
          showBottomSheet = false
        },
      )
    }
  }
}

@Composable
internal fun AuthorBooksScreen(
  viewState: AuthorBooksViewState,
  nowPlaying: BookOverviewItemViewState?,
  nowPlayingActive: Boolean,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  onBackClick: () -> Unit,
  onNowPlayingClick: (BookId) -> Unit,
  onNowPlayingPlayPauseClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(viewState.folderName ?: stringResource(StringsR.string.library_browse_unsorted))
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              imageVector = VoiceIcons.Close,
              contentDescription = stringResource(StringsR.string.common_action_close),
            )
          }
        },
      )
    },
    bottomBar = {
      if (nowPlaying != null) {
        NowPlayingBar(
          book = nowPlaying,
          playing = nowPlayingActive,
          onClick = onNowPlayingClick,
          onPlayPauseClick = onNowPlayingPlayPauseClick,
        )
      }
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
  ) { contentPadding ->
    LazyColumn(
      modifier = Modifier.consumeWindowInsets(contentPadding),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(
        top = contentPadding.calculateTopPadding() + 8.dp,
        start = 8.dp,
        end = 8.dp,
        bottom = 16.dp,
      ),
    ) {
      items(
        items = viewState.books,
        key = { it.id.value },
        contentType = { "item" },
      ) { book ->
        ListBookRow(
          book = book,
          onBookClick = onBookClick,
          onBookLongClick = onBookLongClick,
        )
      }
    }
  }
}
