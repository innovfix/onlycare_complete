@extends('api-docs.layout')

@section('title', 'Wallet & Payments APIs - Only Care')

@section('content')
<main class="flex-1 flex min-w-0">
    <!-- Documentation (Left Side) -->
    <div class="documentation-panel w-1/2 p-8 overflow-y-auto flex-shrink min-w-0" style="height: calc(100vh - 64px);">
        <!-- Page Header -->
        <div class="mb-8">
            <h1 class="text-3xl font-bold text-white mb-2">Wallet & Payments APIs</h1>
            <p class="text-gray-400">Manage coin packages, wallet balance, purchases, and transaction history.</p>
        </div>

        <!-- Get Coin Packages -->
        <section id="get-packages" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/wallet/packages</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Get Coin Packages</h2>
                <p class="text-gray-400">Retrieves all active coin packages available for purchase with pricing and discounts.</p>
            </div>

            <div class="mb-4 bg-yellow-900 bg-opacity-30 border border-yellow-700 rounded-lg px-4 py-3">
                <div class="flex items-start">
                    <i class="fas fa-lock text-yellow-400 mr-3 mt-1"></i>
                    <div>
                        <span class="text-sm font-semibold text-yellow-400">Requires Authentication: Bearer Token</span>
                    </div>
                </div>
            </div>

            <div>
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">Packages retrieved successfully</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized - Invalid or missing token</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- Get Wallet Balance -->
        <section id="get-balance" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/wallet/balance</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Get Wallet Balance</h2>
                <p class="text-gray-400">Get user's current coin balance, earnings, and spending statistics.</p>
            </div>

            <div class="mb-4 bg-yellow-900 bg-opacity-30 border border-yellow-700 rounded-lg px-4 py-3">
                <div class="flex items-start">
                    <i class="fas fa-lock text-yellow-400 mr-3 mt-1"></i>
                    <div>
                        <span class="text-sm font-semibold text-yellow-400">Requires Authentication: Bearer Token</span>
                    </div>
                </div>
            </div>

            <div>
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">Balance retrieved successfully</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized - Invalid or missing token</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- Initiate Purchase -->
        <section id="initiate-purchase" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/wallet/purchase</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Initiate Purchase</h2>
                <p class="text-gray-400">Start a coin package purchase transaction and get payment gateway details.</p>
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
                                <td class="px-4 py-3 font-mono text-purple-400">package_id</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Package ID (e.g., PKG_1)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">payment_method</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">PhonePe, GooglePay, Paytm, UPI, Card</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div>
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">Purchase initiated successfully</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">404</span>
                        <span class="text-gray-400">Package not found</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">422</span>
                        <span class="text-gray-400">Validation error</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- Verify Purchase -->
        <section id="verify-purchase" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/wallet/verify-purchase</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Verify Purchase</h2>
                <p class="text-gray-400">Verify and complete purchase transaction. Coins are added to wallet on success.</p>
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
                                <td class="px-4 py-3 font-mono text-purple-400">transaction_id</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Transaction ID from initiate</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">payment_gateway_id</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Payment gateway transaction ID</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">status</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">SUCCESS or FAILED</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div>
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">Purchase verified, coins added</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">404</span>
                        <span class="text-gray-400">Transaction not found</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- Get Transactions -->
        <section id="get-transactions" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/wallet/transactions</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Get Transaction History</h2>
                <p class="text-gray-400">Retrieve paginated transaction history for the authenticated user.</p>
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
                                <td class="px-4 py-3 font-mono text-purple-400">limit</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3 text-gray-400">No</td>
                                <td class="px-4 py-3 text-gray-400">Items per page (default: 20, max: 50)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">page</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3 text-gray-400">No</td>
                                <td class="px-4 py-3 text-gray-400">Page number (default: 1)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">type</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3 text-gray-400">No</td>
                                <td class="px-4 py-3 text-gray-400">Filter: PURCHASE, CALL_SPENT, WITHDRAWAL</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div>
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">Transactions retrieved successfully</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized - Invalid or missing token</span>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- Code Examples (Right Side) -->
    <div class="code-panel w-1/2 bg-black p-8 overflow-y-auto border-l border-gray-800 flex-shrink min-w-0" style="height: calc(100vh - 64px);">
        <!-- Interactive Testing Form - Get Coin Packages -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-yellow-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-yellow-400 mr-2"></i>
                Test Get Coin Packages
            </h3>
            <form id="testGetPackagesForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">
                        <i class="fas fa-key text-yellow-400 mr-1"></i> Bearer Token
                    </label>
                    <input type="text" name="token" placeholder="Enter your access token" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <button type="submit" class="w-full bg-yellow-600 hover:bg-yellow-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-play mr-2"></i>Run Test
                </button>
            </form>
            <div id="testGetPackagesResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusPackages" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testGetPackagesResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300" style="max-height: 400px;"></pre>
                </div>
            </div>
        </div>

        <!-- Interactive Testing Form - Get Wallet Balance -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-green-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-green-400 mr-2"></i>
                Test Get Wallet Balance
            </h3>
            <form id="testGetBalanceForm" class="space-y-4">
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
            <div id="testGetBalanceResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusBalance" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testGetBalanceResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300" style="max-height: 400px;"></pre>
                </div>
            </div>
        </div>

        <!-- Get Packages Example -->
        <div id="code-get-packages" class="code-example mb-12">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('get-packages-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="get-packages-req" class="p-4 text-sm overflow-x-auto"><code>curl -X GET {{ url('/api/v1') }}/wallet/packages \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                        <button onclick="copyCode('get-packages-res-success')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="get-packages-res-success" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "data": {
    "packages": [
      {
        "id": 1,
        "name": "Starter Pack",
        "coins": 100,
        "price": 99.00,
        "discount_percentage": 0,
        "final_price": 99.00,
        "popular": false,
        "bonus_coins": 0,
        "total_coins": 100,
        "currency": "INR",
        "description": "Perfect to get started",
        "status": "active"
      },
      {
        "id": 2,
        "name": "Popular Pack",
        "coins": 500,
        "price": 499.00,
        "discount_percentage": 10,
        "final_price": 449.10,
        "popular": true,
        "bonus_coins": 50,
        "total_coins": 550,
        "currency": "INR",
        "description": "Most popular choice",
        "status": "active"
      }
    ]
  }
}</code></pre>
                </div>
            </div>

            <!-- Error Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">401 Unauthorized</span>
                        <button onclick="copyCode('get-packages-res-401')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="get-packages-res-401" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Unauthorized. Please login first."
}</code></pre>
                </div>
            </div>
        </div>

        <!-- Get Balance Example -->
        <div id="code-get-balance" class="code-example mb-12">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('get-balance-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="get-balance-req" class="p-4 text-sm overflow-x-auto"><code>curl -X GET {{ url('/api/v1') }}/wallet/balance \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                        <button onclick="copyCode('get-balance-res-success')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="get-balance-res-success" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "data": {
    "balance": {
      "user_id": 123,
      "current_balance": 1250,
      "total_earned": 5000,
      "total_spent": 3750,
      "total_purchased": 10000,
      "currency": "coins",
      "last_updated": "2025-11-04T14:30:22Z"
    },
    "statistics": {
      "total_purchases": 5,
      "total_amount_spent": "₹2,495.00",
      "lifetime_earnings": 5000,
      "lifetime_spending": 3750
    }
  }
}</code></pre>
                </div>
            </div>

            <!-- Error Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">401 Unauthorized</span>
                        <button onclick="copyCode('get-balance-res-401')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="get-balance-res-401" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Unauthorized. Please login first."
}</code></pre>
                </div>
            </div>
        </div>

        <!-- Initiate Purchase Example -->
        <div id="code-initiate-purchase" class="code-example mb-12">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('initiate-purchase-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="initiate-purchase-req" class="p-4 text-sm overflow-x-auto"><code>curl -X POST {{ url('/api/v1') }}/wallet/purchase \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "package_id": "PKG_2",
    "payment_method": "PhonePe"
  }'</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                        <button onclick="copyCode('initiate-purchase-res-success')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-purchase-res-success" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "Purchase initiated successfully",
  "data": {
    "transaction": {
      "transaction_id": "TXN_1699012345678",
      "order_id": "ORDER_1699012345",
      "package_id": "PKG_2",
      "package_name": "Popular Pack",
      "coins": 500,
      "bonus_coins": 50,
      "total_coins": 550,
      "amount": 449.10,
      "currency": "INR",
      "payment_method": "PhonePe",
      "status": "pending",
      "created_at": "2025-11-04T14:35:45Z"
    },
    "payment_gateway": {
      "gateway": "PhonePe",
      "redirect_url": "https://phonepe.com/pay?orderId=ORDER_1699012345",
      "merchant_id": "MERCHANT123",
      "merchant_transaction_id": "TXN_1699012345678"
    }
  }
}</code></pre>
                </div>
            </div>

            <!-- Error Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">404 Not Found</span>
                        <button onclick="copyCode('initiate-purchase-res-404')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-purchase-res-404" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Package not found or inactive"
}</code></pre>
                </div>
            </div>

            <!-- Validation Error Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">422 Validation Error</span>
                        <button onclick="copyCode('initiate-purchase-res-422')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="initiate-purchase-res-422" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "package_id": ["The package_id field is required."],
    "payment_method": ["The selected payment_method is invalid."]
  }
}</code></pre>
                </div>
            </div>
        </div>

        <!-- Verify Purchase Example -->
        <div id="code-verify-purchase" class="code-example mb-12">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('verify-purchase-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="verify-purchase-req" class="p-4 text-sm overflow-x-auto"><code>curl -X POST {{ url('/api/v1') }}/wallet/verify-purchase \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "transaction_id": "TXN_1699012345678",
    "payment_gateway_id": "PHONEPE_987654321",
    "status": "SUCCESS"
  }'</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                        <button onclick="copyCode('verify-purchase-res-success')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="verify-purchase-res-success" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "Purchase verified successfully. Coins added to your wallet.",
  "data": {
    "transaction": {
      "transaction_id": "TXN_1699012345678",
      "package_name": "Popular Pack",
      "coins": 500,
      "bonus_coins": 50,
      "total_coins": 550,
      "amount": 449.10,
      "status": "completed",
      "verified_at": "2025-11-04T14:36:10Z"
    },
    "wallet": {
      "previous_balance": 1250,
      "coins_added": 550,
      "new_balance": 1800
    }
  }
}</code></pre>
                </div>
            </div>

            <!-- Error Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">404 Not Found</span>
                        <button onclick="copyCode('verify-purchase-res-404')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="verify-purchase-res-404" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Transaction not found or already processed"
}</code></pre>
                </div>
            </div>

            <!-- Failed Payment Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">200 OK - Payment Failed</span>
                        <button onclick="copyCode('verify-purchase-res-failed')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="verify-purchase-res-failed" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Payment failed. Transaction marked as failed.",
  "data": {
    "transaction": {
      "transaction_id": "TXN_1699012345678",
      "status": "failed",
      "payment_gateway_id": "PHONEPE_987654321",
      "failed_at": "2025-11-04T14:36:10Z"
    }
  }
}</code></pre>
                </div>
            </div>
        </div>

        <!-- Get Transactions Example -->
        <div id="code-get-transactions" class="code-example mb-12">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('get-transactions-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="get-transactions-req" class="p-4 text-sm overflow-x-auto"><code>curl -X GET "{{ url('/api/v1') }}/wallet/transactions?page=1&limit=20&type=PURCHASE" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                        <button onclick="copyCode('get-transactions-res-success')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="get-transactions-res-success" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "data": {
    "transactions": [
      {
        "id": 501,
        "transaction_id": "TXN_1699012345678",
        "type": "PURCHASE",
        "amount": 449.10,
        "coins": 550,
        "description": "Purchased Popular Pack (500 + 50 bonus coins)",
        "status": "completed",
        "payment_method": "PhonePe",
        "created_at": "2025-11-04T14:35:45Z"
      },
      {
        "id": 502,
        "transaction_id": "TXN_1699012346789",
        "type": "CALL_SPENT",
        "amount": 0,
        "coins": -50,
        "description": "Video call with Priya (10 minutes)",
        "status": "completed",
        "created_at": "2025-11-04T15:20:30Z"
      },
      {
        "id": 503,
        "transaction_id": "TXN_1699012347890",
        "type": "REFERRAL_BONUS",
        "amount": 0,
        "coins": 100,
        "description": "Referral bonus from user signup",
        "status": "completed",
        "created_at": "2025-11-04T17:05:20Z"
      }
    ],
    "pagination": {
      "current_page": 1,
      "per_page": 20,
      "total": 45,
      "last_page": 3
    }
  }
}</code></pre>
                </div>
            </div>

            <!-- Error Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">401 Unauthorized</span>
                        <button onclick="copyCode('get-transactions-res-401')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="get-transactions-res-401" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Unauthorized. Please login first."
}</code></pre>
                </div>
            </div>
        </div>
    </div>
</main>

<script>
function copyCode(elementId) {
    const element = document.getElementById(elementId);
    const text = element.textContent;
    navigator.clipboard.writeText(text).then(() => {
        // Show a brief "Copied!" message
        const button = event.target.closest('button');
        const originalHTML = button.innerHTML;
        button.innerHTML = '<i class="fas fa-check mr-1"></i>Copied!';
        button.classList.add('text-green-400');
        setTimeout(() => {
            button.innerHTML = originalHTML;
            button.classList.remove('text-green-400');
        }, 2000);
    });
}

// Helper function to display API responses
function displayApiResponse(responseDiv, statusSpan, bodyPre, response, data) {
    if (response.ok || response.status) {
        const status = response.status || response;
        statusSpan.textContent = status >= 200 && status < 300 ? `${status} OK` : `${status} Error`;
        statusSpan.className = status >= 200 && status < 300 
            ? 'text-xs font-mono px-2 py-1 rounded bg-green-900 text-green-400'
            : 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
    }
    bodyPre.textContent = JSON.stringify(data, null, 2);
    responseDiv.classList.remove('hidden');
    responseDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// Test Get Coin Packages Form
document.getElementById('testGetPackagesForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const token = formData.get('token');
    
    try {
        const response = await fetch('{{ url('/api/v1') }}/wallet/packages', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        const responseData = await response.json();
        displayApiResponse(
            document.getElementById('testGetPackagesResponse'),
            document.getElementById('responseStatusPackages'),
            document.getElementById('testGetPackagesResponseBody'),
            response, responseData
        );
    } catch (error) {
        displayApiResponse(
            document.getElementById('testGetPackagesResponse'),
            document.getElementById('responseStatusPackages'),
            document.getElementById('testGetPackagesResponseBody'),
            'error', { error: error.message }
        );
    }
});

// Test Get Wallet Balance Form
document.getElementById('testGetBalanceForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const token = formData.get('token');
    
    try {
        const response = await fetch('{{ url('/api/v1') }}/wallet/balance', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        const responseData = await response.json();
        displayApiResponse(
            document.getElementById('testGetBalanceResponse'),
            document.getElementById('responseStatusBalance'),
            document.getElementById('testGetBalanceResponseBody'),
            response, responseData
        );
    } catch (error) {
        displayApiResponse(
            document.getElementById('testGetBalanceResponse'),
            document.getElementById('responseStatusBalance'),
            document.getElementById('testGetBalanceResponseBody'),
            'error', { error: error.message }
        );
    }
});
</script>
@endsection
