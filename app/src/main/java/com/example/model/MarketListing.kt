package com.example.model

enum class MarketListingStatus {
    ACTIVE,
    WON,
    OUTBID,
    EXPIRED
}

enum class SortOption(val displayName: String) {
    RATING_DESC("Highest Rating"),
    PRICE_ASC("Lowest Price"),
    EXPIRING_SOON("Ending Soon"),
    WAGE_ASC("Lowest Wage")
}

data class MarketListing(
    val id: String,
    val player: PlayerCard,
    val currentBid: Long,
    val buyNowPrice: Long,
    val highestBidderName: String? = null,
    val isUserHighestBidder: Boolean = false,
    val userBidAmount: Long? = null,
    val secondsRemaining: Int,
    val bidCount: Int = 1,
    val status: MarketListingStatus = MarketListingStatus.ACTIVE,
    val isWatched: Boolean = false
) {
    val currentBidFormatted: String
        get() = "€${currentBid / 1_000_000}M"

    val buyNowFormatted: String
        get() = "€${buyNowPrice / 1_000_000}M"

    val timeLeftFormatted: String
        get() {
            val mins = secondsRemaining / 60
            val secs = secondsRemaining % 60
            return String.format("%02d:%02d", mins, secs)
        }

    val isExpiringSoon: Boolean
        get() = secondsRemaining in 1..45
}

data class MarketFilter(
    val searchQuery: String = "",
    val positionCategory: PositionCategory? = null,
    val league: String? = null,
    val nationality: String? = null,
    val minAge: Int = 17,
    val maxAge: Int = 40,
    val maxWage: Int = 600, // in €k
    val sortBy: SortOption = SortOption.RATING_DESC
)
