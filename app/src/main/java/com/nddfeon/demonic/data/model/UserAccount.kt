package com.nddfeon.demonic.data.model

data class UserAccount(
    val uid: String,
    val displayName: String,
    val email: String? = null,
    val photoUrl: String? = null
)
