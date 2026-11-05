package com.tukorea.driver.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme =
    lightColorScheme(
        primary = Blue500,
        onPrimary = White,
        primaryContainer = Blue50,
        onPrimaryContainer = Blue600,
        secondary = Gray700,
        onSecondary = White,
        secondaryContainer = Gray100,
        onSecondaryContainer = Gray800,
        tertiary = Green500,
        onTertiary = White,
        error = Red500,
        onError = White,
        errorContainer = Red50,
        onErrorContainer = Red500,
        background = Gray100,
        onBackground = Gray900,
        surface = White,
        onSurface = Gray900,
        surfaceVariant = Gray100,
        onSurfaceVariant = Gray600,
        outline = Gray300,
        outlineVariant = Gray200,
        scrim = Black.copy(alpha = 0.4f),
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Blue500,
        onPrimary = White,
        primaryContainer = DarkBlue50,
        onPrimaryContainer = Blue100,
        secondary = Gray400,
        onSecondary = Gray900,
        secondaryContainer = DarkSurfaceHigh,
        onSecondaryContainer = Gray100,
        tertiary = Green500,
        onTertiary = White,
        error = Red500,
        onError = White,
        errorContainer = DarkRed50,
        onErrorContainer = Red500,
        background = DarkBackground,
        onBackground = Gray100,
        surface = DarkSurface,
        onSurface = Gray100,
        surfaceVariant = DarkSurfaceHigh,
        onSurfaceVariant = Gray400,
        outline = DarkOutline,
        outlineVariant = DarkSurfaceHigh,
        scrim = Black.copy(alpha = 0.6f),
    )

/** Material 색상 체계에 없는 상태색·보조 텍스트색. */
@Immutable
data class BusColors(
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val textSecondary: Color,
    val textTertiary: Color,
)

private val LightBusColors =
    BusColors(
        success = Green500,
        successContainer = Green50,
        warning = Orange500,
        warningContainer = Orange50,
        danger = Red500,
        dangerContainer = Red50,
        textSecondary = Gray600,
        textTertiary = Gray500,
    )

private val DarkBusColors =
    BusColors(
        success = Green500,
        successContainer = DarkGreen50,
        warning = Orange500,
        warningContainer = DarkOrange50,
        danger = Red500,
        dangerContainer = DarkRed50,
        textSecondary = Gray400,
        textTertiary = Gray500,
    )

private val LocalBusColors = staticCompositionLocalOf { LightBusColors }

val MaterialTheme.busColors: BusColors
    @Composable
    @ReadOnlyComposable
    get() = LocalBusColors.current

private val BusShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

@Composable
fun BusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalBusColors provides if (darkTheme) DarkBusColors else LightBusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = BusShapes,
            content = content,
        )
    }
}
