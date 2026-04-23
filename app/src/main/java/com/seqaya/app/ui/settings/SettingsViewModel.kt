package com.seqaya.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seqaya.app.data.remote.DeleteAccountService
import com.seqaya.app.data.repository.AuthRepository
import com.seqaya.app.domain.model.AuthState
import com.seqaya.app.domain.model.AuthUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val user: AuthUser? = null,
    val deletion: DeletionState = DeletionState.Idle,
)

sealed interface DeletionState {
    data object Idle : DeletionState
    data object Deleting : DeletionState
    data class Error(val message: String) : DeletionState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val deleteAccountService: DeleteAccountService,
) : ViewModel() {

    private val deletionFlow = MutableStateFlow<DeletionState>(DeletionState.Idle)

    private val userFlow = authRepository.authState.map { auth ->
        (auth as? AuthState.Authenticated)?.user
    }

    val state: StateFlow<SettingsUiState> = combine(userFlow, deletionFlow) { user, deletion ->
        SettingsUiState(user = user, deletion = deletion)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }

    fun deleteAccount() {
        if (deletionFlow.value == DeletionState.Deleting) return
        deletionFlow.value = DeletionState.Deleting
        viewModelScope.launch {
            deleteAccountService.deleteAccount()
                .onSuccess {
                    authRepository.signOut()
                    // AuthState flow flips to Unauthenticated → SeqayaRoot routes to SignIn.
                }
                .onFailure { throwable ->
                    deletionFlow.value = DeletionState.Error(throwable.message.orEmpty())
                }
        }
    }

    fun dismissDeletionError() {
        if (deletionFlow.value is DeletionState.Error) {
            deletionFlow.value = DeletionState.Idle
        }
    }
}
