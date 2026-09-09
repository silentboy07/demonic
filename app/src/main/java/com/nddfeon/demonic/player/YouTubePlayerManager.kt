package com.nddfeon.demonic.player

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubePlayerManager @Inject constructor() {

    private var youTubePlayerInstance: YouTubePlayer? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerConstants.PlayerState.UNKNOWN)
    val playerState: StateFlow<PlayerConstants.PlayerState> = _playerState.asStateFlow()

    private val _currentSecond = MutableStateFlow(0f)
    val currentSecond: StateFlow<Float> = _currentSecond.asStateFlow()

    private val _duration = MutableStateFlow(0f)
    val duration: StateFlow<Float> = _duration.asStateFlow()

    private val _activeVideoId = MutableStateFlow("")
    val activeVideoId: StateFlow<String> = _activeVideoId.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private var pendingVideoId: String = ""
    private var pendingStartSeconds: Float = 0f
    private var pendingAutoPlay: Boolean = false

    fun isPlaying(): Boolean = _playerState.value == PlayerConstants.PlayerState.PLAYING
    fun isBuffering(): Boolean = _playerState.value == PlayerConstants.PlayerState.BUFFERING

    val listener = object : AbstractYouTubePlayerListener() {
        override fun onReady(youTubePlayer: YouTubePlayer) {
            android.util.Log.d("DemonicPlayer", "YouTubePlayer onReady received!")
            youTubePlayerInstance = youTubePlayer
            _isReady.value = true

            // If there was a pending video load/cue request
            val targetId = pendingVideoId.ifEmpty { _activeVideoId.value }
            if (targetId.isNotEmpty()) {
                android.util.Log.d("DemonicPlayer", "onReady -> loading pending video: $targetId (autoPlay=$pendingAutoPlay, start=$pendingStartSeconds)")
                if (pendingAutoPlay) {
                    youTubePlayer.loadVideo(targetId, pendingStartSeconds)
                } else {
                    youTubePlayer.cueVideo(targetId, pendingStartSeconds)
                }
            }
        }

        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
            android.util.Log.d("DemonicPlayer", "onStateChange: $state")
            _playerState.value = state
        }

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            _currentSecond.value = second
        }

        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            _duration.value = duration
        }

        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
            android.util.Log.e("DemonicPlayer", "YouTube Player onError: $error")
            _playbackError.value = "Playback error: $error"
        }
    }

    fun play() {
        val player = youTubePlayerInstance
        if (player != null) {
            player.play()
        } else {
            pendingAutoPlay = true
        }
    }

    fun pause() {
        pendingAutoPlay = false
        youTubePlayerInstance?.pause()
    }

    fun seekTo(seconds: Float) {
        val clamped = seconds.coerceAtLeast(0f)
        _currentSecond.value = clamped
        youTubePlayerInstance?.seekTo(clamped)
    }

    fun loadOrCueVideo(videoId: String, startSeconds: Float = 0f, autoPlay: Boolean = false) {
        _activeVideoId.value = videoId
        _currentSecond.value = startSeconds
        pendingVideoId = videoId
        pendingStartSeconds = startSeconds
        pendingAutoPlay = autoPlay

        val player = youTubePlayerInstance
        if (player != null) {
            android.util.Log.d("DemonicPlayer", "loadOrCueVideo: $videoId, startSeconds=$startSeconds, autoPlay=$autoPlay")
            if (autoPlay) {
                player.loadVideo(videoId, startSeconds)
            } else {
                player.cueVideo(videoId, startSeconds)
            }
        } else {
            android.util.Log.d("DemonicPlayer", "loadOrCueVideo: Player instance is null, queued as pending: $videoId")
        }
    }

    fun release() {
        youTubePlayerInstance = null
        _isReady.value = false
        pendingVideoId = ""
        pendingStartSeconds = 0f
        pendingAutoPlay = false
    }
}
