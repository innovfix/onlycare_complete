package com.onlycare.app.domain.model

/**
 * Friends + requests bundle returned from the friends endpoint.
 */
data class FriendsData(
    val friends: List<User> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val receivedRequests: List<FriendRequest> = emptyList()
)


