<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;

class UserController extends Controller
{
    public function index(Request $request)
    {
        $query = User::query();
        
        // Filters
        if ($request->filled('gender')) {
            $query->where('gender', $request->gender);
        }
        
        if ($request->filled('status')) {
            switch ($request->status) {
                case 'active':
                    $query->whereDate('updated_at', '>=', now()->subDays(7));
                    break;
                case 'blocked':
                    $query->where('is_blocked', true);
                    break;
                case 'verified':
                    $query->where('is_verified', true);
                    break;
            }
        }
        
        
        if ($request->filled('kyc_status')) {
            $query->where('kyc_status', $request->kyc_status);
        }
        
        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                  ->orWhere('phone', 'like', "%{$search}%")
                  ->orWhere('id', 'like', "%{$search}%");
            });
        }
        
        $users = $query->latest()->paginate(50);
        
        // Get stats
        $stats = [
            'total' => User::count(),
            'active' => User::whereDate('updated_at', '>=', now()->subDays(7))->count(),
            'male' => User::where('gender', 'MALE')->count(),
            'female' => User::where('gender', 'FEMALE')->count(),
        ];
        
        return view('users.index', compact('users', 'stats'));
    }
    
    public function show($id)
    {
        $user = User::with([
            'callsAsCaller',
            'callsAsReceiver',
            'transactions',
            'withdrawals',
            'kycDocuments',
            'bankAccounts'
        ])->findOrFail($id);
        
        // Calculate statistics
        $stats = [
            'total_calls' => $user->callsAsCaller->count() + $user->callsAsReceiver->count(),
            'total_earned' => $user->transactions()
                ->where('type', 'CALL')
                ->where('status', 'SUCCESS')
                ->where('coins', '>', 0)
                ->sum('amount') ?? 0,
        ];
        
        // Get recent calls
        $recentCalls = \App\Models\Call::where(function($q) use ($id) {
            $q->where('caller_id', $id)->orWhere('receiver_id', $id);
        })->with(['caller', 'receiver'])->latest()->limit(10)->get();
        
        // Get recent transactions with gender-based filtering
        // FEMALE users: Only show CALL_EARNED (hide CALL_SPENT)
        // MALE users: Only show CALL_SPENT (hide CALL_EARNED)
        $recentTransactionsQuery = $user->transactions();
        if ($user->user_type === 'FEMALE' || $user->gender === 'FEMALE') {
            // Female users should not see CALL_SPENT transactions
            $recentTransactionsQuery->where('type', '!=', 'CALL_SPENT');
        } elseif ($user->user_type === 'MALE' || $user->gender === 'MALE') {
            // Male users should not see CALL_EARNED transactions
            $recentTransactionsQuery->where('type', '!=', 'CALL_EARNED');
        }
        $recentTransactions = $recentTransactionsQuery->latest()->limit(10)->get();
        
        // Get primary bank account
        $primaryBankAccount = $user->bankAccounts()->where('is_primary', true)->first();
        
        return view('users.show', compact('user', 'stats', 'recentCalls', 'recentTransactions', 'primaryBankAccount'));
    }
    
    public function edit($id)
    {
        $user = User::with('bankAccounts')->findOrFail($id);
        $primaryBankAccount = $user->bankAccounts()->where('is_primary', true)->first();
        return view('users.edit', compact('user', 'primaryBankAccount'));
    }
    
    public function update(Request $request, $id)
    {
        $user = User::findOrFail($id);
        
        $validated = $request->validate([
            'name' => 'required|string|max:100',
            'phone' => 'required|string|max:20',
            'age' => 'nullable|integer|min:18|max:99',
            'gender' => 'required|in:MALE,FEMALE,OTHER',
            'language' => 'nullable|string|max:50',
            'bio' => 'nullable|string|max:500',
            'coin_balance' => 'nullable|integer|min:0',
            'online_status' => 'nullable|in:ONLINE,OFFLINE,BUSY',
            'call_availability' => 'nullable|in:AVAILABLE,UNAVAILABLE,IN_CALL',
            'is_verified' => 'nullable|boolean',
            'is_blocked' => 'nullable|boolean',
            // Bank account fields
            'account_holder_name' => 'nullable|string|max:100',
            'account_number' => 'nullable|string|max:30',
            'ifsc_code' => 'nullable|string|max:11',
            'bank_name' => 'nullable|string|max:100',
            'branch_name' => 'nullable|string|max:100',
            'upi_id' => 'nullable|string|max:100',
            'pancard_name' => 'nullable|string|max:100',
            'pancard_number' => 'nullable|string|max:10',
        ]);
        
        // Map checkbox fields
        $validated['is_verified'] = $request->has('is_verified');
        $validated['is_blocked'] = $request->has('is_blocked');

        // The edit form uses "online_status" and "call_availability" selects, but the DB uses:
        // - is_online (boolean)
        // - is_busy (boolean)
        // - audio_call_enabled (boolean)
        // - video_call_enabled (boolean)
        $updateData = $validated;
        unset($updateData['online_status'], $updateData['call_availability']);

        // Online status -> is_online/is_busy
        $onlineStatus = $request->input('online_status');
        if ($onlineStatus === 'ONLINE') {
            $updateData['is_online'] = true;
            $updateData['is_busy'] = false;
            $updateData['online_datetime'] = now();
        } elseif ($onlineStatus === 'OFFLINE') {
            $updateData['is_online'] = false;
            $updateData['is_busy'] = false;
        } elseif ($onlineStatus === 'BUSY') {
            $updateData['is_online'] = true;
            $updateData['is_busy'] = true;
            $updateData['online_datetime'] = now();
        }

        // Call availability -> audio/video enabled + busy
        $callAvailability = $request->input('call_availability');
        if ($callAvailability === 'AVAILABLE') {
            $updateData['audio_call_enabled'] = true;
            $updateData['video_call_enabled'] = true;
            // Available implies not busy unless admin explicitly sets BUSY above
            if ($onlineStatus !== 'BUSY') {
                $updateData['is_busy'] = false;
            }
        } elseif ($callAvailability === 'UNAVAILABLE') {
            $updateData['audio_call_enabled'] = false;
            $updateData['video_call_enabled'] = false;
            if ($onlineStatus !== 'BUSY') {
                $updateData['is_busy'] = false;
            }
        } elseif ($callAvailability === 'IN_CALL') {
            $updateData['is_busy'] = true;
        }

        $user->update($updateData);
        
        // Handle bank account update/create
        $primaryBankAccount = \App\Models\BankAccount::where('user_id', $user->id)
            ->where('is_primary', true)
            ->first();
        
        $accountHolderName = trim($request->input('account_holder_name', ''));
        $accountNumber = trim($request->input('account_number', ''));
        $ifscCode = trim($request->input('ifsc_code', ''));
        
        if ($primaryBankAccount) {
            // Update existing bank account
            // Use provided values or keep existing values for required fields
            $bankData = [
                'account_holder_name' => !empty($accountHolderName) ? $accountHolderName : $primaryBankAccount->account_holder_name,
                'account_number' => !empty($accountNumber) ? $accountNumber : $primaryBankAccount->account_number,
                'ifsc_code' => !empty($ifscCode) ? $ifscCode : $primaryBankAccount->ifsc_code,
            ];
            
            // Always update optional fields (form always sends them, even if empty)
            $bankName = trim($request->input('bank_name', ''));
            $branchName = trim($request->input('branch_name', ''));
            $upiId = trim($request->input('upi_id', ''));
            $pancardName = trim($request->input('pancard_name', ''));
            $pancardNumber = trim($request->input('pancard_number', ''));
            
            $bankData['bank_name'] = !empty($bankName) ? $bankName : null;
            $bankData['branch_name'] = !empty($branchName) ? $branchName : null;
            $bankData['upi_id'] = !empty($upiId) ? $upiId : null;
            $bankData['pancard_name'] = !empty($pancardName) ? $pancardName : null;
            $bankData['pancard_number'] = !empty($pancardNumber) ? $pancardNumber : null;
            
            $primaryBankAccount->update($bankData);
        } elseif (!empty($accountHolderName) && !empty($accountNumber) && !empty($ifscCode)) {
            // Create new bank account only if all required fields are provided
            $bankData = [
                'id' => 'BANK_' . time() . rand(1000, 9999),
                'user_id' => $user->id,
                'account_holder_name' => $accountHolderName,
                'account_number' => $accountNumber,
                'ifsc_code' => $ifscCode,
                'bank_name' => trim($request->input('bank_name', '')) ?: null,
                'branch_name' => trim($request->input('branch_name', '')) ?: null,
                'upi_id' => trim($request->input('upi_id', '')) ?: null,
                'pancard_name' => trim($request->input('pancard_name', '')) ?: null,
                'pancard_number' => trim($request->input('pancard_number', '')) ?: null,
                'is_primary' => true,
                'is_verified' => false
            ];
            
            \App\Models\BankAccount::create($bankData);
        }
        
        return redirect()->route('users.show', $id)
            ->with('success', 'User updated successfully');
    }
    
    public function block(Request $request, $id)
    {
        $user = User::findOrFail($id);
        
        $user->update([
            'is_blocked' => true,
            'blocked_reason' => $request->input('reason', 'Blocked by admin')
        ]);
        
        return back()->with('success', 'User blocked successfully');
    }
    
    public function unblock($id)
    {
        $user = User::findOrFail($id);
        
        $user->update([
            'is_blocked' => false,
            'blocked_reason' => null
        ]);
        
        return back()->with('success', 'User unblocked successfully');
    }
    
    public function destroy($id)
    {
        $user = User::findOrFail($id);
        $user->delete();
        
        return redirect()->route('users.index')
            ->with('success', 'User deleted successfully');
    }

    /**
     * Add or reduce coins to/from user account
     */
    public function addCoins(Request $request, $id)
    {
        $user = User::findOrFail($id);
        
        $validated = $request->validate([
            'coins' => 'required|integer|min:-1000000|max:1000000|not_in:0',
            'reason' => 'nullable|string|max:200'
        ], [
            'coins.not_in' => 'Coin amount cannot be zero',
            'coins.min' => 'Coin amount cannot be less than -1,000,000',
            'coins.max' => 'Coin amount cannot be more than 1,000,000'
        ]);
        
        $coinAmount = $validated['coins'];
        $isAddition = $coinAmount > 0;
        $absAmount = abs($coinAmount);
        
        // Update user balance
        if ($isAddition) {
            $user->increment('coin_balance', $absAmount);
        } else {
            $user->decrement('coin_balance', $absAmount);
        }
        
        // Determine transaction type based on operation
        // Use BONUS for additions and REFUND for reductions (taking coins back)
        $transactionType = $isAddition ? 'BONUS' : 'REFUND';
        $description = $validated['reason'] ?? ($isAddition ? 'Coins added by admin' : 'Coins reduced by admin');
        
        // Log the transaction
        \App\Models\Transaction::create([
            'id' => 'TXN_' . time() . rand(1000, 9999),
            'user_id' => $user->id,
            'type' => $transactionType,
            'amount' => 0,
            'coins' => $coinAmount, // Keep the sign for transaction history
            'description' => $description,
            'status' => 'SUCCESS',
            'payment_method' => 'ADMIN',
            'created_at' => now(),
            'updated_at' => now()
        ]);
        
        $action = $isAddition ? 'added' : 'reduced';
        return back()->with('success', "Successfully {$action} {$absAmount} coins " . ($isAddition ? 'to' : 'from') . " user account. New balance: " . number_format($user->fresh()->coin_balance) . " coins");
    }

    /**
     * Generate API token for user
     */
    public function generateToken($id)
    {
        $user = User::findOrFail($id);
        
        // Delete all existing tokens for this user
        $user->tokens()->delete();
        
        // Generate a new Sanctum token
        $token = $user->createToken('admin-generated-token')->plainTextToken;
        
        // Store the plain text token in the api_token field for display purposes
        $user->update([
            'api_token' => $token
        ]);
        
        return back()->with('success', 'API token generated successfully');
    }

    /**
     * Toggle user online status
     */
    public function toggleOnlineStatus($id)
    {
        $user = User::findOrFail($id);
        
        // Toggle the is_online status
        $newStatus = !$user->is_online;
        $user->update([
            'is_online' => $newStatus
        ]);
        
        return response()->json([
            'success' => true,
            'is_online' => $newStatus,
            'message' => $newStatus ? 'User is now online' : 'User is now offline'
        ]);
    }
}

