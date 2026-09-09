package com.nddfeon.demonic

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nddfeon.demonic.data.repository.AuthRepository
import com.nddfeon.demonic.data.repository.FirebaseAuthRepository
import com.nddfeon.demonic.data.repository.FirebaseRoomRepository
import com.nddfeon.demonic.data.repository.RoomRepository
import com.nddfeon.demonic.player.YouTubePlayerManager
import com.nddfeon.demonic.player.YouTubeSearchManager

class DemonicApp : Application() {

    lateinit var authRepository: AuthRepository
        private set
    lateinit var roomRepository: RoomRepository
        private set
    lateinit var youTubePlayerManager: YouTubePlayerManager
        private set
    lateinit var youTubeSearchManager: YouTubeSearchManager
        private set

    override fun onCreate() {
        super.onCreate()
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        try {
            database.setPersistenceEnabled(true)
        } catch (_: Exception) {}

        authRepository = FirebaseAuthRepository(auth)
        roomRepository = FirebaseRoomRepository(database)
        youTubePlayerManager = YouTubePlayerManager()
        youTubeSearchManager = YouTubeSearchManager()
    }
}