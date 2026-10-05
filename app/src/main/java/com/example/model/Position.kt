package com.example.model

enum class Position(val code: String, val category: PositionCategory) {
    GK("GK", PositionCategory.GOALKEEPER),
    CB("CB", PositionCategory.DEFENDER),
    LB("LB", PositionCategory.DEFENDER),
    RB("RB", PositionCategory.DEFENDER),
    CDM("CDM", PositionCategory.MIDFIELDER),
    CM("CM", PositionCategory.MIDFIELDER),
    CAM("CAM", PositionCategory.MIDFIELDER),
    LW("LW", PositionCategory.ATTACKER),
    RW("RW", PositionCategory.ATTACKER),
    ST("ST", PositionCategory.ATTACKER);

    companion object {
        fun fromString(str: String): Position = entries.find { it.code.equals(str, ignoreCase = true) } ?: ST
    }
}

enum class PositionCategory(val displayName: String) {
    GOALKEEPER("Goalkeepers"),
    DEFENDER("Defenders"),
    MIDFIELDER("Midfielders"),
    ATTACKER("Attackers")
}

enum class CardTier(
    val displayName: String,
    val minRating: Int,
    val maxRating: Int,
    val glowColorHex: Long,
    val tagLabel: String
) {
    COMMON("Common Bronze", 45, 64, 0xFFCD7F32, "BRONZE"),
    UNKNOWN("Unknown Teal", 65, 74, 0xFF14B8A6, "TEAL SILVER"),
    RARE("Rare Gold", 75, 81, 0xFFFFD700, "GOLD"),
    EPIC("Epic Amethyst", 82, 89, 0xFFD946EF, "NEON PURPLE"),
    LEGENDARY("Legendary Diamond", 90, 99, 0xFF38BDF8, "HOLOGRAPHIC");

    companion object {
        fun fromRating(rating: Int): CardTier = when {
            rating >= 90 -> LEGENDARY
            rating >= 82 -> EPIC
            rating >= 75 -> RARE
            rating >= 65 -> UNKNOWN
            else -> COMMON
        }
    }
}
