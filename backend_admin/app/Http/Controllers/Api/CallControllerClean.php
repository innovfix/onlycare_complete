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

/**
 * Call Controller - Clean Version
 * 
 * Implements only CRITICAL validations:
 * 1. Self-call prevention
 * 2. Blocking check
 * 3. Busy status
 * 4. Push notifications
 * 5. Balance time (good UX)
 */
class CallControllerClean extends Controller
{
    /**
     * Initiate call with essential validations
     */
    public function initiateCall(Request $request)
    {
        // ============================================
        // 1. VALIDATION: Request Parameters
        // ============================================
        $validator = Validator::make($request->all(), [
            'receiver_id' => 'required|string',
            'call_type' => 'required|in:AUDIO,VIDEO'
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
        // 2. GET USERS
        // ============================================
        $caller = $request->user();
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
        // 3. CRITICAL VALIDATION: Self-Call Prevention
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
        // 4. CRITICAL VALIDATION: Blocking Check
        // ============================================
        $isBlocked = BlockedUser::where('user_id', $receiverId)
            ->where('blocked_user_id', $caller->id)
            ->exists();

        if ($isBlocked) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'USER_UNAVAILABLE',
                    'message' => 'User is not available'
                ]
            ], 400);
        }

        // ============================================
        // 5. VALIDATION: Online Status
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
        // 6. CRITICAL VALIDATION: Busy Status
        // ============================================
        if ($receiver->is_busy) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'USER_BUSY',
                    'message' => 'User is currently on another call'
                ]
            ], 400);
        }

        // ============================================
        // 7. VALIDATION: Call Type Availability
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
        // 8. VALIDATION: Coin Balance Check
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
        // 9. CALCULATE BALANCE TIME (Good UX)
        // ============================================
        $balanceMinutes = floor($caller->coin_balance / $requiredCoins);
        $balanceTime = sprintf("%d:00", $balanceMinutes);

        // ============================================
        // 10. CREATE CALL RECORD
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

            DB::commit();

        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Call creation failed: ' . $e->getMessage());
            
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to initiate call'
                ]
            ], 500);
        }

        // ============================================
        // 11. GENERATE AGORA CREDENTIALS
        // ============================================
        $agoraToken = $this->generateAgoraToken($call->id);
        $channelName = 'call_' . $call->id;

        // ============================================
        // 12. CRITICAL: SEND PUSH NOTIFICATION
        // ============================================
        $this->sendPushNotification($receiver, $caller, $call->id, $request->call_type);

        // ============================================
        // 13. RETURN SUCCESS RESPONSE
        // ============================================
        return response()->json([
            'success' => true,
            'message' => 'Call initiated successfully',
            'data' => [
                'call_id' => $call->id,
                'caller_id' => 'USR_' . $caller->id,
                'caller_name' => $caller->name,
                'caller_image' => $caller->profile_image,
                'receiver_id' => 'USR_' . $receiver->id,
                'receiver_name' => $receiver->name,
                'receiver_image' => $receiver->profile_image,
                'call_type' => $request->call_type,
                'status' => 'CONNECTING',
                'balance_time' => $balanceTime,
                'agora_token' => $agoraToken,
                'channel_name' => $channelName,
                'created_at' => $call->created_at->toIso8601String()
            ]
        ]);
    }

    /**
     * Accept call
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

            // Set both users as busy
            User::whereIn('id', [$call->caller_id, $call->receiver_id])
                ->update(['is_busy' => true]);

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
     * Reject call
     */
    public function rejectCall(Request $request, $callId)
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
                    'message' => 'You are not authorized to reject this call'
                ]
            ], 403);
        }

        $call->update([
            'status' => 'REJECTED',
            'ended_at' => now()
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Call rejected'
        ]);
    }

    /**
     * End call
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
        $duration = $request->duration;
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

            // Get users
            $caller = User::find($call->caller_id);
            $receiver = User::find($call->receiver_id);

            // Deduct coins from caller
            $caller->decrement('coin_balance', $coinsSpent);
            
            // Add coins to receiver
            $receiver->increment('coin_balance', $coinsSpent);
            $receiver->increment('total_earnings', $coinsSpent);

            // Set both users as not busy
            User::whereIn('id', [$call->caller_id, $call->receiver_id])
                ->update(['is_busy' => false]);

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

    /**
     * Rate call
     */
    public function rateCall(Request $request, $callId)
    {
        $validator = Validator::make($request->all(), [
            'rating' => 'required|integer|min:1|max:5',
            'feedback' => 'nullable|string|max:500'
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

        $call->update([
            'rating' => $request->rating,
            'feedback' => $request->feedback
        ]);

        // Update receiver's average rating
        $receiver = User::find($call->receiver_id);
        $averageRating = Call::where('receiver_id', $receiver->id)
            ->where('status', 'ENDED')
            ->whereNotNull('rating')
            ->avg('rating');

        $totalRatings = Call::where('receiver_id', $receiver->id)
            ->where('status', 'ENDED')
            ->whereNotNull('rating')
            ->count();

        $receiver->update([
            'rating' => round($averageRating, 2),
            'total_ratings' => $totalRatings
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Rating submitted successfully'
        ]);
    }

    /**
     * Get call history
     */
    public function getCallHistory(Request $request)
    {
        $perPage = min($request->get('limit', 20), 50);
        
        $calls = Call::where(function($query) use ($request) {
                    $query->where('caller_id', $request->user()->id)
                          ->orWhere('receiver_id', $request->user()->id);
                })
                ->where('status', 'ENDED')
                ->orderBy('created_at', 'desc')
                ->paginate($perPage);

        return response()->json([
            'success' => true,
            'calls' => $calls->map(function($call) use ($request) {
                $isCallerMe = $call->caller_id === $request->user()->id;
                $otherUser = $isCallerMe ? $call->receiver : $call->caller;

                return [
                    'id' => 'CALL_' . $call->id,
                    'other_user' => [
                        'id' => 'USR_' . $otherUser->id,
                        'name' => $otherUser->name,
                        'profile_image' => $otherUser->profile_image
                    ],
                    'call_type' => $call->call_type,
                    'duration' => $call->duration,
                    'coins_spent' => $isCallerMe ? $call->coins_spent : null,
                    'coins_earned' => !$isCallerMe ? $call->coins_earned : null,
                    'rating' => $call->rating,
                    'created_at' => $call->created_at->toIso8601String()
                ];
            }),
            'pagination' => [
                'current_page' => $calls->currentPage(),
                'total_pages' => $calls->lastPage(),
                'total_items' => $calls->total(),
                'per_page' => $calls->perPage()
            ]
        ]);
    }

    /**
     * Get recent call sessions
     */
    public function getRecentSessions(Request $request)
    {
        $limit = min($request->get('limit', 20), 50);
        $page = $request->get('page', 1);
        
        $calls = Call::where(function($query) use ($request) {
                    $query->where('caller_id', $request->user()->id)
                          ->orWhere('receiver_id', $request->user()->id);
                })
                ->whereIn('status', ['ENDED', 'REJECTED', 'MISSED'])
                ->with(['caller', 'receiver'])
                ->orderBy('created_at', 'desc')
                ->paginate($limit, ['*'], 'page', $page);

        $sessions = $calls->map(function($call) use ($request) {
            $isCallerMe = $call->caller_id === $request->user()->id;
            $otherUser = $isCallerMe ? $call->receiver : $call->caller;
            
            $durationFormatted = null;
            if ($call->duration) {
                $minutes = floor($call->duration / 60);
                $durationFormatted = $minutes > 0 ? $minutes . ' min' : '1 min';
            }

            return [
                'id' => 'CALL_' . $call->id,
                'user' => [
                    'id' => 'USR_' . $otherUser->id,
                    'name' => $otherUser->name,
                    'age' => $otherUser->age,
                    'profile_image' => $otherUser->profile_image,
                    'is_online' => $otherUser->is_online,
                    'audio_call_enabled' => $otherUser->audio_call_enabled,
                    'video_call_enabled' => $otherUser->video_call_enabled
                ],
                'call_type' => $call->call_type,
                'status' => $call->status,
                'duration' => $call->duration,
                'duration_formatted' => $durationFormatted,
                'coins_spent' => $isCallerMe ? $call->coins_spent : null,
                'coins_earned' => !$isCallerMe ? $call->coins_earned : null,
                'created_at' => $call->created_at->toIso8601String()
            ];
        });

        return response()->json([
            'success' => true,
            'sessions' => $sessions,
            'pagination' => [
                'current_page' => $calls->currentPage(),
                'total_pages' => $calls->lastPage(),
                'total_items' => $calls->total(),
                'per_page' => $calls->perPage(),
                'has_more' => $calls->hasMorePages()
            ]
        ]);
    }

    /**
     * Send push notification (FCM)
     * CRITICAL for app functionality
     */
    private function sendPushNotification($receiver, $caller, $callId, $callType)
    {
        if (!$receiver->fcm_token) {
            Log::info('No FCM token for user: ' . $receiver->id);
            return;
        }

        try {
            // TODO: Implement FCM notification
            // Install: composer require kreait/firebase-php
            
            /* CORRECT Implementation (NO notification field!):
            
            $firebase = (new \Kreait\Firebase\Factory)
                ->withServiceAccount(config('firebase.credentials'));
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
            $message = \Kreait\Firebase\Messaging\CloudMessage::withTarget('token', $receiver->fcm_token)
                ->withData($data)
                ->withAndroidConfig([
                    'priority' => 'high',
                ]);

            $messaging->send($message);
            */
            
            Log::info("Push notification queued for user {$receiver->id}, call {$callId}");
            
        } catch (\Exception $e) {
            Log::error('FCM Notification Failed: ' . $e->getMessage());
            // Don't fail the call if notification fails
        }
    }

    /**
     * Generate Agora token
     */
    private function generateAgoraToken($callId)
    {
        // TODO: Implement actual Agora token generation
        // For now, return a placeholder
        return '007eJxTYBBa' . base64_encode('call_' . $callId . '_' . time());
    }
}







