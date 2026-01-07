# 🚨 URGENT: Backend Must Forward Call Rejections

## Issue

When **Female rejects Male's call**, the Male doesn't see the rejection - he keeps waiting forever.

## Root Cause

Backend is **receiving** the rejection but **NOT forwarding** it to the caller.

---

## What's Happening Now

```
1. Male calls Female
2. Female clicks "Reject"
3. Female's app sends: socket.emit("call:reject", { callId, reason })
4. ✅ Backend receives it
5. ❌ Backend does NOT send it back to Male
6. ❌ Male never knows Female rejected
7. ❌ Male keeps waiting (broken UX!)
```

---

## Fix Required

When backend receives `call:reject`, you MUST forward it to the caller:

```javascript
socket.on('call:reject', async (data) => {
  const { callId, reason } = data;
  
  console.log('📥 Received call:reject from receiver');
  
  // 1. Update database
  await Call.findByIdAndUpdate(callId, {
    status: 'rejected',
    ended_at: new Date()
  });
  
  // 2. Get call to find caller
  const call = await Call.findById(callId);
  const callerId = call.caller_id;
  
  // 3. ⚡ FORWARD to caller (THIS IS MISSING!)
  const callerSocketId = getUserSocketId(callerId);
  
  if (callerSocketId) {
    io.to(callerSocketId).emit('call:rejected', {
      callId: callId,
      reason: reason || 'User declined',
      timestamp: Date.now()
    });
    
    console.log('✅ Rejection forwarded to caller');
  }
});
```

---

## Testing

After implementing, test:

1. Male calls Female
2. Female rejects
3. Male should see "Call Rejected" screen ✅

---

**Without this fix, calls will appear broken to users!**

Please implement ASAP.

---

**Date:** December 3, 2025  
**Priority:** 🔴 **CRITICAL**


