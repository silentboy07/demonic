package com.nddfeon.demonic.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Room(
    val roomCode: String = "",
    val hostId: String = "",
    val djId: String? = null,
    val videoId: String = "",
    val state: String = "paused", // "playing" | "paused"
    val position: Double = 0.0,   // seconds, position at the moment updatedAt was written
    val updatedAt: Long = 0L,     // Server timestamp in ms
    val videoTitle: String = "",
    val isPublic: Boolean = true,
    val memberCount: Int = 1,
    val queue: List<QueueItem> = emptyList(),
    val theme: String = "CYBER_NEON",
    val visualizerStyle: String = "CIRCULAR",
    val activeEffect: RoomSpecialEffect? = null
) {
    val isPlaying: Boolean
        get() = state.equals("playing", ignoreCase = true)

    val themePreset: RoomThemePreset
        get() = RoomThemePreset.fromId(theme)

    val visualizerPreset: VisualizerStylePreset
        get() = VisualizerStylePreset.fromId(visualizerStyle)

    fun canControlPlayback(uid: String): Boolean {
        if (uid.isEmpty()) return true
        if (hostId == uid) return true
        if (djId != null && djId == uid) return true
        // Fallback for local demo guest mode
        if (hostId.startsWith("guest_") && uid.startsWith("guest_")) return true
        return false
    }
}
