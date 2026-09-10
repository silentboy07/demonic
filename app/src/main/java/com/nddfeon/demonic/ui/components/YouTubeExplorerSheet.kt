package com.nddfeon.demonic.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nddfeon.demonic.player.YouTubeSearchManager
import com.nddfeon.demonic.player.YouTubeSearchResult
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicCrimsonDark
import com.nddfeon.demonic.ui.theme.DemonicErrorRed
import com.nddfeon.demonic.ui.theme.DemonicSyncTeal
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicTextSecondary
import com.nddfeon.demonic.ui.theme.DemonicViolet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeExplorerSheet(
    searchManager: YouTubeSearchManager,
    canControlPlayback: Boolean,
    onDismiss: () -> Unit,
    onPlayNow: (videoId: String, title: String) -> Unit,
    onAddToQueue: (videoId: String, title: String) -> Unit,
    onImportPlaylistOrLink: (
        input: String,
        onProgress: (String) -> Unit,
        onSuccess: (count: Int, message: String) -> Unit,
        onError: (String) -> Unit
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    // Importer state
    var linkInput by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var importStatus by remember { mutableStateOf<String?>(null) }
    var importFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var isImportError by remember { mutableStateOf(false) }

    // Categories and search
    var selectedCategory by remember { mutableStateOf(YouTubeSearchManager.CATEGORIES.first()) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var songsList by remember { mutableStateOf(searchManager.getCategorySongs(selectedCategory)) }
    var queuedFeedbackVideoId by remember { mutableStateOf<String?>(null) }

    var searchJob by remember { mutableStateOf<Job?>(null) }

    fun triggerSearch(q: String) {
        searchJob?.cancel()
        searchJob = scope.launch {
            if (q.isBlank()) {
                songsList = searchManager.getCategorySongs(selectedCategory)
                isSearching = false
                return@launch
            }
            isSearching = true
            delay(400L) // Debounce typing
            songsList = searchManager.search(q)
            isSearching = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DemonicBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DemonicBorder)
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(DemonicCrimson, DemonicViolet)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "DEMONIC MUSIC HUB",
                            color = DemonicTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (canControlPlayback) "Play syncs live to all phones • Auto-queue playlists" else "Add songs or playlists to room queue",
                            color = DemonicTextMuted,
                            fontSize = 11.sp
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

            // Playlist / Link Auto-Queue Importer Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DemonicSurface)
                    .border(1.dp, DemonicViolet.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = DemonicViolet,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "IMPORT PLAYLIST / VIDEO LINK",
                                color = DemonicViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        if (isImporting) {
                            CircularProgressIndicator(
                                color = DemonicViolet,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DemonicTextField(
                            value = linkInput,
                            onValueChange = {
                                linkInput = it
                                importFeedbackMessage = null
                            },
                            placeholder = "Paste YouTube playlist URL or song link...",
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .height(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (linkInput.isNotBlank() && !isImporting) {
                                        Brush.horizontalGradient(listOf(DemonicCrimson, DemonicViolet))
                                    } else {
                                        Brush.horizontalGradient(listOf(DemonicSurfaceVariant, DemonicSurfaceVariant))
                                    }
                                )
                                .clickable(enabled = linkInput.isNotBlank() && !isImporting) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                    isImporting = true
                                    importFeedbackMessage = null
                                    isImportError = false

                                    onImportPlaylistOrLink(
                                        linkInput,
                                        { progress ->
                                            importStatus = progress
                                        },
                                        { count, msg ->
                                            isImporting = false
                                            importStatus = null
                                            importFeedbackMessage = msg
                                            isImportError = false
                                            linkInput = ""
                                        },
                                        { err ->
                                            isImporting = false
                                            importStatus = null
                                            importFeedbackMessage = err
                                            isImportError = true
                                        }
                                    )
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "IMPORT",
                                color = if (linkInput.isNotBlank() && !isImporting) Color.White else DemonicTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Status / Feedback message
                    AnimatedVisibility(visible = importStatus != null || importFeedbackMessage != null) {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            if (importStatus != null) {
                                Text(
                                    text = "⏳ $importStatus",
                                    color = DemonicTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            if (importFeedbackMessage != null) {
                                Text(
                                    text = if (isImportError) "⚠️ $importFeedbackMessage" else "✅ $importFeedbackMessage",
                                    color = if (isImportError) DemonicErrorRed else DemonicSyncTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Zero-Search Quick Pick Categories ("Bina search kiye gaane chalayein")
            Text(
                text = "INSTANT PICKS (NO SEARCH NEEDED)",
                color = DemonicTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(YouTubeSearchManager.CATEGORIES) { category ->
                    val isSelected = (selectedCategory == category && searchQuery.isEmpty())
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(listOf(DemonicCrimson, DemonicCrimsonDark))
                                } else {
                                    Brush.horizontalGradient(listOf(DemonicSurface, DemonicSurface))
                                }
                            )
                            .border(
                                1.dp,
                                if (isSelected) DemonicCrimson else DemonicSurfaceVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedCategory = category
                                searchQuery = ""
                                songsList = searchManager.getCategorySongs(category)
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else DemonicTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Manual Search Input Bar
            DemonicTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    triggerSearch(it)
                },
                placeholder = "Or search specific song, singer, movie...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = DemonicTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Songs Feed List
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DemonicCrimson, modifier = Modifier.size(36.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(songsList, key = { it.videoId }) { song ->
                        val isQueuedJustNow = (queuedFeedbackVideoId == song.videoId)

                        MusicHubSongCard(
                            song = song,
                            canControlPlayback = canControlPlayback,
                            isQueuedFeedback = isQueuedJustNow,
                            onPlayNow = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                onPlayNow(song.videoId, song.title)
                                onDismiss()
                            },
                            onAddToQueue = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onAddToQueue(song.videoId, song.title)
                                queuedFeedbackVideoId = song.videoId
                                scope.launch {
                                    delay(2000)
                                    if (queuedFeedbackVideoId == song.videoId) {
                                        queuedFeedbackVideoId = null
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MusicHubSongCard(
    song: YouTubeSearchResult,
    canControlPlayback: Boolean,
    isQueuedFeedback: Boolean,
    onPlayNow: () -> Unit,
    onAddToQueue: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DemonicSurface)
            .border(1.dp, DemonicBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // High-Quality Video Thumbnail with Dark Bevel
        Box(
            modifier = Modifier
                .size(width = 84.dp, height = 54.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Play overlay icon for visual cue
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Channel / Category
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = DemonicTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = song.channel.ifEmpty { "YouTube Music" },
                color = DemonicTextMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Native Mobile Control Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. Play Now (Broadcast to Room) - For Host / DJ
            if (canControlPlayback) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(DemonicCrimson, DemonicCrimsonDark)
                            )
                        )
                        .clickable { onPlayNow() }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "PLAY",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. Add to Queue Button (Available to Host, DJ and Listeners)
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isQueuedFeedback) DemonicSyncTeal.copy(alpha = 0.2f) else DemonicViolet.copy(alpha = 0.15f))
                .border(
                    1.dp,
                    if (isQueuedFeedback) DemonicSyncTeal else DemonicViolet.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .clickable(enabled = !isQueuedFeedback) { onAddToQueue() }
                .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isQueuedFeedback) Icons.Default.Check else Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = "Queue",
                        tint = if (isQueuedFeedback) DemonicSyncTeal else DemonicViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (isQueuedFeedback) "QUEUED" else "QUEUE",
                        color = if (isQueuedFeedback) DemonicSyncTeal else DemonicViolet,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
