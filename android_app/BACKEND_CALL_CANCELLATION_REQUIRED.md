# 🚫 Backend Implementation Required: Call Cancellation

## 📋 Overview

When a **caller disconnects/cancels** a call **before the receiver accepts**, the receiver's app should **instantly stop ringing** and close the incoming call screen.

**Status:** ✅ Frontend is ready, ⚠️ Backend needs to implement

---

## 🎯 When This Happens

**Scenario:**
1. Male (caller) initiates a call to Female (receiver)
2. Female receives incoming call notification (ringing)
3. **Before Female accepts**, Male disconnects/cancels the call
4. **Expected:** Female's app should instantly stop ringing and close the call screen

---

## 📤 What the App Sends (Already Working)

When caller cancels before receiver accepts, the app sends:

### WebSocket Event: `call:cancel`

```javascript
// Event name: "call:cancel"
// Sent from: Caller's app
{
  "callId": "CALL_17637599232099",
  "reason": "Caller ended call"
}
```

**Where it's sent from:**
- `AudioCallViewModel.endCall()` (line 654)
- `VideoCallViewModel.endCall()` (line 629)

**Condition:** Only sent when `isWaitingForReceiver == true` AND `remoteUserJoined == false`

---

## 📥 What Backend Must Do

### Step 1: Listen for `call:cancel` Event

```javascript
socket.on('call:cancel', async (data) => {
  const { callId, reason } = data;
  
  console.log(`📥 Received call:cancel from caller`);
  console.log(`   Call ID: ${callId}`);
  console.log(`   Reason: ${reason}`);
  
  // 1. Update database
  await updateCallStatus(callId, 'cancelled');
  
  // 2. Get call to find receiver
  const call = await Call.findById(callId);
  
  if (!call) {
    console.error(`❌ Call not found: ${callId}`);
    return;
  }
  
  // 3. ⚡ NOTIFY RECEIVER (REQUIRED)
  await notifyReceiver(call.receiverId, callId, reason);
});
```

### Step 2: Notify Receiver (Dual Mechanism)

You must send **BOTH** WebSocket and FCM notifications:

```javascript
async function notifyReceiver(receiverId, callId, reason) {
  // 1. WebSocket notification (PRIMARY - instant, <100ms)
  const receiverSocketId = getUserSocketId(receiverId);
  
  if (receiverSocketId) {
    io.to(receiverSocketId).emit('call:cancelled', {
      callId: callId,
      reason: reason || 'Caller cancelled',
      timestamp: Date.now()
    });
    
    console.log(`✅ Sent call:cancelled to receiver ${receiverId} via WebSocket`);
  }
  
  // 2. FCM notification (BACKUP - works if WebSocket disconnected or app killed)
  const receiverFcmToken = await getFCMToken(receiverId);
  
  if (receiverFcmToken) {
    await sendFCMNotification(receiverFcmToken, {
      type: 'call_cancelled',
      callId: callId,
      callerId: call.callerId
    });
    
    console.log(`✅ Sent call_cancelled FCM to receiver ${receiverId}`);
  } else {
    console.warn(`⚠️ No FCM token for receiver ${receiverId}`);
  }
}
```

---

## 📨 Event Formats

### WebSocket Event: `call:cancelled` (Server → Receiver)

```json
{
  "callId": "CALL_17637599232099",
  "reason": "Caller ended call",
  "timestamp": 1700000000000
}
```

**Event name:** `call:cancelled`  
**Sent to:** Receiver's WebSocket connection

---

### FCM Notification: `call_cancelled` (Server → Receiver)

```json
{
  "data": {
    "type": "call_cancelled",
    "callId": "CALL_17637599232099",
    "callerId": "USR_123"
  }
}
```

**Notification type:** `call_cancelled`  
**Sent to:** Receiver's FCM token

**Important:** This is a **data-only** notification (no notification payload), so it works even when app is killed.

---

## ✅ What Happens in the App (Already Implemented)

### WebSocket Handler (Primary)
- `WebSocketManager.handleCallCancelled()` receives `call:cancelled`
- Emits `CallCancelled` event to app
- `IncomingCallActivity` receives event
- Stops ringing service
- Closes incoming call screen
- Navigates to MainActivity

### FCM Handler (Backup)
- `CallNotificationService.handleCallCancelled()` receives FCM
- Stops `IncomingCallService`
- Sends broadcast `com.onlycare.app.CALL_CANCELLED`
- `IncomingCallActivity` receives broadcast
- Closes incoming call screen

---

## 🧪 Testing

### Test Case 1: WebSocket Connected
1. Device A (caller) and Device B (receiver) both have WebSocket connected
2. Device A calls Device B
3. Device B receives incoming call (ringing)
4. **Before Device B accepts**, Device A disconnects
5. **Expected:** Device B stops ringing instantly (<100ms)

### Test Case 2: WebSocket Disconnected (FCM Fallback)
1. Device B (receiver) has WebSocket disconnected
2. Device A calls Device B
3. Device B receives incoming call via FCM
4. **Before Device B accepts**, Device A disconnects
5. **Expected:** Device B receives FCM `call_cancelled` and stops ringing

### Test Case 3: App Killed (FCM Only)
1. Device B (receiver) has app killed
2. Device A calls Device B
3. Device B receives incoming call via FCM
4. **Before Device B accepts**, Device A disconnects
5. **Expected:** Device B receives FCM `call_cancelled` notification

---

## 📝 Summary

**What you need to implement:**

1. ✅ Listen for `call:cancel` event from caller
2. ✅ Update call status to `cancelled` in database
3. ✅ Send `call:cancelled` WebSocket event to receiver
4. ✅ Send `call_cancelled` FCM notification to receiver

**Why both WebSocket and FCM?**
- **WebSocket:** Instant notification (<100ms) when receiver is online
- **FCM:** Backup notification when WebSocket is disconnected or app is killed

---

## 🔗 Related Events

This is similar to the `call:reject` flow:

| Event | From | To | Purpose |
|-------|------|-----|---------|
| `call:reject` | Receiver | Server | Receiver rejects call |
| `call:rejected` | Server | Caller | Notify caller of rejection |
| `call:cancel` | Caller | Server | Caller cancels before acceptance |
| `call:cancelled` | Server | Receiver | Notify receiver of cancellation |

---

## ❓ Questions?

If you need clarification on:
- Event formats
- Database updates
- FCM token retrieval
- WebSocket connection management

Please ask! The frontend is ready and waiting for these events.









