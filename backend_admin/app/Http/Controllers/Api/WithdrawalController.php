<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Withdrawal;
use App\Models\BankAccount;
use App\Models\Transaction;
use App\Models\AppSetting;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;

class WithdrawalController extends Controller
{
    /**
     * Request withdrawal (Female only)
     */
    public function requestWithdrawal(Request $request)
    {
        if ($request->user()->user_type !== 'FEMALE') {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'FORBIDDEN',
                    'message' => 'Only female users can request withdrawals'
                ]
            ], 403);
        }

        $validator = Validator::make($request->all(), [
            'amount' => 'required|numeric|min:1',
            'bank_account_id' => 'required|string'
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

        // Check minimum withdrawal amount
        $settings = AppSetting::first();
        $minWithdrawalAmount = $settings->min_withdrawal_amount ?? 500;

        if ($request->amount < $minWithdrawalAmount) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'MIN_WITHDRAWAL_NOT_MET',
                    'message' => "Minimum withdrawal amount is ₹{$minWithdrawalAmount}",
                    'details' => [
                        'minimum_required' => $minWithdrawalAmount,
                        'requested' => $request->amount
                    ]
                ]
            ], 400);
        }

        // Check KYC status
        if ($request->user()->kyc_status !== 'APPROVED') {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'KYC_NOT_APPROVED',
                    'message' => 'Please complete KYC verification to withdraw'
                ]
            ], 400);
        }

        // Verify bank account
        $bankAccountId = str_replace('BANK_', '', $request->bank_account_id);
        $bankAccount = BankAccount::where('id', $bankAccountId)
                                  ->where('user_id', $request->user()->id)
                                  ->first();

        if (!$bankAccount) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Bank account not found'
                ]
            ], 404);
        }

        // Calculate available balance
        $user = $request->user();
        $totalWithdrawals = Transaction::where('user_id', $user->id)
                                      ->where('type', 'WITHDRAWAL')
                                      ->whereIn('status', ['SUCCESS', 'PENDING'])
                                      ->sum('coins');
        
        $availableBalance = $user->total_earnings - $totalWithdrawals;

        // Check if user has sufficient balance
        if ($request->amount > $availableBalance) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INSUFFICIENT_BALANCE',
                    'message' => 'Insufficient balance for withdrawal',
                    'details' => [
                        'available' => $availableBalance,
                        'requested' => $request->amount
                    ]
                ]
            ], 400);
        }

        DB::beginTransaction();
        try {
            // Create withdrawal record
            $withdrawal = Withdrawal::create([
                'user_id' => $user->id,
                'amount' => $request->amount,
                'coins' => $request->amount,
                'bank_account_id' => $bankAccount->id,
                'status' => 'PENDING'
            ]);

            // Create transaction record
            Transaction::create([
                'user_id' => $user->id,
                'type' => 'WITHDRAWAL',
                'amount' => $request->amount,
                'coins' => $request->amount,
                'status' => 'PENDING',
                'reference_id' => $withdrawal->id,
                'reference_type' => 'WITHDRAWAL'
            ]);

            DB::commit();

            $newAvailableBalance = $availableBalance - $request->amount;

            return response()->json([
                'success' => true,
                'withdrawal' => [
                    'id' => 'WD_' . $withdrawal->id,
                    'amount' => (float) $withdrawal->amount,
                    'coins' => $withdrawal->coins,
                    'status' => $withdrawal->status,
                    'bank_account' => [
                        'account_holder' => $bankAccount->account_holder_name,
                        'account_number' => 'XXXX-XXXX-' . substr($bankAccount->account_number, -4),
                        'ifsc_code' => $bankAccount->ifsc_code
                    ],
                    'requested_at' => $withdrawal->created_at->toIso8601String(),
                    'processing_days' => '3-5'
                ],
                'new_available_balance' => $newAvailableBalance
            ]);
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to process withdrawal request'
                ]
            ], 500);
        }
    }

    /**
     * Get withdrawal history
     */
    public function getWithdrawalHistory(Request $request)
    {
        if ($request->user()->user_type !== 'FEMALE') {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'FORBIDDEN',
                    'message' => 'Only female users can access withdrawal history'
                ]
            ], 403);
        }

        $perPage = min($request->get('limit', 20), 50);
        
        $withdrawals = Withdrawal::where('user_id', $request->user()->id)
                                 ->with('bankAccount')
                                 ->orderBy('created_at', 'desc')
                                 ->paginate($perPage);

        return response()->json([
            'success' => true,
            'withdrawals' => $withdrawals->map(function($withdrawal) {
                return [
                    'id' => 'WD_' . $withdrawal->id,
                    'amount' => (float) $withdrawal->amount,
                    'coins' => $withdrawal->coins,
                    'status' => $withdrawal->status,
                    'bank_account' => [
                        'account_holder' => $withdrawal->bankAccount->account_holder_name,
                        'account_number' => 'XXXX-XXXX-' . substr($withdrawal->bankAccount->account_number, -4),
                        'ifsc_code' => $withdrawal->bankAccount->ifsc_code,
                        'bank_name' => $withdrawal->bankAccount->bank_name
                    ],
                    'requested_at' => $withdrawal->created_at->toIso8601String(),
                    'completed_at' => $withdrawal->completed_at ? $withdrawal->completed_at->toIso8601String() : null
                ];
            }),
            'pagination' => [
                'current_page' => $withdrawals->currentPage(),
                'total_pages' => $withdrawals->lastPage(),
                'total_items' => $withdrawals->total(),
                'per_page' => $withdrawals->perPage()
            ]
        ]);
    }
}







