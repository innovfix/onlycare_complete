<?php
// Fix: Add agora_app_id to initiateCall response

$file = '/var/www/onlycare_admin/app/Http/Controllers/Api/CallController.php';
$content = file_get_contents($file);

// Find the response section and add agora_app_id
$old = "'agora_token' => \$agoraToken,
            'channel_name' => \$channelName,
            'balance_time' => \$balanceTime
        ]);
    }";

$new = "'agora_token' => \$agoraToken,
            'agora_app_id' => \$agoraAppId,
            'channel_name' => \$channelName,
            'balance_time' => \$balanceTime
        ]);
    }";

if (strpos($content, "'agora_app_id' => \$agoraAppId") !== false) {
    echo "Already fixed!\n";
} else {
    $content = str_replace($old, $new, $content);
    file_put_contents($file, $content);
    echo "Fixed! Added agora_app_id to response.\n";
}

// Verify the fix
$verify = file_get_contents($file);
if (strpos($verify, "'agora_app_id' => \$agoraAppId") !== false) {
    echo "Verified: agora_app_id is now in the response!\n";
} else {
    echo "Warning: Fix may not have been applied correctly.\n";
}





