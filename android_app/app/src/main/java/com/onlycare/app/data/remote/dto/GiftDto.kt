package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Gift data model
 */
data class GiftData(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("gift_icon")
    val giftIcon: String,
    
    @SerializedName("coins")
    val coins: Int,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Response for fetching gift images
 */
data class GiftImageResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: List<GiftData>
)

/**
 * Response for sending a gift
 */
data class SendGiftResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: SendGiftData?
)

/**
 * Data returned when gift is sent successfully
 */
data class SendGiftData(
    @SerializedName("sender_name")
    val senderName: String,
    
    @SerializedName("receiver_name")
    val receiverName: String,
    
    @SerializedName("gift_id")
    val giftId: Int,
    
    @SerializedName("gift_icon")
    val giftIcon: String,
    
    @SerializedName("gift_coins")
    val giftCoins: Int
)

/**
 * Response for getting remaining time
 */
data class GetRemainingTimeResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: RemainingTimeData?
)

/**
 * Remaining time data
 */
data class RemainingTimeData(
    @SerializedName("remaining_time")
    val remainingTime: String  // Format: "MM:SS" or "HH:MM:SS"
)





