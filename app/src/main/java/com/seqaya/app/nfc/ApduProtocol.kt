package com.seqaya.app.nfc

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.security.SecureRandom

/**
 * Wire protocol between the Android app (HCE card) and Seqaya firmware (PN532 reader).
 *
 * Payload is a JSON object; firmware parses with ArduinoJson. The old
 * `$`-delimited format bricked devices on SSIDs/passwords containing `$`
 * (real-world: "Pa$$w0rd", "Café$TPMO"). JSON handles every character via
 * proper string escaping, and unknown keys are ignored — new fields are
 * additive, no coordinated app+firmware rollout needed.
 *
 * Transaction (Qanat's firmware is the authority):
 *   1. Firmware → app: SELECT AID APDU (12 bytes, F2 23 34 45 56 67).
 *   2. App → firmware: OK 90 00 (armed) or NACK 6A 82 (idle).
 *   3. Firmware → app: poll byte 0x02 until response starts with 90 00.
 *   4. App → firmware: chunks of `[2-byte status][up to PAYLOAD_PER_CHUNK payload bytes]`.
 *      Non-terminal status 00 00, terminal 90 00. processResponse() treats
 *      bytes 0-1 of every chunk as status, so the prefix is mandatory on all.
 *
 * Compact short keys to keep the payload small across NFC chunks:
 *   c=command, ssid, pw=password, uid=userId, sn=serial,
 *   t=targetMoisture, h=holdMode, k=keepHistory
 */
object ApduProtocol {

    val AID = byteArrayOf(0xF2.toByte(), 0x23, 0x34, 0x45, 0x56, 0x67)

    val OK_STATUS = byteArrayOf(0x90.toByte(), 0x00)
    val NACK_STATUS = byteArrayOf(0x6A, 0x82.toByte())
    private val CONTINUE_STATUS = byteArrayOf(0x00, 0x00)

    private val PULL_BYTE = byteArrayOf(0x02)

    /** Max payload bytes per chunk. Adafruit_PN532's internal pn532_packetbuffer is 64 B,
     *  of which 8 bytes are framing and 2 are our status prefix, leaving ~54 B for payload.
     *  48 gives a safe margin. With 8-byte chunks an Add payload took ~20 round-trips and
     *  exceeded the HCE link window on most phones; at 48 it's 3-4 round-trips, comfortably
     *  within the link-maintenance budget. */
    const val PAYLOAD_PER_CHUNK = 48

    /** Canonical SELECT AID APDU from firmware: 00 A4 04 00 06 <AID> 00. */
    private val SELECT_AID_HEADER = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, 0x06)

    private val JSON = Json { encodeDefaults = true }

    sealed class Command(val letter: Char) {
        protected abstract fun extraFields(builder: JsonObjectBuilderScope)

        fun encode(): ByteArray {
            val obj = buildJsonObject {
                put("c", letter.toString())
                extraFields(JsonObjectBuilderScope(this))
            }
            return JSON.encodeToString(JsonObject.serializer(), obj)
                .toByteArray(Charsets.UTF_8)
        }

        data class Add(
            val ssid: String,
            val password: String,
            val userId: String,
            val serial: String,
            val targetMoisture: Int,
            val holdMode: Boolean,
        ) : Command('A') {
            init {
                require(targetMoisture in 0..100) {
                    "targetMoisture $targetMoisture out of 0..100"
                }
            }

            override fun extraFields(builder: JsonObjectBuilderScope) = builder.apply {
                put("ssid", ssid)
                put("pw", password)
                put("uid", userId)
                put("sn", serial)
                put("t", targetMoisture)
                put("h", if (holdMode) 1 else 0)
            }.let { }
        }

        data object Locate : Command('L') {
            override fun extraFields(builder: JsonObjectBuilderScope) {}
        }

        data object HoldToggle : Command('H') {
            override fun extraFields(builder: JsonObjectBuilderScope) {}
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
                require(targetMoisture in 0..100) {
                    "targetMoisture $targetMoisture out of 0..100"
                }
            }

            override fun extraFields(builder: JsonObjectBuilderScope) = builder.apply {
                put("ssid", ssid)
                put("pw", password)
                put("uid", userId)
                put("sn", serial)
                put("t", targetMoisture)
                put("h", if (holdMode) 1 else 0)
                put("k", if (keepHistory) 1 else 0)
            }.let { }
        }

        data object DryMap : Command('D') {
            override fun extraFields(builder: JsonObjectBuilderScope) {}
        }

        data object WetMap : Command('W') {
            override fun extraFields(builder: JsonObjectBuilderScope) {}
        }
    }

    /** Thin wrapper so subclasses don't need to import buildJsonObject internals. */
    class JsonObjectBuilderScope(private val delegate: kotlinx.serialization.json.JsonObjectBuilder) {
        fun put(key: String, value: String) { delegate.put(key, JsonPrimitive(value)) }
        fun put(key: String, value: Int) { delegate.put(key, JsonPrimitive(value)) }
    }

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
