package com.seqaya.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.seqaya.app.R

private fun fraunces(opsz: Int, weight: Int = 500) = FontFamily(
    Font(
        R.font.fraunces,
        weight = FontWeight(weight),
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(weight),
            FontVariation.Setting("opsz", opsz.toFloat()),
        ),
    ),
    Font(
        R.font.fraunces_italic,
        weight = FontWeight(weight),
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(weight),
            FontVariation.Setting("opsz", opsz.toFloat()),
        ),
    ),
)

private fun inter(weight: Int) = FontFamily(
    Font(
        R.font.inter,
        weight = FontWeight(weight),
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    ),
    Font(
        R.font.inter_italic,
        weight = FontWeight(weight),
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    ),
)

private val MonoFamily = FontFamily(
    Font(
        R.font.jetbrains_mono,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
)

@Immutable
data class SeqayaTypography(
    val displayXL: TextStyle,
    val displayL: TextStyle,
    val hXL: TextStyle,
    val hL: TextStyle,
    val hM: TextStyle,
    val body: TextStyle,
    val bodySecondary: TextStyle,
    val caption: TextStyle,
    val fine: TextStyle,
    val labelCaps: TextStyle,
    val wordmark: TextStyle,
    val italic: TextStyle,
)

val SeqayaType = SeqayaTypography(
    displayXL = TextStyle(
        fontFamily = fraunces(opsz = 144, weight = 500),
        fontSize = 72.sp,
        lineHeight = 68.sp,
        letterSpacing = (-0.03).em,
    ),
    displayL = TextStyle(
        fontFamily = fraunces(opsz = 72, weight = 500),
        fontSize = 48.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.02).em,
    ),
    hXL = TextStyle(
        fontFamily = fraunces(opsz = 72, weight = 500),
        fontSize = 34.sp,
        lineHeight = 37.sp,
        letterSpacing = (-0.02).em,
    ),
    hL = TextStyle(
        fontFamily = fraunces(opsz = 36, weight = 500),
        fontSize = 26.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.015).em,
    ),
    hM = TextStyle(
        fontFamily = fraunces(opsz = 24, weight = 500),
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).em,
    ),
    body = TextStyle(
        fontFamily = inter(400),
        fontSize = 16.sp,
        lineHeight = 25.sp,
    ),
    bodySecondary = TextStyle(
        fontFamily = inter(400),
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    caption = TextStyle(
        fontFamily = inter(500),
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
    fine = TextStyle(
        fontFamily = inter(400),
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    labelCaps = TextStyle(
        fontFamily = MonoFamily,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.16.em,
    ),
    wordmark = TextStyle(
        fontFamily = fraunces(opsz = 14, weight = 500),
        fontSize = 18.sp,
        letterSpacing = (-0.01).em,
    ),
    italic = TextStyle(
        fontFamily = fraunces(opsz = 14, weight = 400),
        fontStyle = FontStyle.Italic,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
)

val LocalSeqayaTypography = staticCompositionLocalOf { SeqayaType }
