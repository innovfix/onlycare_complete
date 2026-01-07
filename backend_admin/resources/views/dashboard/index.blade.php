@extends('layouts.app')

@section('title', 'Dashboard')

@section('content')
<div class="space-y-8">
    <!-- Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <!-- Total Users -->
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border hover:border-primary/50 transition-colors duration-300">
            <div class="flex items-center justify-between mb-4">
                <div class="p-3 rounded-xl bg-primary/10 text-primary">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
                    </svg>
                </div>
                <span class="text-xs font-medium text-success bg-success/10 px-2 py-1 rounded-lg">+12%</span>
            </div>
            <h3 class="text-3xl font-bold text-white mb-1">{{ number_format($totalUsers) }}</h3>
            <p class="text-dark-text-secondary text-sm font-medium">Total Users</p>
            <div class="mt-4 flex items-center text-xs font-medium space-x-3">
                <span class="flex items-center text-blue-400"><span class="w-2 h-2 rounded-full bg-blue-400 mr-1"></span>{{ $maleUsers }} Male</span>
                <span class="flex items-center text-pink-500"><span class="w-2 h-2 rounded-full bg-pink-500 mr-1"></span>{{ $femaleUsers }} Female</span>
            </div>
        </div>

        <!-- Active Today -->
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border hover:border-success/50 transition-colors duration-300">
            <div class="flex items-center justify-between mb-4">
                <div class="p-3 rounded-xl bg-success/10 text-success">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
                <span class="text-xs font-medium text-success bg-success/10 px-2 py-1 rounded-lg">Now</span>
            </div>
            <h3 class="text-3xl font-bold text-white mb-1">{{ number_format($activeToday) }}</h3>
            <p class="text-dark-text-secondary text-sm font-medium">Active Today</p>
            <p class="mt-4 text-xs font-medium text-success flex items-center">
                <span class="w-2 h-2 rounded-full bg-success mr-2 animate-pulse"></span>
                {{ $onlineNow }} online now
            </p>
        </div>

        <!-- Total Calls -->
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border hover:border-warning/50 transition-colors duration-300">
            <div class="flex items-center justify-between mb-4">
                <div class="p-3 rounded-xl bg-warning/10 text-warning">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
                    </svg>
                </div>
            </div>
            <h3 class="text-3xl font-bold text-white mb-1">{{ number_format($totalCalls) }}</h3>
            <p class="text-dark-text-secondary text-sm font-medium">Total Calls</p>
            <p class="mt-4 text-xs font-medium text-dark-text-secondary">
                {{ $callsToday }} calls made today
            </p>
        </div>

        <!-- Revenue -->
        <div class="bg-dark-surface rounded-2xl p-6 border border-dark-border hover:border-purple-500/50 transition-colors duration-300">
            <div class="flex items-center justify-between mb-4">
                <div class="p-3 rounded-xl bg-purple-500/10 text-purple-500">
                    <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
                <span class="text-xs font-medium text-success bg-success/10 px-2 py-1 rounded-lg">+8.4%</span>
            </div>
            <h3 class="text-3xl font-bold text-white mb-1">₹{{ number_format($totalRevenue) }}</h3>
            <p class="text-dark-text-secondary text-sm font-medium">Total Revenue</p>
            <p class="mt-4 text-xs font-medium text-success">
                ₹{{ number_format($revenueToday) }} earned today
            </p>
        </div>
    </div>

    <!-- Pending Actions -->
    <div class="bg-dark-surface rounded-2xl border border-dark-border overflow-hidden">
        <div class="px-6 py-4 border-b border-dark-border bg-black/20">
            <h3 class="text-lg font-semibold text-white">Pending Actions</h3>
        </div>
        <div class="p-6">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <a href="{{ route('withdrawals.index', ['status' => 'PENDING']) }}" class="group flex items-center justify-between p-4 bg-dark-bg rounded-xl border border-dark-border hover:border-danger/50 transition-all duration-300">
                    <div>
                        <p class="text-dark-text-secondary text-xs font-medium uppercase tracking-wider mb-1">Withdrawals</p>
                        <p class="text-2xl font-bold text-white group-hover:text-danger transition-colors">{{ $pendingWithdrawals }}</p>
                    </div>
                    <span class="px-3 py-1 rounded-full text-xs font-bold bg-danger/10 text-danger">ACTION</span>
                </a>

                <a href="{{ route('kyc.index', ['status' => 'PENDING']) }}" class="group flex items-center justify-between p-4 bg-dark-bg rounded-xl border border-dark-border hover:border-warning/50 transition-all duration-300">
                    <div>
                        <p class="text-dark-text-secondary text-xs font-medium uppercase tracking-wider mb-1">KYC Verifications</p>
                        <p class="text-2xl font-bold text-white group-hover:text-warning transition-colors">{{ $pendingKyc }}</p>
                    </div>
                    <span class="px-3 py-1 rounded-full text-xs font-bold bg-warning/10 text-warning">REVIEW</span>
                </a>

                <a href="{{ route('reports.index', ['status' => 'PENDING']) }}" class="group flex items-center justify-between p-4 bg-dark-bg rounded-xl border border-dark-border hover:border-primary/50 transition-all duration-300">
                    <div>
                        <p class="text-dark-text-secondary text-xs font-medium uppercase tracking-wider mb-1">Reports</p>
                        <p class="text-2xl font-bold text-white group-hover:text-primary transition-colors">{{ $pendingReports }}</p>
                    </div>
                    <span class="px-3 py-1 rounded-full text-xs font-bold bg-primary/10 text-primary">RESOLVE</span>
                </a>
            </div>
        </div>
    </div>

    <!-- Charts -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- User Growth Chart -->
        <div class="bg-dark-surface rounded-2xl border border-dark-border p-6">
            <h3 class="text-lg font-semibold text-white mb-6">User Growth <span class="text-dark-text-secondary text-sm font-normal ml-2">(Last 7 Days)</span></h3>
            <div class="h-64">
                <canvas id="userGrowthChart"></canvas>
            </div>
        </div>

        <!-- Revenue Chart -->
        <div class="bg-dark-surface rounded-2xl border border-dark-border p-6">
            <h3 class="text-lg font-semibold text-white mb-6">Revenue <span class="text-dark-text-secondary text-sm font-normal ml-2">(Last 7 Days)</span></h3>
            <div class="h-64">
                <canvas id="revenueChart"></canvas>
            </div>
        </div>
    </div>

    <!-- Recent Activity -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Recent Users -->
        <div class="bg-dark-surface rounded-2xl border border-dark-border overflow-hidden">
            <div class="px-6 py-4 border-b border-dark-border flex items-center justify-between bg-black/20">
                <h3 class="text-lg font-semibold text-white">Recent Users</h3>
                <a href="{{ route('users.index') }}" class="text-primary text-sm font-medium hover:text-primary-dark transition-colors">View All</a>
            </div>
            <div class="overflow-x-auto">
                <table class="w-full">
                    <thead class="bg-black/20">
                        <tr>
                            <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase tracking-wider">Name</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase tracking-wider">Gender</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase tracking-wider">Joined</th>
                            <th class="px-6 py-3 text-right text-xs font-medium text-dark-text-secondary uppercase tracking-wider">Action</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-dark-border">
                        @foreach($recentUsers as $user)
                        <tr class="hover:bg-white/5 transition-colors">
                            <td class="px-6 py-4 whitespace-nowrap">
                                <div class="flex items-center">
                                    <div class="w-8 h-8 rounded-full bg-gradient-to-br from-gray-700 to-gray-900 flex items-center justify-center mr-3 text-white font-bold text-xs border border-dark-border">
                                        {{ substr($user->name, 0, 1) }}
                                    </div>
                                    <span class="text-sm font-medium text-white">{{ $user->name }}</span>
                                </div>
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="px-2 py-1 text-xs font-bold rounded-full {{ $user->gender === 'MALE' ? 'bg-blue-500/10 text-blue-500' : 'bg-pink-500/10 text-pink-500' }}">
                                    {{ $user->gender }}
                                </span>
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap text-sm text-dark-text-secondary">
                                {{ $user->created_at->diffForHumans() }}
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap text-right">
                                <a href="{{ route('users.show', $user->id) }}" class="text-primary hover:text-primary-dark text-sm font-medium">
                                    View
                                </a>
                            </td>
                        </tr>
                        @endforeach
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Recent Calls -->
        <div class="bg-dark-surface rounded-2xl border border-dark-border overflow-hidden">
            <div class="px-6 py-4 border-b border-dark-border flex items-center justify-between bg-black/20">
                <h3 class="text-lg font-semibold text-white">Recent Calls</h3>
                <a href="{{ route('calls.index') }}" class="text-primary text-sm font-medium hover:text-primary-dark transition-colors">View All</a>
            </div>
            <div class="overflow-x-auto">
                <table class="w-full">
                    <thead class="bg-black/20">
                        <tr>
                            <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase tracking-wider">Type</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase tracking-wider">Duration</th>
                            <th class="px-6 py-3 text-left text-xs font-medium text-dark-text-secondary uppercase tracking-wider">Coins</th>
                            <th class="px-6 py-3 text-right text-xs font-medium text-dark-text-secondary uppercase tracking-wider">Time</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-dark-border">
                        @foreach($recentCalls as $call)
                        <tr class="hover:bg-white/5 transition-colors">
                            <td class="px-6 py-4 whitespace-nowrap">
                                <span class="px-2 py-1 text-xs font-bold rounded-full {{ $call->call_type === 'VIDEO' ? 'bg-purple-500/10 text-purple-500' : 'bg-green-500/10 text-green-500' }}">
                                    {{ $call->call_type }}
                                </span>
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap text-sm text-white">
                                {{ floor($call->duration / 60) }}m {{ $call->duration % 60 }}s
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap text-sm font-bold text-warning">
                                {{ $call->coins_spent }}
                            </td>
                            <td class="px-6 py-4 whitespace-nowrap text-right text-sm text-dark-text-secondary">
                                {{ $call->created_at->diffForHumans() }}
                            </td>
                        </tr>
                        @endforeach
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

@push('scripts')
<script>
    // Chart Defaults
    Chart.defaults.color = '#94A3B8';
    Chart.defaults.borderColor = '#334155';

    // User Growth Chart
    const userGrowthCtx = document.getElementById('userGrowthChart').getContext('2d');
    new Chart(userGrowthCtx, {
        type: 'line',
        data: {
            labels: {!! json_encode($userGrowthData->pluck('date')->map(fn($date) => \Carbon\Carbon::parse($date)->format('M d'))) !!},
            datasets: [{
                label: 'New Users',
                data: {!! json_encode($userGrowthData->pluck('count')) !!},
                borderColor: '#FF1744',
                backgroundColor: 'rgba(255, 23, 68, 0.1)',
                tension: 0.4,
                fill: true,
                pointBackgroundColor: '#FF1744',
                pointBorderColor: '#000',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(18, 18, 18, 0.9)',
                    titleColor: '#fff',
                    bodyColor: '#ccc',
                    borderColor: 'rgba(255, 255, 255, 0.1)',
                    borderWidth: 1,
                    padding: 10,
                    displayColors: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(255, 255, 255, 0.05)'
                    },
                    ticks: {
                        stepSize: 1
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            }
        }
    });

    // Revenue Chart
    const revenueCtx = document.getElementById('revenueChart').getContext('2d');
    new Chart(revenueCtx, {
        type: 'bar',
        data: {
            labels: {!! json_encode($revenueData->pluck('date')->map(fn($date) => \Carbon\Carbon::parse($date)->format('M d'))) !!},
            datasets: [{
                label: 'Revenue (₹)',
                data: {!! json_encode($revenueData->pluck('total')) !!},
                backgroundColor: '#10B981',
                borderRadius: 4,
                hoverBackgroundColor: '#34D399'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(18, 18, 18, 0.9)',
                    titleColor: '#fff',
                    bodyColor: '#ccc',
                    borderColor: 'rgba(255, 255, 255, 0.1)',
                    borderWidth: 1,
                    padding: 10,
                    displayColors: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(255, 255, 255, 0.05)'
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            }
        }
    });
</script>
@endpush
@endsection

