package audiobook.features.bookOverview.overview

import androidx.compose.runtime.Immutable
import audiobook.core.data.Book
import audiobook.core.data.BookId
import audiobook.core.logging.api.Logger
import audiobook.core.ui.formatTime

@Immutable
data class BookOverviewItemViewState(
  val name: String,
  val author: String?,
  val progress: Float,
  val id: BookId,
  val remainingTime: String,
)

internal fun Book.toItemViewState() = BookOverviewItemViewState(
  name = content.name,
  author = content.author,
  id = id,
  progress = progress(),
  remainingTime = formatTime(duration - position),
)

private fun Book.progress(): Float {
  val globalPosition = position
  val totalDuration = duration
  val progress = globalPosition.toFloat() / totalDuration.toFloat()
  if (progress < 0F) {
    Logger.w("Couldn't determine progress for book=$this")
  }
  return progress.coerceIn(0F, 1F)
}
