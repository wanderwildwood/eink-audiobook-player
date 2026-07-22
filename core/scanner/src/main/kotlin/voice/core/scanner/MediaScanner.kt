package voice.core.scanner

import dev.zacsweers.metro.Inject
import voice.core.data.BookId
import voice.core.data.audioFileCount
import voice.core.data.folders.FolderType
import voice.core.data.isAudioFile
import voice.core.data.repo.BookContentRepo
import voice.core.documentfile.CachedDocumentFile
import voice.core.documentfile.walk
import voice.core.logging.api.Logger

@Inject
internal class MediaScanner(
  private val contentRepo: BookContentRepo,
  private val chapterParser: ChapterParser,
  private val bookParser: BookParser,
  private val deviceHasPermissionBug: DeviceHasStoragePermissionBug,
) {

  suspend fun scan(folders: Map<FolderType, List<CachedDocumentFile>>) {
    val files = folders.flatMap { (folderType, files) ->
      when (folderType) {
        FolderType.SingleFile, FolderType.SingleFolder -> {
          files.map { ScannedFile(it, folderName = null) }
        }
        FolderType.Root -> {
          files.flatMap { file ->
            file.children.map { ScannedFile(it, folderName = null) }
          }
        }
        FolderType.Author -> {
          files.flatMap { folder ->
            folder.children.flatMap { author ->
              if (author.isFile) {
                listOf(ScannedFile(author, folderName = null))
              } else {
                author.children.map { book ->
                  ScannedFile(book, folderName = author.name)
                }
              }
            }
          }
        }
      }
    }

    contentRepo.setAllInactiveExcept(files.map { BookId(it.file.uri) })

    val probeFile = folders.values.flatten().findProbeFile()
    if (probeFile != null) {
      if (deviceHasPermissionBug.checkForBugAndSet(probeFile)) {
        Logger.w("Device has permission bug, aborting scan! Probed $probeFile")
        return
      }
    }

    files
      .sortedBy { it.file.audioFileCount() }
      .forEach { scannedFile ->
        scan(scannedFile.file, scannedFile.folderName)
      }
  }

  private data class ScannedFile(
    val file: CachedDocumentFile,
    val folderName: String?,
  )

  private fun List<CachedDocumentFile>.findProbeFile(): CachedDocumentFile? {
    return asSequence().flatMap { it.walk() }
      .firstOrNull { child ->
        child.isAudioFile() && child.uri.authority == "com.android.externalstorage.documents"
      }
  }

  private suspend fun scan(
    file: CachedDocumentFile,
    folderName: String?,
  ) {
    val parseResult = chapterParser.parse(file)
    val chapters = parseResult.chapters
    if (chapters.isEmpty()) return

    val content = bookParser.parseAndStore(chapters, file, parseResult.firstChapterMetadata, folderName)

    val chapterIds = chapters.map { it.id }
    val currentChapterGone = content.currentChapter !in chapterIds
    val currentChapter = if (currentChapterGone) chapterIds.first() else content.currentChapter
    val positionInChapter = if (currentChapterGone) 0 else content.positionInChapter
    val updated = content.copy(
      chapters = chapterIds,
      currentChapter = currentChapter,
      positionInChapter = positionInChapter,
      isActive = true,
      folderName = folderName ?: content.folderName,
    )
    if (content != updated) {
      validateIntegrity(updated, chapters)
      contentRepo.put(updated)
    }
  }
}
