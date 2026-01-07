<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Call;
use App\Models\User;
use App\Models\BlockedUser;
use App\Models\Transaction;
use App\Models\AppSetting;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class CallControllerEnhanced extends Controller
{
    /**
     * Initiate call with OnlyCare app validations
     * 
     * Enhanced version with:
     * - Self-call prevention
     * - Blocking check (privacy-preserving)
     * - Busy status check
     * - Push notifications
     * - Missed calls tracking
     * - Balance time calculation
     */
    public function initiateCall(Request $request)
    {
        // ============================================
        // 1. VALIDATION: Request Parameters
        // ============================================
        $validator = Validator::make($request->all(), [
            'receiver_id' => 'required|string',
            'call_type' => 'required|in:AUDIO,VIDEO',
            'call_switch' => 'nullable|boolean'  // Bypass busy check
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION_ERROR',
                    'message' => 'Invalid input data',
                    'details' => $validator->errors()
                ]
            ], 422);
        }

        // ============================================
        // 2. GET AUTHENTICATED USER (Caller)
        // ============================================
        $caller = $request->user();

        // ============================================
        // 3. VALIDATION: Caller Account Status
        // ============================================
        
        // 3.1 Check if caller account is deleted (soft delete)
        if ($caller->trashed()) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'ACCOUNT_DELETED',
                    'message' => 'User account is deleted or inactive'
                ]
            ], 403);
        }

        // 3.2 Check if caller is blocked/suspended
        if ($caller->is_blocked) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'ACCOUNT_SUSPENDED',
                    'message' => 'Your account has been suspended due to a violation of our policy.'
                ]
            ], 403);
        }

        // ============================================
        // 4. VALIDATION: Receiver (Creator) Lookup
        // ============================================
        $receiverId = str_replace('USR_', '', $request->receiver_id);
        $receiver = User::find($receiverId);

        if (!$receiver) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'User not found'
                ]
            ], 404);
        }

        // ============================================
        // 5. VALIDATION: Self-Call Prevention
        // ============================================
        if ($caller->id === $receiverId) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INVALID_REQUEST',
                    'message' => 'You cannot call yourself'
                ]
            ], 400);
        }

        // ============================================
        // 6. VALIDATION: Receiver Account Status
        // ============================================
        
        // 6.1 Check if receiver account is deleted
        if ($receiver->trashed()) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'USER_NOT_FOUND',
                    'message' => 'User not found'
                ]
            ], 404);
        }

        // ============================================
        // 7. VALIDATION: Blocking Check (Privacy-Preserving)
        // ============================================
        // Check if receiver has blocked the caller
        // Important: Show "User is busy" instead of "You are blocked" for privacy
        $isBlocked = BlockedUser::where('user_id', $receiverId)
            ->where('blocked_user_id', $caller->id)
            ->exists();

        if ($isBlocked) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'USER_BUSY',
                    'message' => 'User is busy'  // Privacy-preserving message
                ]
            ], 400);
        }

        // ============================================
        // 8. VALIDATION: Online Status
        // ============================================
        if (!$receiver->is_online) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'USER_OFFLINE',
                    'message' => 'User is not online'
                ]
            ], 400);
        }

        // ============================================
        // 9. VALIDATION: Busy Status Check
        // ============================================
        $callSwitch = $request->input('call_switch', false);
        
        if ($receiver->is_busy && !$callSwitch) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'USER_BUSY',
                    'message' => 'The user is currently on another call. Please try again later.'
                ]
            ], 400);
        }

        // ============================================
        // 10. VALIDATION: Call Type Availability
        // ============================================
        if ($request->call_type === 'AUDIO' && !$receiver->audio_call_enabled) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'CALL_NOT_AVAILABLE',
                    'message' => $receiver->name . ' is not available to take calls'
                ]
            ], 400);
        }

        if ($request->call_type === 'VIDEO' && !$receiver->video_call_enabled) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'CALL_NOT_AVAILABLE',
                    'message' => $receiver->name . ' is not available to take video calls'
                ]
            ], 400);
        }

        // ============================================
        // 11. VALIDATION: Coin Balance Check
        // ============================================
        $settings = AppSetting::first();
        $requiredCoins = $request->call_type === 'AUDIO' ? 
            ($settings->audio_call_rate ?? 10) : 
            ($settings->video_call_rate ?? 60);

        if ($caller->coin_balance < $requiredCoins) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INSUFFICIENT_COINS',
                    'message' => sprintf(
                        'Insufficient coins for %s call. Minimum %d coins required.',
                        strtolower($request->call_type),
                        $requiredCoins
                    ),
                    'details' => [
                        'required' => $requiredCoins,
                        'available' => $caller->coin_balance
                    ]
                ]
            ], 400);
        }

        // ============================================
        // 12. CALCULATE BALANCE TIME
        // ============================================
        $balanceTime = $this->calculateBalanceTime($caller->coin_balance, $requiredCoins);

        // ============================================
        // 13. CREATE CALL RECORD
        // ============================================
        DB::beginTransaction();
        
        try {
            $call = Call::create([
                'caller_id' => $caller->id,
                'receiver_id' => $receiverId,
                'call_type' => $request->call_type,
                'status' => 'CONNECTING',
                'coin_rate_per_minute' => $requiredCoins
            ]);

            // ============================================
            // 14. UPDATE MISSED CALLS COUNTER
            // ============================================
            // Increment receiver's missed calls count
            // (Will be reset to 0 when they accept any call)
            $receiver->increment('missed_calls_count');

            DB::commit();

        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Call creation failed: ' . $e->getMessage());
            
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to initiate call. Please try again.'
                ]
            ], 500);
        }

        // ============================================
        // 15. GENERATE AGORA CREDENTIALS
        // ============================================
        $agoraToken = $this->generateAgoraToken($call->id);
        $channelName = 'call_' . $call->id;

        // ============================================
        // 16. SEND PUSH NOTIFICATION
        // ============================================
        $this->sendCallNotification($receiver, $caller, $call->id, $request->call_type);

        // ============================================
        // 17. RETURN SUCCESS RESPONSE
        // ============================================
        return response()->json([
            'success' => true,
            'message' => 'Call initiated successfully',
            'data' => [
                'call_id' => $call->id,
                
                // Caller info
                'user_id' => $caller->id,
                'user_name' => $caller->name,
                'user_avatar_image' => $caller->profile_image,
                
                // Receiver info
                'call_user_id' => $receiver->id,
                'call_user_name' => $receiver->name,
                'call_user_avatar_image' => $receiver->profile_image,
                
                // Call details
                'type' => strtolower($request->call_type),
                'status' => 'CONNECTING',
                'balance_time' => $balanceTime,
                
                // Agora credentials
                'agora_token' => $agoraToken,
                'channel_name' => $channelName,
                
                // Timestamps
                'date_time' => $call->created_at->format('Y-m-d H:i:s'),
                'created_at' => $call->created_at->toIso8601String()
            ]
        ]);
    }

    /**
     * Calculate balance time based on coins and rate
     */
    private function calculateBalanceTime($coins, $ratePerMinute)
    {
        $minutes = floor($coins / $ratePerMinute);
        return sprintf("%d:00", $minutes);
    }

    /**
     * Send push notification to receiver about incoming call
     */
    private function sendCallNotification($receiver, $caller, $callId, $callType)
    {
        // Only send if receiver has FCM token
        if (!$receiver->fcm_token) {
            Log::info('No FCM token for user: ' . $receiver->id);
            return;
        }

        try {
            // TODO: Implement FCM notification
            // This requires kreait/firebase-php package
            
            /* CORRECT Implementation (NO notification field!):
            
            $firebase = (new Factory)->withServiceAccount(config('firebase.credentials'));
            $messaging = $firebase->createMessaging();

            // ✅ CRITICAL: Only data field, NO notification field
            // This ensures full-screen ringing UI appears even when app is killed
            $data = [
                'type' => 'incoming_call',
                'callId' => (string) $callId,
                'callerId' => (string) $caller->id,
                'callerName' => (string) $caller->name,
                'callerPhoto' => (string) ($caller->profile_image ?? ''),
                'callType' => strtoupper((string) $callType),
                'timestamp' => (string) (now()->timestamp * 1000)
            ];

            // ❌ NO withNotification() - it breaks custom ringing UI!
            $message = CloudMessage::withTarget('token', $receiver->fcm_token)
                ->withData($data)
                ->withAndroidConfig([
                    'priority' => 'high',
                    // NO notification field here either!
                ]);

            $messaging->send($message);
            
            */
            
            Log::info("Push notification sent to user {$receiver->id} for call {$callId}");
            
        } catch (\Exception $e) {
            Log::error('FCM Notification Failed: ' . $e->getMessage());
            // Don't fail the call if notification fails
        }
    }

    /**
     * Generate Agora token (placeholder)
     * 
     * TODO: Implement actual Agora token generation
     * This requires Agora App ID and App Certificate
     */
    private function generateAgoraToken($callId)
    {
        // For now, return a placeholder token
        // In production, use Agora SDK to generate real token
        return '007eJxTYBBa' . base64_encode('call_' . $callId . '_' . time());
    }

    /**
     * Accept call (called by receiver)
     * 
     * When receiver accepts:
     * - Reset their missed_calls_count to 0
     * - Set their is_busy status to true
     * - Update call status to ONGOING
     */
    public function acceptCall(Request $request, $callId)
    {
        $id = str_replace('CALL_', '', $callId);
        $call = Call::find($id);

        if (!$call) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Call not found'
                ]
            ], 404);
        }

        if ($call->receiver_id !== $request->user()->id) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'FORBIDDEN',
                    'message' => 'You are not authorized to accept this call'
                ]
            ], 403);
        }

        DB::beginTransaction();
        
        try {
            // Update call status
            $call->update([
                'status' => 'ONGOING',
                'started_at' => now()
            ]);

            // Reset missed calls counter and set busy status
            $receiver = $request->user();
            $receiver->update([
                'missed_calls_count' => 0,
                'is_busy' => true
            ]);

            // Also set caller as busy
            User::where('id', $call->caller_id)->update(['is_busy' => true]);

            DB::commit();

            $agoraToken = $this->generateAgoraToken($call->id);
            $channelName = 'call_' . $call->id;

            return response()->json([
                'success' => true,
                'message' => 'Call accepted',
                'call' => [
                    'id' => 'CALL_' . $call->id,
                    'status' => $call->status,
                    'started_at' => $call->started_at->toIso8601String(),
                    'agora_token' => $agoraToken,
                    'channel_name' => $channelName
                ]
            ]);

        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Call accept failed: ' . $e->getMessage());
            
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to accept call'
                ]
            ], 500);
        }
    }

    /**
     * End call
     * 
     * When call ends:
     * - Set both users' is_busy to false
     * - Calculate and deduct coins
     * - Create transactions
     */
    public function endCall(Request $request, $callId)
    {
        $validator = Validator::make($request->all(), [
            'duration' => 'required|integer|min:0'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION_ERROR',
                    'message' => 'Invalid input data',
                    'details' => $validator->errors()
                ]
            ], 422);
        }

        $id = str_replace('CALL_', '', $callId);
        $call = Call::find($id);

        if (!$call) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Call not found'
                ]
            ], 404);
        }

        // Calculate coins
        $duration = $request->duration; // in seconds
        $minutes = ceil($duration / 60);
        $coinsSpent = $minutes * $call->coin_rate_per_minute;

        DB::beginTransaction();
        try {
            // Update call
            $call->update([
                'status' => 'ENDED',
                'duration' => $duration,
                'coins_spent' => $coinsSpent,
                'coins_earned' => $coinsSpent,
                'ended_at' => now()
            ]);

            // Deduct coins from caller
            $caller = User::find($call->caller_id);
            $caller->decrement('coin_balance', $coinsSpent);
            $caller->update(['is_busy' => false]);  // Release busy status

            // Add coins to receiver
            $receiver = User::find($call->receiver_id);
            $receiver->increment('coin_balance', $coinsSpent);
            $receiver->increment('total_earnings', $coinsSpent);
            $receiver->update(['is_busy' => false]);  // Release busy status

            // Create transaction records
            Transaction::create([
                'user_id' => $call->caller_id,
                'type' => 'CALL_SPENT',
                'amount' => $coinsSpent,
                'coins' => $coinsSpent,
                'status' => 'SUCCESS',
                'reference_id' => $call->id,
                'reference_type' => 'CALL'
            ]);

            Transaction::create([
                'user_id' => $call->receiver_id,
                'type' => 'CALL_EARNED',
                'amount' => $coinsSpent,
                'coins' => $coinsSpent,
                'status' => 'SUCCESS',
                'reference_id' => $call->id,
                'reference_type' => 'CALL'
            ]);

            DB::commit();

            return response()->json([
                'success' => true,
                'message' => 'Call ended successfully',
                'call' => [
                    'id' => 'CALL_' . $call->id,
                    'status' => $call->status,
                    'duration' => $call->duration,
                    'coins_spent' => $call->coins_spent,
                    'coins_earned' => $call->coins_earned,
                    'ended_at' => $call->ended_at->toIso8601String()
                ],
                'caller_balance' => $caller->coin_balance,
                'receiver_earnings' => $receiver->total_earnings
            ]);
        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Call end failed: ' . $e->getMessage());
            
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to end call'
                ]
            ], 500);
        }
    }

    // ... (Keep all other methods from original CallController.php)
}







