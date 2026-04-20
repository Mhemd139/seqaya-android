package com.seqaya.app.data.remote.dto

import com.seqaya.app.domain.model.Device
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class DeviceDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val serial: String,
    val nickname: String? = null,
    @SerialName("plant_id") val plantId: String? = null,
    @SerialName("target_moisture_percent") val targetMoisturePercent: Int? = null,
    @SerialName("hold_mode_active") val holdModeActive: Boolean = false,
    @SerialName("registered_at") val registeredAt: String,
) {
    fun toDomain(): Device = Device(
        id = id,
        serial = serial,
        nickname = nickname,
        plantCommonName = null,
        plantScientificName = null,
        targetMoisturePercent = targetMoisturePercent,
        holdModeActive = holdModeActive,
        registeredAt = Instant.parse(registeredAt),
    )
}
