package com.seqaya.app.ui.device

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import com.seqaya.app.ui.theme.Seqaya
import java.time.Duration
import java.time.Instant
import kotlin.math.abs
import kotlin.math.roundToInt

data class MoisturePoint(val timestamp: Instant, val percent: Int)

@Composable
fun MoistureChart(
    points: List<MoisturePoint>,
    wateringEvents: List<Instant>,
    targetPercent: Int?,
    modifier: Modifier = Modifier,
) {
    val colors = Seqaya.colors

    if (points.isEmpty()) {
        Box(
            modifier = modifier.height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Waiting for the first reading…",
                color = colors.textTertiary,
                style = Seqaya.type.body.copy(fontSize = 14.sp),
            )
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    val accentGreen = colors.accentGreen
    val borderColor = colors.border

    LaunchedEffect(points) {
        modelProducer.runTransaction {
            lineSeries { series(points.map { it.percent.toDouble() }) }
        }
    }

    val line = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(accentGreen)),
        thickness = 1.6.dp,
        areaFill = null,
    )

    val targetDecoration = remember(targetPercent, borderColor) {
        if (targetPercent == null) {
            emptyList()
        } else {
            listOf(
                HorizontalLine(
                    y = { targetPercent.toDouble() },
                    line = LineComponent(
                        fill = Fill(borderColor.toArgb()),
                        thicknessDp = 1f,
                        shape = DashedShape(
                            dashLengthDp = 4f,
                            gapLengthDp = 4f,
                        ),
                    ),
                )
            )
        }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var chartSizePx by remember { mutableStateOf(IntOffset.Zero) }
    val density = LocalDensity.current
    val dotRadiusPx = with(density) { 2.8.dp.toPx() }
    val now = remember(points) { Instant.now() }

    Box(modifier = modifier.height(180.dp)) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(line),
                ),
                decorations = targetDecoration,
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxSize(),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )

        // Watering dots + tap-to-inspect overlay.
        // Positions are approximate — linearly interpolated across the chart width
        // based on each point's index within the series. This matches Vico's default
        // layout for evenly-spaced series.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat()
                        if (w <= 0f || points.size < 2) return@detectTapGestures
                        val step = w / (points.size - 1)
                        val nearest = (offset.x / step).roundToInt().coerceIn(0, points.size - 1)
                        val dx = offset.x - nearest * step
                        selectedIndex = if (abs(dx) <= step / 2f) nearest else null
                    }
                },
        ) {
            chartSizePx = IntOffset(size.width.roundToInt(), size.height.roundToInt())
            if (points.size < 2 || wateringEvents.isEmpty()) return@Canvas

            val w = size.width
            val h = size.height
            val step = w / (points.size - 1)
            val minTs = points.first().timestamp
            val maxTs = points.last().timestamp
            val totalMs = Duration.between(minTs, maxTs).toMillis().coerceAtLeast(1L)

            wateringEvents.forEach { event ->
                if (event < minTs || event > maxTs) return@forEach
                val ratio = Duration.between(minTs, event).toMillis().toDouble() / totalMs
                val fractionalIndex = ratio * (points.size - 1)
                val x = (fractionalIndex * step).toFloat()

                // Use the y of the nearest data point for vertical placement (approximate).
                val idx = fractionalIndex.roundToInt().coerceIn(0, points.size - 1)
                val percent = points[idx].percent.toDouble()
                val minY = points.minOf { it.percent }.toDouble()
                val maxY = points.maxOf { it.percent }.toDouble()
                val yRange = (maxY - minY).coerceAtLeast(1.0)
                val y = (h - ((percent - minY) / yRange).toFloat() * h)

                drawCircle(
                    color = colors.accentBrown,
                    radius = dotRadiusPx,
                    center = Offset(x, y),
                )
            }
        }

        val sel = selectedIndex
        if (sel != null && chartSizePx.x > 0) {
            val point = points[sel]
            val step = chartSizePx.x.toFloat() / (points.size - 1).coerceAtLeast(1)
            val xPx = (sel * step).roundToInt()
            val labelOffsetDp = with(density) { xPx.toDp() }

            Box(
                modifier = Modifier
                    .offset(x = labelOffsetDp - 40.dp, y = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.bgCreamLightest)
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${point.percent}% · ${formatRelative(point.timestamp, now)}",
                    color = colors.textPrimary,
                    style = Seqaya.type.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                )
            }
        }
    }
}

private fun formatRelative(ts: Instant, now: Instant): String {
    val minutes = Duration.between(ts, now).toMinutes()
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 60 * 24 -> "${minutes / 60}h ago"
        else -> "${minutes / (60 * 24)}d ago"
    }
}
