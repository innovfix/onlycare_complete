package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface GiftApiService {
    
    /**
     * Get list of available gifts
     * POST /auth/gifts_list
     */
    @POST("auth/gifts_list")
    fun getGiftImages(): Call<GiftImageResponse>
    
    /**
     * Send a gift to a user
     * POST /auth/send_gifts
     * 
     * @param userId Sender's user ID
     * @param receiverId Receiver's user ID
     * @param giftId Gift ID to send
     */
    @FormUrlEncoded
    @POST("auth/send_gifts")
    fun sendGift(
        @Field("user_id") userId: String,
        @Field("receiver_id") receiverId: String,
        @Field("gift_id") giftId: Int
    ): Call<SendGiftResponse>
    
    /**
     * Get remaining call time for coin calculation
     * POST /auth/get_remaining_time
     */
    @FormUrlEncoded
    @POST("auth/get_remaining_time")
    fun getRemainingTime(
        @Field("user_id") userId: String,
        @Field("call_type") callType: String
    ): Call<GetRemainingTimeResponse>
    
    /**
     * Send FCM notification to receiver when gift is sent
     * POST /auth/send_gift_notification
     */
    @FormUrlEncoded
    @POST("auth/send_gift_notification")
    fun sendGiftNotification(
        @Field("sender_id") senderId: String,
        @Field("receiver_id") receiverId: String,
        @Field("gift_id") giftId: Int,
        @Field("gift_icon") giftIcon: String,
        @Field("gift_coins") giftCoins: Int,
        @Field("call_type") callType: String
    ): Call<ApiResponse<String>>
}

