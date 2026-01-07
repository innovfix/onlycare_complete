<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\AppSetting;
use Illuminate\Http\Request;

class SettingsController extends Controller
{
    /**
     * Get app settings
     */
    public function getAppSettings(Request $request)
    {
        $settings = AppSetting::first();

        if (!$settings) {
            // Return default settings if none exist
            return response()->json([
                'success' => true,
                'settings' => [
                    'audio_call_rate' => 10,
                    'video_call_rate' => 15,
                    'min_withdrawal_amount' => 500,
                    'coin_to_inr_rate' => 1,
                    'referral_bonus_referrer' => 100,
                    'referral_bonus_referred' => 50
                ]
            ]);
        }

        return response()->json([
            'success' => true,
            'settings' => [
                'audio_call_rate' => $settings->audio_call_rate,
                'video_call_rate' => $settings->video_call_rate,
                'min_withdrawal_amount' => $settings->min_withdrawal_amount,
                'coin_to_inr_rate' => $settings->coin_to_inr_rate,
                'referral_bonus_referrer' => $settings->referral_bonus_referrer,
                'referral_bonus_referred' => $settings->referral_bonus_referred
            ]
        ]);
    }
}







