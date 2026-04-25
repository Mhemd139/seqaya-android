package com.seqaya.app.ui.provisioning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seqaya.app.data.repository.DeviceRepository
import com.seqaya.app.data.repository.PlantRepository
import com.seqaya.app.domain.model.Plant
import com.seqaya.app.nfc.ApduProtocol
import com.seqaya.app.nfc.ProvisioningSession
import com.seqaya.app.wifi.CurrentWifiProvider
import com.seqaya.app.wifi.WifiNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddDeviceUiState(
    val step: Step = Step.PickPlant,
    val plants: List<Plant> = emptyList(),
    val plantsLoading: Boolean = true,
    val selectedPlant: Plant? = null,
    val ssid: String = "",
    val password: String = "",
    val ssidPrefilled: Boolean = false,
    val locationServicesOff: Boolean = false,
    val pickerOpen: Boolean = false,
    val pickerNetworks: List<WifiNetwork> = emptyList(),
    val sessionStatus: ProvisioningSession.Status = ProvisioningSession.Status.Idle,
    val serial: String? = null,
    val createdDeviceNickname: String? = null,
    val error: String? = null,
) {
    val nextEnabledFromPickPlant: Boolean get() = selectedPlant != null
    val nextEnabledFromWifi: Boolean get() = ssid.isNotBlank() && password.isNotBlank()
    val transferProgress: Float
        get() = when (val s = sessionStatus) {
            is ProvisioningSession.Status.Transferring ->
                if (s.totalChunks == 0) 0f else s.sentChunks.toFloat() / s.totalChunks
            is ProvisioningSession.Status.Transferred,
            is ProvisioningSession.Status.Success -> 1f
            else -> 0f
        }
}

enum class Step { PickPlant, Wifi, Tap, Success }

sealed interface AddDeviceEvent {
    data class Finished(val deviceSerial: String, val nickname: String) : AddDeviceEvent
    data object Cancelled : AddDeviceEvent
    data object RequestLocationPermission : AddDeviceEvent
}

@HiltViewModel
class AddDeviceWizardViewModel @Inject constructor(
    private val plantRepository: PlantRepository,
    private val deviceRepository: DeviceRepository,
    private val wifiProvider: CurrentWifiProvider,
    private val session: ProvisioningSession,
    private val supabase: SupabaseClient,
) : ViewModel() {

    private val _ui = MutableStateFlow(AddDeviceUiState())
    val ui: StateFlow<AddDeviceUiState> = combine(_ui, session.status) { base, status ->
        base.copy(sessionStatus = status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _ui.value)

    private val _events = Channel<AddDeviceEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var hasInsertedDevice = false
    // Set when the user taps "Pick from nearby networks" without location
    // permission. After grant, re-runs openNetworkPicker instead of just
    // re-prefilling the SSID — keeps the action they took intent-coupled.
    private var pickerPendingAfterPermission = false

    init {
        loadPlants()
        viewModelScope.launch {
            session.status.collect { status ->
                // The session is a @Singleton shared with contextual actions. Only
                // react to a Transferred carrying *our* Add command with the serial
                // we armed — otherwise a Locate/Hold/Dry/Wet Transferred could
                // trigger a spurious device insert if this VM hasn't been cleared yet.
                if (status !is ProvisioningSession.Status.Transferred) return@collect
                if (hasInsertedDevice) return@collect
                val cmd = status.command
                val expectedSerial = _ui.value.serial
                if (cmd is ApduProtocol.Command.Add && cmd.serial == expectedSerial) {
                    completeDeviceInsert()
                }
            }
        }
    }

    fun selectPlant(plant: Plant) {
        _ui.update { it.copy(selectedPlant = plant, error = null) }
    }

    fun advanceToWifi() {
        val selected = _ui.value.selectedPlant ?: return
        _ui.update {
            it.copy(
                step = Step.Wifi,
                error = null,
                locationServicesOff = !wifiProvider.isLocationServicesEnabled,
            )
        }
        // Warm the scan cache so the picker has fresh results when opened.
        // No-op if location permission isn't granted yet.
        wifiProvider.triggerScan()
        viewModelScope.launch {
            val prefilled = withTimeoutOrNull(1_500) { wifiProvider.currentSsid.firstOrNull { it != null } }
            _ui.update {
                it.copy(
                    ssid = if (it.ssid.isEmpty() && prefilled != null) prefilled else it.ssid,
                    ssidPrefilled = prefilled != null && it.ssid.isEmpty(),
                )
            }
            if (prefilled == null && !wifiProvider.hasLocationPermission) {
                _events.send(AddDeviceEvent.RequestLocationPermission)
            }
        }
    }

    /**
     * Re-checks the OS Location toggle. Called when the user returns from system
     * Settings (the only way to flip the toggle), so an enabled toggle clears
     * the banner and we retry SSID prefill.
     */
    fun refreshLocationServices() {
        if (_ui.value.step != Step.Wifi) return
        val nowOn = wifiProvider.isLocationServicesEnabled
        _ui.update { it.copy(locationServicesOff = !nowOn) }
        if (nowOn) {
            wifiProvider.triggerScan()
            viewModelScope.launch {
                val prefilled = withTimeoutOrNull(1_500) { wifiProvider.currentSsid.firstOrNull { it != null } } ?: return@launch
                _ui.update {
                    it.copy(
                        ssid = if (it.ssid.isEmpty()) prefilled else it.ssid,
                        ssidPrefilled = it.ssid.isEmpty(),
                    )
                }
            }
        }
    }

    fun setSsid(value: String) = _ui.update {
        // Manual edits clear the prefilled badge so the user isn't told their
        // hand-typed value was system-detected.
        it.copy(ssid = value, ssidPrefilled = false)
    }
    fun setPassword(value: String) = _ui.update { it.copy(password = value) }

    fun openNetworkPicker() {
        if (!wifiProvider.hasLocationPermission) {
            pickerPendingAfterPermission = true
            viewModelScope.launch { _events.send(AddDeviceEvent.RequestLocationPermission) }
            return
        }
        // Read off the lambda — scanResults touches WifiManager and we don't
        // want it inside _ui.update's atomic update block.
        val networks = wifiProvider.scanNetworks()
        _ui.update { it.copy(pickerOpen = true, pickerNetworks = networks) }
    }

    fun closeNetworkPicker() {
        _ui.update { it.copy(pickerOpen = false) }
    }

    fun selectNetworkFromPicker(ssid: String) {
        _ui.update {
            it.copy(
                ssid = ssid,
                ssidPrefilled = false,
                pickerOpen = false,
                error = null,
            )
        }
    }

    /**
     * Invoked by AddDeviceScreen after the Android permission dialog returns a grant.
     * Retry whichever action triggered the request — picker open if the user
     * tapped "Pick from nearby networks", otherwise SSID prefill.
     */
    fun onLocationPermissionGranted() {
        if (_ui.value.step != Step.Wifi) return
        // Permission was just granted, so any earlier triggerScan() in
        // advanceToWifi() was a no-op. Fire one now so the cache has data.
        wifiProvider.triggerScan()
        if (pickerPendingAfterPermission) {
            pickerPendingAfterPermission = false
            openNetworkPicker()
            return
        }
        viewModelScope.launch {
            val prefilled = withTimeoutOrNull(1_500) { wifiProvider.currentSsid.firstOrNull { it != null } } ?: return@launch
            _ui.update {
                it.copy(
                    ssid = if (it.ssid.isEmpty()) prefilled else it.ssid,
                    ssidPrefilled = it.ssid.isEmpty(),
                )
            }
        }
    }

    fun advanceToTap() {
        val ui = _ui.value
        val plant = ui.selectedPlant ?: return
        if (ui.ssid.isBlank() || ui.password.isBlank()) return

        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId.isNullOrBlank()) {
            _ui.update { it.copy(error = "Not signed in. Sign in again and retry.") }
            return
        }

        // Plant rows from Supabase can technically carry out-of-range targets; clamp
        // before passing to the APDU encoder so a bad row can't crash the wizard.
        val targetMoisture = (plant.moistureTargetPercent ?: DEFAULT_TARGET).coerceIn(0, 100)

        // Reuse the serial across retries so the physical device and the cloud don't
        // diverge if the first attempt burned it into firmware NVS but cloud sync failed.
        val serial = ui.serial ?: ApduProtocol.generateSerial()

        // Don't clobber an in-flight NFC flow belonging to someone else (e.g. a
        // Locate/Hold/Dry/Wet from Device Detail). Allow re-arming only if the
        // active session is this wizard's own pending Add with the same serial.
        val active = activeSessionCommand()
        val ownsSession = active is ApduProtocol.Command.Add && active.serial == serial
        if (active != null && !ownsSession) {
            _ui.update {
                it.copy(error = "Another NFC action is in progress. Finish it before adding a device.")
            }
            return
        }

        hasInsertedDevice = false
        _ui.update {
            it.copy(
                step = Step.Tap,
                serial = serial,
                error = null,
            )
        }
        session.arm(
            ApduProtocol.Command.Add(
                ssid = ui.ssid,
                password = ui.password,
                userId = userId,
                serial = serial,
                targetMoisture = targetMoisture,
                holdMode = false,
            ),
        )
    }

    private fun activeSessionCommand(): ApduProtocol.Command? =
        when (val current = session.status.value) {
            is ProvisioningSession.Status.ReadyToTap -> current.command
            is ProvisioningSession.Status.Transferring -> current.command
            is ProvisioningSession.Status.Transferred -> current.command
            is ProvisioningSession.Status.Success -> current.command
            else -> null
        }

    fun cancel() {
        android.util.Log.d("AddDeviceVM", "cancel() called", Throwable())
        dismissOwnedSession()
        viewModelScope.launch { _events.send(AddDeviceEvent.Cancelled) }
    }

    /**
     * Only dismiss the shared [ProvisioningSession] if its current command is *our*
     * Add with the serial we armed. A stale wizard VM (not yet garbage-collected)
     * must not tear down an in-flight Locate/Hold/Map from a different flow.
     */
    private fun dismissOwnedSession() {
        val ownedCommand = activeSessionCommand()
        val expectedSerial = _ui.value.serial
        val isOurs = ownedCommand is ApduProtocol.Command.Add &&
            ownedCommand.serial == expectedSerial
        if (isOurs) session.dismiss()
    }

    fun retryTap() {
        // advanceToTap() reuses the cached serial (via `ui.serial ?: generate`) so the
        // physical device's programmed serial and the cloud record stay in sync even if
        // only the cloud-insert leg failed the first time.
        if (_ui.value.step == Step.Tap) advanceToTap()
    }

    fun dismissError() {
        _ui.update { it.copy(error = null) }
    }

    private fun completeDeviceInsert() {
        val ui = _ui.value
        val serial = ui.serial ?: return
        val plant = ui.selectedPlant ?: return
        // Match the clamp applied in advanceToTap() so the value persisted to
        // Supabase equals the value burned into firmware NVS via the APDU payload.
        val targetMoisture = (plant.moistureTargetPercent ?: DEFAULT_TARGET).coerceIn(0, 100)
        hasInsertedDevice = true

        viewModelScope.launch {
            deviceRepository.addDevice(
                serial = serial,
                nickname = plant.commonName ?: plant.scientificName,
                plantId = plant.id,
                targetMoisturePercent = targetMoisture,
            ).onSuccess { device ->
                session.confirmSuccess()
                _ui.update {
                    it.copy(
                        step = Step.Success,
                        createdDeviceNickname = device.nickname,
                    )
                }
                _events.send(
                    AddDeviceEvent.Finished(
                        deviceSerial = device.serial,
                        nickname = device.nickname ?: plant.scientificName,
                    ),
                )
            }.onFailure {
                hasInsertedDevice = false
                session.reportFailure(ProvisioningSession.FailureReason.TransferIncomplete)
                _ui.update { it.copy(error = "Saved to your device, but we couldn't sync to the cloud. Try again.") }
            }
        }
    }

    private fun loadPlants() {
        viewModelScope.launch {
            _ui.update { it.copy(plantsLoading = true) }
            val plants = plantRepository.fetchAll()
            _ui.update { it.copy(plants = plants, plantsLoading = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dismissOwnedSession()
    }

    companion object {
        const val DEFAULT_TARGET = 50
    }
}
