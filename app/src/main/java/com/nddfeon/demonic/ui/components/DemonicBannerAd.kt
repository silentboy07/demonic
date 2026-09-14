package com.nddfeon.demonic.ui.components

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.nddfeon.demonic.ads.AdMobManager
import com.nddfeon.demonic.ui.theme.DemonicBorder
import com.nddfeon.demonic.ui.theme.DemonicSurface

@Composable
fun DemonicBannerAd(
    isAdsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isAdsEnabled) return
    val isPreview = LocalInspectionMode.current
    var isAdLoaded by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isAdsEnabled,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DemonicSurface)
                        .border(1.dp, DemonicBorder, RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .border(1.dp, DemonicBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { context ->
                            AdView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                setAdSize(AdSize.BANNER)
                                adUnitId = AdMobManager.getBannerAdUnitId(context)
                                adListener = object : AdListener() {
                                    override fun onAdLoaded() {
                                        super.onAdLoaded()
                                        isAdLoaded = true
                                    }

                                    override fun onAdFailedToLoad(error: LoadAdError) {
                                        super.onAdFailedToLoad(error)
                                        isAdLoaded = false
                                    }
                                }
                                loadAd(AdRequest.Builder().build())
                            }
                        }
                    )
                }
            }
        }
    }
}
