<?php

require __DIR__ . '/vendor/autoload.php';

$app = require_once __DIR__ . '/bootstrap/app.php';
$app->make('Illuminate\Contracts\Console\Kernel')->bootstrap();

use App\Models\User;
use App\Models\Call;

echo "=== Deleting Call History for user_2222 ===\n\n";

// Find user by ID or username pattern
$user = User::where('id', 'USR_2222')
    ->orWhere('id', '2222')
    ->orWhere('username', 'LIKE', '%2222%')
    ->orWhere('name', 'LIKE', '%2222%')
    ->first();

if (!$user) {
    echo "❌ User 2222 not found\n";

    // Try to find calls directly by user ID pattern
    echo "\nSearching for calls with user_id containing '2222'...\n";
    $callsByCallerId = Call::where('caller_id', 'LIKE', '%2222%')->count();
    $callsByReceiverId = Call::where('receiver_id', 'LIKE', '%2222%')->count();

    echo "Calls as caller: {$callsByCallerId}\n";
    echo "Calls as receiver: {$callsByReceiverId}\n";

    if ($callsByCallerId > 0 || $callsByReceiverId > 0) {
        echo "\nDeleting calls...\n";

        $deleted1 = Call::where('caller_id', 'LIKE', '%2222%')->delete();
        $deleted2 = Call::where('receiver_id', 'LIKE', '%2222%')->delete();

        echo "✅ Deleted {$deleted1} calls as caller\n";
        echo "✅ Deleted {$deleted2} calls as receiver\n";
    }

    exit;
}

echo "✓ Found user:\n";
echo "  - ID: {$user->id}\n";
echo "  - Name: {$user->name}\n";
echo "  - Username: " . ($user->username ?? 'N/A') . "\n";
echo "  - Phone: " . ($user->phone ?? 'N/A') . "\n";
echo "\n";

// Find all calls where this user is caller or receiver
$calls = Call::where(function($q) use ($user) {
    $q->where('caller_id', $user->id)
      ->orWhere('receiver_id', $user->id);
})->get();

$callCount = $calls->count();
echo "Found {$callCount} call(s)\n\n";

if ($callCount > 0) {
    // Show details of calls to be deleted
    foreach ($calls as $call) {
        $callerName = User::find($call->caller_id)->name ?? 'Unknown';
        $receiverName = User::find($call->receiver_id)->name ?? 'Unknown';
        echo "- Call ID: {$call->id}\n";
        echo "  Type: {$call->call_type}\n";
        echo "  Caller: {$callerName} ({$call->caller_id})\n";
        echo "  Receiver: {$receiverName} ({$call->receiver_id})\n";
        echo "  Status: {$call->status}\n";
        echo "  Duration: " . ($call->duration ?? 0) . " seconds\n";
        echo "  Date: {$call->created_at}\n";
        echo "  ---\n";
    }

    echo "\nDeleting all calls...\n";

    // Delete all calls
    Call::where(function($q) use ($user) {
        $q->where('caller_id', $user->id)
          ->orWhere('receiver_id', $user->id);
    })->delete();

    echo "✅ Deleted {$callCount} call(s)\n";
} else {
    echo "ℹ️  No calls to delete\n";
}

echo "\n=== Verification ===\n";
$remainingCalls = Call::where(function($q) use ($user) {
    $q->where('caller_id', $user->id)
      ->orWhere('receiver_id', $user->id);
})->count();

echo "User {$user->name} ({$user->id}): {$remainingCalls} calls remaining\n";

echo "\n✅ Process completed!\n";
