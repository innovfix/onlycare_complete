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
}

