package audiobook.core.scanner

import audiobook.core.data.Book
import audiobook.core.data.BookContent
import audiobook.core.data.BookId
import audiobook.core.data.Chapter
import audiobook.core.data.repo.BookContentRepo
import audiobook.core.data.repo.getOrPut
import audiobook.core.data.toUri
import audiobook.core.documentfile.CachedDocumentFile
import audiobook.core.documentfile.CachedDocumentFileFactory
import audiobook.core.logging.api.Logger
import dev.zacsweers.metro.Inject
import java.time.Instant

@Inject
internal class BookParser(
  private val contentRepo: BookContentRepo,
  private val mediaAnalyzer: MediaAnalyzer,
  private val fileFactory: CachedDocumentFileFactory,
) {

  suspend fun parseAndStore(
    chapters: List<Chapter>,
    file: CachedDocumentFile,
    firstChapterMetadata: Metadata?,
    folderName: String?,
  ): BookContent {
    val id = BookId(file.uri)
    return contentRepo.getOrPut(id) {
      val analyzed = firstChapterMetadata
        ?: mediaAnalyzer.analyze(fileFactory.create(chapters.first().id.toUri()))
      parse(chapters, id, analyzed, file, folderName)
    }
  }

  fun parse(
    chapters: List<Chapter>,
    id: BookId,
    analyzed: Metadata?,
    file: CachedDocumentFile,
    folderName: String?,
  ): BookContent {
    return BookContent(
      id = id,
      isActive = true,
      addedAt = Instant.now(),
      author = analyzed?.artist,
      lastPlayedAt = Instant.EPOCH,
      name = analyzed?.album
        ?: analyzed?.title?.takeIf { file.isFile }
        ?: file.bookName(),
      playbackSpeed = 1F,
      skipSilence = false,
      chapters = chapters.map { it.id },
      positionInChapter = 0L,
      currentChapter = chapters.first().id,
      cover = null,
      gain = 0F,
      genre = analyzed?.genre,
      narrator = analyzed?.narrator,
      series = analyzed?.series,
      part = analyzed?.part,
      folderName = folderName,
    ).also {
      validateIntegrity(it, chapters)
    }
  }

  private fun CachedDocumentFile.bookName(): String {
    val fileName = name
    return if (fileName == null) {
      uri.toString()
        .removePrefix("/storage/emulated/0/")
        .removePrefix("/storage/emulated/")
        .removePrefix("/storage/")
        .also {
          Logger.w("Could not parse fileName from $this. Fallback to $it")
        }
    } else {
      if (isFile) {
        fileName.substringBeforeLast(".")
      } else {
        fileName
      }
    }
  }
}

internal fun validateIntegrity(
  content: BookContent,
  chapters: List<Chapter>,
) {
  // the init block performs integrity validation
  @Suppress("RETURN_VALUE_NOT_USED")
  Book(content, chapters)
}
