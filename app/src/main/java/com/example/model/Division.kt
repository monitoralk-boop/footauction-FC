package com.example.model

enum class DivisionTier(
    val divisionNumber: Int,
    val divisionName: String,
    val minTrophies: Int,
    val maxTrophies: Int,
    val promotionRewardCoins: Long,
    val promotionSkillName: String,
    val rankBadge: String,
    val resetTargetDivision: Int
) {
    DIV_9(9, "Division 9: Grassroots", 0, 99, 3_000_000L, "Basic Ball Control", "🥉", 9),
    DIV_8(8, "Division 8: Regional Cup", 100, 199, 6_000_000L, "Sprint Acceleration", "🥉", 9),
    DIV_7(7, "Division 7: National Tier", 200, 299, 10_000_000L, "Through-Pass Vision", "🥈", 9),
    DIV_6(6, "Division 6: Challenger League", 300, 399, 15_000_000L, "Curled Finishing", "🥈", 8),
    DIV_5(5, "Division 5: Pro Conference", 400, 499, 22_000_000L, "Defensive Interception", "🥈", 8),
    DIV_4(4, "Division 4: Master League", 500, 599, 32_000_000L, "Midfield Engine", "🥇", 8),
    DIV_3(3, "Division 3: Champions Flight", 600, 699, 45_000_000L, "Acrobat Volley", "🥇", 5),
    DIV_2(2, "Division 2: Super Elite", 700, 849, 65_000_000L, "Counter-Attack Blitz", "💎", 3),
    DIV_1(1, "Division 1: Legendary Premier", 850, 9999, 100_000_000L, "Ballon d'Or Aura & Epic Icon", "👑", 3);

    val nextDivision: DivisionTier?
        get() = entries.find { it.divisionNumber == divisionNumber - 1 }

    companion object {
        fun fromTrophies(trophies: Int): DivisionTier {
            return entries.find { trophies in it.minTrophies..it.maxTrophies } ?: if (trophies >= 850) DIV_1 else DIV_9
        }

        fun fromNumber(divNum: Int): DivisionTier {
            return entries.find { it.divisionNumber == divNum } ?: DIV_9
        }
    }
}

data class SeasonStatus(
    val currentSeasonNumber: Int = 1,
    val daysRemaining: Int = 14,
    val previousDivisionNumber: Int? = null,
    val lastRewardClaimed: String? = null
)
