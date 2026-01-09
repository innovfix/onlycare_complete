<?php

use App\Http\Controllers\AdminAuthController;
use App\Http\Controllers\DashboardController;
use App\Http\Controllers\UserController;
use App\Http\Controllers\CallController;
use App\Http\Controllers\TransactionController;
use App\Http\Controllers\WithdrawalController;
use App\Http\Controllers\KycController;
use App\Http\Controllers\ReportController;
use App\Http\Controllers\ChatController;
use App\Http\Controllers\NotificationController;
use App\Http\Controllers\CoinPackageController;
use App\Http\Controllers\SettingController;
use App\Http\Controllers\AvatarController;
use App\Http\Controllers\ApiDocController;
<<<<<<< HEAD
use App\Http\Controllers\PolicyController;
use App\Http\Controllers\PublicPageController;
=======
use App\Http\Controllers\GiftsController;
use App\Http\Controllers\PushNotificationsController;
use App\Http\Controllers\ScreenNotificationsController;
>>>>>>> 0dbc77d8778a3756793d130c37a01ca0d1c65976
use Illuminate\Support\Facades\Route;

// ========================================
// PUBLIC PAGES (No Authentication Required)
// Support both hyphen and underscore URLs
// ========================================
Route::get('/privacy-policy', [PublicPageController::class, 'privacyPolicy'])->name('public.privacy-policy');
Route::get('/privacy_policy', [PublicPageController::class, 'privacyPolicy']);
Route::get('/terms-conditions', [PublicPageController::class, 'termsConditions'])->name('public.terms-conditions');
Route::get('/terms_conditions', [PublicPageController::class, 'termsConditions']);
Route::get('/refund-policy', [PublicPageController::class, 'refundPolicy'])->name('public.refund-policy');
Route::get('/refund_policy', [PublicPageController::class, 'refundPolicy']);
Route::get('/community-guidelines', [PublicPageController::class, 'communityGuidelines'])->name('public.community-guidelines');
Route::get('/community_guidelines', [PublicPageController::class, 'communityGuidelines']);

// Test route
Route::get('/test', function () {
    return 'Laravel is working!';
});

// API Documentation (Public Access for Developers)
Route::prefix('api-docs')->name('api.docs.')->group(function () {
    Route::get('/', [ApiDocController::class, 'index'])->name('index');
    Route::get('/auth', [ApiDocController::class, 'auth'])->name('auth');
    Route::get('/creators', [ApiDocController::class, 'creators'])->name('creators');
    Route::get('/wallet', [ApiDocController::class, 'wallet'])->name('wallet');
    Route::get('/calls', [ApiDocController::class, 'calls'])->name('calls');
    Route::get('/referrals', [ApiDocController::class, 'referrals'])->name('referrals');
    Route::get('/content', [ApiDocController::class, 'content'])->name('content');
});

// Guest routes (not authenticated)
Route::middleware('guest:admin')->group(function () {
    Route::get('/login', [AdminAuthController::class, 'showLoginForm'])->name('login');
    Route::post('/login', [AdminAuthController::class, 'login'])->name('login.post');
});

// Authenticated admin routes
Route::middleware('auth:admin')->group(function () {
    Route::post('/logout', [AdminAuthController::class, 'logout'])->name('logout');
    
    // Dashboard
    Route::get('/', [DashboardController::class, 'index'])->name('dashboard');
    Route::get('/dashboard', [DashboardController::class, 'index'])->name('dashboard.index');
    
    // User Management
    Route::prefix('users')->name('users.')->group(function () {
        Route::get('/', [UserController::class, 'index'])->name('index');
        Route::get('/{id}', [UserController::class, 'show'])->name('show');
        Route::get('/{id}/edit', [UserController::class, 'edit'])->name('edit');
        Route::put('/{id}', [UserController::class, 'update'])->name('update');
        Route::post('/{id}/block', [UserController::class, 'block'])->name('block');
        Route::post('/{id}/unblock', [UserController::class, 'unblock'])->name('unblock');
        Route::post('/{id}/add-coins', [UserController::class, 'addCoins'])->name('add-coins');
        Route::post('/{id}/generate-token', [UserController::class, 'generateToken'])->name('generate-token');
        Route::post('/{id}/toggle-online', [UserController::class, 'toggleOnlineStatus'])->name('toggle-online');
        Route::delete('/{id}', [UserController::class, 'destroy'])->name('destroy');
    });
    
    // Call Management
    Route::prefix('calls')->name('calls.')->group(function () {
        Route::get('/', [CallController::class, 'index'])->name('index');
        Route::post('/delete-by-phone', [CallController::class, 'deleteByPhone'])->name('delete-by-phone');
        Route::get('/{id}', [CallController::class, 'show'])->name('show');
        Route::delete('/{id}', [CallController::class, 'destroy'])->name('destroy');
        Route::get('/analytics', [CallController::class, 'analytics'])->name('analytics');
    });
    
    // Transaction Management
    Route::prefix('transactions')->name('transactions.')->group(function () {
        Route::get('/', [TransactionController::class, 'index'])->name('index');
        Route::get('/{id}', [TransactionController::class, 'show'])->name('show');
        Route::get('/export', [TransactionController::class, 'export'])->name('export');
    });
    
    // Withdrawal Management
    Route::prefix('withdrawals')->name('withdrawals.')->group(function () {
        Route::get('/', [WithdrawalController::class, 'index'])->name('index');
        Route::get('/{id}', [WithdrawalController::class, 'show'])->name('show');
        Route::post('/{id}/approve', [WithdrawalController::class, 'approve'])->name('approve');
        Route::post('/{id}/reject', [WithdrawalController::class, 'reject'])->name('reject');
        Route::post('/{id}/complete', [WithdrawalController::class, 'complete'])->name('complete');
    });
    
    // KYC Verification
    Route::prefix('kyc')->name('kyc.')->group(function () {
        Route::get('/', [KycController::class, 'index'])->name('index');
        Route::get('/{userId}/review', [KycController::class, 'review'])->name('review');
        Route::post('/{userId}/approve', [KycController::class, 'approve'])->name('approve');
        Route::post('/{userId}/reject', [KycController::class, 'reject'])->name('reject');
    });
    
    // Reports & Content Moderation
    Route::prefix('reports')->name('reports.')->group(function () {
        Route::get('/', [ReportController::class, 'index'])->name('index');
        Route::get('/{id}', [ReportController::class, 'show'])->name('show');
        Route::post('/{id}/resolve', [ReportController::class, 'resolve'])->name('resolve');
        Route::post('/{id}/dismiss', [ReportController::class, 'dismiss'])->name('dismiss');
    });
    
    // Chat Management
    Route::prefix('chats')->name('chats.')->group(function () {
        Route::get('/', [ChatController::class, 'index'])->name('index');
        Route::get('/{userId}', [ChatController::class, 'show'])->name('show');
        Route::delete('/{id}', [ChatController::class, 'destroy'])->name('destroy');
    });
    
    // Notifications Management
    Route::prefix('notifications')->name('notifications.')->group(function () {
        Route::get('/', [NotificationController::class, 'index'])->name('index');
        Route::get('/{id}', [NotificationController::class, 'show'])->name('show');
        Route::post('/{id}/mark-read', [NotificationController::class, 'markRead'])->name('mark-read');
        Route::post('/mark-all-read', [NotificationController::class, 'markAllRead'])->name('mark-all-read');
        Route::delete('/{id}', [NotificationController::class, 'destroy'])->name('destroy');
    });
    
    // Push Notifications (Immediate)
    Route::resource('push_notifications', PushNotificationsController::class);
    
    // Scheduled Notifications
    Route::resource('screen_notifications', ScreenNotificationsController::class);
    
    // Coin Packages
    Route::prefix('coin-packages')->name('coin-packages.')->group(function () {
        Route::get('/', [CoinPackageController::class, 'index'])->name('index');
        Route::get('/create', [CoinPackageController::class, 'create'])->name('create');
        Route::post('/', [CoinPackageController::class, 'store'])->name('store');
        Route::get('/{id}/edit', [CoinPackageController::class, 'edit'])->name('edit');
        Route::put('/{id}', [CoinPackageController::class, 'update'])->name('update');
        Route::delete('/{id}', [CoinPackageController::class, 'destroy'])->name('destroy');
    });
    
    // Gift Management
    Route::prefix('gifts')->name('gifts.')->group(function () {
        Route::get('/', [GiftsController::class, 'index'])->name('index');
        Route::get('/create', [GiftsController::class, 'create'])->name('create');
        Route::post('/', [GiftsController::class, 'store'])->name('store');
        Route::get('/{id}/edit', [GiftsController::class, 'edit'])->name('edit');
        Route::put('/{id}', [GiftsController::class, 'update'])->name('update');
        Route::delete('/{id}', [GiftsController::class, 'destroy'])->name('destroy');
    });
    
    // Settings
    Route::prefix('settings')->name('settings.')->group(function () {
        Route::get('/', [SettingController::class, 'index'])->name('index');
        Route::post('/', [SettingController::class, 'update'])->name('update');
    });
<<<<<<< HEAD

    // Policies Management (support both hyphen and underscore URLs)
    Route::prefix('policies')->name('policies.')->group(function () {
        Route::get('/', [PolicyController::class, 'index'])->name('index');
        Route::get('/privacy-policy', [PolicyController::class, 'editPrivacyPolicy'])->name('privacy-policy');
        Route::get('/privacy_policy', [PolicyController::class, 'editPrivacyPolicy']);
        Route::get('/terms-conditions', [PolicyController::class, 'editTermsConditions'])->name('terms-conditions');
        Route::get('/terms_conditions', [PolicyController::class, 'editTermsConditions']);
        Route::get('/refund-policy', [PolicyController::class, 'editRefundPolicy'])->name('refund-policy');
        Route::get('/refund_policy', [PolicyController::class, 'editRefundPolicy']);
        Route::get('/community-guidelines', [PolicyController::class, 'editCommunityGuidelines'])->name('community-guidelines');
        Route::get('/community_guidelines', [PolicyController::class, 'editCommunityGuidelines']);
        Route::put('/{key}', [PolicyController::class, 'update'])->name('update');
    });
=======
    
    // Avatar Management
    Route::prefix('avatars')->name('avatars.')->group(function () {
        Route::get('/', [AvatarController::class, 'index'])->name('index');
        Route::get('/create', [AvatarController::class, 'create'])->name('create');
        Route::post('/', [AvatarController::class, 'store'])->name('store');
        Route::get('/{id}/edit', [AvatarController::class, 'edit'])->name('edit');
        Route::put('/{id}', [AvatarController::class, 'update'])->name('update');
        Route::delete('/{id}', [AvatarController::class, 'destroy'])->name('destroy');
    });
    
>>>>>>> 0dbc77d8778a3756793d130c37a01ca0d1c65976
});

