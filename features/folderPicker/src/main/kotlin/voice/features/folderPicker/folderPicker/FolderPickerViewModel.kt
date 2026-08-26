package voice.features.folderPicker.folderPicker

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import voice.core.data.folders.AudiobookFolders
import voice.core.data.folders.FolderType
import voice.core.documentfile.nameWithoutExtension
import voice.core.featureflag.FeatureFlag
import voice.core.scanner.MediaScanTrigger
import voice.navigation.Destination
import voice.navigation.Navigator
import voice.navigation.Origin

@Inject
class FolderPickerViewModel(
  private val audiobookFolders: AudiobookFolders,
  private val navigator: Navigator,
  private val mediaScanTrigger: MediaScanTrigger,
) {

  /**
   * Manual rescan. restartIfScanning so tapping it again actually restarts rather than
   * no-opping while a scan is already running.
   */
  fun rescan() {
    mediaScanTrigger.scan(restartIfScanning = true)
  }

  @Composable
  fun viewState(): FolderPickerViewState {
    val folders: List<FolderPickerViewState.Item> by remember {
      items()
    }.collectAsState(initial = emptyList())
    return FolderPickerViewState(folders)
  }

  private fun items(): Flow<List<FolderPickerViewState.Item>> {
    return audiobookFolders.all().map { folders ->
      withContext(Dispatchers.IO) {
        folders.flatMap { (folderType, folders) ->
          folders.map { (documentFile, uri) ->
            FolderPickerViewState.Item(
              name = documentFile.nameWithoutExtension(),
              id = uri,
              folderType = folderType,
            )
          }
        }.sortedDescending()
      }
    }
  }

  internal fun onCloseClick() {
    navigator.goBack()
  }

  internal fun add() {
    navigator.goTo(
      Destination.AddContent(
        Origin.Default,
      ),
    )
  }

  fun removeFolder(item: FolderPickerViewState.Item) {
    audiobookFolders.remove(item.id, item.folderType)
  }

  /**
   * Correct how a folder is read when the detection guessed wrong. The folder is registered
   * under its type, so changing it means dropping the old registration and adding the same uri
   * back under the new one. A rescan follows because every book in that folder is now a
   * different shape.
   */
  fun changeFolderType(
    item: FolderPickerViewState.Item,
    folderType: FolderType,
  ) {
    if (folderType == item.folderType) return
    audiobookFolders.remove(item.id, item.folderType)
    audiobookFolders.add(item.id, folderType)
    mediaScanTrigger.scan(restartIfScanning = true)
  }
}
