package com.seqaya.app.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single HCE service for Seqaya device provisioning + contextual commands.
 *
 * Replaces the four competing implementations from the v1 tutorial fork
 * (KHostApduService / MyHostApduService / MyHostApduServiceSimple / BasicHceService).
 * All six firmware commands (Add, Locate, HoldToggle, Reprogram, DryMap, WetMap)
 * go through this one service, arming is tracked in [ProvisioningSession].
 *
 * When the phone is near an NFC reader but [ProvisioningSession.isActive] is false,
 * SELECT AID replies NACK so stray reads do not receive a session handshake.
 */
@AndroidEntryPoint
class SeqayaHceService : HostApduService() {

    @Inject lateinit var session: ProvisioningSession

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null || commandApdu.isEmpty()) return ApduProtocol.NACK_STATUS

        if (ApduProtocol.isSelectAid(commandApdu)) {
            return if (session.onSelectAid()) ApduProtocol.OK_STATUS else ApduProtocol.NACK_STATUS
        }

        if (ApduProtocol.isPullByte(commandApdu)) {
            val chunk = session.nextChunk()
            if (chunk != null) return chunk
            // nextChunk() returns null either when the transfer just completed (Transferred)
            // or when the session is idle/stray. Only reply 90 00 in the completed case;
            // stray polls get NACK so we don't silently acknowledge unrelated NFC activity.
            return if (session.status.value is ProvisioningSession.Status.Transferred) {
                ApduProtocol.OK_STATUS
            } else {
                ApduProtocol.NACK_STATUS
            }
        }

        Log.w(TAG, "Unknown APDU: ${commandApdu.joinToString(" ") { "%02X".format(it) }}")
        return ApduProtocol.NACK_STATUS
    }

    override fun onDeactivated(reason: Int) {
        session.onDeactivated(reason)
    }

    private companion object {
        const val TAG = "SeqayaHceService"
    }
}
