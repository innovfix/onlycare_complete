<?php

namespace App\Http\Controllers;

use App\Models\Report;
use Illuminate\Http\Request;

class ReportController extends Controller
{
    public function index(Request $request)
    {
        $query = Report::with(['reporter', 'reportedUser']);
        
        // Filters
        if ($request->filled('type')) {
            $query->where('report_type', $request->type);
        }
        
        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }
        
        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function($q) use ($search) {
                $q->whereHas('reporter', function($q2) use ($search) {
                    $q2->where('name', 'like', "%{$search}%");
                })->orWhereHas('reportedUser', function($q2) use ($search) {
                    $q2->where('name', 'like', "%{$search}%");
                });
            });
        }
        
        $reports = $query->latest()->paginate(50);
        
        // Get stats
        $stats = [
            'total' => Report::count(),
            'pending' => Report::where('status', 'PENDING')->count(),
            'resolved' => Report::where('status', 'RESOLVED')->count(),
            'dismissed' => Report::where('status', 'DISMISSED')->count(),
        ];
        
        return view('reports.index', compact('reports', 'stats'));
    }
    
    public function show($id)
    {
        $report = Report::with(['reporter', 'reportedUser', 'reportedUser.reportsReceived'])
            ->findOrFail($id);
        
        return view('reports.show', compact('report'));
    }
    
    public function resolve(Request $request, $id)
    {
        $report = Report::findOrFail($id);
        
        $report->update([
            'status' => 'RESOLVED',
            'admin_notes' => $request->admin_notes,
            'resolved_at' => now(),
            'resolved_by' => auth()->id()
        ]);
        
        // Handle action on reported user if specified
        if ($request->filled('action')) {
            $user = $report->reportedUser;
            
            switch ($request->action) {
                case 'block':
                    $user->update([
                        'is_blocked' => true,
                        'blocked_reason' => 'Blocked due to report: ' . $report->report_type
                    ]);
                    break;
                // Add more actions as needed
            }
        }
        
        return redirect()->route('reports.index')
            ->with('success', 'Report resolved successfully');
    }
    
    public function dismiss(Request $request, $id)
    {
        $report = Report::findOrFail($id);
        
        $report->update([
            'status' => 'DISMISSED',
            'admin_notes' => $request->admin_notes,
            'resolved_at' => now(),
            'resolved_by' => auth()->id()
        ]);
        
        return redirect()->route('reports.index')
            ->with('success', 'Report dismissed');
    }
}

