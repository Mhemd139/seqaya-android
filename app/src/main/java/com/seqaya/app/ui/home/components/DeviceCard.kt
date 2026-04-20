package com.seqaya.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seqaya.app.R
import com.seqaya.app.domain.model.DeviceWithReading
import com.seqaya.app.ui.home.relativeTime
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun DeviceCard(
    item: DeviceWithReading,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = Seqaya.colors
    val typography = Seqaya.type
    val latest = item.latest

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(Seqaya.shapes.card)
            .background(colors.bgCreamLightest)
            .border(1.dp, colors.border, Seqaya.shapes.card)
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.displayName,
                style = typography.hM.copy(color = colors.textPrimary),
                modifier = Modifier.weight(1f),
            )
            StatusDot(thirsty = item.needsAttention)
        }

        item.device.plantScientificName?.let { latin ->
            Text(
                text = latin,
                style = typography.italic.copy(
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                ),
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Spacer(Modifier.height(18.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            if (latest != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = latest.soilMoisturePercent.toString(),
                        style = typography.displayL.copy(color = colors.textPrimary),
                    )
                    Text(
                        text = "%",
                        style = typography.displayL.copy(color = colors.textTertiary, fontSize = 22.sp),
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                    )
                }
            } else {
                Text(
                    text = "—",
                    style = typography.displayL.copy(color = colors.textTertiary),
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.home_card_last_24h),
                style = typography.labelCaps.copy(color = colors.textTertiary),
            )
        }

        Spacer(Modifier.height(4.dp))

        Sparkline(
            moisturePoints = item.recentMoisture,
            wateringEventIndices = emptyList(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
        )

        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    R.string.home_card_last_watered,
                    item.lastWateredAt?.let(::relativeTime) ?: "—",
                ),
                style = typography.caption.copy(color = colors.textSecondary, fontSize = 12.sp),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(
                    R.string.home_card_seen,
                    latest?.recordedAt?.let(::relativeTime) ?: "—",
                ),
                style = typography.caption.copy(color = colors.textSecondary, fontSize = 12.sp),
            )
        }
    }
}

@Composable
private fun StatusDot(thirsty: Boolean) {
    val color = if (thirsty) Seqaya.colors.accentBrown else Seqaya.colors.accentGreen
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(color),
    )
}
