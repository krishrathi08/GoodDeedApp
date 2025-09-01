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

// Define our custom Dark Color Scheme using colors from Color.kt
private val CustomDarkColorScheme = darkColorScheme(
    primary = BlueButtonColor,
    secondary = OrangeButtonColor,
    background = AppBackgroundColor,
    surface = AppBackgroundColor, // Important for Surfaces, Cards, etc.
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextColor,
    onSurface = TextColor,
)

// Define our custom Light Color Scheme using colors from Color.kt
private val CustomLightColorScheme = lightColorScheme(
    primary = BlueButtonColor,
    secondary = OrangeButtonColor,
    background = LightGrayBackground,
    surface = Color.White, // Cards and Surfaces will be white
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LightTextColor,
    onSurface = LightTextColor,
)

@Composable
fun GoodDeedProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled to enforce our custom brand colors.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        CustomDarkColorScheme
    } else {
        CustomLightColorScheme
    }

    // This block changes the system's status bar color to match our app's background
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
        typography = Typography, // Added typography to apply your Poppins/Inter fonts
        content = content
    )
}