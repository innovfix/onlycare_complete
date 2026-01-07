#!/usr/bin/env php
<?php

/**
 * Send Test FCM to Specific User
 * 
 * Sends a test incoming call notification to User_1111
 * 
 * Usage:
 *   php send-test-fcm-to-user.php
 */

require __DIR__ . '/vendor/autoload.php';
$app = require_once __DIR__ . '/bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use App\Models\User;
use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;

echo "\n";
echo "╔══════════════════════════════════════════════════════════════╗\n";
echo "║       TEST FCM TO USER_1111 (Phone: 9668555511)             ║\n";
echo "╚══════════════════════════════════════════════════════════════╝\n";
echo "\n";

// Get user
$user = User::where('phone', '9668555511')->first();

if (!$user) {
    echo "❌ User not found!\n";
    exit(1);
}

echo "✅ User found:\n";
echo "   ID: {$user->id}\n";
echo "   Name: {$user->name}\n";
echo "   Phone: {$user->phone}\n";
echo "   FCM Token: " . substr($user->fcm_token, 0, 50) . "...\n";
echo "\n";

if (!$user->fcm_token) {
    echo "❌ User has no FCM token!\n";
    exit(1);
}

// Initialize Firebase
try {
    $firebase = (new Factory)->withServiceAccount(config('firebase.credentials'));
    $messaging = $firebase->createMessaging();
    echo "✅ Firebase initialized\n";
} catch (Exception $e) {
    echo "❌ Firebase error: {$e->getMessage()}\n";
    exit(1);
}

// Prepare test notification
$testData = [
    'type' => 'incoming_call',
    'callerId' => 'TEST_BACKEND_001',
    'callerName' => 'Backend Test System',
    'callerPhoto' => '',
    'channelId' => 'test_call_' . time(),
    'agoraToken' => '',
    'agoraAppId' => config('services.agora.app_id', '63783c2ad2724b839b1e58714bfc2629'),
    'callId' => 'TEST_' . time(),
    'callType' => 'AUDIO',
];

echo "\n📤 Sending test incoming call notification...\n";
echo "─────────────────────────────────────────────────────\n";
echo "Payload:\n";
echo json_encode($testData, JSON_PRETTY_PRINT) . "\n";
echo "─────────────────────────────────────────────────────\n";

try {
    $message = CloudMessage::withTarget('token', $user->fcm_token)
        ->withData($testData)
        ->withAndroidConfig([
            'priority' => 'high',
            'notification' => [
                'channel_id' => 'incoming_calls',
                'sound' => 'default',
            ],
        ]);

    $result = $messaging->send($message);

    echo "✅ FCM NOTIFICATION SENT SUCCESSFULLY!\n";
    echo "   Message ID: " . print_r($result, true) . "\n";
    echo "\n";
    echo "╔══════════════════════════════════════════════════════════════╗\n";
    echo "║                    CHECK YOUR PHONE!                         ║\n";
    echo "╚══════════════════════════════════════════════════════════════╝\n";
    echo "\n";
    echo "📱 Expected behavior:\n";
    echo "   • Full-screen incoming call UI appears\n";
    echo "   • Caller name: \"Backend Test System\"\n";
    echo "   • Phone rings/vibrates\n";
    echo "   • Works EVEN IF APP IS COMPLETELY CLOSED!\n";
    echo "\n";
    echo "If notification appeared:\n";
    echo "   ✅ Backend FCM is working perfectly!\n";
    echo "   → Issue must be in mobile app's call initiation or real-time sync\n";
    echo "\n";
    echo "If notification did NOT appear:\n";
    echo "   → Check mobile app's background FCM handler\n";
    echo "   → Check notification channel is created ('incoming_calls')\n";
    echo "   → Check notification permissions\n";
    echo "   → Check google-services.json matches Firebase project\n";
    echo "\n";
    
} catch (\Kreait\Firebase\Exception\Messaging\NotFound $e) {
    echo "❌ FAIL: FCM token not found or invalid\n";
    echo "   Error: {$e->getMessage()}\n";
    echo "\n";
    echo "This usually means:\n";
    echo "   • FCM token has expired (user reinstalled app)\n";
    echo "   • FCM token is from wrong Firebase project\n";
    echo "   • User unregistered from FCM\n";
    echo "\n";
    echo "Solution:\n";
    echo "   • Mobile app should refresh FCM token\n";
    echo "   • Ensure google-services.json matches backend Firebase project\n";
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

echo "Test completed!\n";
echo "\n";

exit(0);

