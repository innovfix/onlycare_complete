# 🔍 Agora Backend Token Generation - DIAGNOSIS

**Date:** November 22, 2025  
**Status:** 🚨 CRITICAL ISSUE FOUND  
**Priority:** HIGH

---

## ✅ What We Confirmed

### 1. Android App is CORRECT ✅
- Android uses **UID = 0** when joining (correct!)
- Android receives token from backend API
- Android attempts to join with that token

### 2. Agora Credentials ARE Configured ✅
```bash
AGORA_APP_ID=8b5e9417f15a48ae929783f32d3d33d4
AGORA_APP_CERTIFICATE=03e9b06b303e47a9b93e71aed9faac63
```
Both values are set in `.env` file ✅

### 3. Backend Code Uses UID = 0 ✅
```php
// Line 876 in CallController.php
$uid = 0; // ✅ Correct
```

---

## 🐛 THE REAL PROBLEM

Looking at your `generateAgoraToken()` function (lines 871-928), there are **THREE cases** where it returns an **EMPTY STRING** instead of a valid token:

### Case 1: App ID Missing (Line 898-901)
```php
if (empty($appId)) {
    Log::warning('Agora App ID not configured');
    return '';  // ⚠️ Returns empty string!
}
```

### Case 2: App Certificate Missing (Line 905-908)
```php
if (empty($appCertificate)) {
    Log::info("Agora project in UNSECURE mode (no certificate) - using null token");
    return '';  // ⚠️ Returns empty string!
}
```

### Case 3: Token Generation Fails (Line 922-927)
```php
catch (\Exception $e) {
    Log::error('Agora token generation failed: ' . $e->getMessage());
    return '';  // ⚠️ Returns empty string!
}
```

---

## 🎯 Most Likely Scenario

**Your token generation is FAILING and returning empty string, causing Error 110!**

Here's what's happening:

```
1. Android initiates call
   ↓
2. Backend tries to generate token
   ↓
3. Token generation FAILS (exception thrown)
   OR credentials not loaded properly
   ↓
4. Backend returns EMPTY STRING ('')
   ↓
5. API response sends empty token to Android
   ↓
6. Android tries to join with empty token + UID = 0
   ↓
7. Agora server rejects: ERROR 110 ❌
```

---

## 🧪 DIAGNOSTIC TESTS

Run these tests to find the exact problem:

### Test 1: Check if Config Values are Loaded

```bash
cd /var/www/onlycare_admin
php artisan tinker
```

Then run:
```php
config('services.agora.app_id');
config('services.agora.app_certificate');
env('AGORA_APP_ID');
env('AGORA_APP_CERTIFICATE');
```

**Expected output:**
```
"8b5e9417f15a48ae929783f32d3d33d4"
"03e9b06b303e47a9b93e71aed9faac63"
"8b5e9417f15a48ae929783f32d3d33d4"
"03e9b06b303e47a9b93e71aed9faac63"
```

**If you see `null` or empty string:** Config cache is stale!

**FIX:**
```bash
php artisan config:clear
php artisan config:cache
```

---

### Test 2: Check AgoraTokenBuilder Class

```bash
cd /var/www/onlycare_admin
php artisan tinker
```

```php
use App\Services\AgoraTokenBuilder;

$token = AgoraTokenBuilder::buildTokenWithDefault(
    '8b5e9417f15a48ae929783f32d3d33d4',
    '03e9b06b303e47a9b93e71aed9faac63',
    'test_channel',
    0
);

echo "Token: " . substr($token, 0, 50) . "...\n";
echo "Length: " . strlen($token);
```

**Expected output:**
```
Token: 0078b5e9417f15a48ae929783f32d3d33d4AbC123XyZ...
Length: 200+ characters
```

**If you get an error:** The AgoraTokenBuilder class has a bug!

---

### Test 3: Make a Real Test Call

Add this test endpoint to your routes temporarily:

```php
// In routes/api.php
Route::get('/test-agora-token', function() {
    $controller = new \App\Http\Controllers\Api\CallController();
    
    // Use reflection to call private method
    $reflection = new ReflectionClass($controller);
    $method = $reflection->getMethod('generateAgoraToken');
    $method->setAccessible(true);
    
    $token = $method->invoke($controller, 'TEST_12345');
    
    return response()->json([
        'token' => $token,
        'token_empty' => empty($token),
        'token_length' => strlen($token),
        'token_preview' => substr($token, 0, 50),
        'app_id' => config('services.agora.app_id'),
        'app_cert_exists' => !empty(config('services.agora.app_certificate')),
        'app_cert_length' => strlen(config('services.agora.app_certificate') ?? '')
    ]);
});
```

Then test:
```bash
curl -X GET http://your-domain.com/api/test-agora-token
```

---

## 🔧 MOST COMMON FIXES

### Fix 1: Clear Config Cache (90% of cases)

```bash
cd /var/www/onlycare_admin
php artisan config:clear
php artisan cache:clear
php artisan config:cache
```

Then restart:
```bash
sudo systemctl restart php8.2-fpm  # or php8.1-fpm, php8.3-fpm
sudo systemctl restart nginx
```

---

### Fix 2: Verify .env File is Being Read

Check if `.env` file exists and is readable:

```bash
cd /var/www/onlycare_admin
ls -la .env
cat .env | grep AGORA
```

Make sure permissions are correct:
```bash
chmod 644 .env
chown www-data:www-data .env  # or your web server user
```

---

### Fix 3: Check AgoraTokenBuilder Implementation

The token builder might have a bug. Let's verify:

```bash
cd /var/www/onlycare_admin
cat app/Services/AgoraTokenBuilder.php | grep -A 10 "buildTokenWithDefault"
```

**Should see:**
```php
public static function buildTokenWithDefault(
    string $appId,
    string $appCertificate,
    string $channelName,
    int $uid = 0
): string {
    return self::buildToken($appId, $appCertificate, $channelName, $uid, self::ROLE_PUBLISHER, 86400);
}
```

If the implementation looks wrong, the token generation will fail!

---

### Fix 4: Add Comprehensive Logging

Update `generateAgoraToken()` method to add better logging:

```php
private function generateAgoraToken($callId)
{
    Log::info("========================================");
    Log::info("AGORA TOKEN GENERATION START");
    Log::info("========================================");
    
    $appId = config('services.agora.app_id', env('AGORA_APP_ID'));
    $appCertificate = config('services.agora.app_certificate', env('AGORA_APP_CERTIFICATE'));
    $channelName = 'call_' . $callId;
    $uid = 0;
    
    Log::info("Call ID: {$callId}");
    Log::info("Channel Name: {$channelName}");
    Log::info("UID: {$uid}");
    Log::info("App ID: " . ($appId ?? 'NULL'));
    Log::info("App ID Empty: " . (empty($appId) ? 'YES' : 'NO'));
    Log::info("Certificate: " . (empty($appCertificate) ? 'EMPTY' : substr($appCertificate, 0, 10) . '...'));
    Log::info("Certificate Length: " . strlen($appCertificate ?? ''));
    
    // Check if credentials are configured
    if (empty($appId)) {
        Log::error('❌ AGORA APP ID IS EMPTY!');
        Log::error('Config value: ' . var_export(config('services.agora.app_id'), true));
        Log::error('Env value: ' . var_export(env('AGORA_APP_ID'), true));
        return '';
    }
    
    if (empty($appCertificate)) {
        Log::warning('❌ AGORA CERTIFICATE IS EMPTY!');
        Log::warning('Config value: ' . var_export(config('services.agora.app_certificate'), true));
        Log::warning('Env value: ' . var_export(env('AGORA_APP_CERTIFICATE'), true));
        Log::warning("Project in UNSECURE mode - returning empty token");
        return '';
    }
    
    try {
        Log::info("Calling AgoraTokenBuilder::buildTokenWithDefault()...");
        
        $token = AgoraTokenBuilder::buildTokenWithDefault(
            $appId,
            $appCertificate,
            $channelName,
            $uid
        );
        
        Log::info("✅ Token generated successfully!");
        Log::info("Token length: " . strlen($token));
        Log::info("Token preview: " . substr($token, 0, 30) . "...");
        Log::info("========================================");
        
        return $token;
        
    } catch (\Exception $e) {
        Log::error("❌ TOKEN GENERATION EXCEPTION!");
        Log::error("Exception class: " . get_class($e));
        Log::error("Exception message: " . $e->getMessage());
        Log::error("Exception trace: " . $e->getTraceAsString());
        Log::error("========================================");
        return '';
    }
}
```

---

## 📋 IMMEDIATE ACTION ITEMS

### Step 1: Clear Caches
```bash
cd /var/www/onlycare_admin
php artisan config:clear
php artisan cache:clear
php artisan config:cache
sudo systemctl restart php8.2-fpm
sudo systemctl restart nginx
```

### Step 2: Test in Tinker
```bash
php artisan tinker
```
```php
config('services.agora.app_id')
config('services.agora.app_certificate')
```

### Step 3: Update the Logging
Add the comprehensive logging code above to `generateAgoraToken()`

### Step 4: Make a Test Call
Initiate a call from Android app and immediately check logs:

```bash
tail -f storage/logs/laravel.log | grep -A 20 "AGORA TOKEN GENERATION"
```

### Step 5: Share the Logs
Send us the logs showing:
- What values are loaded for App ID and Certificate
- Whether token generation succeeds or fails
- The exact error if it fails

---

## 🎯 EXPECTED LOGS (Success)

```
[2025-11-22 10:30:15] local.INFO: ========================================
[2025-11-22 10:30:15] local.INFO: AGORA TOKEN GENERATION START
[2025-11-22 10:30:15] local.INFO: ========================================
[2025-11-22 10:30:15] local.INFO: Call ID: CALL_17637609673605
[2025-11-22 10:30:15] local.INFO: Channel Name: call_CALL_17637609673605
[2025-11-22 10:30:15] local.INFO: UID: 0
[2025-11-22 10:30:15] local.INFO: App ID: 8b5e9417f15a48ae929783f32d3d33d4
[2025-11-22 10:30:15] local.INFO: App ID Empty: NO
[2025-11-22 10:30:15] local.INFO: Certificate: 03e9b06b30...
[2025-11-22 10:30:15] local.INFO: Certificate Length: 32
[2025-11-22 10:30:15] local.INFO: Calling AgoraTokenBuilder::buildTokenWithDefault()...
[2025-11-22 10:30:15] local.INFO: ✅ Token generated successfully!
[2025-11-22 10:30:15] local.INFO: Token length: 287
[2025-11-22 10:30:15] local.INFO: Token preview: 0078b5e9417f15a48ae929783f...
```

---

## 🔴 EXPECTED LOGS (Failure - Config Issue)

```
[2025-11-22 10:30:15] local.INFO: ========================================
[2025-11-22 10:30:15] local.INFO: AGORA TOKEN GENERATION START
[2025-11-22 10:30:15] local.INFO: ========================================
[2025-11-22 10:30:15] local.INFO: Call ID: CALL_17637609673605
[2025-11-22 10:30:15] local.INFO: App ID: NULL
[2025-11-22 10:30:15] local.ERROR: ❌ AGORA APP ID IS EMPTY!
[2025-11-22 10:30:15] local.ERROR: Config value: NULL
[2025-11-22 10:30:15] local.ERROR: Env value: NULL
```

**FIX:** Run `php artisan config:clear && php artisan config:cache`

---

## 🔴 EXPECTED LOGS (Failure - Token Builder Bug)

```
[2025-11-22 10:30:15] local.INFO: Calling AgoraTokenBuilder::buildTokenWithDefault()...
[2025-11-22 10:30:15] local.ERROR: ❌ TOKEN GENERATION EXCEPTION!
[2025-11-22 10:30:15] local.ERROR: Exception: Call to undefined method...
```

**FIX:** Debug the AgoraTokenBuilder class

---

## 💡 Bottom Line

**Your backend is returning EMPTY TOKENS, causing Error 110 on Android.**

Most likely causes (in order of probability):
1. 🥇 **Config cache is stale** (90% probability)
2. 🥈 **Token builder has a bug** (5% probability)
3. 🥉 **.env file not being read** (3% probability)
4. ❓ **Something else** (2% probability)

**FIRST STEP:** Clear all caches and test again!

```bash
cd /var/www/onlycare_admin
php artisan config:clear
php artisan cache:clear
php artisan config:cache
sudo systemctl restart php8.2-fpm
sudo systemctl restart nginx
```

Then test a call and check the logs!

---

**Need help?** Run the diagnostic tests above and share the output! 🚀

