package com.nddfeon.demonic.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.nddfeon.demonic.player.YouTubeUrlParser
import com.nddfeon.demonic.ui.theme.DemonicBackground
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicCrimson
import com.nddfeon.demonic.ui.theme.DemonicCrimsonDark
import com.nddfeon.demonic.ui.theme.DemonicSurface
import com.nddfeon.demonic.ui.theme.DemonicSurfaceVariant
import com.nddfeon.demonic.ui.theme.DemonicTextMuted
import com.nddfeon.demonic.ui.theme.DemonicTextPrimary
import com.nddfeon.demonic.ui.theme.DemonicViolet

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeExplorerSheet(
    onDismiss: () -> Unit,
    onPlayNow: (videoId: String, title: String) -> Unit,
    onAddToQueue: (videoId: String, title: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var pageTitle by remember { mutableStateOf("YouTube Explorer") }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }

    // Intercepted video prompt state
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    var selectedVideoTitle by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DemonicBackground,
        dragHandle = null,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DemonicSurface)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (webViewRef?.canGoBack() == true) {
                            webViewRef?.goBack()
                        } else {
                            onDismiss()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DemonicTextPrimary
                    )
                }

                IconButton(
                    onClick = {
                        if (webViewRef?.canGoForward() == true) {
                            webViewRef?.goForward()
                        }
                    },
                    enabled = webViewRef?.canGoForward() == true
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (webViewRef?.canGoForward() == true) DemonicTextPrimary else DemonicTextMuted.copy(alpha = 0.4f)
                    )
                }

                IconButton(
                    onClick = { webViewRef?.reload() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = DemonicTextMuted
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "LIVE EXPLORER",
                            color = DemonicCrimson,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = pageTitle,
                        color = DemonicTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DemonicSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DemonicTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Loading Progress Indicator
            if (isLoading && loadProgress < 1f) {
                LinearProgressIndicator(
                    progress = { loadProgress },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = DemonicCrimson,
                    trackColor = DemonicSurfaceVariant
                )
            }

            // Webview Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewRef = this
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = true // Don't auto-play inside explorer!
                                loadWithOverviewMode = true
                                useWideViewPort = true

                                // Use clean mobile user agent
                                val currentUa = userAgentString ?: ""
                                if (currentUa.contains("; wv") || currentUa.contains("Version/")) {
                                    userAgentString = currentUa
                                        .replace("; wv", "")
                                        .replace(Regex("Version/[0-9.]+ "), "")
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    loadProgress = newProgress / 100f
                                    isLoading = newProgress < 100
                                }

                                override fun onReceivedTitle(view: WebView?, title: String?) {
                                    title?.let { pageTitle = it }
                                }
                            }

                            fun injectAdBlocker(view: WebView?) {
                                val js = """
                                    (function() {
                                        var cssId = 'demonic-adblock-css';
                                        if (!document.getElementById(cssId)) {
                                            var style = document.createElement('style');
                                            style.id = cssId;
                                            style.innerHTML = `
                                                ytm-promoted-sparkles-web-renderer,
                                                ytm-promoted-video-renderer,
                                                ytm-companion-ad-renderer,
                                                ytm-promoted-sparkles-text-search-web-renderer,
                                                ytm-ad-slot-renderer,
                                                .ytm-promoted-sparkles-web-renderer,
                                                .ad-container,
                                                .ad-div,
                                                .video-ads,
                                                .ytp-ad-overlay-container,
                                                .ytp-ad-message-container,
                                                .ytp-ad-action-interstitial,
                                                .companion-ad-container,
                                                .ytp-ad-preview-container,
                                                .ad-created,
                                                .ytp-ad-module,
                                                .ytp-ad-image-overlay,
                                                .ytp-ad-text-overlay,
                                                ytd-promoted-video-renderer,
                                                ytd-display-ad-renderer,
                                                ytd-banner-promo-renderer,
                                                ytd-in-feed-ad-layout-renderer,
                                                ytd-ad-slot-renderer,
                                                .mobile-topbar-header-sign-in-button,
                                                .upsell-dialog-renderer,
                                                #masthead-ad,
                                                #player-ads,
                                                ytm-item-section-renderer[section-identifier="comment-item-section"] + ytm-ad-slot-renderer {
                                                    display: none !important;
                                                    visibility: hidden !important;
                                                    height: 0px !important;
                                                    width: 0px !important;
                                                    opacity: 0 !important;
                                                    pointer-events: none !important;
                                                }
                                            `;
                                            (document.head || document.documentElement).appendChild(style);
                                        }
                                        if (!window._demonicExplorerAdBlock) {
                                            window._demonicExplorerAdBlock = true;
                                            setInterval(function() {
                                                try {
                                                    var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .videoAdUiSkipButton, .ytp-skip-ad-button, .ytp-ad-overlay-close-button');
                                                    if (skipBtn) skipBtn.click();
                                                    var ad = document.querySelector('.ad-showing, .ad-interrupting');
                                                    var v = document.querySelector('video');
                                                    if (ad && v) {
                                                        v.muted = true;
                                                        v.playbackRate = 16.0;
                                                    } else if (v && v.playbackRate > 1.0) {
                                                        v.playbackRate = 1.0;
                                                        v.muted = false;
                                                    }
                                                    var appPromo = document.querySelector('.upsell-dialog-renderer button, .mobile-topbar-header-sign-in-button');
                                                    if (appPromo) appPromo.click();
                                                } catch(e) {}
                                            }, 250);
                                        }
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(js, null)
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    injectAdBlocker(view)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                    injectAdBlocker(view)
                                }

                                override fun onLoadResource(view: WebView?, url: String?) {
                                    super.onLoadResource(view, url)
                                    injectAdBlocker(view)
                                }

                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): WebResourceResponse? {
                                    val reqUrl = request?.url?.toString()?.lowercase() ?: return null
                                    if (reqUrl.contains("doubleclick.net") ||
                                        reqUrl.contains("/pagead/") ||
                                        reqUrl.contains("googleads") ||
                                        reqUrl.contains("adservice.google") ||
                                        reqUrl.contains("googlesyndication.com") ||
                                        reqUrl.contains("/api/stats/ads") ||
                                        reqUrl.contains("/ptracking") ||
                                        reqUrl.contains("/get_midroll_info") ||
                                        reqUrl.contains("/youtubei/v1/player/ad_break") ||
                                        reqUrl.contains("ad.doubleclick") ||
                                        reqUrl.contains("amazon-adsystem") ||
                                        reqUrl.contains("adnxs.com")
                                    ) {
                                        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                                    }
                                    return super.shouldInterceptRequest(view, request)
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    val extractedId = YouTubeUrlParser.extractVideoId(url)

                                    if (extractedId != null) {
                                        // Intercept YouTube video link!
                                        selectedVideoId = extractedId
                                        selectedVideoTitle = view?.title ?: "Selected Track"
                                        return true // Intercept click
                                    }
                                    return false // Allow normal web navigation
                                }
                            }

                            loadUrl("https://m.youtube.com")
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Intercepted Video Action Card (Floats over bottom of webview)
                val currentSelectedId = selectedVideoId
                if (currentSelectedId != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f))
                            .clickable { selectedVideoId = null },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .background(DemonicSurface)
                                .border(1.dp, DemonicBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .clickable(enabled = false) {}
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AsyncImage(
                                    model = "https://img.youtube.com/vi/$currentSelectedId/hqdefault.jpg",
                                    contentDescription = "Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, DemonicBorder, RoundedCornerShape(8.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "VIDEO DETECTED",
                                        color = DemonicCrimson,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = selectedVideoTitle.ifEmpty { "ID: $currentSelectedId" },
                                        color = DemonicTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(onClick = { selectedVideoId = null }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = DemonicTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Play Now Button
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.horizontalGradient(listOf(DemonicCrimson, DemonicCrimsonDark)))
                                        .clickable {
                                            onPlayNow(currentSelectedId, selectedVideoTitle)
                                            selectedVideoId = null
                                            onDismiss()
                                        }
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Play Now",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Add to Queue Button
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DemonicSurfaceVariant)
                                        .border(1.dp, DemonicBorder, RoundedCornerShape(12.dp))
                                        .clickable {
                                            onAddToQueue(currentSelectedId, selectedVideoTitle)
                                            selectedVideoId = null
                                        }
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = DemonicTextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Add to Queue",
                                        color = DemonicTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
