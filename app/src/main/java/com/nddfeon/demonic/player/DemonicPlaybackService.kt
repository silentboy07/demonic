package com.nddfeon.demonic.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.nddfeon.demonic.DemonicApp
import com.nddfeon.demonic.MainActivity
import com.nddfeon.demonic.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DemonicPlaybackService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private var currentRoomCode: String = ""
    private var currentVideoId: String = ""
    private var currentVideoTitle: String = "Demonic Stream"
    private var isPlaying: Boolean = false
    private var cachedThumbnail: Bitmap? = null

    companion object {
        const val CHANNEL_ID = "demonic_playback_channel"
        const val NOTIFICATION_ID = 666

        const val ACTION_START = "com.nddfeon.demonic.ACTION_START"
        const val ACTION_UPDATE = "com.nddfeon.demonic.ACTION_UPDATE"
        const val ACTION_PLAY = "com.nddfeon.demonic.ACTION_PLAY"
        const val ACTION_PAUSE = "com.nddfeon.demonic.ACTION_PAUSE"
        const val ACTION_NEXT = "com.nddfeon.demonic.ACTION_NEXT"
        const val ACTION_STOP = "com.nddfeon.demonic.ACTION_STOP"

        const val EXTRA_ROOM_CODE = "extra_room_code"
        const val EXTRA_VIDEO_ID = "extra_video_id"
        const val EXTRA_VIDEO_TITLE = "extra_video_title"
        const val EXTRA_IS_PLAYING = "extra_is_playing"

        var onNextTrackCallback: (() -> Unit)? = null

        fun startService(context: Context, roomCode: String, videoId: String, title: String, isPlaying: Boolean) {
            val intent = Intent(context, DemonicPlaybackService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ROOM_CODE, roomCode)
                putExtra(EXTRA_VIDEO_ID, videoId)
                putExtra(EXTRA_VIDEO_TITLE, title)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun updatePlayback(context: Context, videoId: String, title: String, isPlaying: Boolean) {
            val intent = Intent(context, DemonicPlaybackService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_VIDEO_ID, videoId)
                putExtra(EXTRA_VIDEO_TITLE, title)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }

        fun stopService(context: Context) {
            val intent = Intent(context, DemonicPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as? DemonicApp
        val playerManager = app?.youTubePlayerManager

        when (intent?.action) {
            ACTION_START -> {
                currentRoomCode = intent.getStringExtra(EXTRA_ROOM_CODE) ?: currentRoomCode
                currentVideoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: currentVideoId
                currentVideoTitle = intent.getStringExtra(EXTRA_VIDEO_TITLE) ?: currentVideoTitle
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)

                startForeground(NOTIFICATION_ID, buildNotification())
                loadThumbnailIfNeeded(currentVideoId)
            }
            ACTION_UPDATE -> {
                val newVideoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: currentVideoId
                val newTitle = intent.getStringExtra(EXTRA_VIDEO_TITLE) ?: currentVideoTitle
                val newPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, isPlaying)

                val videoChanged = newVideoId != currentVideoId
                currentVideoId = newVideoId
                currentVideoTitle = newTitle
                isPlaying = newPlaying

                if (videoChanged) {
                    cachedThumbnail = null
                    loadThumbnailIfNeeded(currentVideoId)
                }

                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(NOTIFICATION_ID, buildNotification())
            }
            ACTION_PLAY -> {
                isPlaying = true
                playerManager?.play()
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(NOTIFICATION_ID, buildNotification())
            }
            ACTION_PAUSE -> {
                isPlaying = false
                playerManager?.pause()
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(NOTIFICATION_ID, buildNotification())
            }
            ACTION_NEXT -> {
                onNextTrackCallback?.invoke()
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun loadThumbnailIfNeeded(videoId: String) {
        if (videoId.isEmpty()) return
        scope.launch {
            val imageLoader = ImageLoader(this@DemonicPlaybackService)
            val request = ImageRequest.Builder(this@DemonicPlaybackService)
                .data("https://img.youtube.com/vi/$videoId/hqdefault.jpg")
                .allowHardware(false)
                .build()
            val result = withContext(Dispatchers.IO) {
                try {
                    imageLoader.execute(request)
                } catch (_: Exception) {
                    null
                }
            }
            val bitmap = (result?.drawable as? BitmapDrawable)?.bitmap
            if (bitmap != null) {
                cachedThumbnail = bitmap
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(NOTIFICATION_ID, buildNotification())
            }
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent = Intent(this, DemonicPlaybackService::class.java).apply { action = ACTION_PLAY }
        val playPendingIntent = PendingIntent.getService(
            this, 1, playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, DemonicPlaybackService::class.java).apply { action = ACTION_PAUSE }
        val pausePendingIntent = PendingIntent.getService(
            this, 2, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent(this, DemonicPlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(
            this, 3, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, DemonicPlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 4, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action.Builder(
                R.drawable.ic_pause, "Pause", pausePendingIntent
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                R.drawable.ic_play, "Play", playPendingIntent
            ).build()
        }

        val nextAction = NotificationCompat.Action.Builder(
            R.drawable.ic_skip_next, "Next", nextPendingIntent
        ).build()

        val closeAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel, "Close", stopPendingIntent
        ).build()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(currentVideoTitle.ifEmpty { "Demonic Music Stream" })
            .setContentText(if (currentRoomCode.isNotEmpty()) "Room: $currentRoomCode • Synchronized" else "Synchronized Playback")
            .setContentIntent(openPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(isPlaying)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .addAction(closeAction)

        cachedThumbnail?.let {
            builder.setLargeIcon(it)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Demonic Music Player"
            val descriptionText = "Persistent playback controls and lock screen status"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
