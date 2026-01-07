# ❌ vs ✅ Agora Token Generation: What's Wrong vs What's Right

## 🔴 WRONG Implementation (Causes Error 110)

### Scenario 1: Using UID = 0 but token generated for different UID

```php
// ❌ Backend generates token for UID 1002
$token = $agoraService->generateToken($channelName, 1002);

// Returns to app
return ['agora_token' => $token];  // No UID returned!

// Android app joins with UID 0 (default)
rtcEngine.joinChannel(token, channel, 0)  // ❌ 0 != 1002 → ERROR 110
```

**Why it fails:** Token generated for UID 1002, but app tries to join with UID 0.

---

### Scenario 2: Caller and receiver use the SAME token

```php
// ❌ POST /calls/initiate - generates ONE token
$token = $agoraService->generateToken($channelName, 1001);
$call->agora_token = $token;

// GET /calls/incoming - returns SAME token
return [
    'agora_token' => $call->agora_token  // Same token!
];

// Both caller and receiver join with UID 0
// Caller: joins with UID 0, but token is for UID 1001 → ERROR 110
// Receiver: joins with UID 0, but token is for UID 1001 → ERROR 110
```

**Why it fails:** Token generated for UID 1001, but both users join with UID 0.

---

### Scenario 3: Token generated for NULL UID

```php
// ❌ Backend passes null/undefined as UID
$token = $agoraService->generateToken($channelName, null);

// Android app joins with UID 0
rtcEngine.joinChannel(token, channel, 0)  // ❌ 0 != null → ERROR 110
```

**Why it fails:** Null UID doesn't match UID 0.

---

## 🟢 CORRECT Implementation (Works Perfectly)

### ✅ Solution 1: Use UID = 0 for Everyone (Simplest)

```php
// ✅ POST /calls/initiate - Generate token with UID 0
$token = $agoraService->generateToken(
    $channelName, 
    0,  // ✅ UID = 0
    RtcTokenBuilder::ROLE_PUBLISHER,
    3600  // 1 hour expiry
);

// Save to database
$call->agora_token = $token;
$call->channel_name = $channelName;

// Return to caller
return [
    'call_id' => $call->id,
    'agora_token' => $token,
    'channel_name' => $channelName
];

// ✅ GET /calls/incoming - Generate NEW token with UID 0
$token = $agoraService->generateToken(
    $call->channel_name, 
    0,  // ✅ UID = 0 (same as above)
    RtcTokenBuilder::ROLE_PUBLISHER,
    3600
);

// Return to receiver
return [
    'id' => $call->id,
    'agora_token' => $token,  // New token, but also for UID 0
    'channel_name' => $call->channel_name
];

// Android app joins with UID 0
rtcEngine.joinChannel(token, channel, 0)  // ✅ 0 == 0 → SUCCESS!
```

**Why it works:** Token generated for UID 0, app joins with UID 0. Match! ✅

---

### ✅ Solution 2: Use Unique UIDs (Better Security)

```php
// ✅ POST /calls/initiate - Caller token
$callerUid = $this->getUserAgoraUid($callerId);  // e.g., 1001

$callerToken = $agoraService->generateToken(
    $channelName, 
    $callerUid,  // ✅ UID = 1001
    RtcTokenBuilder::ROLE_PUBLISHER,
    3600
);

// Save to database
$call->caller_agora_uid = $callerUid;
$call->caller_agora_token = $callerToken;

// Return to caller with UID
return [
    'call_id' => $call->id,
    'agora_token' => $callerToken,
    'agora_uid' => $callerUid,  // ✅ Tell app to use UID 1001
    'channel_name' => $channelName
];

// ✅ GET /calls/incoming - Receiver token (DIFFERENT from caller)
$receiverUid = $this->getUserAgoraUid($receiverId);  // e.g., 1002

$receiverToken = $agoraService->generateToken(
    $call->channel_name, 
    $receiverUid,  // ✅ UID = 1002 (different from caller!)
    RtcTokenBuilder::ROLE_PUBLISHER,
    3600
);

// Save to database
$call->receiver_agora_uid = $receiverUid;
$call->receiver_agora_token = $receiverToken;

// Return to receiver with UID
return [
    'id' => $call->id,
    'agora_token' => $receiverToken,  // Different token
    'agora_uid' => $receiverUid,      // ✅ Tell app to use UID 1002
    'channel_name' => $call->channel_name
];

// Android app joins with the UID provided by backend
// Caller: joins with UID 1001, token is for UID 1001 ✅
// Receiver: joins with UID 1002, token is for UID 1002 ✅
```

**Why it works:** 
- Caller token generated for UID 1001, caller joins with UID 1001 ✅
- Receiver token generated for UID 1002, receiver joins with UID 1002 ✅
- Each user has their own token and UID!

---

## 📊 Side-by-Side Comparison

| Feature | ❌ Wrong Way | ✅ Solution 1 (UID=0) | ✅ Solution 2 (Unique UIDs) |
|---------|-------------|---------------------|---------------------------|
| **Token UID** | Random/null/wrong | 0 | User-specific (1001, 1002) |
| **App UID** | 0 (default) | 0 | Matches token UID |
| **Caller Token** | UID mismatch | UID = 0 | UID = 1001 |
| **Receiver Token** | Same as caller | UID = 0 | UID = 1002 (different!) |
| **Backend Returns UID** | ❌ No | Optional | ✅ Yes |
| **Result** | ❌ ERROR 110 | ✅ Works | ✅ Works (better) |
| **Security** | Poor | Good | Best |
| **Debugging** | Hard | Medium | Easy |

---

## 🧪 Real Example from Your Logs

### What's Happening Now (ERROR 110):

```
Backend generates token → unknown UID (maybe 1002?)
Android receives token → no UID provided
Android joins channel → uses UID 0 by default
Agora says: "Token is for UID 1002, but you're joining with UID 0!"
Result: ERROR 110 ❌
```

### What Should Happen:

**Option 1 (UID = 0):**
```
Backend generates token → UID = 0
Android receives token → no UID needed (uses 0)
Android joins channel → uses UID 0
Agora says: "Token is for UID 0, you're joining with UID 0. Welcome!"
Result: SUCCESS ✅
```

**Option 2 (Unique UIDs):**
```
Backend generates token → UID = 1002
Backend returns: { token: "...", agora_uid: 1002 }
Android receives: "Use UID 1002"
Android joins channel → uses UID 1002
Agora says: "Token is for UID 1002, you're joining with UID 1002. Welcome!"
Result: SUCCESS ✅
```

---

## 🔍 How to Debug Your Current Code

### Step 1: Find token generation code

Search for:
```php
generateToken(
RtcTokenBuilder::
buildTokenWithUid(
```

### Step 2: Check what UID is being passed

```php
// Example - check this line:
$token = $agoraService->generateToken($channelName, $uid);
                                                     ^^^^
                                                   What is this?
```

### Step 3: Add debug log

```php
Log::debug('🔑 AGORA TOKEN DEBUG', [
    'channel' => $channelName,
    'uid' => $uid,  // ← THIS IS THE KEY!
    'uid_type' => gettype($uid),
    'token_first_10' => substr($token, 0, 10)
]);
```

### Step 4: Test and check logs

Make a call and look for your debug log. Share the `uid` value with Android team.

---

## 📝 Complete Code Example (PHP with Agora RTC SDK)

```php
<?php

use Agora\RtcTokenBuilder;

class AgoraService 
{
    private $appId = '8b5e9417f15a48ae929783f32d3d33d4';
    private $appCertificate = 'YOUR_APP_CERTIFICATE';

    /**
     * ✅ CORRECT: Generate token with specific UID
     */
    public function generateToken($channelName, $uid) 
    {
        $role = RtcTokenBuilder::ROLE_PUBLISHER;
        $expireTimeInSeconds = 3600; // 1 hour
        $currentTimestamp = time();
        $privilegeExpiredTs = $currentTimestamp + $expireTimeInSeconds;

        // Generate token
        $token = RtcTokenBuilder::buildTokenWithUid(
            $this->appId,
            $this->appCertificate,
            $channelName,
            $uid,  // ✅ This MUST match the UID when joining!
            $role,
            $privilegeExpiredTs
        );

        // ✅ Add debug log
        Log::debug('🔑 Agora Token Generated', [
            'channel' => $channelName,
            'uid' => $uid,
            'uid_type' => gettype($uid),
            'token_length' => strlen($token),
            'expires_in' => $expireTimeInSeconds . ' seconds'
        ]);

        return $token;
    }

    /**
     * ✅ Helper: Convert user ID to Agora UID
     */
    private function getUserAgoraUid($userId) 
    {
        // If your user IDs are like "USR_12345"
        $numericId = (int) str_replace('USR_', '', $userId);
        
        // Or use database ID
        // $numericId = User::where('id', $userId)->value('id');
        
        return $numericId;
    }
}
```

---

## ✅ Checklist for Backend Team

- [ ] Find your Agora token generation code
- [ ] Check what UID value you're passing
- [ ] Add debug logs to see the UID
- [ ] Choose Solution 1 (UID=0) or Solution 2 (Unique UIDs)
- [ ] If Solution 2, add `agora_uid` to API responses
- [ ] Test with a real call
- [ ] Share debug logs with Android team

---

## 🎯 Success Criteria

After implementing the fix, Android logs should show:

```
✅ Receiver joins channel (result: 0)
✅ Remote user joined (UID: XXXX)
✅ Call connected!
```

**No more ERROR 110!** 🎉🎉🎉




