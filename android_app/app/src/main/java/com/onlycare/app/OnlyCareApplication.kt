package com.onlycare.app

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.onlycare.app.utils.CallNotificationManager
import com.onlycare.app.utils.FCMTokenManager
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class OnlyCareApplication : Application(), ImageLoaderFactory {
    
    companion object {
        private const val TAG = "OnlyCareApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channel for incoming calls
        initializeNotificationChannels()
        
        // Initialize FCM
        initializeFCM()
        
        Log.d(TAG, "✅ Coil ImageLoader configured with aggressive caching")
    }
    
    /**
     * Configure Coil ImageLoader with optimized caching for fast avatar loading
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of app's available memory for image cache
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024) // 100MB disk cache
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED) // Enable memory cache
            .diskCachePolicy(CachePolicy.ENABLED)   // Enable disk cache
            .networkCachePolicy(CachePolicy.ENABLED) // Enable network cache
            .crossfade(300) // Smooth fade-in animation (300ms)
            .respectCacheHeaders(false) // Ignore server cache headers, use our policy
            .logger(DebugLogger()) // Enable logging for debugging
            .build()
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
