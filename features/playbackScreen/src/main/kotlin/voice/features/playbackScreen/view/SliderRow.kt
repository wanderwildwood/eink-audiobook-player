package voice.features.playbackScreen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import voice.core.ui.formatTime
import kotlin.time.Duration

/**
 * Full-width progress, with elapsed and total underneath at either end - the shape eInk Music
 * uses.
 *
 * The times used to flank the slider on the same line, which squeezed the bar itself into
 * whatever was left. Dropping them below gives the whole width to the part you drag, which is
 * the part that has to be hit accurately on a touch screen.
 */
@Composable
internal fun SliderRow(
  duration: Duration,
  playedTime: Duration,
  onSeek: (Duration) -> Unit,
) {
  var localValue by remember { mutableFloatStateOf(0F) }
  val interactionSource = remember { MutableInteractionSource() }
  val dragging by interactionSource.collectIsDraggedAsState()

  Column(modifier = Modifier.fillMaxWidth()) {
    val fraction = if (dragging) {
      localValue
    } else {
      (playedTime / duration).toFloat().coerceIn(0F, 1F)
    }
    Slider(
      modifier = Modifier.fillMaxWidth(),
      interactionSource = interactionSource,
      value = fraction,
      onValueChange = { localValue = it },
      onValueChangeFinished = { onSeek(duration * localValue.toDouble()) },
      // A line and a dot, the way eInk Music draws it. Material's default track is a thick
      // rounded slab which, in a palette with no colour to separate played from unplayed, turns
      // into one heavy black bar across the page.
      thumb = {
        Box(
          modifier = Modifier
            .size(16.dp)
            .background(MaterialTheme.colorScheme.onSurface, CircleShape),
        )
      },
      track = {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth(fraction)
              .height(2.dp)
              .background(MaterialTheme.colorScheme.onSurface),
          )
        }
      },
    )

    Spacer(modifier = Modifier.size(4.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        // While dragging this follows the thumb, so you can see where you are letting go.
        text = formatTime(
          timeMs = if (dragging) {
            (duration * localValue.toDouble()).inWholeMilliseconds
          } else {
            playedTime.inWholeMilliseconds
          },
          durationMs = duration.inWholeMilliseconds,
        ),
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = formatTime(
          timeMs = duration.inWholeMilliseconds,
          durationMs = duration.inWholeMilliseconds,
        ),
        style = MaterialTheme.typography.titleMedium,
      )
    }
  }
}
