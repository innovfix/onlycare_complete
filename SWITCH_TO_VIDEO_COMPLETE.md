# Switch-to-Video Feature - COMPLETE ✅

## Final Issue & Resolution

### Problem
- ✅ Backend API working (HTTP 200, new call created)
- ✅ Android app sending WebSocket `call:upgrade` event
- ❌ **Receiver not getting dialog box**

### Root Cause
The WebSocket server (`/var/www/onlycare_admin/socket-server/server.js`) was **not handling** the `call:upgrade` event!

The server was listening to:
- `call:initiate` ✅
- `call:accept` ✅
- `call:reject` ✅
- `call:cancel` ✅
- `call:end` ✅
- `call:upgrade` ❌ **MISSING!**

### Solution Applied ✅

Added three new WebSocket event handlers to `server.js`:

#### 1. **call:upgrade** (Request from sender)
```javascript
socket.on('call:upgrade', (data, callback) => {
    const { oldCallId, newCallId, receiverId, senderId } = data;
    
    // Find receiver's socket
    const receiverSocketId = connectedUsers.get(receiverId);
    
    if (receiverSocketId) {
        // Forward request to receiver
        io.to(receiverSocketId).emit('call:upgrade:request', {
            oldCallId,
            newCallId,
            senderId,
            timestamp: Date.now()
        });
        
        callback({ success: true, message: 'Request sent to receiver' });
    } else {
        callback({ success: false, message: 'Receiver is offline' });
    }
});
```

#### 2. **call:upgrade:response** (Accept/Decline from receiver)
```javascript
socket.on('call:upgrade:response', (data) => {
    const { oldCallId, newCallId, senderId, accepted } = data;
    
    const senderSocketId = connectedUsers.get(senderId);
    
    if (senderSocketId) {
        if (accepted) {
            io.to(senderSocketId).emit('call:upgrade:accepted', {
                oldCallId,
                newCallId,
                timestamp: Date.now()
            });
        } else {
            io.to(senderSocketId).emit('call:upgrade:declined', {
                oldCallId,
                newCallId,
                reason: data.reason || 'Not now',
                timestamp: Date.now()
            });
        }
    }
});
```

### Deployment Status ✅

1. ✅ **Backend API** (`CallController.php`)
   - Fixed: `RtcTokenBuilder::RolePublisher` constant
   - Fixed: `calculateBalanceTime` parameters
   - Deployed and caches cleared

2. ✅ **WebSocket Server** (`socket-server/server.js`)
   - Added: `call:upgrade` handler
   - Added: `call:upgrade:response` handler
   - Deployed and PM2 restarted

3. ✅ **Android App** (`ApiDataRepository.kt`)
   - Enhanced error message parsing
   - Build ready for testing

---

## Complete Flow (How It Works)

### Step 1: User Clicks Switch-to-Video Button
```
Male User (Audio Call Screen)
    ↓
Clicks 🎥 button
    ↓
Dialog: "Switch to Video Call?"
    ↓
Clicks "Yes"
```

### Step 2: Android App → Backend API
```
Android App
    ↓
POST /api/v1/calls/switch-to-video
    ↓
Backend validates:
  - Call exists ✅
  - Call is AUDIO ✅
  - Call is ONGOING ✅
  - Male has ≥60 coins ✅
    ↓
Backend creates NEW video call:
  - New Call ID: CALL_xxxxx
  - New Channel: onlycare_xxxxx
  - New Agora Token
  - Status: PENDING
    ↓
Returns: HTTP 200 with new call details
```

### Step 3: Android App → WebSocket Server
```
Android App
    ↓
Emits: call:upgrade
Data: {
  oldCallId: "CALL_audio",
  newCallId: "CALL_video",
  receiverId: "USR_female",
  senderId: "USR_male"
}
    ↓
WebSocket Server receives
    ↓
Finds receiver's socket
    ↓
Emits to receiver: call:upgrade:request
```

### Step 4: Receiver Gets Dialog
```
Female User (Audio Call Screen)
    ↓
Receives: call:upgrade:request
    ↓
AudioCallViewModel updates state:
  showSwitchToVideoRequestDialog = true
    ↓
Dialog appears: "[User] wants to switch to video call"
    ↓
Options: [Accept] [Decline]
```

### Step 5A: Receiver Accepts
```
Female clicks "Accept"
    ↓
Android emits: call:upgrade:response (accepted=true)
    ↓
WebSocket forwards to sender: call:upgrade:accepted
    ↓
Both users:
  - End old audio call (background)
  - Navigate to VideoCallScreen
  - Join new video call channel
    ↓
✅ Video call starts seamlessly!
```

### Step 5B: Receiver Declines
```
Female clicks "Decline"
    ↓
Android emits: call:upgrade:response (accepted=false)
    ↓
WebSocket forwards to sender: call:upgrade:declined
    ↓
Male sees Toast: "Not now"
    ↓
Audio call continues
```

---

## Testing Instructions

### Prerequisites
- Male user with ≥60 coins
- Female user online and in audio call
- Both users have WebSocket connected

### Test Steps

1. **Start Audio Call**
   ```
   Male → Calls Female (Audio)
   Female → Accepts
   Both in audio call ✅
   ```

2. **Request Switch-to-Video**
   ```
   Male → Clicks 🎥 button
   Male → Clicks "Yes" in dialog
   Male → Sees Toast: "Requesting switch to video..."
   ```

3. **Receiver Gets Request**
   ```
   Female → Dialog appears: "[User] wants to switch to video call"
   Female → Options: [Accept] [Decline]
   ```

4. **Test Accept**
   ```
   Female → Clicks "Accept"
   Both → Navigate to video call screen
   Both → Video call starts ✅
   ```

5. **Test Decline** (in another call)
   ```
   Female → Clicks "Decline"
   Male → Sees Toast: "Not now"
   Both → Audio call continues ✅
   ```

### Verification Logs

**Android (Male):**
```bash
adb logcat | grep -E "(📹|SWITCH TO VIDEO|call:upgrade)"
```

**Expected:**
```
📤 SWITCH TO VIDEO API REQUEST
✅ Switch-to-video request validated. New call ID: CALL_xxxxx
📤 Sending switch-to-video request with new callId=CALL_xxxxx
✅ WebSocket request sent
```

**Android (Female):**
```bash
adb logcat | grep -E "(📹|call:upgrade:request)"
```

**Expected:**
```
📹 Switch-to-video requested for oldCallId=CALL_audio, newCallId=CALL_video
```

**WebSocket Server:**
```bash
ssh root@64.227.163.211 "pm2 logs onlycare-socket-server --lines 20"
```

**Expected:**
```
╔════════════════════════════════════════════════════════════
║ 📹 SWITCH TO VIDEO REQUEST RECEIVED
║ Old Call ID: CALL_audio
║ New Call ID: CALL_video
║ Sender ID: USR_male
║ Receiver ID: USR_female
╚════════════════════════════════════════════════════════════
✅ Receiver is online, forwarding request...
```

---

## Summary of All Fixes

### Issue 1: Wrong Agora Constant ✅
**Error:** `Undefined constant RtcTokenBuilder::ROLE_PUBLISHER`
**Fix:** Changed to `RtcTokenBuilder::RolePublisher`
**File:** `backend_admin/app/Http/Controllers/Api/CallController.php:1200`

### Issue 2: Insufficient Coins ✅
**Error:** HTTP 400 - "Insufficient coins for video call. Minimum 60 coins required."
**Fix:** User added more coins (40 → 70)
**Solution:** Backend correctly validates coin balance

### Issue 3: Wrong calculateBalanceTime Parameters ✅
**Error:** `Unsupported operand types: App\Models\User / string`
**Fix:** Changed `calculateBalanceTime($maleUser, 'VIDEO')` to `calculateBalanceTime($maleUser->coin_balance, $videoCallRate)`
**File:** `backend_admin/app/Http/Controllers/Api/CallController.php:1205`

### Issue 4: Missing WebSocket Event Handlers ✅
**Error:** Receiver not getting dialog (WebSocket not forwarding request)
**Fix:** Added `call:upgrade` and `call:upgrade:response` handlers to WebSocket server
**File:** `backend_admin/socket-server/server.js`

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    SWITCH-TO-VIDEO FLOW                      │
└─────────────────────────────────────────────────────────────┘

┌──────────────┐                              ┌──────────────┐
│  Male User   │                              │ Female User  │
│ (Audio Call) │                              │ (Audio Call) │
└──────┬───────┘                              └──────▲───────┘
       │                                             │
       │ 1. Click 🎥 button                         │
       ├─────────────────────────────────────────┐  │
       │                                         │  │
       │ 2. POST /api/v1/calls/switch-to-video  │  │
       ├────────────────────────────────────────►│  │
       │                                         │  │
       │                    ┌────────────────────┤  │
       │                    │  Backend API       │  │
       │                    │  - Validate call   │  │
       │                    │  - Check coins     │  │
       │                    │  - Create new call │  │
       │                    └────────────────────┤  │
       │                                         │  │
       │ 3. HTTP 200 + new call details          │  │
       │◄────────────────────────────────────────┤  │
       │                                         │  │
       │ 4. Emit: call:upgrade                   │  │
       ├────────────────────────────────────────►│  │
       │                                         │  │
       │                    ┌────────────────────┤  │
       │                    │ WebSocket Server   │  │
       │                    │ - Find receiver    │  │
       │                    │ - Forward request  │  │
       │                    └────────────────────┤  │
       │                                         │  │
       │                                         │  │ 5. Emit: call:upgrade:request
       │                                         ├──┤
       │                                         │  │
       │                                         │  │ 6. Show dialog
       │                                         │  │ "Switch to video?"
       │                                         │  │
       │                                         │  │ 7. User clicks "Accept"
       │                                         │  │
       │                                         │  │ 8. Emit: call:upgrade:response
       │                                         │◄─┤
       │                                         │  │
       │                    ┌────────────────────┤  │
       │                    │ WebSocket Server   │  │
       │                    │ - Forward response │  │
       │                    └────────────────────┤  │
       │                                         │  │
       │ 9. Emit: call:upgrade:accepted          │  │
       │◄────────────────────────────────────────┤  │
       │                                         │  │
       │ 10. Navigate to VideoCallScreen         │  │ 10. Navigate to VideoCallScreen
       ├─────────────────────────────────────────┼──┤
       │                                         │  │
       │ 11. Join new video channel              │  │ 11. Join new video channel
       │                                         │  │
┌──────▼───────┐                              ┌──────▼───────┐
│  Male User   │                              │ Female User  │
│ (Video Call) │◄─────────────────────────────►│ (Video Call) │
└──────────────┘      12. Video call active   └──────────────┘
```

---

## Files Modified

### Backend
1. `backend_admin/app/Http/Controllers/Api/CallController.php`
   - Fixed Agora constant
   - Fixed calculateBalanceTime parameters
   - Added requestSwitchToVideo method

2. `backend_admin/routes/api.php`
   - Added route: `POST /api/v1/calls/switch-to-video`

3. `backend_admin/database/migrations/2026_01_10_120000_add_upgraded_from_call_id_to_calls_table.php`
   - Added `upgraded_from_call_id` column to track call upgrades

4. `backend_admin/socket-server/server.js`
   - Added `call:upgrade` event handler
   - Added `call:upgrade:response` event handler

### Android App
1. `android_app/app/src/main/java/com/onlycare/app/data/remote/api/CallApiService.kt`
   - Added `requestSwitchToVideo` API method

2. `android_app/app/src/main/java/com/onlycare/app/data/remote/dto/CallDto.kt`
   - Added `SwitchToVideoRequest` DTO
   - Added `SwitchToVideoResponse` DTO
   - Added `SwitchToVideoData` DTO

3. `android_app/app/src/main/java/com/onlycare/app/data/repository/ApiDataRepository.kt`
   - Added `requestSwitchToVideo` method
   - Enhanced error message parsing

4. `android_app/app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallViewModel.kt`
   - Added `requestSwitchToVideo` method
   - Added `acceptSwitchToVideo` method
   - Added `declineSwitchToVideo` method
   - Added state variables for switch-to-video

5. `android_app/app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallScreen.kt`
   - Added switch-to-video button (🎥)
   - Added confirmation dialog
   - Added request dialog
   - Added Toast messages
   - Added navigation logic

6. `android_app/app/src/main/java/com/onlycare/app/websocket/WebSocketManager.kt`
   - Added `requestSwitchToVideo` method
   - Added `acceptSwitchToVideo` method
   - Added `declineSwitchToVideo` method
   - Added event handlers for responses

7. `android_app/app/src/main/java/com/onlycare/app/websocket/WebSocketEvents.kt`
   - Added `SwitchToVideoRequested` event
   - Added `SwitchToVideoAccepted` event
   - Added `SwitchToVideoDeclined` event

---

## Status: ✅ READY FOR TESTING

All components deployed and ready:
- ✅ Backend API
- ✅ WebSocket Server
- ✅ Android App (build available)

**Test now and it should work!** 🎉

---

**Date:** January 10, 2026  
**Feature:** Switch-to-Video from Audio Call  
**Status:** Complete and Deployed ✅
