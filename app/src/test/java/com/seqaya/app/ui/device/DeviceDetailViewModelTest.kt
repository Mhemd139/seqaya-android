package com.seqaya.app.ui.device

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.seqaya.app.data.repository.DeviceRepository
import com.seqaya.app.data.repository.ReadingRepository
import com.seqaya.app.domain.model.Device
import com.seqaya.app.domain.model.Reading
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val deviceRepo: DeviceRepository = mockk(relaxed = true)
    private val readingRepo: ReadingRepository = mockk(relaxed = true)

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vm(serial: String = "SQ-1"): DeviceDetailViewModel {
        val handle = SavedStateHandle(mapOf("serial" to serial))
        coEvery { deviceRepo.observeDevices() } returns flowOf(listOf(testDevice(serial)))
        coEvery { readingRepo.observeRecent(any(), any()) } returns flowOf(emptyList())
        coEvery { readingRepo.refreshWindow(any(), any(), any()) } returns Result.success(Unit)
        return DeviceDetailViewModel(deviceRepo, readingRepo, handle)
    }

    @Test fun `default range is DAYS_30`() = runTest(dispatcher) {
        val sut = vm()
        sut.state.test {
            assertEquals(ChartRange.DAYS_30, awaitItem().range)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `setRange updates state and refreshes window`() = runTest(dispatcher) {
        val sut = vm()
        backgroundScope.launch { sut.state.collect {} }
        sut.setRange(ChartRange.HOURS_24)
        advanceUntilIdle()
        assertEquals(ChartRange.HOURS_24, sut.state.value.range)
        coVerify(atLeast = 1) { readingRepo.refreshWindow("SQ-1", any(), any()) }
    }

    @Test fun `deleteDevice success flips shouldNavigateBack`() = runTest(dispatcher) {
        coEvery { deviceRepo.delete(any()) } returns Result.success(Unit)
        val sut = vm()
        backgroundScope.launch { sut.state.collect {} }
        advanceUntilIdle()
        assertFalse(sut.state.value.shouldNavigateBack)
        sut.deleteDevice()
        advanceUntilIdle()
        assertTrue(sut.state.value.shouldNavigateBack)
    }

    @Test fun `deleteDevice failure surfaces error and keeps user on screen`() = runTest(dispatcher) {
        coEvery { deviceRepo.delete(any()) } returns Result.failure(RuntimeException("boom"))
        val sut = vm()
        backgroundScope.launch { sut.state.collect {} }
        advanceUntilIdle()
        sut.deleteDevice()
        advanceUntilIdle()
        assertFalse(sut.state.value.shouldNavigateBack)
        assertEquals("Couldn't remove device.", sut.state.value.error)
    }

    @Test fun `renameDevice with blank does not call repo`() = runTest(dispatcher) {
        val sut = vm()
        advanceUntilIdle()
        sut.renameDevice("   ")
        advanceUntilIdle()
        coVerify(exactly = 0) { deviceRepo.updateNickname(any(), any()) }
    }

    @Test fun `renameDevice success emits Renamed event`() = runTest(dispatcher) {
        coEvery { deviceRepo.updateNickname(any(), any()) } returns Result.success(Unit)
        val sut = vm()
        advanceUntilIdle()
        sut.events.test {
            sut.renameDevice("Figgy")
            advanceUntilIdle()
            assertEquals(DeviceDetailEvent.Renamed, awaitItem())
        }
    }

    @Test fun `updateTarget success emits TargetUpdated event`() = runTest(dispatcher) {
        coEvery { deviceRepo.updateTarget(any(), any()) } returns Result.success(Unit)
        val sut = vm()
        advanceUntilIdle()
        sut.events.test {
            sut.updateTarget(55)
            advanceUntilIdle()
            assertEquals(DeviceDetailEvent.TargetUpdated, awaitItem())
        }
    }

    @Test fun `dismissError clears error state`() = runTest(dispatcher) {
        coEvery { deviceRepo.delete(any()) } returns Result.failure(RuntimeException("boom"))
        val sut = vm()
        backgroundScope.launch { sut.state.collect {} }
        advanceUntilIdle()
        sut.deleteDevice()
        advanceUntilIdle()
        assertEquals("Couldn't remove device.", sut.state.value.error)
        sut.dismissError()
        advanceUntilIdle()
        assertNull(sut.state.value.error)
    }

    @Test fun `windowStart is now minus range duration`() {
        val now = 1_700_000_000_000L
        assertEquals(
            now - ChartRange.DAYS_7.durationMs,
            DeviceDetailUiState.windowStart(ChartRange.DAYS_7, now),
        )
    }

    private fun testDevice(serial: String): Device = Device(
        id = "id-$serial",
        serial = serial,
        nickname = "Test",
        plantCommonName = null,
        plantScientificName = null,
        targetMoisturePercent = 60,
        holdModeActive = false,
        registeredAt = Instant.EPOCH,
    )
}
