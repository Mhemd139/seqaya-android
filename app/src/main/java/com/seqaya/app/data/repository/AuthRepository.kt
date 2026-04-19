package com.seqaya.app.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.seqaya.app.BuildConfig
import com.seqaya.app.domain.model.AuthState
import com.seqaya.app.domain.model.AuthUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.contentOrNull

private const val TAG = "AuthRepository"

class AuthRepository(
    private val appContext: Context,
    private val supabase: SupabaseClient,
) {
    private val credentialManager = CredentialManager.create(appContext)

    val authState: Flow<AuthState> = supabase.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            is SessionStatus.Authenticated -> AuthState.Authenticated(status.session.user.toDomain())
            is SessionStatus.RefreshFailure -> AuthState.Error("Session refresh failed. Please sign in again.")
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): SignInOutcome {
        val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (clientId.isBlank()) {
            return SignInOutcome.Failure(
                "Google Web Client ID not configured. Add GOOGLE_WEB_CLIENT_ID to local.properties."
            )
        }
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        val idToken: String = try {
            val response = credentialManager.getCredential(activityContext, request)
            val credential = response.credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return SignInOutcome.Failure("Unexpected credential type: ${credential.type}")
            }
            try {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Google ID token parse failed", e)
                return SignInOutcome.Failure("Could not parse Google credentials.")
            }
        } catch (_: GetCredentialCancellationException) {
            return SignInOutcome.Cancelled
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential fetch failed", e)
            return SignInOutcome.Failure("Couldn't get Google credentials. Check your connection and try again.")
        }

        return try {
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }
            SignInOutcome.Success
        } catch (e: Exception) {
            Log.e(TAG, "Supabase sign-in failed", e)
            SignInOutcome.Failure("Sign-in failed. Please try again.")
        }
    }

    suspend fun signOut() {
        runCatching { supabase.auth.signOut() }
            .onFailure { Log.e(TAG, "Sign out failed", it) }
    }

    private fun UserInfo.toDomain(): AuthUser {
        val meta = userMetadata
        fun metaString(key: String): String? {
            val element = meta?.get(key) ?: return null
            return (element as? kotlinx.serialization.json.JsonPrimitive)?.contentOrNull
        }
        return AuthUser(
            id = id,
            email = email,
            displayName = metaString("full_name") ?: metaString("name"),
            avatarUrl = metaString("avatar_url") ?: metaString("picture"),
        )
    }
}

sealed interface SignInOutcome {
    data object Success : SignInOutcome
    data object Cancelled : SignInOutcome
    data class Failure(val message: String) : SignInOutcome
}
