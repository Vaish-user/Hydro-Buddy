package com.hydrobuddy.app.ui.theme

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
import com.hydrobuddy.app.data.model.AppThemeMode

/**
 * Phase 5 Material 3 Light Color Scheme.
 * Single accent color (Sky Blue) with neutral, flat slate surfaces.
 */
private val LightColorScheme = lightColorScheme(
    primary = HydroAccent,
    onPrimary = Color.White,
    primaryContainer = HydroAccentContainerLight,
    onPrimaryContainer = HydroAccent,
    secondary = HydroAccent,
    onSecondary = Color.White,
    secondaryContainer = M3LightSurfaceVariant,
    onSecondaryContainer = M3LightOnSurface,
    tertiary = StatusActive,
    onTertiary = Color.White,
    background = M3LightBackground,
    onBackground = M3LightOnSurface,
    surface = M3LightSurface,
    onSurface = M3LightOnSurface,
    surfaceVariant = M3LightSurfaceVariant,
    onSurfaceVariant = M3LightOnSurfaceVariant,
    outline = M3LightOutline,
    outlineVariant = M3LightOutline
)

/**
 * Phase 5 Material 3 Dark Color Scheme.
 * Accessible Sky 400 accent with dark slate surfaces.
 */
private val DarkColorScheme = darkColorScheme(
    primary = HydroAccentDark,
    onPrimary = Color(0xFF082F49),
    primaryContainer = HydroAccentContainerDark,
    onPrimaryContainer = HydroAccentDark,
    secondary = HydroAccentDark,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = M3DarkSurfaceVariant,
    onSecondaryContainer = M3DarkOnSurface,
    tertiary = StatusActive,
    onTertiary = Color.White,
    background = M3DarkBackground,
    onBackground = M3DarkOnSurface,
    surface = M3DarkSurface,
    onSurface = M3DarkOnSurface,
    surfaceVariant = M3DarkSurfaceVariant,
    onSurfaceVariant = M3DarkOnSurfaceVariant,
    outline = M3DarkOutline,
    outlineVariant = M3DarkOutline
)

@Composable
fun HydroBuddyTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    },
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HydroTypography,
        shapes = HydroShapes,
        content = content
    )
}
