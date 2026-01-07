<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\BankAccount;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class BankAccountController extends Controller
{
    /**
     * Get bank accounts
     */
    public function index(Request $request)
    {
        $bankAccounts = BankAccount::where('user_id', $request->user()->id)
                                  ->orderBy('is_primary', 'desc')
                                  ->orderBy('created_at', 'desc')
                                  ->get();

        return response()->json([
            'success' => true,
            'bank_accounts' => $bankAccounts->map(function($account) {
                return $this->formatBankAccountResponse($account);
            })
        ]);
    }

    /**
     * Add bank account
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'account_holder_name' => 'required|string|max:100',
            'account_number' => 'required|string|max:20',
            'ifsc_code' => 'required|string|size:11',
            'upi_id' => 'nullable|string|max:100'
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

        // Get bank name from IFSC (simplified - in production use actual IFSC API)
        $bankName = $this->getBankNameFromIfsc($request->ifsc_code);

        // Check if this is the first bank account
        $isFirstAccount = BankAccount::where('user_id', $request->user()->id)->count() === 0;

        $bankAccount = BankAccount::create([
            'user_id' => $request->user()->id,
            'account_holder_name' => $request->account_holder_name,
            'account_number' => $request->account_number,
            'ifsc_code' => strtoupper($request->ifsc_code),
            'bank_name' => $bankName,
            'upi_id' => $request->upi_id,
            'is_primary' => $isFirstAccount,
            'is_verified' => false
        ]);

        return response()->json([
            'success' => true,
            'bank_account' => $this->formatBankAccountResponse($bankAccount)
        ]);
    }

    /**
     * Update bank account
     */
    public function update(Request $request, $accountId)
    {
        $id = str_replace('BANK_', '', $accountId);
        
        $bankAccount = BankAccount::where('id', $id)
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

        $validator = Validator::make($request->all(), [
            'account_holder_name' => 'sometimes|string|max:100',
            'upi_id' => 'nullable|string|max:100',
            'is_primary' => 'sometimes|boolean'
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

        // If UPI ID is being updated, verify it using Paysprint
        if ($request->has('upi_id') && !empty($request->upi_id)) {
            // Get AuthController instance to use validation method
            $authController = new \App\Http\Controllers\Api\AuthController();
            $upiValidation = $authController->validateUpiIdWithPaysprint($request->upi_id);

            if (!$upiValidation['valid']) {
                return response()->json([
                    'success' => false,
                    'error' => [
                        'code' => 'VALIDATION_ERROR',
                        'message' => 'Invalid or unverified UPI ID.'
                    ]
                ], 422);
            }

            // Optional: Verify name matches PAN card name
            $user = $request->user();
            $res = $upiValidation['response'];
            $verifiedNameRaw = strtoupper(trim($res['data']['full_name'] ?? ''));
            $providedNameRaw = strtoupper(trim($user->pancard_name ?? ''));

            if (!empty($providedNameRaw) && !empty($verifiedNameRaw)) {
                $verifiedWords = preg_split('/\s+/', $verifiedNameRaw);
                $providedWords = preg_split('/\s+/', $providedNameRaw);

                $matchFound = false;
                foreach ($providedWords as $providedWord) {
                    if (in_array($providedWord, $verifiedWords)) {
                        $matchFound = true;
                        break;
                    }
                }

                if (!$matchFound) {
                    return response()->json([
                        'success' => false,
                        'error' => [
                            'code' => 'NAME_MISMATCH',
                            'message' => 'PAN name and UPI holder name do not match.'
                        ]
                    ], 422);
                }
            }
        }

        $updateData = [];
        if ($request->has('account_holder_name')) {
            $updateData['account_holder_name'] = $request->account_holder_name;
        }
        if ($request->has('upi_id')) {
            $updateData['upi_id'] = $request->upi_id;
        }

        // If setting as primary, unset other primary accounts
        if ($request->has('is_primary') && $request->is_primary) {
            BankAccount::where('user_id', $request->user()->id)
                      ->where('id', '!=', $id)
                      ->update(['is_primary' => false]);
            $updateData['is_primary'] = true;
        }

        $bankAccount->update($updateData);

        return response()->json([
            'success' => true,
            'bank_account' => $this->formatBankAccountResponse($bankAccount)
        ]);
    }

    /**
     * Delete bank account
     */
    public function destroy(Request $request, $accountId)
    {
        $id = str_replace('BANK_', '', $accountId);
        
        $bankAccount = BankAccount::where('id', $id)
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

        // Don't allow deletion if it's the only bank account
        $accountCount = BankAccount::where('user_id', $request->user()->id)->count();
        if ($accountCount === 1) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'FORBIDDEN',
                    'message' => 'Cannot delete the only bank account'
                ]
            ], 403);
        }

        $wasPrimary = $bankAccount->is_primary;
        $bankAccount->delete();

        // If deleted account was primary, set another account as primary
        if ($wasPrimary) {
            $nextAccount = BankAccount::where('user_id', $request->user()->id)->first();
            if ($nextAccount) {
                $nextAccount->update(['is_primary' => true]);
            }
        }

        return response()->json([
            'success' => true,
            'message' => 'Bank account deleted successfully'
        ]);
    }

    /**
     * Format bank account response
     */
    private function formatBankAccountResponse($account)
    {
        return [
            'id' => 'BANK_' . $account->id,
            'account_holder_name' => $account->account_holder_name,
            'account_number' => $account->account_number,
            'ifsc_code' => $account->ifsc_code,
            'bank_name' => $account->bank_name,
            'branch_name' => $account->branch_name,
            'upi_id' => $account->upi_id,
            'pancard_name' => $account->pancard_name,
            'pancard_number' => $account->pancard_number,
            'is_primary' => $account->is_primary,
            'is_verified' => $account->is_verified
        ];
    }

    /**
     * Get bank name from IFSC code
     */
    private function getBankNameFromIfsc($ifsc)
    {
        // Simplified bank name mapping
        // In production, use actual IFSC API
        $bankCodes = [
            'SBIN' => 'State Bank of India',
            'HDFC' => 'HDFC Bank',
            'ICIC' => 'ICICI Bank',
            'AXIS' => 'Axis Bank',
            'PUNB' => 'Punjab National Bank',
            'UBIN' => 'Union Bank of India',
            'CNRB' => 'Canara Bank',
            'BARB' => 'Bank of Baroda',
        ];

        $code = substr($ifsc, 0, 4);
        return $bankCodes[$code] ?? 'Other Bank';
    }
}







