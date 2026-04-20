package com.seqaya.app.ui.device

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seqaya.app.data.repository.DeviceRepository
import com.seqaya.app.data.repository.ReadingRepository
import com.seqaya.app.domain.model.Device
import com.seqaya.app.domain.model.Reading
import com.seqaya.app.domain.wateringEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class DeviceDetailUiState(
    val device: Device? = null,
    val points: List<MoisturePoint> = emptyList(),
    val wateringEvents: List<Instant> = emptyList(),
    val range: ChartRange = ChartRange.DAYS_30,
    val error: String? = null,
    val shouldNavigateBack: Boolean = false,
) {
    val latestPercent: Int? get() = points.lastOrNull()?.percent
    val isEmpty: Boolean get() = points.isEmpty()

    companion object {
        fun windowStart(range: ChartRange, nowEpochMs: Long = System.currentTimeMillis()): Long =
            nowEpochMs - range.durationMs
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DeviceDetailViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val readingRepository: ReadingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val serial: String = requireNotNull(savedStateHandle["serial"]) { "serial route arg missing" }
    private val rangeFlow = MutableStateFlow(ChartRange.DAYS_30)
    private val errorFlow = MutableStateFlow<String?>(null)
    private val navigateBackFlow = MutableStateFlow(false)

    private val deviceFlow = deviceRepository.observeDevices()
        .map { list -> list.firstOrNull { it.serial == serial } }
        .distinctUntilChanged()

    private val readingsFlow = rangeFlow.flatMapLatest { range ->
        readingRepository.observeRecent(serial, DeviceDetailUiState.windowStart(range))
    }

    val state: StateFlow<DeviceDetailUiState> = combine(
        deviceFlow,
        readingsFlow,
        rangeFlow,
        errorFlow,
        navigateBackFlow,
    ) { device, readings, range, error, navBack ->
        DeviceDetailUiState(
            device = device,
            points = readings.toMoisturePoints(),
            wateringEvents = wateringEvents(readings),
            range = range,
            error = error,
            shouldNavigateBack = navBack,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DeviceDetailUiState(),
    )

    fun setRange(range: ChartRange) {
        rangeFlow.value = range
    }

    fun renameDevice(nickname: String) {
        val id = state.value.device?.id ?: return
        viewModelScope.launch {
            deviceRepository.updateNickname(id, nickname)
                .onFailure { errorFlow.value = "Couldn't rename device." }
        }
    }

    fun updateTarget(percent: Int) {
        val id = state.value.device?.id ?: return
        viewModelScope.launch {
            deviceRepository.updateTarget(id, percent)
                .onFailure { errorFlow.value = "Couldn't update target." }
        }
    }

    fun deleteDevice() {
        val id = state.value.device?.id ?: return
        viewModelScope.launch {
            deviceRepository.delete(id)
                .onSuccess { navigateBackFlow.value = true }
                .onFailure { errorFlow.value = "Couldn't remove device." }
        }
    }

    fun dismissError() {
        errorFlow.value = null
    }

    fun consumeNavigation() {
        navigateBackFlow.value = false
    }

    private fun List<Reading>.toMoisturePoints(): List<MoisturePoint> =
        map { MoisturePoint(it.recordedAt, it.soilMoisturePercent) }
}
