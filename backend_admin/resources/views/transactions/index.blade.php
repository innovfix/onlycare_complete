@extends('layouts.app')

@section('title', 'Transactions')

@section('content')
<div class="space-y-6">
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Transactions</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['total']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Amount</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">₹{{ number_format($stats['total_amount'], 2) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Completed</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['completed']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Pending</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['pending']) }}</div>
        </div>
    </div>

    <!-- Search and Filters -->
    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="GET" action="{{ route('transactions.index') }}" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                <input type="text" name="search" placeholder="Search by user..." 
                       value="{{ request('search') }}"
                       class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                
                       <select name="type" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                        <option value="">All Types</option>
                        <option value="PURCHASE" {{ request('type') == 'PURCHASE' ? 'selected' : '' }}>Purchase</option>
                        <option value="CALL_SPENT" {{ request('type') == 'CALL_SPENT' ? 'selected' : '' }}>Call Spent</option>
                        <option value="CALL_EARNED" {{ request('type') == 'CALL_EARNED' ? 'selected' : '' }}>Call Earned</option>
                        <option value="WITHDRAWAL" {{ request('type') == 'WITHDRAWAL' ? 'selected' : '' }}>Withdrawal</option>
                        <option value="BONUS" {{ request('type') == 'BONUS' ? 'selected' : '' }}>Bonus</option>
                        <option value="REFUND" {{ request('type') == 'REFUND' ? 'selected' : '' }}>Refund</option>
                        <option value="GIFT" {{ request('type') == 'GIFT' ? 'selected' : '' }}>Gift</option>
                    </select>

                <select name="status" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Status</option>
                    <option value="COMPLETED" {{ request('status') == 'COMPLETED' ? 'selected' : '' }}>Completed</option>
                    <option value="PENDING" {{ request('status') == 'PENDING' ? 'selected' : '' }}>Pending</option>
                    <option value="FAILED" {{ request('status') == 'FAILED' ? 'selected' : '' }}>Failed</option>
                </select>

                <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                    Filter
                </button>
            </div>
        </form>
    </div>

    <!-- Transactions Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-700">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">User</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Type</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Amount</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Coins</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Date</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    @foreach($transactions as $transaction)
                    <tr class="hover:bg-gray-50 dark:hover:bg-gray-700">
                        <td class="px-6 py-4 whitespace-nowrap">
                            <div class="text-sm font-medium text-gray-900 dark:text-white">
                                {{ $transaction->user->name ?? 'N/A' }}
                            </div>
                            <div class="text-sm text-gray-500 dark:text-gray-400">{{ $transaction->user->phone ?? '' }}</div>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                                @if($transaction->type == 'PURCHASE') bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200
                                @elseif($transaction->type == 'CALL_SPENT') bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200
                                @elseif($transaction->type == 'CALL_EARNED') bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200
                                @elseif($transaction->type == 'CALL') bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200
                                @elseif($transaction->type == 'WITHDRAWAL') bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200
                                @elseif($transaction->type == 'BONUS') bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200
                                @elseif($transaction->type == 'GIFT') bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200
                                @else bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200
                                @endif">
                                {{ $transaction->type }}
                            </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            @if($transaction->type == 'CALL_SPENT')
                                {{-- Male users: Don't show amount for CALL_SPENT --}}
                                -
                            @elseif($transaction->type == 'CALL_EARNED')
                                {{-- Female users: Show amount earned --}}
                                <span class="text-green-600 dark:text-green-400 font-medium">
                                    ₹{{ number_format($transaction->amount ?? 0, 2) }}
                                </span>
                            @else
                                {{-- Other transaction types: Show amount if available --}}
                                @if($transaction->amount)
                                    ₹{{ number_format($transaction->amount, 2) }}
                                @else
                                    -
                                @endif
                            @endif
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">
                            @if($transaction->type == 'CALL_SPENT')
                                {{-- Male users: Show coins spent --}}
                                <span class="text-red-600 dark:text-red-400 font-medium">
                                    {{ number_format($transaction->coins ?? 0) }}
                                </span>
                            @elseif($transaction->type == 'CALL_EARNED')
                                {{-- Female users: Don't show coins for CALL_EARNED --}}
                                -
                            @else
                                {{-- Other transaction types: Show coins if available --}}
                                @if($transaction->coins)
                                    {{ number_format($transaction->coins) }}
                                @else
                                    -
                                @endif
                            @endif
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap">
                            <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                                @if($transaction->status == 'COMPLETED' || $transaction->status == 'SUCCESS') bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200
                                @elseif($transaction->status == 'PENDING') bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200
                                @else bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200
                                @endif">
                                {{ $transaction->status }}
                            </span>
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                            {{ $transaction->created_at->format('M d, Y H:i') }}
                        </td>
                    </tr>
                    @endforeach
                </tbody>
            </table>
        </div>
        
        <!-- Pagination -->
        <div class="bg-white dark:bg-gray-800 px-4 py-3 border-t border-gray-200 dark:border-gray-700 sm:px-6">
            {{ $transactions->appends(request()->query())->links() }}
        </div>
    </div>
</div>
@endsection

