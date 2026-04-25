package com.seqaya.app.ui.provisioning

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.seqaya.app.ui.theme.Seqaya

@Composable
fun AddDeviceScreen(
    onFinish: (serial: String, nickname: String) -> Unit,
    onCancel: () -> Unit,
    viewModel: AddDeviceWizardViewModel = hiltViewModel(),
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()

    // Real launcher for ACCESS_FINE_LOCATION — the VM emits RequestLocationPermission when
    // it enters the Wi-Fi step and can't prefill the SSID because the permission is denied.
    // On grant, the VM re-runs prefill via onLocationPermissionGranted.
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.onLocationPermissionGranted()
    }

    // The OS Location toggle is changed via Quick Settings (notification panel),
    // not from inside the app. We auto-detect when the user comes back foreground
    // by observing ON_RESUME and re-checking the system flag.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshLocationServices()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is AddDeviceEvent.Finished -> onFinish(ev.deviceSerial, ev.nickname)
                AddDeviceEvent.Cancelled -> onCancel()
                AddDeviceEvent.RequestLocationPermission ->
                    locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    BackHandler {
        viewModel.cancel()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Seqaya.colors.bgCream)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        StepHeader(
            current = state.step,
            onCancel = viewModel::cancel,
        )

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                (fadeIn(tween(260))) togetherWith fadeOut(tween(180))
            },
            modifier = Modifier.fillMaxSize(),
            label = "wizardStep",
        ) { step ->
            when (step) {
                Step.PickPlant -> PickPlantStep(
                    plants = state.plants,
                    loading = state.plantsLoading,
                    selected = state.selectedPlant,
                    onSelect = viewModel::selectPlant,
                    onNext = viewModel::advanceToWifi,
                )
                Step.Wifi -> WifiStep(
                    ssid = state.ssid,
                    password = state.password,
                    ssidPrefilled = state.ssidPrefilled,
                    locationServicesOff = state.locationServicesOff,
                    pickerOpen = state.pickerOpen,
                    pickerNetworks = state.pickerNetworks,
                    onSsidChange = viewModel::setSsid,
                    onPasswordChange = viewModel::setPassword,
                    onOpenPicker = viewModel::openNetworkPicker,
                    onClosePicker = viewModel::closeNetworkPicker,
                    onPickNetwork = viewModel::selectNetworkFromPicker,
                    onNext = viewModel::advanceToTap,
                    error = state.error,
                )
                Step.Tap, Step.Success -> TapDeviceStep(
                    state = state,
                    onRetry = viewModel::retryTap,
                    onCancel = viewModel::cancel,
                    onDismissError = viewModel::dismissError,
                )
            }
        }
    }
}

@Composable
private fun StepHeader(
    current: Step,
    onCancel: () -> Unit,
) {
    val total = 3
    val active = when (current) {
        Step.PickPlant -> 1
        Step.Wifi -> 2
        Step.Tap, Step.Success -> 3
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(total) { i ->
                val isActive = i + 1 <= active
                Box(
                    modifier = Modifier
                        .width(if (i + 1 == active) 22.dp else 8.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) Seqaya.colors.textPrimary
                            else Seqaya.colors.borderStrong,
                        ),
                )
                if (i < total - 1) Spacer(modifier = Modifier.width(6.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Step $active of $total",
            style = Seqaya.type.labelCaps,
            color = Seqaya.colors.textSecondary,
        )
        Box(modifier = Modifier.weight(1f))
        Text(
            text = "Cancel",
            style = Seqaya.type.caption,
            color = Seqaya.colors.textSecondary,
            modifier = Modifier
                .clickable(onClick = onCancel)
                .semantics { contentDescription = "Cancel add device" }
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

private fun tween(millis: Int): androidx.compose.animation.core.TweenSpec<Float> =
    androidx.compose.animation.core.tween(durationMillis = millis)
