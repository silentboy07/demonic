package com.nddfeon.demonic.data.manager

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnerConfigManager @Inject constructor(
    context: Context,
    private val database: FirebaseDatabase? = null
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("demonic_owner_config", Context.MODE_PRIVATE)

    // Master Ads Switch (Default: FALSE / OFF as requested)
    private val _isAdsEnabled = MutableStateFlow(prefs.getBoolean("ads_enabled", false))
    val isAdsEnabled: StateFlow<Boolean> = _isAdsEnabled.asStateFlow()

    private val _bannerAdsEnabled = MutableStateFlow(prefs.getBoolean("banner_ads_enabled", false))
    val bannerAdsEnabled: StateFlow<Boolean> = _bannerAdsEnabled.asStateFlow()

    private val _interstitialAdsEnabled = MutableStateFlow(prefs.getBoolean("interstitial_ads_enabled", false))
    val interstitialAdsEnabled: StateFlow<Boolean> = _interstitialAdsEnabled.asStateFlow()

    private val _rewardedAdsEnabled = MutableStateFlow(prefs.getBoolean("rewarded_ads_enabled", false))
    val rewardedAdsEnabled: StateFlow<Boolean> = _rewardedAdsEnabled.asStateFlow()

    private val _maintenanceMode = MutableStateFlow(prefs.getBoolean("maintenance_mode", false))
    val maintenanceMode: StateFlow<Boolean> = _maintenanceMode.asStateFlow()

    private val _globalAnnouncement = MutableStateFlow(prefs.getString("global_announcement", null))
    val globalAnnouncement: StateFlow<String?> = _globalAnnouncement.asStateFlow()

    private val _ownerPin = MutableStateFlow(prefs.getString("owner_pin", "7777") ?: "7777")
    val ownerPin: StateFlow<String> = _ownerPin.asStateFlow()

    // Live Telemetry Stats for Owner
    private val _liveActiveRooms = MutableStateFlow(0)
    val liveActiveRooms: StateFlow<Int> = _liveActiveRooms.asStateFlow()

    private val _liveActiveMembers = MutableStateFlow(0)
    val liveActiveMembers: StateFlow<Int> = _liveActiveMembers.asStateFlow()

    init {
        // Sync with Firebase Remote Config /app_config if connected
        try {
            database?.getReference("app_config")?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val ads = snapshot.child("ads_enabled").getValue(Boolean::class.java)
                        if (ads != null && ads != _isAdsEnabled.value) {
                            _isAdsEnabled.value = ads
                            prefs.edit().putBoolean("ads_enabled", ads).apply()
                        }
                        val announcement = snapshot.child("global_announcement").getValue(String::class.java)
                        if (announcement != _globalAnnouncement.value) {
                            _globalAnnouncement.value = announcement
                            prefs.edit().putString("global_announcement", announcement).apply()
                        }
                        val remotePin = snapshot.child("owner_pin").getValue(String::class.java)
                        if (!remotePin.isNullOrBlank() && remotePin != _ownerPin.value) {
                            _ownerPin.value = remotePin
                            prefs.edit().putString("owner_pin", remotePin).apply()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (_: Exception) {}

        // Listen for Realtime Network Telemetry (Active Rooms & Connected Listeners)
        try {
            database?.getReference("rooms")?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        _liveActiveRooms.value = snapshot.childrenCount.toInt()
                        var totalMembers = 0
                        for (roomSnap in snapshot.children) {
                            totalMembers += roomSnap.child("members").childrenCount.toInt()
                        }
                        _liveActiveMembers.value = totalMembers
                    } else {
                        _liveActiveRooms.value = 0
                        _liveActiveMembers.value = 0
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (_: Exception) {}
    }

    fun verifyPin(inputPin: String): Boolean {
        val trimmed = inputPin.trim()
        return trimmed == _ownerPin.value
    }

    fun setAdsEnabled(enabled: Boolean) {
        _isAdsEnabled.value = enabled
        prefs.edit().putBoolean("ads_enabled", enabled).apply()
        pushToFirebase("ads_enabled", enabled)
    }

    fun setBannerAdsEnabled(enabled: Boolean) {
        _bannerAdsEnabled.value = enabled
        prefs.edit().putBoolean("banner_ads_enabled", enabled).apply()
        pushToFirebase("banner_ads_enabled", enabled)
    }

    fun setInterstitialAdsEnabled(enabled: Boolean) {
        _interstitialAdsEnabled.value = enabled
        prefs.edit().putBoolean("interstitial_ads_enabled", enabled).apply()
        pushToFirebase("interstitial_ads_enabled", enabled)
    }

    fun setRewardedAdsEnabled(enabled: Boolean) {
        _rewardedAdsEnabled.value = enabled
        prefs.edit().putBoolean("rewarded_ads_enabled", enabled).apply()
        pushToFirebase("rewarded_ads_enabled", enabled)
    }

    fun setMaintenanceMode(enabled: Boolean) {
        _maintenanceMode.value = enabled
        prefs.edit().putBoolean("maintenance_mode", enabled).apply()
        pushToFirebase("maintenance_mode", enabled)
    }

    fun setGlobalAnnouncement(announcement: String?) {
        _globalAnnouncement.value = announcement
        prefs.edit().putString("global_announcement", announcement).apply()
        pushToFirebase("global_announcement", announcement ?: "")
    }

    fun setOwnerPin(newPin: String): Boolean {
        val trimmed = newPin.trim()
        if (trimmed.length in 4..8) {
            _ownerPin.value = trimmed
            prefs.edit().putString("owner_pin", trimmed).apply()
            pushToFirebase("owner_pin", trimmed)
            return true
        }
        return false
    }

    private fun pushToFirebase(key: String, value: Any) {
        try {
            database?.getReference("app_config")?.child(key)?.setValue(value)
        } catch (_: Exception) {}
    }
}
