package voice.features.folderPicker.folderPicker

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.data.folders.FolderType
import voice.core.ui.NoAnimationAlertDialog
import voice.core.ui.icons.VoiceIcons
import voice.features.folderPicker.FolderTypeIcon
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.strings.R as StringsR

@ContributesTo(AppScope::class)
interface FolderPickerGraph {
  val folderPickerViewModel: FolderPickerViewModel
}

@ContributesTo(AppScope::class)
interface FolderPickerProvider {

  @Provides
  @IntoSet
  fun folderPickerNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.FolderPicker> { key ->
    NavEntry(key) {
      FolderOverview()
    }
  }
}

@Composable
fun FolderOverview() {
  val viewModel: FolderPickerViewModel = retain<FolderPickerViewModel> {
    rootGraphAs<FolderPickerGraph>()
      .folderPickerViewModel
  }
  val viewState = viewModel.viewState()
  FolderOverviewView(
    viewState = viewState,
    onAddClick = {
      viewModel.add()
    },
    onDeleteClick = {
      viewModel.removeFolder(it)
    },
    onCloseClick = viewModel::onCloseClick,
    onRescanClick = viewModel::rescan,
    onChangeFolderType = { item, folderType ->
      viewModel.changeFolderType(item, folderType)
    },
  )
}

@Composable
private fun FolderOverviewView(
  viewState: FolderPickerViewState,
  onAddClick: () -> Unit,
  onDeleteClick: (FolderPickerViewState.Item) -> Unit,
  onCloseClick: () -> Unit,
  onRescanClick: () -> Unit,
  onChangeFolderType: (FolderPickerViewState.Item, FolderType) -> Unit,
) {
  var changingTypeOf: FolderPickerViewState.Item? by remember { mutableStateOf(null) }
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      MediumTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
          Text(text = stringResource(StringsR.string.library_folders_title))
        },
        navigationIcon = {
          IconButton(onClick = onCloseClick) {
            Icon(
              imageVector = VoiceIcons.ArrowBack,
              contentDescription = stringResource(StringsR.string.common_action_close),
            )
          }
        },
        actions = {
          TextButton(onClick = onRescanClick) {
            Text(text = stringResource(StringsR.string.library_scan_action))
          }
        },
      )
    },
    floatingActionButton = {
      if (viewState.showActions) {
        val text = stringResource(id = StringsR.string.common_action_add)
        ExtendedFloatingActionButton(
          text = {
            Text(text)
          },
          onClick = {
            onAddClick()
          },
          icon = {
            Icon(
              imageVector = VoiceIcons.Add,
              contentDescription = text,
            )
          },
        )
      }
    },
  ) { contentPadding ->
    changingTypeOf?.let { item ->
      FolderTypeDialog(
        current = item.folderType,
        onSelect = { folderType ->
          onChangeFolderType(item, folderType)
          changingTypeOf = null
        },
        onDismiss = { changingTypeOf = null },
      )
    }
    LazyColumn(contentPadding = contentPadding) {
      item { Spacer(modifier = Modifier.size(16.dp)) }
      items(viewState.items) { item ->
        ListItem(
          // A single audio file has no structure to get wrong, so only folders open the dialog.
          modifier = if (item.folderType != FolderType.SingleFile) {
            Modifier.clickable { changingTypeOf = item }
          } else {
            Modifier
          },
          leadingContent = {
            FolderTypeIcon(folderType = item.folderType)
          },
          trailingContent = if (viewState.showActions) {
            {
              IconButton(
                onClick = {
                  onDeleteClick(item)
                },
                content = {
                  Icon(
                    imageVector = VoiceIcons.Delete,
                    contentDescription = stringResource(StringsR.string.common_action_delete),
                  )
                },
              )
            }
          } else {
            null
          },
          headlineContent = {
            Text(text = item.name)
          },
        )
      }
    }
  }
}

/**
 * Corrects how a folder is read. The app works the layout out on its own when a folder is added,
 * which is right nearly always and silent when it is - this is the door for the times it is not,
 * so that a wrong guess does not mean rebuilding a library by hand.
 */
@Composable
private fun FolderTypeDialog(
  current: FolderType,
  onSelect: (FolderType) -> Unit,
  onDismiss: () -> Unit,
) {
  NoAnimationAlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(stringResource(StringsR.string.folder_type_change_title))
    },
    text = {
      Column {
        // SingleFile is not offered: a folder is never one file.
        listOf(FolderType.Author, FolderType.Root, FolderType.SingleFolder).forEach { folderType ->
          ListItem(
            modifier = Modifier.clickable { onSelect(folderType) },
            leadingContent = {
              RadioButton(
                selected = folderType == current,
                onClick = { onSelect(folderType) },
              )
            },
            headlineContent = {
              Text(stringResource(folderType.labelRes()))
            },
          )
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(StringsR.string.common_dialog_ok))
      }
    },
  )
}

private fun FolderType.labelRes(): Int = when (this) {
  FolderType.Author -> StringsR.string.folder_mode_author_title
  FolderType.Root -> StringsR.string.folder_mode_root_title
  FolderType.SingleFile,
  FolderType.SingleFolder,
  -> StringsR.string.folder_mode_single_title
}

@Suppress("ktlint:compose:preview-public-check")
@Composable
@Preview
fun FolderOverviewPreview() {
  FolderOverviewView(
    viewState = FolderPickerViewState(
      items = listOf(
        FolderPickerViewState.Item(
          name = "My Audiobooks",
          id = Uri.EMPTY,
          folderType = FolderType.Root,
        ),
        FolderPickerViewState.Item(
          name = "Bobiverse 1-4",
          id = Uri.EMPTY,
          folderType = FolderType.SingleFolder,
        ),
        FolderPickerViewState.Item(
          name = "Harry Potter 1",
          id = Uri.EMPTY,
          folderType = FolderType.SingleFile,
        ),
      ),
    ),
    onAddClick = { },
    onDeleteClick = {},
    onCloseClick = {},
    onRescanClick = {},
    onChangeFolderType = { _, _ -> },
  )
}
