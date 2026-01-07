<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once __DIR__ . '/../vendor/autoload.php';

$app = require_once __DIR__ . '/../bootstrap/app.php';
$app->make('Illuminate\Contracts\Console\Kernel')->bootstrap();

use Illuminate\Support\Facades\DB;

$phone = $_GET['phone'] ?? '';

if (empty($phone)) {
    echo json_encode(['success' => false, 'error' => 'Phone number required']);
    exit;
}

// Remove country code prefix if present (+91, +1, etc.)
$phone = preg_replace('/^\+\d{1,3}/', '', $phone);
$phone = trim($phone);

try {
    $user = DB::table('users')->where('phone', $phone)->first();
    
    if (!$user) {
        echo json_encode(['success' => false, 'error' => 'User not found with phone: ' . $phone]);
        exit;
    }
    
    $token = DB::table('personal_access_tokens')
        ->where('tokenable_id', $user->id)
        ->orderBy('created_at', 'desc')
        ->first();
    
    if (!$token) {
        echo json_encode(['success' => false, 'error' => 'No token found. Please login in the app first.']);
        exit;
    }
    
    echo json_encode([
        'success' => true,
        'token' => $token->token,
        'name' => $user->name,
        'username' => $user->username,
        'phone' => $user->phone,
        'user_id' => $user->id
    ]);
    
} catch (Exception $e) {
    echo json_encode(['success' => false, 'error' => 'Database error: ' . $e->getMessage()]);
}

