<?php
// This script adds the getCall method to CallController

$file = '/var/www/onlycare_admin/app/Http/Controllers/Api/CallController.php';
$content = file_get_contents($file);

// Check if method already exists
if (strpos($content, 'function getCall') !== false) {
    echo "getCall method already exists\n";
    exit(0);
}

// Method to add
$method = '
    /**
     * Get call details by ID
     */
    public function getCall(Request $request, $callId)
    {
        $call = Call::find($callId);
        
        if (!$call) {
            return response()->json([
                \'success\' => false,
                \'error\' => [
                    \'code\' => \'CALL_NOT_FOUND\',
                    \'message\' => \'Call not found\'
                ]
            ], 404);
        }
        
        $userId = $request->user()->id;
        if ($call->caller_id !== $userId && $call->receiver_id !== $userId) {
            return response()->json([
                \'success\' => false,
                \'error\' => [
                    \'code\' => \'FORBIDDEN\',
                    \'message\' => \'Not authorized\'
                ]
            ], 403);
        }
        
        return response()->json([
            \'success\' => true,
            \'call\' => [
                \'id\' => $call->id,
                \'caller_id\' => $call->caller_id,
                \'receiver_id\' => $call->receiver_id,
                \'call_type\' => $call->call_type,
                \'status\' => $call->status,
                \'channel_name\' => \'call_\' . $call->id
            ]
        ]);
    }
';

// Insert before the last closing brace
$lastBrace = strrpos($content, '}');
$newContent = substr($content, 0, $lastBrace) . $method . "\n}\n";

file_put_contents($file, $newContent);
echo "getCall method added successfully!\n";





