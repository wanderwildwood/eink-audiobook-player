package audiobook.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import audiobook.core.data.ThemeColorScheme
import audiobook.core.data.ThemeMode
import com.mudita.mmd.ThemeMMD
import com.mudita.mmd.eInkColorScheme

val Blue = Color(0xFF003b7f)

// eInkColorScheme leaves several newer Material3 roles (surfaceContainer*, surfaceBright/Dim) as
// Color.Unspecified. Multiple stock Material3 components (TopAppBar's scrolled state, among
// others) read those roles directly and crash (ColorSpace.get: "Invalid ID") rather than treating
// Unspecified as "don't draw" - so every role needs a real value, fixed once here for the whole app.
private val fullySpecifiedEInkColorScheme = eInkColorScheme.copy(
  surfaceBright = eInkColorScheme.background,
  surfaceDim = eInkColorScheme.background,
  surfaceContainer = eInkColorScheme.background,
  surfaceContainerHigh = eInkColorScheme.background,
  surfaceContainerHighest = eInkColorScheme.background,
  surfaceContainerLowest = eInkColorScheme.background,
)

// themeMode/themeColorScheme are kept for call-site compatibility with the settings screen,
// but ThemeMMD's e-ink color scheme is intentionally fixed (monochrome) regardless of either.
@Suppress("UNUSED_PARAMETER")
@Composable
fun Theme(
  themeMode: ThemeMode = ThemeMode.FollowSystem,
  themeColorScheme: ThemeColorScheme = ThemeColorScheme.Blue,
  content: @Composable () -> Unit,
) {
  ThemeMMD(colorScheme = fullySpecifiedEInkColorScheme) {
    content()
  }
}
