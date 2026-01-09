package com.onlycare.app.data.repository

import android.util.Log
import com.onlycare.app.BuildConfig
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.remote.api.*
import com.onlycare.app.data.remote.dto.*
import com.onlycare.app.data.remote.mapper.toDomainModel
import com.onlycare.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call as RetrofitCall
import retrofit2.Response
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation using real API calls
 */
@Singleton
class ApiDataRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val userApiService: UserApiService,
    private val callApiService: CallApiService,
    private val walletApiService: WalletApiService,
    private val earningsApiService: EarningsApiService,
    private val chatApiService: ChatApiService,
    private val kycApiService: KycApiService,
    private val friendApiService: FriendApiService,
    private val contentApiService: ContentApiService,
    private val referralApiService: ReferralApiService,
    private val avatarApiService: AvatarApiService,
    private val giftApiService: GiftApiService,
    private val sessionManager: SessionManager
) {
    
    companion object {
        private const val TAG = "ApiDataRepository"
    }

    private suspend fun <T> execute(call: RetrofitCall<T>): Response<T> {
        return withContext(Dispatchers.IO) { call.execute() }
    }

    private fun isDebugAuth(): Boolean {
        return sessionManager.getAuthToken()?.startsWith("debug_token_") == true
    }
    
    // ========================================
    // Authentication APIs
    // ========================================
    
    suspend fun sendOtp(phone: String, countryCode: String = "+91"): Result<SendOtpResponse> {
        return try {
            val request = SendOtpRequest(phone, countryCode)
            val response = execute(authApiService.sendOtp(request))
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to send OTP: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendOtp error", e)
            // NOTE: In release/minified builds Log.* may be stripped; printStackTrace ensures we still get
            // the real root cause in adb logcat for debugging (staging/debug only).
            if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE == "staging") {
                e.printStackTrace()
            }
            Result.failure(e)
        }
    }
    
    suspend fun verifyOtp(phone: String, otp: String, otpId: String): Result<VerifyOtpResponse> {
        return try {
            val request = VerifyOtpRequest(phone, otp, otpId)
            val response = execute(authApiService.verifyOtp(request))
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                // Save auth token
                sessionManager.saveAuthToken(body.accessToken)
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Verify OTP Failed. Code: ${response.code()}, Body: $errorBody")
                Result.failure(Exception("Invalid OTP: ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "verifyOtp error", e)
            if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE == "staging") {
                e.printStackTrace()
            }
            Result.failure(e)
        }
    }

    /**
     * Truecaller OAuth login (skip OTP).
     * Sends authorization code + code_verifier to backend; backend validates with Truecaller and returns session token/user.
     */
    suspend fun loginWithTruecaller(code: String, codeVerifier: String): Result<TruecallerLoginResponse> {
        return try {
            // Send both fields for maximum compatibility; backend may read "mobile" or "phone".
            val response = execute(authApiService.loginWithTruecaller(
                mobile = "0",
                phone = "0",
                code = code,
                codeVerifier = codeVerifier
            ))

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                // Save auth token if present (registered users typically get a token)
                val token = body.token ?: body.accessToken
                if (!token.isNullOrBlank()) {
                    sessionManager.saveAuthToken(token)
                }

                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Truecaller login failed. Code: ${response.code()}, Body: $errorBody")
                Result.failure(Exception("Truecaller login failed: ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "loginWithTruecaller error", e)
            Result.failure(e)
        }
    }
    
    suspend fun register(
        phone: String,
        gender: Gender,
        avatar: String,
        language: Language,
        age: Int? = null,
        interests: List<String>? = null,
        description: String? = null,
        referralCode: String? = null
    ): Result<RegisterResponse> {
        return try {
            val request = RegisterRequest(
                phone = phone,
                gender = gender.name, // Send uppercase: "MALE" or "FEMALE"
                avatar = avatar,
                language = language.name,
                age = age,
                interests = interests,
                description = description,
                referralCode = referralCode?.uppercase()?.takeIf { it.isNotBlank() }
            )
            
            // Log request data
            Log.d("restirernewuser", "═══════════════════════════════════════════════════════════")
            Log.d("restirernewuser", "📤 REGISTER API REQUEST")
            Log.d("restirernewuser", "═══════════════════════════════════════════════════════════")
            Log.d("restirernewuser", "Endpoint: POST /api/v1/auth/register")
            Log.d("restirernewuser", "")
            Log.d("restirernewuser", "Request Data:")
            Log.d("restirernewuser", "  - phone: $phone")
            Log.d("restirernewuser", "  - gender: ${gender.name}")
            Log.d("restirernewuser", "  - avatar: $avatar")
            Log.d("restirernewuser", "  - language: ${language.name}")
            Log.d("restirernewuser", "  - age: ${age ?: "null"}")
            Log.d("restirernewuser", "  - interests: ${interests?.joinToString(", ") ?: "null"}")
            Log.d("restirernewuser", "  - description: ${description ?: "null"}")
            Log.d("restirernewuser", "")
            Log.d("restirernewuser", "Making API call...")
            
            val response = execute(authApiService.register(request))
            
            // Log response details
            Log.d("restirernewuser", "")
            Log.d("restirernewuser", "═══════════════════════════════════════════════════════════")
            Log.d("restirernewuser", "📥 REGISTER API RESPONSE")
            Log.d("restirernewuser", "═══════════════════════════════════════════════════════════")
            Log.d("restirernewuser", "HTTP Status Code: ${response.code()}")
            Log.d("restirernewuser", "Is Successful: ${response.isSuccessful}")
            Log.d("restirernewuser", "Response Message: ${response.message()}")
            Log.d("restirernewuser", "")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("restirernewuser", "✅ SUCCESS - Registration successful")
                Log.d("restirernewuser", "")
                Log.d("restirernewuser", "Response Body:")
                Log.d("restirernewuser", "  - success: ${body.success}")
                Log.d("restirernewuser", "  - message: ${body.message}")
                Log.d("restirernewuser", "  - access_token: ${if (body.accessToken.isNotEmpty()) "${body.accessToken.take(20)}... (${body.accessToken.length} chars)" else "EMPTY"}")
                Log.d("restirernewuser", "  - user: ${if (body.user != null) "Present (ID: ${body.user.id}, Name: ${body.user.name})" else "null"}")
                Log.d("restirernewuser", "")
                
                // Save auth token
                sessionManager.saveAuthToken(body.accessToken)
                Log.d("restirernewuser", "✅ Auth token saved to SessionManager")
                Log.d("restirernewuser", "═══════════════════════════════════════════════════════════")
                
                Result.success(body)
            } else {
                // Log error response
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("restirernewuser", "❌ ERROR - Registration failed")
                Log.e("restirernewuser", "")
                Log.e("restirernewuser", "Error Details:")
                Log.e("restirernewuser", "  - HTTP Code: ${response.code()}")
                Log.e("restirernewuser", "  - Error Message: ${response.message()}")
                Log.e("restirernewuser", "  - Error Body: $errorBody")
                Log.e("restirernewuser", "═══════════════════════════════════════════════════════════")

                fun extractServerMessage(raw: String): String? {
                    // Common backend format: {"success":false,"message":"..."} or {"message":"..."}
                    val msgRegex = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
                    return msgRegex.find(raw)?.groupValues?.getOrNull(1)
                }

                val serverMsg = extractServerMessage(errorBody)
                val fallback = response.message().takeIf { it.isNotBlank() }
                    ?: "Registration failed (code ${response.code()})"

                Result.failure(Exception(serverMsg ?: errorBody.takeIf { it.isNotBlank() } ?: fallback))
            }
        } catch (e: Exception) {
            Log.e("restirernewuser", "═══════════════════════════════════════════════════════════")
            Log.e("restirernewuser", "❌ EXCEPTION - Registration error")
            Log.e("restirernewuser", "═══════════════════════════════════════════════════════════")
            Log.e("restirernewuser", "Exception Type: ${e.javaClass.simpleName}")
            Log.e("restirernewuser", "Exception Message: ${e.message}")
            Log.e("restirernewuser", "Stack Trace:", e)
            Log.e("restirernewuser", "═══════════════════════════════════════════════════════════")
            Log.e(TAG, "register error", e)
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            execute(authApiService.logout())
            sessionManager.clearAuthToken()
            sessionManager.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "logout error", e)
            // Even if API call fails, clear local session
            sessionManager.clearAuthToken()
            sessionManager.logout()
            Result.success(Unit)
        }
    }
    
    // ========================================
    // Avatar APIs
    // ========================================
    
    suspend fun getAvatars(gender: Gender): Result<List<AvatarDto>> {
        return try {
            // Convert Gender enum to lowercase string for API
            val genderString = gender.name.lowercase() // "male" or "female"
            val genderBody = genderString.toRequestBody(null)
            
            val response = execute(avatarApiService.getAvatars(genderBody))
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body.avatars)
                } else {
                    Result.failure(Exception("Failed to fetch avatars: ${response.message()}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "getAvatars failed. Code: ${response.code()}, Body: $errorBody")
                Result.failure(Exception("Failed to fetch avatars: ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getAvatars error", e)
            Result.failure(e)
        }
    }
    
    suspend fun updatePanCard(
        pancardName: String,
        pancardNumber: String
    ): Result<UpdatePanCardResponse> {
        val logTag = "panverify"
        Log.d(logTag, "🚀 updatePanCard() METHOD CALLED - pancardName: $pancardName, pancardNumber: $pancardNumber")
        return try {
            // Log request data
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "📤 UPDATE PAN CARD API REQUEST")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "Endpoint: POST /api/v1/auth/update-pancard")
            Log.d(logTag, "")
            Log.d(logTag, "Request Data:")
            Log.d(logTag, "  - pancard_name: $pancardName")
            Log.d(logTag, "  - pancard_number: $pancardNumber")
            Log.d(logTag, "")
            Log.d(logTag, "Making API call...")
            
            // Convert String to RequestBody for multipart/form-data
            // Using null media type for form-data text fields
            val pancardNameBody = pancardName.trim().toRequestBody("text/plain".toMediaType())
            val pancardNumberBody = pancardNumber.trim().uppercase().toRequestBody("text/plain".toMediaType())
            
            val response = execute(authApiService.updatePanCard(pancardNameBody, pancardNumberBody))
            
            // Log response details
            Log.d(logTag, "")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "📥 UPDATE PAN CARD API RESPONSE")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "HTTP Status Code: ${response.code()}")
            Log.d(logTag, "Is Successful: ${response.isSuccessful}")
            Log.d(logTag, "Response Message: ${response.message()}")
            Log.d(logTag, "")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                if (!body.success) {
                    Log.e(logTag, "❌ PAN verification failed: ${body.message}")
                    return Result.failure(Exception(body.message.ifBlank { "Failed to verify PAN card" }))
                }

                Log.d(logTag, "✅ SUCCESS - PAN Card verified successfully")
                Log.d(logTag, "")
                Log.d(logTag, "Response Body:")
                Log.d(logTag, "  - success: ${body.success}")
                Log.d(logTag, "  - message: ${body.message}")
                if (body.data != null) {
                    Log.d(logTag, "  - data.pancard_name: ${body.data.pancardName}")
                    Log.d(logTag, "  - data.pancard_number: ${body.data.pancardNumber}")
                    Log.d(logTag, "  - data.verified_name: ${body.data.verifiedName}")
                }
                Log.d(logTag, "")
                Log.d(logTag, "═══════════════════════════════════════════════════════════")
                
                Result.success(body)
            } else {
                // Log error response
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(logTag, "❌ ERROR - Update PAN Card failed")
                Log.e(logTag, "")
                Log.e(logTag, "Error Details:")
                Log.e(logTag, "  - HTTP Code: ${response.code()}")
                Log.e(logTag, "  - Error Message: ${response.message()}")
                Log.e(logTag, "  - Error Body: $errorBody")
                Log.e(logTag, "═══════════════════════════════════════════════════════════")
                
                val errorMsg = parseApiErrorMessage(
                    errorBodyString = errorBody,
                    httpCode = response.code(),
                    fallback = "Failed to update PAN card"
                )
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "❌ EXCEPTION - Update PAN Card error")
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "Exception Type: ${e.javaClass.simpleName}")
            Log.e(logTag, "Exception Message: ${e.message}")
            Log.e(logTag, "Stack Trace:", e)
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    suspend fun updateUpi(upiId: String): Result<UpdateUpiResponse> {
        val logTag = "upiverify"
        Log.d(logTag, "🚀 updateUpi() METHOD CALLED - upiId: $upiId")
        return try {
            // Log request data
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "📤 UPDATE UPI API REQUEST")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "Endpoint: POST /api/v1/auth/update-upi")
            Log.d(logTag, "")
            Log.d(logTag, "Request Data:")
            Log.d(logTag, "  - upi_id: $upiId")
            Log.d(logTag, "")
            Log.d(logTag, "Making API call...")
            
            // Convert String to RequestBody for multipart/form-data
            // Using null media type for form-data text fields
            val upiIdBody = upiId.trim().toRequestBody("text/plain".toMediaType())
            
            val response = execute(authApiService.updateUpi(upiIdBody))
            
            // Log response details
            Log.d(logTag, "")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "📥 UPDATE UPI API RESPONSE")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "HTTP Status Code: ${response.code()}")
            Log.d(logTag, "Is Successful: ${response.isSuccessful}")
            Log.d(logTag, "Response Message: ${response.message()}")
            Log.d(logTag, "")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                if (!body.success) {
                    Log.e(logTag, "❌ UPI verification failed: ${body.message}")
                    return Result.failure(Exception(body.message.ifBlank { "Failed to verify UPI" }))
                }

                Log.d(logTag, "✅ SUCCESS - UPI verified successfully")
                Log.d(logTag, "")
                Log.d(logTag, "Response Body:")
                Log.d(logTag, "  - success: ${body.success}")
                Log.d(logTag, "  - message: ${body.message}")
                if (body.data != null) {
                    Log.d(logTag, "  - data.upi_id: ${body.data.upiId}")
                    Log.d(logTag, "  - data.verified_name: ${body.data.verifiedName}")
                }
                Log.d(logTag, "")
                Log.d(logTag, "═══════════════════════════════════════════════════════════")
                
                Result.success(body)
            } else {
                // Log error response
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(logTag, "❌ ERROR - Update UPI failed")
                Log.e(logTag, "")
                Log.e(logTag, "Error Details:")
                Log.e(logTag, "  - HTTP Code: ${response.code()}")
                Log.e(logTag, "  - Error Message: ${response.message()}")
                Log.e(logTag, "  - Error Body: $errorBody")
                Log.e(logTag, "═══════════════════════════════════════════════════════════")
                
                val errorMsg = parseApiErrorMessage(
                    errorBodyString = errorBody,
                    httpCode = response.code(),
                    fallback = "Failed to verify UPI"
                )
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "❌ EXCEPTION - Update UPI error")
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "Exception Type: ${e.javaClass.simpleName}")
            Log.e(logTag, "Exception Message: ${e.message}")
            Log.e(logTag, "Stack Trace:", e)
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    suspend fun updateVoice(voiceFile: java.io.File): Result<UpdateVoiceResponse> {
        val logTag = "voiceupload"
        Log.d(logTag, "🚀 updateVoice() METHOD CALLED - voiceFile: ${voiceFile.name}")
        return try {
            // Log request data
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "📤 UPDATE VOICE API REQUEST")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "Endpoint: POST /api/v1/auth/update-voice")
            Log.d(logTag, "")
            Log.d(logTag, "Request Data:")
            Log.d(logTag, "  - voice file: ${voiceFile.name}")
            Log.d(logTag, "  - file size: ${voiceFile.length()} bytes")
            Log.d(logTag, "")
            Log.d(logTag, "Making API call...")
            
            // Create multipart body part for the voice file
            // Use audio/mp4 MIME type for M4A files
            val mimeType = "audio/mp4"
            
            Log.d(logTag, "  - MIME type: $mimeType")
            
            val requestFile = okhttp3.RequestBody.create(
                mimeType.toMediaType(),
                voiceFile
            )
            val voicePart = okhttp3.MultipartBody.Part.createFormData(
                "voice",
                voiceFile.name,
                requestFile
            )
            
            val response = execute(authApiService.updateVoice(voicePart))
            
            // Log response details
            Log.d(logTag, "")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "📥 UPDATE VOICE API RESPONSE")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "HTTP Status Code: ${response.code()}")
            Log.d(logTag, "Is Successful: ${response.isSuccessful}")
            Log.d(logTag, "Response Message: ${response.message()}")
            Log.d(logTag, "")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(logTag, "✅ SUCCESS - Voice updated successfully")
                Log.d(logTag, "")
                Log.d(logTag, "Response Body:")
                Log.d(logTag, "  - success: ${body.success}")
                Log.d(logTag, "  - message: ${body.message}")
                if (body.data != null) {
                    Log.d(logTag, "  - data.user_gender: ${body.data.userGender}")
                    Log.d(logTag, "  - data.gender: ${body.data.gender}")
                    Log.d(logTag, "  - data.voice: ${body.data.voice}")
                    Log.d(logTag, "  - data.is_verified: ${body.data.isVerified}")
                    Log.d(logTag, "  - data.voice_gender: ${body.data.voiceGender}")
                    Log.d(logTag, "  - data.audio_status: ${body.data.audioStatus}")
                    Log.d(logTag, "  - data.video_status: ${body.data.videoStatus}")
                }
                Log.d(logTag, "")
                Log.d(logTag, "═══════════════════════════════════════════════════════════")
                
                // Update session with ALL user data from voice response
                // IMPORTANT: Update gender to match what backend detected/returned
                sessionManager.updateUserProfile(
                    gender = body.data.gender, // Update actual user gender
                    voice = body.data.voice,
                    isVerified = body.data.isVerified,
                    verifiedDatetime = body.data.verifiedDatetime,
                    voiceGender = body.data.voiceGender,
                    audioStatus = body.data.audioStatus,
                    videoStatus = body.data.videoStatus
                )
                
                Log.d(logTag, "✅ Session updated with gender: ${body.data.gender}")
                
                Result.success(body)
            } else {
                // Log error response
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(logTag, "❌ ERROR - Update Voice failed")
                Log.e(logTag, "")
                Log.e(logTag, "Error Details:")
                Log.e(logTag, "  - HTTP Code: ${response.code()}")
                Log.e(logTag, "  - Error Message: ${response.message()}")
                Log.e(logTag, "  - Error Body: $errorBody")
                Log.e(logTag, "═══════════════════════════════════════════════════════════")
                
                Result.failure(Exception("Failed to update voice: ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "❌ EXCEPTION - Update Voice error")
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "Exception Type: ${e.javaClass.simpleName}")
            Log.e(logTag, "Exception Message: ${e.message}")
            Log.e(logTag, "Stack Trace:", e)
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    // ========================================
    // User APIs
    // ========================================
    
    suspend fun getCurrentUser(): Result<User> {
        return try {
            if (isDebugAuth()) {
                val audioStatus = sessionManager.getAudioStatus()
                val videoStatus = sessionManager.getVideoStatus()
                val audioEnabled = audioStatus == 1
                val videoEnabled = videoStatus == 1

                return Result.success(
                    User(
                        id = sessionManager.getUserId(),
                        name = sessionManager.getName(),
                        username = sessionManager.getUsername(),
                        age = sessionManager.getAge(),
                        gender = sessionManager.getGender(),
                        phone = sessionManager.getPhone(),
                        profileImage = sessionManager.getProfileImage(),
                        bio = sessionManager.getBio(),
                        language = sessionManager.getLanguage(),
                        coinBalance = sessionManager.getCoinBalance(),
                        audioCallEnabled = audioEnabled,
                        videoCallEnabled = videoEnabled,
                        isVerified = sessionManager.isVerified()
                    )
                )
            }

            val response = execute(userApiService.getCurrentUser())
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userData = response.body()?.data
                if (userData != null) {
                    val user = userData.toDomainModel()
                    // Keep local session availability flags in sync with backend so
                    // background/recents presence decisions remain correct.
                    sessionManager.updateUserProfile(
                        audioStatus = if (user.audioCallEnabled) 1 else 0,
                        videoStatus = if (user.videoCallEnabled) 1 else 0
                    )
                    Result.success(user)
                } else {
                    Result.failure(Exception("Failed to get user: No data"))
                }
            } else {
                Result.failure(Exception("Failed to get user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentUser error", e)
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUserDto(): Result<UserDto> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: GET /api/v1/users/me")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Making request...")
            
            val response = execute(userApiService.getCurrentUser())
            
            Log.d(TAG, "Response received:")
            Log.d(TAG, "   - HTTP Code: ${response.code()}")
            Log.d(TAG, "   - Success: ${response.isSuccessful}")
            Log.d(TAG, "   - Message: ${response.message()}")
            
            val responseBody = response.body()
            Log.d(TAG, "   - Body Success: ${responseBody?.success}")
            Log.d(TAG, "   - Body Data: ${if (responseBody?.data != null) "Present" else "Null"}")
            
            if (response.isSuccessful && responseBody?.success == true) {
                val userData = responseBody.data
                if (userData != null) {
                    Log.d(TAG, "✅ User data extracted successfully")
                    Log.d(TAG, "   - User ID: ${userData.id}")
                    Log.d(TAG, "   - Name: ${userData.name}")
                    Log.d(TAG, "   - isVerified: ${userData.isVerified}")
                    Log.d(TAG, "   - kycStatus: ${userData.kycStatus}")
                    Log.d(TAG, "   - verifiedDatetime: ${userData.verifiedDatetime}")
                    Log.d(TAG, "========================================")
                    Result.success(userData)
                } else {
                    Log.e(TAG, "❌ Response body data is null")
                    Log.d(TAG, "========================================")
                    Result.failure(Exception("Failed to get user: No data"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ API call failed")
                Log.e(TAG, "   - Error Body: $errorBody")
                Log.d(TAG, "========================================")
                Result.failure(Exception("Failed to get user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ EXCEPTION in getCurrentUserDto")
            Log.e(TAG, "========================================")
            Log.e(TAG, "Error: ${e.message}")
            Log.e(TAG, "Exception Type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            Log.e(TAG, "========================================")
            Result.failure(e)
        }
    }
    
    fun getFemaleUsers(
        online: Boolean? = null,
        verified: Boolean? = null,
        page: Int = 1
    ): Flow<Result<List<User>>> = flow {
        try {
            val response = execute(userApiService.getFemaleUsers(
                online = online,
                verified = verified,
                page = page,
                limit = 20
            ))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val users = response.body()?.data?.map { it.toDomainModel() } ?: emptyList()
                emit(Result.success(users))
            } else {
                emit(Result.failure(Exception("Failed to get users: ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFemaleUsers error", e)
            emit(Result.failure(e))
        }
    }
    
    suspend fun updateCallAvailability(
        audioCallEnabled: Boolean? = null,
        videoCallEnabled: Boolean? = null
    ): Result<String> {
        return try {
            if (isDebugAuth()) {
                // Demo mode: avoid backend 401; persist locally and succeed.
                sessionManager.updateUserProfile(
                    audioStatus = audioCallEnabled?.let { if (it) 1 else 0 },
                    videoStatus = videoCallEnabled?.let { if (it) 1 else 0 }
                )
                return Result.success("Updated (debug)")
            }

            val request = UpdateCallAvailabilityRequest(
                audioCallEnabled = audioCallEnabled,
                videoCallEnabled = videoCallEnabled,
                audioStatus = audioCallEnabled?.let { if (it) 1 else 0 },
                videoStatus = videoCallEnabled?.let { if (it) 1 else 0 }
            )
            
            Log.d(TAG, "Updating call availability - audio: $audioCallEnabled, video: $videoCallEnabled")
            
            val response = execute(userApiService.updateCallAvailability(request))
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    val message = body.data ?: body.message ?: "Call availability updated"
                    Log.d(TAG, "Call availability updated successfully: $message")
                    // Persist to session so presence logic works when app is in background/recents
                    sessionManager.updateUserProfile(
                        audioStatus = audioCallEnabled?.let { if (it) 1 else 0 },
                        videoStatus = videoCallEnabled?.let { if (it) 1 else 0 }
                    )
                    Result.success(message)
                } else {
                    // Parse error from successful response with error body
                    val errorMsg = body?.error?.message ?: body?.message ?: "Unknown error"
                    Log.e(TAG, "API returned error: $errorMsg (code: ${body?.error?.code})")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                // Parse error from error response body
                val errorBodyString = response.errorBody()?.string() ?: ""
                Log.e(TAG, "HTTP Error ${response.code()}: $errorBodyString")
                
                try {
                    // Try to parse error response using Gson
                    val gson = com.google.gson.GsonBuilder().create()
                    val errorResponse = gson.fromJson(errorBodyString, ApiResponse::class.java)
                    val errorMsg = errorResponse?.error?.message ?: errorResponse?.message ?: response.message()
                    Result.failure(Exception(errorMsg ?: "Failed to update call availability"))
                } catch (parseError: Exception) {
                    // If parsing fails, use raw error message
                    val errorMsg = if (errorBodyString.isNotEmpty()) {
                        errorBodyString
                    } else {
                        when (response.code()) {
                            403 -> "Access denied. Only female users can update call availability."
                            401 -> "Authentication required. Please login again."
                            422 -> "Invalid request. Please check your input."
                            500 -> "Server error. Please try again later."
                            else -> "Failed to update call availability (${response.code()})"
                        }
                    }
                    Log.e(TAG, "Failed to parse error response: ${parseError.message}")
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network error: Server not reachable", e)
            Result.failure(Exception("Cannot connect to server. Please check your internet connection."))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network error: Request timeout", e)
            Result.failure(Exception("Request timeout. Please try again."))
        } catch (e: Exception) {
            Log.e(TAG, "updateCallAvailability error", e)
            Result.failure(Exception("Failed to update call availability: ${e.message ?: "Unknown error"}"))
        }
    }
    
    /**
     * Update user online status
     * Call this when app starts (online) or goes to background (offline)
     */
    suspend fun updateOnlineStatus(isOnline: Boolean): Result<String> {
        return try {
            Log.d(TAG, "🔄 updateOnlineStatus: Setting status to ${if (isOnline) "ONLINE ✅" else "OFFLINE ❌"}")
            
            val request = UpdateStatusRequest(isOnline = isOnline)
            val response = execute(userApiService.updateStatus(request))
            
            Log.d(TAG, "updateOnlineStatus: Response code = ${response.code()}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val message = response.body()?.data ?: "Status updated"
                Log.d(TAG, "✅ updateOnlineStatus: Success! $message")
                Result.success(message)
            } else {
                val errorMsg = "Failed to update online status"
                Log.e(TAG, "❌ updateOnlineStatus: $errorMsg - Code: ${response.code()}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateOnlineStatus error", e)
            // Don't fail silently - this is important for call functionality
            Result.failure(Exception("Failed to update online status: ${e.message ?: "Unknown error"}"))
        }
    }
    
    /**
     * Update online datetime
     * Call this to update the user's online datetime timestamp
     */
    suspend fun updateOnlineDatetime(): Result<UpdateOnlineDatetimeResponse> {
        return try {
            Log.d(TAG, "🕐 updateOnlineDatetime: Updating online datetime")
            
            val response = execute(userApiService.updateOnlineDatetime())
            
            Log.d(TAG, "updateOnlineDatetime: Response code = ${response.code()}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Log.d(TAG, "✅ updateOnlineDatetime: Success! Online datetime: ${data.onlineDatetime}")
                    Result.success(data)
                } else {
                    Log.e(TAG, "❌ updateOnlineDatetime: Response data is null")
                    Result.failure(Exception("Response data is null"))
                }
            } else {
                val errorMsg = "Failed to update online datetime"
                Log.e(TAG, "❌ updateOnlineDatetime: $errorMsg - Code: ${response.code()}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network error: No internet connection", e)
            Result.failure(Exception("Cannot connect to server. Please check your internet connection."))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network error: Request timeout", e)
            Result.failure(Exception("Request timeout. Please try again."))
        } catch (e: Exception) {
            Log.e(TAG, "updateOnlineDatetime error", e)
            Result.failure(Exception("Failed to update online datetime: ${e.message ?: "Unknown error"}"))
        }
    }
    
    /**
     * Update FCM token for push notifications
     * Call this when FCM token is generated or refreshed
     */
    suspend fun updateFCMToken(fcmToken: String): Result<String> {
        return try {
            Log.d(TAG, "🔔 updateFCMToken: Sending FCM token to backend")
            
            val request = UpdateFCMTokenRequest(fcmToken = fcmToken)
            val response = execute(userApiService.updateFCMToken(request))
            
            Log.d(TAG, "updateFCMToken: Response code = ${response.code()}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val message = response.body()?.data ?: "FCM token updated successfully"
                Log.d(TAG, "✅ updateFCMToken: Success! $message")
                Result.success(message)
            } else {
                val errorMsg = "Failed to update FCM token"
                Log.e(TAG, "❌ updateFCMToken: $errorMsg - Code: ${response.code()}")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateFCMToken error", e)
            Result.failure(Exception("Failed to update FCM token: ${e.message ?: "Unknown error"}"))
        }
    }
    
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            Log.d(TAG, "getUserById: Fetching user with ID: $userId")
            val response = execute(userApiService.getUserById(userId))
            
            Log.d(TAG, "getUserById: Response code = ${response.code()}, isSuccessful = ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userData = response.body()?.data
                if (userData != null) {
                    Log.d(TAG, "getUserById: Success! User found: ${userData.name}")
                    val user = userData.toDomainModel()
                    Result.success(user)
                } else {
                    val errorMsg = "User not found"
                    Log.e(TAG, "getUserById: $errorMsg - Response body data is null")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                // Try to parse error from response body or error body
                val errorMessage = try {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.error != null) {
                        responseBody.error.message
                    } else {
                        val errorBodyString = response.errorBody()?.string()
                        if (errorBodyString != null) {
                            // Try to parse JSON error response
                            try {
                                val errorJson = org.json.JSONObject(errorBodyString)
                                errorJson.optJSONObject("error")?.optString("message") 
                                    ?: errorJson.optString("message") 
                                    ?: when (response.code()) {
                                        404 -> "User not found"
                                        403 -> "Access denied"
                                        401 -> "Authentication required"
                                        else -> "Failed to load user"
                                    }
                            } catch (e: Exception) {
                                when (response.code()) {
                                    404 -> "User not found"
                                    403 -> "Access denied"
                                    401 -> "Authentication required"
                                    else -> response.message() ?: "Failed to load user"
                                }
                            }
                        } else {
                            when (response.code()) {
                                404 -> "User not found"
                                403 -> "Access denied"
                                401 -> "Authentication required"
                                else -> response.message() ?: "Failed to load user"
                            }
                        }
                    }
                } catch (e: Exception) {
                    when (response.code()) {
                        404 -> "User not found"
                        403 -> "Access denied"
                        401 -> "Authentication required"
                        else -> response.message() ?: "Failed to load user"
                    }
                }
                
                Log.e(TAG, "getUserById: API error - Code: ${response.code()}, Message: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getUserById error: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message ?: "Unable to connect"}"))
        }
    }
    
    suspend fun updateProfile(
        username: String? = null,
        name: String? = null,
        interests: List<String>? = null,
        profileImage: String? = null
        // Note: age, bio, gender, language NOT allowed - set during registration only
    ): Result<User> {
        return try {
            val request = UpdateProfileRequest(
                username = username,
                name = name,
                interests = interests,
                profileImage = profileImage
            )
            
            // DETAILED DEBUG LOGGING
            Log.d("editprofilelog", "═══════════════════════════════════════════════════════════")
            Log.d("editprofilelog", "📤 UPDATE PROFILE API REQUEST")
            Log.d("editprofilelog", "═══════════════════════════════════════════════════════════")
            Log.d("editprofilelog", "Endpoint: PUT /api/v1/users/me")
            Log.d("editprofilelog", "")
            Log.d("editprofilelog", "Request Data:")
            Log.d("editprofilelog", "  - username: ${username ?: "null"}")
            Log.d("editprofilelog", "  - name: ${name ?: "null"} (username value)")
            Log.d("editprofilelog", "  - interests: ${interests?.joinToString(", ") ?: "null"}")
            Log.d("editprofilelog", "  - profile_image: ${profileImage ?: "null"} (avatar ID)")
            Log.d("editprofilelog", "")
            Log.d("editprofilelog", "Making API call...")
            
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "🔍 UPDATE PROFILE - STARTING")
            Log.e(TAG, "═══════════════════════════════════════")
            Log.e(TAG, "📝 Request Data:")
            Log.e(TAG, "   - username: $username")
            Log.e(TAG, "   - name: $name")
            Log.e(TAG, "   - interests: $interests")
            Log.e(TAG, "   - profileImage: $profileImage")
            Log.e(TAG, "")
            Log.e(TAG, "🌐 Making API call...")
            
            val response = execute(userApiService.updateProfile(request))
            
            // Log response details with editprofilelog tag
            Log.d("editprofilelog", "")
            Log.d("editprofilelog", "═══════════════════════════════════════════════════════════")
            Log.d("editprofilelog", "📥 UPDATE PROFILE API RESPONSE")
            Log.d("editprofilelog", "═══════════════════════════════════════════════════════════")
            Log.d("editprofilelog", "HTTP Status Code: ${response.code()}")
            Log.d("editprofilelog", "Is Successful: ${response.isSuccessful}")
            Log.d("editprofilelog", "Response Message: ${response.message()}")
            Log.d("editprofilelog", "")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val responseBody = response.body()
                Log.d("editprofilelog", "✅ SUCCESS - Profile update successful")
                Log.d("editprofilelog", "")
                Log.d("editprofilelog", "Response Body:")
                Log.d("editprofilelog", "  - success: ${responseBody?.success}")
                Log.d("editprofilelog", "  - message: ${responseBody?.message}")
                
                val userData = responseBody?.data
                if (userData == null) {
                    Log.e("editprofilelog", "❌ ERROR: Response data is NULL!")
                    Log.e("editprofilelog", "   API returned success but no user data")
                    Log.e("editprofilelog", "   Full response: $responseBody")
                    Log.e("editprofilelog", "═══════════════════════════════════════════════════════════")
                    return Result.failure(Exception("Profile updated but failed to retrieve user data"))
                }
                
                Log.d("editprofilelog", "")
                Log.d("editprofilelog", "User Data:")
                Log.d("editprofilelog", "  - id: ${userData.id}")
                Log.d("editprofilelog", "  - name: ${userData.name}")
                Log.d("editprofilelog", "  - username: ${userData.username ?: "null"}")
                Log.d("editprofilelog", "  - phone: ${userData.phone ?: "null"}")
                Log.d("editprofilelog", "  - age: ${userData.age ?: "null"}")
                Log.d("editprofilelog", "  - bio: ${userData.bio ?: "null"}")
                Log.d("editprofilelog", "  - gender: ${userData.gender}")
                Log.d("editprofilelog", "  - language: ${userData.language ?: "null"}")
                Log.d("editprofilelog", "  - profile_image: ${userData.profileImage ?: "null"}")
                Log.d("editprofilelog", "  - interests: ${userData.interests ?: "null"}")
                Log.d("editprofilelog", "  - coin_balance: ${userData.coinBalance ?: "null"}")
                Log.d("editprofilelog", "  - is_verified: ${userData.isVerified}")
                Log.d("editprofilelog", "  - audio_call_enabled: ${userData.audioCallEnabled}")
                Log.d("editprofilelog", "  - video_call_enabled: ${userData.videoCallEnabled}")
                Log.d("editprofilelog", "  - rating: ${userData.rating}")
                Log.d("editprofilelog", "  - total_ratings: ${userData.totalRatings}")
                Log.d("editprofilelog", "  - is_online: ${userData.isOnline}")
                Log.d("editprofilelog", "  - last_seen: ${userData.lastSeen ?: "null"}")
                Log.d("editprofilelog", "")
                Log.d("editprofilelog", "═══════════════════════════════════════════════════════════")
                
                val user = userData.toDomainModel()
                Log.e(TAG, "✅ SUCCESS! Profile updated")
                Log.e(TAG, "   User: ${user.name}")
                Log.e(TAG, "═══════════════════════════════════════")
                Result.success(user)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("editprofilelog", "❌ ERROR - Profile update failed")
                Log.e("editprofilelog", "")
                Log.e("editprofilelog", "Error Details:")
                Log.e("editprofilelog", "  - HTTP Code: ${response.code()}")
                Log.e("editprofilelog", "  - Error Message: ${response.message()}")
                Log.e("editprofilelog", "  - Error Body: $errorBody")
                Log.e("editprofilelog", "═══════════════════════════════════════════════════════════")
                
                Log.e(TAG, "❌ API ERROR!")
                Log.e(TAG, "   - Error Body: $errorBody")
                Log.e(TAG, "═══════════════════════════════════════")
                Result.failure(Exception("Failed to update profile: $errorBody"))
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "❌ UNKNOWN HOST ERROR!")
            Log.e(TAG, "   Cannot resolve domain name")
            Log.e(TAG, "   Possible causes:")
            Log.e(TAG, "   1. No internet connection")
            Log.e(TAG, "   2. DNS issue")
            Log.e(TAG, "   3. Wrong URL")
            Log.e(TAG, "   Error: ${e.message}", e)
            Log.e(TAG, "═══════════════════════════════════════")
            Result.failure(Exception("Cannot connect to server. Please check your internet connection."))
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "❌ CONNECTION REFUSED ERROR!")
            Log.e(TAG, "   Server refused connection")
            Log.e(TAG, "   Possible causes:")
            Log.e(TAG, "   1. Server not running")
            Log.e(TAG, "   2. Wrong port")
            Log.e(TAG, "   3. Firewall blocking")
            Log.e(TAG, "   Error: ${e.message}", e)
            Log.e(TAG, "═══════════════════════════════════════")
            Result.failure(Exception("Cannot connect to server. Server may be offline."))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "❌ TIMEOUT ERROR!")
            Log.e(TAG, "   Request took too long")
            Log.e(TAG, "   Possible causes:")
            Log.e(TAG, "   1. Slow internet")
            Log.e(TAG, "   2. Server overloaded")
            Log.e(TAG, "   Error: ${e.message}", e)
            Log.e(TAG, "═══════════════════════════════════════")
            Result.failure(Exception("Request timed out. Please try again."))
        } catch (e: javax.net.ssl.SSLException) {
            Log.e(TAG, "❌ SSL ERROR!")
            Log.e(TAG, "   HTTPS/SSL connection failed")
            Log.e(TAG, "   Possible causes:")
            Log.e(TAG, "   1. Invalid SSL certificate")
            Log.e(TAG, "   2. SSL handshake failed")
            Log.e(TAG, "   Error: ${e.message}", e)
            Log.e(TAG, "═══════════════════════════════════════")
            Result.failure(Exception("Secure connection failed. SSL error."))
        } catch (e: Exception) {
            Log.e(TAG, "❌ UNKNOWN ERROR!")
            Log.e(TAG, "   Type: ${e.javaClass.simpleName}")
            Log.e(TAG, "   Message: ${e.message}")
            Log.e(TAG, "   Stack trace:", e)
            Log.e(TAG, "═══════════════════════════════════════")
            Result.failure(Exception("Network error: ${e.javaClass.simpleName} - ${e.message}"))
        }
    }
    
    suspend fun blockUser(userId: String): Result<Unit> {
        return try {
            val response = execute(userApiService.blockUser(userId))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to block user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "blockUser error", e)
            Result.failure(e)
        }
    }
    
    suspend fun unblockUser(userId: String): Result<Unit> {
        return try {
            val response = execute(userApiService.unblockUser(userId))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to unblock user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "unblockUser error", e)
            Result.failure(e)
        }
    }
    
    suspend fun getBlockedUsers(): Result<List<User>> {
        return try {
            val response = execute(userApiService.getBlockedUsers())
            if (response.isSuccessful && response.body()?.success == true) {
                val users = response.body()?.data?.map { it.toDomainModel() } ?: emptyList()
                Result.success(users)
            } else {
                Result.failure(Exception("Failed to get blocked users: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getBlockedUsers error", e)
            Result.failure(e)
        }
    }
    
    // ========================================
    // Call APIs
    // ========================================
    
    suspend fun initiateCall(receiverId: String, callType: CallType): Result<InitiateCallResponse> {
        return try {
            val request = InitiateCallRequest(
                receiverId = receiverId,
                callType = callType.name
            )
            
            Log.d(TAG, "Initiating call - receiver: $receiverId, type: ${callType.name}")
            
            val response = execute(callApiService.initiateCall(request))
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Log the full response for debugging
                    Log.d(TAG, "Call initiation response body: success=${body.success}, call=${body.call}, message=${body.message}")
                    
                    // Validate response has call object
                    val callId = body.call?.id
                    if (callId != null && callId.isNotEmpty()) {
                        Log.d(TAG, "Call initiated successfully: $callId")
                        Result.success(body)
                    } else {
                    // Try to read raw response for debugging
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Call initiated but response.call is null or id is empty. Error body: $errorBody")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to read error body: ${e.message}")
                    }
                    Result.failure(Exception("Invalid response: Call object is missing or invalid. Please try again."))
                    }
                } else {
                    Log.e(TAG, "Call initiation response body is null")
                    Result.failure(Exception("Failed to initiate call: Empty response"))
                }
            } else {
                // Parse error response
                val errorBodyString = response.errorBody()?.string() ?: ""
                Log.e(TAG, "HTTP Error ${response.code()}: $errorBodyString")
                
                try {
                    val gson = com.google.gson.GsonBuilder().create()
                    val errorResponse = gson.fromJson(errorBodyString, ApiResponse::class.java)
                    val errorMsg = errorResponse?.error?.message ?: errorResponse?.message ?: ""
                    
                    if (errorMsg.isNotEmpty()) {
                        Log.e(TAG, "Parsed error message: $errorMsg")
                        Result.failure(Exception(errorMsg))
                    } else {
                        // If we can't parse the error, try to extract any message from JSON
                        val fallbackMsg = if (errorBodyString.contains("message")) {
                            try {
                                val json = org.json.JSONObject(errorBodyString)
                                json.optJSONObject("error")?.optString("message") 
                                    ?: json.optString("message")
                                    ?: "Failed to initiate call"
                            } catch (e: Exception) {
                                "Failed to initiate call"
                            }
                        } else {
                            when (response.code()) {
                                400 -> "Invalid request. Please check your input."
                                401 -> "Authentication required. Please login again."
                                403 -> "Access denied."
                                404 -> "User not found."
                                422 -> "Validation error. Please check your input."
                                500 -> "Server error. Please try again later."
                                else -> "Failed to initiate call (${response.code()})"
                            }
                        }
                        Result.failure(Exception(fallbackMsg))
                    }
                } catch (parseError: Exception) {
                    Log.e(TAG, "Error parsing error response: ${parseError.message}")
                    // If parsing completely fails, show the raw error or generic message
                    val errorMsg = if (errorBodyString.isNotEmpty() && errorBodyString.length < 200) {
                        errorBodyString
                    } else {
                        when (response.code()) {
                            400 -> "Request failed. Please try again."
                            401 -> "Authentication required. Please login again."
                            403 -> "Access denied."
                            404 -> "User not found."
                            422 -> "Validation error."
                            500 -> "Server error. Please try again later."
                            else -> "Failed to initiate call (${response.code()})"
                        }
                    }
                    Result.failure(Exception(errorMsg))
                }
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network error: Server not reachable", e)
            Result.failure(Exception("Cannot connect to server. Please check your internet connection."))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network error: Request timeout", e)
            Result.failure(Exception("Request timeout. Please try again."))
        } catch (e: Exception) {
            Log.e(TAG, "initiateCall error", e)
            Result.failure(Exception("Failed to initiate call: ${e.message ?: "Unknown error"}"))
        }
    }
    
    suspend fun endCall(callId: String, duration: Int): Result<EndCallResponse> {
        return try {
            val request = EndCallRequest(duration)
            val response = execute(callApiService.endCall(callId, request))
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to end call: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "endCall error", e)
            Result.failure(e)
        }
    }
    
    suspend fun rejectCall(callId: String): Result<String> {
        return try {
            Log.d(TAG, "Rejecting call: $callId")
            val response = execute(callApiService.rejectCall(callId))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val message = response.body()?.data ?: "Call rejected"
                Log.d(TAG, "Call rejected successfully: $callId")
                Result.success(message)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Log.e(TAG, "Failed to reject call: $errorMsg")
                Result.failure(Exception("Failed to reject call: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "rejectCall error", e)
            Result.failure(e)
        }
    }
    
    suspend fun cancelCall(callId: String): Result<String> {
        return try {
            Log.d(TAG, "Cancelling call: $callId")
            val response = execute(callApiService.cancelCall(callId))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val message = response.body()?.data ?: "Call cancelled"
                Log.d(TAG, "Call cancelled successfully: $callId")
                Result.success(message)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Log.e(TAG, "Failed to cancel call: $errorMsg")
                Result.failure(Exception("Failed to cancel call: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "cancelCall error", e)
            Result.failure(e)
        }
    }

    suspend fun acceptCall(callId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Accepting call: $callId")
            val response = execute(callApiService.acceptCall(callId))
            
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "Call accepted successfully: $callId")
                // No need to return call data - receiver already has it from IncomingCallDto
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Log.e(TAG, "Failed to accept call: $errorMsg")
                Result.failure(Exception("Failed to accept call: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "acceptCall error", e)
            Result.failure(e)
        }
    }

    suspend fun getCallStatus(callId: String): Result<CallDto> {
        return try {
            Log.d(TAG, "Getting call status: $callId")
            val response = execute(callApiService.getCallStatus(callId))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val call = response.body()?.data
                if (call != null) {
                    Log.d(TAG, "Call status retrieved: ${call.status}")
                    Result.success(call)
                } else {
                    Log.e(TAG, "Call data is null in response")
                    Result.failure(Exception("Call data not found"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Log.e(TAG, "Failed to get call status: $errorMsg")
                Result.failure(Exception("Failed to get call status: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCallStatus error", e)
            Result.failure(e)
        }
    }

    suspend fun deductCallCoins(callId: String, duration: Int): Result<WalletResponse> {
        return try {
            val request = EndCallRequest(duration) // Reusing EndCallRequest since it has duration
            val response = execute(callApiService.deductCallCoins(callId, request))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()
                if (body?.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Failed to deduct coins: No data"))
                }
            } else {
                Result.failure(Exception("Failed to deduct coins: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deductCallCoins error", e)
            Result.failure(e)
        }
    }
    
    suspend fun rateCall(callId: String, rating: Float, feedback: String?): Result<Unit> {
        return try {
            val request = RateCallRequest(rating, feedback)
            val response = execute(callApiService.rateCall(callId, request))
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to rate call: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "rateCall error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get incoming calls for the current user
     * This is polled by female users to detect incoming calls
     */
    suspend fun getIncomingCalls(): Result<List<IncomingCallDto>> {
        return try {
            val response = execute(callApiService.getIncomingCalls())
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Log.d(TAG, "Incoming calls fetched: ${body.data.size} calls")
                    
                    // Log Agora credentials for each incoming call
                    body.data.forEachIndexed { index, call ->
                        Log.d(TAG, "")
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "📞 INCOMING CALL #${index + 1} FROM API")
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "Call ID: ${call.id}")
                        Log.d(TAG, "Caller: ${call.callerName} (${call.callerId})")
                        Log.d(TAG, "Call Type: ${call.callType}")
                        Log.d(TAG, "Status: ${call.status}")
                        Log.d(TAG, "Balance Time: ${call.balanceTime ?: "NULL"}")
                        Log.d(TAG, "")
                        Log.d(TAG, "🔑 AGORA CREDENTIALS FROM API:")
                        Log.d(TAG, "========================================")
                        Log.d(TAG, "App ID: ${call.agoraAppId ?: "NULL"}")
                        Log.d(TAG, "Channel Name: ${call.channelName ?: "NULL"}")
                        Log.d(TAG, "Token: ${call.agoraToken ?: "NULL"}")
                        Log.d(TAG, "Token Length: ${call.agoraToken?.length ?: 0}")
                        Log.d(TAG, "Token Empty: ${call.agoraToken.isNullOrEmpty()}")
                        Log.d(TAG, "========================================")
                    }
                    
                    Result.success(body.data)
                } else {
                    Log.e(TAG, "No incoming calls data in response")
                    Result.success(emptyList())
                }
            } else {
                Log.e(TAG, "Failed to fetch incoming calls: ${response.code()}")
                Result.failure(Exception("Failed to fetch incoming calls"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getIncomingCalls error", e)
            Result.failure(e)
        }
    }
    
    fun getCallHistory(
        filter: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Flow<Result<List<Call>>> = flow {
        val logTag = "callhistory"
        try {
            // Log request
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "📞 CALL HISTORY API REQUEST")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "Endpoint: GET /api/v1/calls/history")
            Log.d(logTag, "")
            Log.d(logTag, "Request Parameters:")
            Log.d(logTag, "  - filter: ${filter ?: "none (default/recent)"}")
            Log.d(logTag, "  - page: $page")
            Log.d(logTag, "  - limit: $limit")
            Log.d(logTag, "")
            Log.d(logTag, "Making API call...")
            
            val response = execute(callApiService.getCallHistory(
                filter = filter,
                page = page,
                limit = limit
            ))
            
            // Log response
            Log.d(logTag, "")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "📥 CALL HISTORY API RESPONSE")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "HTTP Status Code: ${response.code()}")
            Log.d(logTag, "Is Successful: ${response.isSuccessful}")
            Log.d(logTag, "Response Message: ${response.message()}")
            Log.d(logTag, "")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val calls = body.data?.map { it.toDomainModel() } ?: emptyList()
                
                Log.d(logTag, "✅ SUCCESS - Call history loaded")
                Log.d(logTag, "")
                Log.d(logTag, "Response Data:")
                Log.d(logTag, "  - Total calls returned: ${calls.size}")
                Log.d(logTag, "  - Filter applied: ${filter ?: "recent (default)"}")
                Log.d(logTag, "  - Page: $page")
                Log.d(logTag, "")
                
                if (calls.isNotEmpty()) {
                    Log.d(logTag, "First 5 calls (preview):")
                    calls.take(5).forEachIndexed { index, call ->
                        Log.d(logTag, "  ${index + 1}. ${call.otherUserName} - ${formatDurationProminent(call.duration)} - ${call.callType}")
                    }
                    Log.d(logTag, "")
                }
                
                Log.d(logTag, "═══════════════════════════════════════════════════════════")
                
                emit(Result.success(calls))
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(logTag, "❌ ERROR - Get call history failed")
                Log.e(logTag, "")
                Log.e(logTag, "Error Details:")
                Log.e(logTag, "  - HTTP Code: ${response.code()}")
                Log.e(logTag, "  - Error Message: ${response.message()}")
                Log.e(logTag, "  - Error Body: $errorBody")
                Log.e(logTag, "═══════════════════════════════════════════════════════════")
                
                emit(Result.failure(Exception("Failed to get call history: $errorBody")))
            }
        } catch (e: Exception) {
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "❌ EXCEPTION - Get call history error")
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "Exception Type: ${e.javaClass.simpleName}")
            Log.e(logTag, "Exception Message: ${e.message}")
            Log.e(logTag, "Stack Trace:", e)
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            
            emit(Result.failure(e))
        }
    }
    
    private fun formatDurationProminent(seconds: Int): String {
        return when {
            seconds < 60 -> "$seconds sec"
            seconds < 3600 -> "${seconds / 60} min"
            else -> "${seconds / 3600} hr ${(seconds % 3600) / 60} min"
        }
    }
    
    // ========================================
    // Wallet APIs
    // ========================================
    
    fun getCoinPackages(): Flow<Result<List<CoinPackage>>> = flow {
        try {
            Log.d(TAG, "getCoinPackages: Calling API endpoint /wallet/packages")
            val response = execute(walletApiService.getPackages())
            
            Log.d(TAG, "getCoinPackages: Response code = ${response.code()}, isSuccessful = ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val packagesList = response.body()?.packages ?: emptyList()
                Log.d(TAG, "getCoinPackages: Received ${packagesList.size} packages from API")
                
                if (packagesList.isEmpty()) {
                    Log.w(TAG, "getCoinPackages: WARNING - API returned empty packages list!")
                } else {
                    packagesList.forEachIndexed { index, pkg ->
                        Log.d(TAG, "getCoinPackages: Package $index: ${pkg.coins} coins, ₹${pkg.price}, isPopular=${pkg.isPopular}, isBestValue=${pkg.isBestValue}")
                    }
                }
                
                val packages = packagesList.map { it.toDomainModel() }
                emit(Result.success(packages))
            } else {
                val errorMsg = "Failed to get packages: ${response.message()}, code: ${response.code()}"
                Log.e(TAG, "getCoinPackages: $errorMsg")
                if (response.body() != null) {
                    Log.e(TAG, "getCoinPackages: Response body = ${response.body()}")
                }
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // IMPORTANT: don't swallow cancellations (e.g., Flow.first()).
            // Swallowing and then emitting breaks Flow exception transparency.
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "getCoinPackages error: ${e.message}", e)
            emit(Result.failure(e))
        }
    }
    
    suspend fun getBestOffers(): Result<List<BestOfferDto>> {
        val logTag = "getBestOffers"
        Log.d(logTag, "═══════════════════════════════════════════════════════════")
        Log.d(logTag, "📤 GET BEST OFFERS API REQUEST")
        Log.d(logTag, "═══════════════════════════════════════════════════════════")
        Log.d(logTag, "Endpoint: POST /api/v1/wallet/best-offers")
        Log.d(logTag, "")
        Log.d(logTag, "Making API call...")
        
        return try {
            val response = execute(walletApiService.getBestOffers())
            
            // Log response details
            Log.d(logTag, "")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "📥 GET BEST OFFERS API RESPONSE")
            Log.d(logTag, "═══════════════════════════════════════════════════════════")
            Log.d(logTag, "HTTP Status Code: ${response.code()}")
            Log.d(logTag, "Is Successful: ${response.isSuccessful}")
            Log.d(logTag, "Response Message: ${response.message()}")
            Log.d(logTag, "")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val responseBody = response.body()!!
                val offers = responseBody.data ?: emptyList()
                
                Log.d(logTag, "✅ API Call Successful!")
                Log.d(logTag, "Response Data:")
                Log.d(logTag, "  - Success: ${responseBody.success}")
                Log.d(logTag, "  - Message: ${responseBody.message}")
                Log.d(logTag, "  - Total Offers: ${responseBody.total}")
                Log.d(logTag, "  - Offers Count: ${offers.size}")
                Log.d(logTag, "")
                
                if (offers.isNotEmpty()) {
                    Log.d(logTag, "Offers Details:")
                    offers.forEachIndexed { index, offer ->
                        Log.d(logTag, "  Offer ${index + 1}:")
                        Log.d(logTag, "    - ID: ${offer.id}")
                        Log.d(logTag, "    - Coins: ${offer.coins}")
                        Log.d(logTag, "    - Price: ₹${offer.price}")
                        Log.d(logTag, "    - Discount Price: ₹${offer.discountPrice}")
                        Log.d(logTag, "    - Save: ₹${offer.save}")
                        Log.d(logTag, "    - Payment Gateway: ${offer.pg}")
                    }
                } else {
                    Log.w(logTag, "⚠️ No offers returned in response")
                }
                
                Log.d(logTag, "═══════════════════════════════════════════════════════════")
                Result.success(offers)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Failed to get best offers: ${response.message()}, code: ${response.code()}"
                
                Log.e(logTag, "❌ API Call Failed!")
                Log.e(logTag, "Error Details:")
                Log.e(logTag, "  - Status Code: ${response.code()}")
                Log.e(logTag, "  - Message: ${response.message()}")
                if (errorBody != null) {
                    Log.e(logTag, "  - Error Body: $errorBody")
                }
                Log.e(logTag, "═══════════════════════════════════════════════════════════")
                
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "❌ NETWORK ERROR")
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "Error: No internet connection", e)
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Result.failure(Exception("Cannot connect to server. Please check your internet connection."))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "❌ TIMEOUT ERROR")
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "Error: Request timeout", e)
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Result.failure(Exception("Request timeout. Please try again."))
        } catch (e: Exception) {
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "❌ EXCEPTION")
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Log.e(logTag, "Error: ${e.message}", e)
            Log.e(logTag, "Stack trace:")
            e.printStackTrace()
            Log.e(logTag, "═══════════════════════════════════════════════════════════")
            Result.failure(e)
        }
    }
    
    suspend fun getWalletBalance(): Result<Int> {
        return try {
            Log.d(TAG, "getWalletBalance: Calling API endpoint /wallet/balance")
            val response = execute(walletApiService.getBalance())
            
            Log.d(TAG, "getWalletBalance: Response code = ${response.code()}, isSuccessful = ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val balance = response.body()?.coinBalance ?: 0
                Log.d(TAG, "getWalletBalance: Success! Balance = $balance")
                Result.success(balance)
            } else {
                val errorMsg = "Failed to get balance: ${response.message()}, code: ${response.code()}"
                Log.e(TAG, "getWalletBalance: $errorMsg")
                if (response.body() != null) {
                    Log.e(TAG, "getWalletBalance: Response body = ${response.body()}")
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getWalletBalance error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    fun getTransactionHistory(): Flow<Result<List<com.onlycare.app.domain.model.Transaction>>> = flow {
        try {
            // Log request
            Log.d("transactiontag", "═══════════════════════════════════════════════════════════")
            Log.d("transactiontag", "📤 TRANSACTION HISTORY API REQUEST")
            Log.d("transactiontag", "═══════════════════════════════════════════════════════════")
            Log.d("transactiontag", "Endpoint: GET /api/v1/wallet/transactions")
            Log.d("transactiontag", "Parameters:")
            Log.d("transactiontag", "  - page: 1")
            Log.d("transactiontag", "  - limit: 50")
            Log.d("transactiontag", "")
            Log.d("transactiontag", "Making API call...")
            
            val response = execute(walletApiService.getTransactionHistory(page = 1, limit = 50))
            
            // Log response details
            Log.d("transactiontag", "")
            Log.d("transactiontag", "═══════════════════════════════════════════════════════════")
            Log.d("transactiontag", "📥 TRANSACTION HISTORY API RESPONSE")
            Log.d("transactiontag", "═══════════════════════════════════════════════════════════")
            Log.d("transactiontag", "HTTP Status Code: ${response.code()}")
            Log.d("transactiontag", "Is Successful: ${response.isSuccessful}")
            Log.d("transactiontag", "Response Message: ${response.message()}")
            Log.d("transactiontag", "")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Log.d("transactiontag", "✅ SUCCESS - Response received")
                Log.d("transactiontag", "")
                Log.d("transactiontag", "Response Body:")
                Log.d("transactiontag", "  - success: ${responseBody.success}")
                
                if (responseBody.success && responseBody.data != null) {
                    val transactions = responseBody.data.map { it.toDomainModel() }
                    Log.d("transactiontag", "")
                    Log.d("transactiontag", "Transaction Data:")
                    Log.d("transactiontag", "  - Total Transactions: ${transactions.size}")
                    
                    // Log pagination info if available
                    responseBody.pagination?.let { pagination ->
                        Log.d("transactiontag", "")
                        Log.d("transactiontag", "Pagination Info:")
                        Log.d("transactiontag", "  - current_page: ${pagination.currentPage}")
                        Log.d("transactiontag", "  - total_pages: ${pagination.totalPages}")
                        Log.d("transactiontag", "  - total_items: ${pagination.totalItems}")
                        Log.d("transactiontag", "  - per_page: ${pagination.perPage}")
                        Log.d("transactiontag", "  - has_next: ${pagination.hasNext}")
                        Log.d("transactiontag", "  - has_prev: ${pagination.hasPrev}")
                    }
                    
                    // Log first few transactions details
                    if (transactions.isNotEmpty()) {
                        Log.d("transactiontag", "")
                        Log.d("transactiontag", "Transaction Details (showing first ${minOf(5, transactions.size)}):")
                        transactions.take(5).forEachIndexed { index, transaction ->
                            Log.d("transactiontag", "")
                            Log.d("transactiontag", "  Transaction ${index + 1}:")
                            Log.d("transactiontag", "    - id: ${transaction.id}")
                            Log.d("transactiontag", "    - type: ${transaction.type}")
                            Log.d("transactiontag", "    - amount: ${transaction.amount}")
                            Log.d("transactiontag", "    - coins: ${transaction.coins}")
                            Log.d("transactiontag", "    - isCredit: ${transaction.isCredit}")
                            Log.d("transactiontag", "    - status: ${transaction.status}")
                            Log.d("transactiontag", "    - paymentMethod: ${transaction.paymentMethod}")
                            Log.d("transactiontag", "    - title: ${transaction.title}")
                            Log.d("transactiontag", "    - description: ${transaction.description}")
                            Log.d("transactiontag", "    - timestamp: ${transaction.timestamp}")
                        }
                        if (transactions.size > 5) {
                            Log.d("transactiontag", "  ... and ${transactions.size - 5} more transactions")
                        }
                    } else {
                        Log.d("transactiontag", "  - No transactions found")
                    }
                    
                    Log.d("transactiontag", "")
                    Log.d("transactiontag", "✅ Transaction data parsed successfully")
                    Log.d("transactiontag", "═══════════════════════════════════════════════════════════")
                    
                    emit(Result.success(transactions))
                } else {
                    Log.e("transactiontag", "❌ ERROR - Transaction data is null or empty")
                    Log.e("transactiontag", "  - Response success: ${responseBody.success}")
                    Log.e("transactiontag", "  - Data: ${if (responseBody.data == null) "null" else "empty list (${responseBody.data.size} items)"}")
                    Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                    
                    emit(Result.failure(Exception("Failed to get transactions: No data")))
                }
            } else {
                // Log error response
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("transactiontag", "❌ ERROR - Request failed")
                Log.e("transactiontag", "")
                Log.e("transactiontag", "Error Details:")
                Log.e("transactiontag", "  - HTTP Code: ${response.code()}")
                Log.e("transactiontag", "  - Error Message: ${response.message()}")
                Log.e("transactiontag", "  - Is Successful: ${response.isSuccessful}")
                Log.e("transactiontag", "  - Response Body: ${if (response.body() != null) "Present" else "null"}")
                Log.e("transactiontag", "  - Error Body: $errorBody")
                Log.e("transactiontag", "")
                
                // Special handling for different HTTP error codes
                when (response.code()) {
                    401 -> {
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                        Log.e("transactiontag", "⚠️ 401 UNAUTHORIZED - Authentication Failed")
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                        Log.e("transactiontag", "Possible causes:")
                        Log.e("transactiontag", "  1. User is not logged in (token is null/empty)")
                        Log.e("transactiontag", "  2. Token has expired")
                        Log.e("transactiontag", "  3. Token is invalid")
                        Log.e("transactiontag", "  4. Backend authentication middleware is rejecting the token")
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                    }
                    500 -> {
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                        Log.e("transactiontag", "🔥 500 INTERNAL SERVER ERROR - Backend Issue")
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                        Log.e("transactiontag", "This is a SERVER-SIDE error, not a client issue.")
                        Log.e("transactiontag", "")
                        Log.e("transactiontag", "Possible backend issues:")
                        Log.e("transactiontag", "  1. Database connection error")
                        Log.e("transactiontag", "  2. Server-side code exception/crash")
                        Log.e("transactiontag", "  3. Missing or invalid database query")
                        Log.e("transactiontag", "  4. Server resource exhaustion")
                        Log.e("transactiontag", "")
                        Log.e("transactiontag", "Error message from server: $errorBody")
                        Log.e("transactiontag", "")
                        Log.e("transactiontag", "⚠️ ACTION REQUIRED: Check backend server logs")
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                    }
                    502, 503, 504 -> {
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                        Log.e("transactiontag", "⚠️ ${response.code()} SERVER UNAVAILABLE")
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                        Log.e("transactiontag", "Server is temporarily unavailable or overloaded")
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                    }
                    404 -> {
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                        Log.e("transactiontag", "⚠️ 404 NOT FOUND - Endpoint may not exist")
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                        Log.e("transactiontag", "Check if the API endpoint is correct: GET /api/v1/wallet/transactions")
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                    }
                    422 -> {
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                        Log.e("transactiontag", "⚠️ 422 VALIDATION ERROR - Request parameters invalid")
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                        Log.e("transactiontag", "Check request parameters: page=1, limit=50")
                        Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                    }
                }
                
                // Try to parse error body if it's JSON
                if (errorBody.isNotEmpty() && errorBody.startsWith("{")) {
                    Log.e("transactiontag", "Error Body (Parsed):")
                    Log.e("transactiontag", "  $errorBody")
                }
                Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
                
                // Create more descriptive error message
                val errorMessage = when (response.code()) {
                    500 -> "Server error occurred. Please try again later or contact support."
                    401 -> "Authentication failed. Please login again."
                    404 -> "API endpoint not found. Please check backend configuration."
                    422 -> "Invalid request parameters."
                    502, 503, 504 -> "Server temporarily unavailable. Please try again later."
                    else -> "Failed to get transactions: ${response.message()}"
                }
                
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (e: Exception) {
            Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
            Log.e("transactiontag", "❌ EXCEPTION - Transaction history error")
            Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
            Log.e("transactiontag", "Exception Type: ${e.javaClass.simpleName}")
            Log.e("transactiontag", "Exception Message: ${e.message}")
            Log.e("transactiontag", "Stack Trace:", e)
            Log.e("transactiontag", "═══════════════════════════════════════════════════════════")
            Log.e(TAG, "getTransactionHistory error", e)
            emit(Result.failure(e))
        }
    }
    
    // ========================================
    // Chat APIs
    // ========================================
    
    fun getConversations(): Flow<Result<List<ChatConversation>>> = flow {
        try {
            val response = execute(chatApiService.getConversations())
            
            if (response.isSuccessful && response.body()?.success == true) {
                val conversations = response.body()?.conversations?.map { it.toDomainModel() } ?: emptyList()
                emit(Result.success(conversations))
            } else {
                emit(Result.failure(Exception("Failed to get conversations: ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getConversations error", e)
            emit(Result.failure(e))
        }
    }
    
    fun getMessages(userId: String): Flow<Result<List<Message>>> = flow {
        try {
            val response = execute(chatApiService.getMessages(userId, page = 1, limit = 100))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val messages = response.body()?.messages?.map { it.toDomainModel() } ?: emptyList()
                emit(Result.success(messages))
            } else {
                emit(Result.failure(Exception("Failed to get messages: ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMessages error", e)
            emit(Result.failure(e))
        }
    }
    
    suspend fun sendMessage(userId: String, message: String): Result<Message> {
        return try {
            val request = SendMessageRequest(message)
            val response = execute(chatApiService.sendMessage(userId, request))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val messageData = response.body()?.data
                if (messageData != null) {
                    val msg = messageData.toDomainModel()
                    Result.success(msg)
                } else {
                    Result.failure(Exception("Failed to send message: No data"))
                }
            } else {
                Result.failure(Exception("Failed to send message: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage error", e)
            Result.failure(e)
        }
    }
    
    // ========================================
    // Friends APIs
    // ========================================
    
    fun getFriends(): Flow<Result<FriendsData>> = flow {
        try {
            val response = execute(friendApiService.getFriends())
            
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()

                val friends = body?.friends?.map { it.toDomainModel() } ?: emptyList()
                val sentRequests = buildFriendRequests(
                    friendDtos = body?.sentRequests,
                    userDtos = body?.sentRequestUsers
                )
                val receivedRequests = buildFriendRequests(
                    friendDtos = body?.receivedRequests,
                    userDtos = body?.receivedRequestUsers
                )

                emit(
                    Result.success(
                        FriendsData(
                            friends = friends,
                            sentRequests = sentRequests,
                            receivedRequests = receivedRequests
                        )
                    )
                )
            } else {
                emit(Result.failure(Exception("Failed to get friends: ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFriends error", e)
            emit(Result.failure(e))
        }
    }

    private fun buildFriendRequests(
        friendDtos: List<FriendDto>?,
        userDtos: List<UserDto>?
    ): List<FriendRequest> {
        // Prefer explicit request objects if provided; otherwise fallback to direct user lists.
        val fromFriendDtos = friendDtos?.mapNotNull { dto ->
            val user = dto.user.toDomainModel()
            FriendRequest(
                userId = user.id,
                name = user.name,
                profileImage = user.profileImage.ifBlank { null },
                timestamp = parseIso8601ToMillis(dto.createdAt)
            )
        } ?: emptyList()

        if (fromFriendDtos.isNotEmpty()) return fromFriendDtos

        return userDtos?.map { u ->
            val user = u.toDomainModel()
            FriendRequest(
                userId = user.id,
                name = user.name,
                profileImage = user.profileImage.ifBlank { null },
                timestamp = System.currentTimeMillis()
            )
        } ?: emptyList()
    }

    private fun parseIso8601ToMillis(iso8601String: String?): Long {
        if (iso8601String.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            // ISO8601 format: "2024-11-17T13:49:00.000000Z" or "2024-11-17T13:49:00+00:00"
            val dateTimeString = iso8601String.replace("Z", "+00:00")
            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            OffsetDateTime.parse(dateTimeString, formatter).toInstant().toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }
    
    suspend fun sendFriendRequest(userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: SEND FRIEND REQUEST")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Target UserId: $userId")
            
            val response = execute(friendApiService.sendFriendRequest(userId))
            
            Log.d(TAG, "Response Code: ${response.code()}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ FRIEND REQUEST SUCCESS")
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                Log.e(TAG, "❌ FRIEND REQUEST FAILED")
                Log.e(TAG, "   Error Body: $errorBodyString")
                
                val errorMsg = parseApiErrorMessage(
                    errorBodyString = errorBodyString,
                    httpCode = response.code(),
                    fallback = response.message().ifBlank { "Failed to send friend request" }
                )
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ EXCEPTION: sendFriendRequest")
            Log.e(TAG, "   Error: ${e.message}")
            Result.failure(e)
        } finally {
            Log.d(TAG, "========================================")
        }
    }
    
    suspend fun acceptFriendRequest(userId: String): Result<Unit> {
        return try {
            val response = execute(friendApiService.acceptFriendRequest(userId))
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMsg = parseApiErrorMessage(
                    errorBodyString = errorBodyString,
                    httpCode = response.code(),
                    fallback = response.message().ifBlank { "Failed to accept friend request" }
                )
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "acceptFriendRequest error", e)
            Result.failure(e)
        }
    }
    
    suspend fun rejectFriendRequest(userId: String): Result<Unit> {
        return try {
            val response = execute(friendApiService.rejectFriendRequest(userId))
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMsg = parseApiErrorMessage(
                    errorBodyString = errorBodyString,
                    httpCode = response.code(),
                    fallback = response.message().ifBlank { "Failed to reject friend request" }
                )
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "rejectFriendRequest error", e)
            Result.failure(e)
        }
    }
    
    suspend fun removeFriend(userId: String): Result<Unit> {
        return try {
            val response = execute(friendApiService.removeFriend(userId))
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMsg = parseApiErrorMessage(
                    errorBodyString = errorBodyString,
                    httpCode = response.code(),
                    fallback = response.message().ifBlank { "Failed to remove friend" }
                )
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "removeFriend error", e)
            Result.failure(e)
        }
    }

    private fun parseApiErrorMessage(
        errorBodyString: String?,
        httpCode: Int,
        fallback: String
    ): String {
        if (errorBodyString.isNullOrBlank()) {
            return when (httpCode) {
                404 -> "User not found"
                401 -> "Authentication required. Please login again."
                403 -> "Access denied"
                429 -> "Too many attempts. Please try again later."
                else -> fallback
            }
        }

        // Try JSON formats like:
        // {"success":false,"error":{"code":"NOT_FOUND","message":"User not found"}}
        // {"message":"..."}
        return try {
            val json = org.json.JSONObject(errorBodyString)
            json.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
                ?: json.optString("message").takeIf { it.isNotBlank() }
                ?: fallback
        } catch (_: Exception) {
            // If server returned plain text, surface it (but avoid dumping huge payloads)
            errorBodyString.take(200).ifBlank { fallback }
        }
    }
    
    // ========================================
    // Content APIs
    // ========================================
    
    suspend fun getPrivacyPolicy(): Result<PrivacyPolicyDataDto> {
        return try {
            val response = execute(contentApiService.getPrivacyPolicy())

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Failed to get privacy policy"))
                }
            } else {
                Result.failure(Exception("Failed to get privacy policy: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPrivacyPolicy error", e)
            Result.failure(e)
        }
    }
    
    suspend fun getTermsAndConditions(): Result<TermsDataDto> {
        return try {
            val response = execute(contentApiService.getTermsAndConditions())

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Failed to get terms and conditions"))
                }
            } else {
                Result.failure(Exception("Failed to get terms and conditions: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTermsAndConditions error", e)
            Result.failure(e)
        }
    }
    
    suspend fun getRefundPolicy(): Result<RefundPolicyDataDto> {
        return try {
            val response = execute(contentApiService.getRefundPolicy())

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Failed to get refund policy"))
                }
            } else {
                Result.failure(Exception("Failed to get refund policy: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getRefundPolicy error", e)
            Result.failure(e)
        }
    }
    
    suspend fun getCommunityGuidelines(): Result<CommunityGuidelinesDataDto> {
        return try {
            val response = execute(contentApiService.getCommunityGuidelines())

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Failed to get community guidelines"))
                }
            } else {
                Result.failure(Exception("Failed to get community guidelines: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCommunityGuidelines error", e)
            Result.failure(e)
        }
    }
    
    // ========================================
    // Earnings & Withdrawal APIs
    // ========================================
    
    /**
     * Get earnings dashboard
     */
    suspend fun getEarningsDashboard(): Result<EarningsDashboardDto> {
        return try {
            if (isDebugAuth()) {
                return Result.success(
                    EarningsDashboardDto(
                        totalEarnings = 0.0,
                        todayEarnings = 0.0,
                        weekEarnings = 0.0,
                        monthEarnings = 0.0,
                        availableBalance = 0.0,
                        pendingWithdrawals = 0.0,
                        totalCalls = 0,
                        todayCalls = 0,
                        averageCallDuration = 0,
                        averageEarningsPerCall = 0.0,
                        audioCallsCount = 0,
                        videoCallsCount = 0
                    )
                )
            }

            // Log request
            Log.d("earningtag", "═══════════════════════════════════════════════════════════")
            Log.d("earningtag", "📤 EARNINGS DASHBOARD API REQUEST")
            Log.d("earningtag", "═══════════════════════════════════════════════════════════")
            Log.d("earningtag", "Endpoint: GET /api/v1/earnings/dashboard")
            Log.d("earningtag", "")
            Log.d("earningtag", "Making API call...")
            
            val response = execute(earningsApiService.getEarningsDashboard())
            
            // Log response details
            Log.d("earningtag", "")
            Log.d("earningtag", "═══════════════════════════════════════════════════════════")
            Log.d("earningtag", "📥 EARNINGS DASHBOARD API RESPONSE")
            Log.d("earningtag", "═══════════════════════════════════════════════════════════")
            Log.d("earningtag", "HTTP Status Code: ${response.code()}")
            Log.d("earningtag", "Is Successful: ${response.isSuccessful}")
            Log.d("earningtag", "Response Message: ${response.message()}")
            Log.d("earningtag", "")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Log.d("earningtag", "✅ SUCCESS - Response received")
                Log.d("earningtag", "")
                Log.d("earningtag", "Response Body:")
                Log.d("earningtag", "  - success: ${responseBody.success}")
                
                if (responseBody.success && responseBody.dashboard != null) {
                    val dashboard = responseBody.dashboard
                    Log.d("earningtag", "")
                    Log.d("earningtag", "Dashboard Data:")
                    Log.d("earningtag", "  - total_earnings: ${dashboard.totalEarnings}")
                    Log.d("earningtag", "  - today_earnings: ${dashboard.todayEarnings}")
                    Log.d("earningtag", "  - week_earnings: ${dashboard.weekEarnings}")
                    Log.d("earningtag", "  - month_earnings: ${dashboard.monthEarnings}")
                    Log.d("earningtag", "  - available_balance: ${dashboard.availableBalance}")
                    Log.d("earningtag", "  - pending_withdrawals: ${dashboard.pendingWithdrawals}")
                    Log.d("earningtag", "  - total_calls: ${dashboard.totalCalls}")
                    Log.d("earningtag", "  - today_calls: ${dashboard.todayCalls}")
                    Log.d("earningtag", "  - average_call_duration: ${dashboard.averageCallDuration}")
                    Log.d("earningtag", "  - average_earnings_per_call: ${dashboard.averageEarningsPerCall}")
                    Log.d("earningtag", "  - audio_calls_count: ${dashboard.audioCallsCount}")
                    Log.d("earningtag", "  - video_calls_count: ${dashboard.videoCallsCount}")
                    Log.d("earningtag", "")
                    Log.d("earningtag", "✅ Dashboard data parsed successfully")
                    Log.d("earningtag", "═══════════════════════════════════════════════════════════")
                    
                    Result.success(dashboard)
                } else {
                    Log.e("earningtag", "❌ ERROR - Dashboard data is null")
                    Log.e("earningtag", "  - Response success: ${responseBody.success}")
                    Log.e("earningtag", "  - Dashboard: ${if (responseBody.dashboard == null) "null" else "present"}")
                    Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                    
                    Result.failure(Exception("Failed to get earnings dashboard: No data"))
                }
            } else {
                // Log error response
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("earningtag", "❌ ERROR - Request failed")
                Log.e("earningtag", "")
                Log.e("earningtag", "Error Details:")
                Log.e("earningtag", "  - HTTP Code: ${response.code()}")
                Log.e("earningtag", "  - Error Message: ${response.message()}")
                Log.e("earningtag", "  - Is Successful: ${response.isSuccessful}")
                Log.e("earningtag", "  - Response Body: ${if (response.body() != null) "Present" else "null"}")
                Log.e("earningtag", "  - Error Body: $errorBody")
                Log.e("earningtag", "")
                
                // Special handling for different HTTP error codes
                when (response.code()) {
                    401 -> {
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                        Log.e("earningtag", "⚠️ 401 UNAUTHORIZED - Authentication Failed")
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                        Log.e("earningtag", "Possible causes:")
                        Log.e("earningtag", "  1. User is not logged in (token is null/empty)")
                        Log.e("earningtag", "  2. Token has expired")
                        Log.e("earningtag", "  3. Token is invalid")
                        Log.e("earningtag", "  4. Backend authentication middleware is rejecting the token")
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                    }
                    500 -> {
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                        Log.e("earningtag", "🔥 500 INTERNAL SERVER ERROR - Backend Issue")
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                        Log.e("earningtag", "This is a SERVER-SIDE error, not a client issue.")
                        Log.e("earningtag", "")
                        Log.e("earningtag", "Possible backend issues:")
                        Log.e("earningtag", "  1. Database connection error")
                        Log.e("earningtag", "  2. Server-side code exception/crash")
                        Log.e("earningtag", "  3. Missing or invalid database query")
                        Log.e("earningtag", "  4. Server resource exhaustion")
                        Log.e("earningtag", "")
                        Log.e("earningtag", "Error message from server: $errorBody")
                        Log.e("earningtag", "")
                        Log.e("earningtag", "⚠️ ACTION REQUIRED: Check backend server logs")
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                    }
                    502, 503, 504 -> {
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                        Log.e("earningtag", "⚠️ ${response.code()} SERVER UNAVAILABLE")
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                        Log.e("earningtag", "Server is temporarily unavailable or overloaded")
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                    }
                    404 -> {
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                        Log.e("earningtag", "⚠️ 404 NOT FOUND - Endpoint may not exist")
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                        Log.e("earningtag", "Check if the API endpoint is correct: GET /api/v1/earnings/dashboard")
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                    }
                    422 -> {
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                        Log.e("earningtag", "⚠️ 422 VALIDATION ERROR - Request parameters invalid")
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                        Log.e("earningtag", "Check request parameters")
                        Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                    }
                }
                
                // Try to parse error body if it's JSON
                if (errorBody.isNotEmpty() && errorBody.startsWith("{")) {
                    Log.e("earningtag", "Error Body (Parsed):")
                    Log.e("earningtag", "  $errorBody")
                }
                Log.e("earningtag", "═══════════════════════════════════════════════════════════")
                
                // Create more descriptive error message
                val errorMessage = when (response.code()) {
                    500 -> "Server error occurred. Please try again later or contact support."
                    401 -> "Authentication failed. Please login again."
                    404 -> "API endpoint not found. Please check backend configuration."
                    422 -> "Invalid request parameters."
                    502, 503, 504 -> "Server temporarily unavailable. Please try again later."
                    else -> "Failed to get earnings dashboard: ${response.message()}"
                }
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("earningtag", "═══════════════════════════════════════════════════════════")
            Log.e("earningtag", "❌ EXCEPTION - Earnings dashboard error")
            Log.e("earningtag", "═══════════════════════════════════════════════════════════")
            Log.e("earningtag", "Exception Type: ${e.javaClass.simpleName}")
            Log.e("earningtag", "Exception Message: ${e.message}")
            Log.e("earningtag", "Stack Trace:", e)
            Log.e("earningtag", "═══════════════════════════════════════════════════════════")
            Log.e(TAG, "getEarningsDashboard error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Request withdrawal
     */
    suspend fun requestWithdrawal(amount: Double, bankAccountId: String): Result<RequestWithdrawalResponse> {
        return try {
            val request = RequestWithdrawalRequest(amount, bankAccountId)
            val response = execute(earningsApiService.requestWithdrawal(request))
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Failed to request withdrawal"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "requestWithdrawal error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get withdrawal history
     */
    fun getWithdrawalHistory(page: Int = 1, limit: Int = 20): Flow<Result<WithdrawalHistoryResponse>> = flow {
        try {
            val response = execute(earningsApiService.getWithdrawalHistory(page, limit))
            
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Result.success(response.body()!!))
            } else {
                emit(Result.failure(Exception("Failed to get withdrawal history: ${response.message()}")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getWithdrawalHistory error", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get bank accounts
     */
    suspend fun getBankAccounts(): Result<List<BankAccountDto>> {
        return try {
            val response = execute(earningsApiService.getBankAccounts())
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.bankAccounts ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get bank accounts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getBankAccounts error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add bank account
     */
    suspend fun addBankAccount(
        accountHolderName: String,
        accountNumber: String,
        ifscCode: String,
        bankName: String? = null,
        upiId: String? = null
    ): Result<BankAccountDto> {
        return try {
            val request = AddBankAccountRequest(
                accountHolderName, accountNumber, ifscCode, bankName, upiId
            )
            val response = execute(earningsApiService.addBankAccount(request))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val bankAccount = response.body()?.data
                if (bankAccount != null) {
                    Result.success(bankAccount)
                } else {
                    Result.failure(Exception("Bank account data is null"))
                }
            } else {
                Result.failure(Exception("Failed to add bank account: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "addBankAccount error", e)
            Result.failure(e)
        }
    }
    
    // ========================================
    // Referral APIs
    // ========================================
    
    suspend fun getReferralCode(): Result<ReferralCodeData> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: GET REFERRAL CODE")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Endpoint: GET /referral/code")
            val response = execute(referralApiService.getReferralCode())
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Success: ${response.isSuccessful}")
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()
                if (body != null) {
                    val data = body.toDomainModel()
                    Log.d(
                        TAG,
                        "✅ ReferralCodeData: code=${data.referralCode}, myInvites=${data.myInvites}, rewardType=${data.rewardType}, coinsPerInvite=${data.coinsPerInvite}, rupeesPerInvite=${data.rupeesPerInvite}, totalCoins=${data.totalCoinsEarned}, totalRupees=${data.totalRupeesEarned}"
                    )
                    Log.d(TAG, "========================================")
                    Result.success(data)
                } else {
                    Log.e(TAG, "❌ getReferralCode: body is null")
                    Log.d(TAG, "========================================")
                    Result.failure(Exception("Failed to get referral code: No data"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ getReferralCode failed: ${response.message()}")
                Log.e(TAG, "   errorBody: $errorBody")
                Log.d(TAG, "========================================")
                Result.failure(Exception("Failed to get referral code"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getReferralCode error", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
    
    suspend fun applyReferralCode(code: String): Result<ApplyReferralResponse> {
        return try {
            val request = ApplyReferralRequest(referralCode = code.uppercase())
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: APPLY REFERRAL CODE")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Endpoint: POST /referral/apply")
            Log.d(TAG, "Request: referral_code=${request.referralCode}")
            val response = execute(referralApiService.applyReferralCode(request))
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Success: ${response.isSuccessful}")
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                Log.d(TAG, "✅ applyReferralCode success: message=${body.message}, bonus_coins=${body.bonusCoins}, referrer_bonus=${body.referrerBonus}, new_balance=${body.newBalance}")
                Log.d(TAG, "========================================")
                Result.success(body)
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                Log.e(TAG, "❌ applyReferralCode failed: $errorMsg")
                Log.d(TAG, "========================================")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "applyReferralCode error", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
    
    suspend fun getReferralHistory(): Result<ReferralHistoryData> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: GET REFERRAL HISTORY")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Endpoint: GET /referral/history")
            val response = execute(referralApiService.getReferralHistory())
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Success: ${response.isSuccessful}")
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()
                if (body != null) {
                    val data = body.toDomainModel()
                    Log.d(TAG, "✅ ReferralHistoryData: totalReferrals=${data.totalReferrals}, totalEarnings=${data.totalEarnings}, items=${data.referrals.size}")
                    data.referrals.take(10).forEachIndexed { idx, item ->
                        Log.d(TAG, "   [$idx] ${item.userName}: coins=${item.coinsEarned}, rupees=${item.rupeesEarned}, rewardType=${item.rewardType}, claimed=${item.isClaimed}")
                    }
                    Log.d(TAG, "========================================")
                    Result.success(data)
                } else {
                    Log.e(TAG, "❌ getReferralHistory: body is null")
                    Log.d(TAG, "========================================")
                    Result.failure(Exception("Failed to get referral history: No data"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ getReferralHistory failed: ${response.message()}")
                Log.e(TAG, "   errorBody: $errorBody")
                Log.d(TAG, "========================================")
                Result.failure(Exception("Failed to get referral history"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getReferralHistory error", e)
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
    
    // ========================================
    // Gift APIs
    // ========================================
    
    suspend fun getGiftImages(): Result<List<GiftData>> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: GET GIFT IMAGES")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Endpoint: POST /auth/gifts_list")
            Log.d(TAG, "Request: (no parameters)")
            Log.d(TAG, "Status: Sending request...")
            
            val response = execute(giftApiService.getGiftImages())
            
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Success: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Log.d(TAG, "✅ API SUCCESS: ${body.data.size} gifts found")
                    body.data.forEachIndexed { index, gift ->
                        Log.d(TAG, "   Gift ${index + 1}: ID=${gift.id}, Coins=${gift.coins}")
                    }
                    Log.d(TAG, "========================================")
                    Result.success(body.data)
                } else {
                    Log.e(TAG, "❌ API returned success=false: ${body.message}")
                    Log.d(TAG, "========================================")
                    Result.failure(Exception("API returned success=false: ${body.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ API FAILED")
                Log.e(TAG, "   Code: ${response.code()}")
                Log.e(TAG, "   Body: $errorBody")
                Log.d(TAG, "========================================")
                Result.failure(Exception("Failed to fetch gifts: ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ EXCEPTION: getGiftImages")
            Log.e(TAG, "   Error: ${e.message}")
            Log.e(TAG, "   Stack: ${e.stackTraceToString()}")
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
    
    suspend fun sendGift(
        userId: String,
        receiverId: String,
        giftId: Int
    ): Result<SendGiftData> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: SEND GIFT")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Endpoint: POST /auth/send_gifts")
            Log.d(TAG, "Request Parameters:")
            Log.d(TAG, "   user_id: $userId")
            Log.d(TAG, "   receiver_id: $receiverId")
            Log.d(TAG, "   gift_id: $giftId")
            Log.d(TAG, "Status: Sending request...")
            
            val response = execute(giftApiService.sendGift(userId, receiverId, giftId))
            
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Success: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Log.d(TAG, "✅ API SUCCESS: Gift sent successfully")
                    Log.d(TAG, "   Gift Icon: ${body.data!!.giftIcon}")
                    Log.d(TAG, "   Gift Coins: ${body.data!!.giftCoins}")
                    Log.d(TAG, "   Sender: ${body.data!!.senderName}")
                    Log.d(TAG, "   Receiver: ${body.data!!.receiverName}")
                    Log.d(TAG, "========================================")
                    Result.success(body.data!!)
                } else {
                    Log.e(TAG, "❌ API returned success=false: ${body.message}")
                    Log.d(TAG, "========================================")
                    Result.failure(Exception("API returned success=false: ${body.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ API FAILED")
                Log.e(TAG, "   Code: ${response.code()}")
                Log.e(TAG, "   Body: $errorBody")
                Log.d(TAG, "========================================")
                Result.failure(Exception("Failed to send gift: ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ EXCEPTION: sendGift")
            Log.e(TAG, "   Error: ${e.message}")
            Log.e(TAG, "   Stack: ${e.stackTraceToString()}")
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
    
    suspend fun getRemainingTime(
        userId: String,
        callType: String
    ): Result<String> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: GET REMAINING TIME")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Endpoint: POST /auth/get_remaining_time")
            Log.d(TAG, "Request Parameters:")
            Log.d(TAG, "   user_id: $userId")
            Log.d(TAG, "   call_type: $callType")
            Log.d(TAG, "Status: Sending request...")
            
            val response = execute(giftApiService.getRemainingTime(userId, callType))
            
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Success: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Log.d(TAG, "✅ API SUCCESS: Remaining time fetched")
                    Log.d(TAG, "   Remaining Time: ${body.data!!.remainingTime}")
                    Log.d(TAG, "========================================")
                    Result.success(body.data!!.remainingTime)
                } else {
                    Log.e(TAG, "❌ API returned success=false: ${body.message}")
                    Log.d(TAG, "========================================")
                    Result.failure(Exception("API returned success=false: ${body.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ API FAILED")
                Log.e(TAG, "   Code: ${response.code()}")
                Log.e(TAG, "   Body: $errorBody")
                Log.d(TAG, "========================================")
                Result.failure(Exception("Failed to get remaining time: ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ EXCEPTION: getRemainingTime")
            Log.e(TAG, "   Error: ${e.message}")
            Log.e(TAG, "   Stack: ${e.stackTraceToString()}")
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
    
    suspend fun sendGiftNotification(
        senderId: String,
        receiverId: String,
        giftId: Int,
        giftIcon: String,
        giftCoins: Int,
        callType: String
    ): Result<String> {
        return try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📡 API CALL: SEND GIFT FCM NOTIFICATION")
            Log.d(TAG, "========================================")
            Log.d(TAG, "Endpoint: POST /auth/send_gift_notification")
            Log.d(TAG, "Request Parameters:")
            Log.d(TAG, "   sender_id: $senderId")
            Log.d(TAG, "   receiver_id: $receiverId")
            Log.d(TAG, "   gift_id: $giftId")
            Log.d(TAG, "   gift_icon: $giftIcon")
            Log.d(TAG, "   gift_coins: $giftCoins")
            Log.d(TAG, "   call_type: $callType")
            Log.d(TAG, "Status: Sending request...")
            
            val response = execute(
                giftApiService.sendGiftNotification(senderId, receiverId, giftId, giftIcon, giftCoins, callType)
            )
            
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Success: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Log.d(TAG, "✅ API SUCCESS: FCM notification sent")
                    Log.d(TAG, "   Message: ${body.message}")
                    Log.d(TAG, "   Female user will receive gift notification")
                    Log.d(TAG, "========================================")
                    Result.success(body.message ?: "Notification sent")
                } else {
                    Log.e(TAG, "❌ API returned success=false: ${body.message}")
                    Log.d(TAG, "========================================")
                    Result.failure(Exception("API returned success=false: ${body.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ API FAILED")
                Log.e(TAG, "   Code: ${response.code()}")
                Log.e(TAG, "   Body: $errorBody")
                Log.d(TAG, "========================================")
                Result.failure(Exception("Failed to send gift notification: ${errorBody ?: response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ EXCEPTION: sendGiftNotification")
            Log.e(TAG, "   Error: ${e.message}")
            Log.e(TAG, "   Stack: ${e.stackTraceToString()}")
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
}

