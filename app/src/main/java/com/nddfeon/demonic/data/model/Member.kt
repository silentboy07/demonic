package com.nddfeon.demonic.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Member(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val joinedAt: Long = 0L,
    val isHost: Boolean = false
)
