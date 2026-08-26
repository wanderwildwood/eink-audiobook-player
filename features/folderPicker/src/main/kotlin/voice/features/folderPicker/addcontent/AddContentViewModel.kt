package voice.features.folderPicker.addcontent

import android.net.Uri
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import voice.core.common.DispatcherProvider
import voice.core.data.folders.AudiobookFolders
import voice.core.data.folders.FolderType
import voice.core.data.store.OnboardingCompletedStore
import voice.core.documentfile.CachedDocumentFileFactory
import voice.features.folderPicker.detectFolderType
import voice.features.folderPicker.folderPicker.FileTypeSelection
import voice.navigation.Destination
import voice.navigation.Navigator
import voice.navigation.Origin

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
          documentFileFactory.create(uri).detectFolderType()
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
