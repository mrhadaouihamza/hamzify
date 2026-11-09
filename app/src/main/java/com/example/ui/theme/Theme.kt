package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class CustomThemeColors(
    val subtleBorder: Color,
    val success: Color,
    val warning: Color,
    val textSecondary: Color,
    val surfaceSubtle: Color,
    val cardBackground: Color
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomThemeColors(
        subtleBorder = LightBorder,
        success = LightSuccess,
        warning = LightWarning,
        textSecondary = LightTextSecondary,
        surfaceSubtle = LightSurfaceVariant,
        cardBackground = LightSurface
    )
}

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECEBFF),
    onPrimaryContainer = LightPrimaryVariant,
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EAF6),
    onSecondaryContainer = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF28254A),
    onPrimaryContainer = Color(0xFFD6D2FF),
    secondary = DarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF272938),
    onSecondaryContainer = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkError,
    onError = Color.White
)

@Composable
fun HabitFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) {
        CustomThemeColors(
            subtleBorder = DarkBorder,
            success = DarkSuccess,
            warning = DarkWarning,
            textSecondary = DarkTextSecondary,
            surfaceSubtle = DarkSurfaceVariant,
            cardBackground = DarkSurface
        )
    } else {
        CustomThemeColors(
            subtleBorder = LightBorder,
            success = LightSuccess,
            warning = LightWarning,
            textSecondary = LightTextSecondary,
            surfaceSubtle = LightSurfaceVariant,
            cardBackground = LightSurface
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

object HabitTheme {
    val customColors: CustomThemeColors
        @Composable
        get() = LocalCustomColors.current
}
