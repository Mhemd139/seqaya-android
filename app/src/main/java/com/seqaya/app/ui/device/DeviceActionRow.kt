package com.seqaya.app.ui.device

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seqaya.app.R
import com.seqaya.app.ui.components.SensorIllustration
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun DeviceActionRow(
    holdActive: Boolean,
    enabled: Boolean,
    onHoldToggle: () -> Unit,
    onDryMap: () -> Unit,
    onWetMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .heightIn(min = 96.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Seqaya.colors.border, RoundedCornerShape(14.dp)),
    ) {
        ActionCell(
            icon = { HoldGlyph(playing = holdActive) },
            label = stringResource(
                if (holdActive) R.string.device_action_resume else R.string.device_action_pause,
            ),
            description = stringResource(
                if (holdActive) R.string.device_action_resume_description
                else R.string.device_action_pause_description,
            ),
            enabled = enabled,
            onClick = onHoldToggle,
            modifier = Modifier.weight(1f),
        )
        CellDivider()
        ActionCell(
            icon = { SensorIllustration(size = 30.dp, withDroplets = false) },
            label = stringResource(R.string.device_action_calibrate_dry),
            description = stringResource(R.string.device_action_calibrate_dry_description),
            enabled = enabled,
            onClick = onDryMap,
            modifier = Modifier.weight(1f),
        )
        CellDivider()
        ActionCell(
            icon = { SensorIllustration(size = 30.dp, withDroplets = true) },
            label = stringResource(R.string.device_action_calibrate_wet),
            description = stringResource(R.string.device_action_calibrate_wet_description),
            enabled = enabled,
            onClick = onWetMap,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActionCell(
    icon: @Composable () -> Unit,
    label: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cellAlpha = if (enabled) 1f else 0.4f
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .alpha(cellAlpha)
            .semantics {
                role = Role.Button
                contentDescription = description
            }
            .padding(vertical = 14.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = Seqaya.type.body.copy(
                color = Seqaya.colors.textPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun CellDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(Seqaya.colors.border),
    )
}

@Composable
private fun HoldGlyph(playing: Boolean) {
    val ink = Seqaya.colors.textPrimary
    Canvas(modifier = Modifier.size(30.dp)) {
        val w = size.width
        val h = size.height
        val stroke = w / 16f
        if (playing) {
            val path = Path().apply {
                moveTo(w * 0.32f, h * 0.22f)
                lineTo(w * 0.78f, h * 0.50f)
                lineTo(w * 0.32f, h * 0.78f)
                close()
            }
            drawPath(path = path, color = ink.copy(alpha = 0.18f))
            drawPath(path = path, color = ink, style = Stroke(width = stroke))
        } else {
            val barW = w * 0.10f
            val topY = h * 0.24f
            val botY = h * 0.76f
            val leftX = w * 0.38f
            val rightX = w * 0.62f
            drawLine(ink, Offset(leftX, topY), Offset(leftX, botY), strokeWidth = barW)
            drawLine(ink, Offset(rightX, topY), Offset(rightX, botY), strokeWidth = barW)
        }
    }
}
