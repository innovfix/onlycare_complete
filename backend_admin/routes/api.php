<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\UserController;
use App\Http\Controllers\Api\CallController;
use App\Http\Controllers\Api\WalletController;
use App\Http\Controllers\Api\EarningsController;
use App\Http\Controllers\Api\WithdrawalController;
use App\Http\Controllers\Api\BankAccountController;
use App\Http\Controllers\Api\KycController;
use App\Http\Controllers\Api\ChatController;
use App\Http\Controllers\Api\FriendController;
use App\Http\Controllers\Api\ReferralController;
use App\Http\Controllers\Api\ReportController;
use App\Http\Controllers\Api\NotificationController;
use App\Http\Controllers\Api\SettingsController;
use App\Http\Controllers\Api\ContentController;
use App\Http\Controllers\Api\AvatarController;

/*
|--------------------------------------------------------------------------
| API Routes - Only Care App v1
|--------------------------------------------------------------------------
|
| All API routes for the Only Care mobile application
|
*/

// API Version 1
Route::prefix('v1')->group(function () {
    
    // TEST ENDPOINT (No Auth Required)
    Route::get('/test-connection', function() {
        return response()->json([
            'success' => true,
            'message' => 'API is working!',
            'timestamp' => now()->toDateTimeString(),
            'mysql' => \DB::connection()->getPdo() ? 'Connected' : 'Not Connected'
        ]);
    });
    
    // LOG ENDPOINT - App can send error logs here
    Route::post('/log-error', function(\Illuminate\Http\Request $request) {
        \Log::error('APP ERROR: ' . $request->input('error'), [
            'details' => $request->input('details'),
            'timestamp' => $request->input('timestamp')
        ]);
        return response()->json(['success' => true]);
    });
    
    // ========================================
    // 1. AUTHENTICATION APIs (Public Routes)
    // ========================================
    // Truecaller OAuth login (outside auth prefix for compatibility)
    Route::post('/login', [AuthController::class, 'login']); // Truecaller login endpoint
    
    Route::prefix('auth')->group(function () {
        Route::post('/send-otp', [AuthController::class, 'sendOtp']);
        Route::post('/verify-otp', [AuthController::class, 'verifyOtp']);
        Route::post('/register', [AuthController::class, 'register']);
        
        // Gift APIs
        Route::post('/gifts_list', [AuthController::class, 'gifts_list']); // Public route
        
        // Protected auth routes
        Route::middleware('auth:sanctum')->group(function () {
            Route::post('/refresh-token', [AuthController::class, 'refreshToken']);
            Route::post('/logout', [AuthController::class, 'logout']);
            Route::post('/update-upi', [AuthController::class, 'update_upi']);
            Route::post('/update-pancard', [AuthController::class, 'update_pancard']);
            Route::post('/update-voice', [AuthController::class, 'update_voice']);
            Route::post('/send_gifts', [AuthController::class, 'send_gifts']); // Protected route
            Route::post('/send_gift_notification', [AuthController::class, 'sendGiftNotification']); // Protected route
            Route::post('/get_remaining_time', [AuthController::class, 'get_remaining_time']); // Get remaining call time
        });
    });

    // ========================================
    // 2. CONTENT APIs (Public Routes)
    // ========================================
    Route::prefix('content')->group(function () {
        Route::get('/privacy-policy', [ContentController::class, 'getPrivacyPolicy']);
        Route::get('/terms-conditions', [ContentController::class, 'getTermsAndConditions']);
        Route::get('/refund-policy', [ContentController::class, 'getRefundPolicy']);
        Route::get('/community-guidelines', [ContentController::class, 'getCommunityGuidelines']);
    });

    // ========================================
    // SETTINGS APIs (Public Routes)
    // ========================================
    Route::prefix('settings')->group(function () {
        Route::get('/app', [SettingsController::class, 'getAppSettings']);
    });

    // ========================================
    // AVATAR APIs (Public Routes)
    // ========================================
    Route::prefix('avatars')->group(function () {
        Route::post('/', [AvatarController::class, 'index']);
    });

    // ========================================
    // Protected Routes (Require Authentication)
    // ========================================
    Route::middleware('auth:sanctum')->group(function () {
        
        // ========================================
        // 3. USER APIs
        // ========================================
        Route::prefix('users')->group(function () {
            // Current user
            Route::get('/me', [UserController::class, 'me']);
            Route::put('/me', [UserController::class, 'updateProfile']);
            Route::post('/me', [UserController::class, 'updateProfile']); // Alternative POST endpoint
            Route::post('/me/status', [UserController::class, 'updateStatus']);
            Route::post('/me/call-availability', [UserController::class, 'updateCallAvailability']);
            Route::post('/me/update-online-datetime', [UserController::class, 'updateOnlineDateTime']);
            Route::get('/me/blocked', [UserController::class, 'getBlockedUsers']);
            
            // FCM Token for push notifications
            Route::post('/update-fcm-token', [UserController::class, 'updateFcmToken']);
            
            // Username availability check
            Route::get('/check-username', [UserController::class, 'checkUsernameAvailability']);
            
            // Female users listing
            Route::get('/females', [UserController::class, 'getFemales']);
            
            // Account deletion
            Route::delete('/account', [UserController::class, 'deleteAccount']);
            Route::post('/delete-account', [UserController::class, 'deleteAccount']); // Alternative POST endpoint
            
            // User actions (dynamic routes must come last)
            Route::get('/{userId}', [UserController::class, 'getUserById']);
            Route::post('/{userId}/block', [UserController::class, 'blockUser']);
            Route::post('/{userId}/unblock', [UserController::class, 'unblockUser']);
        });

        // ========================================
        // 4. CALL APIs
        // ========================================
        Route::prefix('calls')->group(function () {
            // ✅ IMPORTANT: Static routes MUST be defined BEFORE dynamic routes with {callId}
            // Otherwise Laravel will match '/history' as {callId}='history'
            Route::post('/initiate', [CallController::class, 'initiateCall']);
            Route::post('/random_user', [CallController::class, 'random_user']); // Random user matching
            Route::get('/incoming', [CallController::class, 'getIncomingCalls']);
            Route::get('/history', [CallController::class, 'getCallHistory']);
            Route::get('/recent-sessions', [CallController::class, 'getRecentSessions']);
            Route::get('/recent-callers', [CallController::class, 'getRecentCallers']);
            Route::post('/switch-to-video', [CallController::class, 'requestSwitchToVideo']); // ✅ NEW: Switch audio to video
            
            // Dynamic routes with {callId} parameter MUST come AFTER static routes
            Route::get('/{callId}', [CallController::class, 'getCallStatus']);
            Route::post('/{callId}/accept', [CallController::class, 'acceptCall']);
            Route::post('/{callId}/reject', [CallController::class, 'rejectCall']);
            Route::post('/{callId}/cancel', [CallController::class, 'cancelCall']);
            Route::post('/{callId}/end', [CallController::class, 'endCall']);
            Route::post('/{callId}/rate', [CallController::class, 'rateCall']);
            
            // Internal route for FCM notification (called by WebSocket server)
            Route::post('/notify-cancelled', [CallController::class, 'notifyCallCancelled']);
        });

        // ========================================
        // 5. WALLET & PAYMENT APIs
        // ========================================
        Route::prefix('wallet')->group(function () {
            Route::get('/packages', [WalletController::class, 'getPackages']);
            Route::post('/best-offers', [WalletController::class, 'getBestOffers']);
            Route::post('/purchase', [WalletController::class, 'initiatePurchase']);
            Route::post('/verify-purchase', [WalletController::class, 'verifyPurchase']);
            Route::get('/transactions', [WalletController::class, 'getTransactionHistory']);
            Route::get('/balance', [WalletController::class, 'getBalance']);
        });

        // ========================================
        // 6. EARNINGS & WITHDRAWAL APIs (Female Users)
        // ========================================
        Route::prefix('earnings')->group(function () {
            Route::get('/dashboard', [EarningsController::class, 'getDashboard']);
        });

        Route::prefix('withdrawals')->group(function () {
            Route::post('/request', [WithdrawalController::class, 'requestWithdrawal']);
            Route::get('/history', [WithdrawalController::class, 'getWithdrawalHistory']);
        });

        // ========================================
        // 7. BANK ACCOUNT APIs
        // ========================================
        Route::prefix('bank-accounts')->group(function () {
            Route::get('/', [BankAccountController::class, 'index']);
            Route::post('/', [BankAccountController::class, 'store']);
            Route::put('/{accountId}', [BankAccountController::class, 'update']);
            Route::delete('/{accountId}', [BankAccountController::class, 'destroy']);
        });

        // ========================================
        // 8. KYC APIs (Female Users)
        // ========================================
        Route::prefix('kyc')->group(function () {
            Route::get('/status', [KycController::class, 'getStatus']);
            Route::post('/submit', [KycController::class, 'submitDocuments']);
        });

        // ========================================
        // 9. CHAT APIs
        // ========================================
        Route::prefix('chat')->group(function () {
            Route::get('/conversations', [ChatController::class, 'getConversations']);
            Route::get('/{userId}/messages', [ChatController::class, 'getMessages']);
            Route::post('/{userId}/messages', [ChatController::class, 'sendMessage']);
            Route::post('/{userId}/mark-read', [ChatController::class, 'markAsRead']);
        });

        // ========================================
        // 10. FRIENDS APIs
        // ========================================
        Route::prefix('friends')->group(function () {
            Route::get('/', [FriendController::class, 'getFriends']);
            Route::post('/{userId}/request', [FriendController::class, 'sendRequest']);
            Route::post('/{userId}/accept', [FriendController::class, 'acceptRequest']);
            Route::post('/{userId}/reject', [FriendController::class, 'rejectRequest']);
            Route::delete('/{userId}', [FriendController::class, 'removeFriend']);
        });

        // ========================================
        // 11. REFERRAL APIs
        // ========================================
        Route::prefix('referral')->group(function () {
            Route::get('/code', [ReferralController::class, 'getReferralCode']);
            Route::post('/apply', [ReferralController::class, 'applyReferralCode']);
            Route::get('/history', [ReferralController::class, 'getReferralHistory']);
        });

        // ========================================
        // 12. REPORT APIs
        // ========================================
        Route::prefix('reports')->group(function () {
            Route::post('/user', [ReportController::class, 'reportUser']);
        });

        // ========================================
        // 13. NOTIFICATION APIs
        // ========================================
        Route::prefix('notifications')->group(function () {
            Route::get('/', [NotificationController::class, 'getNotifications']);
            Route::post('/{notificationId}/read', [NotificationController::class, 'markAsRead']);
            Route::post('/read-all', [NotificationController::class, 'markAllAsRead']);
        });
    });
});

// Default Laravel user route (kept for backward compatibility)
Route::middleware('auth:sanctum')->get('/user', function (Request $request) {
    return $request->user();
});

// Voice update routes (alternative paths for backward compatibility)
Route::middleware('auth:sanctum')->post('/api/auth/update_voice', [AuthController::class, 'update_voice']);
Route::middleware('auth:sanctum')->post('/api/update_voice', [AuthController::class, 'update_voice']);

// Random user route (alternative path for backward compatibility)
Route::middleware('auth:sanctum')->post('/api/random_user', [CallController::class, 'random_user']);

// Test notification route
Route::post('/send-test-notification', [\App\Http\Controllers\PushNotificationsController::class, 'sendTestNotification']);

// Cron job route for scheduled notifications
Route::get('/auth/cron_jobs', [AuthController::class, 'cron_jobs']);
