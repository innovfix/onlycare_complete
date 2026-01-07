package com.onlycare.app.utils

import android.util.Log

/**
 * Utility functions for time parsing and formatting
 */
object TimeUtils {
    
    private const val TAG = "TimeUtils"
    
    /**
     * Parse balance time string from backend to seconds
     * 
     * Supports formats:
     * - "MM:SS" (e.g., "25:00" = 25 minutes)
     * - "HH:MM:SS" (e.g., "1:30:00" = 1 hour 30 minutes)
     * 
     * @param balanceTime Time string from backend
     * @return Total seconds, or 0 if parsing fails
     */
    fun parseBalanceTime(balanceTime: String?): Int {
        if (balanceTime.isNullOrEmpty()) {
            Log.w(TAG, "Balance time is null or empty")
            return 0
        }
        
        return try {
            val parts = balanceTime.split(":")
            
            when (parts.size) {
                2 -> {
                    // MM:SS format
                    val minutes = parts[0].toIntOrNull() ?: 0
                    val seconds = parts[1].toIntOrNull() ?: 0
                    val totalSeconds = minutes * 60 + seconds
                    
                    Log.d(TAG, "Parsed balance time '$balanceTime' → $totalSeconds seconds ($minutes min $seconds sec)")
                    totalSeconds
                }
                3 -> {
                    // HH:MM:SS format
                    val hours = parts[0].toIntOrNull() ?: 0
                    val minutes = parts[1].toIntOrNull() ?: 0
                    val seconds = parts[2].toIntOrNull() ?: 0
                    val totalSeconds = hours * 3600 + minutes * 60 + seconds
                    
                    Log.d(TAG, "Parsed balance time '$balanceTime' → $totalSeconds seconds ($hours hr $minutes min $seconds sec)")
                    totalSeconds
                }
                else -> {
                    Log.e(TAG, "Invalid balance time format: $balanceTime (expected MM:SS or HH:MM:SS)")
                    0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse balance time: $balanceTime", e)
            0
        }
    }
    
    /**
     * Format seconds to time string for display
     * 
     * @param seconds Total seconds
     * @return Formatted string (e.g., "25:00", "1:30:00", "0:45")
     */
    fun formatTime(seconds: Int): String {
        val absSeconds = kotlin.math.abs(seconds)
        val hours = absSeconds / 3600
        val minutes = (absSeconds % 3600) / 60
        val secs = absSeconds % 60
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, secs)
            else -> String.format("%d:%02d", minutes, secs)
        }
    }
    
    /**
     * Check if remaining time is low (warning threshold)
     * 
     * @param seconds Remaining seconds
     * @return true if time is critically low
     */
    fun isLowTime(seconds: Int): Boolean {
        return seconds in 1..120 // Less than 2 minutes
    }
    
    /**
     * Check if time has run out
     * 
     * @param seconds Remaining seconds
     * @return true if no time left
     */
    fun isTimeUp(seconds: Int): Boolean {
        return seconds <= 0
    }
}






