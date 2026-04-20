package com.seqaya.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceReadingDto(
    val id: Long,
    @SerialName("device_serial") val deviceSerial: String,
    @SerialName("soil_moisture_percent") val soilMoisturePercent: Int,
    @SerialName("is_valve_open") val isValveOpen: Boolean = false,
    @SerialName("is_watering_paused") val isWateringPaused: Boolean? = null,
    @SerialName("recorded_at") val recordedAt: String,
)
