package audiobook.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * Drop-in replacement for [androidx.compose.material3.DropdownMenu] with no open/close animation.
 *
 * Material3's menu scales and fades itself in. Unlike the platform window animation behind
 * [NoAnimationAlertDialog], that one is a Compose transition inside the component with no flag to
 * turn it off, so the menu is rebuilt here on a plain [Popup] - which simply appears. On e-ink a
 * scale-and-fade is the worst case for the panel: every frame of it is a full redraw that leaves
 * the ghost of the frame before.
 *
 * Takes [androidx.compose.material3.DropdownMenuItem]s as content, exactly like the original.
 */
@Composable
fun NoAnimationDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  anchorHeight: androidx.compose.ui.unit.Dp = 48.dp,
  content: @Composable ColumnScope.() -> Unit,
) {
  if (!expanded) return

  // Hung off the bottom of whatever opened it, which is what the Material menu looks like once
  // its animation has finished.
  val offsetY = with(LocalDensity.current) { anchorHeight.roundToPx() }
  Popup(
    alignment = Alignment.TopEnd,
    offset = IntOffset(x = 0, y = offsetY),
    onDismissRequest = onDismissRequest,
    properties = PopupProperties(focusable = true),
  ) {
    Surface(
      modifier = modifier,
      shape = MaterialTheme.shapes.extraSmall,
      color = MaterialTheme.colorScheme.surfaceContainer,
      tonalElevation = 3.dp,
      shadowElevation = 3.dp,
    ) {
      Column(
        modifier = Modifier
          .padding(vertical = 8.dp)
          .width(IntrinsicSize.Max)
          .verticalScroll(rememberScrollState()),
        content = content,
      )
    }
  }
}
