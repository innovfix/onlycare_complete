package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * User DTO matching Laravel API response
 */
data class UserDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("age")
    val age: Int? = null,
    
    @SerializedName("gender")
    val gender: String, // "MALE" or "FEMALE"
    
    @SerializedName("profile_image")
    val profileImage: String? = null,
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("language")
    val language: String? = null,
    
    @SerializedName("interests")
    val interests: List<String>? = null,
    
    @SerializedName(value = "is_online", alternate = ["isOnline", "online", "online_status", "is_online_status"])
    val isOnline: Any? = null,

    @SerializedName("last_seen")
    val lastSeen: Long? = null,
    
    @SerializedName("rating")
    val rating: Float = 0f,
    
    @SerializedName("total_ratings")
    val totalRatings: Int = 0,
    
    @SerializedName("coin_balance")
    val coinBalance: Int? = null,
    
    @SerializedName("total_earnings")
    val totalEarnings: Double? = null,
    
    // Some APIs return boolean flags (audio_call_enabled/video_call_enabled),
    // others return int flags (audio_status/video_status). We support both via Any? and mapping.
    @SerializedName(value = "audio_call_enabled", alternate = ["audio_enabled", "is_audio_enabled", "audio_call_status", "audio_status", "audioStatus"])
    val audioCallEnabled: Any? = null,
    
    @SerializedName(value = "video_call_enabled", alternate = ["video_enabled", "is_video_enabled", "video_call_status", "video_status", "videoStatus"])
    val videoCallEnabled: Any? = null,
    
    @SerializedName(value = "is_verified", alternate = ["isVerified", "verified", "is_approved", "verification_status"])
    val isVerified: Any? = null,

    // Some backends expose verification via timestamp rather than boolean.
    @SerializedName(value = "verified_datetime", alternate = ["verifiedDatetime", "verified_at", "verifiedAt"])
    val verifiedDatetime: String? = null,
    
    @SerializedName("kyc_status")
    val kycStatus: String? = null,
    
    @SerializedName("audio_call_rate")
    val audioCallRate: Int? = null,
    
    @SerializedName("video_call_rate")
    val videoCallRate: Int? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("upi_id")
    val upiId: String? = null,
    
    @SerializedName("pancard_name")
    val pancardName: String? = null,
    
    @SerializedName("pancard_number")
    val pancardNumber: String? = null
)

/**
 * Update Profile Request
 * Note: age, bio, gender, language are set during registration and cannot be updated
 */
data class UpdateProfileRequest(
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("name")
    val name: String? = null,  // Username value is passed here
    
    @SerializedName("interests")
    val interests: List<String>? = null,
    
    @SerializedName("profile_image")
    val profileImage: String? = null  // Avatar ID (as string) - backend expects "profile_image" field name
)

/**
 * Update Status Request
 */
data class UpdateStatusRequest(
    @SerializedName("is_online")
    val isOnline: Boolean
)

/**
 * Update Call Availability Request (Female only)
 */
data class UpdateCallAvailabilityRequest(
    @SerializedName("audio_call_enabled")
    val audioCallEnabled: Any? = null,
    
    @SerializedName("video_call_enabled")
    val videoCallEnabled: Any? = null,

    @SerializedName("audio_status")
    val audioStatus: Any? = null,

    @SerializedName("video_status")
    val videoStatus: Any? = null
)

/**
 * Update FCM Token Request (for push notifications)
 */
data class UpdateFCMTokenRequest(
    @SerializedName("fcm_token")
    val fcmToken: String
)

/**
 * Update Online Datetime Response Data
 */
data class UpdateOnlineDatetimeData(
    @SerializedName("online_datetime")
    val onlineDatetime: String,
    
    @SerializedName("online_datetime_utc")
    val onlineDatetimeUtc: String,
    
    @SerializedName("online_datetime_timestamp")
    val onlineDatetimeTimestamp: Long,
    
    @SerializedName("formatted_time")
    val formattedTime: String,
    
    @SerializedName("time_ago")
    val timeAgo: String
)

/**
 * Update Online Datetime Response
 */
data class UpdateOnlineDatetimeResponse(
    @SerializedName("online_datetime")
    val onlineDatetime: String,
    
    @SerializedName("online_datetime_utc")
    val onlineDatetimeUtc: String,
    
    @SerializedName("online_datetime_timestamp")
    val onlineDatetimeTimestamp: Long,
    
    @SerializedName("formatted_time")
    val formattedTime: String,
    
    @SerializedName("time_ago")
    val timeAgo: String
)

