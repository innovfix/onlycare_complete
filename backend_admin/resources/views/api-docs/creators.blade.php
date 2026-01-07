@extends('api-docs.layout')

@section('title', 'Users & Creators APIs - Only Care')

@section('content')
<main class="flex-1 flex min-w-0">
    <!-- Documentation (Left Side) -->
    <div class="documentation-panel w-1/2 p-8 overflow-y-auto flex-shrink min-w-0" style="height: calc(100vh - 64px);">
        <!-- Page Header -->
        <div class="mb-8">
            <h1 class="text-3xl font-bold text-white mb-2">Users / Creators APIs</h1>
            <p class="text-gray-400">Get list of available creators for the home screen with filtering and pagination.</p>
        </div>

        <!-- Get Creators (Home Screen) -->
        <section id="get-creators" class="mb-12">
            <div class="mb-6">
                <div class="flex items-center space-x-3 mb-4">
                    <span class="method-get text-white px-3 py-1.5 text-xs font-bold rounded">GET</span>
                    <code class="text-lg font-mono text-blue-400">/users/females</code>
                </div>
                <h2 class="text-2xl font-bold text-white mb-2">Get Female Creators (Home Screen)</h2>
                <p class="text-gray-400">Retrieves a list of female creators for the home screen with their profile, interests, and call rates.</p>
                <div class="mt-3 p-3 bg-yellow-900 bg-opacity-20 border border-yellow-600 rounded-lg">
                    <p class="text-sm text-yellow-400"><i class="fas fa-lock mr-2"></i><strong>Requires Authentication:</strong> Bearer Token</p>
                    <p class="text-sm text-yellow-400 mt-1"><i class="fas fa-user-shield mr-2"></i><strong>Access:</strong> Male users only</p>
                </div>
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
                                <td class="px-4 py-3 font-mono text-purple-400">limit</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3"><span class="text-gray-400">No</span></td>
                                <td class="px-4 py-3 text-gray-400">Items per page (default: 20, max: 50)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">page</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3"><span class="text-gray-400">No</span></td>
                                <td class="px-4 py-3 text-gray-400">Page number (default: 1)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">online</td>
                                <td class="px-4 py-3 text-gray-400">boolean</td>
                                <td class="px-4 py-3"><span class="text-gray-400">No</span></td>
                                <td class="px-4 py-3 text-gray-400">Filter by online status (true/false)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">language</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3"><span class="text-gray-400">No</span></td>
                                <td class="px-4 py-3 text-gray-400">Filter by language (Hindi, Tamil, etc.)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">verified</td>
                                <td class="px-4 py-3 text-gray-400">boolean</td>
                                <td class="px-4 py-3"><span class="text-gray-400">No</span></td>
                                <td class="px-4 py-3 text-gray-400">Filter by KYC verification status</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Response Fields -->
            <div class="mb-6">
                <h4 class="text-sm font-semibold text-gray-300 mb-3">RESPONSE FIELDS (CREATOR OBJECT)</h4>
                <div class="bg-black rounded-lg border border-gray-800 overflow-hidden">
                    <table class="w-full text-sm">
                        <thead class="bg-black">
                            <tr>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Field</th>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Type</th>
                                <th class="px-4 py-3 text-left text-xs font-semibold text-gray-400 uppercase">Description</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">id</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3 text-gray-400">Unique creator ID</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">name</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3 text-gray-400">Creator's display name</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">profile_image</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3 text-gray-400">Avatar/profile image URL</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">bio</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3 text-gray-400">Creator's description/bio</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">language</td>
                                <td class="px-4 py-3 text-gray-400">string</td>
                                <td class="px-4 py-3 text-gray-400">Preferred language</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">interests</td>
                                <td class="px-4 py-3 text-gray-400">array</td>
                                <td class="px-4 py-3 text-gray-400">Array of interest strings</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">is_online</td>
                                <td class="px-4 py-3 text-gray-400">boolean</td>
                                <td class="px-4 py-3 text-gray-400">Online status</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">audio_call_rate</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3 text-gray-400">Audio call cost (coins/min)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">video_call_rate</td>
                                <td class="px-4 py-3 text-gray-400">integer</td>
                                <td class="px-4 py-3 text-gray-400">Video call cost (coins/min)</td>
                            </tr>
                            <tr class="border-t border-gray-800">
                                <td class="px-4 py-3 font-mono text-purple-400">is_verified</td>
                                <td class="px-4 py-3 text-gray-400">boolean</td>
                                <td class="px-4 py-3 text-gray-400">KYC verification status</td>
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
                        <span class="text-gray-400">Success - Returns creator list</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">401</span>
                        <span class="text-gray-400">Unauthorized - Invalid or missing token</span>
                    </div>
                    <div class="flex items-center">
                        <span class="font-mono text-sm text-yellow-400 w-12">403</span>
                        <span class="text-gray-400">Forbidden - Only male users can access</span>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- Code Examples (Right Side) -->
    <div class="code-panel w-1/2 bg-black p-8 overflow-y-auto border-l border-gray-800 flex-shrink min-w-0" style="height: calc(100vh - 64px);">
        <!-- Interactive Testing Form -->
        <div class="mb-8 bg-[#1a1f2e] rounded-lg border border-blue-800 p-6">
            <h3 class="text-lg font-semibold text-white mb-4 flex items-center">
                <i class="fas fa-flask text-blue-400 mr-2"></i>
                Test This Endpoint
            </h3>
            <form id="testCreatorsForm" class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">
                        <i class="fas fa-key text-yellow-400 mr-1"></i> Bearer Token
                    </label>
                    <input type="text" name="token" placeholder="Enter your access token" 
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none" required>
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-sm font-medium text-gray-300 mb-2">Limit</label>
                        <input type="number" name="limit" placeholder="20" value="20"
                            class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-300 mb-2">Page</label>
                        <input type="number" name="page" placeholder="1" value="1"
                            class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none">
                    </div>
                </div>
                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-sm font-medium text-gray-300 mb-2">Online Status</label>
                        <select name="online" class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none">
                            <option value="">All</option>
                            <option value="true">Online Only</option>
                            <option value="false">Offline Only</option>
                        </select>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-300 mb-2">Verified</label>
                        <select name="verified" class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none">
                            <option value="">All</option>
                            <option value="true">Verified Only</option>
                            <option value="false">Not Verified</option>
                        </select>
                    </div>
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-300 mb-2">Language</label>
                    <input type="text" name="language" placeholder="e.g., Hindi, Tamil, English"
                        class="w-full bg-black border border-gray-700 rounded px-3 py-2 text-white text-sm focus:border-blue-500 focus:outline-none">
                </div>
                <button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 px-4 rounded transition">
                    <i class="fas fa-play mr-2"></i>Run Test
                </button>
            </form>
            <div id="testCreatorsResponse" class="mt-4 hidden">
                <div class="flex items-center justify-between mb-2">
                    <h4 class="text-sm font-semibold text-gray-300">Response:</h4>
                    <span id="responseStatus" class="text-xs font-mono px-2 py-1 rounded"></span>
                </div>
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <pre id="testCreatorsResponseBody" class="p-4 text-sm overflow-x-auto text-gray-300" style="max-height: 400px;"></pre>
                </div>
            </div>
        </div>

        <!-- Get Creators Example -->
        <div id="code-get-creators" class="code-example">
            <h3 class="text-sm font-semibold text-gray-400 mb-4">REQUEST EXAMPLE</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL</span>
                    <button onclick="copyCode('get-creators-req')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="get-creators-req" class="p-4 text-sm overflow-x-auto"><code>curl -X GET {{ url('/api/v1') }}/users/females?limit=20&page=1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json"</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">WITH FILTERS</h3>
            <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                    <span class="text-xs text-gray-400 font-mono">cURL - Online & Language Filter</span>
                    <button onclick="copyCode('get-creators-req-filter')" class="text-xs text-gray-400 hover:text-white transition">
                        <i class="fas fa-copy mr-1"></i>Copy
                    </button>
                </div>
                <pre id="get-creators-req-filter" class="p-4 text-sm overflow-x-auto"><code>curl -X GET {{ url('/api/v1') }}/users/females?online=true&language=Malayalam&limit=10 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json"</code></pre>
            </div>

            <h3 class="text-sm font-semibold text-gray-400 mb-4 mt-8">RESPONSE EXAMPLES</h3>
            
            <!-- Success Response -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-green-400 font-mono">200 OK - Success</span>
                        <button onclick="copyCode('get-creators-res-success')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="get-creators-res-success" class="p-4 text-sm overflow-x-auto"><code>{
  "success": true,
  "data": [
    {
      "id": "USR_1730736000000",
      "name": "Ananya798",
      "age": 24,
      "gender": "FEMALE",
      "profile_image": "https://cdn.example.com/profiles/ananya.jpg",
      "bio": "D. boss all movies",
      "language": "Kannada",
      "interests": ["Travel", "Movies", "Music"],
      "is_online": true,
      "last_seen": 1730736123456,
      "rating": 4.5,
      "total_ratings": 127,
      "audio_call_enabled": true,
      "video_call_enabled": true,
      "is_verified": true,
      "audio_call_rate": 10,
      "video_call_rate": 60
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 5,
    "total_items": 94,
    "per_page": 20,
    "has_next": true,
    "has_prev": false
  }
}</code></pre>
                </div>
            </div>

            <!-- Forbidden Error -->
            <div class="mb-4">
                <div class="bg-[#0D1117] rounded-lg border border-gray-800 overflow-hidden">
                    <div class="flex items-center justify-between px-4 py-2 bg-black border-b border-gray-800">
                        <span class="text-xs text-yellow-400 font-mono">403 Forbidden - Not Male User</span>
                        <button onclick="copyCode('get-creators-res-403')" class="text-xs text-gray-400 hover:text-white transition">
                            <i class="fas fa-copy mr-1"></i>Copy
                        </button>
                    </div>
                    <pre id="get-creators-res-403" class="p-4 text-sm overflow-x-auto"><code>{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "Only male users can access this endpoint"
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

// Test Creators Form Handler
document.getElementById('testCreatorsForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const token = formData.get('token');
    
    // Build query parameters
    const params = new URLSearchParams();
    if (formData.get('limit')) params.append('limit', formData.get('limit'));
    if (formData.get('page')) params.append('page', formData.get('page'));
    if (formData.get('online')) params.append('online', formData.get('online'));
    if (formData.get('verified')) params.append('verified', formData.get('verified'));
    if (formData.get('language')) params.append('language', formData.get('language'));
    
    const url = `{{ url('/api/v1') }}/users/females?${params.toString()}`;
    
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        
        const data = await response.json();
        
        // Display response
        const responseDiv = document.getElementById('testCreatorsResponse');
        const statusSpan = document.getElementById('responseStatus');
        const bodyPre = document.getElementById('testCreatorsResponseBody');
        
        // Set status badge color
        if (response.ok) {
            statusSpan.textContent = `${response.status} OK`;
            statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-green-900 text-green-400';
        } else {
            statusSpan.textContent = `${response.status} Error`;
            statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        }
        
        bodyPre.textContent = JSON.stringify(data, null, 2);
        responseDiv.classList.remove('hidden');
        
        // Scroll to response
        responseDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    } catch (error) {
        const responseDiv = document.getElementById('testCreatorsResponse');
        const statusSpan = document.getElementById('responseStatus');
        const bodyPre = document.getElementById('testCreatorsResponseBody');
        
        statusSpan.textContent = 'Network Error';
        statusSpan.className = 'text-xs font-mono px-2 py-1 rounded bg-red-900 text-red-400';
        bodyPre.textContent = JSON.stringify({ error: error.message }, null, 2);
        responseDiv.classList.remove('hidden');
    }
});
</script>
@endsection





