@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package voice.core.ui

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import voice.core.data.BookId

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

// E-ink displays can't render animated bounds/fades cleanly, so every shared element snaps instantly.
@OptIn(ExperimentalSharedTransitionApi::class)
private val InstantBoundsTransform = BoundsTransform { _, _ -> tween(durationMillis = 0) }

fun sharedCoverKey(bookId: BookId): String = "book-cover-${bookId.value}"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedCoverElementModifier(bookId: BookId): Modifier {
  val sharedTransitionScope = LocalSharedTransitionScope.current
    ?: return this
  return with(sharedTransitionScope) {
    sharedElement(
      sharedContentState = rememberSharedContentState(key = sharedCoverKey(bookId)),
      animatedVisibilityScope = LocalNavAnimatedContentScope.current,
      boundsTransform = InstantBoundsTransform,
    )
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.playButtonSharedBoundsModifier(): Modifier {
  val sharedTransitionScope = LocalSharedTransitionScope.current
    ?: return this
  return with(sharedTransitionScope) {
    sharedBounds(
      sharedContentState = rememberSharedContentState(key = "play-button"),
      animatedVisibilityScope = LocalNavAnimatedContentScope.current,
      boundsTransform = InstantBoundsTransform,
      enter = EnterTransition.None,
      exit = ExitTransition.None,
    )
  }
}
