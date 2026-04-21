package com.seqaya.app.nfc

import java.security.SecureRandom

/**
 * Wire protocol between the Android app (HCE card) and Seqaya firmware (PN532 reader).
 *
 * Transaction shape (Qanat's firmware is the authority):
 *   1. Firmware → app: SELECT AID APDU (12 bytes, F2 23 34 45 56 67).
 *   2. App → firmware: OK status 90 00 (session armed) or NACK 6A 82 (not ready).
 *   3. Firmware → app: poll byte 0x02, repeated until it receives a response whose first
 *      two bytes are 90 00.
 *   4. App → firmware: one response per poll, each carrying a 2-byte status prefix +
 *      up to 8 bytes of payload. Non-terminal status = 00 00, terminal = 90 00.
 *
 * SeqayaStreamingV2.ino:processResponse treats bytes 0-1 of every response as the
 * status code and appends bytes 2+ as payload, so the status prefix is mandatory on
 * every chunk — not just the last one.
 */
object ApduProtocol {

    val AID = byteArrayOf(0xF2.toByte(), 0x23, 0x34, 0x45, 0x56, 0x67)

    val OK_STATUS = byteArrayOf(0x90.toByte(), 0x00)
    val NACK_STATUS = byteArrayOf(0x6A, 0x82.toByte())
    private val CONTINUE_STATUS = byteArrayOf(0x00, 0x00)

    /** Firmware's single-byte "next chunk please" request. */
    private val PULL_BYTE = byteArrayOf(0x02)

    /** Max payload bytes per chunk — 10-byte total chunk minus 2-byte status prefix. */
    const val PAYLOAD_PER_CHUNK = 8

    /** Canonical SELECT AID APDU from firmware: 00 A4 04 00 06 <AID> 00. */
    private val SELECT_AID_HEADER = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, 0x06)

    /**
     * The firmware parser splits payload on literal `$`. Any field that itself contains `$`
     * shifts every following field position, corrupting the serial / moisture / hold-mode
     * writes to NVS. Since firmware does not unescape, we reject any string field carrying
     * `$` at construction time and surface the error at the UI boundary (Wi-Fi step).
     */
    class DelimiterInFieldException(val field: String) :
        IllegalArgumentException("Field '$field' contains '\$' which is reserved as the APDU delimiter. Rejecting to avoid firmware parse corruption.")

    private fun String.requireNoDelimiter(fieldName: String): String {
        if (contains('$')) throw DelimiterInFieldException(fieldName)
        return this
    }

    sealed class Command(val letter: Char) {
        protected abstract fun body(): String

        fun encode(): ByteArray = "$letter\$${body()}".toByteArray(Charsets.UTF_8)

        data class Add(
            val ssid: String,
            val password: String,
            val userId: String,
            val serial: String,
            val targetMoisture: Int,
            val holdMode: Boolean,
        ) : Command('A') {
            init {
                ssid.requireNoDelimiter("ssid")
                password.requireNoDelimiter("password")
                userId.requireNoDelimiter("userId")
                serial.requireNoDelimiter("serial")
                require(targetMoisture in 0..100) {
                    "targetMoisture $targetMoisture out of 0..100"
                }
            }

            override fun body() = buildString {
                append(ssid); append('$')
                append(password); append('$')
                append(userId); append('$')
                append(serial); append('$')
                append(targetMoisture); append('$')
                append(if (holdMode) '1' else '0'); append('$')
            }
        }

        data object Locate : Command('L') {
            override fun body() = ""
        }

        data object HoldToggle : Command('H') {
            override fun body() = ""
        }

        data class Reprogram(
            val ssid: String,
            val password: String,
            val userId: String,
            val serial: String,
            val targetMoisture: Int,
            val holdMode: Boolean,
            val keepHistory: Boolean,
        ) : Command('R') {
            init {
                ssid.requireNoDelimiter("ssid")
                password.requireNoDelimiter("password")
                userId.requireNoDelimiter("userId")
                serial.requireNoDelimiter("serial")
                require(targetMoisture in 0..100) {
                    "targetMoisture $targetMoisture out of 0..100"
                }
            }

            override fun body() = buildString {
                append(ssid); append('$')
                append(password); append('$')
                append(userId); append('$')
                append(serial); append('$')
                append(targetMoisture); append('$')
                append(if (holdMode) '1' else '0'); append('$')
                append(if (keepHistory) '1' else '0'); append('$')
            }
        }

        data object DryMap : Command('D') {
            override fun body() = ""
        }

        data object WetMap : Command('W') {
            override fun body() = ""
        }
    }

    /** Convenience predicate for UI validation (Wi-Fi step). */
    fun fieldContainsDelimiter(value: String): Boolean = value.contains('$')

    /**
     * Split [payload] into chunks that firmware's poll loop can reassemble.
     *
     * Each chunk is [2 status bytes][0..PAYLOAD_PER_CHUNK payload bytes].
     * Non-terminal chunks carry CONTINUE_STATUS; the final chunk carries OK_STATUS.
     * A zero-length payload collapses to a single terminal chunk of just the status.
     */
    fun chunkResponses(payload: ByteArray): List<ByteArray> {
        if (payload.isEmpty()) return listOf(OK_STATUS)

        val chunks = mutableListOf<ByteArray>()
        var offset = 0
        while (offset < payload.size) {
            val end = minOf(offset + PAYLOAD_PER_CHUNK, payload.size)
            val isTerminal = end == payload.size
            val status = if (isTerminal) OK_STATUS else CONTINUE_STATUS
            chunks += status + payload.copyOfRange(offset, end)
            offset = end
        }
        return chunks
    }

    fun isSelectAid(apdu: ByteArray): Boolean {
        if (apdu.size < SELECT_AID_HEADER.size + AID.size) return false
        for (i in SELECT_AID_HEADER.indices) {
            if (apdu[i] != SELECT_AID_HEADER[i]) return false
        }
        for (i in AID.indices) {
            if (apdu[SELECT_AID_HEADER.size + i] != AID[i]) return false
        }
        return true
    }

    fun isPullByte(apdu: ByteArray): Boolean =
        apdu.size == 1 && apdu[0] == PULL_BYTE[0]

    /**
     * Generate a fresh device serial. Format: `SQ-{8 hex uppercase}`, e.g. `SQ-A3F72B91`.
     * 2^32 namespace; Supabase UNIQUE constraint is the collision backstop — on conflict,
     * call generateSerial() again and retry.
     */
    fun generateSerial(): String {
        val bytes = ByteArray(4).also { RANDOM.nextBytes(it) }
        val hex = bytes.joinToString("") { "%02X".format(it) }
        return "SQ-$hex"
    }

    private val RANDOM = SecureRandom()
}
