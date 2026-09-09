package com.nddfeon.demonic.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class QueueItem(
    val id: String = "",
    val videoId: String = "",
    val title: String = "",
    val thumbnailUrl: String = "",
    val addedByUid: String = "",
    val addedByName: String = "",
    val addedAt: Long = 0L,
    val upvotes: Map<String, Boolean> = emptyMap()
)
