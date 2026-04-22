package com.seqaya.app.nfc

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApduProtocolTest {

    // Parse the encoded payload back into a JsonObject so tests read fields by name.
    private fun ApduProtocol.Command.encodedAsJson(): JsonObject =
        Json.parseToJsonElement(encode().toString(Charsets.UTF_8)) as JsonObject

    @Test fun `AID is F2 23 34 45 56 67`() {
        assertArrayEquals(
            byteArrayOf(0xF2.toByte(), 0x23, 0x34, 0x45, 0x56, 0x67),
            ApduProtocol.AID,
        )
    }

    @Test fun `OK status is 90 00`() {
        assertArrayEquals(byteArrayOf(0x90.toByte(), 0x00), ApduProtocol.OK_STATUS)
    }

    @Test fun `NACK status is 6A 82`() {
        assertArrayEquals(byteArrayOf(0x6A, 0x82.toByte()), ApduProtocol.NACK_STATUS)
    }

    @Test fun `generateSerial returns SQ-prefixed 8 hex uppercase`() {
        repeat(50) {
            val s = ApduProtocol.generateSerial()
            assertTrue("unexpected: $s", s.matches(Regex("^SQ-[0-9A-F]{8}$")))
        }
    }

    @Test fun `generateSerial produces unique values`() {
        val serials = (1..200).map { ApduProtocol.generateSerial() }.toSet()
        assertTrue("collisions in 200 samples: ${200 - serials.size}", serials.size >= 195)
    }

    @Test fun `Add command encodes all seven fields`() {
        val json = ApduProtocol.Command.Add(
            ssid = "MyWifi",
            password = "secret123",
            userId = "user-abc",
            serial = "SQ-A3F72B91",
            targetMoisture = 60,
            holdMode = false,
        ).encodedAsJson()

        assertEquals("A", json["c"]!!.jsonPrimitive.content)
        assertEquals("MyWifi", json["ssid"]!!.jsonPrimitive.content)
        assertEquals("secret123", json["pw"]!!.jsonPrimitive.content)
        assertEquals("user-abc", json["uid"]!!.jsonPrimitive.content)
        assertEquals("SQ-A3F72B91", json["sn"]!!.jsonPrimitive.content)
        assertEquals(60, json["t"]!!.jsonPrimitive.intOrNull)
        assertEquals(0, json["h"]!!.jsonPrimitive.intOrNull)
    }

    @Test fun `Add encodes holdMode true as 1`() {
        val json = ApduProtocol.Command.Add("w", "p", "u", "SQ-00000001", 55, true)
            .encodedAsJson()
        assertEquals(1, json["h"]!!.jsonPrimitive.intOrNull)
    }

    @Test fun `Locate encodes command-only JSON`() {
        val json = ApduProtocol.Command.Locate.encodedAsJson()
        assertEquals("L", json["c"]!!.jsonPrimitive.content)
        assertEquals(1, json.size)
    }

    @Test fun `HoldToggle encodes command-only JSON`() {
        val json = ApduProtocol.Command.HoldToggle.encodedAsJson()
        assertEquals("H", json["c"]!!.jsonPrimitive.content)
        assertEquals(1, json.size)
    }

    @Test fun `DryMap encodes command-only JSON`() {
        val json = ApduProtocol.Command.DryMap.encodedAsJson()
        assertEquals("D", json["c"]!!.jsonPrimitive.content)
        assertEquals(1, json.size)
    }

    @Test fun `WetMap encodes command-only JSON`() {
        val json = ApduProtocol.Command.WetMap.encodedAsJson()
        assertEquals("W", json["c"]!!.jsonPrimitive.content)
        assertEquals(1, json.size)
    }

    @Test fun `Reprogram keepHistory true encodes k as 1`() {
        val json = ApduProtocol.Command.Reprogram(
            ssid = "w", password = "p", userId = "u",
            serial = "SQ-DEADBEEF", targetMoisture = 70, holdMode = false,
            keepHistory = true,
        ).encodedAsJson()
        assertEquals("R", json["c"]!!.jsonPrimitive.content)
        assertEquals(1, json["k"]!!.jsonPrimitive.intOrNull)
    }

    @Test fun `Reprogram keepHistory false encodes k as 0`() {
        val json = ApduProtocol.Command.Reprogram(
            "w", "p", "u", "SQ-00000002", 50, true, keepHistory = false,
        ).encodedAsJson()
        assertEquals(0, json["k"]!!.jsonPrimitive.intOrNull)
    }

    @Test fun `Add accepts SSID containing dollar sign`() {
        val json = ApduProtocol.Command.Add(
            ssid = "Café\$TPMO", password = "p", userId = "u",
            serial = "SQ-A3F72B91", targetMoisture = 60, holdMode = false,
        ).encodedAsJson()
        assertEquals("Café\$TPMO", json["ssid"]!!.jsonPrimitive.content)
    }

    @Test fun `Add accepts password with quotes backslashes and emoji`() {
        val pw = "Pa\$\$\"w0rd\\\\🔐"
        val json = ApduProtocol.Command.Add(
            ssid = "w", password = pw, userId = "u",
            serial = "SQ-A3F72B91", targetMoisture = 60, holdMode = false,
        ).encodedAsJson()
        assertEquals(pw, json["pw"]!!.jsonPrimitive.content)
    }

    @Test fun `Add accepts multi-byte unicode and newline in ssid`() {
        val ssid = "Café 🌱\nWifi"
        val json = ApduProtocol.Command.Add(
            ssid = ssid, password = "p", userId = "u",
            serial = "SQ-A3F72B91", targetMoisture = 60, holdMode = false,
        ).encodedAsJson()
        assertEquals(ssid, json["ssid"]!!.jsonPrimitive.content)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Add rejects targetMoisture below 0`() {
        ApduProtocol.Command.Add(
            "w", "p", "u", "SQ-A3F72B91", targetMoisture = -1, holdMode = false,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Add rejects targetMoisture above 100`() {
        ApduProtocol.Command.Add(
            "w", "p", "u", "SQ-A3F72B91", targetMoisture = 101, holdMode = false,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Reprogram rejects out-of-range targetMoisture`() {
        ApduProtocol.Command.Reprogram(
            "w", "p", "u", "SQ-A3F72B91", targetMoisture = 200, holdMode = false,
            keepHistory = false,
        )
    }

    @Test fun `chunkResponses wraps tiny payload in one terminal chunk with 90 00`() {
        val chunks = ApduProtocol.chunkResponses("L".toByteArray(Charsets.UTF_8))
        assertEquals(1, chunks.size)
        assertArrayEquals(
            byteArrayOf(0x90.toByte(), 0x00, 'L'.code.toByte()),
            chunks[0],
        )
    }

    @Test fun `chunkResponses splits 16-byte payload into two chunks`() {
        val payload = "0123456789ABCDEF".toByteArray(Charsets.UTF_8) // exactly 16 bytes
        val chunks = ApduProtocol.chunkResponses(payload)
        assertEquals(2, chunks.size)
        assertArrayEquals(
            byteArrayOf(0x00, 0x00) + "01234567".toByteArray(Charsets.UTF_8),
            chunks[0],
        )
        assertArrayEquals(
            byteArrayOf(0x90.toByte(), 0x00) + "89ABCDEF".toByteArray(Charsets.UTF_8),
            chunks[1],
        )
    }

    @Test fun `chunkResponses 17-byte payload splits into two non-terminal plus terminal single-byte`() {
        val payload = "0123456789ABCDEFG".toByteArray(Charsets.UTF_8)
        val chunks = ApduProtocol.chunkResponses(payload)
        assertEquals(3, chunks.size)
        assertEquals("00 00", chunks[0].take(2).joinToString(" ") { "%02X".format(it) })
        assertEquals("00 00", chunks[1].take(2).joinToString(" ") { "%02X".format(it) })
        assertEquals("90 00", chunks[2].take(2).joinToString(" ") { "%02X".format(it) })
        assertEquals(10, chunks[0].size)
        assertEquals(10, chunks[1].size)
        assertEquals(3, chunks[2].size)
    }

    @Test fun `chunkResponses 0-byte payload is one terminal chunk of just 90 00`() {
        val chunks = ApduProtocol.chunkResponses(ByteArray(0))
        assertEquals(1, chunks.size)
        assertArrayEquals(byteArrayOf(0x90.toByte(), 0x00), chunks[0])
    }

    @Test fun `chunkResponses round-trip reassembles full payload from data bytes`() {
        val original = ApduProtocol.Command.Add(
            "MyLongNetworkSSID_with_some_chars", "CorrectHorseBatteryStaple", "uuid-1234",
            "SQ-A3F72B91", 65, false,
        ).encode()
        val chunks = ApduProtocol.chunkResponses(original)
        val reassembled = chunks.fold(ByteArray(0)) { acc, chunk -> acc + chunk.drop(2).toByteArray() }
        assertArrayEquals(original, reassembled)
    }

    @Test fun `isSelectAid recognizes canonical SELECT AID APDU`() {
        val selectApdu = byteArrayOf(
            0x00, 0xA4.toByte(), 0x04, 0x00, 0x06,
            0xF2.toByte(), 0x23, 0x34, 0x45, 0x56, 0x67,
            0x00,
        )
        assertTrue(ApduProtocol.isSelectAid(selectApdu))
    }

    @Test fun `isSelectAid rejects wrong AID`() {
        val wrong = byteArrayOf(
            0x00, 0xA4.toByte(), 0x04, 0x00, 0x06,
            0xF0.toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        )
        assertFalse(ApduProtocol.isSelectAid(wrong))
    }

    @Test fun `isSelectAid rejects wrong INS byte`() {
        val wrong = byteArrayOf(
            0x00, 0xB0.toByte(), 0x04, 0x00, 0x06,
            0xF2.toByte(), 0x23, 0x34, 0x45, 0x56, 0x67, 0x00,
        )
        assertFalse(ApduProtocol.isSelectAid(wrong))
    }

    @Test fun `isSelectAid rejects short APDU`() {
        assertFalse(ApduProtocol.isSelectAid(byteArrayOf(0x00, 0xA4.toByte())))
    }

    @Test fun `isPullByte recognizes 0x02`() {
        assertTrue(ApduProtocol.isPullByte(byteArrayOf(0x02)))
    }

    @Test fun `isPullByte rejects other single bytes`() {
        assertFalse(ApduProtocol.isPullByte(byteArrayOf(0x03)))
        assertFalse(ApduProtocol.isPullByte(byteArrayOf(0x00)))
    }

    @Test fun `isPullByte rejects multi-byte arrays`() {
        assertFalse(ApduProtocol.isPullByte(byteArrayOf(0x02, 0x00)))
    }
}
