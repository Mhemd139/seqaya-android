package com.seqaya.app.debug

import android.util.Log
import com.seqaya.app.data.repository.DeviceRepository
import com.seqaya.app.data.repository.ReadingRepository
import com.seqaya.app.nfc.ApduProtocol
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin
import kotlin.random.Random

/**
 * Inserts a fake device + 48 hours of synthetic readings into Supabase so the app
 * can be exercised without real hardware. Gate all callers on `BuildConfig.DEBUG`
 * — never reference this from release-shipped UI.
 */
@Singleton
class DebugSeeder @Inject constructor(
    private val supabase: SupabaseClient,
    private val deviceRepository: DeviceRepository,
    private val readingRepository: ReadingRepository,
) {

    suspend fun seedMockDevice(): Result<String> = runCatching {
        // Match the production serial shape (SQ-{8hex}) so anything that validates
        // serial format later won't reject mock devices. The full 8 hex gives us
        // 2^32 namespace too (vs 2^16 with the old SQ-MOCK%04X).
        val requestedSerial = ApduProtocol.generateSerial()
        val device = deviceRepository.addDevice(
            serial = requestedSerial,
            nickname = "Lucy (mock)",
            plantId = null,
            targetMoisturePercent = TARGET,
        ).getOrThrow()
        // Supabase generates `id` but respects our `serial`; read back the canonical value.
        val serial = device.serial

        val now = Instant.now()
        val readings = (0 until READINGS_COUNT).map { i ->
            MockReadingPayload(
                deviceSerial = serial,
                soilMoisturePercent = syntheticMoisture(i),
                isValveOpen = i % VALVE_PERIOD_MIN in 0..2,
                isWateringPaused = false,
                recordedAt = now.minusSeconds(i.toLong() * 60L).toString(),
            )
        }
        readings.chunked(BATCH_SIZE).forEach { batch ->
            supabase.from("device_readings").insert(batch)
        }
        readingRepository.refreshWindow(
            serial = serial,
            sinceEpochMs = now.minusSeconds(2L * 24 * 3600).toEpochMilli(),
        ).getOrThrow()
        Log.i(TAG, "Seeded $serial with $READINGS_COUNT readings")
        serial
    }.onFailure { Log.e(TAG, "Seed mock device failed", it) }

    private fun syntheticMoisture(minutesAgo: Int): Int {
        val base = 50.0 + 18.0 * sin(minutesAgo / 180.0)
        val noise = Random.nextDouble(-2.0, 2.0)
        return (base + noise).toInt().coerceIn(0, 100)
    }

    @Serializable
    private data class MockReadingPayload(
        @SerialName("device_serial") val deviceSerial: String,
        @SerialName("soil_moisture_percent") val soilMoisturePercent: Int,
        @SerialName("is_valve_open") val isValveOpen: Boolean,
        @SerialName("is_watering_paused") val isWateringPaused: Boolean,
        @SerialName("recorded_at") val recordedAt: String,
    )

    private companion object {
        const val TAG = "DebugSeeder"
        const val TARGET = 55
        const val READINGS_COUNT = 2880 // 48 h at 1 reading/min
        const val BATCH_SIZE = 500
        const val VALVE_PERIOD_MIN = 180 // valve opens briefly every ~3 h
    }
}
