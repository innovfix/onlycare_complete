@extends('layouts.app')

@section('title', 'Avatar Management')

@section('content')
<div class="space-y-6">
    <!-- Header with Add Button -->
    <div class="flex justify-between items-center">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">Avatar Management</h2>
        <a href="{{ route('avatars.create') }}" class="bg-black hover:bg-black text-white font-medium rounded-lg px-4 py-2">
            Add New Avatar
        </a>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Avatars</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['total']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Male Avatars</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['male']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Female Avatars</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['female']) }}</div>
        </div>
    </div>

    <!-- Filters -->
    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="GET" action="{{ route('avatars.index') }}" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <select name="gender" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Genders</option>
                    <option value="MALE" {{ request('gender') == 'MALE' ? 'selected' : '' }}>Male</option>
                    <option value="FEMALE" {{ request('gender') == 'FEMALE' ? 'selected' : '' }}>Female</option>
                </select>

                <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                    Filter
                </button>
            </div>
        </form>
    </div>

    <!-- Avatars Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        @foreach($avatars as $avatar)
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
            <div class="text-center mb-4">
                <img src="{{ $avatar->image_url }}" alt="Avatar" 
                     class="w-32 h-32 rounded-full mx-auto object-cover border-4 
                            {{ $avatar->gender == 'MALE' ? 'border-blue-500' : 'border-pink-500' }}">
            </div>
            
            <div class="text-center mb-4">
                <div class="text-sm text-gray-500 dark:text-gray-400">
                    {{ $avatar->gender }}
                </div>
                <div class="text-xs text-gray-400 dark:text-gray-500 mt-2">
                    Created: {{ $avatar->created_at->format('M d, Y') }}
                </div>
                @if($avatar->updated_at != $avatar->created_at)
                <div class="text-xs text-gray-400 dark:text-gray-500">
                    Updated: {{ $avatar->updated_at->format('M d, Y') }}
                </div>
                @endif
            </div>
            
            <div class="flex justify-center space-x-3 mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
                <a href="{{ route('avatars.edit', $avatar->id) }}" 
                   class="text-black hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 font-medium">
                    Edit
                </a>
                <form method="POST" action="{{ route('avatars.destroy', $avatar->id) }}" class="inline">
                    @csrf
                    @method('DELETE')
                    <button type="submit" 
                            onclick="return confirm('Are you sure you want to delete this avatar?')"
                            class="text-black hover:text-red-900 dark:text-red-400 dark:hover:text-red-300 font-medium">
                        Delete
                    </button>
                </form>
            </div>
        </div>
        @endforeach
    </div>
    
    @if($avatars->isEmpty())
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-12 text-center">
        <div class="text-gray-400 mb-4">
            <svg class="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
        </div>
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">No avatars found</h3>
        <p class="text-gray-500 dark:text-gray-400 mb-4">Get started by creating a new avatar.</p>
        <a href="{{ route('avatars.create') }}" class="bg-black hover:bg-black text-white font-medium rounded-lg px-6 py-2 inline-block">
            Add New Avatar
        </a>
    </div>
    @endif
    
    <!-- Pagination -->
    @if($avatars->hasPages())
    <div class="bg-white dark:bg-gray-800 px-4 py-3 border-t border-gray-200 dark:border-gray-700 sm:px-6 rounded-lg">
        {{ $avatars->appends(request()->query())->links() }}
    </div>
    @endif
</div>
@endsection

