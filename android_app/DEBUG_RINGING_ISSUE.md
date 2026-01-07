# 🔍 DEBUG: Both Sides Still Stuck on Ringing

## Issue
Backend says it's fixed, but both caller and receiver still see "Ringing" screen after accepting call.

---

## 🎯 STEP 1: Check Backend Response (CRITICAL!)

### Open Terminal and Test API:

```bash
# Replace TOKEN_HERE with a real female user token
curl -X GET 'https://onlycare.in/api/v1/calls/incoming' \
  -H 'Authorization: Bearer TOKEN_HERE' \
  -H 'Accept: application/json' | jq '.'
```

### ✅ What You SHOULD See:
```json
{
  "success": true,
  "data": [
    {
      "id": "CALL_123...",
      "caller_id": "USR_...",
      "caller_name": "John Doe",
      "call_type": "AUDIO",
      "status": "CONNECTING",
      "agora_token": "0078b5e9417f15a48ae...",  ← MUST BE LONG STRING!
      "channel_name": "call_CALL_123..."        ← MUST BE PRESENT!
    }
  ]
}
```

### ❌ If You See This (PROBLEM!):
```json
{
  "agora_token": null,    ← STILL NULL!
  "channel_name": null    ← STILL NULL!
}
```

**If backend is STILL returning null, the backend fix didn't work!**

---

## 🎯 STEP 2: Check Android Logs

### In Android Studio:

1. **Open Logcat** (bottom panel)
2. **Filter by**: `AudioCallScreen`
3. **Clear logs**: Click 🗑️ (trash icon)
4. **Accept a call** on receiver device
5. **Look for these logs**:

### ✅ Good Logs (Working):
```
AudioCallScreen: 🔍 Screen parameters:
AudioCallScreen:   - userId: USR_1111
AudioCallScreen:   - callId: CALL_123...
AudioCallScreen:   - token: 0078b5e9417f... (length: 143)  ← HAS LENGTH!
AudioCallScreen:   - channel: call_CALL_123...             ← PRESENT!
AudioCallScreen: ✅ All checks passed, joining call...
```

### ❌ Bad Logs (Problem!):
```
AudioCallScreen:   - token: EMPTY                          ← EMPTY!
AudioCallScreen:   - channel: EMPTY                        ← EMPTY!
AudioCallScreen: ❌ Missing credentials...
AudioCallScreen: ❌ Invalid Call Setup
```

---

## 🎯 STEP 3: Check Navigation Parameters

### Add Debug Logging

**File**: `FemaleHomeScreen.kt` (line 84-101)

Add these logs:

```kotlin
onClick = {
    // Accept the call via API, then navigate to call screen
    android.util.Log.d("FemaleHomeScreen", "========================================")
    android.util.Log.d("FemaleHomeScreen", "📞 ACCEPTING CALL")
    android.util.Log.d("FemaleHomeScreen", "Call ID: ${call.id}")
    android.util.Log.d("FemaleHomeScreen", "Caller ID: ${call.callerId}")
    android.util.Log.d("FemaleHomeScreen", "Token: ${call.agoraToken ?: "NULL"}")
    android.util.Log.d("FemaleHomeScreen", "Channel: ${call.channelName ?: "NULL"}")
    android.util.Log.d("FemaleHomeScreen", "========================================")
    
    viewModel.acceptIncomingCall(
        onSuccess = {
            android.util.Log.d("FemaleHomeScreen", "✅ Accept API succeeded")
            android.util.Log.d("FemaleHomeScreen", "Navigating with:")
            android.util.Log.d("FemaleHomeScreen", "  - token: ${call.agoraToken ?: "EMPTY"}")
            android.util.Log.d("FemaleHomeScreen", "  - channel: ${call.channelName ?: "EMPTY"}")
            
            val route = if (call.callType == "VIDEO") {
                Screen.VideoCall.createRoute(
                    userId = call.callerId,
                    callId = call.id,
                    token = call.agoraToken ?: "",
                    channel = call.channelName ?: ""
                )
            } else {
                Screen.AudioCall.createRoute(
                    userId = call.callerId,
                    callId = call.id,
                    token = call.agoraToken ?: "",
                    channel = call.channelName ?: ""
                )
            }
            
            android.util.Log.d("FemaleHomeScreen", "Route: $route")
            navController.navigate(route)
        },
        onError = { error ->
            android.util.Log.e("FemaleHomeScreen", "❌ Accept API failed: $error")
        }
    )
}
```

### Rebuild and Test:
1. Make this change
2. Build → Rebuild Project
3. Run on device
4. Accept a call
5. Check Logcat for these logs

---

## 🎯 STEP 4: Check IncomingCallDto Polling

### Add Logging to ViewModel

**File**: `FemaleHomeViewModel.kt` (around line 193)

```kotlin
if (latestCall != null) {
    Log.d("FemaleHome", "📞 INCOMING CALL from ${latestCall.callerName} (${latestCall.callType})")
    Log.d("FemaleHome", "Call ID: ${latestCall.id}")
    Log.d("FemaleHome", "Agora Token: ${if (latestCall.agoraToken.isNullOrEmpty()) "NULL/EMPTY" else "${latestCall.agoraToken.take(20)}... (${latestCall.agoraToken.length} chars)"}")
    Log.d("FemaleHome", "Channel Name: ${latestCall.channelName ?: "NULL"}")
    
    _state.update {
        it.copy(
            incomingCall = latestCall,
            hasIncomingCall = true
        )
    }
}
```

---

## 🔎 DIAGNOSIS SCENARIOS

### Scenario A: Backend Returns NULL
**Symptoms**: 
- Logs show `token: NULL`, `channel: NULL`
- Backend API test shows null values

**Solution**: Backend fix didn't work properly. Backend team needs to:
1. Check migration was actually run
2. Verify credentials are being saved when call is initiated
3. Verify credentials are being retrieved in getIncomingCalls()

---

### Scenario B: Backend Returns Data BUT App Shows Empty
**Symptoms**:
- Backend API test shows real values
- BUT app logs show EMPTY

**Possible Causes**:
1. **JSON parsing issue**: Field names don't match
2. **Caching**: Old API response cached
3. **Wrong endpoint**: App calling different endpoint

**Solutions**:
1. Check `IncomingCallDto` field names match backend exactly
2. Clear app data: Settings → Apps → Only Care → Clear Data
3. Uninstall and reinstall app

---

### Scenario C: Data is There, But Validation Fails
**Symptoms**:
- Logs show token and channel with values
- BUT still see "Missing credentials" error

**Possible Cause**: Token or channel is a weird string like "null" (string) instead of empty

**Solution**: Check the actual values in logs

---

## 🚨 QUICK FIXES TO TRY

### Fix 1: Clear App Data
```
Settings → Apps → Only Care → Clear Data
Then re-login and test
```

### Fix 2: Force Stop and Restart
```
Settings → Apps → Only Care → Force Stop
Then reopen app
```

### Fix 3: Uninstall and Reinstall
```
Uninstall app completely
Build → Rebuild Project in Android Studio
Run → Install fresh
Test again
```

### Fix 4: Check Backend Database Directly
```sql
-- On backend server, check if credentials are actually saved
SELECT id, status, 
       CASE WHEN agora_token IS NULL THEN 'NULL' 
            WHEN agora_token = '' THEN 'EMPTY STRING'
            ELSE CONCAT(LEFT(agora_token, 20), '... (', LENGTH(agora_token), ' chars)')
       END as token_status,
       channel_name
FROM calls
WHERE status IN ('CONNECTING', 'PENDING')
ORDER BY created_at DESC
LIMIT 5;
```

---

## 📊 Expected vs Actual

### Expected Flow:
```
1. Caller initiates call
   → Backend generates token + channel
   → Backend saves to database ✅
   
2. Receiver polls /calls/incoming
   → Backend reads from database ✅
   → Returns token + channel ✅
   
3. Receiver accepts call
   → Navigates with token + channel ✅
   → Joins Agora successfully ✅
   → Both see "Connected" ✅
```

### If Still Broken:
```
One of these steps is failing:
❌ Backend not saving credentials
❌ Backend not returning credentials
❌ App not receiving credentials
❌ App not passing credentials to screen
❌ Agora rejecting credentials
```

---

## 🆘 Next Steps

After adding the debug logs above:

1. **Accept a call** on receiver device
2. **Copy all logs** from Logcat (filter by `FemaleHomeScreen`)
3. **Send me the logs** so I can see exactly what's happening
4. **Also test the backend API** with curl and send the response

---

## 💡 Most Likely Issue

Based on your description, I suspect:
- **Backend is STILL returning null values**
- The fix might not have been deployed properly
- OR the migration wasn't run on the production database

**Ask your backend team**:
1. Did you run the migration? (`php artisan migrate`)
2. Can you check the `calls` table to see if `agora_token` and `channel_name` columns exist?
3. Can you manually test the API and show me the response?




