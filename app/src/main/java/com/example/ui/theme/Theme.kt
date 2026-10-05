package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = PitchBlack,
    primaryContainer = GoldDark,
    onPrimaryContainer = Color.White,
    secondary = EmeraldPitch,
    onSecondary = PitchBlack,
    secondaryContainer = PitchDark,
    onSecondaryContainer = PitchGlow,
    tertiary = AccentBlue,
    onTertiary = PitchBlack,
    background = PitchBlack,
    onBackground = TextPrimaryDark,
    surface = CardSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorderGold,
    error = AccentRed,
    onError = Color.White
)

private val LightColorScheme = DarkColorScheme // Football theme is best experienced with dark stadium glow

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
