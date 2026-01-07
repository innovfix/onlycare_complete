@extends('layouts.app')

@section('title', 'KYC Management')

@section('content')
<div class="space-y-6">
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Submissions</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['total']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Pending</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['pending']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Verified</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['verified']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Rejected</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['rejected']) }}</div>
        </div>
    </div>

    <!-- Search and Filters -->
    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="GET" action="{{ route('kyc.index') }}" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <input type="text" name="search" placeholder="Search by user..." 
                       value="{{ request('search') }}"
                       class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                
                <select name="status" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Status</option>
                    <option value="PENDING" {{ request('status') == 'PENDING' ? 'selected' : '' }}>Pending</option>
                    <option value="APPROVED" {{ request('status') == 'APPROVED' ? 'selected' : '' }}>Approved</option>
                    <option value="REJECTED" {{ request('status') == 'REJECTED' ? 'selected' : '' }}>Rejected</option>
                </select>

                <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                    Filter
                </button>
            </div>
        </form>
    </div>

    <!-- KYC Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-700">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">User</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Document Type</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Document Number</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Documents</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Date</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    @foreach($kycDocuments as $kyc)
                    <tr class="hover:bg-gray-50 dark:hover:bg-gray-700">
                        <td class="px-6 py-4 whitespace-nowrap">
                            <div class="text-sm font-medium text-gray-900 dark:text-white">
                                {{ $kyc->user->name ?? 'N/A' }}
                            </div>
                            <div class="text-sm text-gray-500 dark:text-gray-400">{{ $kyc->user->phone ?? '' }}</div>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {{ $kyc->document_type }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {{ $kyc->document_number }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            @if($kyc->document_url)
                            <a href="{{ $kyc->document_url }}" target="_blank" class="text-black hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 text-xs">
                                View Document
                            </a>
                            @else
                            <span class="text-gray-400 text-xs">No document</span>
                            @endif
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                                @if($kyc->status == 'APPROVED') bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200
                                @elseif($kyc->status == 'PENDING') bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200
                                @else bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200
                                @endif">
                                {{ $kyc->status }}
                            </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                            {{ $kyc->submitted_at ? $kyc->submitted_at->format('M d, Y H:i') : 'N/A' }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                            @if($kyc->user && $kyc->status == 'PENDING')
                            <a href="{{ route('kyc.review', $kyc->user->id) }}" class="text-black hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-3">
                                Review
                            </a>
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
            {{ $kycDocuments->appends(request()->query())->links() }}
        </div>
    </div>
</div>
@endsection

