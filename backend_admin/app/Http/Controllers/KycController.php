<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\KycDocument;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class KycController extends Controller
{
    public function index(Request $request)
    {
        $query = KycDocument::with('user');
        
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
        
        $kycDocuments = $query->latest()->paginate(50);
        
        // Get stats
        $stats = [
            'total' => KycDocument::count(),
            'pending' => KycDocument::where('status', 'PENDING')->count(),
            'verified' => KycDocument::where('status', 'APPROVED')->count(),
            'rejected' => KycDocument::where('status', 'REJECTED')->count(),
        ];
        
        return view('kyc.index', compact('kycDocuments', 'stats'));
    }
    
    public function review($userId)
    {
        $user = User::with('kycDocuments')->findOrFail($userId);
        
        return view('kyc.review', compact('user'));
    }
    
    public function approve(Request $request, $userId)
    {
        $user = User::with('kycDocuments')->findOrFail($userId);
        
        // Approve all KYC documents for the user
        $user->kycDocuments()->update([
            'status' => 'APPROVED',
            'verified_at' => now(),
            'verified_by' => Auth::guard('admin')->id()
        ]);
        
        // Update user KYC status
        $user->update([
            'kyc_status' => 'VERIFIED'
        ]);
        
        return redirect()->route('kyc.index')
            ->with('success', 'KYC approved successfully');
    }
    
    public function reject(Request $request, $userId)
    {
        $user = User::with('kycDocuments')->findOrFail($userId);
        
        // Reject all KYC documents for the user
        $user->kycDocuments()->update([
            'status' => 'REJECTED',
            'rejected_reason' => $request->rejected_reason ?? 'Document verification failed',
            'verified_at' => now(),
            'verified_by' => Auth::guard('admin')->id()
        ]);
        
        // Update user KYC status
        $user->update([
            'kyc_status' => 'REJECTED'
        ]);
        
        return redirect()->route('kyc.index')
            ->with('success', 'KYC rejected. User can resubmit documents.');
    }
}

