# 🚨 CRITICAL: Backend Must Forward Call Rejection to Caller

## Problem Reported

**Working:**
- Female calls Male → Male rejects → ✅ Both sides see rejection

**NOT Working:**
- Male calls Female → Female rejects → ❌ Male doesn't see rejection (stays waiting)

---

## Root Cause

The Android app is **correctly sending** the rejection:

### What Happens (Female rejects Male's call):

1. Female clicks "Reject" button
2. `IncomingCallActivity.handleRejectCall()` is called
3. App sends rejection via WebSocket:
   ```kotlin
   webSocketManager.rejectCall(callId, "User declined")
   ```
4. WebSocket emits to backend:
   ```javascript
   socket.emit("call:reject", {
     callId: "CALL_123",
     reason: "User declined"
   })
   ```

5. ✅ **Backend receives the rejection**

6. ❌ **Backend does NOT forward it to the Male (caller)**

7. Male's app keeps waiting (never receives `call:rejected` event)

---

## What Backend MUST Do

When backend receives `call:reject` from receiver, it **MUST**:

### Step 1: Update Database

```javascript
// Update call status to REJECTED
await Call.findByIdAndUpdate(callId, {
  status: 'rejected',
  ended_at: new Date()
});
```

### Step 2: Forward WebSocket Event to Caller

**This is the MISSING piece!**

```javascript
socket.on('call:reject', async (data) => {
  const { callId, reason } = data;
  
  console.log('📥 Received call:reject from receiver');
  console.log('   Call ID:', callId);
  console.log('   Reason:', reason);
  
  // 1. Update database
  await Call.findByIdAndUpdate(callId, {
    status: 'rejected',
    ended_at: new Date()
  });
  
  // 2. Get the call to find the CALLER
  const call = await Call.findById(callId);
  
  if (!call) {
    console.error('Call not found:', callId);
    return;
  }
  
  const callerId = call.caller_id;  // The person who initiated the call
  
  // 3. ⚡ FORWARD rejection to CALLER (REQUIRED!)
  const callerSocketId = getUserSocketId(callerId);
  
  if (callerSocketId) {
    console.log('📤 Forwarding call:rejected to caller:', callerId);
    
    io.to(callerSocketId).emit('call:rejected', {
      callId: callId,
      reason: reason || 'User declined',
      timestamp: Date.now()
    });
    
    console.log('✅ Rejection forwarded to caller via WebSocket');
  } else {
    console.warn('⚠️ Caller not connected via WebSocket:', callerId);
    console.warn('   Caller will detect rejection via API polling (slower)');
  }
  
  // 4. Optional: Send FCM as backup
  sendFCMNotification(callerId, {
    type: 'call_rejected',
    callId: callId,
    reason: reason
  });
});
```

---

## Android App Side (Already Working)

The Android app is **already listening** for the rejection:

### AudioCallViewModel.kt (Line 67-106)

```kotlin
init {
    viewModelScope.launch {
        webSocketManager.callEvents.collect { event ->
            when (event) {
                is WebSocketEvent.CallRejected -> {
                    Log.d(TAG, "📥 CallRejected EVENT RECEIVED")
                    
                    if (event.callId == _state.value.callId) {
                        Log.d(TAG, "✅ MATCH! Showing rejection")
                        
                        // Cancel timeout job
                        connectionTimeoutJob?.cancel()
                        callStatusPollingJob?.cancel()
                        
                        // Update state to show rejection
                        _state.update {
                            it.copy(
                                isCallEnded = true,
                                waitingForReceiver = false,
                                error = "📞 Call Rejected\n\n${event.reason}"
                            )
                        }
                        
                        // Clean up Agora
                        agoraManager?.leaveChannel()
                        agoraManager?.destroy()
                    }
                }
            }
        }
    }
}
```

**So the Android app IS ready to receive rejections - backend just needs to send them!**

---

## Fallback Mechanism (API Polling)

If WebSocket fails, the app polls the API every 2 seconds:

### AudioCallViewModel.kt (Line 224-250)

```kotlin
// Check call status via API
val result = repository.getCallStatus(callId)

result.onSuccess { call ->
    when (call.status?.uppercase()) {
        "REJECTED", "DECLINED", "ENDED" -> {
            // Show rejection error
            _state.update {
                it.copy(
                    isCallEnded = true,
                    error = "📞 Call Rejected\n\nThe receiver declined your call."
                )
            }
        }
    }
}
```

**This works IF backend updates call status to "REJECTED" in database!**

---

## Why Female→Male Works But Male→Female Doesn't

### Scenario 1: Female calls Male, Male rejects ✅

```
1. Female calls Male
2. Male rejects
3. Backend receives call:reject
4. Backend forwards call:rejected to Female ✅
5. Female sees "Call Rejected" ✅
```

### Scenario 2: Male calls Female, Female rejects ❌

```
1. Male calls Female
2. Female rejects
3. Backend receives call:reject
4. Backend DOES NOT forward to Male ❌
5. Male keeps waiting forever ❌
```

**The difference:** Backend is only forwarding rejections in one direction!

---

## Testing

### Test the WebSocket Event:

**Terminal 1 (Male Device - Caller):**
```bash
adb logcat | grep -E "CallRejected EVENT|REJECTION|call:rejected"
```

**Terminal 2 (Female Device - Receiver):**
```bash
adb logcat | grep -E "call:reject emitted|REJECTING CALL"
```

### Perform Test:

1. Male calls Female
2. Female clicks "Reject"
3. Check logs

**Expected (if backend works):**

Female Terminal:
```log
📤 EMITTING call:reject to server
✅ call:reject emitted successfully
```

Male Terminal:
```log
📥 RECEIVED call:rejected from server
✅ CallRejected event emitted successfully
📞 Call Rejected - User declined your call
```

**Actual (if backend broken):**

Female Terminal:
```log
📤 EMITTING call:reject to server  ← Sent!
✅ call:reject emitted successfully
```

Male Terminal:
```log
(No logs - never receives call:rejected)  ← NOT RECEIVED!
```

---

## Backend Fix Checklist

**Backend Developer Must:**

1. ✅ Listen for `socket.on('call:reject', ...)`
2. ✅ Update call status to `"rejected"` in database
3. ✅ Get the call from database to find `caller_id`
4. ✅ Forward `call:rejected` event to caller via WebSocket:
   ```javascript
   io.to(callerSocketId).emit('call:rejected', {
     callId: callId,
     reason: reason,
     timestamp: Date.now()
   })
   ```
5. ✅ Optional: Send FCM as backup if caller not connected

---

## API Endpoint (Also Needed)

**Endpoint:** `POST /api/v1/calls/{callId}/reject`

**Request:**
```json
{
  "reason": "User declined"
}
```

**What it should do:**
1. Update call status to "rejected"
2. Set ended_at timestamp
3. Forward WebSocket event to caller (same as above)
4. Return success response

---

## Summary

**Problem:** Backend receives rejection but doesn't forward it to caller

**Solution:** Backend must forward `call:rejected` WebSocket event to caller

**Android App:** Already ready to receive and handle rejections

**Impact:** Once backend forwards rejections, male will see "Call Rejected" screen instead of waiting forever

---

**Date:** December 3, 2025  
**Priority:** 🔴 **CRITICAL**  
**Status:** ⚠️ **Requires Backend Fix**  
**Android App Status:** ✅ **Ready** (waiting for backend)


