<?php
require __DIR__ . '/vendor/autoload.php';
$app = require_once __DIR__ . '/bootstrap/app.php';
$kernel = $app->make(Illuminate\Contracts\Console\Kernel::class);
$kernel->bootstrap();

use App\Models\User;

// Reset ALL users to not busy
User::query()->update(['is_busy' => false]);
echo "✅ Reset is_busy=0 for all users\n\n";

// Show female users
echo "Female users status:\n";
$females = User::where('user_type', 'FEMALE')->limit(5)->get(['id', 'name', 'is_busy', 'is_verified', 'is_online', 'audio_call_enabled', 'video_call_enabled']);
foreach ($females as $f) {
    echo "  {$f->id}: {$f->name}\n";
    echo "    is_busy={$f->is_busy}, is_verified={$f->is_verified}, is_online={$f->is_online}\n";
    echo "    audio={$f->audio_call_enabled}, video={$f->video_call_enabled}\n";
}

// Check pending calls
echo "\nPending/Active calls:\n";
$calls = \App\Models\Call::whereIn('status', ['PENDING', 'INITIATED', 'RINGING', 'ONGOING'])
    ->orderBy('created_at', 'desc')
    ->limit(5)
    ->get(['id', 'status', 'caller_id', 'receiver_id', 'created_at']);

if ($calls->isEmpty()) {
    echo "  No pending/active calls\n";
} else {
    foreach ($calls as $c) {
        echo "  {$c->id}: {$c->status} - caller:{$c->caller_id} -> receiver:{$c->receiver_id}\n";
    }
}





