# Backend API: Cancel Call Endpoint

## Required Endpoint

**POST** `/api/v1/calls/{callId}/cancel`

**Auth:** Bearer token required

**Response (200):**
```json
{
  "success": true,
  "message": "Call cancelled successfully",
  "data": "Call cancelled"
}
```

---

## What Backend Must Do

1. **Update Database:**
   - Set `call.status = 'cancelled'`
   - Set `call.ended_at = NOW()`

2. **Send FCM to Receiver (Female):**
   ```json
   {
     "token": "receiver_fcm_token",
     "data": {
       "type": "call_cancelled",
       "callId": "CALL_123",
       "callerId": "USR_456",
       "callerName": "John Doe",
       "reason": "Caller ended call",
       "timestamp": "1700000000000"
     },
     "android": { "priority": "high" }
   }
   ```
   ⚠️ **Use `data` only, NOT `notification` field!**

3. **Emit WebSocket (if receiver connected):**
   ```javascript
   io.to(receiverSocketId).emit('call:cancelled', {
     callId: callId,
     reason: "Caller ended call",
     timestamp: Date.now()
   });
   ```

---

## Validation

- Only caller can cancel: `call.callerId == current_user.id`
- Call must be pending: `call.status IN ('pending', 'ringing')`

---

## Flow

```
Male calls API → Backend cancels call → Sends FCM to Female → Female's screen closes
```

**Android app is ready. Just implement the endpoint.**









