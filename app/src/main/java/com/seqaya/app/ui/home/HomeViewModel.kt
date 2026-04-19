package com.seqaya.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seqaya.app.data.remote.ConnectivityObserver
import com.seqaya.app.data.repository.DeviceRepository
import com.seqaya.app.data.repository.ReadingRepository
import com.seqaya.app.domain.model.Device
import com.seqaya.app.domain.model.DeviceWithReading
import com.seqaya.app.domain.model.Reading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val devices: List<DeviceWithReading> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && devices.isEmpty()
    val thirstyDevice: DeviceWithReading? get() = devices.firstOrNull { it.needsAttention }
}

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val readingRepository: ReadingRepository,
    connectivity: ConnectivityObserver,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(
        deviceRepository.observeDevices().debounce(100L).distinctUntilChanged(),
        readingRepository.observeLatest()
            .map { list -> list.associateBy { it.deviceSerial } }
            .distinctUntilChanged(),
        connectivity.isOnline,
    ) { devices, readingsBySerial, online ->
        HomeUiState(
            isLoading = false,
            isOffline = !online,
            devices = merge(devices, readingsBySerial),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            deviceRepository.refresh()
            val serials = deviceRepository.observeDevices().first().map { it.serial }
            if (serials.isEmpty()) return@launch
            readingRepository.refreshLatestFor(serials)
            readingRepository.subscribe(viewModelScope, serials)
        }
    }

    private fun merge(
        devices: List<Device>,
        readings: Map<String, Reading>,
    ): List<DeviceWithReading> = devices.map { device ->
        val latest = readings[device.serial]
        DeviceWithReading(
            device = device,
            latest = latest,
            recentMoisture = latest?.let { List(48) { it.soilMoisturePercent } } ?: emptyList(),
            lastWateredAt = latest?.takeIf { it.isValveOpen }?.recordedAt ?: fallbackWateringInstant(),
        )
    }

    private fun fallbackWateringInstant(): Instant? = null
}
