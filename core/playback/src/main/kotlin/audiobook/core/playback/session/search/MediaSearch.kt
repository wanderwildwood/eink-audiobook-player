package audiobook.core.playback.session.search

data class MediaSearch(
  val query: String? = null,
  val mediaFocus: String? = null,
  val album: String? = null,
  val artist: String? = null,
)
