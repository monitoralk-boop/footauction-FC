package com.example.service

import android.content.Context
import android.content.SharedPreferences
import com.example.model.PlayerCard
import com.example.model.UserProfile
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.tasks.await

class AccountManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("footauction_account_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val playerListType = Types.newParameterizedType(List::class.java, PlayerCard::class.java)
    private val playerListAdapter = moshi.adapter<List<PlayerCard>>(playerListType)

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null && getProfile() != null
    }

    fun getFirebaseUserId(): String? = firebaseAuth.currentUser?.uid

    suspend fun signUpWithEmail(email: String, password: String): Result<AuthResult> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_USER_ID, profile.userId)
            .putString(KEY_MANAGER_NAME, profile.managerName)
            .putString(KEY_CLUB_NAME, profile.clubName)
            .putString(KEY_AVATAR_ICON, profile.avatarIcon)
            .putInt(KEY_TAG_NUMBER, profile.tagNumber)
            .putLong(KEY_CREATED_AT, profile.createdAt)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getProfile(): UserProfile? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val managerName = prefs.getString(KEY_MANAGER_NAME, "Manager") ?: "Manager"
        val clubName = prefs.getString(KEY_CLUB_NAME, "My Club") ?: "My Club"
        val avatarIcon = prefs.getString(KEY_AVATAR_ICON, "\uD83D\uDC51") ?: "\uD83D\uDC51"
        val tagNumber = prefs.getInt(KEY_TAG_NUMBER, 1042)
        val createdAt = prefs.getLong(KEY_CREATED_AT, System.currentTimeMillis())

        return UserProfile(
            userId = userId,
            managerName = managerName,
            clubName = clubName,
            avatarIcon = avatarIcon,
            tagNumber = tagNumber,
            createdAt = createdAt
        )
    }

    fun saveStarterSquad(starting11: List<PlayerCard>, reserves: List<PlayerCard>) {
        try {
            val s11Json = playerListAdapter.toJson(starting11)
            val resJson = playerListAdapter.toJson(reserves)
            prefs.edit()
                .putString(KEY_SAVED_SQUAD, s11Json)
                .putString(KEY_SAVED_RESERVES, resJson)
                .apply()
        } catch (_: Exception) {}
    }

    fun getSavedStarterSquad(): Pair<List<PlayerCard>, List<PlayerCard>>? {
        return try {
            val s11Json = prefs.getString(KEY_SAVED_SQUAD, null) ?: return null
            val resJson = prefs.getString(KEY_SAVED_RESERVES, null) ?: return null
            val s11 = playerListAdapter.fromJson(s11Json) ?: return null
            val res = playerListAdapter.fromJson(resJson) ?: return null
            Pair(s11, res)
        } catch (_: Exception) {
            null
        }
    }

    // Keep legacy logout that just clears the login flag
    fun logout() {
        signOut()
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_MANAGER_NAME = "manager_name"
        private const val KEY_CLUB_NAME = "club_name"
        private const val KEY_AVATAR_ICON = "avatar_icon"
        private const val KEY_TAG_NUMBER = "tag_number"
        private const val KEY_CREATED_AT = "created_at"
        private const val KEY_SAVED_SQUAD = "saved_squad_json"
        private const val KEY_SAVED_RESERVES = "saved_reserves_json"
    }
}
