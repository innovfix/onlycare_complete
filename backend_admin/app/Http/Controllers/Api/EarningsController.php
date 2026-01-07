<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Call;
use App\Models\Transaction;
use Illuminate\Http\Request;
use Carbon\Carbon;

class EarningsController extends Controller
{
    /**
     * Helper method to check if user is female
     * Handles case sensitivity and checks both gender and user_type fields
     * 
     * @param mixed $user
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
     * Get earnings dashboard (Female only)
     */
    public function getDashboard(Request $request)
    {
        $user = $request->user();
        
        // DEBUG: Log user gender info for troubleshooting
        \Log::info('📱 Earnings Dashboard check:', [
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
                    'message' => 'Only female users can access earnings',
                    'debug' => [
                        'user_type' => $user->user_type,
                        'gender' => $user->gender
                    ]
                ]
            ], 403);
        }

        // Today's earnings
        $todayEarnings = Call::where('receiver_id', $user->id)
                            ->where('status', 'ENDED')
                            ->whereDate('created_at', today())
                            ->sum('coins_earned');

        // This week's earnings
        $weekEarnings = Call::where('receiver_id', $user->id)
                           ->where('status', 'ENDED')
                           ->whereBetween('created_at', [
                               Carbon::now()->startOfWeek(),
                               Carbon::now()->endOfWeek()
                           ])
                           ->sum('coins_earned');

        // This month's earnings
        $monthEarnings = Call::where('receiver_id', $user->id)
                            ->where('status', 'ENDED')
                            ->whereMonth('created_at', now()->month)
                            ->whereYear('created_at', now()->year)
                            ->sum('coins_earned');

        // Total withdrawals
        $totalWithdrawals = Transaction::where('user_id', $user->id)
                                      ->where('type', 'WITHDRAWAL')
                                      ->where('status', 'SUCCESS')
                                      ->sum('coins');

        // Pending withdrawals
        $pendingWithdrawals = Transaction::where('user_id', $user->id)
                                        ->where('type', 'WITHDRAWAL')
                                        ->where('status', 'PENDING')
                                        ->sum('coins');

        // Available balance (earnings that can be withdrawn)
        $availableBalance = $user->total_earnings - $totalWithdrawals - $pendingWithdrawals;

        // Total calls
        $totalCalls = Call::where('receiver_id', $user->id)
                         ->where('status', 'ENDED')
                         ->count();

        // Today's calls
        $todayCalls = Call::where('receiver_id', $user->id)
                         ->where('status', 'ENDED')
                         ->whereDate('created_at', today())
                         ->count();

        // Audio calls count
        $audioCallsCount = Call::where('receiver_id', $user->id)
                              ->where('status', 'ENDED')
                              ->where('call_type', 'AUDIO')
                              ->count();

        // Video calls count
        $videoCallsCount = Call::where('receiver_id', $user->id)
                              ->where('status', 'ENDED')
                              ->where('call_type', 'VIDEO')
                              ->count();

        // Average per call
        $averagePerCall = $totalCalls > 0 ? round($user->total_earnings / $totalCalls, 2) : 0;

        // Total duration (in seconds)
        $totalDuration = Call::where('receiver_id', $user->id)
                            ->where('status', 'ENDED')
                            ->sum('duration');

        // Average call duration (in seconds)
        $averageCallDuration = $totalCalls > 0 ? round($totalDuration / $totalCalls) : 0;

        // Calculate total minutes from total duration
        $totalMinutes = round($totalDuration / 60);
        $todayMinutes = Call::where('receiver_id', $user->id)
                            ->where('status', 'ENDED')
                            ->whereDate('created_at', today())
                            ->sum('duration') / 60;

        return response()->json([
            'success' => true,
            'message' => 'Earnings dashboard fetched successfully',
            'dashboard' => [
                'total_earnings' => (float) $user->total_earnings,
                'today_earnings' => (float) $todayEarnings,
                'pending_earnings' => (float) $pendingWithdrawals, // Pending withdrawal = pending earnings
                'withdrawn_earnings' => (float) $totalWithdrawals,
                'total_calls' => $totalCalls,
                'total_minutes' => (int) $totalMinutes,
                'today_calls' => $todayCalls,
                'today_minutes' => (int) round($todayMinutes),
                // Additional fields (keep for backward compatibility)
                'week_earnings' => (float) $weekEarnings,
                'month_earnings' => (float) $monthEarnings,
                'available_balance' => (float) max(0, $availableBalance),
                'pending_withdrawals' => (float) $pendingWithdrawals,
                'total_withdrawals' => (float) $totalWithdrawals,
                'audio_calls_count' => $audioCallsCount,
                'video_calls_count' => $videoCallsCount,
                'average_call_duration' => $averageCallDuration,
                'average_earnings_per_call' => $averagePerCall,
                'total_duration' => $totalDuration
            ]
        ]);
    }
}







