package com.nddfeon.demonic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nddfeon.demonic.data.manager.RecentRoomsManager
import com.nddfeon.demonic.data.model.Room
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
    val publicRooms: List<Room> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val roomRepository: RoomRepository,
    val recentRoomsManager: RecentRoomsManager
) : ViewModel() {

    val currentUser: StateFlow<UserAccount?> = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observePublicRooms()
    }

    private fun observePublicRooms() {
        viewModelScope.launch {
            roomRepository.observePublicRooms().collect { rooms ->
                _uiState.value = _uiState.value.copy(publicRooms = rooms)
            }
        }
    }

    private fun getEffectiveUser(): UserAccount {
        return currentUser.value ?: run {
            val fallback = UserAccount(
                uid = "guest_" + (System.currentTimeMillis() % 100000),
                displayName = "Demon Guest"
            )
            viewModelScope.launch {
                authRepository.signInWithCustomUser(fallback.uid, fallback.displayName, null)
            }
            fallback
        }
    }

    fun updateRoomCodeInput(input: String) {
        val cleaned = if (input.contains("/room/")) {
            input.substringAfter("/room/").takeWhile { it.isLetterOrDigit() }
        } else {
            input
        }
        val filtered = cleaned.uppercase().filter { it.isLetterOrDigit() }.take(6)
        _uiState.value = _uiState.value.copy(roomCodeInput = filtered, errorMessage = null)
    }

    fun createRoom(initialVideoId: String = "", isPublic: Boolean = true, onRoomCreated: (String) -> Unit) {
        val user = getEffectiveUser()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingRoom = true, errorMessage = null)
            val result = roomRepository.createRoom(user, initialVideoId, isPublic)
            result.onSuccess { code ->
                recentRoomsManager.addRoom(code, isHost = true)
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
        val user = getEffectiveUser()
        val trimmed = code.trim().uppercase()
        if (trimmed.length != 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Room code must be 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isJoiningRoom = true, errorMessage = null)
            val result = roomRepository.joinRoom(trimmed, user)
            result.onSuccess { room ->
                recentRoomsManager.addRoom(room.roomCode, isHost = false)
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

    fun updateProfile(displayName: String, photoUrl: String? = null, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val result = authRepository.updateProfile(displayName, photoUrl)
            onComplete?.invoke(result.isSuccess)
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