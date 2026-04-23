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
private const val SAFE_ERROR_MESSAGE = "Couldn't delete your account. Try again or contact Seqaya.io@gmail.com."

class DeleteAccountService(
    private val supabase: SupabaseClient,
) {
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val response: HttpResponse = supabase.functions.invoke(FUNCTION_NAME) {
                method = HttpMethod.Post
            }
            val body = runCatching { response.body<DeleteAccountResponse>() }.getOrNull()
            if (!response.status.isSuccess() || body?.ok != true) {
                Log.e(TAG, "deleteAccount failed: status=${response.status} message=${body?.message}")
                Result.failure(Exception(SAFE_ERROR_MESSAGE))
            } else {
                Result.success(Unit)
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "deleteAccount failed", e)
            Result.failure(Exception(SAFE_ERROR_MESSAGE))
        }
    }

    @Serializable
    private data class DeleteAccountResponse(
        val ok: Boolean = false,
        val message: String? = null,
    )
}
