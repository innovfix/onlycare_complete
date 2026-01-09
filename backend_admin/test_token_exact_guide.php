<?php

require __DIR__ . '/vendor/autoload.php';

$app = require_once __DIR__ . '/bootstrap/app.php';
$app->make('Illuminate\Contracts\Console\Kernel')->bootstrap();

use Yasser\Agora\RtcTokenBuilder;

echo "==========================================================\n";
echo "  TESTING AGORA TOKEN GENERATION (EXACT AS GUIDE)\n";
echo "==========================================================\n\n";

// Get credentials from environment (EXACT as guide line 134-135)
$appID = env('AGORA_APP_ID');
$appCertificate = env('AGORA_APP_CERTIFICATE');

echo "📋 Configuration:\n";
echo "   App ID: {$appID}\n";
echo "   Certificate: " . (empty($appCertificate) ? "EMPTY ❌" : substr($appCertificate, 0, 10) . "... ✅") . "\n";
echo "\n";

// Validate credentials (EXACT as guide line 138-143)
if (empty($appID) || empty($appCertificate)) {
    echo "❌ ERROR: Agora credentials not configured\n";
    exit(1);
}

// Test parameters (EXACT as guide)
$channelName = 'test_channel';
$uid = 0;
$roleInput = 'publisher';
$expireTimeInSeconds = 3600; // 1 hour

echo "📝 Parameters:\n";
echo "   Channel Name: {$channelName}\n";
echo "   UID: {$uid}\n";
echo "   Role: {$roleInput}\n";
echo "   Expire: {$expireTimeInSeconds} seconds (1 hour)\n";
echo "\n";

// Determine role (EXACT as guide line 152-154)
$role = strtolower($roleInput) === 'subscriber' 
    ? RtcTokenBuilder::RoleSubscriber 
    : RtcTokenBuilder::RolePublisher;

echo "🔧 Role Constant: " . ($role === RtcTokenBuilder::RolePublisher ? "RolePublisher ✅" : "RoleSubscriber") . "\n";
echo "\n";

// Calculate expiration timestamp (EXACT as guide line 157-158)
$currentTimestamp = now()->getTimestamp();
$privilegeExpiredTs = $currentTimestamp + $expireTimeInSeconds;

echo "⏰ Timestamps:\n";
echo "   Current: {$currentTimestamp} (" . date('Y-m-d H:i:s', $currentTimestamp) . ")\n";
echo "   Expires: {$privilegeExpiredTs} (" . date('Y-m-d H:i:s', $privilegeExpiredTs) . ")\n";
echo "\n";

// Generate token (EXACT method from guide line 161-168)
echo "🔐 Generating token...\n";
try {
    $rtcToken = RtcTokenBuilder::buildTokenWithUid(
        $appID, 
        $appCertificate, 
        $channelName, 
        $uid, 
        $role, 
        $privilegeExpiredTs
    );
    
    // Validate token generation (EXACT as guide line 171-176)
    if (empty($rtcToken)) {
        echo "❌ ERROR: Failed to generate token - returned empty string\n";
        exit(1);
    }
    
    echo "✅ Token generated successfully!\n";
    echo "\n";
    echo "📊 Token Details:\n";
    echo "   Token: {$rtcToken}\n";
    echo "   Length: " . strlen($rtcToken) . " characters\n";
    echo "   Starts with: " . substr($rtcToken, 0, 3) . "\n";
    echo "   First 50 chars: " . substr($rtcToken, 0, 50) . "...\n";
    echo "\n";
    
    // Expected format check
    if (strlen($rtcToken) < 100) {
        echo "⚠️  WARNING: Token seems too short (expected ~130+ chars)\n";
    }
    
    if (!str_starts_with($rtcToken, '006') && !str_starts_with($rtcToken, '007')) {
        echo "⚠️  WARNING: Token doesn't start with expected prefix (006 or 007)\n";
    }
    
    echo "\n";
    echo "==========================================================\n";
    echo "  ✅ TOKEN GENERATION TEST COMPLETE\n";
    echo "==========================================================\n";
    echo "\n";
    echo "📋 Expected Response Format (as per guide):\n";
    echo json_encode([
        'success' => true,
        'token' => $rtcToken,
        'app_id' => $appID,
        'channel_name' => $channelName,
        'uid' => $uid,
        'role' => $roleInput,
        'expires_in' => $expireTimeInSeconds
    ], JSON_PRETTY_PRINT);
    echo "\n";
    
} catch (\Exception $e) {
    echo "❌ EXCEPTION: " . $e->getMessage() . "\n";
    echo "   File: " . $e->getFile() . "\n";
    echo "   Line: " . $e->getLine() . "\n";
    echo "\n";
    echo "Stack trace:\n";
    echo $e->getTraceAsString();
    exit(1);
}
