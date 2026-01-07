# 🔴 URGENT: Agora Error 110 - Backend Token Issue

## Problem Summary
Both caller and receiver are getting Agora `ERR_OPEN_CHANNEL_TIMEOUT (110)` **within 200ms** of joining the channel. This is **abnormal** - normal timeout is 10-20 seconds.

## Evidence
- **Caller**: Error 110 at 192ms after joining
- **Receiver**: Error 110 at 176ms after joining
- **Both devices**: Multiple rapid-fire Error 110 messages
- **App code**: ✅ Working correctly (role detection is perfect)

## Root Cause
The extremely fast timeout (< 200ms) indicates Agora is **immediately rejecting the connection**, which means:
1. **Token is invalid** (most likely)
2. **Token generation parameters are wrong**
3. **App ID mismatch**

## Backend Actions Required

### 1. Verify Token Generation Parameters

Check your token generation code for these parameters:

```php
// REQUIRED PARAMETERS FOR AGORA TOKEN
$appId = "8b5e9417f15a48ae929783f32d3d33d4";  // Must match app
$appCertificate = "YOUR_APP_CERTIFICATE";      // From Agora console
$channelName = "call_CALL_17638139941273";     // Must match exactly
$uid = 0;                                       // Should be 0 for dynamic UID
$role = RtcTokenBuilder::RolePublisher;        // Must be Publisher (not Subscriber)
$expireTimeInSeconds = 3600;                   // 1 hour
$privilegeExpiredTs = time() + $expireTimeInSeconds;

$token = RtcTokenBuilder::buildTokenWithUid(
    $appId,
    $appCertificate,
    $channelName,
    $uid,
    $role,
    $privilegeExpiredTs
);
```

### 2. Common Token Generation Mistakes

❌ **WRONG:**
```php
// Using subscriber role (can't publish audio/video)
$role = RtcTokenBuilder::RoleSubscriber;

// Wrong UID (must be 0 or match joining UID)
$uid = 123456;

// Expired token
$privilegeExpiredTs = time() - 3600;  // Already expired!

// Wrong channel name format
$channelName = "CALL_17638139941273";  // Missing "call_" prefix
```

✅ **CORRECT:**
```php
// Publisher role (can both publish and subscribe)
$role = RtcTokenBuilder::RolePublisher;

// UID = 0 for dynamic assignment
$uid = 0;

// Token valid for 1 hour
$privilegeExpiredTs = time() + 3600;

// Channel name must match exactly
$channelName = "call_CALL_17638139941273";  // With "call_" prefix
```

### 3. Verify Agora Console Settings

1. Go to **Agora Console** (https://console.agora.io/)
2. Find project with App ID: `8b5e9417f15a48ae929783f32d3d33d4`
3. Verify:
   - ✅ Project is **enabled** (not disabled)
   - ✅ **Primary Certificate** is correct (used for token generation)
   - ✅ **Token authentication** is enabled
   - ✅ No **IP whitelist** restrictions (or add your server IP)

### 4. Test Token Generation

Add this debug endpoint to test token generation:

```php
// /api/v1/test/agora-token
public function testAgoraToken(Request $request)
{
    $channelName = "test_channel_" . time();
    
    // Generate token
    $token = $this->generateAgoraToken($channelName, 0);
    
    // Test token validation (if Agora SDK available)
    $isValid = $this->validateAgoraToken($token, $channelName);
    
    return response()->json([
        'success' => true,
        'channel_name' => $channelName,
        'token' => $token,
        'token_length' => strlen($token),
        'is_valid' => $isValid,
        'app_id' => config('agora.app_id'),
        'expires_in_seconds' => 3600,
        'debug_info' => [
            'uid' => 0,
            'role' => 'PUBLISHER',
            'timestamp' => time(),
        ]
    ]);
}
```

### 5. Check Token Format

A valid Agora token should:
- Start with `006` or `007` (token version)
- Be **139-150 characters** long
- Example from logs: `0078b5e9417f15a48ae929783f32d3d33d4AAAAIGmEOLu3hverWVqzhouhY3UlkZ71pvmKn/POsI5mgtB1BcbaxWkhqmppIvvqABhjYWxsX0NBTExfMTc2MzgxMzk5NDEyNzMAAAAA`

Your tokens in the logs **look correct** (139 chars, starting with `007`), but they might be generated with wrong parameters.

### 6. Enable Agora Backend Logs

If using Agora's backend SDK, enable debug logs to see token generation details:

```php
// In your Agora service class
Log::debug('Generating Agora token', [
    'channel_name' => $channelName,
    'uid' => $uid,
    'app_id' => $appId,
    'app_certificate' => substr($appCertificate, 0, 10) . '...', // Don't log full cert
    'role' => $role,
    'expire_time' => $privilegeExpiredTs,
    'current_time' => time(),
]);
```

### 7. Verify Database Call Status

Check the `calls` table for these specific calls:
- `CALL_17638138666870` (first test)
- `CALL_17638139941273` (second test)

Verify:
- Tokens are being saved correctly
- Channel names match format `call_CALL_*`
- Status transitions: `CONNECTING` → `ONGOING` → `ENDED`

## Expected Behavior After Fix

✅ **Caller:**
1. Joins Agora channel
2. Waits for receiver (10-30 seconds max)
3. Receiver joins → call connected

✅ **Receiver:**
1. Accepts call
2. Joins Agora channel
3. **IMMEDIATELY** sees connected screen (no waiting)

## Contact

If you need help debugging token generation:
1. Share your token generation code (redact app certificate)
2. Share Agora console project settings screenshot
3. Test the `/api/v1/test/agora-token` endpoint

---

**Priority: 🔴 CRITICAL**
**Status: Backend Investigation Required**
**App Code Status: ✅ Working Correctly**



