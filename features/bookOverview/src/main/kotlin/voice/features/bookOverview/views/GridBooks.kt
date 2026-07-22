package voice.features.bookOverview.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import voice.core.data.BookId
import voice.core.ui.sharedCoverElementModifier
import voice.features.bookOverview.overview.BookOverviewItemViewState
import voice.features.bookOverview.overview.LibrarySection
import kotlin.math.roundToInt
import voice.core.ui.R as UiR

@Composable
internal fun GridBooks(
  sections: List<LibrarySection>,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  onFolderClick: (String?) -> Unit,
  showPermissionBugCard: Boolean,
  onPermissionBugCardClick: () -> Unit,
) {
  val cellCount = gridColumnCount()
  LazyVerticalGrid(
    columns = GridCells.Fixed(cellCount),
    verticalArrangement = Arrangement.spacedBy(4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 24.dp, bottom = 4.dp),
  ) {
    if (showPermissionBugCard) {
      item(
        span = { GridItemSpan(maxLineSpan) },
      ) {
        PermissionBugCard(onPermissionBugCardClick)
      }
    }
    sections.forEachIndexed { sectionIndex, section ->
      when (section) {
        is LibrarySection.Folders -> {
          if (section.folders.isNotEmpty()) {
            item(
              span = { GridItemSpan(maxLineSpan) },
              key = "folders-header-$sectionIndex",
              contentType = "header",
            ) {
              BrowseHeader(
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
              )
            }
            items(
              items = section.folders,
              span = { GridItemSpan(maxLineSpan) },
              key = { it.folderName ?: "" },
              contentType = { "folder" },
            ) { folder ->
              FolderRow(folder = folder, onClick = onFolderClick)
            }
          }
        }
        is LibrarySection.Books -> {
          if (section.books.isNotEmpty()) {
            val headerRes = section.headerRes
            if (headerRes != null) {
              item(
                span = { GridItemSpan(maxLineSpan) },
                key = "books-header-$sectionIndex",
                contentType = "header",
              ) {
                SectionHeader(
                  headerRes = headerRes,
                  modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
                )
              }
            }
            items(
              items = section.books,
              key = { it.id.value },
              contentType = { "book" },
            ) { book ->
              GridBook(book = book, onBookClick = onBookClick, onBookLongClick = onBookLongClick)
            }
          }
        }
      }
    }
    item(
      span = { GridItemSpan(maxLineSpan) },
    ) {
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
    }
  }
}

@Composable
internal fun GridBook(
  book: BookOverviewItemViewState,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
) {
  BookCard(
    bookId = book.id,
    onBookClick = onBookClick,
    onBookLongClick = onBookLongClick,
  ) {
    Column(
      modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp),
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(4f / 3f)
          .sharedCoverElementModifier(book.id)
          .clip(MaterialTheme.shapes.large)
          .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
      ) {
        AsyncImage(
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
          model = book.cover,
          placeholder = painterResource(id = UiR.drawable.album_art),
          error = painterResource(id = UiR.drawable.album_art),
          contentDescription = null,
        )
      }

      Spacer(Modifier.height(4.dp))

      Text(
        text = book.name,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
      )

      BookRemainingProgressRow(
        remainingTime = book.remainingTime,
        progress = book.progress,
      )

      Spacer(Modifier.height(8.dp))
      BookProgressIndicator(progress = book.progress)
    }
  }
}

@Composable
internal fun gridColumnCount(): Int {
  val displayMetrics = LocalResources.current.displayMetrics
  val widthPx = displayMetrics.widthPixels.toFloat()
  val desiredPx = with(LocalDensity.current) {
    180.dp.toPx()
  }
  val columns = (widthPx / desiredPx).roundToInt()
  return columns.coerceAtLeast(2)
}

@Composable
@Preview(widthDp = 200)
private fun GridBookPreviewWithProgress() {
  GridBook(BookOverviewPreviewParameterProvider().book().copy(progress = 0.66f), {}, {})
}

@Composable
@Preview(widthDp = 200)
private fun GridBookPreviewWithoutProgress() {
  GridBook(BookOverviewPreviewParameterProvider().book().copy(progress = 0f), {}, {})
}
