package com.seqaya.app.data.repository

import android.util.Log
import com.seqaya.app.data.remote.dto.PlantDto
import com.seqaya.app.domain.model.DefaultPlants
import com.seqaya.app.domain.model.Plant
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

private const val TAG = "PlantRepository"
private val PLANT_COLUMNS = Columns.list(
    "id", "scientific_name", "common_name", "moisture_target",
    "illustration_key", "image_url",
)

/**
 * Lightweight Supabase-only repository for the plants catalog.
 *
 * Plants are a small static-ish set (~6 defaults for MVP), so we don't cache
 * them in Room — the Add Device wizard calls [fetchAll] once per session and
 * holds the result in VM state. If Supabase is empty or unreachable, we fall
 * back to the hardcoded [DefaultPlants] list so the wizard still works offline
 * and on fresh installs where the seed hasn't been applied yet.
 */
class PlantRepository(
    private val supabase: SupabaseClient,
) {
    suspend fun fetchAll(): List<Plant> = runCatching {
        val remote = supabase.from("plants")
            .select(columns = PLANT_COLUMNS) { limit(100) }
            .decodeList<PlantDto>()
        if (remote.isEmpty()) {
            Log.w(TAG, "Supabase plants table is empty — using bundled defaults")
            DefaultPlants
        } else {
            remote.map { it.toDomain() }
        }
    }.getOrElse {
        Log.e(TAG, "fetchAll failed, falling back to defaults", it)
        DefaultPlants
    }
}
