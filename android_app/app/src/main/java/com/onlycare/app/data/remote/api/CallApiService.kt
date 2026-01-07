package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface CallApiService {
    
    @POST("calls/initiate")
    fun initiateCall(
        @Body request: InitiateCallRequest
    ): Call<InitiateCallResponse>
    
    @POST("calls/{callId}/accept")
    fun acceptCall(
        @Path("callId") callId: String
    ): Call<ApiResponse<CallDto>>
    
    @POST("calls/{callId}/reject")
    fun rejectCall(
        @Path("callId") callId: String
    ): Call<ApiResponse<String>>
    
    @POST("calls/{callId}/cancel")
    fun cancelCall(
        @Path("callId") callId: String
    ): Call<ApiResponse<String>>
    
    @POST("calls/{callId}/end")
    fun endCall(
        @Path("callId") callId: String,
        @Body request: EndCallRequest
    ): Call<EndCallResponse>
    
    @POST("calls/{callId}/deduct")
    fun deductCallCoins(
        @Path("callId") callId: String,
        @Body request: EndCallRequest // Reusing EndCallRequest as it has duration
    ): Call<ApiResponse<WalletResponse>>
    
    @POST("calls/{callId}/rate")
    fun rateCall(
        @Path("callId") callId: String,
        @Body request: RateCallRequest
    ): Call<ApiResponse<String>>
    
    @GET("calls/history")
    fun getCallHistory(
        @Query("filter") filter: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Call<PaginatedResponse<CallDto>>
    
    @GET("calls/recent-sessions")
    fun getRecentSessions(
        @Query("limit") limit: Int = 10
    ): Call<ApiResponse<List<CallDto>>>
    
    @GET("calls/recent-callers")
    fun getRecentCallers(
        @Query("limit") limit: Int = 10
    ): Call<ApiResponse<List<RecentCallerDto>>>
    
    @GET("calls/incoming")
    fun getIncomingCalls(): Call<ApiResponse<List<IncomingCallDto>>>
    
    @GET("calls/{callId}")
    fun getCallStatus(
        @Path("callId") callId: String
    ): Call<ApiResponse<CallDto>>
}
