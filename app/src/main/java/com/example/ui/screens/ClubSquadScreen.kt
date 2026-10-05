package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.model.Formation
import com.example.model.PlayerCard
import com.example.ui.components.CardSize
import com.example.ui.components.FifaPlayerCard
import com.example.ui.components.PitchFormationView
import com.example.ui.theme.*
import com.example.viewmodel.AuctionGameViewModel

@Composable
fun ClubSquadScreen(
    viewModel: AuctionGameViewModel,
    modifier: Modifier = Modifier
) {
    val club by viewModel.userClub.collectAsState()

    var managingPlayer by remember { mutableStateOf<PlayerCard?>(null) }
    var playerToSell by remember { mutableStateOf<PlayerCard?>(null) }
    var playerToListAuction by remember { mutableStateOf<PlayerCard?>(null) }
    var playerToSwapStarter by remember { mutableStateOf<PlayerCard?>(null) }
    var showAddPlayerPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Club Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${club.avatarIcon} ${club.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Overall: ${club.teamOverall} • Chemistry: ${club.chemistryRating}%",
                    fontSize = 12.sp,
                    color = GoldLight,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (club.squad.size >= 11) CardSurfaceElevated else Color(0xFF451A03),
                border = BorderStroke(1.dp, if (club.squad.size >= 11) EmeraldPitch else AccentOrange)
            ) {
                Text(
                    text = "${club.squad.size}/11 STARTERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (club.squad.size >= 11) EmeraldPitch else AccentOrange,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // INCOMPLETE SQUAD WARNING IF < 11
        if (club.squad.size < 11) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF451A03).copy(alpha = 0.6f),
                border = BorderStroke(1.dp, AccentOrange)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Squad Incomplete (${club.squad.size}/11)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentOrange
                        )
                        Text(
                            text = "You need 11 starters to play Division matches! Put players from reserves below into your Starting XI.",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Tactical Formations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (formation in Formation.entries) {
                FilterChip(
                    selected = club.formation == formation,
                    onClick = { viewModel.setClubFormation(formation) },
                    label = { Text(formation.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = PitchBlack
                    ),
                    modifier = Modifier.testTag("formation_${formation.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pitch Formation Graphic (Tapping starter opens actions, tapping empty slot opens picker)
        PitchFormationView(
            squad = club.squad,
            formation = club.formation,
            onPlayerSelected = { managingPlayer = it },
            onEmptySlotClicked = { showAddPlayerPicker = true },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Stats: Transfer Budget, Wages, Division
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(label = "Transfer Budget", value = club.budgetFormatted, color = GoldPrimary)
                MetricItem(label = "Wage Bill", value = "€${club.totalWageBill}K/wk", color = EmeraldPitch)
                MetricItem(label = "Division", value = "Div ${club.currentDivision.divisionNumber}", color = GoldLight)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ==========================================
        // SECTION 1: STARTING XI
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STARTING XI (${club.squad.size}/11)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = GoldPrimary,
                letterSpacing = 1.sp
            )
            if (club.squad.size < 11 && club.reserves.isNotEmpty()) {
                TextButton(
                    onClick = { showAddPlayerPicker = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = EmeraldPitch, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Player", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPitch)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (club.squad.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CardSurfaceDark,
                border = BorderStroke(1.dp, Color(0x22FFFFFF))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No players in Starting XI!", color = TextSecondaryDark, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showAddPlayerPicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack)
                    ) {
                        Text("Add Players from Bench", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (player in club.squad) {
                    PlayerRosterItem(
                        player = player,
                        isStarter = true,
                        onInspect = { managingPlayer = player },
                        onAction = { managingPlayer = player }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // SECTION 2: CLUB BENCH / RESERVES
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CLUB BENCH & RESERVES (${club.reserves.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = GoldLight,
                letterSpacing = 1.sp
            )
            Text(
                text = "Tap to promote or put on bid",
                fontSize = 10.sp,
                color = TextSecondaryDark
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (club.reserves.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = CardSurfaceDark,
                border = BorderStroke(1.dp, Color(0x22FFFFFF))
            ) {
                Text(
                    text = "Bench is empty. Win players in Market Auctions or open packs in the Store to build depth!",
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (player in club.reserves) {
                    PlayerRosterItem(
                        player = player,
                        isStarter = false,
                        onInspect = { managingPlayer = player },
                        onAction = {
                            if (club.squad.size < 11) {
                                viewModel.moveReserveToStarting(player)
                            } else {
                                playerToSwapStarter = player
                            }
                        }
                    )
                }
            }
        }

        // ==========================================
        // DIALOGS & ACTIONS
        // ==========================================

        // 1. Managing Player Action Dialog
        managingPlayer?.let { player ->
            val isStarter = club.squad.any { it.id == player.id }
            AlertDialog(
                onDismissRequest = { managingPlayer = null },
                title = {
                    Text(player.name, fontWeight = FontWeight.Black, color = GoldPrimary, fontSize = 18.sp)
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FifaPlayerCard(player = player, size = CardSize.LARGE)

                        Spacer(modifier = Modifier.height(4.dp))

                        if (isStarter) {
                            // Starter options: Move to Bench, Swap, List for Auction, Sell
                            Button(
                                onClick = {
                                    viewModel.moveStartingToReserves(player)
                                    managingPlayer = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Remove to Bench / Reserves", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            // Reserve options: Put in XI
                            Button(
                                onClick = {
                                    if (club.squad.size < 11) {
                                        viewModel.moveReserveToStarting(player)
                                    } else {
                                        playerToSwapStarter = player
                                    }
                                    managingPlayer = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch, contentColor = PitchBlack),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Put in Starting XI", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }

                        // List for Auction on Transfer Market
                        Button(
                            onClick = {
                                playerToListAuction = player
                                managingPlayer = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("List for Auction / Bid", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        // Quick Sell
                        OutlinedButton(
                            onClick = {
                                playerToSell = player
                                managingPlayer = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentOrange),
                            border = BorderStroke(1.dp, AccentOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quick Sell Card", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { managingPlayer = null }) {
                        Text("Close", color = TextSecondaryDark)
                    }
                },
                containerColor = CardSurfaceDark
            )
        }

        // 2. Add Player to Starting XI Picker
        if (showAddPlayerPicker) {
            AlertDialog(
                onDismissRequest = { showAddPlayerPicker = false },
                title = {
                    Text("Select Player for Starting XI", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 16.sp)
                },
                text = {
                    val availableReserves = club.reserves.filter { reserve ->
                        !club.squad.any { it.id == reserve.id || it.name.equals(reserve.name, ignoreCase = true) }
                    }
                    if (availableReserves.isEmpty()) {
                        Text("No eligible reserve players available without creating duplicates.", color = TextSecondaryDark, fontSize = 12.sp)
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableReserves) { reserve ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.moveReserveToStarting(reserve)
                                            showAddPlayerPicker = false
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.Black.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("${reserve.overall}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = GoldLight)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(reserve.position.code, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPitch)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(reserve.flagEmoji, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(reserve.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Text("+ Select", fontSize = 11.sp, fontWeight = FontWeight.Black, color = EmeraldPitch)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAddPlayerPicker = false }) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                },
                containerColor = CardSurfaceDark
            )
        }

        // 3. Swap Reserve with Starter Dialog
        playerToSwapStarter?.let { reserve ->
            AlertDialog(
                onDismissRequest = { playerToSwapStarter = null },
                title = {
                    Text("Swap ${reserve.name} Into Starting XI", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 15.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select which Starter to replace with ${reserve.name}:", fontSize = 12.sp, color = Color.White)
                        LazyColumn(
                            modifier = Modifier.height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(club.squad) { starter ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.swapStartingAndReserve(starter, reserve)
                                            playerToSwapStarter = null
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.Black.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("${starter.overall}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = GoldLight)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(starter.position.code, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPitch)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(starter.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Text("Replace", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { playerToSwapStarter = null }) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                },
                containerColor = CardSurfaceDark
            )
        }

        // 4. List for Auction Dialog
        playerToListAuction?.let { player ->
            var startBidM by remember { mutableStateOf("${(player.marketValue * 0.5 / 1_000_000).toInt().coerceAtLeast(1)}") }
            var buyNowM by remember { mutableStateOf("${(player.marketValue * 1.2 / 1_000_000).toInt().coerceAtLeast(2)}") }

            AlertDialog(
                onDismissRequest = { playerToListAuction = null },
                title = {
                    Text("Put ${player.name} on Auction", fontWeight = FontWeight.Black, color = GoldPrimary, fontSize = 16.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "List this player on the live Transfer Market! Other clubs and managers will bid on him.",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                        OutlinedTextField(
                            value = startBidM,
                            onValueChange = { startBidM = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Starting Bid (€M)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = buyNowM,
                            onValueChange = { buyNowM = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Buy-Now Price (€M)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val startBid = (startBidM.toLongOrNull() ?: 1L) * 1_000_000L
                            val buyNow = (buyNowM.toLongOrNull() ?: 2L) * 1_000_000L
                            viewModel.listPlayerOnMarket(player, startBid, buyNow)
                            playerToListAuction = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack)
                    ) {
                        Text("Put in Auction", fontWeight = FontWeight.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { playerToListAuction = null }) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                },
                containerColor = CardSurfaceDark
            )
        }

        // 5. Sell Confirmation Dialog
        playerToSell?.let { player ->
            val refund = (player.marketValue * 0.7).toLong().coerceAtLeast(1_000_000L)
            AlertDialog(
                onDismissRequest = { playerToSell = null },
                title = {
                    Text("Sell ${player.name}?", fontWeight = FontWeight.Bold, color = GoldPrimary)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Are you sure you want to sell this card to free up squad space?",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, EmeraldPitch)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙 Cash Refund: ", fontSize = 12.sp, color = TextSecondaryDark)
                                Text("+€${refund / 1_000_000}M", fontSize = 14.sp, fontWeight = FontWeight.Black, color = EmeraldPitch)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.sellPlayer(player)
                            playerToSell = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = PitchBlack)
                    ) {
                        Text("Confirm Sale")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { playerToSell = null }) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                },
                containerColor = CardSurfaceDark
            )
        }
    }
}

@Composable
private fun PlayerRosterItem(
    player: PlayerCard,
    isStarter: Boolean,
    onInspect: () -> Unit,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspect() },
        shape = RoundedCornerShape(12.dp),
        color = CardSurfaceDark,
        border = BorderStroke(1.dp, if (player.overall >= 82) Color(0xFFD946EF) else if (player.overall >= 75) CardBorderGold else Color(0x22FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (player.overall >= 80) GoldPrimary else Color(0x44FFFFFF)),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${player.overall}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (player.overall >= 80) GoldPrimary else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(player.flagEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = player.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = player.position.code,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPitch
                        )
                    }
                    Text(
                        text = "${player.club} • ${player.tier.displayName} • Value: ${player.marketValueFormatted}",
                        fontSize = 10.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onInspect,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (isStarter) "Manage" else "Put in XI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = color)
        Text(text = label, fontSize = 10.sp, color = TextSecondaryDark)
    }
}
