# 🔍 Debug Guide: Call Not Starting After Female Accepts

**Issue:** After female accepts call, call doesn't start (no audio/video connection)

---

## 📋 Step-by-Step Debugging Checklist

### **Step 1: Check Backend Logs** ✅

**Command:**
```bash
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && tail -200 storage/logs/laravel.log | grep -E 'call_accepted|acceptCall|agora|ONGOING' | tail -50"
```

**What to Look For:**

✅ **Good Signs:**
```
[call_accepted] Female accepted call
[agora_token] Using Agora credentials from database for acceptCall
status: ONGOING
token_length: 139
```

❌ **Bad Signs:**
```
ERROR: FCM notification FAILED
ERROR: Array to string conversion
token_length: 0
status: CONNECTING (should be ONGOING)
```

---

### **Step 2: Check Android App Logs** 📱

**Command (via ADB):**
```bash
adb logcat | grep -E "acceptCall|AudioCall|VideoCall|Agora|joinChannel|onUserJoined"
```

**What to Look For:**

✅ **Good Signs:**
```
✅ Call accepted successfully
✅ Token received from backend
✅ Joining Agora channel...
✅ Joined channel successfully
✅ Remote user joined
```

❌ **Bad Signs:**
```
❌ Failed to accept call
❌ Token is empty
❌ Failed to join channel
❌ Error 110 (Token invalid)
❌ onUserJoined never fired
```

---

### **Step 3: Verify Token Generation** 🔐

**Test Token Generation:**
```bash
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && php test_token_exact_guide.php"
```

**Expected Output:**
```
✅ Token generated successfully!
Token length: 139 characters
Starts with: 006
```

**If token is wrong:**
- Check `.env` file has correct credentials
- Check `config/services.php` is correct
- Run `php artisan config:clear`

---

### **Step 4: Check API Response** 🌐

**Test Accept Call API:**
```bash
# Get auth token first, then:
curl -X POST "https://onlycare.in/api/v1/calls/CALL_123456/accept" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Call accepted",
  "call": {
    "id": "CALL_123456",
    "status": "ONGOING",
    "agora_app_id": "8b5e9417f15a48ae929783f32d3d33d4",
    "agora_token": "006abc123...",
    "agora_uid": 0,
    "channel_name": "call_CALL_123456"
  }
}
```

**Check:**
- ✅ `status` = "ONGOING" (not "CONNECTING")
- ✅ `agora_token` is present and 139+ chars
- ✅ `channel_name` matches what caller has
- ✅ `agora_uid` = 0

---

### **Step 5: Verify Android App Flow** 📱

**Check These Files:**

#### **A. Accept Call API Call**

**File:** `ApiDataRepository.kt`

**Should have:**
```kotlin
suspend fun acceptCall(callId: String): Result<CallDto> {
    // Calls: POST /api/v1/calls/{callId}/accept
    // Returns: CallDto with agora_token, channel_name, etc.
}
```

#### **B. Accept Button Handler**

**File:** `IncomingCallActivity.kt` or `FemaleHomeScreen.kt`

**Should:**
1. Call `repository.acceptCall(callId)`
2. Get token from response
3. Navigate to CallActivity with token
4. Join Agora channel

#### **C. Agora Channel Join**

**File:** `AudioCallViewModel.kt` or `VideoCallViewModel.kt`

**Should:**
```kotlin
fun initializeAndJoinCall(appId: String, token: String, channelName: String, isReceiver: Boolean) {
    // Initialize Agora
    agoraManager.initialize(appId)
    
    // Join channel with token
    agoraManager.joinChannel(
        token = token,
        channelName = channelName,
        uid = 0  // ✅ MUST BE 0
    )
}
```

---

## 🐛 Common Issues & Fixes

### **Issue 1: Token Not Received** ❌

**Symptoms:**
- Android logs show: `Token is empty`
- API response missing `agora_token`

**Fix:**
1. Check backend generates token: `generateAgoraToken()` is called
2. Check token is saved to database: `$call->agora_token`
3. Check token is returned in API response

**Verify:**
```bash
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && tail -50 storage/logs/laravel.log | grep 'agora_token.*acceptCall'"
```

---

### **Issue 2: UID Mismatch** ❌

**Symptoms:**
- Agora Error 110 (Token invalid)
- Token generated for UID=0, but app joins with different UID

**Fix:**
- Backend generates token with: `$uid = 0`
- Android must join with: `joinChannel(token, channel, 0)` ← Must be 0

**Check Android Code:**
```kotlin
// ✅ CORRECT:
agoraManager.joinChannel(token, channelName, 0)

// ❌ WRONG:
agoraManager.joinChannel(token, channelName, userId)  // Don't use user ID!
```

---

### **Issue 3: Channel Name Mismatch** ❌

**Symptoms:**
- Token generated for: `call_CALL_123456`
- App joins with: `CALL_123456` (missing `call_` prefix)

**Fix:**
- Backend generates token for: `call_CALL_123456`
- Android must join with: `call_CALL_123456` (exact match)

**Check:**
```kotlin
// Backend: channelName = 'call_' . $callId
// Android: Must use exact same channelName from API response
val channelName = response.call?.channelName ?: response.channelName
agoraManager.joinChannel(token, channelName, 0)
```

---

### **Issue 4: Call Status Not Updated** ❌

**Symptoms:**
- Backend shows status = "CONNECTING" (should be "ONGOING")
- Call never transitions to active state

**Fix:**
- Check `acceptCall()` method updates status:
```php
$call->update([
    'status' => 'ONGOING',  // ✅ Must be ONGOING
    'started_at' => now()
]);
```

**Verify:**
```bash
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && tail -50 storage/logs/laravel.log | grep 'status.*ONGOING'"
```

---

### **Issue 5: onUserJoined Not Firing** ❌

**Symptoms:**
- Receiver joins channel successfully
- But `onUserJoined()` callback never fires
- UI stuck on ringing screen

**Root Cause:**
- Caller joins FIRST (waiting for receiver)
- Receiver joins AFTER caller is already in channel
- Agora's `onUserJoined()` only fires for users who join AFTER you
- If caller was already there, callback never fires!

**Fix:**
```kotlin
// In AudioCallViewModel.kt or VideoCallViewModel.kt

override fun onJoinChannelSuccess(channel: String, uid: Int) {
    if (isReceiver) {
        // Receiver joined - check if caller is already there
        viewModelScope.launch {
            delay(1000) // Wait 1 second
            // Check if remote user is already in channel
            val remoteUsers = agoraManager.getRemoteUsers()
            if (remoteUsers.isNotEmpty()) {
                // Caller is already there!
                onUserJoined(remoteUsers.first())
            }
        }
    }
}
```

---

### **Issue 6: FCM Notification Error** ⚠️

**Symptoms:**
```
ERROR: [call_accepted] FCM notification FAILED to notify male
ERROR: Array to string conversion
```

**Impact:**
- Male user doesn't get notified that call was accepted
- Male might not join Agora channel
- Call doesn't connect

**Fix:**
- Check `notifyCallAccepted()` method
- Fix array-to-string conversion error
- Ensure FCM payload is correct format

---

## 🔍 Quick Diagnostic Commands

### **1. Check Recent Call Acceptances**
```bash
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && tail -100 storage/logs/laravel.log | grep 'call_accepted'"
```

### **2. Check Token Generation**
```bash
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && tail -100 storage/logs/laravel.log | grep 'agora_token.*acceptCall'"
```

### **3. Check Call Status Updates**
```bash
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && tail -100 storage/logs/laravel.log | grep 'status.*ONGOING'"
```

### **4. Check Database Call Status**
```bash
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && php artisan tinker --execute=\"echo App\Models\Call::latest()->first()->status;\""
```

### **5. Check Android Logs (Real-time)**
```bash
adb logcat -c  # Clear logs
# Then accept a call and watch:
adb logcat | grep -E "acceptCall|AudioCall|Agora|joinChannel|onUserJoined|Error"
```

---

## 📊 Expected Flow (When Working)

```
1. Female clicks "Accept" button
   ↓
2. Android calls: POST /api/v1/calls/{callId}/accept
   ↓
3. Backend updates: status = "ONGOING"
   ↓
4. Backend returns: { agora_token, channel_name, agora_uid: 0 }
   ↓
5. Android navigates to CallActivity
   ↓
6. Android joins Agora: joinChannel(token, channel, 0)
   ↓
7. Agora callback: onJoinChannelSuccess()
   ↓
8. Agora callback: onUserJoined(uid) ← Caller is already there!
   ↓
9. Android updates UI: remoteUserJoined = true
   ↓
10. Call screen shows: Connected, timer starts ✅
```

---

## 🎯 Most Likely Issues (Based on Logs)

### **Issue #1: FCM Error** ⚠️
```
ERROR: [call_accepted] FCM notification FAILED to notify male
ERROR: Array to string conversion
```

**Impact:** Male doesn't know call was accepted, might not join channel

**Fix:** Fix FCM notification payload format

---

### **Issue #2: onUserJoined Not Firing** ⚠️

**Impact:** Receiver joins but doesn't detect caller is already there

**Fix:** Check if remote users exist after joining, manually trigger callback

---

### **Issue #3: Token/UID/Channel Mismatch** ⚠️

**Impact:** Agora Error 110, can't join channel

**Fix:** Ensure exact match:
- Token UID = 0
- Join UID = 0
- Channel name exact match

---

## 🚀 Next Steps

1. **Run diagnostic commands** above
2. **Check Android logs** during a test call
3. **Verify token generation** is working
4. **Check API response** when accepting call
5. **Verify Agora join** parameters match token

---

**Created:** January 9, 2026  
**Status:** 🔍 Debugging Guide Ready
