package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Friend DTO
 */
data class FriendDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("user")
    val user: UserDto,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Get Friends Response
 */
data class GetFriendsResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("friends")
    val friends: List<UserDto> = emptyList(),

    // Optional: many backends include pending friend requests in the same response.
    // We support multiple common key names via alternates.
    @SerializedName(
        value = "sent_requests",
        alternate = ["sentRequests", "my_requests", "myRequests", "pending_sent_requests", "sent_request"]
    )
    val sentRequests: List<FriendDto>? = null,

    @SerializedName(
        value = "received_requests",
        alternate = ["receivedRequests", "received", "incoming_requests", "requests", "pending_requests", "received_request"]
    )
    val receivedRequests: List<FriendDto>? = null,

    // Some backends return request users directly (as UserDto lists).
    @SerializedName(
        value = "sent_request_users",
        alternate = ["sentUsers", "sent_users", "sentRequestsUsers"]
    )
    val sentRequestUsers: List<UserDto>? = null,

    @SerializedName(
        value = "received_request_users",
        alternate = ["receivedUsers", "received_users", "receivedRequestsUsers"]
    )
    val receivedRequestUsers: List<UserDto>? = null
)

/**
 * Report User Request
 */
data class ReportUserRequest(
    @SerializedName("reported_user_id")
    val reportedUserId: String,
    
    @SerializedName("reason")
    val reason: String,
    
    @SerializedName("description")
    val description: String? = null
)

// Note: Referral-related DTOs have been moved to ReferralDto.kt
