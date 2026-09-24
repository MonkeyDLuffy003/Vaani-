package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VaaniDarkColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    onPrimary = TextPrimary,
    secondary = SecondaryCyan,
    onSecondary = TextPrimary,
    tertiary = TertiaryViolet,
    onTertiary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = AccentRose
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve bespoke sleek dark aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VaaniDarkColorScheme,
        typography = Typography,
        content = content
    )
}

