package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Initiate Call Request
 */
data class InitiateCallRequest(
    @SerializedName("receiver_id")
    val receiverId: String,
    
    @SerializedName("call_type")
    val callType: String // "AUDIO" or "VIDEO"
)

/**
 * Initiate Call Response
 */
data class InitiateCallResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("call")
    val call: CallDto? = null,  // Made nullable for safety
    
    @SerializedName("agora_app_id")
    val agoraAppId: String? = null,
    
    @SerializedName("agora_token")
    val agoraToken: String? = null,
    
    @SerializedName("channel_name")
    val channelName: String? = null,
    
    @SerializedName("balance_time")
    val balanceTime: String? = null // Formatted time string like "90:00" (minutes:seconds)
    
    // Legacy support - if backend returns "data" instead of "call"
    // @SerializedName("data")
    // val data: CallData? = null
)

/**
 * Call DTO
 */
data class CallDto(
    @SerializedName("id")
    val id: String? = null,  // Made nullable to handle missing/malformed responses
    
    @SerializedName("caller_id")
    val callerId: String? = null,  // Made nullable for safety
    
    @SerializedName("caller_name")
    val callerName: String? = null,
    
    @SerializedName("caller_image")
    val callerImage: String? = null,
    
    @SerializedName("receiver_id")
    val receiverId: String? = null,  // Made nullable for safety
    
    @SerializedName("receiver_name")
    val receiverName: String? = null,
    
    @SerializedName("receiver_image")
    val receiverImage: String? = null,
    
    @SerializedName("other_user_id")
    val otherUserId: String? = null,
    
    @SerializedName("other_user_name")
    val otherUserName: String? = null,
    
    @SerializedName("other_user_image")
    val otherUserImage: String? = null,
    
    @SerializedName("call_type")
    val callType: String? = null,  // Made nullable for safety
    
    @SerializedName("status")
    val status: String? = null,  // Made nullable for safety
    
    @SerializedName("agora_app_id")
    val agoraAppId: String? = null,
    
    @SerializedName("agora_token")
    val agoraToken: String? = null,
    
    @SerializedName("channel_name")
    val channelName: String? = null,
    
    @SerializedName("balance_time")
    val balanceTime: String? = null,
    
    @SerializedName("duration")
    val duration: Int = 0,
    
    @SerializedName("coins_spent")
    val coinsSpent: Int = 0,
    
    @SerializedName("coins_earned")
    val coinsEarned: Int = 0,
    
    @SerializedName("started_at")
    val startedAt: String? = null,
    
    @SerializedName("ended_at")
    val endedAt: String? = null,
    
    @SerializedName("rating")
    val rating: Float? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long? = null
)

/**
 * End Call Request
 */
data class EndCallRequest(
    @SerializedName("duration")
    val duration: Int // in seconds
)

/**
 * End Call Response
 */
data class EndCallResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("call")
    val call: CallDto? = null,  // Made nullable for safety
    
    @SerializedName("updated_balance")
    val updatedBalance: Int? = null
)

/**
 * Rate Call Request
 */
data class RateCallRequest(
    @SerializedName("rating")
    val rating: Float,
    
    @SerializedName("feedback")
    val feedback: String? = null
)

/**
 * Recent Caller DTO
 */
data class RecentCallerDto(
    @SerializedName("user")
    val user: UserDto,
    
    @SerializedName("last_call_time")
    val lastCallTime: String,
    
    @SerializedName("total_calls")
    val totalCalls: Int,
    
    @SerializedName("total_duration")
    val totalDuration: Int,
    
    @SerializedName("total_earnings")
    val totalEarnings: Double
)

/**
 * Incoming Call DTO - For real-time call notifications
 */
data class IncomingCallDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("caller_id")
    val callerId: String,
    
    @SerializedName("caller_name")
    val callerName: String,
    
    @SerializedName("caller_image")
    val callerImage: String? = null,
    
    @SerializedName("call_type")
    val callType: String, // "AUDIO" or "VIDEO"
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("agora_app_id")
    val agoraAppId: String? = null,
    
    @SerializedName("agora_token")
    val agoraToken: String? = null,
    
    @SerializedName("channel_name")
    val channelName: String? = null,
    
    @SerializedName("balance_time")
    val balanceTime: String? = null // Time remaining for the call (e.g., "90:00")
)

