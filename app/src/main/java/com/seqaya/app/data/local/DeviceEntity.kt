package com.seqaya.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val serial: String,
    val nickname: String?,
    val targetMoisturePercent: Int?,
    val holdModeActive: Boolean,
    val registeredAtEpochMs: Long,
)

@Entity(tableName = "readings", primaryKeys = ["deviceSerial", "recordedAtEpochMs"])
data class ReadingEntity(
    val deviceSerial: String,
    val soilMoisturePercent: Int,
    val isValveOpen: Boolean,
    val isWateringPaused: Boolean,
    val recordedAtEpochMs: Long,
)
