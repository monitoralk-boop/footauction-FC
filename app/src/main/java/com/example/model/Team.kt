package com.example.model

enum class Formation(val displayName: String, val defCount: Int, val midCount: Int, val attCount: Int) {
    F_4_3_3("4-3-3 Attack", 4, 3, 3),
    F_4_4_2("4-4-2 Classic", 4, 4, 2),
    F_3_5_2("3-5-2 Total Football", 3, 5, 2)
}

data class Team(
    val id: String,
    val name: String,
    val isHuman: Boolean = false,
    val avatarIcon: String = "⚽",
    val budget: Long = 20_000_000L,
    val squad: List<PlayerCard> = emptyList(), // Starting 11
    val reserves: List<PlayerCard> = emptyList(), // Bench/Reserve squad
    val formation: Formation = Formation.F_4_3_3,
    val trophies: Int = 0,
    val matchWins: Int = 0,
    val matchLosses: Int = 0,
    val matchDraws: Int = 0,
    val tournamentPoints: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val unlockedSkills: List<String> = emptyList()
) {
    val currentDivision: DivisionTier
        get() = DivisionTier.fromTrophies(trophies)

    val budgetFormatted: String
        get() {
            return when {
                budget >= 1_000_000_000 -> String.format("€%.1fB", budget / 1_000_000_000.0)
                budget >= 1_000_000 -> "€${budget / 1_000_000}M"
                budget >= 1_000 -> "€${budget / 1_000}K"
                else -> "€$budget"
            }
        }

    val totalWageBill: Int
        get() = squad.sumOf { it.wage } + reserves.sumOf { it.wage }

    val squadCount: Int
        get() = squad.size

    val isCompleteSquad: Boolean
        get() = squad.size >= 11

    val allClubPlayers: List<PlayerCard>
        get() = squad + reserves

    val teamOverall: Int
        get() {
            if (squad.isEmpty()) return 55
            val top11 = squad.take(11)
            val avg = top11.map { it.overall }.average().toInt()
            val chemistryBonus = (chemistryRating * 0.08).toInt()
            return (avg + chemistryBonus).coerceIn(45, 99)
        }

    val attackScore: Int
        get() {
            val attackers = squad.filter { it.position.category == PositionCategory.ATTACKER }
            if (attackers.isEmpty()) return 58
            return attackers.map { it.attackRating }.average().toInt()
        }

    val midfieldScore: Int
        get() {
            val mids = squad.filter { it.position.category == PositionCategory.MIDFIELDER }
            if (mids.isEmpty()) return 58
            return mids.map { it.midfieldRating }.average().toInt()
        }

    val defenseScore: Int
        get() {
            val defs = squad.filter { it.position.category == PositionCategory.DEFENDER || it.position == Position.GK }
            if (defs.isEmpty()) return 58
            return defs.map { it.defenseRating }.average().toInt()
        }

    val chemistryRating: Int
        get() {
            if (squad.size < 2) return 10
            var chem = 35
            val nationalityGroups = squad.groupBy { it.nationality }
            for ((_, group) in nationalityGroups) {
                if (group.size >= 2) chem += (group.size * 6)
            }
            val leagueGroups = squad.groupBy { it.league }
            for ((_, group) in leagueGroups) {
                if (group.size >= 2) chem += (group.size * 4)
            }
            return chem.coerceIn(10, 100)
        }
}
