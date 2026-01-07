<?php
/**
 * Check Referral Code Uniqueness
 * Verifies that all users have unique referral codes
 */

require_once __DIR__ . '/../vendor/autoload.php';

// Load Laravel Application
$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Http\Kernel::class);
$kernel->bootstrap();

use App\Models\User;

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

try {
    // Get all users
    $totalUsers = User::count();
    
    // Get users with referral codes
    $usersWithCodes = User::whereNotNull('referral_code')->count();
    
    // Get all referral codes
    $allCodes = User::whereNotNull('referral_code')->pluck('referral_code');
    
    // Get unique codes
    $uniqueCodes = $allCodes->unique();
    
    // Check for duplicates
    $isUnique = $allCodes->count() === $uniqueCodes->count();
    
    // Find duplicates if any
    $duplicates = [];
    if (!$isUnique) {
        $duplicates = $allCodes->duplicates()->toArray();
    }
    
    // Get sample codes
    $sampleCodes = User::whereNotNull('referral_code')
                      ->select('name', 'referral_code', 'phone')
                      ->limit(5)
                      ->get()
                      ->toArray();
    
    echo json_encode([
        'success' => true,
        'total_users' => $totalUsers,
        'users_with_codes' => $usersWithCodes,
        'unique_codes' => $uniqueCodes->count(),
        'is_unique' => $isUnique,
        'duplicates' => $duplicates,
        'sample_codes' => $sampleCodes,
        'message' => $isUnique 
            ? '✅ All referral codes are unique!' 
            : '❌ Duplicate codes found!'
    ], JSON_PRETTY_PRINT);
    
} catch (\Exception $e) {
    echo json_encode([
        'success' => false,
        'error' => $e->getMessage()
    ], JSON_PRETTY_PRINT);
}

