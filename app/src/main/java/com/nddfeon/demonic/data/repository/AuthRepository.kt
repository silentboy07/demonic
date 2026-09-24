package com.nddfeon.demonic.data.repository

import android.content.Context
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.nddfeon.demonic.data.model.UserAccount
import dagger.hilt.android.qualifiers.ApplicationContext
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
    suspend fun updateProfile(displayName: String, photoUrl: String? = null): Result<UserAccount>
    suspend fun signOut()
}

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @param:ApplicationContext private val context: Context
) : AuthRepository {

    private val prefs = context.getSharedPreferences("demonic_user_prefs", Context.MODE_PRIVATE)
    private val _customUser = MutableStateFlow<UserAccount?>(null)

    init {
        val savedName = prefs.getString("custom_display_name", null)
        val savedUid = prefs.getString("custom_uid", null)
        val savedPhoto = prefs.getString("custom_photo_url", null)
        if (!savedName.isNullOrBlank() && !savedUid.isNullOrBlank()) {
            _customUser.value = UserAccount(
                uid = savedUid,
                displayName = savedName,
                photoUrl = savedPhoto?.ifEmpty { null }
            )
        }
    }

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
            prefs.edit()
                .putString("custom_display_name", name)
                .putString("custom_photo_url", photoUrl ?: "")
                .putString("custom_uid", uid)
                .apply()
            Result.success(account)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(displayName: String, photoUrl: String?): Result<UserAccount> {
        val trimmed = displayName.trim().ifEmpty { "Demon Listener" }
        return try {
            val fbUser = auth.currentUser
            if (fbUser != null) {
                try {
                    val req = UserProfileChangeRequest.Builder()
                        .setDisplayName(trimmed)
                        .apply {
                            if (!photoUrl.isNullOrEmpty()) {
                                setPhotoUri(android.net.Uri.parse(photoUrl))
                            }
                        }
                        .build()
                    fbUser.updateProfile(req).await()
                } catch (_: Exception) {}
            }

            val uid = fbUser?.uid ?: _customUser.value?.uid ?: ("guest_" + (System.currentTimeMillis() % 100000))
            val finalPhoto = photoUrl ?: fbUser?.photoUrl?.toString() ?: _customUser.value?.photoUrl
            val updated = UserAccount(
                uid = uid,
                displayName = trimmed,
                email = fbUser?.email,
                photoUrl = finalPhoto
            )
            _customUser.value = updated

            prefs.edit()
                .putString("custom_display_name", trimmed)
                .putString("custom_photo_url", finalPhoto ?: "")
                .putString("custom_uid", uid)
                .apply()

            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        prefs.edit().clear().apply()
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
