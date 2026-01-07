package com.onlycare.app.presentation.navigation

sealed class Screen(val route: String) {
    // Auth Flow
    object Splash : Screen("splash")
    object Login : Screen("login") {
        const val routeWithPhone: String = "login?phone={phone}"
        fun createRoute(phone: String? = null): String =
            if (phone.isNullOrBlank()) route else "login?phone=$phone"
    }
    object VerifyOTP : Screen("verify_otp/{phone}/{otpId}") {
        fun createRoute(phone: String, otpId: String) = "verify_otp/$phone/$otpId"
    }
    object SelectGender : Screen("select_gender")
    object SelectLanguage : Screen("select_language")
    object SetupProfile : Screen("setup_profile")
    object VoiceIdentification : Screen("voice_identification")
    object VerificationPending : Screen("verification_pending")
    object GrantPermissions : Screen("grant_permissions")
    
    // Main App
    object Main : Screen("main")
    object Home : Screen("home")
    object Recent : Screen("recent")
    object Profile : Screen("profile")
    object Friends : Screen("friends")
    
    // User Profile
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    object EditProfile : Screen("edit_profile")
    
    // Wallet & Payments
    object Wallet : Screen("wallet?message={message}") {
        fun createRoute(message: String? = null) = if (message != null) "wallet?message=$message" else "wallet"
    }
    object Payment : Screen("payment/{packageId}") {
        fun createRoute(packageId: String) = "payment/$packageId"
    }
    object Transactions : Screen("transactions")
    
    // Chat
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }
    
    // Calls
    object CallConnecting : Screen("call_connecting/{userId}/{callType}") {
        fun createRoute(userId: String, callType: String) = "call_connecting/$userId/$callType"
    }
    object RandomCall : Screen("random_call/{callType}") {
        fun createRoute(callType: String) = "random_call/$callType"
    }
    object AudioCall : Screen("audio_call/{userId}/{callId}?appId={appId}&token={token}&channel={channel}&role={role}&balanceTime={balanceTime}") {
        fun createRoute(
            userId: String,
            callId: String = "",
            appId: String = "",
            token: String = "",
            channel: String = "",
            role: String = "caller",
            balanceTime: String = ""
        ) = "audio_call/$userId/$callId?appId=$appId&token=$token&channel=$channel&role=$role&balanceTime=$balanceTime"
    }
    object VideoCall : Screen("video_call/{userId}/{callId}?appId={appId}&token={token}&channel={channel}&role={role}&balanceTime={balanceTime}&upgrade={upgrade}") {
        fun createRoute(
            userId: String,
            callId: String = "",
            appId: String = "",
            token: String = "",
            channel: String = "",
            role: String = "caller",
            balanceTime: String = "",
            upgrade: Boolean = false
        ) = "video_call/$userId/$callId?appId=$appId&token=$token&channel=$channel&role=$role&balanceTime=$balanceTime&upgrade=$upgrade"
    }
    object CallEnded : Screen("call_ended/{userId}/{callId}/{duration}/{coinsSpent}") {
        fun createRoute(
            userId: String,
            callId: String,
            duration: Int,  // in seconds
            coinsSpent: Int
        ) = "call_ended/$userId/$callId/$duration/$coinsSpent"
    }
    object RateUser : Screen("rate_user/{userId}/{callId}") {
        fun createRoute(userId: String, callId: String) = "rate_user/$userId/$callId"
    }
    
    // Female Specific
    object Earnings : Screen("earnings")
    object Withdraw : Screen("withdraw")
    object BankDetails : Screen("bank_details")
    object KYC : Screen("kyc")
    
    // Settings
    object Settings : Screen("settings")
    object AccountPrivacy : Screen("account_privacy")
    object BlockedUsers : Screen("blocked_users")
    object PrivacyPolicy : Screen("privacy_policy")
    object ReferEarn : Screen("refer_earn")
    object Terms : Screen("terms")
    object RefundPolicy : Screen("refund_policy")
    object CommunityGuidelines : Screen("community_guidelines")
    
}



