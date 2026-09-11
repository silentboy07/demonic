package com.nddfeon.demonic.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nddfeon.demonic.data.model.RoomThemePreset
import com.nddfeon.demonic.data.model.SpecialEffectType
import com.nddfeon.demonic.data.model.VisualizerStylePreset
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomFxAndThemesBottomSheet(
    isHost: Boolean,
    currentTheme: RoomThemePreset,
    currentVisualizer: VisualizerStylePreset,
    onSelectTheme: (RoomThemePreset) -> Unit,
    onSelectVisualizer: (VisualizerStylePreset) -> Unit,
    onTriggerSpecialEffect: (SpecialEffectType, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val view = LocalView.current
    var selectedTab by remember { mutableIntStateOf(if (isHost) 2 else 0) } // Default to FX tab if host

    // Form inputs for Romantic Love Blast
    var targetLoverName by remember { mutableStateOf("") }
    var customLoveMessage by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DemonicSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(currentTheme.primaryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ROOM FX & THEMES",
                            color = DemonicTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isHost) "👑 Host Studio • Controls for entire room" else "Previewing room aesthetic",
                            color = currentTheme.primaryColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DemonicTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DemonicSurfaceVariant,
                contentColor = currentTheme.primaryColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = currentTheme.primaryColor,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        selectedTab = 0
                    },
                    text = {
                        Text(
                            text = "🎨 Themes",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        selectedTab = 1
                    },
                    text = {
                        Text(
                            text = "📊 Visualizer",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        selectedTab = 2
                    },
                    text = {
                        Text(
                            text = "💖 Special FX",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Scroll
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // THEMES TAB
                        Text(
                            text = "SELECT ROOM AESTHETIC",
                            color = DemonicTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        RoomThemePreset.entries.forEach { theme ->
                            val isSelected = (theme == currentTheme)
                            ThemeCard(
                                theme = theme,
                                isSelected = isSelected,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onSelectTheme(theme)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    1 -> {
                        // VISUALIZER STYLES TAB
                        Text(
                            text = "AUDIO VISUALIZER ENGINE",
                            color = DemonicTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        VisualizerStylePreset.entries.forEach { style ->
                            val isSelected = (style == currentVisualizer)
                            VisualizerCard(
                                style = style,
                                isSelected = isSelected,
                                primaryColor = currentTheme.primaryColor,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    onSelectVisualizer(style)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    2 -> {
                        // OWNER SPECIAL FX TAB (ROMANCE & HYPE)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF2E0919), Color(0xFF190610))
                                    )
                                )
                                .border(1.dp, Color(0x66FF1493), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "💖", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ROMANTIC LOVE BLAST",
                                        color = Color(0xFFFF69B4),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Send a synchronized floating hearts explosion and glowing love message to impress someone or celebrate your love!",
                                    color = Color(0xFFFFB6C1),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = targetLoverName,
                                    onValueChange = { targetLoverName = it },
                                    label = { Text("Partner / Lover's Name (Optional)", color = DemonicTextMuted, fontSize = 12.sp) },
                                    placeholder = { Text("e.g. Pooja ❤️", color = DemonicTextMuted.copy(alpha = 0.6f), fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF1493),
                                        unfocusedBorderColor = Color(0x66FF1493),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = customLoveMessage,
                                    onValueChange = { customLoveMessage = it },
                                    label = { Text("Custom Love Note (Optional)", color = DemonicTextMuted, fontSize = 12.sp) },
                                    placeholder = { Text("default: I LOVE YOU ❤️", color = DemonicTextMuted.copy(alpha = 0.6f), fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF1493),
                                        unfocusedBorderColor = Color(0x66FF1493),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                DemonicButton(
                                    text = "💖 Launch Love Blast (Sabke Screen Pe)",
                                    variant = DemonicButtonVariant.PRIMARY,
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        val msg = customLoveMessage.ifBlank { "I LOVE YOU ❤️" }
                                        onTriggerSpecialEffect(SpecialEffectType.LOVE_EXPLOSION, targetLoverName, msg)
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "MORE OWNER PARTY FX",
                            color = DemonicTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Party Flames
                        SpecialFxCard(
                            emoji = "🔥",
                            title = "Drop The Bass Fire Blast",
                            description = "Strobe fire flame borders, rising sparks & bass hype banner",
                            color = Color(0xFFFF5500),
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onTriggerSpecialEffect(SpecialEffectType.PARTY_FLAMES, "", "DROP THE BASS! 🔥")
                                onDismiss()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // VIP Crown Flex
                        SpecialFxCard(
                            emoji = "👑",
                            title = "VIP Royal Flex",
                            description = "Cascading golden crowns, luxury sparkles & golden aura",
                            color = Color(0xFFFFD700),
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onTriggerSpecialEffect(SpecialEffectType.CROWN_VIP, "", "ROYAL VIP VIBES 👑")
                                onDismiss()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Matrix Hack
                        SpecialFxCard(
                            emoji = "⚡",
                            title = "Matrix Cyber Overload",
                            description = "Phosphor green digital code rain streaming down chat",
                            color = Color(0xFF00FF66),
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onTriggerSpecialEffect(SpecialEffectType.MATRIX_RAIN, "", "SYSTEM OVERRIDE // 0101")
                                onDismiss()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Demonic Rave
                        SpecialFxCard(
                            emoji = "💀",
                            title = "Demonic Skull Surge",
                            description = "Neon skull explosion, purple lightning & demonic energy",
                            color = Color(0xFFFF0055),
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                onTriggerSpecialEffect(SpecialEffectType.DEMONIC_SURGE, "", "DEMONIC ENERGY UNLEASHED 💀")
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: RoomThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) theme.surfaceVariantColor else DemonicSurfaceVariant)
            .border(
                1.5.dp,
                if (isSelected) theme.primaryColor else DemonicBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color gradient swatch
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(theme.primaryColor, theme.secondaryColor)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = theme.emoji, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = theme.displayName,
                color = if (isSelected) theme.primaryColor else DemonicTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (theme) {
                    RoomThemePreset.CYBER_NEON -> "Electric Purple + Cyber Cyan"
                    RoomThemePreset.MATRIX_HACK -> "Terminal Green + Cyber Lime"
                    RoomThemePreset.SUNSET_LOFI -> "Warm Amber + Sunset Rose"
                    RoomThemePreset.BLOOD_CRIMSON -> "Demonic Blood Red + Fiery Ember"
                },
                color = DemonicTextMuted,
                fontSize = 11.sp
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.primaryColor.copy(alpha = 0.2f))
                    .border(1.dp, theme.primaryColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = theme.primaryColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ACTIVE",
                        color = theme.primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualizerCard(
    style: VisualizerStylePreset,
    isSelected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DemonicSurfaceVariant)
            .border(
                1.5.dp,
                if (isSelected) primaryColor else DemonicBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = style.emoji, fontSize = 22.sp)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = style.displayName,
                color = if (isSelected) primaryColor else DemonicTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = style.description,
                color = DemonicTextMuted,
                fontSize = 11.sp
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(primaryColor.copy(alpha = 0.2f))
                    .border(1.dp, primaryColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ACTIVE",
                    color = primaryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun SpecialFxCard(
    emoji: String,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DemonicSurfaceVariant)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 22.sp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = DemonicTextMuted,
                fontSize = 10.5.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color, RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "FIRE 🚀",
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
