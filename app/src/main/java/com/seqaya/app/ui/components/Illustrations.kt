package com.seqaya.app.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seqaya.app.ui.theme.Seqaya

/**
 * Hand-drawn illustration set for Seqaya's NFC flows.
 *
 * Style rules:
 *  - Stroke color: `Seqaya.colors.textPrimary` (dark ink, theme-aware)
 *  - Stroke width: ~1.5dp baseline, scales with illustration size
 *  - Accent fills: sage green (accentGreen) or terracotta (accentBrown), sparingly
 *  - No gradients, no drop shadows. Everything is ink + subtle fills.
 *
 * All composables take a `size` parameter (default 120.dp) and draw within the
 * box. They are vector (Canvas), so scaling is crisp at any size.
 */

@Composable
fun OpeningLeafIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    val ink = Seqaya.colors.textPrimary
    val accent = Seqaya.colors.accentGreen
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w / 80f
        val center = Offset(w / 2f, h / 2f)

        // Base stem
        drawLine(
            color = ink,
            start = Offset(center.x, h * 0.90f),
            end = Offset(center.x, h * 0.55f),
            strokeWidth = stroke,
        )

        // Opened leaf (larger, tilted right)
        drawLeaf(
            anchor = Offset(center.x, h * 0.55f),
            tipOffset = Offset(w * 0.38f, -h * 0.22f),
            bulge = 0.5f,
            color = ink,
            stroke = stroke,
            fillColor = accent.copy(alpha = 0.18f),
        )
        // Smaller leaf opening left
        drawLeaf(
            anchor = Offset(center.x, h * 0.55f),
            tipOffset = Offset(-w * 0.28f, -h * 0.18f),
            bulge = 0.42f,
            color = ink,
            stroke = stroke,
            fillColor = accent.copy(alpha = 0.12f),
        )
        // Central vein
        drawLine(
            color = ink.copy(alpha = 0.5f),
            start = Offset(center.x, h * 0.55f),
            end = Offset(center.x + w * 0.19f, h * 0.44f),
            strokeWidth = stroke * 0.6f,
        )
    }
}

@Composable
fun ClosedBudIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    val ink = Seqaya.colors.textPrimary
    val accent = Seqaya.colors.accentGreen
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w / 80f
        val center = Offset(w / 2f, h / 2f)

        // Stem
        drawLine(
            color = ink,
            start = Offset(center.x, h * 0.92f),
            end = Offset(center.x, h * 0.62f),
            strokeWidth = stroke,
        )
        // Closed bud (teardrop)
        val budPath = Path().apply {
            moveTo(center.x, h * 0.25f)
            cubicTo(
                x1 = center.x + w * 0.18f, y1 = h * 0.30f,
                x2 = center.x + w * 0.18f, y2 = h * 0.60f,
                x3 = center.x, y3 = h * 0.62f,
            )
            cubicTo(
                x1 = center.x - w * 0.18f, y1 = h * 0.60f,
                x2 = center.x - w * 0.18f, y2 = h * 0.30f,
                x3 = center.x, y3 = h * 0.25f,
            )
            close()
        }
        drawPath(path = budPath, color = accent.copy(alpha = 0.15f))
        drawPath(path = budPath, color = ink, style = Stroke(width = stroke))
        // Fold seam down the middle
        drawLine(
            color = ink.copy(alpha = 0.5f),
            start = Offset(center.x, h * 0.30f),
            end = Offset(center.x, h * 0.60f),
            strokeWidth = stroke * 0.5f,
        )
    }
}

@Composable
fun SensorIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    withDroplets: Boolean = false,
    withCheckmark: Boolean = false,
) {
    val ink = Seqaya.colors.textPrimary
    val accent = Seqaya.colors.accentGreen
    val brown = Seqaya.colors.accentBrown
    val bodyFill = Seqaya.colors.bgCreamLightest
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val minDim = this.size.minDimension
        val stroke = w / 90f

        // Sensor body (rounded rectangle)
        val bodyRect = Rect(
            left = w * 0.32f, top = h * 0.10f,
            right = w * 0.68f, bottom = h * 0.55f,
        )
        val bodyPath = Path().apply {
            addRoundRect(RoundRect(bodyRect, CornerRadius(w * 0.04f, w * 0.04f)))
        }
        drawPath(bodyPath, color = bodyFill)
        drawPath(bodyPath, color = ink, style = Stroke(width = stroke))

        // Label slot on body
        drawLine(
            color = ink.copy(alpha = 0.5f),
            start = Offset(w * 0.38f, h * 0.20f),
            end = Offset(w * 0.62f, h * 0.20f),
            strokeWidth = stroke * 0.6f,
        )
        drawLine(
            color = ink.copy(alpha = 0.35f),
            start = Offset(w * 0.38f, h * 0.28f),
            end = Offset(w * 0.55f, h * 0.28f),
            strokeWidth = stroke * 0.6f,
        )

        // Two prongs below the body
        drawLine(
            color = ink,
            start = Offset(w * 0.42f, h * 0.55f),
            end = Offset(w * 0.42f, h * 0.88f),
            strokeWidth = stroke,
        )
        drawLine(
            color = ink,
            start = Offset(w * 0.58f, h * 0.55f),
            end = Offset(w * 0.58f, h * 0.88f),
            strokeWidth = stroke,
        )
        // Prong tips
        drawCircle(color = ink, radius = stroke * 1.2f, center = Offset(w * 0.42f, h * 0.88f))
        drawCircle(color = ink, radius = stroke * 1.2f, center = Offset(w * 0.58f, h * 0.88f))

        if (withDroplets) {
            drawDroplet(Offset(w * 0.20f, h * 0.60f), minDim * 0.04f, accent.copy(alpha = 0.6f), stroke)
            drawDroplet(Offset(w * 0.82f, h * 0.72f), minDim * 0.045f, accent.copy(alpha = 0.7f), stroke)
            drawDroplet(Offset(w * 0.16f, h * 0.80f), minDim * 0.035f, accent.copy(alpha = 0.5f), stroke)
        }

        if (withCheckmark) {
            val cx = w * 0.82f
            val cy = h * 0.22f
            val r = w * 0.08f
            drawCircle(color = brown.copy(alpha = 0.15f), radius = r, center = Offset(cx, cy))
            drawCircle(color = brown, radius = r, center = Offset(cx, cy), style = Stroke(width = stroke))
            // Checkmark strokes
            drawLine(
                color = brown,
                start = Offset(cx - r * 0.4f, cy),
                end = Offset(cx - r * 0.1f, cy + r * 0.3f),
                strokeWidth = stroke * 1.1f,
            )
            drawLine(
                color = brown,
                start = Offset(cx - r * 0.1f, cy + r * 0.3f),
                end = Offset(cx + r * 0.45f, cy - r * 0.35f),
                strokeWidth = stroke * 1.1f,
            )
        }
    }
}

@Composable
fun SensorInGlassIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    withRipples: Boolean = false,
) {
    val ink = Seqaya.colors.textPrimary
    val water = Seqaya.colors.accentBlue
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w / 100f

        // Glass: slightly flared trapezoid
        val glassLeftTop = Offset(w * 0.20f, h * 0.28f)
        val glassRightTop = Offset(w * 0.80f, h * 0.28f)
        val glassLeftBot = Offset(w * 0.26f, h * 0.92f)
        val glassRightBot = Offset(w * 0.74f, h * 0.92f)

        drawLine(ink, glassLeftTop, glassLeftBot, strokeWidth = stroke)
        drawLine(ink, glassRightTop, glassRightBot, strokeWidth = stroke)
        drawLine(ink, glassLeftBot, glassRightBot, strokeWidth = stroke)

        // Water fill (slightly below top rim)
        val waterLineY = h * 0.42f
        val waterPath = Path().apply {
            moveTo(w * 0.22f, waterLineY)
            lineTo(w * 0.78f, waterLineY)
            lineTo(glassRightBot.x, glassRightBot.y)
            lineTo(glassLeftBot.x, glassLeftBot.y)
            close()
        }
        drawPath(waterPath, color = water.copy(alpha = 0.14f))
        // Water line
        drawLine(
            color = water.copy(alpha = 0.7f),
            start = Offset(w * 0.22f, waterLineY),
            end = Offset(w * 0.78f, waterLineY),
            strokeWidth = stroke,
        )

        // Sensor probe dipping in from above
        val probeLeft = w * 0.45f
        val probeRight = w * 0.55f
        val probeTop = h * 0.12f
        val probeBodyBot = h * 0.35f

        // Body (thin rectangle sticking out of water)
        drawLine(ink, Offset(probeLeft, probeTop), Offset(probeLeft, probeBodyBot), strokeWidth = stroke)
        drawLine(ink, Offset(probeRight, probeTop), Offset(probeRight, probeBodyBot), strokeWidth = stroke)
        drawLine(ink, Offset(probeLeft, probeTop), Offset(probeRight, probeTop), strokeWidth = stroke)
        // Two prongs descending into the water
        drawLine(
            color = ink,
            start = Offset(w * 0.47f, probeBodyBot),
            end = Offset(w * 0.47f, h * 0.78f),
            strokeWidth = stroke,
        )
        drawLine(
            color = ink,
            start = Offset(w * 0.53f, probeBodyBot),
            end = Offset(w * 0.53f, h * 0.78f),
            strokeWidth = stroke,
        )

        if (withRipples) {
            // Two concentric rings around the probe entry point
            listOf(0.10f, 0.18f).forEach { r ->
                drawCircle(
                    color = water.copy(alpha = 0.5f - r),
                    radius = w * r,
                    center = Offset(w / 2f, waterLineY),
                    style = Stroke(width = stroke * 0.8f),
                )
            }
        }
    }
}

@Composable
fun AnimatedWaterRipples(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
) {
    val ink = Seqaya.colors.accentBlue
    val reduceMotion = rememberReduceMotion()

    val progress = if (reduceMotion) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "ripples")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "ripple",
        ).value
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val stroke = w / 120f
        val center = Offset(w / 2f, this.size.height / 2f)
        val maxR = w * 0.40f

        if (reduceMotion) {
            // Static ring at mid-radius — conveys "ripple" idea without motion.
            drawCircle(
                color = ink.copy(alpha = 0.4f),
                radius = maxR * 0.5f,
                center = center,
                style = Stroke(width = stroke),
            )
            return@Canvas
        }
        listOf(progress, (progress + 0.5f) % 1f).forEach { p ->
            val r = p * maxR
            val alpha = (1f - p) * 0.6f
            drawCircle(
                color = ink.copy(alpha = alpha),
                radius = r,
                center = center,
                style = Stroke(width = stroke),
            )
        }
    }
}

@Composable
fun DeviceIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    withHalo: Boolean = false,
) {
    val ink = Seqaya.colors.textPrimary
    val halo = Seqaya.colors.accentGreen
    val bodyFill = Seqaya.colors.bgCreamLightest
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w / 110f

        val center = Offset(w / 2f, h / 2f)

        if (withHalo) {
            // Soft pulsing halo ring (static draw; use AnimatedDeviceHalo for pulse)
            drawCircle(
                color = halo.copy(alpha = 0.12f),
                radius = w * 0.45f,
                center = center,
            )
            drawCircle(
                color = halo.copy(alpha = 0.6f),
                radius = w * 0.38f,
                center = center,
                style = Stroke(width = stroke * 0.6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(w * 0.02f, w * 0.015f))),
            )
        }

        // Device puck body (rounded square)
        val puckSize = w * 0.45f
        val puckRect = Rect(
            left = center.x - puckSize / 2f,
            top = center.y - puckSize / 2f,
            right = center.x + puckSize / 2f,
            bottom = center.y + puckSize / 2f,
        )
        val puckPath = Path().apply {
            addRoundRect(RoundRect(puckRect, CornerRadius(w * 0.06f, w * 0.06f)))
        }
        drawPath(puckPath, color = bodyFill)
        drawPath(puckPath, color = ink, style = Stroke(width = stroke))

        // Inscribed leaf mark (Seqaya wordmark stand-in)
        val leafCenter = center
        drawLeaf(
            anchor = Offset(leafCenter.x - w * 0.04f, leafCenter.y + w * 0.04f),
            tipOffset = Offset(w * 0.08f, -w * 0.08f),
            bulge = 0.5f,
            color = ink,
            stroke = stroke * 0.9f,
            fillColor = halo.copy(alpha = 0.18f),
        )
    }
}

@Composable
fun LeafCheckIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    dew: Boolean = false,
) {
    val ink = Seqaya.colors.textPrimary
    val green = Seqaya.colors.accentGreen
    val brown = Seqaya.colors.accentBrown
    val dewColor = Seqaya.colors.accentBlue
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w / 85f
        val center = Offset(w / 2f, h / 2f)

        // Leaf body
        drawLeaf(
            anchor = Offset(center.x - w * 0.18f, center.y + h * 0.18f),
            tipOffset = Offset(w * 0.38f, -h * 0.36f),
            bulge = 0.5f,
            color = ink,
            stroke = stroke,
            fillColor = green.copy(alpha = 0.15f),
        )

        if (dew) {
            // Dew drops scattered on the leaf
            drawDroplet(Offset(w * 0.40f, h * 0.38f), w * 0.03f, dewColor.copy(alpha = 0.6f), stroke * 0.8f)
            drawDroplet(Offset(w * 0.55f, h * 0.52f), w * 0.035f, dewColor.copy(alpha = 0.7f), stroke * 0.8f)
            drawDroplet(Offset(w * 0.48f, h * 0.68f), w * 0.025f, dewColor.copy(alpha = 0.5f), stroke * 0.8f)
        }

        // Small brown checkmark in upper right
        val cx = w * 0.78f
        val cy = h * 0.22f
        val r = w * 0.09f
        drawCircle(color = brown.copy(alpha = 0.15f), radius = r, center = Offset(cx, cy))
        drawCircle(color = brown, radius = r, center = Offset(cx, cy), style = Stroke(width = stroke))
        drawLine(
            color = brown,
            start = Offset(cx - r * 0.4f, cy),
            end = Offset(cx - r * 0.08f, cy + r * 0.32f),
            strokeWidth = stroke * 1.1f,
        )
        drawLine(
            color = brown,
            start = Offset(cx - r * 0.08f, cy + r * 0.32f),
            end = Offset(cx + r * 0.45f, cy - r * 0.35f),
            strokeWidth = stroke * 1.1f,
        )
    }
}

// ---------- shared drawing helpers ----------

private fun DrawScope.drawLeaf(
    anchor: Offset,
    tipOffset: Offset,
    bulge: Float,
    color: Color,
    stroke: Float,
    fillColor: Color? = null,
) {
    val tip = Offset(anchor.x + tipOffset.x, anchor.y + tipOffset.y)
    val mid = Offset((anchor.x + tip.x) / 2f, (anchor.y + tip.y) / 2f)
    val len = kotlin.math.hypot(tipOffset.x, tipOffset.y)
    val perpX = -tipOffset.y / len
    val perpY = tipOffset.x / len
    val bulgeDist = len * bulge
    val ctrlA = Offset(mid.x + perpX * bulgeDist, mid.y + perpY * bulgeDist)
    val ctrlB = Offset(mid.x - perpX * bulgeDist, mid.y - perpY * bulgeDist)

    val path = Path().apply {
        moveTo(anchor.x, anchor.y)
        quadraticTo(ctrlA.x, ctrlA.y, tip.x, tip.y)
        quadraticTo(ctrlB.x, ctrlB.y, anchor.x, anchor.y)
        close()
    }
    fillColor?.let { drawPath(path = path, color = it) }
    drawPath(path = path, color = color, style = Stroke(width = stroke))
}

private fun DrawScope.drawDroplet(
    center: Offset,
    radius: Float,
    color: Color,
    stroke: Float,
) {
    drawCircle(color = color.copy(alpha = color.alpha * 0.4f), radius = radius, center = center)
    drawCircle(color = color, radius = radius, center = center, style = Stroke(width = stroke))
}
