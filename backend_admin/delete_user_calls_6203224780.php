<?php

require __DIR__ . '/vendor/autoload.php';

$app = require_once __DIR__ . '/bootstrap/app.php';
$app->make('Illuminate\Contracts\Console\Kernel')->bootstrap();

use App\Models\User;
use App\Models\Call;
use App\Models\Transaction;

echo "==========================================================\n";
echo "  DELETE ALL CALLS FOR USER WITH PHONE: 6203224780\n";
echo "==========================================================\n\n";

$phoneNumber = '6203224780';

// Find user by phone number
echo "🔍 Searching for user with phone: {$phoneNumber}...\n";
$user = User::where('phone', $phoneNumber)
    ->orWhere('phone', 'LIKE', '%' . $phoneNumber . '%')
    ->first();

if (!$user) {
    echo "❌ User with phone number {$phoneNumber} not found!\n";
    echo "\nTrying alternative searches...\n";
    
    // Try without country code
    $shortPhone = ltrim($phoneNumber, '+91');
    $user = User::where('phone', 'LIKE', '%' . $shortPhone . '%')->first();
    
    if (!$user) {
        echo "❌ Still not found. User does not exist in database.\n";
        exit(1);
    }
}

echo "✅ Found user:\n";
echo "   ID:       {$user->id}\n";
echo "   Name:     {$user->name}\n";
echo "   Username: " . ($user->username ?? 'N/A') . "\n";
echo "   Phone:    " . ($user->phone ?? 'N/A') . "\n";
echo "   Email:    " . ($user->email ?? 'N/A') . "\n";
echo "   Gender:   " . ($user->gender ?? 'N/A') . "\n";
echo "\n";

// Count calls where this user is caller or receiver
$callsAsCaller = Call::where('caller_id', $user->id)->get();
$callsAsReceiver = Call::where('receiver_id', $user->id)->get();

$totalCalls = $callsAsCaller->count() + $callsAsReceiver->count();

echo "📊 CALL STATISTICS:\n";
echo "   Calls as CALLER:   " . $callsAsCaller->count() . "\n";
echo "   Calls as RECEIVER: " . $callsAsReceiver->count() . "\n";
echo "   TOTAL CALLS:       {$totalCalls}\n";
echo "\n";

if ($totalCalls === 0) {
    echo "ℹ️  No calls found for this user. Nothing to delete.\n";
    exit(0);
}

// Show detailed call information
echo "📋 CALL DETAILS:\n";
echo str_repeat("-", 60) . "\n";

$callNumber = 1;
foreach ($callsAsCaller as $call) {
    $receiver = User::find($call->receiver_id);
    $receiverName = $receiver ? $receiver->name : 'Unknown';
    
    echo "{$callNumber}. Call ID: {$call->id}\n";
    echo "   Type:     {$call->call_type}\n";
    echo "   Role:     CALLER\n";
    echo "   With:     {$receiverName} ({$call->receiver_id})\n";
    echo "   Status:   {$call->status}\n";
    echo "   Duration: " . ($call->duration ?? 0) . " seconds\n";
    echo "   Date:     {$call->created_at}\n";
    echo "   " . str_repeat("-", 58) . "\n";
    $callNumber++;
}

foreach ($callsAsReceiver as $call) {
    $caller = User::find($call->caller_id);
    $callerName = $caller ? $caller->name : 'Unknown';
    
    echo "{$callNumber}. Call ID: {$call->id}\n";
    echo "   Type:     {$call->call_type}\n";
    echo "   Role:     RECEIVER\n";
    echo "   With:     {$callerName} ({$call->caller_id})\n";
    echo "   Status:   {$call->status}\n";
    echo "   Duration: " . ($call->duration ?? 0) . " seconds\n";
    echo "   Date:     {$call->created_at}\n";
    echo "   " . str_repeat("-", 58) . "\n";
    $callNumber++;
}

echo "\n⚠️  WARNING: This will DELETE ALL {$totalCalls} calls for this user!\n";
echo "⚠️  This action CANNOT be undone!\n\n";

// Prompt for confirmation
echo "Are you sure you want to delete all calls? (yes/no): ";
$handle = fopen("php://stdin", "r");
$line = fgets($handle);
$confirmation = trim($line);
fclose($handle);

if (strtolower($confirmation) !== 'yes') {
    echo "\n❌ Operation cancelled by user.\n";
    exit(0);
}

echo "\n🗑️  Starting deletion process...\n\n";

try {
    // Start transaction
    \DB::beginTransaction();
    
    // 1. Delete call-related transactions
    echo "1️⃣  Deleting call-related transactions...\n";
    $transactionsDeleted = Transaction::where('reference_type', 'CALL')
        ->where(function($q) use ($user) {
            $q->where('user_id', $user->id)
              ->orWhereIn('reference_id', function($subQuery) use ($user) {
                  $subQuery->select('id')
                      ->from('calls')
                      ->where('caller_id', $user->id)
                      ->orWhere('receiver_id', $user->id);
              });
        })
        ->delete();
    echo "   ✅ Deleted {$transactionsDeleted} transaction(s)\n\n";
    
    // 2. Delete calls as caller
    echo "2️⃣  Deleting calls where user was CALLER...\n";
    $callerCallsDeleted = Call::where('caller_id', $user->id)->delete();
    echo "   ✅ Deleted {$callerCallsDeleted} call(s)\n\n";
    
    // 3. Delete calls as receiver
    echo "3️⃣  Deleting calls where user was RECEIVER...\n";
    $receiverCallsDeleted = Call::where('receiver_id', $user->id)->delete();
    echo "   ✅ Deleted {$receiverCallsDeleted} call(s)\n\n";
    
    // 4. Reset user busy status (if applicable)
    if ($user->is_busy) {
        echo "4️⃣  Resetting user busy status...\n";
        $user->is_busy = false;
        $user->save();
        echo "   ✅ User busy status reset\n\n";
    }
    
    // Commit transaction
    \DB::commit();
    
    // Final verification
    echo "==========================================================\n";
    echo "  ✅ DELETION COMPLETED SUCCESSFULLY!\n";
    echo "==========================================================\n\n";
    
    echo "📊 SUMMARY:\n";
    echo "   Transactions deleted: {$transactionsDeleted}\n";
    echo "   Calls deleted (as caller): {$callerCallsDeleted}\n";
    echo "   Calls deleted (as receiver): {$receiverCallsDeleted}\n";
    echo "   TOTAL CALLS DELETED: " . ($callerCallsDeleted + $receiverCallsDeleted) . "\n";
    echo "\n";
    
    // Verify deletion
    $remainingCalls = Call::where('caller_id', $user->id)
        ->orWhere('receiver_id', $user->id)
        ->count();
    
    echo "🔍 VERIFICATION:\n";
    echo "   Remaining calls for this user: {$remainingCalls}\n";
    
    if ($remainingCalls === 0) {
        echo "   ✅ All calls successfully deleted!\n";
    } else {
        echo "   ⚠️  WARNING: Some calls still remain!\n";
    }
    
    echo "\n==========================================================\n";
    echo "  Process completed at " . date('Y-m-d H:i:s') . "\n";
    echo "==========================================================\n";
    
} catch (\Exception $e) {
    \DB::rollBack();
    echo "\n❌ ERROR OCCURRED DURING DELETION:\n";
    echo "   {$e->getMessage()}\n";
    echo "   File: {$e->getFile()}\n";
    echo "   Line: {$e->getLine()}\n";
    echo "\n⚠️  All changes have been rolled back.\n";
    exit(1);
}
