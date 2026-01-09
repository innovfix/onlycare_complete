# 🐛 Call Ending Immediately After Accept - Bug Analysis & Fix

**Date:** January 10, 2026  
**Issue:** Calls end automatically immediately after accepting incoming calls  
**Status:** 🔍 INVESTIGATING

---

## 📊 Problem Analysis

Based on code analysis, here are the potential causes:

### 1. **Balance Time Issue** (MOST LIKELY)
```kotlin
// AudioCallViewModel.kt:673-676
if (maxDuration <= 0) {
    Log.w(TAG, "⚠️ WARNING: No balance time available - call may end immediately")
    Log.w(TAG, "⚠️ TIMER WILL NOT BE DISPLAYED (maxCallDuration = 0)")
}
```

**Symptom:** If `balanceTime` is null, empty, or "0:00", the call will have `maxCallDuration = 0`

**Location of balanceTime flow:**
1. IncomingCallActivity receives `balanceTime` from FCM notification
2. Passes it to CallActivity via Intent extra `EXTRA_BALANCE_TIME`
3. CallActivity passes it to AudioCallScreen
4. AudioCallScreen calls `viewModel.setBalanceTime(balanceTime)`

### 2. **Call Status Polling Issue**
```kotlin
// AudioCallViewModel.kt:327-354
when (call.status?.uppercase()) {
    "REJECTED", "DECLINED", "ENDED" -> {
        // This ends the call immediately
        _state.update { it.copy(isCallEnded = true) }
    }
}
```

**Symptom:** If backend returns stale status from previous call, new call ends immediately

### 3. **Stale State from Previous Call**
```kotlin
// AudioCallScreen.kt:302
if (state.isCallEnded && !state.callId.isNullOrEmpty() && state.callReallyStarted) {
    // Ends the call
}
```

**Symptom:** If `isCallEnded` wasn't properly reset, new call ends immediately

---

## 🔍 Debugging Steps

### Step 1: Check Logs When Call Ends

Run the app and look for these log patterns:

**Balance Time Logs:**
```
AudioCallScreen: ⏱️ Balance Time: [VALUE]
AudioCallViewModel: ⏱️ SET BALANCE TIME CALLED
AudioCallViewModel:    Input balanceTime: [VALUE]
AudioCallViewModel:    Parsed maxDuration: [VALUE] seconds
```

**If you see:**
- `balanceTime: EMPTY` or `NULL` → **Balance time not passed correctly**
- `maxDuration: 0 seconds` → **No balance available**
- `⚠️ WARNING: No balance time available` → **Balance time issue confirmed**

**Call Status Polling Logs:**
```
AudioCallViewModel: 📡 Polling call status for: [CALL_ID]
AudioCallViewModel: 📊 Call status: [STATUS]
AudioCallViewModel: ⚡ Call was rejected/ended - detected via API polling
```

**If you see:**
- Poll immediately returns "ENDED" → **Stale backend data**
- Call ends before "Remote user joined" → **Premature ending**

### Step 2: Check FCM Notification Payload

Look for this log in `CallNotificationService`:
```
CallNotificationService: BALANCE_TIME: [VALUE]
```

**If balance_time is missing from FCM payload, this is a backend issue.**

---

## 🔧 Potential Fixes

### Fix 1: Handle Missing Balance Time (Frontend Protection)

If balance time is 0 or missing, don't end call immediately:

```kotlin
// AudioCallViewModel.kt - Modify setBalanceTime
fun setBalanceTime(balanceTime: String?) {
    val maxDuration = TimeUtils.parseBalanceTime(balanceTime)
    
    // ✅ FIX: If balance time is 0, set a default duration (e.g., 30 minutes)
    // This prevents calls from ending immediately due to missing balance_time
    val safeDuration = if (maxDuration <= 0) {
        Log.w(TAG, "⚠️ Balance time is 0 or invalid - using default 30 minutes")
        30 * 60  // 30 minutes default
    } else {
        maxDuration
    }
    
    _state.update { 
        it.copy(
            maxCallDuration = safeDuration,
            remainingTime = safeDuration,
            isLowTime = TimeUtils.isLowTime(safeDuration)
        ) 
    }
}
```

### Fix 2: Delay Call Status Polling (Prevent Stale Data)

Don't start polling immediately - give call time to connect:

```kotlin
// AudioCallViewModel.kt - Modify startCallStatusPolling
private fun startCallStatusPolling() {
    callStatusPollingJob = viewModelScope.launch {
        // ✅ FIX: Wait 5 seconds before starting to poll
        // This prevents checking stale status from previous call
        kotlinx.coroutines.delay(5000)
        
        var shouldContinuePolling = true
        while (shouldContinuePolling) {
            kotlinx.coroutines.delay(2000)
            // ... rest of polling logic
        }
    }
}
```

### Fix 3: Ignore Status If Call Just Started

```kotlin
// AudioCallViewModel.kt - Modify polling logic
when (call.status?.uppercase()) {
    "REJECTED", "DECLINED", "ENDED" -> {
        // ✅ FIX: Ignore if call started less than 5 seconds ago
        val timeSinceInit = System.currentTimeMillis() - callInitializedAt
        if (timeSinceInit < 5000) {
            Log.d(TAG, "⚠️ Call just started - ignoring ENDED status (likely stale)")
            return@launch
        }
        
        // Only set isCallEnded if call was really started
        if (!_state.value.callReallyStarted) {
            Log.d(TAG, "⚠️ Call not really started - ignoring ENDED status")
            return@launch
        }
        
        _state.update { it.copy(isCallEnded = true) }
    }
}
```

### Fix 4: Backend Fix (If Needed)

If `balance_time` is missing from FCM notification, update backend to include it:

**Laravel Backend - CallController.php:**
```php
// In initiateCall or acceptCall response
return response()->json([
    'success' => true,
    'call' => [
        'call_id' => $call->call_id,
        'channel_id' => $call->channel_id,
        'agora_token' => $call->agora_token,
        'balance_time' => $this->calculateBalanceTime($user), // ✅ Always include
    ]
]);
```

---

## ✅ Recommended Fix (Immediate)

Apply **Fix 1** immediately - this provides frontend protection:

1. If backend doesn't send balance_time, use default duration
2. Log warning so we know it's happening
3. Call proceeds normally instead of ending

This is the safest fix that doesn't break anything and prevents calls from ending immediately.

---

## 🧪 Testing Steps

After applying fix:

1. **Clear app data** (important - clears any stale state)
2. Make an incoming call
3. Accept the call
4. **Check logs for:**
   - `⏱️ Balance Time:` value
   - `maxCallDuration:` value
   - `⚠️ Balance time is 0 or invalid - using default` (if triggered)
5. **Verify call stays connected** for at least 30 seconds
6. **Check call ended screen** shows correct duration

---

## 📝 Next Steps

1. Apply Fix 1 (default balance time)
2. Test on device
3. Check logs to identify root cause
4. If balance_time is missing, fix backend
5. If status polling is the issue, apply Fix 2 or Fix 3

---

