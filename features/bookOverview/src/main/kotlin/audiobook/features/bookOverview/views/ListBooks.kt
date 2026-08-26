package audiobook.features.bookOverview.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import audiobook.core.data.BookId
import audiobook.core.ui.sharedCoverElementModifier
import audiobook.features.bookOverview.overview.BookOverviewItemViewState
import audiobook.features.bookOverview.overview.LibrarySection
import audiobook.core.ui.R as UiR

@Composable
internal fun ListBooks(
  sections: List<LibrarySection>,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  onFolderClick: (String?) -> Unit,
  showPermissionBugCard: Boolean,
  onPermissionBugCardClick: () -> Unit,
) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(4.dp),
    contentPadding = PaddingValues(top = 24.dp, start = 8.dp, end = 8.dp, bottom = 16.dp),
  ) {
    if (showPermissionBugCard) {
      item {
        PermissionBugCard(onPermissionBugCardClick)
      }
    }
    sections.forEachIndexed { sectionIndex, section ->
      when (section) {
        is LibrarySection.Folders -> {
          if (section.folders.isNotEmpty()) {
            // The "Library" heading lives in the top bar now.
            items(
              items = section.folders,
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
              stickyHeader(
                key = "books-header-$sectionIndex",
                contentType = "header",
              ) {
                SectionHeader(
                  headerRes = headerRes,
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                )
              }
            }
            items(
              items = section.books,
              key = { it.id.value },
              contentType = { "book" },
            ) { book ->
              ListBookRow(book = book, onBookClick = onBookClick, onBookLongClick = onBookLongClick)
            }
          }
        }
      }
    }
    item {
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
    }
  }
}

@Composable
internal fun ListBookRow(
  book: BookOverviewItemViewState,
  onBookClick: (BookId) -> Unit,
  onBookLongClick: (BookId) -> Unit,
  modifier: Modifier = Modifier,
) {
  BookCard(
    bookId = book.id,
    onBookClick = onBookClick,
    onBookLongClick = onBookLongClick,
    modifier = modifier,
  ) {
    Column(Modifier.padding()) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
          Modifier
            // The cover used to set this row's height and its left inset. Without one the text
            // needs its own padding, or it sits against the card edge and the rows read cramped.
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
            .weight(1f),
        ) {
          if (book.author != null) {
            Text(
              text = book.author.toUpperCase(LocaleList.current),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
            )
          }

          Text(
            text = book.name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
          )

          BookRemainingProgressRow(
            modifier = Modifier
              .padding(end = 12.dp),
            remainingTime = book.remainingTime,
            progress = book.progress,
            remainingTimeMaxLines = 1,
            progressMaxLines = 1,
          )
        }
      }

      if (book.progress > 0.05f) {
        Spacer(Modifier.size(0.dp))
        BookProgressIndicator(
          progress = book.progress,
          modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .height(4.dp),
          color = MaterialTheme.colorScheme.primary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
      }
    }
  }
}

@Composable
@Preview
private fun ListBookRowPreviewWithProgress() {
  ListBookRow(BookOverviewPreviewParameterProvider().book().copy(progress = 0.6f), {}, {})
}

@Composable
@Preview
private fun ListBookRowPreviewWithoutProgress() {
  ListBookRow(BookOverviewPreviewParameterProvider().book().copy(progress = 0f), {}, {})
}
