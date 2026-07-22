package voice.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

/**
 * Drop-in replacement for [AlertDialog] with the platform window's open/close fade+scale
 * animation disabled - Compose doesn't expose an [androidx.compose.animation.core.AnimationSpec]
 * for this (it's the OS window animation, not a Compose one), so it's suppressed by reaching the
 * underlying platform Window and calling `setWindowAnimations(0)`. Needed for e-ink use, where
 * that animation just ghosts.
 */
@Composable
fun NoAnimationAlertDialog(
  onDismissRequest: () -> Unit,
  confirmButton: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  dismissButton: (@Composable () -> Unit)? = null,
  icon: (@Composable () -> Unit)? = null,
  title: (@Composable () -> Unit)? = null,
  text: (@Composable () -> Unit)? = null,
  shape: Shape = AlertDialogDefaults.shape,
  containerColor: Color = AlertDialogDefaults.containerColor,
  iconContentColor: Color = AlertDialogDefaults.iconContentColor,
  titleContentColor: Color = AlertDialogDefaults.titleContentColor,
  textContentColor: Color = AlertDialogDefaults.textContentColor,
  tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
  properties: DialogProperties = DialogProperties(),
) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    confirmButton = {
      DisableDialogWindowAnimation()
      confirmButton()
    },
    modifier = modifier,
    dismissButton = dismissButton,
    icon = icon,
    title = title,
    text = text,
    shape = shape,
    containerColor = containerColor,
    iconContentColor = iconContentColor,
    titleContentColor = titleContentColor,
    textContentColor = textContentColor,
    tonalElevation = tonalElevation,
    properties = properties,
  )
}

@Composable
private fun DisableDialogWindowAnimation() {
  val view = LocalView.current
  SideEffect {
    (view.parent as? DialogWindowProvider)?.window?.setWindowAnimations(0)
  }
}
