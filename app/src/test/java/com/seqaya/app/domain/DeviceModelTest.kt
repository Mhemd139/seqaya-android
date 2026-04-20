package com.seqaya.app.domain

import com.seqaya.app.domain.model.Device
import com.seqaya.app.domain.model.DeviceWithReading
import com.seqaya.app.domain.model.Reading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DeviceModelTest {

    private fun device(
        id: String = "d1",
        serial: String = "1",
        nickname: String? = null,
        plant: String? = null,
        target: Int? = 60,
    ) = Device(
        id = id,
        serial = serial,
        nickname = nickname,
        plantCommonName = plant,
        plantScientificName = null,
        targetMoisturePercent = target,
        holdModeActive = false,
        registeredAt = Instant.EPOCH,
    )

    private fun reading(percent: Int) = Reading(
        deviceSerial = "1",
        soilMoisturePercent = percent,
        isValveOpen = false,
        isWateringPaused = false,
        recordedAt = Instant.EPOCH,
    )

    @Test fun `display name prefers nickname over plant name over serial`() {
        assertEquals("Lucy", with("Lucy", "Pothos", "1").displayName)
        assertEquals("Pothos", with(null, "Pothos", "1").displayName)
        assertEquals("1", with(null, null, "1").displayName)
    }

    @Test fun `needsAttention triggers when moisture is 10 or more below target`() {
        val d = device(target = 60)
        assertFalse(DeviceWithReading(d, reading(55), emptyList(), null).needsAttention)
        assertFalse(DeviceWithReading(d, reading(51), emptyList(), null).needsAttention)
        assertTrue(DeviceWithReading(d, reading(50), emptyList(), null).needsAttention)
        assertTrue(DeviceWithReading(d, reading(49), emptyList(), null).needsAttention)
    }

    @Test fun `needsAttention stays false when no reading`() {
        assertFalse(DeviceWithReading(device(), null, emptyList(), null).needsAttention)
    }

    @Test fun `needsAttention stays false when target unknown`() {
        val d = device(target = null)
        assertFalse(DeviceWithReading(d, reading(10), emptyList(), null).needsAttention)
    }

    private fun with(nickname: String?, plant: String?, serial: String) = DeviceWithReading(
        device = device(nickname = nickname, plant = plant, serial = serial),
        latest = null,
        recentMoisture = emptyList(),
        lastWateredAt = null,
    )
}
