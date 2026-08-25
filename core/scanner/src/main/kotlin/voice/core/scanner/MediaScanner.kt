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
    // Books are parsed once and then left alone, so a tag written to the files after a book was
    // first scanned would never reach it. Genre is taken from the file when the book does not
    // already hold a real one - so a genre set by hand in the app survives, while the junk most
    // rippers leave behind does not stand in the way of a real one arriving.
    val genre = content.genre.asRealGenre() ?: parseResult.firstChapterMetadata?.genre.asRealGenre()

    val updated = content.copy(
      chapters = chapterIds,
      currentChapter = currentChapter,
      positionInChapter = positionInChapter,
      isActive = true,
      folderName = folderName ?: content.folderName,
      genre = genre,
    )
    if (content != updated) {
      validateIntegrity(updated, chapters)
      contentRepo.put(updated)
    }
  }
}

/**
 * The genre tag as something worth shelving a book under, or null.
 *
 * Most audiobook files carry a genre that says nothing about the book: a bare ID3v1 code left as
 * "(101)", or the format itself - "Audiobook", "Spoken Word", "Speech". Treating those as a real
 * value would mean a library filed under one useless heading, and would block a proper genre from
 * ever replacing them.
 */
private fun String?.asRealGenre(): String? {
  val trimmed = this?.trim().orEmpty()
  if (trimmed.isEmpty()) return null
  if (ID3_NUMERIC_GENRE.matches(trimmed)) return null
  if (trimmed.lowercase() in NON_GENRES) return null
  // A list is not a shelf. "Fiction/Literature/Science Fiction" and "Electronic, Folk, Modern
  // Classical, Ambient" are what a ripper wrote across a whole batch, not somewhere to file one
  // book - and a genre chosen here or in the app never contains either separator.
  if (trimmed.any { it == ',' || it == '/' }) return null
  // "Non Fiction Audio Book" and friends: the format again, just spelled out.
  if (trimmed.lowercase().let { "audiobook" in it || "audio book" in it }) return null
  return trimmed
}

private val ID3_NUMERIC_GENRE = Regex("""^\(?\d{1,3}\)?$""")

private val NON_GENRES = setOf(
  "audiobook",
  "audio book",
  "audiobooks",
  "books & spoken",
  "spoken word",
  "spoken",
  "speech",
  "other",
  "unknown",
  "netlibrary audiobook",

  // How it was recorded, not what it is about. If one of these ever wants to be a real shelf,
  // take it off this list - a genre typed in the app is checked against it too.
  "interview",
  "lecture",
  "fiction & literature",
)
