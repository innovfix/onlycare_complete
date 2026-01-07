package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ReferralApiService {
    
    @GET("referral/code")
    fun getReferralCode(): Call<ReferralCodeResponse>
    
    @POST("referral/apply")
    fun applyReferralCode(
        @Body request: ApplyReferralRequest
    ): Call<ApplyReferralResponse>
    
    @GET("referral/history")
    fun getReferralHistory(
        @Query("limit") limit: Int = 20
    ): Call<ReferralHistoryResponse>
}

