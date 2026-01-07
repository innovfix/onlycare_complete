package com.onlycare.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.onlycare.app.presentation.screens.auth.*
import com.onlycare.app.presentation.screens.main.*
import com.onlycare.app.presentation.screens.profile.*
import com.onlycare.app.presentation.screens.wallet.*
import com.onlycare.app.presentation.screens.chat.*
import com.onlycare.app.presentation.screens.call.*
import com.onlycare.app.presentation.screens.female.*
import com.onlycare.app.presentation.screens.settings.*

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Flow
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // Login with optional phone prefill
        composable(
            route = Screen.Login.routeWithPhone,
            arguments = listOf(
                navArgument("phone") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            LoginScreen(navController = navController)
        }
        
        composable(
            route = Screen.VerifyOTP.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("otpId") { type = NavType.StringType }
            )
        ) {
            VerifyOTPScreen(navController = navController)
        }
        
        composable(Screen.SelectGender.route) {
            SelectGenderScreen(navController = navController)
        }
        
        composable(Screen.SelectLanguage.route) {
            SelectLanguageScreen(navController = navController)
        }
        
        composable(Screen.SetupProfile.route) {
            SetupProfileScreen(navController = navController)
        }
        
        composable(Screen.VoiceIdentification.route) {
            VoiceIdentificationScreen(navController = navController)
        }

        composable(Screen.VerificationPending.route) {
            VerificationPendingScreen(navController = navController)
        }
        
        composable(Screen.GrantPermissions.route) {
            GrantPermissionsScreen(navController = navController)
        }
        
        // Main App
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        
        // User Profile
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            UserProfileScreen(navController = navController)
        }
        
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        
        // Wallet & Payments
        composable(
            route = Screen.Wallet.route,
            arguments = listOf(
                navArgument("message") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            WalletScreen(
                navController = navController,
                message = backStackEntry.arguments?.getString("message")
            )
        }
        
        composable(
            route = Screen.Payment.route,
            arguments = listOf(navArgument("packageId") { type = NavType.StringType })
        ) { backStackEntry ->
            val packageId = backStackEntry.arguments?.getString("packageId") ?: ""
            PaymentScreen(
                navController = navController,
                packageId = packageId
            )
        }
        
        composable(Screen.Transactions.route) {
            TransactionsScreen(navController = navController)
        }
        
        // Chat
        composable(Screen.ChatList.route) {
            ChatListScreen(navController = navController)
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            ChatScreen(navController = navController)
        }
        
        // Calls
        composable(
            route = Screen.RandomCall.route,
            arguments = listOf(navArgument("callType") { type = NavType.StringType })
        ) { backStackEntry ->
            val callType = backStackEntry.arguments?.getString("callType") ?: "audio"
            RandomCallScreen(
                navController = navController,
                callType = callType
            )
        }

        composable(
            route = Screen.CallConnecting.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("callType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val callType = backStackEntry.arguments?.getString("callType") ?: "audio"
            CallConnectingScreen(
                navController = navController,
                userId = userId,
                callType = callType
            )
        }
        
        composable(
            route = Screen.AudioCall.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("callId") { 
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("appId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("token") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("channel") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("role") {
                    type = NavType.StringType
                    defaultValue = "caller"
                },
                navArgument("balanceTime") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val callId = backStackEntry.arguments?.getString("callId") ?: ""
            val appId = backStackEntry.arguments?.getString("appId") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val channel = backStackEntry.arguments?.getString("channel") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "caller"
            val balanceTime = backStackEntry.arguments?.getString("balanceTime") ?: ""
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
        
        composable(
            route = Screen.VideoCall.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("callId") { 
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("appId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("token") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("channel") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("role") {
                    type = NavType.StringType
                    defaultValue = "caller"
                },
                navArgument("balanceTime") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("upgrade") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val callId = backStackEntry.arguments?.getString("callId") ?: ""
            val appId = backStackEntry.arguments?.getString("appId") ?: ""
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val channel = backStackEntry.arguments?.getString("channel") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "caller"
            val balanceTime = backStackEntry.arguments?.getString("balanceTime") ?: ""
            val upgrade = backStackEntry.arguments?.getBoolean("upgrade") ?: false
            VideoCallScreen(
                navController = navController,
                userId = userId,
                callId = callId,
                appId = appId,
                token = token,
                channel = channel,
                role = role,
                balanceTime = balanceTime,
                upgrade = upgrade
            )
        }
        
        composable(
            route = Screen.CallEnded.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("callId") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType },
                navArgument("coinsSpent") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val callId = backStackEntry.arguments?.getString("callId") ?: ""
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
            val coinsSpent = backStackEntry.arguments?.getInt("coinsSpent") ?: 0
            
            CallEndedScreen(
                navController = navController,
                userId = userId,
                callId = callId,
                duration = duration,
                coinsSpent = coinsSpent
            )
        }
        
        composable(
            route = Screen.RateUser.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("callId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val callId = backStackEntry.arguments?.getString("callId") ?: ""
            RateUserScreen(
                navController = navController,
                userId = userId,
                callId = callId
            )
        }
        
        // Female Specific
        composable(Screen.Earnings.route) {
            EarningsScreen(navController = navController)
        }
        
        composable(Screen.Withdraw.route) {
            WithdrawScreen(navController = navController)
        }
        
        composable(Screen.BankDetails.route) {
            BankDetailsScreen(navController = navController)
        }
        
        composable(Screen.KYC.route) {
            KYCScreen(navController = navController)
        }
        
        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        composable(Screen.AccountPrivacy.route) {
            AccountPrivacyScreen(navController = navController)
        }
        
        composable(Screen.BlockedUsers.route) {
            BlockedUsersScreen(navController = navController)
        }
        
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(navController = navController)
        }
        
        composable(Screen.ReferEarn.route) {
            ReferEarnScreen(navController = navController)
        }
        
        composable(Screen.Terms.route) {
            TermsScreen(navController = navController)
        }
        
        composable(Screen.RefundPolicy.route) {
            RefundPolicyScreen(navController = navController)
        }
        
        composable(Screen.CommunityGuidelines.route) {
            CommunityGuidelinesScreen(navController = navController)
        }
        
        composable(Screen.Friends.route) {
            FriendsScreen(navController = navController)
        }
        
    }
}



