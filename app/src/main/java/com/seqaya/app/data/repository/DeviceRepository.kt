package com.seqaya.app.data.repository

import android.util.Log
import com.seqaya.app.data.local.DeviceDao
import com.seqaya.app.data.local.DeviceEntity
import com.seqaya.app.data.remote.dto.DeviceDto
import com.seqaya.app.domain.model.Device
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

    suspend fun delete(id: String): Result<Unit> = runCatching {
        supabase.from("devices").delete { filter { eq("id", id) } }
        dao.deleteById(id)
    }.onFailure { Log.e(TAG, "Delete device failed", it) }

    suspend fun updateNickname(id: String, nickname: String): Result<Unit> = runCatching {
        val value = nickname.trim().ifEmpty { null }
        supabase.from("devices").update({ set("nickname", value) }) { filter { eq("id", id) } }
        dao.updateNickname(id, value)
    }.onFailure { Log.e(TAG, "Update nickname failed", it) }

    suspend fun updateTarget(id: String, percent: Int): Result<Unit> = runCatching {
        val clamped = percent.coerceIn(10, 90)
        supabase.from("devices").update({ set("target_moisture_percent", clamped) }) { filter { eq("id", id) } }
        dao.updateTarget(id, clamped)
    }.onFailure { Log.e(TAG, "Update target failed", it) }

    /**
     * Insert a new paired device. Called from the Add Device wizard AFTER the NFC
     * tap succeeds — the serial has already been written to the firmware's NVS at
     * that point, so the Supabase row is the canonical record the firmware will
     * write its future `device_readings` against.
     *
     * Returns the freshly-inserted [Device] (with Supabase-generated id) so the
     * caller can navigate straight to it.
     */
    suspend fun addDevice(
        serial: String,
        nickname: String?,
        plantId: String?,
        targetMoisturePercent: Int,
    ): Result<Device> = runCatching {
        val ownerId = supabase.auth.currentUserOrNull()?.id
            ?: error("Not signed in — cannot pair device")
        val insert = NewDevicePayload(
            serial = serial,
            ownerId = ownerId,
            nickname = nickname,
            plantId = plantId.takeUnless { it.isNullOrBlank() || it.startsWith("default-") },
            targetMoisturePercent = targetMoisturePercent,
        )
        val dto = supabase.from("devices")
            .insert(insert) { select(columns = DEVICE_COLUMNS) }
            .decodeSingle<com.seqaya.app.data.remote.dto.DeviceDto>()
        val entity = DeviceEntity(
            id = dto.id,
            serial = dto.serial,
            nickname = dto.nickname,
            targetMoisturePercent = dto.targetMoisturePercent,
            holdModeActive = dto.holdModeActive,
            registeredAtEpochMs = Instant.parse(dto.registeredAt).toEpochMilli(),
        )
        dao.upsertAll(listOf(entity))
        dto.toDomain()
    }.onFailure { Log.e(TAG, "Add device failed", it) }

    @Serializable
    private data class NewDevicePayload(
        val serial: String,
        @SerialName("owner_id") val ownerId: String,
        val nickname: String? = null,
        @SerialName("plant_id") val plantId: String? = null,
        @SerialName("target_moisture_percent") val targetMoisturePercent: Int,
    )

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
