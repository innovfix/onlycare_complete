package com.onlycare.app

import android.app.Application
import android.util.Log
import com.onlycare.app.utils.CallNotificationManager
import com.onlycare.app.utils.FCMTokenManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OnlyCareApplication : Application() {
    
    companion object {
        private const val TAG = "OnlyCareApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channel for incoming calls
        initializeNotificationChannels()
        
        // Initialize FCM
        initializeFCM()
    }
    
    /**
     * Create notification channels for incoming calls
     */
    private fun initializeNotificationChannels() {
        try {
            CallNotificationManager.createNotificationChannel(this)
            Log.d(TAG, "Notification channels created")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification channels", e)
        }
    }
    
    /**
     * Initialize Firebase Cloud Messaging
     */
    private fun initializeFCM() {
        try {
            FCMTokenManager.initializeFCM(this)
            Log.d(TAG, "FCM initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FCM", e)
        }
    }
}
