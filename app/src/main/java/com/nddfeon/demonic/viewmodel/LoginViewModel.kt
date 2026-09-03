package com.nddfeon.demonic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.nddfeon.demonic.data.model.UserAccount
import com.nddfeon.demonic.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<UserAccount?> = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signInWithCredential(credential: AuthCredential, onSuccess: (UserAccount) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signInWithGoogleCredential(credential)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess(user)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Google Sign-In failed"
                )
            }
        }
    }

    fun signInWithIdToken(idToken: String, onSuccess: (UserAccount) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.signInWithGoogleIdToken(idToken)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess(user)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Authentication failed"
                )
            }
        }
    }

    fun signInDemoUser(name: String = "Demon Lord", onSuccess: (UserAccount) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val uid = "mock_" + System.currentTimeMillis() % 100000
            val result = authRepository.signInWithCustomUser(uid, name, null)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess(user)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
