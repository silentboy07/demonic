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

    val listener = object : AbstractYouTubePlayerListener() {
        override fun onReady(youTubePlayer: YouTubePlayer) {
            youTubePlayerInstance = youTubePlayer
            _isReady.value = true

            // If there was a pending video ID
            if (_activeVideoId.value.isNotEmpty()) {
                youTubePlayer.cueVideo(_activeVideoId.value, _currentSecond.value)
            }
        }

        override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
            _playerState.value = state
        }

        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
            _currentSecond.value = second
        }

        override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
            _duration.value = duration
        }

        override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
            // Handle player errors gracefully
        }
    }

    fun play() {
        youTubePlayerInstance?.play()
    }

    fun pause() {
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
        if (autoPlay) {
            youTubePlayerInstance?.loadVideo(videoId, startSeconds)
        } else {
            youTubePlayerInstance?.cueVideo(videoId, startSeconds)
        }
    }

    fun release() {
        youTubePlayerInstance = null
        _isReady.value = false
    }
}
