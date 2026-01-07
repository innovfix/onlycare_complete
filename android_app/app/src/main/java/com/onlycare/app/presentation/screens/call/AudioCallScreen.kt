package com.onlycare.app.presentation.screens.call

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import com.onlycare.app.BuildConfig
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.onlycare.app.agora.token.AgoraTokenProvider
import com.onlycare.app.di.AppEntryPoint
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.utils.getDisplayName
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioCallScreen(
    navController: NavController,
    userId: String,
    callId: String = "",
    appId: String = "",
    token: String = "",
    channel: String = "",
    role: String = "caller",
    balanceTime: String = "",
    viewModel: AudioCallViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val appContext = LocalContext.current.applicationContext

    // Ensure status bar icons are visible on this light screen (dark icons)
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as? Activity)?.window
            if (window == null) return@DisposableEffect onDispose { }

            val controller = WindowCompat.getInsetsController(window, view)
            val prevStatusColor = window.statusBarColor
            val prevLightStatus = controller.isAppearanceLightStatusBars

            window.statusBarColor = Background.toArgb()
            controller.isAppearanceLightStatusBars = true

            onDispose {
                window.statusBarColor = prevStatusColor
                controller.isAppearanceLightStatusBars = prevLightStatus
            }
        }
    }
    
    // Track that user is in an active call - block other incoming calls
    DisposableEffect(Unit) {
        com.onlycare.app.utils.CallStateManager.setInCall(true)
        com.onlycare.app.utils.CallStateManager.setCurrentCallId(callId)
        android.util.Log.d("AudioCallScreen", "📞 User entered call screen - blocking other incoming calls (Call ID: $callId)")
        
        onDispose {
            com.onlycare.app.utils.CallStateManager.setInCall(false)
            com.onlycare.app.utils.CallStateManager.setCurrentCallId(null)
            android.util.Log.d("AudioCallScreen", "📞 User left call screen - allowing new incoming calls")
        }
    }
    
    // State for end call confirmation dialog
    var showEndCallDialog by remember { mutableStateOf(false) }
    
    // Get current user ID and gender from SessionManager
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppEntryPoint::class.java
        ).sessionManager()
    }
    val currentUserId = remember { sessionManager.getUserId() }
    val userGender = remember { sessionManager.getGender() }
    val isMaleUser = remember { userGender == com.onlycare.app.domain.model.Gender.MALE }
    
    // Broadcast receiver for FCM call rejection (backup if WebSocket fails)
    LaunchedEffect(Unit) {
        val rejectionReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == "${BuildConfig.APPLICATION_ID}.CALL_REJECTED_FCM") {
                    val callId = intent.getStringExtra("callId") ?: return
                    val reason = intent.getStringExtra("reason") ?: "User declined"
                    
                    android.util.Log.d("AudioCallScreen", "========================================")
                    android.util.Log.d("AudioCallScreen", "🚫 FCM CALL REJECTION RECEIVED!")
                    android.util.Log.d("AudioCallScreen", "Call ID: $callId")
                    android.util.Log.d("AudioCallScreen", "Reason: $reason")
                    android.util.Log.d("AudioCallScreen", "========================================")
                    
                    // This will trigger the ViewModel to handle the rejection
                    // The ViewModel already has the handleCallRejection logic
                }
            }
        }
        
        try {
            val filter = android.content.IntentFilter("${BuildConfig.APPLICATION_ID}.CALL_REJECTED_FCM")
            context.registerReceiver(rejectionReceiver, filter)
            android.util.Log.d("AudioCallScreen", "✅ FCM rejection receiver registered")
            
            kotlinx.coroutines.awaitCancellation()
        } catch (e: Exception) {
            android.util.Log.e("AudioCallScreen", "❌ Failed to register rejection receiver: ${e.message}", e)
        } finally {
            try {
                context.unregisterReceiver(rejectionReceiver)
                android.util.Log.d("AudioCallScreen", "❌ FCM rejection receiver unregistered")
            } catch (e: Exception) {
                android.util.Log.e("AudioCallScreen", "❌ Error unregistering rejection receiver: ${e.message}")
            }
        }
    }
    
    // NOTE: Gift UI is intentionally removed in audio calls (both sides).
    
    // Function to handle end call confirmation
    val handleEndCall = {
        viewModel.endCall(
            onSuccess = { callId, duration, coinsSpent ->
                navController.navigate(
                    Screen.CallEnded.createRoute(userId, callId, duration, coinsSpent)
                ) {
                    popUpTo(Screen.Main.route)
                }
            },
            onError = { error ->
                navController.navigate(
                    Screen.CallEnded.createRoute(
                        userId,
                        state.callId ?: "1",
                        state.duration,
                        0
                    )
                ) {
                    popUpTo(Screen.Main.route)
                }
            },
            onCallNeverConnected = {
                // ✅ Call was cancelled before connecting - go back to home instead of showing Call Ended
                android.util.Log.d("AudioCallScreen", "Call never connected - navigating to home")
                Toast.makeText(appContext, "Call not connected. Try again.", Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Main.route) { inclusive = true }
                }
            }
        )
    }
    
    // Request audio permission
    val audioPermission = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    
    // Set callId in ViewModel, load user data, and initialize Agora when screen opens
    LaunchedEffect(userId, callId, appId, token, channel, balanceTime, audioPermission.status) {
        android.util.Log.d("AudioCallScreen", "🔍 Screen parameters:")
        android.util.Log.d("AudioCallScreen", "  - userId: $userId")
        android.util.Log.d("AudioCallScreen", "  - callId: $callId")
        android.util.Log.d("AudioCallScreen", "  - appId (from backend): ${if (appId.isEmpty()) "EMPTY" else appId}")
        android.util.Log.d("AudioCallScreen", "  - channel (from backend): ${if (channel.isEmpty()) "EMPTY" else channel}")
        android.util.Log.d("AudioCallScreen", "  - balanceTime (from backend): ${if (balanceTime.isEmpty()) "EMPTY" else balanceTime}")
        android.util.Log.d("AudioCallScreen", "  - permission granted: ${audioPermission.status.isGranted}")
        android.util.Log.d("AudioCallScreen", "  🔐 Using backend-provided App ID and credentials")
        
        if (callId.isNotEmpty()) {
            viewModel.setCallId(callId)
        }
        if (userId.isNotEmpty()) {
            viewModel.setRemoteUserId(userId)
        }
        if (userId.isNotEmpty()) {
            viewModel.loadUser(userId)
        }
        if (balanceTime.isNotEmpty()) {
            viewModel.setBalanceTime(balanceTime)
        }
        
        // Request audio permission first
        if (!audioPermission.status.isGranted) {
            android.util.Log.d("AudioCallScreen", "⚠️ Requesting audio permission...")
            audioPermission.launchPermissionRequest()
        }
        
        // Generate channel name if not provided (for backward compatibility)
        val finalChannel = if (channel.isEmpty()) {
            "call_$callId"
        } else {
            channel
        }
        
        android.util.Log.d("AudioCallScreen", "📝 Final channel name: $finalChannel")
        android.util.Log.d("AudioCallScreen", "🔑 App ID: $appId")
        android.util.Log.d("AudioCallScreen", "🔐 Token: ${if (token.isEmpty()) "EMPTY (no certificate)" else "PROVIDED"}")
        
        // Use backend-provided token or empty if not provided (no certificate mode)
        val finalToken = token.ifEmpty { "" }
        
        // Initialize Agora and join audio channel after permission granted
        if (audioPermission.status.isGranted) {
            if (appId.isEmpty()) {
                android.util.Log.e("AudioCallScreen", "❌ App ID is missing from backend!")
                android.util.Log.e("AudioCallScreen", "   Cannot proceed without App ID")
                return@LaunchedEffect
            }
            
            // Use explicit role parameter to determine if receiver or caller
            val isReceiver = (role == "receiver")
            android.util.Log.d("AudioCallScreen", "✅ All checks passed, joining call as ${if (isReceiver) "RECEIVER" else "CALLER"} (role=$role)...")
            android.util.Log.d("AudioCallScreen", "📍 Using backend credentials (App ID + Token)")
            // Receiver: join immediately (after accept). Caller: call will defer Agora join until accepted.
            viewModel.initializeAndJoinCall(appId, finalToken, finalChannel, isReceiver)
        } else {
            android.util.Log.w("AudioCallScreen", "⚠️ Waiting for audio permission...")
        }
    }
    
    // Update duration every second - ONLY when remote user has joined
    LaunchedEffect(state.remoteUserJoined) {
        if (state.remoteUserJoined) {
            while (true) {
                delay(1000)
                viewModel.updateDuration(state.duration + 1)
            }
        }
    }
    
    // Handle remote user ending call
    LaunchedEffect(state.isCallEnded) {
        if (state.isCallEnded) {
            // Automatically end call on this side too
            viewModel.endCall(
                onSuccess = { callId, duration, coinsSpent ->
                    navController.navigate(
                        Screen.CallEnded.createRoute(userId, callId, duration, coinsSpent)
                    ) {
                        // Pop the current call destination so Back from CallEnded never returns to call UI.
                        // Works for both MainActivity nav (audio_call/...) and CallActivity nav ("call_screen").
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != null) {
                            popUpTo(currentRoute) { inclusive = true }
                        }
                    }
                },
                onError = { error ->
                    // Force navigate anyway with local values
                    navController.navigate(
                        Screen.CallEnded.createRoute(
                            userId,
                            state.callId ?: "1",
                            state.duration,
                            0
                        )
                    ) {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != null) {
                            popUpTo(currentRoute) { inclusive = true }
                        }
                    }
                },
                onCallNeverConnected = {
                    // Call was cancelled before connecting - go back to home
                    android.util.Log.d("AudioCallScreen", "Call never connected (auto-end) - navigating to home")
                    Toast.makeText(appContext, "Call not connected. Try again.", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
    
    // Handle back button press - show confirmation dialog
    BackHandler(enabled = true) {
        showEndCallDialog = true
    }
    
    // Show error if any (but not if call ended by remote user)
    val shouldShowError = state.error != null && 
        !state.error!!.contains("Call ended by remote user", ignoreCase = true) && 
        !state.isCallEnded
    
    if (shouldShowError) {
        AlertDialog(
            onDismissRequest = { /* Don't dismiss */ },
            title = { 
                Text(
                    if (state.error?.contains("No Answer") == true) "Call Status" 
                    else "Error"
                ) 
            },
            text = { Text(state.error ?: "") },
            confirmButton = {
                TextButton(onClick = { navController.navigateUp() }) {
                    Text("OK")
                }
            }
        )
    }
    
    // End call confirmation dialog
    EndCallConfirmationDialog(
        show = showEndCallDialog,
        onDismiss = { showEndCallDialog = false },
        onConfirm = {
            showEndCallDialog = false
            handleEndCall()
        }
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Light theme background
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Background)
        )
        // Back button at top (hide during caller waiting state; screenshot shows only bottom Cancel)
        if (!(role == "caller" && !state.remoteUserJoined)) {
            IconButton(
                onClick = { showEndCallDialog = true },
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
        }
        
        // Show different UI based on whether receiver has joined
        if (!state.remoteUserJoined) {
            // RINGING STATE - Waiting for receiver to accept
            if (role == "caller") {
                CallerConnectingUI(
                    remoteName = state.user?.username?.takeIf { it.isNotBlank() }
                        ?: (state.user?.getDisplayName()?.takeIf { it.isNotBlank() })
                        ?: "User",
                    remoteImage = state.user?.profileImage,
                    localImage = sessionManager.getProfileImage().ifBlank { null },
                    onCancel = { showEndCallDialog = true }
                )
            } else {
                RingingCallUI(
                    userName = state.user?.getDisplayName() ?: "User",
                    userImage = state.user?.profileImage,
                    isReceiver = (role == "receiver"),
                    acceptanceMessage = state.acceptanceMessage,  // Caller-only; receiver will ignore
                    onEndCall = {
                        showEndCallDialog = true
                    }
                )
            }
        } else {
            // CONNECTED STATE - Show full call UI
            ConnectedCallUI(
                state = state,
                onMuteToggle = { viewModel.toggleMute() },
                onSpeakerToggle = { viewModel.toggleSpeaker() },
                onEndCall = {
                    showEndCallDialog = true
                }
            )
        }
    }
}

@Composable
private fun RingingCallUI(
    userName: String,
    userImage: String?,
    isReceiver: Boolean,
    acceptanceMessage: String? = null,  // Add acceptance message parameter
    onEndCall: () -> Unit
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.EaseInOut),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Animated Profile Image
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
            contentAlignment = Alignment.Center
        ) {
            ProfileImage(
                imageUrl = userImage,
                size = 160.dp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // User Name
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status with animated dots
        StatusDotsText(
            label = if (isReceiver) "Connecting" else "Ringing",
            color = if (isReceiver) Primary else OnlineGreen
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Waiting/connecting message (or acceptance message for caller)
        if (!isReceiver && acceptanceMessage != null) {
            // Show acceptance message with animation
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically()
            ) {
                androidx.compose.material3.Card(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.9f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = acceptanceMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            Text(
                text = if (isReceiver) "Waiting for $userName to join..." else "Waiting for $userName to answer...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Only End Call button during ringing
        EndCallButton(onClick = onEndCall)
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun CallerConnectingUI(
    remoteName: String,
    remoteImage: String?,
    localImage: String?,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "callerConnecting")

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
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
            .padding(horizontal = 24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Audio Session",
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
                text = "Connecting",
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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, Primary.copy(alpha = ringAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ProfileImage(
                    imageUrl = remoteImage,
                    size = 110.dp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(150.dp)
                    .background(Border.copy(alpha = 0.6f))
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, Primary.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ProfileImage(
                    imageUrl = localImage,
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

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Cancel (small text)
        TextButton(onClick = onCancel) {
            Text(
                text = "Cancel",
                color = TextSecondary,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun StatusDotsText(
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    var dotCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            dotCount = (dotCount + 1) % 4
        }
    }
    
    val dots = ".".repeat(dotCount)
    
    Text(
        text = "$label$dots",
        style = MaterialTheme.typography.titleLarge,
        color = color,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ConnectedCallUI(
    state: AudioCallState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        // Profile Image
        ProfileImage(
            imageUrl = state.user?.profileImage,
            size = 160.dp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // User Name
        Text(
            text = state.user?.getDisplayName() ?: "Connected",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Countdown Timer - Show for both caller and receiver
        if (state.maxCallDuration > 0) {
            LabeledCallCountdownTimer(
                remainingSeconds = state.remainingTime,
                isLowTime = state.isLowTime,
                label = "Time Remaining"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Coin display hidden for audio calls as per requirement
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Call Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Mute
            IconButton(
                onClick = onMuteToggle,
                modifier = Modifier.size(64.dp)
            ) {
                Surface(
                    color = if (state.isMuted) ErrorRed else ThemeSurface,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (state.isMuted) White else Primary
                        )
                    }
                }
            }
            
            // End Call
            EndCallButton(onClick = onEndCall)
            
            // Speaker
            IconButton(
                onClick = onSpeakerToggle,
                modifier = Modifier.size(64.dp)
            ) {
                Surface(
                    color = if (state.isSpeakerOn) OnlineGreen else ThemeSurface,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaker",
                            tint = if (state.isSpeakerOn) White else Primary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}



