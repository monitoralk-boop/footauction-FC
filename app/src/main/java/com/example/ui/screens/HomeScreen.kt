package com.example.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuctionType
import com.example.ui.components.CardSize
import com.example.ui.components.FifaPlayerCard
import com.example.ui.theme.*
import com.example.viewmodel.AuctionGameViewModel

@Composable
fun HomeScreen(
    viewModel: AuctionGameViewModel,
    onNavigateToDuel: () -> Unit,
    onNavigateToTournament: () -> Unit,
    onNavigateToLeagueHub: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToSquad: () -> Unit,
    onNavigateToStore: () -> Unit,
    onNavigateToFriends: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val club by viewModel.userClub.collectAsState()
    val allPlayers by viewModel.allPlayers.collectAsState()
    val waitingRoom by viewModel.waitingRoomState.collectAsState()
    val isOnlineDuel by viewModel.isOnlineMultiplayerDuel.collectAsState()
    val multiplayerStatus by viewModel.onlineMultiplayerService.connectionState.collectAsState()
    val featuredCard = allPlayers.firstOrNull { it.overall >= 92 } ?: allPlayers.first()

    LaunchedEffect(isOnlineDuel) {
        if (isOnlineDuel) {
            onNavigateToDuel()
        }
    }

    var showPlayWithFriendDialog by remember { mutableStateOf(false) }
    var showAuctionPvpChoiceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Top Header & "Play with Friend" Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚽", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FOOTAUCTION FC",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Football Bidding & Divisions",
                        fontSize = 10.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            // Quick "Play with Friend" Room Button
            Button(
                onClick = { showPlayWithFriendDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPitch,
                    contentColor = PitchBlack
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("play_with_friend_btn")
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = "Friend Room", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play with Friend", fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Room Banner if in a Room
        waitingRoom?.let { room ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = EmeraldPitch.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, EmeraldPitch)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎮 ", fontSize = 16.sp)
                        Column {
                            Text("Active Room Code: ${room.roomCode}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                            Text("Friends can join your game using this code!", fontSize = 10.sp, color = Color.White)
                        }
                    }
                    IconButton(onClick = { viewModel.dismissWaitingRoom() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Leave", tint = TextSecondaryDark)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Active Club Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, CardBorderGold)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF132F20),
                                Color(0xFF0C1F16),
                                Color(0xFF08150E)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${club.avatarIcon} ${club.name}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${club.currentDivision.rankBadge} ${club.currentDivision.divisionName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                        Text(
                            text = "🏆 ${club.trophies} Trophies • Budget: ${club.budgetFormatted}",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }

                    Button(
                        onClick = onNavigateToSquad,
                        colors = ButtonDefaults.buttonColors(containerColor = CardSurfaceElevated, contentColor = GoldLight),
                        border = BorderStroke(1.dp, CardBorderGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("My Squad", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // GAME SECTION: AUCTION PVP vs DIVISIONS LEAGUE
        Text(
            text = "SELECT PLAY DESTINATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = GoldLight,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // DESTINATION 1: AUCTION PVP
        PrimaryHubCard(
            title = "AUCTION PVP",
            subtitle = "Live Bidding War • 1v1 Duel or Tournament",
            description = "Draft 11 players in a live auction bidding war against a friend (Pass & Play) or AI rival, or compete in 3-8 manager tournaments!",
            icon = Icons.Default.Gavel,
            accentColor = GoldPrimary,
            tag = "pvp_auction_card",
            onClick = { showAuctionPvpChoiceDialog = true }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // DESTINATION 2: DIVISIONS LEAGUE
        PrimaryHubCard(
            title = "DIVISIONS LEAGUE",
            subtitle = "Climb Division 9 to Division 1 • 45s Live Matches",
            description = "Lead your grassroots starter squad up the ranks! Play 45s live matches, earn trophies, claim promotion gifts, and survive the 2-week season renewal!",
            icon = Icons.Default.EmojiEvents,
            accentColor = EmeraldPitch,
            tag = "divisions_league_card",
            onClick = onNavigateToLeagueHub
        )

        Spacer(modifier = Modifier.height(14.dp))

        // DESTINATION 3: FRIENDS & COMMUNITY
        PrimaryHubCard(
            title = "FRIENDS & COMMUNITY",
            subtitle = "Online Profiles • 1v1 Direct Challenges",
            description = "Connect with fellow managers using unique Profile Tags. See who is online, add friends, and issue instant 1v1 Auction Duel challenges!",
            icon = Icons.Default.People,
            accentColor = AccentBlue,
            tag = "friends_community_card",
            onClick = onNavigateToFriends
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SPOTLIGHT CARD
        Text(
            text = "🌟 SPOTLIGHT SUPERSTAR CARD",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = GoldLight,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        FifaPlayerCard(player = featuredCard, size = CardSize.LARGE, isHighlighted = true)

        Spacer(modifier = Modifier.height(16.dp))
    }

    // AUCTION PVP SELECTION MODAL
    if (showAuctionPvpChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showAuctionPvpChoiceDialog = false },
            title = {
                Text("Choose Auction PvP Mode", fontWeight = FontWeight.Black, color = GoldPrimary, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Select how you want to play the Auction Bidding War:",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )

                    // 1v1 Duel Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAuctionPvpChoiceDialog = false
                                onNavigateToDuel()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, CardBorderGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚔️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("1v1 Duel (Between 2)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("11 bidding rounds. Highest bidder wins player, other gets random card, then match simulation!", fontSize = 10.sp, color = TextSecondaryDark)
                            }
                        }
                    }

                    // Tournament Option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAuctionPvpChoiceDialog = false
                                onNavigateToTournament()
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, AccentBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏆", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Tournois (3 to 8 Players)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Multi-manager bidding until all have 11 players. Tournament fixtures decide champion!", fontSize = 10.sp, color = TextSecondaryDark)
                            }
                        }
                    }

                    // Invite Friend to Room
                    Button(
                        onClick = {
                            showAuctionPvpChoiceDialog = false
                            showPlayWithFriendDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch, contentColor = PitchBlack),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Invite")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Room & Invite Friend", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAuctionPvpChoiceDialog = false }) {
                    Text("Close", color = TextSecondaryDark)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    // PLAY WITH FRIEND MODE SELECTION & JOIN DIALOG
    if (showPlayWithFriendDialog) {
        var inputCode by remember { mutableStateOf("") }
        var selectedModeForJoin by remember { mutableStateOf(AuctionType.DUEL_2_PLAYERS) }

        AlertDialog(
            onDismissRequest = { showPlayWithFriendDialog = false },
            title = {
                Text("👥 Play with Friend", fontWeight = FontWeight.Black, color = EmeraldPitch, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Choose the auction mode to play with your friend. You'll enter a Waiting Room where you can invite them with a code!",
                        fontSize = 11.sp,
                        color = Color.White
                    )

                    Text("HOST A NEW ROOM (CHOOSE MODE)", fontSize = 11.sp, fontWeight = FontWeight.Black, color = GoldLight)

                    // Option 1: 1v1 Auction Duel
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPlayWithFriendDialog = false
                                viewModel.openWaitingRoom(AuctionType.DUEL_2_PLAYERS)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, GoldPrimary)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚔️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("1v1 Auction Duel", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("2 Managers bidding battle for 11 rounds + Match", fontSize = 10.sp, color = TextSecondaryDark)
                            }
                        }
                    }

                    // Option 2: Tournament (3-8 Managers)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPlayWithFriendDialog = false
                                viewModel.openWaitingRoom(AuctionType.TOURNAMENT_MULTI)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, AccentBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏆", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Tournois (3 to 8 Players)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Multiplayer drafting auction until all have 11 players", fontSize = 10.sp, color = TextSecondaryDark)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "ℹ️ Note: Divisions League is a solo club career mode and is not available for Play with Friend.",
                            fontSize = 9.sp,
                            color = TextSecondaryDark,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    HorizontalDivider(color = Color(0x33FFFFFF))

                    // Option B: Join Friend's Room
                    Text("OR JOIN FRIEND'S ROOM BY CODE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextSecondaryDark)
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it.uppercase() },
                        placeholder = { Text("Enter room code (e.g. FA-8291)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (viewModel.joinWaitingRoom(inputCode, selectedModeForJoin)) {
                                showPlayWithFriendDialog = false
                            }
                        },
                        enabled = inputCode.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch, contentColor = PitchBlack),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("JOIN ROOM & ENTER LOBBY", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPlayWithFriendDialog = false }) {
                    Text("Close", color = TextSecondaryDark)
                }
            },
            containerColor = CardSurfaceDark
        )
    }

    // WAITING ROOM / LOBBY DIALOG (Room creator presses START)
    waitingRoom?.let { room ->
        AlertDialog(
            onDismissRequest = { /* Require explicit leave/close button */ },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🎮 WAITING ROOM", fontWeight = FontWeight.Black, color = GoldPrimary, fontSize = 16.sp)
                        Text(
                            text = if (room.mode == AuctionType.DUEL_2_PLAYERS) "Mode: 1v1 Auction Duel" else "Mode: Tournois (Multi)",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (room.isHost) GoldPrimary else EmeraldPitch
                    ) {
                        Text(
                            text = if (room.isHost) "CREATOR / HOST" else "GUEST",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = PitchBlack,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Room Code Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        border = BorderStroke(1.5.dp, GoldPrimary)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SHARE INVITE CODE WITH FRIEND", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(room.roomCode, fontSize = 28.sp, fontWeight = FontWeight.Black, color = GoldPrimary, letterSpacing = 2.sp)
                            Text(multiplayerStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (multiplayerStatus.contains("Connected")) EmeraldPitch else GoldPrimary)
                        }
                    }

                    // Connected Managers List
                    Text(
                        text = "CONNECTED PLAYERS IN LOBBY (${room.players.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = TextSecondaryDark,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (playerName in room.players) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0x33FFFFFF))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(playerName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("READY 🟢", fontSize = 10.sp, fontWeight = FontWeight.Black, color = EmeraldPitch)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (room.isHost) {
                        // Room Creator presses Start: game begins!
                        Button(
                            onClick = {
                                val targetMode = room.mode
                                viewModel.startWaitingRoomGame()
                                viewModel.dismissWaitingRoom()
                                if (targetMode == AuctionType.DUEL_2_PLAYERS) {
                                    onNavigateToDuel()
                                } else {
                                    onNavigateToTournament()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_waiting_room_game_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch, contentColor = PitchBlack),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("START MATCH / AUCTION 🚀", fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        // Guest view
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F2B1D),
                            border = BorderStroke(1.dp, EmeraldPitch)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = EmeraldPitch,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Waiting for the Room Creator to press START...",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.dismissWaitingRoom() }) {
                    Text("Leave Waiting Room", color = AccentOrange)
                }
            },
            containerColor = CardSurfaceDark
        )
    }
}

@Composable
private fun PrimaryHubCard(
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.5.dp, accentColor),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, accentColor),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    lineHeight = 15.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
