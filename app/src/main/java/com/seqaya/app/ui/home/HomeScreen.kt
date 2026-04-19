package com.seqaya.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seqaya.app.R
import com.seqaya.app.ui.home.components.AttentionBanner
import com.seqaya.app.ui.home.components.DeviceCard
import com.seqaya.app.ui.home.components.OfflineBanner
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val showComingSoon: () -> Unit = {
        Toast.makeText(context, context.getString(R.string.coming_in_next_update), Toast.LENGTH_SHORT).show()
    }

    Column(modifier = Modifier.fillMaxSize().background(Seqaya.colors.bgCream)) {
        if (state.isOffline) OfflineBanner()

        HomeTopBar(
            showPlus = !state.isEmpty,
            onAddClick = showComingSoon,
        )

        when {
            state.isLoading -> Spacer(Modifier.fillMaxSize())
            state.isEmpty -> HomeEmpty(onAdd = showComingSoon, onLibrary = showComingSoon)
            else -> HomePopulated(state = state, onReviewThirsty = showComingSoon)
        }
    }
}

@Composable
private fun HomeTopBar(showPlus: Boolean, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = Seqaya.type.wordmark.copy(color = Seqaya.colors.textPrimary),
            modifier = Modifier.weight(1f),
        )
        if (showPlus) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAddClick)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Add a device"
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    style = Seqaya.type.hM.copy(
                        color = Seqaya.colors.textPrimary,
                        fontWeight = FontWeight.Normal,
                    ),
                )
            }
            Spacer(Modifier.size(6.dp))
        }
        Avatar(letter = "M")
    }
}

@Composable
private fun Avatar(letter: String) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Seqaya.colors.accentGreen)
            .semantics { contentDescription = "Your profile" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            style = Seqaya.type.hM.copy(color = Seqaya.colors.bgCream, fontSize = 13.sp),
        )
    }
}

@Composable
private fun HomeEmpty(onAdd: () -> Unit, onLibrary: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.home_empty_eyebrow),
            style = Seqaya.type.labelCaps.copy(color = Seqaya.colors.textSecondary),
        )
        Spacer(Modifier.height(10.dp))
        val title = Seqaya.type.hXL.copy(color = Seqaya.colors.textPrimary, fontSize = 36.sp)
        Text(stringResource(R.string.home_empty_title_line_1), style = title)
        Text(stringResource(R.string.home_empty_title_line_2), style = title)
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.home_empty_lede),
            style = Seqaya.type.body.copy(color = Seqaya.colors.textSecondary, fontSize = 14.sp),
        )
        Spacer(Modifier.height(22.dp))
        PrimaryButton(label = stringResource(R.string.home_empty_primary), onClick = onAdd)
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.home_empty_secondary),
            style = Seqaya.type.caption.copy(color = Seqaya.colors.accentBrown, fontSize = 13.sp),
            modifier = Modifier
                .clickable(onClick = onLibrary)
                .padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun HomePopulated(
    state: HomeUiState,
    onReviewThirsty: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        state.thirstyDevice?.let { thirsty ->
            item {
                AttentionBanner(
                    thirstyDeviceName = thirsty.displayName,
                    onReview = onReviewThirsty,
                )
            }
        }
        items(items = state.devices, key = { it.device.id }) { device ->
            DeviceCard(item = device)
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .clip(Seqaya.shapes.button)
            .background(Seqaya.colors.accentBrown)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = Seqaya.type.body.copy(
                color = Seqaya.colors.bgCream,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}
