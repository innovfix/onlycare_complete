# Debug Switch-to-Video Issue

## Problem
Sender stays on audio call, receiver goes to video call.

## Enhanced Logging Added

### Android App (Receiver - Accepter)
```
╔════════════════════════════════════════════════════════════
║ 📤 SENDING: call:upgrade:response (ACCEPTER SIDE)
╠════════════════════════════════════════════════════════════
║ My User ID (accepter): USER_2_ID
║ Receiver ID (original requester): USER_1_ID
║ Old Call ID: CALL_xxx
║ New Call ID: CALL_yyy
╚════════════════════════════════════════════════════════════
```

### Server (WebSocket)
```
╔════════════════════════════════════════════════════════════
║ 📹 SWITCH TO VIDEO RESPONSE RECEIVED
╠════════════════════════════════════════════════════════════
║ Old Call ID: CALL_xxx
║ New Call ID: CALL_yyy
║ From User (accepter): USER_2_ID
║ To User (requester): USER_1_ID
║ Looking up socket for receiverId: USER_1_ID
║ ConnectedUsers map keys: [USER_1_ID, USER_2_ID, ...]
║ Found socket ID: socket_id_123
║ Current socket ID (accepter): socket_id_456
✅ Sending to socket: socket_id_123
```

### Android App (Sender - Requester)
```
╔════════════════════════════════════════════════════════════
║ 📥 RECEIVED: Switch-to-Video ACCEPTED EVENT (SENDER SIDE)
╠════════════════════════════════════════════════════════════
║ Raw JSON: {...}
║ Old Call ID: CALL_xxx
║ New Call ID: CALL_yyy
╚════════════════════════════════════════════════════════════
```

---

## Testing Steps

### 1. Install New APK
```bash
cd android_app
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 2. View Android Logs (Both Devices)
```bash
# Sender device
adb -s <sender_device_id> logcat | grep -E "(WebSocketManager|AudioCallViewModel|╔|║|╚)"

# Receiver device
adb -s <receiver_device_id> logcat | grep -E "(WebSocketManager|AudioCallViewModel|╔|║|╚)"
```

### 3. View Server Logs
```bash
ssh root@64.227.163.211 "pm2 logs onlycare-socket-server --lines 50"
```

Or in real-time:
```bash
ssh root@64.227.163.211 "pm2 logs onlycare-socket-server"
```

### 4. Test Flow
1. User 1 (sender) requests switch to video
2. User 2 (receiver) accepts
3. **Collect logs from:**
   - Sender device (User 1)
   - Receiver device (User 2)
   - Server

---

## What to Look For

### ✅ Expected Flow

**Receiver Device (User 2):**
```
📤 SENDING: call:upgrade:response
   My User ID: USER_2_ID
   Receiver ID: USER_1_ID  ← Should be sender's ID!
```

**Server:**
```
📹 SWITCH TO VIDEO RESPONSE RECEIVED
   From User (accepter): USER_2_ID
   To User (requester): USER_1_ID
   Looking up socket for: USER_1_ID
   Found socket ID: ABC123
   Sending to socket: ABC123  ← Should be sender's socket!
```

**Sender Device (User 1):**
```
📥 RECEIVED: Switch-to-Video ACCEPTED EVENT  ← Should appear here!
```

### ❌ Current Bug

**Receiver Device shows:**
```
📤 SENDING: call:upgrade:response
📥 RECEIVED: Switch-to-Video ACCEPTED EVENT  ← WRONG! Receiver getting their own event!
```

**Sender Device shows:**
```
(No logs - not receiving event)  ← WRONG! Sender should get it!
```

---

## Possible Issues

### 1. Wrong receiverId Being Sent
```
Receiver sends: receiverId = USER_2_ID (their own ID)
Should send: receiverId = USER_1_ID (sender's ID)
```

### 2. Server Looking Up Wrong User
```
Server gets: receiverId = USER_1_ID
But looks up: connectedUsers.get(USER_2_ID)  ← Wrong!
```

### 3. ConnectedUsers Map Wrong
```
Map has: USER_2_ID → socket_123 (sender's socket)
Should be: USER_1_ID → socket_123
```

### 4. Socket.io Broadcasting
```
io.to(socketId).emit()  ← Should send to one socket
But maybe broadcasting to all?
```

---

## Files Modified

### Android
- `WebSocketManager.kt` - Enhanced logging in `acceptSwitchToVideo()` and `handleSwitchToVideoAccepted()`
- `AudioCallViewModel.kt` - Enhanced logging in WebSocket event handler

### Server
- `socket-server/server.js` - Enhanced logging in `call:upgrade:response` handler

---

**Status:** Debugging in progress  
**Next:** Test and collect logs from all three sources
