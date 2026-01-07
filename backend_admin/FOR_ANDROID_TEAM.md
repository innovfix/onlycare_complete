# 📱 For Android Development Team - Agora Error 110 Fix

**Date:** November 22, 2025  
**Priority:** 🚨 HIGH - Calls Failing  
**Issue:** Error 110 immediately after receiver joins Agora channel

---

## 🐛 The Problem

When User B (receiver) accepts a call and joins the Agora channel, Error 110 fires **immediately** (within 150ms), preventing the call from connecting.

### Your Logs:
```
03:06:16.461 - ✅ Receiver joins Agora channel (result: 0)
03:06:16.607 - ❌ ERROR 110: ERR_OPEN_CHANNEL_TIMEOUT
03:06:16.624 - ❌ ERROR 110 (repeated)
```

---

## 🔍 Root Cause (Backend Investigation Complete)

We analyzed the backend and found:

### What Backend Does:
1. **Generates Agora token with UID = 0** (hardcoded)
2. Both caller and receiver get the **SAME token**
3. Token is valid for **UID = 0 only**

### Why Error 110 Happens:
Agora Error 110 fires immediately when **token UID doesn't match join UID**.

```
Backend generates token FOR: UID = 0
Android app joins WITH: UID = ??? (probably NOT 0)
Result: Immediate Error 110 ❌
```

---

## ❓ Questions for Android Team

### 🔴 CRITICAL: What UID is your app using when joining Agora?

**Please check your code where you call `rtcEngine.joinChannel()`:**

```kotlin
// Where is this code in your app?
rtcEngine.joinChannel(
    token = response.agora_token,
    channelId = response.channel_name,
    uid = ???  // ⚠️ WHAT VALUE IS HERE?
)
```

**Possible values you might be using:**

#### Option 1: Using 0 (Correct for current backend)
```kotlin
uid = 0  // ✅ This would work
```

#### Option 2: Using user ID (WRONG - causes Error 110)
```kotlin
uid = userId.toInt()  // ❌ This causes Error 110
// Example: uid = 17637424324851
```

#### Option 3: Using random UID (WRONG - causes Error 110)
```kotlin
uid = Random.nextInt()  // ❌ This causes Error 110
```

#### Option 4: Using empty/null (might work)
```kotlin
uid = 0  // Agora SDK default
```

---

## 📝 Please Share With Us

### 1. Your Current `joinChannel()` Code

**Please share the code snippet where you join the Agora channel:**

```kotlin
// For CALLER (after initiateCall API)
val response = apiService.initiateCall(receiverId, callType)
rtcEngine.joinChannel(
    ???,  // token
    ???,  // channel
    uid = ???  // ⚠️ WHAT IS THIS?
)

// For RECEIVER (after accept API)
val response = apiService.acceptCall(callId)
rtcEngine.joinChannel(
    ???,  // token
    ???,  // channel
    uid = ???  // ⚠️ WHAT IS THIS?
)
```

### 2. Your Debug Logs

**Please add this log and share the output:**

```kotlin
Log.d("AgoraDebug", "=== JOINING AGORA CHANNEL ===")
Log.d("AgoraDebug", "Token: ${agoraToken.substring(0, 20)}...")
Log.d("AgoraDebug", "Channel: $channelName")
Log.d("AgoraDebug", "UID: $uid")  // ⚠️ THIS IS THE KEY VALUE
Log.d("AgoraDebug", "================================")

val result = rtcEngine.joinChannel(agoraToken, channelName, uid)
Log.d("AgoraDebug", "Join result: $result")
```

### 3. Your API Response Models

**Please share your data classes:**

```kotlin
data class CallResponse(
    val success: Boolean,
    val call: CallData,
    val agora_token: String,
    val channel_name: String,
    val agora_uid: Int?,  // ⚠️ Do you have this field?
    // ... other fields
)
```

---

## ✅ The Fix (Two Options)

### Option A: Quick Fix - Use UID from Backend

**Backend team will add `agora_uid` field to API responses:**

```json
{
  "agora_token": "007...",
  "channel_name": "call_CALL_123",
  "agora_uid": 0  // ← Backend will add this field
}
```

**You update your app to use it:**

```kotlin
// OLD CODE (Wrong - causes Error 110)
rtcEngine.joinChannel(
    response.agora_token,
    response.channel_name,
    uid = userId.toInt()  // ❌ Wrong UID
)

// NEW CODE (Correct)
rtcEngine.joinChannel(
    response.agora_token,
    response.channel_name,
    uid = response.agora_uid  // ✅ Use UID from backend (will be 0)
)
```

**Time to fix:** 15 minutes (after backend deploys)

---

### Option B: Always Use UID = 0 (If Backend Not Ready)

**If you want to test immediately without waiting for backend:**

```kotlin
// Force UID = 0 for testing
rtcEngine.joinChannel(
    response.agora_token,
    response.channel_name,
    uid = 0  // ✅ Hardcode 0 for testing
)
```

**This should work immediately if backend tokens are generated with UID=0**

---

## 🧪 Testing Steps

### Step 1: Add Debug Logs

```kotlin
private fun joinAgoraChannel(token: String, channel: String, uid: Int) {
    Log.d("AgoraTest", "========================================")
    Log.d("AgoraTest", "JOINING AGORA CHANNEL")
    Log.d("AgoraTest", "Token: ${token.take(30)}...")
    Log.d("AgoraTest", "Token length: ${token.length}")
    Log.d("AgoraTest", "Channel: $channel")
    Log.d("AgoraTest", "UID: $uid")  // ⚠️ KEY VALUE
    Log.d("AgoraTest", "========================================")
    
    val result = rtcEngine.joinChannel(token, channel, uid)
    
    Log.d("AgoraTest", "Join channel result: $result")
    Log.d("AgoraTest", "Waiting for connection...")
}

// In your Agora event handlers
override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
    Log.d("AgoraTest", "✅ SUCCESS: Joined channel $channel with UID $uid")
}

override fun onError(err: Int) {
    Log.e("AgoraTest", "❌ ERROR $err: ${getErrorMessage(err)}")
    if (err == 110) {
        Log.e("AgoraTest", "ERROR 110 means: Token UID doesn't match join UID")
    }
}
```

### Step 2: Make Test Call

1. User A calls User B
2. Check logs for UID value
3. User B accepts
4. Check logs for UID value
5. Compare: Do they match?

### Step 3: Try Different UID Values

**Test 1: UID = 0**
```kotlin
uid = 0
```
Result: Should work ✅

**Test 2: UID = user ID**
```kotlin
uid = userId.toInt()
```
Result: Will fail with Error 110 ❌

**Test 3: UID from API**
```kotlin
uid = response.agora_uid  // (if backend adds this field)
```
Result: Should work ✅

---

## 📊 Expected Results

### Current (Wrong UID):
```
12:34:56.100 D/AgoraTest: UID: 17637424324851
12:34:56.150 D/AgoraTest: Join channel result: 0
12:34:56.300 E/AgoraTest: ❌ ERROR 110
```

### After Fix (Correct UID):
```
12:34:56.100 D/AgoraTest: UID: 0
12:34:56.150 D/AgoraTest: Join channel result: 0
12:34:56.300 D/AgoraTest: ✅ SUCCESS: Joined channel
12:34:56.500 D/AgoraTest: ✅ Remote user joined
```

---

## 🎯 Action Items for Android Team

### Immediate Actions:
- [ ] Check your `joinChannel()` code - what UID are you using?
- [ ] Add debug logs showing the UID value
- [ ] Share logs with backend team
- [ ] Test with UID = 0 (hardcoded for now)

### After Backend Deploys:
- [ ] Update API response models to include `agora_uid: Int`
- [ ] Update `joinChannel()` to use `response.agora_uid`
- [ ] Test call flow end-to-end
- [ ] Verify Error 110 is resolved

---

## 📞 Backend API Changes (Coming Soon)

Backend will update these 3 endpoints to include `agora_uid`:

### 1. POST /api/v1/calls/initiate
```json
{
  "success": true,
  "call": {
    "agora_token": "007...",
    "channel_name": "call_CALL_123",
    "agora_uid": 0  // ← NEW FIELD
  }
}
```

### 2. GET /api/v1/calls/incoming
```json
{
  "success": true,
  "data": [{
    "agora_token": "007...",
    "channel_name": "call_CALL_123",
    "agora_uid": 0  // ← NEW FIELD
  }]
}
```

### 3. POST /api/v1/calls/{id}/accept
```json
{
  "success": true,
  "call": {
    "agora_token": "007...",
    "channel_name": "call_CALL_123",
    "agora_uid": 0  // ← NEW FIELD
  }
}
```

---

## 🔗 Agora Documentation

**Error 110: ERR_OPEN_CHANNEL_TIMEOUT**
- Docs: https://docs.agora.io/en/video-calling/reference/error-codes#110
- Cause: Token authentication failed (UID mismatch)
- Fix: Use same UID in token generation and channel join

**Token Authentication:**
- Docs: https://docs.agora.io/en/video-calling/develop/authentication-workflow
- Key Rule: "UID in token MUST match UID in joinChannel()"

---

## ✅ Quick Checklist

### To Fix Error 110:
1. ✅ Check what UID you're using in `joinChannel()`
2. ✅ Try changing it to `0`
3. ✅ Add `agora_uid` field to response models
4. ✅ Use `response.agora_uid` in `joinChannel()`
5. ✅ Test call flow
6. ✅ Verify no Error 110

---

## 💬 Questions?

**If you need help:**
1. Share your `joinChannel()` code snippet
2. Share debug logs showing UID value
3. Share API response examples
4. We'll help you fix it!

**Bottom line:** The token is valid for UID=0, so your app must join with UID=0. If you're using a different UID, that's why Error 110 happens immediately.

---

**Let's get this fixed! 🚀**

