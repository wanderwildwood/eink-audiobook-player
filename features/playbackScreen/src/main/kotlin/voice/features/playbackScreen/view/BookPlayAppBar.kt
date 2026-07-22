package voice.features.playbackScreen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import voice.core.strings.R
import voice.core.ui.formatTime
import voice.core.ui.icons.VoiceIcons
import voice.features.playbackScreen.BookPlayViewState
import kotlin.time.Duration.Companion.minutes

@Composable
internal fun BookPlayAppBar(
  viewState: BookPlayViewState,
  onSleepTimerClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onBookmarkLongClick: () -> Unit,
  onSpeedChangeClick: () -> Unit,
  onSkipSilenceClick: () -> Unit,
  onVolumeBoostClick: () -> Unit,
  onCloseClick: () -> Unit,
  useLandscapeLayout: Boolean,
) {
  val appBarActions: @Composable RowScope.() -> Unit = {
    SleepTimerButton(viewState.sleepTimerState, onSleepTimerClick)
    Box(
      modifier = Modifier
        .size(40.dp)
        .combinedClickable(
          onClick = onBookmarkClick,
          onLongClick = onBookmarkLongClick,
          indication = ripple(bounded = false, radius = 20.dp),
          interactionSource = remember { MutableInteractionSource() },
        ),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = VoiceIcons.CollectionsBookmark,
        contentDescription = stringResource(id = R.string.bookmark_title),
      )
    }
    IconButton(onClick = onSpeedChangeClick) {
      Icon(
        imageVector = VoiceIcons.Speed,
        contentDescription = stringResource(id = R.string.playback_speed_title),
      )
    }
    OverflowMenu(
      skipSilence = viewState.skipSilence,
      onSkipSilenceClick = onSkipSilenceClick,
      onVolumeBoostClick = onVolumeBoostClick,
    )
  }
  if (useLandscapeLayout) {
    TopAppBar(
      navigationIcon = {
        CloseIcon(onCloseClick)
      },
      actions = appBarActions,
      title = {
        AppBarTitle(viewState.title)
      },
    )
  } else {
    LargeTopAppBar(
      navigationIcon = {
        CloseIcon(onCloseClick)
      },
      actions = appBarActions,
      title = {
        AppBarTitle(viewState.title)
      },
    )
  }
}

// The sleep timer defaults to 10 minutes everywhere it's started - passing it as the reference
// duration keeps the countdown's minute digit count stable (e.g. "09:45", not "9:45") as it ticks.
private val SleepTimerReferenceDuration = 10.minutes

@Composable
private fun SleepTimerButton(
  sleepTimerState: BookPlayViewState.SleepTimerViewState,
  onClick: () -> Unit,
) {
  val sleepTimerIcon = if (sleepTimerState is BookPlayViewState.SleepTimerViewState.Disabled) {
    VoiceIcons.BedtimeOff
  } else {
    VoiceIcons.Bedtime
  }
  Column(
    modifier = Modifier
      .width(56.dp)
      .clickable(onClick = onClick),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
      imageVector = sleepTimerIcon,
      contentDescription = stringResource(id = R.string.sleep_timer_action_open),
    )
    if (sleepTimerState is BookPlayViewState.SleepTimerViewState.Enabled.WithDuration) {
      Text(
        text = formatTime(
          timeMs = sleepTimerState.leftDuration.inWholeMilliseconds,
          durationMs = SleepTimerReferenceDuration.inWholeMilliseconds,
        ),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
      )
    } else if (sleepTimerState is BookPlayViewState.SleepTimerViewState.Enabled.UntilChapterEnd) {
      Text(
        text = stringResource(id = R.string.sleep_timer_duration_end_of_chapter),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        maxLines = 2,
      )
    }
  }
}
