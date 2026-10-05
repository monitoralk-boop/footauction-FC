package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NotificationBanner
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AuctionGameViewModel

enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    MARKET("Market", Icons.Default.Storefront),
    SQUAD("Squad", Icons.Default.SportsSoccer),
    STORE("Store", Icons.Default.CardGiftcard)
}

enum class SubScreen {
    NONE,
    DUEL,
    TOURNAMENT,
    LEAGUE_HUB,
    CARD_CREATOR,
    FRIENDS
}

class MainActivity : ComponentActivity() {

    private val viewModel: AuctionGameViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.service.RewardedAdService.getInstance().initialize(this)

        setContent {
            MyApplicationTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()

                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = { managerName, clubName, avatar ->
                            viewModel.login(managerName, clubName, avatar)
                        },
                        onFirebaseSignUp = { email, password ->
                            viewModel.accountManager.signUpWithEmail(email, password).map { it as Any }
                        },
                        onFirebaseSignIn = { email, password ->
                            viewModel.accountManager.signInWithEmail(email, password).map { it as Any }
                        }
                    )
                } else {
                    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
                    var activeSubScreen by remember { mutableStateOf(SubScreen.NONE) }
                    val activeNotification by viewModel.activeNotification.collectAsState()
                    val userClub by viewModel.userClub.collectAsState()
                    var showAccountDialog by remember { mutableStateOf(false) }

                    // Hardware gesture / back button handling
                    if (activeSubScreen != SubScreen.NONE) {
                        BackHandler {
                            activeSubScreen = SubScreen.NONE
                        }
                    } else if (selectedTab != MainTab.HOME) {
                        BackHandler {
                            selectedTab = MainTab.HOME
                        }
                    }

                    val currentTitle = when (activeSubScreen) {
                        SubScreen.DUEL -> "1v1 Auction Duel"
                        SubScreen.TOURNAMENT -> "Tournament Auction"
                        SubScreen.LEAGUE_HUB -> "Divisions League"
                        SubScreen.CARD_CREATOR -> "FIFA Card Creator"
                        SubScreen.FRIENDS -> "Friends & Community"
                        SubScreen.NONE -> selectedTab.title
                    }

                    if (showAccountDialog) {
                        AlertDialog(
                            onDismissRequest = { showAccountDialog = false },
                            title = {
                                Text("${userClub.avatarIcon} Manager Account", fontWeight = FontWeight.Black, color = GoldPrimary)
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Club Name: ${userClub.name}", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Budget: ${userClub.budgetFormatted}", color = EmeraldPitch)
                                    Text("Squad Size: ${userClub.squad.size} Players (60-70+ OVR)", color = Color.White)
                                    Text("Reserves: ${userClub.reserves.size} Players", color = TextSecondaryDark)
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showAccountDialog = false
                                        viewModel.logout()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                                ) {
                                    Text("Switch Account / Logout", color = PitchBlack, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAccountDialog = false }) {
                                    Text("Close", color = TextSecondaryDark)
                                }
                            },
                            containerColor = CardSurfaceDark
                        )
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = PitchBlack,
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = currentTitle,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            color = GoldPrimary
                                        )
                                    }
                                },
                                navigationIcon = {
                                    if (activeSubScreen != SubScreen.NONE) {
                                        IconButton(
                                            onClick = { activeSubScreen = SubScreen.NONE },
                                            modifier = Modifier.testTag("nav_back_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    // Friends Button
                                    if (activeSubScreen != SubScreen.FRIENDS) {
                                        IconButton(
                                            onClick = { activeSubScreen = SubScreen.FRIENDS },
                                            modifier = Modifier.testTag("top_friends_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.People,
                                                contentDescription = "Friends",
                                                tint = GoldPrimary
                                            )
                                        }
                                    }

                                    // Profile Button
                                    IconButton(onClick = { showAccountDialog = true }) {
                                        Text(text = userClub.avatarIcon, fontSize = 20.sp)
                                    }

                                    if (selectedTab != MainTab.STORE && activeSubScreen == SubScreen.NONE) {
                                        IconButton(
                                            onClick = { selectedTab = MainTab.STORE },
                                            modifier = Modifier.testTag("top_store_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CardGiftcard,
                                                contentDescription = "Store",
                                                tint = GoldPrimary
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = CardSurfaceDark,
                                    titleContentColor = GoldPrimary
                                )
                            )
                        },
                    // 4 Bottom Navigation Bar Items: Home / Market / Squad / Store
                    bottomBar = {
                        NavigationBar(
                            containerColor = CardSurfaceDark,
                            contentColor = GoldPrimary
                        ) {
                            MainTab.entries.forEach { tab ->
                                val isSelected = (activeSubScreen == SubScreen.NONE && selectedTab == tab)
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        activeSubScreen = SubScreen.NONE
                                        selectedTab = tab
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PitchBlack,
                                        selectedTextColor = GoldPrimary,
                                        indicatorColor = GoldPrimary,
                                        unselectedIconColor = TextSecondaryDark,
                                        unselectedTextColor = TextSecondaryDark
                                    ),
                                    modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Global Heads-Up Notification Banner
                        NotificationBanner(notification = activeNotification)

                        // Main Content
                        Box(modifier = Modifier.fillMaxSize()) {
                            when {
                                activeSubScreen == SubScreen.DUEL -> {
                                    DuelAuctionScreen(viewModel = viewModel)
                                }
                                activeSubScreen == SubScreen.TOURNAMENT -> {
                                    TournamentAuctionScreen(viewModel = viewModel)
                                }
                                activeSubScreen == SubScreen.LEAGUE_HUB -> {
                                    LeagueHubScreen(viewModel = viewModel)
                                }
                                activeSubScreen == SubScreen.CARD_CREATOR -> {
                                    CardCreatorScreen(viewModel = viewModel)
                                }
                                activeSubScreen == SubScreen.FRIENDS -> {
                                    FriendsScreen(
                                        viewModel = viewModel,
                                        onLaunchDuelWithFriend = { friend ->
                                            viewModel.startDuelWithFriend(friend)
                                            activeSubScreen = SubScreen.DUEL
                                        }
                                    )
                                }
                                else -> {
                                    when (selectedTab) {
                                        MainTab.HOME -> HomeScreen(
                                            viewModel = viewModel,
                                            onNavigateToDuel = { activeSubScreen = SubScreen.DUEL },
                                            onNavigateToTournament = { activeSubScreen = SubScreen.TOURNAMENT },
                                            onNavigateToLeagueHub = { activeSubScreen = SubScreen.LEAGUE_HUB },
                                            onNavigateToMarket = { selectedTab = MainTab.MARKET },
                                            onNavigateToSquad = { selectedTab = MainTab.SQUAD },
                                            onNavigateToStore = { selectedTab = MainTab.STORE },
                                            onNavigateToFriends = { activeSubScreen = SubScreen.FRIENDS }
                                        )
                                        MainTab.MARKET -> LeagueMarketScreen(
                                            viewModel = viewModel
                                        )
                                        MainTab.SQUAD -> ClubSquadScreen(
                                            viewModel = viewModel
                                        )
                                        MainTab.STORE -> StoreScreen(
                                            viewModel = viewModel
                                        )
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
