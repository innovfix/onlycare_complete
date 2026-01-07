@extends('api-docs.layout')

@section('title', 'Referral & Rewards APIs - Only Care')

@section('content')
<main class="flex-1 flex min-w-0">
    <!-- Documentation (Left Side) -->
    <div class="documentation-panel w-1/2 p-8 overflow-y-auto flex-shrink min-w-0" style="height: calc(100vh - 64px);">
        
        <!-- Get Referral Code -->
        <section id="get-referral-code" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/referral/code</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Get Referral Code</h2>
                <p class="text-gray-400">Get user's unique referral code for sharing with friends.</p>
            </div>

            <div class="mb-4 bg-yellow-900 bg-opacity-30 border border-yellow-700 rounded-lg px-4 py-3">
                <div class="flex items-start">
                    <i class="fas fa-lock text-yellow-400 mr-3 mt-1"></i>
                    <div>
                        <span class="text-sm font-semibold text-yellow-400">Requires Authentication: Bearer Token</span>
                    </div>
                </div>
            </div>

            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">Success - Referral code retrieved</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized - Invalid or missing token</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- Apply Referral Code -->
        <section id="apply-referral-code" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/referral/apply</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Apply Referral Code</h2>
                <p class="text-gray-400">Apply a referral code to earn bonus coins. Both the referrer and referee receive coins.</p>
            </div>

            <div class="mb-4 bg-yellow-900 bg-opacity-30 border border-yellow-700 rounded-lg px-4 py-3">
                <div class="flex items-start">
                    <i class="fas fa-lock text-yellow-400 mr-3 mt-1"></i>
                    <div>
                        <span class="text-sm font-semibold text-yellow-400">Requires Authentication: Bearer Token</span>
                    </div>
                </div>
            </div>

            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">REQUEST PARAMETERS</h4>
                <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                    <table class="w-full text-sm">
                        <thead class="bg-black">
                            <tr>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Parameter</th>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Type</th>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Required</th>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Description</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">referral_code</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">The referral code to apply (6-10 characters)</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">Success - Referral code applied successfully</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">400</span>
                        <span class="text-gray-400">Bad Request - Invalid code or already used</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized - Invalid or missing token</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- Get Referral History -->
        <section id="get-referral-history" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/referral/history</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Get Referral History</h2>
                <p class="text-gray-400">Get list of users who have used your referral code and rewards earned.</p>
            </div>

            <div class="mb-4 bg-yellow-900 bg-opacity-30 border border-yellow-700 rounded-lg px-4 py-3">
                <div class="flex items-start">
                    <i class="fas fa-lock text-yellow-400 mr-3 mt-1"></i>
                    <div>
                        <span class="text-sm font-semibold text-yellow-400">Requires Authentication: Bearer Token</span>
                    </div>
                </div>
            </div>

            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">QUERY PARAMETERS</h4>
                <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                    <table class="w-full text-sm">
                        <thead class="bg-black">
                            <tr>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Parameter</th>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Type</th>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Required</th>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Description</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">page</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3"><span class="text-gray-500">✗</span></td>
                                <td class="px-4 py-3 text-gray-400">Page number (default: 1)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">per_page</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3"><span class="text-gray-500">✗</span></td>
                                <td class="px-4 py-3 text-gray-400">Items per page (default: 20, max: 100)</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">Success - Referral history retrieved</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized - Invalid or missing token</span>
                    </div>
                </div>
            </div>
        </section>

    </div>

    <!-- Code Examples Panel (Right Side) -->
    <div class="code-examples-panel w-1/2 bg-[#0D1117] p-8 overflow-y-auto border-l border-gray-800" style="height: calc(100vh - 64px);">
        <div class="sticky top-0 bg-[#0D1117] pb-4 z-10">
            <h3 class="text-lg font-semibold text-white mb-4">API Examples</h3>
        </div>

        <!-- Get Referral Code Example -->
        <div id="code-get-referral-code" class="code-example mb-8">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                </div>
                <pre class="p-4 text-sm overflow-x-auto"><code>curl -X GET {{ url('/api/v1') }}/referral/code \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Accept: application/json"</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mt-6 mb-4">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                    </div>
                    <pre class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "referral_code": "CTLA8241",
  "referral_url": "https://onlycare.app/invite/CTLA8241",
  "my_invites": 5,
  "per_invite_coins": 10,
  "total_coins_earned": 50,
  "share_message": "Join me on Only Care! Use my referral code: CTLA8241 and get bonus coins!",
  "whatsapp_share_url": "https://wa.me/?text=Join%20me%20on%20Only%20Care!"
}</code></pre>
                </div>
            </div>

            <!-- Error Response -->
            <div class="mb-4">
                <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">401 Unauthorized</span>
                    </div>
                    <pre class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Unauthenticated."
}</code></pre>
                </div>
            </div>
        </div>

        <!-- Apply Referral Code Example -->
        <div id="code-apply-referral-code" class="code-example mb-8">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                </div>
                <pre class="p-4 text-sm overflow-x-auto"><code>curl -X POST {{ url('/api/v1') }}/referral/apply \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "referral_code": "CTLA8241"
  }'</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mt-6 mb-4">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                    </div>
                    <pre class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "Referral code applied successfully! You earned 10 bonus coins!",
  "coins_earned": 10,
  "referrer_coins": 10,
  "new_balance": 60
}</code></pre>
                </div>
            </div>

            <!-- Error Response - Already Used -->
            <div class="mb-4">
                <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">400 Bad Request - Already Used</span>
                    </div>
                    <pre class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "You have already used a referral code"
}</code></pre>
                </div>
            </div>

            <!-- Error Response - Invalid Code -->
            <div class="mb-4">
                <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">400 Bad Request - Invalid Code</span>
                    </div>
                    <pre class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Invalid referral code"
}</code></pre>
                </div>
            </div>
        </div>

        <!-- Get Referral History Example -->
        <div id="code-get-referral-history" class="code-example mb-8">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                </div>
                <pre class="p-4 text-sm overflow-x-auto"><code>curl -X GET "{{ url('/api/v1') }}/referral/history?page=1&per_page=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Accept: application/json"</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mt-6 mb-4">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                    </div>
                    <pre class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "referrals": [
    {
      "id": 123,
      "referee_name": "John Doe",
      "referee_phone": "+91987654****",
      "joined_at": "2024-03-15T10:30:00Z",
      "coins_earned": 10,
      "status": "completed"
    },
    {
      "id": 124,
      "referee_name": "Jane Smith",
      "referee_phone": "+91987655****",
      "joined_at": "2024-03-16T14:20:00Z",
      "coins_earned": 10,
      "status": "completed"
    }
  ],
  "total_referrals": 5,
  "total_coins_earned": 50,
  "pagination": {
    "current_page": 1,
    "per_page": 20,
    "total_pages": 1,
    "total_items": 5
  }
}</code></pre>
                </div>
            </div>

            <!-- Error Response -->
            <div class="mb-4">
                <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">401 Unauthorized</span>
                    </div>
                    <pre class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Unauthenticated."
}</code></pre>
                </div>
            </div>
        </div>
    </div>
</main>
@endsection
