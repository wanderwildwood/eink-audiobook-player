package audiobook.features.folderPicker

import audiobook.core.data.folders.FolderType
import audiobook.core.documentfile.FileBasedDocumentFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class DetectFolderTypeTest {

  @get:Rule
  val testFolder = TemporaryFolder()

  private fun detect(folder: File): FolderType = FileBasedDocumentFile(folder).detectFolderType()

  private fun File.book(vararg chapters: String) {
    mkdirs()
    chapters.forEach { check(File(this, it).createNewFile()) }
  }

  @Test
  fun authorFoldersHoldingBookFolders() {
    val root = testFolder.newFolder("Audiobooks")
    File(root, "Philip Pullman/03 The Amber Spyglass").book("Amber Spyglass 01.mp3")
    File(root, "Brian Jacques/Book 16 - Loamhedge").book("01.m4b")

    assertEquals(expected = FolderType.Author, actual = detect(root))
  }

  @Test
  fun bookFoldersHoldingAudioDirectly() {
    val root = testFolder.newFolder("Audiobooks")
    File(root, "An Introduction to Zen Buddhism").book("An Introduction to Zen Buddhism.mp3")
    File(root, "The Subtle Knife").book("Subtle Knife 01.mp3")

    assertEquals(expected = FolderType.Root, actual = detect(root))
  }

  @Test
  fun audioSittingDirectlyInTheChosenFolder() {
    val folder = testFolder.newFolder("The Amber Spyglass")
    folder.book("01.mp3", "02.mp3")

    assertEquals(expected = FolderType.SingleFolder, actual = detect(folder))
  }

  @Test
  fun aFolderThatCannotBeReadIsNotOneBook() {
    // A folder is reported as empty both when it is empty and when reading it failed. It used to
    // be called a single book either way, which is how a 4,682-file library became one entry: the
    // add flow queried a tree uri that answers nothing, so the root read as having no children.
    val empty = testFolder.newFolder("Unreadable")

    assertEquals(expected = FolderType.Root, actual = detect(empty))
  }

  @Test
  fun looseAudioBesideSubfoldersIsStillOneBook() {
    val folder = testFolder.newFolder("A Book With Extras")
    folder.book("01.mp3")
    check(File(folder, "artwork").mkdirs())

    assertEquals(expected = FolderType.SingleFolder, actual = detect(folder))
  }

  @Test
  fun theMoreCommonShapeWinsWhenAFolderLooksLikeBoth() {
    val root = testFolder.newFolder("Audiobooks")
    // A series folder whose own subfolders hold the audio, beside two plain book folders.
    File(root, "Redwall/Book 01 - Redwall").book("01.mp3")
    File(root, "Dune").book("01.mp3")
    File(root, "Siddhartha").book("01.mp3")

    assertEquals(expected = FolderType.Root, actual = detect(root))
  }
}
