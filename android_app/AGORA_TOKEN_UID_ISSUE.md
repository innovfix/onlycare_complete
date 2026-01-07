# 🚨 URGENT: Agora Token UID Mismatch Issue

## 📊 Current Status

**Date:** November 22, 2025  
**Priority:** HIGH  
**Issue:** Receiver gets Agora Error 110 immediately after accepting call  
**Impact:** Calls fail to connect even though receiver accepts

---

## ✅ What's Working

1. ✅ Call initiation API (`POST /calls/initiate`)
2. ✅ Incoming calls API (`GET /calls/incoming`) - returns `agora_token` and `channel_name`
3. ✅ Accept call API (`POST /calls/{id}/accept`)
4. ✅ Tokens are being generated and returned
5. ✅ Android app receives tokens correctly

---

## ❌ The Problem

### What We're Seeing in Android Logs:

```
03:06:16.461 - ✅ Receiver joins Agora channel (result: 0)
03:06:16.607 - ❌ ERROR 110: ERR_OPEN_CHANNEL_TIMEOUT
03:06:16.624 - ❌ ERROR 110 (repeated)
03:06:16.635 - ❌ ERROR 110 (repeated)
```

**Key Observation:** Error 110 fires **immediately** (within 150ms) after joining, not after a normal timeout period (5-10 seconds).

### Root Cause: Token/UID Mismatch

Agora Error 110 firing immediately indicates the token is **invalid for the UID being used**.

---

## 📚 How Agora Tokens Work

### Key Concept: UID Must Match

When generating an Agora token, you must specify a **UID (User ID)**:

```
Token = generateToken(channelName, UID, role, privileges)
```

**Critical Rule:** The UID used to **generate the token** must **exactly match** the UID used when **joining the channel**.

### Example of Correct Flow:

```
Backend generates token FOR UID 1002:
  Token = generateToken("call_CALL_123", uid=1002, ...)

Android app joins WITH UID 1002:
  rtcEngine.joinChannel(token, channel, uid=1002)

✅ SUCCESS - UIDs match
```

### Example of WRONG Flow (Causes Error 110):

```
Backend generates token FOR UID 1002:
  Token = generateToken("call_CALL_123", uid=1002, ...)

Android app joins WITH UID 0:
  rtcEngine.joinChannel(token, channel, uid=0)

❌ ERROR 110 - UIDs don't match!
```

---

## 🔍 What We Need You to Check

### 1. Check Token Generation Code

**Find your Agora token generation code and check:**

#### Question 1: What UID are you using when generating tokens?

```php
// Are you doing this?
$token = $agoraService->generateToken($channelName, 0);  // UID = 0?

// Or this?
$token = $agoraService->generateToken($channelName, $userId);  // UID = actual user ID?

// Or this?
$token = $agoraService->generateToken($channelName, null);  // UID = null?
```

**Please check and tell us:** What value are you passing as UID when generating tokens?

---

#### Question 2: Are caller and receiver getting DIFFERENT tokens?

**Scenario:** User A calls User B

```php
// ❌ WRONG - Same token for both users
$call->agora_token = generateToken($channel, 0);
// Caller gets this token
// Receiver gets this SAME token

// ✅ CORRECT - Different tokens for each user
$callerToken = generateToken($channel, $callerId);
$receiverToken = generateToken($channel, $receiverId);
```

**Please check:** When User A calls User B:
- Does the caller get one token?
- Does the receiver get a **different** token?
- Or are they both using the **same** token?

---

#### Question 3: What does your `POST /calls/initiate` return?

**Current response we're getting:**

```json
{
  "success": true,
  "call": {
    "id": "CALL_17637609673605",
    "agora_token": "0078b5e9417f15a48ae9...",
    "channel_name": "call_CALL_17637609673605"
  }
}
```

**Questions:**
1. What UID did you use to generate this `agora_token`?
2. Is this token meant for the **caller** or the **receiver**?

---

#### Question 4: What does your `GET /calls/incoming` return?

**Current response we're getting:**

```json
{
  "success": true,
  "data": [{
    "id": "CALL_17637609673605",
    "caller_id": "USR_17637424324851",
    "agora_token": "0078b5e9417f15a48ae9...",
    "channel_name": "call_CALL_17637609673605"
  }]
}
```

**Questions:**
1. What UID did you use to generate this `agora_token`?
2. Is this a **new token for the receiver**, or is it the **same token** from `/calls/initiate`?

---

## ✅ Recommended Solution

### Option A: Use UID = 0 for Everyone (Simplest)

If you want the simplest solution, generate all tokens with **UID = 0**:

```php
// In POST /calls/initiate
$token = $agoraService->generateToken($channelName, 0);

// In GET /calls/incoming
$token = $agoraService->generateToken($channelName, 0);

// Both caller and receiver can use UID = 0
```

**Requirements:**
- Token MUST be generated with `uid = 0`
- Token MUST have `rolePublisher` (not `roleSubscriber`)
- Token expiration should be at least 1 hour

---

### Option B: Use Unique UID for Each User (Recommended)

For better security and tracking, assign each user a unique UID:

```php
// In POST /calls/initiate (Caller token)
$callerUid = $this->getUserAgoraUid($callerId);  // e.g., 1001
$callerToken = $agoraService->generateToken($channelName, $callerUid);

// Save to database
$call->caller_agora_uid = $callerUid;
$call->caller_agora_token = $callerToken;

// In GET /calls/incoming (Receiver token)
$receiverUid = $this->getUserAgoraUid($receiverId);  // e.g., 1002
$receiverToken = $agoraService->generateToken($channelName, $receiverUid);

// Save to database
$call->receiver_agora_uid = $receiverUid;
$call->receiver_agora_token = $receiverToken;

// Return receiver's token (NOT caller's token)
return [
    'agora_token' => $receiverToken,  // Receiver's unique token
    'agora_uid' => $receiverUid,      // ✅ ADD THIS FIELD
];
```

**Benefits:**
- Each user has their own token
- Can revoke tokens individually
- Better security and auditing

---

## 📝 Required Changes

### 1. Add `agora_uid` to Database

```sql
ALTER TABLE calls 
ADD COLUMN caller_agora_uid INTEGER,
ADD COLUMN receiver_agora_uid INTEGER;
```

### 2. Update API Response (GET /calls/incoming)

**Current response:**
```json
{
  "agora_token": "0078b5e9417f15a48ae9...",
  "channel_name": "call_CALL_17637609673605"
}
```

**New response (add `agora_uid`):**
```json
{
  "agora_token": "0078b5e9417f15a48ae9...",
  "channel_name": "call_CALL_17637609673605",
  "agora_uid": 1002  // ✅ ADD THIS - The UID this token was generated for
}
```

### 3. Update API Response (POST /calls/initiate)

**Current response:**
```json
{
  "agora_token": "0078b5e9417f15a48ae9...",
  "channel_name": "call_CALL_17637609673605"
}
```

**New response (add `agora_uid`):**
```json
{
  "agora_token": "0078b5e9417f15a48ae9...",
  "channel_name": "call_CALL_17637609673605",
  "agora_uid": 1001  // ✅ ADD THIS - The UID this token was generated for
}
```

---

## 🧪 Testing & Verification

### Step 1: Add Debug Logs

Please add these logs to your token generation code:

```php
Log::debug('=== AGORA TOKEN GENERATION ===');
Log::debug('Channel Name: ' . $channelName);
Log::debug('UID: ' . $uid);
Log::debug('Role: ' . $role);
Log::debug('Token Length: ' . strlen($token));
Log::debug('Token (first 20 chars): ' . substr($token, 0, 20));
Log::debug('================================');
```

### Step 2: Test Call Flow

1. **User A initiates call** → `POST /calls/initiate`
   - Check backend logs: What UID was used?
   - Save the response

2. **User B sees incoming call** → `GET /calls/incoming`
   - Check backend logs: What UID was used?
   - Save the response

3. **Compare tokens:**
   - Are they the same or different?
   - Were they generated with the same UID or different UIDs?

### Step 3: Share Logs with Us

Please share:
1. Backend logs showing token generation
2. API responses for both endpoints
3. What UID values you're using

---

## 📋 Quick Checklist

Please answer these questions:

- [ ] What UID are you passing when generating Agora tokens? (0, null, user ID, random number?)
- [ ] Are caller and receiver getting the **same** token or **different** tokens?
- [ ] What Agora SDK/library are you using for token generation?
- [ ] Can you add `agora_uid` field to API responses?
- [ ] Can you share your token generation code snippet?

---

## 🔗 Agora Documentation References

- [Agora Token Authentication](https://docs.agora.io/en/video-calling/develop/authentication-workflow)
- [Token Generation Best Practices](https://docs.agora.io/en/video-calling/develop/token-generation)
- [Error Code 110 (ERR_OPEN_CHANNEL_TIMEOUT)](https://docs.agora.io/en/video-calling/reference/error-codes#110)

**Key Quote from Agora Docs:**
> "The user ID used to generate the token must match the user ID used when joining the channel. Otherwise, the user will be rejected by the Agora server."

---

## 📞 Contact

If you need clarification on any of the above, please let us know. We're ready to help debug this together!

**Android Team**




