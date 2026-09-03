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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.nddfeon.demonic.ui.components.ChatInputBar
import com.nddfeon.demonic.ui.components.ChatMessageItem
import com.nddfeon.demonic.ui.components.DemonicButton
import com.nddfeon.demonic.ui.components.DemonicButtonVariant
import com.nddfeon.demonic.ui.components.DemonicTextField
import com.nddfeon.demonic.ui.components.MemberAvatarRow
import com.nddfeon.demonic.ui.components.SyncStatusBadge
import com.nddfeon.demonic.ui.components.TypingIndicatorBubble
import com.nddfeon.demonic.ui.components.VinylDisc
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicCrimsonDark
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicTextSecondary
import com.nddfeon.demonic.ui.theme.DemonicWarningAmber
import com.nddfeon.demonic.viewmodel.RoomViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.launch

@Composable
fun RoomScreen(
    viewModel: RoomViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val currentSecond by viewModel.playerManager.currentSecond.collectAsState()
    val duration by viewModel.playerManager.duration.collectAsState()

    var showPlayerVideo by remember { mutableStateOf(false) }

    // Chat auto-scroll state
    val chatListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll when new message arrives IF user is already near bottom
    val isNearBottom by remember {
        derivedStateOf {
            val total = chatListState.layoutInfo.totalItemsCount
            if (total == 0) true
            else {
                val lastVisible = chatListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisible >= total - 2
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (isNearBottom && uiState.messages.isNotEmpty()) {
            chatListState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Release player resources on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.leaveRoom()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DemonicBackground)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DemonicSurface)
                .border(1.dp, DemonicBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onNavigateBack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Leave Room",
                        tint = DemonicTextPrimary
                    )
                }

                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = "ROOM ${uiState.roomCode}",
                        color = DemonicCrimson,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (uiState.isHost) "HOST (Controls Active)" else "LISTENER",
                        color = if (uiState.isHost) DemonicWarningAmber else DemonicTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sync status badge
                SyncStatusBadge(
                    driftSeconds = uiState.driftSeconds,
                    isSyncing = uiState.isSyncing
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Toggle Video View / Disc View
                IconButton(onClick = { showPlayerVideo = !showPlayerVideo }) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Toggle Video",
                        tint = if (showPlayerVideo) DemonicCrimson else DemonicTextMuted
                    )
                }

                // Share Room Button
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                "Join my DEMONIC synchronized music room!"
                            )
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Join my synchronized room on DEMONIC with code: ${uiState.roomCode}\n\nDeep Link: demonic://room/${uiState.roomCode}\nWeb Link: https://demonic.app/room/${uiState.roomCode}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Room Code"))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = DemonicTextPrimary
                    )
                }
            }
        }

        // Embedded YouTube Player View (always present to preserve audio playback)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (showPlayerVideo) {
                        Modifier
                            .height(200.dp)
                            .background(Color.Black)
                    } else {
                        // Invisible 1dp player when user enjoys the vinyl disc centerpiece
                        Modifier
                            .height(1.dp)
                            .background(Color.Transparent)
                    }
                )
        ) {
            AndroidView(
                factory = { ctx ->
                    YouTubePlayerView(ctx).apply {
                        enableAutomaticInitialization = false
                        initialize(viewModel.playerManager.listener)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Centerpiece: Rotating Vinyl Disc & Sync Status
        if (!showPlayerVideo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                VinylDisc(
                    videoId = uiState.room?.videoId ?: "",
                    isPlaying = uiState.room?.isPlaying ?: false,
                    size = 180.dp
                )
            }
        }

        // Host Video URL / ID Input (Visible ONLY for Host)
        if (uiState.isHost) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DemonicTextField(
                        value = uiState.videoInput,
                        onValueChange = { viewModel.updateVideoInput(it) },
                        placeholder = "Paste YouTube link or video ID...",
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    DemonicButton(
                        text = "Load",
                        onClick = {
                            viewModel.hostChangeVideo(uiState.videoInput)
                        },
                        enabled = uiState.videoInput.isNotBlank()
                    )
                }
            }
        }

        // Host Playback Controls (Visible and usable ONLY for the host)
        if (uiState.isHost) {
            var isDraggingSlider by remember { mutableStateOf(false) }
            var sliderPosition by remember { mutableFloatStateOf(0f) }

            val displayPosition = if (isDraggingSlider) sliderPosition else currentSecond
            val safeDuration = if (duration > 0f) duration else 100f

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // Seek Bar Slider
                Slider(
                    value = displayPosition.coerceIn(0f, safeDuration),
                    onValueChange = {
                        isDraggingSlider = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        isDraggingSlider = false
                        viewModel.hostSeekTo(sliderPosition)
                    },
                    valueRange = 0f..safeDuration,
                    colors = SliderDefaults.colors(
                        thumbColor = DemonicCrimson,
                        activeTrackColor = DemonicCrimson,
                        inactiveTrackColor = Color(0xFF2B2238)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time labels & Playback buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatSeconds(displayPosition),
                        color = DemonicTextMuted,
                        fontSize = 11.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Rewind 10s
                        IconButton(onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.hostSeekTo((currentSecond - 10f).coerceAtLeast(0f))
                        }) {
                            Icon(
                                imageVector = Icons.Default.FastRewind,
                                contentDescription = "Rewind 10s",
                                tint = DemonicTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Play / Pause Toggle
                        val isPlaying = uiState.room?.isPlaying ?: false
                        Box(
                            modifier = Modifier
                                .size(48.dp)
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
                                tint = DemonicTextPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Fast Forward 10s
                        IconButton(onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.hostSeekTo((currentSecond + 10f).coerceAtMost(safeDuration))
                        }) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Forward 10s",
                                tint = DemonicTextPrimary
                            )
                        }
                    }

                    Text(
                        text = formatSeconds(safeDuration),
                        color = DemonicTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            // Non-host banner: Informs listener that host is controlling playback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DemonicSurfaceVariant)
                    .border(1.dp, DemonicBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Playback synchronized to Host • ${formatSeconds(currentSecond)}",
                    color = DemonicTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Live Member List Row
        MemberAvatarRow(
            members = uiState.members,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Chat Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            Text(
                text = "LIVE ROOM CHAT",
                color = DemonicTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Live Chat Message List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (uiState.messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No messages yet. Start the conversation!",
                        color = DemonicTextMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        val isOwn = (message.senderId == currentUser?.uid)
                        ChatMessageItem(
                            message = message,
                            isOwnMessage = isOwn
                        )
                    }
                }
            }
        }

        // Typing Indicator Bubble
        TypingIndicatorBubble(typingUsers = uiState.typingUsers)

        // Chat Input Bar
        ChatInputBar(
            value = uiState.chatInput,
            onValueChange = { viewModel.updateChatInput(it) },
            onSend = {
                viewModel.sendChatMessage()
                coroutineScope.launch {
                    if (uiState.messages.isNotEmpty()) {
                        chatListState.animateScrollToItem(uiState.messages.size - 1)
                    }
                }
            }
        )
    }
}

private fun formatSeconds(totalSeconds: Float): String {
    val sec = totalSeconds.toInt()
    val minutes = sec / 60
    val remainingSec = sec % 60
    return "%02d:%02d".format(minutes, remainingSec)
}
