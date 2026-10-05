package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ActiveAdPlayback
import com.example.model.AdRewardType
import com.example.ui.theme.*

@Composable
fun RewardedAdModal(
    adPlayback: ActiveAdPlayback?,
    onClaimReward: () -> Unit,
    onCloseAd: () -> Unit
) {
    if (adPlayback == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "ad_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ad_pulse_alpha"
    )

    Dialog(
        onDismissRequest = {
            if (adPlayback.isComplete) onCloseAd()
        },
        properties = DialogProperties(dismissOnBackPress = adPlayback.isComplete, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("rewarded_ad_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F14)),
            border = BorderStroke(2.dp, if (adPlayback.isComplete) EmeraldPitch else GoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header: Sponsor Badge & Countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x33FFFFFF)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SPONSORED VIDEO AD", fontSize = 9.sp, fontWeight = FontWeight.Black, color = GoldLight)
                        }
                    }

                    if (adPlayback.isComplete) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldPitch
                        ) {
                            Text(
                                "COMPLETED ✓",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = PitchBlack,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "${adPlayback.totalSeconds - adPlayback.currentSecond}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated High-Energy Video Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF132F20),
                                    Color(0xFF0F1E17),
                                    Color(0xFF08100C)
                                )
                            )
                        )
                        .border(1.5.dp, if (adPlayback.isComplete) EmeraldPitch else GoldPrimary.copy(alpha = pulseAlpha), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (adPlayback.rewardType.isPack) "🎁 ⚽" else "📺 ⚡",
                            fontSize = 42.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = adPlayback.sponsorName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (adPlayback.rewardType.isPack) "Unlocking Free 70–80 OVR Player Pack" else "Official Matchday Broadcast Sponsor",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Progress bar across the bottom of the video player
                    val progress = (adPlayback.currentSecond.toFloat() / adPlayback.totalSeconds.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .align(Alignment.BottomCenter),
                        color = if (adPlayback.isComplete) EmeraldPitch else GoldPrimary,
                        trackColor = Color(0x33FFFFFF)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reward description banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AD REWARD TO UNLOCK:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (adPlayback.rewardType.isPack) {
                                "⭐ FREE PACK: 70–80 RATED PLAYER"
                            } else {
                                "🪙 +€${adPlayback.rewardType.rewardCoins / 1_000_000}M COINS"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (adPlayback.rewardType.isPack) GoldLight else EmeraldPitch
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button: Claim Reward if complete, or countdown disabled button
                if (adPlayback.isComplete) {
                    Button(
                        onClick = onClaimReward,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("claim_ad_reward_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPitch,
                            contentColor = PitchBlack
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (adPlayback.rewardType.isPack) "OPEN MY FREE PACK 🎁" else "CLAIM REWARD NOW 💰",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            disabledContentColor = TextSecondaryDark
                        ),
                        border = BorderStroke(1.dp, Color(0x33FFFFFF))
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = GoldPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reward unlocks in ${adPlayback.totalSeconds - adPlayback.currentSecond}s...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
