package com.seqaya.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SeqayaColors(
    val bgCream: Color,
    val bgCreamLight: Color,
    val bgCreamLightest: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentGreen: Color,
    val accentGreenSoft: Color,
    val accentGreenInk: Color,
    val accentBrown: Color,
    val accentBrownSoft: Color,
    val accentBrownInk: Color,
    val accentBlue: Color,
    val border: Color,
    val borderStrong: Color,
    val canvas: Color,
    val isDark: Boolean,
)

val LightColors = SeqayaColors(
    bgCream = Color(0xFFFAF9F5),
    bgCreamLight = Color(0xFFE8E6DC),
    bgCreamLightest = Color(0xFFF2F0E8),
    textPrimary = Color(0xFF141413),
    textSecondary = Color(0xFF75736B),
    textTertiary = Color(0xFFB0AEA5),
    accentGreen = Color(0xFF788C5D),
    accentGreenSoft = Color(0x1F788C5D),
    accentGreenInk = Color(0xFF556841),
    accentBrown = Color(0xFFD97757),
    accentBrownSoft = Color(0x1AD97757),
    accentBrownInk = Color(0xFFA85535),
    accentBlue = Color(0xFF6A9BCC),
    border = Color(0xFFE8E6DC),
    borderStrong = Color(0xFFD8D6CC),
    canvas = Color(0xFFEBE9E0),
    isDark = false,
)

val DarkColors = SeqayaColors(
    bgCream = Color(0xFF1A1917),
    bgCreamLight = Color(0xFF25231F),
    bgCreamLightest = Color(0xFF1F1D1B),
    textPrimary = Color(0xFFFAF9F5),
    textSecondary = Color(0xFFB0AEA5),
    textTertiary = Color(0xFF75736B),
    accentGreen = Color(0xFF9EB37F),
    accentGreenSoft = Color(0x2E9EB37F),
    accentGreenInk = Color(0xFFB8C99C),
    accentBrown = Color(0xFFE89878),
    accentBrownSoft = Color(0x29E89878),
    accentBrownInk = Color(0xFFF0B398),
    accentBlue = Color(0xFF8AB4DE),
    border = Color(0xFF2F2C28),
    borderStrong = Color(0xFF3F3C37),
    canvas = Color(0xFF0D0C0B),
    isDark = true,
)

val LocalSeqayaColors = staticCompositionLocalOf { LightColors }
