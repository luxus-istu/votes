package ru.luxus.votes.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors that are not part of the Material 3 color roles.
 * Keeping them in the theme makes success/error feedback readable in both
 * light and dark mode instead of relying on fixed hex values in screens.
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color
) {
    companion object {
        val Light = ExtendedColors(
            success = CorrectGreen,
            onSuccess = White,
            successContainer = UdmurtGreenLight,
            onSuccessContainer = UdmurtGreenDark
        )

        val Dark = ExtendedColors(
            success = Color(0xFF9AD6A9),
            onSuccess = Color(0xFF0C3418),
            successContainer = Color(0xFF255A33),
            onSuccessContainer = Color(0xFFC3F0CE)
        )
    }
}

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors.Light }