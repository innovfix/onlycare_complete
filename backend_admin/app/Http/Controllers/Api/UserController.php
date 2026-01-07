<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\BlockedUser;
use App\Models\AppSetting;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class UserController extends Controller
{
    /**
     * Helper method to check if user is female
     * Handles case sensitivity and checks both gender and user_type fields
     * 
     * @param User $user
     * @return bool
     */
    private function isFemaleUser($user): bool
    {
        // Check user_type first (primary field used in app)
        if ($user->user_type) {
            return strtoupper(trim($user->user_type)) === 'FEMALE';
        }
        
        // Fallback to gender field
        if ($user->gender) {
            return strtoupper(trim($user->gender)) === 'FEMALE';
        }
        
        return false;
    }

    /**
     * Helper method to check if user is male
     * Handles case sensitivity and checks both gender and user_type fields
     * 
     * @param User $user
     * @return bool
     */
    private function isMaleUser($user): bool
    {
        // Check user_type first (primary field used in app)
        if ($user->user_type) {
            return strtoupper(trim($user->user_type)) === 'MALE';
        }
        
        // Fallback to gender field
        if ($user->gender) {
            return strtoupper(trim($user->gender)) === 'MALE';
        }
        
        return false;
    }

    /**
     * Get current user profile
     */
    public function me(Request $request)
    {
        $user = $request->user()->load('bankAccounts');
        
        return response()->json([
            'success' => true,
            'data' => $this->formatUserResponse($user, true)
        ]);
    }

    /**
     * Update user profile
     */
    public function updateProfile(Request $request)
    {
        $user = $request->user();
        
        $validator = Validator::make($request->all(), [
            'username' => [
                'sometimes',
                'string',
                'min:4',
                'max:10',
                'regex:/^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+$/',
                'unique:users,username,' . $user->id
            ],
            'name' => 'sometimes|string|min:2|max:100',
            'interests' => 'nullable|array',
            'profile_image' => 'nullable|integer|exists:avatars,id',  // Changed to integer and exists validation

            // Status fields (backward compatible with multiple app payload formats)
            'is_online' => 'sometimes|nullable|boolean',
            'online_status' => 'sometimes|nullable|in:ONLINE,OFFLINE,BUSY',
            'call_availability' => 'sometimes|nullable|in:AVAILABLE,UNAVAILABLE,IN_CALL',
            'audio_call_enabled' => 'sometimes|nullable|boolean',
            'video_call_enabled' => 'sometimes|nullable|boolean'
        ], [
            'username.required' => 'Username is required',
            'username.min' => 'Username must be at least 4 characters',
            'username.max' => 'Username must not exceed 10 characters',
            'username.regex' => 'Username must contain both letters and numbers',
            'username.unique' => 'This username already exists. Please choose a different username.',
            'name.min' => 'Name must be at least 2 characters',
            'name.max' => 'Name must not exceed 100 characters',
            'interests.array' => 'Interests must be an array',
            'profile_image.integer' => 'Profile image must be a valid avatar ID',
            'profile_image.exists' => 'Invalid avatar ID provided'
        ]);

        if ($validator->fails()) {
            // Check specifically for username uniqueness error
            if ($validator->errors()->has('username') && str_contains($validator->errors()->first('username'), 'already been taken')) {
                return response()->json([
                    'success' => false,
                    'error' => [
                        'code' => 'USERNAME_EXISTS',
                        'message' => 'This username already exists. Please choose a different username.',
                        'details' => [
                            'username' => ['This username already exists. Please choose a different username.']
                        ]
                    ]
                ], 422);
            }
            
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION_ERROR',
                    'message' => 'Invalid input data',
                    'details' => $validator->errors()
                ]
            ], 422);
        }

        $updateData = [];
        if ($request->has('username')) $updateData['username'] = $request->username;
        if ($request->has('name')) $updateData['name'] = $request->name;
        // Note: age, bio, gender, language NOT updated here - set during registration only
        if ($request->has('interests')) $updateData['interests'] = json_encode($request->interests);

        // Handle profile_image as avatar_id - get image_url from avatars table
       // Handle profile_image as avatar_id - get image_url from avatars table
        if ($request->has('profile_image')) {
            $avatarId = $request->profile_image;
            
            // Find avatar by ID and get its image_url
            $avatar = \App\Models\Avatar::find($avatarId);
            if ($avatar) {
                $updateData['avatar_id'] = $avatarId;
                $avatarUrl = $avatar->image_url;  // Get URL from avatar table
                
                // Prepend base URL if it doesn't already have http/https
                if (!empty($avatarUrl) && !preg_match('/^https?:\/\//', $avatarUrl)) {
                    $avatarUrl = 'https://onlycare.in/' . ltrim($avatarUrl, '/');
                }
                
                $updateData['profile_image'] = $avatarUrl;
            } else {
                return response()->json([
                    'success' => false,
                    'error' => [
                        'code' => 'AVATAR_NOT_FOUND',
                        'message' => 'Invalid avatar ID provided'
                    ]
                ], 422);
            }
        }

        // Online status (accept either is_online boolean OR online_status enum-like string)
        if ($request->has('is_online') && $request->is_online !== null) {
            $updateData['is_online'] = $request->boolean('is_online');
            $updateData['last_seen'] = time();

            // If user explicitly sets offline, clear busy
            if ($updateData['is_online'] === false) {
                $updateData['is_busy'] = false;
            }
        } elseif ($request->filled('online_status')) {
            $onlineStatus = strtoupper(trim((string) $request->input('online_status')));
            if ($onlineStatus === 'ONLINE') {
                $updateData['is_online'] = true;
                $updateData['is_busy'] = false;
                $updateData['last_seen'] = time();
            } elseif ($onlineStatus === 'OFFLINE') {
                $updateData['is_online'] = false;
                $updateData['is_busy'] = false;
                $updateData['last_seen'] = time();
            } elseif ($onlineStatus === 'BUSY') {
                $updateData['is_online'] = true;
                $updateData['is_busy'] = true;
                $updateData['last_seen'] = time();
            }
        }

        // Call availability (female users only) - accept either combined string or boolean flags
        if ($this->isFemaleUser($user)) {
            if ($request->filled('call_availability')) {
                $callAvailability = strtoupper(trim((string) $request->input('call_availability')));
                if ($callAvailability === 'AVAILABLE') {
                    $updateData['audio_call_enabled'] = true;
                    $updateData['video_call_enabled'] = true;
                    $updateData['is_busy'] = false;
                } elseif ($callAvailability === 'UNAVAILABLE') {
                    $updateData['audio_call_enabled'] = false;
                    $updateData['video_call_enabled'] = false;
                    $updateData['is_busy'] = false;
                } elseif ($callAvailability === 'IN_CALL') {
                    $updateData['is_busy'] = true;
                }
            } else {
                // Only update boolean flags if present and not null
                if ($request->has('audio_call_enabled') && $request->audio_call_enabled !== null) {
                    $updateData['audio_call_enabled'] = $request->boolean('audio_call_enabled');
                }
                if ($request->has('video_call_enabled') && $request->video_call_enabled !== null) {
                    $updateData['video_call_enabled'] = $request->boolean('video_call_enabled');
                }
            }
        }

        $user->update($updateData);

        return response()->json([
            'success' => true,
            'message' => 'Profile updated successfully',
            'data' => $this->formatUserResponse($user, true)
        ]);
    }

   /**
 * Get female users (Male users only)
 */
public function getFemales(Request $request)
{
    if (!$this->isMaleUser($request->user())) {
        return response()->json([
            'success' => false,
            'error' => [
                'code' => 'FORBIDDEN',
                'message' => 'Only male users can access this endpoint'
            ]
        ], 403);
    }

    $query = User::where('user_type', 'FEMALE')
                ->where('is_active', true);

    // Auto-filter by requesting user's language
    $requestingUser = $request->user();
    $userLanguage = $requestingUser->language;
    
    // If user has a language set, filter by it (unless overridden by query parameter)
    if ($userLanguage && !$request->has('language')) {
        $query->where('language', $userLanguage);
    }
    
    // Apply filters
    if ($request->has('online') && $request->online === 'true') {
        $query->where('is_online', true);
    }

    if ($request->has('verified') && $request->verified === 'true') {
        $query->where('kyc_status', 'APPROVED');
    }

    // Allow manual language override via query parameter
    if ($request->has('language')) {
        $query->where('language', $request->language);
    }

    // Get blocked users to exclude
    $blockedUserIds = BlockedUser::where('user_id', $request->user()->id)
                                ->pluck('blocked_user_id')
                                ->toArray();
    
    if (!empty($blockedUserIds)) {
        $query->whereNotIn('id', $blockedUserIds);
    }

    // ✅ Randomize female users so each refresh returns a different set/order
    // If a "seed" is provided, use deterministic RAND(seed) ordering (helps bypass proxy caching and guarantees a new shuffle per refresh).
    if ($request->filled('seed')) {
        $seed = (int) $request->input('seed');
        $driver = \DB::connection()->getDriverName();
        if (in_array($driver, ['mysql', 'mariadb'], true)) {
            $query->orderByRaw('RAND(' . $seed . ')');
        } else {
            $query->inRandomOrder();
        }
    } else {
        $query->inRandomOrder();
    }

    // Pagination
    $perPage = min($request->get('limit', 20), 50);
    $users = $query->paginate($perPage);

    // Get call rates for response
    $callRates = $this->getCallRates();

    return response()->json([
        'success' => true,
        'data' => $users->map(function($user) use ($callRates) {
            return $this->formatUserResponse($user, false, $callRates);
        }),
        'pagination' => [
            'current_page' => $users->currentPage(),
            'total_pages' => $users->lastPage(),
            'total_items' => $users->total(),
            'per_page' => $users->perPage(),
            'has_next' => $users->hasMorePages(),
            'has_prev' => $users->currentPage() > 1
        ]
    ]);
}
    /**
     * Get user by ID
     */
    public function getUserById(Request $request, $userId)
    {
        // Handle both formats: "USR_1" or "1"
        // First try with the ID as-is (might already have USR_ prefix)
        $user = User::find($userId);
        
        // If not found and ID doesn't have USR_ prefix, try adding it
        if (!$user && strpos($userId, 'USR_') !== 0) {
            $user = User::find('USR_' . $userId);
        }
        
        // If still not found and has USR_ prefix, try without it (fallback)
        if (!$user && strpos($userId, 'USR_') === 0) {
            $id = str_replace('USR_', '', $userId);
            $user = User::find($id);
        }

        if (!$user) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'User not found'
                ]
            ], 404);
        }

        return response()->json([
            'success' => true,
            'data' => $this->formatUserResponse($user, true)
        ]);
    }

    /**
     * Update online status
     */
    public function updateStatus(Request $request)
    {
        $validator = Validator::make($request->all(), [
            // Support both payload formats:
            // - { is_online: true/false }
            // - { online_status: "ONLINE|OFFLINE|BUSY" }
            'is_online' => 'sometimes|nullable|boolean',
            'online_status' => 'sometimes|nullable|in:ONLINE,OFFLINE,BUSY'
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

        $user = $request->user();

        $updateData = [
            'last_seen' => time()
        ];

        if ($request->has('is_online') && $request->is_online !== null) {
            $updateData['is_online'] = $request->boolean('is_online');
            if ($updateData['is_online'] === false) {
                $updateData['is_busy'] = false;
            }
        } elseif ($request->filled('online_status')) {
            $onlineStatus = strtoupper(trim((string) $request->input('online_status')));
            if ($onlineStatus === 'ONLINE') {
                $updateData['is_online'] = true;
                $updateData['is_busy'] = false;
            } elseif ($onlineStatus === 'OFFLINE') {
                $updateData['is_online'] = false;
                $updateData['is_busy'] = false;
            } elseif ($onlineStatus === 'BUSY') {
                $updateData['is_online'] = true;
                $updateData['is_busy'] = true;
            }
        } else {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION_ERROR',
                    'message' => 'Either is_online or online_status must be provided'
                ]
            ], 422);
        }

        $user->update($updateData);

        return response()->json([
            'success' => true,
            'message' => 'Status updated',
            'data' => [
                'is_online' => (bool) $user->is_online,
                'is_busy' => (bool) $user->is_busy,
                'last_seen' => (int) ($user->last_seen ?? time()) * 1000
            ]
        ]);
    }

    /**
     * Update online datetime for MALE users
     * This endpoint allows MALE users to update their online_datetime
     * to indicate they are available to receive calls from FEMALE users
     */
    public function updateOnlineDateTime(Request $request)
    {
        $user = $request->user();
        
        // ✅ Only MALE users can update online_datetime
        if ($user->user_type !== 'MALE') {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'FORBIDDEN',
                    'message' => 'Only MALE users can update online datetime'
                ]
            ], 403);
        }

        // Update online_datetime to current datetime
        $user->update([
            'online_datetime' => now()
        ]);

        // Convert to IST (Indian Standard Time) for display
        $localTime = $user->online_datetime->setTimezone('Asia/Kolkata');

        return response()->json([
            'success' => true,
            'message' => 'Online datetime updated successfully',
            'data' => [
                'online_datetime' => $localTime->format('Y-m-d H:i:s'), // ✅ Formatted time (same as formatted_time)
                'online_datetime_utc' => $user->online_datetime->toIso8601String(), // UTC time for reference
                'online_datetime_timestamp' => $user->online_datetime->timestamp * 1000, // Convert to milliseconds for JavaScript compatibility
                'formatted_time' => $localTime->format('Y-m-d H:i:s'), // ✅ Local time formatted
                'time_ago' => $user->online_datetime->diffForHumans()
            ]
        ]);
    }

    /**
     * Update call availability (Female only)
     */
    public function updateCallAvailability(Request $request)
    {
        $user = $request->user();
        
        // DEBUG: Log user gender info for troubleshooting
        \Log::info('📱 updateCallAvailability check:', [
            'user_id' => $user->id,
            'user_type' => $user->user_type,
            'gender' => $user->gender,
            'is_female' => $this->isFemaleUser($user)
        ]);
        
        if (!$this->isFemaleUser($user)) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'FORBIDDEN',
                    'message' => 'Only female users can update call availability',
                    'debug' => [
                        'user_type' => $user->user_type,
                        'gender' => $user->gender
                    ]
                ]
            ], 403);
        }

        $validator = Validator::make($request->all(), [
            // Support both payload formats:
            // - { call_availability: "AVAILABLE|UNAVAILABLE|IN_CALL" }
            // - { audio_call_enabled: true/false, video_call_enabled: true/false }
            'call_availability' => 'sometimes|nullable|in:AVAILABLE,UNAVAILABLE,IN_CALL',
            'audio_call_enabled' => 'sometimes|nullable|boolean',
            'video_call_enabled' => 'sometimes|nullable|boolean'
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

        $updateData = [];
        
        if ($request->filled('call_availability')) {
            $callAvailability = strtoupper(trim((string) $request->input('call_availability')));
            if ($callAvailability === 'AVAILABLE') {
                $updateData['audio_call_enabled'] = true;
                $updateData['video_call_enabled'] = true;
                $updateData['is_busy'] = false;
            } elseif ($callAvailability === 'UNAVAILABLE') {
                $updateData['audio_call_enabled'] = false;
                $updateData['video_call_enabled'] = false;
                $updateData['is_busy'] = false;
            } elseif ($callAvailability === 'IN_CALL') {
                $updateData['is_busy'] = true;
            }
        } else {
            // Only update if the field is present and not null
            if ($request->has('audio_call_enabled') && $request->audio_call_enabled !== null) {
                $updateData['audio_call_enabled'] = $request->boolean('audio_call_enabled');
            }
            if ($request->has('video_call_enabled') && $request->video_call_enabled !== null) {
                $updateData['video_call_enabled'] = $request->boolean('video_call_enabled');
            }
        }
        
        // Ensure at least one field is being updated
        if (empty($updateData)) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION_ERROR',
                    'message' => 'call_availability or at least one field (audio_call_enabled/video_call_enabled) must be provided'
                ]
            ], 422);
        }

        $user->update($updateData);

        return response()->json([
            'success' => true,
            'data' => [
                'audio_call_enabled' => (bool) $user->audio_call_enabled,
                'video_call_enabled' => (bool) $user->video_call_enabled,
                'is_busy' => (bool) $user->is_busy
            ],
            'message' => 'Call availability updated'
        ]);
    }

    /**
     * Block user
     */
    public function blockUser(Request $request, $userId)
    {
        $id = str_replace('USR_', '', $userId);
        
        $userToBlock = User::find($id);
        if (!$userToBlock) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'User not found'
                ]
            ], 404);
        }

        // Check if already blocked
        $exists = BlockedUser::where('user_id', $request->user()->id)
                            ->where('blocked_user_id', $id)
                            ->exists();

        if (!$exists) {
            $blockId = 'BLK_' . time() . rand(1000, 9999);
            BlockedUser::create([
                'id' => $blockId,
                'user_id' => $request->user()->id,
                'blocked_user_id' => $id
            ]);
        }

        return response()->json([
            'success' => true,
            'message' => 'User blocked successfully'
        ]);
    }

    /**
     * Unblock user
     */
    public function unblockUser(Request $request, $userId)
    {
        $id = str_replace('USR_', '', $userId);
        
        BlockedUser::where('user_id', $request->user()->id)
                  ->where('blocked_user_id', $id)
                  ->delete();

        return response()->json([
            'success' => true,
            'message' => 'User unblocked successfully'
        ]);
    }

    /**
     * Get blocked users
     */
    public function getBlockedUsers(Request $request)
    {
        $blockedUsers = BlockedUser::where('user_id', $request->user()->id)
                                  ->with('blockedUser')
                                  ->get();

        return response()->json([
            'success' => true,
            'blocked_users' => $blockedUsers->map(function($block) {
                return [
                    'id' => $block->blocked_user_id,  // ID already has USR_ prefix
                    'name' => $block->blockedUser->name,
                    'profile_image' => $block->blockedUser->profile_image,
                    'blocked_at' => $block->blocked_at
                ];
            })
        ]);
    }

    /**
     * Format user response
     */
    private function formatUserResponse($user, $detailed = false, $callRates = null)
    {
        $data = [
            'id' => $user->id,  // ID already has USR_ prefix in database
            'name' => $user->name,
            'username' => $user->username,
            'age' => $user->age,
            'gender' => $user->user_type,
            'profile_image' => $user->profile_image,
            'bio' => $user->bio,
            'language' => $user->language,
            'interests' => $user->interests ? json_decode($user->interests) : [],
            'is_online' => $user->is_online,
            'last_seen' => $user->last_seen ? (int)$user->last_seen * 1000 : null,
            'rating' => round($user->rating, 1),
            'total_ratings' => $user->total_ratings ?? 0,
        ];

        // Add online_datetime for MALE users (formatted in IST)
        if ($user->user_type === 'MALE' && $user->online_datetime) {
            $localTime = $user->online_datetime->setTimezone('Asia/Kolkata');
            $data['online_datetime'] = $localTime->format('Y-m-d H:i:s'); // ✅ Formatted time
        }

        if ($detailed) {
            $data['phone'] = $user->phone;
            $data['coin_balance'] = $user->coin_balance;
            $data['total_earnings'] = $user->total_earnings;
            $data['audio_call_enabled'] = $user->audio_call_enabled;
            $data['video_call_enabled'] = $user->video_call_enabled;
            $data['is_verified'] = $user->is_verified ?? false;
            $data['kyc_status'] = $user->kyc_status ?? 'PENDING';
            $data['created_at'] = $user->created_at->toIso8601String();
            
            // Get primary bank account details
            $primaryBankAccount = $user->bankAccounts()->where('is_primary', true)->first();
            $data['upi_id'] = $primaryBankAccount->upi_id ?? '';
            $data['pancard_name'] = $primaryBankAccount->pancard_name ?? '';
            $data['pancard_number'] = $primaryBankAccount->pancard_number ?? '';
        } else {
            $data['audio_call_enabled'] = $user->audio_call_enabled;
            $data['video_call_enabled'] = $user->video_call_enabled;
            $data['is_verified'] = $user->is_verified ?? false;
            
            // Add call rates if provided (for female user listings)
            if ($callRates && $user->user_type === 'FEMALE') {
                $data['audio_call_rate'] = $callRates['audio_call_rate'];
                $data['video_call_rate'] = $callRates['video_call_rate'];
            }
        }

        return $data;
    }

    /**
     * Check if username is available
     */
    public function checkUsernameAvailability(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'username' => 'required|string|min:4|max:10|regex:/^[a-zA-Z0-9]+$/'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION_ERROR',
                    'message' => 'Invalid username format',
                    'details' => $validator->errors()
                ]
            ], 422);
        }

        $username = $request->username;
        $currentUser = $request->user();
        
        // Check if username exists (excluding current user)
        $exists = User::where('username', $username)
            ->where('id', '!=', $currentUser->id)
            ->exists();

        return response()->json([
            'success' => true,
            'data' => !$exists // true if available, false if taken
        ]);
    }

    /**
     * Update FCM token for push notifications
     */
    public function updateFcmToken(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'fcm_token' => 'required|string'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION_ERROR',
                    'message' => 'FCM token is required',
                    'details' => $validator->errors()
                ]
            ], 422);
        }

        $user = $request->user();
        $user->fcm_token = $request->fcm_token;
        $user->save();

        \Log::info('✅ FCM token updated for user: ' . $user->id);

        return response()->json([
            'success' => true,
            'message' => 'FCM token updated successfully'
        ]);
    }

    /**
     * Delete user account
     * Soft deletes the user and revokes all tokens
     */
    public function deleteAccount(Request $request)
    {
        $user = $request->user();
        
        \Log::info('🗑️ Account deletion requested for user: ' . $user->id);
        
        try {
            \DB::beginTransaction();
            
            // Revoke all tokens (logout from all devices)
            $user->tokens()->delete();
            
            // Soft delete the user (keeps data for compliance/recovery if needed)
            // The User model uses SoftDeletes trait
            $user->delete();
            
            \DB::commit();
            
            \Log::info('✅ Account deleted successfully for user: ' . $user->id);
            
            return response()->json([
                'success' => true,
                'message' => 'Account deleted successfully'
            ]);
            
        } catch (\Exception $e) {
            \DB::rollBack();
            \Log::error('❌ Account deletion failed for user: ' . $user->id . ' - ' . $e->getMessage());
            
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to delete account. Please try again.'
                ]
            ], 500);
        }
    }

    /**
     * Get call rates from app settings
     */
    private function getCallRates()
    {
        $audioRate = AppSetting::where('setting_key', 'audio_call_rate')->first();
        $videoRate = AppSetting::where('setting_key', 'video_call_rate')->first();

        return [
            'audio_call_rate' => $audioRate ? (int) $audioRate->setting_value : 10,
            'video_call_rate' => $videoRate ? (int) $videoRate->setting_value : 15,
        ];
    }
}

