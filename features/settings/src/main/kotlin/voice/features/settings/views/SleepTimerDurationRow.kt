package voice.features.settings.views

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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import voice.core.ui.NoAnimationAlertDialog
import voice.core.ui.icons.VoiceIcons
import voice.core.strings.R as StringsR

private val SleepTimerDurationOptionsMinutes = listOf(10, 20, 60)

@Composable
internal fun SleepTimerDurationRow(
  durationMinutes: Int,
  endOfChapter: Boolean,
  onClick: () -> Unit,
) {
  ListItem(
    modifier = Modifier
      .clickable { onClick() }
      .fillMaxWidth(),
    leadingContent = {
      Icon(
        imageVector = VoiceIcons.Bedtime,
        contentDescription = stringResource(StringsR.string.settings_sleep_timer_duration_title),
      )
    },
    headlineContent = {
      Text(text = stringResource(StringsR.string.settings_sleep_timer_duration_title))
    },
    supportingContent = {
      Text(
        text = if (endOfChapter) {
          stringResource(StringsR.string.sleep_timer_duration_end_of_chapter)
        } else {
          LocalResources.current.getQuantityString(
            StringsR.plurals.duration_minutes,
            durationMinutes,
            durationMinutes,
          )
        },
      )
    },
  )
}

@Composable
internal fun SleepTimerDurationDialog(
  currentDurationMinutes: Int,
  currentEndOfChapter: Boolean,
  onDurationSelect: (Int) -> Unit,
  onEndOfChapterSelect: () -> Unit,
  onDismiss: () -> Unit,
) {
  NoAnimationAlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(stringResource(StringsR.string.settings_sleep_timer_duration_title))
    },
    text = {
      Column {
        SleepTimerDurationOptionsMinutes.forEach { minutes ->
          SleepTimerDurationOptionRow(
            selected = !currentEndOfChapter && currentDurationMinutes == minutes,
            label = LocalResources.current.getQuantityString(StringsR.plurals.duration_minutes, minutes, minutes),
            onClick = { onDurationSelect(minutes) },
          )
        }
        SleepTimerDurationOptionRow(
          selected = currentEndOfChapter,
          label = stringResource(StringsR.string.sleep_timer_duration_end_of_chapter),
          onClick = onEndOfChapterSelect,
        )
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
private fun SleepTimerDurationOptionRow(
  selected: Boolean,
  label: String,
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
  )
}
