package voice.features.bookOverview.editGenre

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.ui.NoAnimationAlertDialog
import voice.core.strings.R as StringsR

@Composable
internal fun EditBookGenreDialog(
  onDismissEditGenreClick: () -> Unit,
  onConfirmEditGenre: () -> Unit,
  viewState: EditBookGenreState,
  onUpdateEditGenre: (String) -> Unit,
) {
  NoAnimationAlertDialog(
    onDismissRequest = onDismissEditGenreClick,
    title = {
      Text(text = stringResource(StringsR.string.book_edit_genre_title))
    },
    confirmButton = {
      Button(
        onClick = onConfirmEditGenre,
        enabled = viewState.confirmButtonEnabled,
      ) {
        Text(stringResource(id = StringsR.string.common_dialog_confirm))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismissEditGenreClick) {
        Text(stringResource(id = StringsR.string.common_dialog_cancel))
      }
    },
    text = {
      Column {
        TextField(
          value = viewState.genre,
          onValueChange = onUpdateEditGenre,
          label = {
            Text(stringResource(StringsR.string.book_edit_genre_label))
          },
        )

        // Tapping one fills the field rather than confirming outright, so a near-miss can still
        // be corrected before it is saved.
        val suggestions = viewState.suggestions.filter { it != viewState.genre.trim() }
        if (suggestions.isNotEmpty()) {
          Text(
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            text = stringResource(StringsR.string.book_edit_genre_existing),
            style = MaterialTheme.typography.labelMedium,
          )
          LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
            items(suggestions) { suggestion ->
              Text(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onUpdateEditGenre(suggestion) }
                  .padding(vertical = 12.dp),
                text = suggestion,
                style = MaterialTheme.typography.bodyLarge,
              )
            }
          }
        }
      }
    },
  )
}
