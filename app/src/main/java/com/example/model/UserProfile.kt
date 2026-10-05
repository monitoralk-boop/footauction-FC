package com.example.model

data class UserProfile(
    val userId: String,
    val managerName: String,
    val clubName: String,
    val avatarIcon: String,
    val tagNumber: Int = 1042,
    val createdAt: Long = System.currentTimeMillis()
) {
    val managerTag: String
        get() = "$managerName#$tagNumber"
}
