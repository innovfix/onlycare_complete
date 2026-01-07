<?php

namespace App\Http\Controllers;

use App\Models\Gifts;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\Validator;

class GiftsController extends Controller
{
    /**
     * Display a listing of gifts
     */
    public function index(Request $request)
    {
        $query = Gifts::query();
        
        // Search functionality
        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function($q) use ($search) {
                $q->where('coins', 'like', "%{$search}%")
                  ->orWhere('gift_icon', 'like', "%{$search}%");
            });
        }
        
        $gifts = $query->orderBy('created_at', 'desc')->paginate(50);
        
        return view('gifts.index', compact('gifts'));
    }
    
    /**
     * Show the form for creating a new gift
     */
    public function create()
    {
        return view('gifts.create');
    }
    
    /**
     * Store a newly created gift
     */
    public function store(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'gift_icon' => 'required|image|mimes:jpeg,png,jpg,gif,avif|max:2048',
            'coins' => 'required|numeric|min:1'
        ], [
            'gift_icon.required' => 'Gift icon is required.',
            'gift_icon.image' => 'Gift icon must be an image.',
            'gift_icon.mimes' => 'Gift icon must be jpeg, png, jpg, gif, or avif.',
            'gift_icon.max' => 'Gift icon size must not exceed 2MB.',
            'coins.required' => 'Coins field is required.',
            'coins.numeric' => 'Coins must be a number.',
            'coins.min' => 'Coins must be at least 1.'
        ]);
        
        if ($validator->fails()) {
            return redirect()->back()
                ->withErrors($validator)
                ->withInput();
        }
        
        // Store gift icon
        $giftIconPath = $request->file('gift_icon')->store('gifts', 'public');
        
        // Let Laravel handle timestamps automatically (stores in UTC)
        Gifts::create([
            'gift_icon' => $giftIconPath,
            'coins' => $request->coins
        ]);
        
        return redirect()->route('gifts.index')
            ->with('success', 'Gift created successfully');
    }
    
    /**
     * Show the form for editing the specified gift
     */
    public function edit($id)
    {
        $gift = Gifts::findOrFail($id);
        return view('gifts.edit', compact('gift'));
    }
    
    /**
     * Update the specified gift
     */
    public function update(Request $request, $id)
    {
        $gift = Gifts::findOrFail($id);
        
        $validator = Validator::make($request->all(), [
            'gift_icon' => 'nullable|image|mimes:jpeg,png,jpg,gif,avif|max:2048',
            'coins' => 'required|numeric|min:1'
        ], [
            'gift_icon.image' => 'Gift icon must be an image.',
            'gift_icon.mimes' => 'Gift icon must be jpeg, png, jpg, gif, or avif.',
            'gift_icon.max' => 'Gift icon size must not exceed 2MB.',
            'coins.required' => 'Coins field is required.',
            'coins.numeric' => 'Coins must be a number.',
            'coins.min' => 'Coins must be at least 1.'
        ]);
        
        if ($validator->fails()) {
            return redirect()->back()
                ->withErrors($validator)
                ->withInput();
        }
        
        // Update gift icon if new file uploaded
        if ($request->hasFile('gift_icon')) {
            // Delete old image
            if ($gift->gift_icon && Storage::disk('public')->exists($gift->gift_icon)) {
                Storage::disk('public')->delete($gift->gift_icon);
            }
            $giftIconPath = $request->file('gift_icon')->store('gifts', 'public');
            $gift->gift_icon = $giftIconPath;
        }
        
        $gift->coins = $request->coins;
        // Let Laravel handle updated_at automatically (stores in UTC)
        $gift->save();
        
        return redirect()->route('gifts.index')
            ->with('success', 'Gift updated successfully');
    }
    
    /**
     * Remove the specified gift
     */
    public function destroy($id)
    {
        $gift = Gifts::findOrFail($id);
        
        // Delete gift icon file
        if ($gift->gift_icon && Storage::disk('public')->exists($gift->gift_icon)) {
            Storage::disk('public')->delete($gift->gift_icon);
        }
        
        $gift->delete();
        
        return redirect()->route('gifts.index')
            ->with('success', 'Gift deleted successfully');
    }
}
