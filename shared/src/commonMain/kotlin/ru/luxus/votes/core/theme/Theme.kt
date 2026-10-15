package ru.luxus.votes.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Magenta,
    onPrimary = White,
    secondary = Magenta,
    onSecondary = White,
    background = Black,
    onBackground = White,
    surface = SurfaceDark,
    onSurface = White,
    surfaceVariant = GrayDark,
    onSurfaceVariant = GrayLight
)

private val LightColorScheme = lightColorScheme(
    primary = Magenta,
    onPrimary = White,
    secondary = Magenta,
    onSecondary = White,
    background = White,
    onBackground = Black,
    surface = SurfaceLight,
    onSurface = Black,
    surfaceVariant = GrayLight,
    onSurfaceVariant = Black
)

@Composable
fun T2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
