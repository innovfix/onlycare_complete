# Duplicate Incoming Calls Issue - Fix Summary

**Date**: November 23, 2025  
**Issue**: Receiver continues to see incoming call notifications after accepting a call  
**Status**: ✅ FIXED (App-side) + 📋 Backend fix documented

---

## 🐛 Problem Description

After accepting an incoming call and navigating to the call screen, the receiver's device was showing the same incoming call notification again, even though no new call was being made. This was causing confusion and disrupting the user experience.

**User Report:**
> "First time I call from one device, I attended - it's fine. But automatically calls again come from that device again, but this time I'm not calling. But in my receiver end, call is coming."

---

## 🔍 Root Cause Analysis

The issue had two contributing factors:

### 1. Backend API Not Filtering by Status
The `GET /calls/incoming` API endpoint was returning **all incoming calls** regardless of their status:
- Calls with status "ringing" ✅ (should be returned)
- Calls with status "accepted" ❌ (should NOT be returned)
- Calls with status "connected" ❌ (should NOT be returned)
- Calls with status "ended" ❌ (should NOT be returned)

### 2. App Not Validating Call Status
The app's polling mechanism was displaying any call returned by the API, without checking if the call status was actually "ringing".

### Flow Diagram

```
BEFORE FIX:
1. Call initiated (status: "ringing") → Receiver sees notification ✅
2. Receiver accepts call → Backend updates status to "accepted"
3. Call screen shown
4. Polling continues (every 3s)
5. Backend returns same call (status: "accepted") ❌
6. App shows notification again ❌

AFTER FIX:
1. Call initiated (status: "ringing") → Receiver sees notification ✅
2. Receiver accepts call → Backend updates status to "accepted"
3. Call screen shown
4. Polling continues (every 3s)
5. Backend returns same call (status: "accepted")
6. App filters it out (not "ringing") ✅
7. No duplicate notification ✅
```

---

## ✅ Fixes Implemented

### App-Side Fixes (Completed)

#### 1. Added Status Filtering in Polling
**File**: `FemaleHomeViewModel.kt` (Line ~243)

**Changes:**
```kotlin
// BEFORE
val latestCall = incomingCalls.firstOrNull { call ->
    !_state.value.processedCallIds.contains(call.id)
}

// AFTER
val latestCall = incomingCalls.firstOrNull { call ->
    call.status.equals("ringing", ignoreCase = true) &&
    !_state.value.processedCallIds.contains(call.id)
}
```

**Impact**: Only calls with status "ringing" will be shown, even if the backend returns other statuses.

#### 2. Enhanced WebSocket Filtering
**File**: `FemaleHomeViewModel.kt` (Line ~73)

**Changes:**
- Added logging to track when calls are filtered out
- Ensured only unprocessed calls trigger notifications
- WebSocket events are always treated as "ringing" status

#### 3. Added Logging for Debugging
Added comprehensive logging to track:
- Call status when received from backend
- When calls are filtered out
- When calls are marked as processed

---

## 📋 Backend Fix Required

While the app-side fixes will prevent duplicate notifications, the backend should still be updated to follow best practices.

**What Needs to Change:**
The `GET /calls/incoming` endpoint should filter to only return calls with status "ringing".

**Detailed Documentation:**
See `DUPLICATE_INCOMING_CALLS_FIX.md` for:
- Complete backend fix instructions
- Code examples (Node.js/Express)
- Testing scenarios
- Alternative solutions

---

## 🧪 Testing Instructions

### Test Case 1: Single Call Acceptance
1. **Setup**: Two devices (Device A = Caller, Device B = Receiver)
2. **Steps**:
   - Device A initiates call to Device B
   - Device B sees incoming call notification ✅
   - Device B clicks "Accept"
   - Device B navigates to call screen ✅
   - Wait 10 seconds (polling runs 3 times)
   - Verify NO duplicate notification appears ✅
3. **Expected**: Only one notification, no duplicates

### Test Case 2: Call Rejection
1. **Setup**: Two devices
2. **Steps**:
   - Device A initiates call to Device B
   - Device B sees incoming call notification ✅
   - Device B clicks "Reject"
   - Wait 10 seconds
   - Verify NO duplicate notification appears ✅
3. **Expected**: Notification dismissed, no reappearance

### Test Case 3: Multiple Sequential Calls
1. **Setup**: Three devices (A, B = Callers, C = Receiver)
2. **Steps**:
   - Device A calls Device C → C accepts → Call screen shown
   - During call, Device B calls Device C
   - Device C should NOT see Device A's notification again
   - Device C MAY see Device B's notification (new call)
3. **Expected**: No duplicate for first call, second call handled correctly

### Test Case 4: Backend Returns Non-Ringing Calls
1. **Setup**: Simulate backend returning accepted/connected calls
2. **Steps**:
   - Accept a call
   - Check logs for: "Incoming calls exist but all are processed or not ringing (filtered out)"
3. **Expected**: App filters out non-ringing calls

---

## 📊 Log Messages to Look For

### Success Indicators
```
FemaleHome: 📞 INCOMING CALL DETECTED
FemaleHome: Status: ringing
FemaleHome: Caller: [name]
```

### Filtering Indicators
```
FemaleHome: Incoming calls exist but all are processed or not ringing (filtered out)
FemaleHome: ⚠️ Incoming call already processed (call ID: [id])
```

### Error Indicators (Should NOT Appear)
```
❌ Multiple "📞 INCOMING CALL DETECTED" for same call ID
❌ Status: accepted/connected/ended in incoming call logs
```

---

## 🔧 Files Modified

| File | Changes | Purpose |
|------|---------|---------|
| `FemaleHomeViewModel.kt` | Added status filtering in polling | Prevent non-ringing calls from showing |
| `FemaleHomeViewModel.kt` | Enhanced WebSocket filtering | Prevent duplicate WebSocket notifications |
| `FemaleHomeViewModel.kt` | Added debug logging | Track call filtering behavior |

---

## 📱 Additional Context

### Agora Token Issue
The logs also showed `Agora Token: ⚠️ NULL/EMPTY!` warnings. This is a **separate backend issue** documented in:
- `BACKEND_FIX_REQUIRED.md`
- `BOTH_SIDES_RINGING_ROOT_CAUSE.md`

The backend's `/calls/incoming` endpoint needs to include:
- `agora_token` ✅
- `agora_app_id` ✅
- `channel_name` ✅

These are required for the receiver to join the Agora call successfully.

### WebSocket Connection Errors
The logs showed `Connection error: {"message":"Invalid token"}`. This indicates a WebSocket authentication issue that should be investigated separately.

---

## ✅ Verification Checklist

Before considering this issue fully resolved:

- [x] App-side status filtering implemented
- [x] WebSocket filtering enhanced
- [x] Debug logging added
- [x] No linter errors
- [x] Backend fix documented
- [ ] Backend fix implemented (by backend team)
- [ ] End-to-end testing completed
- [ ] Agora token issue resolved (separate issue)
- [ ] WebSocket authentication fixed (separate issue)

---

## 🎯 Next Steps

1. **Test the app** with the current fixes to verify duplicate notifications are prevented
2. **Share `DUPLICATE_INCOMING_CALLS_FIX.md`** with the backend team
3. **Monitor logs** for the filtering messages to confirm it's working
4. **Address Agora token issue** (refer to `BACKEND_FIX_REQUIRED.md`)
5. **Investigate WebSocket authentication** errors

---

## 📞 Summary

**What was fixed:**
- ✅ App now filters incoming calls to only show "ringing" status
- ✅ Processed call IDs prevent showing the same call twice
- ✅ Enhanced logging for debugging

**What still needs attention:**
- 📋 Backend should filter `/calls/incoming` by status (documented)
- 📋 Agora token/channel missing in backend response (documented)
- 📋 WebSocket authentication errors (needs investigation)

**Impact:**
Duplicate incoming call notifications should now be **eliminated** on the app side, providing a better user experience.



