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

// Vico's HorizontalAxis.rememberBottom reserves vertical space for label text +
// tick + guideline. There's no public API to query the actual reserved height,
// so we mirror the default (12sp label + ~12dp padding) here so the watering-dot
// overlay shares the chart's plot rect rather than sitting below the line.
private val BOTTOM_AXIS_INSET = 24.dp

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
    val pointCount = points.size

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

    val rangeProvider = remember { CartesianLayerRangeProvider.fixed(minY = Y_AXIS_MIN, maxY = Y_AXIS_MAX) }

    val xFormatter = remember(points, range) {
        CartesianValueFormatter { _, value, _ ->
            val idx = value.toInt().coerceIn(0, (points.size - 1).coerceAtLeast(0))
            val ts = points.getOrNull(idx)?.timestamp ?: return@CartesianValueFormatter ""
            formatAxisLabel(ts, range)
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
                .pointerInput(pointCount) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat()
                        if (w <= 0f) return@detectTapGestures
                        val step = w / (pointCount - 1)
                        val nearest = (offset.x / step).roundToInt().coerceIn(0, pointCount - 1)
                        val dx = offset.x - nearest * step
                        selectedIndex = if (abs(dx) <= step / 2f) nearest else null
                    }
                },
        ) {
            if (wateringEvents.isEmpty()) return@Canvas

            val w = size.width
            val h = size.height
            val step = w / (pointCount - 1)
            val minTs = points.first().timestamp
            val maxTs = points.last().timestamp
            val totalMs = Duration.between(minTs, maxTs).toMillis().coerceAtLeast(1L)

            wateringEvents.forEach { event ->
                if (event.isBefore(minTs) || event.isAfter(maxTs)) return@forEach
                val ratio = Duration.between(minTs, event).toMillis().toDouble() / totalMs
                val idx = (ratio * (pointCount - 1)).roundToInt().coerceIn(0, pointCount - 1)
                val x = idx * step
                val y = (h - ((points[idx].percent - Y_AXIS_MIN) / (Y_AXIS_MAX - Y_AXIS_MIN)).toFloat() * h)
                drawCircle(color = accentBrown, radius = dotRadiusPx, center = Offset(x, y))
            }
        }

        val sel = selectedIndex
        if (sel != null && chartSizePx.width > 0 && sel in points.indices) {
            val point = points[sel]
            val step = chartSizePx.width.toFloat() / (pointCount - 1).coerceAtLeast(1)
            val xPx = (sel * step).roundToInt()
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
