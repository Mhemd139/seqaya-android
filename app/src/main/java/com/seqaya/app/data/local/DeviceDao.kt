package com.seqaya.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT id, serial, nickname, targetMoisturePercent, holdModeActive, registeredAtEpochMs FROM devices ORDER BY registeredAtEpochMs ASC")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices WHERE id NOT IN (:keepIds)")
    suspend fun deleteNotIn(keepIds: List<String>)

    @Transaction
    suspend fun replaceAll(devices: List<DeviceEntity>) {
        upsertAll(devices)
        deleteNotIn(devices.map { it.id })
    }

    @Query("DELETE FROM devices")
    suspend fun clear()
}

@Dao
interface ReadingDao {
    @Query(
        """
        SELECT r.deviceSerial, r.soilMoisturePercent, r.isValveOpen, r.isWateringPaused, r.recordedAtEpochMs
        FROM readings r
        WHERE r.recordedAtEpochMs = (
            SELECT MAX(recordedAtEpochMs) FROM readings WHERE deviceSerial = r.deviceSerial
        )
        """
    )
    fun observeLatestPerDevice(): Flow<List<ReadingEntity>>

    @Query(
        """
        SELECT deviceSerial, soilMoisturePercent, isValveOpen, isWateringPaused, recordedAtEpochMs
        FROM readings
        WHERE deviceSerial = :serial AND recordedAtEpochMs >= :sinceEpochMs
        ORDER BY recordedAtEpochMs ASC
        """
    )
    fun observeRecent(serial: String, sinceEpochMs: Long): Flow<List<ReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(readings: List<ReadingEntity>)

    @Query("DELETE FROM readings WHERE deviceSerial = :serial")
    suspend fun clearForDevice(serial: String)

    @Query("DELETE FROM readings")
    suspend fun clear()
}
