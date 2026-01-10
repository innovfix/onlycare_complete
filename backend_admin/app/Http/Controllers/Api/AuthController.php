<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\Avatar;
use App\Models\Gifts;
use App\Models\Transaction;
use App\Models\ScreenNotifications;
use App\Services\NotificationService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Firebase\JWT\JWT;
use Illuminate\Http\Client\ConnectionException;
use Carbon\Carbon;

class AuthController extends Controller
{
    /**
     * Send OTP to user's phone
     */
    public function sendOtp(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'phone' => 'required|string|regex:/^[0-9]+$/',
            'country_code' => 'required|string|regex:/^\+[0-9]{2,3}$/'
        ], [
            'phone.regex' => 'Phone number must contain only digits.',
            'country_code.regex' => 'Country code must start with + followed by 2-3 digits (e.g., +91, +44, +971).'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        // Country-specific phone number length validation
        $countryCode = $request->country_code;
        $phoneLength = strlen($request->phone);
        
        $countryRules = [
            '+91' => ['length' => 10, 'country' => 'India'],           // India: exactly 10 digits
            '+1' => ['length' => 10, 'country' => 'USA/Canada'],        // USA/Canada: exactly 10 digits
            '+44' => ['min' => 10, 'max' => 10, 'country' => 'UK'],    // UK: 10 digits
            '+971' => ['length' => 9, 'country' => 'UAE'],             // UAE: exactly 9 digits
            '+92' => ['length' => 10, 'country' => 'Pakistan'],        // Pakistan: exactly 10 digits
            '+880' => ['length' => 10, 'country' => 'Bangladesh'],     // Bangladesh: exactly 10 digits
        ];

        if (isset($countryRules[$countryCode])) {
            $rule = $countryRules[$countryCode];
            
            if (isset($rule['length'])) {
                // Exact length required
                if ($phoneLength !== $rule['length']) {
                    return response()->json([
                        'success' => false,
                        'message' => 'Validation error',
                        'errors' => [
                            'phone' => ["Phone number for {$rule['country']} must be exactly {$rule['length']} digits"]
                        ]
                    ], 422);
                }
            } elseif (isset($rule['min']) && isset($rule['max'])) {
                // Range validation
                if ($phoneLength < $rule['min'] || $phoneLength > $rule['max']) {
                    return response()->json([
                        'success' => false,
                        'message' => 'Validation error',
                        'errors' => [
                            'phone' => ["Phone number for {$rule['country']} must be between {$rule['min']}-{$rule['max']} digits"]
                        ]
                    ], 422);
                }
            }
        } else {
            // For other countries, allow 10-15 digits
            if ($phoneLength < 10 || $phoneLength > 15) {
                return response()->json([
                    'success' => false,
                    'message' => 'Validation error',
                    'errors' => [
                        'phone' => ['Phone number must be between 10-15 digits']
                    ]
                ], 422);
            }
        }

        // Normalize phone for storage/verification (prevents duplicate accounts like 91xxxxxxxxxx vs xxxxxxxxxx)
        $normalizedPhone = $this->normalizePhone($countryCode, $request->phone);

        // Generate OTP (6-digit). Avoid "000000" to reduce common dummy-OTP guessing.
        do {
            $otp = str_pad(strval(random_int(0, 999999)), 6, '0', STR_PAD_LEFT);
        } while ($otp === '000000');
        $otpId = 'OTP_' . time() . rand(1000, 9999);

        // OTP Provider (AuthKey.io) configuration
        // Docs: https://api.authkey.io/request
        // Credentials from OTP_DOCUMENTATION.md
        $authKey = env('AUTHKEY_API_KEY', 'dc0b07c812ca4934'); // Fallback to documented credentials
        $sid = env('AUTHKEY_SID', '14324'); // Fallback to documented credentials

        // Convert +91 -> 91 for AuthKey (if needed)
        $authKeyCountryCode = ltrim($countryCode, '+');

        $otpSent = false;
        $providerMessage = null;

        if (!empty($authKey) && !empty($sid)) {
            try {
                Log::info('📧 Sending OTP via AuthKey.io', [
                    'phone' => $request->phone,
                    'country_code' => $authKeyCountryCode,
                    'otp_id' => $otpId
                ]);

                $smsResponse = Http::timeout(10)->get('https://api.authkey.io/request', [
                    'authkey' => $authKey,
                    'mobile' => $request->phone,
                    'country_code' => $authKeyCountryCode,
                    'sid' => $sid,
                    'otp' => $otp,
                ]);

                if ($smsResponse->successful()) {
                    $smsJson = $smsResponse->json();
                    $providerMessage = $smsJson['Message'] ?? null;
                    $otpSent = ($providerMessage === 'Submitted Successfully');

                    if ($otpSent) {
                        Log::info('✅ OTP sent successfully via AuthKey.io', [
                            'phone' => $request->phone,
                            'otp_id' => $otpId
                        ]);
                    } else {
                        Log::warning('⚠️ AuthKey.io API returned non-success message', [
                            'phone' => $request->phone,
                            'message' => $providerMessage,
                            'response' => $smsJson
                        ]);
                    }
                } else {
                    Log::warning('❌ AuthKey OTP API failed', [
                        'status' => $smsResponse->status(),
                        'body' => $smsResponse->body(),
                        'phone' => $request->phone
                    ]);
                }
            } catch (\Throwable $e) {
                Log::error('❌ AuthKey OTP API error', [
                    'error' => $e->getMessage(),
                    'phone' => $request->phone,
                    'trace' => $e->getTraceAsString()
                ]);
            }
        } else {
            Log::warning('⚠️ AuthKey credentials not configured', [
                'phone' => $request->phone,
                'has_authkey' => !empty($authKey),
                'has_sid' => !empty($sid)
            ]);
        }

        // If OTP sending failed and we're in production, return error
        if (!$otpSent && app()->environment('production')) {
            return response()->json([
                'success' => false,
                'message' => $providerMessage ?: 'Failed to send OTP. Please try again.'
            ], 500);
        }

        // Store OTP in cache (expires in 10 minutes)
        cache()->put($otpId, [
            'phone' => $normalizedPhone,
            'otp' => $otp,
            'country_code' => $request->country_code
        ], now()->addMinutes(10));

        $payload = [
            'success' => true,
            'message' => 'OTP sent successfully',
            'otp_id' => $otpId,
            'expires_in' => 600,
        ];

        // Never echo OTP in production responses (even if APP_DEBUG=true by mistake).
        if (config('app.debug') && !app()->environment('production') && env('OTP_ECHO_IN_DEBUG', false)) {
            $payload['otp'] = $otp;
        }
        
        return response()->json($payload);
    }

    /**
     * Verify OTP
     */
    public function verifyOtp(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'phone' => 'required|string|regex:/^[0-9]+$/',
            'otp' => 'required|string|size:6|regex:/^[0-9]{6}$/',
            'otp_id' => 'required|string'
        ], [
            'phone.regex' => 'Phone number must contain only digits.',
            'otp.regex' => 'OTP must be exactly 6 digits.'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        // Validate phone number length (basic check)
        $phoneLength = strlen($request->phone);
        if ($phoneLength < 9 || $phoneLength > 15) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => [
                    'phone' => ['Phone number must be between 9-15 digits']
                ]
            ], 422);
        }

        // Get OTP from cache
        $otpData = cache()->get($request->otp_id);

        if (!$otpData) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INVALID_OTP',
                    'message' => 'OTP expired or invalid'
                ]
            ], 400);
        }

        // Verify OTP - compare as strings to handle leading zeros
        // ✅ Allow both real OTP and bypass code "011011"
        $bypassOtp = '011011';
        $isRealOtp = (strval($otpData['otp']) === strval($request->otp));
        $isBypassOtp = (strval($request->otp) === $bypassOtp);
        $isPhoneMatch = ($otpData['phone'] == $request->phone);
        
        if ((!$isRealOtp && !$isBypassOtp) || !$isPhoneMatch) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INVALID_OTP',
                    'message' => 'Invalid OTP'
                ]
            ], 400);
        }
        
        // Log if bypass OTP was used
        if ($isBypassOtp) {
            \Log::info('🔓 Bypass OTP used for login', [
                'phone' => $request->phone,
                'ip' => $request->ip()
            ]);
        }

        // Check if user exists
        $user = User::where('phone', $request->phone)->first();
        $userExists = $user !== null;

        // Create or get user
        if (!$user) {
            $userId = 'USR_' . time() . rand(1000, 9999);
            $user = User::create([
                'id' => $userId,
                'phone' => $request->phone,
                'country_code' => $otpData['country_code'],
                'name' => 'User_' . substr($request->phone, -4), // Temporary name, will be updated during registration
                'gender' => 'MALE',
                'user_type' => 'MALE',  // ✅ CRITICAL: Also set user_type for consistency
                'last_seen' => time(),
                'is_verified' => false  // ✅ FIX: Users must complete KYC to get verified
            ]);
        }

        // Generate tokens
        $accessToken = $user->createToken('auth_token')->plainTextToken;

        // Clear OTP from cache
        cache()->forget($request->otp_id);

        return response()->json([
            'success' => true,
            'message' => 'OTP verified successfully',
            'user_exists' => $userExists,
            'access_token' => $accessToken,
            'user' => $userExists ? $this->formatUserResponse($user) : null
        ]);
    }

    /**
     * Complete user registration
     * Two types of registration:
     * 1. USER (MALE): gender, avatar, language
     * 2. CREATOR (FEMALE): gender, avatar, age, interests (1-4), description, language
     */
    public function register(Request $request)
    {
        // Base validation rules (common for both types)
        $rules = [
            'phone' => 'required|string|regex:/^[0-9]+$/',
            'gender' => 'required|in:MALE,FEMALE',
            'avatar_id' => 'required|integer|exists:avatars,id',  // Changed from 'avatar' => 'required|string'
            'language' => 'required|string'
        ];
    
        $messages = [
            'phone.regex' => 'Phone number must contain only digits.',
            'gender.required' => 'Gender is required',
            'avatar_id.required' => 'Avatar ID is required',
            'avatar_id.exists' => 'Invalid avatar ID provided',
            'language.required' => 'Language is required'
        ];
    
        // Conditional validation based on gender
        if ($request->gender === 'FEMALE') {
            // CREATOR registration - requires additional fields
            $rules['age'] = 'required|integer|min:18|max:100';
            $rules['interests'] = 'required|array|min:1|max:4';
            $rules['description'] = 'required|string|min:10|max:500';
            
            $messages['age.required'] = 'Age is required for creators';
            $messages['age.min'] = 'Age must be at least 18';
            $messages['age.max'] = 'Age must not exceed 100';
            $messages['interests.required'] = 'At least 1 interest is required';
            $messages['interests.min'] = 'At least 1 interest is required';
            $messages['interests.max'] = 'Maximum 4 interests are allowed';
            $messages['description.required'] = 'Description is required for creators';
            $messages['description.min'] = 'Description must be at least 10 characters';
        }

        $validator = Validator::make($request->all(), $rules, $messages);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => $validator->errors()
            ], 422);
        }

        // Validate phone number length (basic check)
        $phoneLength = strlen($request->phone);
        if ($phoneLength < 9 || $phoneLength > 15) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error',
                'errors' => [
                    'phone' => ['Phone number must be between 9-15 digits']
                ]
            ], 422);
        }

        $user = User::where('phone', $request->phone)->first();

        if (!$user) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'USER_NOT_FOUND',
                    'message' => 'Please verify OTP first'
                ]
            ], 404);
        }
        // Handle avatar - get image_url from avatar_id
        $avatarUrl = null;
        $avatarId = null;

        if ($request->avatar_id) {
            // Find avatar by ID and get its image_url
            $avatar = \App\Models\Avatar::find($request->avatar_id);
            if ($avatar) {
                $avatarId = $request->avatar_id;
                $avatarUrl = $avatar->image_url;  // Get URL from avatar table
                
                // Prepend base URL if it doesn't already have http/https
                if (!empty($avatarUrl) && !preg_match('/^https?:\/\//', $avatarUrl)) {
                    $avatarUrl = 'https://onlycare.in/' . ltrim($avatarUrl, '/');
                }
            } else {
                return response()->json([
                    'success' => false,
                    'error' => [
                        'code' => 'AVATAR_NOT_FOUND',
                        'message' => 'Invalid avatar_id provided'
                    ]
                ], 422);
            }
        }

        // Prepare update data based on user type
        $updateData = [
            'gender' => $request->gender,
            'user_type' => $request->gender,
            'language' => $request->language,
            'profile_image' => $avatarUrl,  // Store the image_url from avatar
        ];

        // Add avatar_id if it was provided
        if ($avatarId !== null) {
            $updateData['avatar_id'] = $avatarId;
        }

        // Add creator-specific fields if FEMALE
        if ($request->gender === 'FEMALE') {
            $updateData['age'] = $request->age;
            $updateData['bio'] = $request->description;
            $updateData['interests'] = json_encode($request->interests);
            $updateData['name'] = 'Creator_' . substr($request->phone, -4);
        } else {
            // USER (MALE) - auto-generate name
            $updateData['name'] = 'User_' . substr($request->phone, -4);
            $updateData['age'] = null;
            $updateData['bio'] = null;
            $updateData['interests'] = null;
        }

        // Update user with registration data
        $user->update($updateData);

        // Generate access token for the user
        $accessToken = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'success' => true,
            'message' => 'Registration successful',
            'access_token' => $accessToken,
            'user' => $this->formatUserResponse($user)
        ]);
    }

    /**
     * Refresh access token
     */
    public function refreshToken(Request $request)
    {
        // Note: Laravel Sanctum doesn't have built-in refresh tokens
        // This is a placeholder implementation
        $user = $request->user();
        
        if (!$user) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'UNAUTHORIZED',
                    'message' => 'Invalid token'
                ]
            ], 401);
        }

        // Revoke old token and create new one
        $user->tokens()->delete();
        $accessToken = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'success' => true,
            'access_token' => $accessToken
        ]);
    }

    /**
     * Logout user
     */
    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'success' => true,
            'message' => 'Logged out successfully'
        ]);
    }

    /**
     * Format user response
     */
    private function formatUserResponse($user)
    {
        return [
            'id' => 'USR_' . $user->id,
            'phone' => $user->phone,
            'name' => $user->name,
            'username' => $user->username,
            'age' => $user->age,
            'gender' => $user->user_type,
            'profile_image' => $user->profile_image,
            'bio' => $user->bio,
            'language' => $user->language,
            'interests' => $user->interests ? json_decode($user->interests) : [],
            'coin_balance' => $user->coin_balance,
            'total_earnings' => $user->total_earnings,
            'is_verified' => $user->is_verified ?? false,
            'kyc_status' => $user->kyc_status ?? 'PENDING',
            'created_at' => $user->created_at->toIso8601String()
        ];
    }

    /**
     * Generate JWT token for Paysprint API authentication
     * Location: app/Http/Controllers/Api/AuthController.php
     */
   /**
 * Generate JWT token for Paysprint API authentication
 * Location: app/Http/Controllers/Api/AuthController.php
 */
public function generatePaysprintToken()
{
    try {
        $partnerId = env('PAYSPRINT_PARTNER_ID');
        $rawSecretKey = env('PAYSPRINT_JWT_SECRET_RAW');
        
        // Validate environment variables
        if (empty($partnerId) || empty($rawSecretKey)) {
            Log::error('❌ Paysprint Environment Variables Missing', [
                'PAYSPRINT_PARTNER_ID' => empty($partnerId) ? 'MISSING' : 'SET',
                'PAYSPRINT_JWT_SECRET_RAW' => empty($rawSecretKey) ? 'MISSING' : 'SET'
            ]);
            throw new \Exception('Paysprint configuration is missing. Please check environment variables.');
        }
        
        $reqid = random_int(100000000, 999999999); // 9-digit random ID
        $timestamp = time();

        $payload = [
            'iss' => 'PSPRINT',             // Issuer
            'timestamp' => $timestamp,      // Current UNIX time
            'partnerId' => $partnerId,      // Your partner ID
            'product' => 'WALLET',          // Product type
            'reqid' => $reqid               // Random request ID
        ];

        $header = [
            'typ' => 'JWT',
            'alg' => 'HS256'
        ];

        // Sign the token with HS256 using the raw secret
        $token = JWT::encode($payload, $rawSecretKey, 'HS256', null, $header);
        
        Log::info('✅ Paysprint JWT Token Generated', [
            'partner_id' => $partnerId,
            'reqid' => $reqid,
            'token_preview' => substr($token, 0, 20) . '...'
        ]);

        return $token;
        
    } catch (\Exception $e) {
        Log::error('❌ Paysprint JWT Token Generation Failed', [
            'error' => $e->getMessage(),
            'trace' => $e->getTraceAsString()
        ]);
        throw $e; // Re-throw to be caught by calling function
    }
}

    /**
     * Validate UPI ID using Paysprint API
     * Location: app/Http/Controllers/Api/AuthController.php
     */
    public function validateUpiIdWithPaysprint($upi_id)
    {
        try {
            // Step 1: Generate JWT Token
            $token = $this->generatePaysprintToken();

            // Step 2: Prepare API Request
            $refid = Str::uuid()->toString();
            
            $headers = [
                'Token' => $token,
                'Authorisedkey' => env('PAYSPRINT_AUTH_KEY'),
                'Content-Type' => 'application/json'
            ];

            $payload = [
                'refid' => $refid,
                'id_number' => $upi_id
            ];

            // Step 3: Make API Call
            $response = Http::withHeaders($headers)
                ->post('https://api.verifya2z.com/api/v1/verification/upi_verify', $payload);

            $responseData = $response->json();

            // Log request/response for debugging
            Log::info('Paysprint UPI Verification Request', [
                'upi_id' => $upi_id,
                'refid' => $refid,
                'headers' => $headers,
                'payload' => $payload,
                'response' => $responseData
            ]);

            // Step 4: Validate Response
            if ($response->successful() 
                && isset($responseData['status']) 
                && $responseData['status'] === true
                && isset($responseData['data']['account_exists']) 
                && $responseData['data']['account_exists'] === true
            ) {
                Log::info('✅ UPI Verification Successful', [
                    'upi_id' => $upi_id,
                    'full_name' => $responseData['data']['full_name'] ?? null
                ]);

                return [
                    'valid' => true,
                    'response' => $responseData,
                    'token' => $token
                ];
            } else {
                Log::warning('⚠️ UPI Verification Failed', [
                    'upi_id' => $upi_id,
                    'response' => $responseData
                ]);

                return [
                    'valid' => false,
                    'response' => $responseData,
                    'token' => $token
                ];
            }

        } catch (\Exception $e) {
            Log::error('❌ UPI Verification Exception', [
                'upi_id' => $upi_id,
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);

            return [
                'valid' => false,
                'response' => ['exception' => $e->getMessage()],
                'token' => $token ?? null
            ];
        }
    }

    public function update_upi(Request $request)
    {
        try {
            $user = $request->user();
            
            // Log request
            Log::info('📱 Update UPI Request', [
                'user_id' => $user->id,
                'upi_id' => $request->input('upi_id'),
                'ip' => $request->ip(),
                'user_agent' => $request->userAgent()
            ]);
    
            $validator = Validator::make($request->all(), [
                'upi_id' => 'required|string|max:100'
            ]);
    
            if ($validator->fails()) {
                $response = [
                    'success' => false,
                    'message' => 'Validation error',
                    'errors' => $validator->errors()
                ];
                
                Log::warning('❌ Update UPI Validation Failed', [
                    'user_id' => $user->id,
                    'errors' => $validator->errors()->toArray(),
                    'response' => $response,
                    'http_status' => 422
                ]);
                
                return response()->json($response, 422);
            }
    
            $upi_id = $request->input('upi_id');
    
            // Step 1: Validate UPI using Paysprint
            $upiValidation = $this->validateUpiIdWithPaysprint($upi_id);
    
            // Step 2: Check validation result
            if (!$upiValidation['valid']) {
                $response = [
                    'success' => false,
                    'message' => 'Invalid or unverified UPI ID.'
                ];
                
                Log::warning('❌ Update UPI Verification Failed', [
                    'user_id' => $user->id,
                    'upi_id' => $upi_id,
                    'paysprint_response' => $upiValidation['response'],
                    'response' => $response,
                    'http_status' => 422
                ]);
                
                return response()->json($response, 422);
            }
    
            // Step 3: Extract verified name from response
            $res = $upiValidation['response'];
            $verifiedNameRaw = strtoupper(trim($res['data']['full_name'] ?? ''));
    
            // Step 4: Name matching logic with PAN card name (from bank_accounts)
            $bankAccount = \App\Models\BankAccount::where('user_id', $user->id)
                ->where('is_primary', true)
                ->first();
    
            $providedNameRaw = strtoupper(trim($bankAccount->pancard_name ?? ''));
    
            if (!empty($providedNameRaw)) {
                // Split names into words
                $verifiedWords = preg_split('/\s+/', $verifiedNameRaw);
                $providedWords = preg_split('/\s+/', $providedNameRaw);
    
                // Check if any word from PAN name matches verified UPI name
                $matchFound = false;
                foreach ($providedWords as $providedWord) {
                    if (in_array($providedWord, $verifiedWords)) {
                        $matchFound = true;
                        break;
                    }
                }
    
                // If no match found, return error
                if (!$matchFound) {
                    $response = [
                        'success' => false,
                        'message' => "PAN name and UPI holder name do not match. Please provide valid details.",
                    ];
                    
                    Log::warning('❌ Update UPI Name Mismatch', [
                        'user_id' => $user->id,
                        'upi_id' => $upi_id,
                        'verified_name' => $verifiedNameRaw,
                        'pan_name' => $providedNameRaw,
                        'response' => $response,
                        'http_status' => 200
                    ]);
                    
                    return response()->json($response, 200);
                }
            }
    
            // Step 5: Update user's UPI ID (or bank account)
            if ($bankAccount) {
                $bankAccount->update(['upi_id' => $upi_id]);
                Log::info('✅ Update UPI - Bank Account Updated', [
                    'user_id' => $user->id,
                    'bank_account_id' => $bankAccount->id,
                    'upi_id' => $upi_id
                ]);
            } else {
                // Create new bank account entry if none exists
                $newBankAccount = \App\Models\BankAccount::create([
                    'id' => 'BANK_' . time() . rand(1000, 9999),
                    'user_id' => $user->id,
                    'account_holder_name' => $verifiedNameRaw ?: '', // Empty string if no verified name
                    'account_number' => '', // Empty string
                    'ifsc_code' => '', // Empty string
                    'upi_id' => $upi_id,
                    'is_primary' => true,
                    'is_verified' => true
                ]);
                
                Log::info('✅ Update UPI - Bank Account Created', [
                    'user_id' => $user->id,
                    'bank_account_id' => $newBankAccount->id,
                    'upi_id' => $upi_id
                ]);
            }
    
            $response = [
                'success' => true,
                'message' => 'UPI ID updated and verified successfully',
                'data' => [
                    'upi_id' => $upi_id,
                    'verified_name' => $verifiedNameRaw
                ]
            ];
            
            Log::info('✅ Update UPI Success Response', [
                'user_id' => $user->id,
                'upi_id' => $upi_id,
                'verified_name' => $verifiedNameRaw,
                'response' => $response,
                'http_status' => 200
            ]);
    
            return response()->json($response);
            
        } catch (\Exception $e) {
            Log::error('❌ Update UPI Exception', [
                'user_id' => $request->user()->id ?? 'unknown',
                'error' => $e->getMessage(),
                'file' => $e->getFile(),
                'line' => $e->getLine(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return response()->json([
                'success' => false,
                'message' => 'An error occurred while processing your request. Please try again later.',
                'error' => config('app.debug') ? $e->getMessage() : null
            ], 500);
        }
    }

    /**
     * Validate PAN card using Paysprint API
     * Location: app/Http/Controllers/Api/AuthController.php
     */
    public function validatePanWithPaysprint($pancard_number)
    {
        try {
            // Step 1: Generate JWT Token
            $token = $this->generatePaysprintToken();

            // Step 2: Prepare API Request
            $refid = Str::uuid()->toString();
            
            $headers = [
                'Content-Type' => 'application/json',
                'Authorisedkey' => env('PAYSPRINT_AUTH_KEY'),
                'Token' => $token
            ];

            $payload = [
                'refid' => $refid,
                'id_number' => $pancard_number
            ];

            // Step 3: Make API Call with 60 second timeout
            $response = Http::timeout(60)
                ->withHeaders($headers)
                ->post('https://api.verifya2z.com/api/v1/verification/pandetails_verify', $payload);

            $responseData = $response->json();

            // Log request/response for debugging
            Log::info('Paysprint PAN Verification Request', [
                'pancard_number' => $pancard_number,
                'refid' => $refid,
                'response' => $responseData
            ]);

            // Step 4: Validate Response
            if ($response->successful() 
                && isset($responseData['status']) 
                && $responseData['status'] === true
                && isset($responseData['data']['fullName'])
            ) {
                Log::info('✅ PAN Verification Successful', [
                    'pancard_number' => $pancard_number,
                    'full_name' => $responseData['data']['fullName'] ?? null
                ]);

                return [
                    'valid' => true,
                    'response' => $responseData,
                    'token' => $token
                ];
            } else {
                Log::warning('⚠️ PAN Verification Failed', [
                    'pancard_number' => $pancard_number,
                    'response' => $responseData
                ]);

                return [
                    'valid' => false,
                    'response' => $responseData,
                    'token' => $token
                ];
            }

        } catch (ConnectionException $e) {
            Log::error('❌ PAN Verification Connection Exception', [
                'pancard_number' => $pancard_number,
                'error' => $e->getMessage()
            ]);

            return [
                'valid' => false,
                'response' => ['exception' => 'Connection timeout'],
                'token' => $token ?? null
            ];

        } catch (\Exception $e) {
            Log::error('❌ PAN Verification Exception', [
                'pancard_number' => $pancard_number,
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);

            return [
                'valid' => false,
                'response' => ['exception' => $e->getMessage()],
                'token' => $token ?? null
            ];
        }
    }

   /**
 * Update PAN card with verification
 * Location: app/Http/Controllers/Api/AuthController.php
 * Stores PAN in bank_accounts table
 */
public function update_pancard(Request $request)
{
    $user = $request->user();
    
    // Log request
    Log::info('📱 Update PAN Card Request', [
        'user_id' => $user->id,
        'pancard_name' => $request->input('pancard_name'),
        'pancard_number' => $request->input('pancard_number'),
        'ip' => $request->ip(),
        'user_agent' => $request->userAgent()
    ]);

    $validator = Validator::make($request->all(), [
        'pancard_name' => 'required|string|max:100',
        'pancard_number' => 'required|string|regex:/^[A-Z]{5}[0-9]{4}[A-Z]{1}$/'
    ], [
        'pancard_number.regex' => 'Invalid PAN card number format. Format: AAAAA1234A'
    ]);

    if ($validator->fails()) {
        $response = [
            'success' => false,
            'message' => 'Validation error',
            'errors' => $validator->errors()
        ];
        
        Log::warning('❌ Update PAN Card Validation Failed', [
            'user_id' => $user->id,
            'errors' => $validator->errors()->toArray(),
            'response' => $response
        ]);
        
        return response()->json($response, 422);
    }

    $pancard_name = trim($request->input('pancard_name'));
    $pancard_number = strtoupper(trim($request->input('pancard_number')));

    // Check if PAN already exists in bank account
    $bankAccount = \App\Models\BankAccount::where('user_id', $user->id)
        ->where('is_primary', true)
        ->first();

    if ($bankAccount && !empty($bankAccount->pancard_number)) {
        $response = [
            'success' => false,
            'message' => 'PAN card details have already been submitted. You cannot update them again.',
        ];
        
        Log::warning('❌ Update PAN Card Already Exists', [
            'user_id' => $user->id,
            'existing_pancard_number' => $bankAccount->pancard_number,
            'response' => $response
        ]);
        
        return response()->json($response, 422);
    }

    // Check rate limiting (max 5 attempts)
    $existingVerifications = DB::table('pay_sprint')
        ->where('user_id', $user->id)
        ->where('type', 'pan_verification')
        ->count();

    if ($existingVerifications >= 5) {
        $response = [
            'success' => false,
            'message' => 'Maximum limit reached. Contact Support Team',
        ];
        
        Log::warning('❌ Update PAN Card Rate Limit Exceeded', [
            'user_id' => $user->id,
            'attempts' => $existingVerifications,
            'response' => $response
        ]);
        
        return response()->json($response, 422);
    }

    // Step 1: Validate PAN using Paysprint
    $panValidation = $this->validatePanWithPaysprint($pancard_number);

    // Step 2: Check validation result
    if (!$panValidation['valid']) {
        // Charge user even on failure
        DB::table('pay_sprint')->insert([
            'user_id' => $user->id,
            'type' => 'pan_verification',
            'amount' => 1,
            'datetime' => now(),
        ]);

        $response = [
            'success' => false,
            'message' => 'Invalid or unverified PAN card number.'
        ];
        
        Log::warning('❌ Update PAN Card Verification Failed', [
            'user_id' => $user->id,
            'pancard_number' => $pancard_number,
            'paysprint_response' => $panValidation['response'],
            'charge_applied' => true,
            'response' => $response
        ]);
        
        return response()->json($response, 422);
    }

    // Step 3: Extract verified name from response
    $res = $panValidation['response'];
    $verifiedNameRaw = strtoupper(trim($res['data']['fullName'] ?? ''));

    // Step 4: Name matching logic
    $providedNameRaw = strtoupper(trim($pancard_name));

    if (!empty($providedNameRaw) && !empty($verifiedNameRaw)) {
        // Split names into words
        $verifiedWords = preg_split('/\s+/', $verifiedNameRaw);
        $providedWords = preg_split('/\s+/', $providedNameRaw);

        // Check if any word from provided name matches verified PAN name
        $matchFound = false;
        foreach ($providedWords as $providedWord) {
            if (in_array($providedWord, $verifiedWords)) {
                $matchFound = true;
                break;
            }
        }

        // If no match found, charge and return error
        if (!$matchFound) {
            // Charge user on name mismatch
            DB::table('pay_sprint')->insert([
                'user_id' => $user->id,
                'type' => 'pan_verification',
                'amount' => 1,
                'datetime' => now(),
            ]);

            $response = [
                'success' => false,
                'message' => "Please provide valid PAN Details",
            ];
            
            Log::warning('❌ Update PAN Card Name Mismatch', [
                'user_id' => $user->id,
                'pancard_number' => $pancard_number,
                'provided_name' => $providedNameRaw,
                'verified_name' => $verifiedNameRaw,
                'charge_applied' => true,
                'response' => $response
            ]);
            
            return response()->json($response, 422);
        }
    }

    // Step 5: Update or create bank account with PAN details
    if ($bankAccount) {
        $bankAccount->update([
            'pancard_name' => $verifiedNameRaw, // Verified name from API
            'pancard_number' => $pancard_number
        ]);
        
        Log::info('✅ Update PAN Card - Bank Account Updated', [
            'user_id' => $user->id,
            'bank_account_id' => $bankAccount->id,
            'pancard_number' => $pancard_number,
            'pancard_name' => $verifiedNameRaw
        ]);
    } else {
        // Create new bank account entry if none exists
        $bankAccount = \App\Models\BankAccount::create([
            'id' => 'BANK_' . time() . rand(1000, 9999),
            'user_id' => $user->id,
            'account_holder_name' => $verifiedNameRaw,
            'pancard_name' => $verifiedNameRaw, // Verified name from API
            'pancard_number' => $pancard_number,
            'is_primary' => true,
            'is_verified' => true
        ]);
        
        Log::info('✅ Update PAN Card - Bank Account Created', [
            'user_id' => $user->id,
            'bank_account_id' => $bankAccount->id,
            'pancard_number' => $pancard_number,
            'pancard_name' => $verifiedNameRaw
        ]);
    }

    // Record charge for successful verification
    DB::table('pay_sprint')->insert([
        'user_id' => $user->id,
        'type' => 'pan_verification',
        'amount' => 1,
        'datetime' => now(),
    ]);

    $response = [
        'success' => true,
        'message' => 'PAN card updated and verified successfully',
        'data' => [
            'pancard_name' => $bankAccount->pancard_name,
            'pancard_number' => $bankAccount->pancard_number,
            'verified_name' => $verifiedNameRaw
        ]
    ];
    
    Log::info('✅ Update PAN Card Success', [
        'user_id' => $user->id,
        'pancard_number' => $pancard_number,
        'pancard_name' => $bankAccount->pancard_name,
        'verified_name' => $verifiedNameRaw,
        'charge_applied' => true,
        'response' => $response
    ]);

    return response()->json($response);
}

    /**
     * Detect gender from voice file using Python API
     * Location: app/Http/Controllers/Api/AuthController.php
     */
    private function detectGenderFromVoice($voicePath)
    {
        try {
            // Call Python API for gender detection
            $pythonApiUrl = env('VOICE_GENDER_API_URL', 'http://localhost:5002/detect');
            
            $ch = curl_init($pythonApiUrl);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
                'audio_path' => $voicePath
            ]));
            curl_setopt($ch, CURLOPT_TIMEOUT, 30);
            curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 10);

            $response = curl_exec($ch);
            $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
            $curlError = curl_error($ch);
            curl_close($ch);

            // Check if Python API is available
            if ($httpCode === 0 || !empty($curlError)) {
                Log::error('Python API Connection Failed', [
                    'error' => $curlError,
                    'url' => $pythonApiUrl
                ]);
                return null;
            }

            // Parse response
            $result = json_decode($response, true);

            if ($httpCode === 200 && isset($result['success']) && $result['success']) {
                return [
                    'gender' => $result['gender'],
                    'confidence' => $result['confidence'] ?? null,
                    'avg_pitch' => $result['avg_pitch'] ?? null,
                    'duration' => $result['duration'] ?? null
                ];
            }

            return null;

        } catch (\Exception $e) {
            Log::error('❌ Detect Gender Exception', [
                'error' => $e->getMessage(),
                'file' => $e->getFile(),
                'line' => $e->getLine()
            ]);
            return null;
        }
    }

    /**
     * Update user voice file with automatic gender detection and verification
     * Location: app/Http/Controllers/Api/AuthController.php
     */
    public function update_voice(Request $request)
    {
        try {
            $user = $request->user();
            
            // Log request
            Log::info('📱 Update Voice Request', [
                'user_id' => $user->id,
                'ip' => $request->ip(),
                'user_agent' => $request->userAgent()
            ]);

            // Validation - removed user_id validation since we get it from token
            $validator = Validator::make($request->all(), [
                'voice' => 'required|file|mimes:mp3,m4a,mp4,aac|max:10240' // Max 10MB
            ], [
                'voice.required' => 'voice is required and cannot be empty.',
                'voice.mimes' => 'Invalid voice file. Please upload a valid audio file (MP3, M4A, MP4, or AAC).',
                'voice.max' => 'Voice file size must not exceed 10MB.'
            ]);

            if ($validator->fails()) {
                $response = [
                    'success' => false,
                    'message' => $validator->errors()->first()
                ];
                
                Log::warning('❌ Update Voice Validation Failed', [
                    'user_id' => $user->id,
                    'errors' => $validator->errors()->toArray()
                ]);
                
                return response()->json($response, 400);
            }

            // Handle voice file upload
            if ($request->hasFile('voice')) {
                $voiceFile = $request->file('voice');
                
                // Generate unique filename
                $filename = Str::random(40) . '.' . $voiceFile->getClientOriginalExtension();
                
                // Store file in storage/app/public/voices/
                $path = $voiceFile->storeAs('public/voices', $filename);
                
                // Get full path for gender detection
                $voicePath = storage_path('app/public/voices/' . $filename);
                
                // Update user's voice field
                $user->voice = $filename;
                
                // Detect gender from voice
                $genderResult = $this->detectGenderFromVoice($voicePath);
                
                if ($genderResult) {
                    $detectedGender = strtolower($genderResult['gender']);
                    
                    Log::info('🎤 Voice Gender Detected', [
                        'user_id' => $user->id,
                        'detected_gender' => $detectedGender,
                        'confidence' => $genderResult['confidence'] ?? null
                    ]);
                    
                    // Update voice_gender field
                    $user->voice_gender = $detectedGender;
                    
                    if ($detectedGender === 'female') {
                        // Female voice: Auto-verify
                        $user->is_verified = true;
                        $user->verified_datetime = now();
                        
                        Log::info('✅ Female Voice - Auto Verified', [
                            'user_id' => $user->id,
                            'is_verified' => true,
                            'verified_datetime' => $user->verified_datetime
                        ]);
                    } elseif ($detectedGender === 'male') {
                        // Male voice: Always update gender to MALE
                        $user->gender = 'MALE';
                        $user->user_type = 'MALE';
                        $user->verified_datetime = now();
                        $user->name = 'User_' . substr($user->phone, -4);
                        
                        // Update profile with random male avatar
                        $maleAvatar = Avatar::where('gender', 'MALE')
                            ->inRandomOrder()
                            ->first();
                        
                        if ($maleAvatar) {
                            $avatarUrl = $maleAvatar->image_url;
                            
                            // Prepend base URL if it doesn't already have http/https
                            if (!empty($avatarUrl) && !preg_match('/^https?:\/\//', $avatarUrl)) {
                                $avatarUrl = 'https://onlycare.in/' . ltrim($avatarUrl, '/');
                            }
                            
                            // Update avatar and profile image
                            $user->profile_image = $avatarUrl;
                            
                            Log::info('✅ Male Voice - Profile Updated', [
                                'user_id' => $user->id,
                                'profile_image' => $avatarUrl,
                                'gender' => 'MALE',
                                'user_type' => 'MALE',
                                'verified_datetime' => $user->verified_datetime
                            ]);
                        } else {
                            Log::warning('⚠️ No Male Avatar Found - Gender Updated Only', [
                                'user_id' => $user->id,
                                'gender' => 'MALE',
                                'user_type' => 'MALE'
                            ]);
                        }
                    }
                } else {
                    // Gender detection failed - set to pending verification
                    $user->is_verified = false;
                    Log::warning('⚠️ Gender Detection Failed', [
                        'user_id' => $user->id
                    ]);
                }
                
                $user->save();
                
                Log::info('✅ Update Voice Success', [
                    'user_id' => $user->id,
                    'voice_file' => $filename,
                    'is_verified' => $user->is_verified,
                    'voice_gender' => $user->voice_gender ?? null
                ]);

                // Format response
                $response = [
                    'success' => true,
                    'message' => 'user voice updated successfully.',
                    'data' => [
                        'id' => $user->id,
                        'name' => $user->name,
                        'user_gender' => $user->user_type ?? $user->gender,
                        'image' => $user->profile_image,
                        'gender' => $user->user_type ?? $user->gender,
                        'language' => $user->language,
                        'age' => $user->age,
                        'mobile' => $user->phone,
                        'interests' => $user->interests,
                        'describe_yourself' => $user->bio,
                        'voice' => url('storage/voices/' . $filename),
                        'is_verified' => $user->is_verified,
                        'verified_datetime' => $user->verified_datetime ? $user->verified_datetime->setTimezone('Asia/Kolkata')->format('Y-m-d H:i:s') : null,
                        'voice_gender' => $user->voice_gender ?? null,
                        'balance' => $user->coin_balance ?? 0,
                        'audio_status' => $user->audio_call_enabled ? 1 : 0,
                        'video_status' => $user->video_call_enabled ? 1 : 0,
                        'updated_at' => $user->updated_at->format('Y-m-d H:i:s'),
                        'created_at' => $user->created_at->format('Y-m-d H:i:s')
                    ]
                ];

                return response()->json($response);
            }

            return response()->json([
                'success' => false,
                'message' => 'voice is required and cannot be empty.'
            ], 400);

        } catch (\Exception $e) {
            Log::error('❌ Update Voice Exception', [
                'user_id' => $request->user()->id ?? 'unknown',
                'error' => $e->getMessage(),
                'file' => $e->getFile(),
                'line' => $e->getLine(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return response()->json([
                'success' => false,
                'message' => 'An error occurred while processing your request. Please try again later.',
                'error' => config('app.debug') ? $e->getMessage() : null
            ], 500);
        }
    }

    /**
     * Get gifts list
     * POST /api/v1/auth/gifts_list
     */
    public function gifts_list(Request $request)
    {
        $gifts = Gifts::orderBy('created_at', 'desc')->get();
        
        if ($gifts->isEmpty()) {
            return response()->json([
                'success' => false,
                'message' => 'No gifts found.'
            ], 200);
        }
        
        $formattedGifts = $gifts->map(function($gift) {
            // Get full URL for gift icon
            $giftIconUrl = $gift->gift_icon;
            if (!empty($giftIconUrl) && !preg_match('/^https?:\/\//', $giftIconUrl)) {
                $giftIconUrl = url('storage/' . $giftIconUrl);
            }
            
            return [
                'id' => $gift->id,
                'gift_icon' => $giftIconUrl,
                'coins' => $gift->coins,
                'updated_at' => $gift->updated_at->setTimezone('Asia/Kolkata')->format('Y-m-d H:i:s'),
                'created_at' => $gift->created_at->setTimezone('Asia/Kolkata')->format('Y-m-d H:i:s')
            ];
        });
        
        return response()->json([
            'success' => true,
            'message' => 'Gifts listed successfully.',
            'data' => $formattedGifts
        ]);
    }

    /**
     * Send gift
     * POST /api/v1/auth/send_gifts
     */
    public function send_gifts(Request $request)
    {
        try {
            $validator = Validator::make($request->all(), [
                'user_id' => 'required',
                'receiver_id' => 'required',
                'gift_id' => 'required'
            ], [
                'user_id.required' => 'user_id is required.',
                'receiver_id.required' => 'receiver_id is required.',
                'gift_id.required' => 'gift_id is required.'
            ]);
            
            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => $validator->errors()->first()
                ], 200);
            }
            
            // Get authenticated user from token
            $authenticatedUser = $request->user();
            
            if (!$authenticatedUser) {
                return response()->json([
                    'success' => false,
                    'message' => 'Unauthorized. Please login first.'
                ], 401);
            }
            
            // Handle user_id format (accept with or without USR_ prefix)
            $senderId = $request->user_id;
            if (!str_starts_with($senderId, 'USR_')) {
                $senderId = 'USR_' . $senderId;
            }
            
            // Get sender
            $sender = User::find($senderId);
            
            if (!$sender) {
                Log::warning('Send Gift - Sender not found', [
                    'requested_user_id' => $request->user_id,
                    'normalized_sender_id' => $senderId,
                    'authenticated_user_id' => $authenticatedUser->id ?? null
                ]);
                return response()->json([
                    'success' => false,
                    'message' => 'Sender not found.'
                ], 200);
            }
            
            // Verify sender is the authenticated user
            if ($authenticatedUser->id !== $senderId && $authenticatedUser->id !== $sender->id) {
                Log::warning('Send Gift - Unauthorized sender', [
                    'authenticated_user_id' => $authenticatedUser->id,
                    'requested_sender_id' => $senderId,
                    'sender_db_id' => $sender->id
                ]);
                return response()->json([
                    'success' => false,
                    'message' => 'Unauthorized. You can only send gifts as yourself.'
                ], 401);
            }
            
            // Handle receiver_id format (accept with or without USR_ prefix)
            $receiverId = $request->receiver_id;
            if (!str_starts_with($receiverId, 'USR_')) {
                $receiverId = 'USR_' . $receiverId;
            }
            
            // Get receiver
            $receiver = User::find($receiverId);
            if (!$receiver) {
                Log::warning('Send Gift - Receiver not found', [
                    'requested_receiver_id' => $request->receiver_id,
                    'normalized_receiver_id' => $receiverId
                ]);
                return response()->json([
                    'success' => false,
                    'message' => 'Receiver not found.'
                ], 200);
            }
            
            // Get gift
            $gift = Gifts::find($request->gift_id);
            if (!$gift) {
                Log::warning('Send Gift - Gift not found', [
                    'gift_id' => $request->gift_id
                ]);
                return response()->json([
                    'success' => false,
                    'message' => 'Gift not found.'
                ], 200);
            }
            
            // Validate gift has coins value
            if (is_null($gift->coins) || $gift->coins <= 0) {
                Log::error('Send Gift - Invalid gift coins', [
                    'gift_id' => $gift->id,
                    'gift_coins' => $gift->coins
                ]);
                return response()->json([
                    'success' => false,
                    'message' => 'Invalid gift configuration.'
                ], 400);
            }
            
            // Validate receiver gender (must be female)
            if (strtolower($receiver->gender) !== 'female' && strtolower($receiver->user_type) !== 'female') {
                return response()->json([
                    'success' => false,
                    'message' => 'Gifts can only be sent to female users.'
                ], 200);
            }
            
            // Check sender has sufficient coins
            $senderCoinBalance = $sender->coin_balance ?? 0;
            if ($senderCoinBalance < $gift->coins) {
                return response()->json([
                    'success' => false,
                    'message' => 'Insufficient coins to send this gift.'
                ], 400);
            }
            
            // Calculate rupee amount for receiver (1 coin = ₹0.1)
            $amountInRupees = $gift->coins * 0.1;
            // Convert to paise (multiply by 100) for total_earnings integer field
            $amountToCredit = round($amountInRupees * 100);
            
            DB::beginTransaction();
            try {
                // Generate unique reference ID for this gift transaction
                // This allows users to send the same gift multiple times
                // Format: GIFT_{gift_id}_{timestamp}_{random}
                $uniqueReferenceId = 'GIFT_' . $gift->id . '_' . time() . '_' . rand(1000, 9999);
                
                // Deduct coins from sender's coin_balance (direct coin deduction, no conversion)
                $sender->decrement('coin_balance', $gift->coins);

                // Credit coins to receiver
                $receiver->increment('coin_balance', $gift->coins);
                
                // Credit rupees to receiver's total_earnings (convert coins to rupees, then to paise)
                $receiver->increment('total_earnings', $amountToCredit);
                
                // Generate unique transaction IDs
                $senderTxnId = 'TXN_' . time() . rand(10000, 99999) . '_' . uniqid();
                $receiverTxnId = 'TXN_' . time() . rand(10000, 99999) . '_' . uniqid();
                
                // Create sender transaction (debit) - type: send_gift
                Transaction::create([
                    'id' => $senderTxnId,
                    'user_id' => $sender->id,
                    'type' => 'GIFT',
                    'coins' => -$gift->coins, // Negative for debit (coins)
                    'amount' => 0,
                    'status' => 'SUCCESS',
                    'reference_id' => $uniqueReferenceId, // Unique reference to allow multiple sends
                    'reference_type' => 'GIFT',
                    'description' => 'Gift sent to ' . $receiver->name,
                    'created_at' => now(),
                    'updated_at' => now()
                ]);
                
                // Create receiver transaction (credit) - type: receive_gift
                Transaction::create([
                    'id' => $receiverTxnId,
                    'user_id' => $receiver->id,
                    'type' => 'GIFT',
                    'coins' => 0, // No coins credited
                    'amount' => $amountInRupees, // Amount in rupees (for transaction record)
                    'status' => 'SUCCESS',
                    'reference_id' => $uniqueReferenceId, // Same unique reference for linking
                    'reference_type' => 'GIFT',
                    'description' => 'Gift received from ' . $sender->name,
                    'created_at' => now(),
                    'updated_at' => now()
                ]);
                
                DB::commit();
                
                // Get full URL for gift icon
                $giftIconUrl = $gift->gift_icon;
                if (!empty($giftIconUrl) && !preg_match('/^https?:\/\//', $giftIconUrl)) {
                    $giftIconUrl = url('storage/' . $giftIconUrl);
                }
                
                Log::info('Send Gift - Success', [
                    'sender_id' => $sender->id,
                    'receiver_id' => $receiver->id,
                    'gift_id' => $gift->id,
                    'gift_coins' => $gift->coins,
                    'amount_credited' => $amountToCredit
                ]);
                
                return response()->json([
                    'success' => true,
                    'message' => 'Gift sent successfully!',
                    'data' => [
                        'sender_name' => $sender->name,
                        'receiver_name' => $receiver->name,
                        'gift_id' => $gift->id,
                        'gift_icon' => $giftIconUrl,
                        'gift_coins' => $gift->coins
                    ]
                ]);
                
            } catch (\Exception $e) {
                DB::rollBack();
                Log::error('Send Gift Error - Transaction Failed', [
                    'error' => $e->getMessage(),
                    'file' => $e->getFile(),
                    'line' => $e->getLine(),
                    'sender_id' => $sender->id ?? null,
                    'receiver_id' => $receiver->id ?? null,
                    'gift_id' => $gift->id ?? null,
                    'trace' => $e->getTraceAsString()
                ]);
                
                return response()->json([
                    'success' => false,
                    'message' => 'An error occurred while sending the gift. Please try again.'
                ], 500);
            }
            
        } catch (\Exception $e) {
            Log::error('Send Gift Error - Outer Exception', [
                'error' => $e->getMessage(),
                'file' => $e->getFile(),
                'line' => $e->getLine(),
                'request_data' => $request->all(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return response()->json([
                'success' => false,
                'message' => 'An error occurred while sending the gift. Please try again.'
            ], 500);
        }
    }

    /**
     * Send FCM notification for gift sent during call
     * POST /api/v1/auth/send_gift_notification
     */
    public function sendGiftNotification(Request $request)
    {
        try {
            $validator = Validator::make($request->all(), [
                'sender_id' => 'required|string',
                'receiver_id' => 'required|string',
                'gift_id' => 'required|integer',
                'gift_icon' => 'required|url',
                'gift_coins' => 'required|integer',
                'call_type' => 'required|in:audio,video'
            ], [
                'sender_id.required' => 'sender_id is required.',
                'receiver_id.required' => 'receiver_id is required.',
                'gift_id.required' => 'gift_id is required.',
                'gift_id.integer' => 'gift_id must be an integer.',
                'gift_icon.required' => 'gift_icon is required.',
                'gift_icon.url' => 'gift_icon must be a valid URL.',
                'gift_coins.required' => 'gift_coins is required.',
                'gift_coins.integer' => 'gift_coins must be an integer.',
                'call_type.required' => 'call_type is required.',
                'call_type.in' => 'call_type must be either "audio" or "video".'
            ]);
            
            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => 'Validation failed',
                    'error' => [
                        'code' => 'VALIDATION_ERROR',
                        'message' => $validator->errors()->first(),
                        'errors' => $validator->errors()
                    ]
                ], 400);
            }
            
            // Get authenticated user from token
            $authenticatedUser = $request->user();
            
            if (!$authenticatedUser) {
                return response()->json([
                    'success' => false,
                    'message' => 'Unauthorized. Please login first.',
                    'error' => [
                        'code' => 'UNAUTHORIZED',
                        'message' => 'Invalid or missing authentication token'
                    ]
                ], 401);
            }
            
            // Handle user_id format (accept with or without USR_ prefix)
            $senderId = $request->sender_id;
            if (!str_starts_with($senderId, 'USR_')) {
                $senderId = 'USR_' . $senderId;
            }
            
            $receiverId = $request->receiver_id;
            if (!str_starts_with($receiverId, 'USR_')) {
                $receiverId = 'USR_' . $receiverId;
            }
            
            // Get receiver (female user who will receive notification)
            $receiver = User::find($receiverId);
            if (!$receiver) {
                Log::warning('Send Gift Notification - Receiver not found', [
                    'receiver_id' => $receiverId
                ]);
                return response()->json([
                    'success' => false,
                    'message' => 'Receiver not found.',
                    'error' => [
                        'code' => 'NOT_FOUND',
                        'message' => 'Receiver user not found'
                    ]
                ], 404);
            }
            
            // Check if receiver has FCM token
            if (!$receiver->fcm_token) {
                Log::warning('Send Gift Notification - Receiver FCM token not found', [
                    'receiver_id' => $receiverId
                ]);
                return response()->json([
                    'success' => false,
                    'message' => 'Receiver FCM token not found',
                    'error' => [
                        'code' => 'FCM_TOKEN_NOT_FOUND',
                        'message' => 'Receiver does not have an FCM token registered'
                    ]
                ], 400);
            }
            
            // Get sender (male user who sent the gift)
            $sender = User::find($senderId);
            if (!$sender) {
                Log::warning('Send Gift Notification - Sender not found', [
                    'sender_id' => $senderId
                ]);
                return response()->json([
                    'success' => false,
                    'message' => 'Sender not found.',
                    'error' => [
                        'code' => 'NOT_FOUND',
                        'message' => 'Sender user not found'
                    ]
                ], 404);
            }
            
            // Verify sender is the authenticated user
            if ($authenticatedUser->id !== $senderId && $authenticatedUser->id !== $sender->id) {
                Log::warning('Send Gift Notification - Unauthorized sender', [
                    'authenticated_user_id' => $authenticatedUser->id,
                    'requested_sender_id' => $senderId
                ]);
                return response()->json([
                    'success' => false,
                    'message' => 'Unauthorized. You can only send gift notifications as yourself.',
                    'error' => [
                        'code' => 'UNAUTHORIZED',
                        'message' => 'You can only send gift notifications as yourself'
                    ]
                ], 401);
            }
            
            // Initialize Firebase
            try {
                $firebase = (new \Kreait\Firebase\Factory)
                    ->withServiceAccount(config('firebase.credentials'));
                $messaging = $firebase->createMessaging();
                
                // Prepare FCM data payload
                // ✅ CRITICAL: ALL VALUES MUST BE STRINGS for Android FCM compatibility
                // ✅ CRITICAL: NO notification field - only data payload!
                $data = [
                    'type' => 'gift_sent',
                    'sender_id' => (string) $senderId,
                    'sender_name' => (string) ($sender->name ?? 'Someone'),
                    'receiver_id' => (string) $receiverId,
                    'gift_id' => (string) $request->gift_id,
                    'gift_icon' => (string) $request->gift_icon,
                    'gift_coins' => (string) $request->gift_coins,
                    'call_type' => (string) $request->call_type,
                    'timestamp' => (string) (now()->timestamp * 1000), // Milliseconds for Android
                ];
                
                // Create FCM message with high priority for Android
                $message = \Kreait\Firebase\Messaging\CloudMessage::withTarget('token', $receiver->fcm_token)
                    ->withData($data)
                    ->withAndroidConfig([
                        'priority' => 'high',
                        // NO notification field - app handles display
                    ]);
                
                // Send notification
                $result = $messaging->send($message);
                
                Log::info('✅ Gift FCM notification sent successfully', [
                    'sender_id' => $senderId,
                    'receiver_id' => $receiverId,
                    'gift_id' => $request->gift_id,
                    'gift_coins' => $request->gift_coins,
                    'call_type' => $request->call_type,
                    'fcm_result' => $result
                ]);
                
                return response()->json([
                    'success' => true,
                    'message' => 'Gift notification sent successfully',
                    'data' => 'Notification sent'
                ]);
                
            } catch (\Kreait\Firebase\Exception\MessagingException $e) {
                $errorMessage = $e->getMessage();
                
                // Detect invalid token errors and log them
                $isInvalidToken = str_contains($errorMessage, 'Requested entity was not found') ||
                                 str_contains($errorMessage, 'Invalid registration token') ||
                                 str_contains($errorMessage, 'registration-token-not-registered') ||
                                 str_contains($errorMessage, 'MismatchSenderId');
                
                if ($isInvalidToken) {
                    Log::warning('⚠️ Invalid FCM token detected for gift notification', [
                        'receiver_id' => $receiverId,
                        'error' => $errorMessage
                    ]);
                    
                    // Optionally clear invalid token
                    // $receiver->update(['fcm_token' => null]);
                }
                
                Log::error('❌ Gift FCM Messaging Exception', [
                    'receiver_id' => $receiverId,
                    'sender_id' => $senderId,
                    'error' => $errorMessage,
                    'trace' => $e->getTraceAsString()
                ]);
                
                return response()->json([
                    'success' => false,
                    'message' => 'Failed to send FCM notification',
                    'error' => [
                        'code' => 'FCM_ERROR',
                        'message' => 'Failed to send notification: ' . $errorMessage
                    ]
                ], 500);
                
            } catch (\Exception $e) {
                Log::error('❌ Send Gift Notification Error', [
                    'error' => $e->getMessage(),
                    'file' => $e->getFile(),
                    'line' => $e->getLine(),
                    'sender_id' => $senderId,
                    'receiver_id' => $receiverId,
                    'trace' => $e->getTraceAsString()
                ]);
                
                return response()->json([
                    'success' => false,
                    'message' => 'An error occurred while sending the notification. Please try again.',
                    'error' => [
                        'code' => 'INTERNAL_ERROR',
                        'message' => config('app.debug') ? $e->getMessage() : 'Internal server error'
                    ]
                ], 500);
            }
            
        } catch (\Exception $e) {
            Log::error('❌ Send Gift Notification - Outer Exception', [
                'error' => $e->getMessage(),
                'file' => $e->getFile(),
                'line' => $e->getLine(),
                'request_data' => $request->all(),
                'trace' => $e->getTraceAsString()
            ]);
            
            return response()->json([
                'success' => false,
                'message' => 'An error occurred while processing your request. Please try again.',
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => config('app.debug') ? $e->getMessage() : 'Internal server error'
                ]
            ], 500);
        }
    }

    /**
     * Get remaining call time based on coin balance and elapsed time
     * POST /api/v1/auth/get_remaining_time
     * 
     * @param Request $request
     * @return \Illuminate\Http\JsonResponse
     */
    public function get_remaining_time(Request $request)
    {
        try {
            // Check authentication (token still required for security)
            $authenticatedUser = $request->user();
            
            if (!$authenticatedUser) {
                return response()->json([
                    'success' => false,
                    'message' => 'Unauthorized. Please provide a valid token.'
                ], 401);
            }

            // Validation - user_id and call_type are required
            $validator = Validator::make($request->all(), [
                'user_id' => 'required',
                'call_type' => 'required|in:audio,video,AUDIO,VIDEO'
            ], [
                'user_id.required' => 'user_id is required.',
                'call_type.required' => 'call_type is required.',
                'call_type.in' => 'Invalid call_type. It must be either "audio" or "video".'
            ]);

            if ($validator->fails()) {
                return response()->json([
                    'success' => false,
                    'message' => $validator->errors()->first()
                ], 200);
            }

            $user_id = $request->user_id;
            $call_type = strtoupper($request->call_type); // Normalize to uppercase (AUDIO or VIDEO)

            // Handle user_id format (accept with or without USR_ prefix)
            if (!str_starts_with($user_id, 'USR_')) {
                $user_id = 'USR_' . $user_id;
            }

            // Find user by user_id (not from token)
            $user = User::find($user_id);
            
            if (!$user) {
                return response()->json([
                    'success' => false,
                    'message' => 'User not found for the provided user_id.'
                ], 200);
            }

            // Refresh coin balance to get latest from database
            $user->refresh();
            $coins = $user->coin_balance ?? 0;

            // Find ongoing call (check both caller_id and receiver_id since either user can request)
            $ongoingCall = DB::table('calls')
                ->where(function($query) use ($user_id) {
                    $query->where('caller_id', $user_id)
                          ->orWhere('receiver_id', $user_id);
                })
                ->where('call_type', $call_type)
                ->where('status', 'ONGOING')
                ->whereNull('ended_at')
                ->latest('started_at')
                ->first();

            // Calculate elapsed time
            $elapsedSeconds = 0;
            if ($ongoingCall && $ongoingCall->started_at) {
                $startedAt = \Carbon\Carbon::parse($ongoingCall->started_at);
                $elapsedSeconds = now()->diffInSeconds($startedAt);
            }

            // Conversion rates: coins per minute
            $conversionRate = ($call_type === 'AUDIO') ? 10 : 60; // Audio: 10 coins/min, Video: 60 coins/min

            // Calculate total available time (in seconds)
            // Formula: (coins / conversion_rate) * 60 seconds
            $totalSeconds = ($coins / $conversionRate) * 60;

            // Calculate remaining time (ensure it never goes below 0)
            $remainingSeconds = max(0, $totalSeconds - $elapsedSeconds);

            // Format times (M:SS format)
            $remainingMinutes = floor($remainingSeconds / 60);
            $remainingSecs = floor($remainingSeconds % 60);
            $remainingTime = sprintf("%d:%02d", $remainingMinutes, $remainingSecs);

            $elapsedMinutes = floor($elapsedSeconds / 60);
            $elapsedSecs = floor($elapsedSeconds % 60);
            $elapsedTime = sprintf("%d:%02d", $elapsedMinutes, $elapsedSecs);

            Log::info('✅ Get Remaining Time Success', [
                'requested_user_id' => $user_id,
                'authenticated_user_id' => $authenticatedUser->id,
                'call_type' => $call_type,
                'coins' => $coins,
                'elapsed_seconds' => $elapsedSeconds,
                'remaining_seconds' => $remainingSeconds,
                'remaining_time' => $remainingTime,
                'elapsed_time' => $elapsedTime
            ]);

            return response()->json([
                'success' => true,
                'message' => 'Remaining Time Listed successfully.',
                'data' => [
                    'remaining_time' => $remainingTime,
                    'elapsed_time' => $elapsedTime,
                    'latest_coins' => $coins
                ]
            ]);

        } catch (\Exception $e) {
            Log::error('❌ Get Remaining Time Exception', [
                'user_id' => $request->user_id ?? 'unknown',
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);

            return response()->json([
                'success' => false,
                'error' => $e->getMessage()
            ], 500);
        }
    }

    /**
     * Normalize phone digits into a consistent DB key.
     * - For India (+91), store the last 10 digits (strips leading 91 / 0)
     * - For other countries, store digits as-is (9-15 digits typical)
     */
    private function normalizePhone(?string $countryCode, string $phone): string
    {
        $digits = preg_replace('/\D+/', '', (string) $phone);

        // India normalization
        if ($countryCode === '+91') {
            if (str_starts_with($digits, '91') && strlen($digits) > 10) {
                $digits = substr($digits, 2);
            }
            if (strlen($digits) > 10) {
                $digits = substr($digits, -10);
            }
            // Strip leading 0 if present and still >10 (defensive)
            if (str_starts_with($digits, '0') && strlen($digits) > 10) {
                $digits = ltrim($digits, '0');
                if (strlen($digits) > 10) {
                    $digits = substr($digits, -10);
                }
            }
        }

        return $digits;
    }

    /**
     * Process scheduled notifications (called by cron job)
     */
    public function cron_jobs(Request $request)
    {
        try {
            $currentDay = Carbon::now('Asia/Kolkata')->format('l'); // Monday, Tuesday, etc.
            $currentHourMinute = Carbon::now('Asia/Kolkata')->format('H:i'); // 14:30

            // Find notifications scheduled for current day/time
            $notifications = ScreenNotifications::where(function ($query) use ($currentDay) {
                $query->where('day', $currentDay)
                      ->orWhere('day', 'all'); // Include "all" days
            })
            ->where('time', $currentHourMinute)
            ->get();

            if ($notifications->isEmpty()) {
                return response()->json([
                    'success' => true,
                    'message' => 'No notifications scheduled for this time',
                    'processed' => 0
                ]);
            }

            $notificationService = new NotificationService();
            $sentToUserIds = [];
            $processed = 0;

            // Shuffle notifications for fair distribution
            foreach ($notifications->shuffle() as $notification) {
                try {
                    // Send notification
                    $result = $notificationService->sendScheduledNotification($notification);
                    
                    if ($result) {
                        $processed++;
                        Log::info('Scheduled notification sent', [
                            'notification_id' => $notification->id,
                            'title' => $notification->title,
                            'day' => $notification->day,
                            'time' => $notification->time
                        ]);
                    }
                } catch (\Exception $e) {
                    Log::error('Failed to send scheduled notification', [
                        'notification_id' => $notification->id,
                        'error' => $e->getMessage()
                    ]);
                    // Continue processing other notifications
                }
            }

            return response()->json([
                'success' => true,
                'message' => "Processed {$processed} scheduled notifications",
                'processed' => $processed,
                'total' => $notifications->count()
            ]);

        } catch (\Exception $e) {
            Log::error('Cron job error: ' . $e->getMessage());
            return response()->json([
                'success' => false,
                'error' => $e->getMessage()
            ], 500);
        }
    }

}
