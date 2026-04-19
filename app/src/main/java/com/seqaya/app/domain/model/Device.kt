package com.seqaya.app.domain.model

import java.time.Instant

data class Device(
    val id: String,
    val serial: String,
    val nickname: String?,
    val plantCommonName: String?,
    val plantScientificName: String?,
    val targetMoisturePercent: Int?,
    val holdModeActive: Boolean,
    val registeredAt: Instant,
)

data class Reading(
    val deviceSerial: String,
    val soilMoisturePercent: Int,
    val isValveOpen: Boolean,
    val isWateringPaused: Boolean,
    val recordedAt: Instant,
)

data class DeviceWithReading(
    val device: Device,
    val latest: Reading?,
    val recentMoisture: List<Int>,
    val lastWateredAt: Instant?,
) {
    val displayName: String
        get() = device.nickname ?: device.plantCommonName ?: device.serial

    val needsAttention: Boolean
        get() = latest != null &&
            device.targetMoisturePercent != null &&
            latest.soilMoisturePercent < device.targetMoisturePercent - 10
}
