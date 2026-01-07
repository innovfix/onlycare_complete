<?php
// Fix: Add is_busy reset to rejectCall function

$file = '/var/www/onlycare_admin/app/Http/Controllers/Api/CallController.php';
$content = file_get_contents($file);

// Find and fix rejectCall - add is_busy reset
$old = "\$call->update([
            'status' => 'REJECTED',
            'ended_at' => now()
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Call rejected'
        ]);";

$new = "// Reset busy status for both users when call is rejected
        User::whereIn('id', [\$call->caller_id, \$call->receiver_id])
            ->update(['is_busy' => false]);

        \$call->update([
            'status' => 'REJECTED',
            'ended_at' => now()
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Call rejected'
        ]);";

if (strpos($content, "Reset busy status for both users when call is rejected") !== false) {
    echo "Already fixed!\n";
} else {
    $content = str_replace($old, $new, $content);
    file_put_contents($file, $content);
    echo "Fixed rejectCall!\n";
}

// Also fix cancelCall if it exists - search for 'CANCELLED' status
if (strpos($content, "'status' => 'CANCELLED'") !== false) {
    // Find the cancelCall function and add is_busy reset if not there
    $old2 = "\$call->update([
            'status' => 'CANCELLED',
            'ended_at' => now()
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Call cancelled'
        ]);";

    $new2 = "// Reset busy status for both users when call is cancelled
        User::whereIn('id', [\$call->caller_id, \$call->receiver_id])
            ->update(['is_busy' => false]);

        \$call->update([
            'status' => 'CANCELLED',
            'ended_at' => now()
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Call cancelled'
        ]);";

    if (strpos($content, "Reset busy status for both users when call is cancelled") !== false) {
        echo "cancelCall already fixed!\n";
    } else {
        $content = file_get_contents($file); // Re-read after first fix
        $content = str_replace($old2, $new2, $content);
        file_put_contents($file, $content);
        echo "Fixed cancelCall!\n";
    }
}

// Verify
$verify = file_get_contents($file);
$count = substr_count($verify, "is_busy => false");
echo "Total is_busy resets in file: $count\n";





