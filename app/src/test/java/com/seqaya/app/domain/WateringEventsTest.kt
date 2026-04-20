package com.seqaya.app.domain

import com.seqaya.app.domain.model.Reading
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class WateringEventsTest {

    private fun reading(seconds: Long, valveOpen: Boolean): Reading = Reading(
        deviceSerial = "S1",
        soilMoisturePercent = 50,
        isValveOpen = valveOpen,
        isWateringPaused = false,
        recordedAt = Instant.ofEpochSecond(seconds),
    )

    @Test
    fun `empty list returns empty list`() {
        assertEquals(emptyList<Instant>(), wateringEvents(emptyList()))
    }

    @Test
    fun `single reading with valve closed returns empty`() {
        assertEquals(emptyList<Instant>(), wateringEvents(listOf(reading(1, false))))
    }

    @Test
    fun `single reading with valve open returns empty`() {
        assertEquals(emptyList<Instant>(), wateringEvents(listOf(reading(1, true))))
    }

    @Test
    fun `false then true returns one event at second reading`() {
        val readings = listOf(reading(1, false), reading(2, true))
        assertEquals(listOf(Instant.ofEpochSecond(2)), wateringEvents(readings))
    }

    @Test
    fun `true then true returns empty`() {
        val readings = listOf(reading(1, true), reading(2, true))
        assertEquals(emptyList<Instant>(), wateringEvents(readings))
    }

    @Test
    fun `false then true then true returns one event at first rising edge`() {
        val readings = listOf(reading(1, false), reading(2, true), reading(3, true))
        assertEquals(listOf(Instant.ofEpochSecond(2)), wateringEvents(readings))
    }

    @Test
    fun `false true false true returns two events at rising edges`() {
        val readings = listOf(
            reading(1, false),
            reading(2, true),
            reading(3, false),
            reading(4, true),
        )
        assertEquals(
            listOf(Instant.ofEpochSecond(2), Instant.ofEpochSecond(4)),
            wateringEvents(readings),
        )
    }
}
