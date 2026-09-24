package com.nddfeon.demonic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.nddfeon.demonic.data.model.VisualizerStylePreset
import kotlin.math.cos
import kotlin.math.sin

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nddfeon.demonic.data.model.RoomThemePreset
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicCrimsonDark
import com.nddfeon.demonic.ui.theme.DemonicCrimsonLight
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicViolet

@Composable
fun LiveEqualizerBars(
    isPlaying: Boolean,
    primaryColor: Color = DemonicCrimson,
    secondaryColor: Color = DemonicCrimsonLight,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "equalizer")

    val bar1 by transition.animateFloat(
        initialValue = 0.22f,
        targetValue = if (isPlaying) 0.92f else 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(340, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val bar2 by transition.animateFloat(
        initialValue = 0.75f,
        targetValue = if (isPlaying) 0.25f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3 by transition.animateFloat(
        initialValue = 0.33f,
        targetValue = if (isPlaying) 1.0f else 0.30f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val bar4 by transition.animateFloat(
        initialValue = 0.62f,
        targetValue = if (isPlaying) 0.30f else 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(390, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    Canvas(
        modifier = modifier.size(width = 24.dp, height = 24.dp)
    ) {
        val barWidth = 3.5.dp.toPx()
        val spacing = 3.dp.toPx()
        val totalWidth = 4 * barWidth + 3 * spacing
        val startX = (size.width - totalWidth) / 2f
        val maxHeight = size.height
        val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())

        val brush = Brush.verticalGradient(
            colors = listOf(secondaryColor, primaryColor),
            startY = 0f,
            endY = maxHeight
        )

        val fractions = floatArrayOf(bar1, bar2, bar3, bar4)
        for (i in 0 until 4) {
            val barHeight = maxHeight * fractions[i].coerceIn(0.18f, 1f)
            val left = startX + i * (barWidth + spacing)
            val top = maxHeight - barHeight

            drawRoundRect(
                brush = brush,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}

@Composable
fun NowPlayingTrackBanner(
    title: String,
    isPlaying: Boolean,
    nextTrackTitle: String?,
    hasPreviousTrack: Boolean = false,
    theme: RoomThemePreset = RoomThemePreset.CYBER_NEON,
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val swipeThresholdPx = with(LocalDensity.current) { 46.dp.toPx() }
    val maxDragPx = with(LocalDensity.current) { 110.dp.toPx() }
    var currentOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(theme.surfaceColor.copy(alpha = 0.5f))
            .border(1.dp, theme.borderColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
    ) {
        // Background Actions Revealed on Swipe
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Side: Previous Track Hint (Shown when swiping right)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(if (currentOffset > 8f) ((currentOffset / swipeThresholdPx).coerceIn(0.2f, 1f)) else 0f)
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = null,
                    tint = if (hasPreviousTrack) theme.secondaryColor else DemonicTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (hasPreviousTrack) "PREV TRACK" else "NO HISTORY",
                    color = if (hasPreviousTrack) theme.secondaryColor else DemonicTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right Side: Next Track Hint (Shown when swiping left)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(if (currentOffset < -8f) (((-currentOffset) / swipeThresholdPx).coerceIn(0.2f, 1f)) else 0f)
            ) {
                Text(
                    text = if (!nextTrackTitle.isNullOrBlank()) "NEXT TRACK" else "QUEUE END",
                    color = if (!nextTrackTitle.isNullOrBlank()) theme.primaryColor else DemonicTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    tint = if (!nextTrackTitle.isNullOrBlank()) theme.primaryColor else DemonicTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Swipable Track Banner Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            currentOffset = (currentOffset + delta).coerceIn(-maxDragPx, maxDragPx)
                            offsetX.snapTo(currentOffset)
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            if (currentOffset < -swipeThresholdPx) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onSwipeLeft()
                            } else if (currentOffset > swipeThresholdPx) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onSwipeRight()
                            }
                            currentOffset = 0f
                            offsetX.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                        }
                    }
                )
                .clickable(onClick = onClick)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(theme.surfaceVariantColor, theme.surfaceColor)
                    )
                )
                .border(1.dp, theme.borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) theme.primaryColor.copy(alpha = 0.22f) else theme.surfaceColor
                    )
                    .border(1.dp, if (isPlaying) theme.primaryColor.copy(alpha = 0.5f) else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.MusicNote else Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = if (isPlaying) theme.primaryColor else DemonicTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (title.isNotEmpty()) title else "Demonic Live Stream",
                    color = DemonicTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (!nextTrackTitle.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = null,
                            tint = theme.secondaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Next: " + nextTrackTitle,
                            color = theme.secondaryColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "‹ Slide ›",
                            color = theme.primaryColor.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isPlaying) "Playing in Sync • 48kHz" else "Paused",
                            color = DemonicTextMuted,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "‹ Slide to Skip ›",
                            color = theme.primaryColor.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            LiveEqualizerBars(
                isPlaying = isPlaying,
                primaryColor = theme.primaryColor,
                secondaryColor = theme.secondaryColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerDialog(
    currentMinutes: Int?,
    onSelectMinutes: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val options = listOf(
        Pair("Turn Off Timer", null),
        Pair("15 Minutes", 15),
        Pair("30 Minutes", 30),
        Pair("45 Minutes", 45),
        Pair("60 Minutes", 60)
    )

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DemonicSurface)
                .border(1.dp, DemonicBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = DemonicViolet,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SLEEP TIMER",
                        color = DemonicTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Playback will automatically pause after the timer expires.",
                    color = DemonicTextMuted,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                options.forEach { (label, minutes) ->
                    val isSelected = (currentMinutes == minutes)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) DemonicCrimsonDark.copy(alpha = 0.35f) else DemonicSurfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isSelected) DemonicCrimson else DemonicBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                onSelectMinutes(minutes)
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) DemonicCrimson else DemonicTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )

                            if (isSelected) {
                                Text(
                                    text = "ACTIVE",
                                    color = DemonicCrimson,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                DemonicButton(
                    text = "Close",
                    variant = DemonicButtonVariant.OUTLINE,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


// ---------------------------------------------------------------------------------
// 1. 🔄 PULSING CIRCULAR SPECTRUM (Radial orbiting audio bars)
// ---------------------------------------------------------------------------------
@Composable
fun CircularSpectrumVisualizer(
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit = {}
) {
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        var lastTime = 0L
        while (isPlaying) {
            withFrameNanos { time ->
                if (lastTime != 0L) {
                    val dt = (time - lastTime) / 1_000_000_000f
                    phase = (phase + dt * 2.5f) % (2f * Math.PI.toFloat())
                }
                lastTime = time
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.72f
            val barCount = 48
            val angleStep = (2f * Math.PI.toFloat()) / barCount

            for (i in 0 until barCount) {
                val angle = i * angleStep
                val wave1 = sin(angle * 3f + phase)
                val wave2 = cos(angle * 5f - phase * 1.5f)
                val waveFactor = if (isPlaying) (wave1 + wave2 + 2f) / 4f else 0.08f

                val barLen = if (isPlaying) 10.dp.toPx() + waveFactor * 26.dp.toPx() else 4.dp.toPx()

                val startX = center.x + baseRadius * cos(angle)
                val startY = center.y + baseRadius * sin(angle)
                val endX = center.x + (baseRadius + barLen) * cos(angle)
                val endY = center.y + (baseRadius + barLen) * sin(angle)

                val barColor = if (i % 2 == 0) primaryColor else secondaryColor

                drawLine(
                    color = barColor.copy(alpha = if (isPlaying) 0.85f else 0.3f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Outer subtle glow ring
            drawCircle(
                color = primaryColor.copy(alpha = if (isPlaying) 0.25f else 0.1f),
                radius = baseRadius - 4.dp.toPx(),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        centerContent()
    }
}

// ---------------------------------------------------------------------------------
// 2. 📊 CYBER NEON BARS (High-dynamic multi-band equalizer)
// ---------------------------------------------------------------------------------
@Composable
fun CyberNeonBarsVisualizer(
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        var lastTime = 0L
        while (isPlaying) {
            withFrameNanos { time ->
                if (lastTime != 0L) {
                    val dt = (time - lastTime) / 1_000_000_000f
                    phase = (phase + dt * 4f) % 1000f
                }
                lastTime = time
            }
        }
    }

    Canvas(modifier = modifier.fillMaxWidth().height(160.dp)) {
        val barCount = 28
        val spacing = 4.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(3.dp.toPx())
        val maxHeight = size.height * 0.85f

        for (i in 0 until barCount) {
            val offset = i * 0.35f
            val sinVal = sin(phase + offset)
            val cosVal = cos(phase * 0.7f + offset * 1.2f)
            val factor = if (isPlaying) ((sinVal + cosVal + 2f) / 4f).coerceIn(0.12f, 1f) else 0.08f

            val barHeight = (maxHeight * factor).coerceAtLeast(6.dp.toPx())
            val x = i * (barWidth + spacing)
            val y = size.height - barHeight

            // Bar Gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, secondaryColor),
                    startY = y,
                    endY = size.height
                ),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Top Peak Cap
            if (isPlaying && factor > 0.3f) {
                val capY = (y - 5.dp.toPx()).coerceAtLeast(0f)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset(x, capY),
                    size = androidx.compose.ui.geometry.Size(barWidth, 2.5.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 3. 〰️ WAVEFORM OSCILLOSCOPE (Smooth fluid sine wave)
// ---------------------------------------------------------------------------------
@Composable
fun WaveformOscilloscopeVisualizer(
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        var lastTime = 0L
        while (isPlaying) {
            withFrameNanos { time ->
                if (lastTime != 0L) {
                    val dt = (time - lastTime) / 1_000_000_000f
                    phase = (phase + dt * 3.5f) % (2f * Math.PI.toFloat())
                }
                lastTime = time
            }
        }
    }

    Canvas(modifier = modifier.fillMaxWidth().height(160.dp)) {
        val midY = size.height / 2f
        val waveAmplitude = if (isPlaying) size.height * 0.35f else size.height * 0.05f
        val points = 60
        val dx = size.width / points

        val pathPrimary = Path()
        val pathSecondary = Path()

        for (i in 0..points) {
            val x = i * dx
            val normalizedX = (i.toFloat() / points) * 2f * Math.PI.toFloat()
            val y1 = midY + sin(normalizedX * 2.5f + phase) * waveAmplitude
            val y2 = midY + cos(normalizedX * 3.2f - phase * 1.2f) * (waveAmplitude * 0.75f)

            if (i == 0) {
                pathPrimary.moveTo(x, y1)
                pathSecondary.moveTo(x, y2)
            } else {
                pathPrimary.lineTo(x, y1)
                pathSecondary.lineTo(x, y2)
            }
        }

        // Draw primary wave
        drawPath(
            path = pathPrimary,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw secondary wave
        drawPath(
            path = pathSecondary,
            color = secondaryColor.copy(alpha = 0.75f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Center line
        drawLine(
            color = primaryColor.copy(alpha = 0.2f),
            start = Offset(0f, midY),
            end = Offset(size.width, midY),
            strokeWidth = 1.dp.toPx()
        )
    }
}
