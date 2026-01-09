<?php

namespace App\Http\Controllers;

use App\Models\Call;
use Illuminate\Http\Request;

class CallController extends Controller
{
    public function index(Request $request)
    {
        $query = Call::with(['caller', 'receiver']);
        
        // Filters
        if ($request->filled('type')) {
            $query->where('call_type', $request->type);
        }
        
        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }
        
        if ($request->filled('search')) {
            $search = $request->search;
            $query->whereHas('caller', function($q) use ($search) {
                $q->where('name', 'like', "%{$search}%");
            })->orWhereHas('receiver', function($q) use ($search) {
                $q->where('name', 'like', "%{$search}%");
            });
        }
        
        $calls = $query->latest()->paginate(50);
        
        // Get stats
        $stats = [
            'total' => Call::count(),
            'completed' => Call::where('status', 'ENDED')->count(),
            'total_duration' => Call::where('status', 'ENDED')->sum('duration'),
            'avg_duration' => Call::where('status', 'ENDED')->avg('duration') ?? 0,
        ];
        
        return view('calls.index', compact('calls', 'stats'));
    }
    
    public function show($id)
    {
        $call = Call::with(['caller', 'receiver'])->findOrFail($id);
        
        return view('calls.show', compact('call'));
    }
    
    public function analytics()
    {
        // Implement analytics view
        return view('calls.analytics');
    }
    
    /**
     * Delete a call
     */
    public function destroy($id)
    {
        try {
            $call = Call::findOrFail($id);
            
            // Store caller and receiver names for the success message
            $callerName = $call->caller->name ?? 'Unknown';
            $receiverName = $call->receiver->name ?? 'Unknown';
            
            // Delete the call
            $call->delete();
            
            return redirect()
                ->route('calls.index')
                ->with('success', "Call deleted successfully! (Caller: {$callerName}, Receiver: {$receiverName})");
                
        } catch (\Exception $e) {
            return redirect()
                ->route('calls.index')
                ->with('error', 'Failed to delete call: ' . $e->getMessage());
        }
    }
    
    /**
     * Delete multiple calls by user phone number
     */
    public function deleteByPhone(Request $request)
    {
        try {
            $phone = $request->input('phone');
            
            if (empty($phone)) {
                return redirect()
                    ->route('calls.index')
                    ->with('error', 'Phone number is required');
            }
            
            // Find user by phone
            $user = \App\Models\User::where('phone', $phone)
                ->orWhere('phone', 'LIKE', '%' . $phone . '%')
                ->first();
            
            if (!$user) {
                return redirect()
                    ->route('calls.index')
                    ->with('error', "User with phone {$phone} not found");
            }
            
            // Count calls before deletion
            $callCount = Call::where('caller_id', $user->id)
                ->orWhere('receiver_id', $user->id)
                ->count();
            
            if ($callCount === 0) {
                return redirect()
                    ->route('calls.index')
                    ->with('info', "No calls found for {$user->name} ({$phone})");
            }
            
            // Delete all calls for this user
            Call::where('caller_id', $user->id)
                ->orWhere('receiver_id', $user->id)
                ->delete();
            
            return redirect()
                ->route('calls.index')
                ->with('success', "Successfully deleted {$callCount} calls for {$user->name} ({$phone})");
                
        } catch (\Exception $e) {
            return redirect()
                ->route('calls.index')
                ->with('error', 'Failed to delete calls: ' . $e->getMessage());
        }
    }
}

