package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface KycApiService {
    
    @GET("kyc/status")
    fun getKycStatus(): Call<KycStatusResponse>
    
    @POST("kyc/submit")
    fun submitKyc(
        @Body request: SubmitKycRequest
    ): Call<ApiResponse<String>>
}

