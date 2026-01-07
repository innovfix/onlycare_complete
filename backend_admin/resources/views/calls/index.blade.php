@extends('layouts.app')

@section('title', 'Calls Management')

@section('content')
<div class="space-y-6">
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
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Caller</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Receiver</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Type</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Duration</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Date</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    @foreach($calls as $call)
                    <tr class="hover:bg-gray-50 dark:hover:bg-gray-700">
                        <td class="px-6 py-4 whitespace-nowrap">
                            <div class="text-sm font-medium text-gray-900 dark:text-white">
                                {{ $call->caller->name ?? 'N/A' }}
                            </div>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <div class="text-sm text-gray-900 dark:text-white">{{ $call->receiver->name ?? 'N/A' }}</div>
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

