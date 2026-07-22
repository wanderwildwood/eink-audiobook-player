package voice.features.bookOverview.overview

import androidx.compose.runtime.Immutable

/**
 * A row in the "Browse" section of the library, grouping books by the on-disk author folder
 * they were scanned from. [folderName] is null for books that weren't scanned via an
 * author-folder structure, rendered as an "Other" bucket.
 */
@Immutable
data class AuthorFolderViewState(
  val folderName: String?,
  val bookCount: Int,
)
