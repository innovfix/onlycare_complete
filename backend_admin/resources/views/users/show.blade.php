@extends('layouts.app')

@section('title', 'User Details')

@section('content')
<div class="space-y-6">
    <!-- User Header -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <div class="flex items-center justify-between">
        <div class="flex items-center space-x-4">
            @if($user->profile_image)
                <img src="{{ $user->profile_image }}" alt="{{ $user->name }}" class="w-20 h-20 rounded-full object-cover">
            @else
                <div class="w-20 h-20 bg-gray-300 dark:bg-gray-600 rounded-full flex items-center justify-center text-3xl font-bold text-white">
                    {{ substr($user->name, 0, 1) }}
                </div>
            @endif
                <div>
                    <h2 class="text-2xl font-bold text-gray-900 dark:text-white">{{ $user->name }}</h2>
                    <p class="text-gray-500 dark:text-gray-400">{{ $user->phone }}</p>
                    <div class="mt-1">
                        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                            {{ $user->is_blocked ? 'bg-gray-100 text-black dark:bg-black dark:text-white' : 'bg-gray-100 text-black dark:bg-black dark:text-white' }}">
                            {{ $user->is_blocked ? 'BLOCKED' : 'ACTIVE' }}
                        </span>
                        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800 dark:bg-white dark:text-white">
                            {{ $user->gender }}
                        </span>
                    </div>
                </div>
            </div>
            <div class="flex space-x-3">
                <a href="{{ route('users.edit', $user->id) }}" class="btn btn-primary">
                    Edit User
                </a>
                @if($user->is_blocked)
                <form method="POST" action="{{ route('users.unblock', $user->id) }}">
                    @csrf
                    <button type="submit" class="btn btn-success">
                        Unblock User
                    </button>
                </form>
                @else
                <form method="POST" action="{{ route('users.block', $user->id) }}">
                    @csrf
                    <button type="submit" class="btn btn-danger">
                        Block User
                    </button>
                </form>
                @endif
            </div>
        </div>
    </div>

    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm mb-2">Coin Balance</div>
            <div class="flex items-center justify-between">
                <div class="text-3xl font-bold text-black dark:text-white">{{ number_format($user->coin_balance) }}</div>
                <button onclick="showAddCoinsModal()" class="btn btn-primary whitespace-nowrap">
                    ⚙️ Manage Coins
                </button>
            </div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Calls</div>
            <div class="text-3xl font-bold text-gray-900 dark:text-white mt-2">{{ number_format($stats['total_calls']) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Total Earned</div>
            <div class="text-3xl font-bold text-black mt-2">₹{{ number_format($stats['total_earned'], 2) }}</div>
        </div>
        <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
            <div class="text-gray-500 dark:text-gray-400 text-sm">Average Rating</div>
            <div class="text-3xl font-bold text-yellow-500 mt-2">{{ number_format($user->rating ?? 0, 1) }}</div>
        </div>
    </div>

    <!-- User Information -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Basic Info -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Basic Information</h3>
            <dl class="space-y-3">
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">User ID</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white font-mono">{{ $user->id }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Age</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $user->age ?? 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Gender</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $user->gender }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Language</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $user->language ?? 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Member Since</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $user->created_at->format('M d, Y') }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Last Active</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $user->updated_at->diffForHumans() }}</dd>
                </div>
            </dl>
        </div>

        <!-- Bank Account Info -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Bank Account Details</h3>
            <dl class="space-y-3">
                @if($primaryBankAccount)
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Account Holder Name</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $primaryBankAccount->account_holder_name ?: 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Account Number</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $primaryBankAccount->account_number ?: 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">IFSC Code</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $primaryBankAccount->ifsc_code ?: 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Bank Name</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $primaryBankAccount->bank_name ?: 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Branch Name</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $primaryBankAccount->branch_name ?: 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">UPI ID</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $primaryBankAccount->upi_id ?: 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">PAN Card Name</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $primaryBankAccount->pancard_name ?: 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">PAN Card Number</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $primaryBankAccount->pancard_number ?: 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Verified</dt>
                    <dd class="text-sm font-medium">
                        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                            {{ $primaryBankAccount->is_verified ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200' }}">
                            {{ $primaryBankAccount->is_verified ? 'Yes' : 'No' }}
                        </span>
                    </dd>
                </div>
                @else
                <div>
                    <dd class="text-sm text-gray-500 dark:text-gray-400">No bank account details available</dd>
                </div>
                @endif
            </dl>
        </div>
    </div>

    <!-- Account Status -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Account Status</h3>
            <dl class="space-y-3">
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Verified</dt>
                    <dd class="text-sm font-medium">
                        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                            {{ $user->is_verified ? 'bg-gray-100 text-black dark:bg-black dark:text-white' : 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200' }}">
                            {{ $user->is_verified ? 'Yes' : 'No' }}
                        </span>
                    </dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">KYC Status</dt>
                    <dd class="text-sm font-medium">
                        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                            @if($user->kyc_status == 'VERIFIED') bg-gray-100 text-black dark:bg-black dark:text-white
                            @elseif($user->kyc_status == 'PENDING') bg-gray-100 text-black dark:bg-black dark:text-white
                            @else bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200
                            @endif">
                            {{ $user->kyc_status }}
                        </span>
                    </dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Online Status</dt>
                    <dd class="text-sm font-medium">
                        <div class="flex items-center gap-2">
                            <span id="onlineStatusBadge" class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                                {{ $user->is_online ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200' }}">
                                {{ $user->is_online ? 'Online' : 'Offline' }}
                            </span>
                        </div>
                    </dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Busy Status</dt>
                    <dd class="text-sm font-medium">
                        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                            {{ $user->is_busy ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200' : 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200' }}">
                            {{ $user->is_busy ? 'Busy' : 'Available' }}
                        </span>
                    </dd>
                </div>
                @php
                    $callAvailabilityLabel = $user->is_busy
                        ? 'In Call'
                        : (($user->audio_call_enabled || $user->video_call_enabled) ? 'Available' : 'Unavailable');
                    $callAvailabilityClass = $user->is_busy
                        ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                        : (($user->audio_call_enabled || $user->video_call_enabled)
                            ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                            : 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200');
                @endphp
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Call Availability</dt>
                    <dd class="text-sm font-medium">
                        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full {{ $callAvailabilityClass }}">
                            {{ $callAvailabilityLabel }}
                        </span>
                    </dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">Referral Code</dt>
                    <dd class="text-sm font-medium text-gray-900 dark:text-white">{{ $user->referral_code ?? 'N/A' }}</dd>
                </div>
                <div>
                    <dt class="text-sm text-gray-500 dark:text-gray-400">API Token</dt>
                    <dd class="text-xs font-mono text-gray-900 dark:text-white break-all">
                        @if(!empty($user->api_token))
                            <div class="flex items-center gap-2 mb-2">
                                <span id="apiToken" class="flex-1">{{ $user->api_token }}</span>
                            </div>
                            <div class="flex items-center gap-2">
                                <button onclick="copyToken(event)" class="btn btn-secondary text-xs px-3 py-1 whitespace-nowrap">
                                    📋 Copy
                                </button>
                                <form method="POST" action="{{ route('users.generate-token', $user->id) }}" class="inline">
                                    @csrf
                                    <button type="submit" class="btn btn-warning text-xs px-3 py-1 whitespace-nowrap">
                                        🔄 Regenerate
                                    </button>
                                </form>
                            </div>
                        @else
                            <div class="flex items-center gap-2">
                                <span class="text-gray-400">No token generated</span>
                                <form method="POST" action="{{ route('users.generate-token', $user->id) }}" class="inline">
                                    @csrf
                                    <button type="submit" class="btn btn-primary text-xs px-3 py-1">
                                        Generate Token
                                    </button>
                                </form>
                            </div>
                        @endif
                    </dd>
                </div>
            </dl>
        </div>
    </div>

    <!-- Recent Calls -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Recent Calls</h3>
        </div>
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-700">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Type</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">With</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Duration</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Date</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    @forelse($recentCalls as $call)
                    <tr>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ $call->call_type }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {{ $call->caller_id == $user->id ? $call->receiver->name : $call->caller->name }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ $call->status }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ gmdate("i:s", $call->duration ?? 0) }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ $call->created_at->diffForHumans() }}</td>
                    </tr>
                    @empty
                    <tr>
                        <td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400">No calls found</td>
                    </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>

    <!-- Recent Transactions -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Recent Transactions</h3>
        </div>
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-700">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Type</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Amount</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Coins</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Status</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase">Date</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    @forelse($recentTransactions as $transaction)
                    <tr>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ $transaction->type }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                            {{ $transaction->amount ? '₹' . number_format($transaction->amount, 2) : '-' }}
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ number_format($transaction->coins) }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ $transaction->status }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ $transaction->created_at->diffForHumans() }}</td>
                    </tr>
                    @empty
                    <tr>
                        <td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400">No transactions found</td>
                    </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Add/Reduce Coins Modal -->
<div id="addCoinsModal" class="hidden fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center" style="z-index: 9999; display: none;">
    <div class="bg-gray-800 rounded-lg shadow-2xl w-full max-w-md mx-4">
        <!-- Modal Header -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-gray-700">
            <h3 id="modalTitle" class="text-xl font-bold text-white">Add/Reduce Coins</h3>
            <button onclick="closeAddCoinsModal()" 
                class="text-gray-400 hover:text-white transition">
                <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>
        </div>
        
        <!-- Modal Body -->
        <form method="POST" action="{{ route('users.add-coins', $user->id) }}">
            @csrf
            <div class="px-6 py-6 space-y-5">
                <!-- Current Balance Info -->
                <div class="bg-gray-700 rounded-lg p-4 border border-gray-600">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-sm text-gray-400">Current Balance</p>
                            <p id="currentBalance" class="text-2xl font-bold text-green-400">{{ number_format($user->coin_balance) }} coins</p>
                        </div>
                        <div class="text-4xl">💰</div>
                    </div>
                </div>

                <!-- Number of Coins Input -->
                <div>
                    <label class="form-label text-gray-300">
                        Number of Coins <span class="text-red-400">*</span>
                    </label>
                    <input type="number" 
                        id="coinsInput"
                        name="coins" 
                        min="-1000000" 
                        max="1000000" 
                        required
                        class="form-input"
                        placeholder="Positive to add, negative to reduce (e.g., 1000 or -500)"
                        oninput="updateModalPreview()">
                    <p class="mt-1 text-xs text-gray-400">
                        <span class="text-green-400">Positive</span> to add coins, 
                        <span class="text-red-400">Negative</span> to reduce coins
                    </p>
                    <div id="previewBalance" class="mt-2 p-2 bg-gray-750 rounded text-sm"></div>
                </div>
                
                <!-- Reason Input -->
                <div>
                    <label class="form-label text-gray-300">
                        Reason <span class="text-gray-500 text-xs">(Optional)</span>
                    </label>
                    <textarea name="reason" rows="3" maxlength="200"
                        class="form-input resize-none"
                        placeholder="E.g., Test coins for API, Bonus reward, Compensation, Refund"></textarea>
                    <p class="mt-1 text-xs text-gray-400">Max 200 characters</p>
                </div>
            </div>

            <!-- Modal Footer -->
            <div class="flex items-center space-x-3 px-6 py-4 bg-gray-750 border-t border-gray-700 rounded-b-lg">
                <button type="button" 
                    onclick="closeAddCoinsModal()"
                    class="btn btn-secondary flex-1 py-3">
                    Cancel
                </button>
                <button type="submit"
                    id="submitBtn"
                    class="btn btn-success flex-1 py-3">
                    Add Coins
                </button>
            </div>
        </form>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('addCoinsModal');
    
    // Force hide modal on page load
    modal.style.display = 'none';
    
    // Close modal when clicking outside
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            closeAddCoinsModal();
        }
    });
});

function showAddCoinsModal() {
    const modal = document.getElementById('addCoinsModal');
    modal.style.display = 'flex';
    modal.classList.remove('hidden');
    // Reset input
    document.getElementById('coinsInput').value = '';
    document.getElementById('previewBalance').innerHTML = '';
}

function closeAddCoinsModal() {
    const modal = document.getElementById('addCoinsModal');
    modal.style.display = 'none';
    modal.classList.add('hidden');
}

function updateModalPreview() {
    const input = document.getElementById('coinsInput');
    const preview = document.getElementById('previewBalance');
    const submitBtn = document.getElementById('submitBtn');
    const modalTitle = document.getElementById('modalTitle');
    const currentBalance = {{ $user->coin_balance }};
    
    const coinValue = parseInt(input.value) || 0;
    
    if (coinValue === 0) {
        preview.innerHTML = '';
        submitBtn.textContent = 'Add Coins';
        submitBtn.className = 'btn btn-success flex-1 py-3';
        modalTitle.textContent = 'Add/Reduce Coins';
        return;
    }
    
    const newBalance = currentBalance + coinValue;
    
    if (coinValue > 0) {
        preview.innerHTML = `<span class="text-green-400">✓ Will add ${coinValue.toLocaleString()} coins</span><br>
                            <span class="text-gray-300">New Balance: ${newBalance.toLocaleString()} coins</span>`;
        submitBtn.textContent = `Add ${coinValue.toLocaleString()} Coins`;
        submitBtn.className = 'btn btn-success flex-1 py-3';
        modalTitle.textContent = 'Add Coins to User';
    } else {
        const absValue = Math.abs(coinValue);
        if (newBalance < 0) {
            preview.innerHTML = `<span class="text-red-400">⚠ Warning: Balance will be negative!</span><br>
                                <span class="text-red-300">New Balance: ${newBalance.toLocaleString()} coins</span>`;
        } else {
            preview.innerHTML = `<span class="text-orange-400">⚠ Will reduce ${absValue.toLocaleString()} coins</span><br>
                                <span class="text-gray-300">New Balance: ${newBalance.toLocaleString()} coins</span>`;
        }
        submitBtn.textContent = `Reduce ${absValue.toLocaleString()} Coins`;
        submitBtn.className = 'btn btn-danger flex-1 py-3';
        modalTitle.textContent = 'Reduce User Coins';
    }
}

function copyToken(event) {
    const tokenElement = document.getElementById('apiToken');
    const token = tokenElement.innerText;
    
    // Copy to clipboard
    navigator.clipboard.writeText(token).then(function() {
        // Show success feedback
        const btn = event.target;
        const originalText = btn.innerHTML;
        btn.innerHTML = '✅ Copied!';
        btn.style.backgroundColor = '#16a34a';
        
        setTimeout(function() {
            btn.innerHTML = originalText;
            btn.style.backgroundColor = '';
        }, 2000);
    }).catch(function(err) {
        alert('Failed to copy token: ' + err);
    });
}

// Online status changes are done via "Edit User" form (no toggle endpoint).
</script>

@endsection





