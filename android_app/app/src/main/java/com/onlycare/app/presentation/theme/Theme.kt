package com.onlycare.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 🔷 PORTER-LIKE PREMIUM BLUE LIGHT THEME - FLAT DESIGN
private val LightColorScheme = lightColorScheme(
    // Primary - Premium Blue
    primary = Primary,               // #0D45D2 Denim
    onPrimary = White,               // White text on blue
    primaryContainer = AccentLavender, // Soft ice tint
    onPrimaryContainer = TextPrimary,
    
    // Secondary - Blue accents
    secondary = PrimaryLight,        // #5E80CE Danube
    onSecondary = White,
    secondaryContainer = AccentLavender, // #DBEAFE Ice
    onSecondaryContainer = TextPrimary,
    
    // Tertiary - Neutral Grays
    tertiary = TextSecondary,
    onTertiary = White,
    tertiaryContainer = Surface,     // #F7F9FF Blue-white
    onTertiaryContainer = TextPrimary,
    
    // Error states
    error = ErrorRed,               // #EF4444 Ruby red
    onError = White,
    errorContainer = ErrorRed.copy(alpha = 0.1f),
    onErrorContainer = ErrorRed,
    
    // Background & Surface (Pure White Luxury)
    background = Background,        // #FFFFFF Pure white
    onBackground = TextPrimary,     // #1F2C55 Cloud burst
    surface = Surface,              // #F7F9FF Blue-white
    onSurface = TextPrimary,
    surfaceVariant = Card,          // #FFFFFF White cards
    onSurfaceVariant = TextSecondary,
    
    // Outline & Borders (Refined)
    outline = Border,               // #D4D9E2 Mischka
    outlineVariant = BorderLight,   // #F4F4F5 Very subtle
    scrim = Black.copy(alpha = 0.32f),
    
    // Inverse (for contrast elements)
    inverseSurface = TextPrimary,   // Dark for contrast
    inverseOnSurface = Background,  // White text
    inversePrimary = PrimaryLight   // Danube
)

@Composable
fun OnlyCareTheme(
    darkTheme: Boolean = false, // Light theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Light theme status bars
            window.statusBarColor = Background.toArgb()  // White status bar
            window.navigationBarColor = Background.toArgb()  // White navigation bar
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true  // Dark icons on light background
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

