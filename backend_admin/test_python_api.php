<?php
/**
 * Test script to check if Python API is working on port 5002
 * Run: php test_python_api.php
 */

$port = 5002;
$apiUrl = "http://localhost:{$port}";

echo "========================================\n";
echo "Testing Voice Gender Detection API\n";
echo "========================================\n";
echo "API URL: {$apiUrl}\n\n";

// Test 1: Health Check
echo "1. Testing Health Check Endpoint...\n";
echo "   GET {$apiUrl}/health\n";

$ch = curl_init("{$apiUrl}/health");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_TIMEOUT, 5);
curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 5);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curlError = curl_error($ch);
curl_close($ch);

if ($httpCode === 200) {
    echo "   ✅ Health check passed!\n";
    $data = json_decode($response, true);
    if ($data) {
        echo "   Status: " . ($data['status'] ?? 'N/A') . "\n";
        echo "   Service: " . ($data['service'] ?? 'N/A') . "\n";
        echo "   Detector Loaded: " . (isset($data['detector_loaded']) ? ($data['detector_loaded'] ? 'Yes' : 'No') : 'N/A') . "\n";
    }
    echo "   Response: {$response}\n";
} else {
    echo "   ❌ Health check failed!\n";
    if (!empty($curlError)) {
        echo "   Error: {$curlError}\n";
    }
    echo "   HTTP Code: {$httpCode}\n";
    if ($response) {
        echo "   Response: {$response}\n";
    }
    echo "\n";
    echo "========================================\n";
    echo "❌ API is NOT running on port {$port}\n";
    echo "========================================\n";
    echo "\n";
    echo "To start the API, run:\n";
    echo "  cd voice-gender-detection-test\n";
    echo "  ./START_SERVICE.sh\n";
    echo "\n";
    exit(1);
}

echo "\n";
echo "========================================\n";
echo "✅ API is working on port {$port}!\n";
echo "========================================\n";
echo "\n";
echo "To test gender detection from Laravel, use:\n";
echo "  POST {$apiUrl}/detect\n";
echo "  Form data: audio_path=/path/to/voice/file.mp3\n";
echo "\n";









