package com.nddfeon.demonic.ui.home

import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.text.style.TextOverflow
import com.nddfeon.demonic.ui.components.UserProfileBottomSheet
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nddfeon.demonic.data.manager.OwnerConfigManager
import com.nddfeon.demonic.player.YouTubeUrlParser
import com.nddfeon.demonic.ui.components.DemonicBannerAd
import com.nddfeon.demonic.ui.components.DemonicButton
import com.nddfeon.demonic.ui.components.DemonicButtonVariant
import com.nddfeon.demonic.ui.components.DemonicTextField
import com.nddfeon.demonic.ui.components.OwnerControlBottomSheet
import com.nddfeon.demonic.ui.components.SmartClipboardBanner
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicCrimsonDark
import com.nddfeon.demonic.ui.theme.DemonicErrorRed
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicSyncTeal
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicTextSecondary
import com.nddfeon.demonic.ui.theme.DemonicViolet
import com.nddfeon.demonic.ui.theme.DemonicWarningAmber
import com.nddfeon.demonic.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    ownerConfigManager: OwnerConfigManager,
    onNavigateToRoom: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val recentRooms by viewModel.recentRoomsManager.recentRooms.collectAsState()

    var clipboardYoutubeVideoId by remember { mutableStateOf<String?>(null) }
    var dismissedClipboardVideoId by rememberSaveable { mutableStateOf("") }
    var isPublicRoom by rememberSaveable { mutableStateOf(true) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                try {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    if (clipboard?.hasPrimaryClip() == true) {
                        val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: ""
                        val extracted = YouTubeUrlParser.extractVideoId(clipText)
                        if (!extracted.isNullOrBlank() && extracted != dismissedClipboardVideoId) {
                            clipboardYoutubeVideoId = extracted
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isAdsEnabled by ownerConfigManager.isAdsEnabled.collectAsState()
    val globalAnnouncement by ownerConfigManager.globalAnnouncement.collectAsState()
    var showOwnerPanel by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DemonicBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Profile Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        showProfileSheet = true
                    }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                // User Avatar with edit pencil badge
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF221A30))
                            .border(2.dp, DemonicCrimson, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser?.photoUrl?.startsWith("emoji:") == true) {
                            Text(
                                text = currentUser!!.photoUrl!!.removePrefix("emoji:"),
                                fontSize = 24.sp
                            )
                        } else if (!currentUser?.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentUser?.photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "User Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            val initial = currentUser?.displayName?.firstOrNull()?.uppercase() ?: "D"
                            Text(
                                text = initial,
                                color = DemonicTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Little edit badge on bottom-right of avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(DemonicCrimson)
                            .border(1.5.dp, DemonicBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PROFILE",
                            color = DemonicCrimson,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "• TAP TO EDIT ✏️",
                            color = DemonicTextMuted,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = currentUser?.displayName ?: "Demon Listener",
                        color = DemonicTextPrimary,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Top action buttons: Owner Panel & Sign Out
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Owner Control Center Trigger (PIN Protected)
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        showOwnerPanel = true
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DemonicWarningAmber.copy(alpha = 0.12f))
                            .border(1.dp, DemonicWarningAmber.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👑",
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Sign out button
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.signOut()
                        onSignOut()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = DemonicTextMuted
                    )
                }
            }
        }

        // Global Owner Announcement (if broadcasted)
        if (!globalAnnouncement.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF2B1D0E), Color(0xFF1B1424))
                        )
                    )
                    .border(1.dp, DemonicWarningAmber.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📢", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GLOBAL ANNOUNCEMENT",
                            color = DemonicWarningAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = globalAnnouncement!!,
                            color = DemonicTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Smart Clipboard YouTube Link Detector
        AnimatedVisibility(
            visible = clipboardYoutubeVideoId != null,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it }
        ) {
            clipboardYoutubeVideoId?.let { vid ->
                SmartClipboardBanner(
                    videoId = vid,
                    canControlPlayback = true,
                    onPlayNow = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.createRoom(initialVideoId = vid, isPublic = isPublicRoom) { code ->
                            onNavigateToRoom(code)
                        }
                        dismissedClipboardVideoId = vid
                        clipboardYoutubeVideoId = null
                    },
                    onAddToQueue = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        viewModel.createRoom(initialVideoId = vid, isPublic = isPublicRoom) { code ->
                            onNavigateToRoom(code)
                        }
                        dismissedClipboardVideoId = vid
                        clipboardYoutubeVideoId = null
                    },
                    onDismiss = {
                        dismissedClipboardVideoId = vid
                        clipboardYoutubeVideoId = null
                    },
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }
        }

        // App Monogram Title with Official Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 18.dp)
        ) {
            Image(
                painter = painterResource(id = com.nddfeon.demonic.R.drawable.auxparty_logo),
                contentDescription = "AuxParty Logo",
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AUXPARTY ROOMS",
                color = DemonicCrimson,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        // Card 1: Create Room
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = DemonicCrimson.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E1729), Color(0xFF13101C))
                    )
                )
                .border(1.dp, DemonicBorder, RoundedCornerShape(22.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DemonicCrimson.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create",
                            tint = DemonicCrimson,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Create Room",
                            color = DemonicTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Host a synchronized music session",
                            color = DemonicTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You will be the host with full playback controls. Share your 6-character room code or deep link with friends to listen together in millisecond sync.",
                    color = DemonicTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Public Room Toggle / Option (Default: Checked / Ticked)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isPublicRoom) DemonicCrimson.copy(alpha = 0.08f)
                            else Color(0xFF161220)
                        )
                        .border(
                            1.dp,
                            if (isPublicRoom) DemonicCrimson.copy(alpha = 0.35f)
                            else DemonicBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            isPublicRoom = !isPublicRoom
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPublicRoom,
                        onCheckedChange = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            isPublicRoom = it
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = DemonicCrimson,
                            uncheckedColor = DemonicTextMuted,
                            checkmarkColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPublicRoom) "Public Room" else "Private Room",
                                color = if (isPublicRoom) DemonicTextPrimary else DemonicTextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isPublicRoom) DemonicCrimson.copy(alpha = 0.2f)
                                        else DemonicSurfaceVariant
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isPublicRoom) "🌐 DISCOVERABLE" else "🔒 INVITE ONLY",
                                    fontSize = 9.sp,
                                    color = if (isPublicRoom) DemonicCrimson else DemonicTextMuted,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isPublicRoom) {
                                "Visible in Discover tab so anyone can find and join"
                            } else {
                                "Hidden from Discover. Only people with your 6-digit code can join"
                            },
                            color = DemonicTextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                DemonicButton(
                    text = if (isPublicRoom) "Create Public Room 🌐" else "Create Private Room 🔒",
                    isLoading = uiState.isCreatingRoom,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.createRoom(isPublic = isPublicRoom) { code ->
                            onNavigateToRoom(code)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Card 2: Join Room
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1524), Color(0xFF120F1A))
                    )
                )
                .border(1.dp, DemonicBorder, RoundedCornerShape(22.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DemonicViolet.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Join",
                            tint = DemonicViolet,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Join Room",
                            color = DemonicTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enter a 6-character code",
                            color = DemonicTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                DemonicTextField(
                    value = uiState.roomCodeInput,
                    onValueChange = { viewModel.updateRoomCodeInput(it) },
                    placeholder = "e.g. AB7X9K",
                    label = "ROOM CODE",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            viewModel.joinRoom(uiState.roomCodeInput) { code ->
                                onNavigateToRoom(code)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                DemonicButton(
                    text = "Join Room",
                    variant = DemonicButtonVariant.SECONDARY,
                    enabled = uiState.roomCodeInput.length == 6,
                    isLoading = uiState.isJoiningRoom,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.joinRoom(uiState.roomCodeInput) { code ->
                            onNavigateToRoom(code)
                        }
                    }
                )
            }
        }

        // Section: Recent Rooms History
        if (recentRooms.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = DemonicSyncTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECENT ROOMS",
                        color = DemonicSyncTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                Text(
                    text = "Clear All",
                    color = DemonicTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.recentRoomsManager.clearAll()
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentRooms.forEach { recent ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DemonicSurface)
                            .border(1.dp, DemonicSurfaceVariant, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (recent.isHost) DemonicCrimson.copy(alpha = 0.2f) else DemonicViolet.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (recent.isHost) "👑" else "🎧",
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ROOM ${recent.roomCode}",
                                        color = DemonicTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (recent.isHost) DemonicCrimson.copy(alpha = 0.2f) else DemonicViolet.copy(alpha = 0.2f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (recent.isHost) "HOST" else "MEMBER",
                                            color = if (recent.isHost) DemonicCrimson else DemonicViolet,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DemonicButton(
                                text = "REJOIN ▶",
                                variant = DemonicButtonVariant.SECONDARY,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    onNavigateToRoom(recent.roomCode)
                                },
                                modifier = Modifier.height(34.dp),
                                fontSize = 12.sp,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    viewModel.recentRoomsManager.removeRoom(recent.roomCode)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = DemonicTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card 3: Discover Public Rooms
        if (uiState.publicRooms.isNotEmpty()) {
            Text(
                text = "DISCOVER PUBLIC ROOMS",
                color = DemonicCrimson,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                uiState.publicRooms.forEach { room ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DemonicSurface)
                            .border(1.dp, DemonicSurfaceVariant, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ROOM ${room.roomCode}",
                                    color = DemonicTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DemonicCrimson.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${room.memberCount} listening",
                                        color = DemonicCrimson,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = room.videoTitle.ifEmpty { "Synchronized Music Stream" },
                                color = DemonicTextMuted,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        DemonicButton(
                            text = "JOIN ▶",
                            onClick = {
                                viewModel.joinRoom(room.roomCode) { joinedCode ->
                                    onNavigateToRoom(joinedCode)
                                }
                            },
                            modifier = Modifier.height(34.dp),
                            fontSize = 12.sp,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Non-intrusive Banner Ad slot (Controlled by OwnerConfigManager)
        DemonicBannerAd(
            isAdsEnabled = isAdsEnabled,
            modifier = Modifier.padding(top = 16.dp)
        )

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = error,
                color = DemonicErrorRed,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    if (showOwnerPanel) {
        OwnerControlBottomSheet(
            ownerConfigManager = ownerConfigManager,
            onDismiss = { showOwnerPanel = false }
        )
    }

    if (showProfileSheet) {
        UserProfileBottomSheet(
            currentUser = currentUser,
            recentRoomsCount = recentRooms.size,
            onSaveProfile = { newName, newPhotoUrl ->
                viewModel.updateProfile(newName, newPhotoUrl)
            },
            onSignOut = {
                viewModel.signOut()
                onSignOut()
            },
            onDismiss = { showProfileSheet = false }
        )
    }
}
