package com.onlycare.app.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * Helper class to manage full-screen intent permission
 * Required for showing full-screen incoming call notifications on Android 12+
 */
object FullScreenIntentHelper {
    
    private const val TAG = "FullScreenIntentHelper"
    
    /**
     * Check if the app has full-screen intent permission
     * On Android 14+, this requires explicit permission from the user
     * On Android 13 and below, it's auto-granted from manifest
     */
    fun hasFullScreenIntentPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val hasPermission = notificationManager.canUseFullScreenIntent()
            Log.d(TAG, "Full-screen intent permission status (Android 14+): $hasPermission")
            hasPermission
        } else {
            // Android 13 and below - permission is auto-granted from manifest
            Log.d(TAG, "Full-screen intent permission auto-granted (Android < 14)")
            true
        }
    }
    
    /**
     * Open settings to allow the user to grant full-screen intent permission
     * This is required on Android 14+ for full-screen incoming call notifications
     */
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun openFullScreenIntentSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opened full-screen intent settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening full-screen intent settings", e)
            // Fallback to app details settings
            openAppDetailsSettings(context)
        }
    }
    
    /**
     * Open app details settings as a fallback
     */
    fun openAppDetailsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Opened app details settings")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening app details settings", e)
        }
    }
    
    /**
     * Check if battery optimization is disabled for the app
     * Battery optimization can prevent incoming call notifications when app is closed
     */
    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            Log.d(TAG, "Battery optimization disabled: $isIgnoring")
            isIgnoring
        } else {
            true // Not applicable for older versions
        }
    }
    
    /**
     * Request to disable battery optimization
     * This allows the app to receive incoming call notifications even when closed
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun requestDisableBatteryOptimization(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d(TAG, "Requested to disable battery optimization")
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting battery optimization", e)
        }
    }
    
    /**
     * Get a user-friendly message explaining what permissions are needed
     */
    fun getPermissionExplanationMessage(context: Context): String {
        val missingPermissions = mutableListOf<String>()
        
        if (!hasFullScreenIntentPermission(context)) {
            missingPermissions.add("• Full-screen notifications (to show incoming calls)")
        }
        
        if (!isBatteryOptimizationDisabled(context)) {
            missingPermissions.add("• Battery optimization exemption (to receive calls when app is closed)")
        }
        
        return if (missingPermissions.isEmpty()) {
            "All permissions are granted! You should receive incoming calls normally."
        } else {
            "To receive incoming calls when the app is closed, please grant these permissions:\n\n" +
            missingPermissions.joinToString("\n") +
            "\n\nTap OK to open settings."
        }
    }
    
    /**
     * Check all required permissions and return a list of missing ones
     */
    fun getMissingPermissions(context: Context): List<PermissionType> {
        val missing = mutableListOf<PermissionType>()
        
        if (!hasFullScreenIntentPermission(context)) {
            missing.add(PermissionType.FULL_SCREEN_INTENT)
        }
        
        if (!isBatteryOptimizationDisabled(context)) {
            missing.add(PermissionType.BATTERY_OPTIMIZATION)
        }
        
        return missing
    }
    
    enum class PermissionType {
        FULL_SCREEN_INTENT,
        BATTERY_OPTIMIZATION
    }
}






