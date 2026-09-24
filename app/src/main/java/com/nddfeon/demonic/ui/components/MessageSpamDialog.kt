package com.nddfeon.demonic.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicErrorRed
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicTextSecondary
import com.nddfeon.demonic.ui.theme.DemonicWarningAmber
import kotlin.math.roundToInt

private val QUICK_PRESET_MESSAGES = listOf(
    "🔥 OP BHAI",
    "⚡ HYPEEE",
    "🚀 NEXT LEVEL",
    "💖 LOVE IT",
    "😈 DEMONIC",
    "👑 GOD LEVEL"
)

private val COUNT_PRESETS = listOf(5, 10, 25, 50, 100)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageSpamDialog(
    isBlasting: Boolean,
    blastSent: Int,
    blastTotal: Int,
    isTimedOut: Boolean,
    onStartBlast: (text: String, count: Int) -> Unit,
    onStopBlast: () -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    var messageText by rememberSaveable { mutableStateOf("🔥 OP BHAI") }
    var count by rememberSaveable { mutableIntStateOf(20) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    BasicAlertDialog(
        onDismissRequest = {
            if (!isBlasting) onDismiss()
            else onDismiss()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DemonicSurface)
                .border(1.2.dp, if (isBlasting) Color(0xFFFF1744) else DemonicBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isBlasting) Color(0xFFFF1744).copy(alpha = 0.2f)
                                    else Color(0xFFFF5722).copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBlasting) "💣" else "⚡",
                                fontSize = 18.sp,
                                modifier = if (isBlasting) Modifier.scale(pulseScale) else Modifier
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBlasting) "BLASTING CHAT..." else "CHAT SPAMMER",
                                color = if (isBlasting) Color(0xFFFF5252) else DemonicTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = if (isBlasting) "Rapid flood in progress (Max 100)" else "Send rapid messages instantly (Max 100)",
                                color = DemonicTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DemonicTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress state when currently blasting
                if (isBlasting) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF280B10))
                            .border(1.dp, Color(0xFFFF1744).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rapid Burst Active ⚡",
                                    color = Color(0xFFFF5252),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$blastSent / $blastTotal",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val progress = if (blastTotal > 0) blastSent.toFloat() / blastTotal.toFloat() else 0f
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFFF1744),
                                trackColor = Color.White.copy(alpha = 0.1f),
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "“$messageText”",
                                color = DemonicTextSecondary,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Stop Blast Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF1744), Color(0xFFD50000))
                                )
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                onStopBlast()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.StopCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STOP BLAST NOW",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                } else {
                    // Input: Message Text
                    Text(
                        text = "MESSAGE TO SPAM",
                        color = DemonicTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    DemonicTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = "Type message to blast...",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Quick Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QUICK_PRESET_MESSAGES.forEach { preset ->
                            val isSelected = messageText == preset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) Color(0xFFFF5722).copy(alpha = 0.25f)
                                        else DemonicSurfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFFFF5722) else DemonicBorder,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        messageText = preset
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = preset,
                                    color = if (isSelected) Color(0xFFFF8A65) else DemonicTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Number of Messages (Max 100)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NUMBER OF MESSAGES",
                            color = DemonicTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )

                        // Highlight Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF5722).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFFF5722).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$count / 100 MAX",
                                color = Color(0xFFFF8A65),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Count Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        COUNT_PRESETS.forEach { presetVal ->
                            val isSelected = count == presetVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) Color(0xFFFF5722)
                                        else DemonicSurfaceVariant
                                    )
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        count = presetVal
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$presetVal",
                                    color = if (isSelected) Color.White else DemonicTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fine-tuning Stepper + Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (count > 1) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    count = (count - 1).coerceAtLeast(1)
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DemonicSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = DemonicTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Slider(
                            value = count.toFloat(),
                            onValueChange = {
                                count = it.roundToInt().coerceIn(1, 100)
                            },
                            valueRange = 1f..100f,
                            steps = 98,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF5722),
                                activeTrackColor = Color(0xFFFF5722),
                                inactiveTrackColor = DemonicSurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = {
                                if (count < 100) {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    count = (count + 1).coerceAtMost(100)
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DemonicSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = DemonicTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Warning if timed out
                    if (isTimedOut) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "⚠️ You are timed out by the host. Spamming is disabled.",
                            color = DemonicErrorRed,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Start Blast Button
                    val canBlast = messageText.isNotBlank() && !isTimedOut
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (canBlast) {
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF5722), Color(0xFFFF1744))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(DemonicSurfaceVariant, DemonicSurfaceVariant)
                                    )
                                }
                            )
                            .clickable(enabled = canBlast) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onStartBlast(messageText.trim(), count)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = if (canBlast) Color.White else DemonicTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "START RAPID BLAST ($count MSGS) ⚡",
                                color = if (canBlast) Color.White else DemonicTextMuted,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
