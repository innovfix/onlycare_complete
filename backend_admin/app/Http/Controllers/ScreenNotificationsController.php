<?php

namespace App\Http\Controllers;

use App\Models\ScreenNotifications;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class ScreenNotificationsController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index(Request $request)
    {
        $query = ScreenNotifications::query();

        // Filter by day
        if ($request->filled('day') && $request->day !== 'all') {
            $query->where('day', $request->day);
        }

        // Filter by gender
        if ($request->filled('gender')) {
            $query->where('gender', $request->gender);
        }

        // Filter by language
        if ($request->filled('language')) {
            $query->where('language', $request->language);
        }

        $notifications = $query->orderBy('day')->orderBy('time')->paginate(50);

        return view('screen_notifications.index', compact('notifications'));
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        return view('screen_notifications.create');
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'required|string|max:5000',
            'time' => 'required|date_format:H:i',
            'day' => 'required|string|in:Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday,all',
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

        ScreenNotifications::create($validated);

        return redirect()->route('screen_notifications.index')
            ->with('success', 'Scheduled notification created successfully!');
    }

    /**
     * Display the specified resource.
     */
    public function show($id)
    {
        $notification = ScreenNotifications::findOrFail($id);
        return view('screen_notifications.show', compact('notification'));
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit($id)
    {
        $notification = ScreenNotifications::findOrFail($id);
        return view('screen_notifications.edit', compact('notification'));
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, $id)
    {
        $notification = ScreenNotifications::findOrFail($id);

        $validated = $request->validate([
            'title' => 'required|string|max:255',
            'description' => 'required|string|max:5000',
            'time' => 'required|date_format:H:i',
            'day' => 'required|string|in:Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday,all',
            'gender' => 'required|string|in:all,male,female',
            'language' => 'required|string',
            'logo' => 'nullable|image|mimes:jpeg,png,jpg,gif,svg|max:2048',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif,svg|max:2048',
        ]);

        // Handle file uploads
        if ($request->hasFile('logo')) {
            // Delete old logo
            if ($notification->logo) {
                Storage::disk('public')->delete($notification->logo);
            }
            $validated['logo'] = $request->file('logo')->store('notifications/logos', 'public');
        }

        if ($request->hasFile('image')) {
            // Delete old image
            if ($notification->image) {
                Storage::disk('public')->delete($notification->image);
            }
            $validated['image'] = $request->file('image')->store('notifications/images', 'public');
        }

        $notification->update($validated);

        return redirect()->route('screen_notifications.index')
            ->with('success', 'Scheduled notification updated successfully!');
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy($id)
    {
        $notification = ScreenNotifications::findOrFail($id);

        // Delete associated files
        if ($notification->logo) {
            Storage::disk('public')->delete($notification->logo);
        }
        if ($notification->image) {
            Storage::disk('public')->delete($notification->image);
        }

        $notification->delete();

        return redirect()->route('screen_notifications.index')
            ->with('success', 'Scheduled notification deleted successfully!');
    }
}
