package ai.sentinel.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmberBright,
    onPrimary = TextPrimary,
    primaryContainer = CardElevation2,
    onPrimaryContainer = TextPrimary,
    secondary = EmberCore,
    onSecondary = TextPrimary,
    secondaryContainer = CardElevation1,
    onSecondaryContainer = FlameAccent,
    tertiary = AmberHighlight,
    background = DeepObsidian,
    onBackground = TextPrimary,
    surface = CardElevation1,
    onSurface = TextPrimary,
    surfaceVariant = CardElevation2,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = CriticalCrimson,
    onError = TextPrimary,
    errorContainer = CriticalContainer,
    onErrorContainer = CriticalOnContainer
)

@Composable
fun SentinelTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DeepObsidian.toArgb()
                window.navigationBarColor = DeepObsidian.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = SentinelTypography,
        content = content
    )
}
