package audiobook.features.folderPicker.addcontent

import android.net.Uri
import android.provider.DocumentsContract
import androidx.datastore.core.DataStore
import audiobook.core.common.DispatcherProvider
import audiobook.core.data.folders.AudiobookFolders
import audiobook.core.data.folders.FolderType
import audiobook.core.data.store.OnboardingCompletedStore
import audiobook.core.documentfile.CachedDocumentFileFactory
import audiobook.features.folderPicker.detectFolderType
import audiobook.features.folderPicker.folderPicker.FileTypeSelection
import audiobook.navigation.Destination
import audiobook.navigation.Navigator
import audiobook.navigation.Origin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AssistedInject
class AddContentViewModel(
  private val audiobookFolders: AudiobookFolders,
  private val navigator: Navigator,
  private val documentFileFactory: CachedDocumentFileFactory,
  private val dispatcherProvider: DispatcherProvider,
  @OnboardingCompletedStore
  private val onboardingCompletedStore: DataStore<Boolean>,
  @Assisted
  private val origin: Origin,
) {

  private val scope = MainScope()

  internal fun add(
    uri: Uri,
    type: FileTypeSelection,
  ) {
    scope.launch {
      val folderType = when (type) {
        FileTypeSelection.File -> FolderType.SingleFile
        // Reading the folder to work out its shape is disk work over SAF, so it stays off the
        // main thread. Asking instead was a whole screen that only ever restated what the files
        // already say.
        FileTypeSelection.Folder -> withContext(dispatcherProvider.io) {
          // The picker hands back a tree uri, which cannot be queried as a document: asking for
          // its contents returns nothing rather than failing, so the folder reads as empty and
          // every library was detected as a single book. Everything else that walks a stored
          // folder converts it the same way first - see AudiobookFoldersImpl.toDocumentFile.
          val documentUri = DocumentsContract.buildDocumentUriUsingTree(
            uri,
            DocumentsContract.getTreeDocumentId(uri),
          )
          documentFileFactory.create(documentUri).detectFolderType()
        }
      }
      audiobookFolders.add(uri, folderType)
      // Adding the first book is the whole of onboarding, so finishing it is what marks
      // onboarding done - there is no completion screen left to do it.
      if (origin == Origin.Onboarding) {
        onboardingCompletedStore.updateData { true }
      }
      navigator.setRoot(Destination.BookOverview)
    }
  }

  internal fun back() {
    navigator.goBack()
  }

  @AssistedFactory
  interface Factory {
    fun create(origin: Origin): AddContentViewModel
  }
}
