package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.model.MatchEventType
import com.example.model.MatchResult
import com.example.ui.theme.*

@Composable
fun MatchScoreboardModal(
    result: MatchResult,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("match_scoreboard_modal"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(2.dp, GoldPrimary),
        elevation = CardDefaults.cardElevation(16.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F2535),
                            Color(0xFF0A151E),
                            PitchBlack
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Trophy & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = GoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FULL TIME MATCH RESULT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldLight,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = result.homeTeam.avatarIcon,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.homeTeam.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "OVR ${result.homeTeam.teamOverall}",
                        fontSize = 11.sp,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Big Score Display
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(1.5.dp, CardBorderGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${result.homeScore}",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = if (result.homeScore >= result.awayScore) GoldPrimary else Color.White
                        )
                        Text(
                            text = " - ",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "${result.awayScore}",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = if (result.awayScore >= result.homeScore) GoldPrimary else Color.White
                        )
                    }
                }

                // Away Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = result.awayTeam.avatarIcon,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.awayTeam.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "OVR ${result.awayTeam.teamOverall}",
                        fontSize = 11.sp,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Result Verdict Badge
            val winner = result.winner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (winner != null) EmeraldPitch.copy(alpha = 0.2f) else AccentBlue.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, if (winner != null) EmeraldPitch else AccentBlue)
            ) {
                Text(
                    text = if (winner != null) "🏆 ${winner.name.uppercase()} WINS (+3 PTS)!" else "🤝 DRAW (+1 PT EACH)!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = if (winner != null) EmeraldPitch else AccentBlue,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Match Stats
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "MATCH STATS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    StatCompareRow(label = "Possession", home = "${result.homePossession}%", away = "${result.awayPossession}%")
                    StatCompareRow(label = "Total Shots", home = "${result.homeShots}", away = "${result.awayShots}")
                    StatCompareRow(label = "Chemistry", home = "${result.homeTeam.chemistryRating}", away = "${result.awayTeam.chemistryRating}")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Events Ticker Timeline (up to 4 key events)
            Text(
                text = "MATCH TIMELINE HIGHLIGHTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 140.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val keyEvents = result.events.filter { it.type == MatchEventType.GOAL || it.type == MatchEventType.SAVE || it.type == MatchEventType.CHANCE }.take(4)
                for (event in keyEvents) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (event.type == MatchEventType.GOAL) GoldPrimary else Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${event.minute}'",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (event.type == MatchEventType.GOAL) PitchBlack else Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = event.description,
                            fontSize = 11.sp,
                            color = if (event.type == MatchEventType.GOAL) Color.White else TextSecondaryDark,
                            fontWeight = if (event.type == MatchEventType.GOAL) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("match_close_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = PitchBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CONTINUE TO DASHBOARD",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun StatCompareRow(label: String, home: String, away: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = home, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = label, fontSize = 11.sp, color = TextSecondaryDark)
        Text(text = away, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
