package com.seqaya.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seqaya.app.data.remote.ConnectivityObserver
import com.seqaya.app.data.repository.AuthRepository
import com.seqaya.app.data.repository.DeviceRepository
import com.seqaya.app.data.repository.ReadingRepository
import com.seqaya.app.domain.model.AuthState
import com.seqaya.app.domain.model.Device
import com.seqaya.app.domain.model.DeviceWithReading
import com.seqaya.app.domain.model.Reading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val devices: List<DeviceWithReading> = emptyList(),
    val error: String? = null,
    val avatarLetter: String = "",
) {
    val isEmpty: Boolean get() = !isLoading && devices.isEmpty()
    val thirstyDevice: DeviceWithReading? get() = devices.firstOrNull { it.needsAttention }
}

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val readingRepository: ReadingRepository,
    private val debugSeeder: com.seqaya.app.debug.DebugSeeder,
    authRepository: AuthRepository,
    connectivity: ConnectivityObserver,
) : ViewModel() {

    private val errorFlow = MutableStateFlow<String?>(null)

    private val avatarLetterFlow = authRepository.authState.map { auth ->
        val user = (auth as? AuthState.Authenticated)?.user
        (user?.displayName ?: user?.email).orEmpty().firstOrNull()?.uppercase().orEmpty()
    }.distinctUntilChanged()

    val state: StateFlow<HomeUiState> = combine(
        deviceRepository.observeDevices().debounce(100L).distinctUntilChanged(),
        readingRepository.observeLatest()
            .map { list -> list.associateBy { it.deviceSerial } }
            .distinctUntilChanged(),
        connectivity.isOnline,
        errorFlow,
        avatarLetterFlow,
    ) { devices, readingsBySerial, online, error, avatarLetter ->
        HomeUiState(
            isLoading = false,
            isOffline = !online,
            devices = merge(devices, readingsBySerial),
            error = error,
            avatarLetter = avatarLetter,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            deviceRepository.refresh().onFailure { errorFlow.value = "Couldn't reach the cloud. Showing the last state we know." }
            val serials = deviceRepository.serialsSnapshot()
            if (serials.isEmpty()) return@launch
            readingRepository.refreshLatestFor(serials).onFailure { errorFlow.value = "Couldn't refresh moisture data." }
            readingRepository.subscribe(viewModelScope, serials)
        }
    }

    fun dismissError() {
        errorFlow.value = null
    }

    /**
     * Debug-only: seed a mock device + 48 h of readings. The UI gate in HomeScreen
     * already hides the trigger in release builds, but we defence-in-depth here too
     * — any accidental release-side caller gets a no-op instead of writing mock data.
     */
    fun seedMockDevice() {
        if (!com.seqaya.app.BuildConfig.DEBUG) return
        viewModelScope.launch {
            debugSeeder.seedMockDevice()
                .onSuccess { serial ->
                    deviceRepository.refresh()
                        .onFailure { errorFlow.value = "Seeded, but couldn't refresh device list." }
                    readingRepository.refreshLatestFor(listOf(serial))
                        .onFailure { errorFlow.value = "Seeded, but couldn't refresh readings." }
                }
                .onFailure {
                    errorFlow.value = "Seeder failed: ${it.message ?: "unknown"}"
                }
        }
    }

    private fun merge(
        devices: List<Device>,
        readings: Map<String, Reading>,
    ): List<DeviceWithReading> = devices.map { device ->
        val latest = readings[device.serial]
        val flatHistory = if (latest != null) List(48) { latest.soilMoisturePercent } else emptyList()
        DeviceWithReading(
            device = device,
            latest = latest,
            recentMoisture = flatHistory,
            lastWateredAt = latest?.takeIf { it.isValveOpen }?.recordedAt,
        )
    }
}
