package com.seqaya.app.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private val stroke = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)

@Composable
fun GlyphHome(filled: Boolean, tint: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val s = size.minDimension / 24f
        val path = Path().apply {
            moveTo(4f * s, 11f * s)
            lineTo(12f * s, 4f * s)
            lineTo(20f * s, 11f * s)
            lineTo(20f * s, 20f * s)
            lineTo(4f * s, 20f * s)
            close()
        }
        drawOutline(path, tint, filled)
    }
}

@Composable
fun GlyphScan(filled: Boolean, tint: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val s = size.minDimension / 24f
        drawCornerBrackets(s = s, tint = tint)
        val cx = 12f * s
        val cy = 12f * s
        val r = 3f * s
        val circle = Path().apply {
            addOval(Rect(cx - r, cy - r, cx + r, cy + r))
        }
        drawOutline(circle, tint, filled)
    }
}

@Composable
fun GlyphBook(filled: Boolean, tint: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val s = size.minDimension / 24f
        val spine = Path().apply {
            moveTo(5f * s, 4f * s); lineTo(12f * s, 4f * s)
            lineTo(15f * s, 7f * s); lineTo(15f * s, 20f * s)
            lineTo(7f * s, 20f * s); lineTo(5f * s, 18f * s)
            close()
        }
        drawOutline(spine, tint, filled)
        val cover = Path().apply {
            moveTo(12f * s, 7f * s); lineTo(19f * s, 7f * s); lineTo(19f * s, 20f * s)
            moveTo(15f * s, 4f * s); lineTo(15f * s, 20f * s)
        }
        drawPath(cover, color = tint, style = stroke)
    }
}

private fun DrawScope.drawOutline(path: Path, tint: Color, filled: Boolean) {
    if (filled) drawPath(path, color = tint, style = Fill)
    drawPath(path, color = tint, style = stroke)
}

private fun DrawScope.drawCornerBrackets(s: Float, tint: Color) {
    val frame = Path().apply {
        moveTo(4f * s, 8f * s); lineTo(4f * s, 6f * s); lineTo(6f * s, 4f * s); lineTo(8f * s, 4f * s)
        moveTo(16f * s, 4f * s); lineTo(18f * s, 4f * s); lineTo(20f * s, 6f * s); lineTo(20f * s, 8f * s)
        moveTo(20f * s, 16f * s); lineTo(20f * s, 18f * s); lineTo(18f * s, 20f * s); lineTo(16f * s, 20f * s)
        moveTo(8f * s, 20f * s); lineTo(6f * s, 20f * s); lineTo(4f * s, 18f * s); lineTo(4f * s, 16f * s)
    }
    drawPath(frame, color = tint, style = stroke)
}
