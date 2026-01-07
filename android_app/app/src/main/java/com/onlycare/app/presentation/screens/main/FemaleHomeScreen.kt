package com.onlycare.app.presentation.screens.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.onlycare.app.BuildConfig
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.onlycare.app.domain.model.CallStatus
import com.onlycare.app.presentation.components.*
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.*
import com.onlycare.app.presentation.theme.Surface as ThemeSurface
import com.onlycare.app.presentation.theme.Border as ThemeBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FemaleHomeScreen(
    navController: NavController,
    viewModel: FemaleHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Keep backend online presence in sync while creator is in the home screen.
    LaunchedEffect(Unit) {
        viewModel.refreshOnlinePresence()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshOnlinePresence()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    // Show error dialog if any
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = ThemeSurface,
            title = { Text("Error", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(state.error ?: "", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearError() },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
    
    // Track if we're navigating to prevent dialog recomposition issues
    var isNavigating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Incoming calls + notification permission are handled globally in MainActivity for female users
    // so calls can appear on ANY screen (not only Home).
    
    // Listen for call reject broadcasts from IncomingCallActivity
    // (Call accept is now handled in MainActivity to persist across activity lifecycle)
    DisposableEffect(context) {
        val callRejectedReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == "${BuildConfig.APPLICATION_ID}.CALL_REJECTED") {
                    android.util.Log.d("FemaleHomeScreen", "❌ Call rejected broadcast received")
                    viewModel.rejectIncomingCall()
                }
            }
        }
        
        val rejectFilter = android.content.IntentFilter("${BuildConfig.APPLICATION_ID}.CALL_REJECTED")
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(callRejectedReceiver, rejectFilter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(callRejectedReceiver, rejectFilter)
        }
        
        onDispose {
            try {
                context.unregisterReceiver(callRejectedReceiver)
            } catch (e: Exception) {
                android.util.Log.e("FemaleHomeScreen", "Error unregistering receivers", e)
            }
        }
    }
    
    // OLD DIALOG - DISABLED (replaced with full-screen activity above)
    if (false && state.hasIncomingCall && state.incomingCall != null && !isNavigating) {
        val call = state.incomingCall!!
        AlertDialog(
            onDismissRequest = { /* Don't dismiss - user must respond */ },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (call.callType == "VIDEO") Icons.Default.Videocam else Icons.Default.Phone,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Incoming ${call.callType} Call")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = call.callerName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "is calling you...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Prevent multiple clicks
                        if (isNavigating) return@Button
                        isNavigating = true
                        
                        // Save call details BEFORE dismissing
                        val callId = call.id
                        val callerId = call.callerId
                        val callType = call.callType
                        val agoraAppId = call.agoraAppId ?: ""
                        val agoraToken = call.agoraToken ?: ""
                        val channelName = call.channelName ?: ""
                        val balanceTime = call.balanceTime ?: "" // Get caller's balance time
                        
                        // Accept the call via API, then navigate to call screen
                        android.util.Log.d("FemaleHomeScreen", "========================================")
                        android.util.Log.d("FemaleHomeScreen", "📞 ACCEPTING CALL")
                        android.util.Log.d("FemaleHomeScreen", "Call ID: $callId")
                        android.util.Log.d("FemaleHomeScreen", "Caller ID: $callerId")
                        android.util.Log.d("FemaleHomeScreen", "App ID from IncomingCallDto: ${agoraAppId.ifEmpty { "NULL/EMPTY" }}")
                        android.util.Log.d("FemaleHomeScreen", "Token from IncomingCallDto: ${if (agoraToken.isEmpty()) "NULL/EMPTY" else "${agoraToken.take(20)}... (${agoraToken.length} chars)"}")
                        android.util.Log.d("FemaleHomeScreen", "Channel from IncomingCallDto: ${channelName.ifEmpty { "NULL" }}")
                        android.util.Log.d("FemaleHomeScreen", "Balance Time from IncomingCallDto: ${balanceTime.ifEmpty { "NULL/EMPTY" }}")
                        android.util.Log.d("FemaleHomeScreen", "========================================")
                        
                        // Accept via ViewModel
                        viewModel.acceptIncomingCall(
                            onSuccess = {
                                android.util.Log.d("FemaleHomeScreen", "✅ Accept API call succeeded")
                                
                                // Dismiss dialog
                                viewModel.dismissIncomingCall()
                                
                                android.util.Log.d("FemaleHomeScreen", "Navigating to call screen with:")
                                android.util.Log.d("FemaleHomeScreen", "  - userId: $callerId")
                                android.util.Log.d("FemaleHomeScreen", "  - callId: $callId")
                                android.util.Log.d("FemaleHomeScreen", "  - appId: ${agoraAppId.ifEmpty { "EMPTY!" }}")
                                android.util.Log.d("FemaleHomeScreen", "  - token: ${if (agoraToken.isEmpty()) "EMPTY!" else "OK (${agoraToken.length} chars)"}")
                                android.util.Log.d("FemaleHomeScreen", "  - channel: ${channelName.ifEmpty { "EMPTY!" }}")
                                android.util.Log.d("FemaleHomeScreen", "  - balanceTime: ${balanceTime.ifEmpty { "EMPTY!" }}")
                                
                                val route = if (callType == "VIDEO") {
                                    Screen.VideoCall.createRoute(
                                        userId = callerId,
                                        callId = callId,
                                        appId = agoraAppId,
                                        token = agoraToken,
                                        channel = channelName,
                                        role = "receiver",
                                        balanceTime = balanceTime
                                    )
                                } else {
                                    Screen.AudioCall.createRoute(
                                        userId = callerId,
                                        callId = callId,
                                        appId = agoraAppId,
                                        token = agoraToken,
                                        channel = channelName,
                                        role = "receiver",
                                        balanceTime = balanceTime
                                    )
                                }
                                
                                android.util.Log.d("FemaleHomeScreen", "Navigation route: $route")
                                
                                // Small delay to ensure dialog is dismissed and touch events are cleared
                                coroutineScope.launch {
                                    delay(100) // 100ms delay
                                    navController.navigate(route)
                                }
                            },
                            onError = { error ->
                                // Reset navigation flag on error
                                isNavigating = false
                                android.util.Log.e("FemaleHomeScreen", "❌ Failed to accept call API: $error")
                            }
                        )
                    },
                    enabled = !isNavigating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Prevent multiple clicks
                        if (isNavigating) return@Button
                        isNavigating = true
                        
                        // Reject the call via API and dismiss dialog
                        android.util.Log.d("FemaleHomeScreen", "❌ REJECTING CALL - callId: ${call.id}")
                        viewModel.rejectIncomingCall()
                    },
                    enabled = !isNavigating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFF44336)
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reject")
                }
            }
        )
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            // Light header (no purple background) + purple title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    // Slightly higher placement vs previous (less top padding after status bar)
                    .padding(top = 6.dp, bottom = 10.dp)
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val titleOffset = maxWidth * 0.05f // move right ~5% (15% back to the left)

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Only Care",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 36.sp,
                                letterSpacing = (-0.2).sp
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = Primary,
                            modifier = Modifier.padding(start = titleOffset)
                        )

                        EarningsWalletChip(
                            amount = state.availableBalance,
                            onClick = { navController.navigate(Screen.Earnings.route) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = state.isRefreshing),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Toggles
            item {
                OnlyCareSoftShadowContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    shadowOffsetY = 4.dp,
                    shadowColor = ThemeBorder.copy(alpha = 0.28f)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Call Availability",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Audio Call Toggle - Connected to API
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Audio Calls",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }
                            Switch(
                                checked = state.audioCallEnabled,
                                onCheckedChange = { if (!state.isUpdatingAvailability) viewModel.updateAudioCallAvailability(it) },
                                enabled = !state.isUpdatingAvailability,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Primary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = PrimaryLight.copy(alpha = 0.55f),
                                    uncheckedBorderColor = PrimaryLight
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Video Call Toggle - Connected to API
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = Primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Video Calls",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }
                            Switch(
                                checked = state.videoCallEnabled,
                                onCheckedChange = { if (!state.isUpdatingAvailability) viewModel.updateVideoCallAvailability(it) },
                                enabled = !state.isUpdatingAvailability,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Primary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = PrimaryLight.copy(alpha = 0.55f),
                                    uncheckedBorderColor = PrimaryLight
                                )
                            )
                        }
                    }
                    }
                }
            }
            
            // Info Message Box
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFFFF4E5)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = androidx.compose.ui.graphics.Color(0xFFFFA000),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "If you are not available, please disable audio or video session. Once you are available, you can enable them again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.ui.graphics.Color(0xFF6B5A3E),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            // Today's Activity Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Today's Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your performance summary for today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Approx Earnings Card - Connected to API
            item {
                OnlyCareSoftShadowContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    shadowOffsetY = 4.dp,
                    shadowColor = ThemeBorder.copy(alpha = 0.28f)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PrimaryLight, RoundedCornerShape(16.dp))
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Canvas(
                                modifier = Modifier.size(44.dp)
                            ) {
                                drawCircle(
                                    color = OnlineGreen.copy(alpha = 0.15f),
                                    radius = size.minDimension / 2
                                )
                                drawCircle(
                                    color = OnlineGreen,
                                    radius = size.minDimension / 4
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Approx earnings",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                        if (state.isLoading) {
                            OnlyCareSkeletonTextLine(
                                widthFraction = 0.28f,
                                height = 22.dp,
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Text(
                                text = String.format("%.0f", state.todayEarnings),
                                style = MaterialTheme.typography.headlineSmall,
                                color = OnlineGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
                }
            }
            
            // Total Session Card - Connected to API
            item {
                OnlyCareSoftShadowContainer(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    shadowOffsetY = 4.dp,
                    shadowColor = ThemeBorder.copy(alpha = 0.28f)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ThemeSurface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PrimaryLight, RoundedCornerShape(16.dp))
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Canvas(
                                modifier = Modifier.size(44.dp)
                            ) {
                                drawCircle(
                                    color = Primary.copy(alpha = 0.15f),
                                    radius = size.minDimension / 2
                                )
                                drawCircle(
                                    color = Primary,
                                    radius = size.minDimension / 4
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Total Session",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                        if (state.isLoading) {
                            OnlyCareSkeletonTextLine(
                                widthFraction = 0.22f,
                                height = 22.dp,
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Text(
                                text = state.todayCalls.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
                }
            }
        }
        }
    }
}

@Composable
private fun EarningsWalletChip(
    amount: Double,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(100),
        color = AccentLavender,
        onClick = onClick,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "₹${String.format("%.0f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

