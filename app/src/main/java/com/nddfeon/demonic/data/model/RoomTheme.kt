package com.nddfeon.demonic.data.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.database.IgnoreExtraProperties

enum class RoomThemePreset(
    val id: String,
    val displayName: String,
    val emoji: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceVariantColor: Color,
    val borderColor: Color,
    val chatOwnGradient: List<Color>,
    val buttonGradient: List<Color>
) {
    CYBER_NEON(
        id = "CYBER_NEON",
        displayName = "Cyber Neon",
        emoji = "🟣",
        primaryColor = Color(0xFFB026FF),
        secondaryColor = Color(0xFF00F5FF),
        glowColor = Color(0x59B026FF),
        backgroundColor = Color(0xFF0B0814),
        surfaceColor = Color(0xFF141022),
        surfaceVariantColor = Color(0xFF1F1833),
        borderColor = Color(0xFF382B59),
        chatOwnGradient = listOf(Color(0xFF9C27B0), Color(0xFF673AB7)),
        buttonGradient = listOf(Color(0xFFB026FF), Color(0xFF00F5FF))
    ),
    MATRIX_HACK(
        id = "MATRIX_HACK",
        displayName = "Matrix Hack",
        emoji = "🟢",
        primaryColor = Color(0xFF00FF66),
        secondaryColor = Color(0xFF39FF14),
        glowColor = Color(0x5900FF66),
        backgroundColor = Color(0xFF030B05),
        surfaceColor = Color(0xFF07190B),
        surfaceVariantColor = Color(0xFF0E2C14),
        borderColor = Color(0xFF164721),
        chatOwnGradient = listOf(Color(0xFF00C853), Color(0xFF007E33)),
        buttonGradient = listOf(Color(0xFF00FF66), Color(0xFF009624))
    ),
    SUNSET_LOFI(
        id = "SUNSET_LOFI",
        displayName = "Sunset Lo-Fi",
        emoji = "🟠",
        primaryColor = Color(0xFFFF9E00),
        secondaryColor = Color(0xFFFF4D6D),
        glowColor = Color(0x59FF9E00),
        backgroundColor = Color(0xFF140A0F),
        surfaceColor = Color(0xFF1F1017),
        surfaceVariantColor = Color(0xFF2E1723),
        borderColor = Color(0xFF4C2539),
        chatOwnGradient = listOf(Color(0xFFFF9E00), Color(0xFFE65100)),
        buttonGradient = listOf(Color(0xFFFF9E00), Color(0xFFFF4D6D))
    ),
    BLOOD_CRIMSON(
        id = "BLOOD_CRIMSON",
        displayName = "Blood Crimson",
        emoji = "🔴",
        primaryColor = Color(0xFFFF2A54),
        secondaryColor = Color(0xFFFF597B),
        glowColor = Color(0x59FF2A54),
        backgroundColor = Color(0xFF0A090D),
        surfaceColor = Color(0xFF14111A),
        surfaceVariantColor = Color(0xFF1E1927),
        borderColor = Color(0xFF332A44),
        chatOwnGradient = listOf(Color(0xFFFF2A54), Color(0xFFC70039)),
        buttonGradient = listOf(Color(0xFFFF2A54), Color(0xFFFF597B))
    );

    companion object {
        fun fromId(id: String?): RoomThemePreset {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: CYBER_NEON
        }
    }
}

enum class VisualizerStylePreset(
    val id: String,
    val displayName: String,
    val emoji: String,
    val description: String
) {
    CIRCULAR(
        id = "CIRCULAR",
        displayName = "Circular Spectrum",
        emoji = "🔄",
        description = "Pulsing radial rings orbiting the center stage"
    ),
    NEON_BARS(
        id = "NEON_BARS",
        displayName = "Cyber Neon Bars",
        emoji = "📊",
        description = "Multi-band high dynamic frequency spectrum"
    ),
    OSCILLOSCOPE(
        id = "OSCILLOSCOPE",
        displayName = "Waveform Oscilloscope",
        emoji = "〰️",
        description = "Smooth cybernetic sine waves and fluid glow"
    );

    companion object {
        fun fromId(id: String?): VisualizerStylePreset {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: CIRCULAR
        }
    }
}

enum class SpecialEffectType(
    val id: String,
    val title: String,
    val emoji: String,
    val defaultText: String,
    val badgeLabel: String
) {
    LOVE_EXPLOSION(
        id = "LOVE_EXPLOSION",
        title = "Romantic Love Blast",
        emoji = "💖",
        defaultText = "I LOVE YOU ❤️",
        badgeLabel = "ROMANCE FX"
    ),
    PARTY_FLAMES(
        id = "PARTY_FLAMES",
        title = "Drop The Bass",
        emoji = "🔥",
        defaultText = "DROP THE BASS! 🔥",
        badgeLabel = "HYPE BLAST"
    ),
    CROWN_VIP(
        id = "CROWN_VIP",
        title = "VIP Royal Flex",
        emoji = "👑",
        defaultText = "ROYAL VIP VIBES 👑",
        badgeLabel = "VIP FLEX"
    ),
    MATRIX_RAIN(
        id = "MATRIX_RAIN",
        title = "Matrix Cyber Overload",
        emoji = "⚡",
        defaultText = "SYSTEM OVERRIDE // 0101",
        badgeLabel = "CYBER HACK"
    ),
    DEMONIC_SURGE(
        id = "DEMONIC_SURGE",
        title = "Demonic Skull Rave",
        emoji = "💀",
        defaultText = "DEMONIC ENERGY UNLEASHED 💀",
        badgeLabel = "DEMONIC RAVE"
    );

    companion object {
        fun fromId(id: String?): SpecialEffectType {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: LOVE_EXPLOSION
        }
    }
}

@IgnoreExtraProperties
data class RoomSpecialEffect(
    val id: String = "",
    val type: String = SpecialEffectType.LOVE_EXPLOSION.id,
    val senderName: String = "",
    val targetName: String = "",
    val customMessage: String = "",
    val timestamp: Long = 0L
)
