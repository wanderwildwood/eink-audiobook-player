package voice.features.settings.views

import androidx.annotation.PluralsRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import voice.core.ui.NoAnimationAlertDialog
import voice.core.ui.icons.VoiceIcons
import voice.core.strings.R as StringsR

/** Seconds a press moves by. Fine enough to land on a value, coarse enough to get there. */
private const val STEP_SECONDS = 5

/**
 * Steps onto a multiple of [STEP_SECONDS] rather than adding to whatever odd number is set, so
 * pressing up from 3 gives 5 and not 8. The ends of the range are still reachable exactly.
 */
private fun Int.nextStep(max: Int): Int = (((this / STEP_SECONDS) + 1) * STEP_SECONDS).coerceAtMost(max)
private fun Int.previousStep(min: Int): Int = (((this - 1) / STEP_SECONDS) * STEP_SECONDS).coerceAtLeast(min)

@Composable
fun TimeSettingDialog(
  title: String,
  currentSeconds: Int,
  @PluralsRes textPluralRes: Int,
  minSeconds: Int,
  maxSeconds: Int,
  onSecondsConfirm: (Int) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Was a slider. E-ink smears under a dragged thumb and the panel cannot keep up with the
  // finger, so the value is stepped with buttons instead - same reasoning as the speed dialog.
  var seconds by remember { mutableIntStateOf(currentSeconds) }
  NoAnimationAlertDialog(
    onDismissRequest = onDismiss,
    modifier = modifier,
    title = {
      Text(text = title)
    },
    text = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          enabled = seconds > minSeconds,
          onClick = { seconds = seconds.previousStep(minSeconds) },
        ) {
          Icon(
            imageVector = VoiceIcons.Remove,
            contentDescription = stringResource(StringsR.string.common_dialog_shorter),
          )
        }

        // The amount is what this dialog is about, so it is set to be read at a glance rather
        // than squinted at under the controls.
        Text(
          text = LocalResources.current.getQuantityString(textPluralRes, seconds, seconds),
          style = MaterialTheme.typography.headlineMedium,
        )

        IconButton(
          enabled = seconds < maxSeconds,
          onClick = { seconds = seconds.nextStep(maxSeconds) },
        ) {
          Icon(
            imageVector = VoiceIcons.Add,
            contentDescription = stringResource(StringsR.string.common_dialog_longer),
          )
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onSecondsConfirm(seconds)
          onDismiss()
        },
      ) {
        Text(stringResource(StringsR.string.common_dialog_confirm))
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onDismiss()
        },
      ) {
        Text(stringResource(StringsR.string.common_dialog_cancel))
      }
    },
  )
}
