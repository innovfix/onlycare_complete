package com.onlycare.app.agora.token

/**
 * Agora RTC Token Builder (Version 2)
 * 
 * Generates tokens for Agora RTC SDK 4.x
 * 
 * ⚠️ WARNING: This exposes your App Certificate in the app code!
 * Only use this for TESTING. In production, generate tokens on backend server.
 * 
 * Usage:
 * ```
 * val token = RtcTokenBuilder2.buildTokenWithUid(
 *     appId = "your_app_id",
 *     appCertificate = "your_app_certificate",
 *     channelName = "test_channel",
 *     uid = 0,
 *     role = Role.PUBLISHER,
 *     expireTimestamp = System.currentTimeMillis() / 1000 + 3600
 * )
 * ```
 */
object RtcTokenBuilder2 {
    
    /**
     * User role in the channel
     */
    enum class Role(val value: Int) {
        /**
         * Publisher: Can send and receive streams
         */
        PUBLISHER(1),
        
        /**
         * Subscriber: Can only receive streams
         */
        SUBSCRIBER(2)
    }
    
    /**
     * Build RTC token with numeric user ID
     * 
     * @param appId Agora App ID (32-character hex string)
     * @param appCertificate Agora App Certificate (32-character hex string)
     * @param channelName Channel name (must not be empty)
     * @param uid User ID (0 for dynamic assignment by Agora, or specific user ID)
     * @param role User role (PUBLISHER or SUBSCRIBER)
     * @param tokenExpire Token expiration timestamp (Unix timestamp in seconds)
     * @param privilegeExpire Privilege expiration timestamp (Unix timestamp in seconds)
     * @return Generated token string, or empty string on error
     */
    fun buildTokenWithUid(
        appId: String,
        appCertificate: String,
        channelName: String,
        uid: Int,
        role: Role,
        tokenExpire: Int,
        privilegeExpire: Int = tokenExpire
    ): String {
        return buildTokenWithAccount(
            appId = appId,
            appCertificate = appCertificate,
            channelName = channelName,
            account = uid.toString(),
            role = role,
            tokenExpire = tokenExpire,
            privilegeExpire = privilegeExpire
        )
    }
    
    /**
     * Build RTC token with string account
     * 
     * @param appId Agora App ID
     * @param appCertificate Agora App Certificate
     * @param channelName Channel name
     * @param account User account (string)
     * @param role User role
     * @param tokenExpire Token expiration timestamp
     * @param privilegeExpire Privilege expiration timestamp
     * @return Generated token string
     */
    fun buildTokenWithAccount(
        appId: String,
        appCertificate: String,
        channelName: String,
        account: String,
        role: Role,
        tokenExpire: Int,
        privilegeExpire: Int = tokenExpire
    ): String {
        return try {
            val token = AccessToken2(appId, appCertificate, channelName, account)
            
            // Add privileges based on role
            when (role) {
                Role.PUBLISHER -> {
                    // Publisher can join and publish audio/video/data
                    token.addPrivilege(AccessToken2.PRIVILEGE_JOIN_CHANNEL, privilegeExpire)
                    token.addPrivilege(AccessToken2.PRIVILEGE_PUBLISH_AUDIO_STREAM, privilegeExpire)
                    token.addPrivilege(AccessToken2.PRIVILEGE_PUBLISH_VIDEO_STREAM, privilegeExpire)
                    token.addPrivilege(AccessToken2.PRIVILEGE_PUBLISH_DATA_STREAM, privilegeExpire)
                }
                Role.SUBSCRIBER -> {
                    // Subscriber can only join (receive streams)
                    token.addPrivilege(AccessToken2.PRIVILEGE_JOIN_CHANNEL, privilegeExpire)
                }
            }
            
            token.build()
        } catch (e: Exception) {
            android.util.Log.e("RtcTokenBuilder2", "Failed to build token: ${e.message}", e)
            ""
        }
    }
    
    /**
     * Get current Unix timestamp in seconds
     */
    fun getCurrentTimestamp(): Int {
        return (System.currentTimeMillis() / 1000).toInt()
    }
    
    /**
     * Calculate expiration timestamp from now + seconds
     * 
     * @param expirationSeconds Number of seconds until expiration (e.g., 3600 = 1 hour)
     * @return Unix timestamp
     */
    fun getExpireTimestamp(expirationSeconds: Int = 3600): Int {
        return getCurrentTimestamp() + expirationSeconds
    }
}






