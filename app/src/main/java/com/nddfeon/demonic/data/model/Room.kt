package com.nddfeon.demonic.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Room(
    val roomCode: String = "",
    val hostId: String = "",
    val videoId: String = "",
    val state: String = "paused", // "playing" | "paused"
    val position: Double = 0.0,   // seconds, position at the moment updatedAt was written
    val updatedAt: Long = 0L,     // Server timestamp in ms
    val videoTitle: String = ""
) {
    val isPlaying: Boolean
        get() = state.equals("playing", ignoreCase = true)
}
