package com.example.model

data class LiveCommentaryEvent(
    val matchMinute: Int,
    val text: String,
    val isGoal: Boolean = false,
    val isHomeGoal: Boolean = false
)

data class LiveMatchState(
    val isLive: Boolean = false,
    val isFinished: Boolean = false,
    val homeTeam: Team,
    val awayTeam: Team,
    val currentRealSecond: Int = 0,
    val matchMinute: Int = 1,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val commentary: List<LiveCommentaryEvent> = emptyList(),
    val homePossession: Int = 50,
    val awayPossession: Int = 50,
    val trophiesDelta: Int = 0,
    val coinsEarned: Long = 0L,
    val promotionTriggered: DivisionTier? = null
)
