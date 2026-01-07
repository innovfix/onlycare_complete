<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once __DIR__ . '/../vendor/autoload.php';

$app = require_once __DIR__ . '/../bootstrap/app.php';
$kernel = $app->make('Illuminate\Contracts\Http\Kernel');

use Illuminate\Http\Request;

// Create a request for login
$phone = $_GET['phone'] ?? '';

if (empty($phone)) {
    echo json_encode(['success' => false, 'error' => 'Phone number required']);
    exit;
}

// Remove country code if present
$phone = preg_replace('/^\+\d{1,3}/', '', $phone);
$phone = trim($phone);

try {
    $user = \DB::table('users')->where('phone', $phone)->first();
    
    if (!$user) {
        echo json_encode(['success' => false, 'error' => 'User not found']);
        exit;
    }
    
    // Delete old tokens for this user
    \DB::table('personal_access_tokens')->where('tokenable_id', $user->id)->delete();
    
    // Create new token
    $userModel = \App\Models\User::find($user->id);
    $token = $userModel->createToken('test-device')->plainTextToken;
    
    echo json_encode([
        'success' => true,
        'token' => $token,
        'user' => [
            'id' => $user->id,
            'name' => $user->name,
            'phone' => $user->phone,
            'username' => $user->username
        ]
    ]);
    
} catch (\Exception $e) {
    echo json_encode(['success' => false, 'error' => $e->getMessage()]);
}

