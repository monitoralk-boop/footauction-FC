package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AdRewardType
import com.example.model.CardTier
import com.example.ui.components.CardSize
import com.example.ui.components.FifaPlayerCard
import com.example.ui.theme.*
import com.example.viewmodel.AuctionGameViewModel

@Composable
fun StoreScreen(
    viewModel: AuctionGameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val club by viewModel.userClub.collectAsState()
    val packCard by viewModel.lastPackCard.collectAsState()
    val hasClaimedDaily by viewModel.hasClaimedDailyGrant.collectAsState()
    val dailyAds by viewModel.dailyAdsState.collectAsState()
    val activeAd by viewModel.activeAdPlayback.collectAsState()
    val resetCountdown by viewModel.rewardedAdService.timeUntilResetFormatted.collectAsState()

    var purchaseConfirmDialog by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Store Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FOOTAUCTION STORE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Real money packs, coin bundles & daily video rewards",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CardSurfaceElevated,
                border = BorderStroke(1.dp, CardBorderGold)
            ) {
                Text(
                    text = club.budgetFormatted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DAILY ALLOWANCE (ONLY 20M FREE PER DAY)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = if (hasClaimedDaily) CardSurfaceDark else Color(0xFF0D251A),
            border = BorderStroke(1.dp, if (hasClaimedDaily) Color(0x33FFFFFF) else EmeraldPitch)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📅", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily €20M Grant",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (hasClaimedDaily) "Claimed for today ✓ (Renews in 24h)" else "Only 20M free daily allowance (No infinite money)",
                            fontSize = 10.sp,
                            color = if (hasClaimedDaily) TextSecondaryDark else EmeraldPitch
                        )
                    }
                }

                Button(
                    onClick = { viewModel.claimDailyGrant() },
                    enabled = !hasClaimedDaily,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPitch,
                        contentColor = PitchBlack,
                        disabledContainerColor = Color(0x22FFFFFF),
                        disabledContentColor = TextSecondaryDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (hasClaimedDaily) "CLAIMED" else "CLAIM €20M",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // BLOCS D'ANNONCE (REWARDED VIDEO ADS - 10M, 25M, 40M)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📺 BLOCS D'ANNONCES (VIDÉO 24H)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = GoldLight,
                letterSpacing = 1.sp
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0x22FFFFFF)
            ) {
                Text(
                    text = "RENOUVELLEMENT: $resetCountdown",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Video 1 -> 10M
            AdBlockRow(
                videoNumber = 1,
                rewardLabel = "+€10,000,000 Coins",
                isClaimed = dailyAds.video1Claimed,
                isEnabled = !dailyAds.video1Claimed,
                onWatch = { viewModel.watchRewardedAd(AdRewardType.VIDEO_1, activity) }
            )

            // Video 2 -> 25M (requires video 1)
            AdBlockRow(
                videoNumber = 2,
                rewardLabel = "+€25,000,000 Coins",
                isClaimed = dailyAds.video2Claimed,
                isEnabled = dailyAds.video1Claimed && !dailyAds.video2Claimed,
                lockHint = if (!dailyAds.video1Claimed) "Watch Video 1 first" else null,
                onWatch = { viewModel.watchRewardedAd(AdRewardType.VIDEO_2, activity) }
            )

            // Video 3 -> 40M (requires video 2)
            AdBlockRow(
                videoNumber = 3,
                rewardLabel = "+€40,000,000 Coins",
                isClaimed = dailyAds.video3Claimed,
                isEnabled = dailyAds.video2Claimed && !dailyAds.video3Claimed,
                lockHint = if (!dailyAds.video2Claimed) "Watch Video 2 first" else null,
                onWatch = { viewModel.watchRewardedAd(AdRewardType.VIDEO_3, activity) }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // SUPERSTAR PACKS (FREE AD PACK & REAL MONEY)
        // ==========================================
        Text(
            text = "⭐ PLAYER PACKS (ANNONCE GRATUITE & ACHATS)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = GoldLight,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Free Ad Pack (Random player 70-80 OVR)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("free_ad_pack_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, EmeraldPitch)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0F3B25), Color(0xFF071F14), PitchBlack)
                        )
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎁", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PACK BLOC D'ANNONCE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldPitch
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldPitch
                            ) {
                                Text(
                                    text = "FREE / GRATUIT",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PitchBlack,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Watch video to sign random 70–80 OVR player\n(Güler, Mainoo, Cubarsí, Endrick, Barcola, Tel)",
                            fontSize = 10.sp,
                            color = Color.White,
                            lineHeight = 13.sp
                        )
                    }
                }

                Button(
                    onClick = { viewModel.watchRewardedAd(AdRewardType.FREE_PACK, activity) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch, contentColor = PitchBlack),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Watch", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("FREE", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row of Gold Pack ($1.99) & Wonderkid Pack ($9.99)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Gold Pack ($1.99)
            RealPackTile(
                title = "GOLD PACK",
                rating = "75–81 Rare Gold",
                price = "$1.99",
                gradient = listOf(Color(0xFF3E3113), Color(0xFF1E1606)),
                border = GoldPrimary,
                icon = "⭐",
                modifier = Modifier.weight(1f),
                onBuy = {
                    purchaseConfirmDialog = Pair("Gold Pack for $1.99") {
                        viewModel.purchaseSuperstarPackRealMoney(CardTier.RARE, "$1.99")
                    }
                }
            )

            // Wonderkid Pack ($9.99)
            RealPackTile(
                title = "WONDERKID",
                rating = "82–89 Epic Amethyst",
                price = "$9.99",
                gradient = listOf(Color(0xFF3B0F3F), Color(0xFF1B051D)),
                border = Color(0xFFD946EF),
                icon = "🔥",
                modifier = Modifier.weight(1f),
                onBuy = {
                    purchaseConfirmDialog = Pair("Wonderkid Pack for $9.99") {
                        viewModel.purchaseSuperstarPackRealMoney(CardTier.EPIC, "$9.99")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Icon Legends Pack ($19.99)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("icon_legends_pack_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(2.dp, Color(0xFF38BDF8))
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF0C2B42),
                                Color(0xFF061826),
                                PitchBlack
                            )
                        )
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👑", fontSize = 34.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ICON LEGENDS PACK",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "Guaranteed 90–99 Holographic Diamond Legend\n(Zidane, Pelé, Ronaldinho, Mbappé, Haaland)",
                            fontSize = 10.sp,
                            color = Color.White,
                            lineHeight = 14.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        purchaseConfirmDialog = Pair("Icon Legends Pack for $19.99") {
                            viewModel.purchaseSuperstarPackRealMoney(CardTier.LEGENDARY, "$19.99")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = PitchBlack),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("$19.99", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ==========================================
        // COIN BUNDLES (REAL MONEY: $1 to $50)
        // ==========================================
        Text(
            text = "💳 PIÈCES DE CLUB (ACHAT RÉEL)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = GoldLight,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // $1.00 for 50M
            RealMoneyCoinCard(
                coinsLabel = "€50,000,000 COINS",
                price = "$1.00",
                description = "Starter transfer budget boost",
                onBuy = {
                    purchaseConfirmDialog = Pair("€50M Coins for $1.00") {
                        viewModel.purchaseCoinsRealMoney(50_000_000L, "$1.00")
                    }
                }
            )

            // $5.00 for 500M
            RealMoneyCoinCard(
                coinsLabel = "€500,000,000 COINS",
                price = "$5.00",
                description = "Build a formidable championship roster",
                isPopular = true,
                onBuy = {
                    purchaseConfirmDialog = Pair("€500M Coins for $5.00") {
                        viewModel.purchaseCoinsRealMoney(500_000_000L, "$5.00")
                    }
                }
            )

            // $15.00 for 1.5B
            RealMoneyCoinCard(
                coinsLabel = "€1,500,000,000 (1.5B) COINS",
                price = "$15.00",
                description = "Galácticos investment pack",
                onBuy = {
                    purchaseConfirmDialog = Pair("€1.5B Coins for $15.00") {
                        viewModel.purchaseCoinsRealMoney(1_500_000_000L, "$15.00")
                    }
                }
            )

            // $25.00 for 2.5B
            RealMoneyCoinCard(
                coinsLabel = "€2,500,000,000 (2.5B) COINS",
                price = "$25.00",
                description = "Elite Champions League takeover fund",
                onBuy = {
                    purchaseConfirmDialog = Pair("€2.5B Coins for $25.00") {
                        viewModel.purchaseCoinsRealMoney(2_500_000_000L, "$25.00")
                    }
                }
            )

            // $50.00 for 5B
            RealMoneyCoinCard(
                coinsLabel = "€5,000,000,000 (5B) COINS",
                price = "$50.00",
                description = "Ultimate Club Tycoon unlimited takeover fund",
                isTycoon = true,
                onBuy = {
                    purchaseConfirmDialog = Pair("€5B Coins for $50.00") {
                        viewModel.purchaseCoinsRealMoney(5_000_000_000L, "$50.00")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Purchase Confirmation Mock Sheet
        purchaseConfirmDialog?.let { (itemTitle, onConfirm) ->
            AlertDialog(
                onDismissRequest = { purchaseConfirmDialog = null },
                title = {
                    Text("Confirm In-App Purchase", fontWeight = FontWeight.Black, color = GoldPrimary, fontSize = 16.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Simulate Google Play Store Checkout:", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(itemTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Funds and cards will be directly added to your club.", fontSize = 11.sp, color = EmeraldPitch)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onConfirm()
                            purchaseConfirmDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack)
                    ) {
                        Text("Confirm & Pay")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { purchaseConfirmDialog = null }) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                },
                containerColor = CardSurfaceDark
            )
        }

        // Pack Reveal Modal
        packCard?.let { card ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissPackReveal() },
                title = null,
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 PLAYER SIGNED & TRANSFERRED!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FifaPlayerCard(player = card, size = CardSize.LARGE, isHighlighted = true)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissPackReveal() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack)
                    ) {
                        Text("ADD TO SQUAD", fontWeight = FontWeight.Black)
                    }
                },
                containerColor = CardSurfaceDark
            )
        }
    }

    // ==========================================
    // INTERACTIVE REWARDED VIDEO AD PLAYER MODAL
    // ==========================================
    activeAd?.let { ad ->
        Dialog(
            onDismissRequest = {
                if (ad.isComplete) {
                    viewModel.claimRewardedAd()
                } else {
                    viewModel.dismissAdPlayback()
                }
            },
            properties = DialogProperties(dismissOnBackPress = ad.isComplete, dismissOnClickOutside = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                color = PitchBlack,
                border = BorderStroke(2.dp, GoldPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ad Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x33FFFFFF)
                        ) {
                            Text(
                                text = "SPONSORED ADVERTISEMENT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = if (ad.isComplete) "✓ Complete" else "Reward in ${ad.totalSeconds - ad.currentSecond}s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (ad.isComplete) EmeraldPitch else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Video Player Simulated Viewport
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0x33FFFFFF))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (ad.rewardType.isPack) "⚽ FOOTBALL PACK SPOTLIGHT" else "🏆 CHAMPIONS MATCHDAY SPOTLIGHT",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = ad.sponsorName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (ad.isComplete) "🎉 Video Finished! Claim reward below." else "Playing sponsor video...",
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    val progress = if (ad.totalSeconds > 0) ad.currentSecond.toFloat() / ad.totalSeconds.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = if (ad.isComplete) EmeraldPitch else GoldPrimary,
                        trackColor = Color(0x33FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reward summary & claim button
                    if (ad.isComplete) {
                        Button(
                            onClick = { viewModel.claimRewardedAd() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch, contentColor = PitchBlack),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Claim")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (ad.rewardType) {
                                    AdRewardType.VIDEO_1 -> "CLAIM +€10,000,000 COINS"
                                    AdRewardType.VIDEO_2 -> "CLAIM +€25,000,000 COINS"
                                    AdRewardType.VIDEO_3 -> "CLAIM +€40,000,000 COINS"
                                    AdRewardType.FREE_PACK -> "OPEN FREE 70–80 OVR PACK"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.dismissAdPlayback() },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryDark)
                        ) {
                            Text("Skip Video (Forfeits Reward)", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdBlockRow(
    videoNumber: Int,
    rewardLabel: String,
    isClaimed: Boolean,
    isEnabled: Boolean,
    lockHint: String? = null,
    onWatch: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardSurfaceDark,
        border = BorderStroke(
            1.dp,
            if (isClaimed) Color(0x33FFFFFF) else if (isEnabled) GoldPrimary else Color(0x22FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (isClaimed) "✅" else "📺", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Vidéo $videoNumber ($rewardLabel)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isClaimed) TextSecondaryDark else Color.White
                    )
                    Text(
                        text = when {
                            isClaimed -> "Claimed ✓ (Renouvelle dans 24h)"
                            lockHint != null -> lockHint
                            else -> "Regarder l'annonce pour recevoir vos pièces"
                        },
                        fontSize = 10.sp,
                        color = if (isClaimed) TextSecondaryDark else if (isEnabled) EmeraldPitch else TextSecondaryDark
                    )
                }
            }

            Button(
                onClick = onWatch,
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = PitchBlack,
                    disabledContainerColor = Color(0x22FFFFFF),
                    disabledContentColor = TextSecondaryDark
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = if (isClaimed) "REÇU" else "REGARDER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun RealMoneyCoinCard(
    coinsLabel: String,
    price: String,
    description: String,
    isPopular: Boolean = false,
    isTycoon: Boolean = false,
    onBuy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardSurfaceDark,
        border = BorderStroke(
            if (isPopular || isTycoon) 1.5.dp else 1.dp,
            if (isTycoon) Color(0xFF38BDF8) else if (isPopular) GoldPrimary else Color(0x33FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isTycoon) "💎" else "🪙", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(coinsLabel, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                        if (isPopular) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GoldPrimary
                            ) {
                                Text("POPULAR", fontSize = 8.sp, fontWeight = FontWeight.Black, color = PitchBlack, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                    Text(description, fontSize = 10.sp, color = TextSecondaryDark)
                }
            }

            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTycoon) Color(0xFF38BDF8) else GoldPrimary,
                    contentColor = PitchBlack
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(price, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun RealPackTile(
    title: String,
    rating: String,
    price: String,
    gradient: List<Color>,
    border: Color,
    icon: String,
    modifier: Modifier = Modifier,
    onBuy: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, border)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(gradient))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Black, color = border)
            Text(text = rating, fontSize = 10.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = border, contentColor = PitchBlack),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(price, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
