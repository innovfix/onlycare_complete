<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Notification;
use Illuminate\Http\Request;

class NotificationController extends Controller
{
    /**
     * Get notifications
     */
    public function getNotifications(Request $request)
    {
        $perPage = min($request->get('limit', 20), 50);
        
        $query = Notification::where('user_id', $request->user()->id);

        // Filter by unread
        if ($request->has('unread') && $request->unread === 'true') {
            $query->where('is_read', false);
        }

        $notifications = $query->orderBy('created_at', 'desc')
                              ->paginate($perPage);

        // Count unread notifications
        $unreadCount = Notification::where('user_id', $request->user()->id)
                                  ->where('is_read', false)
                                  ->count();

        return response()->json([
            'success' => true,
            'notifications' => $notifications->map(function($notification) {
                return [
                    'id' => 'NOTIF_' . $notification->id,
                    'title' => $notification->title,
                    'message' => $notification->message,
                    'type' => $notification->type,
                    'reference_id' => $notification->reference_id,
                    'is_read' => $notification->is_read,
                    'created_at' => $notification->created_at->toIso8601String()
                ];
            }),
            'unread_count' => $unreadCount,
            'pagination' => [
                'current_page' => $notifications->currentPage(),
                'total_pages' => $notifications->lastPage(),
                'total_items' => $notifications->total(),
                'per_page' => $notifications->perPage()
            ]
        ]);
    }

    /**
     * Mark notification as read
     */
    public function markAsRead(Request $request, $notificationId)
    {
        $id = str_replace('NOTIF_', '', $notificationId);
        
        $notification = Notification::where('id', $id)
                                   ->where('user_id', $request->user()->id)
                                   ->first();

        if (!$notification) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Notification not found'
                ]
            ], 404);
        }

        $notification->update(['is_read' => true]);

        return response()->json([
            'success' => true,
            'message' => 'Notification marked as read'
        ]);
    }

    /**
     * Mark all notifications as read
     */
    public function markAllAsRead(Request $request)
    {
        Notification::where('user_id', $request->user()->id)
                   ->where('is_read', false)
                   ->update(['is_read' => true]);

        return response()->json([
            'success' => true,
            'message' => 'All notifications marked as read'
        ]);
    }
}







