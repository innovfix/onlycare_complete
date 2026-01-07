package com.onlycare.app.agora.token

import android.util.Log

/**
 * Agora Token Provider
 * 
 * Centralized token generation for testing
 * 
 * ⚠️ SECURITY WARNING:
 * This class contains hardcoded App Certificate for TESTING ONLY.
 * In production:
 * 1. Remove this class
 * 2. Generate tokens on secure backend server
 * 3. Fetch tokens via API calls
 */
object AgoraTokenProvider {
    
    private const val TAG = "AgoraTokenProvider"
    
    // ⚠️ NO CERTIFICATE - Tokens NOT required!
    // This project has NO App Certificate enabled
    private const val APP_ID = "8b5e9417f15a48ae929783f32d3d33d4"
    private const val APP_CERTIFICATE = "" // NO CERTIFICATE
    
    // Token expiration: 24 hours (in seconds)
    private const val TOKEN_EXPIRATION_SECONDS = 86400
    
    /**
     * Generate RTC token for audio/video call
     * 
     * CRITICAL: The UID used here MUST match the UID in joinChannel()!
     * 
     * @param channelName Channel name
     * @param uid User ID (default 0 for Agora dynamic assignment)
     * @param expirationSeconds Token validity duration (default 24 hours)
     * @return Generated token string
     */
    fun generateRtcToken(
        channelName: String,
        uid: Int = 0,
        expirationSeconds: Int = TOKEN_EXPIRATION_SECONDS
    ): String {
        return try {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🔐 GENERATING AGORA TOKEN (CLIENT-SIDE)")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "📝 Parameters:")
            Log.d(TAG, "  - App ID: $APP_ID")
            Log.d(TAG, "  - Channel: $channelName")
            Log.d(TAG, "  - UID: $uid")
            Log.d(TAG, "  - Expiration: $expirationSeconds seconds")
            Log.d(TAG, "  - Role: PUBLISHER (can send & receive)")
            
            val expireTimestamp = RtcTokenBuilder2.getExpireTimestamp(expirationSeconds)
            
            val token = RtcTokenBuilder2.buildTokenWithUid(
                appId = APP_ID,
                appCertificate = APP_CERTIFICATE,
                channelName = channelName,
                uid = uid,
                role = RtcTokenBuilder2.Role.PUBLISHER,
                tokenExpire = expireTimestamp,
                privilegeExpire = expireTimestamp
            )
            
            if (token.isEmpty()) {
                Log.e(TAG, "❌ Token generation FAILED - returned empty string")
                Log.e(TAG, "   Check App ID and Certificate format")
            } else {
                Log.i(TAG, "✅ Token generated successfully")
                Log.d(TAG, "  - Token length: ${token.length} characters")
                Log.d(TAG, "  - Token prefix: ${token.take(20)}...")
                Log.d(TAG, "  - Expires at: ${expireTimestamp} (Unix timestamp)")
                
                // Calculate readable expiration
                val expiresIn = expireTimestamp - RtcTokenBuilder2.getCurrentTimestamp()
                val hours = expiresIn / 3600
                val minutes = (expiresIn % 3600) / 60
                Log.d(TAG, "  - Valid for: ${hours}h ${minutes}m")
            }
            
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            
            token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during token generation: ${e.message}", e)
            e.printStackTrace()
            ""
        }
    }
    
    /**
     * Generate token with custom role
     * 
     * @param channelName Channel name
     * @param uid User ID
     * @param role User role (PUBLISHER or SUBSCRIBER)
     * @param expirationSeconds Token validity duration
     * @return Generated token string
     */
    fun generateRtcTokenWithRole(
        channelName: String,
        uid: Int = 0,
        role: RtcTokenBuilder2.Role = RtcTokenBuilder2.Role.PUBLISHER,
        expirationSeconds: Int = TOKEN_EXPIRATION_SECONDS
    ): String {
        return try {
            val expireTimestamp = RtcTokenBuilder2.getExpireTimestamp(expirationSeconds)
            
            RtcTokenBuilder2.buildTokenWithUid(
                appId = APP_ID,
                appCertificate = APP_CERTIFICATE,
                channelName = channelName,
                uid = uid,
                role = role,
                tokenExpire = expireTimestamp,
                privilegeExpire = expireTimestamp
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate token with role: ${e.message}", e)
            ""
        }
    }
    
    /**
     * Check if credentials are configured
     * 
     * ⚠️ NOTE: This project has NO App Certificate
     * Tokens are NOT required for joining channels
     */
    fun areCredentialsConfigured(): Boolean {
        val isAppIdValid = APP_ID.isNotEmpty() && APP_ID.length == 32
        
        if (!isAppIdValid) {
            Log.e(TAG, "❌ Invalid Agora App ID!")
            Log.e(TAG, "  App ID length: ${APP_ID.length} (expected: 32)")
        } else {
            if (APP_CERTIFICATE.isEmpty()) {
                Log.i(TAG, "ℹ️ NO App Certificate configured")
                Log.i(TAG, "  → Tokens are NOT required")
                Log.i(TAG, "  → Can join channels without tokens (TEST MODE)")
            }
        }
        
        return isAppIdValid
    }
    
    /**
     * Check if certificate is enabled
     */
    fun isCertificateEnabled(): Boolean = APP_CERTIFICATE.isNotEmpty()
    
    /**
     * Get App ID (for debugging)
     */
    fun getAppId(): String = APP_ID
}

