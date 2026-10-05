package com.example.model

enum class TournamentPhase {
    SETUP,
    DRAFT_AUCTION,
    MATCH_STAGE,
    LEADERBOARD,
    COMPLETED
}

data class TeamStanding(
    val team: Team,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int
) {
    val goalDifference: Int
        get() = goalsFor - goalsAgainst
}

data class TournamentMatch(
    val id: String,
    val roundNumber: Int,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val isPlayed: Boolean = false,
    val result: MatchResult? = null
)

data class TournamentState(
    val phase: TournamentPhase = TournamentPhase.SETUP,
    val numberOfManagers: Int = 4,
    val teams: List<Team> = emptyList(),
    val matches: List<TournamentMatch> = emptyList(),
    val currentMatchIndex: Int = 0,
    val championTeam: Team? = null
)
