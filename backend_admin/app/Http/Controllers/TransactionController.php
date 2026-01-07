<?php

namespace App\Http\Controllers;

use App\Models\Transaction;
use Illuminate\Http\Request;

class TransactionController extends Controller
{
    public function index(Request $request)
    {
        $query = Transaction::with('user');
        
        // Filters
        if ($request->filled('type')) {
            $query->where('type', $request->type);
        }
        
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
        
        $transactions = $query->latest()->paginate(50);
        
        // Get stats
        $stats = [
            'total' => Transaction::count(),
            'total_amount' => Transaction::where('status', 'COMPLETED')->sum('amount') ?? 0,
            'completed' => Transaction::where('status', 'COMPLETED')->count(),
            'pending' => Transaction::where('status', 'PENDING')->count(),
        ];
        
        return view('transactions.index', compact('transactions', 'stats'));
    }
    
    public function show($id)
    {
        $transaction = Transaction::with('user')->findOrFail($id);
        
        return view('transactions.show', compact('transaction'));
    }
    
    public function export(Request $request)
    {
        // Implement CSV export
        // For now, redirect back
        return back()->with('info', 'Export functionality will be implemented');
    }
}

