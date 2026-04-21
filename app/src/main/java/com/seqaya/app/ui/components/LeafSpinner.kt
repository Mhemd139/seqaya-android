package com.seqaya.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seqaya.app.ui.theme.Seqaya
import kotlin.math.cos
import kotlin.math.sin

/**
 * Signature loading indicator for Seqaya. Five stylized leaves orbit a center
 * point on a slow eased cycle. Stroke width and palette match the hand-drawn
 * illustration set so the spinner reads as part of the same visual family.
 *
 * Used everywhere in the app that previously would have shown a CircularProgressIndicator.
 * Respects reduced-motion: static first-frame layout when motion is disabled.
 */
@Composable
fun LeafSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = Seqaya.colors.accentGreen,
) {
    // Compose's LocalAccessibilityManager is too thin to expose a reduce-motion flag,
    // and Android itself doesn't have a first-class reduce-motion API. Use the platform
    // AccessibilityManager's isTouchExplorationEnabled as the closest public-API proxy:
    // users running a screen-reader / assistive tech are the population most harmed by
    // animated motion, so degrading to a static state for them is conservative and correct.
    val context = LocalContext.current
    val reduceMotion = (context.getSystemService(Context.ACCESSIBILITY_SERVICE)
        as? AccessibilityManager)?.isTouchExplorationEnabled == true

    val rotation = if (reduceMotion) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "leafSpinnerRotation")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "leafRotation",
        ).value
    }

    val breathe = if (reduceMotion) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "leafSpinnerBreath")
        transition.animateFloat(
            initialValue = 0.88f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "leafBreath",
        ).value
    }

    Canvas(modifier = modifier.size(size)) {
        drawLeafRing(rotation = rotation, scale = breathe, color = color, leafCount = 5)
    }
}

private fun DrawScope.drawLeafRing(
    rotation: Float,
    scale: Float,
    color: Color,
    leafCount: Int,
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val ringRadius = size.minDimension / 2.6f
    val leafLengthPx = size.minDimension / 4.2f * scale
    val strokeWidthPx = size.minDimension / 22f

    translate(center.x, center.y) {
        rotate(rotation, pivot = Offset.Zero) {
            val step = 360f / leafCount
            for (i in 0 until leafCount) {
                val angle = (i * step) * (Math.PI / 180f).toFloat()
                val leafAnchor = Offset(
                    x = cos(angle) * ringRadius,
                    y = sin(angle) * ringRadius,
                )
                drawLeafAt(
                    anchor = leafAnchor,
                    angleRad = angle,
                    leafLength = leafLengthPx,
                    strokeWidth = strokeWidthPx,
                    color = color,
                )
            }
        }
    }
}

private fun DrawScope.drawLeafAt(
    anchor: Offset,
    angleRad: Float,
    leafLength: Float,
    strokeWidth: Float,
    color: Color,
) {
    val tipX = anchor.x + cos(angleRad) * leafLength
    val tipY = anchor.y + sin(angleRad) * leafLength
    val perpX = -sin(angleRad)
    val perpY = cos(angleRad)
    val bulge = leafLength * 0.35f
    val midX = (anchor.x + tipX) / 2f
    val midY = (anchor.y + tipY) / 2f
    val ctrlAX = midX + perpX * bulge
    val ctrlAY = midY + perpY * bulge
    val ctrlBX = midX - perpX * bulge
    val ctrlBY = midY - perpY * bulge

    val path = Path().apply {
        moveTo(anchor.x, anchor.y)
        quadraticTo(ctrlAX, ctrlAY, tipX, tipY)
        quadraticTo(ctrlBX, ctrlBY, anchor.x, anchor.y)
    }
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
}
