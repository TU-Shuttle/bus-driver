package com.tukorea.driver.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // Primary
    primary = PrimaryBlue,
    onPrimary = OnPrimary,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue700,

    // Secondary
    secondary = SecondaryBlue,
    onSecondary = OnSecondary,
    secondaryContainer = Blue100,
    onSecondaryContainer = Blue700,

    // Tertiary
    tertiary = TertiaryBlue,
    onTertiary = White,
    tertiaryContainer = Blue100,
    onTertiaryContainer = Blue700,

    // Error
    error = ErrorRed,
    onError = White,
    errorContainer = Red100,
    onErrorContainer = Red700,

    // Background & Surface
    background = BackgroundLight,
    onBackground = OnBackground,
    surface = SurfaceLight,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariantColor,
    onSurfaceVariant = OnSurfaceVariant,

    // Outline
    outline = Gray300,
    outlineVariant = Gray200,
    scrim = Black.copy(alpha = 0.32f)
)

@Composable
fun BusTheme(content: @Composable () -> Unit) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
