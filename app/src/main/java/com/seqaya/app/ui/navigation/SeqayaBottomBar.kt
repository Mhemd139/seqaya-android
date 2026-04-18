package com.seqaya.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun SeqayaBottomBar(
    current: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val border = Seqaya.colors.border
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Seqaya.colors.bgCream)
            .drawBehind {
                drawLine(
                    color = border,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1f,
                )
            }
            .padding(top = 8.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TopLevelDestination.entries.forEach { dest ->
            BottomBarItem(
                destination = dest,
                selected = dest == current,
                onClick = { onSelect(dest) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = if (selected) Seqaya.colors.textPrimary else Seqaya.colors.textTertiary
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .height(56.dp)
            .semantics { role = Role.Tab }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TabGlyph(destination = destination, selected = selected, color = content)
        Text(
            text = stringResource(destination.labelRes),
            style = Seqaya.type.labelCaps.copy(color = content, textAlign = TextAlign.Center),
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

@Composable
private fun TabGlyph(
    destination: TopLevelDestination,
    selected: Boolean,
    color: Color,
) {
    Box(modifier = Modifier.size(22.dp)) {
        when (destination) {
            TopLevelDestination.Home -> GlyphHome(filled = selected, tint = color)
            TopLevelDestination.Scan -> GlyphScan(filled = selected, tint = color)
            TopLevelDestination.Library -> GlyphBook(filled = selected, tint = color)
        }
    }
}
