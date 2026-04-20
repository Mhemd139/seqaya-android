package com.seqaya.app.ui.device

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seqaya.app.ui.theme.Seqaya

enum class ChartRange(val label: String, val durationMs: Long) {
    HOURS_24("24h", 24L * 60 * 60 * 1000),
    DAYS_7("7d", 7L * 24 * 60 * 60 * 1000),
    DAYS_30("30d", 30L * 24 * 60 * 60 * 1000),
}

@Composable
fun RangeSegmentedControl(
    selected: ChartRange,
    onSelect: (ChartRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Seqaya.colors
    val shape = RoundedCornerShape(100.dp)
    val textStyle = Seqaya.type.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.bgCreamLightest)
            .border(1.dp, colors.border, shape)
            .padding(4.dp),
    ) {
        ChartRange.entries.forEach { range ->
            val isSelected = range == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(shape)
                    .background(if (isSelected) colors.bgCream else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) colors.border else Color.Transparent,
                        shape = shape,
                    )
                    .clickable { onSelect(range) }
                    .semantics {
                        role = Role.Button
                        this.selected = isSelected
                        contentDescription = "Show last ${range.label}"
                    }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = range.label,
                    style = textStyle,
                    color = if (isSelected) colors.textPrimary else colors.textSecondary,
                )
            }
        }
    }
}
