# 🎯 Backend is Perfect! Now Let's Debug Why It's Not Working

## ✅ What We Know For Sure

### Backend WebSocket Server (Lines 267-300):
```javascript
socket.on('call:reject', (data) => {
    const { callId, reason } = data;
    const call = activeCalls.get(callId);
    
    if (!call) {
        console.log(`❌ Call ${callId} not found`);
        return; // ⚠️ This is probably happening!
    }
    
    // Notify caller INSTANTLY
    const callerSocketId = connectedUsers.get(call.callerId);
    if (callerSocketId) {
        io.to(callerSocketId).emit('call:rejected', {
            callId,
            reason: reason || 'User declined',
            timestamp: Date.now()
        });
        console.log(`✅ Caller ${call.callerId} notified INSTANTLY: call rejected`);
    }
    
    activeCalls.delete(callId);
    socket.emit('call:reject:confirmed', { callId });
});
```

**✅ Backend code is PERFECT!**

### Android App - Caller Side:
```kotlin
// Line 242-254 in CallConnectingViewModel.kt
webSocketManager.initiateCall(
    receiverId = receiverId,
    callId = callId,
    callType = callType.uppercase(),
    channelName = channel,
    agoraToken = token
)
```

This emits:
```kotlin
// Line 378 in WebSocketManager.kt
socket?.emit("call:initiate", data, Ack { ... })
```

**✅ Android caller is sending call:initiate!**

### Android App - Receiver Side:
```kotlin
// Line 325 in FemaleHomeViewModel.kt
webSocketManager.rejectCall(callId, "User declined")
```

This emits:
```kotlin
// Line 453 in WebSocketManager.kt
socket?.emit("call:reject", data)
```

**✅ Android receiver is sending call:reject!**

---

## 🔍 The Real Problem

If the backend logs show:
```
❌ Call CALL_xxxxx not found
```

Then the issue is: **The call is NOT in the `activeCalls` Map!**

### Why Would This Happen?

#### Scenario 1: Backend Doesn't Store Call on Initiation ⚠️
**Check your backend's `call:initiate` handler:**

```javascript
// ❌ WRONG - If this is missing or incomplete:
socket.on('call:initiate', (data) => {
    // ... does something but doesn't store in activeCalls
});

// ✅ CORRECT - Should look like this:
socket.on('call:initiate', (data, callback) => {
    const { receiverId, callId, callType, channelName, agoraToken } = data;
    
    // 1. Store in activeCalls Map (THIS IS CRITICAL!)
    activeCalls.set(callId, {
        callerId: socket.userId,  // or data.callerId
        receiverId: receiverId,
        callType: callType,
        channelName: channelName,
        status: 'ringing',
        timestamp: Date.now()
    });
    
    console.log(`✅ Call stored in activeCalls: ${callId}`);
    console.log(`   Caller: ${socket.userId}, Receiver: ${receiverId}`);
    
    // 2. Notify receiver
    const receiverSocketId = connectedUsers.get(receiverId);
    if (receiverSocketId) {
        io.to(receiverSocketId).emit('call:incoming', {
            callId,
            callerId: socket.userId,
            callerName: socket.userName,
            callType,
            channelName,
            agoraToken,
            timestamp: Date.now()
        });
    }
    
    // 3. Send acknowledgment back to caller
    callback({ success: true });
});
```

#### Scenario 2: WebSocket Connection Issues 🔌
**Caller or Receiver not properly connected:**

Android logs should show:
```
✅ Connected to WebSocket server
```

If not, check:
- Server URL: `https://onlycare.in`
- Auth token is valid
- userId is set correctly

#### Scenario 3: Timing Issue ⏱️
**Call expires from `activeCalls` before rejection:**

Check if your backend has a timeout that removes calls:
```javascript
// If you have something like this:
setTimeout(() => {
    activeCalls.delete(callId);
}, 30000); // Removes after 30 seconds
```

---

## 🧪 Debug Steps

### Step 1: Check Backend Logs During Call Initiation

**Make a call from Device A to Device B, then check server logs:**

#### ✅ Expected Logs (GOOD):
```
📥 Received call:initiate from user123
   callId: CALL_17637599232099
   receiverId: user456
✅ Call stored in activeCalls: CALL_17637599232099
   Caller: user123, Receiver: user456
📤 Notifying receiver user456
```

#### ❌ Problem Logs (BAD):
```
📥 Received call:initiate from user123
   (no "stored in activeCalls" message)
```
**FIX:** Backend is not storing call in `activeCalls` Map!

---

### Step 2: Check Backend Logs During Call Rejection

**Reject the call on Device B, then check server logs:**

#### ✅ Expected Logs (GOOD):
```
📥 Received call:reject
   callId: CALL_17637599232099
   reason: User declined
✅ Found call in activeCalls
   Caller: user123, Receiver: user456
📤 Notifying caller user123
✅ Caller user123 notified INSTANTLY: call rejected
```

#### ❌ Problem Logs (BAD):
```
📥 Received call:reject
   callId: CALL_17637599232099
❌ Call CALL_17637599232099 not found
```
**FIX:** Call was never stored (see Scenario 1) or expired (see Scenario 3)!

---

### Step 3: Check Android Logs

#### Device A (Caller) - Should See:
```
📤 Emitting call:initiate for callId: CALL_17637599232099
✅ Call initiated via WebSocket
```

Then after rejection:
```
========================================
📥 RECEIVED call:rejected from server
Raw data: {"callId":"CALL_17637599232099","reason":"User declined","timestamp":1700000000000}
========================================
✅ CallRejected event emitted successfully
```

#### Device B (Receiver) - Should See:
```
⚡ INSTANT incoming call via WebSocket!
Caller: John Doe
Call ID: CALL_17637599232099
```

Then when rejecting:
```
🚫 rejectCall() called
📤 EMITTING call:reject to server
✅ call:reject emitted successfully
```

---

### Step 4: Verify Backend Data Structures

**Add debug endpoint to backend:**

```javascript
// Add this to your backend:
app.get('/api/debug/active-calls', (req, res) => {
    const calls = Array.from(activeCalls.entries()).map(([id, call]) => ({
        callId: id,
        ...call
    }));
    
    res.json({
        activeCallsCount: activeCalls.size,
        calls: calls
    });
});

app.get('/api/debug/connected-users', (req, res) => {
    const users = Array.from(connectedUsers.entries()).map(([userId, socketId]) => ({
        userId,
        socketId
    }));
    
    res.json({
        connectedUsersCount: connectedUsers.size,
        users: users
    });
});
```

**Test it:**
```bash
# After making a call, check if it's stored:
curl http://localhost:3001/api/debug/active-calls

# Should return:
{
  "activeCallsCount": 1,
  "calls": [
    {
      "callId": "CALL_17637599232099",
      "callerId": "user123",
      "receiverId": "user456",
      "status": "ringing"
    }
  ]
}

# Check connected users:
curl http://localhost:3001/api/debug/connected-users

# Should show both users:
{
  "connectedUsersCount": 2,
  "users": [
    { "userId": "user123", "socketId": "abc123" },
    { "userId": "user456", "socketId": "def456" }
  ]
}
```

---

## 🎯 Most Likely Issue

Based on the evidence, **the most likely problem is:**

### Backend's `call:initiate` handler is NOT storing the call in `activeCalls` Map!

**Fix:**
```javascript
socket.on('call:initiate', (data, callback) => {
    const { receiverId, callId, callType, channelName, agoraToken } = data;
    
    // 🚨 ADD THIS CRITICAL LINE:
    activeCalls.set(callId, {
        callerId: socket.userId,
        receiverId: receiverId,
        callType: callType,
        status: 'ringing',
        timestamp: Date.now()
    });
    
    console.log(`✅ Stored call in activeCalls: ${callId}`);
    
    // ... rest of your code ...
});
```

---

## 📋 Checklist

**Check your backend code:**
- [ ] Does `socket.on('call:initiate')` exist?
- [ ] Does it call `activeCalls.set(callId, { ... })`?
- [ ] Does it log "Stored call in activeCalls"?
- [ ] Is the `activeCalls` Map defined globally?
- [ ] Is the Map being cleared/reset somewhere?

**Check Android logs:**
- [ ] Caller shows "✅ Call initiated via WebSocket"
- [ ] Receiver shows "⚡ INSTANT incoming call"
- [ ] Receiver shows "✅ call:reject emitted successfully"
- [ ] Caller shows "📥 RECEIVED call:rejected from server" (THIS IS THE KEY!)

**Check backend logs:**
- [ ] Shows "✅ Call stored in activeCalls" on initiation
- [ ] Shows "✅ Caller notified INSTANTLY" on rejection
- [ ] Does NOT show "❌ Call not found"

---

## 🚀 Quick Test

**Add extra logging to your `call:reject` handler:**

```javascript
socket.on('call:reject', (data) => {
    const { callId, reason } = data;
    
    // DEBUG: Log entire activeCalls Map
    console.log(`========================================`);
    console.log(`📥 Received call:reject for ${callId}`);
    console.log(`📊 Current activeCalls:`);
    console.log(`   Total calls: ${activeCalls.size}`);
    for (const [id, call] of activeCalls.entries()) {
        console.log(`   - ${id}: ${call.callerId} → ${call.receiverId}`);
    }
    console.log(`========================================`);
    
    const call = activeCalls.get(callId);
    
    if (!call) {
        console.log(`❌ Call ${callId} NOT found in activeCalls!`);
        console.log(`   This means call:initiate didn't store it!`);
        return;
    }
    
    // ... rest of code ...
});
```

---

## 📁 Next Steps

1. **Check backend's `call:initiate` handler** - Does it store in `activeCalls`?
2. **Add debug logging** - Use the enhanced logging above
3. **Test with real devices** - Check all logs (Android + Backend)
4. **Use debug endpoints** - Verify data structures in real-time
5. **Report findings** - Share logs so we can identify the exact issue

---

## 💬 Summary

**Backend WebSocket server's `call:reject` handler is PERFECT!**  
**Android app's emit code is PERFECT!**  

**The missing piece is likely:**
- Backend's `call:initiate` handler not storing call in `activeCalls` Map
- OR WebSocket connection not established properly
- OR call timing out before rejection

**Follow the debug steps above to identify the exact issue!**



