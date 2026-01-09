package com.onlycare.app.presentation.screens.call

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.onlycare.app.presentation.navigation.Screen
import com.onlycare.app.presentation.theme.Background
import com.onlycare.app.presentation.theme.OnlyCareTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Dedicated Activity for rating screen
 * Launched after call ends to collect user feedback
 */
@AndroidEntryPoint
class RatingActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "RatingActivity"
        
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_CALL_ID = "call_id"
        
        /**
         * Create intent to launch RatingActivity
         */
        fun createIntent(
            context: Context,
            userId: String,
            callId: String
        ): Intent {
            return Intent(context, RatingActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_CALL_ID, callId)
                // Clear back stack - rating should be a fresh start
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
    
    private var userId: String = ""
    private var callId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "════════════════════════════════════════════════════════════")
        Log.d(TAG, "⭐ RatingActivity.onCreate() - STARTING")
        Log.d(TAG, "════════════════════════════════════════════════════════════")
        
        super.onCreate(savedInstanceState)
        
        // Extract data from intent
        extractRatingDataAndSetupUI(intent)
    }
    
    /**
     * Extract rating data from intent and setup UI
     */
    private fun extractRatingDataAndSetupUI(intent: Intent) {
        Log.d(TAG, "📦 Extracting rating data from intent...")
        
        userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
        callId = intent.getStringExtra(EXTRA_CALL_ID) ?: ""
        
        Log.d(TAG, "════════════════════════════════════════════════════════════")
        Log.d(TAG, "⭐ RatingActivity INITIALIZED")
        Log.d(TAG, "  - User ID: $userId")
        Log.d(TAG, "  - Call ID: $callId")
        Log.d(TAG, "════════════════════════════════════════════════════════════")
        
        // Validate required data
        if (userId.isEmpty() || callId.isEmpty()) {
            Log.e(TAG, "════════════════════════════════════════════════════════════")
            Log.e(TAG, "❌ CRITICAL ERROR: Missing required rating data!")
            Log.e(TAG, "  - User ID empty: ${userId.isEmpty()}")
            Log.e(TAG, "  - Call ID empty: ${callId.isEmpty()}")
            Log.e(TAG, "  Finishing activity and navigating to home...")
            Log.e(TAG, "════════════════════════════════════════════════════════════")
            navigateToMainActivity()
            finish()
            return
        }
        
        // Setup Compose UI
        Log.d(TAG, "🎨 Setting up Compose UI...")
        try {
            setContent {
                OnlyCareTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Background
                    ) {
                        RatingActivityContent(
                            userId = userId,
                            callId = callId,
                            onNavigateToMain = { navigateToMainActivity() },
                            onFinish = { finish() }
                        )
                    }
                }
            }
            Log.d(TAG, "✅ Compose UI setup completed")
            Log.d(TAG, "════════════════════════════════════════════════════════════")
            Log.d(TAG, "✅ RatingActivity setup - COMPLETED SUCCESSFULLY")
            Log.d(TAG, "   Rating screen should now be visible!")
            Log.d(TAG, "════════════════════════════════════════════════════════════")
        } catch (e: Exception) {
            Log.e(TAG, "════════════════════════════════════════════════════════════")
            Log.e(TAG, "❌ CRITICAL ERROR: Failed to setup Compose UI!", e)
            Log.e(TAG, "════════════════════════════════════════════════════════════")
            e.printStackTrace()
            finish()
        }
    }
    
    /**
     * Navigate back to MainActivity
     */
    private fun navigateToMainActivity() {
        Log.d(TAG, "════════════════════════════════════════════════════════════")
        Log.d(TAG, "🏠 Navigating back to MainActivity from RatingActivity")
        Log.d(TAG, "════════════════════════════════════════════════════════════")
        
        val mainIntent = Intent(this, com.onlycare.app.presentation.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from_rating", true)
        }
        
        try {
            Log.d(TAG, "📱 Starting MainActivity")
            startActivity(mainIntent)
            
            // Use instant transition
            overridePendingTransition(0, 0)
            
            // Finish this activity
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                Log.d(TAG, "✅ Finishing RatingActivity")
                finish()
            }, 100)
            
            Log.d(TAG, "✅ MainActivity launch initiated")
            Log.d(TAG, "════════════════════════════════════════════════════════════")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to launch MainActivity", e)
            e.printStackTrace()
            finish()
        }
    }
}

/**
 * Content composable for RatingActivity
 */
@Composable
private fun RatingActivityContent(
    userId: String,
    callId: String,
    onNavigateToMain: () -> Unit,
    onFinish: () -> Unit
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "rating_screen"
    ) {
        composable("rating_screen") {
            RatingScreenWrapper(
                userId = userId,
                callId = callId,
                onNavigateToMain = onNavigateToMain,
                onFinish = onFinish
            )
        }
        
        // Handle navigation to main
        composable(Screen.Main.route) {
            // When navigating to main, call the callback
            LaunchedEffect(Unit) {
                onNavigateToMain()
            }
            
            // Show blank surface while transitioning
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Background
            ) {
                // Empty - instant transition
            }
        }
    }
}

/**
 * Wrapper for RateUserScreen that handles navigation
 */
@Composable
private fun RatingScreenWrapper(
    userId: String,
    callId: String,
    onNavigateToMain: () -> Unit,
    onFinish: () -> Unit
) {
    // Create a simple NavController just for RateUserScreen
    // (RateUserScreen expects NavController for navigation)
    val navController = rememberNavController()
    
    // Intercept navigation to Main route
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            if (backStackEntry.destination.route == Screen.Main.route) {
                onNavigateToMain()
            }
        }
    }
    
    // Show the rating screen with same UI and features
    RateUserScreen(
        navController = navController,
        userId = userId,
        callId = callId
    )
}
