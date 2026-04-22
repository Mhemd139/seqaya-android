package com.seqaya.app.ui.provisioning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seqaya.app.data.repository.DeviceRepository
import com.seqaya.app.data.repository.PlantRepository
import com.seqaya.app.domain.model.Plant
import com.seqaya.app.nfc.ApduProtocol
import com.seqaya.app.nfc.ProvisioningSession
import com.seqaya.app.wifi.CurrentWifiProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
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
        val prefilled = runCatching { wifiProvider.currentSsid() }.getOrNull()
        _ui.update {
            it.copy(
                step = Step.Wifi,
                ssid = if (it.ssid.isEmpty() && prefilled != null) prefilled else it.ssid,
                ssidPrefilled = prefilled != null && it.ssid.isEmpty(),
                error = null,
            )
        }
        if (prefilled == null && !wifiProvider.hasLocationPermission) {
            viewModelScope.launch {
                _events.send(AddDeviceEvent.RequestLocationPermission)
            }
        }
        // `selected` used for target default in arming below; no-op here.
        @Suppress("UNUSED_VARIABLE") val _ignore = selected
    }

    fun setSsid(value: String) = _ui.update { it.copy(ssid = value) }
    fun setPassword(value: String) = _ui.update { it.copy(password = value) }

    /**
     * Invoked by AddDeviceScreen after the Android permission dialog returns a grant.
     * Retry the SSID prefill now that we can read WifiManager.connectionInfo.
     */
    fun onLocationPermissionGranted() {
        val current = _ui.value
        if (current.step != Step.Wifi) return
        val prefilled = runCatching { wifiProvider.currentSsid() }.getOrNull() ?: return
        _ui.update {
            it.copy(
                ssid = if (it.ssid.isEmpty()) prefilled else it.ssid,
                ssidPrefilled = it.ssid.isEmpty(),
            )
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

    fun cancel() {
        dismissOwnedSession()
        viewModelScope.launch { _events.send(AddDeviceEvent.Cancelled) }
    }

    /**
     * Only dismiss the shared [ProvisioningSession] if its current command is *our*
     * Add with the serial we armed. A stale wizard VM (not yet garbage-collected)
     * must not tear down an in-flight Locate/Hold/Map from a different flow.
     */
    private fun dismissOwnedSession() {
        val current = session.status.value
        val ownedCommand = when (current) {
            is ProvisioningSession.Status.ReadyToTap -> current.command
            is ProvisioningSession.Status.Transferring -> current.command
            is ProvisioningSession.Status.Transferred -> current.command
            is ProvisioningSession.Status.Success -> current.command
            else -> null
        }
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
        hasInsertedDevice = true

        viewModelScope.launch {
            deviceRepository.addDevice(
                serial = serial,
                nickname = plant.commonName ?: plant.scientificName,
                plantId = plant.id,
                targetMoisturePercent = plant.moistureTargetPercent ?: DEFAULT_TARGET,
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
