<?php

require __DIR__.'/../vendor/autoload.php';

$app = require_once __DIR__.'/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use App\Models\User;

echo "========================================\n";
echo "   MALE USERS FOR TESTING\n";
echo "========================================\n\n";

$males = User::where('user_type', 'MALE')
    ->select('id', 'name', 'phone', 'gender')
    ->take(5)
    ->get();

if ($males->isEmpty()) {
    echo "❌ No male users found in database!\n\n";
    echo "Creating a test male user...\n";
    
    $testUser = User::create([
        'id' => 'USR_TEST_MALE',
        'name' => 'Test Male User',
        'phone' => '1234567890',
        'user_type' => 'MALE',
        'gender' => 'MALE',
        'age' => 25,
        'language' => 'ENGLISH',
        'is_verified' => true,
        'coin_balance' => 100
    ]);
    
    echo "✅ Test user created!\n";
    echo "📱 Phone: 1234567890\n";
    echo "🔑 OTP: Any 6 digits (in development mode)\n\n";
} else {
    foreach ($males as $user) {
        echo "📱 Phone: " . $user->phone . "\n";
        echo "👤 Name: " . $user->name . "\n";
        echo "🆔 ID: " . $user->id . "\n";
        echo "---\n";
    }
}

echo "\n========================================\n";
echo "   HOW TO LOGIN:\n";
echo "========================================\n\n";
echo "1. Open OnlyCare app\n";
echo "2. Enter phone number (WITHOUT country code):\n";
echo "   Example: 9876543210\n";
echo "3. Enter any 6-digit OTP (development mode):\n";
echo "   Example: 123456\n";
echo "4. You'll be logged in!\n\n";

echo "🔑 NOTE: In development, ANY OTP works!\n";
echo "   Just enter 123456 or 111111\n\n";

