#!/usr/bin/env php
<?php

/**
 * Test Agora Token Generation
 * This script tests the token generation without making an actual call
 */

require __DIR__.'/vendor/autoload.php';

$app = require_once __DIR__.'/bootstrap/app.php';
$app->make('Illuminate\Contracts\Console\Kernel')->bootstrap();

use App\Services\AgoraTokenBuilder;
use Illuminate\Support\Facades\Log;

echo "\n";
echo "🔐 AGORA TOKEN GENERATION TEST\n";
echo "═══════════════════════════════════════════════════\n\n";

// Get configuration
$appId = config('services.agora.app_id', env('AGORA_APP_ID'));
$appCertificate = config('services.agora.app_certificate', env('AGORA_APP_CERTIFICATE'));

echo "📋 CONFIGURATION:\n";
echo "   App ID: " . $appId . "\n";
echo "   Certificate: " . substr($appCertificate, 0, 20) . "..." . substr($appCertificate, -4) . "\n";
echo "   Certificate Length: " . strlen($appCertificate) . " chars\n\n";

// Test data
$testCallId = 'CALL_' . time() . rand(1000, 9999);
$channelName = 'call_' . $testCallId;
$uid = 0;

echo "🧪 TEST PARAMETERS:\n";
echo "   Call ID: {$testCallId}\n";
echo "   Channel Name: {$channelName}\n";
echo "   UID: {$uid}\n";
echo "   Role: PUBLISHER (1)\n\n";

// Check if credentials are configured
if (empty($appId)) {
    echo "❌ ERROR: Agora App ID not configured\n";
    echo "   Set AGORA_APP_ID in .env file\n\n";
    exit(1);
}

if (empty($appCertificate)) {
    echo "⚠️  WARNING: Agora App Certificate not configured\n";
    echo "   Token will be empty (UNSECURE mode)\n";
    echo "   Set AGORA_APP_CERTIFICATE in .env file\n\n";
    
    $token = '';
    $tokenLength = 0;
} else {
    try {
        echo "🔑 GENERATING TOKEN...\n\n";
        
        // Generate token
        $startTime = microtime(true);
        $token = AgoraTokenBuilder::buildTokenWithDefault(
            $appId,
            $appCertificate,
            $channelName,
            $uid
        );
        $endTime = microtime(true);
        $duration = round(($endTime - $startTime) * 1000, 2);
        
        $tokenLength = strlen($token);
        
        echo "✅ TOKEN GENERATED SUCCESSFULLY!\n\n";
        echo "⏱️  Generation Time: {$duration}ms\n\n";
        
    } catch (\Exception $e) {
        echo "❌ TOKEN GENERATION FAILED!\n";
        echo "   Error: " . $e->getMessage() . "\n\n";
        exit(1);
    }
}

// Display results
echo "📊 RESULTS:\n";
echo "═══════════════════════════════════════════════════\n\n";

if (!empty($token)) {
    echo "✅ MODE: SECURE (Certificate enabled)\n\n";
    
    echo "🎫 Token Details:\n";
    echo "   Length: {$tokenLength} characters\n";
    echo "   Version: " . substr($token, 0, 3) . "\n";
    echo "   App ID in Token: " . substr($token, 3, 32) . "\n";
    echo "   First 50 chars: " . substr($token, 0, 50) . "...\n";
    echo "   Last 20 chars: ..." . substr($token, -20) . "\n\n";
    
    echo "📋 Full Token (copy this for testing):\n";
    echo "─────────────────────────────────────────────────\n";
    echo $token . "\n";
    echo "─────────────────────────────────────────────────\n\n";
    
    // Validate token format
    echo "🔍 TOKEN VALIDATION:\n";
    
    $checks = [
        'Starts with 007' => substr($token, 0, 3) === '007',
        'Contains App ID' => strpos($token, $appId) !== false,
        'Length > 400 chars' => strlen($token) > 400,
        'Base64 encoded' => preg_match('/^[A-Za-z0-9+\/=]+$/', substr($token, 35))
    ];
    
    foreach ($checks as $check => $result) {
        $icon = $result ? '✅' : '❌';
        $status = $result ? 'PASS' : 'FAIL';
        echo "   {$icon} {$check}: {$status}\n";
    }
    
} else {
    echo "⚠️  MODE: UNSECURE (No certificate)\n";
    echo "   Token: (empty string)\n";
    echo "   App will use NULL token with Agora SDK\n";
}

echo "\n";
echo "📡 WHAT YOU'LL SEND TO APP:\n";
echo "═══════════════════════════════════════════════════\n";
echo json_encode([
    'call_id' => $testCallId,
    'channel_name' => $channelName,
    'agora_token' => $token,
    'app_id' => $appId
], JSON_PRETTY_PRINT);
echo "\n\n";

echo "✅ Test Complete!\n\n";

// Log the test
Log::info('🧪 Token generation test completed', [
    'test_call_id' => $testCallId,
    'token_length' => $tokenLength,
    'mode' => !empty($token) ? 'SECURE' : 'UNSECURE',
    'duration_ms' => $duration ?? 0
]);

echo "💡 TIP: Check logs with:\n";
echo "   tail -50 storage/logs/laravel.log | grep 'Token generation test'\n\n";








