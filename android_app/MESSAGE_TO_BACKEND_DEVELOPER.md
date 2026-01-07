# Backend API: Cancel Call

## Problem
When male ends call before female accepts, female's screen keeps ringing. Need to notify female when call is cancelled.

## Required: POST `/api/v1/calls/{callId}/cancel`

**Auth:** Bearer token  
**Response:** `{ "success": true, "data": "Call cancelled" }`

## What Backend Must Do

1. **Update database:** `status = 'cancelled'`, `ended_at = NOW()`

2. **Send FCM to receiver:**
   ```json
   {
     "token": "receiver_fcm_token",
     "data": {
       "type": "call_cancelled",
       "callId": "CALL_123",
       "callerId": "USR_456",
       "callerName": "John",
       "reason": "Caller ended call",
       
       "timestamp": "1700000000000"
     },
     "android": { "priority": "high" }
   }
   ```
   ⚠️ Use `data` only, NOT `notification` field!

3. **Emit WebSocket (if receiver connected):**
   ```javascript
   io.to(receiverSocketId).emit('call:cancelled', {
     callId: callId,
     reason: "Caller ended call",
     timestamp: Date.now()
   });
   ```

## WebSocket Handler (Alternative)

If using WebSocket instead of REST API:

```javascript
socket.on('call:cancel', async (data) => {
  const { callId, reason } = data;
  
  // 1. Update database
  await Call.updateOne({ _id: callId }, { status: 'cancelled', ended_at: new Date() });
  
  // 2. Get call and receiver
  const call = await Call.findById(callId);
  
  // 3. Emit to receiver
  const receiverSocketId = getUserSocketId(call.receiverId);
  if (receiverSocketId) {
    io.to(receiverSocketId).emit('call:cancelled', {
      callId, reason: reason || 'Caller cancelled', timestamp: Date.now()
    });
  }
  
  // 4. Send FCM backup
  const receiver = await User.findById(call.receiverId);
  if (receiver?.fcmToken) {
    await sendFCMNotification(receiver.fcmToken, {
      type: 'call_cancelled',
      callId, callerId: call.callerId, callerName: call.callerName,
      reason: reason || 'Caller cancelled', timestamp: Date.now().toString()
    });
  }
});
```

## Validation
- Only caller can cancel: `call.callerId == current_user.id`
- Call must be pending: `call.status IN ('pending', 'ringing')`

## Test
1. Male calls female
2. Female's phone rings
3. Male ends call before female accepts
4. **Expected:** Female's screen closes immediately

**Android app is ready. Just implement the endpoint.**

