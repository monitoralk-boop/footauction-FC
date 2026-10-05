package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AuctionGameViewModel

@Composable
fun TournamentAuctionScreen(
    viewModel: AuctionGameViewModel,
    modifier: Modifier = Modifier
) {
    val tourneyState by viewModel.tournamentState.collectAsState()
    val auctionState by viewModel.tournamentAuctionState.collectAsState()

    var managerCount by remember { mutableIntStateOf(4) }
    var managerNames by remember {
        mutableStateOf(
            listOf("You (Manager 1)", "Pep's Man City", "Ancelotti's Madrid", "Bayern Munich", "PSG All-Stars", "Arsenal", "Barcelona", "Liverpool")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
    ) {
        when (tourneyState.phase) {
            TournamentPhase.SETUP -> {
                TournamentSetupView(
                    managerCount = managerCount,
                    onCountChange = { managerCount = it },
                    managerNames = managerNames,
                    onNameChange = { idx, name ->
                        val updated = managerNames.toMutableList()
                        updated[idx] = name
                        managerNames = updated
                    },
                    onStart = {
                        viewModel.setupTournament(managerCount, managerNames.take(managerCount))
                    }
                )
            }
            TournamentPhase.DRAFT_AUCTION -> {
                TournamentDraftAuctionView(
                    tourneyState = tourneyState,
                    auctionState = auctionState,
                    onPlaceBid = { teamId, amount -> viewModel.placeTournamentBid(teamId, amount) },
                    onPass = { teamId -> viewModel.passTournamentBid(teamId) },
                    onContinue = { viewModel.continueToNextTournamentRound() }
                )
            }
            TournamentPhase.MATCH_STAGE, TournamentPhase.COMPLETED -> {
                TournamentMatchesView(
                    tourneyState = tourneyState,
                    onSimulateNext = { viewModel.simulateNextTournamentMatch() },
                    onRestart = { viewModel.setupTournament(managerCount, managerNames.take(managerCount)) }
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun TournamentSetupView(
    managerCount: Int,
    onCountChange: (Int) -> Unit,
    managerNames: List<String>,
    onNameChange: (Int, String) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏆 TOURNAMENT DRAFT AUCTION",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = GoldPrimary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Multi-manager bidding battle (3 to 8 players)",
            fontSize = 12.sp,
            color = TextSecondaryDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, CardBorderGold)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "RULES OF THE TOURNAMENT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• 3 to 8 managers start with €200M budget each.\n• Players are auctioned one-by-one. Highest bidder wins the card.\n• Unlike 1v1 mode, losing managers DO NOT receive random cards.\n• Auction continues until all managers have built their 11-player squads!\n• Then, the tournament kicks off: matches are simulated and the manager with the best score wins!",
                    fontSize = 12.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Select Number of Managers (3 to 8)
        Text(
            text = "NUMBER OF MANAGERS: $managerCount",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (count in 3..8) {
                FilterChip(
                    selected = managerCount == count,
                    onClick = { onCountChange(count) },
                    label = { Text("$count", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = PitchBlack
                    ),
                    modifier = Modifier.testTag("tourney_count_$count")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manager Names List
        Text(
            text = "CUSTOMIZE MANAGERS (PLAY WITH FRIENDS OR CLUBS):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondaryDark,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        for (i in 0 until managerCount) {
            OutlinedTextField(
                value = managerNames.getOrElse(i) { "Manager ${i + 1}" },
                onValueChange = { onNameChange(i, it) },
                label = { Text("Manager ${i + 1} (${if (i == 0) "You" else "Friend/Club"})") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("start_tournament_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = PitchBlack
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "START TOURNAMENT DRAFT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun TournamentDraftAuctionView(
    tourneyState: TournamentState,
    auctionState: AuctionState,
    onPlaceBid: (String, Long) -> Unit,
    onPass: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tournament Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOURNAMENT DRAFT AUCTION",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = GoldPrimary
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CardSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderGold)
            ) {
                Text(
                    text = "${tourneyState.teams.count { it.squad.size >= 11 }}/${tourneyState.teams.size} SQUADS FULL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Manager Squad Completion Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "MANAGERS SQUAD TRACKER (GOAL: 11 PLAYERS)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (team in tourneyState.teams) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = team.avatarIcon, fontSize = 20.sp)
                            Text(
                                text = team.name.take(6),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${team.squad.size}/11",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (team.squad.size >= 11) EmeraldPitch else GoldPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card On Auction
        auctionState.currentPlayer?.let { player ->
            FifaPlayerCard(
                player = player,
                size = CardSize.LARGE,
                isHighlighted = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bidding Actions / Controls
        val currentTurnTeam = tourneyState.teams.find { it.id == auctionState.currentTurnManagerId }
        val isHuman = currentTurnTeam?.isHuman == true

        AuctionBiddingPanel(
            auctionState = auctionState,
            currentTurnTeam = currentTurnTeam,
            isHumanTurn = isHuman,
            onPlaceBid = { currentTurnTeam?.let { t -> onPlaceBid(t.id, it) } },
            onPass = { currentTurnTeam?.let { t -> onPass(t.id) } },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Round Summary
        if (auctionState.phase == AuctionPhase.ROUND_SUMMARY) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2535)),
                border = BorderStroke(1.5.dp, GoldPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = auctionState.auctionMessage,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onContinue,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Next Auction", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentMatchesView(
    tourneyState: TournamentState,
    onSimulateNext: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tournament Stage Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOURNAMENT FINALS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldPrimary
                )
                Text(
                    text = if (tourneyState.phase == TournamentPhase.COMPLETED) "👑 TOURNAMENT FINISHED" else "Fixture ${tourneyState.currentMatchIndex + 1} of ${tourneyState.matches.size}",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated, contentColor = GoldLight),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("New Tourney", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Champion Banner if Finished
        tourneyState.championTeam?.let { champion ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2413)),
                border = BorderStroke(2.dp, GoldPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🏆 TOURNAMENT CHAMPION 🏆", fontSize = 16.sp, fontWeight = FontWeight.Black, color = GoldPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${champion.avatarIcon} ${champion.name}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(text = "${champion.tournamentPoints} PTS • ${champion.matchWins} WINS", fontSize = 12.sp, color = EmeraldPitch, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Leaderboard Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, CardBorderGold)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "LEADERBOARD STANDINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Table Header
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("#  Club", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark, modifier = Modifier.weight(2f))
                    Text("W", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("D", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("L", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("PTS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = GoldPrimary, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                }

                HorizontalDivider(color = Color(0x33FFFFFF), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                val sortedTeams = tourneyState.teams.sortedWith(
                    compareByDescending<Team> { it.tournamentPoints }
                        .thenByDescending { it.goalsFor - it.goalsAgainst }
                )

                sortedTeams.forEachIndexed { index, team ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. ${team.avatarIcon} ${team.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(2f),
                            maxLines = 1
                        )
                        Text(text = "${team.matchWins}", fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                        Text(text = "${team.matchDraws}", fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                        Text(text = "${team.matchLosses}", fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                        Text(text = "${team.tournamentPoints}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = GoldPrimary, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Match Simulation Action
        if (tourneyState.phase != TournamentPhase.COMPLETED) {
            Button(
                onClick = onSimulateNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("simulate_next_match_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.SportsSoccer, contentDescription = "Play")
                Spacer(modifier = Modifier.width(6.dp))
                Text("SIMULATE NEXT MATCH", fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Match List / Results
        Text(
            text = "FIXTURES & RESULTS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondaryDark,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        for (match in tourneyState.matches) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = CardSurfaceDark,
                border = BorderStroke(1.dp, if (match.isPlayed) Color(0x33FFFFFF) else CardBorderGold)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match.homeTeam.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = if (match.isPlayed) "${match.homeScore} - ${match.awayScore}" else "VS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (match.isPlayed) GoldPrimary else TextSecondaryDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = match.awayTeam.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
