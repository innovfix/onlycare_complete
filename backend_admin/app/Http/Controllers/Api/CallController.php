<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Call;
use App\Models\User;
use App\Models\BlockedUser;
use App\Models\Transaction;
use App\Models\AppSetting;
use Yasser\Agora\RtcTokenBuilder;
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
 * 4. Creator verification (only verified creators can receive calls)
 * 5. Call type availability (audio/video enabled)
 * 6. Push notifications
 * 7. Balance time (good UX)
 */
class CallController extends Controller
{
    /**
     * Initiate call with essential validations
     */
    public function initiateCall(Request $request)
    {
        // DEBUG: Log incoming request
        Log::info('📞 INITIATE CALL REQUEST:', [
            'caller' => $request->user()?->id ?? 'NO AUTH',
            'receiver_id' => $request->receiver_id,
            'call_type' => $request->call_type,
            'all_data' => $request->all()
        ]);
        
        // ============================================
        // 1. VALIDATION: Request Parameters
        // ============================================
        $validator = Validator::make($request->all(), [
            'receiver_id' => 'required|string',
            'call_type' => 'required|in:AUDIO,VIDEO'
        ]);

        if ($validator->fails()) {
            Log::error('❌ VALIDATION FAILED:', $validator->errors()->toArray());
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
        
        // Handle receiver_id format (accept with or without USR_ prefix)
        $receiverId = $request->receiver_id;
        // If it doesn't start with USR_, add it
        if (!str_starts_with($receiverId, 'USR_')) {
            $receiverId = 'USR_' . $receiverId;
        }
        
        $receiver = User::find($receiverId);

        if (!$receiver) {
            Log::error('❌ USER NOT FOUND:', ['receiver_id' => $receiverId]);
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'User not found'
                ]
            ], 404);
        }

        Log::info('✅ Receiver found:', ['name' => $receiver->name, 'type' => $receiver->user_type]);

        // ============================================
        // 3. CRITICAL VALIDATION: Self-Call Prevention
        // ============================================
        if ($caller->id === $receiverId) {
            Log::error('❌ SELF CALL ATTEMPT');
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
            Log::warning('❌ BLOCKED USER');
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
            Log::warning('❌ USER OFFLINE:', ['receiver' => $receiver->name]);
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
        // Check if user actually has an ongoing call (more reliable than just checking flag)
        $hasOngoingCall = Call::where(function($query) use ($receiverId) {
                $query->where('caller_id', $receiverId)
                      ->orWhere('receiver_id', $receiverId);
            })
            ->where('status', 'ONGOING')
            ->whereNull('ended_at')
            ->exists();

        // Auto-fix: Clear stale busy flag if no actual ongoing call exists
        if (!$hasOngoingCall && $receiver->is_busy) {
            Log::warning('🔧 Auto-fixing stale busy flag for user: ' . $receiverId, [
                'user_id' => $receiverId,
                'user_name' => $receiver->name,
                'was_busy' => true,
                'has_ongoing_call' => false
            ]);
            $receiver->update(['is_busy' => false]);
            $receiver->refresh(); // Refresh to get updated status
        }

        // Check busy status (either flag or actual ongoing call)
        if ($hasOngoingCall || $receiver->is_busy) {
            Log::warning('❌ USER BUSY', [
                'user_id' => $receiverId,
                'has_ongoing_call' => $hasOngoingCall,
                'is_busy_flag' => $receiver->is_busy
            ]);
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'USER_BUSY',
                    'message' => 'User is currently on another call'
                ]
            ], 400);
        }

        // NOTE: Voice/creator verification gating removed by product request.

        // ============================================
        // 7.5. VALIDATION: MALE Online DateTime Check (FEMALE → MALE calls)
        // ============================================
        // If caller is FEMALE and receiver is MALE, check if MALE's online_datetime is within 1 hour
        if ($caller->user_type === 'FEMALE' && $receiver->user_type === 'MALE') {
            if (!$receiver->online_datetime) {
                Log::warning('❌ MALE USER ONLINE DATETIME NOT SET', [
                    'receiver_id' => $receiver->id,
                    'receiver_name' => $receiver->name
                ]);
                return response()->json([
                    'success' => false,
                    'error' => [
                        'code' => 'USER_BUSY',
                        'message' => 'User is not available to receive calls'
                    ]
                ], 400);
            }

            // Check if online_datetime is less than 1 hour old
            $oneHourAgo = now()->subHour();
            if ($receiver->online_datetime->lt($oneHourAgo)) {
                Log::warning('❌ MALE USER ONLINE DATETIME EXPIRED', [
                    'receiver_id' => $receiver->id,
                    'receiver_name' => $receiver->name,
                    'online_datetime' => $receiver->online_datetime,
                    'one_hour_ago' => $oneHourAgo,
                    'hours_ago' => now()->diffInHours($receiver->online_datetime)
                ]);
                return response()->json([
                    'success' => false,
                    'error' => [
                        'code' => 'USER_BUSY',
                        'message' => 'User is not available to receive calls'
                    ]
                ], 400);
            }

            Log::info('✅ MALE user online datetime check passed', [
                'receiver_id' => $receiver->id,
                'online_datetime' => $receiver->online_datetime,
                'hours_since_update' => now()->diffInHours($receiver->online_datetime)
            ]);
        }

        // ============================================
        // 8. VALIDATION: Call Type Availability
        // ============================================
        if ($request->call_type === 'AUDIO' && !$receiver->audio_call_enabled) {
            Log::warning('❌ AUDIO CALL NOT ENABLED');
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'CALL_NOT_AVAILABLE',
                    'message' => $receiver->name . ' is not available to take calls'
                ]
            ], 400);
        }

        if ($request->call_type === 'VIDEO' && !$receiver->video_call_enabled) {
            Log::warning('❌ VIDEO CALL NOT ENABLED');
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'CALL_NOT_AVAILABLE',
                    'message' => $receiver->name . ' is not available to take video calls'
                ]
            ], 400);
        }

        Log::info('✅ Call type availability check passed');

        // ============================================
        // 9. VALIDATION: Coin Balance Check
        // ============================================
        $settings = AppSetting::first();
        $requiredCoins = $request->call_type === 'AUDIO' ? 
            ($settings->audio_call_rate ?? 10) : 
            ($settings->video_call_rate ?? 60);

        // ✅ BUSINESS LOGIC: MALE always pays, FEMALE always earns
        // Determine who should pay (MALE user)
        $payer = ($caller->user_type === 'MALE') ? $caller : $receiver;
        $earner = ($caller->user_type === 'FEMALE') ? $caller : $receiver;

        // ✅ SPECIAL CASE: FEMALE → MALE calls skip coin balance check
        // FEMALE can call MALE even if MALE has 0 coins (only online_datetime check applies)
        $isFemaleCallingMale = ($caller->user_type === 'FEMALE' && $receiver->user_type === 'MALE');

        Log::info('💰 Coin check:', [
            'required' => $requiredCoins, 
            'payer_balance' => $payer->coin_balance,
            'payer_type' => $payer->user_type,
            'payer_id' => $payer->id,
            'caller_type' => $caller->user_type,
            'receiver_type' => $receiver->user_type,
            'is_female_calling_male' => $isFemaleCallingMale,
            'skip_balance_check' => $isFemaleCallingMale
        ]);

        // Check payer's (MALE) balance - SKIP if FEMALE is calling MALE
        if (!$isFemaleCallingMale && $payer->coin_balance < $requiredCoins) {
            Log::warning('❌ INSUFFICIENT BALANCE', [
                'payer' => $payer->id,
                'payer_type' => $payer->user_type,
                'required' => $requiredCoins,
                'available' => $payer->coin_balance
            ]);
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
                        'available' => $payer->coin_balance
                    ]
                ]
            ], 400);
        }

        // Log when FEMALE calls MALE (coin check skipped)
        if ($isFemaleCallingMale) {
            Log::info('✅ FEMALE → MALE call: Coin balance check skipped', [
                'male_balance' => $payer->coin_balance,
                'required' => $requiredCoins,
                'online_datetime_check' => 'passed'
            ]);
        }

        Log::info('✅ All validations passed! Creating call...');

        // ============================================
        // 10. CALCULATE BALANCE TIME (Good UX)
        // ============================================
        // Calculate balance time for the payer (MALE user)
        $balanceTime = $this->calculateBalanceTime($payer->coin_balance, $requiredCoins);
        Log::info('💰 Balance time calculated:', [
            'balance_time' => $balanceTime, 
            'payer_coins' => $payer->coin_balance, 
            'rate' => $requiredCoins,
            'payer_type' => $payer->user_type
        ]);

        // ============================================
        // 11. GENERATE AGORA CREDENTIALS (Before creating call)
        // ============================================
        // Generate unique call ID first
        $callId = 'CALL_' . time() . rand(1000, 9999);
        
        Log::info('[agora_token] 🔑 Generating Agora token for call:', ['call_id' => $callId]);
        $agoraToken = $this->generateAgoraToken($callId);
        $channelName = 'call_' . $callId;
        
        // ✅ COMPLETE LOGGING: Log all credentials after generation
        Log::info('[agora_token] ✅ Agora credentials generated for call', [
            'call_id' => $callId,
            'app_id' => env('AGORA_APP_ID'),
            'channel_name' => $channelName,
            'uid' => 0,
            'token' => $agoraToken, // Complete token
            'token_length' => strlen($agoraToken)
        ]);

        // ============================================
        // 12. CREATE CALL RECORD WITH AGORA CREDENTIALS
        // ============================================
        DB::beginTransaction();
        
        try {
            Log::info('📝 Creating call record with Agora credentials:', ['call_id' => $callId, 'type' => $request->call_type]);
            
            $call = Call::create([
                'id' => $callId,
                'caller_id' => $caller->id,
                'receiver_id' => $receiverId,
                'call_type' => $request->call_type,
                'status' => 'CONNECTING',
                'coin_rate_per_minute' => $requiredCoins,
                'agora_token' => $agoraToken,  // ✅ SAVE TOKEN TO DATABASE
                'channel_name' => $channelName  // ✅ SAVE CHANNEL TO DATABASE
            ]);

            Log::info('✅ Call record created successfully with Agora credentials saved');

            DB::commit();

        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('❌ Call creation failed: ' . $e->getMessage());
            
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to initiate call'
                ]
            ], 500);
        }

        // ============================================
        // 13. CRITICAL: SEND PUSH NOTIFICATION
        // ============================================
        Log::info('📧 Sending push notification...');
        $this->sendPushNotification($receiver, $caller, $call->id, $request->call_type);

        // ============================================
        // 14. RETURN SUCCESS RESPONSE
        // ============================================
        Log::info('🎉 Returning success response to app (MALE USER)');
        
        // ✅ LOG WHAT'S BEING SENT TO MALE USER
        Log::info('========================================');
        Log::info('📤 SENDING TO MALE USER (API RESPONSE)');
        Log::info('========================================');
        Log::info('Call ID: ' . $call->id);
        Log::info('Caller: ' . $caller->name . ' (' . $caller->id . ')');
        Log::info('Receiver: ' . $receiver->name . ' (' . $receiver->id . ')');
        Log::info('Call Type: ' . $request->call_type);
        Log::info('');
        Log::info('🔑 AGORA CREDENTIALS SENT TO MALE:');
        Log::info('========================================');
        Log::info('AGORA_APP_ID: ' . config('services.agora.app_id'));
        Log::info('CHANNEL_NAME: ' . $channelName);
        Log::info('AGORA_TOKEN: ' . $agoraToken);
        Log::info('TOKEN_LENGTH: ' . strlen($agoraToken));
        Log::info('AGORA_UID: 0');
        Log::info('BALANCE_TIME: ' . $balanceTime);
        Log::info('========================================');
        
        return response()->json([
            'success' => true,
            'message' => 'Call initiated successfully',
            'call' => [
                'id' => $call->id,
                'caller_id' => $caller->id,
                'caller_name' => $caller->name,
                'caller_image' => $caller->profile_image,
                'receiver_id' => $receiver->id,
                'receiver_name' => $receiver->name,
                'receiver_image' => $receiver->profile_image,
                'call_type' => $request->call_type,
                'status' => 'CONNECTING',
                'duration' => 0,
                'coins_spent' => 0,
                'coins_earned' => 0,
                'started_at' => null,
                'ended_at' => null,
                'rating' => null,
                'timestamp' => $call->created_at->timestamp,
                'agora_app_id' => config('services.agora.app_id'),
                'agora_token' => $agoraToken,
                'agora_uid' => 0,  // ✅ CRITICAL: Android must use UID=0 when joining
                'channel_name' => $channelName,
                'balance_time' => $balanceTime
            ],
            'agora_app_id' => config('services.agora.app_id'),
            'agora_token' => $agoraToken,
            'agora_uid' => 0,  // ✅ CRITICAL: Android must use UID=0 when joining
            'channel_name' => $channelName,
            'balance_time' => $balanceTime
        ]);
    }

    /**
     * Get incoming calls for the authenticated user
     * ✅ FIXED: Now retrieves saved Agora credentials from database
     */
    public function getIncomingCalls(Request $request)
    {
        try {
            $user = $request->user();
            
            // Get pending calls where this user is the receiver
            $incomingCalls = Call::where('receiver_id', $user->id)
                ->whereIn('status', ['CONNECTING', 'PENDING'])
                ->with(['caller:id,name,phone,profile_image,coin_balance,user_type'])
                ->orderBy('created_at', 'desc')
                ->get()
                ->map(function ($call) {
                    $callerName = $call->caller->name ?? 'Unknown';
                    
                    // ✅ CRITICAL FIX: Use saved Agora credentials from database
                    // This ensures receiver gets the EXACT SAME credentials as the caller
                    $agoraToken = $call->agora_token;
                    $channelName = $call->channel_name;
                    
                    // ✅ COMPLETE LOGGING: Log credentials used in getIncomingCalls
                    Log::info('[agora_token] Using Agora credentials from database for incoming call', [
                        'call_id' => $call->id,
                        'app_id' => env('AGORA_APP_ID'),
                        'channel_name' => $channelName,
                        'uid' => 0,
                        'token' => $agoraToken, // Complete token
                        'token_length' => strlen($agoraToken ?? ''),
                        'token_from_db' => true
                    ]);
                    
                    // Fallback: If credentials are missing (old calls), regenerate them
                    if (empty($agoraToken) || empty($channelName)) {
                        Log::warning('[agora_token] ⚠️ Missing Agora credentials in getIncomingCalls, regenerating...', [
                            'call_id' => $call->id,
                            'token_empty' => empty($agoraToken),
                            'channel_empty' => empty($channelName)
                        ]);
                        $agoraToken = $this->generateAgoraToken($call->id);
                        $channelName = 'call_' . $call->id;
                        
                        // ✅ COMPLETE LOGGING: Log regenerated credentials
                        Log::info('[agora_token] ✅ Regenerated Agora credentials for getIncomingCalls', [
                            'call_id' => $call->id,
                            'app_id' => env('AGORA_APP_ID'),
                            'channel_name' => $channelName,
                            'uid' => 0,
                            'token' => $agoraToken, // Complete token
                            'token_length' => strlen($agoraToken),
                            'token_regenerated' => true
                        ]);
                        
                        // Save the regenerated credentials
                        $call->update([
                            'agora_token' => $agoraToken,
                            'channel_name' => $channelName
                        ]);
                    }
                    
                    // ✅ Calculate balance_time from MALE user's balance (MALE always pays)
                    $balanceTime = '0:00';
                    $caller = $call->caller;
                    // Get receiver with user_type
                    $receiver = User::select('id', 'user_type', 'coin_balance')->find($call->receiver_id);
                    // Determine MALE user (payer)
                    $payer = ($caller && $caller->user_type === 'MALE') ? $caller : $receiver;
                    if ($payer && isset($payer->coin_balance)) {
                        $callRate = $call->coin_rate_per_minute ?? ($call->call_type === 'AUDIO' ? 10 : 60);
                        $balanceTime = $this->calculateBalanceTime($payer->coin_balance, $callRate);
                    }
                    
                    // DEBUG: Log what we're actually sending
                    Log::debug('📞 Incoming call data:', [
                        'call_id' => $call->id,
                        'caller_id' => $call->caller_id,
                        'caller_name' => $callerName,
                        'call_type' => $call->call_type,
                        'agora_token_length' => strlen($agoraToken ?? ''),
                        'channel_name' => $channelName,
                        'balance_time' => $balanceTime
                    ]);
                    
                    return [
                        'id' => $call->id,
                        'caller_id' => $call->caller_id,
                        'caller_name' => $callerName,
                        'caller_image' => $call->caller->profile_image ?? null,
                        'call_type' => $call->call_type,
                        'status' => $call->status,
                        'created_at' => $call->created_at->toDateTimeString(),
                        'agora_app_id' => config('services.agora.app_id'),  // ✅ App ID for Agora SDK
                        'agora_token' => $agoraToken,      // ✅ Retrieved from database
                        'agora_uid' => 0,                  // ✅ CRITICAL: Android must use UID=0 when joining
                        'channel_name' => $channelName,    // ✅ Retrieved from database
                        'balance_time' => $balanceTime,    // ✅ NEW: Caller's balance time for countdown timer
                    ];
                });

            return response()->json([
                'success' => true,
                'data' => $incomingCalls
            ]);
            
        } catch (\Exception $e) {
            Log::error('Get incoming calls failed: ' . $e->getMessage());
            
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to fetch incoming calls'
                ]
            ], 500);
        }
    }

    /**
     * Get call status by ID
     * Allows apps to check the current status of a call
     */
    public function getCallStatus($callId)
    {
        // Handle call ID format (accept with or without CALL_ prefix)
        if (!str_starts_with($callId, 'CALL_')) {
            $callId = 'CALL_' . $callId;
        }
        
        $call = Call::find($callId);

        if (!$call) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Call not found'
                ]
            ], 404);
        }

        // Calculate remaining balance time for ongoing calls
        // ✅ BUSINESS LOGIC: MALE always pays, so calculate from MALE's balance
        $balanceTime = null;
        if ($call->status === 'ONGOING' && $call->started_at) {
            $caller = User::find($call->caller_id);
            $receiver = User::find($call->receiver_id);
            // Determine MALE user (payer)
            $payer = ($caller && $caller->user_type === 'MALE') ? $caller : $receiver;
            
            if ($payer) {
                // Calculate elapsed time and coins spent
                $elapsedSeconds = now()->diffInSeconds($call->started_at);
                $elapsedMinutes = $elapsedSeconds / 60;
                $coinsSpentSoFar = ceil($elapsedMinutes) * $call->coin_rate_per_minute;
                
                // Calculate remaining balance from MALE's balance
                $remainingCoins = max(0, $payer->coin_balance - $coinsSpentSoFar);
                $balanceTime = $this->calculateBalanceTime($remainingCoins, $call->coin_rate_per_minute);
            }
        }

        return response()->json([
            'success' => true,
            'data' => [
                'id' => $call->id,
                'caller_id' => $call->caller_id,
                'receiver_id' => $call->receiver_id,
                'call_type' => $call->call_type,
                'status' => $call->status,  // REJECTED, ONGOING, ENDED, etc.
                'agora_app_id' => config('services.agora.app_id'),
                'agora_token' => $call->agora_token,
                'channel_name' => $call->channel_name,
                'duration' => $call->duration,
                'coins_spent' => $call->coins_spent,
                'balance_time' => $balanceTime,  // Remaining time for ongoing calls
                'started_at' => $call->started_at ? $call->started_at->toIso8601String() : null,
                'ended_at' => $call->ended_at ? $call->ended_at->toIso8601String() : null,
                'timestamp' => $call->created_at->timestamp
            ]
        ]);
    }

    /**
     * Accept call
     */
    public function acceptCall(Request $request, $callId)
    {
        // Handle call ID format (accept with or without CALL_ prefix)
        if (!str_starts_with($callId, 'CALL_')) {
            $callId = 'CALL_' . $callId;
        }
        $call = Call::find($callId);

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

        // Get users for logging
        $receiver = $request->user();
        $caller = User::find($call->caller_id);
        
        // ✅ CALL_ACCEPTED LOG: Female side (receiver accepts)
        Log::info('[call_accepted] Female accepted call', [
            'call_id' => $callId,
            'caller_id' => $call->caller_id,
            'caller_name' => $caller->name ?? 'Unknown',
            'caller_type' => $caller->user_type ?? 'Unknown',
            'receiver_id' => $receiver->id,
            'receiver_name' => $receiver->name,
            'receiver_type' => $receiver->user_type,
            'call_type' => $call->call_type,
            'timestamp' => now()->toIso8601String()
        ]);

        DB::beginTransaction();
        
        try {
            // Update call status and set receiver_joined_at timestamp
            // This marks the exact moment receiver accepts the call
            $call->update([
                'status' => 'ONGOING',
                'started_at' => now(),
                'receiver_joined_at' => now()  // ✅ CRITICAL: Track when receiver actually picks up
            ]);

            // Set both users as busy
            User::whereIn('id', [$call->caller_id, $call->receiver_id])
                ->update(['is_busy' => true]);

            DB::commit();

            // ✅ Use saved Agora credentials from database (same as caller received)
            $agoraToken = $call->agora_token;
            $channelName = $call->channel_name;
            
            // ✅ COMPLETE LOGGING: Log credentials used in acceptCall
            Log::info('[agora_token] Using Agora credentials from database for acceptCall', [
                'call_id' => $callId,
                'app_id' => env('AGORA_APP_ID'),
                'channel_name' => $channelName,
                'uid' => 0,
                'token' => $agoraToken, // Complete token
                'token_length' => strlen($agoraToken ?? ''),
                'token_from_db' => true
            ]);
            
            // Fallback: If credentials are missing, regenerate them
            if (empty($agoraToken) || empty($channelName)) {
                Log::warning('[agora_token] ⚠️ Missing Agora credentials in acceptCall, regenerating...', [
                    'call_id' => $callId,
                    'token_empty' => empty($agoraToken),
                    'channel_empty' => empty($channelName)
                ]);
                $agoraToken = $this->generateAgoraToken($call->id);
                $channelName = 'call_' . $call->id;
                
                // ✅ COMPLETE LOGGING: Log regenerated credentials
                Log::info('[agora_token] ✅ Regenerated Agora credentials for acceptCall', [
                    'call_id' => $callId,
                    'app_id' => env('AGORA_APP_ID'),
                    'channel_name' => $channelName,
                    'uid' => 0,
                    'token' => $agoraToken, // Complete token
                    'token_length' => strlen($agoraToken),
                    'token_regenerated' => true
                ]);
            }

            // ✅ Notify caller (MALE) that call was accepted via FCM + WebSocket
            Log::info('[call_accepted] Female accepted call - Notifying male', [
                'call_id' => $callId,
                'caller_id' => $caller->id,
                'caller_name' => $caller->name ?? 'Unknown',
                'receiver_id' => $receiver->id,
                'receiver_name' => $receiver->name,
                'caller_has_fcm_token' => !empty($caller->fcm_token),
                'timestamp' => now()->toIso8601String()
            ]);
            Log::channel('daily')->info('[call_accepted] Female accepted call - Notifying male', [
                'call_id' => $callId,
                'caller_id' => $caller->id,
                'receiver_id' => $receiver->id,
                'caller_has_fcm_token' => !empty($caller->fcm_token)
            ]);
            
            // Send FCM notification
            $this->notifyCallAccepted($caller, $receiver, $callId, $call->call_type);
            
            // Send WebSocket notification (instant, if male is connected)
            $this->emitCallAcceptedWebSocket($caller->id, $callId, $receiver->id);

            // ✅ CALL_ACCEPTED LOG: Male side (caller notified)
            Log::info('[call_accepted] Male notified - Call accepted by female', [
                'call_id' => $callId,
                'caller_id' => $caller->id,
                'caller_name' => $caller->name ?? 'Unknown',
                'caller_type' => $caller->user_type ?? 'Unknown',
                'receiver_id' => $receiver->id,
                'receiver_name' => $receiver->name,
                'receiver_type' => $receiver->user_type,
                'call_type' => $call->call_type,
                'caller_has_fcm_token' => !empty($caller->fcm_token),
                'timestamp' => now()->toIso8601String()
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Call accepted',
                'call' => [
                    'id' => $call->id,
                    'status' => $call->status,
                    'started_at' => $call->started_at->toIso8601String(),
                    'receiver_joined_at' => $call->receiver_joined_at->toIso8601String(),  // Actual pickup time
                    'agora_app_id' => config('services.agora.app_id'),
                    'agora_token' => $agoraToken,
                    'agora_uid' => 0,  // ✅ CRITICAL: Android must use UID=0 when joining
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
        // Handle call ID format (accept with or without CALL_ prefix)
        if (!str_starts_with($callId, 'CALL_')) {
            $callId = 'CALL_' . $callId;
        }
        $call = Call::find($callId);

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

        // Clear busy status if it was set (in case call was accepted but then rejected)
        User::whereIn('id', [$call->caller_id, $call->receiver_id])
            ->update(['is_busy' => false]);

        Log::info('✅ Call rejected and busy status cleared', [
            'call_id' => $callId,
            'caller_id' => $call->caller_id,
            'receiver_id' => $call->receiver_id
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Call rejected'
        ]);
    }

    /**
     * Cancel call (called by caller before receiver accepts)
     * ✅ Public API endpoint - Requires Bearer token authentication
     */
    public function cancelCall(Request $request, $callId)
    {
        // Handle call ID format
        if (!str_starts_with($callId, 'CALL_')) {
            $callId = 'CALL_' . $callId;
        }
        
        $call = Call::with(['caller', 'receiver'])->find($callId);

        if (!$call) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Call not found'
                ]
            ], 404);
        }

        $user = $request->user();
        
        // ✅ VALIDATION: Only caller can cancel
        if ($call->caller_id !== $user->id) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'FORBIDDEN',
                    'message' => 'Only the caller can cancel this call'
                ]
            ], 403);
        }

        // ✅ VALIDATION: Call must be pending/ringing/connecting
        if (!in_array($call->status, ['PENDING', 'CONNECTING', 'RINGING'])) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INVALID_STATUS',
                    'message' => 'Call cannot be cancelled in current status: ' . $call->status
                ]
            ], 400);
        }

        DB::beginTransaction();
        try {
            // 1. Update call status
            $call->update([
                'status' => 'CANCELLED',
                'ended_at' => now()
            ]);

            // 2. Clear busy status if it was set (in case call was accepted but then cancelled)
            User::whereIn('id', [$call->caller_id, $call->receiver_id])
                ->update(['is_busy' => false]);

            Log::info('✅ Call cancelled in database and busy status cleared', [
                'call_id' => $callId,
                'caller_id' => $call->caller_id,
                'receiver_id' => $call->receiver_id
            ]);

            DB::commit();

            // 2. Send FCM notification to receiver
            $reason = $request->input('reason', 'Caller ended call');
            $this->sendCallCancelledNotification(
                $call->receiver, 
                $call->caller, 
                $callId, 
                $reason
            );

            // 3. Emit WebSocket event (if receiver is connected)
            $this->emitCallCancelledWebSocket($call->receiver_id, $callId, $reason);

            return response()->json([
                'success' => true,
                'data' => 'Call cancelled'
            ]);

        } catch (\Exception $e) {
            DB::rollBack();
            Log::error('Call cancellation failed: ' . $e->getMessage());
            
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to cancel call'
                ]
            ], 500);
        }
    }

    /**
     * Send FCM notification for call cancellation
     * Called internally by WebSocket server
     */
    public function notifyCallCancelled(Request $request)
    {
        // Verify internal secret
        $internalSecret = $request->header('X-Internal-Secret');
        if ($internalSecret !== config('websocket.secret')) {
            return response()->json([
                'success' => false,
                'error' => ['code' => 'UNAUTHORIZED']
            ], 401);
        }

        $receiverId = $request->input('receiverId');
        $callerId = $request->input('callerId');
        $callId = $request->input('callId');
        $reason = $request->input('reason', 'Caller ended call');

        $receiver = User::find($receiverId);
        $caller = User::find($callerId);

        if (!$receiver || !$caller) {
            return response()->json([
                'success' => false,
                'error' => ['code' => 'USER_NOT_FOUND']
            ], 404);
        }

        // Send FCM notification
        $this->sendCallCancelledNotification($receiver, $caller, $callId, $reason);

        return response()->json([
            'success' => true,
            'message' => 'Notification sent'
        ]);
    }

    /**
     * Emit WebSocket event for call cancellation
     * Makes HTTP call to WebSocket server to emit event
     */
    private function emitCallCancelledWebSocket($receiverId, $callId, $reason)
    {
        try {
            $socketUrl = config('websocket.url', 'http://localhost:3001');
            $secret = config('websocket.secret');
            
            if (empty($secret)) {
                Log::warning('⚠️ WebSocket secret not configured, skipping WebSocket emission');
                return;
            }

            $response = \Illuminate\Support\Facades\Http::timeout(2)
                ->withHeaders([
                    'X-Internal-Secret' => $secret,
                    'Content-Type' => 'application/json'
                ])
                ->post("{$socketUrl}/api/emit/call-cancelled", [
                    'receiverId' => $receiverId,
                    'callId' => $callId,
                    'reason' => $reason
                ]);
            
            if ($response->successful()) {
                $emitted = $response->json('emitted', false);
                Log::info('✅ WebSocket event emission result', [
                    'receiver_id' => $receiverId,
                    'call_id' => $callId,
                    'emitted' => $emitted
                ]);
            } else {
                Log::warning('⚠️ WebSocket emission request failed', [
                    'receiver_id' => $receiverId,
                    'call_id' => $callId,
                    'status' => $response->status()
                ]);
            }
            
        } catch (\Exception $e) {
            Log::warning('WebSocket emission failed (FCM will handle notification): ' . $e->getMessage());
            // Don't fail the request if WebSocket emission fails - FCM is the backup
        }
    }

    /**
     * Send FCM notification for call cancellation
     */
    private function sendCallCancelledNotification($receiver, $caller, $callId, $reason)
    {
        if (!$receiver->fcm_token) {
            Log::info('⚠️ No FCM token for user: ' . $receiver->id);
            return;
        }

        try {
            $firebase = (new \Kreait\Firebase\Factory)
                ->withServiceAccount(config('firebase.credentials'));
            $messaging = $firebase->createMessaging();

            // FCM data payload (data-only, no notification field)
            // ✅ ALL VALUES MUST BE STRINGS for Android FCM compatibility
            $data = [
                'type' => 'call_cancelled',
                'callId' => (string) $callId,
                'callerId' => (string) $caller->id,
                'callerName' => (string) $caller->name,
                'reason' => (string) $reason,
                'timestamp' => (string) (now()->timestamp * 1000) // Milliseconds
            ];

            $message = \Kreait\Firebase\Messaging\CloudMessage::withTarget('token', $receiver->fcm_token)
                ->withData($data)
                ->withAndroidConfig([
                    'priority' => 'high'
                ]);

            $result = $messaging->send($message);
            
            Log::info('✅ Call cancellation FCM sent', [
                'receiver_id' => $receiver->id,
                'call_id' => $callId,
                'result' => $result
            ]);

        } catch (\Exception $e) {
            Log::error('FCM cancellation notification failed: ' . $e->getMessage());
        }
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

        // Handle call ID format (accept with or without CALL_ prefix)
        if (!str_starts_with($callId, 'CALL_')) {
            $callId = 'CALL_' . $callId;
        }
        $call = Call::find($callId);

        if (!$call) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Call not found'
                ]
            ], 404);
        }

        // ============================================
        // IDEMPOTENCY CHECK: Prevent duplicate processing
        // ============================================
        if ($call->status === 'ENDED') {
            Log::warning('⚠️ Duplicate endCall request detected', [
                'call_id' => $call->id,
                'status' => $call->status,
                'duration' => $call->duration,
                'coins_spent' => $call->coins_spent
            ]);
            
            return response()->json([
                'success' => true,
                'message' => 'Call already ended',
                'call' => [
                    'id' => $call->id,
                    'status' => $call->status,
                    'duration' => $call->duration,
                    'coins_spent' => $call->coins_spent,
                    'coins_earned' => $call->coins_earned,
                    'started_at' => $call->started_at ? $call->started_at->toIso8601String() : null,
                    'receiver_joined_at' => $call->receiver_joined_at ? $call->receiver_joined_at->toIso8601String() : null,
                    'ended_at' => $call->ended_at ? $call->ended_at->toIso8601String() : null
                ],
                'caller_balance' => User::find($call->caller_id)->coin_balance ?? 0,
                'receiver_earnings' => User::find($call->receiver_id)->total_earnings ?? 0
            ]);
        }

        // ============================================
        // CALCULATE ACCURATE DURATION FROM SERVER
        // ============================================
        $clientDuration = $request->duration; // Duration from client (for comparison)
        $serverDuration = 0;
        
        // If receiver actually joined the call, calculate from receiver_joined_at
        if ($call->receiver_joined_at) {
            $serverDuration = now()->diffInSeconds($call->receiver_joined_at);
            Log::info('✅ Calculating duration from receiver_joined_at', [
                'call_id' => $call->id,
                'receiver_joined_at' => $call->receiver_joined_at,
                'ended_at' => now(),
                'server_duration' => $serverDuration,
                'client_duration' => $clientDuration
            ]);
        } else {
            // Fallback: Call was never answered or old call
            $serverDuration = 0;
            Log::warning('⚠️ No receiver_joined_at timestamp - call was never answered', [
                'call_id' => $call->id,
                'status' => $call->status
            ]);
        }
        
        // Validate duration difference (log if client and server differ significantly)
        $durationDifference = abs($serverDuration - $clientDuration);
        if ($durationDifference > 30 && $serverDuration > 0) {
            Log::warning('⚠️ Duration mismatch detected', [
                'call_id' => $call->id,
                'server_duration' => $serverDuration,
                'client_duration' => $clientDuration,
                'difference' => $durationDifference
            ]);
        }
        
        // Use server duration for billing (more reliable)
        $duration = $serverDuration;
        $minutes = ceil($duration / 60);
        $coinsSpent = $minutes * $call->coin_rate_per_minute;
        
        // ✅ MINIMUM CALL DURATION: No charge if call duration is less than 10 seconds
        if ($duration < 10) {
            $coinsSpent = 0;
            Log::info('⏱️ Call duration less than 10 seconds - No coins charged', [
                'call_id' => $call->id,
                'duration' => $duration,
                'coins_spent' => 0
            ]);
        }
        
        Log::info('📞 Processing call end - FINAL BILLING', [
            'call_id' => $call->id,
            'duration_used_for_billing' => $duration,
            'client_duration' => $clientDuration,
            'minutes_charged' => $minutes,
            'coins_to_spend' => $coinsSpent,
            'rate_per_minute' => $call->coin_rate_per_minute,
            'minimum_duration_applied' => $duration < 10
        ]);

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

            // ✅ BUSINESS LOGIC: MALE always pays, FEMALE always earns
            // Determine payer (MALE) and earner (FEMALE)
            $payer = ($caller->user_type === 'MALE') ? $caller : $receiver;
            $earner = ($caller->user_type === 'FEMALE') ? $caller : $receiver;

            Log::info('💰 Processing payment:', [
                'payer_id' => $payer->id,
                'payer_type' => $payer->user_type,
                'earner_id' => $earner->id,
                'earner_type' => $earner->user_type,
                'coins' => $coinsSpent,
                'duration' => $duration
            ]);

            // ✅ Only process coins if duration >= 10 seconds
            if ($coinsSpent > 0) {
                // Deduct coins from payer (MALE)
                $payer->decrement('coin_balance', $coinsSpent);
                
                // Add coins to earner (FEMALE)
                $earner->increment('coin_balance', $coinsSpent);
                $earner->increment('total_earnings', $coinsSpent);

                // Create transaction records
                Transaction::create([
                    'id' => 'TXN_' . time() . rand(1000, 9999),
                    'user_id' => $payer->id,  // ✅ MALE user pays
                    'type' => 'CALL_SPENT',
                    'amount' => $coinsSpent,
                    'coins' => $coinsSpent,
                    'status' => 'SUCCESS',
                    'reference_id' => $call->id,
                    'reference_type' => 'CALL'
                ]);

                Transaction::create([
                    'id' => 'TXN_' . time() . rand(1000, 9999),
                    'user_id' => $earner->id,  // ✅ FEMALE user earns
                    'type' => 'CALL_EARNED',
                    'amount' => $coinsSpent,
                    'coins' => $coinsSpent,
                    'status' => 'SUCCESS',
                    'reference_id' => $call->id,
                    'reference_type' => 'CALL'
                ]);
                
                Log::info('✅ Coins processed successfully', [
                    'call_id' => $call->id,
                    'coins_deducted' => $coinsSpent,
                    'coins_earned' => $coinsSpent
                ]);
            } else {
                Log::info('✅ No coins processed - Call duration less than 10 seconds', [
                    'call_id' => $call->id,
                    'duration' => $duration,
                    'coins_spent' => 0
                ]);
            }

            // Set both users as not busy (regardless of duration)
            User::whereIn('id', [$call->caller_id, $call->receiver_id])
                ->update(['is_busy' => false]);

            DB::commit();

            return response()->json([
                'success' => true,
                'message' => 'Call ended successfully',
                'call' => [
                    'id' => $call->id,
                    'status' => $call->status,
                    'duration' => $call->duration,
                    'coins_spent' => $call->coins_spent,
                    'coins_earned' => $call->coins_earned,
                    'started_at' => $call->started_at ? $call->started_at->toIso8601String() : null,
                    'receiver_joined_at' => $call->receiver_joined_at ? $call->receiver_joined_at->toIso8601String() : null,
                    'ended_at' => $call->ended_at->toIso8601String()
                ],
                'payer_balance' => $payer->coin_balance,  // ✅ MALE's balance
                'earner_earnings' => $earner->total_earnings  // ✅ FEMALE's earnings
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

        // Handle call ID format (accept with or without CALL_ prefix)
        if (!str_starts_with($callId, 'CALL_')) {
            $callId = 'CALL_' . $callId;
        }
        $call = Call::find($callId);

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
        
        // Read filter from query parameters first (standard way)
        $filter = $request->query('filter');
        
        // If not in query params, try to read from request body (for form-data in GET requests)
        // Laravel's input() method checks both query params and request body
        if (empty($filter)) {
            $filter = $request->input('filter');
        }
        
        // Also check Symfony's request parameters (for form-data)
        if (empty($filter) && $request->request->has('filter')) {
            $filter = $request->request->get('filter');
        }
        
        // If still empty and it's a GET request, manually parse form-data body
        // This handles form-data body in GET requests (non-standard but Postman allows it)
        if (empty($filter) && $request->method() === 'GET') {
            $contentType = $request->header('Content-Type', '');
            
            // Check if it's multipart/form-data
            if (strpos($contentType, 'multipart/form-data') !== false) {
                $content = $request->getContent();
                if (!empty($content)) {
                    // Parse multipart/form-data format
                    // Format: --boundary\r\nContent-Disposition: form-data; name="filter"\r\n\r\nvalue\r\n
                    // Look for name="filter" and extract the value after the next blank line
                    if (preg_match('/Content-Disposition:\s*form-data[^;]*;\s*name=["\']filter["\'][\r\n]+[\r\n]+([^\r\n]+)/i', $content, $matches)) {
                        $filter = trim($matches[1]);
                    } elseif (preg_match('/name=["\']filter["\'][\s\S]*?[\r\n]{2,}([^\r\n]+)/i', $content, $matches)) {
                        $filter = trim($matches[1]);
                    }
                }
            } elseif (strpos($contentType, 'application/x-www-form-urlencoded') !== false) {
                // Parse URL-encoded format: filter=talktime
                $content = $request->getContent();
                parse_str($content, $params);
                if (isset($params['filter'])) {
                    $filter = $params['filter'];
                }
            }
        }
        
        // Default to 'recent' if still empty
        if (empty($filter)) {
            $filter = 'recent';
        }
        
        // Normalize and validate filter parameter
        $filter = strtolower(trim($filter));
        $validFilters = ['recent', 'talktime', 'az'];
        if (!in_array($filter, $validFilters)) {
            $filter = 'recent'; // Default to recent if invalid
        }
        
        $userId = $request->user()->id;
        
        // Build base query
        $query = Call::where(function($query) use ($userId) {
                $query->where('caller_id', $userId)
                      ->orWhere('receiver_id', $userId);
            })
            ->where('status', 'ENDED')
            ->with(['caller', 'receiver']);
        
        // Handle A-Z filter separately (requires sorting in PHP after eager loading)
        if ($filter === 'az') {
            // Fetch all calls and sort by the other user's name
            $allCalls = $query->get();
            
            // Sort by the other user's name alphabetically
            $sortedCalls = $allCalls->sortBy(function($call) use ($userId) {
                $isCallerMe = $call->caller_id === $userId;
                $otherUser = $isCallerMe ? $call->receiver : $call->caller;
                return $otherUser ? strtolower($otherUser->name ?? '') : '';
            })->values();
            
            // Manual pagination
            $page = $request->get('page', 1);
            $offset = ($page - 1) * $perPage;
            $paginatedCalls = $sortedCalls->slice($offset, $perPage);
            
            // Create paginator
            $calls = new \Illuminate\Pagination\LengthAwarePaginator(
                $paginatedCalls,
                $sortedCalls->count(),
                $perPage,
                $page,
                ['path' => $request->url(), 'query' => $request->query()]
            );
        } else {
            // Apply ordering based on filter for other cases
            switch ($filter) {
                case 'talktime':
                    // Order by duration descending (longest calls first)
                    $query->orderBy('duration', 'desc')
                          ->orderBy('created_at', 'desc'); // Secondary sort by recent
                    break;
                    
                case 'recent':
                default:
                    // Order by created_at descending (most recent first) - default behavior
                    $query->orderBy('created_at', 'desc');
                    break;
            }
            
            $calls = $query->paginate($perPage);
        }

        // Map call data
        $callData = $calls->map(function($call) use ($request) {
            $isCallerMe = $call->caller_id === $request->user()->id;
            $otherUser = $isCallerMe ? $call->receiver : $call->caller;

            return [
                'id' => $call->id,
                'caller_id' => $call->caller_id,
                'caller_name' => $call->caller->name ?? 'Unknown',
                'caller_image' => $call->caller->profile_image ?? null,
                'receiver_id' => $call->receiver_id,
                'receiver_name' => $call->receiver->name ?? 'Unknown',
                'receiver_image' => $call->receiver->profile_image ?? null,
                // Add other_user field to show who the authenticated user talked to
                'other_user_id' => $otherUser ? ($otherUser->id ?? null) : null,
                'other_user_name' => $otherUser ? ($otherUser->name ?? 'Unknown') : 'Unknown',
                'other_user_image' => $otherUser ? ($otherUser->profile_image ?? null) : null,
                'call_type' => $call->call_type,
                'status' => $call->status,
                'duration' => $call->duration ?? 0,
                'coins_spent' => $isCallerMe ? ($call->coins_spent ?? 0) : 0,
                'coins_earned' => !$isCallerMe ? ($call->coins_earned ?? 0) : 0,
                'rating' => $call->rating ?? '0.0',
                'created_at' => $call->created_at->toIso8601String(),
                'started_at' => $call->started_at ? $call->started_at->toIso8601String() : null,
                'ended_at' => $call->ended_at ? $call->ended_at->toIso8601String() : null,
                'timestamp' => $call->created_at->timestamp * 1000 // Add timestamp in milliseconds
            ];
        })->values();

        // Determine message based on result
        $message = $calls->total() > 0 
            ? 'Call history fetched successfully' 
            : 'No call history found';

        return response()->json([
            'success' => true,
            'message' => $message,
            'filter' => $filter, // Include active filter in response
            'data' => $callData,
            'pagination' => [
                'current_page' => $calls->currentPage(),
                'total_pages' => $calls->lastPage(),
                'total_count' => $calls->total(),
                'total_items' => $calls->total(),
                'per_page' => $calls->perPage(),
                'has_next' => $calls->hasMorePages(),
                'has_prev' => $calls->currentPage() > 1
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
                'id' => $call->id,
                'user' => [
                    'id' => $otherUser->id,
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
     * Get recent callers (unique users who have called or been called)
     */
    public function getRecentCallers(Request $request)
    {
        $limit = min($request->get('limit', 20), 50);
        $userId = $request->user()->id;
        
        // Get unique users from call history
        $callerIds = Call::where('receiver_id', $userId)
            ->whereIn('status', ['ENDED', 'REJECTED', 'MISSED'])
            ->distinct()
            ->pluck('caller_id');
            
        $receiverIds = Call::where('caller_id', $userId)
            ->whereIn('status', ['ENDED', 'REJECTED', 'MISSED'])
            ->distinct()
            ->pluck('receiver_id');
            
        // Merge and get unique user IDs
        $uniqueUserIds = $callerIds->merge($receiverIds)->unique()->take($limit);
        
        // Get user details
        $users = User::whereIn('id', $uniqueUserIds)->get();
        
        $recentCallers = $users->map(function($user) use ($userId) {
            // Get last call with this user
            $lastCall = Call::where(function($q) use ($userId, $user) {
                    $q->where('caller_id', $userId)->where('receiver_id', $user->id);
                })
                ->orWhere(function($q) use ($userId, $user) {
                    $q->where('caller_id', $user->id)->where('receiver_id', $userId);
                })
                ->orderBy('created_at', 'desc')
                ->first();
                
            return [
                'id' => $user->id,
                'name' => $user->name,
                'age' => $user->age,
                'profile_image' => $user->profile_image,
                'is_online' => $user->is_online,
                'audio_call_enabled' => $user->audio_call_enabled,
                'video_call_enabled' => $user->video_call_enabled,
                'last_call_at' => $lastCall ? $lastCall->created_at->toIso8601String() : null,
                'last_call_type' => $lastCall ? $lastCall->call_type : null
            ];
        });
        
        return response()->json([
            'success' => true,
            'data' => $recentCallers
        ]);
    }

    /**
     * Send push notification (FCM)
     * CRITICAL for app functionality
     */
    private function sendPushNotification($receiver, $caller, $callId, $callType)
    {
        // ✅ FCM_CHECK LOG: Track when male calls female
        Log::channel('daily')->info('[fcm_check] Male called female - Checking FCM notification', [
            'caller_id' => $caller->id,
            'caller_name' => $caller->name,
            'caller_type' => $caller->user_type,
            'receiver_id' => $receiver->id,
            'receiver_name' => $receiver->name,
            'receiver_type' => $receiver->user_type,
            'call_id' => $callId,
            'call_type' => $callType,
            'receiver_has_fcm_token' => !empty($receiver->fcm_token)
        ]);
        
        if (!$receiver->fcm_token) {
            Log::info('⚠️ No FCM token for user: ' . $receiver->id);
            Log::channel('daily')->warning('[fcm_check] FCM notification FAILED - No FCM token', [
                'receiver_id' => $receiver->id,
                'receiver_name' => $receiver->name,
                'call_id' => $callId
            ]);
            return;
        }

        // ✅ VALIDATION: Check token format (FCM tokens are typically 150+ characters)
        $tokenLength = strlen(trim($receiver->fcm_token));
        if ($tokenLength < 100) {
            Log::warning('⚠️ Invalid FCM token format (too short) for user: ' . $receiver->id, [
                'token_length' => $tokenLength,
                'token_preview' => substr($receiver->fcm_token, 0, 20) . '...'
            ]);
            Log::channel('daily')->warning('[fcm_check] FCM notification FAILED - Invalid token format', [
                'receiver_id' => $receiver->id,
                'token_length' => $tokenLength,
                'call_id' => $callId
            ]);
            // Clear invalid token so app can update it on next login
            $receiver->update(['fcm_token' => null]);
            return;
        }

        try {
            Log::info('📧 Preparing FCM notification for user: ' . $receiver->id, [
                'token_preview' => substr($receiver->fcm_token, 0, 20) . '...',
                'token_length' => $tokenLength
            ]);

            // Get Agora credentials for the call
            $call = Call::find($callId);
            if (!$call) {
                Log::error('❌ Call not found for FCM notification: ' . $callId);
                return;
            }

            // ✅ Calculate balance_time from MALE user's balance (MALE always pays)
            $balanceTime = '0:00';
            // Determine MALE user (payer)
            $payer = ($caller->user_type === 'MALE') ? $caller : User::find($call->receiver_id);
            if ($payer && $payer->coin_balance) {
                $callRate = $call->coin_rate_per_minute ?? ($callType === 'AUDIO' ? 10 : 60);
                $balanceTime = $this->calculateBalanceTime($payer->coin_balance, $callRate);
            }

            // Check if Firebase credentials file exists
            $credentialsPath = config('firebase.credentials');
            if (!file_exists($credentialsPath)) {
                Log::channel('daily')->error('[fcm_check] FCM notification FAILED - Firebase credentials file missing', [
                    'receiver_id' => $receiver->id,
                    'call_id' => $callId,
                    'credentials_path' => $credentialsPath,
                    'error' => 'Firebase credentials file not found. Please upload firebase-credentials.json to storage/app/'
                ]);
                Log::error('❌ Firebase credentials file not found: ' . $credentialsPath);
                return;
            }

            // Initialize Firebase
            $firebase = (new \Kreait\Firebase\Factory)
                ->withServiceAccount($credentialsPath);
            $messaging = $firebase->createMessaging();

            // Prepare FCM data payload (REQUIRED for background/killed app state)
            // ✅ ALL VALUES MUST BE STRINGS for Android FCM compatibility
            $data = [
                'type' => 'incoming_call',
                'callerId' => (string) $caller->id,
                'callerName' => (string) $caller->name,
                'callerPhoto' => (string) ($caller->profile_image ?? ''),
                'channelId' => (string) $call->channel_name,
                'agoraToken' => (string) ($call->agora_token ?? ''),
                'agoraAppId' => (string) config('services.agora.app_id'),
                'callId' => (string) $callId,
                'callType' => strtoupper((string) $callType),  // Ensure uppercase (AUDIO/VIDEO)
                'balanceTime' => (string) $balanceTime,  // Caller's balance time for countdown timer
                'timestamp' => (string) (now()->timestamp * 1000),  // Milliseconds for Android
            ];

            // ✅ LOG WHAT'S BEING SENT TO FEMALE VIA FCM
            Log::info('========================================');
            Log::info('📤 SENDING TO FEMALE USER (FCM NOTIFICATION)');
            Log::info('========================================');
            Log::info('Caller: ' . $caller->name . ' (' . $caller->id . ')');
            Log::info('Receiver: ' . $receiver->name . ' (' . $receiver->id . ')');
            Log::info('Call ID: ' . $callId);
            Log::info('Call Type: ' . $callType);
            Log::info('');
            Log::info('🔑 AGORA CREDENTIALS SENT TO FEMALE (VIA FCM):');
            Log::info('========================================');
            Log::info('AGORA_APP_ID: ' . config('services.agora.app_id'));
            Log::info('CHANNEL_NAME: ' . $call->channel_name);
            Log::info('AGORA_TOKEN: ' . ($call->agora_token ?? 'NULL'));
            Log::info('TOKEN_LENGTH: ' . strlen($call->agora_token ?? ''));
            Log::info('BALANCE_TIME: ' . $balanceTime);
            Log::info('========================================');

            // Create FCM message with high priority for Android
            // ✅ CRITICAL: NO notification field - only data payload!
            // This ensures the app's message handler wakes up even when app is killed
            $message = \Kreait\Firebase\Messaging\CloudMessage::withTarget('token', $receiver->fcm_token)
                ->withData($data)
                ->withAndroidConfig([
                    'priority' => 'high',
                    // ❌ REMOVED: notification field - it prevents custom ringing UI
                    // The app handles the notification display via CallNotificationService
                ]);

            // Send notification
            $result = $messaging->send($message);
            
            Log::info('✅ FCM notification sent successfully', [
                'user_id' => $receiver->id,
                'call_id' => $callId,
                'balance_time' => $balanceTime,
                'result' => $result
            ]);
            
            // ✅ FCM_CHECK LOG: Successfully sent FCM to female
            Log::channel('daily')->info('[fcm_check] FCM notification SENT successfully to female', [
                'caller_id' => $caller->id,
                'caller_name' => $caller->name,
                'receiver_id' => $receiver->id,
                'receiver_name' => $receiver->name,
                'call_id' => $callId,
                'call_type' => $callType,
                'balance_time' => $balanceTime,
                'fcm_result' => (string) $result
            ]);
            
        } catch (\Kreait\Firebase\Exception\MessagingException $e) {
            $errorMessage = $e->getMessage();
            
            // ✅ DETECT: Invalid token errors and clear them automatically
            $isInvalidToken = str_contains($errorMessage, 'Requested entity was not found') ||
                             str_contains($errorMessage, 'Invalid registration token') ||
                             str_contains($errorMessage, 'registration-token-not-registered') ||
                             str_contains($errorMessage, 'MismatchSenderId');
            
            if ($isInvalidToken) {
                Log::warning('⚠️ Invalid FCM token detected - clearing token for user', [
                    'user_id' => $receiver->id,
                    'call_id' => $callId,
                    'error' => $errorMessage,
                    'token_preview' => substr($receiver->fcm_token, 0, 20) . '...'
                ]);
                
                // Clear invalid token so app can update it on next login/launch
                $receiver->update(['fcm_token' => null]);
            } else {
                Log::error('❌ FCM Messaging Exception: ' . $errorMessage, [
                    'user_id' => $receiver->id,
                    'call_id' => $callId,
                    'error_type' => get_class($e)
                ]);
                Log::channel('daily')->error('[fcm_check] FCM notification FAILED - Messaging Exception', [
                    'receiver_id' => $receiver->id,
                    'call_id' => $callId,
                    'error' => $errorMessage
                ]);
            }
            // Don't fail the call if notification fails
        } catch (\Exception $e) {
            Log::error('❌ FCM Notification Failed: ' . $e->getMessage(), [
                'user_id' => $receiver->id,
                'call_id' => $callId,
                'error_type' => get_class($e),
                'trace' => $e->getTraceAsString()
            ]);
            Log::channel('daily')->error('[fcm_check] FCM notification FAILED - Exception', [
                'receiver_id' => $receiver->id,
                'call_id' => $callId,
                'error' => $e->getMessage()
            ]);
            // Don't fail the call if notification fails
        }
    }

    /**
     * Emit WebSocket event for call acceptance
     * Makes HTTP call to WebSocket server to notify male instantly
     */
    private function emitCallAcceptedWebSocket($callerId, $callId, $receiverId)
    {
        try {
            $socketUrl = config('websocket.url', 'http://localhost:3001');
            $secret = config('websocket.secret');
            
            Log::info('[websocket_check] Attempting to notify male via WebSocket', [
                'caller_id' => $callerId,
                'call_id' => $callId,
                'receiver_id' => $receiverId,
                'socket_url' => $socketUrl,
                'has_secret' => !empty($secret)
            ]);
            
            if (empty($secret)) {
                Log::warning('[websocket_check] ⚠️ WebSocket secret not configured, skipping WebSocket emission');
                return;
            }

            $response = \Illuminate\Support\Facades\Http::timeout(2)
                ->withHeaders([
                    'X-Internal-Secret' => $secret,
                    'Content-Type' => 'application/json'
                ])
                ->post("{$socketUrl}/api/emit/call-accepted", [
                    'callerId' => $callerId,
                    'callId' => $callId,
                    'receiverId' => $receiverId
                ]);
            
            if ($response->successful()) {
                $emitted = $response->json('emitted', false);
                $reason = $response->json('reason', null);
                
                Log::info('[websocket_check] WebSocket notification result', [
                    'caller_id' => $callerId,
                    'call_id' => $callId,
                    'emitted' => $emitted,
                    'reason' => $reason,
                    'method' => 'WebSocket',
                    'status' => 'SUCCESS'
                ]);
                Log::channel('daily')->info('[websocket_check] WebSocket notification result', [
                    'caller_id' => $callerId,
                    'call_id' => $callId,
                    'emitted' => $emitted,
                    'reason' => $reason,
                    'method' => 'WebSocket'
                ]);
            } else {
                Log::warning('[websocket_check] WebSocket emission request failed', [
                    'caller_id' => $callerId,
                    'call_id' => $callId,
                    'status_code' => $response->status(),
                    'response_body' => $response->body()
                ]);
                Log::channel('daily')->warning('[websocket_check] WebSocket emission request failed', [
                    'caller_id' => $callerId,
                    'call_id' => $callId,
                    'status_code' => $response->status()
                ]);
            }
            
        } catch (\Exception $e) {
            Log::error('[websocket_check] WebSocket emission exception', [
                'caller_id' => $callerId,
                'call_id' => $callId,
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);
            Log::channel('daily')->error('[websocket_check] WebSocket emission exception', [
                'caller_id' => $callerId,
                'call_id' => $callId,
                'error' => $e->getMessage()
            ]);
            // Don't fail the request if WebSocket emission fails - FCM is the backup
        }
    }

    /**
     * Notify caller that call was accepted by receiver
     */
    private function notifyCallAccepted($caller, $receiver, $callId, $callType)
    {
        if (!$caller->fcm_token) {
            Log::warning('[call_accepted] Cannot notify male - No FCM token', [
                'caller_id' => $caller->id,
                'call_id' => $callId
            ]);
            Log::channel('daily')->warning('[call_accepted] Cannot notify male - No FCM token', [
                'caller_id' => $caller->id,
                'call_id' => $callId
            ]);
            return;
        }

        // Check if Firebase credentials file exists
        $credentialsPath = config('firebase.credentials');
        if (!file_exists($credentialsPath)) {
            Log::error('[call_accepted] Cannot notify male - Firebase credentials file missing', [
                'caller_id' => $caller->id,
                'call_id' => $callId,
                'credentials_path' => $credentialsPath,
                'error' => 'Firebase credentials file not found'
            ]);
            Log::channel('daily')->error('[call_accepted] Cannot notify male - Firebase credentials file missing', [
                'caller_id' => $caller->id,
                'call_id' => $callId,
                'credentials_path' => $credentialsPath,
                'error' => 'Firebase credentials file not found'
            ]);
            return;
        }

        try {
            $firebase = (new \Kreait\Firebase\Factory)
                ->withServiceAccount($credentialsPath);
            $messaging = $firebase->createMessaging();

            // Prepare FCM data payload for call accepted notification
            $data = [
                'type' => 'call_accepted',
                'callId' => (string) $callId,
                'receiverId' => (string) $receiver->id,
                'receiverName' => (string) $receiver->name,
                'receiverPhoto' => (string) ($receiver->profile_image ?? ''),
                'callType' => strtoupper((string) $callType),
                'timestamp' => (string) (now()->timestamp * 1000),
            ];

            $message = \Kreait\Firebase\Messaging\CloudMessage::withTarget('token', $caller->fcm_token)
                ->withData($data)
                ->withAndroidConfig([
                    'priority' => 'high',
                ]);

            $result = $messaging->send($message);
            
            // Handle result properly (result can be object/array, not string)
            $resultString = is_object($result) ? json_encode($result) : (is_array($result) ? json_encode($result) : (string) $result);
            
            Log::info('[call_accepted] FCM notification SENT to male - Call accepted', [
                'caller_id' => $caller->id,
                'receiver_id' => $receiver->id,
                'call_id' => $callId,
                'fcm_result' => $resultString
            ]);
            Log::channel('daily')->info('[call_accepted] FCM notification SENT to male - Call accepted', [
                'caller_id' => $caller->id,
                'receiver_id' => $receiver->id,
                'call_id' => $callId,
                'fcm_result' => $resultString
            ]);
            
        } catch (\Exception $e) {
            Log::error('[call_accepted] FCM notification FAILED to notify male', [
                'caller_id' => $caller->id,
                'call_id' => $callId,
                'error' => $e->getMessage()
            ]);
            Log::channel('daily')->error('[call_accepted] FCM notification FAILED to notify male', [
                'caller_id' => $caller->id,
                'call_id' => $callId,
                'error' => $e->getMessage()
            ]);
            // Don't fail the call if notification fails
        }
    }

    /**
     * Calculate balance time in MM:SS or HH:MM:SS format
     * 
     * @param int $coinBalance User's current coin balance
     * @param int $coinsPerMinute Cost per minute (10 for audio, 60 for video)
     * @return string Formatted time string (e.g., "25:00", "13:30", "1:30:00")
     */
    private function calculateBalanceTime($coinBalance, $coinsPerMinute)
    {
        // Calculate total available minutes (including fractional minutes)
        $availableMinutes = $coinBalance / $coinsPerMinute;
        
        // Extract hours, minutes, and seconds
        $hours = floor($availableMinutes / 60);
        $minutes = floor($availableMinutes % 60);
        $seconds = round(($availableMinutes - floor($availableMinutes)) * 60);
        
        // Handle edge case: if seconds round to 60, add to minutes
        if ($seconds >= 60) {
            $seconds = 0;
            $minutes++;
            if ($minutes >= 60) {
                $minutes = 0;
                $hours++;
            }
        }
        
        // Format based on duration
        if ($hours > 0) {
            // Format: HH:MM:SS for calls longer than 1 hour
            return sprintf("%d:%02d:%02d", $hours, $minutes, $seconds);
        } else {
            // Format: MM:SS for calls less than 1 hour
            return sprintf("%d:%02d", $minutes, $seconds);
        }
    }

    /**
     * Generate Agora token
     * Using exact same method from AGORA_TOKEN_GENERATION_COMPLETE_GUIDE.md
     */
    private function generateAgoraToken($callId)
    {
        try {
            // Get credentials from environment (EXACT as guide line 134-135)
            $appID = env('AGORA_APP_ID');
            $appCertificate = env('AGORA_APP_CERTIFICATE');

            // Validate credentials (EXACT as guide line 138-143)
            if (empty($appID) || empty($appCertificate)) {
                Log::warning('[agora_token] Agora credentials not configured', [
                    'call_id' => $callId,
                    'app_id_set' => !empty($appID),
                    'certificate_set' => !empty($appCertificate)
                ]);
                return '';
            }

            // Get parameters (EXACT as guide line 146-149)
            $channelName = 'call_' . $callId;
            $uid = (int) 0; // EXACT as guide: 0 for anonymous user
            $roleInput = 'publisher'; // EXACT as guide: default 'publisher'
            $expireTimeInSeconds = (int) 86400; // 24 hours (EXACT as guide allows up to 86400)

            // Determine role (EXACT as guide line 152-154)
            $role = strtolower($roleInput) === 'subscriber' 
                ? RtcTokenBuilder::RoleSubscriber 
                : RtcTokenBuilder::RolePublisher;

            // Calculate expiration timestamp (EXACT as guide line 157-158)
            $currentTimestamp = now()->getTimestamp();
            $privilegeExpiredTs = $currentTimestamp + $expireTimeInSeconds;

            // Generate token (EXACT method from guide line 161-168)
            $rtcToken = RtcTokenBuilder::buildTokenWithUid(
                $appID, 
                $appCertificate, 
                $channelName, 
                $uid, 
                $role, 
                $privilegeExpiredTs
            );
            
            // Validate token generation
            if (empty($rtcToken)) {
                Log::error('[agora_token] Failed to generate Agora token - returned empty string', [
                    'call_id' => $callId,
                    'app_id' => $appID,
                    'channel_name' => $channelName,
                    'uid' => $uid
                ]);
                return '';
            }
            
            // ✅ COMPLETE LOGGING: Log all token details for each call
            Log::info('[agora_token] ✅ Agora token generated successfully', [
                'call_id' => $callId,
                'app_id' => $appID,
                'channel_name' => $channelName,
                'uid' => $uid,
                'token' => $rtcToken, // Complete token
                'token_length' => strlen($rtcToken),
                'expires_in_seconds' => $expireTimeInSeconds,
                'expires_at_timestamp' => $privilegeExpiredTs,
                'expires_at_datetime' => date('Y-m-d H:i:s', $privilegeExpiredTs),
                'role' => 'RolePublisher',
                'method' => 'RtcTokenBuilder::buildTokenWithUid (EXACT as AGORA_TOKEN_GENERATION_COMPLETE_GUIDE.md)'
            ]);
            Log::channel('daily')->info('[agora_token] ✅ Agora token generated successfully', [
                'call_id' => $callId,
                'app_id' => $appID,
                'channel_name' => $channelName,
                'uid' => $uid,
                'token' => $rtcToken, // Complete token
                'token_length' => strlen($rtcToken),
                'expires_in_seconds' => $expireTimeInSeconds
            ]);
            
            return $rtcToken;
            
        } catch (\Exception $e) {
            Log::error('[agora_token] Agora token generation failed', [
                'call_id' => $callId,
                'app_id' => $appID,
                'channel_name' => $channelName,
                'uid' => $uid,
                'error' => $e->getMessage(),
                'error_type' => get_class($e),
                'trace' => $e->getTraceAsString()
            ]);
            Log::channel('daily')->error('[agora_token] Agora token generation failed', [
                'call_id' => $callId,
                'app_id' => $appID,
                'channel_name' => $channelName,
                'uid' => $uid,
                'error' => $e->getMessage()
            ]);
            // Return empty string if generation fails (app will use null token)
            return '';
        }
    }

    /**
     * Random User API - Connect male users with female creators for calls
     * POST /api/random_user
     * Based on documentation: Tier-based selection with 3-layer repeat prevention
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function random_user(Request $request)
    {
        try {
            // ============================================
            // 1. AUTHENTICATION & VALIDATION
            // ============================================
            $user = $request->user();
            
            if (!$user) {
                return response()->json([
                    'success' => false,
                    'message' => 'Unauthorized. Please provide a valid token.'
                ], 401);
            }

            // Validation (removed user_id - now using authenticated user from token)
            $validator = Validator::make($request->all(), [
                'call_type' => 'required|in:audio,video'
            ], [
                'call_type.required' => 'call_type is required and must be either \'audio\' or \'video\'.',
                'call_type.in' => 'call_type is required and must be either \'audio\' or \'video\'.'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => $validator->errors()->first()
                ], 200);
            }

            $callType = strtolower($request->call_type);
            $userId = $user->id; // Use authenticated user's ID from token

            // Check if user is blocked
            if ($user->is_blocked) {
                return response()->json([
                    'success' => false,
                    'message' => 'Your account has been suspended for 48 hours due to a violation of our policy.'
                ], 200);
            }

            // ============================================
            // 2. COIN BALANCE CHECK
            // ============================================
            $coins = $user->coin_balance ?? 0;
            $minCoins = ($callType === 'audio') ? 10 : 60;
            
            if ($coins < $minCoins) {
                $message = ($callType === 'audio') 
                    ? 'Insufficient coins for audio call. Minimum 10 coins required.'
                    : 'Insufficient coins for video call. Minimum 60 coins required.';
                
                return response()->json([
                    'success' => false,
                    'message' => $message
                ], 200);
            }

            // Calculate balance time
            $conversionRate = ($callType === 'audio') ? 10 : 60;
            $availableMinutes = floor($coins / $conversionRate);
            $balanceTime = sprintf("%d:%02d", $availableMinutes, 0);

            // ============================================
            // 3. TIER-BASED CREATOR SELECTION
            // ============================================
            $language = $user->language ?? 'ENGLISH';
            
            // Get eligible creators by tier (adapted to project structure)
            $eligibleCreators = $this->getCreatorsWithTiers($language, $callType);
            
            // Check if any creators available
            $totalCreators = 0;
            foreach ($eligibleCreators as $tier => $creators) {
                $totalCreators += $creators->count();
            }
            
            if ($totalCreators === 0) {
                return response()->json([
                    'success' => false,
                    'message' => 'Users are busy right now.'
                ], 200);
            }

            // ============================================
            // 4. REPEAT CONNECTION PREVENTION (3-LAYER)
            // ============================================
            $blockedCreatorIds = $this->getBlockedCreatorIds($userId, $callType);
            
            // Filter out blocked creators from each tier
            foreach ($eligibleCreators as $tier => $creators) {
                $eligibleCreators[$tier] = $creators->whereNotIn('id', $blockedCreatorIds);
            }

            // ============================================
            // 5. PROBABILITY-BASED TIER SELECTION
            // ============================================
            $probabilities = [
                'high' => 85,
                'medium' => 10,
                'trial' => 5,
                'low' => 0
            ];
            
            $selectedTier = $this->selectTierByProbability($probabilities, $eligibleCreators);
            
            // Get creators from selected tier
            $tierCreators = $eligibleCreators[$selectedTier] ?? collect();
            
            // If selected tier is empty, get next best tier
            if ($tierCreators->isEmpty()) {
                $selectedTier = $this->getNextBestTier($selectedTier, $eligibleCreators);
                $tierCreators = $eligibleCreators[$selectedTier] ?? collect();
            }

            if ($tierCreators->isEmpty()) {
                // Try with relaxed blocking (last 5 calls only)
                $blockedCreatorIds = $this->getBlockedCreatorIds($userId, $callType, true);
                
                foreach ($eligibleCreators as $tier => $creators) {
                    $eligibleCreators[$tier] = $creators->whereNotIn('id', $blockedCreatorIds);
                }
                
                $selectedTier = $this->selectTierByProbability($probabilities, $eligibleCreators);
                $tierCreators = $eligibleCreators[$selectedTier] ?? collect();
                
                if ($tierCreators->isEmpty()) {
                    $selectedTier = $this->getNextBestTier($selectedTier, $eligibleCreators);
                    $tierCreators = $eligibleCreators[$selectedTier] ?? collect();
                }
            }

            if ($tierCreators->isEmpty()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Users are busy right now.'
                ], 200);
            }

            // ============================================
            // 6. CREATOR SELECTION FROM TIER
            // ============================================
            // High tier: Prioritize by revenue, others: random
            if ($selectedTier === 'high') {
                // Sort by revenue (if field exists, otherwise random)
                if (DB::getSchemaBuilder()->hasColumn('users', 'total_earnings')) {
                    $tierCreators = $tierCreators->sortByDesc('total_earnings');
                }
                $selectedCreator = $tierCreators->first();
            } else {
                // Random selection for other tiers
                $selectedCreator = $tierCreators->random();
            }

            // ============================================
            // 7. REAL-TIME AVAILABILITY CHECK
            // ============================================
            $creator = User::find($selectedCreator->id);
            
            if (!$creator) {
                return response()->json([
                    'success' => false,
                    'message' => 'Users are busy right now.'
                ], 200);
            }

            // Re-check busy status (prevents race conditions)
            $creator->refresh();
            if ($creator->is_busy) {
                // Try another creator
                $tierCreators = $tierCreators->where('id', '!=', $creator->id);
                if ($tierCreators->isEmpty()) {
                    return response()->json([
                        'success' => false,
                        'message' => 'Users are busy right now.'
                    ], 200);
                }
                $selectedCreator = $tierCreators->random();
                $creator = User::find($selectedCreator->id);
            }

            // Check blocking status
            $isBlocked = DB::table('blocked_users')
                ->where(function($query) use ($userId, $creator) {
                    $query->where('user_id', $userId)
                          ->where('blocked_user_id', $creator->id)
                          ->orWhere('user_id', $creator->id)
                          ->where('blocked_user_id', $userId);
                })
                ->exists();

            if ($isBlocked) {
                // Try another creator
                $tierCreators = $tierCreators->where('id', '!=', $creator->id);
                if ($tierCreators->isEmpty()) {
                    return response()->json([
                        'success' => false,
                        'message' => 'Users are busy right now.'
                    ], 200);
                }
                $selectedCreator = $tierCreators->random();
                $creator = User::find($selectedCreator->id);
            }

            // ============================================
            // 8. CALL RECORD CREATION
            // ============================================
            $callId = 'CALL_' . time() . rand(1000, 9999);
            $callTypeUpper = strtoupper($callType);
            
            // Generate Agora credentials (reuse existing method)
            Log::info('[agora_token] 🔑 Generating Agora token for random_user call:', ['call_id' => $callId]);
            $agoraToken = $this->generateAgoraToken($callId);
            $channelName = 'call_' . $callId;
            
            // ✅ COMPLETE LOGGING: Log all credentials after generation for random_user
            Log::info('[agora_token] ✅ Agora credentials generated for random_user call', [
                'call_id' => $callId,
                'app_id' => env('AGORA_APP_ID'),
                'channel_name' => $channelName,
                'uid' => 0,
                'token' => $agoraToken, // Complete token
                'token_length' => strlen($agoraToken)
            ]);

            DB::beginTransaction();
            
            try {
                // Create call record (using calls table, adapted from documentation's user_calls)
                $call = Call::create([
                    'id' => $callId,
                    'caller_id' => $userId,
                    'receiver_id' => $creator->id,
                    'call_type' => $callTypeUpper,
                    'status' => 'CONNECTING',
                    'coin_rate_per_minute' => $conversionRate,
                    'agora_token' => $agoraToken,
                    'channel_name' => $channelName
                ]);

                // Increment creator's missed calls (if field exists)
                try {
                    if (DB::getSchemaBuilder()->hasColumn('users', 'missed_calls')) {
                        $creator->increment('missed_calls');
                    }
                } catch (\Exception $e) {
                    Log::warning('Failed to increment missed_calls', ['error' => $e->getMessage()]);
                }

                DB::commit();

            } catch (\Exception $e) {
                DB::rollBack();
                Log::error('Random call creation failed: ' . $e->getMessage());
                
                return response()->json([
                    'success' => false,
                    'message' => 'An error occurred while processing your request. Please try again.'
                ], 500);
            }

            // ============================================
            // 9. SEND PUSH NOTIFICATION
            // ============================================
            $this->sendPushNotification($creator, $user, $call->id, $callTypeUpper);

            // ============================================
            // 10. RESPONSE PREPARATION
            // ============================================
            // Get avatar images
            $userAvatar = $this->getAvatarImage($user->profile_image);
            $creatorAvatar = $this->getAvatarImage($creator->profile_image);

            return response()->json([
                'success' => true,
                'message' => 'Data created successfully.',
                'data' => [
                    'call_id' => $call->id,
                    'user_id' => $user->id,
                    'user_name' => $user->name,
                    'user_avatar_image' => $userAvatar,
                    'call_user_id' => $creator->id,
                    'call_user_name' => $creator->name,
                    'call_user_avatar_image' => $creatorAvatar,
                    'type' => $callType,
                    'started_time' => '',
                    'balance_time' => $balanceTime,
                    'date_time' => now()->format('Y-m-d H:i:s'),
                    // Include Agora credentials
                    'agora_app_id' => config('services.agora.app_id'),
                    'agora_token' => $agoraToken,
                    'agora_uid' => 0,
                    'channel_name' => $channelName
                ]
            ]);

        } catch (\Exception $e) {
            Log::error('❌ Random User Exception', [
                'user_id' => $request->user_id ?? 'unknown',
                'error' => $e->getMessage(),
                'file' => $e->getFile(),
                'line' => $e->getLine(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return response()->json([
                'success' => false,
                'message' => 'An error occurred while processing your request. Please try again.',
                'error' => config('app.debug') ? $e->getMessage() : null
            ], 500);
        }
    }

    /**
     * Get creators grouped by tiers
     * Adapted from documentation - uses project's database structure
     * 
     * @param string $language
     * @param string $callType
     * @return array
     */
    private function getCreatorsWithTiers($language, $callType)
    {
        // Get connected languages (simple exact match)
        $connectedLanguages = $this->getConnectedLanguages($language);
        
        // Get active call user IDs (currently in calls)
        $activeCallUserIds = DB::table('calls')
            ->where('status', 'ONGOING')
            ->whereNull('ended_at')
            ->where(function($query) {
                $query->whereNotNull('started_at')
                      ->whereNull('ended_at');
            })
            ->pluck('receiver_id')
            ->toArray();

        // Build base query
        $statusField = ($callType === 'audio') ? 'audio_call_enabled' : 'video_call_enabled';
        
        $query = User::where('user_type', 'FEMALE')
            ->where('gender', 'FEMALE')
            ->whereIn('language', $connectedLanguages)
            ->where('is_busy', false)
            ->where($statusField, true)
            ->where('is_active', true)
            ->where('is_blocked', false)
            ->whereNotIn('id', $activeCallUserIds);

        // Get all eligible creators
        $allCreators = $query->select('id', 'name', 'profile_image', 'language', 'total_earnings')
            ->get();

        // Group by tier (simplified - since no tier fields in DB, use revenue-based tiers)
        $tiers = [
            'trial' => collect(),
            'high' => collect(),
            'medium' => collect(),
            'low' => collect()
        ];

        foreach ($allCreators as $creator) {
            $revenue = $creator->total_earnings ?? 0;
            
            // Tier assignment based on revenue (you can adjust thresholds)
            if ($revenue >= 10000) {
                $tiers['high']->push($creator);
            } elseif ($revenue >= 1000) {
                $tiers['medium']->push($creator);
            } elseif ($revenue >= 0) {
                $tiers['trial']->push($creator);
            } else {
                $tiers['low']->push($creator);
            }
        }

        return $tiers;
    }

    /**
     * Select tier based on weighted probability
     * From documentation
     * 
     * @param array $probabilities
     * @param array $eligibleCreators
     * @return string
     */
    private function selectTierByProbability($probabilities, $eligibleCreators)
    {
        // Filter out tiers with no creators
        $availableProbabilities = [];
        foreach ($probabilities as $tier => $prob) {
            if (($eligibleCreators[$tier] ?? collect())->isNotEmpty()) {
                $availableProbabilities[$tier] = $prob;
            }
        }

        // If no tiers available, return trial
        if (empty($availableProbabilities)) {
            return 'trial';
        }

        // Normalize probabilities
        $total = array_sum($availableProbabilities);
        if ($total == 0) {
            return 'trial';
        }

        $cumulative = 0;
        $random = rand(1, 100);
        
        foreach ($availableProbabilities as $tier => $prob) {
            $cumulative += ($prob / $total) * 100;
            if ($random <= $cumulative) {
                return $tier;
            }
        }

        // Fallback to first available tier
        return array_key_first($availableProbabilities);
    }

    /**
     * Get next best tier if selected tier is empty
     * From documentation
     * 
     * @param string $currentTier
     * @param array $eligibleCreators
     * @return string
     */
    private function getNextBestTier($currentTier, $eligibleCreators)
    {
        $priorityOrder = ['high', 'medium', 'trial'];
        
        // Remove current tier from priority
        $priorityOrder = array_filter($priorityOrder, function($tier) use ($currentTier) {
            return $tier !== $currentTier;
        });
        
        // Find first non-empty tier
        foreach ($priorityOrder as $tier) {
            if (($eligibleCreators[$tier] ?? collect())->isNotEmpty()) {
                return $tier;
            }
        }
        
        // Fallback to trial
        return 'trial';
    }

    /**
     * Get blocked creator IDs (3-layer protection)
     * From documentation - adapted to use calls table
     * 
     * @param string $userId
     * @param string $callType
     * @param bool $relaxed If true, only blocks last 5 calls
     * @return array
     */
    private function getBlockedCreatorIds($userId, $callType, $relaxed = false)
    {
        $blockedIds = [];
        $callTypeUpper = strtoupper($callType);

        // Layer 1: Recent random connections (last 30 minutes)
        // Using calls table - all calls are considered "random" for this endpoint
        $thirtyMinutesAgo = now()->subMinutes(30);
        $recentRandomCalls = DB::table('calls')
            ->where('caller_id', $userId)
            ->where('call_type', $callTypeUpper)
            ->where('created_at', '>=', $thirtyMinutesAgo)
            ->pluck('receiver_id')
            ->toArray();
        
        $blockedIds = array_merge($blockedIds, $recentRandomCalls);

        // Layer 2: Recent call history (last 15 minutes, same call type)
        $fifteenMinutesAgo = now()->subMinutes(15);
        $recentCalls = DB::table('calls')
            ->where('caller_id', $userId)
            ->where('call_type', $callTypeUpper)
            ->where('created_at', '>=', $fifteenMinutesAgo)
            ->pluck('receiver_id')
            ->toArray();
        
        $blockedIds = array_merge($blockedIds, $recentCalls);

        // Layer 3: Session-based blocking (last 20 random calls)
        if (!$relaxed) {
            $sessionCalls = DB::table('calls')
                ->where('caller_id', $userId)
                ->where('call_type', $callTypeUpper)
                ->orderBy('created_at', 'desc')
                ->limit(20)
                ->pluck('receiver_id')
                ->toArray();
            
            $blockedIds = array_merge($blockedIds, $sessionCalls);
        } else {
            // Relaxed: Only last 5 calls
            $sessionCalls = DB::table('calls')
                ->where('caller_id', $userId)
                ->where('call_type', $callTypeUpper)
                ->orderBy('created_at', 'desc')
                ->limit(5)
                ->pluck('receiver_id')
                ->toArray();
            
            $blockedIds = array_merge($blockedIds, $sessionCalls);
        }

        // Remove duplicates
        return array_unique($blockedIds);
    }

    /**
     * Get connected languages based on user's language
     * Simple version - exact match only (as requested)
     * 
     * @param string $language
     * @return array
     */
    private function getConnectedLanguages($language)
    {
        // Simple: just return the user's language as an array
        if (empty($language)) {
            return ['ENGLISH']; // Default fallback
        }
        
        // Normalize to uppercase to match database enum values
        return [strtoupper($language)];
    }

    /**
     * Get avatar image URL
     * 
     * @param string|null $profileImage
     * @return string
     */
    private function getAvatarImage($profileImage)
    {
        if (empty($profileImage)) {
            return '';
        }

        // If already a full URL, return as is
        if (preg_match('/^https?:\/\//', $profileImage)) {
            return $profileImage;
        }

        // Otherwise, prepend base URL
        return url('storage/' . ltrim($profileImage, '/'));
    }
}

