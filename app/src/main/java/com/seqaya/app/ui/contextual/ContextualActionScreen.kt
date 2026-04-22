package com.seqaya.app.ui.contextual

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seqaya.app.nfc.ProvisioningSession
import com.seqaya.app.ui.components.ClosedBudIllustration
import com.seqaya.app.ui.components.DeviceIllustration
import com.seqaya.app.ui.components.LeafCheckIllustration
import com.seqaya.app.ui.components.LeafSpinner
import com.seqaya.app.ui.components.NfcWaves
import com.seqaya.app.ui.components.OpeningLeafIllustration
import com.seqaya.app.ui.components.SensorIllustration
import com.seqaya.app.ui.components.SensorInGlassIllustration
import com.seqaya.app.ui.components.SoundService
import com.seqaya.app.ui.components.SuccessCelebration
import com.seqaya.app.ui.theme.Seqaya
import dagger.hilt.android.EntryPointAccessors

@Composable
fun ContextualActionScreen(
    onDismiss: () -> Unit,
    onChainToWetMapping: (serial: String) -> Unit,
    viewModel: ContextualActionViewModel = hiltViewModel(),
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sounds = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            com.seqaya.app.ui.provisioning.SoundEntryPoint::class.java,
        ).soundService()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                ContextualEvent.Dismissed -> onDismiss()
                is ContextualEvent.ChainWetMapping -> onChainToWetMapping(ev.serial)
            }
        }
    }

    BackHandler { viewModel.dismiss() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Seqaya.colors.bgCream)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        CancelBar(onCancel = viewModel::dismiss)
        Spacer(Modifier.height(8.dp))

        AnimatedContent(
            targetState = state.step,
            transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(180)) },
            label = "ctxStep",
            modifier = Modifier.fillMaxSize(),
        ) { step ->
            when (step) {
                ContextualStep.Intro -> IntroContent(state = state, onNext = viewModel::advanceFromIntro)
                ContextualStep.Prepare -> PrepareContent(state = state, onNext = viewModel::advanceFromPrepare, onSkip = viewModel::skipPrepareTimer)
                ContextualStep.Tap -> TapContent(state = state, onRetry = viewModel::retry, onCancel = viewModel::dismiss)
                ContextualStep.Success -> SuccessContent(
                    state = state,
                    onChime = {
                        val pitch = when (state.action) {
                            ContextualAction.DryMap -> SoundService.Pitch.E5
                            ContextualAction.WetMap -> SoundService.Pitch.G5
                            else -> SoundService.Pitch.C5
                        }
                        sounds.play(pitch)
                    },
                    onDismiss = viewModel::dismiss,
                    onChainWet = viewModel::chainToWetMapping,
                )
            }
        }
    }
}

@Composable
private fun CancelBar(onCancel: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f))
        Text(
            text = "Cancel",
            style = Seqaya.type.caption,
            color = Seqaya.colors.textSecondary,
            modifier = Modifier
                .clickable(onClick = onCancel)
                .semantics { contentDescription = "Cancel action" }
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun IntroContent(state: ContextualUiState, onNext: () -> Unit) {
    val (title, body, cta) = when (state.action) {
        ContextualAction.Locate -> Triple(
            "Is this ${state.deviceNickname}?",
            "Blink the LED on the device for 10 seconds so you can confirm which pot you're looking at.",
            "Blink to confirm",
        )
        ContextualAction.HoldToggle -> Triple(
            "Put ${state.deviceNickname} on hold?",
            "Pause automatic watering until you resume. The device stays connected and keeps logging moisture readings.",
            "Pause watering",
        )
        ContextualAction.DryMap -> Triple(
            "Teach ${state.deviceNickname} what dry feels like",
            "We'll measure your sensor when it's completely dry. Takes about two minutes. You'll do this once per device, and again only if you replace the sensor.",
            "Start dry mapping",
        )
        ContextualAction.WetMap -> Triple(
            "Now teach it what wet feels like",
            "We'll measure your sensor fully submerged in a glass of tap water. One more minute.",
            "Start wet mapping",
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(Modifier.height(20.dp))
        when (state.action) {
            ContextualAction.Locate -> DeviceIllustration(size = 150.dp, withHalo = true)
            ContextualAction.HoldToggle -> ClosedBudIllustration(size = 130.dp)
            ContextualAction.DryMap -> SensorIllustration(size = 150.dp)
            ContextualAction.WetMap -> SensorInGlassIllustration(size = 170.dp)
        }
        Spacer(Modifier.height(28.dp))
        Text(title, style = Seqaya.type.hXL, color = Seqaya.colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            body,
            style = Seqaya.type.body.copy(fontSize = 14.sp),
            color = Seqaya.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(text = cta, onClick = onNext)
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun PrepareContent(state: ContextualUiState, onNext: () -> Unit, onSkip: () -> Unit) {
    val (title, steps) = when (state.action) {
        ContextualAction.DryMap -> "Take the sensor out" to listOf(
            "Gently pull the probe from the soil.",
            "Wipe the metal rods with a dry cloth or paper towel.",
            "Let it air-dry for about 30 seconds until no moisture is visible.",
        )
        ContextualAction.WetMap -> "Submerge the sensor" to listOf(
            "Fill a glass with room-temperature tap water.",
            "Put the sensor in — metal rods fully submerged.",
            "Wait 10 seconds for the sensor to saturate.",
        )
        else -> "Get ready" to listOf("Hold the device and your phone near each other.")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(Modifier.height(16.dp))
        if (state.action == ContextualAction.DryMap) {
            SensorIllustration(size = 130.dp, withDroplets = true)
        } else {
            SensorInGlassIllustration(size = 160.dp, withRipples = true)
        }
        Spacer(Modifier.height(24.dp))
        Text(title, style = Seqaya.type.hXL, color = Seqaya.colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.86f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            steps.forEachIndexed { i, s ->
                Row {
                    Text("${i + 1}. ", style = Seqaya.type.body, color = Seqaya.colors.textSecondary)
                    Text(s, style = Seqaya.type.body.copy(fontSize = 14.sp), color = Seqaya.colors.textSecondary)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            if (state.prepareUnlocked) "Ready when you are."
            else "Waiting for sensor to settle… ${state.prepareRemainingSeconds}s",
            style = Seqaya.type.caption,
            color = Seqaya.colors.textTertiary,
            modifier = Modifier
                .clickable(enabled = !state.prepareUnlocked, onClick = onSkip)
                .padding(vertical = 4.dp),
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = if (state.action == ContextualAction.DryMap) "It's dry now" else "It's soaked now",
            onClick = onNext,
            enabled = state.prepareUnlocked,
        )
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun TapContent(state: ContextualUiState, onRetry: () -> Unit, onCancel: () -> Unit) {
    val status = state.sessionStatus
    val failure = status as? ProvisioningSession.Status.Failed
    val transferring = status is ProvisioningSession.Status.Transferring || status is ProvisioningSession.Status.Transferred

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "Tap your phone\nto the device.",
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
                failure != null -> FailureCard(failure.reason, onRetry, onCancel)
                transferring -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LeafSpinner(size = 72.dp)
                    Spacer(Modifier.height(14.dp))
                    Text("Talking to your device…", style = Seqaya.type.body, color = Seqaya.colors.textSecondary)
                }
                else -> Box(contentAlignment = Alignment.Center) {
                    NfcWaves(size = 240.dp)
                    DeviceIllustration(size = 140.dp)
                }
            }
        }
        Text(
            "Press the button on your Seqaya device, then hold\nyour phone against the Seqaya logo.",
            style = Seqaya.type.body.copy(fontSize = 14.sp),
            color = Seqaya.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun SuccessContent(
    state: ContextualUiState,
    onChime: () -> Unit,
    onDismiss: () -> Unit,
    onChainWet: () -> Unit,
) {
    val nickname = state.deviceNickname.ifBlank { "Your device" }
    val headline = when (state.action) {
        ContextualAction.Locate -> "$nickname is blinking."
        ContextualAction.HoldToggle -> "Paused."
        ContextualAction.DryMap -> "Dry baseline captured."
        ContextualAction.WetMap -> "Fully calibrated."
    }
    val illustration: @Composable () -> Unit = {
        when (state.action) {
            ContextualAction.Locate -> DeviceIllustration(size = 140.dp, withHalo = true)
            ContextualAction.HoldToggle -> ClosedBudIllustration(size = 120.dp)
            ContextualAction.DryMap -> LeafCheckIllustration(size = 120.dp)
            ContextualAction.WetMap -> LeafCheckIllustration(size = 120.dp, dew = true)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Spacer(Modifier.weight(1f))
        SuccessCelebration(
            headline = headline,
            illustration = illustration,
            onChime = onChime,
            onComplete = null, // user-driven dismiss via buttons below
        )
        Spacer(Modifier.weight(1f))
        if (state.action == ContextualAction.DryMap) {
            PrimaryButton(text = "Now teach it wet", onClick = onChainWet)
            Spacer(Modifier.height(8.dp))
            TextButton("Maybe later", onDismiss)
        } else {
            PrimaryButton(text = "Done", onClick = onDismiss)
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun FailureCard(
    reason: ProvisioningSession.FailureReason,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
) {
    val (title, body) = when (reason) {
        ProvisioningSession.FailureReason.Timeout ->
            "We didn't hear from your device." to "Press the button on the device and hold your phone to it again."
        ProvisioningSession.FailureReason.LinkLost ->
            "Connection dropped." to "Hold your phone steady and try again."
        ProvisioningSession.FailureReason.TransferIncomplete,
        ProvisioningSession.FailureReason.RemoteNack ->
            "Command didn't go through." to "Try once more — the device may have been busy."
        ProvisioningSession.FailureReason.NfcDisabled ->
            "NFC is off." to "Turn it on in Settings, then retry."
        ProvisioningSession.FailureReason.NfcNotAvailable ->
            "This phone doesn't support NFC." to "You need an NFC-capable phone."
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
        Text(title, style = Seqaya.type.hM, color = Seqaya.colors.textPrimary)
        Spacer(Modifier.height(6.dp))
        Text(body, style = Seqaya.type.body.copy(fontSize = 14.sp), color = Seqaya.colors.textSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        PrimaryButton("Try again", onRetry)
        Spacer(Modifier.height(6.dp))
        TextButton("Cancel", onCancel)
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Seqaya.colors.accentGreen,
            contentColor = Seqaya.colors.bgCream,
            disabledContainerColor = Seqaya.colors.borderStrong,
            disabledContentColor = Seqaya.colors.textTertiary,
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Text(text, style = Seqaya.type.body)
    }
}

@Composable
private fun TextButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = Seqaya.type.caption,
        color = Seqaya.colors.textSecondary,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
    )
}
