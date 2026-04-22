package com.seqaya.app.ui.contextual

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seqaya.app.data.repository.DeviceRepository
import com.seqaya.app.nfc.ApduProtocol
import com.seqaya.app.nfc.ProvisioningSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ContextualAction { Locate, HoldToggle, DryMap, WetMap }

enum class ContextualStep { Intro, Prepare, Tap, Success }

data class ContextualUiState(
    val action: ContextualAction,
    val deviceSerial: String,
    val deviceNickname: String = "",
    val step: ContextualStep = ContextualStep.Intro,
    val prepareRemainingSeconds: Int = 0,
    val sessionStatus: ProvisioningSession.Status = ProvisioningSession.Status.Idle,
    val error: String? = null,
) {
    val prepareUnlocked: Boolean get() = prepareRemainingSeconds <= 0
    val needsPrepareStep: Boolean
        get() = action == ContextualAction.DryMap || action == ContextualAction.WetMap
}

sealed interface ContextualEvent {
    data object Dismissed : ContextualEvent
    data class ChainWetMapping(val serial: String) : ContextualEvent
}

@HiltViewModel
class ContextualActionViewModel @Inject constructor(
    private val session: ProvisioningSession,
    private val deviceRepository: DeviceRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val action: ContextualAction = ContextualAction.valueOf(
        requireNotNull(savedStateHandle["action"]) { "action route arg missing" },
    )
    private val serial: String = requireNotNull(savedStateHandle["serial"]) { "serial route arg missing" }

    private val _ui = MutableStateFlow(
        ContextualUiState(
            action = action,
            deviceSerial = serial,
            step = ContextualStep.Intro,
            prepareRemainingSeconds = when (action) {
                ContextualAction.DryMap -> 30
                ContextualAction.WetMap -> 10
                else -> 0
            },
        )
    )
    val ui: StateFlow<ContextualUiState> = combine(_ui, session.status) { base, status ->
        base.copy(sessionStatus = status)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _ui.value)

    private val _events = Channel<ContextualEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Populate nickname from the device repo once, then drop the subscription.
        // The nickname for a running contextual flow is stable for the flow's lifetime —
        // holding a long-lived collector on observeDevices() is wasted work.
        viewModelScope.launch {
            val device = deviceRepository.observeDevices()
                .map { list -> list.firstOrNull { it.serial == serial } }
                .filterNotNull()
                .first()
            _ui.update { it.copy(deviceNickname = device.nickname ?: "your device") }
        }
        viewModelScope.launch {
            session.status.collect { status ->
                if (status is ProvisioningSession.Status.Transferred) {
                    session.confirmSuccess()
                    _ui.update { it.copy(step = ContextualStep.Success) }
                }
            }
        }
    }

    fun advanceFromIntro() {
        val next = if (_ui.value.needsPrepareStep) ContextualStep.Prepare else ContextualStep.Tap
        _ui.update { it.copy(step = next) }
        if (next == ContextualStep.Prepare) runPrepareTimer()
        if (next == ContextualStep.Tap) armSession()
    }

    fun advanceFromPrepare() {
        _ui.update { it.copy(step = ContextualStep.Tap) }
        armSession()
    }

    fun skipPrepareTimer() {
        _ui.update { it.copy(prepareRemainingSeconds = 0) }
    }

    fun retry() {
        armSession()
    }

    fun dismiss() {
        session.dismiss()
        viewModelScope.launch { _events.send(ContextualEvent.Dismissed) }
    }

    fun chainToWetMapping() {
        session.dismiss()
        viewModelScope.launch { _events.send(ContextualEvent.ChainWetMapping(serial)) }
    }

    private fun runPrepareTimer() {
        viewModelScope.launch {
            while (_ui.value.prepareRemainingSeconds > 0) {
                delay(1_000)
                _ui.update { it.copy(prepareRemainingSeconds = (it.prepareRemainingSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    private fun armSession() {
        val cmd = when (action) {
            ContextualAction.Locate -> ApduProtocol.Command.Locate
            ContextualAction.HoldToggle -> ApduProtocol.Command.HoldToggle
            ContextualAction.DryMap -> ApduProtocol.Command.DryMap
            ContextualAction.WetMap -> ApduProtocol.Command.WetMap
        }
        session.arm(cmd)
    }

    override fun onCleared() {
        super.onCleared()
        session.dismiss()
    }
}
