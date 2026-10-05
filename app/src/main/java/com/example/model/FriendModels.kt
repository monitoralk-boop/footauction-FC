package com.example.model

data class FriendProfile(
    val userId: String,
    val managerName: String,
    val managerTag: String, // e.g. "Pep#1998"
    val clubName: String,
    val avatarIcon: String,
    val trophies: Int = 0,
    val divisionTier: Int = 9,
    val isOnline: Boolean = false
) {
    val divisionName: String
        get() = DivisionTier.fromNumber(divisionTier).divisionName
}

data class FriendRequest(
    val requestId: String,
    val fromUserId: String,
    val fromManagerName: String,
    val fromTag: String,
    val fromClub: String,
    val fromAvatar: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChallengeInvite(
    val challengeId: String,
    val fromUserId: String,
    val fromManagerName: String,
    val fromAvatar: String,
    val fromClub: String,
    val mode: String = "DUEL"
)
