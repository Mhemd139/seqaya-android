package com.seqaya.app.ui.signin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seqaya.app.data.repository.AuthRepository
import com.seqaya.app.data.repository.SignInOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignInUiState(
    val inFlight: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun signIn(activityContext: Context) {
        if (_state.value.inFlight) return
        _state.value = SignInUiState(inFlight = true)
        viewModelScope.launch {
            when (val outcome = authRepository.signInWithGoogle(activityContext)) {
                SignInOutcome.Success ->
                    _state.value = SignInUiState(inFlight = false)
                SignInOutcome.Cancelled ->
                    _state.value = SignInUiState(inFlight = false)
                is SignInOutcome.Failure ->
                    _state.value = SignInUiState(inFlight = false, errorMessage = outcome.message)
            }
        }
    }

    fun dismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
