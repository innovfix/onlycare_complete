# 🚨 URGENT: Duplicate Incoming Call Notifications - Backend Fix Required

**Priority**: HIGH  
**Impact**: Receivers continue seeing incoming call notifications even after accepting the call  
**Estimated Fix Time**: 5-10 minutes

---

## 📋 Problem Description

After a receiver accepts an incoming call, their device **continues to receive incoming call notifications** for the same call. This happens because the backend's `GET /calls/incoming` API endpoint is returning calls that are no longer in the "ringing" state.

### User Report
> "First time I call from one device, I attended - it's fine. But automatically calls again come from that device again, but this time I'm not calling. But in my receiver end, call is coming."

---

## 🔍 Root Cause

The `GET /calls/incoming` API endpoint is returning **ALL incoming calls** regardless of their status, including:
- ✅ Calls with status "ringing" (SHOULD be returned)
- ❌ Calls with status "accepted" (should NOT be returned)
- ❌ Calls with status "connected" (should NOT be returned)
- ❌ Calls with status "ended" (should NOT be returned)

### What's Happening

```
1. Caller initiates call → Backend creates call with status "ringing"
   ↓
2. Receiver polls GET /calls/incoming → Receives call ✅
   ↓
3. Receiver clicks "Accept" → Backend updates call status to "accepted"
   ↓
4. Receiver navigates to call screen
   ↓
5. Polling continues every 3 seconds
   ↓
6. GET /calls/incoming STILL returns this call ❌
   ↓
7. Receiver sees DUPLICATE incoming call notification ❌
```

---

## 🎯 Backend Fix Required

### API Endpoint to Fix

```
Method:  GET
URL:     https://onlycare.in/api/v1/calls/incoming
Headers: Authorization: Bearer {user_token}
```

### Current Behavior (INCORRECT ❌)

The endpoint returns calls in **any status**:

```json
{
  "success": true,
  "data": [
    {
      "id": "67895d8d9c9e12a1b4e3f7c8",
      "caller_id": "user_123",
      "caller_name": "John Doe",
      "status": "accepted",  // ❌ Should NOT be returned!
      "call_type": "AUDIO",
      ...
    },
    {
      "id": "67895d8d9c9e12a1b4e3f7c9",
      "caller_id": "user_456",
      "caller_name": "Jane Smith",
      "status": "ringing",  // ✅ Should be returned
      "call_type": "VIDEO",
      ...
    }
  ]
}
```

### Required Behavior (CORRECT ✅)

The endpoint should **ONLY return calls with status "ringing"**:

```json
{
  "success": true,
  "data": [
    {
      "id": "67895d8d9c9e12a1b4e3f7c9",
      "caller_id": "user_456",
      "caller_name": "Jane Smith",
      "status": "ringing",  // ✅ Only "ringing" calls
      "call_type": "VIDEO",
      ...
    }
  ]
}
```

### Backend Code Change Needed

**BEFORE (Incorrect):**
```javascript
// Example Node.js/Express code
router.get('/calls/incoming', authenticate, async (req, res) => {
  const calls = await Call.find({
    receiver_id: req.user.id,
    // ❌ No status filter!
  }).sort({ created_at: -1 });
  
  res.json({ success: true, data: calls });
});
```

**AFTER (Correct):**
```javascript
// Example Node.js/Express code
router.get('/calls/incoming', authenticate, async (req, res) => {
  const calls = await Call.find({
    receiver_id: req.user.id,
    status: 'ringing'  // ✅ Filter for only "ringing" status
  }).sort({ created_at: -1 });
  
  res.json({ success: true, data: calls });
});
```

---

## 📱 App-Side Fix (Already Implemented)

We've added **defensive filtering** in the app to handle this backend issue:

### Changes in `FemaleHomeViewModel.kt`

#### 1. Polling Filter (Line ~243)
```kotlin
// Filter: Only show calls with status "ringing" that haven't been processed
val latestCall = incomingCalls.firstOrNull { call ->
    call.status.equals("ringing", ignoreCase = true) &&
    !_state.value.processedCallIds.contains(call.id)
}
```

#### 2. WebSocket Filter (Line ~73)
```kotlin
// Filter: Only show calls that haven't been processed
// WebSocket events are always "ringing" status by design
if (!_state.value.processedCallIds.contains(event.callId)) {
    // Show incoming call notification
}
```

These app-side changes will **mitigate** the issue, but the backend fix is still required for:
- Reducing unnecessary data transfer
- Improving API response time
- Preventing other potential issues
- Following API best practices

---

## 🧪 Testing After Backend Fix

### Test Scenario 1: Single Call Flow
1. ✅ Caller initiates call → Receiver sees notification
2. ✅ Receiver accepts → Navigates to call screen
3. ✅ Polling continues → NO duplicate notification
4. ✅ Call ends → Notification cleared

### Test Scenario 2: Multiple Calls
1. ✅ Caller A initiates call → Receiver sees notification
2. ✅ Receiver accepts Call A → Navigates to call screen
3. ✅ Caller B initiates call during Call A → Notification queued
4. ✅ Call A ends → Receiver sees Call B notification
5. ✅ No duplicate notifications for Call A

### Test Scenario 3: Rejected Calls
1. ✅ Caller initiates call → Receiver sees notification
2. ✅ Receiver rejects → Notification dismissed
3. ✅ Polling continues → NO duplicate notification

---

## 🔧 Alternative Backend Solutions

If filtering by status is not feasible, consider these alternatives:

### Option 1: Time-based Filter
Only return calls created in the last 60 seconds:
```javascript
const calls = await Call.find({
  receiver_id: req.user.id,
  status: 'ringing',
  created_at: { $gte: new Date(Date.now() - 60000) }
});
```

### Option 2: Delete "ringing" calls on acceptance
When a call is accepted, delete the "ringing" call record:
```javascript
// In POST /calls/{callId}/accept
await Call.findByIdAndDelete(callId);
// Then create a new "active_calls" record
```

However, **Option 1 (status filtering)** is the recommended and standard approach.

---

## ✅ Summary

**Issue**: `/calls/incoming` returns calls in any status, causing duplicate notifications.

**Root Cause**: Missing status filter in backend query.

**Solution**: Add `status: 'ringing'` filter to the backend query.

**App Changes**: Already implemented defensive filtering as a workaround.

**Backend Fix Priority**: HIGH (should be fixed to prevent other issues and improve performance)

---

## 📞 Contact

If you have questions about this issue, please contact the mobile app team or refer to the call flow documentation.

**Related Files**:
- `FemaleHomeViewModel.kt` (app-side filtering)
- `IncomingCallService.kt` (notification service)
- `CallApiService.kt` (API interface)



