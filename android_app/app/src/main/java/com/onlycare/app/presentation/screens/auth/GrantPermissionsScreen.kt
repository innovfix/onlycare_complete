package com.onlycare.app.presentation.screens.auth

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.onlycare.app.presentation.components.OnlyCarePrimaryButton
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GrantPermissionsScreen(navController: NavController) {
    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val permissionsState = rememberMultiplePermissionsState(requiredPermissions)
    val allGranted = permissionsState.allPermissionsGranted

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)  // White background
    ) {
        // Back Button
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary  // Dark arrow on white
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
        
        Text(
            text = "Grant Permissions",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary  // Dark text on white
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "We need these permissions for the best experience",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,  // Medium gray
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(60.dp))
        
        // Permissions List
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PermissionItem(
                icon = Icons.Default.Videocam,
                title = "Camera",
                description = "For video calls"
            )
            
            PermissionItem(
                icon = Icons.Default.Mic,
                title = "Microphone",
                description = "For audio and video calls"
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "For incoming call alerts"
            )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        OnlyCarePrimaryButton(
            text = if (allGranted) "Continue" else "Grant Permissions",
            onClick = {
                if (allGranted) {
                navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.GrantPermissions.route) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = Primary  // Royal violet icons
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary  // Dark text
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary  // Medium gray
            )
        }
    }
}

