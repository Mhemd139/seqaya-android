package com.seqaya.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class SeqayaShapes(
    val button: Shape,
    val card: Shape,
    val sheet: Shape,
    val chip: Shape,
    val innerTile: Shape,
    val photoFrame: Shape,
    val field: Shape,
    val section: Shape,
)

val SeqayaShape = SeqayaShapes(
    button = RoundedCornerShape(14.dp),
    card = RoundedCornerShape(20.dp),
    sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    chip = RoundedCornerShape(100.dp),
    innerTile = RoundedCornerShape(16.dp),
    photoFrame = RoundedCornerShape(20.dp),
    field = RoundedCornerShape(10.dp),
    section = RoundedCornerShape(24.dp),
)

val LocalSeqayaShapes = staticCompositionLocalOf { SeqayaShape }
