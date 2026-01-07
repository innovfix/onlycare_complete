# 🚨 Backend API Required: Cancel Call Endpoint

## Overview

When a **male user disconnects a call before the female accepts**, the backend must:
1. Update the call status to `cancelled`
2. Send FCM notification to the female user
3. Forward WebSocket event to the female user (if connected)

---

## API Endpoint Required

### POST `/api/v1/calls/{callId}/cancel`

**Purpose:** Cancel a call when the caller disconnects before the receiver accepts.

**Authentication:** Required (Bearer token)

**Request:**
```
POST /api/v1/calls/{callId}/cancel
Authorization: Bearer {token}
```

**Path Parameters:**
- `callId` (string, required) - The ID of the call to cancel

**Response (Success - 200):**
```json
{
  "success": true,
  "message": "Call cancelled successfully",
  "data": "Call cancelled"
}
```

**Response (Error - 404):**
```json
{
  "success": false,
  "error": {
    "code": "CALL_NOT_FOUND",
    "message": "Call not found"
  }
}
```

**Response (Error - 403):**
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Only the caller can cancel this call"
  }
}
```

---

## Backend Implementation Requirements

### 1. Update Call Status

When the cancel endpoint is called:
- Set `call.status = 'cancelled'`
- Set `call.ended_at = NOW()`
- Set `call.ended_by = caller_id`

### 2. Send FCM Notification to Receiver

**Critical:** Send FCM notification to the **female user** (receiver) with this exact format:

```json
{
  "token": "receiver_fcm_token_from_database",
  "data": {
    "type": "call_cancelled",
    "callId": "CALL_1234567890",
    "callerId": "USR_123",
    "callerName": "John Doe",
    "reason": "Caller ended call",
    "timestamp": "1700000000000"
  },
  "android": {
    "priority": "high"
  },
  "apns": {
    "headers": {
      "apns-priority": "10"
    }
  }
}
```

**⚠️ IMPORTANT:**
- Use **data-only payload** (NOT `notification` field)
- All values in `data` must be **strings**
- Set `priority: "high"` for Android
- Set `apns-priority: "10"` for iOS

### 3. Forward WebSocket Event (if receiver is connected)

If the receiver is connected via WebSocket, emit this event:

**Event:** `call:cancelled`

```json
{
  "callId": "CALL_1234567890",
  "reason": "Caller ended call",
  "timestamp": 1700000000000
}
```

**Example Node.js/Socket.io:**
```javascript
socket.on('call:cancel', async (data) => {
  const { callId, reason } = data;
  
  // 1. Update database
  await Call.updateOne(
    { _id: callId },
    { 
      status: 'cancelled',
      ended_at: new Date(),
      ended_by: socket.userId
    }
  );
  
  // 2. Get call details
  const call = await Call.findById(callId);
  const receiverId = call.receiverId;
  
  // 3. Send WebSocket event to receiver
  const receiverSocketId = getUserSocketId(receiverId);
  if (receiverSocketId) {
    io.to(receiverSocketId).emit('call:cancelled', {
      callId: callId,
      reason: reason || 'Caller cancelled',
      timestamp: Date.now()
    });
  }
  
  // 4. Send FCM notification (backup)
  const receiver = await User.findById(receiverId);
  if (receiver?.fcmToken) {
    await sendFCMNotification(receiver.fcmToken, {
      type: 'call_cancelled',
      callId: callId,
      callerId: call.callerId,
      callerName: call.callerName || 'Unknown',
      reason: reason || 'Caller cancelled',
      timestamp: Date.now().toString()
    });
  }
});
```

---

## Complete Flow

```
1. Male app calls: POST /api/v1/calls/{callId}/cancel
   ↓
2. Backend validates:
   - Call exists
   - User is the caller
   - Call status is 'pending' or 'ringing'
   ↓
3. Backend updates database:
   - status = 'cancelled'
   - ended_at = NOW()
   ↓
4. Backend sends FCM to female:
   - type: 'call_cancelled'
   - callId, callerId, callerName, reason
   ↓
5. Backend emits WebSocket (if connected):
   - event: 'call:cancelled'
   - to: receiver socket
   ↓
6. Female app receives FCM/WebSocket:
   - Stops ringing
   - Closes IncomingCallActivity
   - Navigates to MainActivity
```

---

## Validation Rules

1. **Only the caller can cancel** - Verify `call.callerId == current_user.id`
2. **Call must be pending** - Verify `call.status IN ('pending', 'ringing')`
3. **Call must exist** - Return 404 if call not found
4. **Receiver must exist** - Return error if receiver not found

---

## Error Handling

### Call Not Found (404)
```json
{
  "success": false,
  "error": {
    "code": "CALL_NOT_FOUND",
    "message": "Call not found"
  }
}
```

### Unauthorized (403)
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Only the caller can cancel this call"
  }
}
```

### Call Already Ended (400)
```json
{
  "success": false,
  "error": {
    "code": "CALL_ALREADY_ENDED",
    "message": "Call has already been ended or cancelled"
  }
}
```

---

## Testing Checklist

- [ ] Male calls female
- [ ] Female's phone rings (IncomingCallActivity shows)
- [ ] Male taps "End Call" before female accepts
- [ ] Backend receives POST `/api/v1/calls/{callId}/cancel`
- [ ] Backend updates call status to `cancelled`
- [ ] Backend sends FCM notification to female
- [ ] Female receives FCM notification
- [ ] Female's IncomingCallActivity closes
- [ ] Female navigates to MainActivity
- [ ] No errors in backend logs

---

## FCM Notification Format (CRITICAL!)

**DO NOT use `notification` field - use `data` only!**

### ✅ CORRECT Format:
```json
{
  "token": "fcm_token_here",
  "data": {
    "type": "call_cancelled",
    "callId": "CALL_123",
    "callerId": "USR_456",
    "callerName": "John",
    "reason": "Caller ended call",
    "timestamp": "1700000000000"
  },
  "priority": "high"
}
```

### ❌ WRONG Format (DO NOT USE):
```json
{
  "token": "fcm_token_here",
  "notification": {  // ❌ This prevents onMessageReceived() from being called!
    "title": "Call Cancelled",
    "body": "Caller ended the call"
  },
  "data": {
    "type": "call_cancelled"
  }
}
```

---

## Summary

**What's needed:**
1. ✅ Create `POST /api/v1/calls/{callId}/cancel` endpoint
2. ✅ Update call status to `cancelled` in database
3. ✅ Send FCM notification with `type: "call_cancelled"` to receiver
4. ✅ Emit WebSocket `call:cancelled` event to receiver (if connected)
5. ✅ Validate only caller can cancel
6. ✅ Validate call is in pending/ringing state

**Android app is ready** - Once backend implements this endpoint, the feature will work immediately!









