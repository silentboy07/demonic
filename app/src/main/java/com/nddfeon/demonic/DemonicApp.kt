package com.nddfeon.demonic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nddfeon.demonic.data.manager.OwnerConfigManager
import com.nddfeon.demonic.data.repository.AuthRepository
import com.nddfeon.demonic.data.repository.FirebaseAuthRepository
import com.nddfeon.demonic.data.repository.FirebaseRoomRepository
import com.nddfeon.demonic.data.repository.RoomRepository
import com.nddfeon.demonic.player.YouTubePlayerManager
import com.nddfeon.demonic.player.YouTubeSearchManager

class DemonicApp : Application(), ImageLoaderFactory {

    lateinit var authRepository: AuthRepository
        private set
    lateinit var roomRepository: RoomRepository
        private set
    lateinit var youTubePlayerManager: YouTubePlayerManager
        private set
    lateinit var youTubeSearchManager: YouTubeSearchManager
        private set
    lateinit var ownerConfigManager: OwnerConfigManager
        private set

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("demonic_image_cache"))
                    .maxSizeBytes(60L * 1024 * 1024) // 60MB disk cache for instant image reloads
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        val auth = FirebaseAuth.getInstance()
        val database = try {
            FirebaseDatabase.getInstance("https://demonic-4a2f7-default-rtdb.firebaseio.com")
        } catch (_: Exception) {
            FirebaseDatabase.getInstance()
        }
        try {
            database.setPersistenceEnabled(true)
        } catch (_: Exception) {}

        authRepository = FirebaseAuthRepository(auth)
        roomRepository = FirebaseRoomRepository(database)
        youTubePlayerManager = YouTubePlayerManager()
        youTubeSearchManager = YouTubeSearchManager()
        ownerConfigManager = OwnerConfigManager(this, database)
    }
}