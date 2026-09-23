package com.nddfeon.demonic.data.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class RecentRoom(
    val roomCode: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isHost: Boolean = false
)

@Singleton
class RecentRoomsManager @Inject constructor(
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("demonic_recent_rooms_prefs", Context.MODE_PRIVATE)
    private val _recentRooms = MutableStateFlow<List<RecentRoom>>(emptyList())
    val recentRooms: StateFlow<List<RecentRoom>> = _recentRooms.asStateFlow()

    init {
        loadRooms()
    }

    private fun loadRooms() {
        val raw = prefs.getString(KEY_ROOMS, null) ?: return
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<RecentRoom>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val code = obj.optString("roomCode", "")
                val time = obj.optLong("timestamp", System.currentTimeMillis())
                val host = obj.optBoolean("isHost", false)
                if (code.isNotBlank()) {
                    list.add(RecentRoom(roomCode = code, timestamp = time, isHost = host))
                }
            }
            _recentRooms.value = list
        } catch (_: Exception) {
            _recentRooms.value = emptyList()
        }
    }

    fun addRoom(roomCode: String, isHost: Boolean = false) {
        val cleanCode = roomCode.trim().uppercase()
        if (cleanCode.isBlank()) return

        val current = _recentRooms.value.filter { it.roomCode != cleanCode }.toMutableList()
        current.add(0, RecentRoom(roomCode = cleanCode, timestamp = System.currentTimeMillis(), isHost = isHost))

        val capped = current.take(5)
        _recentRooms.value = capped
        saveRooms(capped)
    }

    fun removeRoom(roomCode: String) {
        val filtered = _recentRooms.value.filter { it.roomCode != roomCode.trim().uppercase() }
        _recentRooms.value = filtered
        saveRooms(filtered)
    }

    fun clearAll() {
        _recentRooms.value = emptyList()
        prefs.edit().remove(KEY_ROOMS).apply()
    }

    private fun saveRooms(list: List<RecentRoom>) {
        try {
            val arr = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("roomCode", item.roomCode)
                    put("timestamp", item.timestamp)
                    put("isHost", item.isHost)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_ROOMS, arr.toString()).apply()
        } catch (_: Exception) {}
    }

    companion object {
        private const val KEY_ROOMS = "recent_rooms_json"
    }
}
