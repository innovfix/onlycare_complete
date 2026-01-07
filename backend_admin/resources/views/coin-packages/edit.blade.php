@extends('layouts.app')

@section('title', 'Edit Coin Package')

@section('content')
<div class="max-w-3xl mx-auto">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">Edit Coin Package</h2>
        
        <form method="POST" action="{{ route('coin-packages.update', $package->id) }}" class="space-y-6">
            @csrf
            @method('PUT')
            
            <div>
                <label for="coins" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Coins Amount *
                </label>
                <input type="number" name="coins" id="coins" required value="{{ $package->coins }}"
                       class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
            </div>
            
            <div>
                <label for="price" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Price (₹) *
                </label>
                <input type="number" step="0.01" name="price" id="price" required value="{{ $package->price }}"
                       class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
            </div>
            
            <div>
                <label for="bonus_coins" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Bonus Coins
                </label>
                <input type="number" name="bonus_coins" id="bonus_coins" value="{{ $package->bonus_coins }}"
                       class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
            </div>
            
            <div>
                <label for="description" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Description
                </label>
                <textarea name="description" id="description" rows="3"
                          class="w-full bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">{{ $package->description }}</textarea>
            </div>
            
            <div class="flex items-center">
                <input type="checkbox" name="is_popular" id="is_popular" value="1" {{ $package->is_popular ? 'checked' : '' }}
                       class="w-4 h-4 text-black bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600">
                <label for="is_popular" class="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                    Mark as Popular
                </label>
            </div>
            
            <div class="flex items-center">
                <input type="checkbox" name="is_active" id="is_active" value="1" {{ $package->is_active ? 'checked' : '' }}
                       class="w-4 h-4 text-black bg-gray-100 border-gray-300 rounded focus:ring-blue-500 dark:focus:ring-blue-600 dark:ring-offset-gray-800 focus:ring-2 dark:bg-gray-700 dark:border-gray-600">
                <label for="is_active" class="ml-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                    Active
                </label>
            </div>
            
            <div class="flex justify-end space-x-3 pt-6 border-t border-gray-200 dark:border-gray-700">
                <a href="{{ route('coin-packages.index') }}" 
                   class="bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium rounded-lg px-6 py-2 hover:bg-gray-300 dark:hover:bg-gray-600">
                    Cancel
                </a>
                <button type="submit" 
                        class="bg-black hover:bg-black text-white font-medium rounded-lg px-6 py-2">
                    Update Package
                </button>
            </div>
        </form>
    </div>
</div>
@endsection







