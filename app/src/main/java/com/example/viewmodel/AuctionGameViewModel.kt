package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PlayerDatabase
import com.example.model.*
import com.example.service.AccountManager
import com.example.service.GameServerService
import com.example.service.MultiplayerEvent
import com.example.service.OnlineMultiplayerService
import com.example.service.RewardedAdService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class AuctionGameViewModel(application: Application) : AndroidViewModel(application) {

    val accountManager = AccountManager(application)
    val onlineMultiplayerService = OnlineMultiplayerService.getInstance()
    val gameServerService = GameServerService.getInstance()

    private val _isLoggedIn = MutableStateFlow(accountManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isOnlineMultiplayerDuel = MutableStateFlow(false)
    val isOnlineMultiplayerDuel: StateFlow<Boolean> = _isOnlineMultiplayerDuel.asStateFlow()

    // --- PLAYERS POOL ---
    private val _allPlayers = MutableStateFlow<List<PlayerCard>>(PlayerDatabase.defaultPlayers)
    val allPlayers: StateFlow<List<PlayerCard>> = _allPlayers.asStateFlow()

    // --- USER CLUB (MODE 2) ---
    // Initialized from saved account or with unique random 60-70 rated players
    private val _userClub = MutableStateFlow(
        run {
            val savedSquad = accountManager.getSavedStarterSquad()
            val profile = accountManager.getProfile()
            if (profile != null && savedSquad != null) {
                Team(
                    id = profile.userId,
                    name = profile.clubName,
                    isHuman = true,
                    avatarIcon = profile.avatarIcon,
                    budget = 20_000_000L,
                    squad = savedSquad.first,
                    reserves = savedSquad.second,
                    formation = Formation.F_4_3_3,
                    trophies = 0
                )
            } else {
                val (starting11, reserves) = PlayerDatabase.generateRandomStarterSquad()
                Team(
                    id = "user_club_init",
                    name = "FootAuction Club",
                    isHuman = true,
                    avatarIcon = "👑",
                    budget = 20_000_000L,
                    squad = starting11,
                    reserves = reserves,
                    formation = Formation.F_4_3_3,
                    trophies = 0
                )
            }
        }
    )
    val userClub: StateFlow<Team> = _userClub.asStateFlow()

    // Daily €20M grant claim tracker (No infinite free money!)
    private val _hasClaimedDailyGrant = MutableStateFlow(false)
    val hasClaimedDailyGrant: StateFlow<Boolean> = _hasClaimedDailyGrant.asStateFlow()

    // --- DAILY REWARDED ADS (10M, 25M, 40M & FREE 70-80 PACK) ---
    private val _dailyAdsState = MutableStateFlow(DailyAdState())
    val dailyAdsState: StateFlow<DailyAdState> = _dailyAdsState.asStateFlow()

    private val _activeAdPlayback = MutableStateFlow<ActiveAdPlayback?>(null)
    val activeAdPlayback: StateFlow<ActiveAdPlayback?> = _activeAdPlayback.asStateFlow()
    private var adPlaybackJob: Job? = null

    private val _seasonStatus = MutableStateFlow(SeasonStatus(currentSeasonNumber = 1, daysRemaining = 14))
    val seasonStatus: StateFlow<SeasonStatus> = _seasonStatus.asStateFlow()

    // --- MODE 2: 45-SECOND LIVE COMMENTARY MATCH ---
    private val _liveMatchState = MutableStateFlow<LiveMatchState?>(null)
    val liveMatchState: StateFlow<LiveMatchState?> = _liveMatchState.asStateFlow()
    private var liveMatchJob: Job? = null

    // --- NOTIFICATION BANNER ---
    private val _activeNotification = MutableStateFlow<com.example.ui.components.AppNotification?>(null)
    val activeNotification: StateFlow<com.example.ui.components.AppNotification?> = _activeNotification.asStateFlow()

    // --- WAITING ROOM (PLAY WITH FRIEND) ---
    private val _waitingRoomState = MutableStateFlow<WaitingRoomState?>(null)
    val waitingRoomState: StateFlow<WaitingRoomState?> = _waitingRoomState.asStateFlow()

    // --- MODE 1: STANDALONE AUCTION PVP (1v1 DUEL) ---
    private val _duelPlayer1 = MutableStateFlow(
        Team(id = "duel_p1", name = "Manager 1 (You)", isHuman = true, avatarIcon = "🔴", budget = 200_000_000L)
    )
    val duelPlayer1: StateFlow<Team> = _duelPlayer1.asStateFlow()

    private val _duelPlayer2 = MutableStateFlow(
        Team(id = "duel_p2", name = "Manager 2 (Rival)", isHuman = false, avatarIcon = "🔵", budget = 200_000_000L)
    )
    val duelPlayer2: StateFlow<Team> = _duelPlayer2.asStateFlow()

    private val _isDuelPassAndPlay = MutableStateFlow(false)
    val isDuelPassAndPlay: StateFlow<Boolean> = _isDuelPassAndPlay.asStateFlow()

    private val _duelAuctionState = MutableStateFlow(
        AuctionState(
            type = AuctionType.DUEL_2_PLAYERS,
            currentRound = 1,
            maxRounds = 11,
            phase = AuctionPhase.BIDDING
        )
    )
    val duelAuctionState: StateFlow<AuctionState> = _duelAuctionState.asStateFlow()

    private val _duelMatchResult = MutableStateFlow<MatchResult?>(null)
    val duelMatchResult: StateFlow<MatchResult?> = _duelMatchResult.asStateFlow()

    private var duelTimerJob: Job? = null
    private var duelPool: MutableList<PlayerCard> = mutableListOf()

    // --- MODE 1: TOURNAMENT (3 to 8 MANAGERS) ---
    private val _tournamentState = MutableStateFlow(TournamentState())
    val tournamentState: StateFlow<TournamentState> = _tournamentState.asStateFlow()

    private val _tournamentAuctionState = MutableStateFlow(
        AuctionState(
            type = AuctionType.TOURNAMENT_MULTI,
            currentRound = 1,
            phase = AuctionPhase.BIDDING
        )
    )
    val tournamentAuctionState: StateFlow<AuctionState> = _tournamentAuctionState.asStateFlow()

    private var tournamentTimerJob: Job? = null
    private var tournamentPool: MutableList<PlayerCard> = mutableListOf()

    // --- TRANSFER MARKET (BIDDING MARKET) ---
    private val _marketListings = MutableStateFlow<List<MarketListing>>(emptyList())
    val marketListings: StateFlow<List<MarketListing>> = _marketListings.asStateFlow()

    private val _marketFilter = MutableStateFlow(MarketFilter())
    val marketFilter: StateFlow<MarketFilter> = _marketFilter.asStateFlow()

    private var marketTickerJob: Job? = null

    // --- STORE / PACK REVEAL ---
    private val _lastPackCard = MutableStateFlow<PlayerCard?>(null)
    val lastPackCard: StateFlow<PlayerCard?> = _lastPackCard.asStateFlow()

    val rewardedAdService = RewardedAdService.getInstance()

    init {
        initializeMarket()
        startMarketTicker()
        startNewDuel(passAndPlay = false)
        setupMultiplayerEvents()
        viewModelScope.launch {
            rewardedAdService.adSlotState.collect { state ->
                _dailyAdsState.value = state
            }
        }
    }

    private fun setupMultiplayerEvents() {
        // Observe peer connection
        viewModelScope.launch {
            onlineMultiplayerService.connectedPeerProfile.collect { peer ->
                if (peer != null) {
                    _waitingRoomState.update { room ->
                        room?.copy(
                            players = if (room.isHost) {
                                listOf("${_userClub.value.name} (Host 👑)", "${peer.managerName} (${peer.avatarIcon} 🟢 Connected)")
                            } else {
                                listOf("${peer.managerName} (Host 👑)", "${_userClub.value.name} (${_userClub.value.avatarIcon} 🟢 Connected)")
                            }
                        )
                    }
                    showNotification("Player Joined!", "${peer.managerName} (${peer.clubName}) is ready in the room!")
                }
            }
        }

        // Handle online game packets
        onlineMultiplayerService.onEventReceived = { event ->
            when (event) {
                is MultiplayerEvent.GameStarted -> {
                    val peer = onlineMultiplayerService.connectedPeerProfile.value
                    startNewOnlineDuel(event.poolPlayerCards, isHost = false, peerProfile = peer)
                }
                is MultiplayerEvent.BidPlaced -> {
                    handleRemoteBid(event.senderId, event.bidderName, event.amount)
                }
                is MultiplayerEvent.PassPlaced -> {
                    handleRemotePass(event.senderId)
                }
                is MultiplayerEvent.PeerDisconnected -> {
                    showNotification("Opponent Disconnected", event.message, isAlert = true)
                }
                else -> {}
            }
        }
    }

    fun login(managerName: String, clubName: String, avatar: String) {
        val userId = "usr_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
        val tagNumber = Random.nextInt(1000, 9999)
        val profile = UserProfile(
            userId = userId,
            managerName = managerName,
            clubName = clubName,
            avatarIcon = avatar,
            tagNumber = tagNumber
        )
        // Generate a unique random starter squad (60-70 OVR) from worldwide leagues
        val (starter11, starterReserves) = PlayerDatabase.generateRandomStarterSquad()

        accountManager.saveProfile(profile)
        accountManager.saveStarterSquad(starter11, starterReserves)

        _userClub.value = Team(
            id = userId,
            name = clubName,
            isHuman = true,
            avatarIcon = avatar,
            budget = 20_000_000L,
            squad = starter11,
            reserves = starterReserves,
            formation = Formation.F_4_3_3,
            trophies = 0
        )

        _isLoggedIn.value = true
        showNotification("Welcome $managerName!", "Your Manager Tag is ${profile.managerTag}! Drafted 11 starter players.")
    }

    fun startDuelWithFriend(friend: FriendProfile) {
        val user = _userClub.value
        _duelPlayer1.value = Team(
            id = user.id,
            name = user.name,
            isHuman = true,
            avatarIcon = user.avatarIcon,
            budget = 200_000_000L
        )
        _duelPlayer2.value = Team(
            id = friend.userId,
            name = "${friend.managerName} (${friend.clubName})",
            isHuman = false,
            avatarIcon = friend.avatarIcon,
            budget = 200_000_000L
        )
        startNewDuel(passAndPlay = false)
        showNotification("1v1 Duel Started!", "Bidding battle against ${friend.managerName} is live!")
    }

    fun logout() {
        accountManager.logout()
        _isLoggedIn.value = false
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================
    fun showNotification(title: String, message: String, isAlert: Boolean = false) {
        viewModelScope.launch {
            _activeNotification.value = com.example.ui.components.AppNotification(
                id = UUID.randomUUID().toString(),
                title = title,
                message = message,
                isAlert = isAlert
            )
            delay(4000)
            _activeNotification.value = null
        }
    }

    // ==========================================
    // PLAY WITH FRIEND: REAL ONLINE ROOM SYSTEM
    // ==========================================
    data class WaitingRoomState(
        val roomCode: String,
        val mode: AuctionType, // 1v1 Duel or Tournament
        val isHost: Boolean,
        val players: List<String>,
        val isStarted: Boolean = false
    )

    fun openWaitingRoom(mode: AuctionType) {
        val user = _userClub.value
        val profile = UserProfile(
            userId = user.id,
            managerName = user.name,
            clubName = user.name,
            avatarIcon = user.avatarIcon
        )
        val code = onlineMultiplayerService.createRoom(profile)
        _waitingRoomState.value = WaitingRoomState(
            roomCode = code,
            mode = mode,
            isHost = true,
            players = listOf("${user.name} (Host ${user.avatarIcon})", "Friend (Waiting for code: $code)...")
        )
        showNotification("Online Room Created", "Room Code: $code. Share with your friend!")
    }

    fun joinWaitingRoom(code: String, mode: AuctionType): Boolean {
        if (code.isNotBlank()) {
            val cleanCode = code.trim().uppercase()
            val user = _userClub.value
            val profile = UserProfile(
                userId = user.id,
                managerName = user.name,
                clubName = user.name,
                avatarIcon = user.avatarIcon
            )
            onlineMultiplayerService.joinRoom(profile, cleanCode)
            _waitingRoomState.value = WaitingRoomState(
                roomCode = cleanCode,
                mode = mode,
                isHost = false,
                players = listOf("Connecting to host...", "${user.name} (${user.avatarIcon} 🟢)")
            )
            showNotification("Connecting to $cleanCode", "Joined room $cleanCode! Waiting for host to start.")
            return true
        }
        return false
    }

    fun startWaitingRoomGame() {
        val current = _waitingRoomState.value ?: return
        val peer = onlineMultiplayerService.connectedPeerProfile.value
        val pool = _allPlayers.value.filter { it.overall >= 75 }.shuffled().take(15)

        if (onlineMultiplayerService.isRoomReadyToStart.value && peer != null) {
            onlineMultiplayerService.broadcastGameStart(pool)
            startNewOnlineDuel(pool, isHost = true, peerProfile = peer)
        } else {
            // Local fallback
            if (current.mode == AuctionType.DUEL_2_PLAYERS) {
                startNewDuel(passAndPlay = true, p1Name = "You (Host)", p2Name = peer?.managerName ?: "Player 2")
            } else {
                setupTournament(
                    numberOfManagers = 4,
                    managerNames = listOf("You (Host)", peer?.managerName ?: "Player 2", "Manager 3", "Manager 4")
                )
            }
        }
        _waitingRoomState.update { it?.copy(isStarted = true) }
    }

    fun dismissWaitingRoom() {
        onlineMultiplayerService.disconnect()
        _waitingRoomState.value = null
    }

    fun createCustomCard(card: PlayerCard) {
        _allPlayers.update { listOf(card) + it }
        _userClub.update { it.copy(reserves = it.reserves + card) }
        showNotification("Card Created", "Created ${card.name} (${card.overall} OVR)! Added to club reserves.")
    }

    // ==========================================
    // SQUAD MANAGEMENT (NO DUPLICATES, SWAP, REMOVE, SELL)
    // ==========================================
    fun swapStartingAndReserve(startingPlayer: PlayerCard, reservePlayer: PlayerCard) {
        val club = _userClub.value
        // Prevent duplicate by ID or Name
        if (club.squad.any { it.id != startingPlayer.id && (it.id == reservePlayer.id || it.name.equals(reservePlayer.name, ignoreCase = true)) }) {
            showNotification("Duplicate Player", "You cannot put duplicate copies of ${reservePlayer.name} in the Starting XI!", isAlert = true)
            return
        }

        val newStarting = club.squad.map { if (it.id == startingPlayer.id) reservePlayer else it }
        val newReserves = (club.reserves.filter { it.id != reservePlayer.id } + startingPlayer).distinctBy { it.id }

        _userClub.update { it.copy(squad = newStarting, reserves = newReserves) }
        showNotification("Squad Updated", "Substituted ${reservePlayer.name} into Starting 11 for ${startingPlayer.name}.")
    }

    fun moveStartingToReserves(player: PlayerCard) {
        val club = _userClub.value
        val newStarting = club.squad.filter { it.id != player.id }
        val newReserves = (club.reserves + player).distinctBy { it.id }
        _userClub.update { it.copy(squad = newStarting, reserves = newReserves) }
        showNotification(
            "Player Benched",
            "${player.name} moved to Reserves. (Starting XI: ${newStarting.size}/11${if (newStarting.size < 11) " - Needs 11 to play matches!" else ""})"
        )
    }

    fun moveReserveToStarting(player: PlayerCard) {
        val club = _userClub.value
        // Prevent duplicate by ID or Name
        if (club.squad.any { it.id == player.id || it.name.equals(player.name, ignoreCase = true) }) {
            showNotification("Duplicate Player", "You cannot put the same player (${player.name}) twice in the squad!", isAlert = true)
            return
        }

        if (club.squad.size >= 11) {
            showNotification("Starting XI Full", "Starting XI already has 11 players! Tap a starter to swap.", isAlert = true)
            return
        }

        val newReserves = club.reserves.filter { it.id != player.id }
        val newStarting = club.squad + player
        _userClub.update { it.copy(squad = newStarting, reserves = newReserves) }
        showNotification("Player In Squad", "${player.name} added to Starting XI (${newStarting.size}/11).")
    }

    fun sellPlayer(card: PlayerCard) {
        val user = _userClub.value
        val refundAmount = (card.marketValue * 0.7).toLong().coerceAtLeast(1_000_000L)
        val newStarting = user.squad.filter { it.id != card.id }
        val newReserves = user.reserves.filter { it.id != card.id }
        _userClub.update {
            it.copy(budget = it.budget + refundAmount, squad = newStarting, reserves = newReserves)
        }
        showNotification("Player Sold", "Sold ${card.name} for €${refundAmount / 1_000_000}M! (Squad: ${newStarting.size}/11)")
    }

    fun setClubFormation(formation: Formation) {
        _userClub.update { it.copy(formation = formation) }
    }

    // ==========================================
    // USER LISTS PLAYER ON THE TRANSFER MARKET
    // ==========================================
    fun listPlayerOnMarket(player: PlayerCard, startBid: Long, buyNowPrice: Long) {
        val club = _userClub.value
        // Remove from club starting XI or reserves
        val newStarting = club.squad.filter { it.id != player.id }
        val newReserves = club.reserves.filter { it.id != player.id }

        _userClub.update { it.copy(squad = newStarting, reserves = newReserves) }

        // Add to active listings on the market
        val newListing = MarketListing(
            id = "user_mkt_${player.id}_${System.currentTimeMillis()}",
            player = player,
            currentBid = startBid,
            buyNowPrice = buyNowPrice,
            highestBidderName = "Marketplace Open",
            isUserHighestBidder = false,
            secondsRemaining = 90, // 1.5 minutes live auction
            bidCount = 0
        )
        _marketListings.update { listOf(newListing) + it }
        showNotification("Player Listed!", "Put ${player.name} up for auction at €${startBid / 1_000_000}M!")
    }

    // ==========================================
    // STORE: REAL MONEY IN-APP PURCHASES & DAILY €20M GRANT
    // ==========================================
    fun claimDailyGrant() {
        if (_hasClaimedDailyGrant.value) {
            showNotification("Already Claimed", "Daily €20M grant already claimed today! Check back tomorrow.", isAlert = true)
            return
        }
        _hasClaimedDailyGrant.value = true
        _userClub.update { it.copy(budget = it.budget + 20_000_000L) }
        showNotification("Daily Reward Claimed!", "Added free €20M daily allowance to your budget!")
    }

    fun purchaseCoinsRealMoney(amountCoins: Long, priceDollar: String) {
        _userClub.update { it.copy(budget = it.budget + amountCoins) }
        showNotification("Purchase Successful ($priceDollar)", "Added €${amountCoins / 1_000_000}M coins to your club balance!")
    }

    fun purchaseSuperstarPackRealMoney(tier: CardTier, priceDollar: String) {
        val pool = when (tier) {
            CardTier.LEGENDARY -> _allPlayers.value.filter { it.tier == CardTier.LEGENDARY || it.overall >= 90 }
            CardTier.EPIC -> _allPlayers.value.filter { it.tier == CardTier.EPIC || it.overall in 82..89 }
            else -> _allPlayers.value.filter { it.tier == CardTier.RARE || it.overall in 75..81 }
        }

        // Filter out players already owned in club to prevent duplicates
        val ownedIds = _userClub.value.allClubPlayers.map { it.id }.toSet()
        val unownedPool = pool.filter { !ownedIds.contains(it.id) }
        val wonCard = unownedPool.randomOrNull() ?: pool.random()

        _lastPackCard.value = wonCard
        addPlayerToClubNoDuplicate(wonCard)
        showNotification("Pack Purchased ($priceDollar)!", "YOU SIGNED ${wonCard.name} (${wonCard.overall} OVR)!")
    }

    private fun addPlayerToClubNoDuplicate(card: PlayerCard) {
        _userClub.update { club ->
            if (club.allClubPlayers.none { it.id == card.id }) {
                if (club.squad.size < 11) {
                    club.copy(squad = club.squad + card)
                } else {
                    club.copy(reserves = club.reserves + card)
                }
            } else club
        }
    }

    fun dismissPackReveal() {
        _lastPackCard.value = null
    }

    // ==========================================
    // MODE 2: 45-SECOND LIVE COMMENTARY DIVISION MATCH
    // ==========================================
    fun startLiveDivisionMatch(): Boolean {
        val user = _userClub.value
        // Validation: squad must have at least 11 players!
        if (user.squad.size < 11) {
            showNotification("Incomplete Squad", "Division match requires 11 players in your Starting XI! You have only ${user.squad.size}.", isAlert = true)
            return false
        }

        liveMatchJob?.cancel()
        val currentDiv = user.currentDivision
        val rivalRoster = _allPlayers.value.filter { it.overall in 64..78 }.shuffled().take(11)

        val rival = Team(
            id = "div_rival_${currentDiv.divisionNumber}",
            name = "${currentDiv.divisionName.split(":").lastOrNull()?.trim() ?: "League"} Rival FC",
            avatarIcon = listOf("🛡️", "⚔️", "🔥", "🦅", "🦁").random(),
            squad = rivalRoster
        )

        val totalDurationSeconds = 45
        _liveMatchState.value = LiveMatchState(
            isLive = true,
            isFinished = false,
            homeTeam = user,
            awayTeam = rival,
            currentRealSecond = 0,
            matchMinute = 1,
            homeScore = 0,
            awayScore = 0,
            commentary = listOf(LiveCommentaryEvent(1, "The referee blows the whistle! Kick-off in Division ${currentDiv.divisionNumber}."))
        )

        liveMatchJob = viewModelScope.launch {
            var homeScore = 0
            var awayScore = 0
            val commentaryList = mutableListOf<LiveCommentaryEvent>()
            commentaryList.add(LiveCommentaryEvent(1, "Kick-off! Electric atmosphere in the stadium."))

            val expectedHomeGoals = ((user.attackScore - rival.defenseScore + 18) / 12.0).coerceIn(0.0, 4.0)
            val expectedAwayGoals = ((rival.attackScore - user.defenseScore + 18) / 12.0).coerceIn(0.0, 3.0)

            val totalHomeGoals = (expectedHomeGoals.toInt() + if (Random.nextDouble() < (expectedHomeGoals % 1)) 1 else 0).coerceAtLeast(0)
            val totalAwayGoals = (expectedAwayGoals.toInt() + if (Random.nextDouble() < (expectedAwayGoals % 1)) 1 else 0).coerceAtLeast(0)

            val goalSecondsHome = (5..42).shuffled().take(totalHomeGoals).toSet()
            val goalSecondsAway = (5..42).shuffled().filter { !goalSecondsHome.contains(it) }.take(totalAwayGoals).toSet()

            for (sec in 1..totalDurationSeconds) {
                delay(1000)
                val matchMin = (sec * 2).coerceIn(1, 90)

                if (goalSecondsHome.contains(sec)) {
                    homeScore++
                    val scorer = user.squad.filter { it.position.category == PositionCategory.ATTACKER || it.position.category == PositionCategory.MIDFIELDER }.randomOrNull()?.name ?: "Striker"
                    commentaryList.add(
                        LiveCommentaryEvent(matchMin, "⚽ GOAL for ${user.name}! $scorer finishes into the bottom corner!", isGoal = true, isHomeGoal = true)
                    )
                } else if (goalSecondsAway.contains(sec)) {
                    awayScore++
                    val scorer = rival.squad.randomOrNull()?.name ?: "Opponent Striker"
                    commentaryList.add(
                        LiveCommentaryEvent(matchMin, "⚽ GOAL for ${rival.name}! $scorer heads it in!", isGoal = true, isHomeGoal = false)
                    )
                } else if (sec % 7 == 0) {
                    val comments = listOf(
                        "🧤 Sensational diving save by the keeper to deny a certain goal!",
                        "⚡ Quick counter-attack down the right flank!",
                        "🔥 Tremendous shot whistles inches wide of the post!",
                        "🟨 Yellow card shown after a tactical foul in midfield.",
                        "🎯 Sublime passing combinations opening up space."
                    )
                    commentaryList.add(LiveCommentaryEvent(matchMin, comments.random()))
                }

                _liveMatchState.update {
                    it?.copy(
                        currentRealSecond = sec,
                        matchMinute = matchMin,
                        homeScore = homeScore,
                        awayScore = awayScore,
                        commentary = commentaryList.toList()
                    )
                }
            }

            finishLiveMatch(homeScore, awayScore)
        }
        return true
    }

    fun skipLiveMatch() {
        liveMatchJob?.cancel()
        val cur = _liveMatchState.value ?: return
        if (cur.isFinished) return

        val user = cur.homeTeam
        val rival = cur.awayTeam
        val homeScore = (Random.nextInt(1, 4) + (if (user.teamOverall > rival.teamOverall) 1 else 0)).coerceAtLeast(0)
        val awayScore = Random.nextInt(0, 3)

        finishLiveMatch(homeScore, awayScore)
    }

    private fun finishLiveMatch(homeScore: Int, awayScore: Int) {
        val user = _userClub.value
        val oldDivision = user.currentDivision

        val (trophyDelta, coinsEarned) = when {
            homeScore > awayScore -> Pair(30, 5_000_000L)
            homeScore == awayScore -> Pair(10, 2_000_000L)
            else -> Pair(-10, 500_000L)
        }

        val newTrophies = (user.trophies + trophyDelta).coerceAtLeast(0)
        val newDivision = DivisionTier.fromTrophies(newTrophies)

        var promotionCelebration: DivisionTier? = null
        var rewardCoins = coinsEarned

        if (newDivision.divisionNumber < oldDivision.divisionNumber) {
            promotionCelebration = newDivision
            rewardCoins += newDivision.promotionRewardCoins
            showNotification(
                "PROMOTION CELEBRATION! 🏆",
                "Promoted to ${newDivision.divisionName}! Gift: +€${newDivision.promotionRewardCoins / 1_000_000}M & Skill: ${newDivision.promotionSkillName}!"
            )
        }

        _userClub.update {
            it.copy(
                budget = it.budget + rewardCoins,
                trophies = newTrophies,
                matchWins = it.matchWins + (if (homeScore > awayScore) 1 else 0),
                matchDraws = it.matchDraws + (if (homeScore == awayScore) 1 else 0),
                matchLosses = it.matchLosses + (if (homeScore < awayScore) 1 else 0),
                unlockedSkills = if (promotionCelebration != null) it.unlockedSkills + promotionCelebration.promotionSkillName else it.unlockedSkills
            )
        }

        val endComments = _liveMatchState.value?.commentary.orEmpty() +
                LiveCommentaryEvent(90, "🏁 Full-Time Whistle! Final score: ${user.name} $homeScore - $awayScore ${_liveMatchState.value?.awayTeam?.name}.")

        _liveMatchState.update {
            it?.copy(
                isFinished = true,
                homeScore = homeScore,
                awayScore = awayScore,
                trophiesDelta = trophyDelta,
                coinsEarned = rewardCoins,
                promotionTriggered = promotionCelebration,
                commentary = endComments
            )
        }
    }

    fun dismissLiveMatch() {
        liveMatchJob?.cancel()
        _liveMatchState.value = null
    }

    // ==========================================
    // 2-WEEK SEASON RENEWAL / SOFT RESET
    // ==========================================
    fun renewSeason() {
        val user = _userClub.value
        val currentDiv = user.currentDivision.divisionNumber
        var epicCardReward: PlayerCard? = null
        var rewardMsg = ""

        val (resetDivNumber, resetTrophies) = when (currentDiv) {
            1 -> {
                epicCardReward = _allPlayers.value.find { it.tier == CardTier.LEGENDARY } ?: _allPlayers.value.first()
                rewardMsg = "Division 1 Champion! Received Epic ${epicCardReward.name} + €100M!"
                Pair(3, 600)
            }
            2 -> {
                rewardMsg = "Division 2 finish! Relegated to Division 3 (+€30M bonus)."
                Pair(3, 600)
            }
            3 -> {
                rewardMsg = "Division 3 finish! Relegated to Division 5."
                Pair(5, 400)
            }
            4, 5, 6 -> {
                rewardMsg = "Division $currentDiv finish! Relegated to Division 8."
                Pair(8, 100)
            }
            7, 8 -> {
                rewardMsg = "Division $currentDiv finish! Reset to Division 9."
                Pair(9, 0)
            }
            else -> {
                rewardMsg = "Division 9 remains in Division 9."
                Pair(9, 0)
            }
        }

        val bonusMoney = if (currentDiv == 1) 100_000_000L else if (currentDiv == 2) 30_000_000L else 5_000_000L

        _userClub.update {
            val updatedSquad = if (epicCardReward != null && it.allClubPlayers.none { p -> p.id == epicCardReward.id }) {
                it.squad + epicCardReward
            } else it.squad
            it.copy(
                budget = it.budget + bonusMoney,
                trophies = resetTrophies,
                squad = updatedSquad
            )
        }

        val newSeasonNumber = _seasonStatus.value.currentSeasonNumber + 1
        _seasonStatus.value = SeasonStatus(
            currentSeasonNumber = newSeasonNumber,
            daysRemaining = 14,
            previousDivisionNumber = currentDiv,
            lastRewardClaimed = rewardMsg
        )

        showNotification("Season $newSeasonNumber Renewed!", rewardMsg)
    }

    // ==========================================
    // REWARDED VIDEO ADS (10M, 25M, 40M & FREE PACK)
    // ==========================================
    fun watchRewardedAd(type: AdRewardType, activity: Activity? = null) {
        if (activity != null && rewardedAdService.isAdReady.value) {
            rewardedAdService.showRewardedAd(
                activity = activity,
                slotType = type,
                onRewardEarned = { slotType ->
                    grantRewardDirectly(slotType)
                },
                onFallbackSimulation = { slotType ->
                    startSimulatedAdPlayback(slotType)
                }
            )
        } else {
            startSimulatedAdPlayback(type)
        }
    }

    private fun startSimulatedAdPlayback(type: AdRewardType) {
        adPlaybackJob?.cancel()
        val sponsor = when (type) {
            AdRewardType.VIDEO_1 -> "FootAuction Kickoff TV Sponsor"
            AdRewardType.VIDEO_2 -> "World Championship League Broadcast"
            AdRewardType.VIDEO_3 -> "Galácticos Global Matchday Partner"
            AdRewardType.FREE_PACK -> "EA FC Pro Card Showcase"
        }

        val totalSeconds = 4
        _activeAdPlayback.value = ActiveAdPlayback(
            rewardType = type,
            sponsorName = sponsor,
            totalSeconds = totalSeconds,
            currentSecond = 0,
            isComplete = false
        )

        adPlaybackJob = viewModelScope.launch {
            for (sec in 1..totalSeconds) {
                delay(1000)
                _activeAdPlayback.update {
                    it?.copy(
                        currentSecond = sec,
                        isComplete = sec >= totalSeconds
                    )
                }
            }
        }
    }

    fun dismissAdPlayback() {
        adPlaybackJob?.cancel()
        _activeAdPlayback.value = null
    }

    fun claimRewardedAd() {
        val ad = _activeAdPlayback.value ?: return
        if (!ad.isComplete) return
        grantRewardDirectly(ad.rewardType)
        _activeAdPlayback.value = null
    }

    private fun grantRewardDirectly(slotType: AdRewardType) {
        rewardedAdService.claimSlotReward(slotType)
        when (slotType) {
            AdRewardType.VIDEO_1 -> {
                _userClub.update { it.copy(budget = it.budget + 10_000_000L) }
                _dailyAdsState.update { it.copy(video1Claimed = true) }
                showNotification("Ad Reward Claimed!", "Added +€10M Coins to your club budget!")
            }
            AdRewardType.VIDEO_2 -> {
                _userClub.update { it.copy(budget = it.budget + 25_000_000L) }
                _dailyAdsState.update { it.copy(video2Claimed = true) }
                showNotification("Ad Reward Claimed!", "Added +€25M Coins to your club budget!")
            }
            AdRewardType.VIDEO_3 -> {
                _userClub.update { it.copy(budget = it.budget + 40_000_000L) }
                _dailyAdsState.update { it.copy(video3Claimed = true) }
                showNotification("Ad Reward Claimed!", "Added +€40M Coins to your club budget!")
            }
            AdRewardType.FREE_PACK -> {
                // Guaranteed random player between 70 and 80 OVR
                val candidatePool = PlayerDatabase.adPackPlayers70To80.ifEmpty {
                    PlayerDatabase.defaultPlayers.filter { it.overall in 70..80 }
                }
                val wonCard = candidatePool.random()
                addPlayerToClubNoDuplicate(wonCard)
                _lastPackCard.value = wonCard
                showNotification("Free Pack Opened!", "You packed ${wonCard.name} (${wonCard.overall} OVR)! Added to club reserves.")
            }
        }
    }

    // ==========================================
    // TRANSFER MARKET (CLEAN MARKET - NO FAKE BOTS)
    // ==========================================
    private fun initializeMarket() {
        val listings = _allPlayers.value.filter { it.overall >= 68 }.take(25).mapIndexed { idx, player ->
            val startBid = (player.marketValue * 0.35).toLong().coerceAtLeast(800_000L)
            val buyNow = (player.marketValue * 1.25).toLong()
            val secs = 60 + (idx * 40)
            MarketListing(
                id = "mkt_${player.id}",
                player = player,
                currentBid = startBid,
                buyNowPrice = buyNow,
                highestBidderName = null, // Fresh player starts clean, no fake bots
                isUserHighestBidder = false,
                secondsRemaining = secs,
                bidCount = 0,
                isWatched = false
            )
        }
        _marketListings.value = listings
    }

    private fun startMarketTicker() {
        marketTickerJob?.cancel()
        marketTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val updated = _marketListings.value.map { listing ->
                    if (listing.status == MarketListingStatus.ACTIVE) {
                        val newSec = listing.secondsRemaining - 1
                        if (newSec <= 0) {
                            if (listing.isUserHighestBidder) {
                                addPlayerToClubNoDuplicate(listing.player)
                                showNotification("Auction Won!", "You signed ${listing.player.name} for ${listing.currentBidFormatted}!")
                                listing.copy(status = MarketListingStatus.WON, secondsRemaining = 0)
                            } else {
                                listing.copy(status = MarketListingStatus.EXPIRED, secondsRemaining = 0)
                            }
                        } else {
                            if (newSec == 30 && listing.isWatched) {
                                showNotification("Auction Expiring!", "${listing.player.name} has only 30s left!", isAlert = true)
                            }
                            listing.copy(secondsRemaining = newSec)
                        }
                    } else listing
                }
                _marketListings.value = updated
            }
        }
    }

    fun updateMarketFilter(filter: MarketFilter) {
        _marketFilter.value = filter
    }

    fun placeMarketBid(listingId: String, amount: Long) {
        val club = _userClub.value
        if (club.budget < amount) {
            showNotification("Insufficient Budget", "You need ${amount / 1_000_000}M to bid! Buy coins in the Store.", isAlert = true)
            return
        }

        _marketListings.update { list ->
            list.map { l ->
                if (l.id == listingId) {
                    l.copy(
                        currentBid = amount,
                        highestBidderName = "You (${club.name})",
                        isUserHighestBidder = true,
                        userBidAmount = amount,
                        bidCount = l.bidCount + 1,
                        status = MarketListingStatus.ACTIVE,
                        secondsRemaining = (l.secondsRemaining + 15).coerceAtLeast(20)
                    )
                } else l
            }
        }
        showNotification("Bid Placed!", "You lead with €${amount / 1_000_000}M.")
    }

    fun buyNowMarketPlayer(listingId: String) {
        val listing = _marketListings.value.find { it.id == listingId } ?: return
        val club = _userClub.value

        if (club.budget < listing.buyNowPrice) {
            showNotification("Insufficient Budget", "Buy now requires ${listing.buyNowFormatted}! Buy coins in Store.", isAlert = true)
            return
        }

        _userClub.update { it.copy(budget = it.budget - listing.buyNowPrice) }
        addPlayerToClubNoDuplicate(listing.player)

        _marketListings.update { list ->
            list.map { if (it.id == listingId) it.copy(status = MarketListingStatus.WON, isUserHighestBidder = true) else it }
        }

        showNotification("Player Signed!", "Signed ${listing.player.name} to your squad!")
    }

    fun toggleWatchlist(listingId: String) {
        _marketListings.update { list ->
            list.map { if (it.id == listingId) it.copy(isWatched = !it.isWatched) else it }
        }
    }

    // ==========================================
    // MODE 1: 1v1 DUEL AUCTION (REAL ONLINE OR LOCAL)
    // ==========================================
    fun startNewDuel(passAndPlay: Boolean, p1Name: String = "Manager 1", p2Name: String = "Opponent") {
        duelTimerJob?.cancel()
        _isDuelPassAndPlay.value = passAndPlay
        _isOnlineMultiplayerDuel.value = false
        _duelMatchResult.value = null

        val user = _userClub.value
        _duelPlayer1.value = Team(
            id = user.id,
            name = if (passAndPlay) p1Name else "${user.name} (You)",
            isHuman = true,
            avatarIcon = user.avatarIcon,
            budget = 200_000_000L,
            squad = emptyList()
        )
        _duelPlayer2.value = Team(
            id = "duel_p2",
            name = p2Name,
            isHuman = passAndPlay,
            avatarIcon = "🔵",
            budget = 200_000_000L,
            squad = emptyList()
        )

        duelPool = _allPlayers.value.filter { it.overall >= 75 }.shuffled().toMutableList()
        startDuelRound(1)
    }

    fun startNewOnlineDuel(poolCards: List<PlayerCard>, isHost: Boolean, peerProfile: UserProfile?) {
        duelTimerJob?.cancel()
        _isDuelPassAndPlay.value = false
        _isOnlineMultiplayerDuel.value = true
        _duelMatchResult.value = null

        val myTeam = _userClub.value
        val peerName = peerProfile?.managerName ?: "Online Opponent"
        val peerClub = peerProfile?.clubName ?: "FC"
        val peerAvatar = peerProfile?.avatarIcon ?: "⚽"

        val me = Team(
            id = myTeam.id,
            name = "${myTeam.name} (You)",
            isHuman = true,
            avatarIcon = myTeam.avatarIcon,
            budget = 200_000_000L,
            squad = emptyList()
        )
        val opponent = Team(
            id = peerProfile?.userId ?: "peer_user",
            name = "$peerName ($peerClub)",
            isHuman = true,
            avatarIcon = peerAvatar,
            budget = 200_000_000L,
            squad = emptyList()
        )

        if (isHost) {
            _duelPlayer1.value = me
            _duelPlayer2.value = opponent
        } else {
            _duelPlayer1.value = opponent
            _duelPlayer2.value = me
        }

        duelPool = poolCards.toMutableList()
        _waitingRoomState.value = null
        startDuelRound(1)
    }

    private fun handleRemoteBid(senderId: String, bidderName: String, amount: Long) {
        val current = _duelAuctionState.value
        if (current.phase != AuctionPhase.BIDDING) return

        val newLog = current.bidHistory + BidLogEntry(bidderName, amount, "Round ${current.currentRound}")
        _duelAuctionState.update {
            it.copy(
                currentHighestBid = amount,
                highestBidderTeamId = senderId,
                highestBidderName = bidderName,
                secondsRemaining = 12,
                currentTurnManagerId = _userClub.value.id,
                bidHistory = newLog,
                auctionMessage = "⚡ $bidderName bid €${amount / 1_000_000}M!"
            )
        }
    }

    private fun handleRemotePass(senderId: String) {
        val current = _duelAuctionState.value
        if (current.phase != AuctionPhase.BIDDING) return
        _duelAuctionState.update {
            it.copy(auctionMessage = "Opponent passed! Finalizing round...")
        }
        finalizeDuelRound()
    }

    private fun startDuelRound(round: Int) {
        if (round > 11 || duelPool.size < 2) {
            finishDuelDraftAndSimulateMatch()
            return
        }

        val cardOnAuction = duelPool.removeAt(0)
        val startBid = (cardOnAuction.marketValue * 0.3).toLong().coerceAtLeast(5_000_000L)

        _duelAuctionState.value = AuctionState(
            type = AuctionType.DUEL_2_PLAYERS,
            phase = AuctionPhase.BIDDING,
            currentRound = round,
            maxRounds = 11,
            currentPlayer = cardOnAuction,
            currentHighestBid = startBid,
            startingBid = startBid,
            highestBidderTeamId = null,
            highestBidderName = null,
            secondsRemaining = 12,
            activeManagersInRound = setOf(_duelPlayer1.value.id, _duelPlayer2.value.id),
            currentTurnManagerId = _duelPlayer1.value.id,
            auctionMessage = "Round $round/11: ${cardOnAuction.name} (${cardOnAuction.position.code} ${cardOnAuction.overall} OVR)"
        )

        startDuelCountdown()
    }

    private fun startDuelCountdown() {
        duelTimerJob?.cancel()
        duelTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _duelAuctionState.value
                if (current.phase == AuctionPhase.ROUND_SUMMARY || current.phase == AuctionPhase.MATCH_TIME) break

                val newSec = current.secondsRemaining - 1
                if (newSec <= 0) {
                    finalizeDuelRound()
                    break
                } else {
                    val phase = when {
                        newSec <= 3 -> AuctionPhase.GOING_TWICE
                        newSec <= 6 -> AuctionPhase.GOING_ONCE
                        else -> AuctionPhase.BIDDING
                    }
                    _duelAuctionState.update { it.copy(secondsRemaining = newSec, phase = phase) }

                    if (!_isDuelPassAndPlay.value && !_isOnlineMultiplayerDuel.value && current.currentTurnManagerId == _duelPlayer2.value.id) {
                        handleAiDuelTurn()
                    }
                }
            }
        }
    }

    fun placeDuelBid(amount: Long) {
        val current = _duelAuctionState.value
        val myId = _userClub.value.id
        val bidderTeam = if (_isOnlineMultiplayerDuel.value) {
            if (_duelPlayer1.value.id == myId) _duelPlayer1.value else _duelPlayer2.value
        } else {
            val turnId = current.currentTurnManagerId ?: _duelPlayer1.value.id
            if (turnId == _duelPlayer1.value.id) _duelPlayer1.value else _duelPlayer2.value
        }

        if (bidderTeam.budget < amount) {
            showNotification("Budget Exceeded", "${bidderTeam.name} cannot afford €${amount / 1_000_000}M!", isAlert = true)
            return
        }

        val nextTurnId = if (bidderTeam.id == _duelPlayer1.value.id) _duelPlayer2.value.id else _duelPlayer1.value.id
        val newLog = current.bidHistory + BidLogEntry(bidderTeam.name, amount, "Round ${current.currentRound}")

        _duelAuctionState.update {
            it.copy(
                currentHighestBid = amount,
                highestBidderTeamId = bidderTeam.id,
                highestBidderName = bidderTeam.name,
                secondsRemaining = 12,
                phase = AuctionPhase.BIDDING,
                currentTurnManagerId = nextTurnId,
                bidHistory = newLog,
                auctionMessage = "${bidderTeam.name} raised to €${amount / 1_000_000}M!"
            )
        }

        if (_isOnlineMultiplayerDuel.value) {
            onlineMultiplayerService.broadcastBid(amount)
        } else if (!_isDuelPassAndPlay.value && nextTurnId == _duelPlayer2.value.id) {
            viewModelScope.launch {
                delay(1400)
                handleAiDuelTurn()
            }
        }
    }

    fun passDuelBid() {
        val current = _duelAuctionState.value
        val myId = _userClub.value.id
        val passerName = if (_isOnlineMultiplayerDuel.value) {
            _userClub.value.name
        } else {
            val passerId = current.currentTurnManagerId ?: _duelPlayer1.value.id
            if (passerId == _duelPlayer1.value.id) _duelPlayer1.value.name else _duelPlayer2.value.name
        }

        val newLog = current.bidHistory + BidLogEntry(passerName, 0L, "Round ${current.currentRound}", isAutoPass = true)
        _duelAuctionState.update {
            it.copy(bidHistory = newLog, auctionMessage = "$passerName passed the bid!")
        }

        if (_isOnlineMultiplayerDuel.value) {
            onlineMultiplayerService.broadcastPass()
        }
        finalizeDuelRound()
    }

    private fun handleAiDuelTurn() {
        val current = _duelAuctionState.value
        if (current.phase != AuctionPhase.BIDDING) return
        val player = current.currentPlayer ?: return
        val aiTeam = _duelPlayer2.value

        val currentBid = current.currentHighestBid
        val maxAiWilling = (player.marketValue * 0.95).toLong()
        val raise = currentBid + 3_000_000L

        if (raise <= maxAiWilling && aiTeam.budget >= raise && Random.nextInt(100) < 75) {
            val nextTurnId = _duelPlayer1.value.id
            val newLog = current.bidHistory + BidLogEntry(aiTeam.name, raise, "Round ${current.currentRound}")
            _duelAuctionState.update {
                it.copy(
                    currentHighestBid = raise,
                    highestBidderTeamId = aiTeam.id,
                    highestBidderName = aiTeam.name,
                    secondsRemaining = 12,
                    phase = AuctionPhase.BIDDING,
                    currentTurnManagerId = nextTurnId,
                    bidHistory = newLog,
                    auctionMessage = "${aiTeam.name} bids €${raise / 1_000_000}M!"
                )
            }
        } else {
            passDuelBid()
        }
    }

    private fun finalizeDuelRound() {
        duelTimerJob?.cancel()
        val current = _duelAuctionState.value
        val auctionCard = current.currentPlayer ?: return

        val winnerId = current.highestBidderTeamId
        val winningBid = current.currentHighestBid
        val myUserId = _userClub.value.id

        // Generates random draft consolation card
        val randomConsolationCard = if (duelPool.isNotEmpty()) duelPool.removeAt(0) else PlayerDatabase.generateRandomPlayer(Position.ST, 64, 74)

        if (winnerId != null) {
            // A player won the auction!
            val winnerTeam = if (winnerId == _duelPlayer1.value.id) _duelPlayer1.value else _duelPlayer2.value
            val loserTeam = if (winnerId == _duelPlayer1.value.id) _duelPlayer2.value else _duelPlayer1.value

            val updatedWinner = winnerTeam.copy(
                budget = (winnerTeam.budget - winningBid).coerceAtLeast(0L),
                squad = winnerTeam.squad + auctionCard
            )
            // THE LOSER / WHOEVER LET THE BID GO GETS THE RANDOM PLAYER!
            val updatedLoser = loserTeam.copy(
                squad = loserTeam.squad + randomConsolationCard
            )

            if (winnerId == _duelPlayer1.value.id) {
                _duelPlayer1.value = updatedWinner
                _duelPlayer2.value = updatedLoser
            } else {
                _duelPlayer1.value = updatedLoser
                _duelPlayer2.value = updatedWinner
            }

            // Also grant card to permanent club reserves!
            if (winnerId == myUserId || (!_isOnlineMultiplayerDuel.value && winnerId == _duelPlayer1.value.id)) {
                addPlayerToClubNoDuplicate(auctionCard)
                showNotification("Auction Won!", "Signed ${auctionCard.name} (${auctionCard.overall} OVR)!")
            } else {
                // User lost or passed the bid -> USER RECEIVES THE RANDOM CONSOLATION PLAYER!
                addPlayerToClubNoDuplicate(randomConsolationCard)
                showNotification("Random Draft Pick!", "You passed / lost the bid and drafted ${randomConsolationCard.name} (${randomConsolationCard.overall} OVR) for free!")
            }

            _duelAuctionState.update {
                it.copy(
                    phase = AuctionPhase.ROUND_SUMMARY,
                    winnerCard = auctionCard,
                    loserRandomCard = randomConsolationCard,
                    auctionMessage = "🏆 ${winnerTeam.name} won ${auctionCard.name}! ${loserTeam.name} drafted ${randomConsolationCard.name}!"
                )
            }
        } else {
            // Both managers passed / let the bid go without any bids!
            // BOTH MANAGERS RECEIVE A RANDOM CONSOLATION DRAFT PICK!
            val randomConsolationCard2 = if (duelPool.isNotEmpty()) duelPool.removeAt(0) else PlayerDatabase.generateRandomPlayer(Position.CM, 64, 74)

            _duelPlayer1.update { it.copy(squad = it.squad + randomConsolationCard) }
            _duelPlayer2.update { it.copy(squad = it.squad + randomConsolationCard2) }

            // Add free random card to user's club!
            addPlayerToClubNoDuplicate(randomConsolationCard)

            _duelAuctionState.update {
                it.copy(
                    phase = AuctionPhase.ROUND_SUMMARY,
                    winnerCard = null,
                    loserRandomCard = randomConsolationCard,
                    auctionMessage = "Bid passed! You drafted ${randomConsolationCard.name} (${randomConsolationCard.overall} OVR) for free!"
                )
            }
            showNotification("Free Draft Pick", "Drafted ${randomConsolationCard.name} (${randomConsolationCard.overall} OVR) for free!")
        }
    }

    fun continueToNextDuelRound() {
        val currentRound = _duelAuctionState.value.currentRound
        if (currentRound >= 11 || _duelPlayer1.value.squad.size >= 11) {
            finishDuelDraftAndSimulateMatch()
        } else {
            startDuelRound(currentRound + 1)
        }
    }

    private fun finishDuelDraftAndSimulateMatch() {
        _duelAuctionState.update { it.copy(phase = AuctionPhase.MATCH_TIME) }
        val result = MatchSimulator.simulate(_duelPlayer1.value, _duelPlayer2.value)
        _duelMatchResult.value = result

        if (result.homeScore > result.awayScore) {
            _duelPlayer1.update { it.copy(matchWins = it.matchWins + 1, tournamentPoints = it.tournamentPoints + 3) }
            _duelPlayer2.update { it.copy(matchLosses = it.matchLosses + 1) }
        } else if (result.awayScore > result.homeScore) {
            _duelPlayer2.update { it.copy(matchWins = it.matchWins + 1, tournamentPoints = it.tournamentPoints + 3) }
            _duelPlayer1.update { it.copy(matchLosses = it.matchLosses + 1) }
        } else {
            _duelPlayer1.update { it.copy(matchDraws = it.matchDraws + 1, tournamentPoints = it.tournamentPoints + 1) }
            _duelPlayer2.update { it.copy(matchDraws = it.matchDraws + 1, tournamentPoints = it.tournamentPoints + 1) }
        }
    }

    fun closeDuelMatchModal() {
        _duelMatchResult.value = null
    }

    // ==========================================
    // MODE 1: TOURNAMENT (3 to 8 MANAGERS)
    // ==========================================
    fun setupTournament(numberOfManagers: Int, managerNames: List<String>) {
        val avatars = listOf("👑", "🦁", "⚡", "🦅", "🐺", "🔥", "🛡️", "⭐")
        val teams = mutableListOf<Team>()

        for (i in 0 until numberOfManagers) {
            val name = managerNames.getOrNull(i) ?: if (i == 0) "You (Manager 1)" else "Manager ${i + 1}"
            teams.add(
                Team(
                    id = "tourney_team_$i",
                    name = name,
                    isHuman = (i == 0),
                    avatarIcon = avatars[i % avatars.size],
                    budget = 200_000_000L,
                    squad = emptyList()
                )
            )
        }

        tournamentPool = _allPlayers.value.filter { it.overall >= 75 }.shuffled().toMutableList()
        _tournamentState.value = TournamentState(
            phase = TournamentPhase.DRAFT_AUCTION,
            numberOfManagers = numberOfManagers,
            teams = teams
        )

        startTournamentAuctionRound()
    }

    private fun startTournamentAuctionRound() {
        val teams = _tournamentState.value.teams
        val eligibleTeams = teams.filter { it.squad.size < 11 }

        if (eligibleTeams.isEmpty() || tournamentPool.isEmpty()) {
            finishTournamentDraftAndStartMatches()
            return
        }

        val cardOnAuction = tournamentPool.removeAt(0)
        val startBid = (cardOnAuction.marketValue * 0.25).toLong().coerceAtLeast(5_000_000L)

        _tournamentAuctionState.value = AuctionState(
            type = AuctionType.TOURNAMENT_MULTI,
            phase = AuctionPhase.BIDDING,
            currentRound = _tournamentAuctionState.value.currentRound + 1,
            currentPlayer = cardOnAuction,
            currentHighestBid = startBid,
            startingBid = startBid,
            highestBidderTeamId = null,
            highestBidderName = null,
            secondsRemaining = 12,
            activeManagersInRound = eligibleTeams.map { it.id }.toSet(),
            currentTurnManagerId = eligibleTeams.first().id,
            auctionMessage = "Tournament Block: ${cardOnAuction.name} (${cardOnAuction.position.code} ${cardOnAuction.overall} OVR)"
        )

        startTournamentCountdown()
    }

    private fun startTournamentCountdown() {
        tournamentTimerJob?.cancel()
        tournamentTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _tournamentAuctionState.value
                if (current.phase == AuctionPhase.ROUND_SUMMARY || current.phase == AuctionPhase.MATCH_TIME) break

                val newSec = current.secondsRemaining - 1
                if (newSec <= 0) {
                    finalizeTournamentRound()
                    break
                } else {
                    val phase = when {
                        newSec <= 3 -> AuctionPhase.GOING_TWICE
                        newSec <= 6 -> AuctionPhase.GOING_ONCE
                        else -> AuctionPhase.BIDDING
                    }
                    _tournamentAuctionState.update { it.copy(secondsRemaining = newSec, phase = phase) }

                    val currentTurnId = current.currentTurnManagerId
                    val activeTeam = _tournamentState.value.teams.find { it.id == currentTurnId }
                    if (activeTeam != null && !activeTeam.isHuman) {
                        handleAiTournamentTurn(activeTeam)
                    }
                }
            }
        }
    }

    fun placeTournamentBid(teamId: String, amount: Long) {
        val current = _tournamentAuctionState.value
        val team = _tournamentState.value.teams.find { it.id == teamId } ?: return

        if (team.budget < amount) {
            showNotification("Budget Exceeded", "${team.name} cannot afford €${amount / 1_000_000}M!", isAlert = true)
            return
        }

        val eligibleTeams = _tournamentState.value.teams.filter { it.squad.size < 11 && current.activeManagersInRound.contains(it.id) }
        val currentIndex = eligibleTeams.indexOfFirst { it.id == teamId }
        val nextTeam = if (currentIndex != -1 && eligibleTeams.size > 1) {
            eligibleTeams[(currentIndex + 1) % eligibleTeams.size]
        } else team

        val newLog = current.bidHistory + BidLogEntry(team.name, amount, "Auction")
        _tournamentAuctionState.update {
            it.copy(
                currentHighestBid = amount,
                highestBidderTeamId = team.id,
                highestBidderName = team.name,
                secondsRemaining = 10,
                phase = AuctionPhase.BIDDING,
                currentTurnManagerId = nextTeam.id,
                bidHistory = newLog,
                auctionMessage = "${team.name} placed bid: €${amount / 1_000_000}M!"
            )
        }
    }

    fun passTournamentBid(teamId: String) {
        val current = _tournamentAuctionState.value
        val team = _tournamentState.value.teams.find { it.id == teamId } ?: return

        val remainingActive = current.activeManagersInRound - teamId
        val eligibleTeams = _tournamentState.value.teams.filter { remainingActive.contains(it.id) }

        if (remainingActive.size <= 1) {
            finalizeTournamentRound()
        } else {
            val nextTeam = eligibleTeams.firstOrNull() ?: team
            _tournamentAuctionState.update {
                it.copy(
                    activeManagersInRound = remainingActive,
                    currentTurnManagerId = nextTeam.id,
                    auctionMessage = "${team.name} dropped out of the bidding."
                )
            }
        }
    }

    private fun handleAiTournamentTurn(aiTeam: Team) {
        val current = _tournamentAuctionState.value
        val player = current.currentPlayer ?: return
        val currentBid = current.currentHighestBid
        val maxWilling = (player.marketValue * 0.9).toLong()

        val raise = currentBid + 3_000_000L
        if (raise <= maxWilling && aiTeam.budget >= raise && Random.nextInt(100) < 65) {
            placeTournamentBid(aiTeam.id, raise)
        } else {
            passTournamentBid(aiTeam.id)
        }
    }

    private fun finalizeTournamentRound() {
        tournamentTimerJob?.cancel()
        val current = _tournamentAuctionState.value
        val card = current.currentPlayer ?: return

        val winnerId = current.highestBidderTeamId
        val winningBid = current.currentHighestBid

        if (winnerId != null) {
            val teams = _tournamentState.value.teams.map { t ->
                if (t.id == winnerId) {
                    t.copy(
                        budget = (t.budget - winningBid).coerceAtLeast(0L),
                        squad = t.squad + card
                    )
                } else t
            }
            _tournamentState.update { it.copy(teams = teams) }

            val winner = teams.find { it.id == winnerId }
            _tournamentAuctionState.update {
                it.copy(
                    phase = AuctionPhase.ROUND_SUMMARY,
                    winnerCard = card,
                    auctionMessage = "🏆 ${winner?.name} won ${card.name} for €${winningBid / 1_000_000}M!"
                )
            }
        } else {
            _tournamentAuctionState.update {
                it.copy(
                    phase = AuctionPhase.ROUND_SUMMARY,
                    auctionMessage = "No bids placed. Card returned to pool."
                )
            }
        }
    }

    fun continueToNextTournamentRound() {
        val allCompleted = _tournamentState.value.teams.all { it.squad.size >= 11 }
        if (allCompleted) {
            finishTournamentDraftAndStartMatches()
        } else {
            startTournamentAuctionRound()
        }
    }

    private fun finishTournamentDraftAndStartMatches() {
        val teams = _tournamentState.value.teams
        val matches = mutableListOf<TournamentMatch>()
        var matchId = 1

        for (i in teams.indices) {
            for (j in i + 1 until teams.size) {
                matches.add(
                    TournamentMatch(
                        id = "m_${matchId++}",
                        roundNumber = 1,
                        homeTeam = teams[i],
                        awayTeam = teams[j]
                    )
                )
            }
        }

        _tournamentState.update {
            it.copy(
                phase = TournamentPhase.MATCH_STAGE,
                matches = matches,
                currentMatchIndex = 0
            )
        }
    }

    fun simulateNextTournamentMatch() {
        val current = _tournamentState.value
        val matchIndex = current.currentMatchIndex
        if (matchIndex >= current.matches.size) return

        val match = current.matches[matchIndex]
        val result = MatchSimulator.simulate(match.homeTeam, match.awayTeam)

        val updatedMatches = current.matches.toMutableList()
        updatedMatches[matchIndex] = match.copy(
            homeScore = result.homeScore,
            awayScore = result.awayScore,
            isPlayed = true,
            result = result
        )

        val updatedTeams = current.teams.map { t ->
            when (t.id) {
                match.homeTeam.id -> {
                    val pts = if (result.homeScore > result.awayScore) 3 else if (result.homeScore == result.awayScore) 1 else 0
                    t.copy(
                        matchWins = t.matchWins + (if (result.homeScore > result.awayScore) 1 else 0),
                        matchDraws = t.matchDraws + (if (result.homeScore == result.awayScore) 1 else 0),
                        matchLosses = t.matchLosses + (if (result.homeScore < result.awayScore) 1 else 0),
                        tournamentPoints = t.tournamentPoints + pts,
                        goalsFor = t.goalsFor + result.homeScore,
                        goalsAgainst = t.goalsAgainst + result.awayScore
                    )
                }
                match.awayTeam.id -> {
                    val pts = if (result.awayScore > result.homeScore) 3 else if (result.awayScore == result.homeScore) 1 else 0
                    t.copy(
                        matchWins = t.matchWins + (if (result.awayScore > result.homeScore) 1 else 0),
                        matchDraws = t.matchDraws + (if (result.awayScore == result.homeScore) 1 else 0),
                        matchLosses = t.matchLosses + (if (result.awayScore < result.homeScore) 1 else 0),
                        tournamentPoints = t.tournamentPoints + pts,
                        goalsFor = t.goalsFor + result.awayScore,
                        goalsAgainst = t.goalsAgainst + result.homeScore
                    )
                }
                else -> t
            }
        }

        val nextIndex = matchIndex + 1
        val isAllCompleted = nextIndex >= updatedMatches.size
        val champion = if (isAllCompleted) updatedTeams.maxByOrNull { it.tournamentPoints } else null

        _tournamentState.update {
            it.copy(
                teams = updatedTeams,
                matches = updatedMatches,
                currentMatchIndex = nextIndex,
                phase = if (isAllCompleted) TournamentPhase.COMPLETED else TournamentPhase.MATCH_STAGE,
                championTeam = champion
            )
        }
    }
}
