package com.nddfeon.demonic.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nddfeon.demonic.data.model.UserAccount
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicErrorRed
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicSyncTeal
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicTextSecondary
import com.nddfeon.demonic.ui.theme.DemonicViolet

data class AvatarPreset(
    val emoji: String,
    val label: String,
    val backgroundColors: List<Color>
)

private val AVATAR_PRESETS = listOf(
    AvatarPreset("🎧", "DJ", listOf(Color(0xFF8B0000), Color(0xFF3B0054))),
    AvatarPreset("👑", "VIP", listOf(Color(0xFFFFB300), Color(0xFF6B4500))),
    AvatarPreset("🔥", "Fire", listOf(Color(0xFFFF3D00), Color(0xFF7F0000))),
    AvatarPreset("⚡", "Volt", listOf(Color(0xFF00E5FF), Color(0xFF00387A))),
    AvatarPreset("🎵", "Music", listOf(Color(0xFF00E676), Color(0xFF004D20))),
    AvatarPreset("🎸", "Rock", listOf(Color(0xFFE040FB), Color(0xFF4A148C))),
    AvatarPreset("😈", "Demon", listOf(Color(0xFFE50914), Color(0xFF1E0505))),
    AvatarPreset("👾", "Cyber", listOf(Color(0xFF7C4DFF), Color(0xFF1A0B2E))),
    AvatarPreset("🌟", "Star", listOf(Color(0xFFFFD700), Color(0xFFB8860B)))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileBottomSheet(
    currentUser: UserAccount?,
    recentRoomsCount: Int = 0,
    onSaveProfile: (String, String?) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var nameInput by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var selectedPresetEmoji by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DemonicBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DemonicCrimson.copy(alpha = 0.15f))
                            .border(1.dp, DemonicCrimson.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = DemonicCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "EDIT PROFILE",
                            color = DemonicTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Customize how others see you in rooms",
                            color = DemonicTextMuted,
                            fontSize = 11.5.sp
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
                        tint = DemonicTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Big Avatar Centerpiece
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF3A1C48), Color(0xFF140D20))
                        )
                    )
                    .border(2.5.dp, DemonicCrimson, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selectedPresetEmoji != null) {
                    Text(
                        text = selectedPresetEmoji!!,
                        fontSize = 42.sp
                    )
                } else if (!currentUser?.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentUser?.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(92.dp)
                    )
                } else {
                    val initial = nameInput.trim().firstOrNull()?.uppercase()
                        ?: currentUser?.displayName?.firstOrNull()?.uppercase()
                        ?: "D"
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Avatar Preset Selector
            Text(
                text = "CHOOSE AVATAR STYLE",
                color = DemonicTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Option to use default / photo
                item {
                    val isSelected = (selectedPresetEmoji == null)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) DemonicCrimson.copy(alpha = 0.25f)
                                else DemonicSurface
                            )
                            .border(
                                1.5.dp,
                                if (isSelected) DemonicCrimson else DemonicBorder,
                                CircleShape
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                selectedPresetEmoji = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!currentUser?.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(currentUser?.photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Original Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "👤",
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                // Presets
                items(AVATAR_PRESETS) { preset ->
                    val isSelected = (selectedPresetEmoji == preset.emoji)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(preset.backgroundColors))
                            .border(
                                if (isSelected) 2.5.dp else 1.dp,
                                if (isSelected) Color.White else DemonicBorder,
                                CircleShape
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                selectedPresetEmoji = preset.emoji
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.emoji,
                            fontSize = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Display Name Input Box
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISPLAY NAME",
                        color = DemonicCrimson,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${nameInput.length}/24",
                        color = DemonicTextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                DemonicTextField(
                    value = nameInput,
                    onValueChange = {
                        if (it.length <= 24) nameInput = it
                    },
                    placeholder = "Enter your display name",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "This name appears beside your messages in chat and room member lists.",
                    color = DemonicTextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Account Metadata Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DemonicSurface)
                    .border(1.dp, DemonicSurfaceVariant, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Account Type",
                            color = DemonicTextMuted,
                            fontSize = 12.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(DemonicSyncTeal)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (!currentUser?.email.isNullOrEmpty()) "Google Account" else "Guest Demon",
                                color = DemonicTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (!currentUser?.email.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Email",
                                color = DemonicTextMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                text = currentUser.email ?: "",
                                color = DemonicTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "User ID",
                            color = DemonicTextMuted,
                            fontSize = 12.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("User ID", currentUser?.uid ?: ""))
                                    Toast.makeText(context, "User ID copied! 📋", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = (currentUser?.uid ?: "").take(12) + "...",
                                color = DemonicTextSecondary,
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy UID",
                                tint = DemonicTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Rooms",
                            color = DemonicTextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$recentRoomsCount visited",
                            color = DemonicViolet,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Save Button
            DemonicButton(
                text = "Save Changes ✨",
                isLoading = isSaving,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val trimmed = nameInput.trim()
                    if (trimmed.isEmpty()) {
                        Toast.makeText(context, "Display name cannot be empty", Toast.LENGTH_SHORT).show()
                        return@DemonicButton
                    }
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    isSaving = true
                    // If a preset was selected, we can encode it or keep existing photoUrl
                    val photoUrlToSave = if (selectedPresetEmoji != null) {
                        // Store preset tag or null
                        "emoji:$selectedPresetEmoji"
                    } else {
                        currentUser?.photoUrl
                    }
                    onSaveProfile(trimmed, photoUrlToSave)
                    Toast.makeText(context, "Profile updated successfully! ✨", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sign Out / Switch Account Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        onDismiss()
                        onSignOut()
                    }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sign Out",
                    tint = DemonicErrorRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sign Out / Switch Account",
                    color = DemonicErrorRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
