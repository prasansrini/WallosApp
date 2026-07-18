package com.wallosapp.android

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Minimalist Color Palette Definitions
val Primary = Color(0xFF000000)        // Black
val Secondary = Color(0xFF607D8B)      // Slate Blue
val Background = Color(0xFFFFFFFF)     // Pure White
val TextColor = Color(0xFF424242)      // Dark Gray
val LightGray = Color(0xFFF5F5F5)      // Light Gray

// Material 3 Light Color Scheme mapping
val MinimalistLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Background,
    secondary = Secondary,
    onSecondary = Background,
    background = LightGray,
    onBackground = TextColor,
    surface = Background,
    onSurface = TextColor
)

// Material 3 Dark Color Scheme mapping (inverted for dark mode)
val MinimalistDarkColorScheme = darkColorScheme(
    primary = Background,
    onPrimary = Primary,
    secondary = Secondary,
    onSecondary = Primary,
    background = Primary,
    onBackground = Background,
    surface = TextColor,
    onSurface = Background
)
