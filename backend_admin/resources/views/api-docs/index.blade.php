@extends('api-docs.layout')

@section('title', 'Only Care - API Documentation Overview')

@section('content')
<main class="flex-1 p-8 overflow-y-auto" style="height: calc(100vh - 64px);">
    <div class="max-w-5xl mx-auto">
        <!-- Hero Section -->
        <div class="mb-12">
            <h1 class="text-4xl font-bold text-white mb-4">Welcome to Only Care API</h1>
            <p class="text-xl text-gray-400 mb-6">
                Complete REST API documentation for integrating with the Only Care video calling platform.
            </p>
            <div class="flex space-x-4">
                <span class="px-4 py-2 bg-purple-600 text-white rounded-lg font-semibold">REST API</span>
                <span class="px-4 py-2 bg-gray-800 text-gray-300 rounded-lg font-semibold">Version 1.0</span>
                <span class="px-4 py-2 bg-gray-800 text-gray-300 rounded-lg font-mono text-sm">JSON</span>
            </div>
        </div>

        <!-- Quick Start -->
        <div class="bg-gray-900 border border-gray-800 rounded-lg p-6 mb-8">
            <h2 class="text-2xl font-bold text-white mb-4">
                <i class="fas fa-rocket text-purple-400 mr-2"></i>
                Quick Start
            </h2>
            <div class="space-y-4">
                <div>
                    <h3 class="text-lg font-semibold text-white mb-2">Base URL</h3>
                    <code class="block bg-black text-green-400 p-3 rounded font-mono text-sm">
                        {{ url('/api/v1') }}
                    </code>
                </div>
                <div>
                    <h3 class="text-lg font-semibold text-white mb-2">Authentication</h3>
                    <p class="text-gray-400 mb-2">Most endpoints require Bearer token authentication:</p>
                    <code class="block bg-black text-gray-300 p-3 rounded font-mono text-sm">
                        Authorization: Bearer YOUR_ACCESS_TOKEN
                    </code>
                </div>
                <div>
                    <h3 class="text-lg font-semibold text-white mb-2">Content Type</h3>
                    <code class="block bg-black text-gray-300 p-3 rounded font-mono text-sm">
                        Content-Type: application/json
                    </code>
                </div>
            </div>
        </div>

        <!-- API Sections -->
        <div class="mb-8">
            <h2 class="text-2xl font-bold text-white mb-6">
                <i class="fas fa-book text-purple-400 mr-2"></i>
                API Sections
            </h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <!-- Authentication -->
                <a href="{{ route('api.docs.auth') }}" class="bg-gray-900 border border-gray-800 rounded-lg p-6 hover:border-purple-600 transition-all group">
                    <div class="flex items-start">
                        <div class="flex-shrink-0">
                            <i class="fas fa-key text-3xl text-yellow-400 group-hover:text-yellow-300"></i>
                        </div>
                        <div class="ml-4">
                            <h3 class="text-lg font-bold text-white mb-2 group-hover:text-purple-400">Authentication</h3>
                            <p class="text-sm text-gray-400">OTP-based authentication flow including send OTP, verify OTP, and user registration</p>
                            <div class="mt-3 flex space-x-2">
                                <span class="px-2 py-1 bg-green-600 text-white text-xs rounded">POST</span>
                                <span class="text-xs text-gray-500">3 endpoints</span>
                            </div>
                        </div>
                    </div>
                </a>

                <!-- Users / Creators -->
                <a href="{{ route('api.docs.creators') }}" class="bg-gray-900 border border-gray-800 rounded-lg p-6 hover:border-purple-600 transition-all group">
                    <div class="flex items-start">
                        <div class="flex-shrink-0">
                            <i class="fas fa-users text-3xl text-blue-400 group-hover:text-blue-300"></i>
                        </div>
                        <div class="ml-4">
                            <h3 class="text-lg font-bold text-white mb-2 group-hover:text-purple-400">Users / Creators</h3>
                            <p class="text-sm text-gray-400">Get list of available creators for the home screen with filtering and pagination</p>
                            <div class="mt-3 flex space-x-2">
                                <span class="px-2 py-1 bg-blue-600 text-white text-xs rounded">GET</span>
                                <span class="text-xs text-gray-500">1 endpoint</span>
                            </div>
                        </div>
                    </div>
                </a>

                <!-- Call APIs -->
                <a href="{{ route('api.docs.calls') }}" class="bg-gray-900 border border-gray-800 rounded-lg p-6 hover:border-purple-600 transition-all group">
                    <div class="flex items-start">
                        <div class="flex-shrink-0">
                            <i class="fas fa-phone text-3xl text-green-400 group-hover:text-green-300"></i>
                        </div>
                        <div class="ml-4">
                            <h3 class="text-lg font-bold text-white mb-2 group-hover:text-purple-400">Call APIs</h3>
                            <p class="text-sm text-gray-400">Initiate video calls and retrieve recent call sessions with detailed information</p>
                            <div class="mt-3 flex space-x-2">
                                <span class="px-2 py-1 bg-green-600 text-white text-xs rounded">POST</span>
                                <span class="px-2 py-1 bg-blue-600 text-white text-xs rounded">GET</span>
                                <span class="text-xs text-gray-500">2 endpoints</span>
                            </div>
                        </div>
                    </div>
                </a>

                <!-- Wallet & Payments -->
                <a href="{{ route('api.docs.wallet') }}" class="bg-gray-900 border border-gray-800 rounded-lg p-6 hover:border-purple-600 transition-all group">
                    <div class="flex items-start">
                        <div class="flex-shrink-0">
                            <i class="fas fa-wallet text-3xl text-pink-400 group-hover:text-pink-300"></i>
                        </div>
                        <div class="ml-4">
                            <h3 class="text-lg font-bold text-white mb-2 group-hover:text-purple-400">Wallet & Payments</h3>
                            <p class="text-sm text-gray-400">Coin packages, wallet balance, purchase initiation, verification, and transaction history</p>
                            <div class="mt-3 flex space-x-2">
                                <span class="px-2 py-1 bg-green-600 text-white text-xs rounded">POST</span>
                                <span class="px-2 py-1 bg-blue-600 text-white text-xs rounded">GET</span>
                                <span class="text-xs text-gray-500">5 endpoints</span>
                            </div>
                        </div>
                    </div>
                </a>

                <!-- Referral & Rewards -->
                <a href="{{ route('api.docs.referrals') }}" class="bg-gray-900 border border-gray-800 rounded-lg p-6 hover:border-purple-600 transition-all group">
                    <div class="flex items-start">
                        <div class="flex-shrink-0">
                            <i class="fas fa-gift text-3xl text-orange-400 group-hover:text-orange-300"></i>
                        </div>
                        <div class="ml-4">
                            <h3 class="text-lg font-bold text-white mb-2 group-hover:text-purple-400">Referral & Rewards</h3>
                            <p class="text-sm text-gray-400">Referral code management, applying referral codes, and viewing referral history</p>
                            <div class="mt-3 flex space-x-2">
                                <span class="px-2 py-1 bg-green-600 text-white text-xs rounded">POST</span>
                                <span class="px-2 py-1 bg-blue-600 text-white text-xs rounded">GET</span>
                                <span class="text-xs text-gray-500">3 endpoints</span>
                            </div>
                        </div>
                    </div>
                </a>

                <!-- Content & Policies -->
                <a href="{{ route('api.docs.content') }}" class="bg-gray-900 border border-gray-800 rounded-lg p-6 hover:border-purple-600 transition-all group">
                    <div class="flex items-start">
                        <div class="flex-shrink-0">
                            <i class="fas fa-file-alt text-3xl text-cyan-400 group-hover:text-cyan-300"></i>
                        </div>
                        <div class="ml-4">
                            <h3 class="text-lg font-bold text-white mb-2 group-hover:text-purple-400">Content & Policies</h3>
                            <p class="text-sm text-gray-400">Privacy policy, terms & conditions, refund policy, and community guidelines</p>
                            <div class="mt-3 flex space-x-2">
                                <span class="px-2 py-1 bg-blue-600 text-white text-xs rounded">GET</span>
                                <span class="text-xs text-gray-500">4 endpoints</span>
                            </div>
                        </div>
                    </div>
                </a>
            </div>
        </div>

        <!-- Response Format -->
        <div class="bg-gray-900 border border-gray-800 rounded-lg p-6 mb-8">
            <h2 class="text-2xl font-bold text-white mb-4">
                <i class="fas fa-code text-purple-400 mr-2"></i>
                Response Format
            </h2>
            <p class="text-gray-400 mb-4">All API responses follow a consistent JSON format:</p>
            
            <div class="mb-6">
                <h3 class="text-lg font-semibold text-white mb-2">Success Response</h3>
                <pre class="p-4 rounded overflow-x-auto text-sm"><code>{
    "success": true,
    "message": "Operation successful",
    "data": {
        // Response data here
    }
}</code></pre>
            </div>

            <div>
                <h3 class="text-lg font-semibold text-white mb-2">Error Response</h3>
                <pre class="p-4 rounded overflow-x-auto text-sm"><code>{
    "success": false,
    "message": "Error description",
    "errors": {
        // Validation errors if applicable
    }
}</code></pre>
            </div>
        </div>

        <!-- HTTP Status Codes -->
        <div class="bg-gray-900 border border-gray-800 rounded-lg p-6 mb-8">
            <h2 class="text-2xl font-bold text-white mb-4">
                <i class="fas fa-list-ol text-purple-400 mr-2"></i>
                HTTP Status Codes
            </h2>
            <div class="space-y-2">
                <div class="flex items-center">
                    <span class="px-3 py-1 bg-green-600 text-white text-sm font-mono rounded mr-3">200</span>
                    <span class="text-gray-400">Success - Request completed successfully</span>
                </div>
                <div class="flex items-center">
                    <span class="px-3 py-1 bg-yellow-600 text-white text-sm font-mono rounded mr-3">201</span>
                    <span class="text-gray-400">Created - Resource created successfully</span>
                </div>
                <div class="flex items-center">
                    <span class="px-3 py-1 bg-orange-600 text-white text-sm font-mono rounded mr-3">400</span>
                    <span class="text-gray-400">Bad Request - Invalid input parameters</span>
                </div>
                <div class="flex items-center">
                    <span class="px-3 py-1 bg-red-600 text-white text-sm font-mono rounded mr-3">401</span>
                    <span class="text-gray-400">Unauthorized - Invalid or missing authentication token</span>
                </div>
                <div class="flex items-center">
                    <span class="px-3 py-1 bg-red-600 text-white text-sm font-mono rounded mr-3">403</span>
                    <span class="text-gray-400">Forbidden - Insufficient permissions</span>
                </div>
                <div class="flex items-center">
                    <span class="px-3 py-1 bg-red-600 text-white text-sm font-mono rounded mr-3">404</span>
                    <span class="text-gray-400">Not Found - Resource doesn't exist</span>
                </div>
                <div class="flex items-center">
                    <span class="px-3 py-1 bg-red-600 text-white text-sm font-mono rounded mr-3">422</span>
                    <span class="text-gray-400">Unprocessable Entity - Validation failed</span>
                </div>
                <div class="flex items-center">
                    <span class="px-3 py-1 bg-gray-600 text-white text-sm font-mono rounded mr-3">500</span>
                    <span class="text-gray-400">Server Error - Internal server error</span>
                </div>
            </div>
        </div>

        <!-- Support -->
        <div class="bg-gradient-to-r from-purple-900 to-pink-900 border border-purple-700 rounded-lg p-6">
            <h2 class="text-2xl font-bold text-white mb-2">
                <i class="fas fa-headset text-white mr-2"></i>
                Need Help?
            </h2>
            <p class="text-gray-200 mb-4">
                If you have questions or need assistance with the API, please contact our support team.
            </p>
            <a href="mailto:support@onlycare.com" class="inline-flex items-center px-4 py-2 bg-white text-purple-900 font-semibold rounded-lg hover:bg-gray-100 transition">
                <i class="fas fa-envelope mr-2"></i>
                Contact Support
            </a>
        </div>
    </div>
</main>
@endsection







