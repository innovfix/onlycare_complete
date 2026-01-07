@extends('api-docs.layout')

@section('title', 'Authentication APIs - Only Care')

@section('content')
<main class="flex-1 flex min-w-0">
    <!-- Documentation (Left Side) -->
    <div class="documentation-panel w-1/2 p-8 overflow-y-auto flex-shrink min-w-0" style="height: calc(100vh - 64px);">
        <!-- Page Header -->
        <div class="mb-8">
            <h1 class="text-3xl font-bold text-white mb-2">Authentication APIs</h1>
            <p class="text-gray-400">OTP-based authentication flow for secure user login and registration.</p>
        </div>

        <!-- Send OTP -->
        <section id="send-otp" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/auth/send-otp</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Send OTP</h2>
                <p class="text-gray-400">Sends a 6-digit OTP to the user's phone number for authentication.</p>
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
                                <td class="px-4 py-3 font-mono text-purple-400">phone</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">
                                    Phone number (digits only, no letters)<br>
                                    <span class="text-xs text-blue-400">• India (+91): exactly 10 digits</span><br>
                                    <span class="text-xs text-blue-400">• USA/Canada (+1): exactly 10 digits</span><br>
                                    <span class="text-xs text-blue-400">• UAE (+971): exactly 9 digits</span><br>
                                    <span class="text-xs text-blue-400">• Others: 10-15 digits</span>
                                </td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">country_code</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Country code with + and 2-3 digits (e.g., +91, +44, +971)</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Response Codes -->
            <div>
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE CODES</h4>
                <div class="space-y-2">
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-green-400 w-12">200</span>
                        <span class="text-gray-400">OTP sent successfully</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">422</span>
                        <span class="text-gray-400">Validation error (invalid phone)</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-red-400 w-12">500</span>
                        <span class="text-gray-400">Internal server error</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- Verify OTP -->
        <section id="verify-otp" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/auth/verify-otp</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Verify OTP</h2>
                <p class="text-gray-400">Verifies the OTP and returns an access token for authentication.</p>
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
                                <td class="px-4 py-3 font-mono text-purple-400">phone</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Phone number</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">otp</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">6-digit OTP</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">otp_id</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">OTP ID from send-otp</td>
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
                        <span class="text-gray-400">OTP verified successfully</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">400</span>
                        <span class="text-gray-400">Invalid or expired OTP</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">422</span>
                        <span class="text-gray-400">Validation error</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- Register -->
        <section id="register" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-post text-white px-3 py-1.5 text-xs font-bold rounded">POST</span>
                    <code class="text-lg font-mono text-blue-400">/auth/register</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Complete Registration</h2>
                <p class="text-gray-400">Complete user profile registration with personal details.</p>
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
                                <td class="px-4 py-3 font-mono text-purple-400">phone</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Verified phone number</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">gender</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">MALE or FEMALE</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">avatar</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Profile image URL</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">language</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-red-400 font-semibold">✓</span></td>
                                <td class="px-4 py-3 text-gray-400">Preferred language</td>
                            </tr>
                            <tr class="border-t border-gray-800 bg-purple-900 bg-opacity-10">
                                <td class="px-4 py-3 font-mono text-purple-400">age</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3"><span class="text-yellow-400 font-semibold">✓*</span></td>
                                <td class="px-4 py-3 text-gray-400">Age (18-100) - <span class="text-yellow-400">Required for FEMALE only</span></td>
                            </tr>
                            <tr class="border-t border-gray-800 bg-purple-900 bg-opacity-10">
                                <td class="px-4 py-3 font-mono text-purple-400">interests</td>
                                <td class="px-4 py-3 text-gray-400">array</td>
                                <td class="px-4 py-3"><span class="text-yellow-400 font-semibold">✓*</span></td>
                                <td class="px-4 py-3 text-gray-400">Array of 1-4 interest strings - <span class="text-yellow-400">Required for FEMALE only</span></td>
                            </tr>
                            <tr class="border-t border-gray-800 bg-purple-900 bg-opacity-10">
                                <td class="px-4 py-3 font-mono text-purple-400">description</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-yellow-400 font-semibold">✓*</span></td>
                                <td class="px-4 py-3 text-gray-400">Bio/description (10-500 chars) - <span class="text-yellow-400">Required for FEMALE only</span></td>
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
                        <span class="text-gray-400">Registration successful</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">404</span>
                        <span class="text-gray-400">User not found (verify OTP first)</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">422</span>
                        <span class="text-gray-400">Validation error</span>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- Code Examples (Right Side) -->
    <div class="code-panel w-1/2 bg-black p-8 overflow-y-auto border-l border-gray-800 flex-shrink min-w-0" style="height: calc(100vh - 64px);">
        <!-- Interactive Testing Form - Send OTP -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-purple-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-purple-400 mr-2"></i>
                Test Send OTP
            </h3>
            <form id="testSendOtpForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Phone Number</label>
                    <input type="text" name="phone" placeholder="9876543210" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                    <p class="text-xs text-gray-500 mt-1">For India (+91): exactly 10 digits</p>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Country Code</label>
                    <input type="text" name="country_code" placeholder="+91" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                    <p class="text-xs text-gray-500 mt-1">e.g., +91, +44, +971</p>
                </div>
                <button type="submit" class="w-full bg-purple-600 hover:bg-purple-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-play mr-2"></i>Run Test
                </button>
            </form>
            <div id="testSendOtpResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusSendOtp" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testSendOtpResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300" style="max-height: 400px;"></pre>
                </div>
            </div>
        </div>

        <!-- Interactive Testing Form - Verify OTP -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-green-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-green-400 mr-2"></i>
                Test Verify OTP
            </h3>
            <form id="testVerifyOtpForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Phone Number</label>
                    <input type="text" name="phone" placeholder="9876543210" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">OTP Code</label>
                    <input type="text" name="otp" placeholder="123456" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                    <p class="text-xs text-gray-500 mt-1">6-digit OTP</p>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">OTP ID</label>
                    <input type="text" name="otp_id" placeholder="From send-otp response" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <button type="submit" class="w-full bg-green-600 hover:bg-green-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-play mr-2"></i>Run Test
                </button>
            </form>
            <div id="testVerifyOtpResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusVerifyOtp" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testVerifyOtpResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300" style="max-height: 400px;"></pre>
                </div>
            </div>
        </div>

        <!-- Interactive Testing Form - Register -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-blue-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-blue-400 mr-2"></i>
                Test Register
            </h3>
            <form id="testRegisterForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Phone</label>
                    <input type="text" name="phone" placeholder="9876543210" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                    <p class="text-xs text-gray-500 mt-1">Phone number verified with OTP</p>
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-sm font-medium text-gray-300 mb-2">Gender</label>
                        <select name="gender" id="registerGender" class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                            <option value="">Select Gender</option>
                            <option value="MALE">Male (User)</option>
                            <option value="FEMALE">Female (Creator)</option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-300 mb-2">Language</label>
                        <input type="text" name="language" placeholder="Hindi, English, Tamil..." 
                            class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                    </div>
                </div>
                
                <!-- Female/Creator Only Fields -->
                <div id="creatorFields" style="display: none;">
                    <div class="border-t border-gray-700 pt-4 mt-2">
                        <p class="text-xs text-yellow-400 mb-3"><i class="fas fa-info-circle mr-1"></i> Required for Female/Creator accounts only</p>
                        <div class="space-y-3">
                            <div>
                                <label class="block text-sm font-medium text-gray-300 mb-2">Age</label>
                                <input type="number" name="age" placeholder="25" min="18" max="100"
                                    class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none">
                                <p class="text-xs text-gray-500 mt-1">Must be 18 or above</p>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-300 mb-2">Interests (1-4 items)</label>
                                <input type="text" name="interests" placeholder="Travel, Music, Movies, Cooking" 
                                    class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none">
                                <p class="text-xs text-gray-500 mt-1">Comma-separated, 1-4 interests</p>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-300 mb-2">Description/Bio</label>
                                <textarea name="description" placeholder="Tell users about yourself..." rows="3"
                                    class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none"></textarea>
                                <p class="text-xs text-gray-500 mt-1">10-500 characters</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Avatar URL</label>
                    <input type="text" name="avatar" placeholder="https://example.com/avatar.jpg" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                    <p class="text-xs text-gray-500 mt-1">Profile image URL</p>
                </div>
                
                <button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-play mr-2"></i>Run Test
                </button>
            </form>
            <div id="testRegisterResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatusRegister" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testRegisterResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300" style="max-height: 400px;"></pre>
                </div>
            </div>
        </div>

        <!-- Send OTP Example -->
        <div id="code-send-otp" class="code-example mb-12">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('send-otp-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="send-otp-req" class="p-4 text-sm overflow-x-auto"><code>curl -X POST {{ url('/api/v1') }}/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "9876543210",
    "country_code": "+91"
  }'</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                        <button onclick="copyCode('send-otp-res-success')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="send-otp-res-success" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "OTP sent successfully",
  "otp_id": "OTP_1234567890",
  "expires_in": 600,
  "otp": "123456"
}</code></pre>
                </div>
            </div>

            <!-- Validation Error Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">422 Validation Error</span>
                        <button onclick="copyCode('send-otp-res-422')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="send-otp-res-422" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Validation error",
  "errors": {
    "phone": ["Phone number must contain only digits."],
    "country_code": ["Country code must start with + followed by 2-3 digits."]
  }
}</code></pre>
                </div>
            </div>
        </div>

        <!-- Verify OTP Example -->
        <div id="code-verify-otp" class="code-example mb-12">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('verify-otp-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="verify-otp-req" class="p-4 text-sm overflow-x-auto"><code>curl -X POST {{ url('/api/v1') }}/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "9876543210",
    "otp": "123456",
    "otp_id": "OTP_1234567890"
  }'</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response - New User -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - New User</span>
                        <button onclick="copyCode('verify-otp-res-success-new')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="verify-otp-res-success-new" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "OTP verified successfully",
  "user_exists": false,
  "access_token": "1|abcd1234...",
  "user": null
}</code></pre>
                </div>
            </div>

            <!-- Success Response - Existing User -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Existing User</span>
                        <button onclick="copyCode('verify-otp-res-success-existing')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="verify-otp-res-success-existing" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "OTP verified successfully",
  "user_exists": true,
  "access_token": "2|xyz5678...",
  "user": {
    "id": "USR_1",
    "phone": "9876543210",
    "name": "John Doe",
    "age": 25,
    "gender": "MALE"
  }
}</code></pre>
                </div>
            </div>

            <!-- Invalid OTP Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">400 Bad Request - Invalid OTP</span>
                        <button onclick="copyCode('verify-otp-res-400')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="verify-otp-res-400" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Invalid or expired OTP",
  "error": "The OTP you entered is incorrect or has expired"
}</code></pre>
                </div>
            </div>
        </div>

        <!-- Register Example -->
        <div id="code-register" class="code-example mb-12">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('register-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="register-req" class="p-4 text-sm overflow-x-auto"><code>curl -X POST {{ url('/api/v1') }}/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "9876543210",
    "name": "John Doe",
    "age": 25,
    "gender": "MALE",
    "language": "HINDI",
    "bio": "Love music and travel"
  }'</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                        <button onclick="copyCode('register-res-success')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="register-res-success" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "message": "Registration successful",
  "access_token": "1|abcd1234efgh5678...",
  "user": {
    "id": "USR_1",
    "phone": "9876543210",
    "name": "User_3210",
    "age": null,
    "gender": "MALE",
    "coin_balance": 0,
    "total_earnings": 0,
    "language": "HINDI"
  }
}</code></pre>
                </div>
            </div>

            <!-- User Not Found Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">404 Not Found - User Not Found</span>
                        <button onclick="copyCode('register-res-404')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="register-res-404" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "User not found. Please verify OTP first.",
  "error": "Please complete OTP verification before registration"
}</code></pre>
                </div>
            </div>

            <!-- Validation Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">422 Validation Error</span>
                        <button onclick="copyCode('register-res-422')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="register-res-422" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "message": "Validation error",
  "errors": {
    "phone": ["The phone field is required."],
    "name": ["The name must be between 2 and 100 characters."],
    "age": ["The age must be between 18 and 100."],
    "gender": ["The gender must be either MALE or FEMALE."],
    "language": ["The language field is required."]
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
        // Show success feedback
        const button = event.currentTarget;
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

// Test Send OTP Form
document.getElementById('testSendOtpForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = {
        phone: formData.get('phone'),
        country_code: formData.get('country_code')
    };
    
    try {
        const response = await fetch('{{ url('/api/v1') }}/auth/send-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(data)
        });
        const responseData = await response.json();
        displayApiResponse(
            document.getElementById('testSendOtpResponse'),
            document.getElementById('responseStatusSendOtp'),
            document.getElementById('testSendOtpResponseBody'),
            response, responseData
        );
    } catch (error) {
        displayApiResponse(
            document.getElementById('testSendOtpResponse'),
            document.getElementById('responseStatusSendOtp'),
            document.getElementById('testSendOtpResponseBody'),
            'error', { error: error.message }
        );
    }
});

// Test Verify OTP Form
document.getElementById('testVerifyOtpForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const data = {
        phone: formData.get('phone'),
        otp: formData.get('otp'),
        otp_id: formData.get('otp_id')
    };
    
    try {
        const response = await fetch('{{ url('/api/v1') }}/auth/verify-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(data)
        });
        const responseData = await response.json();
        displayApiResponse(
            document.getElementById('testVerifyOtpResponse'),
            document.getElementById('responseStatusVerifyOtp'),
            document.getElementById('testVerifyOtpResponseBody'),
            response, responseData
        );
    } catch (error) {
        displayApiResponse(
            document.getElementById('testVerifyOtpResponse'),
            document.getElementById('responseStatusVerifyOtp'),
            document.getElementById('testVerifyOtpResponseBody'),
            'error', { error: error.message }
        );
    }
});

// Toggle creator fields based on gender selection
document.getElementById('registerGender').addEventListener('change', function() {
    const creatorFields = document.getElementById('creatorFields');
    const ageInput = document.querySelector('input[name="age"]');
    const interestsInput = document.querySelector('input[name="interests"]');
    const descriptionInput = document.querySelector('textarea[name="description"]');
    
    if (this.value === 'FEMALE') {
        creatorFields.style.display = 'block';
        ageInput.required = true;
        interestsInput.required = true;
        descriptionInput.required = true;
    } else {
        creatorFields.style.display = 'none';
        ageInput.required = false;
        interestsInput.required = false;
        descriptionInput.required = false;
    }
});

// Test Register Form
document.getElementById('testRegisterForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    
    // Build data object
    const data = {
        phone: formData.get('phone'),
        gender: formData.get('gender'),
        language: formData.get('language'),
        avatar: formData.get('avatar')
    };
    
    // Add creator-specific fields if FEMALE
    if (formData.get('gender') === 'FEMALE') {
        data.age = parseInt(formData.get('age'));
        
        // Parse interests as array
        const interestsStr = formData.get('interests');
        if (interestsStr) {
            data.interests = interestsStr.split(',').map(i => i.trim()).filter(i => i);
        }
        
        data.description = formData.get('description');
    }
    
    try {
        const response = await fetch('{{ url('/api/v1') }}/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(data)
        });
        const responseData = await response.json();
        displayApiResponse(
            document.getElementById('testRegisterResponse'),
            document.getElementById('responseStatusRegister'),
            document.getElementById('testRegisterResponseBody'),
            response, responseData
        );
    } catch (error) {
        displayApiResponse(
            document.getElementById('testRegisterResponse'),
            document.getElementById('responseStatusRegister'),
            document.getElementById('testRegisterResponseBody'),
            'error', { error: error.message }
        );
    }
});
</script>
@endsection





