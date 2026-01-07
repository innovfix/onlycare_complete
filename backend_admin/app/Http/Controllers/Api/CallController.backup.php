<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Call;
use App\Models\User;
use App\Models\Transaction;
use App\Models\AppSetting;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;

class CallController extends Controller
{
    /**
     * Initiate call
     */
    public function initiateCall(Request $request)
    {
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

        // Check if receiver is online
        if (!$receiver->is_online) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'USER_OFFLINE',
                    'message' => 'User is not online'
                ]
            ], 400);
        }

        // Check if call type is enabled for receiver
        if ($request->call_type === 'AUDIO' && !$receiver->audio_call_enabled) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'CALL_NOT_AVAILABLE',
                    'message' => 'Audio call not available'
                ]
            ], 400);
        }

        if ($request->call_type === 'VIDEO' && !$receiver->video_call_enabled) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'CALL_NOT_AVAILABLE',
                    'message' => 'Video call not available'
                ]
            ], 400);
        }

        // Check caller's coin balance
        $settings = AppSetting::first();
        $requiredCoins = $request->call_type === 'AUDIO' ? 
            ($settings->audio_call_rate ?? 10) : 
            ($settings->video_call_rate ?? 15);

        if ($request->user()->coin_balance < $requiredCoins) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INSUFFICIENT_COINS',
                    'message' => 'You don\'t have enough coins to make this call',
                    'details' => [
                        'required' => $requiredCoins,
                        'available' => $request->user()->coin_balance
                    ]
                ]
            ], 400);
        }

        // Create call
        $call = Call::create([
            'caller_id' => $request->user()->id,
            'receiver_id' => $receiverId,
            'call_type' => $request->call_type,
            'status' => 'CONNECTING',
            'rate_per_minute' => $requiredCoins
        ]);

        // Generate Agora token (placeholder)
        $agoraToken = $this->generateAgoraToken($call->id);
        $channelName = 'call_' . $call->id;

        return response()->json([
            'success' => true,
            'call' => [
                'id' => 'CALL_' . $call->id,
                'caller_id' => 'USR_' . $call->caller_id,
                'receiver_id' => 'USR_' . $call->receiver_id,
                'call_type' => $call->call_type,
                'status' => $call->status,
                'created_at' => $call->created_at->toIso8601String(),
                'agora_token' => $agoraToken,
                'channel_name' => $channelName
            ],
            'receiver' => [
                'name' => $receiver->name,
                'profile_image' => $receiver->profile_image
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

        $call->update([
            'status' => 'ONGOING',
            'started_at' => now()
        ]);

        $agoraToken = $this->generateAgoraToken($call->id);
        $channelName = 'call_' . $call->id;

        return response()->json([
            'success' => true,
            'call' => [
                'id' => 'CALL_' . $call->id,
                'status' => $call->status,
                'started_at' => $call->started_at->toIso8601String(),
                'agora_token' => $agoraToken,
                'channel_name' => $channelName
            ]
        ]);
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
        $duration = $request->duration; // in seconds
        $minutes = ceil($duration / 60);
        $coinsSpent = $minutes * $call->rate_per_minute;

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

            // Add coins to receiver
            $receiver = User::find($call->receiver_id);
            $receiver->increment('coin_balance', $coinsSpent);
            $receiver->increment('total_earnings', $coinsSpent);

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

        // Update call rating
        $call->update([
            'rating' => $request->rating,
            'feedback' => $request->feedback
        ]);

        // Update receiver's average rating
        $receiver = User::find($call->receiver_id);
        $totalRatings = Call::where('receiver_id', $receiver->id)
                           ->where('status', 'ENDED')
                           ->whereNotNull('rating')
                           ->count();
        
        $averageRating = Call::where('receiver_id', $receiver->id)
                            ->where('status', 'ENDED')
                            ->whereNotNull('rating')
                            ->avg('rating');

        $receiver->update([
            'rating' => round($averageRating, 2),
            'total_ratings' => $totalRatings
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Rating submitted'
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
                    'status' => $call->status,
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
     * Get recent call sessions (all users)
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
            
            // Format duration
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
                    'username' => $otherUser->username,
                    'age' => $otherUser->age,
                    'profile_image' => $otherUser->profile_image,
                    'rating' => $otherUser->rating,
                    'is_online' => $otherUser->is_online,
                    'audio_call_enabled' => $otherUser->audio_call_enabled,
                    'video_call_enabled' => $otherUser->video_call_enabled
                ],
                'call_type' => $call->call_type,
                'status' => $call->status,
                'duration' => $call->duration,
                'duration_formatted' => $durationFormatted,
                'is_incoming' => !$isCallerMe,
                'is_outgoing' => $isCallerMe,
                'coins_spent' => $isCallerMe ? $call->coins_spent : null,
                'coins_earned' => !$isCallerMe ? $call->coins_earned : null,
                'rating' => $call->rating,
                'created_at' => $call->created_at->toIso8601String(),
                'created_at_formatted' => $this->formatCallTimestamp($call->created_at)
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
     * Get recent callers (Female only)
     */
    public function getRecentCallers(Request $request)
    {
        if ($request->user()->user_type !== 'FEMALE') {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'FORBIDDEN',
                    'message' => 'Only female users can access this endpoint'
                ]
            ], 403);
        }

        $limit = min($request->get('limit', 20), 50);

        $recentCallers = Call::where('receiver_id', $request->user()->id)
                            ->where('status', 'ENDED')
                            ->with('caller')
                            ->select('caller_id')
                            ->selectRaw('COUNT(*) as total_calls')
                            ->selectRaw('SUM(coins_earned) as total_earnings')
                            ->selectRaw('MAX(created_at) as last_call_at')
                            ->groupBy('caller_id')
                            ->orderBy('last_call_at', 'desc')
                            ->limit($limit)
                            ->get();

        $callers = $recentCallers->map(function($item) use ($request) {
            $lastCall = Call::where('receiver_id', $request->user()->id)
                           ->where('caller_id', $item->caller_id)
                           ->where('status', 'ENDED')
                           ->orderBy('created_at', 'desc')
                           ->first();

            return [
                'user' => [
                    'id' => 'USR_' . $item->caller->id,
                    'name' => $item->caller->name,
                    'age' => $item->caller->age,
                    'profile_image' => $item->caller->profile_image,
                    'rating' => $item->caller->rating
                ],
                'last_call' => [
                    'call_type' => $lastCall->call_type,
                    'duration' => $lastCall->duration,
                    'coins_earned' => $lastCall->coins_earned,
                    'created_at' => $lastCall->created_at->toIso8601String()
                ],
                'total_calls' => $item->total_calls,
                'total_earnings' => $item->total_earnings
            ];
        });

        return response()->json([
            'success' => true,
            'callers' => $callers
        ]);
    }

    /**
     * Format call timestamp for display
     */
    private function formatCallTimestamp($timestamp)
    {
        $now = now();
        $diff = $now->diffInHours($timestamp);

        if ($diff < 24) {
            return 'Today ' . $timestamp->format('h:i A');
        } elseif ($diff < 48) {
            return 'Yesterday ' . $timestamp->format('h:i A');
        } else {
            return $timestamp->format('M d, Y h:i A');
        }
    }

    /**
     * Generate Agora token (placeholder)
     */
    private function generateAgoraToken($callId)
    {
        // TODO: Implement actual Agora token generation
        // This requires Agora App ID and App Certificate
        return '007eJxTYBBa' . base64_encode('call_' . $callId . '_' . time());
    }
}

