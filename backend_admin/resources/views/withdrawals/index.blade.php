@extends('layouts.app')

@section('title', 'Withdrawals Management')

@section('content')
<div class="space-y-6">
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Withdrawals</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['total']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Pending</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['pending']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Approved</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['approved']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Amount</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">₹{{ number_format($stats['total_amount'], 2) }}</div>
        </div>
    </div>

    <!-- Search and Filters -->
    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="GET" action="{{ route('withdrawals.index') }}" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <input type="text" name="search" placeholder="Search by user..." 
                       value="{{ request('search') }}"
                       class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                
                <select name="status" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Status</option>
                    <option value="PENDING" {{ request('status') == 'PENDING' ? 'selected' : '' }}>Pending</option>
                    <option value="PROCESSING" {{ request('status') == 'PROCESSING' ? 'selected' : '' }}>Processing</option>
                    <option value="COMPLETED" {{ request('status') == 'COMPLETED' ? 'selected' : '' }}>Completed</option>
                    <option value="REJECTED" {{ request('status') == 'REJECTED' ? 'selected' : '' }}>Rejected</option>
                </select>

                <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                    Filter
                </button>
            </div>
        </form>
    </div>

    <!-- Withdrawals Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-700">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">User</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Amount</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Bank Details</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Date</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    @foreach($withdrawals as $withdrawal)
                    <tr class="hover:bg-gray-50 dark:hover:bg-gray-700">
                        <td class="px-6 py-4 whitespace-nowrap">
                            <div class="text-sm font-medium text-gray-900 dark:text-white">
                                {{ $withdrawal->user->name ?? 'N/A' }}
                            </div>
                            <div class="text-sm text-gray-500 dark:text-gray-400">{{ $withdrawal->user->phone ?? '' }}</div>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900 dark:text-white">
                            ₹{{ number_format($withdrawal->amount, 2) }}
                        </td>
                        <td class="px-6 py-4">
                            @if($withdrawal->bankAccount)
                                <div class="text-sm text-gray-900 dark:text-white">{{ $withdrawal->bankAccount->account_holder_name }}</div>
                                <div class="text-sm text-gray-500 dark:text-gray-400">{{ $withdrawal->bankAccount->account_number }}</div>
                                <div class="text-sm text-gray-500 dark:text-gray-400">IFSC: {{ $withdrawal->bankAccount->ifsc_code }}</div>
                            @else
                                <div class="text-sm text-gray-500 dark:text-gray-400">N/A</div>
                            @endif
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                                @if($withdrawal->status == 'COMPLETED') bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200
                                @elseif($withdrawal->status == 'PENDING') bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200
                                @elseif($withdrawal->status == 'PROCESSING') bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200
                                @else bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200
                                @endif">
                                {{ $withdrawal->status }}
                            </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                            {{ $withdrawal->requested_at ? $withdrawal->requested_at->format('M d, Y H:i') : 'N/A' }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                            @if($withdrawal->status == 'PENDING')
                            <form method="POST" action="{{ route('withdrawals.approve', $withdrawal->id) }}" class="inline">
                                @csrf
                                <button type="submit" class="text-black hover:text-green-900 dark:text-green-400 dark:hover:text-green-300 mr-3">
                                    Approve
                                </button>
                            </form>
                            <form method="POST" action="{{ route('withdrawals.reject', $withdrawal->id) }}" class="inline">
                                @csrf
                                <button type="submit" class="text-black hover:text-red-900 dark:text-red-400 dark:hover:text-red-300">
                                    Reject
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
            {{ $withdrawals->appends(request()->query())->links() }}
        </div>
    </div>
</div>
@endsection

