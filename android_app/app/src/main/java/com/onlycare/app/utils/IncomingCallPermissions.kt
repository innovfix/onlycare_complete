package com.onlycare.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Helper for incoming call permissions
 * Handles runtime permissions for notifications, full-screen intent, and overlay
 */
object IncomingCallPermissions {
    
    private const val TAG = "IncomingCallPermissions"
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true  // Not required for Android 12 and below
        }
    }
    
    /**
     * Check if system alert window (overlay) permission is granted
     */
    fun isOverlayPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    /**
     * Open system settings to grant overlay permission
     */
    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening overlay settings", e)
            }
        }
    }
    
    /**
     * Request notification permission (Android 13+)
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_NOTIFICATION
            )
        }
    }
    
    /**
     * Check if all incoming call permissions are granted
     */
    fun areAllPermissionsGranted(context: Context): Boolean {
        val notificationGranted = isNotificationPermissionGranted(context)
        val overlayGranted = isOverlayPermissionGranted(context)
        
        Log.d(TAG, "Notification permission: $notificationGranted")
        Log.d(TAG, "Overlay permission: $overlayGranted")
        
        return notificationGranted && overlayGranted
    }
    
    /**
     * Log permission status
     */
    fun logPermissionStatus(context: Context) {
        Log.d(TAG, "=== Incoming Call Permission Status ===")
        Log.d(TAG, "Notification: ${isNotificationPermissionGranted(context)}")
        Log.d(TAG, "Overlay: ${isOverlayPermissionGranted(context)}")
        Log.d(TAG, "======================================")
    }
    
    const val REQUEST_CODE_NOTIFICATION = 1001
    const val REQUEST_CODE_OVERLAY = 1002
}

/**
 * Composable to request notification permission (Android 13+)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermission(
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {}
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        ) { isGranted ->
            if (isGranted) {
                Log.d("IncomingCallPermissions", "Notification permission granted")
                onPermissionGranted()
            } else {
                Log.w("IncomingCallPermissions", "Notification permission denied")
                onPermissionDenied()
            }
        }
        
        LaunchedEffect(Unit) {
            if (!notificationPermissionState.status.isGranted) {
                if (!notificationPermissionState.status.shouldShowRationale) {
                    // First time asking, directly request
                    notificationPermissionState.launchPermissionRequest()
                }
            } else {
                onPermissionGranted()
            }
        }
    } else {
        // Not needed for Android 12 and below
        LaunchedEffect(Unit) {
            onPermissionGranted()
        }
    }
}






