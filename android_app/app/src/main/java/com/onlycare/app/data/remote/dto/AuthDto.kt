package com.onlycare.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Send OTP Request
 */
data class SendOtpRequest(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("country_code")
    val countryCode: String = "+91"
)

/**
 * Send OTP Response
 */
data class SendOtpResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("otp_id")
    val otpId: String,
    
    @SerializedName("expires_in")
    val expiresIn: Int,
    
    @SerializedName("otp")
    val otp: String? = null // Only in debug mode
)

/**
 * Verify OTP Request
 */
data class VerifyOtpRequest(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("otp")
    val otp: String,
    
    @SerializedName("otp_id")
    val otpId: String
)

/**
 * Verify OTP Response
 */
data class VerifyOtpResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("user_exists")
    val userExists: Boolean,
    
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("user")
    val user: UserDto? = null
)

/**
 * Truecaller Login Response (supports multiple backend response shapes)
 *
 * Backend may return:
 * - { success, registered, token, data, usernumber }
 * OR
 * - { success, user_exists, access_token, user }
 */
data class TruecallerLoginResponse(
    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("message")
    val message: String? = null,

    // Shape A (documented /login)
    @SerializedName("registered")
    val registered: Boolean? = null,

    @SerializedName("token")
    val token: String? = null,

    @SerializedName("data")
    val data: UserDto? = null,

    @SerializedName("usernumber")
    val userNumber: String? = null,

    // Shape B (OTP-style)
    @SerializedName("user_exists")
    val userExists: Boolean? = null,

    @SerializedName("access_token")
    val accessToken: String? = null,

    @SerializedName("user")
    val user: UserDto? = null
)

/**
 * Register Request
 */
data class RegisterRequest(
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("gender")
    val gender: String, // "MALE" or "FEMALE"
    
    @SerializedName("avatar_id")
    val avatar: String, // Avatar ID (as string) - backend expects "avatar_id" field name
    
    @SerializedName("language")
    val language: String,
    
    // Female/Creator specific fields (optional for male users)
    @SerializedName("age")
    val age: Int? = null,
    
    @SerializedName("interests")
    val interests: List<String>? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    // Referral code (optional)
    @SerializedName("referral_code")
    val referralCode: String? = null
)

/**
 * Register Response
 */
data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("user")
    val user: UserDto
)

/**
 * Update PAN Card Response Data
 */
data class UpdatePanCardData(
    @SerializedName("pancard_name")
    val pancardName: String,
    
    @SerializedName("pancard_number")
    val pancardNumber: String,
    
    @SerializedName("verified_name")
    val verifiedName: String
)

/**
 * Update PAN Card Response
 */
data class UpdatePanCardResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: UpdatePanCardData
)

/**
 * Update UPI Response Data
 */
data class UpdateUpiData(
    @SerializedName("upi_id")
    val upiId: String,
    
    @SerializedName("verified_name")
    val verifiedName: String
)

/**
 * Update UPI Response
 */
data class UpdateUpiResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: UpdateUpiData
)

/**
 * Update Voice Response Data
 */
data class UpdateVoiceData(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("user_gender")
    val userGender: String,
    
    @SerializedName("image")
    val image: String? = null,
    
    @SerializedName("gender")
    val gender: String,
    
    @SerializedName("language")
    val language: String,
    
    @SerializedName("age")
    val age: Int,
    
    @SerializedName("mobile")
    val mobile: String,
    
    @SerializedName("interests")
    val interests: String? = null,
    
    @SerializedName("describe_yourself")
    val describeYourself: String? = null,
    
    @SerializedName("voice")
    val voice: String,
    
    @SerializedName("is_verified")
    val isVerified: Boolean,
    
    @SerializedName("verified_datetime")
    val verifiedDatetime: String? = null,
    
    @SerializedName("voice_gender")
    val voiceGender: String? = null,
    
    @SerializedName("balance")
    val balance: Int? = null,
    
    @SerializedName("audio_status")
    val audioStatus: Int? = null,
    
    @SerializedName("video_status")
    val videoStatus: Int? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null
)

/**
 * Update Voice Response
 */
data class UpdateVoiceResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: UpdateVoiceData
)

