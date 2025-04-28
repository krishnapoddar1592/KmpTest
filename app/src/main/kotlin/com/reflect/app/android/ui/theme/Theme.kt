// File: ui/theme/Theme.kt
package com.reflect.app.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Theme enum to allow switching between themes
enum class EmotionAppTheme {
    COSMIC,
    SERENE
}

// Custom color scheme to hold our theme-specific colors
data class EmotionColorScheme(
    val background: Color,
    val backgroundSecondary: Color,
    val interactive: Color,
    val interactiveSecondary: Color = Color.Unspecified,
    val interactiveDisabled: Color = Color.Gray,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color = Color.Gray,
    val isLight: Boolean
)

// Create composition local to provide our custom color scheme
val LocalEmotionColorScheme = staticCompositionLocalOf {
    EmotionColorScheme(
        background = Color.Unspecified,
        backgroundSecondary = Color.Unspecified,
        interactive = Color.Unspecified,
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        isLight = true
    )
}

// Create composition local to track current theme
val LocalEmotionAppTheme = compositionLocalOf { EmotionAppTheme.SERENE }

// Create our Material color schemes for both themes
private val CosmicColorScheme = darkColorScheme(
    primary = CosmicColors.Accent,
    secondary = CosmicColors.BackgroundSecondary,
    background = CosmicColors.BackgroundPrimary,
    surface = CosmicColors.BackgroundSecondary,
    onPrimary = CosmicColors.TextPrimary,
    onSecondary = CosmicColors.TextPrimary,
    onBackground = CosmicColors.TextPrimary,
    onSurface = CosmicColors.TextPrimary,
)

private val SereneColorScheme = lightColorScheme(
    primary = SereneColors.InteractivePrimary,
    secondary = SereneColors.InteractiveSecondary,
    background = SereneColors.BackgroundPrimary,
    surface = SereneColors.BackgroundSecondary,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SereneColors.TextPrimary,
    onSurface = SereneColors.TextPrimary,
)

// Create our custom EmotionColorScheme for both themes
private fun cosmicEmotionColorScheme(): EmotionColorScheme = EmotionColorScheme(
    background = CosmicColors.BackgroundPrimary,
    backgroundSecondary = CosmicColors.BackgroundSecondary,
    interactive = CosmicColors.Accent,
    textPrimary = CosmicColors.TextPrimary,
    textSecondary = CosmicColors.TextSecondary,
    isLight = false
)

private fun sereneEmotionColorScheme(): EmotionColorScheme = EmotionColorScheme(
    background = SereneColors.BackgroundPrimary,
    backgroundSecondary = SereneColors.BackgroundSecondary,
    interactive = SereneColors.InteractivePrimary,
    interactiveSecondary = SereneColors.InteractiveSecondary,
    interactiveDisabled = SereneColors.InteractiveDisabled,
    textPrimary = SereneColors.TextPrimary,
    textSecondary = SereneColors.TextSecondary,
    textDisabled = SereneColors.TextDisabled,
    isLight = true
)

@Composable
fun EmotionAppTheme(
    theme: EmotionAppTheme = EmotionAppTheme.COSMIC,
    content: @Composable () -> Unit
) {
    // Select the appropriate color schemes based on the theme
    val colorScheme = when (theme) {
        EmotionAppTheme.COSMIC -> CosmicColorScheme
        EmotionAppTheme.SERENE -> SereneColorScheme
    }

    val emotionColorScheme = when (theme) {
        EmotionAppTheme.COSMIC -> cosmicEmotionColorScheme()
        EmotionAppTheme.SERENE -> sereneEmotionColorScheme()
    }

    // Update the status bar color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = theme == EmotionAppTheme.SERENE
        }
    }

    // Provide the theme and color scheme via CompositionLocal
    CompositionLocalProvider(
        LocalEmotionAppTheme provides theme,
        LocalEmotionColorScheme provides emotionColorScheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Convenient extension function to access emotion color scheme
object EmotionTheme {
    val colors: EmotionColorScheme
        @Composable
        get() = LocalEmotionColorScheme.current

    val currentTheme: EmotionAppTheme
        @Composable
        get() = LocalEmotionAppTheme.current
}