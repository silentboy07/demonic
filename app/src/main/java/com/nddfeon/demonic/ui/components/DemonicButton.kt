package com.nddfeon.demonic.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicCrimsonDark
import com.nddfeon.demonic.ui.theme.DemonicCrimsonLight
import com.nddfeon.demonic.ui.theme.DemonicEmberGlow
import com.nddfeon.demonic.ui.theme.DemonicSurfaceHighlight
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary

enum class DemonicButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINE
}

@Composable
fun DemonicButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: DemonicButtonVariant = DemonicButtonVariant.PRIMARY,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile spring scale animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "ButtonScale"
    )

    val shape = RoundedCornerShape(14.dp)

    val (backgroundBrush, borderModifier) = when (variant) {
        DemonicButtonVariant.PRIMARY -> {
            if (enabled) {
                Brush.horizontalGradient(listOf(DemonicCrimson, DemonicCrimsonDark)) to Modifier.border(
                    width = 1.dp,
                    color = DemonicCrimsonLight.copy(alpha = 0.5f),
                    shape = shape
                )
            } else {
                Brush.linearGradient(listOf(DemonicSurfaceHighlight, DemonicSurfaceHighlight)) to Modifier
            }
        }
        DemonicButtonVariant.SECONDARY -> {
            Brush.linearGradient(listOf(Color(0xFF221B2E), Color(0xFF191322))) to Modifier.border(
                width = 1.dp,
                color = Color(0xFF43345A),
                shape = shape
            )
        }
        DemonicButtonVariant.OUTLINE -> {
            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)) to Modifier.border(
                width = 1.dp,
                color = if (enabled) DemonicCrimson.copy(alpha = 0.7f) else DemonicBorderColor(),
                shape = shape
            )
        }
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (enabled && variant == DemonicButtonVariant.PRIMARY) 12.dp else 0.dp,
                shape = shape,
                spotColor = DemonicCrimson,
                ambientColor = DemonicCrimson
            )
            .clip(shape)
            .background(backgroundBrush)
            .then(borderModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = DemonicTextPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let {
                    it()
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    color = if (enabled) DemonicTextPrimary else DemonicTextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}

@Composable
private fun DemonicBorderColor() = Color(0xFF332A44)
