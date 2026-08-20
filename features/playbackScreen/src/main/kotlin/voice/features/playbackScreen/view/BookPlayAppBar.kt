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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
  onLockClick: () -> Unit,
  useLandscapeLayout: Boolean,
) {
  val locked = viewState.locked
  val appBarActions: @Composable RowScope.() -> Unit = {
    // While locked every action but the lock is a no-op and dimmed. They stay in place rather
    // than disappearing, so the bar does not rearrange itself under your thumb.
    LockableAction(locked) {
      SleepTimerButton(viewState.sleepTimerState, onSleepTimerClick.takeUnless { locked } ?: {})
    }
    LockableAction(locked) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .combinedClickable(
            enabled = !locked,
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
    }
    LockableAction(locked) {
      SpeedButton(viewState.playbackSpeed, onSpeedChangeClick.takeUnless { locked } ?: {})
    }
    LockButton(locked = locked, onClick = onLockClick)
    LockableAction(locked) {
      OverflowMenu(
        skipSilence = viewState.skipSilence,
        onSkipSilenceClick = onSkipSilenceClick,
        onVolumeBoostClick = onVolumeBoostClick,
        enabled = !locked,
      )
    }
  }
  // No title here any more: the book is named in the content, in the size it deserves, and a
  // LargeTopAppBar repeating it cost a fifth of the page to say the same thing twice.
  TopAppBar(
    navigationIcon = {
      CloseIcon(onCloseClick)
    },
    actions = appBarActions,
    title = {},
  )
}

// The sleep timer defaults to 10 minutes everywhere it's started - passing it as the reference
// duration keeps the countdown's minute digit count stable (e.g. "09:45", not "9:45") as it ticks.
private val SleepTimerReferenceDuration = 10.minutes

/** Dims whatever it wraps while the controls are locked, so "inert" is visible, not just true. */
@Composable
private fun LockableAction(
  locked: Boolean,
  content: @Composable () -> Unit,
) {
  Box(modifier = Modifier.alpha(if (locked) 0.38f else 1f)) {
    content()
  }
}

/**
 * Locks the controls, so a book can keep playing in a pocket or under a pillow without a stray
 * touch seeking it somewhere else. Tapping it again unlocks - it is the one control that keeps
 * working while locked, which is the whole point of it.
 */
@Composable
private fun LockButton(
  locked: Boolean,
  onClick: () -> Unit,
) {
  IconButton(onClick = onClick) {
    Icon(
      imageVector = if (locked) VoiceIcons.Lock else VoiceIcons.LockOpen,
      contentDescription = stringResource(
        id = if (locked) R.string.playback_action_unlock else R.string.playback_action_lock,
      ),
    )
  }
}

/**
 * The speed icon, with the current speed underneath - but only when it is not 1x.
 *
 * Same shape as the sleep timer button next to it: the icon says what the control is, the label
 * appears only when there is a value worth reading. At 1x there is nothing to say, and a
 * permanent "1.0x" would just be another number to read past on a screen meant to be quiet.
 */
@Composable
private fun SpeedButton(
  speed: Float,
  onClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .width(56.dp)
      .clickable(onClick = onClick),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Icon(
      imageVector = VoiceIcons.Speed,
      contentDescription = stringResource(id = R.string.playback_speed_title),
    )
    if (speed != 1F) {
      Text(
        // Trailing zero dropped: "1.5x", but "2x" rather than "2.0x".
        text = "${speed.toString().removeSuffix(".0")}x",
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
      )
    }
  }
}

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
