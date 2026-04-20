package com.seqaya.app.di

import android.content.Context
import androidx.room.Room
import com.seqaya.app.data.local.DeviceDao
import com.seqaya.app.data.local.ReadingDao
import com.seqaya.app.data.local.SeqayaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SeqayaDatabase =
        Room.databaseBuilder(context, SeqayaDatabase::class.java, "seqaya.db").build()

    @Provides
    fun provideDeviceDao(db: SeqayaDatabase): DeviceDao = db.deviceDao()

    @Provides
    fun provideReadingDao(db: SeqayaDatabase): ReadingDao = db.readingDao()
}
