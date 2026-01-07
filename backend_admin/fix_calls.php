<?php
require __DIR__ . '/vendor/autoload.php';
$app = require_once __DIR__ . '/bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use App\Models\User;
use App\Models\Call;

echo "=== FIXING CALL ISSUES ===\n\n";

// 1. End all stuck calls
$stuckCalls = Call::whereIn('status', ['PENDING', 'INITIATED', 'RINGING', 'ONGOING'])->get();
echo "Found " . $stuckCalls->count() . " stuck calls. Ending them...\n";
foreach ($stuckCalls as $call) {
    $call->status = 'ENDED';
    $call->ended_at = now();
    $call->save();
    echo "  Ended: {$call->id}\n";
}

// 2. Reset ALL users to not busy
User::query()->update(['is_busy' => false]);
echo "\n✅ Reset is_busy=0 for ALL users\n";

// 3. Make female creators verified and online for testing
User::where('user_type', 'FEMALE')
    ->update([
        'is_verified' => true,
        'is_online' => true,
        'is_busy' => false
    ]);
echo "✅ Set all female users to: is_verified=1, is_online=1, is_busy=0\n";

// 4. Show status
echo "\nFemale users after fix:\n";
$females = User::where('user_type', 'FEMALE')->limit(5)->get(['id', 'name', 'is_busy', 'is_verified', 'is_online']);
foreach ($females as $f) {
    echo "  {$f->name}: busy={$f->is_busy}, verified={$f->is_verified}, online={$f->is_online}\n";
}

echo "\n=== DONE! Calls should work now ===\n";

