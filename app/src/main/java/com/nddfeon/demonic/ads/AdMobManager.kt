package com.nddfeon.demonic.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.nddfeon.demonic.R

object AdMobManager {
    private const val TAG = "DemonicAdMob"

    // Google Sample / Test Ad Unit IDs (Fallback)
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialTime = 0L
    private const val INTERSTITIAL_COOLDOWN_MS = 45_000L // 45 seconds cooldown

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private val mainHandler = Handler(Looper.getMainLooper())

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob MobileAds initialized: ${status.adapterStatusMap.keys}")
                // Preload first interstitial and rewarded ads
                loadInterstitial(context)
                loadRewarded(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob", e)
        }
    }

    fun getBannerAdUnitId(context: Context): String {
        return try {
            val id = context.getString(R.string.admob_banner_ad_unit_id)
            if (id.isNotBlank()) id else TEST_BANNER_ID
        } catch (_: Exception) {
            TEST_BANNER_ID
        }
    }

    fun getInterstitialAdUnitId(context: Context): String {
        return try {
            val id = context.getString(R.string.admob_interstitial_ad_unit_id)
            if (id.isNotBlank()) id else TEST_INTERSTITIAL_ID
        } catch (_: Exception) {
            TEST_INTERSTITIAL_ID
        }
    }

    fun getRewardedAdUnitId(context: Context): String {
        return try {
            val id = context.getString(R.string.admob_rewarded_ad_unit_id)
            if (id.isNotBlank()) id else TEST_REWARDED_ID
        } catch (_: Exception) {
            TEST_REWARDED_ID
        }
    }

    // -------------------------------------------------------------
    // INTERSTITIAL ADS (Room exit, Screen transitions)
    // -------------------------------------------------------------
    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        val adUnitId = getInterstitialAdUnitId(context)

        InterstitialAd.load(
            context.applicationContext,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial Ad successfully loaded!")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showInterstitial(
        activity: Activity,
        forceSkipCooldown: Boolean = false,
        onDismiss: () -> Unit = {}
    ) {
        val now = System.currentTimeMillis()
        if (!forceSkipCooldown && (now - lastInterstitialTime) < INTERSTITIAL_COOLDOWN_MS) {
            Log.d(TAG, "Interstitial skipped due to cooldown. Resuming navigation.")
            onDismiss()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed.")
                    interstitialAd = null
                    lastInterstitialTime = System.currentTimeMillis()
                    loadInterstitial(activity)
                    mainHandler.post { onDismiss() }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial ad failed to show: ${adError.message}")
                    interstitialAd = null
                    loadInterstitial(activity)
                    mainHandler.post { onDismiss() }
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "No interstitial ad ready to show.")
            loadInterstitial(activity)
            onDismiss()
        }
    }

    // -------------------------------------------------------------
    // REWARDED ADS (Unlock VIP Themes, FX, Coins)
    // -------------------------------------------------------------
    fun loadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        val adUnitId = getRewardedAdUnitId(context)

        RewardedAd.load(
            context.applicationContext,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded Ad successfully loaded!")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun isRewardedAdReady(): Boolean = (rewardedAd != null)

    fun showRewarded(
        activity: Activity,
        onRewardEarned: (amount: Int) -> Unit,
        onDismiss: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad != null) {
            var rewardGranted = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed. Reward granted = $rewardGranted")
                    rewardedAd = null
                    loadRewarded(activity)
                    mainHandler.post { onDismiss() }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                    loadRewarded(activity)
                    mainHandler.post { onDismiss() }
                }
            }

            ad.show(activity) { rewardItem ->
                rewardGranted = true
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                mainHandler.post { onRewardEarned(rewardItem.amount) }
            }
        } else {
            Log.d(TAG, "No rewarded ad available right now.")
            loadRewarded(activity)
            onDismiss()
        }
    }
}
