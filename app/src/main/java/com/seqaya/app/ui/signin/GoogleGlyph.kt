package com.seqaya.app.ui.signin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

private val Blue = Color(0xFF4285F4)
private val Green = Color(0xFF34A853)
private val Yellow = Color(0xFFFBBC05)
private val Red = Color(0xFFEA4335)

@Composable
fun GoogleGlyph(modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 18f
        drawPath(
            path = Path().apply {
                moveTo(17.64f * s, 9.2f * s)
                cubicTo(17.64f * s, 8.56f * s, 17.58f * s, 7.95f * s, 17.47f * s, 7.36f * s)
                lineTo(9f * s, 7.36f * s); lineTo(9f * s, 10.84f * s); lineTo(13.84f * s, 10.84f * s)
                cubicTo(13.63f * s, 11.97f * s, 13f * s, 12.93f * s, 12.04f * s, 13.56f * s)
                lineTo(12.04f * s, 15.82f * s); lineTo(14.96f * s, 15.82f * s)
                cubicTo(16.66f * s, 14.25f * s, 17.64f * s, 11.94f * s, 17.64f * s, 9.2f * s); close()
            },
            color = Blue,
        )
        drawPath(
            path = Path().apply {
                moveTo(9f * s, 18f * s)
                cubicTo(11.43f * s, 18f * s, 13.47f * s, 17.2f * s, 14.96f * s, 15.82f * s)
                lineTo(12.04f * s, 13.56f * s)
                cubicTo(11.24f * s, 14.1f * s, 10.2f * s, 14.42f * s, 9f * s, 14.42f * s)
                cubicTo(6.66f * s, 14.42f * s, 4.67f * s, 12.84f * s, 3.96f * s, 10.71f * s)
                lineTo(0.9f * s, 10.71f * s); lineTo(0.9f * s, 13.04f * s)
                cubicTo(2.38f * s, 16.02f * s, 5.48f * s, 18f * s, 9f * s, 18f * s); close()
            },
            color = Green,
        )
        drawPath(
            path = Path().apply {
                moveTo(3.96f * s, 10.71f * s)
                cubicTo(3.78f * s, 10.17f * s, 3.68f * s, 9.59f * s, 3.68f * s, 9f * s)
                cubicTo(3.68f * s, 8.41f * s, 3.78f * s, 7.83f * s, 3.96f * s, 7.29f * s)
                lineTo(3.96f * s, 4.96f * s); lineTo(0.9f * s, 4.96f * s)
                cubicTo(0.33f * s, 6.17f * s, 0f * s, 7.55f * s, 0f * s, 9f * s)
                cubicTo(0f * s, 10.45f * s, 0.33f * s, 11.83f * s, 0.9f * s, 13.04f * s)
                lineTo(3.96f * s, 10.71f * s); close()
            },
            color = Yellow,
        )
        drawPath(
            path = Path().apply {
                moveTo(9f * s, 3.58f * s)
                cubicTo(10.32f * s, 3.58f * s, 11.5f * s, 4.03f * s, 12.44f * s, 4.93f * s)
                lineTo(15.02f * s, 2.34f * s)
                cubicTo(13.46f * s, 0.89f * s, 11.43f * s, 0f * s, 9f * s, 0f * s)
                cubicTo(5.48f * s, 0f * s, 2.38f * s, 1.98f * s, 0.9f * s, 4.96f * s)
                lineTo(3.96f * s, 7.29f * s)
                cubicTo(4.67f * s, 5.16f * s, 6.66f * s, 3.58f * s, 9f * s, 3.58f * s); close()
            },
            color = Red,
        )
    }
}
