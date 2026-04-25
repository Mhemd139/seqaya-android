package com.seqaya.app.di

import android.content.Context
import com.seqaya.app.data.remote.ConnectivityObserver
import com.seqaya.app.data.remote.DeleteAccountService
import com.seqaya.app.data.remote.SupabaseClientProvider
import com.seqaya.app.data.repository.AuthRepository
import com.seqaya.app.data.repository.DeviceRepository
import com.seqaya.app.data.repository.PlantRepository
import com.seqaya.app.data.repository.ReadingRepository
import com.seqaya.app.data.local.DeviceDao
import com.seqaya.app.data.local.ReadingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = SupabaseClientProvider.create()

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver =
        ConnectivityObserver(context)

    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        supabase: SupabaseClient,
    ): AuthRepository = AuthRepository(context, supabase)

    @Provides
    @Singleton
    fun provideDeviceRepository(
        supabase: SupabaseClient,
        dao: DeviceDao,
        readingDao: ReadingDao,
    ): DeviceRepository = DeviceRepository(supabase, dao, readingDao)

    @Provides
    @Singleton
    fun provideReadingRepository(supabase: SupabaseClient, dao: ReadingDao): ReadingRepository =
        ReadingRepository(supabase, dao)

    @Provides
    @Singleton
    fun providePlantRepository(supabase: SupabaseClient): PlantRepository =
        PlantRepository(supabase)

    @Provides
    @Singleton
    fun provideDeleteAccountService(supabase: SupabaseClient): DeleteAccountService =
        DeleteAccountService(supabase)
}
