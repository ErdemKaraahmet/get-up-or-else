package com.getuporelse.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF141313)
val DarkSurface = Color(0xFF141313)
val OnSurface = Color(0xFFE5E2E1)
val Primary = Color(0xFFC8C6C5)
val Secondary = Color(0xFFDDB7FF)
val OnSecondary = Color(0xFF40215E)
val SurfaceContainer = Color(0xFF201F1F)
val SurfaceContainerHigh = Color(0xFF2B2A2A)
val OnSurfaceVariant = Color(0xFFC4C7C7)
val Outline = Color(0xFF8E9192)
val OutlineVariant = Color(0xFF444748)
val Error = Color(0xFFFFB4AB)

val GetUpOrElseColorScheme: ColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color(0xFF313030),
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = DarkBackground,
    onBackground = OnSurface,
    surface = DarkSurface,
    onSurface = OnSurface,
    surfaceVariant = Color(0xFF353434),
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh
)
