package com.nddfeon.demonic.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nddfeon.demonic.player.YouTubeUrlParser
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicEmberGlow

@Composable
fun VinylDisc(
    videoId: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    // Keep continuous rotation angle across play/pause
    var currentAngle by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        var lastTime = 0L
        while (isPlaying) {
            withFrameNanos { time ->
                if (lastTime != 0L) {
                    val deltaSeconds = (time - lastTime) / 1_000_000_000f
                    // 33.3 RPM = approx 200 degrees per second
                    currentAngle = (currentAngle + deltaSeconds * 90f) % 360f
                }
                lastTime = time
            }
        }
    }

    val thumbnailUrl = remember(videoId) {
        if (videoId.isNotEmpty()) YouTubeUrlParser.getThumbnailUrl(videoId, "hqdefault") else null
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isPlaying) 20.dp else 8.dp,
                shape = CircleShape,
                spotColor = DemonicCrimson,
                ambientColor = Color.Black
            )
            .rotate(currentAngle),
        contentAlignment = Alignment.Center
    ) {
        // Vinyl Body & Realistic Grooves Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = size.toPx() / 2f

            // Outer Vinyl Disc
            drawCircle(
                color = Color(0xFF110E17),
                radius = radius,
                center = center
            )

            // Outer Edge Rim Ring
            drawCircle(
                color = Color(0xFF2A2337),
                radius = radius - 2.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // High-detail concentric sound grooves
            val grooveStart = radius * 0.46f
            val grooveEnd = radius * 0.94f
            val grooveCount = 18

            for (i in 0..grooveCount) {
                val grooveRadius = grooveStart + (grooveEnd - grooveStart) * (i.toFloat() / grooveCount)
                val alpha = if (i % 3 == 0) 0.18f else 0.08f
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = grooveRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Dual Vinyl Light Sheen (Reflection cones across 45 degrees)
            val sheenBrush = Brush.sweepGradient(
                listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.06f),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = center
            )
            drawCircle(
                brush = sheenBrush,
                radius = radius,
                center = center
            )
        }

        // Center Album Art / Thumbnail Label
        val centerSize = size * 0.44f
        Box(
            modifier = Modifier
                .size(centerSize)
                .clip(CircleShape)
                .background(Color(0xFF1E1928))
                .border(2.dp, DemonicCrimson.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!thumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Album Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback DEMONIC Emblem
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(DemonicCrimson, Color(0xFF19050C))
                            )
                        )
                )
            }

            // Center Spindle Hole
            Box(
                modifier = Modifier
                    .size(centerSize * 0.18f)
                    .clip(CircleShape)
                    .background(DemonicBackground)
                    .border(1.dp, Color(0xFF4A3B60), CircleShape)
            )
        }
    }
}
