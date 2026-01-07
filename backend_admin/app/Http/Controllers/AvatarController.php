<?php

namespace App\Http\Controllers;

use App\Models\Avatar;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class AvatarController extends Controller
{
    /**
     * Display a listing of avatars
     */
    public function index(Request $request)
    {
        $query = Avatar::query();
        
        // Filters
        if ($request->filled('gender')) {
            $query->where('gender', $request->gender);
        }
        
        $avatars = $query->orderBy('gender', 'asc')
            ->orderBy('created_at', 'desc')
            ->paginate(50);
        
        // Get stats
        $stats = [
            'total' => Avatar::count(),
            'male' => Avatar::where('gender', 'MALE')->count(),
            'female' => Avatar::where('gender', 'FEMALE')->count(),
        ];
        
        return view('avatars.index', compact('avatars', 'stats'));
    }
    
    /**
     * Show the form for creating a new avatar
     */
    public function create()
    {
        return view('avatars.create');
    }
    
    /**
     * Store a newly created avatar
     */
    public function store(Request $request)
    {
        $validated = $request->validate([
            'gender' => 'required|in:MALE,FEMALE',
            'image' => 'required|image|mimes:jpeg,png,jpg,gif|max:5120', // 5MB max
        ]);
        
        // Upload image
        $data = [
            'gender' => $validated['gender']
        ];
        
        if ($request->hasFile('image')) {
            $image = $request->file('image');
            $filename = 'avatar_' . time() . '_' . uniqid() . '.' . $image->getClientOriginalExtension();
            $path = $image->storeAs('avatars', $filename, 'public');
            $data['image_url'] = Storage::url($path);
        }
        
        Avatar::create($data);
        
        return redirect()->route('avatars.index')
            ->with('success', 'Avatar created successfully');
    }
    
    /**
     * Show the form for editing the specified avatar
     */
    public function edit($id)
    {
        $avatar = Avatar::findOrFail($id);
        return view('avatars.edit', compact('avatar'));
    }
    
    /**
     * Update the specified avatar
     */
    public function update(Request $request, $id)
    {
        $avatar = Avatar::findOrFail($id);
        
        $validated = $request->validate([
            'gender' => 'required|in:MALE,FEMALE',
            'image' => 'nullable|image|mimes:jpeg,png,jpg,gif|max:5120', // 5MB max, optional for update
        ]);
        
        $data = [
            'gender' => $validated['gender']
        ];
        
        // Upload new image if provided
        if ($request->hasFile('image')) {
            // Delete old image
            if ($avatar->image_url) {
                // Extract path from URL (e.g., /storage/avatars/file.jpg -> avatars/file.jpg)
                $oldPath = str_replace('/storage/', '', parse_url($avatar->image_url, PHP_URL_PATH));
                if ($oldPath) {
                    Storage::disk('public')->delete($oldPath);
                }
            }
            
            // Upload new image
            $image = $request->file('image');
            $filename = 'avatar_' . time() . '_' . uniqid() . '.' . $image->getClientOriginalExtension();
            $path = $image->storeAs('avatars', $filename, 'public');
            $data['image_url'] = Storage::url($path);
        }
        
        $avatar->update($data);
        
        return redirect()->route('avatars.index')
            ->with('success', 'Avatar updated successfully');
    }
    
    /**
     * Remove the specified avatar
     */
    public function destroy($id)
    {
        $avatar = Avatar::findOrFail($id);
        
        // Delete image file
        if ($avatar->image_url) {
            // Extract path from URL (e.g., /storage/avatars/file.jpg -> avatars/file.jpg)
            $path = str_replace('/storage/', '', parse_url($avatar->image_url, PHP_URL_PATH));
            if ($path) {
                Storage::disk('public')->delete($path);
            }
        }
        
        $avatar->delete();
        
        return redirect()->route('avatars.index')
            ->with('success', 'Avatar deleted successfully');
    }
}
