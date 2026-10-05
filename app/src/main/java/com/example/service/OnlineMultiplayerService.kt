package com.example.service

import android.util.Log
import com.example.model.PlayerCard
import com.example.model.UserProfile
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

sealed class MultiplayerEvent {
    data class RoomPresence(val roomCode: String, val hostProfile: UserProfile, val guestProfile: UserProfile?, val isHost: Boolean) : MultiplayerEvent()
    data class GameStarted(val poolPlayerCards: List<PlayerCard>) : MultiplayerEvent()
    data class BidPlaced(val senderId: String, val bidderName: String, val amount: Long) : MultiplayerEvent()
    data class PassPlaced(val senderId: String) : MultiplayerEvent()
    data class RoundFinalized(val winnerId: String?, val winningBid: Long, val winnerCard: PlayerCard, val loserCard: PlayerCard) : MultiplayerEvent()
    data class PeerDisconnected(val message: String) : MultiplayerEvent()
}

class OnlineMultiplayerService private constructor() {

    companion object {
        private const val TAG = "MultiplayerService"
        private const val ROOMS_COLLECTION = "rooms"
        private const val EVENTS_SUBCOLLECTION = "events"

        @Volatile
        private var instance: OnlineMultiplayerService? = null

        fun getInstance(): OnlineMultiplayerService {
            return instance ?: synchronized(this) {
                instance ?: OnlineMultiplayerService().also { instance = it }
            }
        }
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val playerListType = Types.newParameterizedType(List::class.java, PlayerCard::class.java)
    private val playerListAdapter = moshi.adapter<List<PlayerCard>>(playerListType)

    private val scope = CoroutineScope(Dispatchers.IO)

    private var firestore: FirebaseFirestore? = null
    private var roomListener: ListenerRegistration? = null
    private var eventsListener: ListenerRegistration? = null
    private var currentRoomCode: String? = null
    private var myProfile: UserProfile? = null
    private var isHostUser = false
    private var processedEventIds = mutableSetOf<String>()

    private val _connectionState = MutableStateFlow<String>("Disconnected")
    val connectionState: StateFlow<String> = _connectionState.asStateFlow()

    private val _connectedPeerProfile = MutableStateFlow<UserProfile?>(null)
    val connectedPeerProfile: StateFlow<UserProfile?> = _connectedPeerProfile.asStateFlow()

    private val _isRoomReadyToStart = MutableStateFlow(false)
    val isRoomReadyToStart: StateFlow<Boolean> = _isRoomReadyToStart.asStateFlow()

    var onEventReceived: ((MultiplayerEvent) -> Unit)? = null

    private fun getFirestore(): FirebaseFirestore? {
        if (firestore == null) {
            firestore = try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Firebase Firestore not available: ${e.message}")
                _connectionState.value = "⚠️ Firebase not configured. Online play unavailable."
                null
            }
        }
        return firestore
    }

    fun createRoom(host: UserProfile, customCode: String? = null): String {
        disconnect()
        myProfile = host
        isHostUser = true
        val code = customCode ?: "FA-${kotlin.random.Random.nextInt(1000, 9999)}"
        currentRoomCode = code
        _connectionState.value = "Creating room $code..."
        _connectedPeerProfile.value = null
        _isRoomReadyToStart.value = false
        processedEventIds.clear()

        val db = getFirestore()
        if (db == null) {
            _connectionState.value = "⚠️ Firebase not available. Cannot create online room."
            return code
        }

        val roomData = hashMapOf(
            "hostId" to host.userId,
            "hostName" to host.managerName,
            "hostClub" to host.clubName,
            "hostAvatar" to host.avatarIcon,
            "guestId" to null,
            "guestName" to null,
            "guestClub" to null,
            "guestAvatar" to null,
            "status" to "waiting",
            "createdAt" to System.currentTimeMillis()
        )

        scope.launch {
            try {
                db.collection(ROOMS_COLLECTION).document(code).set(roomData).await()
                _connectionState.value = "Waiting for friend to join room $code..."
                listenToRoom(code)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create room: ${e.message}")
                _connectionState.value = "Error creating room: ${e.message}"
            }
        }

        return code
    }

    fun joinRoom(guest: UserProfile, roomCode: String) {
        disconnect()
        myProfile = guest
        isHostUser = false
        val cleanCode = roomCode.trim().uppercase()
        currentRoomCode = cleanCode
        _connectionState.value = "Connecting to room $cleanCode..."
        _connectedPeerProfile.value = null
        _isRoomReadyToStart.value = false
        processedEventIds.clear()

        val db = getFirestore()
        if (db == null) {
            _connectionState.value = "⚠️ Firebase not available. Cannot join online room."
            return
        }

        scope.launch {
            try {
                val roomRef = db.collection(ROOMS_COLLECTION).document(cleanCode)
                val roomDoc = roomRef.get().await()

                if (!roomDoc.exists()) {
                    _connectionState.value = "❌ Room $cleanCode not found. Check the code and try again."
                    return@launch
                }

                val status = roomDoc.getString("status")
                if (status != "waiting") {
                    _connectionState.value = "❌ Room $cleanCode is already in a game or full."
                    return@launch
                }

                // Join the room
                roomRef.update(
                    mapOf(
                        "guestId" to guest.userId,
                        "guestName" to guest.managerName,
                        "guestClub" to guest.clubName,
                        "guestAvatar" to guest.avatarIcon,
                        "status" to "ready"
                    )
                ).await()

                // Read host profile from room
                val hostProfile = UserProfile(
                    userId = roomDoc.getString("hostId") ?: "host",
                    managerName = roomDoc.getString("hostName") ?: "Host Manager",
                    clubName = roomDoc.getString("hostClub") ?: "Host FC",
                    avatarIcon = roomDoc.getString("hostAvatar") ?: "👑"
                )
                _connectedPeerProfile.value = hostProfile
                _isRoomReadyToStart.value = true
                _connectionState.value = "🟢 Connected with Host: ${hostProfile.managerName} (${hostProfile.clubName})"

                listenToRoom(cleanCode)
                listenToEvents(cleanCode)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to join room: ${e.message}")
                _connectionState.value = "Error joining room: ${e.message}"
            }
        }
    }

    private fun listenToRoom(code: String) {
        val db = getFirestore() ?: return
        roomListener?.remove()

        roomListener = db.collection(ROOMS_COLLECTION).document(code)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Room listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val guestId = snapshot.getString("guestId")
                val status = snapshot.getString("status")

                if (isHostUser && guestId != null && guestId.isNotEmpty()) {
                    val guestProfile = UserProfile(
                        userId = guestId,
                        managerName = snapshot.getString("guestName") ?: "Friend",
                        clubName = snapshot.getString("guestClub") ?: "Friend FC",
                        avatarIcon = snapshot.getString("guestAvatar") ?: "⚽"
                    )
                    _connectedPeerProfile.value = guestProfile
                    _isRoomReadyToStart.value = true
                    _connectionState.value = "🟢 Friend Connected: ${guestProfile.managerName} (${guestProfile.clubName})"

                    // Start listening to events now that we have a guest
                    listenToEvents(code)
                }

                if (status == "disconnected") {
                    onEventReceived?.invoke(MultiplayerEvent.PeerDisconnected("Opponent disconnected from the room."))
                }
            }
    }

    private fun listenToEvents(code: String) {
        val db = getFirestore() ?: return
        eventsListener?.remove()

        eventsListener = db.collection(ROOMS_COLLECTION).document(code)
            .collection(EVENTS_SUBCOLLECTION)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Events listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshots == null) return@addSnapshotListener

                for (change in snapshots.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val eventId = doc.id

                        // Skip already processed events
                        if (processedEventIds.contains(eventId)) continue
                        processedEventIds.add(eventId)

                        val senderId = doc.getString("senderId") ?: ""
                        // Skip our own events
                        if (senderId == myProfile?.userId) continue

                        val type = doc.getString("type") ?: ""
                        val dataJson = doc.getString("data") ?: "{}"

                        try {
                            when (type) {
                                "GAME_START" -> {
                                    val data = JSONObject(dataJson)
                                    val poolJson = data.optString("poolCards", "[]")
                                    val cards = playerListAdapter.fromJson(poolJson) ?: emptyList()
                                    onEventReceived?.invoke(MultiplayerEvent.GameStarted(cards))
                                }
                                "BID" -> {
                                    val data = JSONObject(dataJson)
                                    val amount = data.optLong("amount")
                                    val bidderName = data.optString("bidderName", "Opponent")
                                    onEventReceived?.invoke(MultiplayerEvent.BidPlaced(senderId, bidderName, amount))
                                }
                                "PASS" -> {
                                    onEventReceived?.invoke(MultiplayerEvent.PassPlaced(senderId))
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing event: ${e.message}")
                        }
                    }
                }
            }
    }

    fun broadcastGameStart(poolCards: List<PlayerCard>) {
        val code = currentRoomCode ?: return
        val db = getFirestore() ?: return
        val poolJson = playerListAdapter.toJson(poolCards)

        val eventData = hashMapOf(
            "type" to "GAME_START",
            "senderId" to (myProfile?.userId ?: ""),
            "data" to JSONObject().apply {
                put("poolCards", poolJson)
            }.toString(),
            "timestamp" to System.currentTimeMillis()
        )

        scope.launch {
            try {
                db.collection(ROOMS_COLLECTION).document(code)
                    .collection(EVENTS_SUBCOLLECTION)
                    .add(eventData).await()
                // Update room status
                db.collection(ROOMS_COLLECTION).document(code)
                    .update("status", "playing").await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to broadcast game start: ${e.message}")
            }
        }
    }

    fun broadcastBid(amount: Long) {
        val code = currentRoomCode ?: return
        val db = getFirestore() ?: return

        val eventData = hashMapOf(
            "type" to "BID",
            "senderId" to (myProfile?.userId ?: ""),
            "data" to JSONObject().apply {
                put("amount", amount)
                put("bidderName", myProfile?.managerName ?: "Player")
            }.toString(),
            "timestamp" to System.currentTimeMillis()
        )

        scope.launch {
            try {
                db.collection(ROOMS_COLLECTION).document(code)
                    .collection(EVENTS_SUBCOLLECTION)
                    .add(eventData).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to broadcast bid: ${e.message}")
            }
        }
    }

    fun broadcastPass() {
        val code = currentRoomCode ?: return
        val db = getFirestore() ?: return

        val eventData = hashMapOf(
            "type" to "PASS",
            "senderId" to (myProfile?.userId ?: ""),
            "data" to "{}",
            "timestamp" to System.currentTimeMillis()
        )

        scope.launch {
            try {
                db.collection(ROOMS_COLLECTION).document(code)
                    .collection(EVENTS_SUBCOLLECTION)
                    .add(eventData).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to broadcast pass: ${e.message}")
            }
        }
    }

    fun disconnect() {
        val code = currentRoomCode
        roomListener?.remove()
        eventsListener?.remove()
        roomListener = null
        eventsListener = null

        if (code != null) {
            scope.launch {
                try {
                    getFirestore()?.collection(ROOMS_COLLECTION)?.document(code)
                        ?.update("status", "disconnected")
                } catch (_: Exception) {}
            }
        }

        currentRoomCode = null
        myProfile = null
        _connectionState.value = "Disconnected"
        _connectedPeerProfile.value = null
        _isRoomReadyToStart.value = false
        processedEventIds.clear()
    }
}
