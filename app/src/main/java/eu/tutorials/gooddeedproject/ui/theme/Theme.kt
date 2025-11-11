package eu.tutorials.gooddeedproject.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = OrangeButtonColor,
    onPrimary = Color.White,
    background = LightGrayBackground,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.DarkGray
)

// ✅ Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = OrangeButtonColor,
    onPrimary = OnDarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = OnDarkText,
    onSurface = OnDarkText
)

@Composable
fun GoodDeedProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Automatically detect system theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // This code handles the status bar color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}