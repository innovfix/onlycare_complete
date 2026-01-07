@extends('api-docs.layout')

@section('title', 'Call APIs Documentation')

@section('content')
<main class="flex-1 flex min-w-0">
    <!-- Documentation (Left Side) -->
    <div class="documentation-panel w-1/2 p-8 overflow-y-auto flex-shrink min-w-0" style="height: calc(100vh - 64px);">
        <!-- Page Header -->
        <div class="mb-8">
            <h1 class="text-3xl font-bold text-white mb-2">Call APIs</h1>
            <p class="text-gray-400">Comprehensive call management system with audio and video support.</p>
        </div>

        <!-- Initiate Call Endpoint -->
        <section id="initiate-call" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/calls/initiate</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Initiate Audio/Video Call</h2>
                <p class="text-gray-400">Initiate an audio or video call with a creator. This endpoint performs 14 critical validations including coin balance, blocking status, busy check, and self-call prevention before connecting the call.</p>
            </div>

            <div class="mb-4 p-3 bg-yellow-900 bg-opacity-20 border border-yellow-600 rounded">
                <p class="text-sm text-yellow-300"><i class="fas fa-lock mr-1"></i><strong>Requires Authentication:</strong> Bearer Token</p>
            </div>

            <!-- Request Parameters -->
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
                                <td class="px-4 py-3 font-mono text-purple-400">receiver_id</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Creator's user ID (format: USR_xxxxx)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">call_type</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Type of call: "AUDIO" or "VIDEO"</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Validations Performed -->
            <div class="mb-6">
                <h3 class="text-lg font-bold text-white mb-3"><i class="fas fa-shield-alt mr-2 text-red-500"></i>VALIDATIONS PERFORMED (14 CHECKS)</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-3 bg-purple-900 bg-opacity-10 border border-purple-700 rounded-lg p-4">
                    <!-- Left Column -->
                    <div class="space-y-2 text-sm">
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">1. Authentication check</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">2. Request parameters valid</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">3. Caller exists</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">4. Caller not deleted</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">5. Caller not blocked</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">6. Receiver exists</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">7. Self-call prevention</span>
                        </div>
                    </div>
                    <!-- Right Column -->
                    <div class="space-y-2 text-sm">
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">8. Receiver not deleted</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">9. Blocking check (privacy)</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">10. Receiver is online</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">11. Busy status check</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">12. Call type enabled</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">13. Sufficient coins</span>
                        </div>
                        <div class="flex items-start">
                            <i class="fas fa-check-circle text-green-400 mt-0.5 mr-2 flex-shrink-0"></i>
                            <span class="text-gray-300">14. Balance time calculated</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Response Codes -->
            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2 text-sm">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">Call initiated successfully</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">400</span>
                        <span class="text-gray-400">Self-call / Blocked / Busy / Offline / Insufficient coins</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized (invalid token)</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">404</span>
                        <span class="text-gray-400">User not found</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">422</span>
                        <span class="text-gray-400">Validation error (invalid parameters)</span>
                    </div>
                </div>
            </div>

            <!-- Coin Rates Info -->
            <div class="bg-purple-900 bg-opacity-20 border border-purple-600 rounded-lg p-4">
                <h4 class="text-sm font-semibold text-purple-300 mb-2"><i class="fas fa-coins mr-1"></i>COIN RATES</h4>
                <div class="grid grid-cols-2 gap-4 text-sm">
                    <div class="flex items-center space-x-2">
                        <i class="fas fa-microphone text-blue-400"></i>
                        <span class="text-gray-300">Audio: <strong class="text-white">10 coins/min</strong></span>
                    </div>
                    <div class="flex items-center space-x-2">
                        <i class="fas fa-video text-pink-400"></i>
                        <span class="text-gray-300">Video: <strong class="text-white">60 coins/min</strong></span>
                    </div>
                </div>
                <p class="text-xs text-gray-500 mt-2">* Minimum balance required to initiate call</p>
            </div>
        </section>

        <!-- Accept Call Endpoint -->
        <section id="accept-call" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/calls/{call_id}/accept</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Accept Incoming Call</h2>
                <p class="text-gray-400">Receiver accepts an incoming call. Changes call status to ONGOING, marks both users as busy, and records receiver_joined_at timestamp for accurate billing.</p>
            </div>

            <div class="mb-4 p-3 bg-yellow-900 bg-opacity-20 border border-yellow-600 rounded">
                <p class="text-sm text-yellow-300"><i class="fas fa-lock mr-1"></i><strong>Requires Authentication:</strong> Bearer Token (Receiver)</p>
            </div>

            <div class="mb-6 bg-gray-900 rounded-lg p-4">
                <p class="text-sm text-gray-300"><strong>No request body required.</strong> Call ID is passed in URL parameter.</p>
            </div>

            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE (200 OK)</h4>
                <pre class="bg-black text-green-400 rounded-lg p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "Call accepted",
  "call": {
    "id": "CALL_17623328403256",
    "status": "ONGOING",
    "started_at": "2025-11-05T08:55:00+00:00",
    "receiver_joined_at": "2025-11-05T08:55:00+00:00",
    "agora_app_id": "your_agora_app_id",
    "agora_token": "007eJxTYBBa...",
    "agora_uid": 0,
    "channel_name": "call_CALL_17623328403256"
  }
}</code></pre>
            </div>
        </section>

        <!-- Reject Call Endpoint -->
        <section id="reject-call" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/calls/{call_id}/reject</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Reject Incoming Call</h2>
                <p class="text-gray-400">Receiver rejects an incoming call. Changes call status to REJECTED. No coins are charged.</p>
            </div>

            <div class="mb-4 p-3 bg-yellow-900 bg-opacity-20 border border-yellow-600 rounded">
                <p class="text-sm text-yellow-300"><i class="fas fa-lock mr-1"></i><strong>Requires Authentication:</strong> Bearer Token (Receiver)</p>
            </div>

            <div class="mb-6 bg-gray-900 rounded-lg p-4">
                <p class="text-sm text-gray-300"><strong>No request body required.</strong> Call ID is passed in URL parameter.</p>
            </div>

            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE (200 OK)</h4>
                <pre class="bg-black text-green-400 rounded-lg p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "Call rejected"
}</code></pre>
            </div>
        </section>

        <!-- End Call Endpoint -->
        <section id="end-call" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/calls/{call_id}/end</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">End Call</h2>
                <p class="text-gray-400">Either party (caller or receiver) can end the call. Server calculates duration from receiver_joined_at timestamp (excluding ringing time), deducts coins from caller, adds to receiver, and creates transaction records.</p>
            </div>

            <div class="mb-4 p-3 bg-yellow-900 bg-opacity-20 border border-yellow-600 rounded">
                <p class="text-sm text-yellow-300"><i class="fas fa-lock mr-1"></i><strong>Requires Authentication:</strong> Bearer Token</p>
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
                                <td class="px-4 py-3 font-mono text-purple-400">duration</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Call duration in SECONDS from client side (e.g., 120 for 2 minutes). Server validates this but uses server-calculated duration for billing.</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE (200 OK)</h4>
                <pre class="bg-black text-green-400 rounded-lg p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "Call ended successfully",
  "call": {
    "id": "CALL_17623328403256",
    "status": "ENDED",
    "duration": 120,
    "coins_spent": 20,
    "coins_earned": 20,
    "started_at": "2025-11-05T08:54:30+00:00",
    "receiver_joined_at": "2025-11-05T08:55:00+00:00",
    "ended_at": "2025-11-05T08:57:00+00:00"
  },
  "caller_balance": 980,
  "receiver_earnings": 20
}</code></pre>
            </div>

            <div class="bg-blue-900 bg-opacity-20 border border-blue-600 rounded-lg p-4 mt-6">
                <h4 class="text-sm font-bold text-blue-300 mb-2"><i class="fas fa-calculator mr-2"></i>Coin Calculation (Server-Side)</h4>
                <p class="text-sm text-gray-300"><strong>Duration</strong> = ended_at - receiver_joined_at (excludes ringing time)</p>
                <p class="text-sm text-gray-300"><strong>Minutes</strong> = ceil(duration / 60)</p>
                <p class="text-sm text-gray-300"><strong>Coins</strong> = Minutes × Rate (10 coins/min for audio, 60 coins/min for video)</p>
                <p class="text-sm text-gray-400 mt-2"><strong>Example:</strong> 120 seconds talk time = 2 minutes × 10 = 20 coins</p>
                <p class="text-sm text-yellow-300 mt-2"><i class="fas fa-info-circle mr-1"></i><strong>Fair Billing:</strong> Users are charged only for actual conversation time, NOT ringing time</p>
            </div>
        </section>

        <!-- Rate Call Endpoint -->
        <section id="rate-call" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/calls/{call_id}/rate</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Rate Call</h2>
                <p class="text-gray-400">Caller rates the call experience. Updates receiver's average rating.</p>
            </div>

            <div class="mb-4 p-3 bg-yellow-900 bg-opacity-20 border border-yellow-600 rounded">
                <p class="text-sm text-yellow-300"><i class="fas fa-lock mr-1"></i><strong>Requires Authentication:</strong> Bearer Token (Caller)</p>
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
                                <td class="px-4 py-3 font-mono text-purple-400">rating</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Rating from 1 to 5 stars</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">feedback</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-gray-500">✗</span></td>
                                <td class="px-4 py-3 text-gray-400">Optional feedback (max 500 characters)</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE (200 OK)</h4>
                <pre class="bg-black text-green-400 rounded-lg p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "Rating submitted successfully"
}</code></pre>
            </div>
        </section>

        <!-- Get Call History Endpoint -->
        <section id="call-history" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/calls/history</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Get Call History</h2>
                <p class="text-gray-400">Retrieve paginated list of all past ended calls.</p>
            </div>

            <div class="mb-4 p-3 bg-yellow-900 bg-opacity-20 border border-yellow-600 rounded">
                <p class="text-sm text-yellow-300"><i class="fas fa-lock mr-1"></i><strong>Requires Authentication:</strong> Bearer Token</p>
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
                                <td class="px-4 py-3 font-mono text-purple-400">limit</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3"><span class="text-gray-500">✗</span></td>
                                <td class="px-4 py-3 text-gray-400">Number of results (1-50, default: 20)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">page</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3"><span class="text-gray-500">✗</span></td>
                                <td class="px-4 py-3 text-gray-400">Page number (default: 1)</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE (200 OK)</h4>
                <pre class="bg-black text-green-400 rounded-lg p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "calls": [
    {
      "id": "CALL_17623328403256",
      "other_user": {
        "id": "USR_1762281762005",
        "name": "Divya_Hindi",
        "profile_image": "https://..."
      },
      "call_type": "AUDIO",
      "duration": 120,
      "coins_spent": 20,
      "rating": 5,
      "created_at": "2025-11-05T08:54:00+00:00"
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 5,
    "total_items": 87,
    "per_page": 20
  }
}</code></pre>
            </div>
        </section>

        <!-- Get Recent Sessions Endpoint -->
        <section id="recent-sessions" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/calls/recent-sessions</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Get Recent Sessions</h2>
                <p class="text-gray-400">Get recent call sessions with user details and call availability status. Perfect for displaying in the "Recent" tab.</p>
            </div>

            <div class="mb-4 p-3 bg-yellow-900 bg-opacity-20 border border-yellow-600 rounded">
                <p class="text-sm text-yellow-300"><i class="fas fa-lock mr-1"></i><strong>Requires Authentication:</strong> Bearer Token</p>
            </div>

            <!-- Query Parameters -->
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
                                <td class="px-4 py-3 text-gray-400">Items per page (default: 20, max: 50)</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Response Fields -->
            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE FIELDS</h4>
                <div class="bg-black rounded-lg border border-gray-800 p-4 space-y-3 text-sm">
                    <div>
                        <code class="text-purple-400">sessions[]</code>
                        <span class="text-gray-400"> - Array of recent call sessions</span>
                    </div>
                    <div class="ml-4">
                        <code class="text-blue-400">user</code>
                        <span class="text-gray-400"> - User details (id, name, age, profile_image)</span>
                    </div>
                    <div class="ml-4">
                        <code class="text-blue-400">is_online</code>
                        <span class="text-gray-400"> - Current online status</span>
                    </div>
                    <div class="ml-4">
                        <code class="text-blue-400">audio_call_enabled</code>
                        <span class="text-gray-400"> - Audio call availability</span>
                    </div>
                    <div class="ml-4">
                        <code class="text-blue-400">video_call_enabled</code>
                        <span class="text-gray-400"> - Video call availability</span>
                    </div>
                    <div class="ml-4">
                        <code class="text-blue-400">call_type</code>
                        <span class="text-gray-400"> - Type of previous call (AUDIO/VIDEO)</span>
                    </div>
                    <div class="ml-4">
                        <code class="text-blue-400">duration</code>
                        <span class="text-gray-400"> - Call duration in seconds</span>
                    </div>
                    <div class="ml-4">
                        <code class="text-blue-400">duration_formatted</code>
                        <span class="text-gray-400"> - Human-readable duration</span>
                    </div>
                    <div class="ml-4">
                        <code class="text-blue-400">coins_spent</code>
                        <span class="text-gray-400"> - Coins spent on that call</span>
                    </div>
                </div>
            </div>

            <!-- Response Codes -->
            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2 text-sm">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">Recent sessions retrieved successfully</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized (invalid token)</span>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- Code Examples (Right Side) -->
    <div class="code-panel w-1/2 bg-black p-8 overflow-y-auto border-l border-gray-800 flex-shrink min-w-0" style="height: calc(100vh - 64px);">
        <!-- Interactive Testing Form - Initiate Call -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-blue-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-blue-400 mr-2"></i>
                Test Initiate Call
            </h3>
            <form id="testInitiateCallForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">
                        <i class="fas fa-key text-yellow-400 mr-1"></i> Bearer Token
                    </label>
                    <input type="text" name="token" placeholder="Enter your access token" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Receiver ID</label>
                    <input type="text" name="receiver_id" placeholder="USR_1234567890 or 1234567890" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                    <p class="text-xs text-gray-500 mt-1">With or without USR_ prefix (e.g., USR_1762281762005 or 1762281762005)</p>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Call Type</label>
                    <select name="call_type" class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                        <option value="">Select Call Type</option>
                        <option value="AUDIO">Audio Call</option>
                        <option value="VIDEO">Video Call</option>
                    </select>
                </div>
                <button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-play mr-2"></i>Run Test
                </button>
            </form>
            <div id="testInitiateCallResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusInitiate" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testInitiateCallResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300" style="max-height: 400px;"></pre>
                </div>
            </div>
        </div>

        <!-- Interactive Testing Form - Recent Sessions -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-green-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-green-400 mr-2"></i>
                Test Recent Sessions
            </h3>
            <form id="testRecentSessionsForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">
                        <i class="fas fa-key text-yellow-400 mr-1"></i> Bearer Token
                    </label>
                    <input type="text" name="token" placeholder="Enter your access token" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <button type="submit" class="w-full bg-green-600 hover:bg-green-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-play mr-2"></i>Run Test
                </button>
            </form>
            <div id="testRecentSessionsResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusRecent" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testRecentSessionsResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300" style="max-height: 400px;"></pre>
                </div>
            </div>
        </div>

        <!-- Interactive Testing Form - Accept Call -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-green-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-green-400 mr-2"></i>
                Test Accept Call
            </h3>
            <form id="testAcceptCallForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">
                        <i class="fas fa-key text-yellow-400 mr-1"></i> Bearer Token (Receiver)
                    </label>
                    <input type="text" name="token" placeholder="Enter receiver's access token" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Call ID</label>
                    <input type="text" name="call_id" placeholder="CALL_1234567890 or 1234567890" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <button type="submit" class="w-full bg-green-600 hover:bg-green-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-check mr-2"></i>Accept Call
                </button>
            </form>
            <div id="testAcceptCallResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusAccept" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testAcceptCallResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300"></pre>
                </div>
            </div>
        </div>

        <!-- Interactive Testing Form - Reject Call -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-red-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-red-400 mr-2"></i>
                Test Reject Call
            </h3>
            <form id="testRejectCallForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">
                        <i class="fas fa-key text-yellow-400 mr-1"></i> Bearer Token (Receiver)
                    </label>
                    <input type="text" name="token" placeholder="Enter receiver's access token" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Call ID</label>
                    <input type="text" name="call_id" placeholder="CALL_1234567890 or 1234567890" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <button type="submit" class="w-full bg-red-600 hover:bg-red-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-times mr-2"></i>Reject Call
                </button>
            </form>
            <div id="testRejectCallResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusReject" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testRejectCallResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300"></pre>
                </div>
            </div>
        </div>

        <!-- Interactive Testing Form - End Call -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-orange-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-orange-400 mr-2"></i>
                Test End Call
            </h3>
            <form id="testEndCallForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">
                        <i class="fas fa-key text-yellow-400 mr-1"></i> Bearer Token
                    </label>
                    <input type="text" name="token" placeholder="Enter your access token" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Call ID</label>
                    <input type="text" name="call_id" placeholder="CALL_1234567890 or 1234567890" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Duration (seconds)</label>
                    <input type="number" name="duration" placeholder="120" min="1" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                    <p class="text-xs text-gray-500 mt-1">Example: 120 seconds = 2 minutes</p>
                </div>
                <button type="submit" class="w-full bg-orange-600 hover:bg-orange-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-phone-slash mr-2"></i>End Call
                </button>
            </form>
            <div id="testEndCallResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusEnd" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testEndCallResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300"></pre>
                </div>
            </div>
        </div>

        <!-- Interactive Testing Form - Rate Call -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-yellow-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-yellow-400 mr-2"></i>
                Test Rate Call
            </h3>
            <form id="testRateCallForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">
                        <i class="fas fa-key text-yellow-400 mr-1"></i> Bearer Token (Caller)
                    </label>
                    <input type="text" name="token" placeholder="Enter caller's access token" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Call ID</label>
                    <input type="text" name="call_id" placeholder="CALL_1234567890 or 1234567890" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Rating</label>
                    <select name="rating" class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                        <option value="">Select rating...</option>
                        <option value="5">⭐⭐⭐⭐⭐ (5 stars)</option>
                        <option value="4">⭐⭐⭐⭐ (4 stars)</option>
                        <option value="3">⭐⭐⭐ (3 stars)</option>
                        <option value="2">⭐⭐ (2 stars)</option>
                        <option value="1">⭐ (1 star)</option>
                    </select>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Feedback (Optional)</label>
                    <textarea name="feedback" placeholder="Enter your feedback (max 500 characters)" maxlength="500" rows="3"
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none resize-none"></textarea>
                </div>
                <button type="submit" class="w-full bg-yellow-600 hover:bg-yellow-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-star mr-2"></i>Submit Rating
                </button>
            </form>
            <div id="testRateCallResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusRate" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testRateCallResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300"></pre>
                </div>
            </div>
        </div>

        <!-- Interactive Testing Form - Get Call History -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-purple-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-purple-400 mr-2"></i>
                Test Get Call History
            </h3>
            <form id="testCallHistoryForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">
                        <i class="fas fa-key text-yellow-400 mr-1"></i> Bearer Token
                    </label>
                    <input type="text" name="token" placeholder="Enter your access token" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Limit (Optional)</label>
                    <input type="number" name="limit" placeholder="20" min="1" max="50" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none">
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Page (Optional)</label>
                    <input type="number" name="page" placeholder="1" min="1" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none">
                </div>
                <button type="submit" class="w-full bg-purple-600 hover:bg-purple-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-history mr-2"></i>Get Call History
                </button>
            </form>
            <div id="testCallHistoryResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusHistory" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testCallHistoryResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300" style="max-height: 400px;"></pre>
                </div>
            </div>
        </div>

        <!-- Initiate Call Example -->
        <div id="code-initiate-call" class="code-example mb-12">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('initiate-call-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="initiate-call-req" class="p-4 text-sm overflow-x-auto"><code>curl -X POST {{ url('/api/v1') }}/calls/initiate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO"
  }'</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                        <button onclick="copyCode('initiate-call-res-success')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-call-res-success" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "Call initiated successfully",
  "data": {
    "call_id": 123,
    "caller_id": "USR_987654321",
    "caller_name": "John Doe",
    "caller_image": "https://example.com/profile.jpg",
    "receiver_id": "USR_1234567890",
    "receiver_name": "Ananya798",
    "receiver_image": "https://example.com/creator.jpg",
    "call_type": "AUDIO",
    "status": "CONNECTING",
    "balance_time": "15:00",
    "agora_token": "007eJxTY...",
    "channel_name": "call_123",
    "created_at": "2024-11-06T10:30:00Z"
  }
}</code></pre>
                </div>
            </div>

            <!-- Self-Call Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">400 Bad Request - Self-Call</span>
                        <button onclick="copyCode('initiate-call-res-self')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-call-res-self" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "You cannot call yourself"
  }
}</code></pre>
                </div>
            </div>

            <!-- Blocked Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">400 Bad Request - Blocked</span>
                        <button onclick="copyCode('initiate-call-res-blocked')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-call-res-blocked" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "error": {
    "code": "USER_UNAVAILABLE",
    "message": "User is not available"
  }
}</code></pre>
                </div>
                <p class="text-xs text-gray-500 mt-2 px-4"><i class="fas fa-info-circle mr-1"></i>Note: For privacy, we don't explicitly say "blocked"</p>
            </div>

            <!-- User Busy Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">400 Bad Request - User Busy</span>
                        <button onclick="copyCode('initiate-call-res-busy')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-call-res-busy" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "error": {
    "code": "USER_BUSY",
    "message": "User is currently on another call"
  }
}</code></pre>
                </div>
            </div>

            <!-- Insufficient Coins Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">400 Bad Request - Insufficient Coins</span>
                        <button onclick="copyCode('initiate-call-res-coins')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-call-res-coins" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_COINS",
    "message": "Insufficient coins for audio call. Minimum 10 coins required.",
    "details": {
      "required": 10,
      "available": 5
    }
  }
}</code></pre>
                </div>
            </div>

            <!-- User Offline Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">400 Bad Request - User Offline</span>
                        <button onclick="copyCode('initiate-call-res-offline')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-call-res-offline" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "error": {
    "code": "USER_OFFLINE",
    "message": "User is not online"
  }
}</code></pre>
                </div>
            </div>

            <!-- Call Not Available Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">400 Bad Request - Call Disabled</span>
                        <button onclick="copyCode('initiate-call-res-disabled')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-call-res-disabled" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "error": {
    "code": "CALL_NOT_AVAILABLE",
    "message": "Audio call not available"
  }
}</code></pre>
                </div>
            </div>

            <!-- User Not Found Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">404 Not Found - User</span>
                        <button onclick="copyCode('initiate-call-res-404')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-call-res-404" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "User not found"
  }
}</code></pre>
                </div>
            </div>

            <!-- Validation Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">422 Validation Error</span>
                        <button onclick="copyCode('initiate-call-res-422')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-call-res-422" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": {
      "receiver_id": ["The receiver id field is required."],
      "call_type": ["The call type must be AUDIO or VIDEO."]
    }
  }
}</code></pre>
                </div>
            </div>
        </div>

        <!-- Get Recent Sessions Example -->
        <div id="code-recent-sessions" class="code-example mb-12">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('recent-sessions-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="recent-sessions-req" class="p-4 text-sm overflow-x-auto"><code>curl -X GET "{{ url('/api/v1') }}/calls/recent-sessions?page=1&per_page=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Accept: application/json"</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                        <button onclick="copyCode('recent-sessions-res-success')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="recent-sessions-res-success" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "sessions": [
    {
      "id": "CALL_123",
      "user": {
        "id": "USR_456",
        "name": "Ananya798",
        "age": 24,
        "profile_image": "https://example.com/profile.jpg",
        "is_online": true,
        "audio_call_enabled": true,
        "video_call_enabled": true
      },
      "call_type": "AUDIO",
      "status": "ENDED",
      "duration": 205,
      "duration_formatted": "3 min",
      "coins_spent": 40,
      "created_at": "2024-11-06T10:30:00Z"
    },
    {
      "id": "CALL_124",
      "user": {
        "id": "USR_789",
        "name": "Priya_23",
        "age": 26,
        "profile_image": "https://example.com/profile2.jpg",
        "is_online": false,
        "audio_call_enabled": true,
        "video_call_enabled": false
      },
      "call_type": "AUDIO",
      "status": "ENDED",
      "duration": 180,
      "duration_formatted": "3 min",
      "coins_spent": 30,
      "created_at": "2024-11-05T15:20:00Z"
    }
  ],
  "pagination": {
    "current_page": 1,
    "per_page": 20,
    "total_pages": 3,
    "total_items": 45,
    "has_more": true
  }
}</code></pre>
                </div>
            </div>

            <!-- Empty Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - No Sessions</span>
                        <button onclick="copyCode('recent-sessions-res-empty')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="recent-sessions-res-empty" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "sessions": [],
  "pagination": {
    "current_page": 1,
    "per_page": 20,
    "total_pages": 0,
    "total_items": 0,
    "has_more": false
  }
}</code></pre>
                </div>
            </div>

            <!-- Unauthorized Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">401 Unauthorized</span>
                        <button onclick="copyCode('recent-sessions-res-401')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="recent-sessions-res-401" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Unauthenticated."
  }
}</code></pre>
                </div>
            </div>
        </div>
    </div>
</main>

<script>
function copyCode(id) {
    const element = document.getElementById(id);
    const text = element.textContent;
    navigator.clipboard.writeText(text).then(() => {
        // Show feedback
        const btn = event.target.closest('button');
        const originalHTML = btn.innerHTML;
        btn.innerHTML = '<i class="fas fa-check mr-1"></i>Copied!';
        btn.classList.add('text-green-400');
        setTimeout(() => {
            btn.innerHTML = originalHTML;
            btn.classList.remove('text-green-400');
        }, 2000);
    });
}

// Test Initiate Call Form Handler
document.getElementById('testInitiateCallForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const token = formData.get('token');
    const data = {
        receiver_id: formData.get('receiver_id'),
        call_type: formData.get('call_type')
    };
    
    try {
        const response = await fetch('{{ url('/api/v1') }}/calls/initiate', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        const responseData = await response.json();
        
        // Display response
        const responseDiv = document.getElementById('testInitiateCallResponse');
        const statusSpan = document.getElementById('responseStatusInitiate');
        const bodyPre = document.getElementById('testInitiateCallResponseBody');
        
        // Set status badge color
        if (response.ok) {
            statusSpan.textContent = `${response.status} OK`;
            statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-green-900 text-green-400';
        } else {
            statusSpan.textContent = `${response.status} Error`;
            statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        }
        
        bodyPre.textContent = JSON.stringify(responseData, null, 2);
        responseDiv.classList.remove('hidden');
        responseDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    } catch (error) {
        const responseDiv = document.getElementById('testInitiateCallResponse');
        const statusSpan = document.getElementById('responseStatusInitiate');
        const bodyPre = document.getElementById('testInitiateCallResponseBody');
        
        statusSpan.textContent = 'Network Error';
        statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        bodyPre.textContent = JSON.stringify({ error: error.message }, null, 2);
        responseDiv.classList.remove('hidden');
    }
});

// Test Recent Sessions Form Handler
document.getElementById('testRecentSessionsForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const token = formData.get('token');
    
    try {
        const response = await fetch('{{ url('/api/v1') }}/calls/recent-sessions', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        
        const responseData = await response.json();
        
        // Display response
        const responseDiv = document.getElementById('testRecentSessionsResponse');
        const statusSpan = document.getElementById('responseStatusRecent');
        const bodyPre = document.getElementById('testRecentSessionsResponseBody');
        
        // Set status badge color
        if (response.ok) {
            statusSpan.textContent = `${response.status} OK`;
            statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-green-900 text-green-400';
        } else {
            statusSpan.textContent = `${response.status} Error`;
            statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        }
        
        bodyPre.textContent = JSON.stringify(responseData, null, 2);
        responseDiv.classList.remove('hidden');
        responseDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    } catch (error) {
        const responseDiv = document.getElementById('testRecentSessionsResponse');
        const statusSpan = document.getElementById('responseStatusRecent');
        const bodyPre = document.getElementById('testRecentSessionsResponseBody');
        
        statusSpan.textContent = 'Network Error';
        statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        bodyPre.textContent = JSON.stringify({ error: error.message }, null, 2);
        responseDiv.classList.remove('hidden');
    }
});

// Test Accept Call Form Handler
document.getElementById('testAcceptCallForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const token = formData.get('token');
    let callId = formData.get('call_id');
    
    // Handle call ID format
    if (!callId.startsWith('CALL_')) {
        callId = 'CALL_' + callId;
    }
    
    const responseDiv = document.getElementById('testAcceptCallResponse');
    const statusSpan = document.getElementById('responseStatusAccept');
    const bodyPre = document.getElementById('testAcceptCallResponseBody');
    
    try {
        const response = await fetch(`{{ url('/api/v1') }}/calls/${callId}/accept`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        
        const data = await response.json();
        
        statusSpan.textContent = `${response.status} ${response.statusText}`;
        statusSpan.className = response.ok ? 
            'text-xs font-mono px-2 py-1 rounded bg-green-900 text-green-400' : 
            'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        
        bodyPre.textContent = JSON.stringify(data, null, 2);
        responseDiv.classList.remove('hidden');
    } catch (error) {
        statusSpan.textContent = 'Network Error';
        statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        bodyPre.textContent = JSON.stringify({ error: error.message }, null, 2);
        responseDiv.classList.remove('hidden');
    }
});

// Test Reject Call Form Handler
document.getElementById('testRejectCallForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const token = formData.get('token');
    let callId = formData.get('call_id');
    
    // Handle call ID format
    if (!callId.startsWith('CALL_')) {
        callId = 'CALL_' + callId;
    }
    
    const responseDiv = document.getElementById('testRejectCallResponse');
    const statusSpan = document.getElementById('responseStatusReject');
    const bodyPre = document.getElementById('testRejectCallResponseBody');
    
    try {
        const response = await fetch(`{{ url('/api/v1') }}/calls/${callId}/reject`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        
        const data = await response.json();
        
        statusSpan.textContent = `${response.status} ${response.statusText}`;
        statusSpan.className = response.ok ? 
            'text-xs font-mono px-2 py-1 rounded bg-green-900 text-green-400' : 
            'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        
        bodyPre.textContent = JSON.stringify(data, null, 2);
        responseDiv.classList.remove('hidden');
    } catch (error) {
        statusSpan.textContent = 'Network Error';
        statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        bodyPre.textContent = JSON.stringify({ error: error.message }, null, 2);
        responseDiv.classList.remove('hidden');
    }
});

// Test End Call Form Handler
document.getElementById('testEndCallForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const token = formData.get('token');
    let callId = formData.get('call_id');
    const duration = formData.get('duration');
    
    // Handle call ID format
    if (!callId.startsWith('CALL_')) {
        callId = 'CALL_' + callId;
    }
    
    const responseDiv = document.getElementById('testEndCallResponse');
    const statusSpan = document.getElementById('responseStatusEnd');
    const bodyPre = document.getElementById('testEndCallResponseBody');
    
    try {
        const response = await fetch(`{{ url('/api/v1') }}/calls/${callId}/end`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ duration: parseInt(duration) })
        });
        
        const data = await response.json();
        
        statusSpan.textContent = `${response.status} ${response.statusText}`;
        statusSpan.className = response.ok ? 
            'text-xs font-mono px-2 py-1 rounded bg-green-900 text-green-400' : 
            'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        
        bodyPre.textContent = JSON.stringify(data, null, 2);
        responseDiv.classList.remove('hidden');
    } catch (error) {
        statusSpan.textContent = 'Network Error';
        statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        bodyPre.textContent = JSON.stringify({ error: error.message }, null, 2);
        responseDiv.classList.remove('hidden');
    }
});

// Test Rate Call Form Handler
document.getElementById('testRateCallForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const token = formData.get('token');
    let callId = formData.get('call_id');
    const rating = formData.get('rating');
    const feedback = formData.get('feedback');
    
    // Handle call ID format
    if (!callId.startsWith('CALL_')) {
        callId = 'CALL_' + callId;
    }
    
    const responseDiv = document.getElementById('testRateCallResponse');
    const statusSpan = document.getElementById('responseStatusRate');
    const bodyPre = document.getElementById('testRateCallResponseBody');
    
    const requestBody = { rating: parseInt(rating) };
    if (feedback) requestBody.feedback = feedback;
    
    try {
        const response = await fetch(`{{ url('/api/v1') }}/calls/${callId}/rate`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });
        
        const data = await response.json();
        
        statusSpan.textContent = `${response.status} ${response.statusText}`;
        statusSpan.className = response.ok ? 
            'text-xs font-mono px-2 py-1 rounded bg-green-900 text-green-400' : 
            'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        
        bodyPre.textContent = JSON.stringify(data, null, 2);
        responseDiv.classList.remove('hidden');
    } catch (error) {
        statusSpan.textContent = 'Network Error';
        statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        bodyPre.textContent = JSON.stringify({ error: error.message }, null, 2);
        responseDiv.classList.remove('hidden');
    }
});

// Test Call History Form Handler
document.getElementById('testCallHistoryForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const token = formData.get('token');
    const limit = formData.get('limit');
    const page = formData.get('page');
    
    const responseDiv = document.getElementById('testCallHistoryResponse');
    const statusSpan = document.getElementById('responseStatusHistory');
    const bodyPre = document.getElementById('testCallHistoryResponseBody');
    
    // Build query parameters
    const params = new URLSearchParams();
    if (limit) params.append('limit', limit);
    if (page) params.append('page', page);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    
    try {
        const response = await fetch(`{{ url('/api/v1') }}/calls/history${queryString}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json'
            }
        });
        
        const data = await response.json();
        
        statusSpan.textContent = `${response.status} ${response.statusText}`;
        statusSpan.className = response.ok ? 
            'text-xs font-mono px-2 py-1 rounded bg-green-900 text-green-400' : 
            'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        
        bodyPre.textContent = JSON.stringify(data, null, 2);
        responseDiv.classList.remove('hidden');
    } catch (error) {
        statusSpan.textContent = 'Network Error';
        statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        bodyPre.textContent = JSON.stringify({ error: error.message }, null, 2);
        responseDiv.classList.remove('hidden');
    }
});

// Sync scroll between documentation and code examples
const docPanel = document.querySelector('.documentation-panel');
const codePanel = document.querySelector('.code-panel');
let isDocScrolling = false;
let isCodeScrolling = false;

if (docPanel && codePanel) {
    docPanel.addEventListener('scroll', () => {
        if (!isCodeScrolling) {
            isDocScrolling = true;
            const scrollPercentage = docPanel.scrollTop / (docPanel.scrollHeight - docPanel.clientHeight);
            codePanel.scrollTop = scrollPercentage * (codePanel.scrollHeight - codePanel.clientHeight);
            setTimeout(() => { isDocScrolling = false; }, 100);
        }
    });

    codePanel.addEventListener('scroll', () => {
        if (!isDocScrolling) {
            isCodeScrolling = true;
            const scrollPercentage = codePanel.scrollTop / (codePanel.scrollHeight - codePanel.clientHeight);
            docPanel.scrollTop = scrollPercentage * (docPanel.scrollHeight - docPanel.clientHeight);
            setTimeout(() => { isCodeScrolling = false; }, 100);
        }
    });
}
</script>

<style>
.method-post { background: #10B981; }
.method-get { background: #3B82F6; }
</style>
@endsection
