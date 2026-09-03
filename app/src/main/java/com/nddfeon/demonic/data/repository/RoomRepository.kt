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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
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

    private val _serverTimeOffsetMs = MutableStateFlow(0L)
    override val serverTimeOffsetMs: StateFlow<Long> = _serverTimeOffsetMs.asStateFlow()

    init {
        // Monitor Firebase server time offset
        val offsetRef = database.getReference(".info/serverTimeOffset")
        offsetRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offset = snapshot.getValue(Long::class.java) ?: 0L
                _serverTimeOffsetMs.value = offset
            }
            override fun onCancelled(error: DatabaseError) {
                // Keep previous offset
            }
        })
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

    override suspend fun createRoom(user: UserAccount, initialVideoId: String): Result<String> {
        return try {
            val roomsRef = database.getReference("rooms")
            var roomCode = ""
            var attempts = 0
            val maxAttempts = 10

            while (attempts < maxAttempts) {
                val candidateCode = generateRandomCode(6)
                val checkSnapshot = roomsRef.child(candidateCode).get().await()
                if (!checkSnapshot.exists()) {
                    roomCode = candidateCode
                    break
                }
                attempts++
            }

            if (roomCode.isEmpty()) {
                throw IllegalStateException("Failed to generate unique room code after $maxAttempts attempts")
            }

            val targetRoomRef = roomsRef.child(roomCode)
            val roomData = hashMapOf<String, Any>(
                "hostId" to user.uid,
                "videoId" to initialVideoId,
                "state" to "paused",
                "position" to 0.0,
                "updatedAt" to ServerValue.TIMESTAMP,
                "videoTitle" to "Synchronized Playback"
            )

            targetRoomRef.setValue(roomData).await()

            // Add host to members
            val memberData = hashMapOf<String, Any>(
                "name" to user.displayName,
                "photoUrl" to (user.photoUrl ?: ""),
                "joinedAt" to ServerValue.TIMESTAMP
            )
            targetRoomRef.child("members").child(user.uid).setValue(memberData).await()

            Result.success(roomCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinRoom(roomCode: String, user: UserAccount): Result<Room> {
        return try {
            val upperCode = roomCode.trim().uppercase()
            val roomRef = database.getReference("rooms").child(upperCode)
            val snapshot = roomRef.get().await()

            if (!snapshot.exists()) {
                return Result.failure(IllegalArgumentException("Room '$upperCode' does not exist."))
            }

            val hostId = snapshot.child("hostId").getValue(String::class.java) ?: ""
            val videoId = snapshot.child("videoId").getValue(String::class.java) ?: ""
            val state = snapshot.child("state").getValue(String::class.java) ?: "paused"
            val position = snapshot.child("position").getValue(Double::class.java)
                ?: (snapshot.child("position").getValue(Long::class.java)?.toDouble() ?: 0.0)
            val updatedAt = snapshot.child("updatedAt").getValue(Long::class.java) ?: 0L
            val videoTitle = snapshot.child("videoTitle").getValue(String::class.java) ?: ""

            val memberData = hashMapOf<String, Any>(
                "name" to user.displayName,
                "photoUrl" to (user.photoUrl ?: ""),
                "joinedAt" to ServerValue.TIMESTAMP
            )
            roomRef.child("members").child(user.uid).setValue(memberData).await()

            val room = Room(
                roomCode = upperCode,
                hostId = hostId,
                videoId = videoId,
                state = state,
                position = position,
                updatedAt = updatedAt,
                videoTitle = videoTitle
            )
            Result.success(room)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveRoom(roomCode: String, uid: String) {
        try {
            val upperCode = roomCode.trim().uppercase()
            val memberRef = database.getReference("rooms").child(upperCode).child("members").child(uid)
            memberRef.removeValue().await()
            val typingRef = database.getReference("rooms").child(upperCode).child("typing").child(uid)
            typingRef.removeValue().await()
        } catch (_: Exception) {
        }
    }

    override fun observeRoom(roomCode: String): Flow<Room?> = callbackFlow {
        val upperCode = roomCode.trim().uppercase()
        val roomRef = database.getReference("rooms").child(upperCode)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }

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
                trySend(room)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        roomRef.addValueEventListener(listener)
        awaitClose { roomRef.removeEventListener(listener) }
    }

    override fun observeMembers(roomCode: String, hostId: String): Flow<List<Member>> = callbackFlow {
        val upperCode = roomCode.trim().uppercase()
        val membersRef = database.getReference("rooms").child(upperCode).child("members")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                // Sort with host first, then by joinedAt
                val sorted = list.sortedWith(
                    compareByDescending<Member> { it.isHost }.thenBy { it.joinedAt }
                )
                trySend(sorted)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        membersRef.addValueEventListener(listener)
        awaitClose { membersRef.removeEventListener(listener) }
    }

    override fun observeMessages(roomCode: String): Flow<ChatMessage> = callbackFlow {
        val upperCode = roomCode.trim().uppercase()
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
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        messagesRef.addChildEventListener(listener)
        awaitClose { messagesRef.removeEventListener(listener) }
    }

    override fun observeTypingUsers(roomCode: String, currentUid: String): Flow<List<String>> = callbackFlow {
        val upperCode = roomCode.trim().uppercase()
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
                trySend(typingList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        typingRef.addValueEventListener(listener)
        awaitClose { typingRef.removeEventListener(listener) }
    }

    override suspend fun updatePlaybackState(
        roomCode: String,
        state: String,
        positionSeconds: Double,
        videoId: String?,
        videoTitle: String?
    ): Result<Unit> {
        return try {
            val upperCode = roomCode.trim().uppercase()
            val roomRef = database.getReference("rooms").child(upperCode)

            val updates = hashMapOf<String, Any>(
                "state" to state,
                "position" to positionSeconds,
                "updatedAt" to ServerValue.TIMESTAMP
            )
            videoId?.let { updates["videoId"] = it }
            videoTitle?.let { updates["videoTitle"] = it }

            roomRef.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(
        roomCode: String,
        user: UserAccount,
        text: String
    ): Result<Unit> {
        return try {
            val upperCode = roomCode.trim().uppercase()
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setTyping(roomCode: String, uid: String, userName: String, isTyping: Boolean) {
        try {
            val upperCode = roomCode.trim().uppercase()
            val userTypingRef = database.getReference("rooms").child(upperCode).child("typing").child(uid)
            if (isTyping) {
                userTypingRef.setValue(
                    hashMapOf(
                        "name" to userName,
                        "isTyping" to true
                    )
                )
            } else {
                userTypingRef.removeValue()
            }
        } catch (_: Exception) {}
    }
}
