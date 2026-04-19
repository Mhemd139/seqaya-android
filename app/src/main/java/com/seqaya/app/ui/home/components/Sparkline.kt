package com.seqaya.app.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.seqaya.app.ui.theme.Seqaya

private const val MIN_DOMAIN_PERCENT = 25f
private const val MAX_DOMAIN_PERCENT = 85f

@Composable
fun Sparkline(
    moisturePoints: List<Int>,
    wateringEventIndices: List<Int>,
    modifier: Modifier = Modifier.fillMaxWidth().height(50.dp),
) {
    val lineColor = Seqaya.colors.accentGreen
    val dotColor = Seqaya.colors.accentBrown

    Canvas(modifier = modifier) {
        if (moisturePoints.size < 2) return@Canvas
        val n = moisturePoints.size
        val domain = MAX_DOMAIN_PERCENT - MIN_DOMAIN_PERCENT
        val points = moisturePoints.mapIndexed { index, percent ->
            val clamped = percent.toFloat().coerceIn(MIN_DOMAIN_PERCENT, MAX_DOMAIN_PERCENT)
            val x = index / (n - 1f) * size.width
            val y = size.height - ((clamped - MIN_DOMAIN_PERCENT) / domain) * size.height
            Offset(x, y)
        }
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        wateringEventIndices.forEach { i ->
            val p = points.getOrNull(i) ?: return@forEach
            drawCircle(color = dotColor, radius = 2.5f, center = p)
        }
    }
}
