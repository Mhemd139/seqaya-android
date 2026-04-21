package com.seqaya.app.nfc

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApduProtocolTest {

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

    @Test fun `Add command encodes letter prefix and six fields`() {
        val cmd = ApduProtocol.Command.Add(
            ssid = "MyWifi",
            password = "secret123",
            userId = "user-abc",
            serial = "SQ-A3F72B91",
            targetMoisture = 60,
            holdMode = false,
        )
        assertEquals(
            "A\$MyWifi\$secret123\$user-abc\$SQ-A3F72B91\$60\$0\$",
            cmd.encode().toString(Charsets.UTF_8),
        )
    }

    @Test fun `Add encodes holdMode true as 1`() {
        val cmd = ApduProtocol.Command.Add("w", "p", "u", "SQ-00000001", 55, true)
        assertEquals("A\$w\$p\$u\$SQ-00000001\$55\$1\$", cmd.encode().toString(Charsets.UTF_8))
    }

    @Test fun `Locate command encodes L-dollar only`() {
        assertEquals("L\$", ApduProtocol.Command.Locate.encode().toString(Charsets.UTF_8))
    }

    @Test fun `HoldToggle encodes H-dollar only`() {
        assertEquals("H\$", ApduProtocol.Command.HoldToggle.encode().toString(Charsets.UTF_8))
    }

    @Test fun `DryMap encodes D-dollar only`() {
        assertEquals("D\$", ApduProtocol.Command.DryMap.encode().toString(Charsets.UTF_8))
    }

    @Test fun `WetMap encodes W-dollar only`() {
        assertEquals("W\$", ApduProtocol.Command.WetMap.encode().toString(Charsets.UTF_8))
    }

    @Test fun `Reprogram keepHistory true encodes trailing 1`() {
        val cmd = ApduProtocol.Command.Reprogram(
            ssid = "w", password = "p", userId = "u",
            serial = "SQ-DEADBEEF", targetMoisture = 70, holdMode = false,
            keepHistory = true,
        )
        assertEquals(
            "R\$w\$p\$u\$SQ-DEADBEEF\$70\$0\$1\$",
            cmd.encode().toString(Charsets.UTF_8),
        )
    }

    @Test fun `Reprogram keepHistory false encodes trailing 0`() {
        val cmd = ApduProtocol.Command.Reprogram(
            "w", "p", "u", "SQ-00000002", 50, true, keepHistory = false,
        )
        assertEquals("R\$w\$p\$u\$SQ-00000002\$50\$1\$0\$", cmd.encode().toString(Charsets.UTF_8))
    }

    @Test fun `chunkResponses wraps tiny payload in one terminal chunk with 90 00`() {
        val chunks = ApduProtocol.chunkResponses("L\$".toByteArray(Charsets.UTF_8))
        assertEquals(1, chunks.size)
        assertArrayEquals(
            byteArrayOf(0x90.toByte(), 0x00, 'L'.code.toByte(), '$'.code.toByte()),
            chunks[0],
        )
    }

    @Test fun `chunkResponses splits 16-byte payload into two chunks`() {
        val payload = "0123456789ABCDEF".toByteArray(Charsets.UTF_8) // exactly 16 bytes
        val chunks = ApduProtocol.chunkResponses(payload)
        assertEquals(2, chunks.size)
        // First chunk: 00 00 + first 8 bytes
        assertArrayEquals(
            byteArrayOf(0x00, 0x00) + "01234567".toByteArray(Charsets.UTF_8),
            chunks[0],
        )
        // Second chunk (terminal): 90 00 + last 8 bytes
        assertArrayEquals(
            byteArrayOf(0x90.toByte(), 0x00) + "89ABCDEF".toByteArray(Charsets.UTF_8),
            chunks[1],
        )
    }

    @Test fun `chunkResponses 17-byte payload splits into two non-terminal plus terminal single-byte`() {
        val payload = "0123456789ABCDEFG".toByteArray(Charsets.UTF_8) // 17 bytes
        val chunks = ApduProtocol.chunkResponses(payload)
        assertEquals(3, chunks.size)
        // First two chunks non-terminal (8 bytes each), third chunk terminal with 1 byte
        assertEquals("00 00", chunks[0].take(2).joinToString(" ") { "%02X".format(it) })
        assertEquals("00 00", chunks[1].take(2).joinToString(" ") { "%02X".format(it) })
        assertEquals("90 00", chunks[2].take(2).joinToString(" ") { "%02X".format(it) })
        assertEquals(10, chunks[0].size)
        assertEquals(10, chunks[1].size)
        assertEquals(3, chunks[2].size) // 2 status + 1 payload byte
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
        // Strip 2-byte status prefix from each chunk and concatenate
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
