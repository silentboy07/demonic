package com.nddfeon.demonic.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.nddfeon.demonic.data.model.UserAccount
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    val currentUser: UserAccount?
    fun authStateFlow(): Flow<UserAccount?>
    suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<UserAccount>
    suspend fun signInWithGoogleIdToken(idToken: String): Result<UserAccount>
    suspend fun signInWithCustomUser(uid: String, name: String, photoUrl: String?): Result<UserAccount>
    suspend fun signOut()
}

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    private val _customUser = MutableStateFlow<UserAccount?>(null)

    override val currentUser: UserAccount?
        get() = _customUser.value ?: auth.currentUser?.toUserAccount()

    private val firebaseAuthFlow: Flow<UserAccount?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toUserAccount())
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser?.toUserAccount())
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun authStateFlow(): Flow<UserAccount?> = combine(
        _customUser,
        firebaseAuthFlow
    ) { custom, fbUser ->
        custom ?: fbUser
    }

    override suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<UserAccount> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user?.toUserAccount()
                ?: throw IllegalStateException("Firebase user was null after sign in")
            _customUser.value = null
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<UserAccount> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            signInWithGoogleCredential(credential)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithCustomUser(uid: String, name: String, photoUrl: String?): Result<UserAccount> {
        return try {
            val account = UserAccount(
                uid = uid,
                displayName = name,
                photoUrl = photoUrl
            )
            _customUser.value = account
            Result.success(account)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        _customUser.value = null
        try {
            auth.signOut()
        } catch (_: Exception) {}
    }
}

private fun FirebaseUser.toUserAccount(): UserAccount {
    return UserAccount(
        uid = uid,
        displayName = displayName.takeIf { !it.isNullOrBlank() } ?: (email?.substringBefore('@') ?: "Demon #$uid"),
        email = email,
        photoUrl = photoUrl?.toString()
    )
}
