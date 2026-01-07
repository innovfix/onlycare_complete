# 🚨 CRITICAL: Backend Call Status Management Issue

**Priority**: CRITICAL  
**Impact**: Incoming calls not appearing on receiver's device  
**Estimated Fix Time**: 15-20 minutes

---

## 📋 Problem Description

The backend is incorrectly managing call status transitions, causing incoming calls to have status "CONNECTING" immediately after creation, instead of staying in "ringing" status until the receiver accepts.

### User Report
> "This time I'm not receiving call, no ringing screen appeared"

---

## 🔍 Root Cause Analysis

### Current Backend Behavior (INCORRECT ❌)

```
1. Caller initiates call
   ↓
2. Backend creates call with status "ringing" (maybe?)
   ↓
3. Backend IMMEDIATELY changes status to "CONNECTING" ❌
   ↓
4. FCM notification sent to receiver
   ↓
5. Receiver polls GET /calls/incoming
   ↓
6. API returns call with status "CONNECTING" ❌
   ↓
7. App filters out "CONNECTING" calls (only shows "ringing")
   ↓
8. NO incoming call notification shown ❌
```

### Evidence from Logs

**FCM Notification Received:**
```
2025-11-23 12:58:12.061 CallNotificationService: callId: CALL_17638828916800
2025-11-23 12:58:12.061 CallNotificationService: callType: AUDIO
2025-11-23 12:58:12.061 CallNotificationService: callerId: USR_17637424324851
```

**Polling API Response (3 seconds later):**
```json
{
  "id": "CALL_17638828916800",
  "caller_id": "USR_17637424324851",
  "status": "CONNECTING",  // ❌ Should be "ringing"!
  "created_at": "2025-11-23 07:28:11"
}
```

**Result:**
```
2025-11-23 12:58:14.557 FemaleHome: Incoming calls fetched: 7 calls
2025-11-23 12:58:14.557 FemaleHome: Incoming calls exist but all are processed or not ringing (filtered out)
```

---

## 📊 Call Status State Machine (How It SHOULD Work)

```
┌─────────────┐
│   CREATED   │ (Call record created)
└─────┬───────┘
      ↓
┌─────────────┐
│   RINGING   │ ◄─── SHOULD STAY HERE until receiver accepts!
└─────┬───────┘      (FCM sent, polling returns this status)
      │
      ├─→ Receiver clicks "Accept" → POST /calls/{id}/accept
      │                                      ↓
      │                              ┌──────────────┐
      │                              │  CONNECTING  │ ← Status changes HERE
      │                              └──────┬───────┘
      │                                     ↓
      │                              ┌──────────────┐
      │                              │  CONNECTED   │ (Both in Agora channel)
      │                              └──────┬───────┘
      │                                     ↓
      │                              ┌──────────────┐
      │                              │    ENDED     │
      │                              └──────────────┘
      │
      ├─→ Receiver clicks "Reject" → POST /calls/{id}/reject
      │                                      ↓
      │                              ┌──────────────┐
      │                              │   REJECTED   │
      │                              └──────────────┘
      │
      └─→ No response (timeout) ───→ ┌──────────────┐
                                     │    MISSED    │
                                     └──────────────┘
```

---

## 🎯 Backend Fixes Required

### Fix 1: Keep Status as "ringing" Until Acceptance

**Current (WRONG):**
```javascript
// POST /calls/initiate
router.post('/calls/initiate', authenticate, async (req, res) => {
    // ... create call ...
    
    const call = await Call.create({
        caller_id: req.user.id,
        receiver_id: req.body.receiver_id,
        call_type: req.body.call_type,
        status: 'CONNECTING',  // ❌ WRONG! Should be 'ringing'
        agora_token: agoraToken,
        channel_name: channelName
    });
    
    // Send FCM...
    
    res.json({ success: true, call, agora_token, channel_name });
});
```

**Correct (RIGHT):**
```javascript
// POST /calls/initiate
router.post('/calls/initiate', authenticate, async (req, res) => {
    // ... create call ...
    
    const call = await Call.create({
        caller_id: req.user.id,
        receiver_id: req.body.receiver_id,
        call_type: req.body.call_type,
        status: 'ringing',  // ✅ Correct! Stays 'ringing' until accepted
        agora_token: agoraToken,
        channel_name: channelName
    });
    
    // Send FCM...
    
    res.json({ success: true, call, agora_token, channel_name });
});
```

### Fix 2: Change Status to "CONNECTING" on Acceptance

**Add to POST /calls/{id}/accept:**
```javascript
router.post('/calls/:callId/accept', authenticate, async (req, res) => {
    const call = await Call.findById(req.params.callId);
    
    if (!call) {
        return res.status(404).json({ success: false, message: 'Call not found' });
    }
    
    // ✅ Update status to CONNECTING when receiver accepts
    call.status = 'CONNECTING';
    await call.save();
    
    // Notify caller via WebSocket that receiver accepted
    // ...
    
    res.json({ success: true, data: call });
});
```

### Fix 3: Filter `/calls/incoming` by Status

**Current (Returns all calls):**
```javascript
router.get('/calls/incoming', authenticate, async (req, res) => {
    const calls = await Call.find({
        receiver_id: req.user.id,
        // ❌ No status filter!
    }).sort({ created_at: -1 });
    
    res.json({ success: true, data: calls });
});
```

**Correct (Filter by ringing status):**
```javascript
router.get('/calls/incoming', authenticate, async (req, res) => {
    const calls = await Call.find({
        receiver_id: req.user.id,
        status: 'ringing'  // ✅ Only return calls that are actively ringing
    }).sort({ created_at: -1 });
    
    res.json({ success: true, data: calls });
});
```

---

## 📱 App-Side Workaround (Temporary)

We've implemented a temporary workaround in the app to accept both "ringing" and "CONNECTING" status:

### Changes in `FemaleHomeViewModel.kt`

```kotlin
// Temporary fix: Accept both "ringing" and "CONNECTING"
val latestCall = incomingCalls.firstOrNull { call ->
    (call.status.equals("ringing", ignoreCase = true) || 
     call.status.equals("CONNECTING", ignoreCase = true)) &&
    !_state.value.processedCallIds.contains(call.id)
}

if (latestCall != null) {
    if (latestCall.status.equals("CONNECTING", ignoreCase = true)) {
        Log.w("FemaleHome", "⚠️ Backend incorrectly set status to CONNECTING (should be ringing)")
    }
    // Show incoming call...
}
```

**This is a TEMPORARY workaround. The backend MUST be fixed!**

---

## 🧪 Testing After Backend Fix

### Test Scenario 1: Normal Call Flow
1. ✅ Caller initiates call → Call created with status "ringing"
2. ✅ Receiver polls `/calls/incoming` → Returns call with status "ringing"
3. ✅ Receiver sees incoming call notification
4. ✅ Receiver clicks "Accept" → Status changes to "CONNECTING"
5. ✅ Both join Agora channel → Status changes to "CONNECTED"
6. ✅ Call ends → Status changes to "ENDED"

### Test Scenario 2: Call Rejection
1. ✅ Caller initiates call → Status "ringing"
2. ✅ Receiver sees notification
3. ✅ Receiver clicks "Reject" → Status changes to "REJECTED"
4. ✅ `/calls/incoming` no longer returns this call

### Test Scenario 3: Multiple Calls
1. ✅ Call A: status "ringing" → Receiver sees it
2. ✅ Call A accepted → status "CONNECTING" → No longer in incoming
3. ✅ Call B: status "ringing" → Receiver sees it
4. ✅ Two different calls handled correctly

---

## 🔧 Additional Backend Issues Found

### Issue 1: Empty Agora Token in FCM

**Problem:** FCM notification contains empty `agoraToken`:
```
agoraToken: "" (empty string)
```

**Impact:** App can't validate call setup without token

**Fix:** Include agora_token in FCM payload:
```javascript
const fcmPayload = {
    data: {
        type: 'incoming_call',
        callId: call._id,
        callerId: call.caller_id,
        callerName: caller.name,
        callerPhoto: caller.profile_image || '',
        channelId: call.channel_name,
        agoraToken: call.agora_token,  // ✅ Include token
        agoraAppId: AGORA_APP_ID,
        callType: call.call_type
    }
};
```

### Issue 2: Status Values Case Sensitivity

**Problem:** Backend returns "CONNECTING" (uppercase), but app expects "connecting" (lowercase)

**Recommendation:** Use consistent casing (lowercase) for all status values:
- "ringing" (not "RINGING")
- "connecting" (not "CONNECTING")
- "connected" (not "CONNECTED")
- "ended" (not "ENDED")
- "rejected" (not "REJECTED")
- "missed" (not "MISSED")

The app handles case-insensitive comparison, but consistency is better.

---

## ✅ Summary

**Current Issue:** Backend sets call status to "CONNECTING" immediately, preventing receiver from seeing incoming call.

**Required Fixes:**
1. ✅ Keep status as "ringing" when call is created
2. ✅ Change status to "CONNECTING" only when receiver accepts
3. ✅ Filter `/calls/incoming` to only return "ringing" calls
4. ✅ Include agora_token in FCM payload

**App Workaround:** Temporarily accepting both "ringing" and "CONNECTING" status (but backend MUST be fixed)

**Priority:** CRITICAL - Incoming calls not working without this fix

---

## 📞 Related Issues

- `DUPLICATE_INCOMING_CALLS_FIX.md` - Why calls keep reappearing
- `BACKEND_FIX_REQUIRED.md` - Missing agora credentials issue
- `DUPLICATE_CALLS_FIX_SUMMARY.md` - Overview of all call fixes

---

**Last Updated:** November 23, 2025



