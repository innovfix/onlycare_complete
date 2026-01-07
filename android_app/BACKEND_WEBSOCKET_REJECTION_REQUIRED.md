# ⚠️ URGENT: Backend WebSocket Update Required

## Issue
**Caller keeps ringing after receiver rejects call**

## Root Cause
Backend WebSocket server is not emitting `call:rejected` event to the caller when receiver rejects a call.

## Quick Fix (Backend Team)

### Step 1: Add Helper Function (if you don't have it)

```javascript
/**
 * Notify a specific user via WebSocket
 * @param {string} userId - The user ID to notify
 * @param {string} eventName - The event name to emit
 * @param {object} data - The data to send
 * @returns {boolean} - True if user was notified, false if offline
 */
function notifyUser(userId, eventName, data) {
  const userSocketId = getUserSocketId(userId); // Your existing function to get socket ID
  
  if (userSocketId) {
    io.to(userSocketId).emit(eventName, data);
    console.log(`✅ Sent ${eventName} to user ${userId}`);
    return true;
  } else {
    console.log(`⚠️ User ${userId} not connected to WebSocket`);
    return false;
  }
}
```

### Step 2: Current Code (WRONG):
```javascript
socket.on('call:reject', (data) => {
  // Only updates database, doesn't notify caller
  updateCallStatus(data.callId, 'rejected');
});
```

### Step 3: Required Code (CORRECT):
```javascript
socket.on('call:reject', async (data) => {
  const { callId, reason } = data;
  
  console.log(`📥 Received call:reject from receiver`);
  console.log(`   Call ID: ${callId}`);
  console.log(`   Reason: ${reason}`);
  
  // 1. Update database
  await updateCallStatus(callId, 'rejected');
  
  // 2. Get call to find caller
  const call = await Call.findById(callId);
  
  if (!call) {
    console.error(`❌ Call not found: ${callId}`);
    return;
  }
  
  // 3. ⚡ NOTIFY CALLER (THIS IS THE MISSING PART)
  const notified = notifyUser(call.callerId, 'call:rejected', {
    callId: callId,
    reason: reason || 'User declined',
    timestamp: Date.now()
  });
  
  if (notified) {
    console.log(`✅ Caller ${call.callerId} notified of rejection`);
  } else {
    console.log(`⚠️ Caller ${call.callerId} is offline (will see via API polling)`);
  }
});
```

## Event Format

### What App Sends (Already Working):
```json
Event: "call:reject"
Data: {
  "callId": "CALL_17637599232099",
  "reason": "User declined"
}
```

### What App Expects (MISSING):
```json
Event: "call:rejected"
Data: {
  "callId": "CALL_17637599232099",
  "reason": "User declined",
  "timestamp": 1700000000000
}
```

## How to Test

1. Make a call from Device A to Device B
2. On Device B, tap "Reject"
3. Check server logs - should see:
   ```
   📥 Received call:reject from receiver
   ✅ Sent call:rejected to caller
   ```
4. On Device A, ringing should stop immediately

## All WebSocket Events (Complete Reference)

### Events FROM Client:

1. **call:initiate** - Caller starts a call
2. **call:accept** - Receiver accepts (✅ Working)
3. **call:reject** - Receiver rejects (⚠️ Backend needs to forward this)
4. **call:end** - Either party ends call

### Events TO Client:

1. **call:incoming** - Notify receiver of new call (✅ Working)
2. **call:accepted** - Notify caller that receiver accepted (✅ Working)
3. **call:rejected** - Notify caller that receiver rejected (❌ MISSING - ADD THIS)
4. **call:ended** - Notify other party that call ended
5. **call:timeout** - Notify caller that receiver didn't answer

## Priority: 🔴 HIGH

This affects **every call rejection** in the app. Without this fix, callers have to wait 30-45 seconds for timeout.

With this fix: **Instant feedback (< 100ms)**

---

**Status: ⏳ Waiting for Backend Update**

---

## 📋 Approach A vs Approach B Clarification

### ✅ Approach A (RECOMMENDED - Android Emits Directly)
**This is what the Android app is ALREADY doing!**

- Android emits `call:reject` directly to WebSocket server
- WebSocket server forwards it as `call:rejected` to caller
- **Backend only needs to add the `notifyUser()` function above**
- **No Laravel changes needed**
- Fastest: 50-100ms

### Approach B (Alternative - Laravel Triggers WebSocket)
**This would be a backup/enhancement:**

- Android calls Laravel API `/api/calls/{id}/reject`
- Laravel updates DB **AND** triggers WebSocket event
- Requires more backend changes
- Slower: 200-500ms

### 🎯 What You Need to Do

**Your Android app is ALREADY implementing Approach A!**

You just need to add ONE function to your WebSocket server:
1. Add the `notifyUser()` helper function
2. Update the `socket.on('call:reject')` handler to call `notifyUser()`

That's it! No Laravel changes needed.

### Why Approach A is Better

- ✅ Android app already sends WebSocket events
- ✅ No API latency (direct WebSocket communication)
- ✅ Simpler backend implementation
- ✅ Works even if Laravel is slow/down
- ✅ Real-time (50-100ms vs 200-500ms)

---

See `CALL_REJECTION_FLOW_FIX.md` for complete technical details.
See `TEST_WEBSOCKET_REJECTION.md` for testing guide.

