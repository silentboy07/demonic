package com.nddfeon.demonic.data.repository

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.nddfeon.demonic.data.model.ChatMessage
import com.nddfeon.demonic.data.model.LiveReaction
import com.nddfeon.demonic.data.model.Member
import com.nddfeon.demonic.data.model.QueueItem
import com.nddfeon.demonic.data.model.Room
import com.nddfeon.demonic.data.model.RoomSpecialEffect
import com.nddfeon.demonic.data.model.RoomThemePreset
import com.nddfeon.demonic.data.model.SpecialEffectType
import com.nddfeon.demonic.data.model.UserAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

interface RoomRepository {
    val serverTimeOffsetMs: StateFlow<Long>
    fun getServerNowMs(): Long

    suspend fun createRoom(user: UserAccount, initialVideoId: String = ""): Result<String>
    suspend fun joinRoom(roomCode: String, user: UserAccount): Result<Room>
    suspend fun leaveRoom(roomCode: String, uid: String)

    fun observeRoom(roomCode: String): Flow<Room?>
    fun observeMembers(roomCode: String, hostId: String): Flow<List<Member>>
    fun observeMessages(roomCode: String): Flow<ChatMessage>
    fun observeTypingUsers(roomCode: String, currentUid: String): Flow<List<String>>
    fun observeQueue(roomCode: String): Flow<List<QueueItem>>
    fun observeReactions(roomCode: String): Flow<LiveReaction>
    fun observePublicRooms(): Flow<List<Room>>

    suspend fun updatePlaybackState(
        roomCode: String,
        state: String,
        positionSeconds: Double,
        videoId: String? = null,
        videoTitle: String? = null
    ): Result<Unit>

    suspend fun passTheAux(roomCode: String, djUid: String): Result<Unit>
    suspend fun addToQueue(roomCode: String, item: QueueItem): Result<Unit>
    suspend fun addMultipleToQueue(roomCode: String, items: List<QueueItem>): Result<Unit>
    suspend fun removeFromQueue(roomCode: String, itemId: String): Result<Unit>
    suspend fun reorderQueue(roomCode: String, newQueue: List<QueueItem>): Result<Unit>
    suspend fun upvoteQueueItem(roomCode: String, itemId: String, uid: String): Result<Unit>
    suspend fun sendReaction(roomCode: String, reaction: LiveReaction): Result<Unit>
    suspend fun sendMessage(
        roomCode: String,
        user: UserAccount,
        text: String,
        replyToMessageId: String = "",
        replyToSenderName: String = "",
        replyToText: String = "",
        senderRole: String = ""
    ): Result<Unit>
    suspend fun setTyping(roomCode: String, uid: String, userName: String, isTyping: Boolean)
    fun observeDeletedMessageIds(roomCode: String): Flow<String>
    suspend fun deleteMessage(roomCode: String, messageId: String): Result<Unit>
    suspend fun timeoutMember(roomCode: String, uid: String, durationMinutes: Int, hostName: String, targetName: String): Result<Unit>
    suspend fun removeTimeout(roomCode: String, uid: String, hostName: String, targetName: String): Result<Unit>
    suspend fun deleteRoom(roomCode: String): Result<Unit>
    suspend fun setRoomTheme(roomCode: String, theme: String): Result<Unit>
    suspend fun setVisualizerStyle(roomCode: String, style: String): Result<Unit>
    suspend fun triggerSpecialEffect(roomCode: String, effect: RoomSpecialEffect): Result<Unit>
    fun observeSpecialEffects(roomCode: String): Flow<RoomSpecialEffect>
}
@Singleton
class FirebaseRoomRepository @Inject constructor(
    private val database: FirebaseDatabase
) : RoomRepository {

    private val random = SecureRandom()
    private val charPool: List<Char> = ('A'..'Z') + ('0'..'9')
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _serverTimeOffsetMs = MutableStateFlow(0L)
    override val serverTimeOffsetMs: StateFlow<Long> = _serverTimeOffsetMs.asStateFlow()

    private val localRooms = ConcurrentHashMap<String, MutableStateFlow<Room?>>()
    private val localMembers = ConcurrentHashMap<String, MutableStateFlow<List<Member>>>()
    private val localMessages = ConcurrentHashMap<String, MutableSharedFlow<ChatMessage>>()
    private val localTyping = ConcurrentHashMap<String, MutableStateFlow<List<String>>>()
    private val localQueues = ConcurrentHashMap<String, MutableStateFlow<List<QueueItem>>>()
    private val localReactions = ConcurrentHashMap<String, MutableSharedFlow<LiveReaction>>()
    private val localPublicRooms = MutableStateFlow<List<Room>>(emptyList())
    private val localSpecialEffects = ConcurrentHashMap<String, MutableSharedFlow<RoomSpecialEffect>>()

    private fun getOrCreateLocalSpecialEffects(roomCode: String): MutableSharedFlow<RoomSpecialEffect> {
        return localSpecialEffects.computeIfAbsent(roomCode) { MutableSharedFlow(extraBufferCapacity = 64) }
    }

    init {
        try {
            val offsetRef = database.getReference(".info/serverTimeOffset")
            offsetRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val offset = snapshot.getValue(Long::class.java) ?: 0L
                    _serverTimeOffsetMs.value = offset
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (_: Exception) {}
    }

    override fun getServerNowMs(): Long {
        return System.currentTimeMillis() + _serverTimeOffsetMs.value
    }

    private fun generateRandomCode(length: Int = 6): String {
        return (1..length)
            .map { random.nextInt(charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    private fun getOrCreateLocalRoom(code: String): MutableStateFlow<Room?> =
        localRooms.getOrPut(code) { MutableStateFlow(null) }

    private fun getOrCreateLocalMembers(code: String): MutableStateFlow<List<Member>> =
        localMembers.getOrPut(code) { MutableStateFlow(emptyList()) }

    private fun getOrCreateLocalMessages(code: String): MutableSharedFlow<ChatMessage> =
        localMessages.getOrPut(code) { MutableSharedFlow(replay = 20) }

    private fun getOrCreateLocalTyping(code: String): MutableStateFlow<List<String>> =
        localTyping.getOrPut(code) { MutableStateFlow(emptyList()) }

    private fun getOrCreateLocalQueue(code: String): MutableStateFlow<List<QueueItem>> =
        localQueues.getOrPut(code) { MutableStateFlow(emptyList()) }

    private fun getOrCreateLocalReactions(code: String): MutableSharedFlow<LiveReaction> =
        localReactions.getOrPut(code) { MutableSharedFlow(extraBufferCapacity = 50) }

    private fun updatePublicRoomsList() {
        val list = localRooms.values.mapNotNull { it.value }.filter { it.isPublic }.map { r ->
            r.copy(memberCount = localMembers[r.roomCode]?.value?.size ?: 1)
        }
        localPublicRooms.value = list
    }
    override suspend fun createRoom(user: UserAccount, initialVideoId: String): Result<String> {
        var roomCode = ""
        var attempts = 0
        val maxAttempts = 10

        while (attempts < maxAttempts) {
            val candidateCode = generateRandomCode(6)
            if (!localRooms.containsKey(candidateCode)) {
                roomCode = candidateCode
                break
            }
            attempts++
        }

        if (roomCode.isEmpty()) {
            roomCode = generateRandomCode(6)
        }

        val now = System.currentTimeMillis()
        val room = Room(
            roomCode = roomCode,
            hostId = user.uid,
            djId = null,
            videoId = initialVideoId,
            state = "paused",
            position = 0.0,
            updatedAt = now,
            videoTitle = if (initialVideoId.isNotEmpty()) "Synchronized Playback" else "",
            isPublic = true,
            memberCount = 1
        )

        getOrCreateLocalRoom(roomCode).value = room
        getOrCreateLocalMembers(roomCode).value = listOf(
            Member(
                uid = user.uid,
                name = user.displayName,
                photoUrl = user.photoUrl ?: "",
                joinedAt = now,
                isHost = true
            )
        )
        updatePublicRoomsList()

        try {
            withTimeoutOrNull(4000L) {
                val roomsRef = database.getReference("rooms").child(roomCode)
                val roomData = hashMapOf<String, Any>(
                    "hostId" to user.uid,
                    "videoId" to initialVideoId,
                    "state" to "paused",
                    "position" to 0.0,
                    "updatedAt" to ServerValue.TIMESTAMP,
                    "videoTitle" to (if (initialVideoId.isNotEmpty()) "Synchronized Playback" else ""),
                    "isPublic" to true,
                    "theme" to "CYBER_NEON",
                    "visualizerStyle" to "CIRCULAR"
                )
                roomsRef.setValue(roomData).await()

                val memberData = hashMapOf<String, Any>(
                    "name" to user.displayName,
                    "photoUrl" to (user.photoUrl ?: ""),
                    "joinedAt" to ServerValue.TIMESTAMP
                )
                val memberRef = roomsRef.child("members").child(user.uid)
                memberRef.setValue(memberData).await()
                memberRef.onDisconnect().removeValue()
            }
        } catch (_: Exception) {}

        return Result.success(roomCode)
    }

    override suspend fun joinRoom(roomCode: String, user: UserAccount): Result<Room> {
        val upperCode = roomCode.trim().uppercase()
        val now = System.currentTimeMillis()

        val localRoom = localRooms[upperCode]?.value
        if (localRoom != null) {
            val currentMembers = getOrCreateLocalMembers(upperCode).value
            if (currentMembers.none { it.uid == user.uid }) {
                getOrCreateLocalMembers(upperCode).value = currentMembers + Member(
                    uid = user.uid,
                    name = user.displayName,
                    photoUrl = user.photoUrl ?: "",
                    joinedAt = now,
                    isHost = (user.uid == localRoom.hostId)
                )
            }
            updatePublicRoomsList()
            scope.launch {
                try {
                    val memberData = hashMapOf<String, Any>(
                        "name" to user.displayName,
                        "photoUrl" to (user.photoUrl ?: ""),
                        "joinedAt" to ServerValue.TIMESTAMP
                    )
                    val memberRef = database.getReference("rooms").child(upperCode).child("members").child(user.uid)
                    memberRef.setValue(memberData).await()
                    memberRef.onDisconnect().removeValue()
                } catch (_: Exception) {}
            }
            return Result.success(localRoom)
        }

        var remoteRoom: Room? = null
        try {
            val snapshot = database.getReference("rooms").child(upperCode).get().await()
            if (snapshot.exists()) {
                val hostId = snapshot.child("hostId").getValue(String::class.java) ?: ""
                val djId = snapshot.child("djId").getValue(String::class.java)
                val videoId = snapshot.child("videoId").getValue(String::class.java) ?: "dQw4w9WgXcQ"
                val state = snapshot.child("state").getValue(String::class.java) ?: "paused"
                val position = snapshot.child("position").getValue(Double::class.java)
                    ?: (snapshot.child("position").getValue(Long::class.java)?.toDouble() ?: 0.0)
                val updatedAt = snapshot.child("updatedAt").getValue(Long::class.java) ?: now
                val videoTitle = snapshot.child("videoTitle").getValue(String::class.java) ?: ""
                val isPublic = snapshot.child("isPublic").getValue(Boolean::class.java) ?: true
                val theme = snapshot.child("theme").getValue(String::class.java) ?: "CYBER_NEON"
                val visualizerStyle = snapshot.child("visualizerStyle").getValue(String::class.java) ?: "CIRCULAR"

                remoteRoom = Room(
                    roomCode = upperCode,
                    hostId = hostId,
                    djId = djId,
                    videoId = videoId,
                    state = state,
                    position = position,
                    updatedAt = updatedAt,
                    videoTitle = videoTitle,
                    isPublic = isPublic,
                    theme = theme,
                    visualizerStyle = visualizerStyle
                )
                getOrCreateLocalRoom(upperCode).value = remoteRoom

                scope.launch {
                    try {
                        val memberData = hashMapOf<String, Any>(
                            "name" to user.displayName,
                            "photoUrl" to (user.photoUrl ?: ""),
                            "joinedAt" to ServerValue.TIMESTAMP
                        )
                        val memberRef = database.getReference("rooms").child(upperCode).child("members").child(user.uid)
                        memberRef.setValue(memberData).await()
                        memberRef.onDisconnect().removeValue()

                        // Broadcast join message to chat
                        val sysMsgId = "sys_join_${System.currentTimeMillis()}_${(100..999).random()}"
                        val joinMsg = hashMapOf<String, Any>(
                            "id" to sysMsgId,
                            "senderId" to "system",
                            "senderName" to "DEMONIC",
                            "text" to "${user.displayName} joined the room 👋",
                            "sentAt" to ServerValue.TIMESTAMP
                        )
                        database.getReference("rooms").child(upperCode).child("messages").child(sysMsgId).setValue(joinMsg)
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}

        if (remoteRoom != null) {
            updatePublicRoomsList()
            return Result.success(remoteRoom)
        }

        val fallbackRoom = Room(
            roomCode = upperCode,
            hostId = user.uid,
            videoId = "dQw4w9WgXcQ",
            state = "paused",
            position = 0.0,
            updatedAt = now,
            videoTitle = "Synchronized Playback",
            isPublic = true
        )
        getOrCreateLocalRoom(upperCode).value = fallbackRoom
        getOrCreateLocalMembers(upperCode).value = listOf(
            Member(
                uid = user.uid,
                name = user.displayName,
                photoUrl = user.photoUrl ?: "",
                joinedAt = now,
                isHost = true
            )
        )
        updatePublicRoomsList()
        return Result.success(fallbackRoom)
    }

    override suspend fun leaveRoom(roomCode: String, uid: String) {
        val upperCode = roomCode.trim().uppercase()
        val currentMembers = localMembers[upperCode]?.value ?: emptyList()
        val leavingMember = currentMembers.find { it.uid == uid }
        val displayName = leavingMember?.name ?: "A member"
        localMembers[upperCode]?.value = currentMembers.filter { it.uid != uid }
        updatePublicRoomsList()

        scope.launch {
            try {
                database.getReference("rooms").child(upperCode).child("members").child(uid).removeValue().await()
                database.getReference("rooms").child(upperCode).child("typing").child(uid).removeValue().await()

                // Broadcast leave message to chat
                val sysMsgId = "sys_leave_${System.currentTimeMillis()}_${(100..999).random()}"
                val leaveMsg = hashMapOf<String, Any>(
                    "id" to sysMsgId,
                    "senderId" to "system",
                    "senderName" to "DEMONIC",
                    "text" to "$displayName left the room 🚪",
                    "sentAt" to ServerValue.TIMESTAMP
                )
                database.getReference("rooms").child(upperCode).child("messages").child(sysMsgId).setValue(leaveMsg)
            } catch (_: Exception) {}
        }
    }
    override fun observeRoom(roomCode: String): Flow<Room?> {
        val upperCode = roomCode.trim().uppercase()
        val localFlow = getOrCreateLocalRoom(upperCode)

        val firebaseFlow = callbackFlow {
            val roomRef = database.getReference("rooms").child(upperCode)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val hostId = snapshot.child("hostId").getValue(String::class.java) ?: ""
                        val djId = snapshot.child("djId").getValue(String::class.java)
                        val videoId = snapshot.child("videoId").getValue(String::class.java) ?: ""
                        val state = snapshot.child("state").getValue(String::class.java) ?: "paused"
                        val position = snapshot.child("position").getValue(Double::class.java)
                            ?: (snapshot.child("position").getValue(Long::class.java)?.toDouble() ?: 0.0)
                        val updatedAt = snapshot.child("updatedAt").getValue(Long::class.java) ?: 0L
                        val videoTitle = snapshot.child("videoTitle").getValue(String::class.java) ?: ""
                        val isPublic = snapshot.child("isPublic").getValue(Boolean::class.java) ?: true
                        val theme = snapshot.child("theme").getValue(String::class.java) ?: "CYBER_NEON"
                        val visualizerStyle = snapshot.child("visualizerStyle").getValue(String::class.java) ?: "CIRCULAR"

                        val room = Room(
                            roomCode = upperCode,
                            hostId = hostId,
                            djId = djId,
                            videoId = videoId,
                            state = state,
                            position = position,
                            updatedAt = updatedAt,
                            videoTitle = videoTitle,
                            isPublic = isPublic,
                            theme = theme,
                            visualizerStyle = visualizerStyle
                        )
                        localFlow.value = room
                        trySend(room)
                    } else {
                        localFlow.value = null
                        trySend(null)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            try {
                roomRef.addValueEventListener(listener)
            } catch (_: Exception) {}
            awaitClose {
                try { roomRef.removeEventListener(listener) } catch (_: Exception) {}
            }
        }

        val cached = localFlow.value
        return if (cached != null) {
            merge(flowOf(cached), firebaseFlow)
        } else {
            firebaseFlow
        }
    }

    override fun observeMembers(roomCode: String, hostId: String): Flow<List<Member>> {
        val upperCode = roomCode.trim().uppercase()
        val localFlow = getOrCreateLocalMembers(upperCode)

        val firebaseFlow = callbackFlow {
            val membersRef = database.getReference("rooms").child(upperCode).child("members")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val list = mutableListOf<Member>()
                        for (child in snapshot.children) {
                            val uid = child.key ?: continue
                            val name = child.child("name").getValue(String::class.java) ?: "Guest"
                            val photoUrl = child.child("photoUrl").getValue(String::class.java) ?: ""
                            val joinedAt = child.child("joinedAt").getValue(Long::class.java) ?: 0L
                            val timedOutUntil = child.child("timedOutUntil").getValue(Long::class.java) ?: 0L
                            list.add(
                                Member(
                                    uid = uid,
                                    name = name,
                                    photoUrl = photoUrl,
                                    joinedAt = joinedAt,
                                    isHost = (uid == hostId),
                                    timedOutUntil = timedOutUntil
                                )
                            )
                        }
                        val sorted = list.sortedWith(
                            compareByDescending<Member> { it.isHost }.thenBy { it.joinedAt }
                        )
                        localFlow.value = sorted
                        trySend(sorted)
                    } else {
                        localFlow.value = emptyList()
                        trySend(emptyList())
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            try {
                membersRef.addValueEventListener(listener)
            } catch (_: Exception) {}
            awaitClose {
                try { membersRef.removeEventListener(listener) } catch (_: Exception) {}
            }
        }

        return merge(localFlow, firebaseFlow)
    }

    override fun observeMessages(roomCode: String): Flow<ChatMessage> {
        val upperCode = roomCode.trim().uppercase()
        val localFlow = getOrCreateLocalMessages(upperCode)

        val firebaseFlow = callbackFlow {
            val messagesRef = database.getReference("rooms").child(upperCode).child("messages")
            val listener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val id = snapshot.child("id").getValue(String::class.java) ?: snapshot.key ?: ""
                    val senderId = snapshot.child("senderId").getValue(String::class.java) ?: ""
                    val senderName = snapshot.child("senderName").getValue(String::class.java) ?: "Unknown"
                    val senderPhotoUrl = snapshot.child("senderPhotoUrl").getValue(String::class.java) ?: ""
                    val text = snapshot.child("text").getValue(String::class.java) ?: ""
                    val sentAt = snapshot.child("sentAt").getValue(Long::class.java) ?: System.currentTimeMillis()
                    val replyToMessageId = snapshot.child("replyToMessageId").getValue(String::class.java) ?: ""
                    val replyToSenderName = snapshot.child("replyToSenderName").getValue(String::class.java) ?: ""
                    val replyToText = snapshot.child("replyToText").getValue(String::class.java) ?: ""
                    val senderRole = snapshot.child("senderRole").getValue(String::class.java) ?: ""

                    val msg = ChatMessage(
                        id = id,
                        senderId = senderId,
                        senderName = senderName,
                        senderPhotoUrl = senderPhotoUrl,
                        text = text,
                        sentAt = sentAt,
                        replyToMessageId = replyToMessageId,
                        replyToSenderName = replyToSenderName,
                        replyToText = replyToText,
                        senderRole = senderRole
                    )
                    trySend(msg)
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            }
            try {
                messagesRef.addChildEventListener(listener)
            } catch (_: Exception) {}
            awaitClose {
                try { messagesRef.removeEventListener(listener) } catch (_: Exception) {}
            }
        }

        return merge(localFlow, firebaseFlow)
    }

    override fun observeDeletedMessageIds(roomCode: String): Flow<String> = callbackFlow {
        val upperCode = roomCode.trim().uppercase()
        val messagesRef = database.getReference("rooms").child(upperCode).child("messages")
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val id = snapshot.child("id").getValue(String::class.java) ?: snapshot.key ?: ""
                if (id.isNotEmpty()) {
                    trySend(id)
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        try {
            messagesRef.addChildEventListener(listener)
        } catch (_: Exception) {}
        awaitClose {
            try { messagesRef.removeEventListener(listener) } catch (_: Exception) {}
        }
    }

    override fun observeTypingUsers(roomCode: String, currentUid: String): Flow<List<String>> {
        val upperCode = roomCode.trim().uppercase()
        val localFlow = getOrCreateLocalTyping(upperCode)

        val firebaseFlow = callbackFlow {
            val typingRef = database.getReference("rooms").child(upperCode).child("typing")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val typingList = mutableListOf<String>()
                    for (child in snapshot.children) {
                        val uid = child.key ?: continue
                        if (uid == currentUid) continue
                        val isTyping = child.child("isTyping").getValue(Boolean::class.java) ?: false
                        val userName = child.child("name").getValue(String::class.java) ?: "Someone"
                        if (isTyping) {
                            typingList.add(userName)
                        }
                    }
                    localFlow.value = typingList
                    trySend(typingList)
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            try {
                typingRef.addValueEventListener(listener)
            } catch (_: Exception) {}
            awaitClose {
                try { typingRef.removeEventListener(listener) } catch (_: Exception) {}
            }
        }

        return merge(localFlow, firebaseFlow)
    }
    override fun observeQueue(roomCode: String): Flow<List<QueueItem>> {
        val upperCode = roomCode.trim().uppercase()
        val localFlow = getOrCreateLocalQueue(upperCode)

        val firebaseFlow = callbackFlow {
            val queueRef = database.getReference("rooms").child(upperCode).child("queue")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<QueueItem>()
                    for (child in snapshot.children) {
                        val id = child.key ?: continue
                        val videoId = child.child("videoId").getValue(String::class.java) ?: ""
                        val title = child.child("title").getValue(String::class.java) ?: ""
                        val thumbnailUrl = child.child("thumbnailUrl").getValue(String::class.java) ?: ""
                        val addedByUid = child.child("addedByUid").getValue(String::class.java) ?: ""
                        val addedByName = child.child("addedByName").getValue(String::class.java) ?: ""
                        val addedAt = child.child("addedAt").getValue(Long::class.java) ?: 0L

                        val upvotesMap = mutableMapOf<String, Boolean>()
                        child.child("upvotes").children.forEach { upvoteChild ->
                            upvoteChild.key?.let { upvotesMap[it] = true }
                        }

                        list.add(
                            QueueItem(
                                id = id,
                                videoId = videoId,
                                title = title,
                                thumbnailUrl = thumbnailUrl,
                                addedByUid = addedByUid,
                                addedByName = addedByName,
                                addedAt = addedAt,
                                upvotes = upvotesMap
                            )
                        )
                    }
                    val sorted = list.sortedByDescending { it.upvotes.size }
                    localFlow.value = sorted
                    trySend(sorted)
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            try {
                queueRef.addValueEventListener(listener)
            } catch (_: Exception) {}
            awaitClose {
                try { queueRef.removeEventListener(listener) } catch (_: Exception) {}
            }
        }

        return merge(localFlow, firebaseFlow)
    }

    override fun observeReactions(roomCode: String): Flow<LiveReaction> {
        val upperCode = roomCode.trim().uppercase()
        val localFlow = getOrCreateLocalReactions(upperCode)

        val firebaseFlow = callbackFlow {
            val reactionsRef = database.getReference("rooms").child(upperCode).child("reactions")
            val listener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val id = snapshot.key ?: ""
                    val emoji = snapshot.child("emoji").getValue(String::class.java) ?: "🔥"
                    val senderName = snapshot.child("senderName").getValue(String::class.java) ?: ""
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                    val reaction = LiveReaction(id, emoji, senderName, timestamp)
                    trySend(reaction)
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            }
            try {
                reactionsRef.addChildEventListener(listener)
            } catch (_: Exception) {}
            awaitClose {
                try { reactionsRef.removeEventListener(listener) } catch (_: Exception) {}
            }
        }

        return merge(localFlow, firebaseFlow)
    }

    override fun observePublicRooms(): Flow<List<Room>> {
        val firebaseFlow = callbackFlow {
            val roomsRef = database.getReference("rooms")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Room>()
                    for (child in snapshot.children) {
                        val code = child.key ?: continue
                        val isPublic = child.child("isPublic").getValue(Boolean::class.java) ?: true
                        if (!isPublic) continue

                        val hostId = child.child("hostId").getValue(String::class.java) ?: ""
                        val djId = child.child("djId").getValue(String::class.java)
                        val videoId = child.child("videoId").getValue(String::class.java) ?: ""
                        val state = child.child("state").getValue(String::class.java) ?: "paused"
                        val position = child.child("position").getValue(Double::class.java) ?: 0.0
                        val updatedAt = child.child("updatedAt").getValue(Long::class.java) ?: 0L
                        val videoTitle = child.child("videoTitle").getValue(String::class.java) ?: "Demonic Room"
                        val memberCount = child.child("members").childrenCount.toInt().coerceAtLeast(1)

                        list.add(
                            Room(
                                roomCode = code,
                                hostId = hostId,
                                djId = djId,
                                videoId = videoId,
                                state = state,
                                position = position,
                                updatedAt = updatedAt,
                                videoTitle = videoTitle,
                                isPublic = true,
                                memberCount = memberCount
                            )
                        )
                    }
                    val combined = (localPublicRooms.value + list).distinctBy { it.roomCode }
                    trySend(combined)
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            try {
                roomsRef.addValueEventListener(listener)
            } catch (_: Exception) {}
            awaitClose {
                try { roomsRef.removeEventListener(listener) } catch (_: Exception) {}
            }
        }

        return merge(localPublicRooms, firebaseFlow)
    }

    override suspend fun updatePlaybackState(
        roomCode: String,
        state: String,
        positionSeconds: Double,
        videoId: String?,
        videoTitle: String?
    ): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        val current = localRooms[upperCode]?.value ?: return Result.success(Unit)
        val updated = current.copy(
            state = state,
            position = positionSeconds,
            updatedAt = System.currentTimeMillis(),
            videoId = videoId ?: current.videoId,
            videoTitle = videoTitle ?: current.videoTitle
        )
        localRooms[upperCode]?.value = updated
        updatePublicRoomsList()

        scope.launch {
            try {
                val roomRef = database.getReference("rooms").child(upperCode)
                val updates = hashMapOf<String, Any>(
                    "state" to state,
                    "position" to positionSeconds,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
                videoId?.let { updates["videoId"] = it }
                videoTitle?.let { updates["videoTitle"] = it }
                roomRef.updateChildren(updates).await()
            } catch (_: Exception) {}
        }

        return Result.success(Unit)
    }

    override suspend fun deleteRoom(roomCode: String): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        localRooms[upperCode]?.value = null
        localRooms.remove(upperCode)
        localMembers.remove(upperCode)
        localMessages.remove(upperCode)
        localQueues.remove(upperCode)
        localTyping.remove(upperCode)
        localReactions.remove(upperCode)
        updatePublicRoomsList()

        return try {
            database.getReference("rooms").child(upperCode).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun passTheAux(roomCode: String, djUid: String): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        val current = localRooms[upperCode]?.value
        if (current != null) {
            getOrCreateLocalRoom(upperCode).value = current.copy(djId = djUid.ifEmpty { null })
        }

        scope.launch {
            try {
                val roomRef = database.getReference("rooms").child(upperCode)
                if (djUid.isEmpty()) {
                    roomRef.child("djId").removeValue().await()
                } else {
                    roomRef.child("djId").setValue(djUid).await()
                }
            } catch (_: Exception) {}
        }

        return Result.success(Unit)
    }

    override suspend fun addToQueue(roomCode: String, item: QueueItem): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        val queueFlow = getOrCreateLocalQueue(upperCode)
        val current = queueFlow.value
        queueFlow.value = current + item

        scope.launch {
            try {
                val itemRef = database.getReference("rooms").child(upperCode).child("queue").child(item.id)
                val data = hashMapOf<String, Any>(
                    "videoId" to item.videoId,
                    "title" to item.title,
                    "thumbnailUrl" to item.thumbnailUrl,
                    "addedByUid" to item.addedByUid,
                    "addedByName" to item.addedByName,
                    "addedAt" to ServerValue.TIMESTAMP
                )
                itemRef.setValue(data).await()
            } catch (_: Exception) {}
        }

        return Result.success(Unit)
    }

    override suspend fun addMultipleToQueue(roomCode: String, items: List<QueueItem>): Result<Unit> {
        if (items.isEmpty()) return Result.success(Unit)
        val upperCode = roomCode.trim().uppercase()
        val queueFlow = getOrCreateLocalQueue(upperCode)
        queueFlow.value = queueFlow.value + items

        scope.launch {
            try {
                val updates = hashMapOf<String, Any>()
                items.forEach { item ->
                    updates["rooms/$upperCode/queue/${item.id}"] = hashMapOf(
                        "videoId" to item.videoId,
                        "title" to item.title,
                        "thumbnailUrl" to item.thumbnailUrl,
                        "addedByUid" to item.addedByUid,
                        "addedByName" to item.addedByName,
                        "addedAt" to ServerValue.TIMESTAMP
                    )
                }
                database.reference.updateChildren(updates).await()
            } catch (_: Exception) {}
        }

        return Result.success(Unit)
    }

    override suspend fun removeFromQueue(roomCode: String, itemId: String): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        val queueFlow = getOrCreateLocalQueue(upperCode)
        queueFlow.value = queueFlow.value.filter { it.id != itemId }

        scope.launch {
            try {
                database.getReference("rooms").child(upperCode).child("queue").child(itemId).removeValue().await()
            } catch (_: Exception) {}
        }

        return Result.success(Unit)
    }

    override suspend fun reorderQueue(roomCode: String, newQueue: List<QueueItem>): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        getOrCreateLocalQueue(upperCode).value = newQueue
        return Result.success(Unit)
    }

    override suspend fun upvoteQueueItem(roomCode: String, itemId: String, uid: String): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        val queueFlow = getOrCreateLocalQueue(upperCode)
        val current = queueFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == itemId }
        if (index != -1) {
            val item = current[index]
            val upvotes = item.upvotes.toMutableMap()
            if (upvotes.containsKey(uid)) {
                upvotes.remove(uid)
            } else {
                upvotes[uid] = true
            }
            current[index] = item.copy(upvotes = upvotes)
            queueFlow.value = current.sortedByDescending { it.upvotes.size }
        }

        scope.launch {
            try {
                val upvoteRef = database.getReference("rooms").child(upperCode).child("queue").child(itemId).child("upvotes").child(uid)
                val snap = upvoteRef.get().await()
                if (snap.exists()) {
                    upvoteRef.removeValue().await()
                } else {
                    upvoteRef.setValue(true).await()
                }
            } catch (_: Exception) {}
        }

        return Result.success(Unit)
    }

    override suspend fun sendReaction(roomCode: String, reaction: LiveReaction): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        getOrCreateLocalReactions(upperCode).emit(reaction)

        scope.launch {
            try {
                val reactionsRef = database.getReference("rooms").child(upperCode).child("reactions")
                val newRef = reactionsRef.push()
                val data = hashMapOf<String, Any>(
                    "emoji" to reaction.emoji,
                    "senderName" to reaction.senderName,
                    "timestamp" to ServerValue.TIMESTAMP
                )
                newRef.setValue(data).await()
            } catch (_: Exception) {}
        }

        return Result.success(Unit)
    }

    override suspend fun sendMessage(
        roomCode: String,
        user: UserAccount,
        text: String,
        replyToMessageId: String,
        replyToSenderName: String,
        replyToText: String,
        senderRole: String
    ): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        val msgId = "msg_" + System.currentTimeMillis() + "_" + (1000..9999).random()
        val msg = ChatMessage(
            id = msgId,
            senderId = user.uid,
            senderName = user.displayName,
            senderPhotoUrl = user.photoUrl ?: "",
            text = text.trim(),
            sentAt = System.currentTimeMillis(),
            replyToMessageId = replyToMessageId,
            replyToSenderName = replyToSenderName,
            replyToText = replyToText,
            senderRole = senderRole
        )
        getOrCreateLocalMessages(upperCode).emit(msg)

        scope.launch {
            try {
                val messagesRef = database.getReference("rooms").child(upperCode).child("messages")
                val msgRef = messagesRef.child(msgId)
                val msgData = hashMapOf<String, Any>(
                    "id" to msgId,
                    "senderId" to user.uid,
                    "senderName" to user.displayName,
                    "senderPhotoUrl" to (user.photoUrl ?: ""),
                    "text" to text.trim(),
                    "sentAt" to ServerValue.TIMESTAMP,
                    "replyToMessageId" to replyToMessageId,
                    "replyToSenderName" to replyToSenderName,
                    "replyToText" to replyToText,
                    "senderRole" to senderRole
                )
                msgRef.setValue(msgData).await()
            } catch (_: Exception) {}
        }

        return Result.success(Unit)
    }

    override suspend fun setTyping(roomCode: String, uid: String, userName: String, isTyping: Boolean) {
        val upperCode = roomCode.trim().uppercase()
        val currentTyping = getOrCreateLocalTyping(upperCode).value
        if (isTyping && !currentTyping.contains(userName)) {
            getOrCreateLocalTyping(upperCode).value = currentTyping + userName
        } else if (!isTyping) {
            getOrCreateLocalTyping(upperCode).value = currentTyping.filter { it != userName }
        }

        scope.launch {
            try {
                val userTypingRef = database.getReference("rooms").child(upperCode).child("typing").child(uid)
                if (isTyping) {
                    userTypingRef.setValue(hashMapOf("name" to userName, "isTyping" to true)).await()
                } else {
                    userTypingRef.removeValue().await()
                }
            } catch (_: Exception) {}
        }
    }

    override suspend fun deleteMessage(roomCode: String, messageId: String): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        scope.launch {
            try {
                database.getReference("rooms").child(upperCode).child("messages").child(messageId).removeValue().await()
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    override suspend fun timeoutMember(
        roomCode: String,
        uid: String,
        durationMinutes: Int,
        hostName: String,
        targetName: String
    ): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        val until = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        scope.launch {
            try {
                database.getReference("rooms").child(upperCode).child("members").child(uid)
                    .child("timedOutUntil").setValue(until).await()

                val sysMsgId = "sys_to_${System.currentTimeMillis()}_${(100..999).random()}"
                val sysMsg = hashMapOf<String, Any>(
                    "id" to sysMsgId,
                    "senderId" to "system",
                    "senderName" to "DEMONIC",
                    "text" to "$hostName timed out $targetName for $durationMinutes min ⏱️",
                    "sentAt" to ServerValue.TIMESTAMP
                )
                database.getReference("rooms").child(upperCode).child("messages").child(sysMsgId).setValue(sysMsg)
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    override suspend fun removeTimeout(
        roomCode: String,
        uid: String,
        hostName: String,
        targetName: String
    ): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        scope.launch {
            try {
                database.getReference("rooms").child(upperCode).child("members").child(uid)
                    .child("timedOutUntil").setValue(0L).await()

                val sysMsgId = "sys_unmute_${System.currentTimeMillis()}_${(100..999).random()}"
                val sysMsg = hashMapOf<String, Any>(
                    "id" to sysMsgId,
                    "senderId" to "system",
                    "senderName" to "DEMONIC",
                    "text" to "$hostName removed timeout for $targetName 🔊",
                    "sentAt" to ServerValue.TIMESTAMP
                )
                database.getReference("rooms").child(upperCode).child("messages").child(sysMsgId).setValue(sysMsg)
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    override suspend fun setRoomTheme(roomCode: String, theme: String): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        localRooms[upperCode]?.value?.let { currentRoom ->
            localRooms[upperCode]?.value = currentRoom.copy(theme = theme)
        }
        scope.launch {
            try {
                database.getReference("rooms").child(upperCode).child("theme").setValue(theme).await()
                val themePreset = RoomThemePreset.fromId(theme)
                val sysMsgId = "sys_th_${System.currentTimeMillis()}_${(100..999).random()}"
                val sysMsg = hashMapOf<String, Any>(
                    "id" to sysMsgId,
                    "senderId" to "system",
                    "senderName" to "DEMONIC",
                    "text" to "🎨 Room aesthetic switched to ${themePreset.emoji} ${themePreset.displayName}",
                    "sentAt" to ServerValue.TIMESTAMP
                )
                database.getReference("rooms").child(upperCode).child("messages").child(sysMsgId).setValue(sysMsg)
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    override suspend fun setVisualizerStyle(roomCode: String, style: String): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        localRooms[upperCode]?.value?.let { currentRoom ->
            localRooms[upperCode]?.value = currentRoom.copy(visualizerStyle = style)
        }
        scope.launch {
            try {
                database.getReference("rooms").child(upperCode).child("visualizerStyle").setValue(style).await()
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    override suspend fun triggerSpecialEffect(roomCode: String, effect: RoomSpecialEffect): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        val effectId = if (effect.id.isNotEmpty()) effect.id else "fx_${System.currentTimeMillis()}_${(100..999).random()}"
        val updatedEffect = effect.copy(id = effectId, timestamp = System.currentTimeMillis())

        getOrCreateLocalSpecialEffects(upperCode).emit(updatedEffect)

        scope.launch {
            try {
                val fxData = hashMapOf<String, Any>(
                    "id" to updatedEffect.id,
                    "type" to updatedEffect.type,
                    "senderName" to updatedEffect.senderName,
                    "targetName" to updatedEffect.targetName,
                    "customMessage" to updatedEffect.customMessage,
                    "timestamp" to ServerValue.TIMESTAMP
                )
                database.getReference("rooms").child(upperCode).child("activeEffects").child(effectId).setValue(fxData).await()

                val effectPreset = SpecialEffectType.fromId(updatedEffect.type)
                val chatText = when (updatedEffect.type) {
                    SpecialEffectType.LOVE_EXPLOSION.id -> {
                        if (updatedEffect.targetName.isNotBlank()) {
                            "💖 ${updatedEffect.senderName} sent a Romantic Love Blast to ${updatedEffect.targetName}: \"${updatedEffect.customMessage}\" 🌹"
                        } else {
                            "💖 ${updatedEffect.senderName} sent a Romantic Love Blast: \"${updatedEffect.customMessage}\" 🌹"
                        }
                    }
                    SpecialEffectType.PARTY_FLAMES.id -> "🔥 ${updatedEffect.senderName} dropped the Bass Fire Blast! 💥"
                    SpecialEffectType.CROWN_VIP.id -> "👑 ${updatedEffect.senderName} triggered VIP Royal Vibes! ✨"
                    SpecialEffectType.MATRIX_RAIN.id -> "⚡ ${updatedEffect.senderName} initiated Matrix Cyber Overload! 👾"
                    SpecialEffectType.DEMONIC_SURGE.id -> "💀 ${updatedEffect.senderName} unleashed Demonic Rave Energy! ⚡"
                    else -> "✨ ${updatedEffect.senderName} triggered ${effectPreset?.title ?: "Special FX"}!"
                }

                val sysMsgId = "sys_fx_${System.currentTimeMillis()}_${(100..999).random()}"
                val sysMsg = hashMapOf<String, Any>(
                    "id" to sysMsgId,
                    "senderId" to "system",
                    "senderName" to "DEMONIC FX",
                    "text" to chatText,
                    "sentAt" to ServerValue.TIMESTAMP
                )
                database.getReference("rooms").child(upperCode).child("messages").child(sysMsgId).setValue(sysMsg)
            } catch (_: Exception) {}
        }
        return Result.success(Unit)
    }

    override fun observeSpecialEffects(roomCode: String): Flow<RoomSpecialEffect> {
        val upperCode = roomCode.trim().uppercase()
        val localFlow = getOrCreateLocalSpecialEffects(upperCode)

        val firebaseFlow = callbackFlow {
            val effectsRef = database.getReference("rooms").child(upperCode).child("activeEffects")
            val listener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val id = snapshot.key ?: ""
                    val type = snapshot.child("type").getValue(String::class.java) ?: SpecialEffectType.LOVE_EXPLOSION.id
                    val senderName = snapshot.child("senderName").getValue(String::class.java) ?: ""
                    val targetName = snapshot.child("targetName").getValue(String::class.java) ?: ""
                    val customMessage = snapshot.child("customMessage").getValue(String::class.java) ?: ""
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: System.currentTimeMillis()

                    val effect = RoomSpecialEffect(id, type, senderName, targetName, customMessage, timestamp)
                    trySend(effect)
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            }
            try {
                effectsRef.addChildEventListener(listener)
            } catch (_: Exception) {}
            awaitClose {
                try { effectsRef.removeEventListener(listener) } catch (_: Exception) {}
            }
        }

        return merge(localFlow, firebaseFlow)
    }
}
