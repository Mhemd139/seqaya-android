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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seqaya.app.ui.theme.Seqaya

/**
 * Three concentric rings emanating from the center, staggered to create a
 * pulsing NFC "ready to tap" signal. Matches the design bundle's B·06 mockup:
 * 1.8 s per-ring lifetime, 600 ms stagger, LinearOutSlowInEasing.
 *
 * Passing [active] = false renders a single static ring (reduced-motion path).
 */
@Composable
fun NfcWaves(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    color: Color = Seqaya.colors.accentGreen,
    active: Boolean = true,
) {
    if (!active || rememberReduceMotion()) {
        Canvas(modifier = modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = this.size.minDimension / 4f,
                center = center,
                style = Stroke(width = this.size.minDimension / 120f),
            )
        }
        return
    }

    val transition = rememberInfiniteTransition(label = "nfcWaves")
    val progressA = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "waveA",
    ).value
    val progressB = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearOutSlowInEasing, delayMillis = 600),
            repeatMode = RepeatMode.Restart,
        ),
        label = "waveB",
    ).value
    val progressC = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearOutSlowInEasing, delayMillis = 1200),
            repeatMode = RepeatMode.Restart,
        ),
        label = "waveC",
    ).value

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val maxRadius = this.size.minDimension / 2.2f
        val minRadius = this.size.minDimension / 8f
        val strokeWidth = this.size.minDimension / 140f

        listOf(progressA, progressB, progressC).forEach { p ->
            val radius = minRadius + (maxRadius - minRadius) * p
            val alpha = (1f - p) * 0.7f
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth),
            )
        }
    }
}
