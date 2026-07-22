package voice.features.bookOverview.overview

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

/**
 * What the main library screen renders below its top bar, resolved from the user's
 * [voice.core.data.LibraryOrganization] preference. [Folders] is the long-standing default
 * (author-folder browsing); [Books] renders a flat run of individual books, optionally split
 * into multiple headered groups (used for the "by status" organization).
 */
@Immutable
sealed interface LibrarySection {

  data class Folders(val folders: List<AuthorFolderViewState>) : LibrarySection

  data class Books(
    @param:StringRes val headerRes: Int?,
    val books: List<BookOverviewItemViewState>,
  ) : LibrarySection
}
