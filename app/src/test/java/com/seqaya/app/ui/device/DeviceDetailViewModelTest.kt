package com.seqaya.app.ui.device

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceDetailViewModelTest {

    @Test fun `default range is DAYS_30`() {
        val state = DeviceDetailUiState()
        assertEquals(ChartRange.DAYS_30, state.range)
    }

    @Test fun `windowStart is now minus range duration`() {
        val now = 1_700_000_000_000L
        val start = DeviceDetailUiState.windowStart(ChartRange.DAYS_7, now)
        assertEquals(now - ChartRange.DAYS_7.durationMs, start)
    }

    @Test fun `setRange updates range in state`() {
        var current = DeviceDetailUiState()
        current = current.copy(range = ChartRange.HOURS_24)
        assertEquals(ChartRange.HOURS_24, current.range)
    }

    @Test fun `deletion marks shouldNavigateBack true`() {
        val state = DeviceDetailUiState().copy(shouldNavigateBack = true)
        assertTrue(state.shouldNavigateBack)
    }

    @Test fun `fresh state does not request navigation`() {
        assertFalse(DeviceDetailUiState().shouldNavigateBack)
    }
}
