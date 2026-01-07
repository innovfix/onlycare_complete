<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\CoinPackage;
use App\Models\Transaction;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Facades\DB;

class WalletController extends Controller
{
    /**
     * Get coin packages
     */
    public function getPackages(Request $request)
    {
        $packages = CoinPackage::where('is_active', true)
                              ->orderBy('coins', 'asc')
                              ->get();

        return response()->json([
            'success' => true,
            'packages' => $packages->map(function($package) {
                return [
                    'id' => 'PKG_' . $package->id,
                    'coins' => $package->coins,
                    'price' => (float) $package->price,
                    'original_price' => (float) $package->original_price,
                    'discount' => $package->discount,
                    'is_popular' => $package->is_popular,
                    'is_best_value' => $package->is_best_value
                ];
            })
        ]);
    }

    /**
     * Get best coin offers based on user purchase history
     * New users see ₹99 pack, returning users see best offers
     * Uses authenticated user from token
     */
    public function getBestOffers(Request $request)
    {
        $offset = $request->input('offset', 0);
        $limit = $request->input('limit', 10);

        // Get authenticated user from token
        $user = $request->user();
        
        if (!$user) {
            return response()->json([
                'success' => false,
                'message' => 'Unauthorized. Please provide a valid token.'
            ], 401);
        }

        // Check if user has made any purchases (PURCHASE transactions with payment_method)
        $hasPurchases = Transaction::where('user_id', $user->id)
            ->where('type', 'PURCHASE')
            ->whereNotNull('payment_method')
            ->where('status', 'SUCCESS')
            ->exists();

        // Determine which packages to show
        if (!$hasPurchases) {
            // NEW USER: Show ₹99 coin pack
            $packages = CoinPackage::where('is_active', true)
                ->where('price', 99)
                ->orderBy('price', 'asc')
                ->skip($offset)
                ->take($limit)
                ->get();
        } else {
            // RETURNING USER: Show best offers (is_best_value=1) excluding ₹99 packs
            $packages = CoinPackage::where('is_active', true)
                ->where('is_best_value', true)
                ->where('price', '<>', 99)
                ->orderBy('price', 'asc')
                ->skip($offset)
                ->take($limit)
                ->get();
        }

        if ($packages->isEmpty()) {
            return response()->json([
                'success' => false,
                'message' => 'No Best Offer data available.'
            ], 200);
        }

        // Calculate total_count based on user's coin balance
        $totalCount = 100; // Default for users with 0 coins
        if ($user->coin_balance > 0) {
            // Count PURCHASE transactions in last 3 days + 150
            $recentPurchases = Transaction::where('type', 'PURCHASE')
                ->where('status', 'SUCCESS')
                ->where('created_at', '>=', now()->subDays(3))
                ->count();
            $totalCount = $recentPurchases + 150;
        }

        // Format packages for response
        $formattedPackages = $packages->map(function($package) use ($totalCount) {
            // Get discount percentage from package (e.g., 10 means 10%)
            $discountPercentage = $package->discount ?? 0;
            
            // Calculate discount_price: If price is ₹100 and save is 10%, discount_price = ₹90
            // Formula: discount_price = price - (price * discount / 100)
            // Or: discount_price = price * (1 - discount / 100)
            $discountPrice = $package->price;
            if ($discountPercentage > 0) {
                $discountPrice = $package->price * (1 - ($discountPercentage / 100));
                $discountPrice = round($discountPrice, 2);
            }

            // Return the actual package ID (e.g., "PKG_4AGYX9vhUD")
            // Format: Ensure it has PKG_ prefix
            $packageId = str_starts_with($package->id, 'PKG_') ? $package->id : 'PKG_' . $package->id;
            
            return [
                'id' => $packageId, // Actual package ID string
                'price' => (float) $package->price, // ✅ Current price (e.g., ₹100)
                'discount_price' => (float) $discountPrice, // ✅ Calculated discount price (e.g., ₹90 if save is 10%)
                'coins' => $package->coins,
                'save' => (int) $discountPercentage, // ✅ Discount percentage (e.g., 10%)
                'popular' => $package->is_popular ? 1 : 0,
                'total_count' => $totalCount,
                'best_offer' => $package->is_best_value ? 1 : 0,
                'pg' => 'phonepe', // Default payment gateway
                'updated_at' => $package->updated_at->format('Y-m-d H:i:s'),
                'created_at' => $package->created_at->format('Y-m-d H:i:s')
            ];
        });

        return response()->json([
            'success' => true,
            'message' => 'Best Offers listed successfully.',
            'total' => $formattedPackages->count(),
            'data' => $formattedPackages
        ]);
    }

    /**
     * Initiate purchase
     */
    public function initiatePurchase(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'package_id' => 'required|string',
            'payment_method' => 'required|string|in:PhonePe,GooglePay,Paytm,UPI,Card'
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

        $packageId = str_replace('PKG_', '', $request->package_id);
        $package = CoinPackage::find($packageId);

        if (!$package) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Package not found'
                ]
            ], 404);
        }

        // Create transaction
        $transaction = Transaction::create([
            'user_id' => $request->user()->id,
            'type' => 'PURCHASE',
            'amount' => $package->price,
            'coins' => $package->coins,
            'payment_method' => $request->payment_method,
            'status' => 'PENDING',
            'reference_id' => $package->id,
            'reference_type' => 'PACKAGE'
        ]);

        // Generate payment gateway data (placeholder)
        $paymentGatewayUrl = $this->generatePaymentGatewayUrl($transaction);
        $paymentGatewayData = $this->generatePaymentGatewayData($transaction, $request->payment_method);

        return response()->json([
            'success' => true,
            'transaction' => [
                'id' => 'TXN_' . $transaction->id,
                'package_id' => 'PKG_' . $package->id,
                'coins' => $package->coins,
                'amount' => (float) $package->price,
                'payment_method' => $request->payment_method,
                'status' => $transaction->status
            ],
            'payment_gateway_url' => $paymentGatewayUrl,
            'payment_gateway_data' => $paymentGatewayData
        ]);
    }

    /**
     * Verify purchase
     */
    public function verifyPurchase(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'transaction_id' => 'required|string',
            'payment_gateway_id' => 'required|string',
            'status' => 'required|in:SUCCESS,FAILED'
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

        $transactionId = str_replace('TXN_', '', $request->transaction_id);
        $transaction = Transaction::find($transactionId);

        if (!$transaction) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Transaction not found'
                ]
            ], 404);
        }

        if ($transaction->user_id !== $request->user()->id) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'FORBIDDEN',
                    'message' => 'Unauthorized'
                ]
            ], 403);
        }

        DB::beginTransaction();
        try {
            // Update transaction
            $transaction->update([
                'status' => $request->status,
                'payment_gateway_transaction_id' => $request->payment_gateway_id,
                'completed_at' => now()
            ]);

            $newBalance = $request->user()->coin_balance;

            // If successful, add coins to user
            if ($request->status === 'SUCCESS') {
                $user = $request->user();
                $user->increment('coin_balance', $transaction->coins);
                $newBalance = $user->coin_balance;
            }

            DB::commit();

            return response()->json([
                'success' => true,
                'transaction' => [
                    'id' => 'TXN_' . $transaction->id,
                    'status' => $transaction->status,
                    'coins' => $transaction->coins,
                    'amount' => (float) $transaction->amount
                ],
                'new_balance' => $newBalance
            ]);
        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'INTERNAL_ERROR',
                    'message' => 'Failed to verify purchase'
                ]
            ], 500);
        }
    }

    /**
     * Get transaction history
     */
    public function getTransactionHistory(Request $request)
    {
        $perPage = min($request->get('limit', 20), 50);
        $type = $request->get('type'); // Optional filter: PURCHASE, CALL_SPENT, WITHDRAWAL
        $user = $request->user();
        
        $query = Transaction::where('user_id', $user->id);
        
        // Gender-based filtering:
        // FEMALE users: Only show CALL_EARNED (hide CALL_SPENT)
        // MALE users: Only show CALL_SPENT (hide CALL_EARNED)
        if ($user->user_type === 'FEMALE' || $user->gender === 'FEMALE') {
            // Female users should not see CALL_SPENT transactions
            $query->where('type', '!=', 'CALL_SPENT');
        } elseif ($user->user_type === 'MALE' || $user->gender === 'MALE') {
            // Male users should not see CALL_EARNED transactions
            $query->where('type', '!=', 'CALL_EARNED');
        }
        
        // Filter by transaction type if provided (after gender filtering)
        if ($type) {
            $query->where('type', $type);
        }
        
        // Eager load call relationships with caller and receiver
        $transactions = $query->with(['call.caller', 'call.receiver'])
                             ->orderBy('created_at', 'desc')
                             ->paginate($perPage);

        return response()->json([
            'success' => true,
            'data' => $transactions->map(function($transaction) use ($request) {
                $data = [
                    'id' => 'TXN_' . $transaction->id,
                    'type' => $transaction->type,
                    'coins' => abs($transaction->coins), // Always return positive value
                    'is_credit' => $transaction->type === 'CALL_SPENT' ? false : ($transaction->coins > 0), // CALL_SPENT is always debit
                    'amount' => (float) ($transaction->amount ?? 0), // Always include amount
                    'status' => $transaction->status,
                    'description' => $transaction->description,
                    'payment_method' => $transaction->payment_method,
                    'created_at' => $transaction->created_at->toIso8601String(),
                    'date' => $transaction->created_at->format('M d'),
                    'time' => $transaction->created_at->format('H:i A')
                ];

                // Add call session details for CALL_SPENT transactions
                if ($transaction->type === 'CALL_SPENT' && $transaction->reference_type === 'CALL' && $transaction->call) {
                    $call = $transaction->call;
                    // Determine the partner (the other person in the call)
                    $partner = ($call->caller_id === $request->user()->id) 
                        ? $call->receiver 
                        : $call->caller;

                    // Only add call details if partner exists
                    if ($partner) {
                        $data['call'] = [
                            'id' => 'CALL_' . $call->id,
                            'type' => $call->call_type, // AUDIO or VIDEO
                            'duration' => $call->duration ?? 0, // Duration in seconds
                            'duration_formatted' => $this->formatDuration($call->duration ?? 0),
                            'partner' => [
                                'id' => 'USR_' . $partner->id,
                                'name' => $partner->name ?? 'Unknown',
                                'profile_image' => $partner->profile_image,
                                'gender' => $partner->gender ?? $partner->user_type ?? 'UNKNOWN'
                            ]
                        ];
                        
                        // Create a user-friendly title
                        $callTypeLabel = $call->call_type === 'AUDIO' ? 'Audio session' : 'Video session';
                        $data['title'] = $callTypeLabel . ' with ' . ($partner->name ?? 'Unknown');
                        $data['icon_type'] = strtolower($call->call_type); // 'audio' or 'video'
                    } else {
                        // Fallback if partner is missing
                        $data['title'] = 'Call session';
                        $data['icon_type'] = 'transaction';
                    }
                    
                } elseif ($transaction->type === 'PURCHASE') {
                    $data['title'] = 'Wallet Recharge';
                    $data['icon_type'] = 'wallet';
                    
                } elseif ($transaction->type === 'WITHDRAWAL') {
                    $data['title'] = 'Withdrawal';
                    $data['icon_type'] = 'withdrawal';
                    
                } else {
                    // Generic transaction
                    $data['title'] = $transaction->description ?? ucwords(strtolower(str_replace('_', ' ', $transaction->type)));
                    $data['icon_type'] = 'transaction';
                }

                return $data;
            }),
            'pagination' => [
                'current_page' => $transactions->currentPage(),
                'total_pages' => $transactions->lastPage(),
                'total_items' => $transactions->total(),
                'per_page' => $transactions->perPage()
            ]
        ]);
    }

    /**
     * Format duration in seconds to human-readable format
     */
    private function formatDuration($seconds)
    {
        if ($seconds < 60) {
            return $seconds . ' sec';
        } elseif ($seconds < 3600) {
            $minutes = floor($seconds / 60);
            $remainingSeconds = $seconds % 60;
            return $minutes . ' min' . ($remainingSeconds > 0 ? ' ' . $remainingSeconds . ' sec' : '');
        } else {
            $hours = floor($seconds / 3600);
            $minutes = floor(($seconds % 3600) / 60);
            $remainingSeconds = $seconds % 60;
            $result = $hours . ' hr';
            if ($minutes > 0) $result .= ' ' . $minutes . ' min';
            if ($remainingSeconds > 0) $result .= ' ' . $remainingSeconds . ' sec';
            return $result;
        }
    }

    /**
     * Get wallet balance
     */
    public function getBalance(Request $request)
    {
        $user = $request->user();

        // Calculate spent coins
        $totalSpent = Transaction::where('user_id', $user->id)
                                ->where('type', 'CALL_SPENT')
                                ->where('status', 'SUCCESS')
                                ->sum('coins');

        // For female users, calculate available for withdrawal
        $availableForWithdrawal = 0;
        if ($user->user_type === 'FEMALE') {
            $totalWithdrawn = Transaction::where('user_id', $user->id)
                                        ->where('type', 'WITHDRAWAL')
                                        ->where('status', 'SUCCESS')
                                        ->sum('coins');
            
            $availableForWithdrawal = $user->total_earnings - $totalWithdrawn;
        }

        return response()->json([
            'success' => true,
            'coin_balance' => $user->coin_balance,
            'total_earned' => $user->total_earnings,
            'total_spent' => $totalSpent,
            'available_for_withdrawal' => $availableForWithdrawal
        ]);
    }

    /**
     * Generate payment gateway URL (placeholder)
     */
    private function generatePaymentGatewayUrl($transaction)
    {
        // TODO: Implement actual payment gateway integration
        return 'https://payment-gateway.com/pay/' . $transaction->id;
    }

    /**
     * Generate payment gateway data (placeholder)
     */
    private function generatePaymentGatewayData($transaction, $paymentMethod)
    {
        // TODO: Implement actual payment gateway data generation
        return [
            'merchant_id' => 'MERCHANT_123',
            'transaction_id' => 'TXN_' . $transaction->id,
            'amount' => (float) $transaction->amount,
            'currency' => 'INR',
            'return_url' => url('/api/v1/wallet/payment-callback')
        ];
    }
}

