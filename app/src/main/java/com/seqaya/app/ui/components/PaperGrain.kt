package com.seqaya.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.seqaya.app.R

@Composable
fun PaperGrain(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tile: ImageBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.paper_grain).asImageBitmap()
    }
    val brush = remember(tile) {
        ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
    }
    Box(
        modifier = modifier.drawBehind {
            drawRect(brush = brush, blendMode = BlendMode.Multiply)
        }
    )
}
