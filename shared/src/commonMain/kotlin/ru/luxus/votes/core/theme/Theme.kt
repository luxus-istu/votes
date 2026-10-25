package ru.luxus.votes.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val LightColorScheme = lightColorScheme(
    primary = UdmurtRed,
    onPrimary = White,
    primaryContainer = UdmurtRedLight,
    onPrimaryContainer = UdmurtRedDark,

    secondary = UdmurtGold,
    onSecondary = UdmurtBlack,
    secondaryContainer = UdmurtGoldLight,
    onSecondaryContainer = Color(0xFF6B4A00),

    tertiary = UdmurtGreen,
    onTertiary = White,
    tertiaryContainer = UdmurtGreenLight,
    onTertiaryContainer = UdmurtGreenDark,

    background = UdmurtCream,
    onBackground = UdmurtBlack,
    surface = White,
    onSurface = UdmurtBlack,
    surfaceVariant = UdmurtCardBg,
    onSurfaceVariant = GrayMedium,

    error = IncorrectRed,
    onError = White,
    errorContainer = UdmurtRedLight,
    onErrorContainer = UdmurtRedDark,

    outline = UdmurtOutline,
    outlineVariant = UdmurtCardBg,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF9C8F),
    onPrimary = Color(0xFF4C0E08),
    primaryContainer = Color(0xFF7E241C),
    onPrimaryContainer = Color(0xFFFFDAD4),

    secondary = Color(0xFFF4C56A),
    onSecondary = Color(0xFF3D2A00),
    secondaryContainer = Color(0xFF5C4300),
    onSecondaryContainer = Color(0xFFFFE7B3),

    tertiary = Color(0xFF9AD6A9),
    onTertiary = Color(0xFF0C3418),
    tertiaryContainer = Color(0xFF255A33),
    onTertiaryContainer = Color(0xFFC3F0CE),

    background = SurfaceDark,
    onBackground = Color(0xFFF6EEE6),
    surface = GrayDark,
    onSurface = Color(0xFFF6EEE6),
    surfaceVariant = Color(0xFF332B24),
    onSurfaceVariant = Color(0xFFD9CCBF),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF9C8B7C),
    outlineVariant = Color(0xFF4A4038),
)

@Composable
fun UdmurtTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Kids' app: scale all text up for readability while still respecting the
    // system font-size setting (capped so layouts stay usable).
    val systemDensity = LocalDensity.current
    val kidFriendlyDensity = Density(
        density = systemDensity.density,
        fontScale = (systemDensity.fontScale * 1.2f).coerceIn(1.2f, 1.8f)
    )

    CompositionLocalProvider(LocalDensity provides kidFriendlyDensity) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            content = content
        )
    }
}
