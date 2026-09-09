package com.nddfeon.demonic.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nddfeon.demonic.data.model.LiveReaction
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import kotlin.random.Random

val AVAILABLE_REACTIONS = listOf("🔥", "😈", "🤘", "💖", "😭")

@Composable
fun FloatingReactionsOverlay(
    activeReactions: List<LiveReaction>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        activeReactions.takeLast(15).forEach { reaction ->
            FloatingEmojiParticle(
                key = reaction.id,
                emoji = reaction.emoji
            )
        }
    }
}

@Composable
private fun FloatingEmojiParticle(
    key: String,
    emoji: String
) {
    val randomX = remember(key) { Random.nextInt(-120, 120) }
    val startScale = remember(key) { Random.nextDouble(0.8, 1.4).toFloat() }

    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val scale = remember { Animatable(startScale) }

    LaunchedEffect(key) {
        offsetY.animateTo(
            targetValue = -500f,
            animationSpec = tween(durationMillis = 2200, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(key) {
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 2200, delayMillis = 400)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = emoji,
            fontSize = 32.sp,
            modifier = Modifier
                .offset { IntOffset(randomX, offsetY.value.toInt()) }
                .alpha(alpha.value)
                .scale(scale.value)
        )
    }
}

@Composable
fun ReactionButtonBar(
    onSendReaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(DemonicSurface.copy(alpha = 0.85f))
            .border(1.dp, DemonicSurfaceVariant, RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AVAILABLE_REACTIONS.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        onSendReaction(emoji)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )
            }
        }
    }
}
