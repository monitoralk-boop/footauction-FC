package com.example.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.model.AdRewardType
import com.example.model.DailyAdState
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Service to manage AdMob rewarded video ad slots that provide:
 * - 10M coins payout (Slot 1, 24h renewal)
 * - 25M coins payout (Slot 2, 24h renewal)
 * - 40M coins payout (Slot 3, 24h renewal)
 * - Dedicated slot for a Free Pack reward (70-80 OVR player)
 *
 * Configured with official AdMob credentials:
 * App ID: ca-app-pub-9188107526006130~9514205500
 * Rewarded Ad Unit ID: ca-app-pub-9188107526006130/7299105704
 */
class RewardedAdService private constructor() {

    companion object {
        const val TAG = "RewardedAdService"
        const val ADMOB_APP_ID = "ca-app-pub-9188107526006130~9514205500"
        const val ADMOB_REWARDED_AD_UNIT_ID = "ca-app-pub-9188107526006130/7299105704"

        const val REWARD_PAYOUT_SLOT_1 = 10_000_000L // 10M
        const val REWARD_PAYOUT_SLOT_2 = 25_000_000L // 25M
        const val REWARD_PAYOUT_SLOT_3 = 40_000_000L // 40M

        private const val TWENTY_FOUR_HOURS_MILLIS = 24 * 60 * 60 * 1000L

        @Volatile
        private var instance: RewardedAdService? = null

        fun getInstance(): RewardedAdService {
            return instance ?: synchronized(this) {
                instance ?: RewardedAdService().also { instance = it }
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main)

    private var currentRewardedAd: RewardedAd? = null
    private var isAdLoading = false

    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    private val _adSlotState = MutableStateFlow(DailyAdState())
    val adSlotState: StateFlow<DailyAdState> = _adSlotState.asStateFlow()

    private val _timeUntilResetFormatted = MutableStateFlow("24:00:00")
    val timeUntilResetFormatted: StateFlow<String> = _timeUntilResetFormatted.asStateFlow()

    /**
     * Initialize Google Mobile Ads SDK and pre-load the first rewarded ad.
     */
    fun initialize(context: Context) {
        MobileAds.initialize(context) { status ->
            Log.d(TAG, "AdMob MobileAds initialized with status: $status")
            loadAd(context)
        }
        checkAndResetDailyCycle()
    }

    /**
     * Request and preload a rewarded ad using the provided ad unit ID.
     */
    fun loadAd(context: Context) {
        if (isAdLoading || currentRewardedAd != null) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            ADMOB_REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    isAdLoading = false
                    currentRewardedAd = ad
                    _isAdReady.value = true
                    Log.d(TAG, "AdMob RewardedAd successfully loaded for unit: $ADMOB_REWARDED_AD_UNIT_ID")

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            currentRewardedAd = null
                            _isAdReady.value = false
                            Log.d(TAG, "AdMob RewardedAd dismissed. Preloading next ad...")
                            loadAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            currentRewardedAd = null
                            _isAdReady.value = false
                            Log.e(TAG, "AdMob RewardedAd failed to show: ${adError.message}")
                            loadAd(context)
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "AdMob RewardedAd showing fullscreen content.")
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isAdLoading = false
                    currentRewardedAd = null
                    _isAdReady.value = false
                    Log.w(TAG, "AdMob RewardedAd failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                }
            }
        )
    }

    /**
     * Check if 24 hours have elapsed since the current cycle started.
     * If so, reset all video slots for the new day.
     */
    fun checkAndResetDailyCycle() {
        val now = System.currentTimeMillis()
        val current = _adSlotState.value
        val elapsed = now - current.lastResetTimestamp

        if (elapsed >= TWENTY_FOUR_HOURS_MILLIS) {
            _adSlotState.update {
                it.copy(
                    video1Claimed = false,
                    video2Claimed = false,
                    video3Claimed = false,
                    lastResetTimestamp = now
                )
            }
        }

        // Update countdown string
        val remaining = (TWENTY_FOUR_HOURS_MILLIS - (now - _adSlotState.value.lastResetTimestamp)).coerceAtLeast(0L)
        val hours = remaining / (1000 * 60 * 60)
        val minutes = (remaining / (1000 * 60)) % 60
        val seconds = (remaining / 1000) % 60
        _timeUntilResetFormatted.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Show a rewarded ad for the given slot.
     * If real AdMob ad is ready, displays the fullscreen AdMob ad.
     * If AdMob inventory is unavailable or running on emulator without Play Services,
     * triggers onFallbackSimulation so the user is never blocked from claiming rewards!
     */
    fun showRewardedAd(
        activity: Activity,
        slotType: AdRewardType,
        onRewardEarned: (AdRewardType) -> Unit,
        onFallbackSimulation: (AdRewardType) -> Unit
    ) {
        checkAndResetDailyCycle()
        val ad = currentRewardedAd

        if (ad != null) {
            var rewardClaimed = false
            ad.show(activity) { rewardItem ->
                rewardClaimed = true
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type} for slot $slotType")
                claimSlotReward(slotType)
                onRewardEarned(slotType)
            }
        } else {
            Log.d(TAG, "AdMob RewardedAd not currently cached, launching fallback simulated video...")
            onFallbackSimulation(slotType)
            // Trigger background load for future tries
            loadAd(activity)
        }
    }

    /**
     * Records the claimed status for the respective slot in the 24-hour cycle.
     */
    fun claimSlotReward(slotType: AdRewardType) {
        _adSlotState.update { state ->
            when (slotType) {
                AdRewardType.VIDEO_1 -> state.copy(video1Claimed = true)
                AdRewardType.VIDEO_2 -> state.copy(video2Claimed = true)
                AdRewardType.VIDEO_3 -> state.copy(video3Claimed = true)
                AdRewardType.FREE_PACK -> state // Dedicated slot can be opened repeatedly or daily
            }
        }
    }

    /**
     * Check if a specific slot can be played right now.
     */
    fun isSlotAvailable(slotType: AdRewardType): Boolean {
        checkAndResetDailyCycle()
        val state = _adSlotState.value
        return when (slotType) {
            AdRewardType.VIDEO_1 -> !state.video1Claimed
            AdRewardType.VIDEO_2 -> state.video1Claimed && !state.video2Claimed
            AdRewardType.VIDEO_3 -> state.video2Claimed && !state.video3Claimed
            AdRewardType.FREE_PACK -> true
        }
    }
}
