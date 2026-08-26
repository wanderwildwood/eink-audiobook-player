package audiobook.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import audiobook.core.data.sleeptimer.ShakeSensitivity
import audiobook.core.ui.NoAnimationAlertDialog
import audiobook.core.ui.icons.Icons
import audiobook.core.strings.R as StringsR

@Composable
internal fun ShakeSensitivityRow(
  sensitivity: ShakeSensitivity,
  onClick: () -> Unit,
) {
  ListItem(
    modifier = Modifier
      .clickable { onClick() }
      .fillMaxWidth(),
    leadingContent = {
      Icon(
        imageVector = Icons.AutoAwesome,
        contentDescription = stringResource(StringsR.string.settings_shake_sensitivity_title),
      )
    },
    headlineContent = {
      Text(text = stringResource(StringsR.string.settings_shake_sensitivity_title))
    },
    supportingContent = {
      Text(text = stringResource(sensitivity.labelResId))
    },
  )
}

@Composable
internal fun ShakeSensitivityDialog(
  current: ShakeSensitivity,
  onSelect: (ShakeSensitivity) -> Unit,
  onDismiss: () -> Unit,
) {
  NoAnimationAlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(stringResource(StringsR.string.settings_shake_sensitivity_title))
    },
    text = {
      Column {
        // Listed most sensitive first: someone opening this dialog is far more likely to be
        // here because shaking didn't work than because it worked too easily.
        listOf(ShakeSensitivity.High, ShakeSensitivity.Medium, ShakeSensitivity.Low)
          .forEach { sensitivity ->
            ShakeSensitivityOptionRow(
              selected = current == sensitivity,
              label = stringResource(sensitivity.labelResId),
              description = stringResource(sensitivity.descriptionResId),
              onClick = { onSelect(sensitivity) },
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

@Composable
private fun ShakeSensitivityOptionRow(
  selected: Boolean,
  label: String,
  description: String,
  onClick: () -> Unit,
) {
  ListItem(
    modifier = Modifier.clickable { onClick() },
    leadingContent = {
      RadioButton(selected = selected, onClick = onClick)
    },
    headlineContent = {
      Text(label)
    },
    supportingContent = {
      Text(description)
    },
  )
}

private val ShakeSensitivity.labelResId: Int
  get() = when (this) {
    ShakeSensitivity.Low -> StringsR.string.settings_shake_sensitivity_low
    ShakeSensitivity.Medium -> StringsR.string.settings_shake_sensitivity_medium
    ShakeSensitivity.High -> StringsR.string.settings_shake_sensitivity_high
  }

private val ShakeSensitivity.descriptionResId: Int
  get() = when (this) {
    ShakeSensitivity.Low -> StringsR.string.settings_shake_sensitivity_low_description
    ShakeSensitivity.Medium -> StringsR.string.settings_shake_sensitivity_medium_description
    ShakeSensitivity.High -> StringsR.string.settings_shake_sensitivity_high_description
  }
