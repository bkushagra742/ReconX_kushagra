package com.kushagra.reconx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = RoseAccent,
    onPrimary = DarkBackground,
    primaryContainer = MaroonPrimary,
    onPrimaryContainer = TextPrimaryDark,
    secondary = RoseAccentBright,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder,
    error = DangerRed,
)

private val LightColors = lightColorScheme(
    primary = RoseAccentLight,
    onPrimary = LightSurface,
    primaryContainer = MaroonPrimaryLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = MaroonPrimaryLight,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    error = DangerRed,
)

/**
 * Theme.kt
 * ========
 * App-wide Material 3 theme. Respects the user's Settings > Appearance
 * choice (light / dark / system) via [darkTheme], defaulting to the
 * system setting on first launch.
 */
@Composable
fun ReconXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = ReconXTypography,
        shapes = ReconXShapes,
        content = content,
    )
}
