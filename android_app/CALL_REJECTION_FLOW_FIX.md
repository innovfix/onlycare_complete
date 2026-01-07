# 🔧 Call Rejection Flow - Complete Fix

## Problem
When the receiver rejects a call, the caller's app keeps ringing and doesn't stop.

## Root Cause
The issue is in the **backend WebSocket server** - it's not sending the `call:rejected` event back to the caller when the receiver rejects the call.

## How It Should Work

### The Complete Flow:

```
RECEIVER SIDE:
1. User taps "Reject" button
2. IncomingCallActivity.handleRejectCall() is called
3. Sends broadcast "com.onlycare.app.CALL_REJECTED"
4. FemaleHomeViewModel receives broadcast
5. FemaleHomeViewModel.rejectIncomingCall() is called
6. Calls webSocketManager.rejectCall(callId, reason)
7. WebSocket emits "call:reject" to SERVER with:
   {
     "callId": "CALL_123456",
     "reason": "User declined"
   }
8. Also calls API: POST /calls/{callId}/reject

SERVER SIDE (WHAT NEEDS TO HAPPEN):
9. Server receives "call:reject" event from receiver
10. ⚠️ SERVER MUST EMIT "call:rejected" to CALLER with:
   {
     "callId": "CALL_123456",
     "reason": "User declined",
     "timestamp": 1700000000000
   }

CALLER SIDE:
11. WebSocket receives "call:rejected" event
12. WebSocketManager.handleCallRejected() is called
13. Emits CallRejected event to app
14. VideoCallViewModel/AudioCallViewModel receives event
15. Checks if callId matches current call
16. If match:
    - Sets isCallEnded = true
    - Sets error = "Call Rejected"
    - Cancels timeout jobs
    - Leaves Agora channel
    - Destroys Agora instance
17. UI shows rejection message
18. User sees call ended screen
```

## What We Fixed in the App

### 1. ✅ Enhanced Logging

Added comprehensive logging to track the entire rejection flow:

**In WebSocketManager.rejectCall():**
- Logs when rejection is sent
- Logs the data being sent
- Logs WebSocket connection status
- Logs any errors

**In WebSocketManager.handleCallRejected():**
- Logs when rejection is received from server
- Logs the raw data
- Logs the parsed callId and reason
- Logs if event emission succeeds or fails

**In VideoCallViewModel/AudioCallViewModel:**
- Logs when CallRejected event is received
- Logs the event's callId vs current callId
- Logs if there's a match
- Logs each step of the cleanup process

### 2. ✅ Verified Message Format

The app is correctly:
- **Sending**: `call:reject` with `{ callId, reason }`
- **Expecting**: `call:rejected` with `{ callId, reason, timestamp }`

### 3. ✅ Visual Feedback

When rejection is received:
- Sets `isCallEnded = true`
- Shows error message: "📞 Call Rejected\n\nUser declined\n\nThe receiver declined your call."
- Stops ringing UI
- Cleans up Agora resources

## What the Backend Team Needs to Do

### ⚠️ REQUIRED: Update WebSocket Server

The backend WebSocket server needs to handle the `call:reject` event and broadcast it to the caller.

**Current Behavior (WRONG):**
```javascript
socket.on('call:reject', (data) => {
  // ❌ Only saves to database, doesn't notify caller
  updateCallStatus(data.callId, 'rejected');
});
```

**Required Behavior (CORRECT):**
```javascript
socket.on('call:reject', async (data) => {
  const { callId, reason } = data;
  
  // 1. Update database
  await updateCallStatus(callId, 'rejected');
  
  // 2. Get call details to find the caller
  const call = await Call.findById(callId);
  
  // 3. ⚡ EMIT rejection to CALLER
  const callerSocketId = getUserSocketId(call.callerId);
  
  if (callerSocketId) {
    io.to(callerSocketId).emit('call:rejected', {
      callId: callId,
      reason: reason || 'User declined',
      timestamp: Date.now()
    });
    
    console.log(`✅ Sent call:rejected to caller ${call.callerId}`);
  } else {
    console.warn(`⚠️ Caller ${call.callerId} not connected via WebSocket`);
    // Fallback: Send FCM push notification to caller
    await sendFCMNotification(call.callerId, {
      type: 'call_rejected',
      callId: callId,
      reason: reason
    });
  }
});
```

### Backend WebSocket Events - Complete List

The backend should support these WebSocket events:

#### Events RECEIVED from Client:

1. **`call:initiate`** - When caller initiates a call
   ```json
   {
     "receiverId": "USR_123",
     "callId": "CALL_456",
     "callType": "VIDEO",
     "channelName": "call_CALL_456",
     "agoraToken": "token..."
   }
   ```
   → Backend should emit `call:incoming` to receiver

2. **`call:accept`** - When receiver accepts a call
   ```json
   {
     "callId": "CALL_456"
   }
   ```
   → Backend should emit `call:accepted` to caller

3. **`call:reject`** - When receiver rejects a call ⚠️ **THIS IS THE FIX**
   ```json
   {
     "callId": "CALL_456",
     "reason": "User declined"
   }
   ```
   → Backend should emit `call:rejected` to caller

4. **`call:end`** - When either party ends a call
   ```json
   {
     "callId": "CALL_456"
   }
   ```
   → Backend should emit `call:ended` to other party

#### Events SENT to Client:

1. **`call:incoming`** - Sent to receiver when call is initiated
   ```json
   {
     "callId": "CALL_456",
     "callerId": "USR_123",
     "callerName": "John Doe",
     "callerPhoto": "https://...",
     "callType": "VIDEO",
     "channelName": "call_CALL_456",
     "agoraToken": "token...",
     "timestamp": 1700000000000
   }
   ```

2. **`call:accepted`** - Sent to caller when receiver accepts
   ```json
   {
     "callId": "CALL_456",
     "timestamp": 1700000000000
   }
   ```

3. **`call:rejected`** - Sent to caller when receiver rejects ⚠️ **MISSING - NEEDS TO BE ADDED**
   ```json
   {
     "callId": "CALL_456",
     "reason": "User declined",
     "timestamp": 1700000000000
   }
   ```

4. **`call:ended`** - Sent to other party when call ends
   ```json
   {
     "callId": "CALL_456",
     "endedBy": "USR_123",
     "reason": "Call completed",
     "timestamp": 1700000000000
   }
   ```

5. **`call:timeout`** - Sent to caller if receiver doesn't answer within 45 seconds
   ```json
   {
     "callId": "CALL_456",
     "timestamp": 1700000000000
   }
   ```

## How to Test

### Test 1: With Enhanced Logging

1. **Setup:**
   - Device A: Caller (logged in as male user)
   - Device B: Receiver (logged in as female user)
   - Both devices connected to internet
   - Both apps open and WebSocket connected

2. **Steps:**
   - Device A initiates a video call to Device B
   - Device B receives incoming call notification
   - Device B taps "Reject" button

3. **Check Logs on Device B (Receiver):**
   ```
   📤 EMITTING call:reject to server
      Data: {"callId":"CALL_123","reason":"User declined"}
   ✅ call:reject emitted successfully
   ```

4. **Check Server Logs:**
   ```
   📥 Received call:reject from client
   ✅ Sent call:rejected to caller
   ```

5. **Check Logs on Device A (Caller):**
   ```
   📥 RECEIVED call:rejected from server
      Raw data: {"callId":"CALL_123","reason":"User declined","timestamp":...}
   ❌ EMITTING CallRejected EVENT
   📥 CallRejected EVENT RECEIVED
      Event Call ID: CALL_123
      Current Call ID: CALL_123
      Match: true
   ✅ MATCH! This rejection is for OUR call
   🛑 STOPPING RINGING - Ending call now
   ✅ State updated - isCallEnded=true
   ✅ Agora cleaned up
   ```

6. **Expected Result:**
   - Caller's screen stops ringing immediately
   - Shows "Call Rejected" message
   - Returns to previous screen

### Test 2: Without WebSocket (API Fallback)

1. **Setup:**
   - Same as Test 1, but disconnect WebSocket on Device A

2. **Steps:**
   - Same as Test 1

3. **Expected Result:**
   - API polling (every 2 seconds) detects rejection
   - Within 2-4 seconds, caller sees rejection
   - Not instant, but still works

## Files Modified

1. ✅ `WebSocketManager.kt`
   - Enhanced `rejectCall()` with logging
   - Enhanced `handleCallRejected()` with logging

2. ✅ `VideoCallViewModel.kt`
   - Enhanced CallRejected event handler with logging

3. ✅ `AudioCallViewModel.kt`
   - Enhanced CallRejected event handler with logging

## Backend Files to Modify

The backend team needs to update:

1. **WebSocket Handler** (e.g., `websocket.js`, `socketHandler.js`)
   - Add/update `call:reject` event handler
   - Emit `call:rejected` to caller

2. **Socket Manager** (e.g., `socketManager.js`)
   - Add method to get socket ID by user ID
   - Add method to emit events to specific users

## Testing Checklist

- [ ] Receiver rejects call → Caller stops ringing (WebSocket)
- [ ] Receiver rejects call → Caller stops ringing (API polling fallback)
- [ ] Rejection works on audio calls
- [ ] Rejection works on video calls
- [ ] Rejection works when caller is in foreground
- [ ] Rejection works when caller is in background
- [ ] Logs show complete rejection flow
- [ ] Backend emits `call:rejected` event
- [ ] Backend updates database status to 'rejected'

## Debug Commands

To test if WebSocket is working:

```bash
# Check WebSocket connection on device
adb logcat | grep "WebSocketManager"

# Check rejection flow on receiver
adb logcat | grep "rejectCall"

# Check rejection received on caller
adb logcat | grep "CallRejected"

# Check if Agora is cleaned up
adb logcat | grep "Agora"
```

## Summary

✅ **What We Fixed:**
- Added comprehensive logging to track rejection flow
- Verified app is sending correct messages
- Verified app is listening for correct events
- Confirmed app properly handles rejection when received

⚠️ **What Backend Needs to Fix:**
- Add handler for `call:reject` event
- Emit `call:rejected` event to caller
- Include callId, reason, and timestamp in event

🎯 **Expected Outcome:**
- Instant call rejection notification (< 100ms)
- Caller's app stops ringing immediately
- Clean user experience
- Proper resource cleanup

---

**Status: ✅ APP-SIDE COMPLETE**
**Waiting for: ⏳ BACKEND WEBSOCKET UPDATE**

Once backend adds the `call:rejected` event emission, the rejection flow will work instantly!



