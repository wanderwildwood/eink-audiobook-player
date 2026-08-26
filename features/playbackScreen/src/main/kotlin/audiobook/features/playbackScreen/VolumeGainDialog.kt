package audiobook.features.playbackScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import audiobook.core.playback.misc.Decibel
import audiobook.core.ui.NoAnimationAlertDialog
import audiobook.core.strings.R as StringsR

@Composable
internal fun VolumeGainDialog(
  dialogState: BookPlayDialogViewState.VolumeGainDialog,
  viewModel: BookPlayViewModel,
) {
  NoAnimationAlertDialog(
    onDismissRequest = { viewModel.dismissDialog() },
    confirmButton = {},
    text = {
      Column {
        Text(stringResource(id = StringsR.string.playback_option_volume_boost) + ": " + dialogState.valueFormatted)
        Slider(
          valueRange = 0F..dialogState.maxGain.value,
          value = dialogState.gain.value,
          onValueChange = {
            viewModel.onVolumeGainChanged(Decibel(it))
          },
        )
      }
    },
  )
}
