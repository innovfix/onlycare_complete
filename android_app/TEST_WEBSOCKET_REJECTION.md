# 🧪 Testing WebSocket Call Rejection

## Prerequisites
- Two physical devices or emulators (Device A = Caller, Device B = Receiver)
- Backend WebSocket server running
- Both devices connected to internet

## Test Steps

### Step 1: Enable Verbose Logging
Before testing, filter logs to see only WebSocket events:

```bash
# For Device A (Caller)
adb -s <device_a_id> logcat | grep -E "WebSocketManager|VideoCallViewModel|AudioCallViewModel|CallRejection"

# For Device B (Receiver)  
adb -s <device_b_id> logcat | grep -E "WebSocketManager|FemaleHome|CallRejection"
```

### Step 2: Make a Call
1. On Device A, make a call to Device B
2. **Watch Device A logs** - should see:
   ```
   📤 EMITTING call:initiate for callId: CALL_xxxxx
   ✅ Call initiated via WebSocket
   ```

3. **Watch Device B logs** - should see:
   ```
   📥 RECEIVED call:rejected from server
   ⚡ INSTANT incoming call via WebSocket!
   Caller: [Name]
   Type: video/audio
   ```

### Step 3: Reject the Call
1. On Device B, tap "Reject" button
2. **Watch Device B logs** - should see:
   ```
   🚫 rejectCall() called
      Call ID: CALL_xxxxx
      Reason: User declined
   ========================================
   📤 EMITTING call:reject to server
      Data: {"callId":"CALL_xxxxx","reason":"User declined"}
   ========================================
   ✅ call:reject emitted successfully
      ⏰ Server should send call:rejected back to caller
   ```

3. **Watch Device A logs** - should see **ONE OF TWO OUTCOMES**:

#### ✅ SUCCESS (Backend is Fixed):
```
========================================
📥 RECEIVED call:rejected from server
Raw data: {"callId":"CALL_xxxxx","reason":"User declined","timestamp":1700000000000}
========================================
❌ EMITTING CallRejected EVENT:
   Call ID: CALL_xxxxx
   Reason: User declined
   Timestamp: 1700000000000
========================================
✅ CallRejected event emitted successfully
🔴 Call rejected by receiver: User declined
📴 Cleaning up Agora resources
```
**Result:** Ringing stops IMMEDIATELY (<100ms)

#### ❌ FAILURE (Backend NOT Fixed):
```
(No logs about call:rejected)
(After 2-3 seconds)
📊 Polling call status for CALL_xxxxx
✅ Call status: rejected
🔴 Call rejected by receiver
```
**Result:** Ringing continues for 2-3 seconds until API polling detects rejection

## What to Look For

### ✅ SUCCESS Indicators:
- Device B sends `call:reject` successfully
- Device A receives `call:rejected` within 100ms
- Ringing stops immediately
- No waiting for API polling

### ❌ FAILURE Indicators:
- Device B sends `call:reject` successfully
- Device A **never** receives `call:rejected` event
- Device A detects rejection only after 2-3 seconds via API polling
- **This means backend is NOT forwarding the event**

## Backend Server Logs

Ask your backend team to check their WebSocket server logs. They should see:

```
📥 Received call:reject from user [receiver_id]
   callId: CALL_xxxxx
   reason: User declined
📤 Forwarding call:rejected to user [caller_id]
✅ Event forwarded successfully
```

If they DON'T see the "Forwarding" and "Event forwarded" lines, the backend fix is NOT implemented.

## Quick Debug Commands

### Check WebSocket Connection Status
```bash
adb logcat | grep "Connected to WebSocket"
```

### Watch Only Rejection Events
```bash
adb logcat | grep -E "call:reject|CallRejected"
```

### See Full WebSocket Event Flow
```bash
adb logcat | grep "========"
```

## Expected Timeline

### With Backend Fix (Approach A):
```
00:00.000 - User taps Reject on Device B
00:00.050 - Device B emits call:reject to server
00:00.100 - Server forwards call:rejected to Device A
00:00.150 - Device A receives event and stops ringing
```
**Total: ~150ms** ⚡

### Without Backend Fix (Current):
```
00:00.000 - User taps Reject on Device B
00:00.050 - Device B emits call:reject to server
00:00.100 - Server receives but DOESN'T forward
00:02.000 - Device A's API polling checks status
00:02.200 - API returns "rejected"
00:02.250 - Device A stops ringing
```
**Total: ~2250ms** 🐌

## Troubleshooting

### Problem: Device B doesn't emit call:reject
**Solution:** Check WebSocket connection:
```kotlin
Log.d("Debug", "WebSocket connected: ${webSocketManager.isConnected()}")
```

### Problem: Device A never receives call:rejected
**Solution:** This is the backend issue. Share `BACKEND_WEBSOCKET_REJECTION_REQUIRED.md` with backend team.

### Problem: Both devices show errors
**Solution:** Check backend WebSocket server is running:
```bash
curl https://onlycare.in/socket.io/
# Should return socket.io response
```

## Test Checklist

- [ ] Device A can call Device B successfully
- [ ] Device B receives incoming call notification
- [ ] Device B can reject the call
- [ ] Device B emits `call:reject` to server (check logs)
- [ ] Device A receives `call:rejected` from server (check logs)
- [ ] Device A stops ringing immediately (<200ms)
- [ ] Backend logs show event forwarding

## Result

If ALL checkboxes are ✅: **Backend is fixed, Approach A is working!**

If Device A doesn't receive `call:rejected`: **Backend needs to implement the fix in `BACKEND_WEBSOCKET_REJECTION_REQUIRED.md`**



