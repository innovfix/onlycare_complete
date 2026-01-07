# ✅ Android Call Fix - Quick Checklist

**Copy-paste this code and you're done!**

**🎉 UPDATE:** Backend now returns `agora_uid` field! Use it instead of hardcoding UID=0.

---

## 🎯 3 Files to Update

### 1️⃣ ApiService.kt

**Add this interface:**

```kotlin
@POST("calls/{callId}/accept")
suspend fun acceptCall(
    @Path("callId") callId: String
): Response<CallAcceptResponse>

// Data classes
data class CallAcceptResponse(
    val success: Boolean,
    val message: String,
    val call: CallData
)

data class CallData(
    val id: String,
    val status: String,
    val started_at: String,
    val agora_app_id: String,
    val agora_token: String,
    val agora_uid: Int,      // ✅ NEW: Use this when joining Agora
    val channel_name: String
)
```

---

### 2️⃣ IncomingCallActivity.kt

**Replace accept button code with this:**

```kotlin
acceptButton.setOnClickListener {
    Log.d(TAG, "📞 User tapped Accept")
    
    acceptButton.isEnabled = false
    rejectButton.isEnabled = false
    stopRingtone()
    
    lifecycleScope.launch {
        try {
            val response = apiService.acceptCall(callId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val callData = response.body()?.call
                
                Log.d(TAG, "✅ Call accepted")
                
                // Navigate to call screen
                val intent = Intent(this@IncomingCallActivity, OngoingCallActivity::class.java)
                intent.putExtra("CALL_ID", callId)
                intent.putExtra("CALLER_ID", callerId)
                intent.putExtra("CALLER_NAME", callerName)
                intent.putExtra("CALL_TYPE", callType)
                intent.putExtra("IS_CALLER", false)
                intent.putExtra("AGORA_APP_ID", callData?.agora_app_id ?: agoraAppId)
                intent.putExtra("AGORA_TOKEN", callData?.agora_token ?: "")
                intent.putExtra("AGORA_UID", callData?.agora_uid ?: 0)
                intent.putExtra("CHANNEL_NAME", callData?.channel_name ?: "")
                
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@IncomingCallActivity, "Failed", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this@IncomingCallActivity, "Error", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
```

---

### 3️⃣ OngoingCallActivity.kt

**Fix the joinChannel function:**

```kotlin
private fun joinAgoraChannel(agoraToken: String?, channelName: String, agoraUid: Int) {
    // ✅ CRITICAL FIX: Convert empty token to null
    val token = if (agoraToken.isNullOrEmpty()) null else agoraToken
    
    Log.d(TAG, "Joining channel: $channelName")
    Log.d(TAG, "Token: ${if (token == null) "null (unsecure mode)" else "provided"}")
    Log.d(TAG, "UID: $agoraUid")  // ✅ Use UID from API response
    
    val result = rtcEngine.joinChannel(
        token,        // null for unsecure, token for secure
        channelName,
        null,
        agoraUid      // ✅ Use UID from API (will be 0)
    )
    
    Log.d(TAG, "Join result: $result")
}
```

---

## 🧪 Test It

1. User A calls User B
2. User B should see incoming call screen
3. User B taps Accept
4. Check logs for:
   - `📞 User tapped Accept`
   - `✅ Call accepted`
   - `Joining channel: call_CALL_...`
   - `Join result: 0`
5. Both users should be in call ✅

---

## ⚠️ Two Critical Fixes

### Fix 1: Empty Token → Null
```kotlin
// ❌ WRONG
rtcEngine.joinChannel(agoraToken, channelName, 0)

// ✅ CORRECT
val token = if (agoraToken.isNullOrEmpty()) null else agoraToken
rtcEngine.joinChannel(token, channelName, null, 0)
```

### Fix 2: UID Must Be 0
```kotlin
// ❌ WRONG
rtcEngine.joinChannel(token, channelName, null, userId.toInt())

// ✅ CORRECT
rtcEngine.joinChannel(token, channelName, null, 0)
```

---

## 📊 Expected Logs

```
📞 User tapped Accept
✅ Call accepted
Joining channel: call_CALL_17638785178845
Token: null (unsecure mode)
UID: 0
Join result: 0
✅ JOIN SUCCESS
✅ REMOTE USER JOINED
```

---

## 🚨 If Still Not Working

### Check These:

1. **API Base URL correct?**
   - Should be: `https://onlycare.in/api/v1/`

2. **Authorization header included?**
   - `Authorization: Bearer {token}`

3. **UID is 0?**
   - NOT user ID, MUST be 0

4. **Token converted to null?**
   - Empty string → `null`

---

## 📞 Backend API Details

**Endpoint:** `POST /api/v1/calls/{callId}/accept`

**Example callId:** `CALL_17638785178845`

**Response:**
```json
{
  "success": true,
  "message": "Call accepted",
  "call": {
    "id": "CALL_17638785178845",
    "status": "ONGOING",
    "started_at": "2025-11-23T06:15:23Z",
    "agora_app_id": "63783c2ad2724b839b1e58714bfc2629",
    "agora_token": "",
    "agora_uid": 0,
    "channel_name": "call_CALL_17638785178845"
  }
}
```

**Notes:**
- `agora_token` is empty - this is correct for testing!
- `agora_uid` tells you which UID to use (0 for now)

---

## ✅ Checklist

- [ ] Updated `ApiService.kt` with accept endpoint
- [ ] Fixed accept button in `IncomingCallActivity.kt`
- [ ] Fixed token handling in `OngoingCallActivity.kt`
- [ ] Changed UID to 0 (not user ID)
- [ ] Tested call between two devices
- [ ] Verified logs show successful join
- [ ] Confirmed audio works between users

---

## 🎯 Summary

**3 Changes:**
1. Add accept API call
2. Convert empty token to null
3. Use UID = 0

**Time:** 30 minutes  
**Difficulty:** Easy  
**Impact:** Enables all calls to work ✅

---

**That's it! Copy these 3 code blocks and calls will work! 🚀**

