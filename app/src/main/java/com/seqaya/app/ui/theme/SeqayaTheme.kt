package com.seqaya.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun materialLight(c: SeqayaColors) = lightColorScheme(
    background = c.bgCream,
    surface = c.bgCream,
    surfaceVariant = c.bgCreamLightest,
    onBackground = c.textPrimary,
    onSurface = c.textPrimary,
    onSurfaceVariant = c.textSecondary,
    primary = c.accentGreen,
    onPrimary = c.bgCream,
    secondary = c.accentBrown,
    onSecondary = c.bgCream,
    error = c.accentBrown,
    onError = c.bgCream,
    outline = c.borderStrong,
    outlineVariant = c.border,
)

private fun materialDark(c: SeqayaColors) = darkColorScheme(
    background = c.bgCream,
    surface = c.bgCream,
    surfaceVariant = c.bgCreamLightest,
    onBackground = c.textPrimary,
    onSurface = c.textPrimary,
    onSurfaceVariant = c.textSecondary,
    primary = c.accentGreen,
    onPrimary = c.bgCream,
    secondary = c.accentBrown,
    onSecondary = c.bgCream,
    error = c.accentBrown,
    onError = c.bgCream,
    outline = c.borderStrong,
    outlineVariant = c.border,
)

private fun materialTypography(t: SeqayaTypography) = Typography(
    displayLarge = t.displayXL,
    displayMedium = t.displayL,
    headlineLarge = t.hXL,
    headlineMedium = t.hL,
    headlineSmall = t.hM,
    titleLarge = t.hM,
    bodyLarge = t.body,
    bodyMedium = t.bodySecondary,
    bodySmall = t.caption,
    labelMedium = t.caption,
    labelSmall = t.labelCaps,
)

object Seqaya {
    val colors: SeqayaColors
        @Composable get() = LocalSeqayaColors.current
    val type: SeqayaTypography
        @Composable get() = LocalSeqayaTypography.current
    val shapes: SeqayaShapes
        @Composable get() = LocalSeqayaShapes.current
    val space: SeqayaSpacing
        @Composable get() = LocalSeqayaSpacing.current
    val motion: SeqayaMotion
        @Composable get() = LocalSeqayaMotion.current
}

@Composable
fun SeqayaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val material = if (darkTheme) materialDark(colors) else materialLight(colors)
    CompositionLocalProvider(
        LocalSeqayaColors provides colors,
        LocalSeqayaTypography provides SeqayaType,
        LocalSeqayaShapes provides SeqayaShape,
        LocalSeqayaSpacing provides SeqayaSpace,
        LocalSeqayaMotion provides SeqayaMove,
    ) {
        MaterialTheme(
            colorScheme = material,
            typography = materialTypography(SeqayaType),
            content = content,
        )
    }
}
