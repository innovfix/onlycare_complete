# 🔍 Agora Token UID Mismatch - Root Cause Analysis

**Date:** November 22, 2025  
**Status:** ✅ ROOT CAUSE IDENTIFIED  
**Priority:** HIGH

---

## 📋 Executive Summary

**ROOT CAUSE FOUND:** Your backend generates Agora tokens with **UID = 0** hardcoded, but the Android app is likely joining with a different UID (possibly the user's actual ID). This causes Agora Error 110 to fire immediately.

---

## 🔍 Findings - Answers to Your Questions

### ❓ Question 1: What UID are you using when generating tokens?

**Answer:** `$uid = 0` (HARDCODED)

**Location:** `/app/Http/Controllers/Api/CallController.php` Line 876

```php
private function generateAgoraToken($callId)
{
    $appId = config('services.agora.app_id', env('AGORA_APP_ID'));
    $appCertificate = config('services.agora.app_certificate', env('AGORA_APP_CERTIFICATE'));
    $channelName = 'call_' . $callId;
    $uid = 0; // ⚠️ HARDCODED UID = 0
    
    // ... rest of code
    
    $token = AgoraTokenBuilder::buildTokenWithDefault(
        $appId,
        $appCertificate,
        $channelName,
        $uid  // ⚠️ Always passing 0
    );
}
```

---

### ❓ Question 2: Are caller and receiver getting DIFFERENT tokens?

**Answer:** NO, they get the **SAME** token

**How it works:**

1. **POST /calls/initiate** (Caller):
   - Generate token with UID=0
   - **Save to database:** `agora_token` field
   - Return token to caller

2. **GET /calls/incoming** (Receiver):
   - **Retrieve SAME token from database**
   - Return to receiver

3. **POST /calls/accept** (Receiver):
   - **Retrieve SAME token from database**
   - Return to receiver

**Location:** `/app/Http/Controllers/Api/CallController.php`

```php
// Line 239: Generate token once
$agoraToken = $this->generateAgoraToken($callId);

// Line 258: Save to database
$call = Call::create([
    'agora_token' => $agoraToken,  // ✅ Saved
    'channel_name' => $channelName
]);

// Line 339: GET /calls/incoming - retrieve from DB
$agoraToken = $call->agora_token;  // ⚠️ Same token

// Line 443: POST /calls/accept - retrieve from DB
$agoraToken = $call->agora_token;  // ⚠️ Same token
```

---

### ❓ Question 3: What does POST /calls/initiate return?

**Current Response:**

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

**Details:**
- UID used to generate: **0**
- Token is for: **Both caller AND receiver** (same token)
- Missing field: ❌ `agora_uid` (not included in response)

---

### ❓ Question 4: What does GET /calls/incoming return?

**Current Response:**

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

**Details:**
- UID used to generate: **0** (same token from database)
- This is the **SAME token** from `/calls/initiate`
- Missing field: ❌ `agora_uid` (not included in response)

---

## 🐛 The Problem

### Current Implementation

```
Backend:
  ├─ Generates token with UID = 0
  ├─ Saves token to database
  └─ Returns same token to both users

Android App (probably):
  ├─ Caller joins with UID = callerUserId (e.g., 1001)
  └─ Receiver joins with UID = receiverUserId (e.g., 1002)

Result: ❌ ERROR 110 (Token/UID mismatch)
```

### Why Error 110 Fires Immediately

When you call `rtcEngine.joinChannel(token, channel, uid)`:

1. Agora SDK extracts the UID from the token (UID=0)
2. Agora SDK compares it with the UID you're trying to join with
3. **If they don't match:** Immediate Error 110 (within 150ms)
4. **If they match:** Connection proceeds normally

---

## 📊 Database Schema Status

### Current Schema (Missing UID fields)

```sql
-- Current calls table
CREATE TABLE calls (
    id VARCHAR(50) PRIMARY KEY,
    caller_id VARCHAR(50),
    receiver_id VARCHAR(50),
    agora_token TEXT,         -- ✅ Exists
    channel_name VARCHAR(100), -- ✅ Exists
    -- ❌ MISSING: caller_agora_uid
    -- ❌ MISSING: receiver_agora_uid
    ...
);
```

---

## ✅ Recommended Solutions

### Option A: Use UID = 0 for Everyone (Simplest)

**Pros:**
- Minimal backend changes
- One token works for both users
- Simple to implement

**Cons:**
- Less secure
- Can't track individual users in Agora
- Harder to debug connection issues

**Implementation:**

1. **Backend:** Keep current implementation (UID=0)
2. **Android:** Change app to join with UID=0

```kotlin
// Android app change
rtcEngine.joinChannel(
    token = agoraToken,
    channelId = channelName,
    uid = 0  // ⚠️ MUST be 0 to match token
)
```

3. **API Response:** Add `agora_uid` field

```php
// CallController.php - Line 289
return response()->json([
    'success' => true,
    'call' => [
        'id' => $call->id,
        'agora_token' => $agoraToken,
        'channel_name' => $channelName,
        'agora_uid' => 0,  // ✅ ADD THIS
        // ... rest
    ]
]);
```

---

### Option B: Use Unique UID for Each User (Recommended)

**Pros:**
- Better security
- Can track users individually in Agora
- Can revoke tokens per user
- Better debugging

**Cons:**
- Requires database migration
- More complex implementation
- Need to generate separate tokens

**Implementation:**

See "IMPLEMENTATION_PLAN.md" section below.

---

## 🛠️ What Needs to Change (Option B)

### 1. Database Migration

```sql
-- Add UID fields to calls table
ALTER TABLE calls 
ADD COLUMN caller_agora_uid INTEGER AFTER channel_name,
ADD COLUMN receiver_agora_uid INTEGER AFTER caller_agora_uid;

-- Add index for performance
CREATE INDEX idx_calls_agora_uids ON calls(caller_agora_uid, receiver_agora_uid);
```

### 2. Generate Separate Tokens

```php
// Instead of one token with UID=0
$callerUid = $this->getUserAgoraUid($call->caller_id);
$callerToken = $this->generateAgoraToken($callId, $callerUid);

$receiverUid = $this->getUserAgoraUid($call->receiver_id);
$receiverToken = $this->generateAgoraToken($callId, $receiverUid);

// Save both
$call->caller_agora_uid = $callerUid;
$call->caller_agora_token = $callerToken;
$call->receiver_agora_uid = $receiverUid;
$call->receiver_agora_token = $receiverToken;
```

### 3. Update API Responses

**POST /calls/initiate:**

```json
{
  "agora_token": "...",
  "channel_name": "call_CALL_123",
  "agora_uid": 1001  // ✅ ADD THIS
}
```

**GET /calls/incoming:**

```json
{
  "agora_token": "...",
  "channel_name": "call_CALL_123",
  "agora_uid": 1002  // ✅ ADD THIS (different from caller)
}
```

---

## 🧪 Testing Steps

### Step 1: Verify Current Token Generation

Add these logs to `generateAgoraToken()`:

```php
Log::debug('=== AGORA TOKEN GENERATION ===');
Log::debug('Call ID: ' . $callId);
Log::debug('Channel Name: ' . $channelName);
Log::debug('UID: ' . $uid);  // Will show 0
Log::debug('App ID: ' . $appId);
Log::debug('Certificate Length: ' . strlen($appCertificate));
Log::debug('Token Length: ' . strlen($token));
Log::debug('Token (first 20 chars): ' . substr($token, 0, 20));
Log::debug('================================');
```

### Step 2: Check Android Logs

Check what UID the Android app is using:

```kotlin
Log.d("Agora", "Joining channel with:")
Log.d("Agora", "  - Token: ${agoraToken.substring(0, 20)}...")
Log.d("Agora", "  - Channel: $channelName")
Log.d("Agora", "  - UID: $uid")  // ⚠️ What is this value?
```

### Step 3: Test Call Flow

1. User A calls User B
2. Check backend logs for token generation
3. Check Android logs for join channel
4. **Compare UIDs:** Backend vs Android

---

## 🎯 Quick Fix (Option A)

### Backend Change (Add `agora_uid` to response)

```php
// File: app/Http/Controllers/Api/CallController.php

// Line 289: POST /calls/initiate response
return response()->json([
    'success' => true,
    'call' => [
        'id' => $call->id,
        'agora_token' => $agoraToken,
        'channel_name' => $channelName,
        'agora_uid' => 0,  // ✅ ADD THIS
        'balance_time' => $balanceTime
    ]
]);

// Line 373: GET /calls/incoming response
return [
    'id' => $call->id,
    'agora_token' => $agoraToken,
    'channel_name' => $channelName,
    'agora_uid' => 0,  // ✅ ADD THIS
    // ... rest
];

// Line 460: POST /calls/accept response
return response()->json([
    'success' => true,
    'call' => [
        'id' => $call->id,
        'agora_token' => $agoraToken,
        'channel_name' => $channelName,
        'agora_uid' => 0,  // ✅ ADD THIS
    ]
]);
```

### Android Change (Use UID from API)

```kotlin
// When joining channel
val agoraUid = response.agora_uid  // Get from API
rtcEngine.joinChannel(
    token = response.agora_token,
    channelId = response.channel_name,
    uid = agoraUid  // ✅ Use UID from API (0)
)
```

---

## 📝 Summary

| Question | Answer |
|----------|--------|
| What UID is backend using? | **0** (hardcoded) |
| Are tokens different? | **No** (same token from database) |
| What's missing? | `agora_uid` field in API responses |
| Recommended fix? | Add `agora_uid: 0` to all API responses |
| Long-term solution? | Generate unique UIDs per user |

---

## 🚀 Next Steps

1. **Immediate Fix (Option A):**
   - Add `agora_uid: 0` to API responses
   - Update Android app to use `agora_uid` from API
   - Test call flow

2. **Long-term Fix (Option B):**
   - Implement unique UID system
   - Add database migration
   - Generate separate tokens
   - See IMPLEMENTATION_PLAN.md

---

## 📞 Contact

If you need help implementing either solution, let me know which option you prefer and I can provide the complete code changes!

