package com.seqaya.app.nfc

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProvisioningSessionTest {

    private val locate = ApduProtocol.Command.Locate
    private val add = ApduProtocol.Command.Add(
        ssid = "W", password = "P", userId = "U",
        serial = "SQ-A3F72B91", targetMoisture = 60, holdMode = false,
    )

    @Test fun `initial state is Idle and isActive is false`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        assertTrue(sut.status.value is ProvisioningSession.Status.Idle)
        assertEquals(false, sut.isActive)
    }

    @Test fun `arm transitions to ReadyToTap and isActive becomes true`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        sut.arm(locate)
        assertTrue(sut.status.value is ProvisioningSession.Status.ReadyToTap)
        assertEquals(true, sut.isActive)
    }

    @Test fun `onSelectAid while Idle returns false and stays Idle`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        assertEquals(false, sut.onSelectAid())
        assertTrue(sut.status.value is ProvisioningSession.Status.Idle)
    }

    @Test fun `onSelectAid while ReadyToTap transitions to Transferring and returns true`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        sut.arm(locate)
        assertEquals(true, sut.onSelectAid())
        assertTrue(sut.status.value is ProvisioningSession.Status.Transferring)
    }

    @Test fun `nextChunk returns first chunk then terminates for single-chunk payload`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        sut.arm(locate)
        sut.onSelectAid()

        // Locate encodes to `{"c":"L"}` — 9 bytes → one 8-byte chunk + one 1-byte terminal.
        // Drain every chunk and reassemble; the reconstructed payload must parse as JSON.
        val chunks = generateSequence { sut.nextChunk() }.toList()
        val payload = chunks.fold(ByteArray(0)) { acc, c -> acc + c.drop(2).toByteArray() }
        assertEquals("""{"c":"L"}""", payload.toString(Charsets.UTF_8))

        // Last chunk is terminal (90 00)
        assertEquals(0x90.toByte(), chunks.last()[0])
        assertEquals(0x00.toByte(), chunks.last()[1])

        assertTrue(sut.status.value is ProvisioningSession.Status.Transferred)
        assertEquals(null, sut.nextChunk())
    }

    @Test fun `nextChunk walks through non-terminal then terminal for multi-chunk payload`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        sut.arm(add)
        sut.onSelectAid()

        val chunks = mutableListOf<ByteArray>()
        while (true) {
            val next = sut.nextChunk() ?: break
            chunks += next
        }

        assertTrue("expected >= 2 chunks, got ${chunks.size}", chunks.size >= 2)
        // All except last chunk should have 00 00 prefix
        chunks.dropLast(1).forEach {
            assertEquals("non-terminal status", 0x00.toByte(), it[0])
            assertEquals("non-terminal status", 0x00.toByte(), it[1])
        }
        // Last chunk should have 90 00 prefix
        val last = chunks.last()
        assertEquals(0x90.toByte(), last[0])
        assertEquals(0x00.toByte(), last[1])

        // Session is in Transferred state after last chunk
        assertTrue(sut.status.value is ProvisioningSession.Status.Transferred)
    }

    @Test fun `onDeactivated during transfer transitions to Failed LinkLost`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        sut.arm(add)
        sut.onSelectAid()
        sut.nextChunk() // send first chunk

        sut.onDeactivated(reason = 0) // DEACTIVATION_LINK_LOSS = 0

        val status = sut.status.value
        assertTrue("got $status", status is ProvisioningSession.Status.Failed)
        assertEquals(
            ProvisioningSession.FailureReason.LinkLost,
            (status as ProvisioningSession.Status.Failed).reason,
        )
    }

    @Test fun `onDeactivated after Transferred stays in Transferred`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        sut.arm(locate)
        sut.onSelectAid()
        while (sut.nextChunk() != null) { /* drain */ }
        assertTrue(sut.status.value is ProvisioningSession.Status.Transferred)

        sut.onDeactivated(reason = 1) // DEACTIVATION_DESELECTED = 1
        // Clean deselect after transfer is fine — keep Transferred
        assertTrue(sut.status.value is ProvisioningSession.Status.Transferred)
    }

    @Test fun `confirmSuccess transitions Transferred to Success`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        sut.arm(locate)
        sut.onSelectAid()
        while (sut.nextChunk() != null) { /* drain */ }

        sut.confirmSuccess()
        assertTrue(sut.status.value is ProvisioningSession.Status.Success)
    }

    @Test fun `confirmSuccess from non-Transferred is a no-op`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        sut.arm(locate)
        sut.confirmSuccess()
        assertTrue(
            "should not advance from ReadyToTap",
            sut.status.value is ProvisioningSession.Status.ReadyToTap,
        )
    }

    @Test fun `timeout fires when no SELECT AID arrives within window`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val sut = ProvisioningSession(dispatcher = dispatcher, timeoutMs = 30_000)
        sut.status.test {
            assertTrue(awaitItem() is ProvisioningSession.Status.Idle)
            sut.arm(locate)
            assertTrue(awaitItem() is ProvisioningSession.Status.ReadyToTap)
            advanceTimeBy(30_001)
            val timedOut = awaitItem()
            assertTrue("got $timedOut", timedOut is ProvisioningSession.Status.Failed)
            assertEquals(
                ProvisioningSession.FailureReason.Timeout,
                (timedOut as ProvisioningSession.Status.Failed).reason,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `SELECT AID re-arms the timeout clock`() = runTest {
        // Arm, wait 10s, receive SELECT AID (timer restarts), wait 29.9s more.
        // Total elapsed = 39.9s; only 29.9s since the re-arm so the timer hasn't fired.
        // Verifying: if the timer had NOT been re-armed, the original 30s one would have
        // fired at virtual time 30s and we'd be Failed(Timeout) here.
        val dispatcher = StandardTestDispatcher(testScheduler)
        val sut = ProvisioningSession(dispatcher = dispatcher, timeoutMs = 30_000)
        sut.arm(locate)
        advanceTimeBy(10_000)
        sut.onSelectAid()
        advanceTimeBy(29_900)

        assertTrue(
            "expected Transferring, got ${sut.status.value}",
            sut.status.value is ProvisioningSession.Status.Transferring,
        )
    }

    @Test fun `Transferring state also times out if firmware stops polling`() = runTest {
        // Firmware sent SELECT AID but then stalled without polling a single chunk.
        // The session must not wedge — it should surface Failed(Timeout) so the UI
        // can offer a retry.
        val dispatcher = StandardTestDispatcher(testScheduler)
        val sut = ProvisioningSession(dispatcher = dispatcher, timeoutMs = 30_000)
        sut.status.test {
            assertTrue(awaitItem() is ProvisioningSession.Status.Idle)
            sut.arm(locate)
            assertTrue(awaitItem() is ProvisioningSession.Status.ReadyToTap)
            sut.onSelectAid()
            assertTrue(awaitItem() is ProvisioningSession.Status.Transferring)
            advanceTimeBy(30_001)
            val timedOut = awaitItem()
            assertTrue("got $timedOut", timedOut is ProvisioningSession.Status.Failed)
            assertEquals(
                ProvisioningSession.FailureReason.Timeout,
                (timedOut as ProvisioningSession.Status.Failed).reason,
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `dismiss returns to Idle from any state`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        sut.arm(locate)
        sut.onSelectAid()
        sut.dismiss()
        assertTrue(sut.status.value is ProvisioningSession.Status.Idle)
        assertEquals(false, sut.isActive)
    }

    @Test fun `reArming is allowed and resets state`() = runTest {
        val sut = ProvisioningSession(dispatcher = StandardTestDispatcher(testScheduler))
        sut.arm(add)
        sut.onSelectAid()
        sut.nextChunk()

        // Re-arm with a different command
        sut.arm(locate)
        assertTrue(sut.status.value is ProvisioningSession.Status.ReadyToTap)
        val s = sut.status.value as ProvisioningSession.Status.ReadyToTap
        assertEquals('L', s.command.letter)
    }
}
