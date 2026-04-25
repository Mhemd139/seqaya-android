package com.seqaya.app.ui.device

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import com.seqaya.app.R
import com.seqaya.app.ui.theme.Seqaya
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

data class MoisturePoint(val timestamp: Instant, val percent: Int)

private const val Y_AXIS_MIN = 0.0
private const val Y_AXIS_MAX = 100.0
private const val X_AXIS_SCALE = 10_000

// HorizontalAxis.rememberBottom reserves vertical space for label + tick + guideline.
// Vico exposes no public API to query the actual reserved height, so we pad the
// watering-dot overlay by this constant so dots stay on the line, not the axis.
private val BOTTOM_AXIS_INSET = 24.dp

/**
 * Time-windowed moisture chart.
 *
 * The x-axis spans the full [range] window — `now - range.durationMs` to `now` —
 * regardless of how much data the device has logged. So a 30-day range with
 * only 6 hours of points renders the line clustered on the right with empty
 * space on the left, instead of stretching 6 hours across 30 days. This is the
 * behavior the user expects when switching ranges.
 */
@Composable
fun MoistureChart(
    points: List<MoisturePoint>,
    wateringEvents: List<Instant>,
    targetPercent: Int?,
    modifier: Modifier = Modifier,
    range: ChartRange = ChartRange.DAYS_30,
) {
    val colors = Seqaya.colors

    if (points.size < 2) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.device_empty_chart),
                color = colors.textTertiary,
                style = Seqaya.type.body.copy(fontSize = 14.sp),
            )
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    val accentGreen = colors.accentGreen
    val accentBrown = colors.accentBrown
    val borderColor = colors.border

    // Pin the window's right edge for the duration of this composition so axis
    // labels and the model agree on "now". Re-pins when range or points change.
    val windowEndMs = remember(range, points) { System.currentTimeMillis() }
    val windowStartMs = windowEndMs - range.durationMs
    val windowSpanMs = (windowEndMs - windowStartMs).coerceAtLeast(1L)

    // x-values are scaled to integer [0..X_AXIS_SCALE] within the window. Vico
    // rejects raw epoch ms (OOM in axis generator) AND values with >4 decimals
    // ("x values are too precise"), so we use whole numbers.
    val xValues = remember(points, windowStartMs, windowSpanMs) {
        points.map {
            ((it.timestamp.toEpochMilli() - windowStartMs).toDouble() / windowSpanMs * X_AXIS_SCALE)
                .roundToInt()
                .toDouble()
        }
    }
    val yValues = remember(points) {
        points.map { it.percent.toDouble() }
    }

    LaunchedEffect(xValues, yValues) {
        modelProducer.runTransaction {
            lineSeries { series(x = xValues, y = yValues) }
        }
    }

    val line = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(accentGreen)),
        thickness = 1.6.dp,
        areaFill = null,
    )

    // Fixed window on x, 0..100 on y. Sparse data falls where it actually is
    // in time rather than being stretched edge-to-edge.
    val rangeProvider = remember {
        CartesianLayerRangeProvider.fixed(
            minX = 0.0,
            maxX = X_AXIS_SCALE.toDouble(),
            minY = Y_AXIS_MIN,
            maxY = Y_AXIS_MAX,
        )
    }

    val xFormatter = remember(range, windowStartMs, windowSpanMs) {
        CartesianValueFormatter { _, value, _ ->
            val fraction = (value / X_AXIS_SCALE).coerceIn(0.0, 1.0)
            val epochMs = windowStartMs + (fraction * windowSpanMs).toLong()
            formatAxisLabel(Instant.ofEpochMilli(epochMs), range)
        }
    }
    val bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xFormatter)

    val targetDecoration = remember(targetPercent, borderColor) {
        if (targetPercent == null) emptyList() else listOf(
            HorizontalLine(
                y = { targetPercent.toDouble() },
                line = LineComponent(
                    fill = Fill(borderColor.toArgb()),
                    thicknessDp = 1f,
                    shape = DashedShape(dashLengthDp = 4f, gapLengthDp = 4f),
                ),
            )
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var chartSizePx by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val dotRadiusPx = with(density) { 2.8.dp.toPx() }
    val now = remember(points) { Instant.now() }

    LaunchedEffect(points) { selectedIndex = null }

    Box(
        modifier = modifier
            .onSizeChanged { chartSizePx = it },
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(line),
                    rangeProvider = rangeProvider,
                ),
                bottomAxis = bottomAxis,
                decorations = targetDecoration,
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxSize(),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = BOTTOM_AXIS_INSET)
                .pointerInput(points, windowStartMs, windowEndMs) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat()
                        if (w <= 0f) return@detectTapGestures
                        val tappedMs = windowStartMs + (offset.x / w * (windowEndMs - windowStartMs)).toLong()
                        val nearest = points.indices.minByOrNull {
                            kotlin.math.abs(points[it].timestamp.toEpochMilli() - tappedMs)
                        } ?: return@detectTapGestures
                        // Hit if within ~5% of the visible window (responsive on all ranges).
                        val tolerance = (windowEndMs - windowStartMs) / 20
                        val delta = kotlin.math.abs(points[nearest].timestamp.toEpochMilli() - tappedMs)
                        selectedIndex = if (delta <= tolerance) nearest else null
                    }
                },
        ) {
            if (wateringEvents.isEmpty()) return@Canvas

            val w = size.width
            val h = size.height
            val totalMs = (windowEndMs - windowStartMs).coerceAtLeast(1L)

            wateringEvents.forEach { event ->
                val eventMs = event.toEpochMilli()
                if (eventMs < windowStartMs || eventMs > windowEndMs) return@forEach
                val x = ((eventMs - windowStartMs).toDouble() / totalMs).toFloat() * w
                // Pin the dot's y to the line at the nearest data point in time.
                val nearest = points.minByOrNull { abs(it.timestamp.toEpochMilli() - eventMs) }
                    ?: return@forEach
                val y = (h - ((nearest.percent - Y_AXIS_MIN) / (Y_AXIS_MAX - Y_AXIS_MIN)).toFloat() * h)
                drawCircle(color = accentBrown, radius = dotRadiusPx, center = Offset(x, y))
            }
        }

        val sel = selectedIndex
        if (sel != null && chartSizePx.width > 0 && sel in points.indices) {
            val point = points[sel]
            val totalMs = (windowEndMs - windowStartMs).coerceAtLeast(1L)
            val fraction = (point.timestamp.toEpochMilli() - windowStartMs).toDouble() / totalMs
            val xPx = (fraction * chartSizePx.width).roundToInt().coerceIn(0, chartSizePx.width)
            val labelWidthDp = 88.dp
            val rawX = with(density) { xPx.toDp() } - labelWidthDp / 2
            val maxX = with(density) { chartSizePx.width.toDp() } - labelWidthDp
            val clampedX = rawX.coerceIn(0.dp, maxX.coerceAtLeast(0.dp))

            Box(
                modifier = Modifier
                    .offset(x = clampedX, y = 4.dp)
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

private val AXIS_ZONE: ZoneId = ZoneId.systemDefault()
private val HOUR_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(AXIS_ZONE)
private val DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE").withZone(AXIS_ZONE)
private val MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d").withZone(AXIS_ZONE)

private fun formatAxisLabel(ts: Instant, range: ChartRange): String = when (range) {
    ChartRange.HOURS_24 -> HOUR_FORMATTER.format(ts)
    ChartRange.DAYS_7 -> DAY_FORMATTER.format(ts)
    ChartRange.DAYS_30 -> MONTH_FORMATTER.format(ts)
}

private fun formatRelative(ts: Instant, now: Instant): String {
    val minutes = Duration.between(ts, now).toMinutes().coerceAtLeast(0L)
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 60 * 24 -> "${minutes / 60}h ago"
        else -> "${minutes / (60 * 24)}d ago"
    }
}
