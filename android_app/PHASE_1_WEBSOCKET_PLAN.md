# 🚀 Phase 1: WebSocket Integration - Complete Plan

## 📋 Overview

This plan will guide you to integrate WebSocket (Socket.io) into your OnlyCare app while keeping FCM as backup.

**Goal:** When receiver rejects call → Caller immediately knows and disconnects

**Timeline:** 1-2 weeks

---

## 🎯 What You'll Achieve

### Current Problem (FCM Only):
```
Caller → Call sent (FCM) → 3 seconds delay → Receiver gets call
Receiver clicks "Reject" → 3 seconds delay → Caller knows
Total: 6+ seconds of wasted time ❌
```

### After WebSocket:
```
Caller → Call sent (WebSocket) → 0.05 seconds → Receiver gets call
Receiver clicks "Reject" → 0.05 seconds → Caller knows INSTANTLY
Total: 0.1 seconds ✅
```

---

## 📊 Architecture Overview

```
┌────────────────────────────────────────────────────────────┐
│                    PHASE 1 ARCHITECTURE                     │
└────────────────────────────────────────────────────────────┘

     ┌─────────────┐                           ┌─────────────┐
     │  Caller     │                           │  Receiver   │
     │  (Android)  │                           │  (Android)  │
     └──────┬──────┘                           └──────┬──────┘
            │                                         │
            │ WebSocket Connection                    │ WebSocket Connection
            │ (Persistent)                            │ (Persistent)
            │                                         │
            └────────────┬────────────────────────────┘
                         │
                         ▼
            ┌────────────────────────┐
            │   Socket.io Server     │
            │   (Node.js Backend)    │
            │                        │
            │  Events:               │
            │  • call:initiate       │
            │  • call:accept         │
            │  • call:reject  ⭐     │
            │  • call:end            │
            │  • user:online         │
            └────────────────────────┘
```

---

## 🗓️ Step-by-Step Plan

---

## 📍 STEP 1: Backend Setup (2-3 hours)

### 1.1 Choose Hosting Option

Pick ONE:

| Option | Cost | Difficulty | Best For |
|--------|------|-----------|----------|
| **DigitalOcean Droplet** | $6/month | Easy | Recommended ⭐ |
| **AWS EC2** | $8-15/month | Medium | Scalable |
| **Heroku** | $7/month | Very Easy | Quick start |
| **Railway.app** | $5/month | Very Easy | Modern |
| **Local (Testing)** | Free | Easy | Development only |

**My Recommendation:** Start with **DigitalOcean** - Best price/performance ratio

---

### 1.2 Server Setup Checklist

#### Option A: DigitalOcean (Recommended)

1. **Create Account**
   - Go to digitalocean.com
   - Sign up (get $200 free credit if first time)

2. **Create Droplet**
   - Click "Create" → "Droplets"
   - Choose: Ubuntu 22.04 LTS
   - Plan: Basic ($6/month - 1GB RAM)
   - Region: Choose closest to your users
   - Authentication: SSH Key (more secure) or Password

3. **Connect to Server**
   ```bash
   ssh root@your_droplet_ip
   ```

4. **Install Node.js**
   ```bash
   curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
   sudo apt-get install -y nodejs
   node -v  # Should show v18.x.x
   ```

5. **Install PM2 (Process Manager)**
   ```bash
   sudo npm install -g pm2
   ```

---

### 1.3 Create Server Project

```bash
# On your server
mkdir onlycare-socket-server
cd onlycare-socket-server
npm init -y
```

---

### 1.4 Install Dependencies

```bash
npm install express socket.io cors dotenv jsonwebtoken agora-access-token
```

---

### 1.5 Create Server Files

You need to create 3 files:

#### File 1: `server.js` (Main server)
#### File 2: `.env` (Environment variables)
#### File 3: `package.json` (Already created, just modify)

I'll give you the FILE STRUCTURE first:

```
onlycare-socket-server/
├── server.js           ← Main Socket.io server
├── .env                ← Configuration (API keys, secrets)
├── package.json        ← Dependencies
└── logs/               ← PM2 will create this
```

---

## 📄 File Contents

### File 1: `server.js`

**Purpose:** Handle all WebSocket connections and call events

**Key Events to Implement:**

```javascript
// Pseudo-code structure - I'll give you actual code files separately

1. Connection/Authentication
   - Verify user JWT token
   - Store user's socket ID
   - Mark user as online

2. Call Events
   ├── call:initiate
   │   └── Check if receiver is online
   │   └── Check if receiver is busy
   │   └── Generate Agora token
   │   └── Send to receiver via WebSocket
   │
   ├── call:reject ⭐ (YOUR MAIN CONCERN)
   │   └── Get call info
   │   └── Notify CALLER that call was rejected
   │   └── Clear call from active calls
   │
   ├── call:accept
   │   └── Notify caller that call was accepted
   │   └── Return Agora token
   │
   └── call:end
       └── Notify other user
       └── Clear call from active calls

3. Disconnect Handling
   - Remove user from online users
   - End any active calls they were in
   - Notify other party
```

---

### File 2: `.env`

```env
PORT=3000
JWT_SECRET=your_jwt_secret_key_here
AGORA_APP_ID=your_agora_app_id
AGORA_APP_CERTIFICATE=your_agora_certificate
NODE_ENV=production
```

---

### File 3: Update `package.json`

Add this to scripts section:

```json
{
  "scripts": {
    "start": "node server.js",
    "dev": "nodemon server.js"
  }
}
```

---

## 📍 STEP 2: Critical Logic - Call Rejection Flow

### Server-Side Logic (What happens when receiver rejects)

```
┌─────────────────────────────────────────────────────────────┐
│          CALL REJECTION FLOW (Step by Step)                 │
└─────────────────────────────────────────────────────────────┘

1. Receiver clicks "Reject" button
   ↓
2. Android app emits: socket.emit("call:reject", { callId: "123" })
   ↓
3. Server receives "call:reject" event
   ↓
4. Server logic:
   a) Find call in activeCalls Map
   b) Get caller's socket ID
   c) Emit to caller: socket.to(callerSocketId).emit("call:rejected", {...})
   d) Delete call from activeCalls Map
   ↓
5. Caller's app receives "call:rejected" event
   ↓
6. Caller's CallViewModel handles event:
   - Stop ringing tone
   - Show "Call rejected" message
   - Navigate back to MainActivity
   - Clear call state
   ↓
7. Done! Both sides clean ✅
```

---

### Key Server Code Structure (call:reject handler)

```javascript
// In server.js

// Map to store active calls
const activeCalls = new Map();
// Map to store connected users
const connectedUsers = new Map(); // userId -> socketId

socket.on('call:reject', (data) => {
    const { callId, reason } = data;
    
    // Step 1: Get call info
    const call = activeCalls.get(callId);
    if (!call) {
        console.log(`Call ${callId} not found`);
        return;
    }
    
    // Step 2: Get caller's socket ID
    const callerSocketId = connectedUsers.get(call.callerId);
    
    if (callerSocketId) {
        // Step 3: Notify caller IMMEDIATELY via WebSocket
        io.to(callerSocketId).emit('call:rejected', {
            callId: callId,
            reason: reason || 'User declined',
            timestamp: Date.now()
        });
        
        console.log(`Call ${callId} rejected, notified caller ${call.callerId}`);
    }
    
    // Step 4: Clean up - remove call from active calls
    activeCalls.delete(callId);
    
    // Step 5: Send confirmation back to receiver
    socket.emit('call:reject:success', {
        callId: callId
    });
});
```

---

## 📍 STEP 3: Android App Changes (Your Side)

### 3.1 File Structure You Need to Create/Modify

```
app/src/main/java/com/onlycare/app/
├── websocket/
│   ├── WebSocketManager.kt          ← NEW (handles Socket.io connection)
│   ├── WebSocketEvents.kt           ← NEW (event data classes)
│   └── ConnectionState.kt           ← NEW (connection status)
│
├── call/
│   ├── CallStateManager.kt          ← MODIFY (integrate WebSocket)
│   └── CallViewModel.kt             ← MODIFY (observe WebSocket events)
│
└── di/
    └── NetworkModule.kt              ← MODIFY (provide WebSocket dependency)
```

---

### 3.2 Add Dependency

**File:** `app/build.gradle.kts`

```kotlin
dependencies {
    // Add this line
    implementation("io.socket:socket.io-client:2.1.0")
    
    // Your existing dependencies...
}
```

---

### 3.3 Key Changes Required

#### Change 1: Create `WebSocketManager.kt`

**Purpose:** Handle all WebSocket communication

**Key Functions:**
```kotlin
class WebSocketManager {
    fun connect(userId: String, authToken: String)
    fun disconnect()
    
    // Call actions
    fun initiateCall(receiverId: String, callType: String)
    fun acceptCall(callId: String)
    fun rejectCall(callId: String, reason: String) ⭐
    fun endCall(callId: String)
    
    // Observe events
    val callEvents: SharedFlow<CallEvent>
    val connectionState: StateFlow<ConnectionState>
}
```

**Critical: Call Rejection Logic**
```kotlin
// When receiver clicks reject button
fun rejectCall(callId: String, reason: String = "User declined") {
    socket?.emit("call:reject", JSONObject().apply {
        put("callId", callId)
        put("reason", reason)
    })
}

// Listen for rejection (caller side)
private fun setupListeners() {
    socket?.on("call:rejected") { args ->
        val data = args[0] as JSONObject
        val callId = data.getString("callId")
        val reason = data.getString("reason")
        
        // Emit event to UI
        _callEvents.tryEmit(CallEvent.CallRejected(
            callId = callId,
            reason = reason
        ))
    }
}
```

---

#### Change 2: Update `CallStateManager.kt`

**Current:** Uses `FcmUtils` singleton

**After:** Uses `WebSocketManager`

**Key Change:**
```kotlin
// OLD CODE (FcmUtils based)
FcmUtils.callStatus.observe(this) { statusPair ->
    // Handle call status
}

// NEW CODE (WebSocket based)
viewModelScope.launch {
    webSocketManager.callEvents.collect { event ->
        when (event) {
            is CallEvent.CallRejected -> {
                // Caller receives this INSTANTLY
                handleCallRejected(event.callId, event.reason)
            }
            // ... other events
        }
    }
}

private fun handleCallRejected(callId: String, reason: String) {
    // Stop ringing/calling tone
    audioManager.stopRingtone()
    
    // Clear active call
    _activeCall.value = null
    
    // Show toast
    showToast("Call rejected: $reason")
    
    // Navigate back to home
    navigateToMainActivity()
}
```

---

## 📍 STEP 4: Testing Strategy

### 4.1 Local Testing Setup

Before deploying to production:

```
┌────────────────────────────────────────────┐
│         LOCAL TESTING SETUP                │
├────────────────────────────────────────────┤
│                                            │
│  Your Computer:                            │
│  ├── Run Socket.io server locally          │
│  │   (localhost:3000)                      │
│  │                                         │
│  └── Use ngrok to expose to internet       │
│      (https://abc123.ngrok.io)            │
│                                            │
│  Android Phones:                           │
│  ├── Phone 1 (Caller) → Connect to ngrok  │
│  └── Phone 2 (Receiver) → Connect to ngrok│
│                                            │
└────────────────────────────────────────────┘
```

**Steps:**

1. **Run server locally:**
   ```bash
   cd onlycare-socket-server
   node server.js
   # Server running on http://localhost:3000
   ```

2. **Expose with ngrok:**
   ```bash
   ngrok http 3000
   # You'll get: https://abc123.ngrok.io
   ```

3. **Update Android app:**
   ```kotlin
   // In WebSocketManager
   private val SERVER_URL = "https://abc123.ngrok.io"
   ```

4. **Test call rejection:**
   - Phone 1: Initiate call
   - Phone 2: Receive call → Click Reject
   - Phone 1: Should INSTANTLY see "Call rejected"

---

### 4.2 Test Cases Checklist

#### ✅ Test Case 1: Basic Rejection
- [ ] User A calls User B
- [ ] User B receives call notification
- [ ] User B clicks "Reject"
- [ ] User A sees "Call rejected" within 100ms
- [ ] Both apps return to home screen
- [ ] No memory leaks

#### ✅ Test Case 2: Network Issues
- [ ] User A calls User B
- [ ] Turn off User B's WiFi/data
- [ ] Wait 30 seconds (timeout)
- [ ] User A sees "No answer" message

#### ✅ Test Case 3: App Killed
- [ ] User A calls User B
- [ ] User B's app is killed
- [ ] User B receives FCM notification (fallback)
- [ ] User B clicks "Reject" on notification
- [ ] User A receives rejection via FCM (since WebSocket not connected)

#### ✅ Test Case 4: Simultaneous Calls
- [ ] User A calls User B
- [ ] User C tries to call User B
- [ ] User C sees "User is busy"
- [ ] User B only sees User A's call

---

## 📍 STEP 5: Deployment (Production)

### 5.1 Deploy to DigitalOcean

```bash
# On your server
cd onlycare-socket-server

# Install dependencies
npm install

# Start with PM2 (keeps running forever)
pm2 start server.js --name onlycare-socket

# Save PM2 configuration
pm2 save

# Setup PM2 to start on server reboot
pm2 startup
```

---

### 5.2 Setup Domain (Optional but Recommended)

Instead of using IP address, use a domain:

**Option 1: Free Subdomain (easiest)**
- Use your existing domain
- Add A record: `socket.onlycare.com → your_server_ip`

**Option 2: Buy New Domain**
- Namecheap: $8/year
- Point to your DigitalOcean IP

---

### 5.3 Setup SSL (HTTPS Required)

```bash
# Install Certbot
sudo apt install certbot

# Get SSL certificate (free)
sudo certbot certonly --standalone -d socket.onlycare.com

# Certificates will be in:
# /etc/letsencrypt/live/socket.onlycare.com/
```

**Update server.js to use HTTPS:**
```javascript
const https = require('https');
const fs = require('fs');

const server = https.createServer({
    key: fs.readFileSync('/etc/letsencrypt/live/socket.onlycare.com/privkey.pem'),
    cert: fs.readFileSync('/etc/letsencrypt/live/socket.onlycare.com/fullchain.pem')
}, app);
```

---

### 5.4 Update Android App with Production URL

```kotlin
// In WebSocketManager.kt
companion object {
    private const val SERVER_URL = "https://socket.onlycare.com"
    // OR if using IP:
    // private const val SERVER_URL = "https://your.server.ip.address:3000"
}
```

---

## 📍 STEP 6: Monitoring & Debugging

### 6.1 Server Logs

```bash
# View real-time logs
pm2 logs onlycare-socket

# View specific errors
pm2 logs onlycare-socket --err

# Clear logs
pm2 flush
```

---

### 6.2 Monitor Server Health

```bash
# Check if server is running
pm2 status

# Server resource usage
pm2 monit

# Restart server
pm2 restart onlycare-socket
```

---

### 6.3 Android Debug Logging

Add logging to track WebSocket events:

```kotlin
// In WebSocketManager
private fun setupListeners() {
    socket?.on("call:rejected") { args ->
        Log.d("WebSocket", "Call rejected received: ${args[0]}")
        // ... handle event
    }
    
    socket?.on(Socket.EVENT_DISCONNECT) {
        Log.w("WebSocket", "Disconnected from server")
    }
}
```

---

## 🎯 Integration Checklist

### Backend Tasks:
- [ ] Choose hosting provider (DigitalOcean recommended)
- [ ] Create server instance
- [ ] Install Node.js
- [ ] Create server.js with Socket.io
- [ ] Implement call:reject handler ⭐
- [ ] Implement call:accept handler
- [ ] Implement call:end handler
- [ ] Test locally with ngrok
- [ ] Deploy to production
- [ ] Setup SSL certificate
- [ ] Configure PM2 for auto-restart

### Android Tasks:
- [ ] Add Socket.io dependency
- [ ] Create WebSocketManager.kt
- [ ] Implement connection logic
- [ ] Implement call:reject emission ⭐
- [ ] Listen for call:rejected event ⭐
- [ ] Update CallStateManager to use WebSocket
- [ ] Handle rejection in UI (navigate back, show toast)
- [ ] Keep FCM as fallback (don't remove)
- [ ] Test on 2 physical devices
- [ ] Handle edge cases (network loss, timeout)

### Integration Testing:
- [ ] Test WebSocket connection on app start
- [ ] Test call rejection (instant notification)
- [ ] Test call acceptance
- [ ] Test app killed scenario (FCM fallback)
- [ ] Test poor network conditions
- [ ] Test battery consumption
- [ ] Load test with multiple users

---

## 📚 Files I'll Provide You

When you're ready to implement, I'll give you these complete files:

1. **`server.js`** - Complete Socket.io server with all handlers
2. **`WebSocketManager.kt`** - Android Socket.io client
3. **`WebSocketEvents.kt`** - Data classes for all events
4. **`CallStateManager.kt`** - Updated version using WebSocket
5. **`deployment-guide.md`** - Step-by-step deployment instructions

---

## 💰 Cost Breakdown

| Item | Cost |
|------|------|
| DigitalOcean Droplet (1GB) | $6/month |
| Domain (optional) | $8/year |
| SSL Certificate | FREE (Let's Encrypt) |
| **Total Monthly Cost** | **~$6-7** |

---

## ⏱️ Time Estimate

| Phase | Time |
|-------|------|
| Backend setup (DigitalOcean + Node.js) | 2-3 hours |
| Write server.js code | 3-4 hours |
| Android WebSocketManager | 4-5 hours |
| Integration with existing code | 3-4 hours |
| Testing & debugging | 4-6 hours |
| **Total** | **16-22 hours** |

If working 2 hours/day = 8-11 days  
If working 4 hours/day = 4-6 days

---

## 🎯 Success Criteria

Your Phase 1 is complete when:

✅ Caller initiates call → Receiver sees it in <100ms (WebSocket)  
✅ Receiver rejects call → Caller knows in <100ms ⭐  
✅ Receiver accepts call → Caller knows in <100ms  
✅ Either user ends call → Other knows immediately  
✅ If app is killed → FCM still works (fallback)  
✅ If server is down → FCM fallback activates  
✅ No crashes, no memory leaks  
✅ Battery consumption is acceptable  

---

## 🚨 Common Pitfalls to Avoid

### 1. **Not handling disconnections**
- Always implement reconnection logic
- Use Socket.io's built-in reconnection

### 2. **Not validating call states**
- Always check if call exists before processing
- Validate user permissions

### 3. **Forgetting to clean up**
- Remove calls from `activeCalls` Map
- Clean up event listeners

### 4. **No timeout handling**
- Set 30-second timeout for unanswered calls
- Auto-cleanup stale connections

### 5. **Security issues**
- Always validate JWT tokens
- Don't trust client-side data
- Rate limit connection attempts

---

## 🤝 What I Need From You to Proceed

Before I give you the actual code files, tell me:

1. **Backend Choice:**
   - [ ] I'll use DigitalOcean ($6/month)
   - [ ] I'll use AWS/Heroku/other
   - [ ] I'll test locally first

2. **Current Backend:**
   - Do you already have a backend server?
   - What language? (Node.js/Java/Python/PHP?)
   - Can you add Socket.io to it?

3. **Database:**
   - Where do you store user data?
   - Can your server access it?

4. **Agora Setup:**
   - Do you have Agora App ID and Certificate?
   - Do you currently generate tokens on backend or Android?

5. **Testing:**
   - Do you have 2 Android phones to test?
   - Are they on same WiFi or different networks?

---

## 📞 Next Steps

**Once you answer the questions above, I'll provide:**

1. ✅ Complete `server.js` tailored to your setup
2. ✅ Complete `WebSocketManager.kt` for Android
3. ✅ Step-by-step deployment instructions
4. ✅ Testing scripts and commands
5. ✅ Troubleshooting guide

**For now, you can start with:**
- Setting up DigitalOcean account
- Creating a droplet
- Installing Node.js
- Getting familiar with PM2

---

## 📝 Summary

**Problem:** Receiver rejects call → Caller doesn't know for 3-5 seconds (FCM delay)

**Solution:** WebSocket sends rejection INSTANTLY (50ms)

**Implementation:**
1. Backend: Socket.io server handles `call:reject` event
2. Android: WebSocketManager emits rejection, caller listens
3. Result: Instant notification, both sides clean up immediately

**Cost:** ~$6/month

**Time:** 1-2 weeks

**Difficulty:** Medium (I'll guide you through everything)

---

Ready to start? Tell me your backend choice and I'll give you the actual code! 🚀



