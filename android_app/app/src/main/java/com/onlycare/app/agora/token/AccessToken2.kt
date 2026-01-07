package com.onlycare.app.agora.token

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

/**
 * Agora AccessToken2 Implementation
 * Based on Agora's official token generation algorithm
 * 
 * ⚠️ WARNING: This is for TESTING only!
 * In production, generate tokens on a secure backend server.
 */
class AccessToken2(
    private val appId: String,
    private val appCertificate: String,
    private val channelName: String,
    private val uid: String
) {
    companion object {
        private const val VERSION = "007"
        private const val HMAC_SHA256_ALGORITHM = "HmacSHA256"
        
        // Service types
        const val SERVICE_RTC = 1
        
        // Privileges
        const val PRIVILEGE_JOIN_CHANNEL = 1
        const val PRIVILEGE_PUBLISH_AUDIO_STREAM = 2
        const val PRIVILEGE_PUBLISH_VIDEO_STREAM = 3
        const val PRIVILEGE_PUBLISH_DATA_STREAM = 4
    }
    
    private val salt: Int = Random().nextInt()
    private val ts: Int = (System.currentTimeMillis() / 1000 + 24 * 3600).toInt()
    private val messages: MutableMap<Int, String> = mutableMapOf()
    
    /**
     * Add privilege with expiration timestamp
     */
    fun addPrivilege(privilege: Int, expireTimestamp: Int) {
        messages[privilege] = expireTimestamp.toString()
    }
    
    /**
     * Build the token string
     */
    fun build(): String {
        if (!isUUIDValid(appId)) {
            return ""
        }
        
        if (!isUUIDValid(appCertificate)) {
            return ""
        }
        
        val messageBytes = packMessage()
        val signature = generateSignature(appCertificate, messageBytes)
        
        val packedContent = ByteBuffer()
        packedContent.putBytes(signature)
        packedContent.putInt(salt)
        packedContent.putInt(ts)
        packedContent.putShort(messageBytes.size.toShort())
        packedContent.putBytes(messageBytes)
        
        val content = packedContent.toByteArray()
        val encoded = Base64.encodeToString(content, Base64.NO_WRAP or Base64.NO_PADDING)
        
        return "$VERSION$appId$encoded"
    }
    
    private fun packMessage(): ByteArray {
        val buffer = ByteBuffer()
        
        // Service type (RTC = 1)
        buffer.putShort(SERVICE_RTC.toShort())
        
        // Channel name
        buffer.putString(channelName)
        
        // UID
        buffer.putString(uid)
        
        // Privileges
        buffer.putShort(messages.size.toShort())
        messages.forEach { (privilege, expire) ->
            buffer.putShort(privilege.toShort())
            buffer.putInt(expire.toInt())
        }
        
        return buffer.toByteArray()
    }
    
    private fun generateSignature(key: String, message: ByteArray): ByteArray {
        return try {
            val mac = Mac.getInstance(HMAC_SHA256_ALGORITHM)
            val secretKey = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM)
            mac.init(secretKey)
            mac.doFinal(message)
        } catch (e: Exception) {
            android.util.Log.e("AccessToken2", "Failed to generate signature: ${e.message}")
            byteArrayOf()
        }
    }
    
    private fun isUUIDValid(uuid: String): Boolean {
        if (uuid.isEmpty() || uuid.length != 32) {
            return false
        }
        
        val hex = uuid.toLowerCase(Locale.getDefault())
        for (c in hex) {
            if (!(c in '0'..'9' || c in 'a'..'f')) {
                return false
            }
        }
        return true
    }
}

/**
 * ByteBuffer helper class for packing data
 */
class ByteBuffer {
    private val buffer = mutableListOf<Byte>()
    
    fun putShort(value: Short) {
        buffer.add((value.toInt() shr 8).toByte())
        buffer.add(value.toByte())
    }
    
    fun putInt(value: Int) {
        buffer.add((value shr 24).toByte())
        buffer.add((value shr 16).toByte())
        buffer.add((value shr 8).toByte())
        buffer.add(value.toByte())
    }
    
    fun putString(value: String) {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        putShort(bytes.size.toShort())
        putBytes(bytes)
    }
    
    fun putBytes(bytes: ByteArray) {
        buffer.addAll(bytes.toList())
    }
    
    fun toByteArray(): ByteArray {
        return buffer.toByteArray()
    }
}






