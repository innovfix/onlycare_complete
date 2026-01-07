package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    
    @GET("users/me")
    fun getCurrentUser(): Call<ApiResponse<UserDto>>
    
    @PUT("users/me")
    fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Call<ApiResponse<UserDto>>
    
    @POST("users/me/status")
    fun updateStatus(
        @Body request: UpdateStatusRequest
    ): Call<ApiResponse<String>>
    
    @POST("users/me/call-availability")
    fun updateCallAvailability(
        @Body request: UpdateCallAvailabilityRequest
    ): Call<ApiResponse<String>>
    
    @GET("users/females")
    fun getFemaleUsers(
        @Query("online") online: Boolean? = null,
        @Query("verified") verified: Boolean? = null,
        @Query("language") language: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<PaginatedResponse<UserDto>>
    
    @GET("users/{userId}")
    fun getUserById(
        @Path("userId") userId: String
    ): Call<ApiResponse<UserDto>>
    
    @POST("users/{userId}/block")
    fun blockUser(
        @Path("userId") userId: String
    ): Call<ApiResponse<String>>
    
    @POST("users/{userId}/unblock")
    fun unblockUser(
        @Path("userId") userId: String
    ): Call<ApiResponse<String>>
    
    @GET("users/me/blocked")
    fun getBlockedUsers(): Call<ApiResponse<List<UserDto>>>
    
    @GET("users/check-username")
    fun checkUsernameAvailability(
        @Query("username") username: String
    ): Call<ApiResponse<Boolean>>
    
    @POST("users/update-fcm-token")
    fun updateFCMToken(
        @Body request: UpdateFCMTokenRequest
    ): Call<ApiResponse<String>>
    
    @POST("users/me/update-online-datetime")
    fun updateOnlineDatetime(): Call<ApiResponse<UpdateOnlineDatetimeResponse>>
}

