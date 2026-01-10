# Switch-to-Video: WebSocket Event Mismatch Fix ✅

## Problem (User Reported - Again!)

> "still when receiver accept the request, sender is on audio call. but receiver goes on video. fix it"

**After previous fix, still:**
- User 1 (sender) requests switch ✅
- User 2 (receiver) accepts ✅
- User 2 switches to video ✅
- **User 1 STILL stays on audio** ❌

---

## Root Cause #2: Event Name Mismatch

### Server Emits (server.js)
```javascript
io.to(requesterSocketId).emit('call:upgrade:accepted', { ... });
```

### Android Listens For (WebSocketManager.kt)
```kotlin
on("call:switch_video_accept") { args ->  // ❌ WRONG EVENT NAME!
    handleSwitchToVideoAccepted(args.getOrNull(0) as? JSONObject)
}
```

**Result:** Server sends `call:upgrade:accepted`, but Android is listening for `call:switch_video_accept`. They don't match, so User 1 never receives the notification!

---

## The Fix

### Added New Event Listeners in WebSocketManager.kt

**File:** `android_app/app/src/main/java/com/onlycare/app/websocket/WebSocketManager.kt`

**Before (Lines 260-268):**
```kotlin
// Backward-compatible legacy events
on("call:switch_video_request") { args ->
    handleSwitchToVideoRequested(args.getOrNull(0) as? JSONObject)
}
on("call:switch_video_accept") { args ->  // ❌ Old event name
    handleSwitchToVideoAccepted(args.getOrNull(0) as? JSONObject)
}
on("call:switch_video_decline") { args ->
    handleSwitchToVideoDeclined(args.getOrNull(0) as? JSONObject)
}
```

**After (Lines 260-277):**
```kotlin
// ✅ NEW: Server now uses call:upgrade events
on("call:upgrade:request") { args ->
    handleSwitchToVideoRequested(args.getOrNull(0) as? JSONObject)
}
on("call:upgrade:accepted") { args ->  // ✅ NEW EVENT NAME!
    handleSwitchToVideoAccepted(args.getOrNull(0) as? JSONObject)
}
on("call:upgrade:declined") { args ->
    handleSwitchToVideoDeclined(args.getOrNull(0) as? JSONObject)
}

// Backward-compatible legacy events (kept for safety)
on("call:switch_video_request") { args ->
    handleSwitchToVideoRequested(args.getOrNull(0) as? JSONObject)
}
on("call:switch_video_accept") { args ->
    handleSwitchToVideoAccepted(args.getOrNull(0) as? JSONObject)
}
on("call:switch_video_decline") { args ->
    handleSwitchToVideoDeclined(args.getOrNull(0) as? JSONObject)
}
```

---

## Event Flow (Complete)

### 1. User 1 Requests Switch
```
User 1 (Android) → WebSocket → Server
event: "call:upgrade"
data: { oldCallId, newCallId, receiverId }
```

### 2. Server Forwards to User 2
```
Server → WebSocket → User 2 (Android)
event: "call:upgrade:request"  ✅ (Android listens for this now!)
→ User 2 sees dialog
```

### 3. User 2 Accepts
```
User 2 (Android) → WebSocket → Server
event: "call:upgrade:response"
data: { oldCallId, newCallId, senderId, receiverId, accepted: true }
```

### 4. Server Notifies User 1
```
Server → WebSocket → User 1 (Android)
event: "call:upgrade:accepted"  ✅ (Android listens for this now!)
```

### 5. User 1 Receives Notification ✅
```
Android WebSocketManager:
on("call:upgrade:accepted") { ... }  ✅ MATCHES!
→ handleSwitchToVideoAccepted()
→ Emit WebSocketEvent.SwitchToVideoAccepted
→ AudioCallViewModel receives event
→ Update state: currentCallType = "VIDEO"
→ UI switches to VideoCallUI
```

### 6. Both Users on Video ✅
```
User 1: VideoCallUI visible ✅
User 2: VideoCallUI visible ✅
Both: Same Agora session, video enabled ✅
```

---

## Why This Happened

### History of Event Names

1. **Original:** `call:switch_video_accept` (old backend)
2. **Updated:** `call:upgrade:accepted` (new backend in server.js)
3. **Android:** Still listening for old event name ❌

### The Disconnect

- Backend was updated to use `call:upgrade:*` events
- Android was never updated to listen for new events
- Result: Server talking, Android not listening!

---

## Fixes Applied

### 1. Server Fix (Previous)
✅ Fixed `server.js` to notify correct user (receiverId instead of senderId)

### 2. Android Fix (This Fix)
✅ Added listeners for new `call:upgrade:*` events
✅ Kept old `call:switch_video_*` listeners for backward compatibility

---

## Testing

### Expected Behavior Now

**User 1 (Sender):**
```
1. Click "Switch to Video" → Backend creates CALL_456
2. Wait for User 2 to accept
3. ✅ Receive "call:upgrade:accepted" event
4. ✅ Switch to VideoCallUI
5. ✅ See video call
```

**User 2 (Receiver):**
```
1. Receive "call:upgrade:request" event
2. See dialog "Switch to video call?"
3. Click "Accept"
4. ✅ Switch to VideoCallUI
5. ✅ See video call
```

**Result:**
✅ **Both users switch to video together!**

---

## Deployment

### Android App
- ✅ Updated `WebSocketManager.kt`
- ✅ Added `call:upgrade:accepted` listener
- ✅ Added `call:upgrade:request` listener
- ✅ Added `call:upgrade:declined` listener
- ✅ Build successful
- ✅ **Ready to install and test**

### Server
- ✅ Already deployed (previous fix)
- ✅ WebSocket server restarted
- ✅ Using `call:upgrade:*` events

---

## Summary

### Two Fixes Required

**Fix #1 (Server - Previous):**
- Problem: Server notifying wrong user (senderId instead of receiverId)
- Solution: Changed to `connectedUsers.get(receiverId)`

**Fix #2 (Android - This Fix):**
- Problem: Android listening for wrong event name
- Solution: Added listeners for `call:upgrade:*` events

### Result
✅ **Server sends to correct user**
✅ **Android listens for correct events**
✅ **Both users switch to video together!**

---

**Date:** January 10, 2026  
**Issue:** User 1 not receiving switch-to-video acceptance notification  
**Root Cause:** WebSocket event name mismatch  
**Fix:** Added `call:upgrade:accepted` listener in Android  
**Status:** Fixed and Built ✅  
**Ready for Testing:** Yes ✅
