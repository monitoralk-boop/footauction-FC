package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerDatabase
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AuctionGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueMarketScreen(
    viewModel: AuctionGameViewModel,
    modifier: Modifier = Modifier
) {
    val club by viewModel.userClub.collectAsState()
    val allListings by viewModel.marketListings.collectAsState()
    val filter by viewModel.marketFilter.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Market, 1: Bids Dashboard
    var showFilterSheet by remember { mutableStateOf(false) }
    var inspectingPlayer by remember { mutableStateOf<PlayerCard?>(null) }
    var inspectingListing by remember { mutableStateOf<MarketListing?>(null) }

    // Filter listings based on current deep filter criteria
    val filteredListings = remember(allListings, filter) {
        allListings.filter { listing ->
            val p = listing.player
            val matchesQuery = filter.searchQuery.isEmpty() ||
                    p.name.contains(filter.searchQuery, ignoreCase = true) ||
                    p.club.contains(filter.searchQuery, ignoreCase = true) ||
                    p.nationality.contains(filter.searchQuery, ignoreCase = true)
            val matchesPos = filter.positionCategory == null || p.position.category == filter.positionCategory
            val matchesLeague = filter.league == null || filter.league == "All Leagues" || p.league.equals(filter.league, ignoreCase = true)
            val matchesAge = p.age in filter.minAge..filter.maxAge
            val matchesWage = p.wage <= filter.maxWage

            matchesQuery && matchesPos && matchesLeague && matchesAge && matchesWage
        }.sortedWith(
            when (filter.sortBy) {
                SortOption.RATING_DESC -> compareByDescending { it.player.overall }
                SortOption.PRICE_ASC -> compareBy { it.currentBid }
                SortOption.EXPIRING_SOON -> compareBy { it.secondsRemaining }
                SortOption.WAGE_ASC -> compareBy { it.player.wage }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Market Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardSurfaceDark,
                border = BorderStroke(1.dp, CardBorderGold)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TRANSFER MARKET",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Real-Time Player Bidding & Scouting",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = club.budgetFormatted,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldPrimary
                            )
                            Text(
                                text = "Budget Available",
                                fontSize = 10.sp,
                                color = EmeraldPitch
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Selector: Live Market vs Bids Dashboard
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = GoldPrimary,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Live Bidding Market", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Default.Storefront, contentDescription = "Market", modifier = Modifier.size(18.dp)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                val activeBidsCount = allListings.count { it.isUserHighestBidder || it.status == MarketListingStatus.OUTBID }
                                BadgedBox(
                                    badge = {
                                        if (activeBidsCount > 0) {
                                            Badge(containerColor = GoldPrimary, contentColor = PitchBlack) {
                                                Text("$activeBidsCount")
                                            }
                                        }
                                    }
                                ) {
                                    Text("Bids Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            icon = { Icon(Icons.Default.Gavel, contentDescription = "Bids", modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Live Transfer Market
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        // Search Bar & Filter Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = filter.searchQuery,
                                onValueChange = { viewModel.updateMarketFilter(filter.copy(searchQuery = it)) },
                                placeholder = { Text("Search player, nation, club...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondaryDark) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("market_search_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            FilledIconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("market_filter_btn"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = CardSurfaceElevated,
                                    contentColor = GoldLight
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = "Deep Filters")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips Row (All, Attacker, Midfielder, Defender, Goalkeeper)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = filter.positionCategory == null,
                                    onClick = { viewModel.updateMarketFilter(filter.copy(positionCategory = null)) },
                                    label = { Text("All") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = PitchBlack
                                    )
                                )
                            }
                            items(PositionCategory.entries) { cat ->
                                FilterChip(
                                    selected = filter.positionCategory == cat,
                                    onClick = { viewModel.updateMarketFilter(filter.copy(positionCategory = cat)) },
                                    label = { Text(cat.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = PitchBlack
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Listings Count & Active Filter indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${filteredListings.size} CARDS ON AUCTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondaryDark
                            )
                            if (filter.league != null || filter.maxWage < 600 || filter.minAge > 17 || filter.maxAge < 40) {
                                TextButton(
                                    onClick = { viewModel.updateMarketFilter(MarketFilter()) },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Reset Filters", fontSize = 11.sp, color = GoldPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Market Listings List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredListings, key = { it.id }) { listing ->
                                MarketListingCard(
                                    listing = listing,
                                    onInspect = {
                                        inspectingPlayer = listing.player
                                        inspectingListing = listing
                                    },
                                    onQuickBid = {
                                        viewModel.placeMarketBid(listing.id, listing.currentBid + 2_000_000L)
                                    },
                                    onToggleWatchlist = {
                                        viewModel.toggleWatchlist(listing.id)
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Bids Dashboard
                    BidsDashboardView(
                        allListings = allListings,
                        onInspect = { listing ->
                            inspectingPlayer = listing.player
                            inspectingListing = listing
                        },
                        onQuickBid = { listing ->
                            viewModel.placeMarketBid(listing.id, listing.currentBid + 2_000_000L)
                        }
                    )
                }
            }
        }

        // Deep Filter Bottom Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = CardSurfaceDark
            ) {
                DeepFilterSheetContent(
                    filter = filter,
                    onApply = { newFilter ->
                        viewModel.updateMarketFilter(newFilter)
                        showFilterSheet = false
                    },
                    onReset = {
                        viewModel.updateMarketFilter(MarketFilter())
                        showFilterSheet = false
                    }
                )
            }
        }

        // Player Inspection & Bidding Modal
        if (inspectingPlayer != null && inspectingListing != null) {
            val player = inspectingPlayer!!
            val listing = inspectingListing!!

            AlertDialog(
                onDismissRequest = {
                    inspectingPlayer = null
                    inspectingListing = null
                },
                title = null,
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FifaPlayerCard(player = player, size = CardSize.LARGE)
                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, CardBorderGold)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Current Highest Bid:", fontSize = 11.sp, color = TextSecondaryDark)
                                    Text(listing.currentBidFormatted, fontSize = 13.sp, fontWeight = FontWeight.Black, color = GoldPrimary)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Leader:", fontSize = 11.sp, color = TextSecondaryDark)
                                    Text(listing.highestBidderName ?: "No bids yet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Time Remaining:", fontSize = 11.sp, color = TextSecondaryDark)
                                    Text(listing.timeLeftFormatted, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (listing.isExpiringSoon) AccentRed else EmeraldPitch)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.placeMarketBid(listing.id, listing.currentBid + 2_000_000L)
                                    inspectingPlayer = null
                                    inspectingListing = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Bid +€2M", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.placeMarketBid(listing.id, listing.currentBid + 5_000_000L)
                                    inspectingPlayer = null
                                    inspectingListing = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPitch, contentColor = PitchBlack),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Bid +€5M", fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.buyNowMarketPlayer(listing.id)
                                inspectingPlayer = null
                                inspectingListing = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.5.dp, GoldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("BUY NOW FOR ${listing.buyNowFormatted}", color = GoldPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = {
                        inspectingPlayer = null
                        inspectingListing = null
                    }) {
                        Text("Close", color = TextSecondaryDark)
                    }
                },
                containerColor = CardSurfaceDark
            )
        }
    }
}

@Composable
private fun MarketListingCard(
    listing: MarketListing,
    onInspect: () -> Unit,
    onQuickBid: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("market_card_${listing.id}")
            .clickable { onInspect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(
            if (listing.isUserHighestBidder) 1.5.dp else 1.dp,
            if (listing.isUserHighestBidder) EmeraldPitch else if (listing.isExpiringSoon) AccentOrange else Color(0x33FFFFFF)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (listing.player.overall >= 80) CardBorderGold else Color(0x33FFFFFF)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${listing.player.overall}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (listing.player.overall >= 80) GoldPrimary else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = listing.player.flagEmoji, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = listing.player.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0x33FFFFFF)
                        ) {
                            Text(
                                text = listing.player.position.code,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${listing.player.club} • ${listing.player.league}",
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleWatchlist,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (listing.isWatched) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Watchlist",
                        tint = if (listing.isWatched) GoldPrimary else TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("CURRENT BID", fontSize = 9.sp, color = TextSecondaryDark, fontWeight = FontWeight.Bold)
                    Text(listing.currentBidFormatted, fontSize = 18.sp, fontWeight = FontWeight.Black, color = GoldPrimary)
                    Text(
                        text = if (listing.isUserHighestBidder) "👑 Leading (You)" else "Leader: ${listing.highestBidderName ?: "None"}",
                        fontSize = 10.sp,
                        color = if (listing.isUserHighestBidder) EmeraldPitch else TextSecondaryDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (listing.isExpiringSoon) AccentRed.copy(alpha = 0.2f) else CardSurfaceElevated,
                    border = BorderStroke(1.dp, if (listing.isExpiringSoon) AccentRed else Color(0x33FFFFFF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = if (listing.isExpiringSoon) AccentRed else EmeraldPitch,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = listing.timeLeftFormatted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (listing.isExpiringSoon) AccentRed else Color.White
                        )
                    }
                }

                Button(
                    onClick = onQuickBid,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("+€2M Bid", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun BidsDashboardView(
    allListings: List<MarketListing>,
    onInspect: (MarketListing) -> Unit,
    onQuickBid: (MarketListing) -> Unit
) {
    val activeBids = allListings.filter { it.isUserHighestBidder && it.status == MarketListingStatus.ACTIVE }
    val outbidAlerts = allListings.filter { it.status == MarketListingStatus.OUTBID }
    val wonBids = allListings.filter { it.status == MarketListingStatus.WON }
    val watchlist = allListings.filter { it.isWatched }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (outbidAlerts.isNotEmpty()) {
            item {
                Text(
                    text = "🚨 OUTBID ALERTS (RE-BID REQUIRED)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentRed
                )
            }
            items(outbidAlerts) { listing ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInspect(listing) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF38120B),
                    border = BorderStroke(1.5.dp, AccentRed)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(listing.player.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Outbid by ${listing.highestBidderName ?: "Rival"} at ${listing.currentBidFormatted}", fontSize = 11.sp, color = AccentOrange)
                        }
                        Button(
                            onClick = { onQuickBid(listing) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = PitchBlack),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Re-Bid +€2M", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "⚡ ACTIVE BIDS (${activeBids.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = GoldPrimary
            )
        }
        if (activeBids.isEmpty()) {
            item {
                Text("No active leading bids. Scout the transfer market to place bids!", fontSize = 11.sp, color = TextSecondaryDark)
            }
        } else {
            items(activeBids) { listing ->
                MarketListingCard(listing = listing, onInspect = { onInspect(listing) }, onQuickBid = { onQuickBid(listing) }, onToggleWatchlist = {})
            }
        }

        item {
            Text(
                text = "🏆 WON & SIGNED PLAYERS (${wonBids.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = EmeraldPitch
            )
        }
        if (wonBids.isNotEmpty()) {
            items(wonBids) { listing ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF092917),
                    border = BorderStroke(1.dp, EmeraldPitch)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(listing.player.flagEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(listing.player.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Signed for ${listing.currentBidFormatted} • ${listing.player.position.code} ${listing.player.overall} OVR", fontSize = 11.sp, color = EmeraldPitch)
                            }
                        }
                        Text("WON", fontSize = 12.sp, fontWeight = FontWeight.Black, color = EmeraldPitch)
                    }
                }
            }
        }

        item {
            Text(
                text = "⭐ STARRED WATCHLIST (${watchlist.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = GoldLight
            )
        }
        items(watchlist) { listing ->
            MarketListingCard(listing = listing, onInspect = { onInspect(listing) }, onQuickBid = { onQuickBid(listing) }, onToggleWatchlist = {})
        }
    }
}

@Composable
private fun DeepFilterSheetContent(
    filter: MarketFilter,
    onApply: (MarketFilter) -> Unit,
    onReset: () -> Unit
) {
    var minAge by remember { mutableFloatStateOf(filter.minAge.toFloat()) }
    var maxAge by remember { mutableFloatStateOf(filter.maxAge.toFloat()) }
    var maxWage by remember { mutableFloatStateOf(filter.maxWage.toFloat()) }
    var selectedLeague by remember { mutableStateOf(filter.league ?: "All Leagues") }
    var sortBy by remember { mutableStateOf(filter.sortBy) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("DEEP TRANSFER FILTERS", fontSize = 16.sp, fontWeight = FontWeight.Black, color = GoldPrimary)
            TextButton(onClick = onReset) {
                Text("Reset", color = TextSecondaryDark)
            }
        }

        Text("LEAGUE / TOURNAMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(PlayerDatabase.leagues) { league ->
                FilterChip(
                    selected = selectedLeague == league,
                    onClick = { selectedLeague = league },
                    label = { Text(league, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = PitchBlack
                    )
                )
            }
        }

        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("AGE RANGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark)
                Text("${minAge.toInt()} - ${maxAge.toInt()} yrs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldLight)
            }
            RangeSlider(
                value = minAge..maxAge,
                onValueChange = { range ->
                    minAge = range.start
                    maxAge = range.endInclusive
                },
                valueRange = 17f..40f,
                colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary)
            )
        }

        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MAX WEEKLY WAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark)
                Text("Up to €${maxWage.toInt()}K/wk", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPitch)
            }
            Slider(
                value = maxWage,
                onValueChange = { maxWage = it },
                valueRange = 50f..600f,
                colors = SliderDefaults.colors(thumbColor = EmeraldPitch, activeTrackColor = EmeraldPitch)
            )
        }

        Text("SORT RESULTS BY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(SortOption.entries) { opt ->
                FilterChip(
                    selected = sortBy == opt,
                    onClick = { sortBy = opt },
                    label = { Text(opt.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = PitchBlack
                    )
                )
            }
        }

        Button(
            onClick = {
                onApply(
                    filter.copy(
                        league = if (selectedLeague == "All Leagues") null else selectedLeague,
                        minAge = minAge.toInt(),
                        maxAge = maxAge.toInt(),
                        maxWage = maxWage.toInt(),
                        sortBy = sortBy
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("apply_deep_filter_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = PitchBlack),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("APPLY FILTERS", fontWeight = FontWeight.Black)
        }
    }
}
