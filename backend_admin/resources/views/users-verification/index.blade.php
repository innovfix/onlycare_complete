@extends('layouts.app')

@section('title', 'User Verification - Voice Gender Detection')

@section('content')
<div class="space-y-6">
    <!-- Header -->
    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
                    <svg class="w-8 h-8 mr-3 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"/>
                    </svg>
                    User Voice Verification
                </h1>
                <p class="text-gray-500 dark:text-gray-400 mt-1">Detect gender from voice files using AI</p>
            </div>
        </div>
    </div>

    <!-- Search and Filters -->
    <div class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm">
        <form method="GET" action="{{ route('users-verification.index') }}" class="space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                <input type="text" name="search" placeholder="Search by name, phone, language..." 
                       value="{{ request('search') }}"
                       class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                
                <select name="status" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Status</option>
                    <option value="verified" {{ request('status') == 'verified' ? 'selected' : '' }}>Verified (True)</option>
                    <option value="pending" {{ request('status') == 'pending' ? 'selected' : '' }}>Pending (False)</option>
                    <option value="rejected" {{ request('status') == 'rejected' ? 'selected' : '' }}>Rejected (Male)</option>
                </select>

                <select name="language" class="bg-gray-50 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-white rounded-lg px-4 py-2">
                    <option value="">All Languages</option>
                    <option value="HINDI" {{ request('language') == 'HINDI' ? 'selected' : '' }}>Hindi</option>
                    <option value="ENGLISH" {{ request('language') == 'ENGLISH' ? 'selected' : '' }}>English</option>
                    <option value="TELUGU" {{ request('language') == 'TELUGU' ? 'selected' : '' }}>Telugu</option>
                    <option value="TAMIL" {{ request('language') == 'TAMIL' ? 'selected' : '' }}>Tamil</option>
                    <option value="MALAYALAM" {{ request('language') == 'MALAYALAM' ? 'selected' : '' }}>Malayalam</option>
                </select>
                
                <button type="submit" class="font-medium rounded-lg px-6 py-2" style="background-color: #2563eb; color: white;">
                    Filter
                </button>
            </div>
        </form>
    </div>

    <!-- Bulk Actions -->
    <div class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-sm">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div class="flex items-center space-x-4">
                <button type="button" id="selectAllBtn" class="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors">
                    Select All
                </button>
                <button type="button" id="deselectAllBtn" class="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-gray-100 dark:bg-gray-700 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors">
                    Deselect All
                </button>
                <span id="selectedCount" class="text-sm font-medium text-gray-600 dark:text-gray-400">0 selected</span>
            </div>
            <div class="flex items-center space-x-2 flex-shrink-0">
                <button type="button" id="bulkVerifyBtn" class="px-6 py-2.5 text-sm font-semibold text-white rounded-lg hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md hover:shadow-lg flex items-center justify-center min-w-[140px]" style="background-color: #16a34a !important; border: none;" disabled>
                    <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                    </svg>
                    Verify Selected
                </button>
                <button type="button" id="bulkCancelBtn" class="px-6 py-2.5 text-sm font-semibold text-white rounded-lg hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md hover:shadow-lg flex items-center justify-center min-w-[140px]" style="background-color: #dc2626 !important; border: none;" disabled>
                    <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                    </svg>
                    Cancel Selected
                </button>
            </div>
        </div>
    </div>

    <!-- Users Table -->
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead class="bg-gray-50 dark:bg-gray-700">
                    <tr>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                            <input type="checkbox" id="selectAllCheckbox" class="rounded border-gray-300">
                        </th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">ID</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Name</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Phone</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Language</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Voice File</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Voice Gender</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Is Verified</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Verified DateTime</th>
                        <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                    </tr>
                </thead>
                <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    @forelse($users as $user)
                    <tr class="hover:bg-gray-50 dark:hover:bg-gray-700">
                        <td class="px-6 py-4 whitespace-nowrap">
                            <input type="checkbox" class="user-checkbox rounded border-gray-300" value="{{ $user->id }}" data-user-id="{{ $user->id }}">
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ $user->id }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">{{ $user->name }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ $user->phone }}</td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm">
                            @if($user->language)
                                <span class="px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200">
                                    {{ $user->language }}
                                </span>
                            @else
                                <span class="px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-400">
                                    Not Set
                                </span>
                            @endif
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm">
                            @if($user->voice)
                                @php
                                    $voicePath = storage_path('app/public/voices/' . $user->voice);
                                    $voiceUrl = url('storage/voices/' . $user->voice);
                                    $fileExists = file_exists($voicePath);
                                @endphp
                                @if($fileExists)
                                    <a href="{{ $voiceUrl }}" 
                                       target="_blank" 
                                       class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200 hover:bg-blue-200 dark:hover:bg-blue-800">
                                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"/>
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                                        </svg>
                                        Play Voice
                                    </a>
                                @else
                                    <span class="text-gray-400 dark:text-gray-500 text-xs" title="File not found: {{ $user->voice }}">
                                        File Missing
                                    </span>
                                @endif
                            @else
                                <span class="text-gray-400 dark:text-gray-500">No voice</span>
                            @endif
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm" id="gender-{{ $user->id }}">
                            @if($user->voice_gender)
                                @if($user->voice_gender === 'female')
                                    <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-pink-100 text-pink-800 dark:bg-pink-900 dark:text-pink-200">
                                        👩 Female
                                    </span>
                                @else
                                    <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200">
                                        👨 Male
                                    </span>
                                @endif
                            @else
                                <button type="button" 
                                        class="detect-gender-btn inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-primary text-white hover:bg-opacity-90 transition-colors"
                                        data-user-id="{{ $user->id }}" 
                                        data-voice="{{ $user->voice }}">
                                    <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"/>
                                    </svg>
                                    Detect
                                </button>
                            @endif
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm" id="verified-{{ $user->id }}">
                            @if($user->is_verified)
                                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200" style="background-color: #dcfce7; color: #166534;">
                                    ✓ True
                                </span>
                            @else
                                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200" style="background-color: #fee2e2; color: #991b1b;">
                                    ✗ False
                                </span>
                            @endif
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400" id="verified-datetime-{{ $user->id }}">
                            @if($user->verified_datetime)
                                <span class="text-xs" title="{{ $user->verified_datetime->setTimezone('Asia/Kolkata')->format('Y-m-d H:i:s') }}">
                                    {{ $user->verified_datetime->setTimezone('Asia/Kolkata')->format('Y-m-d H:i') }}
                                </span>
                            @else
                                <span class="text-gray-400 dark:text-gray-500 text-xs">-</span>
                            @endif
                        </td>
                        <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                            <a href="{{ route('users.show', $user->id) }}" 
                               class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300">
                                View
                            </a>
                        </td>
                    </tr>
                    @empty
                    <tr>
                        <td colspan="10" class="px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400">
                            No users with voice files found.
                        </td>
                    </tr>
                    @endforelse
                </tbody>
            </table>
        </div>
        
        <!-- Pagination -->
        @if($users->hasPages())
        <div class="px-6 py-4 border-t border-gray-200 dark:border-gray-700">
            {{ $users->links() }}
        </div>
        @endif
    </div>
</div>

<!-- Toast Notification -->
<div id="toast" class="fixed top-4 right-4 z-50 hidden">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-lg p-4 border-l-4" id="toastContent">
        <div class="flex items-center">
            <span id="toastMessage"></span>
        </div>
    </div>
</div>

@push('styles')
<style>
    /* Mobile touch improvements */
    @media (max-width: 1023px) {
        button, a, input[type="checkbox"], select {
            -webkit-tap-highlight-color: rgba(255, 255, 255, 0.1);
            touch-action: manipulation;
            min-height: 44px; /* Minimum touch target size */
        }
        
        /* Ensure buttons are easily tappable */
        #bulkVerifyBtn, #bulkCancelBtn, #selectAllBtn, #deselectAllBtn,
        button[type="submit"], .detect-btn {
            min-height: 44px;
            padding: 0.75rem 1rem;
            font-size: 1rem;
        }
        
        /* Make checkboxes larger on mobile */
        input[type="checkbox"] {
            width: 24px;
            height: 24px;
            min-width: 24px;
            min-height: 24px;
        }
        
        /* Table responsive improvements */
        .overflow-x-auto {
            -webkit-overflow-scrolling: touch;
        }
        
        /* Ensure table cells are readable */
        table td, table th {
            padding: 0.75rem 0.5rem;
            font-size: 0.875rem;
        }
        
        /* Stack bulk action buttons on mobile */
        .flex.flex-col.md\\:flex-row {
            gap: 0.75rem;
        }
        
        /* Make action buttons stack vertically on very small screens */
        @media (max-width: 640px) {
            #bulkVerifyBtn, #bulkCancelBtn {
                width: 100%;
                margin-bottom: 0.5rem;
            }
        }
    }
    
    /* Prevent double-tap zoom on buttons */
    button {
        touch-action: manipulation;
    }
</style>
@endpush

@push('scripts')
<script>
document.addEventListener('DOMContentLoaded', function() {
    // Check if jQuery is available, if not load it
    if (typeof jQuery === 'undefined') {
        var script = document.createElement('script');
        script.src = 'https://code.jquery.com/jquery-3.6.0.min.js';
        script.onload = function() {
            initAll();
        };
        document.head.appendChild(script);
    } else {
        initAll();
    }
    
    function initAll() {
        initDetectButton();
        initBulkActions();
    }
    
    function showToast(message, type = 'success') {
        var toast = document.getElementById('toast');
        var toastContent = document.getElementById('toastContent');
        var toastMessage = document.getElementById('toastMessage');
        
        toastMessage.textContent = message;
        
        // Set color based on type
        if (type === 'success') {
            toastContent.className = 'bg-white dark:bg-gray-800 rounded-lg shadow-lg p-4 border-l-4 border-green-500';
        } else if (type === 'error') {
            toastContent.className = 'bg-white dark:bg-gray-800 rounded-lg shadow-lg p-4 border-l-4 border-red-500';
        } else {
            toastContent.className = 'bg-white dark:bg-gray-800 rounded-lg shadow-lg p-4 border-l-4 border-blue-500';
        }
        
        toast.classList.remove('hidden');
        setTimeout(function() {
            toast.classList.add('hidden');
        }, 3000);
    }
    
    function initDetectButton() {
        jQuery(document).ready(function($) {
            // Handle detect gender button click
            $(document).on('click', '.detect-gender-btn', function(e) {
                e.preventDefault();
                e.stopPropagation();
                
                var btn = $(this);
                var userId = btn.data('user-id');
                var voiceFile = btn.data('voice');
                var cell = $('#gender-' + userId);
                
                if (!userId || !voiceFile) {
                    showToast('Error: Missing user ID or voice file', 'error');
                    return;
                }
                
                // Show loading state
                var originalHtml = btn.html();
                btn.html('<svg class="animate-spin w-4 h-4 mr-1" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg> Detecting...');
                btn.prop('disabled', true);
                
                // Get CSRF token
                var csrfToken = $('meta[name="csrf-token"]').attr('content') || '{{ csrf_token() }}';
                
                // Make AJAX request
                $.ajax({
                    url: '{{ route("users-verification.detectGender") }}',
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    data: {
                        _token: csrfToken,
                        user_id: userId,
                        voice_file: voiceFile
                    },
                    timeout: 60000, // 60 seconds timeout
                    success: function(response) {
                        if (response.success) {
                            // Create badge
                            var emoji = response.gender === 'female' ? '👩' : '👨';
                            var colorClass = response.gender === 'female' ? 
                                'bg-pink-100 text-pink-800 dark:bg-pink-900 dark:text-pink-200' : 
                                'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
                            var badge = '<span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium ' + colorClass + '">' + 
                                       emoji + ' ' + 
                                       response.gender.toUpperCase() + 
                                       ' (' + response.confidence + '%)</span>';
                            
                            // Update cell
                            cell.html(badge);
                            
                            // Update verified status if female
                            if (response.gender === 'female' && response.is_verified) {
                                $('#verified-' + userId).html('<span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200" style="background-color: #dcfce7; color: #166534;">✓ True</span>');
                                
                                // Update verified_datetime
                                var now = new Date();
                                var datetimeStr = now.getFullYear() + '-' + 
                                    String(now.getMonth() + 1).padStart(2, '0') + '-' + 
                                    String(now.getDate()).padStart(2, '0') + ' ' +
                                    String(now.getHours()).padStart(2, '0') + ':' +
                                    String(now.getMinutes()).padStart(2, '0');
                                $('#verified-datetime-' + userId).html('<span class="text-xs" title="' + datetimeStr + '">' + datetimeStr + '</span>');
                            }
                            
                            // Show toast notification
                            showToast('✅ ' + response.gender.toUpperCase() + ' voice detected (' + response.confidence + '% confidence)', 'success');
                        } else {
                            btn.html(originalHtml);
                            btn.prop('disabled', false);
                            showToast('Error: ' + (response.error || 'Unknown error'), 'error');
                        }
                    },
                    error: function(xhr, status, error) {
                        btn.html(originalHtml);
                        btn.prop('disabled', false);
                        
                        var errorMsg = 'Network error. Please try again.';
                        if (xhr.responseJSON && xhr.responseJSON.error) {
                            errorMsg = xhr.responseJSON.error;
                        } else if (xhr.status === 0 || status === 'timeout') {
                            errorMsg = 'Request timeout. Detection is taking longer than expected.';
                        }
                        showToast('Error: ' + errorMsg, 'error');
                    }
                });
            });
        });
    }
    
    function initBulkActions() {
        jQuery(document).ready(function($) {
            var csrfToken = $('meta[name="csrf-token"]').attr('content') || '{{ csrf_token() }}';
            
            // Select/Deselect All
            $('#selectAllCheckbox, #selectAllBtn').on('click', function() {
                $('.user-checkbox').prop('checked', true);
                updateBulkButtons();
            });
            
            $('#deselectAllBtn').on('click', function() {
                $('.user-checkbox').prop('checked', false);
                $('#selectAllCheckbox').prop('checked', false);
                updateBulkButtons();
            });
            
            // Individual checkbox change
            $(document).on('change', '.user-checkbox', function() {
                updateBulkButtons();
                updateSelectAllCheckbox();
            });
            
            // Update select all checkbox state
            function updateSelectAllCheckbox() {
                var total = $('.user-checkbox').length;
                var checked = $('.user-checkbox:checked').length;
                $('#selectAllCheckbox').prop('checked', total > 0 && total === checked);
            }
            
            // Update bulk action buttons
            function updateBulkButtons() {
                var selected = $('.user-checkbox:checked').length;
                $('#selectedCount').text(selected + ' selected');
                $('#bulkVerifyBtn, #bulkCancelBtn').prop('disabled', selected === 0);
            }
            
            // Bulk Verify
            $('#bulkVerifyBtn').on('click', function() {
                var selectedIds = $('.user-checkbox:checked').map(function() {
                    return $(this).val();
                }).get();
                
                if (selectedIds.length === 0) {
                    showToast('Please select at least one user', 'error');
                    return;
                }
                
                bulkUpdateStatus(selectedIds, 'verify');
            });
            
            // Bulk Cancel
            $('#bulkCancelBtn').on('click', function() {
                var selectedIds = $('.user-checkbox:checked').map(function() {
                    return $(this).val();
                }).get();
                
                if (selectedIds.length === 0) {
                    showToast('Please select at least one user', 'error');
                    return;
                }
                
                bulkUpdateStatus(selectedIds, 'cancel');
            });
            
            // Bulk update function
            function bulkUpdateStatus(userIds, action) {
                var btn = action === 'verify' ? $('#bulkVerifyBtn') : $('#bulkCancelBtn');
                var originalText = btn.html();
                btn.prop('disabled', true).html('<svg class="animate-spin w-4 h-4 inline-block" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg> Processing...');
                
                $.ajax({
                    url: '{{ route("users-verification.bulkUpdateStatus") }}',
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': csrfToken
                    },
                    data: {
                        _token: csrfToken,
                        user_ids: userIds,
                        action: action
                    },
                    success: function(response) {
                        if (response.success) {
                            showToast(response.message, 'success');
                            
                            // Update UI for each user
                            userIds.forEach(function(userId) {
                                var isVerified = action === 'verify';
                                var badge = isVerified ? 
                                    '<span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200" style="background-color: #dcfce7; color: #166534;">✓ True</span>' :
                                    '<span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200" style="background-color: #fee2e2; color: #991b1b;">✗ False</span>';
                                
                                $('#verified-' + userId).html(badge);
                                
                                // Update verified_datetime
                                var datetimeHtml = isVerified ? 
                                    '<span class="text-xs" title="' + new Date().toISOString().slice(0, 19).replace('T', ' ') + '">' + new Date().toLocaleString('en-US', {year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false}).replace(',', '') + '</span>' :
                                    '<span class="text-gray-400 dark:text-gray-500 text-xs">-</span>';
                                
                                $('#verified-datetime-' + userId).html(datetimeHtml);
                                
                                // Uncheck the checkbox
                                $('.user-checkbox[value="' + userId + '"]').prop('checked', false);
                            });
                            
                            // Reset buttons
                            updateBulkButtons();
                            updateSelectAllCheckbox();
                            
                            // Reload page after 1 second
                            setTimeout(function() {
                                location.reload();
                            }, 1000);
                        } else {
                            showToast('Error: ' + (response.error || 'Unknown error'), 'error');
                        }
                        btn.html(originalText).prop('disabled', false);
                    },
                    error: function(xhr) {
                        var errorMsg = 'Network error. Please try again.';
                        if (xhr.responseJSON && xhr.responseJSON.error) {
                            errorMsg = xhr.responseJSON.error;
                        }
                        showToast('Error: ' + errorMsg, 'error');
                        btn.html(originalText).prop('disabled', false);
                    }
                });
            }
        });
    }
});
</script>
@endpush
@endsection
