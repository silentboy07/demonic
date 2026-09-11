package com.nddfeon.demonic.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nddfeon.demonic.data.model.ChatMessage
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicCrimsonDark
import com.nddfeon.demonic.ui.theme.DemonicErrorRed
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatRoleBadge(
    role: String,
    modifier: Modifier = Modifier
) {
    when (role.uppercase()) {
        "HOST" -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF382A05))
                    .border(0.8.dp, Color(0xFFFFB300), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👑 HOST",
                    color = Color(0xFFFFC107),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
        "DJ" -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF26123D))
                    .border(0.8.dp, DemonicViolet, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎧 DJ",
                    color = Color(0xFFD4B0FF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun QuotedReplyInBubble(
    senderName: String,
    text: String,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isOwnMessage) Color(0xFFFFD54F) else DemonicCrimson

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x3D000000))
            .border(0.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(start = 6.dp, top = 4.dp, bottom = 4.dp, end = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "↳ $senderName",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = text,
                    color = DemonicTextPrimary.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isOwnMessage: Boolean,
    isHost: Boolean = false,
    isDj: Boolean = false,
    onLongClick: ((ChatMessage) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.sentAt) {
        if (message.sentAt > 0) timeFormat.format(Date(message.sentAt)) else ""
    }

    if (message.senderId == "system") {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1928))
                    .border(1.dp, DemonicBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = message.text,
                    color = DemonicTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    val effectiveRole = when {
        isHost || message.senderRole.equals("HOST", ignoreCase = true) -> "HOST"
        isDj || message.senderRole.equals("DJ", ignoreCase = true) -> "DJ"
        else -> ""
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = spring(stiffness = 500f)) +
                slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f)
                ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isOwnMessage) {
                // Sender Avatar
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF221C2E))
                        .border(1.dp, DemonicBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (message.senderPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(message.senderPhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = message.senderName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(
                            text = message.senderName.firstOrNull()?.uppercase() ?: "D",
                            color = DemonicTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Bubble Column
            Column(
                horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
                modifier = Modifier.widthIn(max = 290.dp)
            ) {
                // Header (Sender Name + Role Badge)
                if (!isOwnMessage || effectiveRole.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start,
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 2.dp)
                    ) {
                        if (!isOwnMessage) {
                            Text(
                                text = message.senderName,
                                color = DemonicViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (effectiveRole.isNotEmpty()) {
                            if (!isOwnMessage) Spacer(modifier = Modifier.width(6.dp))
                            ChatRoleBadge(role = effectiveRole)
                        }
                    }
                }

                val bubbleShape = if (isOwnMessage) {
                    RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                } else {
                    RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                }

                Box(
                    modifier = Modifier
                        .clip(bubbleShape)
                        .background(
                            if (isOwnMessage) {
                                Brush.horizontalGradient(listOf(DemonicCrimson, DemonicCrimsonDark))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF1B1724), Color(0xFF14111C)))
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isOwnMessage) DemonicCrimson.copy(alpha = 0.45f) else DemonicBorder,
                            shape = bubbleShape
                        )
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                onLongClick?.invoke(message)
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Column {
                        // Quoted Reply Preview
                        if (message.replyToText.isNotBlank()) {
                            QuotedReplyInBubble(
                                senderName = message.replyToSenderName.ifBlank { "User" },
                                text = message.replyToText,
                                isOwnMessage = isOwnMessage,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        // Message Text
                        Text(
                            text = message.text,
                            color = DemonicTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 19.sp
                        )
                    }
                }

                if (formattedTime.isNotEmpty()) {
                    Text(
                        text = formattedTime,
                        color = DemonicTextMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageActionDialog(
    message: ChatMessage,
    canDelete: Boolean = false,
    canTimeout: Boolean = false,
    onDismiss: () -> Unit,
    onReply: (ChatMessage) -> Unit,
    onSendReaction: (String) -> Unit,
    onDeleteMessage: () -> Unit = {},
    onTimeoutUser: (durationMinutes: Int) -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showTimeoutOptions by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DemonicSurface)
                .border(1.dp, DemonicBorder, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Message Actions",
                        color = DemonicTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DemonicTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Message Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF14101D))
                        .border(1.dp, DemonicSurfaceVariant, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = message.senderName,
                            color = DemonicViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = message.text,
                            color = DemonicTextPrimary,
                            fontSize = 13.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Quick Reactions Row
                Text(
                    text = "Quick Reaction",
                    color = DemonicTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF191325))
                        .border(1.dp, DemonicBorder, RoundedCornerShape(14.dp))
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AVAILABLE_REACTIONS.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onSendReaction(emoji)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Reply & Copy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Reply Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(DemonicViolet, Color(0xFF6B2FB8))))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                onReply(message)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = "Reply",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reply",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Copy Text Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF221A30))
                            .border(1.dp, DemonicBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("demonic_chat", message.text)
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "Text copied to clipboard 📋", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = DemonicTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Copy",
                                color = DemonicTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Host Timeout User Button & Option Selector
                if (canTimeout) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (!showTimeoutOptions) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2B1D0E))
                                .border(1.dp, Color(0xFFFFB300).copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    showTimeoutOptions = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timeout User",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Timeout User (${message.senderName})",
                                    color = Color(0xFFFFC107),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Duration picker chips: 1m, 5m, 15m
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1 to "1 Min", 5 to "5 Min", 15 to "15 Min").forEach { (mins, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF382208))
                                        .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(10.dp))
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                            onTimeoutUser(mins)
                                            Toast.makeText(context, "${message.senderName} timed out for $label ⏱️", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = Color(0xFFFFC107),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Delete Message Button (User own message or Host)
                if (canDelete) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF281116))
                            .border(1.dp, DemonicErrorRed.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onDeleteMessage()
                                Toast.makeText(context, "Message deleted 🗑️", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = DemonicErrorRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delete Message",
                                color = DemonicErrorRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReplyPreviewBanner(
    replyingTo: ChatMessage,
    onCancelReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .background(Color(0xFF1A1426))
            .border(1.dp, DemonicViolet.copy(alpha = 0.5f), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DemonicViolet)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Replying to @${replyingTo.senderName}",
                    color = DemonicViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = replyingTo.text,
                    color = DemonicTextMuted,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onCancelReply()
                },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Reply",
                    tint = DemonicTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ScrollToBottomFloatingButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .shadow(10.dp, CircleShape, spotColor = DemonicCrimson)
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2E2242), Color(0xFF1B1428))
                    )
                )
                .border(1.dp, DemonicViolet.copy(alpha = 0.8f), CircleShape)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Scroll to bottom",
                tint = DemonicTextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun TypingIndicatorBubble(
    typingUsers: List<String>,
    modifier: Modifier = Modifier
) {
    if (typingUsers.isEmpty()) return

    val displayText = when (typingUsers.size) {
        1 -> "${typingUsers.first()} is typing"
        2 -> "${typingUsers[0]} and ${typingUsers[1]} are typing"
        else -> "${typingUsers.size} people are typing"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "TypingDots")
    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1"
    )
    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 130, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot2"
    )
    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 260, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot3"
    )

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF171321))
                .border(1.dp, DemonicBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 3 animated wave dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = dot1Offset.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(DemonicCrimson)
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = dot2Offset.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(DemonicCrimson)
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = dot3Offset.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(DemonicCrimson)
                    )
                }

                Text(
                    text = displayText,
                    color = DemonicTextMuted,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    replyingTo: ChatMessage? = null,
    onCancelReply: () -> Unit = {},
    isTimedOut: Boolean = false,
    timedOutUntil: Long = 0L,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val sendScale by animateFloatAsState(
        targetValue = if (isPressed && value.isNotBlank() && !isTimedOut) 0.9f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "SendScale"
    )

    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0D15))
            .border(
                width = 1.dp,
                color = if (isTimedOut) Color(0xFF5A1A22) else Color(0xFF261F33),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
    ) {
        if (isTimedOut) {
            val remainingSec = ((timedOutUntil - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            val remMins = remainingSec / 60
            val remSecs = remainingSec % 60
            val timeDisplay = if (remMins > 0) "${remMins}m ${remSecs}s" else "${remSecs}s"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2B1015))
                    .border(1.dp, DemonicErrorRed.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⏱️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You are timed out by the host ($timeDisplay remaining)",
                        color = Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Quoted Reply Preview Banner
            AnimatedVisibility(
                visible = replyingTo != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (replyingTo != null) {
                    ReplyPreviewBanner(
                        replyingTo = replyingTo,
                        onCancelReply = onCancelReply
                    )
                }
            }

            // Input & Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Input Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(DemonicSurfaceVariant)
                        .border(1.dp, if (replyingTo != null) DemonicViolet else DemonicBorder, shape)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = if (replyingTo != null) "Reply to @${replyingTo.senderName}..." else "Say something in room...",
                            color = DemonicTextMuted,
                            fontSize = 14.sp
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (value.isNotBlank()) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onSend()
                            }
                        }),
                        textStyle = TextStyle(
                            color = DemonicTextPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(DemonicCrimson)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Tactile Send Button
                val canSend = value.isNotBlank()
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .scale(sendScale)
                        .shadow(if (canSend) 8.dp else 0.dp, CircleShape, spotColor = DemonicCrimson)
                        .clip(CircleShape)
                        .background(
                            if (canSend) {
                                Brush.linearGradient(listOf(DemonicCrimson, DemonicCrimsonDark))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF231C2E), Color(0xFF191322)))
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = canSend
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onSend()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) DemonicTextPrimary else DemonicTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
