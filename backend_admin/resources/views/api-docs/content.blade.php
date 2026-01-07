@extends('api-docs.layout')

@section('title', 'Content & Policies APIs - Only Care')

@section('content')
<main class="flex-1 p-8 overflow-y-auto" style="height: calc(100vh - 64px);">
    <div class="max-w-5xl mx-auto">
        <!-- Page Header -->
        <div class="mb-8">
            <h1 class="text-3xl font-bold text-white mb-2">Content & Policies APIs</h1>
            <p class="text-gray-400">Access app policies, terms, and guidelines.</p>
        </div>

        <!-- Privacy Policy -->
        <section class="mb-8 bg-gray-900 rounded-lg p-6 border border-gray-800">
            <div class="mb-4">
                <div class="flex items-center space-x-3 mb-3">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/content/privacy-policy</code>
                </div>
                <h2 class="text-xl font-bold text-white mb-2">Privacy Policy</h2>
                <p class="text-gray-400">Returns the app's privacy policy in HTML or plain text format.</p>
            </div>
            <div class="bg-black rounded p-4">
                <pre class="text-sm text-gray-300"><code>curl -X GET {{ url('/api/v1') }}/content/privacy-policy</code></pre>
            </div>
        </section>

        <!-- Terms & Conditions -->
        <section class="mb-8 bg-gray-900 rounded-lg p-6 border border-gray-800">
            <div class="mb-4">
                <div class="flex items-center space-x-3 mb-3">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/content/terms-conditions</code>
                </div>
                <h2 class="text-xl font-bold text-white mb-2">Terms & Conditions</h2>
                <p class="text-gray-400">Returns the app's terms and conditions of service.</p>
            </div>
            <div class="bg-black rounded p-4">
                <pre class="text-sm text-gray-300"><code>curl -X GET {{ url('/api/v1') }}/content/terms-conditions</code></pre>
            </div>
        </section>

        <!-- Refund Policy -->
        <section class="mb-8 bg-gray-900 rounded-lg p-6 border border-gray-800">
            <div class="mb-4">
                <div class="flex items-center space-x-3 mb-3">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/content/refund-policy</code>
                </div>
                <h2 class="text-xl font-bold text-white mb-2">Refund & Cancellation Policy</h2>
                <p class="text-gray-400">Returns the app's refund and cancellation policy.</p>
            </div>
            <div class="bg-black rounded p-4">
                <pre class="text-sm text-gray-300"><code>curl -X GET {{ url('/api/v1') }}/content/refund-policy</code></pre>
            </div>
        </section>

        <!-- Community Guidelines -->
        <section class="mb-8 bg-gray-900 rounded-lg p-6 border border-gray-800">
            <div class="mb-4">
                <div class="flex items-center space-x-3 mb-3">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/content/community-guidelines</code>
                </div>
                <h2 class="text-xl font-bold text-white mb-2">Community Guidelines</h2>
                <p class="text-gray-400">Returns community guidelines and rules of conduct.</p>
            </div>
            <div class="bg-black rounded p-4">
                <pre class="text-sm text-gray-300"><code>curl -X GET {{ url('/api/v1') }}/content/community-guidelines</code></pre>
            </div>
        </section>

        <div class="bg-blue-900 bg-opacity-20 border border-blue-600 rounded-lg p-4">
            <p class="text-sm text-blue-300">
                <i class="fas fa-info-circle mr-2"></i>
                <strong>Note:</strong> All content endpoints return data in JSON format with HTML content that can be displayed in WebView components.
            </p>
        </div>
    </div>
</main>
@endsection







