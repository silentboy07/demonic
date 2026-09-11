package com.nddfeon.demonic.ui.room

import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MoreVert
import com.nddfeon.demonic.ui.components.WatchPartyFullscreenOverlay
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.UnfoldMore
import coil.compose.AsyncImage
import com.nddfeon.demonic.player.DemonicPlaybackService
import androidx.compose.ui.text.style.TextAlign
import com.nddfeon.demonic.data.model.ChatMessage
import com.nddfeon.demonic.player.YouTubeSearchManager
import com.nddfeon.demonic.ui.components.ChatInputBar
import com.nddfeon.demonic.ui.components.ChatMessageActionDialog
import com.nddfeon.demonic.ui.components.ChatMessageItem
import com.nddfeon.demonic.ui.components.DemonicButton
import com.nddfeon.demonic.ui.components.ScrollToBottomFloatingButton
import com.nddfeon.demonic.ui.components.FloatingReactionsOverlay
import com.nddfeon.demonic.ui.components.MemberAvatarRow
import com.nddfeon.demonic.ui.components.MembersBottomSheet
import com.nddfeon.demonic.ui.components.QueueBottomSheet
import com.nddfeon.demonic.ui.components.ReactionButtonBar
import com.nddfeon.demonic.ui.components.SyncStatusBadge
import com.nddfeon.demonic.ui.components.TypingIndicatorBubble
import com.nddfeon.demonic.ui.components.VinylDisc
import com.nddfeon.demonic.ui.components.NowPlayingTrackBanner
import com.nddfeon.demonic.ui.components.RoomQrDialog
import com.nddfeon.demonic.ui.components.SleepTimerDialog
import com.nddfeon.demonic.ui.components.YouTubeExplorerSheet
import com.nddfeon.demonic.ui.components.YouTubeSearchDialog
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.QrCode
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicCrimsonDark
import com.nddfeon.demonic.ui.theme.DemonicErrorRed
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicTextSecondary
import com.nddfeon.demonic.ui.theme.DemonicViolet
import com.nddfeon.demonic.ui.theme.DemonicWarningAmber
import com.nddfeon.demonic.viewmodel.RoomViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline

enum class PlayerDisplayMode {
    VIDEO,
    VINYL,
    COMPACT
}

@Composable
fun RoomScreen(
    viewModel: RoomViewModel,
    searchManager: YouTubeSearchManager,
    onNavigateBack: () -> Unit,
    isInPip: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Auto navigate back when room is closed/deleted
    LaunchedEffect(uiState.isRoomClosed) {
        if (uiState.isRoomClosed) {
            onNavigateBack()
        }
    }

    var playerDisplayMode by remember { mutableStateOf(PlayerDisplayMode.VIDEO) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showMembersSheet by remember { mutableStateOf(false) }
    var showExplorerSheet by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsState()
    var actionMessage by remember { mutableStateOf<ChatMessage?>(null) }

    val activity = context as? android.app.Activity
    var isFullscreen by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }

    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
        isLandscape = false
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    DisposableEffect(isFullscreen) {
        onDispose {
            if (!isFullscreen) {
                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    // Synchronize Android Foreground Service for lock screen controls & Xiaomi freeze immunity
    LaunchedEffect(uiState.room?.videoId, uiState.room?.isPlaying, uiState.room?.videoTitle) {
        val videoId = uiState.room?.videoId ?: ""
        if (videoId.isNotEmpty()) {
            DemonicPlaybackService.startService(
                context = context,
                roomCode = uiState.roomCode,
                videoId = videoId,
                title = uiState.room?.videoTitle ?: "Demonic Stream",
                isPlaying = uiState.room?.isPlaying ?: false
            )
        } else {
            DemonicPlaybackService.stopService(context)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            DemonicPlaybackService.stopService(context)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var playerViewRef by remember { mutableStateOf<YouTubePlayerView?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                playerViewRef?.release()
                viewModel.playerManager.release()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playerViewRef?.release()
            viewModel.playerManager.release()
        }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val isScrolledToBottom by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems == 0) true
            else {
                val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisibleIndex >= totalItems - 2
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty() && isScrolledToBottom) {
            scope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DemonicBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isInPip && !isFullscreen) Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                    else Modifier
                )
        ) {
            if (!isInPip && !isFullscreen) {
                // Header Navigation Bar (Row 1: Clean, Never Overflows)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                viewModel.leaveRoom()
                                onNavigateBack()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Leave Room",
                                tint = DemonicTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = "ROOM ${uiState.roomCode}",
                                color = DemonicCrimson,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = when {
                                    uiState.isHost -> "HOST 👑"
                                    uiState.isDj -> "DJ 🎧"
                                    else -> "LISTENER"
                                },
                                color = when {
                                    uiState.isHost -> DemonicWarningAmber
                                    uiState.isDj -> DemonicViolet
                                    else -> DemonicTextMuted
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SyncStatusBadge(
                            driftSeconds = uiState.driftSeconds,
                            isSyncing = uiState.isSyncing
                        )

                        // Live Member Count Badge -> Opens MembersBottomSheet
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DemonicSurfaceVariant)
                                .border(1.dp, DemonicBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    showMembersSheet = true
                                }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00F5D4))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Members",
                                    tint = DemonicTextPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.members.size}",
                                    color = DemonicTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // More Actions Dropdown Menu (Share, QR Code, Sleep Timer, End Room)
                        var showHeaderMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    showHeaderMenu = true
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (sleepTimerMinutes != null) {
                                            Badge(
                                                containerColor = DemonicViolet,
                                                contentColor = Color.White
                                            ) {
                                                Text("${sleepTimerMinutes}m", fontSize = 8.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Actions",
                                        tint = DemonicTextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showHeaderMenu,
                                onDismissRequest = { showHeaderMenu = false },
                                modifier = Modifier
                                    .background(DemonicSurface)
                                    .border(1.dp, DemonicBorder, RoundedCornerShape(10.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Share Room Code", color = DemonicTextPrimary, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = DemonicTextPrimary, modifier = Modifier.size(18.dp))
                                    },
                                    onClick = {
                                        showHeaderMenu = false
                                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Join my DEMONIC synchronized music room!")
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Join my synchronized room on DEMONIC with code: ${uiState.roomCode}\n\nDeep Link: demonic://room/${uiState.roomCode}"
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Room Code"))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Room QR Code", color = DemonicTextPrimary, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.QrCode, contentDescription = null, tint = DemonicTextPrimary, modifier = Modifier.size(18.dp))
                                    },
                                    onClick = {
                                        showHeaderMenu = false
                                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                        showQrDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (sleepTimerMinutes != null) "Sleep Timer (${sleepTimerMinutes}m)" else "Sleep Timer",
                                            color = if (sleepTimerMinutes != null) DemonicViolet else DemonicTextPrimary,
                                            fontSize = 13.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Bedtime,
                                            contentDescription = null,
                                            tint = if (sleepTimerMinutes != null) DemonicViolet else DemonicTextPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        showHeaderMenu = false
                                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                        showSleepTimerDialog = true
                                    }
                                )
                                if (uiState.isHost) {
                                    DropdownMenuItem(
                                        text = { Text("End & Delete Room", color = DemonicErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = DemonicErrorRed, modifier = Modifier.size(18.dp))
                                        },
                                        onClick = {
                                            showHeaderMenu = false
                                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                            viewModel.deleteRoom()
                                            onNavigateBack()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Action Bar (Row 2: Search, Queue with badge, YouTube Explorer, and 3-Mode Toggle)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // Demonic Music Hub & Search Pill Button (Opens Native Music Hub)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(DemonicSurface, Color(0xFF1B1429))
                            )
                        )
                        .border(1.dp, DemonicViolet.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            showExplorerSheet = true
                        }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Music Hub",
                        tint = DemonicViolet,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Music Hub & Search...",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Playlist / Up Next Queue Pill Button
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (uiState.queue.isNotEmpty()) DemonicCrimsonDark.copy(alpha = 0.5f) else DemonicSurface)
                        .border(
                            1.dp,
                            if (uiState.queue.isNotEmpty()) DemonicCrimson else DemonicBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { showQueueSheet = true }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = if (uiState.queue.isNotEmpty()) DemonicCrimson else DemonicTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${uiState.queue.size})",
                            color = if (uiState.queue.isNotEmpty()) DemonicCrimson else DemonicTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3-Mode Player Display Toggle (Video -> Vinyl -> Compact)
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        playerDisplayMode = when (playerDisplayMode) {
                            PlayerDisplayMode.VIDEO -> PlayerDisplayMode.VINYL
                            PlayerDisplayMode.VINYL -> PlayerDisplayMode.COMPACT
                            PlayerDisplayMode.COMPACT -> PlayerDisplayMode.VIDEO
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DemonicSurface)
                        .border(1.dp, DemonicBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = when (playerDisplayMode) {
                            PlayerDisplayMode.VIDEO -> Icons.Default.Tv
                            PlayerDisplayMode.VINYL -> Icons.Default.Album
                            PlayerDisplayMode.COMPACT -> Icons.Default.UnfoldMore
                        },
                        contentDescription = "Player Mode",
                        tint = if (playerDisplayMode == PlayerDisplayMode.COMPACT) DemonicCrimson else DemonicTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Fullscreen Watch Party Button
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        playerDisplayMode = PlayerDisplayMode.VIDEO
                        isFullscreen = true
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DemonicCrimsonDark.copy(alpha = 0.35f))
                        .border(1.dp, DemonicCrimson.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Watch Party Fullscreen",
                        tint = DemonicCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Visual Centerpiece: 16:9 YouTube Player or Vinyl Disc or Compact Mini-Bar or Watch Party Fullscreen
        val isCompact = (playerDisplayMode == PlayerDisplayMode.COMPACT)
        Box(
            modifier = if (isInPip || isFullscreen) Modifier.fillMaxSize() else Modifier
                .fillMaxWidth()
                .height(if (isCompact) 64.dp else 200.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            val hasVideo = !uiState.room?.videoId.isNullOrEmpty()

            // Persistent YouTubePlayerView (keeps WebView alive and playing across all modes)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(if (isInPip || isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .then(if (!isInPip && !isFullscreen) Modifier.border(1.dp, DemonicSurfaceVariant, RoundedCornerShape(16.dp)) else Modifier)
                    .alpha(if ((isInPip || isFullscreen || playerDisplayMode == PlayerDisplayMode.VIDEO) && hasVideo) 1f else 0.001f)
            ) {
                    AndroidView(
                        factory = { ctx ->
                            YouTubePlayerView(ctx).apply {
                                playerViewRef = this
                                enableAutomaticInitialization = false
                                enableBackgroundPlayback(true)
                                val options = IFramePlayerOptions.Builder(ctx)
                                    .controls(1)
                                    .autoplay(1)
                                    .rel(0)
                                    .ivLoadPolicy(3)
                                    .ccLoadPolicy(0)
                                    .build()

                                // Unblock programmatic playback in Android WebView & spoof mobile browser
                                fun configureWebView(v: android.view.View) {
                                    if (v is android.webkit.WebView) {
                                        v.settings.apply {
                                            javaScriptEnabled = true
                                            mediaPlaybackRequiresUserGesture = false
                                            domStorageEnabled = true
                                            // Strip WebView tokens so YouTube does not restrict music video playback
                                            val currentUa = userAgentString ?: ""
                                            if (currentUa.contains("; wv") || currentUa.contains("Version/")) {
                                                userAgentString = currentUa
                                                    .replace("; wv", "")
                                                    .replace(Regex("Version/[0-9.]+ "), "")
                                            }
                                        }
                                    } else if (v is android.view.ViewGroup) {
                                        for (i in 0 until v.childCount) {
                                            configureWebView(v.getChildAt(i))
                                        }
                                    }
                                }

                                configureWebView(this)
                                android.util.Log.d("DemonicPlayer", "Calling initialize() with app package origin: ${ctx.packageName}")
                                initialize(viewModel.playerManager.listener, false, options)
                                android.util.Log.d("DemonicPlayer", "initialize() called successfully")
                                post { configureWebView(this) }

                                // DEMONIC Multi-Layer Ad-Killer:
                                // 1. Injects CSS to hide ad banners & overlays
                                // 2. Runs interval to auto-click skip buttons and 16x fast-forward ads while preserving track playback
                                fun injectAdKiller() {
                                    fun findWebView(v: android.view.View): android.webkit.WebView? {
                                        if (v is android.webkit.WebView) return v
                                        if (v is android.view.ViewGroup) {
                                            for (i in 0 until v.childCount) {
                                                val found = findWebView(v.getChildAt(i))
                                                if (found != null) return found
                                            }
                                        }
                                        return null
                                    }

                                    findWebView(this)?.let { webView ->
                                        val adKillerJs = """
                                            (function() {
                                                if (window._demonicAdKillerInstalled) return;
                                                window._demonicAdKillerInstalled = true;
                                                try {
                                                    var style = document.createElement('style');
                                                    style.innerHTML = '.ytp-ad-overlay-container, .ytp-ad-message-container, .ytp-ad-action-interstitial, .companion-ad-container, .ytp-ad-preview-container, .ad-created, .ytp-ad-module, .ytp-ad-image-overlay, .ytp-ad-text-overlay, .video-ads, .ytp-ad-player-overlay { display: none !important; visibility: hidden !important; opacity: 0 !important; pointer-events: none !important; }';
                                                    document.head.appendChild(style);
                                                } catch(e) {}
                                                setInterval(function() {
                                                    try {
                                                        var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .videoAdUiSkipButton, .ytp-skip-ad-button, .ytp-ad-overlay-close-button');
                                                        if (skipBtn) skipBtn.click();
                                                        var adContainer = document.querySelector('.ad-showing, .ad-interrupting');
                                                        var video = document.querySelector('video');
                                                        if (adContainer && video) {
                                                            video.muted = true;
                                                            video.playbackRate = 16.0;
                                                        } else if (video && video.playbackRate > 1.0) {
                                                            video.playbackRate = 1.0;
                                                            video.muted = false;
                                                        }
                                                    } catch(e) {}
                                                }, 250);
                                            })();
                                        """.trimIndent()
                                        webView.evaluateJavascript(adKillerJs, null)
                                    }
                                }

                                postDelayed({ injectAdKiller() }, 1000)
                                postDelayed({ injectAdKiller() }, 3000)
                                postDelayed({ injectAdKiller() }, 6000)
                            }
                        },
                        update = { _ ->
                            val currentRoomVideoId = uiState.room?.videoId ?: ""
                            if (currentRoomVideoId.isNotEmpty() && currentRoomVideoId != viewModel.playerManager.activeVideoId.value) {
                                val isPlaying = uiState.room?.isPlaying ?: false
                                val pos = (uiState.room?.position ?: 0.0).toFloat()
                                viewModel.playerManager.loadOrCueVideo(currentRoomVideoId, pos, autoPlay = isPlaying)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Fullscreen Watch Party Pill Button in normal player mode
                    if (!isInPip && !isFullscreen && playerDisplayMode == PlayerDisplayMode.VIDEO && hasVideo) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.72f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    isFullscreen = true
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Watch Party",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Watch Party 🎬",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Vinyl Disc View Overlay
                if (!isInPip && hasVideo && playerDisplayMode == PlayerDisplayMode.VINYL) {
                    VinylDisc(
                        videoId = uiState.room?.videoId ?: "",
                        isPlaying = uiState.room?.isPlaying ?: false,
                        size = 180.dp
                    )
                }

                // Compact Mini Player Bar
                if (!isInPip && hasVideo && playerDisplayMode == PlayerDisplayMode.COMPACT) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DemonicSurface)
                            .border(1.dp, DemonicBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = "https://img.youtube.com/vi/${uiState.room?.videoId}/hqdefault.jpg",
                            contentDescription = "Thumbnail",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DemonicSurfaceVariant),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = uiState.room?.videoTitle ?: "Now Playing",
                                color = DemonicTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            CompactPlaybackStatusText(
                                playerManager = viewModel.playerManager,
                                isPlaying = (uiState.room?.isPlaying == true)
                            )
                        }
                        if (uiState.canControlPlayback) {
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    viewModel.togglePlayPause()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.room?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = DemonicCrimson,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    viewModel.skipToNextTrack()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next Track",
                                    tint = DemonicTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Empty Track Placeholder (When no song is loaded in room)
                if (!hasVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(DemonicSurface, Color(0xFF0F0B15))
                                )
                            )
                            .border(1.dp, DemonicBorder, RoundedCornerShape(14.dp))
                            .clickable { showExplorerSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompact) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = DemonicCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NO TRACK PLAYING • Tap to search",
                                    color = DemonicTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(DemonicCrimson.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = DemonicCrimson,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "NO TRACK PLAYING",
                                    color = DemonicTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (uiState.canControlPlayback) "Tap here or search icon to play music" else "Waiting for host to pick a track...",
                                    color = DemonicTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Fullscreen Watch Party Cinema HUD Overlay
                if (!isInPip && isFullscreen && hasVideo) {
                    val currentSecond by viewModel.playerManager.currentSecond.collectAsState()
                    val duration by viewModel.playerManager.duration.collectAsState()

                    WatchPartyFullscreenOverlay(
                        roomCode = uiState.roomCode,
                        videoTitle = uiState.room?.videoTitle ?: "",
                        currentSecond = currentSecond,
                        duration = duration,
                        isPlaying = uiState.room?.isPlaying == true,
                        canControlPlayback = uiState.canControlPlayback,
                        driftSeconds = uiState.driftSeconds,
                        isSyncing = uiState.isSyncing,
                        memberCount = uiState.members.size,
                        messages = uiState.messages,
                        hostId = uiState.room?.hostId,
                        djId = uiState.room?.djId,
                        onToggleOrientation = {
                            isLandscape = !isLandscape
                            activity?.requestedOrientation = if (isLandscape) {
                                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                        },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSeekTo = { viewModel.hostSeekTo(it) },
                        onRewind10 = {
                            val safeSec = (currentSecond - 10f).coerceAtLeast(0f)
                            viewModel.hostSeekTo(safeSec)
                        },
                        onForward10 = {
                            val safeDuration = if (duration > 0f) duration else 1f
                            val safeSec = (currentSecond + 10f).coerceAtMost(safeDuration)
                            viewModel.hostSeekTo(safeSec)
                        },
                        onSkipNext = { viewModel.skipToNextTrack() },
                        hasQueue = uiState.queue.isNotEmpty(),
                        onSendReaction = { emoji ->
                            viewModel.sendReaction(emoji)
                        },
                        onSendChatMessage = { msg ->
                            viewModel.updateChatInput(msg)
                            viewModel.sendChatMessage()
                        },
                        onExitFullscreen = {
                            isFullscreen = false
                            isLandscape = false
                            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    )
                }
            }

            if (!isInPip && !isFullscreen) {
                // Now Playing Track Banner with Live Animated Audio Equalizer
                if (playerDisplayMode != PlayerDisplayMode.COMPACT && !uiState.room?.videoId.isNullOrEmpty()) {
                    NowPlayingTrackBanner(
                        title = uiState.room?.videoTitle ?: "",
                        isPlaying = uiState.room?.isPlaying ?: false,
                        nextTrackTitle = uiState.queue.firstOrNull()?.title,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                        onClick = { showQueueSheet = true }
                    )
                }

                // Playback Controls & Progress Scrubber (Host or DJ) - Hidden in Compact Mini-Player mode
                if (uiState.canControlPlayback && playerDisplayMode != PlayerDisplayMode.COMPACT) {
                    RoomPlaybackControlsSection(
                        viewModel = viewModel,
                        isPlaying = uiState.room?.isPlaying ?: false
                    )
                }

                // Live Member Avatars Row (Hidden in Compact mode for maximum chat space)
                if (playerDisplayMode != PlayerDisplayMode.COMPACT) {
                    MemberAvatarRow(
                        members = uiState.members,
                        djId = uiState.room?.djId,
                        isHostUser = uiState.isHost,
                        onPassAux = { targetUid ->
                            viewModel.passTheAux(targetUid)
                        },
                        onOpenMembersSheet = {
                            showMembersSheet = true
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Live Chat Stream
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    if (uiState.messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF130F1C))
                                    .border(1.dp, DemonicBorder, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Text(text = "🎵", fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Welcome to Room ${uiState.roomCode}",
                                    color = DemonicTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Drop a beat, react with emojis, or long-press messages to reply!",
                                    color = DemonicTextMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            reverseLayout = false
                        ) {
                            items(uiState.messages, key = { it.id }) { message ->
                                val isOwnMessage = (message.senderId == currentUser?.uid)
                                ChatMessageItem(
                                    message = message,
                                    isOwnMessage = isOwnMessage,
                                    isHost = (message.senderId == uiState.room?.hostId),
                                    isDj = (message.senderId == uiState.room?.djId),
                                    onLongClick = { actionMessage = it }
                                )
                            }
                        }

                        // Floating Scroll-To-Bottom Pill Button
                        ScrollToBottomFloatingButton(
                            visible = !isScrolledToBottom,
                            onClick = {
                                scope.launch {
                                    listState.animateScrollToItem(uiState.messages.size - 1)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 8.dp, end = 6.dp)
                        )
                    }
                }

                // Typing Indicator Bubble
                AnimatedVisibility(
                    visible = uiState.typingUsers.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TypingIndicatorBubble(
                        typingUsers = uiState.typingUsers,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                    )
                }

                val currentMember = remember(uiState.members, currentUser?.uid) {
                    uiState.members.find { it.uid == currentUser?.uid }
                }
                val isCurrentMemberTimedOut = remember(currentMember?.timedOutUntil) {
                    (currentMember?.timedOutUntil ?: 0L) > System.currentTimeMillis()
                }

                // Reaction bar + Chat Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReactionButtonBar(
                        onSendReaction = { emoji ->
                            if (isCurrentMemberTimedOut) {
                                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                Toast.makeText(context, "You are timed out by the host ⏱️", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.sendReaction(emoji)
                            }
                        }
                    )
                }

                ChatInputBar(
                    value = uiState.chatInput,
                    onValueChange = { viewModel.updateChatInput(it) },
                    onSend = { viewModel.sendChatMessage() },
                    replyingTo = uiState.replyingToMessage,
                    onCancelReply = { viewModel.clearReply() },
                    isTimedOut = isCurrentMemberTimedOut,
                    timedOutUntil = currentMember?.timedOutUntil ?: 0L,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        if (!isInPip) {
            // Floating Live Reactions Overlay
            FloatingReactionsOverlay(
                activeReactions = uiState.activeReactions,
                modifier = Modifier.fillMaxSize()
            )

            // YouTube Search Dialog
            if (showSearchDialog) {
                YouTubeSearchDialog(
                    searchManager = searchManager,
                    canControlPlayback = uiState.canControlPlayback,
                    onDismiss = { showSearchDialog = false },
                    onPlayNow = { videoId, title ->
                        viewModel.playTrack(videoId, title)
                    },
                    onAddToQueue = { videoId, title ->
                        viewModel.addToQueue(videoId, title)
                    }
                )
            }

            // Playlist / Up Next Bottom Sheet
            if (showQueueSheet) {
                QueueBottomSheet(
                    queue = uiState.queue,
                    currentVideoTitle = uiState.room?.videoTitle ?: "",
                    currentVideoId = uiState.room?.videoId ?: "",
                    canControlPlayback = uiState.canControlPlayback,
                    currentUid = currentUser?.uid ?: "",
                    onDismiss = { showQueueSheet = false },
                    onOpenSearch = { showExplorerSheet = true },
                    onPlayTrack = { track ->
                        viewModel.playQueueItem(track)
                    },
                    onRemoveTrack = { itemId ->
                        viewModel.removeFromQueue(itemId)
                    },
                    onMoveTrack = { from, to ->
                        viewModel.reorderQueue(from, to)
                    },
                    onUpvoteTrack = { itemId ->
                        viewModel.upvoteQueueItem(itemId)
                    }
                )
            }

            // Live Members Bottom Sheet
            if (showMembersSheet) {
                MembersBottomSheet(
                    members = uiState.members,
                    currentUid = currentUser?.uid ?: "",
                    hostId = uiState.room?.hostId ?: "",
                    djId = uiState.room?.djId,
                    isHost = uiState.isHost,
                    onDismiss = { showMembersSheet = false },
                    onPassAux = { targetUid ->
                        viewModel.passTheAux(targetUid)
                    },
                    onTimeoutMember = { uid, name, minutes ->
                        viewModel.timeoutMember(uid, name, minutes)
                    },
                    onRemoveTimeout = { uid, name ->
                        viewModel.removeTimeout(uid, name)
                    }
                )
            }

            // Demonic Music Hub Sheet (Zero-Search Picks, Playlist Auto-Queue, Native Playback)
            if (showExplorerSheet) {
                YouTubeExplorerSheet(
                    searchManager = searchManager,
                    canControlPlayback = uiState.canControlPlayback,
                    onDismiss = { showExplorerSheet = false },
                    onPlayNow = { videoId, title ->
                        viewModel.playTrack(videoId, title)
                    },
                    onAddToQueue = { videoId, title ->
                        viewModel.addToQueue(videoId, title)
                    },
                    onImportPlaylistOrLink = { input, onProgress, onSuccess, onError ->
                        viewModel.importPlaylistOrLink(input, onProgress, onSuccess, onError)
                    }
                )
            }

            // Room QR Code Share Dialog
            if (showQrDialog) {
                RoomQrDialog(
                    roomCode = uiState.roomCode,
                    onDismiss = { showQrDialog = false }
                )
            }

            // Sleep Timer Dialog
            if (showSleepTimerDialog) {
                SleepTimerDialog(
                    currentMinutes = sleepTimerMinutes,
                    onSelectMinutes = { viewModel.setSleepTimer(it) },
                    onDismiss = { showSleepTimerDialog = false }
                )
            }

            // Message Action Dialog (Quick Reaction, Reply, Copy Text, Delete, Timeout)
            actionMessage?.let { msg ->
                val isOwnMsg = (msg.senderId == currentUser?.uid)
                val canDelete = isOwnMsg || uiState.isHost
                val canTimeout = uiState.isHost && !isOwnMsg && msg.senderId != "system"

                ChatMessageActionDialog(
                    message = msg,
                    canDelete = canDelete,
                    canTimeout = canTimeout,
                    onDismiss = { actionMessage = null },
                    onReply = {
                        viewModel.setReplyingTo(it)
                    },
                    onSendReaction = { emoji ->
                        viewModel.sendReaction(emoji)
                    },
                    onDeleteMessage = {
                        viewModel.deleteMessage(msg.id)
                    },
                    onTimeoutUser = { mins ->
                        viewModel.timeoutMember(msg.senderId, msg.senderName, mins)
                    }
                )
            }
        }
    }
}

private fun formatSeconds(seconds: Float): String {
    val totalSeconds = seconds.toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val remSeconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, remSeconds)
}

@Composable
private fun CompactPlaybackStatusText(
    playerManager: com.nddfeon.demonic.player.YouTubePlayerManager,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val currentSecond by playerManager.currentSecond.collectAsState()
    Text(
        text = if (isPlaying) "Playing • ${formatSeconds(currentSecond)}" else "Paused",
        color = DemonicCrimson,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
private fun RoomPlaybackControlsSection(
    viewModel: RoomViewModel,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val currentSecond by viewModel.playerManager.currentSecond.collectAsState()
    val duration by viewModel.playerManager.duration.collectAsState()
    val view = LocalView.current

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val displayPosition = if (isDraggingSlider) sliderPosition else currentSecond
    val hasDuration = duration > 0f
    val safeDuration = if (hasDuration) duration else 1f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Slider(
            value = if (hasDuration) displayPosition.coerceIn(0f, safeDuration) else 0f,
            onValueChange = {
                if (hasDuration) {
                    isDraggingSlider = true
                    sliderPosition = it
                }
            },
            onValueChangeFinished = {
                if (hasDuration) {
                    isDraggingSlider = false
                    viewModel.hostSeekTo(sliderPosition)
                }
            },
            enabled = hasDuration,
            valueRange = 0f..safeDuration,
            colors = SliderDefaults.colors(
                thumbColor = if (hasDuration) DemonicCrimson else Color.Transparent,
                activeTrackColor = DemonicCrimson,
                inactiveTrackColor = Color(0xFF2B2238),
                disabledThumbColor = Color.Transparent,
                disabledActiveTrackColor = DemonicSurfaceVariant,
                disabledInactiveTrackColor = Color(0xFF2B2238)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${formatSeconds(displayPosition)} / ${if (hasDuration) formatSeconds(duration) else "--:--"}",
                color = DemonicTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.hostSeekTo((currentSecond - 10f).coerceAtLeast(0f))
                    },
                    enabled = hasDuration
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind 10s",
                        tint = if (hasDuration) DemonicTextPrimary else DemonicTextMuted.copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(12.dp, CircleShape, spotColor = DemonicCrimson)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(DemonicCrimson, DemonicCrimsonDark)
                            )
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            viewModel.togglePlayPause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.hostSeekTo((currentSecond + 10f).coerceAtMost(safeDuration))
                    },
                    enabled = hasDuration
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward 10s",
                        tint = if (hasDuration) DemonicTextPrimary else DemonicTextMuted.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}