<!DOCTYPE html>
<html>
<head>
    <title>Test Profile Update - OnlyCare</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: #1a1a1a;
            color: #fff;
            padding: 20px;
        }
        .container { max-width: 600px; margin: 0 auto; }
        h1 { margin-bottom: 20px; font-size: 24px; }
        .box {
            background: #2a2a2a;
            padding: 20px;
            border-radius: 12px;
            margin-bottom: 20px;
        }
        input, textarea, button {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
            border: 1px solid #444;
            border-radius: 8px;
            font-size: 16px;
        }
        input, textarea {
            background: #1a1a1a;
            color: #fff;
        }
        button {
            background: #fff;
            color: #000;
            font-weight: bold;
            cursor: pointer;
            border: none;
        }
        button:active { background: #ddd; }
        #result {
            margin-top: 20px;
            padding: 15px;
            border-radius: 8px;
            word-wrap: break-word;
        }
        .success { background: #1b5e20; color: #4CAF50; }
        .error { background: #b71c1c; color: #f44336; }
        .info { background: #0d47a1; color: #2196F3; }
        pre {
            background: #000;
            padding: 10px;
            border-radius: 4px;
            overflow-x: auto;
            font-size: 12px;
            margin-top: 10px;
        }
        label { display: block; margin-top: 10px; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🔧 OnlyCare Profile Update Test</h1>
        
        <div class="box">
            <h3>Step 1: Get Your Auth Token</h3>
            <p>Login to your app first, then come back here</p>
            <label>Phone Number (with or without country code):</label>
            <input type="tel" id="phone" placeholder="9876543210 or +919876543210" value="9876543210" />
            <button onclick="getToken()">📱 Get Token</button>
        </div>

        <div class="box">
            <h3>Step 2: Update Profile</h3>
            <label>Auth Token:</label>
            <textarea id="token" rows="3" placeholder="Paste your auth token here"></textarea>
            <label>Username (4-10 chars, letters + numbers):</label>
            <input type="text" id="username" placeholder="user123" />
            <label>Name:</label>
            <input type="text" id="name" placeholder="John Doe" />
            <label>Interests (comma separated):</label>
            <input type="text" id="interests" placeholder="MUSIC,MOVIES,GAMING" />
            <button onclick="updateProfile()">💾 Update Profile</button>
        </div>

        <div id="result"></div>
    </div>

    <script>
        const BASE_URL = 'http://192.168.0.5:8000/api/v1';

        async function getToken() {
            const phone = document.getElementById('phone').value;
            if (!phone) {
                showResult('Please enter phone number', 'error');
                return;
            }

            showResult('🔄 Creating fresh login token...', 'info');

            try {
                // Use quick_login.php to generate a FRESH token
                const response = await fetch('<?php echo $_SERVER['REQUEST_SCHEME'] . '://' . $_SERVER['HTTP_HOST']; ?>/quick_login.php?phone=' + encodeURIComponent(phone));
                const data = await response.json();
                
                if (data.success) {
                    document.getElementById('token').value = data.token;
                    document.getElementById('name').value = data.user.name || '';
                    document.getElementById('username').value = data.user.username || '';
                    showResult('✅ SUCCESS! Fresh token generated!\nUser: ' + data.user.name + '\nYou can now update profile!', 'success');
                } else {
                    showResult('❌ ' + data.error, 'error');
                }
            } catch (error) {
                showResult('❌ Error: ' + error.message, 'error');
            }
        }

        async function updateProfile() {
            const token = document.getElementById('token').value.trim();
            const username = document.getElementById('username').value.trim();
            const name = document.getElementById('name').value.trim();
            const interests = document.getElementById('interests').value.trim();

            if (!token) {
                showResult('Please provide auth token', 'error');
                return;
            }

            const requestBody = {};
            if (name) requestBody.name = name;
            if (username) requestBody.username = username;
            if (interests) requestBody.interests = interests.split(',').map(s => s.trim());

            showResult('🔄 Sending update request...', 'info');

            try {
                const response = await fetch(BASE_URL + '/users/me', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + token,
                        'Accept': 'application/json'
                    },
                    body: JSON.stringify(requestBody)
                });

                const data = await response.json();
                
                if (response.ok && data.success) {
                    showResult('✅ SUCCESS! Profile updated!\n\n' + JSON.stringify(data, null, 2), 'success');
                } else {
                    showResult('❌ API Error (Status ' + response.status + '):\n\n' + JSON.stringify(data, null, 2), 'error');
                }
            } catch (error) {
                showResult('❌ Network Error: ' + error.message + '\n\nMake sure:\n1. Laravel server is running\n2. MySQL is running\n3. You are on the same WiFi network', 'error');
            }
        }

        function showResult(message, type) {
            const resultDiv = document.getElementById('result');
            resultDiv.className = type;
            resultDiv.innerHTML = '<pre>' + message + '</pre>';
        }
    </script>
</body>
</html>

