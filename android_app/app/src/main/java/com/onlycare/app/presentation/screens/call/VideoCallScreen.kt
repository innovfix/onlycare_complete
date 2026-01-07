package com.onlycare.app.presentation.screens.call

import android.app.Activity
import android.util.Log
import android.view.SurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.onlycare.app.agora.token.AgoraTokenProvider
import com.onlycare.app.di.AppEntryPoint
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.screens.gift.GiftBottomSheet
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.presentation.theme.Surface as ThemeSurfaceColor
import com.onlycare.app.utils.getDisplayName
import dagger.hilt.android.EntryPointAccessors
import io.agora.rtc2.Constants as AgoraConstants
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoCallScreen(
    navController: NavController,
    userId: String,
    callId: String = "",
    appId: String = "",
    token: String = "",
    channel: String = "",
    role: String = "caller",
    balanceTime: String = "",
    upgrade: Boolean = false,
    viewModel: VideoCallViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
        android.util.Log.d("VideoCallScreen", "📞 User entered call screen - blocking other incoming calls (Call ID: $callId)")
        
        onDispose {
            com.onlycare.app.utils.CallStateManager.setInCall(false)
            com.onlycare.app.utils.CallStateManager.setCurrentCallId(null)
            android.util.Log.d("VideoCallScreen", "📞 User left call screen - allowing new incoming calls")
        }
    }
    
    // State for end call confirmation dialog
    var showEndCallDialog by remember { mutableStateOf(false) }
    
    // State for gift bottom sheet
    var showGiftBottomSheet by remember { mutableStateOf(false) }
    
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
    
    // Broadcast receiver for gift notifications - register only when call is connected
    // Use LaunchedEffect to delay registration until screen is fully ready
    LaunchedEffect(state.remoteUserJoined) {
        if (!state.remoteUserJoined) {
            return@LaunchedEffect
        }
        
        // Small delay to ensure screen is fully initialized
        kotlinx.coroutines.delay(500)
        
        val giftReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.onlycare.app.GIFT_RECEIVED") {
                    val giftIcon = intent.getStringExtra("gift_icon") ?: return
                    val senderName = intent.getStringExtra("sender_name") ?: "Someone"
                    val giftCoins = intent.getStringExtra("gift_coins") ?: "0"
                    val senderId = intent.getStringExtra("sender_id") ?: ""
                    val callType = intent.getStringExtra("call_type") ?: "video"
                    
                    android.util.Log.d("VideoCallScreen", "========================================")
                    android.util.Log.d("VideoCallScreen", "🎁 GIFT BROADCAST RECEIVED!")
                    android.util.Log.d("VideoCallScreen", "Gift Icon: $giftIcon")
                    android.util.Log.d("VideoCallScreen", "Sender: $senderName")
                    android.util.Log.d("VideoCallScreen", "Sender ID: $senderId")
                    android.util.Log.d("VideoCallScreen", "Coins: $giftCoins")
                    android.util.Log.d("VideoCallScreen", "Call Type: $callType")
                    android.util.Log.d("VideoCallScreen", "========================================")
                    
                    // Trigger animation
                    viewModel.animateGift(giftIcon)
                    
                    // Update remaining time after receiving gift
                    if (senderId.isNotEmpty()) {
                        android.util.Log.d("VideoCallScreen", "📡 Updating remaining time after receiving gift")
                        viewModel.updateRemainingTimeAfterGift(senderId, callType)
                    }
                    
                    // Show toast - use application context to avoid crashes
                    val appContext = context?.applicationContext
                    if (appContext != null) {
                        Toast.makeText(
                            appContext,
                            "Gift received",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        
        try {
            val filter = IntentFilter("com.onlycare.app.GIFT_RECEIVED")
            context.registerReceiver(giftReceiver, filter)
            android.util.Log.d("VideoCallScreen", "✅ Gift broadcast receiver registered")
            
            // Unregister when call ends or screen is disposed
            kotlinx.coroutines.awaitCancellation()
        } catch (e: Exception) {
            android.util.Log.e("VideoCallScreen", "❌ Failed to register gift receiver: ${e.message}", e)
        } finally {
            try {
                context.unregisterReceiver(giftReceiver)
                android.util.Log.d("VideoCallScreen", "❌ Gift broadcast receiver unregistered")
            } catch (e: Exception) {
                android.util.Log.e("VideoCallScreen", "❌ Error unregistering receiver: ${e.message}")
            }
        }
    }
    
    // Function to handle end call confirmation
    val handleEndCall = {
        viewModel.endCall(
            onSuccess = { callId, duration, coinsSpent ->
                navController.navigate(
                    Screen.CallEnded.createRoute(userId, callId, duration, coinsSpent)
                ) {
                    val currentRoute = navController.currentBackStackEntry?.destination?.route
                    if (currentRoute != null) {
                        popUpTo(currentRoute) { inclusive = true }
                    }
                }
            },
            onError = { error ->
                // Even if API fails, navigate with local values
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
                // ✅ Call was cancelled before connecting - go back to home instead of showing Call Ended
                android.util.Log.d("VideoCallScreen", "Call never connected - navigating to home")
                try {
                    Toast.makeText(
                        context.applicationContext,
                        "Call not connected. Try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (_: Throwable) {
                }
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Main.route) { inclusive = true }
                }
            }
        )
    }
    
    // Request camera and audio permissions
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
    )
    
    // Check if all permissions are granted
    val allPermissionsGranted = permissionsState.permissions.all { it.status.isGranted }
    
    // Set callId in ViewModel, load user data, and initialize Agora when screen opens
    LaunchedEffect(userId, callId, appId, token, channel, balanceTime, allPermissionsGranted, upgrade) {
        android.util.Log.d("VideoCallScreen", "🔍 Screen parameters:")
        android.util.Log.d("VideoCallScreen", "  - userId: $userId")
        android.util.Log.d("VideoCallScreen", "  - callId: $callId")
        android.util.Log.d("VideoCallScreen", "  - appId (from backend): ${if (appId.isEmpty()) "EMPTY" else appId}")
        android.util.Log.d("VideoCallScreen", "  - channel (from backend): ${if (channel.isEmpty()) "EMPTY" else channel}")
        android.util.Log.d("VideoCallScreen", "  - balanceTime (from backend): ${if (balanceTime.isEmpty()) "EMPTY" else balanceTime}")
        android.util.Log.d("VideoCallScreen", "  - permissions granted: $allPermissionsGranted")
        android.util.Log.d("VideoCallScreen", "  🔐 Using backend-provided App ID and credentials")
        
        if (callId.isNotEmpty()) {
            viewModel.setCallId(callId)
        }
        if (userId.isNotEmpty()) {
            viewModel.loadUser(userId)
        }
        if (balanceTime.isNotEmpty()) {
            viewModel.setBalanceTime(balanceTime)
        }

        // If this screen is opened as an upgrade from audio -> video, treat it as already accepted
        if (upgrade) {
            viewModel.markCallAcceptedForUpgrade()
        }
        
        // Request permissions first
        if (!allPermissionsGranted) {
            android.util.Log.d("VideoCallScreen", "⚠️ Requesting camera and audio permissions...")
            permissionsState.launchMultiplePermissionRequest()
        }
        
        // Generate channel name if not provided (for backward compatibility)
        val finalChannel = if (channel.isEmpty()) {
            "call_$callId"
        } else {
            channel
        }
        
        android.util.Log.d("VideoCallScreen", "📝 Final channel name: $finalChannel")
        android.util.Log.d("VideoCallScreen", "🔑 App ID: $appId")
        android.util.Log.d("VideoCallScreen", "🔐 Token: ${if (token.isEmpty()) "EMPTY (no certificate)" else "PROVIDED"}")
        
        // Use backend-provided token or empty if not provided (no certificate mode)
        val finalToken = token.ifEmpty { "" }
        
        // Initialize Agora and join video channel after permissions granted
        if (allPermissionsGranted) {
            if (appId.isEmpty()) {
                android.util.Log.e("VideoCallScreen", "❌ App ID is missing from backend!")
                android.util.Log.e("VideoCallScreen", "   Cannot proceed without App ID")
                return@LaunchedEffect
            }
            
            // Use explicit role parameter to determine if receiver or caller
            val isReceiver = (role == "receiver")
            android.util.Log.d("VideoCallScreen", "✅ All checks passed, joining call as ${if (isReceiver) "RECEIVER" else "CALLER"} (role=$role)...")
            android.util.Log.d("VideoCallScreen", "📍 Using backend credentials (App ID + Token)")
            // Small delay to ensure SurfaceViews are created first
            kotlinx.coroutines.delay(500)
            viewModel.initializeAndJoinCall(appId, finalToken, finalChannel, isReceiver)
        } else {
            android.util.Log.w("VideoCallScreen", "⚠️ Waiting for permissions...")
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
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != null) {
                            popUpTo(currentRoute) { inclusive = true }
                        }
                    }
                },
                onError = { error ->
                    // Even if API fails, navigate with local values
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
                    android.util.Log.d("VideoCallScreen", "Call never connected (auto-end) - navigating to home")
                    try {
                        Toast.makeText(
                            context.applicationContext,
                            "Call not connected. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (_: Throwable) {
                    }
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
    
    // Handle back button press - show confirmation dialog
    // Handle back button - close bottom sheet if open, otherwise show end call dialog
    BackHandler(enabled = true) {
        if (showGiftBottomSheet) {
            android.util.Log.d("VideoCallScreen", "🔙 Back button pressed - closing gift bottom sheet")
            showGiftBottomSheet = false
        } else {
            showEndCallDialog = true
        }
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
    if (showEndCallDialog) {
        AlertDialog(
            onDismissRequest = { showEndCallDialog = false },
            title = {
                Text(
                    text = "End Call?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to end this call?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndCallDialog = false
                        handleEndCall()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Primary
                    )
                ) {
                    Text(
                        text = "Yes",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndCallDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Primary
                    )
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background) // Light background behind video
    ) {
        // Remote Video Container (Full Screen)
        Box(modifier = Modifier.fillMaxSize()) {
            // Remote Video (Full Screen) - Only show when remote user joined
            if (state.remoteUserJoined && state.remoteUid != 0) {
                AndroidView(
                    factory = { context ->
                        SurfaceView(context).apply {
                            setZOrderMediaOverlay(false)
                            setZOrderOnTop(false)
                            // Set up remote video view with actual remote UID
                            viewModel.getAgoraManager()?.setupRemoteVideo(this, state.remoteUid)
                            android.util.Log.d("VideoCall", "✅ Remote SurfaceView created for UID: ${state.remoteUid}")
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        
        // Show ringing UI if remote user hasn't joined yet
        if (!state.remoteUserJoined || state.remoteUid == 0) {
                VideoRingingUI(
                userName = state.user?.getDisplayName() ?: "User",
                userImage = state.user?.profileImage,
                    isReceiver = (role == "receiver"),
                acceptanceMessage = state.acceptanceMessage  // Pass acceptance message
            )
        }
        
        }
        
        // Local Video Preview (Top Right)
        // Caller: show only AFTER receiver accepts (so camera doesn't turn on during connecting screen)
        val shouldShowLocalPreview = allPermissionsGranted &&
            (role == "receiver" || state.callAccepted || state.remoteUserJoined) &&
            (viewModel.getAgoraManager() != null)

        if (shouldShowLocalPreview) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .width(120.dp)
                    .height(160.dp)
                    .background(ThemeSurfaceColor, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    factory = { context ->
                        android.util.Log.d("VideoCall", "🏗️ Creating local SurfaceView...")
                        SurfaceView(context).apply {
                            setZOrderMediaOverlay(true)
                            setZOrderOnTop(true)
                            holder.setFixedSize(320, 240)
                            
                            // Wait for surface to be ready before setting up video
                            holder.addCallback(object : android.view.SurfaceHolder.Callback {
                                override fun surfaceCreated(holder: android.view.SurfaceHolder) {
                                    android.util.Log.d("VideoCall", "📱 Surface CREATED - now setting up camera...")
                                    viewModel.getAgoraManager()?.let { manager ->
                                        manager.setupLocalVideo(this@apply)
                                        android.util.Log.d("VideoCall", "✅ Local video setup called from surfaceCreated")
                                    } ?: android.util.Log.e("VideoCall", "❌ AgoraManager is null!")
                                }
                                
                                override fun surfaceChanged(holder: android.view.SurfaceHolder, format: Int, width: Int, height: Int) {
                                    android.util.Log.d("VideoCall", "📐 Surface changed: ${width}x${height}")
                                }
                                
                                override fun surfaceDestroyed(holder: android.view.SurfaceHolder) {
                                    android.util.Log.d("VideoCall", "💥 Surface destroyed")
                                }
                            })
                        }
                    },
                    update = { view ->
                        // Re-setup when recomposed
                        viewModel.getAgoraManager()?.let {
                            android.util.Log.d("VideoCall", "🔄 Re-setting up local video (update)")
                            it.setupLocalVideo(view)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Back button at top
        IconButton(
            onClick = { showEndCallDialog = true },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Primary
            )
        }
        
        // Top Info Bar
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .wrapContentWidth()
                .widthIn(max = 260.dp),
            color = ThemeSurface.copy(alpha = 0.92f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.widthIn(max = 180.dp)) {
                    Text(
                        text = state.user?.getDisplayName() ?: "Connecting...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    // Countdown Timer - Show for both caller and receiver
                    if (state.maxCallDuration > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        CompactCallCountdownTimer(
                            remainingSeconds = state.remainingTime,
                            isLowTime = state.isLowTime
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${state.user?.coinBalance ?: 0}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
        
        // Bottom Controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            color = ThemeSurface.copy(alpha = 0.92f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute
                IconButton(
                    onClick = { viewModel.toggleMute() },
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
                EndCallButton(
                    onClick = {
                        showEndCallDialog = true
                    }
                )
                
                // Video Toggle
                IconButton(
                    onClick = { viewModel.toggleVideo() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Surface(
                        color = if (!state.isVideoOn) ErrorRed else ThemeSurface,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (state.isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Video",
                                tint = if (!state.isVideoOn) White else Primary
                            )
                        }
                    }
                }
            }
        }
        
        // Bouncing gift button on the right side, below coins (only for male users)
        if (isMaleUser && currentUserId.isNotEmpty() && state.remoteUserJoined) {
            BouncingGiftButton(
                onClick = { showGiftBottomSheet = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 120.dp, end = 20.dp)
            )
        }
    }
    
    // Handle back button to close gift bottom sheet
    BackHandler(enabled = showGiftBottomSheet) {
        showGiftBottomSheet = false
    }
    
    // Gift Bottom Sheet
    if (showGiftBottomSheet && currentUserId.isNotEmpty() && state.remoteUserJoined) {
        GiftBottomSheet(
            receiverId = userId,
            callType = "video",
            onGiftSelected = { gift ->
                Log.d("GiftBottomSheet", "========================================")
                Log.d("GiftBottomSheet", "✅✅✅ GIFT SENT SUCCESSFULLY ✅✅✅")
                Log.d("GiftBottomSheet", "========================================")
                Log.d("GiftBottomSheet", "Gift ID: ${gift.id}")
                Log.d("GiftBottomSheet", "Gift Icon: ${gift.giftIcon}")
                Log.d("GiftBottomSheet", "Gift Coins: ${gift.coins}")
                Log.d("GiftBottomSheet", "Sender ID: $currentUserId")
                Log.d("GiftBottomSheet", "Receiver ID: $userId")
                Log.d("GiftBottomSheet", "Call Type: video")
                Log.d("GiftBottomSheet", "========================================")
                viewModel.sendGift(
                    senderId = currentUserId,
                    receiverId = userId,
                    giftId = gift.id,
                    giftIcon = gift.giftIcon,
                    onSuccess = {
                        Log.d("GiftBottomSheet", "========================================")
                        Log.d("GiftBottomSheet", "✅✅✅ GIFT SENT SUCCESSFULLY ✅✅✅")
                        Log.d("GiftBottomSheet", "========================================")
                        Log.d("GiftBottomSheet", "Gift ID: ${gift.id}")
                        Log.d("GiftBottomSheet", "Gift Icon: ${gift.giftIcon}")
                        Log.d("GiftBottomSheet", "Sender ID: $currentUserId")
                        Log.d("GiftBottomSheet", "Receiver ID: $userId")
                        Log.d("GiftBottomSheet", "Status: SUCCESS - Gift sent and FCM notification sent")
                        Log.d("GiftBottomSheet", "========================================")
                        
                        // Show toast for sender - use application context
                        try {
                            val appContext = context.applicationContext
                            Toast.makeText(
                                appContext,
                                "Gift sent",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            android.util.Log.e("VideoCallScreen", "Error showing toast: ${e.message}")
                        }
                        
                        showGiftBottomSheet = false
                    },
                    onError = { error ->
                        Log.e("GiftBottomSheet", "========================================")
                        Log.e("GiftBottomSheet", "❌❌❌ GIFT SEND FAILED ❌❌❌")
                        Log.e("GiftBottomSheet", "========================================")
                        Log.e("GiftBottomSheet", "Error: $error")
                        Log.e("GiftBottomSheet", "========================================")
                        // Error is handled in ViewModel
                    }
                )
            },
            onDismiss = { showGiftBottomSheet = false },
            getRemainingTime = { callType ->
                viewModel.getRemainingTime(callType)
            },
            calculateAvailableCoins = { remainingTime, callType ->
                viewModel.calculateAvailableCoins(remainingTime, callType)
            }
        )
        
        // Show gift animation when gift is received
        // Animation is triggered by BroadcastReceiver when FCM gift notification arrives
        if (state.giftReceived != null) {
            GiftAnimation(
                giftIcon = state.giftReceived,
                message = "Gift Received",
                onAnimationComplete = {
                    viewModel.clearGiftReceived()
                }
            )
        }
        
        // Show gift animation when gift is sent
        if (state.giftSent != null) {
            GiftAnimation(
                giftIcon = state.giftSent,
                message = "Gift Sent",
                onAnimationComplete = {
                    viewModel.clearGiftSent()
                }
            )
        }
    }
}

@Composable
private fun VideoRingingUI(
    userName: String,
    userImage: String?,
    isReceiver: Boolean,
    acceptanceMessage: String? = null  // Add acceptance message parameter
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
                    size = 200.dp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // User Name
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status with animated dots
            VideoStatusDotsText(
                label = if (isReceiver) "Connecting" else "Ringing",
                color = if (isReceiver) White else OnlineGreen
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
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = if (isReceiver) "Waiting for $userName to join..." else "Waiting for $userName to answer...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun VideoStatusDotsText(
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
        style = MaterialTheme.typography.headlineMedium,
        color = color,
        fontWeight = FontWeight.Medium
    )
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}



