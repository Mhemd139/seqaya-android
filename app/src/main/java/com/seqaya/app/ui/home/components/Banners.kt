package com.seqaya.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seqaya.app.R
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun AttentionBanner(
    thirstyDeviceName: String,
    onReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Seqaya.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.accentBrownSoft)
            .border(1.dp, Color(0x40D97757), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(colors.accentBrown),
        )
        Spacer(Modifier.size(10.dp))
        Row(modifier = Modifier.weight(1f)) {
            Text(
                text = thirstyDeviceName,
                style = Seqaya.type.caption.copy(
                    color = colors.accentBrownInk,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Text(
                text = " " + stringResource(R.string.home_attention_suffix),
                style = Seqaya.type.caption.copy(color = colors.accentBrownInk, fontSize = 12.5.sp),
            )
        }
        Text(
            text = stringResource(R.string.home_attention_review),
            style = Seqaya.type.caption.copy(
                color = colors.accentBrownInk,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            modifier = Modifier.clickable(onClick = onReview),
        )
    }
}

@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Seqaya.colors.accentBrown)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = stringResource(R.string.home_offline_banner),
            style = Seqaya.type.caption.copy(color = Seqaya.colors.bgCream, fontSize = 12.5.sp),
        )
    }
}
