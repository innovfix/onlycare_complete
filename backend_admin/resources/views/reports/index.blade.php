@extends('layouts.app')

@section('title', 'Reports Management')

@section('content')
<div class="space-y-6">
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Reports</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['total']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Pending</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['pending']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Resolved</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['resolved']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Dismissed</div>
            <div class="text-3xl font-bold text-gray-600 mt-2">{{ number_format($stats['dismissed']) }}</div>
        </div>
    </div>

    <!-- Search and Filters -->
    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="GET" action="{{ route('reports.index') }}" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                <input type="text" name="search" placeholder="Search..." 
                       value="{{ request('search') }}"
                       class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                
                <select name="type" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Types</option>
                    <option value="INAPPROPRIATE_CONTENT" {{ request('type') == 'INAPPROPRIATE_CONTENT' ? 'selected' : '' }}>Inappropriate Content</option>
                    <option value="SPAM" {{ request('type') == 'SPAM' ? 'selected' : '' }}>Spam</option>
                    <option value="HARASSMENT" {{ request('type') == 'HARASSMENT' ? 'selected' : '' }}>Harassment</option>
                    <option value="FAKE_PROFILE" {{ request('type') == 'FAKE_PROFILE' ? 'selected' : '' }}>Fake Profile</option>
                    <option value="OTHER" {{ request('type') == 'OTHER' ? 'selected' : '' }}>Other</option>
                </select>

                <select name="status" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Status</option>
                    <option value="PENDING" {{ request('status') == 'PENDING' ? 'selected' : '' }}>Pending</option>
                    <option value="UNDER_REVIEW" {{ request('status') == 'UNDER_REVIEW' ? 'selected' : '' }}>Under Review</option>
                    <option value="RESOLVED" {{ request('status') == 'RESOLVED' ? 'selected' : '' }}>Resolved</option>
                    <option value="DISMISSED" {{ request('status') == 'DISMISSED' ? 'selected' : '' }}>Dismissed</option>
                </select>

                <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                    Filter
                </button>
            </div>
        </form>
    </div>

    <!-- Reports Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-700">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Reporter</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Reported User</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Type</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Reason</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Date</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    @foreach($reports as $report)
                    <tr class="hover:bg-gray-50 dark:hover:bg-gray-700">
                        <td class="px-6 py-4 whitespace-nowrap">
                            <div class="text-sm font-medium text-gray-900 dark:text-white">
                                {{ $report->reporter->name ?? 'N/A' }}
                            </div>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <div class="text-sm text-gray-900 dark:text-white">{{ $report->reportedUser->name ?? 'N/A' }}</div>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200">
                                {{ str_replace('_', ' ', $report->report_type) }}
                            </span>
                        </td>
                        <td class="px-6 py-4">
                            <div class="text-sm text-gray-900 dark:text-white max-w-xs truncate">{{ $report->reason }}</div>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                                @if($report->status == 'RESOLVED') bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200
                                @elseif($report->status == 'PENDING') bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200
                                @elseif($report->status == 'UNDER_REVIEW') bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200
                                @else bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200
                                @endif">
                                {{ str_replace('_', ' ', $report->status) }}
                            </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                            {{ $report->created_at->format('M d, Y H:i') }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                            @if($report->status == 'PENDING' || $report->status == 'UNDER_REVIEW')
                            <form method="POST" action="{{ route('reports.resolve', $report->id) }}" class="inline">
                                @csrf
                                <button type="submit" class="text-black hover:text-green-900 dark:text-green-400 dark:hover:text-green-300">
                                    Resolve
                                </button>
                            </form>
                            @else
                            <span class="text-gray-400">-</span>
                            @endif
                        </td>
                    </tr>
                    @endforeach
                </tbody>
            </table>
        </div>
        
        <!-- Pagination -->
        <div class="bg-white dark:bg-gray-800 px-4 py-3 border-t border-gray-200 dark:border-gray-700 sm:px-6">
            {{ $reports->appends(request()->query())->links() }}
        </div>
    </div>
</div>
@endsection

