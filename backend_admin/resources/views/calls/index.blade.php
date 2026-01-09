@extends('layouts.app')

@section('title', 'Calls Management')

@section('content')
<div class="space-y-6">
    <!-- Success/Error Messages -->
    @if(session('success'))
    <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded relative" role="alert">
        <span class="block sm:inline">{{ session('success') }}</span>
    </div>
    @endif
    
    @if(session('error'))
    <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
        <span class="block sm:inline">{{ session('error') }}</span>
    </div>
    @endif
    
    @if(session('info'))
    <div class="bg-blue-100 border border-blue-400 text-blue-700 px-4 py-3 rounded relative" role="alert">
        <span class="block sm:inline">{{ session('info') }}</span>
    </div>
    @endif

    <!-- Delete Calls by Phone Number -->
    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border-2 border-red-200 dark:border-red-800">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">🗑️ Delete All Calls for User</h3>
        <form method="POST" action="{{ route('calls.delete-by-phone') }}" 
              onsubmit="return confirm('⚠️ WARNING: This will DELETE ALL calls for this user. This action CANNOT be undone! Are you sure?');"
              class="flex gap-4 items-end">
            @csrf
            <div class="flex-1">
                <label for="phone" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Phone Number
                </label>
                <input type="text" 
                       name="phone" 
                       id="phone" 
                       placeholder="Enter phone number (e.g., 6203224780)"
                       class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2 w-full"
                       required>
                <p class="mt-1 text-xs text-gray-500 dark:text-gray-400">
                    This will delete ALL calls (as caller & receiver) for the user with this phone number.
                </p>
            </div>
            <button type="submit" 
                    class="font-medium rounded-lg px-6 py-2 bg-red-600 hover:bg-red-700 text-white transition-colors">
                🗑️ Delete All Calls
            </button>
        </form>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Calls</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['total']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Completed</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['completed']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Duration</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ gmdate("H:i:s", $stats['total_duration']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Avg Duration</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ gmdate("i:s", $stats['avg_duration']) }}</div>
        </div>
    </div>

    <!-- Search and Filters -->
    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="GET" action="{{ route('calls.index') }}" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                <input type="text" name="search" placeholder="Search by user..." 
                       value="{{ request('search') }}"
                       class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                
                <select name="type" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Types</option>
                    <option value="AUDIO" {{ request('type') == 'AUDIO' ? 'selected' : '' }}>Audio</option>
                    <option value="VIDEO" {{ request('type') == 'VIDEO' ? 'selected' : '' }}>Video</option>
                </select>

                <select name="status" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Status</option>
                    <option value="PENDING" {{ request('status') == 'PENDING' ? 'selected' : '' }}>Pending</option>
                    <option value="CONNECTING" {{ request('status') == 'CONNECTING' ? 'selected' : '' }}>Connecting</option>
                    <option value="ONGOING" {{ request('status') == 'ONGOING' ? 'selected' : '' }}>Ongoing</option>
                    <option value="ENDED" {{ request('status') == 'ENDED' ? 'selected' : '' }}>Ended</option>
                    <option value="MISSED" {{ request('status') == 'MISSED' ? 'selected' : '' }}>Missed</option>
                    <option value="REJECTED" {{ request('status') == 'REJECTED' ? 'selected' : '' }}>Rejected</option>
                    <option value="CANCELLED" {{ request('status') == 'CANCELLED' ? 'selected' : '' }}>Cancelled</option>
                </select>

                <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                    Filter
                </button>
            </div>
        </form>
    </div>

    <!-- Calls Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-700">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Call ID</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Caller</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Receiver</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Type</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Duration</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Date</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    @foreach($calls as $call)
                    <tr class="hover:bg-gray-50 dark:hover:bg-gray-700">
                        <td class="px-6 py-4 whitespace-nowrap">
                            <div class="text-xs font-mono text-gray-600 dark:text-gray-400">
                                {{ $call->id }}
                            </div>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <div class="text-sm font-medium text-gray-900 dark:text-white">
                                {{ $call->caller->name ?? 'N/A' }}
                            </div>
                            <div class="text-xs text-gray-500 dark:text-gray-400">
                                {{ $call->caller->phone ?? 'N/A' }}
                            </div>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <div class="text-sm text-gray-900 dark:text-white">{{ $call->receiver->name ?? 'N/A' }}</div>
                            <div class="text-xs text-gray-500 dark:text-gray-400">
                                {{ $call->receiver->phone ?? 'N/A' }}
                            </div>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                                {{ $call->call_type == 'VIDEO' ? 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200' : 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200' }}">
                                {{ $call->call_type }}
                            </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                                @if($call->status == 'ENDED') bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200
                                @elseif($call->status == 'ONGOING') bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200
                                @elseif($call->status == 'CONNECTING') bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200
                                @elseif($call->status == 'PENDING') bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200
                                @elseif($call->status == 'MISSED') bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200
                                @elseif($call->status == 'REJECTED') bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200
                                @elseif($call->status == 'CANCELLED') bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200
                                @else bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200
                                @endif">
                                {{ $call->status }}
                            </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {{ gmdate("i:s", $call->duration ?? 0) }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                            {{ $call->created_at->diffForHumans() }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm">
                            <form action="{{ route('calls.destroy', $call->id) }}" 
                                  method="POST" 
                                  onsubmit="return confirm('⚠️ Are you sure you want to delete this call?\n\nCaller: {{ $call->caller->name ?? 'N/A' }}\nReceiver: {{ $call->receiver->name ?? 'N/A' }}\nType: {{ $call->call_type }}\n\nThis action cannot be undone!');"
                                  class="inline">
                                @csrf
                                @method('DELETE')
                                <button type="submit" 
                                        class="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-300 font-medium">
                                    🗑️ Delete
                                </button>
                            </form>
                        </td>
                    </tr>
                    @endforeach
                </tbody>
            </table>
        </div>
        
        <!-- Pagination -->
        <div class="bg-white dark:bg-gray-800 px-4 py-3 border-t border-gray-200 dark:border-gray-700 sm:px-6">
            {{ $calls->appends(request()->query())->links() }}
        </div>
    </div>
</div>
@endsection

