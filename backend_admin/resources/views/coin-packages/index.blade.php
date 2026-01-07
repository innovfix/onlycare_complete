@extends('layouts.app')

@section('title', 'Coin Packages')

@section('content')
<div class="space-y-6">
    <!-- Header with Add Button -->
    <div class="flex justify-between items-center">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">Coin Packages</h2>
        <a href="{{ route('coin-packages.create') }}" class="bg-black hover:bg-black text-white font-medium rounded-lg px-4 py-2">
            Add New Package
        </a>
    </div>

    <!-- Packages Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        @foreach($packages as $package)
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6 {{ $package->is_popular ? 'ring-2 ring-blue-500' : '' }}">
            @if($package->is_popular)
            <div class="bg-black text-white text-xs font-bold px-3 py-1 rounded-full inline-block mb-3">
                POPULAR
            </div>
            @endif
            
            <div class="text-center mb-4">
                <div class="text-4xl font-bold text-gray-900 dark:text-white">
                    {{ number_format($package->coins) }}
                </div>
                <div class="text-sm text-gray-500 dark:text-gray-400">Coins</div>
            </div>
            
            <div class="text-center mb-4">
                <div class="text-3xl font-bold text-black">
                    ₹{{ number_format($package->price, 2) }}
                </div>
                @if($package->bonus_coins > 0)
                <div class="text-sm text-black dark:text-green-400 mt-1">
                    +{{ number_format($package->bonus_coins) }} Bonus Coins
                </div>
                @endif
            </div>
            
            @if($package->description)
            <p class="text-sm text-gray-600 dark:text-gray-400 text-center mb-4">
                {{ $package->description }}
            </p>
            @endif
            
            <div class="flex justify-center space-x-3 mt-4">
                <a href="{{ route('coin-packages.edit', $package->id) }}" 
                   class="text-black hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 font-medium">
                    Edit
                </a>
                <form method="POST" action="{{ route('coin-packages.destroy', $package->id) }}" class="inline">
                    @csrf
                    @method('DELETE')
                    <button type="submit" 
                            onclick="return confirm('Are you sure you want to delete this package?')"
                            class="text-black hover:text-red-900 dark:text-red-400 dark:hover:text-red-300 font-medium">
                        Delete
                    </button>
                </form>
            </div>
            
            <div class="mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
                <div class="text-xs text-gray-500 dark:text-gray-400 text-center">
                    Status: 
                    <span class="font-medium {{ $package->is_active ? 'text-black' : 'text-black' }}">
                        {{ $package->is_active ? 'Active' : 'Inactive' }}
                    </span>
                </div>
            </div>
        </div>
        @endforeach
    </div>
    
    @if($packages->isEmpty())
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-12 text-center">
        <div class="text-gray-400 mb-4">
            <svg class="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
            </svg>
        </div>
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-1">No coin packages found</h3>
        <p class="text-gray-500 dark:text-gray-400">Get started by creating a new coin package.</p>
        <a href="{{ route('coin-packages.create') }}" class="inline-block mt-4 bg-black hover:bg-black text-white font-medium rounded-lg px-4 py-2">
            Create Package
        </a>
    </div>
    @endif
</div>
@endsection







