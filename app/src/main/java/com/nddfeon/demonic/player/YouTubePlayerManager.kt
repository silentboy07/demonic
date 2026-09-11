package com.nddfeon.demonic.player

import android.webkit.WebView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

enum class YouTubeVideoQuality(
    val label: String,
    val code: String,
    val badge: String,
    val description: String
) {
    AUTO("Auto", "default", "AUTO", "Optimized automatically"),
    P1080("1080p Full HD", "hd1080", "1080p", "Crisp Full HD"),
    P720("720p HD", "hd720", "720p", "High definition"),
    P480("480p SD", "large", "480p", "Standard definition"),
    P360("360p Medium", "medium", "360p", "Balanced quality"),
    P240("240p Low", "small", "240p", "Fast loading"),
    P144("144p Data Saver", "tiny", "144p", "Minimal data usage");

    companion object {
        fun fromCode(code: String): YouTubeVideoQuality {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: AUTO
        }
    }
}

@Singleton
class YouTubePlayerManager @Inject constructor() {

    private var youTubePlayerInstance: YouTubePlayer? = null
    private var webViewRef: WeakReference<WebView>? = null

    private val _currentQuality = MutableStateFlow(YouTubeVideoQuality.AUTO)
    val currentQuality: StateFlow<YouTubeVideoQuality> = _currentQuality.asStateFlow()

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

    private val _isMutedLocally = MutableStateFlow(false)
    val isMutedLocally: StateFlow<Boolean> = _isMutedLocally.asStateFlow()

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

            if (_isMutedLocally.value) {
                try {
                    youTubePlayer.mute()
                } catch (_: Exception) {}
            }

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

            applyQualityJs(_currentQuality.value)
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

    fun toggleLocalMute(): Boolean {
        val newMute = !_isMutedLocally.value
        setLocalMute(newMute)
        return newMute
    }

    fun setLocalMute(mute: Boolean) {
        _isMutedLocally.value = mute
        val player = youTubePlayerInstance
        if (player != null) {
            try {
                if (mute) {
                    player.mute()
                } else {
                    player.unMute()
                }
            } catch (e: Exception) {
                android.util.Log.e("DemonicPlayer", "Error toggling local mute: ${e.message}")
            }
        }
    }

    fun loadOrCueVideo(videoId: String, startSeconds: Float = 0f, autoPlay: Boolean = false) {
        if (videoId.isBlank()) return
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
            applyQualityJs(_currentQuality.value)
        } else {
            android.util.Log.d("DemonicPlayer", "loadOrCueVideo: Player instance is null, queued as pending: $videoId")
        }
    }

    fun attachWebView(webView: WebView) {
        webViewRef = WeakReference(webView)
        applyQualityJs(_currentQuality.value)
    }

    fun setQuality(quality: YouTubeVideoQuality) {
        _currentQuality.value = quality
        applyQualityJs(quality)
    }

    private fun applyQualityJs(quality: YouTubeVideoQuality) {
        val code = quality.code
        val js = """
            (function() {
                try {
                    var p = window.player || (typeof player !== 'undefined' ? player : null);
                    if (p) {
                        if (typeof p.setPlaybackQuality === 'function') {
                            p.setPlaybackQuality('$code');
                        }
                        if (typeof p.setPlaybackQualityRange === 'function') {
                            p.setPlaybackQualityRange('$code', '$code');
                        }
                    }
                    var ytp = document.getElementById('movie_player') || document.querySelector('.html5-video-player');
                    if (ytp) {
                        if (typeof ytp.setPlaybackQualityRange === 'function') {
                            ytp.setPlaybackQualityRange('$code', '$code');
                        }
                        if (typeof ytp.setPlaybackQuality === 'function') {
                            ytp.setPlaybackQuality('$code');
                        }
                    }
                    try {
                        localStorage.setItem('yt-player-quality', JSON.stringify({
                            data: '$code',
                            creation: Date.now()
                        }));
                    } catch(e) {}
                } catch(err) {
                    console.error('Failed to set YouTube quality:', err);
                }
            })();
        """.trimIndent()

        webViewRef?.get()?.post {
            try {
                webViewRef?.get()?.evaluateJavascript(js, null)
            } catch (_: Exception) {}
        }
    }

    fun release() {
        youTubePlayerInstance = null
        webViewRef = null
        _isReady.value = false
        pendingVideoId = ""
        pendingStartSeconds = 0f
        pendingAutoPlay = false
    }
}
