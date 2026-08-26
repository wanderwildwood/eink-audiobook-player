package audiobook.features.folderPicker

import audiobook.core.data.folders.FolderType
import audiobook.core.data.isAudioFile
import audiobook.core.documentfile.CachedDocumentFile

/**
 * Works out how a chosen folder is laid out, so that nobody has to be asked.
 *
 * Three shapes account for every library worth supporting: audio sitting directly in the chosen
 * folder (the folder is one book), folders that hold audio (each folder is a book), and folders
 * whose own subfolders hold the audio (the first level is authors).
 *
 * Only a sample of the subfolders is inspected. Reading every one over SAF costs seconds on a
 * large library, and a library that changes shape halfway through is not a library anyone has.
 * A folder can look like both an author and a book - a series folder beside loose chapter files -
 * so the shapes are counted rather than matched, and the more common one wins.
 */
internal fun CachedDocumentFile.detectFolderType(): FolderType {
  val directories = children.filter { it.isDirectory }
  val holdsAudioDirectly = children.any { it.isAudioFile() }

  if (directories.isEmpty()) return FolderType.SingleFolder

  val sample = directories.take(SAMPLE_SIZE)
  var authorShaped = 0
  var bookShaped = 0
  for (directory in sample) {
    val grandchildren = directory.children
    if (grandchildren.any { it.isDirectory && it.children.any { child -> child.isAudioFile() } }) {
      authorShaped++
    }
    if (grandchildren.any { it.isAudioFile() }) {
      bookShaped++
    }
  }

  return when {
    authorShaped > bookShaped -> FolderType.Author
    bookShaped > 0 -> FolderType.Root
    // Subfolders with no audio at either level. If the folder itself holds audio it is one book
    // with some clutter beside it; otherwise assume the subfolders are books that are simply
    // deeper than we sampled.
    holdsAudioDirectly -> FolderType.SingleFolder
    else -> FolderType.Root
  }
}

private const val SAMPLE_SIZE = 8
