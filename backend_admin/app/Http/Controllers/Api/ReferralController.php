<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Referral;
use App\Models\User;
use App\Models\AppSetting;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class ReferralController extends Controller
{
    /**
     * Get referral code and statistics
     */
    public function getReferralCode(Request $request)
    {
        $user = $request->user();

        // Generate referral code if doesn't exist
        if (!$user->referral_code) {
            $user->referral_code = $this->generateReferralCode();
            $user->save();
        }

        // Get referral bonus settings
        $settings = AppSetting::first();
        $perInviteBonus = $settings->referral_bonus_referrer ?? 10;

        // Count total successful referrals (only claimed ones)
        $totalInvites = Referral::where('referrer_id', $user->id)
                               ->where('is_claimed', true)
                               ->count();

        // Calculate total coins earned from referrals
        $totalCoinsEarned = Referral::where('referrer_id', $user->id)
                                   ->where('is_claimed', true)
                                   ->sum('bonus_coins');

        // Create shareable message
        $shareMessage = "Join me on Only Care! Use my referral code {$user->referral_code} and we both get {$perInviteBonus} coins! Download now: https://onlycare.app/invite/{$user->referral_code}";

        return response()->json([
            'success' => true,
            'referral_code' => $user->referral_code,
            'referral_url' => 'https://onlycare.app/invite/' . $user->referral_code,
            'my_invites' => $totalInvites,
            'per_invite_coins' => $perInviteBonus,
            'total_coins_earned' => $totalCoinsEarned,
            'share_message' => $shareMessage,
            'whatsapp_share_url' => 'https://wa.me/?text=' . urlencode($shareMessage)
        ]);
    }

    /**
     * Apply referral code
     */
    public function applyReferralCode(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'referral_code' => 'required|string'
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

        // Check if user already used a referral code
        $existingReferral = Referral::where('referred_user_id', $request->user()->id)->first();
        if ($existingReferral) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'ALREADY_USED',
                    'message' => 'You have already used a referral code'
                ]
            ], 400);
        }

        // Find referrer by code
        $referrer = User::where('referral_code', strtoupper($request->referral_code))->first();

        if (!$referrer) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INVALID_CODE',
                    'message' => 'Invalid referral code'
                ]
            ], 404);
        }

        // Can't use own referral code
        if ($referrer->id === $request->user()->id) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INVALID_CODE',
                    'message' => 'Cannot use your own referral code'
                ]
            ], 400);
        }

        // Get referral bonus settings
        $settings = AppSetting::first();
        $referrerBonus = $settings->referral_bonus_referrer ?? 10;
        $referredBonus = $settings->referral_bonus_referred ?? 10;

        DB::beginTransaction();
        try {
            // Create referral record
            $referral = Referral::create([
                'referrer_id' => $referrer->id,
                'referred_user_id' => $request->user()->id,
                'referral_code' => strtoupper($request->referral_code),
                'bonus_coins' => $referrerBonus,
                'is_claimed' => true,
                'claimed_at' => now()
            ]);

            // Give bonus coins to both users
            $referrer->increment('coin_balance', $referrerBonus);
            $request->user()->increment('coin_balance', $referredBonus);

            DB::commit();

            return response()->json([
                'success' => true,
                'message' => 'Referral code applied successfully! You received ' . $referredBonus . ' coins',
                'bonus_coins' => $referredBonus,
                'referrer_bonus' => $referrerBonus,
                'new_balance' => $request->user()->fresh()->coin_balance
            ]);
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to apply referral code'
                ]
            ], 500);
        }
    }

    /**
     * Get referral history
     */
    public function getReferralHistory(Request $request)
    {
        $perPage = min($request->get('limit', 20), 50);
        
        $referrals = Referral::where('referrer_id', $request->user()->id)
                            ->with('referredUser')
                            ->orderBy('created_at', 'desc')
                            ->paginate($perPage);

        return response()->json([
            'success' => true,
            'referrals' => $referrals->map(function($referral) {
                return [
                    'id' => 'REF_' . $referral->id,
                    'referred_user' => [
                        'id' => 'USR_' . ($referral->referredUser->id ?? ''),
                        'name' => $referral->referredUser->name ?? 'Unknown User',
                        'phone' => $referral->referredUser->phone ?? null,
                        'profile_image' => $referral->referredUser->profile_image ?? null
                    ],
                    'bonus_coins' => $referral->bonus_coins,
                    'is_claimed' => $referral->is_claimed,
                    'created_at' => $referral->created_at->toIso8601String(),
                    'created_at_formatted' => $referral->created_at->format('M d, Y'),
                    'claimed_at' => $referral->claimed_at ? $referral->claimed_at->toIso8601String() : null
                ];
            }),
            'pagination' => [
                'current_page' => $referrals->currentPage(),
                'total_pages' => $referrals->lastPage(),
                'total_items' => $referrals->total(),
                'per_page' => $referrals->perPage()
            ]
        ]);
    }

    /**
     * Generate unique referral code (format: CTLA8241)
     */
    private function generateReferralCode()
    {
        do {
            // Generate 4 random uppercase letters
            $letters = strtoupper(Str::random(4));
            // Generate 4 random numbers
            $numbers = rand(1000, 9999);
            $code = $letters . $numbers;
        } while (User::where('referral_code', $code)->exists());

        return $code;
    }
}

