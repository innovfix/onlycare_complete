package com.onlycare.app.presentation.components

import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.onlycare.app.utils.FullScreenIntentHelper

/**
 * Dialog to prompt user to enable full-screen intent permission
 * Required for Android 14+ to show full-screen incoming call notifications
 */
@Composable
fun FullScreenIntentPermissionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Incoming Call Notifications") },
        text = {
            Text(
                "To see incoming calls when the app is closed, please enable " +
                "\"Full-screen notifications\" permission.\n\n" +
                "You'll be redirected to settings where you can enable it."
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        FullScreenIntentHelper.openFullScreenIntentSettings(context)
                    } else {
                        FullScreenIntentHelper.openAppDetailsSettings(context)
                    }
                    onDismiss()
                }
            ) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}

/**
 * Dialog to prompt user to disable battery optimization
 * Required for reliable incoming call delivery when app is closed
 */
@Composable
fun BatteryOptimizationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Disable Battery Optimization") },
        text = {
            Text(
                "To receive calls when the app is closed, please disable " +
                "battery optimization for Only Care.\n\n" +
                "This allows the app to receive incoming call notifications reliably."
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        FullScreenIntentHelper.requestDisableBatteryOptimization(context)
                    }
                    onDismiss()
                }
            ) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}

/**
 * Combined dialog that checks and prompts for all required permissions
 */
@Composable
fun IncomingCallPermissionsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val missingPermissions = FullScreenIntentHelper.getMissingPermissions(context)
    
    if (missingPermissions.isEmpty()) {
        // All permissions granted, no need to show dialog
        onDismiss()
        return
    }
    
    val message = buildString {
        append("To receive incoming calls when the app is closed, please grant these permissions:\n\n")
        
        missingPermissions.forEach { permission ->
            when (permission) {
                FullScreenIntentHelper.PermissionType.FULL_SCREEN_INTENT -> {
                    append("• Full-screen notifications\n")
                }
                FullScreenIntentHelper.PermissionType.BATTERY_OPTIMIZATION -> {
                    append("• Disable battery optimization\n")
                }
            }
        }
        
        append("\nYou'll be redirected to settings to enable them.")
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Call Permissions") },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = {
                    // Open the most important permission first
                    if (missingPermissions.contains(FullScreenIntentHelper.PermissionType.FULL_SCREEN_INTENT)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            FullScreenIntentHelper.openFullScreenIntentSettings(context)
                        } else {
                            FullScreenIntentHelper.openAppDetailsSettings(context)
                        }
                    } else if (missingPermissions.contains(FullScreenIntentHelper.PermissionType.BATTERY_OPTIMIZATION)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            FullScreenIntentHelper.requestDisableBatteryOptimization(context)
                        }
                    }
                    onDismiss()
                }
            ) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}






