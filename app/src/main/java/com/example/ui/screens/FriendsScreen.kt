package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FriendProfile
import com.example.ui.theme.*
import com.example.viewmodel.AuctionGameViewModel

enum class FriendsTab(val label: String) {
    MY_FRIENDS("Friends"),
    ADD_FRIEND("Add Friend"),
    REQUESTS("Requests")
}

@Composable
fun FriendsScreen(
    viewModel: AuctionGameViewModel,
    onLaunchDuelWithFriend: (FriendProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val userClub by viewModel.userClub.collectAsState()
    val friends by viewModel.gameServerService.friends.collectAsState()
    val pendingRequests by viewModel.gameServerService.pendingRequests.collectAsState()
    val isServerOnline by viewModel.gameServerService.isServerOnline.collectAsState()

    val profile = viewModel.accountManager.getProfile()
    val userTag = profile?.managerTag ?: "${userClub.name}#${profile?.tagNumber ?: 1042}"

    var selectedTab by remember { mutableStateOf(FriendsTab.MY_FRIENDS) }
    var searchTagInput by remember { mutableStateOf("") }
    var isSendingRequest by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(16.dp)
    ) {
        // --- TOP USER PROFILE CARD (Share My Tag) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.5.dp, CardBorderGold)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF1A1A2E),
                                Color(0xFF16213E),
                                Color(0xFF0F3460)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.2f))
                                .border(1.5.dp, GoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = userClub.avatarIcon, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = userClub.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userTag,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(userTag))
                                        Toast.makeText(context, "Copied $userTag to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Tag",
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "🏆 ${userClub.trophies} Trophies • ${userClub.currentDivision.rankBadge} ${userClub.currentDivision.divisionName}",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    // Server Status Indicator
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isServerOnline) EmeraldPitch.copy(alpha = 0.2f) else AccentOrange.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (isServerOnline) EmeraldPitch else AccentOrange)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isServerOnline) EmeraldPitch else AccentOrange)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isServerOnline) "Server Live" else "Cloud Serverless",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isServerOnline) EmeraldPitch else AccentOrange
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- TAB BAR ---
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = CardSurfaceDark,
            contentColor = GoldPrimary,
            indicator = {},
            divider = {}
        ) {
            FriendsTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) GoldPrimary else TextSecondaryDark
                            )
                            if (tab == FriendsTab.REQUESTS && pendingRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(AccentOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${pendingRequests.size}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PitchBlack
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- TAB CONTENTS ---
        when (selectedTab) {
            FriendsTab.MY_FRIENDS -> {
                if (friends.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No Friends Added Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Share your Manager Tag or add friends to duel!",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { selectedTab = FriendsTab.ADD_FRIEND },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                            ) {
                                Text("Add a Friend", color = PitchBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(friends) { friend ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                                border = BorderStroke(1.dp, CardBorderGold.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1E293B)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = friend.avatarIcon, fontSize = 20.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = friend.managerName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                // Online presence dot
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (friend.isOnline) EmeraldPitch else Color.Gray)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (friend.isOnline) "Online" else "Offline",
                                                    fontSize = 9.sp,
                                                    color = if (friend.isOnline) EmeraldPitch else Color.Gray
                                                )
                                            }
                                            Text(
                                                text = "${friend.managerTag} • ${friend.clubName}",
                                                fontSize = 11.sp,
                                                color = TextSecondaryDark
                                            )
                                            Text(
                                                text = "🏆 ${friend.trophies} • ${friend.divisionName}",
                                                fontSize = 10.sp,
                                                color = GoldLight
                                            )
                                        }
                                    }

                                    // Challenge Button
                                    Button(
                                        onClick = {
                                            viewModel.gameServerService.challengeFriend(
                                                fromUserId = profile?.userId ?: "me",
                                                targetFriend = friend
                                            ) { success, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                if (success) {
                                                    onLaunchDuelWithFriend(friend)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "⚔️ Duel",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = PitchBlack
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FriendsTab.ADD_FRIEND -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, CardBorderGold)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Add Manager by Tag",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Text(
                                text = "Ask your friend for their Manager Tag (e.g., Coach#1042)",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = searchTagInput,
                                onValueChange = { searchTagInput = it },
                                label = { Text("Manager Tag (Name#ID)") },
                                placeholder = { Text("e.g. Zidane#1998") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (searchTagInput.isNotBlank()) {
                                        isSendingRequest = true
                                        viewModel.gameServerService.sendFriendRequest(
                                            fromUserId = profile?.userId ?: "me",
                                            targetTag = searchTagInput
                                        ) { ok, msg ->
                                            isSendingRequest = false
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            if (ok) searchTagInput = ""
                                        }
                                    }
                                },
                                enabled = searchTagInput.isNotBlank() && !isSendingRequest,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isSendingRequest) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PitchBlack)
                                } else {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = PitchBlack, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send Friend Request", color = PitchBlack, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // Share My Tag Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📲 Share Your Profile Tag", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Give your tag to friends so they can add you directly into their FootAuction FC club network.",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PitchBlack,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = userTag, fontWeight = FontWeight.Black, color = GoldPrimary, fontSize = 14.sp)
                                    TextButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(userTag))
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Text("Copy", color = EmeraldPitch, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FriendsTab.REQUESTS -> {
                if (pendingRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No pending friend requests", color = TextSecondaryDark, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(pendingRequests) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                                border = BorderStroke(1.dp, CardBorderGold.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = req.fromManagerName, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "${req.fromTag} • ${req.fromClub}", fontSize = 11.sp, color = TextSecondaryDark)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = {
                                                Toast.makeText(context, "Accepted ${req.fromManagerName}!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Accept", fontSize = 10.sp, color = PitchBlack, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                Toast.makeText(context, "Declined", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Decline", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
