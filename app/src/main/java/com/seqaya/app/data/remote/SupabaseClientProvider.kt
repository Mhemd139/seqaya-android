package com.seqaya.app.data.remote

import com.seqaya.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClientProvider {
    fun create(): SupabaseClient {
        check(BuildConfig.SUPABASE_URL.isNotBlank()) {
            "SUPABASE_URL missing — copy local.properties.example to local.properties and fill it in."
        }
        check(BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            "SUPABASE_ANON_KEY missing — copy local.properties.example to local.properties and fill it in."
        }
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
