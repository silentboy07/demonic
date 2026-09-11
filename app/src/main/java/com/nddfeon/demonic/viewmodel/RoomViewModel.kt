package com.nddfeon.demonic.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nddfeon.demonic.data.model.ChatMessage
import com.nddfeon.demonic.data.model.LiveReaction
import com.nddfeon.demonic.data.model.Member
import com.nddfeon.demonic.data.model.QueueItem
import com.nddfeon.demonic.data.model.Room
import com.nddfeon.demonic.data.model.RoomSpecialEffect
import com.nddfeon.demonic.data.model.RoomThemePreset
import com.nddfeon.demonic.data.model.SpecialEffectType
import com.nddfeon.demonic.data.model.VisualizerStylePreset
import com.nddfeon.demonic.data.model.UserAccount
import com.nddfeon.demonic.data.repository.AuthRepository
import com.nddfeon.demonic.data.repository.RoomRepository
import com.nddfeon.demonic.player.DemonicPlaybackService
import com.nddfeon.demonic.player.YouTubePlayerManager
import com.nddfeon.demonic.player.YouTubeSearchManager
import com.nddfeon.demonic.player.YouTubeUrlParser
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val queue: List<QueueItem> = emptyList(),
    val activeReactions: List<LiveReaction> = emptyList(),
    val isHost: Boolean = true,
    val isDj: Boolean = false,
    val canControlPlayback: Boolean = true,
    val isSyncing: Boolean = false,
    val driftSeconds: Float = 0f,
    val videoInput: String = "",
    val chatInput: String = "",
    val replyingToMessage: ChatMessage? = null,
    val isRoomClosed: Boolean = false,
    val errorMessage: String? = null,
    val currentTheme: RoomThemePreset = RoomThemePreset.CYBER_NEON,
    val visualizerStyle: VisualizerStylePreset = VisualizerStylePreset.CIRCULAR,
    val activeSpecialEffect: RoomSpecialEffect? = null,
    val isMutedLocally: Boolean = false,
    val isAfkMode: Boolean = false
)

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val roomRepository: RoomRepository,
    private val authRepository: AuthRepository,
    val playerManager: YouTubePlayerManager,
    val searchManager: YouTubeSearchManager
) : ViewModel() {

    private val roomCode: String = savedStateHandle["roomCode"] ?: ""

    val currentUser: StateFlow<UserAccount?> = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUser)

    private val _uiState = MutableStateFlow(RoomUiState(roomCode = roomCode))
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    private var driftMonitoringJob: Job? = null
    private var sleepTimerJob: Job? = null
    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

    private var lastKnownRoom: Room? = null
    private var lastObservedVideoId: String = ""
    private var lastVideoLoadedTime: Long = 0L
    private var lastDriftSeekTime: Long = 0L

    fun toggleLocalMute(): Boolean {
        val newMute = playerManager.toggleLocalMute()
        _uiState.value = _uiState.value.copy(
            isMutedLocally = newMute,
            isAfkMode = newMute
        )
        return newMute
    }

    fun setSleepTimer(minutes: Int?) {
        _sleepTimerMinutes.value = minutes
        sleepTimerJob?.cancel()
        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                playerManager.pause()
                _sleepTimerMinutes.value = null
            }
        }
    }

    private fun getEffectiveUser(): UserAccount {
        return currentUser.value ?: UserAccount(
            uid = "guest_" + (System.currentTimeMillis() % 100000),
            displayName = "Demon Guest"
        )
    }

    init {
        if (roomCode.isNotEmpty()) {
            initRoom()
        }
    }

    private fun initRoom() {
        observeRoomState()
        observeMembers()
        observeChatMessages()
        observeDeletedMessages()
        observeTyping()
        observeQueue()
        observeReactions()
        observeSpecialEffects()
        observePlayerStateForAutoNext()
        DemonicPlaybackService.onNextTrackCallback = {
            skipToNextTrack()
        }
        viewModelScope.launch {
            playerManager.isMutedLocally.collect { muted ->
                _uiState.value = _uiState.value.copy(
                    isMutedLocally = muted,
                    isAfkMode = muted
                )
            }
        }
        startDriftMonitoringLoop()
    }

    private fun observeRoomState() {
        viewModelScope.launch {
            roomRepository.observeRoom(roomCode).collect { room ->
                if (room == null) {
                    _uiState.value = _uiState.value.copy(
                        room = null,
                        isRoomClosed = true,
                        errorMessage = "Room has been ended."
                    )
                    playerManager.pause()
                    return@collect
                }
                val currentUid = getEffectiveUser().uid
                val isHost = (room.hostId == currentUid || room.hostId.isEmpty() || (room.hostId.startsWith("guest_") && currentUid.startsWith("guest_")))
                val isDj = (room.djId == currentUid)
                val canControl = isHost || isDj || room.canControlPlayback(currentUid)

                val currentHostId = room.hostId
                val updatedMembers = _uiState.value.members.map { member ->
                    member.copy(isHost = currentHostId.isNotEmpty() && member.uid == currentHostId)
                }.sortedWith(compareByDescending<Member> { it.isHost }.thenBy { it.joinedAt })

                _uiState.value = _uiState.value.copy(
                    room = room,
                    members = updatedMembers,
                    isHost = isHost,
                    isDj = isDj,
                    canControlPlayback = canControl,
                    currentTheme = room.themePreset,
                    visualizerStyle = room.visualizerPreset
                )

                handleRemoteRoomUpdate(room)
                lastKnownRoom = room
            }
        }
    }
    private fun handleRemoteRoomUpdate(room: Room) {
        val serverNow = roomRepository.getServerNowMs()
        val videoChanged = (room.videoId != lastObservedVideoId)
        val currentUid = getEffectiveUser().uid
        val isHost = (room.hostId == currentUid || room.hostId.isEmpty() || (room.hostId.startsWith("guest_") && currentUid.startsWith("guest_")))

        if (videoChanged && room.videoId.isNotEmpty()) {
            lastObservedVideoId = room.videoId
            lastVideoLoadedTime = System.currentTimeMillis()
            val deltaSec = if (room.isPlaying) ((serverNow - room.updatedAt).coerceAtLeast(0L)) / 1000.0 else 0.0
            val targetPosition = if (isHost) 0f else (room.position + deltaSec).toFloat()

            android.util.Log.d("DemonicSync", "Loading new video: ${room.videoId} at position: $targetPosition (isHost=$isHost, isPlaying=${room.isPlaying})")
            playerManager.loadOrCueVideo(room.videoId, targetPosition, autoPlay = room.isPlaying)
            if (room.isPlaying) {
                playerManager.play()
            } else {
                playerManager.pause()
            }
            return
        }

        // Host is the master player: never override host playback with client-calculated drift!
        if (isHost) {
            if (room.isPlaying && !playerManager.isPlaying() && !playerManager.isBuffering()) {
                playerManager.play()
            } else if (!room.isPlaying && playerManager.isPlaying()) {
                playerManager.pause()
            }
            return
        }

        // Ultra-Fast Sync for Listeners: Only 1.2s initial buffer before drift correction
        val timeSinceLoad = System.currentTimeMillis() - lastVideoLoadedTime
        if (timeSinceLoad < 1200L) {
            if (room.isPlaying) playerManager.play() else playerManager.pause()
            return
        }

        if (room.isPlaying) {
            val deltaSec = ((serverNow - room.updatedAt).coerceAtLeast(0L)) / 1000.0
            val targetPosition = (room.position + deltaSec).toFloat()
            val currentSec = playerManager.currentSecond.value
            val drift = abs(currentSec - targetPosition)

            // High-precision sub-second sync: correct if drift exceeds 0.85s with 1.5s cooldown
            if (playerManager.isPlaying() && drift > 0.85f && (System.currentTimeMillis() - lastDriftSeekTime > 1500L)) {
                lastDriftSeekTime = System.currentTimeMillis()
                android.util.Log.d("DemonicSync", "Ultra-Fast Sync: drift $drift > 0.85s, seeking to: $targetPosition")
                playerManager.seekTo(targetPosition)
            }
            playerManager.play()
        } else {
            val targetPosition = room.position.toFloat()
            playerManager.seekTo(targetPosition)
            playerManager.pause()
        }
    }

    private fun startDriftMonitoringLoop() {
        driftMonitoringJob?.cancel()
        driftMonitoringJob = viewModelScope.launch {
            var loopTick = 0
            while (isActive) {
                delay(1200L) // Ultra-fast check every 1.2 seconds (was 4000ms!)
                loopTick++
                val room = lastKnownRoom ?: continue
                val currentUid = getEffectiveUser().uid
                val isHost = (room.hostId == currentUid || room.hostId.isEmpty() || (room.hostId.startsWith("guest_") && currentUid.startsWith("guest_")))

                if (isHost) {
                    // Host Heartbeat: Reports true player position every 1.2s to 2.4s
                    if (room.isPlaying && playerManager.isPlaying()) {
                        val currentPos = playerManager.currentSecond.value.toDouble()
                        roomRepository.updatePlaybackState(
                            roomCode = roomCode,
                            state = "playing",
                            positionSeconds = currentPos
                        )
                    }
                    _uiState.value = _uiState.value.copy(driftSeconds = 0f, isSyncing = false)
                } else {
                    // Listener ultra-fast drift evaluation
                    if (room.isPlaying) {
                        val serverNow = roomRepository.getServerNowMs()
                        val deltaSec = ((serverNow - room.updatedAt).coerceAtLeast(0L)) / 1000.0
                        val expectedPosition = (room.position + deltaSec).toFloat()
                        val actualPosition = playerManager.currentSecond.value
                        val drift = actualPosition - expectedPosition

                        _uiState.value = _uiState.value.copy(driftSeconds = drift)

                        val timeSinceLoad = System.currentTimeMillis() - lastVideoLoadedTime
                        // Instant sub-second correction if drift > 0.85s (was 2.5s)
                        if (timeSinceLoad > 1200L && playerManager.isPlaying() && abs(drift) > 0.85f && (System.currentTimeMillis() - lastDriftSeekTime > 1500L)) {
                            _uiState.value = _uiState.value.copy(isSyncing = true)
                            lastDriftSeekTime = System.currentTimeMillis()
                            android.util.Log.d("DemonicSync", "Ultra-Fast Drift correction: seeking listener to $expectedPosition (drift: $drift)")
                            playerManager.seekTo(expectedPosition)
                            delay(250L)
                            _uiState.value = _uiState.value.copy(isSyncing = false)
                        }
                    } else {
                        val actualPosition = playerManager.currentSecond.value
                        val drift = actualPosition - room.position.toFloat()
                        _uiState.value = _uiState.value.copy(driftSeconds = drift)
                        if (abs(drift) > 0.5f) {
                            playerManager.seekTo(room.position.toFloat())
                        }
                    }
                }
            }
        }
    }

    private fun observePlayerStateForAutoNext() {
        viewModelScope.launch {
            playerManager.playerState.collect { state ->
                if (state == PlayerConstants.PlayerState.ENDED) {
                    if (_uiState.value.canControlPlayback) {
                        autoPlayNextTrack()
                    }
                }
            }
        }
    }

    fun skipToNextTrack() {
        if (_uiState.value.canControlPlayback) {
            autoPlayNextTrack()
        }
    }

    private fun autoPlayNextTrack() {
        val currentQueue = _uiState.value.queue
        if (currentQueue.isNotEmpty()) {
            val nextTrack = currentQueue.first()
            playQueueItem(nextTrack)
        }
    }

    private fun observeMembers() {
        viewModelScope.launch {
            roomRepository.observeMembers(roomCode, "").collect { rawMembers ->
                val currentHostId = _uiState.value.room?.hostId ?: ""
                val effectiveUser = getEffectiveUser()
                val withCurrent = if (rawMembers.none { it.uid == effectiveUser.uid }) {
                    rawMembers + Member(
                        uid = effectiveUser.uid,
                        name = effectiveUser.displayName,
                        photoUrl = effectiveUser.photoUrl ?: "",
                        joinedAt = System.currentTimeMillis(),
                        isHost = (effectiveUser.uid == currentHostId),
                        isAfk = _uiState.value.isMutedLocally
                    )
                } else {
                    rawMembers.map { m ->
                        if (m.uid == effectiveUser.uid) m.copy(isAfk = _uiState.value.isMutedLocally) else m
                    }
                }
                val updatedMembers = withCurrent.map { member ->
                    member.copy(isHost = currentHostId.isNotEmpty() && member.uid == currentHostId)
                }.sortedWith(compareByDescending<Member> { it.isHost }.thenBy { it.joinedAt })
                _uiState.value = _uiState.value.copy(members = updatedMembers)
            }
        }
    }

    private fun observeChatMessages() {
        viewModelScope.launch {
            roomRepository.observeMessages(roomCode).collect { message ->
                val current = _uiState.value.messages
                val isDuplicate = current.any { existing ->
                    existing.id == message.id ||
                    (existing.senderId == message.senderId &&
                     existing.text == message.text &&
                     kotlin.math.abs(existing.sentAt - message.sentAt) < 4000L)
                }
                if (!isDuplicate) {
                    _uiState.value = _uiState.value.copy(messages = current + message)
                }
            }
        }
    }

    private fun observeDeletedMessages() {
        viewModelScope.launch {
            roomRepository.observeDeletedMessageIds(roomCode).collect { deletedId ->
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages.filter { it.id != deletedId }
                )
            }
        }
    }

    private fun observeTyping() {
        viewModelScope.launch {
            val currentUid = getEffectiveUser().uid
            roomRepository.observeTypingUsers(roomCode, currentUid).collect { typingUsers ->
                _uiState.value = _uiState.value.copy(typingUsers = typingUsers)
            }
        }
    }

    private fun observeQueue() {
        viewModelScope.launch {
            roomRepository.observeQueue(roomCode).collect { queue ->
                _uiState.value = _uiState.value.copy(queue = queue)
            }
        }
    }

    private fun observeReactions() {
        viewModelScope.launch {
            roomRepository.observeReactions(roomCode).collect { reaction ->
                val current = _uiState.value.activeReactions
                _uiState.value = _uiState.value.copy(activeReactions = (current + reaction).takeLast(20))
            }
        }
    }
    fun togglePlayPause() {
        if (!_uiState.value.canControlPlayback) return
        val room = _uiState.value.room ?: return
        val newState = if (room.isPlaying) "paused" else "playing"
        val currentPosition = playerManager.currentSecond.value.toDouble()

        if (newState == "playing") {
            playerManager.play()
        } else {
            playerManager.pause()
        }

        viewModelScope.launch {
            roomRepository.updatePlaybackState(
                roomCode = roomCode,
                state = newState,
                positionSeconds = currentPosition
            )
        }
    }

    fun hostSeekTo(targetSeconds: Float) {
        if (!_uiState.value.canControlPlayback) return
        val room = _uiState.value.room ?: return
        playerManager.seekTo(targetSeconds)

        viewModelScope.launch {
            roomRepository.updatePlaybackState(
                roomCode = roomCode,
                state = room.state,
                positionSeconds = targetSeconds.toDouble()
            )
        }
    }

    fun hostChangeVideo(input: String) {
        if (!_uiState.value.canControlPlayback) return
        val videoId = YouTubeUrlParser.extractVideoId(input)
        if (videoId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Invalid YouTube URL or Video ID")
            return
        }

        playTrack(videoId, "YouTube Video: $videoId")
        _uiState.value = _uiState.value.copy(videoInput = "", errorMessage = null)
    }

    fun playTrack(videoId: String, title: String) {
        if (!_uiState.value.canControlPlayback) return
        lastObservedVideoId = videoId
        playerManager.loadOrCueVideo(videoId, 0f, autoPlay = true)
        playerManager.play()

        viewModelScope.launch {
            roomRepository.updatePlaybackState(
                roomCode = roomCode,
                state = "playing",
                positionSeconds = 0.0,
                videoId = videoId,
                videoTitle = title
            )
        }
    }

    fun playQueueItem(item: QueueItem) {
        if (!_uiState.value.canControlPlayback) return
        playTrack(item.videoId, item.title)
        removeFromQueue(item.id)
    }

    fun addToQueue(videoId: String, title: String) {
        val user = getEffectiveUser()
        val item = QueueItem(
            id = "q_" + System.currentTimeMillis() + "_" + (100..999).random(),
            videoId = videoId,
            title = title,
            thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
            addedByUid = user.uid,
            addedByName = user.displayName,
            addedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            roomRepository.addToQueue(roomCode, item)
        }
    }

    fun addMultipleToQueue(songs: List<Pair<String, String>>) {
        if (songs.isEmpty()) return
        val user = getEffectiveUser()
        val baseTime = System.currentTimeMillis()
        val items = songs.mapIndexed { index, (videoId, title) ->
            QueueItem(
                id = "q_${baseTime}_${index}_${(100..999).random()}",
                videoId = videoId,
                title = title,
                thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                addedByUid = user.uid,
                addedByName = user.displayName,
                addedAt = baseTime + index
            )
        }
        viewModelScope.launch {
            roomRepository.addMultipleToQueue(roomCode, items)
        }
    }

    fun importPlaylistOrLink(
        input: String,
        onProgress: (String) -> Unit,
        onSuccess: (count: Int, message: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            onError("Please enter a valid YouTube link or Playlist URL")
            return
        }

        viewModelScope.launch {
            try {
                onProgress("Analyzing URL...")
                val playlistId = YouTubeUrlParser.extractPlaylistId(trimmed)
                if (playlistId != null) {
                    onProgress("Fetching tracks from playlist...")
                    val videos = searchManager.fetchPlaylistVideos(playlistId)
                    if (videos.isEmpty()) {
                        onError("Could not extract songs from this playlist. Verify the playlist is public.")
                        return@launch
                    }

                    val pairs = videos.map { it.videoId to it.title }
                    val currentVideo = _uiState.value.room?.videoId ?: ""

                    // If room is empty and user has playback rights, start first song & queue rest
                    if (currentVideo.isEmpty() && _uiState.value.canControlPlayback) {
                        val first = pairs.first()
                        playTrack(first.first, first.second)
                        if (pairs.size > 1) {
                            addMultipleToQueue(pairs.drop(1))
                        }
                        onSuccess(pairs.size, "Playing '${first.second}' & added ${pairs.size - 1} songs to queue! 🎶")
                    } else {
                        addMultipleToQueue(pairs)
                        onSuccess(pairs.size, "Successfully added ${pairs.size} songs from playlist to queue! 🎶")
                    }
                    return@launch
                }

                // If single video link or 11-char ID
                val singleId = YouTubeUrlParser.extractVideoId(trimmed)
                if (singleId != null) {
                    onProgress("Fetching video details...")
                    val results = searchManager.search(singleId)
                    val title = results.firstOrNull()?.title ?: "YouTube Video ($singleId)"
                    val currentVideo = _uiState.value.room?.videoId ?: ""

                    if (currentVideo.isEmpty() && _uiState.value.canControlPlayback) {
                        playTrack(singleId, title)
                        onSuccess(1, "Playing '$title' in room! 🎵")
                    } else {
                        addToQueue(singleId, title)
                        onSuccess(1, "Added '$title' to queue! 🎵")
                    }
                    return@launch
                }

                onError("Unrecognized link. Please paste a valid YouTube video or playlist link.")
            } catch (e: Exception) {
                onError("Failed to import: ${e.localizedMessage ?: "Network error"}")
            }
        }
    }

    fun removeFromQueue(itemId: String) {
        if (!_uiState.value.canControlPlayback) return
        viewModelScope.launch {
            roomRepository.removeFromQueue(roomCode, itemId)
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        if (!_uiState.value.canControlPlayback) return
        val current = _uiState.value.queue.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val moved = current.removeAt(fromIndex)
            current.add(toIndex, moved)
            _uiState.value = _uiState.value.copy(queue = current)
            viewModelScope.launch {
                roomRepository.reorderQueue(roomCode, current)
            }
        }
    }

    fun upvoteQueueItem(itemId: String) {
        val user = getEffectiveUser()
        viewModelScope.launch {
            roomRepository.upvoteQueueItem(roomCode, itemId, user.uid)
        }
    }

    fun passTheAux(targetUid: String) {
        if (!_uiState.value.isHost) return
        viewModelScope.launch {
            roomRepository.passTheAux(roomCode, targetUid)
        }
    }

    fun sendReaction(emoji: String) {
        val user = getEffectiveUser()
        val currentMember = _uiState.value.members.find { it.uid == user.uid }
        if (currentMember != null && currentMember.timedOutUntil > System.currentTimeMillis()) {
            _uiState.value = _uiState.value.copy(errorMessage = "You are timed out by the host!")
            return
        }

        val reaction = LiveReaction(
            id = "rx_" + System.currentTimeMillis() + "_" + (100..999).random(),
            emoji = emoji,
            senderName = user.displayName,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            roomRepository.sendReaction(roomCode, reaction)
        }
    }

    fun updateVideoInput(text: String) {
        _uiState.value = _uiState.value.copy(videoInput = text, errorMessage = null)
    }

    fun updateChatInput(text: String) {
        val user = getEffectiveUser()
        val currentMember = _uiState.value.members.find { it.uid == user.uid }
        if (currentMember != null && currentMember.timedOutUntil > System.currentTimeMillis()) {
            return
        }

        _uiState.value = _uiState.value.copy(chatInput = text)
        viewModelScope.launch {
            roomRepository.setTyping(roomCode, user.uid, user.displayName, text.isNotBlank())
        }
    }

    fun setReplyingTo(message: ChatMessage?) {
        _uiState.value = _uiState.value.copy(replyingToMessage = message)
    }

    fun clearReply() {
        _uiState.value = _uiState.value.copy(replyingToMessage = null)
    }

    fun sendChatMessage() {
        val text = _uiState.value.chatInput.trim()
        val user = getEffectiveUser()
        if (text.isEmpty()) return

        val currentMember = _uiState.value.members.find { it.uid == user.uid }
        if (currentMember != null && currentMember.timedOutUntil > System.currentTimeMillis()) {
            _uiState.value = _uiState.value.copy(errorMessage = "You are timed out by the host!")
            return
        }

        val replyingTo = _uiState.value.replyingToMessage
        val senderRole = when {
            _uiState.value.isHost -> "HOST"
            _uiState.value.isDj -> "DJ"
            else -> "LISTENER"
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(chatInput = "", replyingToMessage = null)
            roomRepository.setTyping(roomCode, user.uid, user.displayName, false)
            roomRepository.sendMessage(
                roomCode = roomCode,
                user = user,
                text = text,
                replyToMessageId = replyingTo?.id ?: "",
                replyToSenderName = replyingTo?.senderName ?: "",
                replyToText = replyingTo?.text ?: "",
                senderRole = senderRole
            )
        }
    }

    fun deleteMessage(messageId: String) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages.filter { it.id != messageId }
        )
        viewModelScope.launch {
            roomRepository.deleteMessage(roomCode, messageId)
        }
    }

    fun timeoutMember(uid: String, targetName: String, durationMinutes: Int) {
        val hostUser = getEffectiveUser()
        viewModelScope.launch {
            roomRepository.timeoutMember(roomCode, uid, durationMinutes, hostUser.displayName, targetName)
        }
    }

    fun removeTimeout(uid: String, targetName: String) {
        val hostUser = getEffectiveUser()
        viewModelScope.launch {
            roomRepository.removeTimeout(roomCode, uid, hostUser.displayName, targetName)
        }
    }

    fun leaveRoom() {
        val user = getEffectiveUser()
        viewModelScope.launch {
            roomRepository.leaveRoom(roomCode, user.uid)
        }
    }

    fun deleteRoom() {
        viewModelScope.launch {
            try {
                playerManager.pause()
                _uiState.value = _uiState.value.copy(
                    room = null,
                    isRoomClosed = true,
                    errorMessage = "Room ended."
                )
                roomRepository.deleteRoom(roomCode)
            } catch (e: Exception) {
                android.util.Log.e("DemonicSync", "Error deleting room: ${e.message}")
            }
        }
    }

    fun forceSyncWithHost() {
        viewModelScope.launch {
            val room = _uiState.value.room ?: return@launch
            val serverNow = roomRepository.getServerNowMs()
            val deltaSec = if (room.isPlaying) ((serverNow - room.updatedAt).coerceAtLeast(0L)) / 1000.0 else 0.0
            val targetPosition = (room.position + deltaSec).toFloat()
            _uiState.value = _uiState.value.copy(isSyncing = true)
            playerManager.seekTo(targetPosition)
            if (room.isPlaying && !playerManager.isPlaying()) {
                playerManager.play()
            }
            delay(300L)
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }


    private fun observeSpecialEffects() {
        viewModelScope.launch {
            roomRepository.observeSpecialEffects(roomCode).collect { effect ->
                _uiState.value = _uiState.value.copy(activeSpecialEffect = effect)
                delay(6000L)
                if (_uiState.value.activeSpecialEffect?.id == effect.id) {
                    _uiState.value = _uiState.value.copy(activeSpecialEffect = null)
                }
            }
        }
    }

    fun setRoomTheme(theme: RoomThemePreset) {
        _uiState.value = _uiState.value.copy(currentTheme = theme)
        viewModelScope.launch {
            roomRepository.setRoomTheme(roomCode, theme.id)
        }
    }

    fun setVisualizerStyle(style: VisualizerStylePreset) {
        _uiState.value = _uiState.value.copy(visualizerStyle = style)
        viewModelScope.launch {
            roomRepository.setVisualizerStyle(roomCode, style.id)
        }
    }

    fun triggerSpecialEffect(
        type: SpecialEffectType,
        targetName: String = "",
        customMessage: String = ""
    ) {
        val user = getEffectiveUser()
        val defaultMsg = if (customMessage.isNotBlank()) customMessage else type.defaultText
        val effect = RoomSpecialEffect(
            id = "fx_${System.currentTimeMillis()}_${(100..999).random()}",
            type = type.id,
            senderName = user.displayName,
            targetName = targetName.trim(),
            customMessage = defaultMsg,
            timestamp = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(activeSpecialEffect = effect)
        viewModelScope.launch {
            roomRepository.triggerSpecialEffect(roomCode, effect)
            delay(6000L)
            if (_uiState.value.activeSpecialEffect?.id == effect.id) {
                _uiState.value = _uiState.value.copy(activeSpecialEffect = null)
            }
        }
    }

    fun clearActiveSpecialEffect() {
        _uiState.value = _uiState.value.copy(activeSpecialEffect = null)
    }

    override fun onCleared() {

        super.onCleared()
        driftMonitoringJob?.cancel()
        sleepTimerJob?.cancel()
        DemonicPlaybackService.onNextTrackCallback = null
        playerManager.release()
    }
}