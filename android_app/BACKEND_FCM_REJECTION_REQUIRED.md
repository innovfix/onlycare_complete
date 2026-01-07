# ✅ Use FCM for Call Rejection (Simple Solution)

## What Backend Needs to Do

When **Female rejects Male's call**, the backend must send an FCM notification to Male.

---

## Backend Implementation

### When Receiver Rejects Call:

```javascript
// 1. Receiver sends rejection via API
app.post('/api/v1/calls/:callId/reject', async (req, res) => {
  const { callId } = req.params;
  const { reason } = req.body;
  
  // 2. Get call details
  const call = await Call.findById(callId);
  
  if (!call) {
    return res.status(404).json({ error: 'Call not found' });
  }
  
  // 3. Update database status to REJECTED
  call.status = 'rejected';
  call.ended_at = new Date();
  await call.save();
  
  // 4. Send FCM to CALLER (the person who initiated the call)
  const callerId = call.caller_id;
  
  await sendFCMNotification(callerId, {
    type: 'call_rejected',   // ✅ Must be exactly this
    callId: callId,
    reason: reason || 'User declined'
  });
  
  console.log(`✅ Sent rejection FCM to caller: ${callerId}`);
  
  res.json({ success: true, message: 'Call rejected' });
});
```

---

## FCM Payload Format

**IMPORTANT:** Use `data` payload, NOT `notification` payload!

```json
{
  "to": "CALLER_FCM_TOKEN",
  "data": {
    "type": "call_rejected",
    "callId": "CALL_123456",
    "reason": "User declined"
  }
}
```

**DO NOT use:**
```json
{
  "notification": {  // ❌ WRONG - Don't use this
    "title": "...",
    "body": "..."
  }
}
```

---

## Android App Side (Already Ready!)

The Android app already has:

### 1. FCM Handler (CallNotificationService.kt)

```kotlin
// Already listens for type: "call_rejected"
when (type) {
    "call_rejected" -> {
        handleCallRejected(data)
    }
}
```

### 2. API Polling Fallback

If FCM fails, the app polls every 2 seconds:

```kotlin
// Checks call status via API
when (call.status) {
    "REJECTED" -> {
        // Shows rejection error ✅
    }
}
```

**So as long as backend:**
1. ✅ Sends FCM with `type: "call_rejected"`
2. ✅ Updates database `status = "rejected"`

**The Android app will handle the rejection!**

---

## Testing

### Backend Logs to Add:

```javascript
console.log('📥 Call rejection received');
console.log('   Call ID:', callId);
console.log('   Caller ID:', callerId);
console.log('📤 Sending FCM to caller...');
console.log('✅ FCM sent successfully');
```

### Test Steps:

1. Male calls Female
2. Female clicks "Reject"
3. Backend receives rejection
4. Backend sends FCM to Male
5. Male sees "Call Rejected" screen ✅

---

## Backend Checklist

- [ ] Update call status to `"rejected"` in database
- [ ] Send FCM with `type: "call_rejected"`
- [ ] Use `data` payload (NOT `notification`)
- [ ] Send to CALLER (not receiver!)
- [ ] Include `callId` and `reason` in FCM data

---

**Once backend implements this, call rejection will work in both directions!**

---

**Date:** December 3, 2025  
**Priority:** 🔴 **HIGH**  
**Status:** ⚠️ **Requires Backend Implementation**  
**Android App Status:** ✅ **READY**


