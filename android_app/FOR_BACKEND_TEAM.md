# 🚨 URGENT: Backend Token Generation Issue

## 📊 Issue Summary

**Date:** November 22, 2025  
**Priority:** HIGH - Calls Failing  
**Status:** Root cause identified

---

## 🔍 What's Happening

Android logs show:

```
03:06:16.460 - Android app joins with UID = 0 ✅
03:06:16.461 - Join channel result: 0 (success) ✅
03:06:16.607 - ERROR 110: Token validation failed ❌
```

**Key Finding:** Error 110 fires **immediately** (within 150ms) after joining, not after a timeout.

---

## 🎯 Root Cause: Backend Token Generation

### What We Know:

1. ✅ Android app correctly uses **UID = 0** when joining
2. ✅ Channel join succeeds (result code: 0)
3. ❌ Agora immediately rejects with Error 110
4. ❌ This means **your tokens are NOT generated for UID = 0**

### What This Means:

```
Backend generates token: FOR UID = ??? (not 0)
Android joins channel:   WITH UID = 0
Agora says: "Token invalid! UID mismatch!"
Result: ERROR 110 ❌
```

---

## ❓ CRITICAL QUESTIONS

### 1. Check Your Token Generation Code

**Find where you call the Agora token builder:**

```php
// Example - what is $uid here?
$token = RtcTokenBuilder::buildTokenWithUid(
    $appId,
    $appCertificate,
    $channelName,
    $uid,  // ⚠️ WHAT VALUE IS THIS?
    $role,
    $privilegeExpiredTs
);
```

**What value are you passing as `$uid`?**

- A) `0` (should work)
- B) `$userId` (user's actual ID - causes Error 110)
- C) `null` (causes Error 110)
- D) Random number (causes Error 110)

---

### 2. Check Your Database Schema

**Do you have these columns?**

```sql
ALTER TABLE calls 
ADD COLUMN caller_agora_uid INTEGER,
ADD COLUMN receiver_agora_token TEXT,
ADD COLUMN receiver_agora_uid INTEGER;
```

**If NO:** You might be reusing the caller's token for the receiver (WRONG).

---

### 3. Trace Your API Endpoints

#### POST /api/v1/calls/initiate

```php
// When caller initiates, what UID do you use?
public function initiateCall(Request $request) 
{
    $callerId = $request->user()->id;
    $channelName = "call_" . $callId;
    
    // ⚠️ WHAT IS $uid HERE?
    $token = $this->agoraService->generateToken($channelName, $uid);
    
    $call->agora_token = $token;
    $call->channel_name = $channelName;
}
```

**Question:** What value is `$uid` in this code?

---

#### GET /api/v1/calls/incoming

```php
// When receiver fetches incoming calls, what token do you return?
public function getIncomingCalls(Request $request) 
{
    $receiverId = $request->user()->id;
    $calls = Call::where('receiver_id', $receiverId)
                 ->where('status', 'CONNECTING')
                 ->get();
    
    // ⚠️ ARE YOU RETURNING CALLER'S TOKEN OR GENERATING NEW ONE?
    return [
        'agora_token' => $call->agora_token,  // Is this caller's token?
        'channel_name' => $call->channel_name
    ];
}
```

**Questions:**
1. Are you returning the **caller's token** (WRONG)?
2. Or generating a **new token for receiver** (CORRECT)?

---

#### POST /api/v1/calls/{id}/accept

```php
// When receiver accepts, what token do you return?
public function acceptCall($callId) 
{
    $call = Call::findOrFail($callId);
    
    // ⚠️ WHAT TOKEN ARE YOU RETURNING?
    return [
        'agora_token' => $call->agora_token,  // Is this caller's token?
        'channel_name' => $call->channel_name
    ];
}
```

**Question:** Are you returning the same token that was given to the caller?

---

## ✅ THE FIX

### Solution 1: Use UID = 0 for All Tokens (Simplest)

```php
class AgoraService 
{
    public function generateToken($channelName) 
    {
        $uid = 0;  // ✅ Always use 0
        
        $token = RtcTokenBuilder::buildTokenWithUid(
            $this->appId,
            $this->appCertificate,
            $channelName,
            0,  // ✅ Hardcoded 0
            RtcTokenBuilder::ROLE_PUBLISHER,
            time() + 3600
        );
        
        Log::debug('Token generated', [
            'channel' => $channelName,
            'uid' => 0,
            'token_length' => strlen($token)
        ]);
        
        return $token;
    }
}
```

**Update all 3 endpoints:**

```php
// POST /calls/initiate
$callerToken = $agoraService->generateToken($channelName);  // UID = 0

// GET /calls/incoming + POST /calls/accept
// Generate NEW token for receiver
$receiverToken = $agoraService->generateToken($channelName);  // UID = 0

// Return NEW token (not caller's token!)
return [
    'agora_token' => $receiverToken,  // ✅ New token
    'channel_name' => $channelName
];
```

---

### Solution 2: Use Unique UIDs (Better but more work)

```php
class AgoraService 
{
    public function generateToken($channelName, $userId) 
    {
        // Convert user ID to integer UID
        $uid = $this->getUserAgoraUid($userId);
        
        $token = RtcTokenBuilder::buildTokenWithUid(
            $this->appId,
            $this->appCertificate,
            $channelName,
            $uid,  // Use actual UID
            RtcTokenBuilder::ROLE_PUBLISHER,
            time() + 3600
        );
        
        Log::debug('Token generated', [
            'channel' => $channelName,
            'uid' => $uid,
            'user_id' => $userId
        ]);
        
        return ['token' => $token, 'uid' => $uid];
    }
    
    private function getUserAgoraUid($userId) 
    {
        // Convert "USR_12345" to 12345
        return (int) str_replace('USR_', '', $userId);
    }
}
```

**Update endpoints to return UID:**

```php
// POST /calls/initiate
$result = $agoraService->generateToken($channelName, $callerId);
return [
    'agora_token' => $result['token'],
    'agora_uid' => $result['uid'],  // ✅ Add this field
    'channel_name' => $channelName
];

// GET /calls/incoming + POST /calls/accept
$result = $agoraService->generateToken($channelName, $receiverId);
return [
    'agora_token' => $result['token'],
    'agora_uid' => $result['uid'],  // ✅ Add this field
    'channel_name' => $channelName
];
```

---

## 🧪 How to Debug

### Add Debug Logs:

```php
Log::debug('===== AGORA TOKEN GENERATION =====');
Log::debug('Channel: ' . $channelName);
Log::debug('UID: ' . $uid);
Log::debug('UID Type: ' . gettype($uid));
Log::debug('User ID: ' . $userId);
Log::debug('Token Length: ' . strlen($token));
Log::debug('Token (first 20): ' . substr($token, 0, 20));
Log::debug('==================================');
```

### Test Flow:

1. **User A initiates call** → Check logs: What UID was used?
2. **User B fetches incoming calls** → Check logs: What UID was used?
3. **User B accepts call** → Check logs: What UID was used?

**Share all 3 sets of logs.**

---

## 📋 Action Items

### Immediate:

- [ ] Find token generation code
- [ ] Check what UID value you're passing
- [ ] Add debug logs
- [ ] Make test call and share logs

### To Fix:

- [ ] Change UID to 0 in all token generation
- [ ] Generate separate tokens for caller and receiver
- [ ] Update API responses to include `agora_uid` field
- [ ] Test end-to-end call flow

---

## 🔗 Agora Documentation

**Token Generation:**
- Must specify UID when generating token
- UID in token MUST match UID when joining
- Docs: https://docs.agora.io/en/video-calling/develop/authentication-workflow

**Error 110:**
- Means: "Token authentication failed"
- Common cause: UID mismatch
- Docs: https://docs.agora.io/en/video-calling/reference/error-codes#110

---

## 📞 What We Need

Please share:

1. **Your token generation code** (the actual PHP/Node.js code)
2. **What UID values** you're using (0, user ID, null, etc.)
3. **Debug logs** from a test call showing:
   - UID used for caller token
   - UID used for receiver token
   - Whether you're reusing caller's token or generating new one

---

## ✅ Expected Result After Fix

Android logs should show:

```
✅ Receiver joins with UID = 0
✅ Token validated successfully
✅ Remote user joined
✅ Call connected!
```

**No more Error 110!** 🎉

---

**Contact:** Reply with your token generation code and debug logs.




