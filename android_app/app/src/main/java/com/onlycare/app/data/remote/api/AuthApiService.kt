package com.onlycare.app.data.remote.api

import com.onlycare.app.data.remote.dto.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    
    @POST("auth/send-otp")
    fun sendOtp(
        @Body request: SendOtpRequest
    ): Call<SendOtpResponse>
    
    @POST("auth/verify-otp")
    fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Call<VerifyOtpResponse>
    
    @POST("auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>
    
    @POST("auth/refresh-token")
    fun refreshToken(): Call<ApiResponse<Map<String, String>>>
    
    @POST("auth/logout")
    fun logout(): Call<ApiResponse<String>>

    /**
     * Truecaller OAuth login (skip OTP)
     * Backend exchanges authorization code + code_verifier for verified phone number and issues token.
     *
     * NOTE: This endpoint is expected at /api/v1/login (BASE_URL already includes /api/v1/).
     */
    @FormUrlEncoded
    @POST("login")
    fun loginWithTruecaller(
        @Field("mobile") mobile: String,
        @Field("phone") phone: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String
    ): Call<TruecallerLoginResponse>
    
    @Multipart
    @POST("auth/update-pancard")
    fun updatePanCard(
        @Part("pancard_name") pancardName: RequestBody,
        @Part("pancard_number") pancardNumber: RequestBody
    ): Call<UpdatePanCardResponse>
    
    @Multipart
    @POST("auth/update-upi")
    fun updateUpi(
        @Part("upi_id") upiId: RequestBody
    ): Call<UpdateUpiResponse>
    
    @Multipart
    @POST("auth/update-voice")
    fun updateVoice(
        @Part voice: okhttp3.MultipartBody.Part
    ): Call<UpdateVoiceResponse>
}

