package com.seqaya.app.data.repository

import android.util.Log
import com.seqaya.app.data.local.ReadingDao
import com.seqaya.app.data.local.ReadingEntity
import com.seqaya.app.data.remote.dto.DeviceReadingDto
import com.seqaya.app.domain.model.Reading
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Instant

private const val TAG = "ReadingRepository"
private val READING_COLUMNS = Columns.list(
    "id", "device_serial", "soil_moisture_percent",
    "is_valve_open", "is_watering_paused", "recorded_at",
)

class ReadingRepository(
    private val supabase: SupabaseClient,
    private val dao: ReadingDao,
) {
    fun observeLatest(): Flow<List<Reading>> =
        dao.observeLatestPerDevice().map { list -> list.map { it.toDomain() } }

    fun observeRecent(serial: String, sinceEpochMs: Long): Flow<List<Reading>> =
        dao.observeRecent(serial, sinceEpochMs).map { list -> list.map { it.toDomain() } }

    suspend fun refreshLatestFor(serials: List<String>, perDeviceLimit: Int = 48): Result<Unit> = runCatching {
        if (serials.isEmpty()) return@runCatching
        val remote = supabase.from("device_readings")
            .select(columns = READING_COLUMNS) {
                filter { isIn("device_serial", serials) }
                order("recorded_at", Order.DESCENDING)
                limit((serials.size * perDeviceLimit).toLong())
            }
            .decodeList<DeviceReadingDto>()
        dao.upsertAll(remote.map { it.toEntity() })
    }.onFailure { Log.e(TAG, "Refresh readings failed", it) }

    fun subscribe(scope: CoroutineScope, serials: List<String>) {
        if (serials.isEmpty()) return
        val channel = supabase.channel("device_readings:${serials.joinToString(",")}")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "device_readings"
        }
        scope.launch {
            changes.collect { action ->
                when (action) {
                    is PostgresAction.Insert -> persist(action.record)
                    is PostgresAction.Update -> persist(action.record)
                    else -> Unit
                }
            }
        }
        scope.launch { runCatching { channel.subscribe(blockUntilSubscribed = true) } }
    }

    private suspend fun persist(record: kotlinx.serialization.json.JsonObject) {
        val dto = runCatching {
            Json { ignoreUnknownKeys = true; coerceInputValues = true }
                .decodeFromJsonElement(DeviceReadingDto.serializer(), record)
        }.getOrElse {
            Log.w(TAG, "Failed to decode realtime reading", it)
            return
        }
        dao.upsertAll(listOf(dto.toEntity()))
    }

    private fun ReadingEntity.toDomain(): Reading = Reading(
        deviceSerial = deviceSerial,
        soilMoisturePercent = soilMoisturePercent,
        isValveOpen = isValveOpen,
        isWateringPaused = isWateringPaused,
        recordedAt = Instant.ofEpochMilli(recordedAtEpochMs),
    )

    private fun DeviceReadingDto.toEntity(): ReadingEntity = ReadingEntity(
        deviceSerial = deviceSerial,
        soilMoisturePercent = soilMoisturePercent,
        isValveOpen = isValveOpen,
        isWateringPaused = isWateringPaused ?: false,
        recordedAtEpochMs = recordedAt?.let { Instant.parse(it).toEpochMilli() } ?: 0L,
    )
}
