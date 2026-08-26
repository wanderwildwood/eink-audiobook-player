package audiobook.core.scanner

import audiobook.core.data.Chapter
import audiobook.core.data.ChapterId
import audiobook.core.data.isAudioFile
import audiobook.core.data.repo.ChapterRepo
import audiobook.core.data.repo.getOrPut
import audiobook.core.documentfile.CachedDocumentFile
import dev.zacsweers.metro.Inject
import java.time.Instant

internal data class ChapterParseResult(
  val chapters: List<Chapter>,
  val firstChapterMetadata: Metadata?,
)

@Inject
internal class ChapterParser(
  private val chapterRepo: ChapterRepo,
  private val mediaAnalyzer: MediaAnalyzer,
) {

  suspend fun parse(documentFile: CachedDocumentFile): ChapterParseResult {
    val result = mutableListOf<Chapter>()
    val analyzedMetadata = mutableMapOf<ChapterId, Metadata>()

    suspend fun parseChapters(file: CachedDocumentFile) {
      if (file.isAudioFile()) {
        val id = ChapterId(file.uri)
        val chapter = chapterRepo.getOrPut(
          id = id,
          lastModified = Instant.ofEpochMilli(file.lastModified),
          fileSize = file.length,
        ) {
          val metaData = mediaAnalyzer.analyze(file) ?: return@getOrPut null
          analyzedMetadata[id] = metaData
          Chapter(
            id = id,
            duration = metaData.duration,
            fileLastModified = Instant.ofEpochMilli(file.lastModified),
            name = metaData.title ?: metaData.fileName,
            markData = metaData.chapters,
            fileSize = file.length,
          )
        }
        if (chapter != null) {
          result.add(chapter)
        }
      } else if (file.isDirectory) {
        file.children
          .forEach {
            parseChapters(it)
          }
      }
    }

    parseChapters(file = documentFile)
    val chapters = result.sorted()
    return ChapterParseResult(
      chapters = chapters,
      firstChapterMetadata = chapters.firstOrNull()?.let { analyzedMetadata[it.id] },
    )
  }
}
