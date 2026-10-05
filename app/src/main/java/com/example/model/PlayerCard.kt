package com.example.model

data class PlayerCard(
    val id: String,
    val name: String,
    val overall: Int,
    val position: Position,
    val nationality: String,
    val flagEmoji: String,
    val club: String,
    val league: String,
    val tier: CardTier = CardTier.fromRating(overall),
    val pac: Int,
    val sho: Int,
    val pas: Int,
    val dri: Int,
    val def: Int,
    val phy: Int,
    val age: Int,
    val wage: Int, // In €k / week
    val marketValue: Long, // In Euros
    val isCustom: Boolean = false,
    val jerseyNumber: Int = 10,
    val imageUrl: String? = null,
    val hairstyle: String = "fade", // "fade", "curly", "locks", "buzz", "slick", "afro"
    val skinToneHex: Long = 0xFFD29B71
) {
    val wageFormatted: String
        get() = "€${wage}K/wk"

    val marketValueFormatted: String
        get() {
            return when {
                marketValue >= 1_000_000 -> "€${marketValue / 1_000_000}M"
                marketValue >= 1_000 -> "€${marketValue / 1_000}K"
                else -> "€$marketValue"
            }
        }

    val attackRating: Int
        get() = ((sho * 0.4) + (pac * 0.3) + (dri * 0.3)).toInt()

    val midfieldRating: Int
        get() = ((pas * 0.4) + (dri * 0.3) + (phy * 0.3)).toInt()

    val defenseRating: Int
        get() = ((def * 0.5) + (phy * 0.3) + (pac * 0.2)).toInt()
}
