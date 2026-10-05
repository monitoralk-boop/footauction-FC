package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuctionPhase
import com.example.model.AuctionState
import com.example.model.Team
import com.example.ui.theme.*

@Composable
fun AuctionBiddingPanel(
    auctionState: AuctionState,
    currentTurnTeam: Team?,
    isHumanTurn: Boolean,
    onPlaceBid: (Long) -> Unit,
    onPass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerColor by animateColorAsState(
        targetValue = when {
            auctionState.secondsRemaining <= 3 -> AccentRed
            auctionState.secondsRemaining <= 6 -> AccentOrange
            else -> EmeraldPitch
        },
        label = "timer_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("auction_bidding_panel"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.5.dp, CardBorderGold),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CardSurfaceElevated,
                            CardSurfaceDark
                        )
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Status Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Auction Gavel",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (auctionState.phase) {
                            AuctionPhase.GOING_ONCE -> "⚡ GOING ONCE..."
                            AuctionPhase.GOING_TWICE -> "🔥 GOING TWICE..."
                            AuctionPhase.SOLD -> "🏆 SOLD!"
                            AuctionPhase.ROUND_SUMMARY -> "📋 ROUND SUMMARY"
                            else -> "🔨 ACTIVE BIDDING"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = when (auctionState.phase) {
                            AuctionPhase.GOING_ONCE -> AccentOrange
                            AuctionPhase.GOING_TWICE -> AccentRed
                            AuctionPhase.SOLD -> EmeraldPitch
                            else -> GoldPrimary
                        }
                    )
                }

                // Countdown Timer Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = timerColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, timerColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(timerColor)
                        )
                        Text(
                            text = "${auctionState.secondsRemaining}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = timerColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Highest Bid Display
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.Black.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT HIGHEST BID",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryDark,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "€${auctionState.currentHighestBid / 1_000_000}M",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary
                    )
                    Text(
                        text = "Held by: ${auctionState.highestBidderName ?: "No bids yet"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (auctionState.highestBidderName != null) Color.White else TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Turn Information
            if (currentTurnTeam != null && auctionState.phase == AuctionPhase.BIDDING) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isHumanTurn) EmeraldPitch.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (isHumanTurn) EmeraldPitch else Color(0x33FFFFFF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHumanTurn) "👉 Your Turn: ${currentTurnTeam.name}" else "⏳ Waiting for: ${currentTurnTeam.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHumanTurn) EmeraldPitch else TextSecondaryDark
                        )
                        Text(
                            text = "Budget: ${currentTurnTeam.budgetFormatted}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldLight
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bidding Buttons for Active Human Player
            AnimatedVisibility(visible = isHumanTurn && auctionState.phase == AuctionPhase.BIDDING) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // +2M
                        BidIncrementButton(
                            label = "+€2M",
                            amount = 2_000_000L,
                            currentHighest = auctionState.currentHighestBid,
                            budget = currentTurnTeam?.budget ?: 0L,
                            onClick = { onPlaceBid(auctionState.currentHighestBid + 2_000_000L) },
                            modifier = Modifier.weight(1f)
                        )
                        // +5M
                        BidIncrementButton(
                            label = "+€5M",
                            amount = 5_000_000L,
                            currentHighest = auctionState.currentHighestBid,
                            budget = currentTurnTeam?.budget ?: 0L,
                            onClick = { onPlaceBid(auctionState.currentHighestBid + 5_000_000L) },
                            modifier = Modifier.weight(1f)
                        )
                        // +10M
                        BidIncrementButton(
                            label = "+€10M",
                            amount = 10_000_000L,
                            currentHighest = auctionState.currentHighestBid,
                            budget = currentTurnTeam?.budget ?: 0L,
                            onClick = { onPlaceBid(auctionState.currentHighestBid + 10_000_000L) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pass Button
                    Button(
                        onClick = onPass,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("auction_pass_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF334155),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Pass",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PASS / DROP OUT OF ROUND",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (!isHumanTurn && auctionState.phase == AuctionPhase.BIDDING) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = GoldPrimary,
                    trackColor = Color(0x33FFFFFF)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "AI rivals considering their counter-bid...",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }
        }
    }
}

@Composable
private fun BidIncrementButton(
    label: String,
    amount: Long,
    currentHighest: Long,
    budget: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetBid = currentHighest + amount
    val canAfford = budget >= targetBid

    Button(
        onClick = onClick,
        enabled = canAfford,
        modifier = modifier
            .height(48.dp)
            .testTag("bid_btn_$label"),
        colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = PitchBlack,
            disabledContainerColor = Color(0x22FFFFFF),
            disabledContentColor = Color(0x55FFFFFF)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "€${targetBid / 1_000_000}M",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
