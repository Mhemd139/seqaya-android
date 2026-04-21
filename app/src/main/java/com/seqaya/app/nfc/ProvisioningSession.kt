package com.seqaya.app.nfc

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the currently-armed NFC provisioning exchange.
 *
 * Lifecycle:
 *   Idle → arm(cmd) → ReadyToTap → onSelectAid() → Transferring → nextChunk()* → Transferred
 *                                                                              → confirmSuccess() → Success
 *                                                                              → onDeactivated(linkLoss) → Failed(LinkLost)
 *                                                                              → (timeout) → Failed(Timeout)
 *   any → dismiss() → Idle
 *
 * Thread-safety: all mutating methods are synchronized via the single coroutine dispatcher
 * this instance was constructed with. Callers are expected to call from the main thread
 * (HostApduService callbacks run on the binder thread, so those callbacks must post to
 * [CoroutineScope(dispatcher).launch] — see SeqayaHceService).
 */
@Singleton
class ProvisioningSession internal constructor(
    dispatcher: CoroutineDispatcher,
    private val timeoutMs: Long = 30_000L,
) {

    @Inject constructor() : this(Dispatchers.Main, 30_000L)

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _status = MutableStateFlow<Status>(Status.Idle)
    val status: StateFlow<Status> = _status

    /** True while a command is queued, mid-transfer, or waiting for confirmation. */
    val isActive: Boolean get() = _status.value !is Status.Idle

    private var pendingChunks: List<ByteArray> = emptyList()
    private var cursor: Int = 0
    private var timeoutJob: Job? = null

    @Synchronized
    fun arm(command: ApduProtocol.Command) {
        cancelTimeout()
        pendingChunks = ApduProtocol.chunkResponses(command.encode())
        cursor = 0
        _status.value = Status.ReadyToTap(command)
        scheduleTimeout()
    }

    /**
     * Firmware sent the SELECT AID APDU. Returns true if we accept (armed) or false
     * if this session is not ready — [SeqayaHceService] must return NACK_STATUS on false.
     */
    @Synchronized
    fun onSelectAid(): Boolean {
        val current = _status.value
        if (current !is Status.ReadyToTap) return false
        _status.value = Status.Transferring(current.command, sentChunks = 0, totalChunks = pendingChunks.size)
        // Re-arm the timeout so a stalled transfer (firmware stops polling without onDeactivated)
        // still reaches a terminal Failed(Timeout) state instead of wedging the singleton.
        scheduleTimeout()
        return true
    }

    /**
     * Firmware polled `0x02` for the next chunk. Returns the raw bytes to send (with
     * status prefix already applied) or null if the transfer is complete.
     *
     * Caller is expected to return [ApduProtocol.OK_STATUS] to firmware when this
     * returns null so the session handshake closes cleanly.
     */
    @Synchronized
    fun nextChunk(): ByteArray? {
        val current = _status.value
        if (current !is Status.Transferring) return null
        if (cursor >= pendingChunks.size) return null

        val chunk = pendingChunks[cursor]
        cursor++

        if (cursor >= pendingChunks.size) {
            // Terminal chunk consumed — transfer complete.
            cancelTimeout()
            _status.value = Status.Transferred(current.command)
        } else {
            _status.value = current.copy(sentChunks = cursor)
        }
        return chunk
    }

    /**
     * Android framework fired HostApduService.onDeactivated. Semantics depend on where
     * we are in the flow:
     *   - mid-transfer (Transferring): firmware lost the link → Failed(LinkLost)
     *   - post-transfer (Transferred, Success): expected, no-op
     *   - idle/ready: no active transfer, no-op
     */
    @Synchronized
    fun onDeactivated(@Suppress("UNUSED_PARAMETER") reason: Int) {
        if (_status.value is Status.Transferring) {
            cancelTimeout()
            _status.value = Status.Failed(FailureReason.LinkLost)
        }
    }

    /** Caller confirmed the firmware processed the command (e.g., saw Supabase event). */
    @Synchronized
    fun confirmSuccess() {
        val current = _status.value
        if (current is Status.Transferred) {
            _status.value = Status.Success(current.command)
        }
    }

    /** Caller reports a specific failure surfaced outside the NFC layer (e.g., Wi-Fi fail). */
    @Synchronized
    fun reportFailure(reason: FailureReason) {
        cancelTimeout()
        _status.value = Status.Failed(reason)
    }

    /** Reset to Idle — cancels timeout, clears pending chunks. Safe from any state. */
    @Synchronized
    fun dismiss() {
        cancelTimeout()
        pendingChunks = emptyList()
        cursor = 0
        _status.value = Status.Idle
    }

    private fun scheduleTimeout() {
        cancelTimeout()
        timeoutJob = scope.launch {
            delay(timeoutMs)
            when (_status.value) {
                is Status.ReadyToTap, is Status.Transferring -> {
                    _status.value = Status.Failed(FailureReason.Timeout)
                }
                else -> { /* terminal state reached before timeout — ignore */ }
            }
        }
    }

    private fun cancelTimeout() {
        timeoutJob?.cancel()
        timeoutJob = null
    }

    fun shutdown() {
        cancelTimeout()
        scope.cancel()
    }

    sealed interface Status {
        data object Idle : Status
        data class ReadyToTap(val command: ApduProtocol.Command) : Status
        data class Transferring(
            val command: ApduProtocol.Command,
            val sentChunks: Int,
            val totalChunks: Int,
        ) : Status
        data class Transferred(val command: ApduProtocol.Command) : Status
        data class Success(val command: ApduProtocol.Command) : Status
        data class Failed(val reason: FailureReason) : Status
    }

    enum class FailureReason {
        Timeout, LinkLost, TransferIncomplete, NfcNotAvailable, NfcDisabled, RemoteNack,
    }
}
