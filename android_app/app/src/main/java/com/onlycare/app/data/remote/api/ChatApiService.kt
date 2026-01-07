package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ChatApiService {
    
    @GET("chat/conversations")
    fun getConversations(): Call<GetConversationsResponse>
    
    @GET("chat/{userId}/messages")
    fun getMessages(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Call<GetMessagesResponse>
    
    @POST("chat/{userId}/messages")
    fun sendMessage(
        @Path("userId") userId: String,
        @Body request: SendMessageRequest
    ): Call<ApiResponse<MessageDto>>
    
    @POST("chat/{userId}/mark-read")
    fun markAsRead(
        @Path("userId") userId: String
    ): Call<ApiResponse<String>>
}

