package ru.luxus.votes.core.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.luxus.votes.core.theme.UdmurtGold
import ru.luxus.votes.core.theme.UdmurtRed

/**
 * Authentic Udmurt Solar Symbol ("Толэзё" / "Шунды-пужы").
 * An eight-pointed solar star, central to the Udmurt flag and cultural heritage,
 * symbolizing the sun, life, and spiritual protection.
 */
@Composable
fun UdmurtSolarSymbol(
    modifier: Modifier = Modifier.size(28.dp),
    color: Color = UdmurtRed,
    strokeWidth: Float = 2.5f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (minOf(w, h) / 2f) * 0.9f

        // Draw 8-pointed star arms using path
        val path = Path()
        val numPoints = 8
        val innerR = r * 0.42f
        val step = (2.0 * Math.PI) / (numPoints * 2)

        for (i in 0 until (numPoints * 2)) {
            val currR = if (i % 2 == 0) r else innerR
            val angle = i * step - (Math.PI / 2.0)
            val x = (cx + currR * kotlin.math.cos(angle)).toFloat()
            val y = (cy + currR * kotlin.math.sin(angle)).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        drawPath(path = path, color = color)
        // Center small diamond accent
        drawCircle(color = Color.White.copy(alpha = 0.85f), radius = innerR * 0.35f, center = Offset(cx, cy))
        drawCircle(color = color, radius = innerR * 0.18f, center = Offset(cx, cy))
    }
}

/**
 * Traditional decorative geometric band divider for Udmurt style headers and cards.
 */
@Composable
fun UdmurtOrnamentBand(
    modifier: Modifier = Modifier.fillMaxWidth().height(8.dp),
    color: Color = UdmurtRed
) {
    Canvas(modifier = modifier) {
        val segW = 16f
        val count = (size.width / segW).toInt()
        val midY = size.height / 2f

        for (i in 0..count) {
            val startX = i * segW
            // Draw zigzag / diamond motif
            val p = Path().apply {
                moveTo(startX, midY)
                lineTo(startX + segW / 2f, 0f)
                lineTo(startX + segW, midY)
                lineTo(startX + segW / 2f, size.height)
                close()
            }
            drawPath(path = p, color = if (i % 2 == 0) color else color.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun MascotBadge(
    emoji: String,
    modifier: Modifier = Modifier.size(56.dp),
    bgColor: Color = UdmurtGold.copy(alpha = 0.2f),
    borderColor: Color = UdmurtRed
) {
    Box(
        modifier = modifier
            .background(bgColor, CircleShape)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        UdmurtSolarSymbol(
            modifier = Modifier.fillMaxSize(),
            color = borderColor.copy(alpha = 0.25f)
        )
        Text(text = emoji, fontSize = 28.sp)
    }
}
