package com.seqaya.app.data.remote

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

private const val TAG = "DeleteAccountService"
private const val FUNCTION_NAME = "delete-account"

class DeleteAccountService(
    private val supabase: SupabaseClient,
) {
    suspend fun deleteAccount(): Result<Unit> = runCatching {
        val response: HttpResponse = supabase.functions.invoke(FUNCTION_NAME) {
            method = HttpMethod.Post
        }
        val body = runCatching { response.body<DeleteAccountResponse>() }.getOrNull()
        if (!response.status.isSuccess() || body?.ok != true) {
            error(body?.message ?: "Couldn't delete your account. Try again or contact Seqaya.io@gmail.com.")
        }
    }.onFailure { Log.e(TAG, "deleteAccount failed", it) }

    @Serializable
    private data class DeleteAccountResponse(
        val ok: Boolean = false,
        val message: String? = null,
    )
}
