<?php

namespace App\Http\Controllers;

use App\Models\PushNotification;
use App\Services\NotificationService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class PushNotificationsController extends Controller
{
    protected $notificationService;

    public function __construct(NotificationService $notificationService)
    {
        $this->notificationService = $notificationService;
    }

    /**
     * Display a listing of the resource.
     */
    public function index(Request $request)
    {
        $query = PushNotification::query();

        // Filter by gender
        if ($request->filled('gender')) {
            $query->where('gender', $request->gender);
        }

        // Filter by language
        if ($request->filled('language')) {
            $query->where('language', $request->language);
        }

        // Search
        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function($q) use ($search) {
                $q->where('title', 'like', "%{$search}%")
                  ->orWhere('description', 'like', "%{$search}%");
            });
        }

        $notifications = $query->orderBy('datetime', 'desc')->paginate(50);

        return view('push_notifications.index', compact('notifications'));
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        return view('push_notifications.create');
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        $validated = $request->validate([
            'title' => 'required|string|max:5000',
            'description' => 'required|string|max:5000',
            'gender' => 'required|string|in:all,male,female',
            'language' => 'required|string',
            'logo' => 'nullable|image|mimes:jpeg,png,jpg,gif,svg|max:2048',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif,svg|max:2048',
        ]);

        // Handle file uploads
        if ($request->hasFile('logo')) {
            $validated['logo'] = $request->file('logo')->store('notifications/logos', 'public');
        }

        if ($request->hasFile('image')) {
            $validated['image'] = $request->file('image')->store('notifications/images', 'public');
        }

        $validated['datetime'] = now();

        // Create notification
        $notification = PushNotification::create($validated);

        // Send notification immediately via OneSignal
        $this->notificationService->sendImmediateNotification($notification);

        return redirect()->route('push_notifications.index')
            ->with('success', 'Notification sent successfully!');
    }

    /**
     * Display the specified resource.
     */
    public function show($id)
    {
        $notification = PushNotification::findOrFail($id);
        return view('push_notifications.show', compact('notification'));
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit($id)
    {
        $notification = PushNotification::findOrFail($id);
        return view('push_notifications.edit', compact('notification'));
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, $id)
    {
        $notification = PushNotification::findOrFail($id);

        $validated = $request->validate([
            'title' => 'required|string|max:5000',
            'description' => 'required|string|max:5000',
        ]);

        $notification->update($validated);

        return redirect()->route('push_notifications.index')
            ->with('success', 'Notification updated successfully!');
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy($id)
    {
        $notification = PushNotification::findOrFail($id);

        // Delete associated files
        if ($notification->logo) {
            Storage::disk('public')->delete($notification->logo);
        }
        if ($notification->image) {
            Storage::disk('public')->delete($notification->image);
        }

        $notification->delete();

        return redirect()->route('push_notifications.index')
            ->with('success', 'Notification deleted successfully!');
    }

    /**
     * Send test notification to specific user
     */
    public function sendTestNotification(Request $request)
    {
        $validated = $request->validate([
            'user_id' => 'required|string',
            'title' => 'required|string',
            'message' => 'required|string',
        ]);

        try {
            $response = $this->notificationService->sendPersonalizedNotification(
                $validated['user_id'],
                $validated['title'],
                $validated['message'],
                'test'
            );

            return response()->json([
                'success' => true,
                'user_id' => $validated['user_id'],
                'response_status' => 200,
                'response_body' => $response ?? ['message' => 'Notification sent']
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'error' => $e->getMessage()
            ], 500);
        }
    }
}
