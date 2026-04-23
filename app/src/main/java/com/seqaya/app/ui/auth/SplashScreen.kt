package com.seqaya.app.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seqaya.app.R
import com.seqaya.app.ui.theme.Seqaya
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Launch sequence — Android port of the Claude Design prototype, paced for
 * a calm 5-second introduction.
 *
 * Strategy (option C, "hybrid"):
 *   - The Android 12+ system splash plays the stroke-by-stroke draw-on via
 *     the AVD (`leaf_awaken`). This is the calligraphy moment.
 *   - The in-app composable takes over with the leaf already FULLY DRAWN
 *     at hero scale — no blank-frame blink at handoff because the first
 *     painted state in Compose matches the last painted state of the AVD.
 *   - The first in-app motion is a gentle "arrival" scale-pulse
 *     (0.95 → 1.04 → 1.00 over 600ms) that acknowledges the handoff
 *     without restarting the calligraphy.
 *   - Then breath, then travel + wordmark reveal, then tagline, then route.
 *
 * Timeline (in-app, after system splash):
 *   T = 0..600    Arrival pulse: scale 0.95 → 1.04 → 1.00, eased.
 *   T = 600..1500 Drawn leaf settles, breathing softly at hero scale.
 *   T = 1500..3300 Leaf glides to rest slot; wordmark reveals in lock-step.
 *   T = 3300..3700 Stillness — composition holds for one breath.
 *   T = 3700..4050 Tagline fades in (350ms).
 *   T = 4050..5000 Final hold, then hand off to the next destination.
 *
 * Reduced motion: snap to final state, hold 300ms, hand off.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val motion = Seqaya.motion
    val reduceMotion = LocalAccessibilityManager.current?.let {
        @Suppress("DEPRECATION")
        it.calculateRecommendedTimeoutMillis(0L, false, true, false) > 0L
    } ?: false

    // Arrival starts at 0.95 so the very first painted frame is visually
    // close to the system splash's final 1.00 (the human eye does not
    // notice a 5% scale delta between two adjacent frames).
    val arrivalScale = remember { Animatable(0.95f) }
    val travelProgress = remember { Animatable(0f) }
    val revealProgress = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    var screenCenter by remember { mutableStateOf(Offset.Zero) }
    var leafRestCenter by remember { mutableStateOf(Offset.Zero) }
    val ready = screenCenter != Offset.Zero && leafRestCenter != Offset.Zero

    LaunchedEffect(ready) {
        if (!ready) return@LaunchedEffect

        if (reduceMotion) {
            arrivalScale.snapTo(1f)
            travelProgress.snapTo(1f)
            revealProgress.snapTo(1f)
            taglineAlpha.snapTo(1f)
            delay(300)
            onFinished()
            return@LaunchedEffect
        }

        // T = 0..600: arrival pulse — leaf "lands" into the new context.
        arrivalScale.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 600
                0.95f at 0 using motion.easing
                1.04f at 360 using motion.easing
                1.00f at 600
            },
        )

        // T = 600..1500: drawn leaf settles, breathing.
        delay(900)

        // T = 1500..3300: leaf travels + wordmark reveals in lock-step.
        launch {
            travelProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1800, easing = motion.easing),
            )
        }
        launch {
            revealProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1800, easing = motion.easing),
            )
        }

        // T = 3300..3700: stillness.
        delay(1800 + 400)

        // T = 3700..4050: tagline fades in.
        launch {
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = motion.easing),
            )
        }

        // T = 4050..5000: final hold.
        delay(950)
        onFinished()
    }

    val breath by rememberInfiniteTransition(label = "leaf-breath").animateFloat(
        initialValue = 1.00f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = motion.easing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "leaf-breath-scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Intentionally NO windowInsetsPadding here: the Android splash
            // icon is positioned at the true geometric centre of the window
            // (under the navigation bar). For the in-app hero leaf to paint
            // at exactly the same screen position as Android's splash icon —
            // so the handoff stacks rather than staggers — the in-app box
            // must be the full window too. The nav bar paints over the
            // bottom; the leaf is at centre and never gets occluded.
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                val sz = coords.size
                screenCenter = Offset(
                    pos.x + sz.width / 2f,
                    pos.y + sz.height / 2f,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(LeafRestSizeDp)
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInRoot()
                        val sz = coords.size
                        leafRestCenter = Offset(
                            pos.x + sz.width / 2f,
                            pos.y + sz.height / 2f,
                        )
                    }
                    .graphicsLayer {
                        if (!ready) {
                            alpha = 0f
                            return@graphicsLayer
                        }

                        val totalDx = screenCenter.x - leafRestCenter.x
                        val totalDy = screenCenter.y - leafRestCenter.y

                        val t = travelProgress.value
                        val invT = 1f - t
                        val midDx = totalDx * 0.55f
                        val midDy = totalDy * 0.85f
                        val curveDx =
                            invT * invT * totalDx + 2f * invT * t * midDx + t * t * 0f
                        val curveDy =
                            invT * invT * totalDy + 2f * invT * t * midDy + t * t * 0f

                        val scaleAtMid = (HeroScale + 1f) * 0.5f * 1.02f
                        val travelScale = if (t <= 0.4f) {
                            val k = t / 0.4f
                            HeroScale + (scaleAtMid - HeroScale) * k
                        } else {
                            val k = (t - 0.4f) / 0.6f
                            scaleAtMid + (1f - scaleAtMid) * k
                        }

                        translationX = curveDx
                        translationY = curveDy
                        // Composed transform: arrival pulse + travel + breath.
                        // arrivalScale runs first and lands at 1.0; breath
                        // takes over from there. They multiply cleanly because
                        // the breath is an oscillation around 1.0.
                        scaleX = travelScale * breath * arrivalScale.value
                        scaleY = travelScale * breath * arrivalScale.value
                        alpha = 1f
                    }
                    .semantics { contentDescription = "Seqaya leaf mark" },
            ) {
                LeafGlyph(
                    color = Seqaya.colors.accentGreen,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.width(GapDp))

            Wordmark(reveal = revealProgress.value)
        }

        Text(
            text = stringResource(R.string.splash_tagline),
            style = Seqaya.type.labelCaps,
            color = Seqaya.colors.textTertiary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .alpha(taglineAlpha.value),
        )
    }
}

/**
 * Wordmark whose drawing is clipped left-to-right by `reveal`. Layout
 * width is constant so the Row stays centred throughout.
 */
@Composable
private fun Wordmark(reveal: Float) {
    Text(
        text = stringResource(R.string.splash_wordmark),
        style = Seqaya.type.displayL.copy(
            fontSize = WordmarkSizeSp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
        ),
        color = Seqaya.colors.textPrimary,
        modifier = Modifier.drawWithContent {
            val visibleWidth = size.width * reveal.coerceIn(0f, 1f)
            val pad = size.height * 0.2f
            clipRect(
                left = 0f,
                top = -pad,
                right = visibleWidth,
                bottom = size.height + pad,
            ) {
                this@drawWithContent.drawContent()
            }
        },
    )
}

private val LeafRestSizeDp: Dp = 38.dp
private val GapDp: Dp = 10.dp
private val WordmarkSizeSp = 48.sp
// Hero size matches Android 12+ splash icon rendering. The system splash
// places the icon's drawable inside a 192dp inner content area; the leaf's
// own bounds within its 108-viewBox occupy ~56 units (26..82), giving an
// effective rendered leaf width of ~100dp. Hero scale puts the in-app
// leaf at the same visible size so the splash → in-app handoff stacks,
// not staggers.
//   100dp / 38dp ≈ 2.63
private const val HeroScale = 100f / 38f

/**
 * Three-stroke hand-drawn leaf, line art only. Same paths as the launcher
 * foreground and the splash icon so the rendering is identical across
 * launcher → system splash → in-app at every scale.
 */
@Composable
private fun LeafGlyph(color: Color, modifier: Modifier) {
    val outline = remember { PathParser().parsePathString(LeafOutlinePath).toPath() }
    val midrib = remember { PathParser().parsePathString(LeafMidribPath).toPath() }
    val vein = remember { PathParser().parsePathString(LeafVeinPath).toPath() }
    Canvas(modifier = modifier) {
        val scaleFactor = size.minDimension / LeafViewBox
        val stroke = Stroke(
            width = LeafStrokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        scale(scale = scaleFactor, pivot = Offset.Zero) {
            drawPath(path = outline, color = color, style = stroke)
            drawPath(path = midrib, color = color, style = stroke)
            drawPath(path = vein, color = color, style = stroke)
        }
    }
}

private const val LeafViewBox = 40f
private const val LeafStrokeWidth = 1.25f
private const val LeafOutlinePath =
    "M6 32 C8 21 14 13 26 8 C25 21 20 30 6 32 Z"
private const val LeafMidribPath =
    "M6 32 C11 27 16 23 23 19"
private const val LeafVeinPath =
    "M12 28 C14 27 16 26.5 18 26.5"
