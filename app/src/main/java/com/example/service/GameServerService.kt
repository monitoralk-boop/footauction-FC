package com.example.service

import android.util.Log
import com.example.model.ChallengeInvite
import com.example.model.FriendProfile
import com.example.model.FriendRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Service coordinating HTTP REST and WebSocket communication with the FootAuction FC Server.
 * Supports:
 * - Local server (http://10.0.2.2:3000 on emulator or 192.168.x.x on LAN)
 * - Cloud free server (Koyeb / Render)
 * - Intelligent offline / sandbox simulation with AI Manager buddies (Zidane, Ancelotti, Guardiola)
 */
class GameServerService private constructor() {

    companion object {
        private const val TAG = "GameServerService"

        // Default local server port (or change to your free Koyeb/Render URL)
        var serverBaseUrl: String = "http://10.0.2.2:3000"
        var webSocketUrl: String = "ws://10.0.2.2:3000/ws"

        @Volatile
        private var instance: GameServerService? = null

        fun getInstance(): GameServerService {
            return instance ?: synchronized(this) {
                instance ?: GameServerService().also { instance = it }
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var webSocket: WebSocket? = null

    // State
    private val _friends = MutableStateFlow<List<FriendProfile>>(defaultMockFriends())
    val friends: StateFlow<List<FriendProfile>> = _friends.asStateFlow()

    private val _pendingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val pendingRequests: StateFlow<List<FriendRequest>> = _pendingRequests.asStateFlow()

    private val _incomingChallenge = MutableStateFlow<ChallengeInvite?>(null)
    val incomingChallenge: StateFlow<ChallengeInvite?> = _incomingChallenge.asStateFlow()

    private val _isServerOnline = MutableStateFlow(false)
    val isServerOnline: StateFlow<Boolean> = _isServerOnline.asServerOnlineState()

    private fun MutableStateFlow<Boolean>.asServerOnlineState(): StateFlow<Boolean> = this.asStateFlow()

    private fun defaultMockFriends(): List<FriendProfile> {
        return listOf(
            FriendProfile(
                userId = "usr_bot_zidane",
                managerName = "Zinedine Zidane",
                managerTag = "Zinedine Zidane#1998",
                clubName = "Icon Legends FC",
                avatarIcon = "👑",
                trophies = 880,
                divisionTier = 1,
                isOnline = true
            ),
            FriendProfile(
                userId = "usr_bot_ancelotti",
                managerName = "Carlo Ancelotti",
                managerTag = "Carlo Ancelotti#2024",
                clubName = "Real Madrid FC",
                avatarIcon = "⚽",
                trophies = 760,
                divisionTier = 2,
                isOnline = true
            ),
            FriendProfile(
                userId = "usr_bot_guardiola",
                managerName = "Pep Guardiola",
                managerTag = "Pep Guardiola#1001",
                clubName = "Manchester Stars",
                avatarIcon = "💎",
                trophies = 620,
                divisionTier = 3,
                isOnline = false
            )
        )
    }

    /**
     * Check if free backend server is reachable.
     */
    fun checkServerHealth(onResult: (Boolean) -> Unit) {
        val request = Request.Builder()
            .url("$serverBaseUrl/api/health")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "Server offline or not deployed yet. Using offline sandbox mode.")
                _isServerOnline.value = false
                onResult(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val ok = response.isSuccessful
                _isServerOnline.value = ok
                response.close()
                onResult(ok)
            }
        })
    }

    /**
     * Fetch friends list from server or fallback to local list.
     */
    fun refreshFriends(userId: String) {
        val request = Request.Builder()
            .url("$serverBaseUrl/api/friends/list/$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Keep current mock friends
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return
                    try {
                        val json = JSONObject(body)
                        val friendsArray = json.optJSONArray("friends") ?: JSONArray()
                        val list = mutableListOf<FriendProfile>()
                        for (i in 0 until friendsArray.length()) {
                            val f = friendsArray.getJSONObject(i)
                            list.add(
                                FriendProfile(
                                    userId = f.getString("userId"),
                                    managerName = f.getString("managerName"),
                                    managerTag = f.getString("managerTag"),
                                    clubName = f.getString("clubName"),
                                    avatarIcon = f.optString("avatarIcon", "⚽"),
                                    trophies = f.optInt("trophies", 0),
                                    divisionTier = f.optInt("divisionTier", 9),
                                    isOnline = f.optBoolean("isOnline", false)
                                )
                            )
                        }
                        if (list.isNotEmpty()) {
                            _friends.value = list
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Parse error: ${e.message}")
                    }
                }
                response.close()
            }
        })
    }

    /**
     * Send friend request by Tag (e.g. "Zidane#1998").
     */
    fun sendFriendRequest(fromUserId: String, targetTag: String, callback: (Boolean, String) -> Unit) {
        val jsonPayload = JSONObject().apply {
            put("fromUserId", fromUserId)
            put("targetTag", targetTag.trim())
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = jsonPayload.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$serverBaseUrl/api/friends/request")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Simulate success in local sandbox if server is not yet hosted
                val clean = targetTag.trim()
                if (clean.contains("#")) {
                    val parts = clean.split("#")
                    val newFriend = FriendProfile(
                        userId = "usr_custom_${System.currentTimeMillis()}",
                        managerName = parts[0],
                        managerTag = clean,
                        clubName = "${parts[0]} FC",
                        avatarIcon = "⚽",
                        trophies = (100..600).random(),
                        divisionTier = (4..8).random(),
                        isOnline = true
                    )
                    _friends.value = _friends.value + newFriend
                    callback(true, "Friend request accepted! $clean is now in your friends list.")
                } else {
                    callback(false, "Invalid format. Tag must include '#' (e.g. Manager#1234).")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                val ok = response.isSuccessful
                val msg = try {
                    JSONObject(body).optString("message", if (ok) "Sent!" else "Failed")
                } catch (_: Exception) {
                    if (ok) "Request sent successfully!" else "Error sending request."
                }
                response.close()
                callback(ok, msg)
            }
        })
    }

    /**
     * Challenge a friend to a 1v1 Auction Duel.
     */
    fun challengeFriend(fromUserId: String, targetFriend: FriendProfile, onResult: (Boolean, String) -> Unit) {
        if (!targetFriend.isOnline) {
            onResult(false, "${targetFriend.managerName} is currently offline.")
            return
        }

        // If WebSocket is active, send via WS
        val ws = webSocket
        if (ws != null) {
            val json = JSONObject().apply {
                put("event", "SEND_CHALLENGE")
                put("fromUserId", fromUserId)
                put("targetUserId", targetFriend.userId)
                put("mode", "DUEL")
            }
            ws.send(json.toString())
            onResult(true, "Challenge invite sent to ${targetFriend.managerName}!")
        } else {
            // Local simulation: Friend instantly accepts!
            onResult(true, "⚔️ Challenge accepted by ${targetFriend.managerName}! Launching Auction Duel...")
        }
    }

    fun dismissChallenge() {
        _incomingChallenge.value = null
    }

    fun acceptIncomingChallenge(invite: ChallengeInvite) {
        _incomingChallenge.value = null
    }
}
