<!DOCTYPE html>
<html lang="en" class="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <meta name="csrf-token" content="{{ csrf_token() }}">
    <title>@yield('title', 'Dashboard') - Only Care Admin</title>
    
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
    <style>
        body { font-family: 'Outfit', sans-serif; }
        .sidebar-link {
            display: flex;
            align-items: center;
            padding: 0.75rem 1rem;
            margin-bottom: 0.5rem;
            border-radius: 0.75rem;
            color: #A0A0A0;
            transition: all 0.3s ease;
            -webkit-tap-highlight-color: transparent;
            touch-action: manipulation;
            cursor: pointer;
            min-height: 44px; /* Minimum touch target size for mobile */
        }
        .sidebar-link:hover {
            background: rgba(255, 255, 255, 0.05);
            color: #FFFFFF;
        }
        .sidebar-link.active {
            background: linear-gradient(45deg, #FF1744, #D50000);
            color: #FFFFFF;
            box-shadow: 0 4px 15px rgba(255, 23, 68, 0.3);
        }
        .glass-header {
            background: rgba(18, 18, 18, 0.8);
            backdrop-filter: blur(12px);
            border-bottom: 1px solid rgba(255, 255, 255, 0.05);
        }
        /* Mobile touch improvements */
        @media (max-width: 1023px) {
            button, a, input, select, textarea {
                -webkit-tap-highlight-color: rgba(255, 255, 255, 0.2);
                touch-action: manipulation;
            }
            .sidebar-link {
                padding: 1rem;
                font-size: 1rem;
                position: relative;
                z-index: 10;
            }
            /* Ensure sidebar is above overlay */
            aside {
                z-index: 60 !important;
            }
            /* Ensure overlay is below sidebar but above content */
            .sidebar-overlay {
                z-index: 50 !important;
            }
        }
        /* Prevent text selection on mobile taps */
        * {
            -webkit-touch-callout: none;
            -webkit-user-select: none;
            -khtml-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
        }
        /* Allow text selection in content areas */
        main, input, textarea {
            -webkit-user-select: text;
            -khtml-user-select: text;
            -moz-user-select: text;
            -ms-user-select: text;
            user-select: text;
        }
        /* Ensure links are clickable */
        a.sidebar-link {
            pointer-events: auto !important;
            cursor: pointer !important;
        }
        
        /* Global Mobile Responsive Styles */
        @media (max-width: 1023px) {
            /* Main content padding */
            main {
                padding: 1rem !important;
            }
            
            /* Cards and containers */
            .bg-white, .bg-dark-surface, .bg-gray-800 {
                padding: 1rem !important;
            }
            
            /* Tables - Make scrollable */
            .overflow-x-auto {
                -webkit-overflow-scrolling: touch;
                overflow-x: auto;
                display: block;
                width: 100%;
            }
            
            table {
                min-width: 600px; /* Ensure table doesn't shrink too much */
                font-size: 0.875rem;
            }
            
            table th, table td {
                padding: 0.75rem 0.5rem !important;
                white-space: nowrap;
            }
            
            /* Grid layouts - Stack on mobile */
            .grid {
                grid-template-columns: 1fr !important;
            }
            
            .grid.grid-cols-1.md\\:grid-cols-2,
            .grid.grid-cols-1.md\\:grid-cols-3,
            .grid.grid-cols-1.md\\:grid-cols-4 {
                grid-template-columns: 1fr !important;
            }
            
            /* Stats cards */
            .grid.grid-cols-1.md\\:grid-cols-2.lg\\:grid-cols-4 {
                grid-template-columns: 1fr !important;
            }
            
            /* Forms - Stack inputs */
            form .grid {
                grid-template-columns: 1fr !important;
            }
            
            /* Buttons - Full width on mobile */
            button[type="submit"],
            .btn,
            button:not(.sidebar-link):not([@click*="sidebar"]) {
                width: 100%;
                min-height: 44px;
                padding: 0.75rem 1rem;
                font-size: 1rem;
            }
            
            /* Input fields */
            input[type="text"],
            input[type="email"],
            input[type="password"],
            input[type="number"],
            input[type="tel"],
            input[type="date"],
            select,
            textarea {
                width: 100%;
                min-height: 44px;
                font-size: 1rem;
                padding: 0.75rem;
            }
            
            /* Headers */
            h1, h2, h3 {
                font-size: 1.5rem !important;
            }
            
            /* Text sizes */
            .text-3xl {
                font-size: 1.875rem !important;
            }
            
            .text-2xl {
                font-size: 1.5rem !important;
            }
            
            /* Spacing */
            .space-y-6 > * + * {
                margin-top: 1rem !important;
            }
            
            .space-y-8 > * + * {
                margin-top: 1.5rem !important;
            }
            
            /* Charts - Responsive */
            canvas {
                max-width: 100%;
                height: auto !important;
            }
            
            /* Action buttons in tables - Stack vertically */
            .flex.items-center.space-x-2:not(.no-stack) {
                flex-direction: column;
                gap: 0.5rem;
                align-items: stretch;
            }
            
            .flex.items-center.space-x-2:not(.no-stack) > * {
                width: 100%;
            }
            
            /* Action buttons - Make touch-friendly */
            .flex.items-center.space-x-2 button,
            .flex.items-center.space-x-2 a {
                min-height: 44px;
                padding: 0.5rem 1rem;
            }
            
            /* Badges and labels */
            .badge, .px-2, .px-3 {
                font-size: 0.75rem;
                padding: 0.25rem 0.5rem;
            }
            
            /* Header adjustments */
            header {
                padding: 1rem !important;
            }
            
            header h2 {
                font-size: 1.25rem !important;
            }
            
            /* Stats cards - Better spacing */
            .grid.grid-cols-1.md\\:grid-cols-4 > div {
                margin-bottom: 0.5rem;
            }
            
            /* Form sections */
            .space-y-4 > * + *,
            .space-y-6 > * + *,
            .space-y-8 > * + * {
                margin-top: 1rem !important;
            }
            
            /* Modal and overlay improvements */
            .modal, .fixed {
                padding: 1rem;
            }
            
            /* Image responsiveness */
            img {
                max-width: 100%;
                height: auto;
            }
            
            /* Chart containers */
            .h-64, [style*="height: 256px"] {
                height: 200px !important;
            }
        }
        
        @media (max-width: 640px) {
            /* Extra small screens */
            main {
                padding: 0.75rem !important;
            }
            
            table {
                min-width: 500px;
                font-size: 0.75rem;
            }
            
            table th, table td {
                padding: 0.5rem 0.25rem !important;
            }
            
            /* Hide less important columns on very small screens */
            .hidden-mobile {
                display: none !important;
            }
        }
        
        /* Touch-friendly elements */
        @media (hover: none) and (pointer: coarse) {
            button, a, input, select, textarea, .clickable {
                -webkit-tap-highlight-color: rgba(255, 255, 255, 0.2);
                touch-action: manipulation;
            }
        }
    </style>
</head>
<body class="bg-dark-bg text-dark-text min-h-screen antialiased">
    <div class="flex h-screen overflow-hidden" x-data="{ sidebarOpen: false }">
        <!-- Sidebar -->
        <aside class="fixed inset-y-0 left-0 z-50 w-64 bg-dark-surface border-r border-dark-border transform transition-transform duration-300 lg:translate-x-0"
               :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
               x-cloak>
            <div class="flex flex-col h-full">
                <!-- Logo -->
                <div class="flex items-center justify-between px-6 py-6">
                    <h1 class="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary tracking-tight">OnlyCare</h1>
                    <button @click="sidebarOpen = false" class="lg:hidden text-dark-text-secondary hover:text-white transition-colors touch-manipulation" style="min-width: 44px; min-height: 44px; display: flex; align-items: center; justify-content: center; z-index: 10; position: relative;">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>

                <!-- Navigation -->
                <nav class="flex-1 px-4 py-6 overflow-y-auto">
                    <a href="{{ route('dashboard') }}" class="sidebar-link {{ request()->routeIs('dashboard*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
                        </svg>
                        Dashboard
                    </a>

                    <a href="{{ route('users.index') }}" class="sidebar-link {{ request()->routeIs('users*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
                        </svg>
                        Users
                    </a>

                    <a href="{{ route('calls.index') }}" class="sidebar-link {{ request()->routeIs('calls*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
                        </svg>
                        Callss
                    </a>

                    <a href="{{ route('transactions.index') }}" class="sidebar-link {{ request()->routeIs('transactions*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"/>
                        </svg>
                        Transactions
                    </a>

                    <a href="{{ route('withdrawals.index') }}" class="sidebar-link {{ request()->routeIs('withdrawals*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                        Withdrawals
                        @php
                            $pendingCount = \App\Models\Withdrawal::where('status', 'PENDING')->count();
                        @endphp
                        @if($pendingCount > 0)
                            <span class="ml-auto badge badge-danger">{{ $pendingCount }}</span>
                        @endif
                    </a>

                    <a href="{{ route('kyc.index') }}" class="sidebar-link {{ request()->routeIs('kyc*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/>
                        </svg>
                        KYC Verification
                        @php
                            $pendingKyc = \App\Models\KycDocument::where('status', 'PENDING')->distinct('user_id')->count('user_id');
                        @endphp
                        @if($pendingKyc > 0)
                            <span class="ml-auto badge badge-warning">{{ $pendingKyc }}</span>
                        @endif
                    </a>

                    <a href="{{ route('reports.index') }}" class="sidebar-link {{ request()->routeIs('reports*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                        </svg>
                        Reports
                    </a>

                    <a href="{{ route('chats.index') }}" class="sidebar-link {{ request()->routeIs('chats*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
                        </svg>
                        Chats
                    </a>

                 

                    <a href="{{ route('push_notifications.index') }}" class="sidebar-link {{ request()->routeIs('push_notifications*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/>
                        </svg>
                        Push Notifications
                    </a>

                    <a href="{{ route('screen_notifications.index') }}" class="sidebar-link {{ request()->routeIs('screen_notifications*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                        Scheduled Notifications
                    </a>

                    <a href="{{ route('coin-packages.index') }}" class="sidebar-link {{ request()->routeIs('coin-packages*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                        </svg>
                        Coin Packages
                    </a>

                    <a href="{{ route('avatars.index') }}" class="sidebar-link {{ request()->routeIs('avatars*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                        </svg>
                        Avatars
                    </a>

                    <a href="{{ route('gifts.index') }}" class="sidebar-link {{ request()->routeIs('gifts*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v13m0-13V6a2 2 0 112 2h-2zm0 0V5.5A2.5 2.5 0 109.5 8H12zm-7 4h14M5 12a2 2 0 110-4h14a2 2 0 110 4M5 12v7a2 2 0 002 2h10a2 2 0 002-2v-7"/>
                        </svg>
                        Gifts
                    </a>

                    <a href="{{ route('settings.index') }}" class="sidebar-link {{ request()->routeIs('settings*') ? 'active' : '' }}" @click="sidebarOpen = false">
                        <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                        </svg>
                        Settings
                    </a>
                </nav>

                <!-- User Info -->
                <div class="px-4 py-4 border-t border-dark-border">
                    <div class="flex items-center">
                        <div class="w-10 h-10 bg-primary rounded-full flex items-center justify-center text-white font-bold">
                            {{ substr(auth()->user()->username, 0, 2) }}
                        </div>
                        <div class="ml-3 flex-1">
                            <p class="text-sm font-medium">{{ auth()->user()->username }}</p>
                            <p class="text-xs text-dark-text-secondary">{{ auth()->user()->role }}</p>
                        </div>
                        <form action="{{ route('logout') }}" method="POST">
                            @csrf
                            <button type="submit" class="text-dark-text-secondary hover:text-danger" title="Logout">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
                                </svg>
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </aside>

        <!-- Main Content -->
        <div class="flex-1 flex flex-col lg:ml-64">
            <!-- Header -->
            <header class="glass-header sticky top-0 z-40 px-6 py-4">
                <div class="flex items-center justify-between">
                    <button @click="sidebarOpen = !sidebarOpen" class="lg:hidden text-dark-text hover:text-primary transition-colors touch-manipulation" style="min-width: 44px; min-height: 44px; display: flex; align-items: center; justify-content: center; z-index: 10; position: relative;">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
                        </svg>
                    </button>
                    
                    <h2 class="text-xl font-semibold tracking-wide">@yield('title', 'Dashboard')</h2>
                    
                    <div class="flex items-center space-x-4">
                        <span class="text-sm text-dark-text-secondary font-medium bg-dark-surface px-3 py-1 rounded-full border border-dark-border">{{ now()->format('D, M j, Y') }}</span>
                    </div>
                </div>
            </header>

            <!-- Content Area -->
            <main class="flex-1 overflow-y-auto p-6">
                @if(session('success'))
                    <div class="alert alert-success mb-6">
                        <svg class="w-5 h-5 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                        </svg>
                        {{ session('success') }}
                    </div>
                @endif

                @if(session('error'))
                    <div class="alert alert-danger mb-6">
                        <svg class="w-5 h-5 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                        {{ session('error') }}
                    </div>
                @endif

                @yield('content')
            </main>
        </div>
    </div>

    <!-- Mobile Sidebar Overlay -->
    <div x-show="sidebarOpen" 
         x-cloak
         @click="sidebarOpen = false"
         @touchstart="sidebarOpen = false"
         x-transition:enter="transition-opacity ease-linear duration-300"
         x-transition:enter-start="opacity-0"
         x-transition:enter-end="opacity-100"
         x-transition:leave="transition-opacity ease-linear duration-300"
         x-transition:leave-start="opacity-100"
         x-transition:leave-end="opacity-0"
         class="sidebar-overlay fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden touch-manipulation">
    </div>
    
    <style>
        [x-cloak] { display: none !important; }
    </style>
    
    @stack('scripts')
    
    <script>
        // Mobile sidebar fallback - ensure it works even if Alpine.js has issues
        document.addEventListener('DOMContentLoaded', function() {
            // Close sidebar when clicking on any sidebar link (mobile)
            if (window.innerWidth < 1024) {
                document.querySelectorAll('.sidebar-link').forEach(function(link) {
                    link.addEventListener('click', function() {
                        // Try Alpine.js first
                        if (window.Alpine && Alpine.store) {
                            try {
                                var sidebarState = Alpine.store('sidebar');
                                if (sidebarState) sidebarState.open = false;
                            } catch(e) {}
                        }
                        // Fallback: directly manipulate DOM
                        var sidebar = document.querySelector('aside');
                        var overlay = document.querySelector('.sidebar-overlay');
                        if (sidebar) {
                            sidebar.classList.remove('translate-x-0');
                            sidebar.classList.add('-translate-x-full');
                        }
                        if (overlay) {
                            overlay.style.display = 'none';
                        }
                    }, { passive: true });
                });
            }
            
            // Ensure hamburger menu button works
            var hamburgerBtn = document.querySelector('button[class*="lg:hidden"]');
            if (hamburgerBtn && !hamburgerBtn.hasAttribute('x-data')) {
                hamburgerBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    var sidebar = document.querySelector('aside');
                    var overlay = document.querySelector('.sidebar-overlay');
                    if (sidebar) {
                        var isOpen = sidebar.classList.contains('translate-x-0');
                        if (isOpen) {
                            sidebar.classList.remove('translate-x-0');
                            sidebar.classList.add('-translate-x-full');
                            if (overlay) overlay.style.display = 'none';
                        } else {
                            sidebar.classList.remove('-translate-x-full');
                            sidebar.classList.add('translate-x-0');
                            if (overlay) overlay.style.display = 'block';
                        }
                    }
                }, { passive: false });
            }
            
            // Ensure overlay closes sidebar
            var overlay = document.querySelector('.sidebar-overlay');
            if (overlay) {
                overlay.addEventListener('click', function() {
                    var sidebar = document.querySelector('aside');
                    if (sidebar) {
                        sidebar.classList.remove('translate-x-0');
                        sidebar.classList.add('-translate-x-full');
                        overlay.style.display = 'none';
                    }
                }, { passive: true });
            }
        });
        
        // Handle window resize
        window.addEventListener('resize', function() {
            if (window.innerWidth >= 1024) {
                var sidebar = document.querySelector('aside');
                var overlay = document.querySelector('.sidebar-overlay');
                if (sidebar) {
                    sidebar.classList.remove('-translate-x-full');
                    sidebar.classList.add('translate-x-0');
                }
                if (overlay) overlay.style.display = 'none';
            }
        }, { passive: true });
    </script>
</body>
</html>

