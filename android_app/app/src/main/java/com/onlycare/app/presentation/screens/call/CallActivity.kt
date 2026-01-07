package com.onlycare.app.presentation.screens.call

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.onlycare.app.agora.token.AgoraTokenProvider
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.Black
import com.onlycare.app.presentation.theme.OnlyCareTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Dedicated Activity for call screens
 * Launched directly from IncomingCallActivity - no MainActivity needed!
 * This eliminates delays and freezing issues
 */
@AndroidEntryPoint
class CallActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "CallActivity"
        
        const val EXTRA_CALLER_ID = "caller_id"
        const val EXTRA_CALL_ID = "call_id"
        const val EXTRA_AGORA_APP_ID = "agora_app_id"
        const val EXTRA_AGORA_TOKEN = "agora_token"
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_CALL_TYPE = "call_type"
        const val EXTRA_BALANCE_TIME = "balance_time"
    }
    
    private var callerId: String = ""
    private var callId: String = ""
    private var agoraAppId: String = ""
    private var agoraToken: String = ""
    private var channelId: String = ""
    private var callType: String = "AUDIO"
    private var balanceTime: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🎬 CallActivity.onCreate() - STARTING")
        Log.d(TAG, "========================================")
        
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "✅ super.onCreate() completed")
        
        // Make activity show over lock screen and turn screen on
        try {
            setupWindowFlags()
            Log.d(TAG, "✅ Window flags setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error setting up window flags", e)
        }
        
        // Extract call data from intent
        Log.d(TAG, "📦 Extracting call data from intent...")
        callerId = intent.getStringExtra(EXTRA_CALLER_ID) ?: ""
        callId = intent.getStringExtra(EXTRA_CALL_ID) ?: ""
        agoraAppId = intent.getStringExtra(EXTRA_AGORA_APP_ID) ?: ""
        agoraToken = intent.getStringExtra(EXTRA_AGORA_TOKEN) ?: ""
        channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: ""
        callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "AUDIO"
        balanceTime = intent.getStringExtra(EXTRA_BALANCE_TIME) ?: ""

        // ✅ FIX: Some incoming-call payloads omit agoraAppId; use app fallback.
        if (agoraAppId.isBlank()) {
            val fallback = AgoraTokenProvider.getAppId()
            Log.w(TAG, "⚠️ Missing Agora App ID in intent - using fallback App ID: $fallback")
            agoraAppId = fallback
        }
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "📞 CallActivity INITIALIZED")
        Log.d(TAG, "  - Caller ID: $callerId")
        Log.d(TAG, "  - Call ID: $callId")
        Log.d(TAG, "  - Call Type: $callType")
        Log.d(TAG, "  - Channel: $channelId")
        Log.d(TAG, "  - App ID: $agoraAppId")
        Log.d(TAG, "  - Token: ${if (agoraToken.isEmpty()) "EMPTY" else "Present (${agoraToken.length} chars)"}")
        Log.d(TAG, "  - Balance Time: $balanceTime")
        Log.d(TAG, "========================================")
        
        // Validate required data
        if (callerId.isEmpty() || callId.isEmpty() || agoraAppId.isEmpty() || channelId.isEmpty()) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ CRITICAL ERROR: Missing required call data!")
            Log.e(TAG, "  - Caller ID empty: ${callerId.isEmpty()}")
            Log.e(TAG, "  - Call ID empty: ${callId.isEmpty()}")
            Log.e(TAG, "  - App ID empty: ${agoraAppId.isEmpty()}")
            Log.e(TAG, "  - Channel empty: ${channelId.isEmpty()}")
            Log.e(TAG, "  Finishing activity...")
            Log.e(TAG, "========================================")
            finish()
            return
        }
        
        // Directly show the call screen - no navigation needed!
        Log.d(TAG, "🎨 Setting up Compose UI...")
        try {
            setContent {
                OnlyCareTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Black
                    ) {
                        CallActivityContent(
                            callerId = callerId,
                            callId = callId,
                            agoraAppId = agoraAppId,
                            agoraToken = agoraToken,
                            channelId = channelId,
                            callType = callType,
                            balanceTime = balanceTime,
                            onCallEnded = { finish() },
                            onNavigateToMain = { navigateToMainActivity() }
                        )
                    }
                }
            }
            Log.d(TAG, "✅ Compose UI setup completed")
            Log.d(TAG, "========================================")
            Log.d(TAG, "✅ CallActivity.onCreate() - COMPLETED SUCCESSFULLY")
            Log.d(TAG, "   Call screen should now be visible!")
            Log.d(TAG, "========================================")
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ CRITICAL ERROR: Failed to setup Compose UI!", e)
            Log.e(TAG, "========================================")
            e.printStackTrace()
            finish()
        }
    }
    
    /**
     * Navigate back to MainActivity when call ends
     * OPTIMIZED: Minimizes black screen by starting MainActivity BEFORE finishing CallActivity
     */
    private fun navigateToMainActivity() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🏠 Navigating back to MainActivity")
        Log.d(TAG, "========================================")
        
        // Create MainActivity intent with optimizations
        val mainIntent = Intent(this, com.onlycare.app.presentation.MainActivity::class.java).apply {
            // Try to reuse existing MainActivity if it exists
            // If not, create new one with optimizations
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            
            // Signal to skip splash screen for instant appearance
            putExtra("from_call", true)
        }
        
        try {
            Log.d(TAG, "📱 Starting MainActivity FIRST (before finishing CallActivity)")
            
            // CRITICAL: Start MainActivity BEFORE finishing CallActivity
            // This ensures MainActivity appears immediately, covering CallActivity
            // No black screen gap!
            startActivity(mainIntent)
            
            // Use instant transition (no animation) to minimize any gap
            overridePendingTransition(0, 0)
            
            // Small delay before finishing to ensure MainActivity has started
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                Log.d(TAG, "✅ Finishing CallActivity after MainActivity started")
                finish()
            }, 100) // 100ms delay - minimal but ensures smooth handoff
            
            Log.d(TAG, "✅ MainActivity launch initiated - will finish CallActivity in 100ms")
            Log.d(TAG, "========================================")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to launch MainActivity", e)
            e.printStackTrace()
            // Fallback: just finish CallActivity
            finish()
        }
    }
    
    /**
     * Setup window flags to show over lock screen and turn screen on
     */
    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }
}

/**
 * Content composable that handles navigation within CallActivity
 */
@Composable
private fun CallActivityContent(
    callerId: String,
    callId: String,
    agoraAppId: String,
    agoraToken: String,
    channelId: String,
    callType: String,
    balanceTime: String,
    onCallEnded: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "call_screen"
    ) {
        composable("call_screen") {
            // Show call screen directly based on call type
            // Wrap with navigation interceptor
            if (callType == "VIDEO") {
                VideoCallScreenWrapper(
                    navController = navController,
                    userId = callerId,
                    callId = callId,
                    appId = agoraAppId,
                    token = agoraToken,
                    channel = channelId,
                    role = "receiver",
                    balanceTime = balanceTime,
                    onNavigateToMain = onNavigateToMain,
                    onCallEnded = onCallEnded
                )
            } else {
                AudioCallScreenWrapper(
                    navController = navController,
                    userId = callerId,
                    callId = callId,
                    appId = agoraAppId,
                    token = agoraToken,
                    channel = channelId,
                    role = "receiver",
                    balanceTime = balanceTime,
                    onNavigateToMain = onNavigateToMain,
                    onCallEnded = onCallEnded
                )
            }
        }
        
        composable(
            route = "call_ended/{userId}/{callId}/{duration}/{coinsSpent}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("callId") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType },
                navArgument("coinsSpent") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val endedUserId = backStackEntry.arguments?.getString("userId") ?: callerId
            val endedCallId = backStackEntry.arguments?.getString("callId") ?: callId
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
            val coinsSpent = backStackEntry.arguments?.getInt("coinsSpent") ?: 0
            
            CallEndedScreenWrapper(
                navController = navController,
                userId = endedUserId,
                callId = endedCallId,
                duration = duration,
                coinsSpent = coinsSpent,
                onNavigateToMain = onNavigateToMain,
                onCallEnded = onCallEnded
            )
        }
        
        composable(
            route = "rate_user/{userId}/{callId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("callId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val callId = backStackEntry.arguments?.getString("callId") ?: ""
            RateUserScreenWrapper(
                navController = navController,
                userId = userId,
                callId = callId,
                onNavigateToMain = onNavigateToMain,
                onCallEnded = onCallEnded
            )
        }
        
        // Add main route to handle navigation to MainActivity
        // This route is used by CallEndedScreen and RateUserScreen to navigate back to MainActivity
        composable(Screen.Main.route) {
            // When navigating to main, launch MainActivity and finish this activity
            // Call immediately without LaunchedEffect to avoid delay
            onNavigateToMain()
            
            // Show a blank surface while transitioning (prevents flash)
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Black
            ) {
                // Empty - instant transition
            }
        }
    }
}

/**
 * Wrapper composables that intercept navigation calls
 */

@Composable
private fun AudioCallScreenWrapper(
    navController: NavHostController,
    userId: String,
    callId: String,
    appId: String,
    token: String,
    channel: String,
    role: String,
    balanceTime: String,
    onNavigateToMain: () -> Unit,
    onCallEnded: () -> Unit
) {
    // Monitor navigation and intercept Main.route
    val backStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(backStackEntry?.destination?.route) {
        val route = backStackEntry?.destination?.route
        if (route == Screen.Main.route) {
            onNavigateToMain()
        }
    }
    
    // Use NavHostController directly - it extends NavController
    AudioCallScreen(
        navController = navController,
        userId = userId,
        callId = callId,
        appId = appId,
        token = token,
        channel = channel,
        role = role,
        balanceTime = balanceTime
    )
}

@Composable
private fun VideoCallScreenWrapper(
    navController: NavHostController,
    userId: String,
    callId: String,
    appId: String,
    token: String,
    channel: String,
    role: String,
    balanceTime: String,
    onNavigateToMain: () -> Unit,
    onCallEnded: () -> Unit
) {
    // Monitor navigation and intercept Main.route
    val backStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(backStackEntry?.destination?.route) {
        val route = backStackEntry?.destination?.route
        if (route == Screen.Main.route) {
            onNavigateToMain()
        }
    }
    
    // Use NavHostController directly - it extends NavController
    VideoCallScreen(
        navController = navController,
        userId = userId,
        callId = callId,
        appId = appId,
        token = token,
        channel = channel,
        role = role,
        balanceTime = balanceTime
    )
}

@Composable
private fun CallEndedScreenWrapper(
    navController: NavHostController,
    userId: String,
    callId: String,
    duration: Int,
    coinsSpent: Int,
    onNavigateToMain: () -> Unit,
    onCallEnded: () -> Unit
) {
    // Monitor navigation and intercept Main.route
    val backStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(backStackEntry?.destination?.route) {
        val route = backStackEntry?.destination?.route
        if (route == Screen.Main.route) {
            onNavigateToMain()
        }
    }
    
    CallEndedScreen(
        navController = navController,
        userId = userId,
        callId = callId,
        duration = duration,
        coinsSpent = coinsSpent,
        onNavigateToMain = onNavigateToMain
    )
}

@Composable
private fun RateUserScreenWrapper(
    navController: NavHostController,
    userId: String,
    callId: String,
    onNavigateToMain: () -> Unit,
    onCallEnded: () -> Unit
) {
    // Monitor navigation and intercept Main.route
    val backStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(backStackEntry?.destination?.route) {
        val route = backStackEntry?.destination?.route
        if (route == Screen.Main.route) {
            onNavigateToMain()
        }
    }
    
    RateUserScreen(
        navController = navController,
        userId = userId,
        callId = callId
    )
}


