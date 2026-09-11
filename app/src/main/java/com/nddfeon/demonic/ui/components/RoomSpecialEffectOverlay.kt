package com.nddfeon.demonic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nddfeon.demonic.data.model.RoomSpecialEffect
import com.nddfeon.demonic.data.model.SpecialEffectType
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun RoomSpecialEffectOverlay(
    effect: RoomSpecialEffect?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = effect != null,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400)),
        modifier = modifier.fillMaxSize()
    ) {
        if (effect == null) return@AnimatedVisibility

        val effectType = remember(effect.type) {
            SpecialEffectType.fromId(effect.type) ?: SpecialEffectType.LOVE_EXPLOSION
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            when (effectType) {
                SpecialEffectType.LOVE_EXPLOSION -> LoveExplosionEffect(effect = effect, onDismiss = onDismiss)
                SpecialEffectType.PARTY_FLAMES -> PartyFlamesEffect(effect = effect, onDismiss = onDismiss)
                SpecialEffectType.CROWN_VIP -> CrownVipEffect(effect = effect, onDismiss = onDismiss)
                SpecialEffectType.MATRIX_RAIN -> MatrixRainEffect(effect = effect, onDismiss = onDismiss)
                SpecialEffectType.DEMONIC_SURGE -> DemonicSurgeEffect(effect = effect, onDismiss = onDismiss)
            }
        }
    }
}

// -------------------------------------------------------------
// 💖 1. ROMANTIC LOVE EXPLOSION ("I LOVE YOU ❤️")
// -------------------------------------------------------------
@Composable
private fun LoveExplosionEffect(
    effect: RoomSpecialEffect,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "love_aura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Romantic Soft Vignette Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x33FF1493),
                            Color(0x66FF0055)
                        )
                    )
                )
        )

        // Cascading Floating Hearts (24 particles)
        val hearts = remember(effect.id) {
            val emojis = listOf("❤️", "💖", "💘", "🌹", "💕", "✨", "💍", "🥰", "💞", "💓")
            (0..23).map { i ->
                FloatingParticle(
                    id = i,
                    emoji = emojis[i % emojis.size],
                    startX = Random.nextFloat(),
                    speedSec = Random.nextInt(2800, 5000),
                    delayMs = Random.nextInt(0, 1800),
                    sizeSp = Random.nextInt(22, 44).sp,
                    swayAmplitude = Random.nextFloat() * 35f + 15f
                )
            }
        }

        hearts.forEach { particle ->
            AnimatedFloatingHeart(
                particle = particle,
                containerWidth = widthPx,
                containerHeight = heightPx
            )
        }

        // Center Glassmorphic Romantic Banner
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xE62A0A1C),
                                Color(0xF218040F)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFFF1493), Color(0xFFFF69B4), Color(0xFFFF0055))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Top Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x4DFF1493))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "💖 SPECIAL ROMANTIC BLAST 💖",
                        color = Color(0xFFFFB6C1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // The Romantic Message ("I LOVE YOU ❤️")
                Text(
                    text = effect.customMessage.ifBlank { "I LOVE YOU ❤️" },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sender & Target announcement
                val senderText = if (effect.targetName.isNotBlank()) {
                    "From ${effect.senderName} 💌 To ${effect.targetName}"
                } else {
                    "Dedicated with love by ${effect.senderName} ❤️"
                }

                Text(
                    text = senderText,
                    color = Color(0xFFFF94C2),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tap anywhere to close",
                    color = Color(0x99FFB6C1),
                    fontSize = 10.5.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 🔥 2. DROP THE BASS / PARTY FLAMES
// -------------------------------------------------------------
@Composable
private fun PartyFlamesEffect(
    effect: RoomSpecialEffect,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flames_pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Strobe Fire Border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 4.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFF3300), Color(0xFFFF9900), Color(0xFFFF0055))
                    ),
                    shape = RoundedCornerShape(0.dp)
                )
                .alpha(borderAlpha)
        )

        // Rising Fire Sparks
        val fireSparks = remember(effect.id) {
            val emojis = listOf("🔥", "💥", "⚡", "🚀", "✨", "🔊")
            (0..20).map { i ->
                FloatingParticle(
                    id = i,
                    emoji = emojis[i % emojis.size],
                    startX = Random.nextFloat(),
                    speedSec = Random.nextInt(1800, 3200),
                    delayMs = Random.nextInt(0, 1000),
                    sizeSp = Random.nextInt(26, 42).sp,
                    swayAmplitude = 20f
                )
            }
        }

        fireSparks.forEach { particle ->
            AnimatedFloatingHeart(
                particle = particle,
                containerWidth = widthPx,
                containerHeight = heightPx
            )
        }

        // Center Hype Banner
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xF21A0800))
                    .border(2.dp, Color(0xFFFF5500), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "🔥 BASS DROP BLAST 🔥",
                    color = Color(0xFFFFB703),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = effect.customMessage.ifBlank { "DROP THE BASS! 🔥" },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Fired by DJ ${effect.senderName} ⚡",
                    color = Color(0xFFFF9E00),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 👑 3. VIP ROYAL FLEX
// -------------------------------------------------------------
@Composable
private fun CrownVipEffect(
    effect: RoomSpecialEffect,
    onDismiss: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val crowns = remember(effect.id) {
            val emojis = listOf("👑", "💎", "✨", "🍾", "🏆", "🌟")
            (0..18).map { i ->
                FloatingParticle(
                    id = i,
                    emoji = emojis[i % emojis.size],
                    startX = Random.nextFloat(),
                    speedSec = Random.nextInt(2600, 4500),
                    delayMs = Random.nextInt(0, 1500),
                    sizeSp = Random.nextInt(26, 44).sp,
                    swayAmplitude = 25f
                )
            }
        }

        crowns.forEach { particle ->
            AnimatedFloatingHeart(
                particle = particle,
                containerWidth = widthPx,
                containerHeight = heightPx
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xF2181504))
                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "👑 VIP ROYAL FLEX 👑",
                    color = Color(0xFFFFE066),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = effect.customMessage.ifBlank { "ROYAL VIP VIBES 👑" },
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "VIP Crown Holder: ${effect.senderName} ✨",
                    color = Color(0xFFFFD700),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// -------------------------------------------------------------
// ⚡ 4. MATRIX CYBER OVERLOAD
// -------------------------------------------------------------
@Composable
private fun MatrixRainEffect(
    effect: RoomSpecialEffect,
    onDismiss: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val matrixGlyphs = remember(effect.id) {
            val glyphs = listOf("0", "1", "λ", "☠", "⚡", "Ω", "π", "▲", "⌘", "01")
            (0..22).map { i ->
                FloatingParticle(
                    id = i,
                    emoji = glyphs[i % glyphs.size],
                    startX = Random.nextFloat(),
                    speedSec = Random.nextInt(1800, 3600),
                    delayMs = Random.nextInt(0, 1200),
                    sizeSp = Random.nextInt(20, 34).sp,
                    swayAmplitude = 10f
                )
            }
        }

        matrixGlyphs.forEach { particle ->
            AnimatedFloatingHeart(
                particle = particle,
                containerWidth = widthPx,
                containerHeight = heightPx
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xF2031106))
                    .border(1.5.dp, Color(0xFF00FF66), RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "⚡ MATRIX CYBER HACK ⚡",
                    color = Color(0xFF39FF14),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = effect.customMessage.ifBlank { "SYSTEM OVERRIDE // 0101" },
                    color = Color(0xFF00FF66),
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Initiated by root@${effect.senderName}",
                    color = Color(0xFF99FFB3),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 💀 5. DEMONIC RAVE SURGE
// -------------------------------------------------------------
@Composable
private fun DemonicSurgeEffect(
    effect: RoomSpecialEffect,
    onDismiss: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val skulls = remember(effect.id) {
            val emojis = listOf("💀", "🩸", "⚡", "🔮", "🖤", "😈", "☠️")
            (0..20).map { i ->
                FloatingParticle(
                    id = i,
                    emoji = emojis[i % emojis.size],
                    startX = Random.nextFloat(),
                    speedSec = Random.nextInt(2200, 4000),
                    delayMs = Random.nextInt(0, 1400),
                    sizeSp = Random.nextInt(24, 42).sp,
                    swayAmplitude = 25f
                )
            }
        }

        skulls.forEach { particle ->
            AnimatedFloatingHeart(
                particle = particle,
                containerWidth = widthPx,
                containerHeight = heightPx
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xF218050D))
                    .border(2.dp, Color(0xFFFF0055), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "💀 DEMONIC RAVE SURGE 💀",
                    color = Color(0xFFFF3366),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = effect.customMessage.ifBlank { "DEMONIC ENERGY UNLEASHED 💀" },
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Demon Master: ${effect.senderName} 🔮",
                    color = Color(0xFFFF597B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// -------------------------------------------------------------
// ANIMATED FLOATING PARTICLE COMPOSABLE
// -------------------------------------------------------------
private data class FloatingParticle(
    val id: Int,
    val emoji: String,
    val startX: Float,
    val speedSec: Int,
    val delayMs: Int,
    val sizeSp: androidx.compose.ui.unit.TextUnit,
    val swayAmplitude: Float
)

@Composable
private fun AnimatedFloatingHeart(
    particle: FloatingParticle,
    containerWidth: Float,
    containerHeight: Float
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(particle.id) {
        delay(particle.delayMs.toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = particle.speedSec,
                easing = LinearEasing
            )
        )
    }

    val currentProgress = progress.value
    if (currentProgress in 0.01f..0.99f) {
        val yPos = containerHeight * (1f - currentProgress)
        val sway = sin(currentProgress * 4f * Math.PI.toFloat()) * particle.swayAmplitude
        val xPos = (containerWidth * particle.startX) + sway

        val alpha = if (currentProgress < 0.2f) {
            currentProgress / 0.2f
        } else if (currentProgress > 0.8f) {
            (1f - currentProgress) / 0.2f
        } else {
            1f
        }

        Text(
            text = particle.emoji,
            fontSize = particle.sizeSp,
            modifier = Modifier
                .offset { IntOffset(xPos.roundToInt(), yPos.roundToInt()) }
                .alpha(alpha)
        )
    }
}
