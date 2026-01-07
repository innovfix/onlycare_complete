package com.onlycare.app.presentation.screens.call

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.utils.getDisplayName
import kotlinx.coroutines.delay

@Composable
fun CallConnectingScreen(
    navController: NavController,
    userId: String,
    callType: String,
    viewModel: CallConnectingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    // Load user details on first launch
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.loadUser(userId)
        }
    }

    // If call fails (no answer / rejected / busy / unavailable), auto-return to Home with toast.
    LaunchedEffect(state.error) {
        val err = state.error ?: return@LaunchedEffect
        val lower = err.lowercase()

        // Don't hijack insufficient balance; user should go recharge.
        if (lower.contains("insufficient")) return@LaunchedEffect

        val shouldAutoExit =
            lower.contains("no answer") ||
                lower.contains("timeout") ||
                lower.contains("rejected") ||
                lower.contains("declined") ||
                lower.contains("busy") ||
                lower.contains("not available") ||
                lower.contains("offline")

        if (shouldAutoExit) {
            val msg = when {
                lower.contains("no answer") || lower.contains("timeout") ->
                    "Creator not answered. Try again."
                lower.contains("busy") ->
                    "Creator is busy. Try again."
                lower.contains("rejected") || lower.contains("declined") ->
                    "Creator rejected the call."
                else ->
                    "Call not connected. Try again."
            }
            Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()

            // Go back to Home (Main) and remove this connecting screen from stack
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Main.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }
    
    // Initiate call after user is loaded
    LaunchedEffect(state.user, state.error) {
        if (state.user != null && !state.isConnecting && state.callId == null && state.error == null) {
            delay(500) // Small delay to show connecting state
            viewModel.initiateCall(
                receiverId = userId,
                callType = callType,
                onSuccess = { callId, appId, token, channel, balanceTime ->
                    // Navigate to appropriate call screen with userId, callId, appId, token, channel, and balanceTime
                    // Pass role="caller" since this is the initiating side
                    val route = when (callType.lowercase()) {
                        "audio" -> Screen.AudioCall.createRoute(userId, callId, appId, token, channel, role = "caller", balanceTime = balanceTime)
                        "video" -> Screen.VideoCall.createRoute(userId, callId, appId, token, channel, role = "caller", balanceTime = balanceTime)
                        else -> Screen.AudioCall.createRoute(userId, callId, appId, token, channel, role = "caller", balanceTime = balanceTime)
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.CallConnecting.route) { inclusive = true }
                    }
                }
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val topRingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "topRingAlpha"
    )

    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.25f at 0
                1f at 300
                0.25f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.25f at 0
                0.25f at 300
                1f at 600
                0.25f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0.25f at 0
                0.25f at 600
                1f at 900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot3"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        when {
            state.isLoading -> {
                // Loading user details
                CircularProgressIndicator(color = Primary)
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
            
            state.error != null -> {
                // Error state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Red
                    )
                    Text(
                        text = state.error ?: "An error occurred",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Button(
                        onClick = { navController.navigateUp() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Go Back")
                    }
                }
            }
            
            else -> {
                // Connecting state UI (male caller side) - layout matches screenshot, colors follow OnlyCare theme
                val title = if (callType.lowercase() == "video") "Video Session" else "Audio Session"
                val remoteName = state.user?.username?.takeIf { it.isNotBlank() }
                    ?: (state.user?.getDisplayName()?.takeIf { it.isNotBlank() })
                    ?: "User"

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (state.isConnecting) "Connecting" else "Preparing",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = dotAlpha1))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = dotAlpha2))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = dotAlpha3))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(56.dp))

                // Avatars + connecting line
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Remote (female) avatar with ring
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, Primary.copy(alpha = topRingAlpha), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileImage(
                            imageUrl = state.user?.profileImage,
                            size = 110.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Connecting line
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(150.dp)
                            .background(Border.copy(alpha = 0.6f))
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Local (male) avatar + "You"
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, Primary.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileImage(
                            imageUrl = state.localProfileImage,
                            size = 110.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "You",
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "Connecting with $remoteName",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Connecting...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Cancel (small text button like screenshot)
        TextButton(
            onClick = {
                viewModel.cancelOutgoingCall()
                navController.navigateUp()
            }
        ) {
            Text(
                text = "Cancel",
                color = TextSecondary,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}



