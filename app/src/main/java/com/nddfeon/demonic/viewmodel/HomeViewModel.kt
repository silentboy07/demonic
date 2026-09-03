package com.nddfeon.demonic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nddfeon.demonic.data.model.UserAccount
import com.nddfeon.demonic.data.repository.AuthRepository
import com.nddfeon.demonic.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val roomCodeInput: String = "",
    val isLoading: Boolean = false,
    val isCreatingRoom: Boolean = false,
    val isJoiningRoom: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    val currentUser: StateFlow<UserAccount?> = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updateRoomCodeInput(input: String) {
        val filtered = input.uppercase().filter { it.isLetterOrDigit() }.take(6)
        _uiState.value = _uiState.value.copy(roomCodeInput = filtered, errorMessage = null)
    }

    fun createRoom(onRoomCreated: (String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingRoom = true, errorMessage = null)
            val result = roomRepository.createRoom(user)
            result.onSuccess { code ->
                _uiState.value = _uiState.value.copy(isCreatingRoom = false)
                onRoomCreated(code)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isCreatingRoom = false,
                    errorMessage = error.localizedMessage ?: "Failed to create room"
                )
            }
        }
    }

    fun joinRoom(code: String, onRoomJoined: (String) -> Unit) {
        val user = currentUser.value ?: return
        val trimmed = code.trim().uppercase()
        if (trimmed.length != 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Room code must be 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isJoiningRoom = true, errorMessage = null)
            val result = roomRepository.joinRoom(trimmed, user)
            result.onSuccess { room ->
                _uiState.value = _uiState.value.copy(isJoiningRoom = false)
                onRoomJoined(room.roomCode)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isJoiningRoom = false,
                    errorMessage = error.localizedMessage ?: "Failed to join room"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
