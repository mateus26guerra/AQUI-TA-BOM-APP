package com.example.aquitabom.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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

private val DarkColorScheme = darkColorScheme(
    primary = OrangeBrand,
    onPrimary = Color.White,
    secondary = OrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1A1A1A),
    onSecondaryContainer = Color.White,
    tertiary = OrangeDark,
    background = Color(0xFF000000), // Preto absoluto (OLED)
    surface = Color(0xFF000000),    // Preto absoluto (OLED)
    surfaceVariant = Color(0xFF0A0A0A), // Cinza quase preto para cards
    onSurfaceVariant = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeBrand,
    onPrimary = Color.White,
    secondary = OrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFF2F2F2),
    onSecondaryContainer = Color.Black,
    tertiary = OrangeDark,
    background = Color(0xFFF8F8F8), // Cinza bem claro
    surface = Color(0xFFFFFFFF),    // Branco puro
    surfaceVariant = Color(0xFFF2F2F2), // Cinza claro para cards
    onSurfaceVariant = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun AquiTaBomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context.findActivity())?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Helper para encontrar a Activity a partir do Context
fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}
