package com.seqaya.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DeviceEntity::class, ReadingEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SeqayaDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun readingDao(): ReadingDao
}
