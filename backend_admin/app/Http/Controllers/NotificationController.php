<?php

namespace App\Http\Controllers;

use App\Models\Notification;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class NotificationController extends Controller
{
    public function index(Request $request)
    {
        $query = Notification::with('user');
        
        // Filters
        if ($request->filled('type')) {
            $query->where('notification_type', $request->type);
        }
        
        if ($request->filled('read_status')) {
            if ($request->read_status === 'read') {
                $query->where('is_read', true);
            } elseif ($request->read_status === 'unread') {
                $query->where('is_read', false);
            }
        }
        
        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function($q) use ($search) {
                $q->where('title', 'like', "%{$search}%")
                  ->orWhere('message', 'like', "%{$search}%")
                  ->orWhereHas('user', function($q2) use ($search) {
                      $q2->where('name', 'like', "%{$search}%");
                  });
            });
        }
        
        $notifications = $query->latest()->paginate(50);
        
        // Stats
        $stats = [
            'total' => Notification::count(),
            'unread' => Notification::where('is_read', false)->count(),
            'read' => Notification::where('is_read', true)->count(),
            'by_type' => Notification::select('notification_type', DB::raw('count(*) as count'))
                ->groupBy('notification_type')
                ->get()
                ->pluck('count', 'notification_type'),
        ];
        
        return view('notifications.index', compact('notifications', 'stats'));
    }
    
    public function show($id)
    {
        $notification = Notification::with('user')->findOrFail($id);
        
        return view('notifications.show', compact('notification'));
    }
    
    public function markRead($id)
    {
        $notification = Notification::findOrFail($id);
        $notification->update(['is_read' => true]);
        
        return redirect()->back()
            ->with('success', 'Notification marked as read');
    }
    
    public function markAllRead()
    {
        Notification::where('is_read', false)->update(['is_read' => true]);
        
        return redirect()->back()
            ->with('success', 'All notifications marked as read');
    }
    
    public function destroy($id)
    {
        $notification = Notification::findOrFail($id);
        $notification->delete();
        
        return redirect()->route('notifications.index')
            ->with('success', 'Notification deleted successfully');
    }
}





