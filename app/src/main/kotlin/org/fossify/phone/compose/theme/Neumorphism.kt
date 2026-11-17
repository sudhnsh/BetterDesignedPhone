package org.fossify.phone.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Pure Neumorphic modifier using dual framework shadow layers (Top-Left light highlight + Bottom-Right dark shadow).
 */
fun Modifier.neumorphic(
    cornerRadius: Dp = 24.dp,
    lightShadowColor: Color = Color.White,
    darkShadowColor: Color = Color(0x14000000), // ~8% Black
    shadowRadius: Dp = 8.dp,
    offset: Dp = 6.dp,
    isPressed: Boolean = false
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = Color.Transparent.toArgb()

        val radiusPx = cornerRadius.toPx()
        val shadowRadiusPx = shadowRadius.toPx()
        val offsetPx = if (isPressed) -offset.toPx() * 0.6f else offset.toPx()

        // 1. Dark shadow (Bottom-Right when unpressed, inverted when pressed)
        frameworkPaint.setShadowLayer(
            shadowRadiusPx,
            offsetPx,
            offsetPx,
            darkShadowColor.toArgb()
        )
        canvas.drawRoundRect(
            left = 0f, top = 0f, right = size.width, bottom = size.height,
            radiusX = radiusPx, radiusY = radiusPx,
            paint = paint
        )

        // 2. Light shadow (Top-Left when unpressed, inverted when pressed)
        frameworkPaint.setShadowLayer(
            shadowRadiusPx,
            -offsetPx,
            -offsetPx,
            lightShadowColor.toArgb()
        )
        canvas.drawRoundRect(
            left = 0f, top = 0f, right = size.width, bottom = size.height,
            radiusX = radiusPx, radiusY = radiusPx,
            paint = paint
        )
    }
}

data class NeumorphicThemeColors(
    val lightShadow: Color,
    val darkShadow: Color,
    val elementBackground: Color,
    val pressedBackground: Color
)

@Composable
fun getNeumorphicColors(): NeumorphicThemeColors {
    val bg = MaterialTheme.colorScheme.background
    val isOled = bg == Color.Black || bg.luminance() < 0.01f
    val isDark = bg.luminance() < 0.35f

    return when {
        isOled -> NeumorphicThemeColors(
            lightShadow = Color(0x38FFFFFF),
            darkShadow = Color.Black,
            elementBackground = bg,
            pressedBackground = Color(0xFF16161A)
        )
        isDark -> NeumorphicThemeColors(
            lightShadow = Color(0x28FFFFFF),
            darkShadow = Color(0x80000000),
            elementBackground = bg,
            pressedBackground = Color(0xFF141418)
        )
        else -> NeumorphicThemeColors(
            lightShadow = Color.White,
            darkShadow = Color(0x24000000),
            elementBackground = bg,
            pressedBackground = Color(0xFFE6E6E0)
        )
    }
}
