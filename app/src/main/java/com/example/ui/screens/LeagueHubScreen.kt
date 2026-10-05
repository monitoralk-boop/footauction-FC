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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DivisionTier
import com.example.model.LiveMatchState
import com.example.ui.theme.*
import com.example.viewmodel.AuctionGameViewModel

@Composable
fun LeagueHubScreen(
    viewModel: AuctionGameViewModel,
    modifier: Modifier = Modifier
) {
    val club by viewModel.userClub.collectAsState()
    val seasonStatus by viewModel.seasonStatus.collectAsState()
    val liveMatch by viewModel.liveMatchState.collectAsState()

    var showClassementModal by remember { mutableStateOf(false) }
    var showSeasonRulesModal by remember { mutableStateOf(false) }

    val currentDiv = club.currentDivision
    val nextDiv = currentDiv.nextDivision
    val currentTrophies = club.trophies
    val progressToNext = if (nextDiv != null) {
        val range = (currentDiv.maxTrophies - currentDiv.minTrophies + 1).toFloat()
        val currentInRange = (currentTrophies - currentDiv.minTrophies).toFloat()
        (currentInRange / range).coerceIn(0f, 1f)
    } else 1f

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DIVISIONS LEAGUE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Division 9 to Division 1 Progression",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, CardBorderGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏆 ", fontSize = 12.sp)
                        Text(
                            text = "${club.trophies} Trophies",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CURRENT DIVISION BANNER
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, GoldPrimary),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF2C2210),
                                    Color(0xFF191307),
                                    PitchBlack
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = currentDiv.rankBadge, fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentDiv.divisionName.uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldPrimary
                        )
                        Text(
                            text = "${club.name} • OVR ${club.teamOverall}",
                            fontSize = 12.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Trophy Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏆 $currentTrophies Trophies",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                            if (nextDiv != null) {
                                Text(
                                    text = "Goal: ${currentDiv.maxTrophies + 1} for ${nextDiv.divisionName.split(":").first()}",
                                    fontSize = 11.sp,
                                    color = EmeraldPitch,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text("MAX DIVISION REACHED", fontSize = 11.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { progressToNext },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = GoldPrimary,
                            trackColor = Color(0x33FFFFFF)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Next Promotion Gift
                        if (nextDiv != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, Color(0x22FFFFFF))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🎁 Promotion Gift: ", fontSize = 11.sp, color = TextSecondaryDark)
                                    Text(
                                        "+€${nextDiv.promotionRewardCoins / 1_000_000}M & ${nextDiv.promotionSkillName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPitch
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2-WEEK SEASON STATUS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, CardBorderGold)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassBottom, contentDescription = "Season", tint = GoldLight, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Season ${seasonStatus.currentSeasonNumber} Active",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Renews in 14 days • Div 1 wins Epic Card!",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { showSeasonRulesModal = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Rules", tint = GoldLight)
                        }
                        IconButton(
                            onClick = { viewModel.renewSeason() },
                            modifier = Modifier.testTag("simulate_season_renewal_btn")
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = "Renew Season", tint = EmeraldPitch)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Squad validation warning if < 11 starters
            if (club.squad.size < 11) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF451A03),
                    border = BorderStroke(1.dp, AccentOrange)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Division Match Locked (${club.squad.size}/11 Starters)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = AccentOrange
                            )
                            Text(
                                text = "You must have 11 players in your Starting XI to play Division matches! Go to the Squad tab to set your starting 11.",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // PRIMARY ACTION BUTTONS: PLAY & CLASSEMENT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play Button (Disabled if squad < 11)
                Button(
                    onClick = { viewModel.startLiveDivisionMatch() },
                    enabled = club.squad.size >= 11,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(56.dp)
                        .testTag("league_play_match_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = PitchBlack,
                        disabledContainerColor = Color(0x33FFFFFF),
                        disabledContentColor = TextSecondaryDark
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = if (club.squad.size >= 11) "PLAY MATCH" else "NEED 11 PLAYERS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (club.squad.size >= 11) "45s Live Match" else "Current: ${club.squad.size}/11",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Classement Button
                Button(
                    onClick = { showClassementModal = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("league_classement_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardSurfaceElevated,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, CardBorderGold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Leaderboard, contentDescription = "Classement", tint = GoldLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CLASSEMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Match Stakes Breakdown
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CardSurfaceDark,
                border = BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MATCH RESULT STAKES IN DIVISION ${currentDiv.divisionNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StakePill(label = "WIN", score = "+30 🏆", money = "+€5M", color = EmeraldPitch)
                        StakePill(label = "DRAW", score = "+10 🏆", money = "+€2M", color = AccentBlue)
                        StakePill(label = "LOSS", score = "-10 🏆", money = "+€500K", color = AccentRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Unlocked Training Skills List
            if (club.unlockedSkills.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0D251A),
                    border = BorderStroke(1.dp, EmeraldPitch)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚡ UNLOCKED DIVISION TRAINING SKILLS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPitch
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            club.unlockedSkills.take(4).forEach { skill ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = "✓ $skill",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 45-SECOND LIVE MATCH MODAL OVERLAY
        liveMatch?.let { match ->
            LiveMatchOverlay(
                state = match,
                onSkip = { viewModel.skipLiveMatch() },
                onClose = { viewModel.dismissLiveMatch() }
            )
        }

        // CLASSEMENT / RANKING MODAL (DIVISION 9 TO 1)
        if (showClassementModal) {
            ClassementDialog(
                currentDivision = currentDiv,
                onDismiss = { showClassementModal = false }
            )
        }

        // SEASON RESET RULES MODAL
        if (showSeasonRulesModal) {
            SeasonRulesDialog(
                onDismiss = { showSeasonRulesModal = false }
            )
        }
    }
}

@Composable
private fun LiveMatchOverlay(
    state: LiveMatchState,
    onSkip: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("live_match_overlay"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(2.dp, GoldPrimary),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0E2218),
                                Color(0xFF091610),
                                PitchBlack
                            )
                        )
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live Match Header & Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (state.isFinished) EmeraldPitch else AccentRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isFinished) "FULL TIME (90')" else "LIVE MATCH (${state.matchMinute}')",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (state.isFinished) EmeraldPitch else AccentRed
                        )
                    }

                    if (!state.isFinished) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, GoldPrimary)
                        ) {
                            Text(
                                text = "${45 - state.currentRealSecond}s left",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scoreboard Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = state.homeTeam.avatarIcon, fontSize = 28.sp)
                        Text(text = state.homeTeam.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, maxLines = 1)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, CardBorderGold)
                    ) {
                        Text(
                            text = "${state.homeScore}  -  ${state.awayScore}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldPrimary,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = state.awayTeam.avatarIcon, fontSize = 28.sp)
                        Text(text = state.awayTeam.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar for the 45s simulation
                if (!state.isFinished) {
                    LinearProgressIndicator(
                        progress = { state.currentRealSecond / 45f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldPitch,
                        trackColor = Color(0x33FFFFFF)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Commentary Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color(0x22FFFFFF))
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        reverseLayout = true
                    ) {
                        items(state.commentary.reversed()) { comment ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "${comment.matchMinute}'",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (comment.isGoal) GoldPrimary else TextSecondaryDark,
                                    modifier = Modifier.width(26.dp)
                                )
                                Text(
                                    text = comment.text,
                                    fontSize = 11.sp,
                                    fontWeight = if (comment.isGoal) FontWeight.Bold else FontWeight.Normal,
                                    color = if (comment.isGoal) GoldLight else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Finished Match Rewards & Action
                if (state.isFinished) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (state.trophiesDelta > 0) EmeraldPitch.copy(alpha = 0.15f) else AccentRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (state.trophiesDelta > 0) EmeraldPitch else AccentRed)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (state.homeScore > state.awayScore) "VICTORY! 🏆" else if (state.homeScore == state.awayScore) "DRAW 🤝" else "DEFEAT ❌",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (state.trophiesDelta > 0) EmeraldPitch else AccentRed
                            )
                            Text(
                                text = "Trophies: ${if (state.trophiesDelta > 0) "+" else ""}${state.trophiesDelta} 🏆 • Coins: +€${state.coinsEarned / 1_000_000}M",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            state.promotionTriggered?.let { promo ->
                                Text(
                                    text = "🎉 PROMOTED TO ${promo.divisionName}!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("COLLECT & RETURN TO LEAGUE", fontWeight = FontWeight.Black)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onSkip) {
                            Text("Skip to Result ⏩", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StakePill(label: String, score: String, money: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = color)
            Text(score, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(money, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ClassementDialog(
    currentDivision: DivisionTier,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🏆 CLASSEMENT & DIVISION PATH", fontWeight = FontWeight.Black, color = GoldPrimary, fontSize = 16.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Climb from Division 9 to Division 1 by earning trophies in 45s live matches:",
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DivisionTier.entries) { div ->
                        val isCurrent = div == currentDivision
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) Color(0xFF2C2210) else CardSurfaceElevated,
                            border = BorderStroke(
                                if (isCurrent) 2.dp else 1.dp,
                                if (isCurrent) GoldPrimary else Color(0x22FFFFFF)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(div.rankBadge, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = div.divisionName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                                            color = if (isCurrent) GoldPrimary else Color.White
                                        )
                                        Text(
                                            text = "${div.minTrophies} - ${div.maxTrophies} 🏆",
                                            fontSize = 10.sp,
                                            color = TextSecondaryDark
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "+€${div.promotionRewardCoins / 1_000_000}M",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPitch
                                    )
                                    Text(
                                        text = div.promotionSkillName,
                                        fontSize = 9.sp,
                                        color = GoldLight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack)
            ) {
                Text("Close")
            }
        },
        containerColor = CardSurfaceDark
    )
}

@Composable
private fun SeasonRulesDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("2-WEEK SEASON RESET RULES", fontWeight = FontWeight.Black, color = GoldPrimary, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Every 2 weeks, divisions renew with the following relegation & reset schedule:",
                    fontSize = 12.sp,
                    color = Color.White
                )
                Text("• Divisions 1 & 2 ➔ Return to Division 3", fontSize = 12.sp, color = GoldLight, fontWeight = FontWeight.Bold)
                Text("• Division 3 ➔ Returns to Division 5", fontSize = 12.sp, color = Color.White)
                Text("• Divisions 4, 5, 6 ➔ Return to Division 8", fontSize = 12.sp, color = Color.White)
                Text("• Divisions 7 & 8 ➔ Return to Division 9", fontSize = 12.sp, color = Color.White)
                Text("• Division 9 ➔ Remains in Division 9", fontSize = 12.sp, color = TextSecondaryDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👑 Grand Prize: Anyone who finishes in Division 1 receives an EPIC CARD + €100,000,000 season bonus!",
                    fontSize = 12.sp,
                    color = EmeraldPitch,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack)
            ) {
                Text("Understood")
            }
        },
        containerColor = CardSurfaceDark
    )
}
