package com.onlycare.app.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlycare.app.data.remote.dto.BestOfferDto
import com.onlycare.app.domain.model.Gender
import kotlinx.coroutines.launch
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.presentation.navigation.NavGraph
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.components.OnlyCareSoftShadowContainer
import com.onlycare.app.presentation.theme.OnlyCareTheme
import com.onlycare.app.presentation.theme.Black
import com.onlycare.app.presentation.theme.Primary
import com.onlycare.app.presentation.theme.PrimaryDark
import com.onlycare.app.presentation.theme.PrimaryLight
import com.onlycare.app.presentation.theme.Background
import com.onlycare.app.presentation.theme.Border
import com.onlycare.app.presentation.theme.TextPrimary
import com.onlycare.app.presentation.theme.TextSecondary
import com.onlycare.app.presentation.theme.White
import com.onlycare.app.presentation.theme.Surface as ThemeSurface
import com.onlycare.app.websocket.WebSocketManager
import com.truecaller.android.sdk.oAuth.TcSdk
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.onlycare.app.utils.CallStateManager
import com.onlycare.app.domain.model.Gender as DomainGender
import com.onlycare.app.data.remote.dto.IncomingCallDto
import com.onlycare.app.services.IncomingCallService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.onlycare.app.utils.RequestNotificationPermission
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import com.onlycare.app.utils.FullScreenIntentHelper

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var repository: ApiDataRepository
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    @Inject
    lateinit var webSocketManager: WebSocketManager
    
    // Navigation state for call screen
    private val pendingCallNavigation = mutableStateOf<PendingCallNavigation?>(null)
    private var navController: NavHostController? = null

    // Female incoming calls (polling fallback while app is open)
    private var femaleIncomingCallJob: Job? = null
    private val launchedIncomingCallIds = mutableSetOf<String>()
    
    // Best offers state
    private val bestOffers = mutableStateOf<List<BestOfferDto>>(emptyList())
    private val showBestOffersSheet = mutableStateOf(false)
    private var hasShownBestOffersThisSession = false // Track if bottom sheet was shown in this session
    
    // Broadcast receiver for call acceptance
    // Truecaller login broadcast receiver (registered in MainActivity to persist)
    private val truecallerAuthReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.onlycare.app.TRUECALLER_AUTH_CODE") {
                val authCode = intent.getStringExtra("authorization_code")
                Log.d("MainActivity", "========================================")
                Log.d("MainActivity", "📨 Truecaller auth code received in MainActivity")
                Log.d("MainActivity", "  - Authorization Code: ${authCode?.take(20)}...")
                Log.d("MainActivity", "  - Forwarding to LoginScreen via shared state...")
                Log.d("MainActivity", "========================================")
                
                // Store in shared preferences or use a callback mechanism
                // For now, we'll use a static variable or shared preferences
                // The LoginScreen will check for this value
                if (authCode != null) {
                    val prefs = context?.getSharedPreferences("truecaller_auth", Context.MODE_PRIVATE)
                    prefs?.edit()?.putString("authorization_code", authCode)?.apply()
                    Log.d("MainActivity", "✅ Authorization code saved to shared preferences")
                }
            }
        }
    }
    
    private val callAcceptedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.onlycare.app.CALL_ACCEPTED") {
                handleCallAccepted(intent)
            }
        }
    }
    
    data class PendingCallNavigation(
        val callerId: String,
        val callId: String,
        val agoraAppId: String,
        val agoraToken: String,
        val channelId: String,
        val callType: String,
        val balanceTime: String = ""
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install premium minimal splash screen
        val splashScreen = installSplashScreen()

        // Don't artificially keep the system splash screen on-screen.
        // Our in-app Compose SplashScreen handles the branded splash experience.
        splashScreen.setKeepOnScreenCondition { false }
        
        // ✅ FIX: Check if this is a call intent, wallet intent, or returning from call
        val isCallIntent = intent?.getStringExtra("navigate_to") == "call_screen"
        val isWalletIntent = intent?.getStringExtra("navigate_to") == "wallet"
        val fromCall = intent?.getBooleanExtra("from_call", false) == true
        
        Log.d("MainActivity", "========================================")
        Log.d("MainActivity", "📱 MainActivity onCreate()")
        Log.d("MainActivity", "  - Is call intent: $isCallIntent")
        Log.d("MainActivity", "  - Is wallet intent: $isWalletIntent")
        Log.d("MainActivity", "  - From call: $fromCall")
        Log.d("MainActivity", "  - Intent: ${intent?.extras?.keySet()}")
        Log.d("MainActivity", "========================================")
        
        if (isCallIntent || isWalletIntent || fromCall) {
            if (isCallIntent) {
                // ✅ Extract call data early (before setContent)
                handleCallNavigationFromIntent(intent)
                Log.d("MainActivity", "✅ Call navigation data extracted and pending navigation set")
            } else if (isWalletIntent) {
                Log.d("MainActivity", "✅ Wallet navigation intent detected")
            } else if (fromCall) {
                Log.d("MainActivity", "✅ Returning from call - MainActivity will show home screen")
            }
        }
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Update online status when app starts (if user is logged in)
        updateOnlinePresenceForCurrentSession(appInForeground = true)
        
        // ✅ CRITICAL: Send FCM token to backend on every app start (if user is logged in)
        // This ensures male users can receive incoming call notifications from females
        if (sessionManager.isLoggedIn()) {
            lifecycleScope.launch {
                try {
                    val fcmToken = com.onlycare.app.utils.FCMTokenManager.getFCMToken()
                    if (fcmToken != null) {
                        Log.d("MainActivity", "📧 Sending FCM token to backend on app start: ${fcmToken.take(20)}...")
                        val result = repository.updateFCMToken(fcmToken)
                        if (result.isSuccess) {
                            Log.d("MainActivity", "✅ FCM token sent to backend successfully")
                        } else {
                            Log.e("MainActivity", "❌ Failed to send FCM token: ${result.exceptionOrNull()?.message}")
                        }
                    } else {
                        Log.w("MainActivity", "⚠️ FCM token not available on app start")
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error sending FCM token on app start", e)
                }
            }
        }
        
        // ⚡ Connect to WebSocket only for MALE users (females use FCM only)
        // WebSocket provides instant call status updates for male callers
        // Female users receive calls via FCM which is more reliable when app is killed
        if (sessionManager.isLoggedIn() && sessionManager.getGender() == Gender.MALE) {
            connectWebSocket()
            loadBestOffers()
        }
        
        // Register broadcast receiver for call acceptance
        val filter = IntentFilter("com.onlycare.app.CALL_ACCEPTED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(callAcceptedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(callAcceptedReceiver, filter)
        }
        
        // Register Truecaller auth code receiver
        val truecallerFilter = IntentFilter("com.onlycare.app.TRUECALLER_AUTH_CODE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(truecallerAuthReceiver, truecallerFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(truecallerAuthReceiver, truecallerFilter)
        }
        Log.d("MainActivity", "✅ Broadcast receiver registered for call acceptance")
        
        setContent {
            OnlyCareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navCtrl = rememberNavController()
                    navController = navCtrl // Save reference
                    val backStackEntry by navCtrl.currentBackStackEntryAsState()
                    
                    // Determine start destination - use normal flow, navigation will happen via LaunchedEffect
                    // IMPORTANT:
                    // Keep startDestination stable for the lifetime of this composition.
                    // During onboarding we update SessionManager fields (gender/avatar/userId, etc).
                    // If startDestination changes during recomposition, NavHost can reset and jump to Main,
                    // skipping onboarding screens (age/bio/interests).
                    val startDestination = remember {
                        when {
                        // Female users who submitted voice but are not yet verified must be blocked on pending screen
                        sessionManager.isLoggedIn() &&
                        !sessionManager.getUserId().startsWith("temp_") &&
                        sessionManager.getGender() == Gender.FEMALE &&
                        sessionManager.getVoice().isNotEmpty() &&
                        !sessionManager.isVerified() -> Screen.VerificationPending.route
                        // If user is logged in and has a real userId (not temp), they've completed registration
                        // Go to Main screen regardless of profile completeness check
                        sessionManager.isLoggedIn() && 
                        !sessionManager.getUserId().startsWith("temp_") -> Screen.Main.route
                        // If logged in but has temp userId, they're in the middle of registration
                        sessionManager.isLoggedIn() -> Screen.SelectGender.route
                        else -> Screen.Splash.route
                        }
                    }

                    // ✅ Start/stop female incoming-call polling based on current session state.
                    // This is critical because login happens without Activity resume.
                    LaunchedEffect(backStackEntry) {
                        if (sessionManager.isLoggedIn() && sessionManager.getGender() == Gender.FEMALE) {
                            startFemaleIncomingCallPollingIfNeeded()
                        } else {
                            femaleIncomingCallJob?.cancel()
                            femaleIncomingCallJob = null
                        }
                    }
                    
                    // ✅ Navigate to wallet if requested
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        val navigateToWallet = intent?.getStringExtra("navigate_to") == "wallet"
                        if (navigateToWallet) {
                            android.util.Log.d("MainActivity", "💰 Navigating to wallet screen")
                            kotlinx.coroutines.delay(200) // Wait for NavGraph to initialize
                            try {
                                navCtrl.navigate(Screen.Wallet.route) {
                                    popUpTo(Screen.Main.route) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                                intent?.removeExtra("navigate_to")
                                android.util.Log.d("MainActivity", "✅ Navigated to wallet screen")
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "❌ Failed to navigate to wallet", e)
                            }
                        }
                    }
                    
                    // ✅ FIX: Navigate immediately to call screen if pending navigation exists
                    // This ensures we go directly to call screen without showing splash/main
                    androidx.compose.runtime.LaunchedEffect(pendingCallNavigation.value) {
                        val pending = pendingCallNavigation.value
                        if (pending != null) {
                            android.util.Log.d("MainActivity", "========================================")
                            android.util.Log.d("MainActivity", "🚀 PENDING CALL NAVIGATION DETECTED")
                            android.util.Log.d("MainActivity", "  - Call ID: ${pending.callId}")
                            android.util.Log.d("MainActivity", "  - Call Type: ${pending.callType}")
                            android.util.Log.d("MainActivity", "  - Caller ID: ${pending.callerId}")
                            android.util.Log.d("MainActivity", "  - Channel: ${pending.channelId}")
                            android.util.Log.d("MainActivity", "  - App ID: ${pending.agoraAppId}")
                            android.util.Log.d("MainActivity", "========================================")
                            
                            // Wait for NavGraph to be fully initialized
                            // Increased delay when app is killed/restarted to ensure everything is ready
                            kotlinx.coroutines.delay(150)
                            
                            // Double-check pending is still set (might have been cleared)
                            val currentPending = pendingCallNavigation.value
                            if (currentPending == null) {
                                android.util.Log.w("MainActivity", "⚠️ Pending navigation was cleared before navigation")
                                return@LaunchedEffect
                            }
                            
                            android.util.Log.d("MainActivity", "🚀 NAVIGATING TO CALL SCREEN NOW")
                            
                            try {
                                val route = if (currentPending.callType == "VIDEO") {
                                    Screen.VideoCall.createRoute(
                                        userId = currentPending.callerId,
                                        callId = currentPending.callId,
                                        appId = currentPending.agoraAppId,
                                        token = currentPending.agoraToken,
                                        channel = currentPending.channelId,
                                        role = "receiver",
                                        balanceTime = currentPending.balanceTime
                                    )
                                } else {
                                    Screen.AudioCall.createRoute(
                                        userId = currentPending.callerId,
                                        callId = currentPending.callId,
                                        appId = currentPending.agoraAppId,
                                        token = currentPending.agoraToken,
                                        channel = currentPending.channelId,
                                        role = "receiver",
                                        balanceTime = currentPending.balanceTime
                                    )
                                }
                                
                                android.util.Log.d("MainActivity", "📍 Navigation route: $route")
                                android.util.Log.d("MainActivity", "📍 Start destination: $startDestination")
                                
                                // Navigate immediately, replacing start destination
                                navCtrl.navigate(route) {
                                    // Clear entire back stack and start fresh at call screen
                                    popUpTo(startDestination) {
                                        inclusive = true
                                    }
                                    // Ensure single top to prevent duplicates
                                    launchSingleTop = true
                                }
                                
                                android.util.Log.d("MainActivity", "✅ Navigation command executed")
                                android.util.Log.d("MainActivity", "📍 Current destination after navigate: ${navCtrl.currentBackStackEntry?.destination?.route}")
                                
                                // Small delay before clearing to ensure navigation completes
                                kotlinx.coroutines.delay(100)
                                
                                // Verify navigation succeeded
                                val finalDestination = navCtrl.currentBackStackEntry?.destination?.route
                                android.util.Log.d("MainActivity", "📍 Final destination: $finalDestination")
                                if (finalDestination?.contains("call") == true) {
                                    android.util.Log.d("MainActivity", "✅ Successfully navigated to call screen!")
                                } else {
                                    android.util.Log.e("MainActivity", "❌ Navigation may have failed - expected call screen, got: $finalDestination")
                                }
                                
                                // Clear pending navigation
                                pendingCallNavigation.value = null
                                intent?.removeExtra("navigate_to")
                                
                                android.util.Log.d("MainActivity", "✅ Navigation to call screen completed!")
                                android.util.Log.d("MainActivity", "========================================")
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "❌ Navigation failed", e)
                                android.util.Log.e("MainActivity", "Error: ${e.message}", e)
                                android.util.Log.e("MainActivity", "Stack trace:", e)
                            }
                        } else {
                            android.util.Log.d("MainActivity", "ℹ️ No pending call navigation")
                        }
                    }
                    
                    // Global female-only notification permission prompt + banner (Android 13+)
                    val isFemale = sessionManager.getGender() == DomainGender.FEMALE
                    val isLoggedIn = sessionManager.isLoggedIn()

                    Box(modifier = Modifier.fillMaxSize()) {
                        NavGraph(
                            navController = navCtrl,
                            startDestination = startDestination
                        )

                        if (isLoggedIn && isFemale) {
                            // Prompt once (Android 13+): system dialog
                            RequestNotificationPermission()
                        }
                    }
                    
                    // Best Offers Bottom Sheet
                    BestOffersBottomSheet(
                        offers = bestOffers.value,
                        show = showBestOffersSheet.value,
                        onDismiss = { showBestOffersSheet.value = false },
                        onNavigateToWallet = {
                            showBestOffersSheet.value = false
                            navCtrl.navigate(Screen.Wallet.route)
                        }
                    )
                }
            }
        }
    }
    
    /**
     * Load best offers from API
     */
    private fun loadBestOffers() {
        lifecycleScope.launch {
            repository.getBestOffers().onSuccess { offers ->
                Log.d("MainActivity", "✅ Best offers loaded: ${offers.size} offers")
                bestOffers.value = offers
                if (offers.isNotEmpty() && !hasShownBestOffersThisSession) {
                    // Show bottom sheet only once per app session
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        showBestOffersSheet.value = true
                        hasShownBestOffersThisSession = true // Mark as shown for this session
                        Log.d("MainActivity", "🎁 Best offers bottom sheet shown (first time this session)")
                    }, 1000) // Show after 1 second
                } else if (hasShownBestOffersThisSession) {
                    Log.d("MainActivity", "ℹ️ Best offers already shown this session, skipping")
                }
            }.onFailure { error ->
                Log.e("MainActivity", "❌ Failed to load best offers: ${error.message}")
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's intent
        
        Log.d("MainActivity", "📨 onNewIntent called")
        Log.d("MainActivity", "  - Extras: ${intent?.extras?.keySet()}")
        
        // Check if this is a call navigation intent
        if (intent?.getStringExtra("navigate_to") == "call_screen") {
            Log.d("MainActivity", "✅ Call navigation intent received!")
            handleCallNavigationFromIntent(intent)
        } else if (intent?.getBooleanExtra("from_call", false) == true) {
            // Returning from call - MainActivity is already showing, just log it
            Log.d("MainActivity", "✅ Returning from call - MainActivity already visible")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "========================================")
        Log.d("MainActivity", "📥 onActivityResult called")
        Log.d("MainActivity", "  - Request Code: $requestCode")
        Log.d("MainActivity", "  - Result Code: $resultCode (${if (resultCode == -1) "RESULT_OK" else if (resultCode == 0) "RESULT_CANCELED" else "OTHER"})")
        Log.d("MainActivity", "  - Data: ${data?.extras?.keySet()}")
        
        // Check if this is a Truecaller OAuth result (request code 100)
        if (requestCode == 100 && data != null && data.extras != null) {
            val responseExtra = data.extras!!.get("OAUTH_SDK_RESPONSE_EXTRA")
            if (responseExtra != null) {
                Log.d("MainActivity", "  - Truecaller Response Extra: $responseExtra")
                Log.d("MainActivity", "  - Response Type: ${responseExtra.javaClass.simpleName}")
                
                val responseString = responseExtra.toString()
                
                // Check if it's a failure response with InvalidPartnerError
                if (responseString.contains("InvalidPartnerError")) {
                    Log.e("MainActivity", "========================================")
                    Log.e("MainActivity", "❌ Truecaller InvalidPartnerError detected!")
                    Log.e("MainActivity", "  This means:")
                    Log.e("MainActivity", "  1. Client ID might be incorrect")
                    Log.e("MainActivity", "  2. Package name doesn't match Truecaller Developer Portal")
                    Log.e("MainActivity", "  3. SHA-256 fingerprint doesn't match")
                    Log.e("MainActivity", "  4. App signature doesn't match")
                    Log.e("MainActivity", "========================================")
                    
                    // Show user-friendly error
                    android.widget.Toast.makeText(
                        this,
                        "Truecaller configuration error. Please check:\n• Client ID\n• Package name\n• SHA-256 fingerprint\n\nUsing OTP instead.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                // Check if it's a success response with authorization code
                else if (responseString.contains("authorizationCode=") && resultCode == -1) {
                    Log.d("MainActivity", "========================================")
                    Log.d("MainActivity", "✅ Truecaller Success Response detected!")
                    Log.d("MainActivity", "  - Extracting authorization code from response...")
                    
                    // Extract authorization code using regex
                    // Pattern matches: authorizationCode=CODE_VALUE (where CODE_VALUE can contain =, -, _, etc.)
                    val authCodePattern = Regex("authorizationCode=([^,\\s)]+)")
                    val match = authCodePattern.find(responseString)
                    val authorizationCode = match?.groupValues?.get(1)?.trim()
                    
                    if (authorizationCode != null) {
                        Log.d("MainActivity", "  - Authorization Code extracted: ${authorizationCode.take(20)}...")
                        
                        // Save directly to SharedPreferences (no broadcast needed since we're in the same Activity)
                        val prefs = getSharedPreferences("truecaller_auth", Context.MODE_PRIVATE)
                        prefs.edit().putString("authorization_code", authorizationCode).apply()
                        Log.d("MainActivity", "========================================")
                        Log.d("MainActivity", "✅ Authorization code saved to SharedPreferences")
                        Log.d("MainActivity", "  - LoginScreen will poll and pick it up")
                        Log.d("MainActivity", "========================================")
                    } else {
                        Log.e("MainActivity", "❌ Failed to extract authorization code from response")
                    }
                    Log.d("MainActivity", "========================================")
                }
            }
        }
        
        if (data != null && data.extras != null) {
            Log.d("MainActivity", "  - Extras details:")
            for (key in data.extras!!.keySet()) {
                Log.d("MainActivity", "    - $key: ${data.extras!!.get(key)}")
            }
        }
        Log.d("MainActivity", "========================================")
        try {
            // Truecaller OAuth may use different request codes than SHARE_PROFILE_REQUEST_CODE.
            // Forward all activity results to Truecaller SDK (it will ignore unrelated ones).
            Log.d("MainActivity", "🔄 Forwarding activity result to Truecaller SDK...")
            Log.d("MainActivity", "  - Request Code: $requestCode (Truecaller uses 100)")
            TcSdk.getInstance().onActivityResultObtained(this, requestCode, resultCode, data)
            Log.d("MainActivity", "✅ Activity result forwarded to Truecaller SDK")
            Log.d("MainActivity", "  - If callback doesn't trigger, SDK might not be initialized with callback")
        } catch (t: Throwable) {
            Log.e("MainActivity", "❌ Error forwarding activity result to Truecaller SDK", t)
            Log.e("MainActivity", "  - Exception: ${t.javaClass.simpleName}")
            Log.e("MainActivity", "  - Message: ${t.message}")
            t.printStackTrace()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // User is active - update presence
        updateOnlinePresenceForCurrentSession(appInForeground = true)
        
        // ✅ CRITICAL: Re-send FCM token when app resumes (in case it expired or was cleared)
        if (sessionManager.isLoggedIn()) {
            lifecycleScope.launch {
                try {
                    val fcmToken = com.onlycare.app.utils.FCMTokenManager.getFCMToken()
                    if (fcmToken != null) {
                        Log.d("MainActivity", "📧 Re-sending FCM token on app resume: ${fcmToken.take(20)}...")
                        repository.updateFCMToken(fcmToken)
                            .onSuccess { Log.d("MainActivity", "✅ FCM token re-sent successfully") }
                            .onFailure { e -> Log.e("MainActivity", "❌ Failed to re-send FCM token: ${e.message}") }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error re-sending FCM token on resume", e)
                }
            }
        }
        
        // ⚡ Reconnect WebSocket if disconnected (only for male users)
        if (sessionManager.isLoggedIn() && sessionManager.getGender() == Gender.MALE) {
            connectWebSocket()
        }

        // ✅ Female: poll incoming calls while app is open so calls appear on ANY screen.
        if (sessionManager.isLoggedIn() && sessionManager.getGender() == Gender.FEMALE) {
            startFemaleIncomingCallPollingIfNeeded()
        }
        
        // ✅ FIX: No need to handle call intent here anymore
        // It's now handled in onCreate() before setContent for proper timing
        // This prevents double-handling and race conditions
    }
    
    override fun onPause() {
        super.onPause()
        // User left the app:
        // - MALE: set offline
        // - FEMALE: keep online if Audio/Video availability is ON (calls must arrive via FCM in background)
        updateOnlinePresenceForCurrentSession(appInForeground = false)
        // Note: Don't disconnect WebSocket here - keep connection for background calls

        // Stop polling when app is backgrounded; FCM handles background incoming calls.
        femaleIncomingCallJob?.cancel()
        femaleIncomingCallJob = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Disconnect WebSocket when app is destroyed
        webSocketManager.disconnect()
        Log.d("MainActivity", "🔌 WebSocket disconnected")
        
        // Unregister broadcast receivers
        try {
            unregisterReceiver(callAcceptedReceiver)
            Log.d("MainActivity", "✅ Call acceptance receiver unregistered")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error unregistering call receiver", e)
        }
        try {
            unregisterReceiver(truecallerAuthReceiver)
            Log.d("MainActivity", "✅ Truecaller auth receiver unregistered")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error unregistering Truecaller receiver", e)
        }

        femaleIncomingCallJob?.cancel()
        femaleIncomingCallJob = null
    }

    private fun startFemaleIncomingCallPollingIfNeeded() {
        if (femaleIncomingCallJob?.isActive == true) return

        femaleIncomingCallJob = lifecycleScope.launch {
            while (true) {
                try {
                    if (!sessionManager.isLoggedIn() || sessionManager.getGender() != Gender.FEMALE) return@launch
                    // Don't trigger if user is already in a call or incoming call screen
                    if (CallStateManager.isBusy) {
                        delay(1500)
                        continue
                    }

                    val result = repository.getIncomingCalls()
                    result.onSuccess { incomingCalls ->
                        val latest = selectLatestValidIncomingCall(incomingCalls)
                        if (latest != null) {
                            launchIncomingCallServiceIfNeeded(latest)
                        }
                    }
                } catch (_: Throwable) {
                }

                delay(3000)
            }
        }
    }

    private fun selectLatestValidIncomingCall(incomingCalls: List<IncomingCallDto>): IncomingCallDto? {
        if (incomingCalls.isEmpty()) return null

        val currentTime = System.currentTimeMillis()
        val twentySecondsAgo = currentTime - 20_000

        return incomingCalls.firstOrNull { call ->
            val callId = call.id
            if (callId.isBlank()) return@firstOrNull false
            if (CallStateManager.isProcessed(callId)) return@firstOrNull false
            if (launchedIncomingCallIds.contains(callId)) return@firstOrNull false

            val isRecent = try {
                parseCallTimestamp(call.createdAt) >= twentySecondsAgo
            } catch (_: Throwable) {
                false
            }
            val statusOk =
                call.status.equals("ringing", ignoreCase = true) ||
                    call.status.equals("CONNECTING", ignoreCase = true)

            isRecent && statusOk
        }
    }

    private fun parseCallTimestamp(timestamp: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.parse(timestamp)?.time ?: 0L
    }

    private fun launchIncomingCallServiceIfNeeded(call: IncomingCallDto) {
        if (CallStateManager.isBusy) return
        if (CallStateManager.isProcessed(call.id)) return
        if (launchedIncomingCallIds.contains(call.id)) return

        launchedIncomingCallIds.add(call.id)

        try {
            val serviceIntent = Intent(
                applicationContext,
                IncomingCallService::class.java
            ).apply {
                action = IncomingCallService.ACTION_INCOMING_CALL
                putExtra(IncomingCallService.EXTRA_CALLER_ID, call.callerId)
                putExtra(IncomingCallService.EXTRA_CALLER_NAME, call.callerName)
                putExtra(IncomingCallService.EXTRA_CALLER_PHOTO, call.callerImage)
                putExtra(IncomingCallService.EXTRA_CHANNEL_ID, call.channelName ?: "")
                putExtra(IncomingCallService.EXTRA_AGORA_TOKEN, call.agoraToken ?: "")
                putExtra(IncomingCallService.EXTRA_CALL_ID, call.id)
                putExtra(IncomingCallService.EXTRA_AGORA_APP_ID, call.agoraAppId ?: "")
                putExtra(IncomingCallService.EXTRA_CALL_TYPE, call.callType)
                putExtra(IncomingCallService.EXTRA_BALANCE_TIME, call.balanceTime)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            // Mark as processed only after we successfully started the service,
            // so we don't permanently suppress calls if the service start fails.
            CallStateManager.markAsProcessed(call.id)
            Log.d("MainActivity", "✅ Female incoming call launched globally: ${call.id}")
        } catch (e: Exception) {
            launchedIncomingCallIds.remove(call.id)
            Log.e("MainActivity", "❌ Failed to start IncomingCallService (global)", e)
        }
    }
    
    /**
     * ⚡ Connect to WebSocket for INSTANT call status updates (MALE USERS ONLY)
     * 
     * Female users use FCM only for incoming calls which is more reliable
     * when the app is killed/in background. Male users need WebSocket for:
     * - Instant call accepted/rejected notifications from females
     * - Call status updates during active calls
     * - Call cancellation notifications
     */
    private fun connectWebSocket() {
        Log.d("MainActivity", "========================================")
        Log.d("MainActivity", "[websocket_check] connectWebSocket() called")
        Log.d("MainActivity", "Is logged in: ${sessionManager.isLoggedIn()}")
        Log.d("MainActivity", "Gender: ${sessionManager.getGender()}")
        Log.d("MainActivity", "User ID: ${sessionManager.getUserId()}")
        Log.d("MainActivity", "========================================")
        
        // Only connect if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Log.w("MainActivity", "[websocket_check] ⚠️ User not logged in, skipping WebSocket connection")
            return
        }
        
        // ✅ Only connect for MALE users (females use FCM only)
        if (sessionManager.getGender() != Gender.MALE) {
            Log.d("MainActivity", "[websocket_check] ℹ️ Female user - skipping WebSocket (using FCM only)")
            return
        }
        
        if (webSocketManager.isConnected()) {
            Log.d("MainActivity", "[websocket_check] ✅ WebSocket already connected")
            return
        }
        
        lifecycleScope.launch {
            try {
                Log.d("MainActivity", "========================================")
                Log.d("MainActivity", "[websocket_check] ⚡ Attempting WebSocket connection (Male user)")
                Log.d("MainActivity", "  - User ID: ${sessionManager.getUserId()}")
                Log.d("MainActivity", "  - Has Token: ${!sessionManager.getAuthToken().isNullOrBlank()}")
                Log.d("MainActivity", "========================================")
                
                webSocketManager.connect()
                
                // Wait a bit and check connection status
                kotlinx.coroutines.delay(3000)
                val isConnected = webSocketManager.isConnected()
                Log.d("MainActivity", "[websocket_check] Connection check after 3 seconds:")
                Log.d("MainActivity", "  Connected: $isConnected")
                
                if (isConnected) {
                    Log.d("MainActivity", "[websocket_check] ✅ WebSocket connected successfully!")
                } else {
                    Log.w("MainActivity", "[websocket_check] ⚠️ WebSocket connection attempt finished but NOT connected")
                    Log.w("MainActivity", "  Check logs above for connection errors")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "[websocket_check] ❌ WebSocket connection error: ${e.message}", e)
            }
        }
    }
    
    private fun updateOnlineStatus(isOnline: Boolean) {
        // Only update if user is logged in
        if (sessionManager.isLoggedIn()) {
            lifecycleScope.launch {
                val result = repository.updateOnlineStatus(isOnline)
                result.onSuccess {
                    Log.d("MainActivity", "✅ Online status updated: ${if (isOnline) "ONLINE" else "OFFLINE"}")
                }.onFailure { error ->
                    Log.w("MainActivity", "⚠️ Failed to update online status: ${error.message}")
                    // Don't crash the app if this fails, just log it
                }
            }
        }
    }

    /**
     * Presence rules:
     * - FEMALE creators: online should follow availability toggles, not app foreground/background.
     *   This ensures male can call while female is in recent/background and FCM can deliver.
     * - MALE users: online follows app foreground/background.
     */
    private fun updateOnlinePresenceForCurrentSession(appInForeground: Boolean) {
        if (!sessionManager.isLoggedIn()) return

        val gender = sessionManager.getGender()
        if (gender == Gender.FEMALE) {
            val available = sessionManager.getAudioStatus() == 1 || sessionManager.getVideoStatus() == 1
            updateOnlineStatus(isOnline = available)
            if (available) {
                lifecycleScope.launch {
                    repository.updateOnlineDatetime()
                        .onFailure { err ->
                            Log.w("MainActivity", "⚠️ Failed to update online datetime: ${err.message}")
                        }
                }
            }
        } else {
            updateOnlineStatus(isOnline = appInForeground)
        }
    }
    
    /**
     * Handle call accepted broadcast
     */
    private fun handleCallAccepted(intent: Intent) {
        Log.d("MainActivity", "✅ Call accepted broadcast received in MainActivity")
        
        // Extract call data
        val callerId = intent.getStringExtra("caller_id")
        val callId = intent.getStringExtra("call_id")
        val agoraAppId = intent.getStringExtra("agora_app_id")
        val agoraToken = intent.getStringExtra("agora_token")
        val channelId = intent.getStringExtra("channel_id")
        val callType = intent.getStringExtra("call_type")
        val balanceTime = intent.getStringExtra("balance_time")
        
        Log.d("MainActivity", "📞 Call data from broadcast:")
        Log.d("MainActivity", "  - Caller ID: $callerId")
        Log.d("MainActivity", "  - Call ID: $callId")
        Log.d("MainActivity", "  - Call Type: $callType")
        Log.d("MainActivity", "  - Channel: $channelId")
        Log.d("MainActivity", "  - Agora App ID: $agoraAppId")
        Log.d("MainActivity", "  - Token: ${if (agoraToken.isNullOrEmpty()) "EMPTY" else "Present"}")
        Log.d("MainActivity", "  - Balance Time: ${if (balanceTime.isNullOrEmpty()) "EMPTY" else balanceTime}")
        
        // Validate required data
        if (callerId == null || callId == null || agoraAppId == null || channelId == null) {
            Log.e("MainActivity", "❌ Missing required call data, cannot navigate")
            return
        }
        
        // Set pending navigation (will be handled in setContent)
        pendingCallNavigation.value = PendingCallNavigation(
            callerId = callerId,
            callId = callId,
            agoraAppId = agoraAppId,
            agoraToken = agoraToken ?: "",
            channelId = channelId,
            callType = callType ?: "AUDIO",
            balanceTime = balanceTime ?: ""
        )
        
        Log.d("MainActivity", "📋 Pending call navigation set, will navigate when composable recomposes")
    }
    
    /**
     * Handle call navigation from intent extras
     */
    private fun handleCallNavigationFromIntent(intent: Intent) {
        Log.d("MainActivity", "🔍 Extracting call data from intent...")
        
        val callerId = intent.getStringExtra("caller_id")
        val callId = intent.getStringExtra("call_id")
        val agoraAppId = intent.getStringExtra("agora_app_id")
        val agoraToken = intent.getStringExtra("agora_token")
        val channelId = intent.getStringExtra("channel_id")
        val callType = intent.getStringExtra("call_type")
        val balanceTime = intent.getStringExtra("balance_time")
        
        Log.d("MainActivity", "📞 Call data from intent:")
        Log.d("MainActivity", "  - Caller ID: $callerId")
        Log.d("MainActivity", "  - Call ID: $callId")
        Log.d("MainActivity", "  - Call Type: $callType")
        Log.d("MainActivity", "  - Channel: $channelId")
        Log.d("MainActivity", "  - Agora App ID: $agoraAppId")
        Log.d("MainActivity", "  - Token: ${if (agoraToken.isNullOrEmpty()) "EMPTY" else "Present"}")
        Log.d("MainActivity", "  - Balance Time: ${if (balanceTime.isNullOrEmpty()) "EMPTY" else balanceTime}")
        
        // Validate required data
        if (callerId == null || callId == null || agoraAppId == null || channelId == null) {
            Log.e("MainActivity", "❌ Missing required call data from intent, cannot navigate")
            return
        }
        
        // Set pending navigation
        pendingCallNavigation.value = PendingCallNavigation(
            callerId = callerId,
            callId = callId,
            agoraAppId = agoraAppId,
            agoraToken = agoraToken ?: "",
            channelId = channelId,
            callType = callType ?: "AUDIO",
            balanceTime = balanceTime ?: ""
        )
        
        Log.d("MainActivity", "📋 Pending call navigation set from intent!")
    }
}

/**
 * Beautiful Bottom Sheet for Best Offers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BestOffersBottomSheet(
    offers: List<BestOfferDto>,
    show: Boolean,
    onDismiss: () -> Unit,
    onNavigateToWallet: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    // Handle back button press OUTSIDE ModalBottomSheet for Android 13+ compatibility
    BackHandler(enabled = show && sheetState.isVisible) {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }
    
    if (show && offers.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Background,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(PrimaryLight, RoundedCornerShape(2.dp))
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 40.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎁 Best Offers",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Limited time deals just for you!",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Offers List
                offers.forEach { offer ->
                    BestOfferCard(
                        offer = offer,
                        onClick = {
                            onDismiss()
                            onNavigateToWallet()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // View All Button
                Button(
                    onClick = {
                        onDismiss()
                        onNavigateToWallet()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "View All Offers",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BestOfferCard(
    offer: BestOfferDto,
    onClick: () -> Unit
) {
    // User request: remove border, use white background, add soft shadow behind
    OnlyCareSoftShadowContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        shadowOffsetY = 4.dp,
        shadowColor = Border.copy(alpha = 0.28f)
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = Background,
            modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Coins
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GoldCoinIcon(size = 22.dp)
                    Text(
                        text = "${offer.coins}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Coins",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Price
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "₹${offer.discountPrice.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    if (offer.save > 0) {
                        Text(
                            text = "₹${offer.price.toInt()}",
                            fontSize = 16.sp,
                            color = TextSecondary,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = com.onlycare.app.presentation.theme.SuccessGreen,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 0.75.dp,
                                color = com.onlycare.app.presentation.theme.SuccessGreen
                            )
                        ) {
                            Text(
                                text = "Save ₹${offer.save}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Add Coin Button
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryLight,
                    contentColor = PrimaryDark
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(42.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GoldCoinIcon(size = 14.dp)
                Text(
                    text = "Add Coin",
                    fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                )
            }
        }
        }
    }
    }
}

@Composable
private fun GoldCoinIcon(
    size: Dp,
    modifier: Modifier = Modifier
) {
    // Wallet-style 3-layer "3D" coin (no shadows)
    val darkGold = Color(0xFFB8860B)
    val midGold = Color(0xFFDAA520)
    val brightGold = Color(0xFFFFD700)

    Box(modifier = modifier.size(size)) {
        // Bottom layer
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = darkGold,
            modifier = Modifier
                .size(size)
                .offset(x = 1.dp, y = 2.dp)
        )
        // Middle layer
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = midGold,
            modifier = Modifier
                .size(size - 1.dp)
                .offset(y = 1.dp)
        )
        // Top layer
        Icon(
            imageVector = Icons.Default.Circle,
            contentDescription = null,
            tint = brightGold,
            modifier = Modifier.size(size - 2.dp)
        )
    }
}

