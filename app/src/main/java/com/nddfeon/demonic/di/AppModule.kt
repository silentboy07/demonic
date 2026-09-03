package com.nddfeon.demonic.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nddfeon.demonic.data.repository.AuthRepository
import com.nddfeon.demonic.data.repository.FirebaseAuthRepository
import com.nddfeon.demonic.data.repository.FirebaseRoomRepository
import com.nddfeon.demonic.data.repository.RoomRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val database = FirebaseDatabase.getInstance()
        try {
            database.setPersistenceEnabled(true)
        } catch (_: Exception) {}
        return database
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRoomRepository(
        impl: FirebaseRoomRepository
    ): RoomRepository
}
