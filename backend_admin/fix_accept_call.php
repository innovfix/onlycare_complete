<?php
// Fix: Add agora_app_id to acceptCall response

$file = '/var/www/onlycare_admin/app/Http/Controllers/Api/CallController.php';
$content = file_get_contents($file);

// Find the acceptCall response and add agora_app_id
$old = "return response()->json([
                'success' => true,
                'message' => 'Call accepted',
                'call' => [
                    'id' => \$call->id,
                    'status' => \$call->status,
                    'started_at' => \$call->started_at->toIso8601String(),
                    'agora_token' => \$agoraToken,
                    'channel_name' => \$channelName
                ]
            ]);";

$new = "\$agoraAppId = config('services.agora.app_id', env('AGORA_APP_ID', ''));
            
            return response()->json([
                'success' => true,
                'message' => 'Call accepted',
                'call' => [
                    'id' => \$call->id,
                    'status' => \$call->status,
                    'started_at' => \$call->started_at->toIso8601String(),
                    'agora_token' => \$agoraToken,
                    'agora_app_id' => \$agoraAppId,
                    'channel_name' => \$channelName
                ],
                'agora_token' => \$agoraToken,
                'agora_app_id' => \$agoraAppId,
                'channel_name' => \$channelName
            ]);";

if (strpos($content, "'agora_app_id' => \$agoraAppId") !== false && strpos($content, "Call accepted") !== false) {
    echo "Already fixed!\n";
} else {
    $content = str_replace($old, $new, $content);
    file_put_contents($file, $content);
    echo "Fixed acceptCall - added agora_app_id!\n";
}

// Verify
$verify = file_get_contents($file);
if (strpos($verify, "'agora_app_id' => \$agoraAppId") !== false) {
    echo "Verified: agora_app_id is in acceptCall response!\n";
} else {
    echo "Warning: Fix may not have been applied correctly.\n";
}





