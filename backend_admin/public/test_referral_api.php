<!DOCTYPE html>
<html>
<head>
    <title>Test Referral API - OnlyCare</title>
    <style>
        body {
            font-family: 'Segoe UI', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            margin: 0;
            padding: 20px;
            min-height: 100vh;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
        }
        .header {
            text-align: center;
            color: white;
            margin-bottom: 30px;
        }
        .header h1 {
            font-size: 2.5em;
            margin: 0 0 10px 0;
        }
        .header p {
            font-size: 1.1em;
            opacity: 0.9;
        }
        .box {
            background: white;
            border-radius: 15px;
            padding: 25px;
            margin-bottom: 20px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
        }
        .box h3 {
            color: #667eea;
            margin-top: 0;
            border-bottom: 2px solid #667eea;
            padding-bottom: 10px;
        }
        label {
            display: block;
            margin: 15px 0 5px 0;
            font-weight: 600;
            color: #555;
        }
        input, textarea {
            width: 100%;
            padding: 12px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 14px;
            box-sizing: border-box;
            transition: border 0.3s;
        }
        input:focus, textarea:focus {
            outline: none;
            border-color: #667eea;
        }
        button {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 14px 30px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 15px;
            font-weight: 600;
            margin-top: 15px;
            transition: transform 0.2s, box-shadow 0.2s;
            width: 100%;
        }
        button:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        button:active {
            transform: translateY(0);
        }
        .result {
            margin-top: 20px;
            padding: 15px;
            border-radius: 8px;
            white-space: pre-wrap;
            font-family: 'Courier New', monospace;
            font-size: 13px;
        }
        .success {
            background: #d4edda;
            border: 2px solid #28a745;
            color: #155724;
        }
        .error {
            background: #f8d7da;
            border: 2px solid #dc3545;
            color: #721c24;
        }
        .info {
            background: #d1ecf1;
            border: 2px solid #17a2b8;
            color: #0c5460;
        }
        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }
        .stat-card h4 {
            margin: 0 0 5px 0;
            font-size: 14px;
            opacity: 0.9;
        }
        .stat-card .value {
            font-size: 32px;
            font-weight: bold;
            margin: 5px 0;
        }
        .code-display {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
            margin: 15px 0;
        }
        .code-display .code {
            font-size: 36px;
            font-weight: bold;
            color: #667eea;
            letter-spacing: 3px;
            font-family: 'Courier New', monospace;
        }
        .btn-copy {
            background: #28a745;
            display: inline-block;
            padding: 10px 20px;
            margin-top: 10px;
            width: auto;
        }
        .btn-copy:hover {
            background: #218838;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎁 Test Referral API</h1>
            <p>Test the referral code generation and verify uniqueness</p>
        </div>

        <div class="box">
            <h3>📱 Step 1: Get Your Token</h3>
            <label>Phone Number (with or without country code):</label>
            <input type="tel" id="phone" placeholder="9876543210 or +919876543210" value="9876543210" />
            <button onclick="getToken()">🔑 Get Token</button>
        </div>

        <div class="box">
            <h3>🎟️ Step 2: Get Your Referral Code</h3>
            <label>Auth Token:</label>
            <input type="text" id="token" placeholder="Your auth token will appear here..." readonly />
            <button onclick="getReferralCode()">🎯 Get My Referral Code</button>
            
            <div id="referralDisplay"></div>
        </div>

        <div class="box">
            <h3>🔍 Step 3: Check Database Uniqueness</h3>
            <button onclick="checkUniqueness()">✅ Verify All Codes Are Unique</button>
        </div>

        <div class="box">
            <h3>📊 Response</h3>
            <div id="result" class="result info">Click a button above to test the API...</div>
        </div>
    </div>

    <script>
        async function getToken() {
            const phone = document.getElementById('phone').value;
            if (!phone) {
                showResult('Please enter phone number', 'error');
                return;
            }

            showResult('🔄 Getting fresh token...', 'info');

            try {
                const response = await fetch('<?php echo $_SERVER['REQUEST_SCHEME'] . '://' . $_SERVER['HTTP_HOST']; ?>/quick_login.php?phone=' + encodeURIComponent(phone));
                const data = await response.json();
                
                if (data.success) {
                    document.getElementById('token').value = data.token;
                    showResult('✅ SUCCESS! Token generated!\nUser: ' + data.user.name + '\n\nYou can now get your referral code!', 'success');
                } else {
                    showResult('❌ ' + data.error, 'error');
                }
            } catch (error) {
                showResult('❌ Error: ' + error.message, 'error');
            }
        }

        async function getReferralCode() {
            const token = document.getElementById('token').value;
            if (!token) {
                showResult('Please get your token first!', 'error');
                return;
            }

            showResult('🔄 Fetching your referral code...', 'info');

            try {
                const response = await fetch('<?php echo $_SERVER['REQUEST_SCHEME'] . '://' . $_SERVER['HTTP_HOST']; ?>/api/v1/referral/code', {
                    method: 'GET',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Accept': 'application/json',
                        'ngrok-skip-browser-warning': 'true'
                    }
                });

                const data = await response.json();
                
                if (data.success) {
                    // Display the referral code prominently
                    document.getElementById('referralDisplay').innerHTML = `
                        <div class="code-display">
                            <h4>🎉 Your Unique Referral Code:</h4>
                            <div class="code" id="codeText">${data.referral_code}</div>
                            <button class="btn-copy" onclick="copyCode('${data.referral_code}')">📋 Copy Code</button>
                        </div>
                        <div class="stats">
                            <div class="stat-card">
                                <h4>My Invites</h4>
                                <div class="value">${data.my_invites}</div>
                            </div>
                            <div class="stat-card">
                                <h4>Per Invite</h4>
                                <div class="value">${data.per_invite_coins} 🪙</div>
                            </div>
                            <div class="stat-card">
                                <h4>Total Earned</h4>
                                <div class="value">${data.total_coins_earned} 🪙</div>
                            </div>
                        </div>
                    `;

                    showResult(
                        '✅ SUCCESS! Your referral code fetched!\n\n' +
                        '📊 API Response:\n' +
                        JSON.stringify(data, null, 2),
                        'success'
                    );
                } else {
                    showResult('❌ ERROR:\n' + JSON.stringify(data, null, 2), 'error');
                }
            } catch (error) {
                showResult('❌ Network Error: ' + error.message, 'error');
            }
        }

        async function checkUniqueness() {
            showResult('🔄 Checking database for duplicate codes...', 'info');

            try {
                const response = await fetch('<?php echo $_SERVER['REQUEST_SCHEME'] . '://' . $_SERVER['HTTP_HOST']; ?>/check_referral_uniqueness.php');
                const data = await response.json();

                let message = `📊 Database Check Results:\n\n`;
                message += `Total Users: ${data.total_users}\n`;
                message += `Users with Codes: ${data.users_with_codes}\n`;
                message += `Unique Codes: ${data.unique_codes}\n\n`;

                if (data.is_unique) {
                    message += `✅ ALL REFERRAL CODES ARE UNIQUE!\n\n`;
                    message += `The system is working perfectly! Each user gets a unique code.`;
                    showResult(message, 'success');
                } else {
                    message += `❌ DUPLICATE CODES FOUND!\n\n`;
                    message += `Duplicates: ${JSON.stringify(data.duplicates, null, 2)}`;
                    showResult(message, 'error');
                }
            } catch (error) {
                showResult('❌ Error: ' + error.message, 'error');
            }
        }

        function copyCode(code) {
            navigator.clipboard.writeText(code).then(() => {
                alert('✅ Code copied to clipboard: ' + code);
            }).catch(err => {
                alert('❌ Failed to copy: ' + err);
            });
        }

        function showResult(text, type) {
            const resultDiv = document.getElementById('result');
            resultDiv.textContent = text;
            resultDiv.className = 'result ' + type;
        }
    </script>
</body>
</html>

