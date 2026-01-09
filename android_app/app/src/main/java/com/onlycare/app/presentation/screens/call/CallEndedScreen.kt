package com.onlycare.app.presentation.screens.call

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*

@Composable
fun CallEndedScreen(
    navController: NavController,
    userId: String,
    callId: String,
    duration: Int,  // in seconds
    coinsSpent: Int,
    onNavigateToMain: (() -> Unit)? = null,
    onRateUser: ((userId: String, callId: String) -> Unit)? = null
) {
    // Get user gender to determine if we should show coins spent
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember {
        dagger.hilt.android.EntryPointAccessors.fromApplication(
            context.applicationContext,
            com.onlycare.app.di.AppEntryPoint::class.java
        ).sessionManager()
    }
    val userGender = remember { sessionManager.getGender() }
    val isMaleUser = remember { userGender == com.onlycare.app.domain.model.Gender.MALE }
    
    // ✅ FIX: Clear call state when entering call ended screen to prevent conflicts with new incoming calls
    LaunchedEffect(Unit) {
        com.onlycare.app.utils.CallStateManager.setInCall(false)
        com.onlycare.app.utils.CallStateManager.setInIncomingCallScreen(false)
        com.onlycare.app.utils.CallStateManager.setCurrentCallId(null)
        android.util.Log.d("CallEndedScreen", "✅ Cleared call state - ready for new calls")
    }
    
    // Handle system back button - make it behave like "Back to Home" button
    BackHandler {
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Main.route) { inclusive = true }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Back button at top
        IconButton(
            onClick = { 
                // Always go back to Main; navigateUp() can return to the call screen if stack isn't cleared.
                // In CallActivity, navigating to Screen.Main triggers CallActivityContent to open MainActivity.
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Main.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Primary
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
        
        Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "Call Ended",
            modifier = Modifier.size(80.dp),
            tint = ErrorRed
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Call Ended",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Call Summary
        OnlyCareSoftShadowContainer(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            shadowOffsetY = 4.dp,
            shadowColor = Border.copy(alpha = 0.28f)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Duration:", color = TextSecondary)
                    Text(
                        formatDuration(duration),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // ✅ Only show Coins Spent for MALE users (they pay for calls)
                // Female users should NOT see this
                if (isMaleUser) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Coins Spent:", color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Circle,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color(0xFFFFC107),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "$coinsSpent",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            }
        }
        
        Spacer(modifier = Modifier.height(60.dp))
        
        OnlyCarePrimaryButton(
            text = "Rate User",
            onClick = {
                // ✅ Use callback if provided (for CallActivity -> RatingActivity)
                // Otherwise fallback to navigation (for backwards compatibility)
                if (onRateUser != null) {
                    onRateUser(userId, callId)
                } else {
                    navController.navigate(Screen.RateUser.createRoute(userId, callId))
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = {
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Main.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryLight),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = ThemeSurface,
                contentColor = Primary
            )
        ) {
            Text(
                text = "Back to Home",
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
    }
}

/**
 * Format duration in seconds to MM:SS format
 */
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}


