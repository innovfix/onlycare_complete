package com.onlycare.app.services

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Manages ringtone playback and vibration for incoming calls
 * Plays system default ringtone in a loop until stopped
 */
class CallRingtoneManager(private val context: Context) {
    
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var isRinging = false
    
    companion object {
        private const val TAG = "CallRingtoneManager"
        
        // Vibration pattern: wait 0ms, vibrate 1000ms, wait 500ms, repeat
        private val VIBRATION_PATTERN = longArrayOf(0, 1000, 500)
    }
    
    init {
        setupVibrator()
    }
    
    /**
     * Setup vibrator for different Android versions
     */
    private fun setupVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    /**
     * Start ringing with system default ringtone and vibration
     */
    fun startRinging() {
        if (isRinging) {
            Log.d(TAG, "Already ringing, ignoring start request")
            return
        }
        
        try {
            Log.d(TAG, "Starting ringtone and vibration")
            
            // Get system default ringtone URI
            val ringtoneUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            
            if (ringtoneUri != null) {
                ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
                
                // Set audio attributes for ringtone
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone?.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    ringtone?.streamType = AudioManager.STREAM_RING
                }
                
                // Start playing ringtone in loop
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone?.isLooping = true
                }
                ringtone?.play()
                
                Log.d(TAG, "Ringtone started playing")
            } else {
                Log.w(TAG, "Could not get default ringtone URI")
            }
            
            // Start vibration
            startVibration()
            
            isRinging = true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ringtone", e)
        }
    }
    
    /**
     * Start vibration pattern
     */
    private fun startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Modern API with VibrationEffect
                val vibrationEffect = VibrationEffect.createWaveform(
                    VIBRATION_PATTERN,
                    0  // Repeat from index 0 (loop)
                )
                vibrator?.vibrate(vibrationEffect)
                Log.d(TAG, "Vibration started (API 26+)")
            } else {
                // Legacy API
                @Suppress("DEPRECATION")
                vibrator?.vibrate(VIBRATION_PATTERN, 0)
                Log.d(TAG, "Vibration started (Legacy API)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration", e)
        }
    }
    
    /**
     * Stop ringing and vibration
     */
    fun stopRinging() {
        if (!isRinging) {
            Log.d(TAG, "Not ringing, ignoring stop request")
            return
        }
        
        try {
            Log.d(TAG, "Stopping ringtone and vibration")
            
            // Stop ringtone
            ringtone?.stop()
            ringtone = null
            
            // Stop vibration
            vibrator?.cancel()
            
            isRinging = false
            
            Log.d(TAG, "Ringtone and vibration stopped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone", e)
        }
    }
    
    /**
     * Check if currently ringing
     */
    fun isCurrentlyRinging(): Boolean {
        return isRinging
    }
    
    /**
     * Release all resources
     */
    fun release() {
        stopRinging()
        ringtone = null
        vibrator = null
    }
}






