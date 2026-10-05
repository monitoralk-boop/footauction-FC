package com.example.model

import kotlin.random.Random

data class MatchEvent(
    val minute: Int,
    val type: MatchEventType,
    val description: String,
    val scoringTeamId: String? = null
)

enum class MatchEventType {
    KICKOFF,
    GOAL,
    CHANCE,
    SAVE,
    YELLOW_CARD,
    PENALTY,
    FULL_TIME
}

data class MatchResult(
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: Int,
    val awayScore: Int,
    val homeScorers: List<String>,
    val awayScorers: List<String>,
    val events: List<MatchEvent>,
    val homePossession: Int,
    val awayPossession: Int,
    val homeShots: Int,
    val awayShots: Int,
    val mvpPlayer: PlayerCard?
) {
    val isDraw: Boolean
        get() = homeScore == awayScore

    val winner: Team?
        get() = when {
            homeScore > awayScore -> homeTeam
            awayScore > homeScore -> awayTeam
            else -> null
        }

    val scoreDisplay: String
        get() = "${homeTeam.name} $homeScore - $awayScore ${awayTeam.name}"
}

object MatchSimulator {
    fun simulate(home: Team, away: Team): MatchResult {
        val homeOvr = home.teamOverall
        val awayOvr = away.teamOverall

        val diff = (homeOvr - awayOvr).coerceIn(-20, 20)
        // Midfield determines possession
        val homeMid = home.midfieldScore
        val awayMid = away.midfieldScore
        val possessionBase = 50 + ((homeMid - awayMid) / 2)
        val homePossession = possessionBase.coerceIn(35, 65)
        val awayPossession = 100 - homePossession

        // Expected goals based on attack vs defense
        val homeExpectedGoals = ((home.attackScore - away.defenseScore + 20) / 10.0).coerceIn(0.5, 4.5)
        val awayExpectedGoals = ((away.attackScore - home.defenseScore + 20) / 10.0).coerceIn(0.5, 4.5)

        val homeGoals = simulateGoals(homeExpectedGoals)
        val awayGoals = simulateGoals(awayExpectedGoals)

        val homeShots = homeGoals + Random.nextInt(4, 10)
        val awayShots = awayGoals + Random.nextInt(4, 10)

        val events = mutableListOf<MatchEvent>()
        events.add(MatchEvent(1, MatchEventType.KICKOFF, "The referee blows the whistle! Match kicks off in an electric atmosphere."))

        val homeAttackers = home.squad.filter { it.position.category == PositionCategory.ATTACKER || it.position.category == PositionCategory.MIDFIELDER }
        val awayAttackers = away.squad.filter { it.position.category == PositionCategory.ATTACKER || it.position.category == PositionCategory.MIDFIELDER }

        val homeScorers = mutableListOf<String>()
        val awayScorers = mutableListOf<String>()

        val minutes = (5..88).shuffled().take(homeGoals + awayGoals + Random.nextInt(3, 6)).sorted()

        var hGoalsAssigned = 0
        var aGoalsAssigned = 0

        for (minute in minutes) {
            val isHomeEvent = Random.nextBoolean()
            if (isHomeEvent && hGoalsAssigned < homeGoals) {
                hGoalsAssigned++
                val scorer = homeAttackers.randomOrNull()?.name ?: "${home.name} Striker"
                homeScorers.add("$scorer $minute'")
                events.add(MatchEvent(minute, MatchEventType.GOAL, "⚽ GOAL! $scorer slots it past the keeper for ${home.name}!", home.id))
            } else if (!isHomeEvent && aGoalsAssigned < awayGoals) {
                aGoalsAssigned++
                val scorer = awayAttackers.randomOrNull()?.name ?: "${away.name} Striker"
                awayScorers.add("$scorer $minute'")
                events.add(MatchEvent(minute, MatchEventType.GOAL, "⚽ GOAL! $scorer strikes with precision for ${away.name}!", away.id))
            } else {
                val randEvent = Random.nextInt(3)
                when (randEvent) {
                    0 -> events.add(MatchEvent(minute, MatchEventType.SAVE, "🧤 SPECTACULAR SAVE! The goalkeeper dives across to deflect the shot away."))
                    1 -> events.add(MatchEvent(minute, MatchEventType.CHANCE, "🔥 Tremendous chance! The ball rattles against the crossbar!"))
                    2 -> events.add(MatchEvent(minute, MatchEventType.YELLOW_CARD, "🟨 Yellow Card shown following a tactical sliding tackle."))
                }
            }
        }

        // Catch any remaining unassigned goals
        while (hGoalsAssigned < homeGoals) {
            val min = Random.nextInt(60, 90)
            val scorer = homeAttackers.randomOrNull()?.name ?: "${home.name} Striker"
            homeScorers.add("$scorer $min'")
            events.add(MatchEvent(min, MatchEventType.GOAL, "⚽ LATE GOAL! $scorer taps in from close range!", home.id))
            hGoalsAssigned++
        }
        while (aGoalsAssigned < awayGoals) {
            val min = Random.nextInt(60, 90)
            val scorer = awayAttackers.randomOrNull()?.name ?: "${away.name} Striker"
            awayScorers.add("$scorer $min'")
            events.add(MatchEvent(min, MatchEventType.GOAL, "⚽ DRAMATIC GOAL! $scorer bursts through the defense to score!", away.id))
            aGoalsAssigned++
        }

        events.sortBy { it.minute }
        events.add(MatchEvent(90, MatchEventType.FULL_TIME, "Full-Time whistle! Final score: ${home.name} $homeGoals - $awayGoals ${away.name}."))

        val allSquadPlayers = (home.squad + away.squad)
        val mvp = if (homeGoals > awayGoals) {
            homeAttackers.maxByOrNull { it.overall } ?: allSquadPlayers.firstOrNull()
        } else if (awayGoals > homeGoals) {
            awayAttackers.maxByOrNull { it.overall } ?: allSquadPlayers.firstOrNull()
        } else {
            allSquadPlayers.maxByOrNull { it.overall }
        }

        return MatchResult(
            homeTeam = home,
            awayTeam = away,
            homeScore = homeGoals,
            awayScore = awayGoals,
            homeScorers = homeScorers,
            awayScorers = awayScorers,
            events = events,
            homePossession = homePossession,
            awayPossession = awayPossession,
            homeShots = homeShots,
            awayShots = awayShots,
            mvpPlayer = mvp
        )
    }

    private fun simulateGoals(expected: Double): Int {
        val base = expected.toInt()
        val remainder = expected - base
        var goals = base
        if (Random.nextDouble() < remainder) goals++
        val luck = Random.nextInt(100)
        if (luck < 15) goals += 1
        if (luck < 5) goals += 1
        return goals.coerceAtLeast(0)
    }
}
