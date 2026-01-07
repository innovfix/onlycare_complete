#!/usr/bin/env php
<?php

/**
 * FCM Test Script for Only Care App
 * 
 * This script tests Firebase Cloud Messaging integration
 * to ensure incoming call notifications work properly.
 * 
 * Usage:
 *   php test-fcm.php
 * 
 * Or make it executable:
 *   chmod +x test-fcm.php
 *   ./test-fcm.php
 */

// Load Laravel
require __DIR__ . '/vendor/autoload.php';
$app = require_once __DIR__ . '/bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use App\Models\User;
use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;

echo "\n";
echo "╔══════════════════════════════════════════════════════════════╗\n";
echo "║           FCM INTEGRATION TEST - ONLY CARE APP               ║\n";
echo "╚══════════════════════════════════════════════════════════════╝\n";
echo "\n";

// ============================================
// TEST 1: Check Firebase Credentials
// ============================================
echo "TEST 1: Checking Firebase Credentials...\n";
echo "─────────────────────────────────────────────────────\n";

$credentialsPath = config('firebase.credentials');
echo "📁 Credentials path: {$credentialsPath}\n";

if (!file_exists($credentialsPath)) {
    echo "❌ FAIL: Firebase credentials file not found!\n";
    echo "   Expected location: {$credentialsPath}\n";
    echo "   Please download from Firebase Console → Settings → Service Accounts\n";
    exit(1);
}

echo "✅ Credentials file exists\n";

try {
    $credentials = json_decode(file_get_contents($credentialsPath), true);
    echo "✅ Credentials file is valid JSON\n";
    echo "   Project ID: {$credentials['project_id']}\n";
    echo "   Client Email: {$credentials['client_email']}\n";
} catch (Exception $e) {
    echo "❌ FAIL: Invalid credentials JSON: {$e->getMessage()}\n";
    exit(1);
}

echo "\n";

// ============================================
// TEST 2: Initialize Firebase
// ============================================
echo "TEST 2: Initializing Firebase...\n";
echo "─────────────────────────────────────────────────────\n";

try {
    $firebase = (new Factory)->withServiceAccount($credentialsPath);
    $messaging = $firebase->createMessaging();
    echo "✅ Firebase SDK initialized successfully\n";
} catch (Exception $e) {
    echo "❌ FAIL: Firebase initialization failed\n";
    echo "   Error: {$e->getMessage()}\n";
    echo "   Check if credentials are correct and not expired\n";
    exit(1);
}

echo "\n";

// ============================================
// TEST 3: Check Database for FCM Tokens
// ============================================
echo "TEST 3: Checking Database for FCM Tokens...\n";
echo "─────────────────────────────────────────────────────\n";

$totalUsers = User::count();
$usersWithTokens = User::whereNotNull('fcm_token')->count();

echo "👥 Total users: {$totalUsers}\n";
echo "📱 Users with FCM tokens: {$usersWithTokens}\n";

if ($usersWithTokens === 0) {
    echo "⚠️  WARNING: No users have FCM tokens saved!\n";
    echo "   This means:\n";
    echo "   1. Mobile app might not be sending tokens to backend\n";
    echo "   2. Or users haven't opened the app yet\n";
    echo "\n";
    echo "   Expected: Mobile app should call POST /api/v1/users/update-fcm-token\n";
    echo "   Skipping notification test...\n";
    exit(0);
}

echo "✅ FCM tokens found in database\n";

// Show recent users with tokens
$recentUsers = User::whereNotNull('fcm_token')
    ->orderBy('updated_at', 'desc')
    ->limit(5)
    ->get(['id', 'name', 'phone', 'fcm_token', 'updated_at']);

echo "\n📋 Recent users with FCM tokens:\n";
foreach ($recentUsers as $user) {
    $tokenPreview = substr($user->fcm_token, 0, 30) . '...';
    $updatedAt = $user->updated_at->format('Y-m-d H:i:s');
    echo "   • {$user->name} ({$user->phone})\n";
    echo "     Token: {$tokenPreview}\n";
    echo "     Updated: {$updatedAt}\n";
}

echo "\n";

// ============================================
// TEST 4: Send Test Notification
// ============================================
echo "TEST 4: Sending Test FCM Notification...\n";
echo "─────────────────────────────────────────────────────\n";

// Interactive selection
echo "\n";
echo "Select a user to send test notification:\n";
echo "─────────────────────────────────────────────────────\n";

$testUsers = User::whereNotNull('fcm_token')
    ->orderBy('updated_at', 'desc')
    ->limit(10)
    ->get(['id', 'name', 'phone', 'fcm_token']);

if ($testUsers->isEmpty()) {
    echo "❌ No users available for testing\n";
    exit(1);
}

foreach ($testUsers as $index => $user) {
    echo ($index + 1) . ". {$user->name} ({$user->phone})\n";
}

echo "\nEnter user number (1-{$testUsers->count()}) or press Enter to skip: ";
$choice = trim(fgets(STDIN));

if (empty($choice)) {
    echo "⏭️  Skipped notification test\n";
    exit(0);
}

$selectedIndex = (int)$choice - 1;
if ($selectedIndex < 0 || $selectedIndex >= $testUsers->count()) {
    echo "❌ Invalid selection\n";
    exit(1);
}

$testUser = $testUsers[$selectedIndex];
echo "\n✅ Selected: {$testUser->name}\n";
echo "📱 FCM Token: " . substr($testUser->fcm_token, 0, 50) . "...\n";

// ============================================
// Send Test Notification
// ============================================
echo "\n📤 Sending test incoming call notification...\n";

$testData = [
    'type' => 'incoming_call',
    'callerId' => 'TEST_CALLER_001',
    'callerName' => 'Test Caller (Backend)',
    'callerPhoto' => '',
    'channelId' => 'test_call_' . time(),
    'agoraToken' => '',
    'agoraAppId' => config('services.agora.app_id', ''),
    'callId' => 'TEST_' . time(),
    'callType' => 'AUDIO',
];

try {
    $message = CloudMessage::withTarget('token', $testUser->fcm_token)
        ->withData($testData)
        ->withAndroidConfig([
            'priority' => 'high',
            'notification' => [
                'channel_id' => 'incoming_calls',
                'sound' => 'default',
            ],
        ]);

    $result = $messaging->send($message);

    echo "✅ FCM notification sent successfully!\n";
    echo "   Message ID: {$result}\n";
    echo "\n";
    echo "📱 Check the mobile device:\n";
    echo "   • You should see a full-screen incoming call\n";
    echo "   • From: Test Caller (Backend)\n";
    echo "   • Even if the app is closed!\n";
    echo "\n";
    
} catch (\Kreait\Firebase\Exception\Messaging\InvalidMessage $e) {
    echo "❌ FAIL: Invalid message format\n";
    echo "   Error: {$e->getMessage()}\n";
    exit(1);
    
} catch (\Kreait\Firebase\Exception\Messaging\NotFound $e) {
    echo "❌ FAIL: FCM token not found or invalid\n";
    echo "   This usually means:\n";
    echo "   1. Token has expired (user reinstalled app)\n";
    echo "   2. Token is from wrong Firebase project\n";
    echo "   3. User unregistered from FCM\n";
    echo "\n";
    echo "   Solution: Mobile app should refresh FCM token\n";
    exit(1);
    
} catch (\Kreait\Firebase\Exception\MessagingException $e) {
    echo "❌ FAIL: FCM messaging error\n";
    echo "   Error: {$e->getMessage()}\n";
    exit(1);
    
} catch (Exception $e) {
    echo "❌ FAIL: Unexpected error\n";
    echo "   Error: {$e->getMessage()}\n";
    exit(1);
}

// ============================================
// FINAL SUMMARY
// ============================================
echo "╔══════════════════════════════════════════════════════════════╗\n";
echo "║                       TEST SUMMARY                           ║\n";
echo "╚══════════════════════════════════════════════════════════════╝\n";
echo "\n";
echo "✅ Firebase credentials: OK\n";
echo "✅ Firebase SDK: OK\n";
echo "✅ Database FCM tokens: OK ({$usersWithTokens} users)\n";
echo "✅ FCM notification sent: OK\n";
echo "\n";

echo "🎯 NEXT STEPS:\n";
echo "─────────────────────────────────────────────────────\n";
echo "1. Check if notification appeared on the test device\n";
echo "2. If notification appeared:\n";
echo "   ✅ Backend is working correctly!\n";
echo "   → Check mobile app's FCM background handler\n";
echo "\n";
echo "3. If notification did NOT appear:\n";
echo "   • Check mobile app's notification permissions\n";
echo "   • Check Firebase project ID matches in mobile app\n";
echo "   • Check Android notification channel is created\n";
echo "   • Check battery optimization is disabled\n";
echo "\n";

echo "📖 For more details, see: FCM_STATUS_AND_DIAGNOSTICS.md\n";
echo "\n";

exit(0);







