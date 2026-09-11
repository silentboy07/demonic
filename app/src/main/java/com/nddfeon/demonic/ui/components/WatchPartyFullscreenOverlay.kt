package com.nddfeon.demonic.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nddfeon.demonic.data.model.ChatMessage
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicCrimsonDark
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicTextSecondary
import com.nddfeon.demonic.ui.theme.DemonicViolet
import com.nddfeon.demonic.ui.theme.DemonicWarningAmber
import kotlinx.coroutines.delay

@Composable
fun WatchPartyFullscreenOverlay(
    roomCode: String,
    videoTitle: String,
    currentSecond: Float,
    duration: Float,
    isPlaying: Boolean,
    canControlPlayback: Boolean,
    driftSeconds: Float,
    isSyncing: Boolean,
    memberCount: Int,
    messages: List<ChatMessage>,
    hostId: String?,
    djId: String?,
    onToggleOrientation: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    onSkipNext: () -> Unit,
    hasQueue: Boolean,
    onSendReaction: (String) -> Unit,
    onSendChatMessage: (String) -> Unit,
    onExitFullscreen: () -> Unit,
    isMutedLocally: Boolean = false,
    onToggleLocalMute: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showControls by remember { mutableStateOf(true) }
    var showFloatingChat by remember { mutableStateOf(true) }
    var showQuickChatDialog by remember { mutableStateOf(false) }

    // Auto-hide controls after 4.5 seconds of inactivity
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4500L)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // TOP HUD BAR (Room Code, Title, Sync, Rotate, Chat Toggle, Viewer Count, Exit)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.88f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Exit Button + Room Info & Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                onExitFullscreen()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Exit Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.widthIn(max = 240.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ROOM $roomCode",
                                    color = DemonicCrimson,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DemonicCrimsonDark.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "WATCH PARTY 🎬",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = videoTitle.ifEmpty { "Synchronized Stream" },
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right: Actions (Sync Badge, Orientation, Chat Toggle, Viewers)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SyncStatusBadge(
                            driftSeconds = driftSeconds,
                            isSyncing = isSyncing
                        )

                        // Local Mute / AFK Toggle
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                onToggleLocalMute()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isMutedLocally) Color(0xFF38230D) else Color.Black.copy(alpha = 0.5f))
                                .border(1.dp, if (isMutedLocally) Color(0xFFFFB300) else Color.Transparent, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isMutedLocally) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = "Local Mute",
                                tint = if (isMutedLocally) Color(0xFFFFC107) else Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Screen Orientation Toggle
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                onToggleOrientation()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "Rotate Screen",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Floating Chat Toggle Button
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                showFloatingChat = !showFloatingChat
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (showFloatingChat) DemonicViolet.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (showFloatingChat) Icons.AutoMirrored.Filled.Chat else Icons.Default.ChatBubbleOutline,
                                contentDescription = "Toggle Floating Chat",
                                tint = if (showFloatingChat) DemonicViolet else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Watching Members Pill
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, DemonicBorder, RoundedCornerShape(8.dp))
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$memberCount",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // FLOATING LIVE WATCH PARTY CHAT (Twitch / TikTok Live Style Overlay)
        if (showFloatingChat && messages.isNotEmpty()) {
            val recentMessages = remember(messages) {
                messages.filter { it.senderId != "system" }.takeLast(4)
            }
            val chatListState = rememberLazyListState()

            LaunchedEffect(recentMessages.size) {
                if (recentMessages.isNotEmpty()) {
                    chatListState.animateScrollToItem(recentMessages.size - 1)
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = 16.dp,
                        bottom = if (showControls) 130.dp else 24.dp
                    )
                    .widthIn(max = 280.dp)
            ) {
                LazyColumn(
                    state = chatListState,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(recentMessages, key = { it.id }) { msg ->
                        val isHost = (msg.senderId == hostId)
                        val isDj = (msg.senderId == djId)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.62f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when {
                                        isHost -> "${msg.senderName} 👑"
                                        isDj -> "${msg.senderName} 🎧"
                                        else -> msg.senderName
                                    },
                                    color = when {
                                        isHost -> DemonicWarningAmber
                                        isDj -> DemonicViolet
                                        else -> Color(0xFF00F5D4)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = msg.text,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM HUD BAR (Scrubber, Controls, Reaction Emojis & Quick Chat Pill)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.94f))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Progress Slider
                    var isDraggingSlider by remember { mutableStateOf(false) }
                    var sliderPosition by remember { mutableFloatStateOf(0f) }
                    val displayPosition = if (isDraggingSlider) sliderPosition else currentSecond
                    val hasDuration = duration > 0f
                    val safeDuration = if (hasDuration) duration else 1f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatSecondsLocal(displayPosition),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Slider(
                            value = if (hasDuration) displayPosition.coerceIn(0f, safeDuration) else 0f,
                            onValueChange = {
                                if (hasDuration && canControlPlayback) {
                                    isDraggingSlider = true
                                    sliderPosition = it
                                }
                            },
                            onValueChangeFinished = {
                                if (hasDuration && canControlPlayback) {
                                    isDraggingSlider = false
                                    onSeekTo(sliderPosition)
                                }
                            },
                            enabled = hasDuration && canControlPlayback,
                            valueRange = 0f..safeDuration,
                            colors = SliderDefaults.colors(
                                thumbColor = if (hasDuration && canControlPlayback) DemonicCrimson else Color.Transparent,
                                activeTrackColor = DemonicCrimson,
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                                disabledThumbColor = Color.Transparent,
                                disabledActiveTrackColor = DemonicCrimson.copy(alpha = 0.7f),
                                disabledInactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        Text(
                            text = if (hasDuration) formatSecondsLocal(duration) else "--:--",
                            color = DemonicTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Playback Controls Row + Quick Reactions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Playback Controls (Rewind, Play/Pause, Forward, Next)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (canControlPlayback) {
                                IconButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onRewind10()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FastRewind,
                                        contentDescription = "Rewind 10s",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .shadow(8.dp, CircleShape, spotColor = DemonicCrimson)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(DemonicCrimson, DemonicCrimsonDark)
                                            )
                                        )
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                            onTogglePlayPause()
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

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        onForward10()
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FastForward,
                                        contentDescription = "Forward 10s",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                if (hasQueue) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                            onSkipNext()
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
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isPlaying) "🎧 Playing with Host" else "⏸️ Paused by Host",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Right: Quick Emoji Reactions & Quick Chat Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickEmojis = listOf("🔥", "😈", "🤘", "💖", "😂")
                            quickEmojis.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            onSendReaction(emoji)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 16.sp)
                                }
                            }

                            // Quick Chat Pill
                            Box(
                                modifier = Modifier
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(DemonicSurfaceVariant)
                                    .border(1.dp, DemonicBorder, RoundedCornerShape(17.dp))
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                        showQuickChatDialog = true
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "Quick Chat",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Chat...",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // QUICK CHAT MODAL (Chat without leaving Fullscreen Watch Party)
        if (showQuickChatDialog) {
            var inputMessage by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showQuickChatDialog = false },
                containerColor = DemonicSurface,
                title = {
                    Text(
                        text = "Watch Party Chat 🎬",
                        color = DemonicTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Say something in the room...", color = DemonicTextMuted, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DemonicCrimson,
                            unfocusedBorderColor = DemonicBorder,
                            focusedTextColor = DemonicTextPrimary,
                            unfocusedTextColor = DemonicTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (inputMessage.trim().isNotEmpty()) {
                                onSendChatMessage(inputMessage.trim())
                                inputMessage = ""
                                showQuickChatDialog = false
                            }
                        }
                    ) {
                        Text("Send 🚀", color = DemonicCrimson, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuickChatDialog = false }) {
                        Text("Cancel", color = DemonicTextMuted)
                    }
                }
            )
        }
    }
}

private fun formatSecondsLocal(seconds: Float): String {
    val totalSeconds = seconds.toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val remSeconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, remSeconds)
}
