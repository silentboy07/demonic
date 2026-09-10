package com.nddfeon.demonic.ui.login

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.nddfeon.demonic.ui.components.DemonicButton
import com.nddfeon.demonic.ui.components.DemonicButtonVariant
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicCrimsonDark
import com.nddfeon.demonic.ui.theme.DemonicCrimsonLight
import com.nddfeon.demonic.ui.theme.DemonicEmberGlow
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicTextSecondary
import com.nddfeon.demonic.ui.theme.DemonicViolet
import com.nddfeon.demonic.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Navigate to Home if already authenticated
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onNavigateToHome()
        }
    }

    // Google Sign-In Client launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrEmpty()) {
                    viewModel.signInWithIdToken(idToken) {
                        onNavigateToHome()
                    }
                } else {
                    // Fallback to name/photo if idToken is not configured in local environment
                    viewModel.signInDemoUser(account?.displayName ?: "Google User") {
                        onNavigateToHome()
                    }
                }
            } catch (e: Exception) {
                // If Play Services OAuth is not configured, give informative message & test option
                Toast.makeText(context, "Google Sign-In: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Subtle pulsating ember glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "EmberAura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DemonicBackground),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background embers & light radial glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        DemonicCrimson.copy(alpha = 0.18f),
                        DemonicViolet.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width * 0.85f * auraScale
                ),
                radius = size.width * 0.85f * auraScale,
                center = center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Brand Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Demonic Emblem Disc Icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(auraScale.coerceIn(0.95f, 1.05f))
                        .shadow(24.dp, CircleShape, spotColor = DemonicCrimson)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    DemonicCrimson,
                                    DemonicViolet,
                                    DemonicCrimsonDark,
                                    DemonicCrimson
                                )
                            )
                        )
                        .border(2.dp, DemonicCrimsonLight.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DemonicBackground)
                            .border(1.dp, DemonicCrimson, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "DEMONIC",
                    color = DemonicTextPrimary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Synchronized Music & Video Streaming",
                    color = DemonicTextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Listen together in real-time across devices",
                    color = DemonicTextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Authentication Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Google Sign-In Button
                GoogleSignInButton(
                    isLoading = uiState.isLoading,
                    onClick = {
                        val webClientId = try {
                            context.getString(com.nddfeon.demonic.R.string.default_web_client_id)
                        } catch (_: Exception) {
                            "491444689763-g7hd82266rc7it6l49vussnpb8kcpdb9.apps.googleusercontent.com"
                        }
                        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestProfile()

                        if (webClientId.isNotBlank()) {
                            gsoBuilder.requestIdToken(webClientId)
                        }
                        val client = GoogleSignIn.getClient(context, gsoBuilder.build())
                        googleSignInLauncher.launch(client.signInIntent)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Demo Sign-In (Ensures full accessibility & offline/emulator testing)
                DemonicButton(
                    text = "Continue as Guest",
                    variant = DemonicButtonVariant.OUTLINE,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.signInDemoUser("Demon Guest") {
                            onNavigateToHome()
                        }
                    }
                )

                uiState.errorMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = it,
                        color = Color(0xFFFF4D6D),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, shape, spotColor = Color(0x33000000))
            .clip(shape)
            .background(Color.White)
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(vertical = 15.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Google 'G' Icon Badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4285F4)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Sign in with Google",
                    color = Color(0xFF1F1F1F),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}
