package com.onlycare.app.domain.model

/**
 * A lightweight representation of a friend request shown in the UI.
 *
 * Note: Accept/Reject APIs use the other user's id, so we store that as userId.
 */
data class FriendRequest(
    val userId: String,
    val name: String,
    val profileImage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)


