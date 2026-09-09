package com.nddfeon.demonic.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class LiveReaction(
    val id: String = "",
    val emoji: String = "🔥",
    val senderName: String = "",
    val timestamp: Long = 0L
)
