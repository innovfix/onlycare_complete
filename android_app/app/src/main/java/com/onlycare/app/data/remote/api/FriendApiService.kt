package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface FriendApiService {
    
    @GET("friends")
    fun getFriends(): Call<GetFriendsResponse>
    
    @POST("friends/{userId}/request")
    fun sendFriendRequest(
        @Path("userId") userId: String
    ): Call<ApiResponse<String>>
    
    @POST("friends/{userId}/accept")
    fun acceptFriendRequest(
        @Path("userId") userId: String
    ): Call<ApiResponse<String>>
    
    @POST("friends/{userId}/reject")
    fun rejectFriendRequest(
        @Path("userId") userId: String
    ): Call<ApiResponse<String>>
    
    @DELETE("friends/{userId}")
    fun removeFriend(
        @Path("userId") userId: String
    ): Call<ApiResponse<String>>
    
    @POST("reports/user")
    fun reportUser(
        @Body request: ReportUserRequest
    ): Call<ApiResponse<String>>
}

// Note: Referral endpoints moved to ReferralApiService.kt

