<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="csrf-token" content="{{ csrf_token() }}">
    <title>@yield('title', 'Only Care - API Documentation')</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        * {
            scrollbar-width: thin;
            scrollbar-color: #333333 #000000;
        }
        *::-webkit-scrollbar {
            width: 8px;
        }
        *::-webkit-scrollbar-track {
            background: #000000;
        }
        *::-webkit-scrollbar-thumb {
            background: #333333;
            border-radius: 4px;
        }
        .method-post { background: #10B981; }
        .method-get { background: #3B82F6; }
        .method-put { background: #F59E0B; }
        .method-delete { background: #EF4444; }
        
        pre {
            background: #0D1117;
            color: #C9D1D9;
            border: 1px solid #30363D;
        }
        
        .sidebar-link {
            border-left: 3px solid transparent;
            transition: all 0.2s;
        }
        .sidebar-link:hover {
            border-left-color: #6366F1;
            background: #1a1a1a !important;
        }
        .sidebar-link.active {
            border-left-color: #10B981;
            background: #1a1a1a;
        }
        .sidebar-section.active {
            border-left-color: #10B981;
            background: #0a0a0a;
        }
        
        .response-success {
            background: #064E3B;
            border-left: 3px solid #10B981;
        }
        .response-error {
            background: #7F1D1D;
            border-left: 3px solid #EF4444;
        }
        
        /* Ensure submenus are visible by default */
        [id$='-submenu'] {
            display: block;
        }
        
        /* Hide submenu when hidden class is added */
        [id$='-submenu'].hidden {
            display: none !important;
        }
    </style>
    @yield('extra_styles')
</head>
<body class="bg-black text-gray-100">
    <!-- Header -->
    <header class="bg-black border-b border-gray-800 sticky top-0 z-50">
        <div class="px-6 py-4">
            <div class="flex items-center justify-between">
                <div class="flex items-center space-x-4">
                    <h1 class="text-2xl font-bold text-white">Only Care API</h1>
                    <span class="px-3 py-1 text-xs font-semibold bg-purple-600 text-white rounded-full">v1.0</span>
                </div>
                <div class="flex items-center space-x-4">
                    <span class="text-sm text-gray-400 font-mono">{{ url('/api/v1') }}</span>
                    <a href="{{ url('/') }}" class="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white transition">
                        <i class="fas fa-arrow-left mr-2"></i>Back to Admin
                    </a>
                </div>
            </div>
        </div>
    </header>

    <div class="flex">
        <!-- Sidebar Navigation -->
        <aside class="w-72 bg-black border-r border-gray-800 overflow-y-auto flex-shrink-0" style="height: calc(100vh - 64px);">
            <div class="p-4">
                <!-- Overview -->
                <div class="mb-6">
                    <a href="{{ route('api.docs.index') }}" class="sidebar-section flex items-center px-3 py-3 text-sm font-semibold text-gray-200 rounded-md transition-colors border-l-3 {{ Route::currentRouteName() == 'api.docs.index' ? 'active' : '' }}">
                        <i class="fas fa-home mr-3 text-purple-400"></i>
                        <span>Overview</span>
                    </a>
                </div>

                <!-- Authentication -->
                <div class="mb-6">
                    <button type="button" onclick="toggleSection(event, 'auth')" class="w-full flex items-center justify-between px-3 py-2.5 text-sm font-semibold text-gray-300 hover:text-white hover:bg-gray-900 rounded-md transition-colors">
                        <div class="flex items-center">
                            <span class="text-lg mr-2">🔑</span>
                            <span class="text-xs uppercase tracking-wider">Authentication</span>
                        </div>
                        <i id="auth-icon" class="fas fa-chevron-down text-xs"></i>
                    </button>
                    <div id="auth-submenu" class="space-y-1 mt-2" style="display: block;">
                        <a href="{{ Route::currentRouteName() == 'api.docs.auth' ? '#send-otp' : route('api.docs.auth') . '#send-otp' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">Send OTP</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.auth' ? '#verify-otp' : route('api.docs.auth') . '#verify-otp' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">Verify OTP</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.auth' ? '#register' : route('api.docs.auth') . '#register' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">Register</span>
                        </a>
                    </div>
                </div>

                <!-- Users / Creators -->
                <div class="mb-6">
                    <button type="button" onclick="toggleSection(event, 'users')" class="w-full flex items-center justify-between px-3 py-2.5 text-sm font-semibold text-gray-300 hover:text-white hover:bg-gray-900 rounded-md transition-colors">
                        <div class="flex items-center">
                            <span class="text-lg mr-2">👥</span>
                            <span class="text-xs uppercase tracking-wider">Users / Creators</span>
                        </div>
                        <i id="users-icon" class="fas fa-chevron-down text-xs"></i>
                    </button>
                    <div id="users-submenu" class="space-y-1 mt-2" style="display: block;">
                        <a href="{{ Route::currentRouteName() == 'api.docs.creators' ? '#get-creators' : route('api.docs.creators') . '#get-creators' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Get Female Creators</span>
                        </a>
                    </div>
                </div>

                <!-- Call APIs -->
                <div class="mb-6">
                    <button type="button" onclick="toggleSection(event, 'calls')" class="w-full flex items-center justify-between px-3 py-2.5 text-sm font-semibold text-gray-300 hover:text-white hover:bg-gray-900 rounded-md transition-colors">
                        <div class="flex items-center">
                            <span class="text-lg mr-2">📞</span>
                            <span class="text-xs uppercase tracking-wider">Call APIs</span>
                        </div>
                        <i id="calls-icon" class="fas fa-chevron-down text-xs"></i>
                    </button>
                    <div id="calls-submenu" class="space-y-1 mt-2" style="display: block;">
                        <a href="{{ Route::currentRouteName() == 'api.docs.calls' ? '#initiate-call' : route('api.docs.calls') . '#initiate-call' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">Initiate Call</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.calls' ? '#accept-call' : route('api.docs.calls') . '#accept-call' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">Accept Call</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.calls' ? '#reject-call' : route('api.docs.calls') . '#reject-call' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">Reject Call</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.calls' ? '#end-call' : route('api.docs.calls') . '#end-call' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">End Call</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.calls' ? '#rate-call' : route('api.docs.calls') . '#rate-call' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">Rate Call</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.calls' ? '#call-history' : route('api.docs.calls') . '#call-history' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Get Call History</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.calls' ? '#recent-sessions' : route('api.docs.calls') . '#recent-sessions' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Get Recent Sessions</span>
                        </a>
                    </div>
                </div>

                <!-- Wallet & Payments -->
                <div class="mb-6">
                    <button type="button" onclick="toggleSection(event, 'wallet')" class="w-full flex items-center justify-between px-3 py-2.5 text-sm font-semibold text-gray-300 hover:text-white hover:bg-gray-900 rounded-md transition-colors">
                        <div class="flex items-center">
                            <span class="text-lg mr-2">💳</span>
                            <span class="text-xs uppercase tracking-wider">Wallet & Payments</span>
                        </div>
                        <i id="wallet-icon" class="fas fa-chevron-down text-xs"></i>
                    </button>
                    <div id="wallet-submenu" class="space-y-1 mt-2" style="display: block;">
                        <a href="{{ Route::currentRouteName() == 'api.docs.wallet' ? '#get-packages' : route('api.docs.wallet') . '#get-packages' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Get Coin Packages</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.wallet' ? '#get-balance' : route('api.docs.wallet') . '#get-balance' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Get Wallet Balance</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.wallet' ? '#initiate-purchase' : route('api.docs.wallet') . '#initiate-purchase' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">Initiate Purchase</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.wallet' ? '#verify-purchase' : route('api.docs.wallet') . '#verify-purchase' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">Verify Purchase</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.wallet' ? '#get-transactions' : route('api.docs.wallet') . '#get-transactions' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Get Transactions</span>
                        </a>
                    </div>
                </div>

                <!-- Referral & Rewards -->
                <div class="mb-6">
                    <button type="button" onclick="toggleSection(event, 'referral')" class="w-full flex items-center justify-between px-3 py-2.5 text-sm font-semibold text-gray-300 hover:text-white hover:bg-gray-900 rounded-md transition-colors">
                        <div class="flex items-center">
                            <span class="text-lg mr-2">🎁</span>
                            <span class="text-xs uppercase tracking-wider">Referral & Rewards</span>
                        </div>
                        <i id="referral-icon" class="fas fa-chevron-down text-xs"></i>
                    </button>
                    <div id="referral-submenu" class="space-y-1 mt-2" style="display: block;">
                        <a href="{{ Route::currentRouteName() == 'api.docs.referrals' ? '#get-referral-code' : route('api.docs.referrals') . '#get-referral-code' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Get Referral Code</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.referrals' ? '#apply-referral-code' : route('api.docs.referrals') . '#apply-referral-code' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-post inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">POST</span>
                            <span class="whitespace-nowrap">Apply Referral Code</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.referrals' ? '#get-referral-history' : route('api.docs.referrals') . '#get-referral-history' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Get Referral History</span>
                        </a>
                    </div>
                </div>

                <!-- Content & Policies -->
                <div class="mb-6">
                    <button type="button" onclick="toggleSection(event, 'content')" class="w-full flex items-center justify-between px-3 py-2.5 text-sm font-semibold text-gray-300 hover:text-white hover:bg-gray-900 rounded-md transition-colors">
                        <div class="flex items-center">
                            <span class="text-lg mr-2">📄</span>
                            <span class="text-xs uppercase tracking-wider">Content & Policies</span>
                        </div>
                        <i id="content-icon" class="fas fa-chevron-down text-xs"></i>
                    </button>
                    <div id="content-submenu" class="space-y-1 mt-2" style="display: block;">
                        <a href="{{ Route::currentRouteName() == 'api.docs.content' ? '#get-privacy-policy' : route('api.docs.content') . '#get-privacy-policy' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Privacy Policy</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.content' ? '#get-terms-conditions' : route('api.docs.content') . '#get-terms-conditions' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Terms & Conditions</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.content' ? '#get-refund-policy' : route('api.docs.content') . '#get-refund-policy' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Refund & Cancellation</span>
                        </a>
                        <a href="{{ Route::currentRouteName() == 'api.docs.content' ? '#get-community-guidelines' : route('api.docs.content') . '#get-community-guidelines' }}" class="sidebar-link flex items-center px-3 py-2.5 text-sm text-gray-300 rounded-md transition-colors ml-8">
                            <span class="method-get inline-flex items-center justify-center px-2 py-0.5 text-xs font-semibold rounded mr-3 flex-shrink-0">GET</span>
                            <span class="whitespace-nowrap">Community Guidelines</span>
                        </a>
                    </div>
                </div>
            </div>
        </aside>

        <!-- Main Content -->
        @yield('content')
    </div>

    @yield('extra_scripts')

    <script>
        // Toggle sidebar section menu
        function toggleSection(event, sectionId) {
            // Prevent any default behavior and stop event propagation
            if (event) {
                event.preventDefault();
                event.stopPropagation();
            }
            
            const submenu = document.getElementById(`${sectionId}-submenu`);
            const icon = document.getElementById(`${sectionId}-icon`);
            
            if (submenu && icon) {
                const isHidden = submenu.classList.contains('hidden');
                
                if (isHidden) {
                    submenu.classList.remove('hidden');
                    icon.classList.remove('fa-chevron-right');
                    icon.classList.add('fa-chevron-down');
                    localStorage.setItem(`submenu-${sectionId}`, 'open');
                } else {
                    submenu.classList.add('hidden');
                    icon.classList.remove('fa-chevron-down');
                    icon.classList.add('fa-chevron-right');
                    localStorage.setItem(`submenu-${sectionId}`, 'closed');
                }
            }
            
            return false;
        }

        // Initialize sections based on saved state or default to open
        document.addEventListener('DOMContentLoaded', function() {
            const sections = ['auth', 'users', 'calls', 'wallet', 'referral', 'content'];
            
            sections.forEach(sectionId => {
                const submenu = document.getElementById(`${sectionId}-submenu`);
                const icon = document.getElementById(`${sectionId}-icon`);
                
                if (submenu && icon) {
                    // Get saved state from localStorage, default to 'open'
                    const savedState = localStorage.getItem(`submenu-${sectionId}`) || 'open';
                    
                    if (savedState === 'open') {
                        submenu.classList.remove('hidden');
                        icon.classList.remove('fa-chevron-right');
                        icon.classList.add('fa-chevron-down');
                    } else {
                        submenu.classList.add('hidden');
                        icon.classList.remove('fa-chevron-down');
                        icon.classList.add('fa-chevron-right');
                    }
                }
            });
            
            // Handle hash in URL on page load
            if (window.location.hash) {
                setTimeout(() => {
                    const target = document.querySelector(window.location.hash);
                    if (target) {
                        target.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    }
                }, 100);
            }
        });

        // Smooth scroll to anchors
        document.querySelectorAll('a[href*="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                const href = this.getAttribute('href');
                
                // Check if it's a link on the current page (contains # but might have full URL)
                if (href.includes('#')) {
                    const hashIndex = href.indexOf('#');
                    const hash = href.substring(hashIndex);
                    const urlPart = href.substring(0, hashIndex);
                    
                    // If URL part is empty or matches current page, handle smooth scroll
                    if (!urlPart || window.location.pathname.includes(urlPart)) {
                        e.preventDefault();
                        const target = document.querySelector(hash);
                        if (target) {
                            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
                            // Update URL without page reload
                            if (history.pushState) {
                                history.pushState(null, null, hash);
                            }
                        }
                    }
                    // Otherwise let it navigate normally to the other page
                }
            });
        });

        // Update active state for section links
        const observerOptions = {
            root: null,
            rootMargin: '-20% 0px -70% 0px',
            threshold: 0
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const id = entry.target.getAttribute('id');
                    document.querySelectorAll('.sidebar-link').forEach(link => {
                        link.classList.remove('active');
                    });
                    const activeLink = document.querySelector(`a[href="#${id}"]`);
                    if (activeLink) {
                        activeLink.classList.add('active');
                    }
                }
            });
        }, observerOptions);

        document.querySelectorAll('section[id]').forEach((section) => {
            observer.observe(section);
        });
    </script>
</body>
</html>

