package com.seqaya.app.ui.provisioning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seqaya.app.nfc.ProvisioningSession
import com.seqaya.app.ui.components.DeviceIllustration
import com.seqaya.app.ui.components.LeafSpinner
import com.seqaya.app.ui.components.NfcWaves
import com.seqaya.app.ui.components.OpeningLeafIllustration
import com.seqaya.app.ui.components.SoundService
import com.seqaya.app.ui.components.SuccessCelebration
import com.seqaya.app.ui.theme.Seqaya
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SoundEntryPoint {
    fun soundService(): SoundService
}

@Composable
fun TapDeviceStep(
    state: AddDeviceUiState,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onDismissError: () -> Unit,
) {
    val context = LocalContext.current
    val soundService = remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, SoundEntryPoint::class.java)
            .soundService()
    }

    val status = state.sessionStatus
    val showWaves = status is ProvisioningSession.Status.ReadyToTap
    val showSpinner = status is ProvisioningSession.Status.Transferring ||
            status is ProvisioningSession.Status.Transferred
    val showSuccess = state.step == Step.Success ||
            status is ProvisioningSession.Status.Success
    val failure = status as? ProvisioningSession.Status.Failed

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (showSuccess) "" else "Tap your phone\nto the device.",
            style = Seqaya.type.hXL,
            color = Seqaya.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            when {
                showSuccess -> {
                    val nickname = state.createdDeviceNickname ?: "Your plant"
                    SuccessCelebration(
                        headline = "$nickname is awake.",
                        illustration = { OpeningLeafIllustration(size = 140.dp) },
                        onChime = { soundService.play(SoundService.Pitch.C5) },
                    )
                }
                showSpinner -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LeafSpinner(size = 72.dp)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = when (status) {
                            is ProvisioningSession.Status.Transferred -> "Syncing…"
                            else -> "Sending to your device…"
                        },
                        style = Seqaya.type.body,
                        color = Seqaya.colors.textSecondary,
                    )
                }
                failure != null -> FailureBlock(
                    reason = failure.reason,
                    onRetry = onRetry,
                    onCancel = onCancel,
                    onDismiss = onDismissError,
                )
                else -> Box(contentAlignment = Alignment.Center) {
                    NfcWaves(size = 240.dp, active = showWaves)
                    DeviceIllustration(size = 140.dp)
                }
            }
        }

        AnimatedVisibility(visible = !showSuccess && failure == null) {
            Text(
                text = "Press the button on your Seqaya device, then hold\nyour phone against the Seqaya logo.",
                style = Seqaya.type.body.copy(fontSize = 14.sp),
                color = Seqaya.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        Spacer(Modifier.height(18.dp))
        if (!showSuccess) {
            Text(
                text = "Cancel setup",
                style = Seqaya.type.caption,
                color = Seqaya.colors.textSecondary,
                modifier = Modifier
                    .clickable(onClick = onCancel)
                    .semantics { contentDescription = "Cancel add device" }
                    .padding(vertical = 12.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
    }

    // Side-effect not needed here — SuccessCelebration fires its own chime via onChime.
}

@Composable
private fun FailureBlock(
    reason: ProvisioningSession.FailureReason,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    val (title, body) = when (reason) {
        ProvisioningSession.FailureReason.Timeout ->
            "We didn't hear from your device." to
                "Make sure it's powered on and press the button, then tap again."
        ProvisioningSession.FailureReason.LinkLost ->
            "Connection dropped." to "Hold your phone steady against the logo and retry."
        ProvisioningSession.FailureReason.TransferIncomplete ->
            "Transfer didn't finish." to "Move phone closer to the device and retry."
        ProvisioningSession.FailureReason.NfcDisabled ->
            "NFC is off." to "Open Settings and turn NFC on, then try again."
        ProvisioningSession.FailureReason.NfcNotAvailable ->
            "This phone doesn't support NFC." to "Phase 5 pairing needs an NFC-capable phone."
        ProvisioningSession.FailureReason.RemoteNack ->
            "Device rejected the command." to "Retry in a moment."
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Seqaya.colors.accentBrownSoft)
            .border(1.dp, Seqaya.colors.accentBrown, RoundedCornerShape(14.dp))
            .padding(20.dp),
    ) {
        Text(text = title, style = Seqaya.type.hM, color = Seqaya.colors.textPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            text = body,
            style = Seqaya.type.body.copy(fontSize = 14.sp),
            color = Seqaya.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Try again",
            style = Seqaya.type.caption,
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Seqaya.colors.accentGreen)
                .clickable {
                    onDismiss()
                    onRetry()
                }
                .padding(horizontal = 22.dp, vertical = 12.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Cancel setup",
            style = Seqaya.type.caption,
            color = Seqaya.colors.textSecondary,
            modifier = Modifier.clickable(onClick = onCancel).padding(vertical = 8.dp),
        )
    }
}

