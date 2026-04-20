package com.seqaya.app.ui.device

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seqaya.app.R
import com.seqaya.app.domain.model.DeviceWithReading
import com.seqaya.app.ui.home.relativeTime
import com.seqaya.app.ui.theme.Seqaya
import java.time.Instant

@Composable
fun DeviceDetailScreen(
    onBack: () -> Unit,
    viewModel: DeviceDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.shouldNavigateBack) {
        if (state.shouldNavigateBack) {
            Toast.makeText(context, context.getString(R.string.device_delete_toast), Toast.LENGTH_SHORT).show()
            viewModel.consumeNavigation()
            onBack()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Seqaya.colors.bgCream)) {
        DeviceTopBar(
            title = state.device?.nickname ?: state.device?.serial ?: "",
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            DeviceHero(
                scientific = state.device?.plantScientificName,
                percent = state.latestPercent,
                target = state.device?.targetMoisturePercent,
                thirsty = derivedThirsty(state.latestPercent, state.device?.targetMoisturePercent),
            )

            Spacer(Modifier.height(22.dp))

            RangeSegmentedControl(
                selected = state.range,
                onSelect = viewModel::setRange,
            )

            Spacer(Modifier.height(14.dp))

            MoistureChart(
                points = state.points,
                wateringEvents = state.wateringEvents,
                targetPercent = state.device?.targetMoisturePercent,
                modifier = Modifier.fillMaxWidth().height(110.dp),
            )

            Spacer(Modifier.height(26.dp))

            DeviceInfoBlock(
                serial = state.device?.serial,
                plantName = state.device?.plantCommonName,
                nickname = state.device?.nickname,
                target = state.device?.targetMoisturePercent,
                lastSeenRelative = state.points.lastOrNull()?.timestamp?.let(::relativeTime),
                onRenameClick = { showRenameDialog = true },
                onTargetClick = { showTargetDialog = true },
            )

            Spacer(Modifier.height(6.dp))

            DeleteDeviceRow(onClick = { showDeleteDialog = true })

            Spacer(Modifier.height(36.dp))
        }
    }

    if (showRenameDialog) {
        EditNicknameDialog(
            currentNickname = state.device?.nickname,
            onDismiss = { showRenameDialog = false },
            onSave = { newName ->
                viewModel.renameDevice(newName)
                showRenameDialog = false
                Toast.makeText(context, context.getString(R.string.device_rename_toast), Toast.LENGTH_SHORT).show()
            },
        )
    }
    if (showTargetDialog) {
        EditTargetDialog(
            currentTarget = state.device?.targetMoisturePercent,
            onDismiss = { showTargetDialog = false },
            onSave = { newTarget ->
                viewModel.updateTarget(newTarget)
                showTargetDialog = false
                Toast.makeText(context, context.getString(R.string.device_target_toast), Toast.LENGTH_SHORT).show()
            },
        )
    }
    if (showDeleteDialog) {
        DeleteConfirmDialog(
            nickname = state.device?.nickname ?: state.device?.serial ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteDevice()
            },
        )
    }
}

@Composable
private fun DeviceTopBar(title: String, onBack: () -> Unit) {
    val backDescription = stringResource(R.string.device_back_content_description)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack)
                .semantics {
                    role = Role.Button
                    contentDescription = backDescription
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "\u2190", style = Seqaya.type.hM.copy(color = Seqaya.colors.textPrimary))
        }
        Text(
            text = title,
            style = Seqaya.type.hM.copy(color = Seqaya.colors.textPrimary),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun DeviceHero(scientific: String?, percent: Int?, target: Int?, thirsty: Boolean) {
    scientific?.let {
        Text(
            text = it,
            style = Seqaya.type.italic.copy(
                color = Seqaya.colors.textSecondary,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
            ),
        )
    }
    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
        Text(
            text = percent?.toString() ?: "—",
            style = Seqaya.type.displayL.copy(color = Seqaya.colors.textPrimary),
        )
        if (percent != null) {
            Text(
                text = "%",
                style = Seqaya.type.displayL.copy(color = Seqaya.colors.textTertiary, fontSize = 22.sp),
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
            )
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 6.dp),
    ) {
        StateChip(thirsty = thirsty)
        target?.let {
            Spacer(Modifier.size(12.dp))
            Text(
                text = stringResource(R.string.device_target_label, it),
                style = Seqaya.type.fine.copy(color = Seqaya.colors.textTertiary),
            )
        }
    }
}

@Composable
private fun StateChip(thirsty: Boolean) {
    val color = if (thirsty) Seqaya.colors.accentBrown else Seqaya.colors.accentGreen
    val softBg = if (thirsty) Seqaya.colors.accentBrownSoft else Seqaya.colors.accentGreenSoft
    val ink = if (thirsty) Seqaya.colors.accentBrownInk else Seqaya.colors.accentGreenInk
    val label = stringResource(if (thirsty) R.string.device_state_thirsty else R.string.device_state_thriving)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(softBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.size(6.dp))
        Text(text = label, style = Seqaya.type.caption.copy(color = ink, fontSize = 12.sp))
    }
}

@Composable
private fun DeviceInfoBlock(
    serial: String?,
    plantName: String?,
    nickname: String?,
    target: Int?,
    lastSeenRelative: String?,
    onRenameClick: () -> Unit,
    onTargetClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.device_info_heading),
            style = Seqaya.type.labelCaps.copy(color = Seqaya.colors.textSecondary),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        InfoRow(label = stringResource(R.string.device_info_serial), value = serial ?: stringResource(R.string.device_info_none), topBorder = false)
        InfoRow(label = stringResource(R.string.device_info_plant), value = plantName ?: stringResource(R.string.device_info_none))
        InfoRow(
            label = stringResource(R.string.device_info_nickname),
            value = nickname ?: stringResource(R.string.device_info_none),
            onClick = onRenameClick,
        )
        InfoRow(
            label = stringResource(R.string.device_info_target),
            value = target?.let { "$it%" } ?: stringResource(R.string.device_info_none),
            onClick = onTargetClick,
        )
        InfoRow(
            label = stringResource(R.string.device_info_last_seen),
            value = lastSeenRelative ?: stringResource(R.string.device_info_none),
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    topBorder: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(vertical = 13.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        if (topBorder) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Seqaya.colors.border))
        }
        Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = Seqaya.type.body.copy(color = Seqaya.colors.textSecondary, fontSize = 13.5.sp),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value,
                style = Seqaya.type.body.copy(color = Seqaya.colors.textPrimary, fontSize = 13.5.sp),
            )
            if (onClick != null) {
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "\u203A",
                    style = Seqaya.type.body.copy(color = Seqaya.colors.textTertiary),
                )
            }
        }
    }
}

@Composable
private fun DeleteDeviceRow(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Seqaya.colors.border))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        Text(
            text = stringResource(R.string.device_delete),
            style = Seqaya.type.body.copy(
                color = Seqaya.colors.accentBrown,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun DeleteConfirmDialog(
    nickname: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.device_delete_confirm_title, nickname)) },
        text = { Text(stringResource(R.string.device_delete_confirm_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.device_delete_confirm_action),
                    color = Seqaya.colors.accentBrown,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private fun derivedThirsty(percent: Int?, target: Int?): Boolean =
    percent != null && target != null && percent <= target - 10
