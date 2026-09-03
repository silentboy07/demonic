package com.nddfeon.demonic.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nddfeon.demonic.data.model.ChatMessage
import com.nddfeon.demonic.data.model.Member
import com.nddfeon.demonic.data.model.Room
import com.nddfeon.demonic.data.model.UserAccount
import com.nddfeon.demonic.data.repository.AuthRepository
import com.nddfeon.demonic.data.repository.RoomRepository
import com.nddfeon.demonic.player.YouTubePlayerManager
import com.nddfeon.demonic.player.YouTubeUrlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class RoomUiState(
    val roomCode: String = "",
    val room: Room? = null,
    val members: List<Member> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val typingUsers: List<String> = emptyList(),
    val isHost: Boolean = false,
    val isSyncing: Boolean = false,
    val driftSeconds: Float = 0f,
    val videoInput: String = "",
    val chatInput: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val roomRepository: RoomRepository,
    private val authRepository: AuthRepository,
    val playerManager: YouTubePlayerManager
) : ViewModel() {

    private val roomCode: String = savedStateHandle["roomCode"] ?: ""

    val currentUser: StateFlow<UserAccount?> = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    private val _uiState = MutableStateFlow(RoomUiState(roomCode = roomCode))
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    private var driftMonitoringJob: Job? = null
    private var lastKnownRoom: Room? = null
    private var lastObservedVideoId: String = ""

    init {
        if (roomCode.isNotEmpty()) {
            initRoom()
        }
    }

    private fun initRoom() {
        observeRoomState()
        observeMembers()
        observeChatMessages()
        observeTyping()
        startDriftMonitoringLoop()
    }

    private fun observeRoomState() {
        viewModelScope.launch {
            roomRepository.observeRoom(roomCode).collect { room ->
                if (room == null) return@collect
                val currentUid = currentUser.value?.uid ?: ""
                val isHost = (room.hostId == currentUid)

                _uiState.value = _uiState.value.copy(
                    room = room,
                    isHost = isHost
                )

                // React to room playback changes (Sync Algorithm Rule 2)
                handleRemoteRoomUpdate(room)
                lastKnownRoom = room
            }
        }
    }

    /**
     * Sync Algorithm Step 2:
     * Every client (host included) attaches a ValueEventListener and reacts to changes:
     * - if state == "playing": targetPosition = position + (serverNow - updatedAt) / 1000;
     *   call player.seekTo(targetPosition) then player.play()
     * - if state == "paused": call player.seekTo(position) then player.pause()
     */
    private fun handleRemoteRoomUpdate(room: Room) {
        val serverNow = roomRepository.getServerNowMs()
        val videoChanged = (room.videoId != lastObservedVideoId)

        if (videoChanged && room.videoId.isNotEmpty()) {
            lastObservedVideoId = room.videoId
            // Cue or load video
            if (room.isPlaying) {
                val deltaSec = ((serverNow - room.updatedAt).coerceAtLeast(0L)) / 1000.0
                val targetPosition = (room.position + deltaSec).toFloat()
                playerManager.loadOrCueVideo(room.videoId, targetPosition, autoPlay = true)
                playerManager.seekTo(targetPosition)
                playerManager.play()
            } else {
                playerManager.loadOrCueVideo(room.videoId, room.position.toFloat(), autoPlay = false)
                playerManager.seekTo(room.position.toFloat())
                playerManager.pause()
            }
            return
        }

        if (room.isPlaying) {
            val deltaSec = ((serverNow - room.updatedAt).coerceAtLeast(0L)) / 1000.0
            val targetPosition = (room.position + deltaSec).toFloat()
            val currentSec = playerManager.currentSecond.value
            val drift = abs(currentSec - targetPosition)

            // Seek if position is different or initial
            if (drift > 0.8f) {
                playerManager.seekTo(targetPosition)
            }
            playerManager.play()
        } else {
            // Paused
            val targetPosition = room.position.toFloat()
            playerManager.seekTo(targetPosition)
            playerManager.pause()
        }
    }

    /**
     * Sync Algorithm Step 3:
     * Every 8 seconds, each client compares its actual local playback position
     * to the expected position calculated from the last known state;
     * if the drift is more than 1.5 seconds, silently reseek without pausing.
     */
    private fun startDriftMonitoringLoop() {
        driftMonitoringJob?.cancel()
        driftMonitoringJob = viewModelScope.launch {
            while (isActive) {
                delay(8000L) // 8-second interval
                val room = lastKnownRoom ?: continue

                if (room.isPlaying) {
                    val serverNow = roomRepository.getServerNowMs()
                    val deltaSec = ((serverNow - room.updatedAt).coerceAtLeast(0L)) / 1000.0
                    val expectedPosition = (room.position + deltaSec).toFloat()
                    val actualPosition = playerManager.currentSecond.value
                    val drift = actualPosition - expectedPosition

                    _uiState.value = _uiState.value.copy(driftSeconds = drift)

                    // If drift > 1.5 seconds, silently reseek without pausing
                    if (abs(drift) > 1.5f) {
                        _uiState.value = _uiState.value.copy(isSyncing = true)
                        playerManager.seekTo(expectedPosition)
                        delay(500L)
                        _uiState.value = _uiState.value.copy(isSyncing = false)
                    }
                } else {
                    val actualPosition = playerManager.currentSecond.value
                    val drift = actualPosition - room.position.toFloat()
                    _uiState.value = _uiState.value.copy(driftSeconds = drift)

                    if (abs(drift) > 1.0f) {
                        playerManager.seekTo(room.position.toFloat())
                    }
                }
            }
        }
    }

    private fun observeMembers() {
        viewModelScope.launch {
            val hostId = _uiState.value.room?.hostId ?: ""
            roomRepository.observeMembers(roomCode, hostId).collect { members ->
                _uiState.value = _uiState.value.copy(members = members)
            }
        }
    }

    private fun observeChatMessages() {
        viewModelScope.launch {
            roomRepository.observeMessages(roomCode).collect { message ->
                val current = _uiState.value.messages
                if (current.none { it.id == message.id }) {
                    _uiState.value = _uiState.value.copy(messages = current + message)
                }
            }
        }
    }

    private fun observeTyping() {
        viewModelScope.launch {
            val currentUid = currentUser.value?.uid ?: ""
            roomRepository.observeTypingUsers(roomCode, currentUid).collect { typingUsers ->
                _uiState.value = _uiState.value.copy(typingUsers = typingUsers)
            }
        }
    }

    /**
     * Sync Algorithm Step 4:
     * Host actions always write to Firebase first —
     * the host's own player only reacts to the Firebase listener firing back.
     * Never update the host's local player directly from the button tap!
     */
    fun togglePlayPause() {
        if (!_uiState.value.isHost) return
        val room = _uiState.value.room ?: return
        val newState = if (room.isPlaying) "paused" else "playing"
        val currentPosition = playerManager.currentSecond.value.toDouble()

        viewModelScope.launch {
            roomRepository.updatePlaybackState(
                roomCode = roomCode,
                state = newState,
                positionSeconds = currentPosition
            )
        }
    }

    fun hostSeekTo(targetSeconds: Float) {
        if (!_uiState.value.isHost) return
        val room = _uiState.value.room ?: return

        viewModelScope.launch {
            roomRepository.updatePlaybackState(
                roomCode = roomCode,
                state = room.state,
                positionSeconds = targetSeconds.toDouble()
            )
        }
    }

    fun hostChangeVideo(input: String) {
        if (!_uiState.value.isHost) return
        val videoId = YouTubeUrlParser.extractVideoId(input)
        if (videoId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Invalid YouTube URL or Video ID")
            return
        }

        viewModelScope.launch {
            roomRepository.updatePlaybackState(
                roomCode = roomCode,
                state = "playing",
                positionSeconds = 0.0,
                videoId = videoId,
                videoTitle = "YouTube Video: $videoId"
            )
            _uiState.value = _uiState.value.copy(videoInput = "", errorMessage = null)
        }
    }

    fun updateVideoInput(text: String) {
        _uiState.value = _uiState.value.copy(videoInput = text, errorMessage = null)
    }

    fun updateChatInput(text: String) {
        _uiState.value = _uiState.value.copy(chatInput = text)
        val user = currentUser.value ?: return
        viewModelScope.launch {
            roomRepository.setTyping(roomCode, user.uid, user.displayName, text.isNotBlank())
        }
    }

    fun sendChatMessage() {
        val text = _uiState.value.chatInput.trim()
        val user = currentUser.value ?: return
        if (text.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(chatInput = "")
            roomRepository.setTyping(roomCode, user.uid, user.displayName, false)
            roomRepository.sendMessage(roomCode, user, text)
        }
    }

    fun leaveRoom() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            roomRepository.leaveRoom(roomCode, user.uid)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        driftMonitoringJob?.cancel()
        playerManager.release()
    }
}
