package com.seqaya.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class SeqayaMotion(
    val easing: CubicBezierEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f),
    val microMs: Int = 180,
    val stateMs: Int = 260,
    val screenMs: Int = 420,
    val leafBreatheMs: Int = 2200,
    val nfcWaveMs: Int = 2400,
    val orbitLoaderMs: Int = 8000,
)

val SeqayaMove = SeqayaMotion()

val LocalSeqayaMotion = staticCompositionLocalOf { SeqayaMove }
