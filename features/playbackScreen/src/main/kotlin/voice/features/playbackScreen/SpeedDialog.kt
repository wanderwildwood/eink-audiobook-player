package voice.features.playbackScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import voice.core.ui.NoAnimationAlertDialog
import voice.core.ui.icons.VoiceIcons
import java.text.DecimalFormat
import kotlin.math.roundToInt
import voice.core.strings.R as StringsR

private const val MIN_SPEED = 0.5F
private const val SPEED_STEP = 0.1F

// The slider this replaced stepped in 0.05. That is the right grain for a thumb you drag and
// the wrong one for a button you press - it doubles every trip to a speed you actually want.
private fun Float.toStep(): Float = (this * 10F).roundToInt() / 10F

@Composable
internal fun SpeedDialog(
  dialogState: BookPlayDialogViewState.SpeedDialog,
  viewModel: BookPlayViewModel,
) {
  val speedFormatter = remember { DecimalFormat("0.0'x'") }
  val speed = dialogState.speed
  val maxSpeed = dialogState.maxSpeed

  NoAnimationAlertDialog(
    onDismissRequest = { viewModel.dismissDialog() },
    confirmButton = {},
    title = {
      Text(stringResource(id = StringsR.string.playback_speed_title))
    },
    text = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          enabled = speed > MIN_SPEED,
          onClick = {
            viewModel.onPlaybackSpeedChanged((speed.toStep() - SPEED_STEP).coerceAtLeast(MIN_SPEED))
          },
        ) {
          Icon(
            imageVector = VoiceIcons.Remove,
            contentDescription = stringResource(id = StringsR.string.playback_speed_slower),
          )
        }

        // The number is the subject of this dialog, not a caption on the controls, so it is
        // sized to be read from arm's length rather than leaned into.
        Text(
          text = speedFormatter.format(speed),
          style = MaterialTheme.typography.headlineMedium,
        )

        IconButton(
          enabled = speed < maxSpeed,
          onClick = {
            viewModel.onPlaybackSpeedChanged((speed.toStep() + SPEED_STEP).coerceAtMost(maxSpeed))
          },
        ) {
          Icon(
            imageVector = VoiceIcons.Add,
            contentDescription = stringResource(id = StringsR.string.playback_speed_faster),
          )
        }
      }
    },
  )
}
