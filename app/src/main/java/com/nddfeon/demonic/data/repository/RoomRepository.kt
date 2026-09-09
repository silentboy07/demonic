package com.nddfeon.demonic.data.repository

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.nddfeon.demonic.data.model.ChatMessage
import com.nddfeon.demonic.data.model.Member
import com.nddfeon.demonic.data.model.Room
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

    suspend fun createRoom(user: UserAccount, initialVideoId: String = "dQw4w9WgXcQ"): Result<String>
    suspend fun joinRoom(roomCode: String, user: UserAccount): Result<Room>
    suspend fun leaveRoom(roomCode: String, uid: String)

    fun observeRoom(roomCode: String): Flow<Room?>
    fun observeMembers(roomCode: String, hostId: String): Flow<List<Member>>
    fun observeMessages(roomCode: String): Flow<ChatMessage>
    fun observeTypingUsers(roomCode: String, currentUid: String): Flow<List<String>>

    suspend fun updatePlaybackState(
        roomCode: String,
        state: String,
        positionSeconds: Double,
        videoId: String? = null,
        videoTitle: String? = null
    ): Result<Unit>

    suspend fun sendMessage(
        roomCode: String,
        user: UserAccount,
        text: String
    ): Result<Unit>

    suspend fun setTyping(roomCode: String, uid: String, userName: String, isTyping: Boolean)
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

    // Resilient local state cache for instant testing / offline / mock fallback
    private val localRooms = ConcurrentHashMap<String, MutableStateFlow<Room?>>()
    private val localMembers = ConcurrentHashMap<String, MutableStateFlow<List<Member>>>()
    private val localMessages = ConcurrentHashMap<String, MutableSharedFlow<ChatMessage>>()
    private val localTyping = ConcurrentHashMap<String, MutableStateFlow<List<String>>>()

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

    private fun getOrCreateLocalRoom(code: String): MutableStateFlow<Room?> {
        return localRooms.getOrPut(code) { MutableStateFlow(null) }
    }

    private fun getOrCreateLocalMembers(code: String): MutableStateFlow<List<Member>> {
        return localMembers.getOrPut(code) { MutableStateFlow(emptyList()) }
    }

    private fun getOrCreateLocalMessages(code: String): MutableSharedFlow<ChatMessage> {
        return localMessages.getOrPut(code) { MutableSharedFlow(replay = 20) }
    }

    private fun getOrCreateLocalTyping(code: String): MutableStateFlow<List<String>> {
        return localTyping.getOrPut(code) { MutableStateFlow(emptyList()) }
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
            videoId = initialVideoId,
            state = "paused",
            position = 0.0,
            updatedAt = now,
            videoTitle = "Synchronized Playback"
        )

        // Set local state immediately for zero-lag UI response
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

        // Try syncing to Firebase in background with safety timeout
        scope.launch {
            try {
                withTimeoutOrNull(3000L) {
                    val roomsRef = database.getReference("rooms").child(roomCode)
                    val roomData = hashMapOf<String, Any>(
                        "hostId" to user.uid,
                        "videoId" to initialVideoId,
                        "state" to "paused",
                        "position" to 0.0,
                        "updatedAt" to ServerValue.TIMESTAMP,
                        "videoTitle" to "Synchronized Playback"
                    )
                    roomsRef.setValue(roomData).await()

                    val memberData = hashMapOf<String, Any>(
                        "name" to user.displayName,
                        "photoUrl" to (user.photoUrl ?: ""),
                        "joinedAt" to ServerValue.TIMESTAMP
                    )
                    roomsRef.child("members").child(user.uid).setValue(memberData).await()
                }
            } catch (_: Exception) {}
        }

        return Result.success(roomCode)
    }

    override suspend fun joinRoom(roomCode: String, user: UserAccount): Result<Room> {
        val upperCode = roomCode.trim().uppercase()
        val now = System.currentTimeMillis()

        // 1. Check local cache first
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
            return Result.success(localRoom)
        }

        // 2. Try fetching from Firebase with timeout
        var remoteRoom: Room? = null
        try {
            val snapshot = withTimeoutOrNull(3000L) {
                database.getReference("rooms").child(upperCode).get().await()
            }
            if (snapshot != null && snapshot.exists()) {
                val hostId = snapshot.child("hostId").getValue(String::class.java) ?: ""
                val videoId = snapshot.child("videoId").getValue(String::class.java) ?: "dQw4w9WgXcQ"
                val state = snapshot.child("state").getValue(String::class.java) ?: "paused"
                val position = snapshot.child("position").getValue(Double::class.java)
                    ?: (snapshot.child("position").getValue(Long::class.java)?.toDouble() ?: 0.0)
                val updatedAt = snapshot.child("updatedAt").getValue(Long::class.java) ?: now
                val videoTitle = snapshot.child("videoTitle").getValue(String::class.java) ?: ""

                remoteRoom = Room(
                    roomCode = upperCode,
                    hostId = hostId,
                    videoId = videoId,
                    state = state,
                    position = position,
                    updatedAt = updatedAt,
                    videoTitle = videoTitle
                )
                getOrCreateLocalRoom(upperCode).value = remoteRoom

                // Add member in background
                scope.launch {
                    try {
                        val memberData = hashMapOf<String, Any>(
                            "name" to user.displayName,
                            "photoUrl" to (user.photoUrl ?: ""),
                            "joinedAt" to ServerValue.TIMESTAMP
                        )
                        database.getReference("rooms").child(upperCode).child("members").child(user.uid).setValue(memberData).await()
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}

        if (remoteRoom != null) {
            return Result.success(remoteRoom)
        }

        // 3. If room code was entered in test/demo mode and not found, auto-create/join as listener room
        val fallbackRoom = Room(
            roomCode = upperCode,
            hostId = user.uid,
            videoId = "dQw4w9WgXcQ",
            state = "paused",
            position = 0.0,
            updatedAt = now,
            videoTitle = "Synchronized Playback"
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
        return Result.success(fallbackRoom)
    }

    override suspend fun leaveRoom(roomCode: String, uid: String) {
        val upperCode = roomCode.trim().uppercase()
        val currentMembers = localMembers[upperCode]?.value ?: emptyList()
        localMembers[upperCode]?.value = currentMembers.filter { it.uid != uid }

        scope.launch {
            try {
                database.getReference("rooms").child(upperCode).child("members").child(uid).removeValue().await()
                database.getReference("rooms").child(upperCode).child("typing").child(uid).removeValue().await()
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
                        val videoId = snapshot.child("videoId").getValue(String::class.java) ?: ""
                        val state = snapshot.child("state").getValue(String::class.java) ?: "paused"
                        val position = snapshot.child("position").getValue(Double::class.java)
                            ?: (snapshot.child("position").getValue(Long::class.java)?.toDouble() ?: 0.0)
                        val updatedAt = snapshot.child("updatedAt").getValue(Long::class.java) ?: 0L
                        val videoTitle = snapshot.child("videoTitle").getValue(String::class.java) ?: ""

                        val room = Room(
                            roomCode = upperCode,
                            hostId = hostId,
                            videoId = videoId,
                            state = state,
                            position = position,
                            updatedAt = updatedAt,
                            videoTitle = videoTitle
                        )
                        localFlow.value = room
                        trySend(room)
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

        return merge(localFlow, firebaseFlow)
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
                            list.add(
                                Member(
                                    uid = uid,
                                    name = name,
                                    photoUrl = photoUrl,
                                    joinedAt = joinedAt,
                                    isHost = (uid == hostId)
                                )
                            )
                        }
                        val sorted = list.sortedWith(
                            compareByDescending<Member> { it.isHost }.thenBy { it.joinedAt }
                        )
                        localFlow.value = sorted
                        trySend(sorted)
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
                    val id = snapshot.key ?: ""
                    val senderId = snapshot.child("senderId").getValue(String::class.java) ?: ""
                    val senderName = snapshot.child("senderName").getValue(String::class.java) ?: "Unknown"
                    val senderPhotoUrl = snapshot.child("senderPhotoUrl").getValue(String::class.java) ?: ""
                    val text = snapshot.child("text").getValue(String::class.java) ?: ""
                    val sentAt = snapshot.child("sentAt").getValue(Long::class.java) ?: System.currentTimeMillis()

                    val msg = ChatMessage(
                        id = id,
                        senderId = senderId,
                        senderName = senderName,
                        senderPhotoUrl = senderPhotoUrl,
                        text = text,
                        sentAt = sentAt
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

    override suspend fun updatePlaybackState(
        roomCode: String,
        state: String,
        positionSeconds: Double,
        videoId: String?,
        videoTitle: String?
    ): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        val current = localRooms[upperCode]?.value
        val updated = current?.copy(
            state = state,
            position = positionSeconds,
            updatedAt = System.currentTimeMillis(),
            videoId = videoId ?: current.videoId,
            videoTitle = videoTitle ?: current.videoTitle
        ) ?: Room(
            roomCode = upperCode,
            state = state,
            position = positionSeconds,
            updatedAt = System.currentTimeMillis(),
            videoId = videoId ?: "dQw4w9WgXcQ"
        )
        getOrCreateLocalRoom(upperCode).value = updated

        // Push to Firebase asynchronously
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

    override suspend fun sendMessage(
        roomCode: String,
        user: UserAccount,
        text: String
    ): Result<Unit> {
        val upperCode = roomCode.trim().uppercase()
        val msg = ChatMessage(
            id = "msg_" + System.currentTimeMillis(),
            senderId = user.uid,
            senderName = user.displayName,
            senderPhotoUrl = user.photoUrl ?: "",
            text = text.trim(),
            sentAt = System.currentTimeMillis()
        )
        getOrCreateLocalMessages(upperCode).emit(msg)

        scope.launch {
            try {
                val messagesRef = database.getReference("rooms").child(upperCode).child("messages")
                val newMsgRef = messagesRef.push()
                val msgData = hashMapOf<String, Any>(
                    "senderId" to user.uid,
                    "senderName" to user.displayName,
                    "senderPhotoUrl" to (user.photoUrl ?: ""),
                    "text" to text.trim(),
                    "sentAt" to ServerValue.TIMESTAMP
                )
                newMsgRef.setValue(msgData).await()
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
}
