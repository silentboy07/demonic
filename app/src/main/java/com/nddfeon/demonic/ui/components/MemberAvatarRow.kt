package com.nddfeon.demonic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nddfeon.demonic.data.model.Member
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicViolet
import com.nddfeon.demonic.ui.theme.DemonicWarningAmber

@Composable
fun MemberAvatarRow(
    members: List<Member>,
    djId: String? = null,
    isHostUser: Boolean = false,
    onPassAux: (uid: String) -> Unit = {},
    onOpenMembersSheet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedMemberForAux by remember { mutableStateOf<Member?>(null) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Members Count Chip
        Box(
            modifier = Modifier
                .padding(start = 16.dp, end = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DemonicSurfaceVariant)
                .border(1.dp, DemonicBorder, RoundedCornerShape(12.dp))
                .clickable { onOpenMembersSheet() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00F5D4))
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${members.size} listening",
                    color = DemonicTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(
                items = members,
                key = { _, member -> member.uid }
            ) { _, member ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = spring(stiffness = 400f)) +
                            scaleIn(
                                initialScale = 0.6f,
                                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                            )
                ) {
                    val isDj = (member.uid == djId)
                    MemberAvatarItem(
                        member = member,
                        isDj = isDj,
                        canClickToPassAux = isHostUser && !member.isHost,
                        onClick = {
                            if (isHostUser && !member.isHost) {
                                selectedMemberForAux = member
                            }
                        }
                    )
                }
            }
        }
    }

    // Pass the Aux confirmation dialog
    selectedMemberForAux?.let { target ->
        val isAlreadyDj = (target.uid == djId)
        AlertDialog(
            onDismissRequest = { selectedMemberForAux = null },
            containerColor = DemonicSurface,
            title = {
                Text(
                    text = if (isAlreadyDj) "Take Back the Aux" else "Pass the Aux (Make DJ)",
                    color = DemonicTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isAlreadyDj) {
                        "Revoke DJ controls from ${target.name} and take back full control?"
                    } else {
                        "Give playback and queue control to ${target.name} as the room DJ?"
                    },
                    color = DemonicTextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPassAux(if (isAlreadyDj) "" else target.uid)
                        selectedMemberForAux = null
                    }
                ) {
                    Text(
                        text = if (isAlreadyDj) "Take Back" else "Pass Aux",
                        color = DemonicCrimson,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMemberForAux = null }) {
                    Text("Cancel", color = DemonicTextMuted)
                }
            }
        )
    }
}

@Composable
fun MemberAvatarItem(
    member: Member,
    isDj: Boolean = false,
    canClickToPassAux: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(54.dp)
            .clickable(enabled = canClickToPassAux, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262033))
                    .border(
                        width = if (member.isHost || isDj) 2.dp else 1.dp,
                        color = when {
                            member.isHost -> DemonicWarningAmber
                            isDj -> DemonicViolet
                            else -> DemonicBorder
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (member.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(member.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = member.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    val initial = member.name.firstOrNull()?.uppercase() ?: "D"
                    Text(
                        text = initial,
                        color = DemonicTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Crown Badge for Host
            if (member.isHost) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(DemonicWarningAmber)
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Host",
                        tint = Color.Black,
                        modifier = Modifier.size(10.dp)
                    )
                }
            } else if (isDj) {
                // Headphone Badge for DJ
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(DemonicViolet)
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "DJ",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when {
                member.isHost -> "${member.name} 👑"
                isDj -> "${member.name} 🎧"
                else -> member.name
            },
            color = when {
                member.isHost -> DemonicWarningAmber
                isDj -> DemonicViolet
                else -> Color(0xFFA59FB1)
            },
            fontSize = 10.sp,
            fontWeight = if (member.isHost || isDj) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
