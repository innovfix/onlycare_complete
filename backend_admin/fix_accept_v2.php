<?php
$file = '/var/www/onlycare_admin/app/Http/Controllers/Api/CallController.php';
$content = file_get_contents($file);

$old = "\$agoraToken = \$this->generateAgoraToken(\$call->id);
            \$channelName = 'call_' . \$call->id;

            return response()->json([
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

$new = "\$agoraToken = \$this->generateAgoraToken(\$call->id);
            \$channelName = 'call_' . \$call->id;
            \$agoraAppId = config('services.agora.app_id', env('AGORA_APP_ID', ''));

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

$content = str_replace($old, $new, $content);
file_put_contents($file, $content);
echo "Fixed!\n";

// Verify
exec("grep -c 'agora_app_id' " . $file, $output);
echo "agora_app_id occurrences: " . $output[0] . "\n";





