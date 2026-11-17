package org.fossify.phone.compose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.fossify.phone.helpers.BarcodeEncoder

@Composable
fun BarcodeCanvas(
    code: String,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(52.dp),
    barColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val modules = remember(code) { BarcodeEncoder.encodeCode128(code) }

    Box(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (modules.isEmpty()) return@Canvas
            val totalModules = modules.size
            val moduleWidth = size.width / totalModules

            modules.forEachIndexed { index, isBar ->
                if (isBar) {
                    drawRect(
                        color = barColor,
                        topLeft = Offset(x = index * moduleWidth, y = 0f),
                        size = Size(width = moduleWidth + 0.5f, height = size.height)
                    )
                }
            }
        }
    }
}
