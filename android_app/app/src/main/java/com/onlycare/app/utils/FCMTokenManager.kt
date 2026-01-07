package com.onlycare.app.utils

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.onlycare.app.data.repository.ApiDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Manages FCM (Firebase Cloud Messaging) tokens
 * Retrieves, stores, and sends tokens to backend
 */
object FCMTokenManager {
    
    private const val TAG = "FCMTokenManager"
    private const val PREFS_NAME = "onlycare_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"
    
    private var repository: ApiDataRepository? = null
    
    /**
     * Initialize with repository for sending tokens to backend
     * Call this from Application onCreate or after Hilt injection is ready
     */
    fun setRepository(repo: ApiDataRepository) {
        repository = repo
        Log.d(TAG, "Repository set for FCM token updates")
    }
    
    /**
     * Get FCM token asynchronously
     */
    suspend fun getFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token retrieved: $token")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            null
        }
    }
    
    /**
     * Get FCM token synchronously with callback
     */
    fun getFCMTokenWithCallback(onTokenReceived: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM Token retrieved: $token")
                onTokenReceived(token)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting FCM token", e)
                onTokenReceived(null)
            }
    }
    
    /**
     * Save FCM token locally
     */
    fun saveTokenLocally(context: Context, token: String) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
            Log.d(TAG, "FCM token saved locally")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token locally", e)
        }
    }
    
    /**
     * Get saved FCM token from local storage
     */
    fun getSavedToken(context: Context): String? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_FCM_TOKEN, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting saved FCM token", e)
            null
        }
    }
    
    /**
     * Send FCM token to backend
     */
    suspend fun sendTokenToBackend(token: String): Boolean {
        return try {
            if (repository == null) {
                Log.w(TAG, "Repository not set, cannot send FCM token to backend")
                return false
            }
            
            Log.d(TAG, "Sending FCM token to backend")
            
            val result = repository!!.updateFCMToken(token)
            
            if (result.isSuccess) {
                Log.d(TAG, "✅ FCM token sent to backend successfully")
                true
            } else {
                Log.e(TAG, "❌ Failed to send FCM token: ${result.exceptionOrNull()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending FCM token to backend", e)
            false
        }
    }
    
    /**
     * Initialize FCM and get token
     * Call this when app starts or user logs in
     */
    fun initializeFCM(context: Context, sendToBackend: Boolean = false) {
        getFCMTokenWithCallback { token ->
            if (token != null) {
                // Save locally
                saveTokenLocally(context, token)
                
                // Send to backend if user is logged in
                if (sendToBackend && repository != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        sendTokenToBackend(token)
                    }
                } else {
                    Log.d(TAG, "FCM token ready. Backend sending skipped (sendToBackend=$sendToBackend, repository=${repository != null})")
                }
            }
        }
    }
    
    /**
     * Send current token to backend (call after user logs in)
     */
    fun sendCurrentTokenToBackend(context: Context) {
        val savedToken = getSavedToken(context)
        if (savedToken != null) {
            CoroutineScope(Dispatchers.IO).launch {
                sendTokenToBackend(savedToken)
            }
        } else {
            Log.w(TAG, "No saved FCM token found")
            // Try to get a new one
            initializeFCM(context, sendToBackend = true)
        }
    }
}

