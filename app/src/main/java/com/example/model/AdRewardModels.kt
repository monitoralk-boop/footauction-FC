package com.example.model

enum class AdRewardType(
    val displayName: String,
    val rewardCoins: Long,
    val isPack: Boolean = false
) {
    VIDEO_1("Video 1 (+€10M)", 10_000_000L),
    VIDEO_2("Video 2 (+€25M)", 25_000_000L),
    VIDEO_3("Video 3 (+€40M)", 40_000_000L),
    FREE_PACK("Free Pack (70–80 OVR)", 0L, isPack = true)
}

data class DailyAdState(
    val video1Claimed: Boolean = false,
    val video2Claimed: Boolean = false,
    val video3Claimed: Boolean = false,
    val lastResetTimestamp: Long = System.currentTimeMillis()
) {
    val totalCoinsAvailable: Long = 75_000_000L // 10M + 25M + 40M
}

data class ActiveAdPlayback(
    val rewardType: AdRewardType,
    val sponsorName: String,
    val totalSeconds: Int = 4,
    val currentSecond: Int = 0,
    val isComplete: Boolean = false
)
