<?php

namespace App\Http\Controllers;

use App\Models\CoinPackage;
use Illuminate\Http\Request;
use Illuminate\Support\Str;

class CoinPackageController extends Controller
{
    public function index()
    {
        $packages = CoinPackage::orderBy('sort_order')->get();
        
        return view('coin-packages.index', compact('packages'));
    }
    
    public function create()
    {
        return view('coin-packages.create');
    }
    
    public function store(Request $request)
    {
        $validated = $request->validate([
            'coins' => 'required|integer|min:1',
            'price' => 'required|numeric|min:0',
            'bonus_coins' => 'nullable|integer|min:0',
            'description' => 'nullable|string',
            'is_popular' => 'nullable|boolean',
            'is_active' => 'nullable|boolean'
        ]);
        
        $validated['is_popular'] = $request->has('is_popular');
        $validated['is_active'] = $request->has('is_active');
        $validated['bonus_coins'] = $validated['bonus_coins'] ?? 0;
        
        CoinPackage::create($validated);
        
        return redirect()->route('coin-packages.index')
            ->with('success', 'Coin package created successfully');
    }
    
    public function edit($id)
    {
        $package = CoinPackage::findOrFail($id);
        
        return view('coin-packages.edit', compact('package'));
    }
    
    public function update(Request $request, $id)
    {
        $package = CoinPackage::findOrFail($id);
        
        $validated = $request->validate([
            'coins' => 'required|integer|min:1',
            'price' => 'required|numeric|min:0',
            'bonus_coins' => 'nullable|integer|min:0',
            'description' => 'nullable|string',
            'is_popular' => 'nullable|boolean',
            'is_active' => 'nullable|boolean'
        ]);
        
        $validated['is_popular'] = $request->has('is_popular');
        $validated['is_active'] = $request->has('is_active');
        $validated['bonus_coins'] = $validated['bonus_coins'] ?? 0;
        
        $package->update($validated);
        
        return redirect()->route('coin-packages.index')
            ->with('success', 'Coin package updated successfully');
    }
    
    public function destroy($id)
    {
        $package = CoinPackage::findOrFail($id);
        $package->delete();
        
        return redirect()->route('coin-packages.index')
            ->with('success', 'Coin package deleted successfully');
    }
}

