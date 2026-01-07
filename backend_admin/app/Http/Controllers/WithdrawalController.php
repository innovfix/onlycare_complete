<?php

namespace App\Http\Controllers;

use App\Models\Withdrawal;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class WithdrawalController extends Controller
{
    public function index(Request $request)
    {
        $query = Withdrawal::with(['user', 'bankAccount']);
        
        // Filters
        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }
        
        if ($request->filled('search')) {
            $search = $request->search;
            $query->whereHas('user', function($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                  ->orWhere('phone', 'like', "%{$search}%");
            });
        }
        
        $withdrawals = $query->latest()->paginate(50);
        
        // Get stats
        $stats = [
            'total' => Withdrawal::count(),
            'pending' => Withdrawal::where('status', 'PENDING')->count(),
            'approved' => Withdrawal::whereIn('status', ['PROCESSING', 'COMPLETED'])->count(),
            'total_amount' => Withdrawal::where('status', 'COMPLETED')->sum('amount') ?? 0,
        ];
        
        return view('withdrawals.index', compact('withdrawals', 'stats'));
    }
    
    public function show($id)
    {
        $withdrawal = Withdrawal::with(['user', 'bankAccount', 'user.kycDocuments'])
            ->findOrFail($id);
        
        return view('withdrawals.show', compact('withdrawal'));
    }
    
    public function approve(Request $request, $id)
    {
        $withdrawal = Withdrawal::findOrFail($id);
        
        if ($withdrawal->status !== 'PENDING') {
            return back()->with('error', 'Withdrawal is not pending');
        }
        
        $withdrawal->update([
            'status' => 'APPROVED',
            'processed_at' => now(),
            'admin_notes' => $request->admin_notes
        ]);
        
        return redirect()->route('withdrawals.index')
            ->with('success', 'Withdrawal approved successfully');
    }
    
    public function reject(Request $request, $id)
    {
        $withdrawal = Withdrawal::findOrFail($id);
        
        if ($withdrawal->status !== 'PENDING') {
            return back()->with('error', 'Withdrawal is not pending');
        }
        
        // Return coins to user
        $user = $withdrawal->user;
        $user->increment('coin_balance', $withdrawal->coins);
        
        $withdrawal->update([
            'status' => 'REJECTED',
            'processed_at' => now(),
            'rejected_reason' => $request->rejected_reason,
            'admin_notes' => $request->admin_notes
        ]);
        
        return redirect()->route('withdrawals.index')
            ->with('success', 'Withdrawal rejected and coins returned to user');
    }
    
    public function complete(Request $request, $id)
    {
        $withdrawal = Withdrawal::findOrFail($id);
        
        if ($withdrawal->status !== 'APPROVED') {
            return back()->with('error', 'Withdrawal is not approved');
        }
        
        $withdrawal->update([
            'status' => 'COMPLETED',
            'completed_at' => now()
        ]);
        
        return back()->with('success', 'Withdrawal marked as completed');
    }
}

