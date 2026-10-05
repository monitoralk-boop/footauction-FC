package com.example.model

enum class AuctionType {
    DUEL_2_PLAYERS,
    TOURNAMENT_MULTI
}

enum class AuctionPhase {
    BIDDING,
    GOING_ONCE,
    GOING_TWICE,
    SOLD,
    ROUND_SUMMARY,
    MATCH_TIME,
    TOURNAMENT_COMPLETE
}

data class BidLogEntry(
    val managerName: String,
    val amount: Long,
    val timestamp: String,
    val isAutoPass: Boolean = false
) {
    val amountFormatted: String
        get() = "€${amount / 1_000_000}M"
}

data class AuctionState(
    val type: AuctionType = AuctionType.DUEL_2_PLAYERS,
    val phase: AuctionPhase = AuctionPhase.BIDDING,
    val currentRound: Int = 1,
    val maxRounds: Int = 11,
    val currentPlayer: PlayerCard? = null,
    val currentHighestBid: Long = 10_000_000L,
    val startingBid: Long = 10_000_000L,
    val highestBidderTeamId: String? = null,
    val highestBidderName: String? = null,
    val secondsRemaining: Int = 10,
    val activeManagersInRound: Set<String> = emptySet(), // Managers who haven't passed yet
    val currentTurnManagerId: String? = null,
    val bidHistory: List<BidLogEntry> = emptyList(),
    val winnerCard: PlayerCard? = null,
    val loserRandomCard: PlayerCard? = null,
    val auctionMessage: String = "Auction started! Place your bids."
)
