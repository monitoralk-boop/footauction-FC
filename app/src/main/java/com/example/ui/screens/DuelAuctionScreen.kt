package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuctionPhase
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AuctionGameViewModel

@Composable
fun DuelAuctionScreen(
    viewModel: AuctionGameViewModel,
    modifier: Modifier = Modifier
) {
    val auctionState by viewModel.duelAuctionState.collectAsState()
    val p1 by viewModel.duelPlayer1.collectAsState()
    val p2 by viewModel.duelPlayer2.collectAsState()
    val isPassAndPlay by viewModel.isDuelPassAndPlay.collectAsState()
    val isOnlineDuel by viewModel.isOnlineMultiplayerDuel.collectAsState()
    val matchResult by viewModel.duelMatchResult.collectAsState()

    var showSetupDialog by remember { mutableStateOf(false) }

    val currentTurnTeam = if (auctionState.currentTurnManagerId == p1.id) p1 else p2
    val isHumanTurn = if (isOnlineDuel) true else currentTurnTeam.isHuman

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Header Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "1v1 DUEL AUCTION",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isOnlineDuel) "🟢 Live Online Match (${p2.name})" else if (isPassAndPlay) "👥 Pass & Play (2 Players)" else "🎯 Solo Practice Match",
                        fontSize = 12.sp,
                        color = if (isOnlineDuel) EmeraldPitch else TextSecondaryDark
                    )
                }

                FilledTonalButton(
                    onClick = { showSetupDialog = true },
                    modifier = Modifier.testTag("duel_new_game_btn"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CardSurfaceElevated,
                        contentColor = GoldLight
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scoreboard Header: Both Managers' Budgets & Squad Count (X/11)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Player 1 Card
                ManagerStatusHeader(
                    team = p1,
                    isCurrentTurn = currentTurnTeam.id == p1.id && auctionState.phase == AuctionPhase.BIDDING,
                    modifier = Modifier.weight(1f)
                )
                // Player 2 Card
                ManagerStatusHeader(
                    team = p2,
                    isCurrentTurn = currentTurnTeam.id == p2.id && auctionState.phase == AuctionPhase.BIDDING,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Round Progress Bar (e.g. Round 3 of 11)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CardSurfaceDark,
                border = BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DRAFT ROUND ${auctionState.currentRound} / 11",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "${(auctionState.currentRound * 100) / 11}% COMPLETE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { auctionState.currentRound / 11f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = GoldPrimary,
                        trackColor = Color(0x22FFFFFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Player Card On Auction
            auctionState.currentPlayer?.let { player ->
                Text(
                    text = "🌟 PLAYER ON THE AUCTION BLOCK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldLight,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                FifaPlayerCard(
                    player = player,
                    size = CardSize.LARGE,
                    isHighlighted = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Bidding Control Panel
            AuctionBiddingPanel(
                auctionState = auctionState,
                currentTurnTeam = currentTurnTeam,
                isHumanTurn = isHumanTurn,
                onPlaceBid = { viewModel.placeDuelBid(it) },
                onPass = { viewModel.passDuelBid() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Round Summary Modal / Dialog when card is sold
            if (auctionState.phase == AuctionPhase.ROUND_SUMMARY) {
                RoundSummaryCard(
                    winnerCard = auctionState.winnerCard,
                    loserCard = auctionState.loserRandomCard,
                    auctionState = auctionState,
                    onContinue = { viewModel.continueToNextDuelRound() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Squad Pitch Preview of Player 1
            Text(
                text = "${p1.name}'s Squad (${p1.squad.size}/11)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            PitchFormationView(
                squad = p1.squad,
                formation = p1.formation,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Full Match Result Scoreboard Modal
        matchResult?.let { result ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                MatchScoreboardModal(
                    result = result,
                    onClose = { viewModel.closeDuelMatchModal() }
                )
            }
        }

        // Restart / Setup Game Dialog
        if (showSetupDialog) {
            DuelSetupDialog(
                onDismiss = { showSetupDialog = false },
                onStart = { passAndPlay, p1Name, p2Name ->
                    viewModel.startNewDuel(passAndPlay, p1Name, p2Name)
                    showSetupDialog = false
                }
            )
        }
    }
}

@Composable
private fun ManagerStatusHeader(
    team: com.example.model.Team,
    isCurrentTurn: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrentTurn) CardSurfaceElevated else CardSurfaceDark,
        border = BorderStroke(
            if (isCurrentTurn) 2.dp else 1.dp,
            if (isCurrentTurn) GoldPrimary else Color(0x33FFFFFF)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${team.avatarIcon} ${team.name}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                if (isCurrentTurn) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(EmeraldPitch, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = team.budgetFormatted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = GoldPrimary
            )
            Text(
                text = "Squad: ${team.squad.size}/11 • OVR ${team.teamOverall}",
                fontSize = 10.sp,
                color = TextSecondaryDark
            )
        }
    }
}

@Composable
private fun RoundSummaryCard(
    winnerCard: com.example.model.PlayerCard?,
    loserCard: com.example.model.PlayerCard?,
    auctionState: com.example.model.AuctionState,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("round_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2535)),
        border = BorderStroke(2.dp, GoldPrimary)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ROUND ${auctionState.currentRound} DRAFT RESULT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = GoldPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = auctionState.auctionMessage,
                fontSize = 12.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Winner's prize
                winnerCard?.let { card ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👑 AUCTION WINNER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FifaPlayerCard(player = card, size = CardSize.MINI)
                    }
                }

                // Loser's consolation random draft card
                loserCard?.let { card ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎲 RANDOM DRAFT PICK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPitch
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FifaPlayerCard(player = card, size = CardSize.MINI)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("continue_next_round_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = PitchBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (auctionState.currentRound >= 11) "SIMULATE FINAL MATCH!" else "CONTINUE TO NEXT ROUND",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun DuelSetupDialog(
    onDismiss: () -> Unit,
    onStart: (Boolean, String, String) -> Unit
) {
    var passAndPlay by remember { mutableStateOf(false) }
    var p1Name by remember { mutableStateOf("Player 1") }
    var p2Name by remember { mutableStateOf("Player 2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Start 1v1 Auction Duel", fontWeight = FontWeight.Bold, color = GoldPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Each manager receives €200M starting budget. Compete over 11 rounds for FIFA cards!",
                    fontSize = 12.sp,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mode: ${if (passAndPlay) "2 Friends (Pass & Play)" else "Play vs AI Rival"}", fontSize = 12.sp, color = Color.White)
                    Switch(checked = passAndPlay, onCheckedChange = { passAndPlay = it })
                }

                OutlinedTextField(
                    value = p1Name,
                    onValueChange = { p1Name = it },
                    label = { Text("Manager 1 Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (passAndPlay) {
                    OutlinedTextField(
                        value = p2Name,
                        onValueChange = { p2Name = it },
                        label = { Text("Manager 2 Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStart(passAndPlay, p1Name, p2Name) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack)
            ) {
                Text("Start Game")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}
