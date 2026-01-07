# 📱 WebSocket Integration for Android Team

## Simple Guide - OnlyCare Video Calling

**Date:** November 22, 2025  
**Purpose:** Add instant call notifications (100x faster than FCM)

---

## 🎯 What You Need to Know

### WebSocket Server is Live ✅

**URL:** `https://onlycare.in`

Your backend team has set up a WebSocket server. Now you need to connect your Android app to it for **instant call notifications** (50-100ms instead of 5-10 seconds).

---

## 📦 Step 1: Add Library

### In your `build.gradle (Module: app)`:

```gradle
dependencies {
    // Add this line
    implementation 'io.socket:socket.io-client:2.1.0'
}
```

Then **Sync Project**.

---

## 🔌 Step 2: Connect to WebSocket

### Connection URL: `https://onlycare.in`

### When to Connect:
- When user logs in successfully
- When app starts (if user is already logged in)

### Authentication Required:
- **User Token** (Bearer token from login API)
- **User ID** (User's unique ID)

---

## 📡 Step 3: Events You Need to Handle

### **Incoming Events** (From Server → Your App)

| Event Name | When It Happens | What You Do |
|-----------|----------------|-------------|
| `call:incoming` | Someone is calling you | Show incoming call screen |
| `call:accepted` | Other person accepted your call | Join video call (Agora) |
| `call:rejected` | Other person rejected your call | Show "Call rejected" message |
| `call:ended` | Other person ended the call | End call and close screen |
| `call:timeout` | No one answered (30 seconds) | Show "No answer" message |
| `call:busy` | Person is already in another call | Show "User busy" message |

### **Outgoing Events** (Your App → Server)

| Event Name | When to Send | Data to Send |
|-----------|-------------|--------------|
| `call:initiate` | When you start a call | receiverId, callId, callType, channelName, agoraToken |
| `call:accept` | When you accept incoming call | callId |
| `call:reject` | When you reject incoming call | callId, reason |
| `call:end` | When you end active call | callId |

---

## 🔄 Complete Call Flow

### **Scenario 1: You Call Someone**

```
1. User clicks "Call" button
   ↓
2. Call your Laravel API: POST /api/v1/calls/initiate
   → Get: callId, agoraToken, channelName
   ↓
3. Send WebSocket event: "call:initiate"
   → Other person gets "call:incoming" INSTANTLY (50ms)
   ↓
4. Wait for response:
   - If they accept → You get "call:accepted" → Join Agora
   - If they reject → You get "call:rejected" → Show message
   - If timeout (30s) → You get "call:timeout" → Show message
```

### **Scenario 2: Someone Calls You**

```
1. You receive WebSocket event: "call:incoming"
   → Contains: callId, callerName, callType, agoraToken, channelName
   ↓
2. Show incoming call screen with caller's name
   ↓
3. User chooses:
   
   If ACCEPT:
   - Send WebSocket event: "call:accept" with callId
   - Join Agora channel with agoraToken
   - Start video call
   
   If REJECT:
   - Send WebSocket event: "call:reject" with callId
   - Caller gets notified INSTANTLY (50ms)
   - Close screen
```

---

## 📋 Data Format

### When You Receive `call:incoming`:

```json
{
  "callId": "call-123456",
  "callerId": "user-789",
  "callerName": "John Doe",
  "callType": "video",
  "channelName": "channel-123456",
  "agoraToken": "agora-token-here",
  "timestamp": 1700000000000
}
```

**What to do:**
- Show incoming call screen
- Display `callerName`
- Use `agoraToken` to join Agora if accepted
- Use `callId` when sending accept/reject

---

### When You Send `call:initiate`:

```json
{
  "receiverId": "user-789",
  "callId": "call-123456",
  "callType": "video",
  "channelName": "channel-123456",
  "agoraToken": "agora-token-here"
}
```

**Where to get this data:**
- Get `callId`, `agoraToken`, `channelName` from Laravel API response
- `receiverId` is the person you're calling
- `callType` is "video" or "audio"

---

### When You Send `call:accept`:

```json
{
  "callId": "call-123456"
}
```

Simple! Just send the call ID.

---

### When You Send `call:reject`:

```json
{
  "callId": "call-123456",
  "reason": "User declined"
}
```

`reason` can be: "User declined", "User busy", etc.

---

### When You Receive `call:rejected`:

```json
{
  "callId": "call-123456",
  "reason": "User declined",
  "timestamp": 1700000000000
}
```

**What to do:**
- Dismiss calling screen
- Show message: "Call declined"

---

## 🔒 Authentication

### How to Connect:

You need to send:
- **token**: Your user's Bearer token (from login)
- **userId**: Your user's ID

The server will verify with Laravel backend. If invalid, connection will be rejected.

---

## ⚡ Performance

### Before (FCM only):
- Call rejection takes: **6-10 seconds** ❌
- User experience: Slow and frustrating

### After (WebSocket):
- Call rejection takes: **50-100 milliseconds** ✅
- User experience: Instant and smooth 🚀

**That's 100x faster!**

---

## 🛡️ Fallback to FCM

If WebSocket is not connected (user is offline or connection lost):
- Your call will automatically fall back to FCM
- No calls are lost
- FCM will still work as backup

**When to use FCM:**
- WebSocket not connected
- Server returns `useFcmFallback: true`
- User's app is closed

---

## 🧪 Testing Checklist

Test these scenarios before going live:

### Basic Tests:
- [ ] Connect to WebSocket when app opens
- [ ] Call another user
- [ ] Receive incoming call notification
- [ ] Accept call - both users join video
- [ ] Reject call - caller gets instant notification
- [ ] End call - other user gets notification

### Edge Cases:
- [ ] Call when other user is offline (FCM should work)
- [ ] Call when other user is busy (get busy message)
- [ ] Internet connection drops during call
- [ ] App is closed, call comes in (FCM should work)
- [ ] Call timeout after 30 seconds

---

## 📞 Connection Status

### Check if Connected:

You should always know if WebSocket is connected:
- Show online/offline indicator
- If not connected, use FCM fallback
- Auto-reconnect if connection drops

---

## 🔍 Events Summary Table

| Event | Direction | Purpose | Priority |
|-------|-----------|---------|----------|
| `call:incoming` | Server → App | Receive call | 🔴 Critical |
| `call:accepted` | Server → App | Other person accepted | 🔴 Critical |
| `call:rejected` | Server → App | Other person rejected | 🔴 Critical |
| `call:ended` | Server → App | Call ended | 🟡 Important |
| `call:timeout` | Server → App | No answer | 🟡 Important |
| `call:busy` | Server → App | User busy | 🟡 Important |
| `call:initiate` | App → Server | Start a call | 🔴 Critical |
| `call:accept` | App → Server | Accept call | 🔴 Critical |
| `call:reject` | App → Server | Reject call | 🔴 Critical |
| `call:end` | App → Server | End call | 🟡 Important |

---

## 💡 Important Notes

### 1. **Connect Early**
Connect to WebSocket as soon as user logs in. Keep connection alive.

### 2. **Handle Reconnection**
If connection drops (internet issue), automatically reconnect.

### 3. **Use Both Systems**
- **WebSocket** for instant notifications (when online)
- **FCM** for backup (when offline or WebSocket fails)

### 4. **Call Flow**
Always call Laravel API first to create call record, then use WebSocket for signaling.

### 5. **Token Authentication**
Send valid Bearer token. If token is invalid, connection will be rejected.

---

## 📱 User Experience

### Before WebSocket:
```
User A calls User B
  ⏱️ Wait 2-5 seconds (FCM delay)
User B sees call
User B rejects
  ⏱️ Wait 6-10 seconds (FCM delay)
User A sees rejection
Total: 8-15 seconds of waiting 😞
```

### After WebSocket:
```
User A calls User B
  ⚡ 50ms (WebSocket)
User B sees call
User B rejects
  ⚡ 50ms (WebSocket)
User A sees rejection
Total: 100ms - INSTANT! 😊
```

---

## 🎯 Implementation Priority

### Phase 1 (Must Have):
1. Connect to WebSocket on login ✅
2. Listen for `call:incoming` ✅
3. Send `call:accept` or `call:reject` ✅
4. Listen for `call:rejected` when you call someone ✅

### Phase 2 (Should Have):
1. Send `call:initiate` when starting call ✅
2. Listen for `call:accepted` ✅
3. Handle `call:timeout` ✅
4. Handle `call:busy` ✅

### Phase 3 (Nice to Have):
1. Show online/offline indicator
2. Reconnection logic
3. Network error handling

---

## 🆘 Common Issues

### Issue: Can't connect to WebSocket
**Check:**
- Are you using `https://onlycare.in`?
- Is token valid?
- Did you add internet permission?

### Issue: Not receiving events
**Check:**
- Did you setup listeners BEFORE connecting?
- Is connection still active?
- Check Android logs for errors

### Issue: Events work sometimes
**Check:**
- Network connection stable?
- App in background? (handle background mode)

---

## 📞 Questions?

Contact your backend team or check server status:

```bash
# Backend team can check server status:
pm2 status
pm2 logs onlycare-socket
```

---

## ✅ Summary for Android Team

**What You're Building:**
- Connect to WebSocket server at `https://onlycare.in`
- Listen for 6 incoming events (call:incoming, call:rejected, etc.)
- Send 4 outgoing events (call:initiate, call:accept, etc.)
- Get **instant call notifications** (100x faster)

**What You Need:**
- Socket.io library: `io.socket:socket.io-client:2.1.0`
- User's Bearer token
- User's ID

**Testing:**
- Make a call between two devices
- Reject call and verify caller gets instant notification (<1 second)
- Accept call and join video

**Expected Result:**
- Call rejection: 50-100ms (instead of 6-10 seconds)
- Much better user experience 🚀

---

**Ready to Start?**

1. Add Socket.io library to your app
2. Connect to `https://onlycare.in` with user token
3. Listen for `call:incoming`, `call:rejected`, `call:accepted`
4. Send `call:accept` or `call:reject` when user responds
5. Test with two devices

**That's it! Simple and instant! 🎉**

---

**Need Code Examples?**

If you need actual Kotlin code examples, check:
- **🚀_WEBSOCKET_READY_FOR_ANDROID.md** (has complete code)

But start with understanding this guide first!

---

**Document Version:** 1.0  
**For:** Android Development Team  
**Status:** ✅ Production Ready  
**Server URL:** https://onlycare.in









