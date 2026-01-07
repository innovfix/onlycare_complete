@extends('layouts.app')

@section('title', 'Manage Policies')

@section('content')
<div class="max-w-6xl mx-auto">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">Manage Policies</h2>

        <p class="text-gray-600 dark:text-gray-400 mb-8">
            Edit and manage app policies. Changes will reflect in both the website and the mobile app.
        </p>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Privacy Policy -->
            <div class="border border-gray-200 dark:border-gray-700 rounded-lg p-6">
                <div class="flex items-center mb-4">
                    <svg class="w-8 h-8 text-blue-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path>
                    </svg>
                    <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Privacy Policy</h3>
                </div>
                <p class="text-gray-600 dark:text-gray-400 mb-4 text-sm">
                    Defines how user data is collected, used, and protected.
                </p>
                <div class="flex space-x-3">
                    <a href="{{ route('policies.privacy-policy') }}"
                       class="bg-black hover:bg-gray-800 text-white font-medium rounded-lg px-4 py-2 text-sm">
                        Edit
                    </a>
                    <a href="{{ url('/privacy-policy') }}" target="_blank"
                       class="border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium rounded-lg px-4 py-2 text-sm">
                        View Page
                    </a>
                </div>
            </div>

            <!-- Terms & Conditions -->
            <div class="border border-gray-200 dark:border-gray-700 rounded-lg p-6">
                <div class="flex items-center mb-4">
                    <svg class="w-8 h-8 text-green-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                    </svg>
                    <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Terms & Conditions</h3>
                </div>
                <p class="text-gray-600 dark:text-gray-400 mb-4 text-sm">
                    Rules and guidelines for using the application.
                </p>
                <div class="flex space-x-3">
                    <a href="{{ route('policies.terms-conditions') }}"
                       class="bg-black hover:bg-gray-800 text-white font-medium rounded-lg px-4 py-2 text-sm">
                        Edit
                    </a>
                    <a href="{{ url('/terms-conditions') }}" target="_blank"
                       class="border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium rounded-lg px-4 py-2 text-sm">
                        View Page
                    </a>
                </div>
            </div>

            <!-- Refund Policy -->
            <div class="border border-gray-200 dark:border-gray-700 rounded-lg p-6">
                <div class="flex items-center mb-4">
                    <svg class="w-8 h-8 text-yellow-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"></path>
                    </svg>
                    <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Refund Policy</h3>
                </div>
                <p class="text-gray-600 dark:text-gray-400 mb-4 text-sm">
                    Refund and cancellation terms for purchases.
                </p>
                <div class="flex space-x-3">
                    <a href="{{ route('policies.refund-policy') }}"
                       class="bg-black hover:bg-gray-800 text-white font-medium rounded-lg px-4 py-2 text-sm">
                        Edit
                    </a>
                    <a href="{{ url('/refund-policy') }}" target="_blank"
                       class="border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium rounded-lg px-4 py-2 text-sm">
                        View Page
                    </a>
                </div>
            </div>

            <!-- Community Guidelines -->
            <div class="border border-gray-200 dark:border-gray-700 rounded-lg p-6">
                <div class="flex items-center mb-4">
                    <svg class="w-8 h-8 text-purple-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
                    </svg>
                    <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Community Guidelines</h3>
                </div>
                <p class="text-gray-600 dark:text-gray-400 mb-4 text-sm">
                    Standards of behavior for all community members.
                </p>
                <div class="flex space-x-3">
                    <a href="{{ route('policies.community-guidelines') }}"
                       class="bg-black hover:bg-gray-800 text-white font-medium rounded-lg px-4 py-2 text-sm">
                        Edit
                    </a>
                    <a href="{{ url('/community-guidelines') }}" target="_blank"
                       class="border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300 font-medium rounded-lg px-4 py-2 text-sm">
                        View Page
                    </a>
                </div>
            </div>
        </div>

        <!-- Public URLs Section -->
        <div class="mt-8 p-4 bg-gray-50 dark:bg-gray-900 rounded-lg">
            <h4 class="font-semibold text-gray-900 dark:text-white mb-3">Public URLs (for Play Store / App Store)</h4>
            <div class="space-y-2 text-sm">
                <div class="flex items-center">
                    <span class="text-gray-600 dark:text-gray-400 w-40">Privacy Policy:</span>
                    <code class="bg-gray-200 dark:bg-gray-700 px-2 py-1 rounded text-gray-800 dark:text-gray-200">{{ url('/privacy-policy') }}</code>
                </div>
                <div class="flex items-center">
                    <span class="text-gray-600 dark:text-gray-400 w-40">Terms & Conditions:</span>
                    <code class="bg-gray-200 dark:bg-gray-700 px-2 py-1 rounded text-gray-800 dark:text-gray-200">{{ url('/terms-conditions') }}</code>
                </div>
                <div class="flex items-center">
                    <span class="text-gray-600 dark:text-gray-400 w-40">Refund Policy:</span>
                    <code class="bg-gray-200 dark:bg-gray-700 px-2 py-1 rounded text-gray-800 dark:text-gray-200">{{ url('/refund-policy') }}</code>
                </div>
            </div>
        </div>
    </div>
</div>
@endsection
