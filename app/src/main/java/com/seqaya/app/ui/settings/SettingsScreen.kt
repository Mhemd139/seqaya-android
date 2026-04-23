package com.seqaya.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seqaya.app.BuildConfig
import com.seqaya.app.R
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Open delete dialog on entry-tap, close it once deletion finishes successfully
    // (the VM signs us out, which tears down this screen anyway — no explicit close).
    val deletionError = (state.deletion as? DeletionState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Seqaya.colors.bgCream),
    ) {
        SettingsTopBar(onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            deletionError?.let { message ->
                ErrorBanner(
                    message = "${stringResource(R.string.delete_failed_prefix)} $message",
                    onDismiss = viewModel::dismissDeletionError,
                )
                Spacer(Modifier.height(12.dp))
            }
            ProfileBlock(
                displayName = state.user?.displayName,
                email = state.user?.email,
                initial = state.user?.let { (it.displayName ?: it.email)?.firstOrNull()?.uppercaseChar() },
            )
            Spacer(Modifier.height(28.dp))
            SectionRow(
                label = stringResource(R.string.settings_sign_out),
                description = stringResource(R.string.settings_sign_out_description),
                onClick = viewModel::signOut,
                emphasis = RowEmphasis.Neutral,
            )
            SectionRow(
                label = stringResource(R.string.settings_delete_account),
                description = stringResource(R.string.settings_delete_account_description),
                onClick = { showDeleteDialog = true },
                emphasis = RowEmphasis.Destructive,
            )
            Spacer(Modifier.height(32.dp))
            AboutBlock()
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            isDeleting = state.deletion is DeletionState.Deleting,
            onDismiss = { showDeleteDialog = false },
            onConfirm = { viewModel.deleteAccount() },
        )
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    val backDescription = stringResource(R.string.settings_back_description)
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
            Text(text = "←", style = Seqaya.type.hM.copy(color = Seqaya.colors.textPrimary))
        }
        Text(
            text = stringResource(R.string.settings_title),
            style = Seqaya.type.hM.copy(color = Seqaya.colors.textPrimary),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun ProfileBlock(displayName: String?, email: String?, initial: Char?) {
    Text(
        text = stringResource(R.string.settings_profile_heading),
        style = Seqaya.type.labelCaps.copy(color = Seqaya.colors.textSecondary),
    )
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Seqaya.colors.accentGreen),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial?.toString() ?: "·",
                style = Seqaya.type.hM.copy(color = Seqaya.colors.bgCream, fontSize = 24.sp),
            )
        }
        Spacer(Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName ?: stringResource(R.string.device_info_none),
                style = Seqaya.type.hM.copy(color = Seqaya.colors.textPrimary, fontSize = 18.sp),
            )
            Text(
                text = email ?: "",
                style = Seqaya.type.body.copy(color = Seqaya.colors.textTertiary, fontSize = 13.sp),
            )
        }
    }
}

@Composable
private fun SectionRow(
    label: String,
    description: String,
    onClick: () -> Unit,
    emphasis: RowEmphasis,
) {
    val labelColor = when (emphasis) {
        RowEmphasis.Neutral -> Seqaya.colors.textPrimary
        RowEmphasis.Destructive -> Seqaya.colors.accentBrown
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Seqaya.colors.border))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .semantics {
                    role = Role.Button
                    contentDescription = description
                }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = Seqaya.type.body.copy(
                        color = labelColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Text(
                    text = description,
                    style = Seqaya.type.caption.copy(
                        color = Seqaya.colors.textTertiary,
                        fontSize = 12.sp,
                    ),
                )
            }
            Text(
                text = "›",
                style = Seqaya.type.body.copy(color = Seqaya.colors.textTertiary),
            )
        }
    }
}

@Composable
private fun AboutBlock() {
    val context = LocalContext.current
    val supportEmail = stringResource(R.string.settings_support_email)
    Text(
        text = stringResource(R.string.settings_about_heading),
        style = Seqaya.type.labelCaps.copy(color = Seqaya.colors.textSecondary),
    )
    Spacer(Modifier.height(12.dp))
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Seqaya.colors.border))
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(
                R.string.settings_version_label,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
            ),
            style = Seqaya.type.body.copy(color = Seqaya.colors.textSecondary, fontSize = 13.5.sp),
            modifier = Modifier.weight(1f),
        )
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Seqaya.colors.border))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$supportEmail"))
                runCatching { context.startActivity(intent) }
            }
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.settings_support_label),
            style = Seqaya.type.body.copy(color = Seqaya.colors.textSecondary, fontSize = 13.5.sp),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = supportEmail,
            style = Seqaya.type.body.copy(color = Seqaya.colors.accentBrown, fontSize = 13.5.sp),
        )
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    val dismissLabel = stringResource(R.string.error_banner_dismiss)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Seqaya.colors.accentBrownSoft)
            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = Seqaya.type.caption.copy(color = Seqaya.colors.accentBrownInk, fontSize = 12.5.sp),
            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
        )
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .clickable(onClick = onDismiss)
                .semantics {
                    role = Role.Button
                    contentDescription = dismissLabel
                }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = dismissLabel,
                style = Seqaya.type.labelCaps.copy(color = Seqaya.colors.accentBrownInk),
            )
        }
    }
}

private enum class RowEmphasis { Neutral, Destructive }
