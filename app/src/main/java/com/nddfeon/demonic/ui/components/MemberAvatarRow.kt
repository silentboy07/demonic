package com.nddfeon.demonic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicWarningAmber

@Composable
fun MemberAvatarRow(
    members: List<Member>,
    modifier: Modifier = Modifier
) {
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
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${members.size} listening",
                color = DemonicTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
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
            ) { index, member ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = spring(stiffness = 400f)) +
                            scaleIn(
                                initialScale = 0.6f,
                                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                            )
                ) {
                    MemberAvatarItem(member = member)
                }
            }
        }
    }
}

@Composable
fun MemberAvatarItem(
    member: Member,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(52.dp),
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
                        width = if (member.isHost) 2.dp else 1.dp,
                        color = if (member.isHost) DemonicWarningAmber else DemonicBorder,
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
                    // Fallback initials
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
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (member.isHost) "${member.name} (Host)" else member.name,
            color = if (member.isHost) DemonicWarningAmber else DemonicTextSecondaryColor(),
            fontSize = 10.sp,
            fontWeight = if (member.isHost) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DemonicTextSecondaryColor() = Color(0xFFA59FB1)
