package com.nddfeon.demonic.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DemonicDarkColorScheme = darkColorScheme(
    primary = DemonicCrimson,
    onPrimary = DemonicTextPrimary,
    primaryContainer = DemonicCrimsonDark,
    onPrimaryContainer = DemonicTextPrimary,
    secondary = DemonicViolet,
    onSecondary = DemonicTextPrimary,
    secondaryContainer = DemonicSurfaceVariant,
    onSecondaryContainer = DemonicTextPrimary,
    tertiary = DemonicSyncTeal,
    onTertiary = DemonicTextInverse,
    background = DemonicBackground,
    onBackground = DemonicTextPrimary,
    surface = DemonicSurface,
    onSurface = DemonicTextPrimary,
    surfaceVariant = DemonicSurfaceVariant,
    onSurfaceVariant = DemonicTextSecondary,
    outline = DemonicBorder,
    error = DemonicErrorRed,
    onError = DemonicTextPrimary
)

@Composable
fun DEMONICTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = DemonicBackground.toArgb()
            window.navigationBarColor = DemonicBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = DemonicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
