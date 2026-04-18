package com.seqaya.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun TabPlaceholder(
    eyebrow: String,
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Seqaya.space.pageHorizontal),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = eyebrow,
            style = Seqaya.type.labelCaps.copy(color = Seqaya.colors.textSecondary),
        )
        Spacer(Modifier.height(Seqaya.space.sm))
        Text(
            text = title,
            style = Seqaya.type.hXL.copy(color = Seqaya.colors.textPrimary),
        )
        Spacer(Modifier.height(Seqaya.space.md))
        Text(
            text = body,
            style = Seqaya.type.body.copy(color = Seqaya.colors.textSecondary),
        )
    }
}
