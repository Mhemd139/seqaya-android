package com.seqaya.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class SeqayaSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 10.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 14.dp,
    val xl: Dp = 16.dp,
    val xxl: Dp = 20.dp,
    val xxxl: Dp = 24.dp,
    val huge: Dp = 32.dp,
    val pageHorizontal: Dp = 20.dp,
    val pageTopSafe: Dp = 14.dp,
    val cardPadding: Dp = 20.dp,
    val cardGap: Dp = 14.dp,
    val sectionGap: Dp = 22.dp,
    val formFieldGap: Dp = 18.dp,
    val inlineIconGap: Dp = 10.dp,
    val buttonGapInline: Dp = 10.dp,
)

val SeqayaSpace = SeqayaSpacing()

val LocalSeqayaSpacing = staticCompositionLocalOf { SeqayaSpace }
