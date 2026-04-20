package com.seqaya.app.data.repository

import android.util.Log
import com.seqaya.app.data.local.DeviceDao
import com.seqaya.app.data.local.DeviceEntity
import com.seqaya.app.data.remote.dto.DeviceDto
import com.seqaya.app.domain.model.Device
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

private const val TAG = "DeviceRepository"
private val DEVICE_COLUMNS = Columns.list(
    "id", "owner_id", "serial", "nickname",
    "plant_id", "target_moisture_percent", "hold_mode_active", "registered_at",
)

class DeviceRepository(
    private val supabase: SupabaseClient,
    private val dao: DeviceDao,
) {
    fun observeDevices(): Flow<List<Device>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun serialsSnapshot(): List<String> = dao.getAllSerials()

    suspend fun refresh(): Result<Unit> = runCatching {
        val remote = supabase.from("devices")
            .select(columns = DEVICE_COLUMNS)
            .decodeList<DeviceDto>()
        dao.replaceAll(remote.map { it.toEntity() })
    }.onFailure { Log.e(TAG, "Refresh failed", it) }

    suspend fun clear() {
        dao.clear()
    }

    private fun DeviceEntity.toDomain(): Device = Device(
        id = id,
        serial = serial,
        nickname = nickname,
        plantCommonName = null,
        plantScientificName = null,
        targetMoisturePercent = targetMoisturePercent,
        holdModeActive = holdModeActive,
        registeredAt = Instant.ofEpochMilli(registeredAtEpochMs),
    )

    private fun DeviceDto.toEntity(): DeviceEntity = DeviceEntity(
        id = id,
        serial = serial,
        nickname = nickname,
        targetMoisturePercent = targetMoisturePercent,
        holdModeActive = holdModeActive,
        registeredAtEpochMs = Instant.parse(registeredAt).toEpochMilli(),
    )
}
