# 🚀 WebSocket Integration Guide for Backend Team

## 📋 Document For: OnlyCare Backend Team (Laravel PHP)

**Date:** November 22, 2025  
**Purpose:** Add real-time WebSocket signaling for instant call rejection/acceptance  
**Current Setup:** Laravel PHP Backend on DigitalOcean  
**Current Issue:** FCM has 1-5 second delays - users complain calls feel slow

---

## 🎯 What We're Building

### Current Flow (FCM Only - SLOW ❌):
```
User A calls User B
  ↓
POST /api/v1/calls/initiate (Laravel)
  ↓
FCM sends notification (1-5 seconds delay)
  ↓
User B rejects
  ↓
POST /api/v1/calls/{callId}/reject (Laravel)
  ↓
User A polls or FCM (1-5 seconds delay)
  ↓
User A knows call rejected (TOTAL: 6-10 seconds) ❌
```

### New Flow (Laravel + WebSocket - INSTANT ✅):
```
User A calls User B
  ↓
POST /api/v1/calls/initiate (Laravel) + WebSocket emit
  ↓
WebSocket instantly notifies User B (0.05 seconds)
  ↓
User B rejects
  ↓
POST /api/v1/calls/{callId}/reject (Laravel) + WebSocket emit
  ↓
WebSocket instantly notifies User A (0.05 seconds)
  ↓
User A knows call rejected (TOTAL: 0.1 seconds) ✅
```

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                   HYBRID ARCHITECTURE                        │
│                                                              │
│  ┌──────────────────┐         ┌──────────────────────┐     │
│  │  Laravel Backend │         │  Socket.io Server    │     │
│  │  (Existing)      │         │  (New - Node.js)     │     │
│  │                  │         │                      │     │
│  │ • REST APIs      │◄───────►│ • Real-time events   │     │
│  │ • Database       │  HTTP   │ • WebSocket          │     │
│  │ • FCM (backup)   │         │ • User connections   │     │
│  │ • Agora tokens   │         │                      │     │
│  └────────┬─────────┘         └──────────┬───────────┘     │
│           │                              │                  │
│           │                              │                  │
│           └──────────┬───────────────────┘                  │
│                      │                                      │
│                      ▼                                      │
│           ┌──────────────────────┐                         │
│           │   Android App        │                         │
│           │                      │                         │
│           │ • REST API calls     │                         │
│           │   → Laravel          │                         │
│           │                      │                         │
│           │ • WebSocket events   │                         │
│           │   → Socket.io        │                         │
│           └──────────────────────┘                         │
└─────────────────────────────────────────────────────────────┘
```

---

## 📍 STEP 1: Setup Node.js Socket.io Server

### Why Separate Server?

- ✅ Laravel is great for REST APIs but NOT for WebSocket
- ✅ Node.js + Socket.io is the industry standard for real-time
- ✅ Laravel and Node.js will communicate via HTTP
- ✅ Both share the same DigitalOcean server

---

### 1.1 Install Node.js on DigitalOcean

**SSH into your server:**

```bash
ssh root@your_server_ip
```

**Install Node.js 18 LTS:**

```bash
# Update package list
apt update

# Install Node.js 18.x
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
apt-get install -y nodejs

# Verify installation
node -v   # Should show v18.x.x
npm -v    # Should show 9.x.x
```

**Install PM2 (Process Manager):**

```bash
npm install -g pm2
```

---

### 1.2 Create Socket.io Server Directory

```bash
# Create directory
mkdir -p /var/www/onlycare-socket
cd /var/www/onlycare-socket

# Initialize Node.js project
npm init -y
```

---

### 1.3 Install Dependencies

```bash
npm install express socket.io cors dotenv axios
```

---

### 1.4 Create Environment File

**File:** `/var/www/onlycare-socket/.env`

```env
PORT=3001
LARAVEL_API_URL=http://localhost/api/v1
LARAVEL_API_SECRET=your_secret_key_here
NODE_ENV=production
```

**Security Note:** `LARAVEL_API_SECRET` is a shared secret between Laravel and Node.js for internal API calls.

---

### 1.5 Create Socket.io Server

**File:** `/var/www/onlycare-socket/server.js`

```javascript
require('dotenv').config();
const express = require('express');
const http = require('http');
const socketIO = require('socket.io');
const cors = require('cors');
const axios = require('axios');

const app = express();
const server = http.createServer(app);

// Socket.io configuration
const io = socketIO(server, {
    cors: {
        origin: "*", // In production, specify your app's domain
        methods: ["GET", "POST"]
    },
    pingTimeout: 60000,
    pingInterval: 25000
});

app.use(cors());
app.use(express.json());

// ========================================
// DATA STORES
// ========================================

// Store connected users: userId -> socketId
const connectedUsers = new Map();

// Store active calls: callId -> { callerId, receiverId, status, channelName }
const activeCalls = new Map();

// ========================================
// HELPER FUNCTIONS
// ========================================

/**
 * Verify user token with Laravel backend
 */
async function verifyUserToken(token) {
    try {
        const response = await axios.get(`${process.env.LARAVEL_API_URL}/users/me`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json'
            }
        });
        
        if (response.data.success && response.data.data) {
            return response.data.data; // Returns user object
        }
        return null;
    } catch (error) {
        console.error('Token verification failed:', error.message);
        return null;
    }
}

/**
 * Notify Laravel about WebSocket events (optional logging)
 */
async function notifyLaravel(endpoint, data) {
    try {
        await axios.post(`${process.env.LARAVEL_API_URL}${endpoint}`, data, {
            headers: {
                'X-Internal-Secret': process.env.LARAVEL_API_SECRET,
                'Content-Type': 'application/json'
            }
        });
    } catch (error) {
        console.error('Failed to notify Laravel:', error.message);
    }
}

// ========================================
// SOCKET.IO MIDDLEWARE
// ========================================

io.use(async (socket, next) => {
    try {
        const token = socket.handshake.auth.token;
        const userId = socket.handshake.auth.userId;
        
        if (!token || !userId) {
            return next(new Error('Authentication required'));
        }
        
        // Verify token with Laravel
        const user = await verifyUserToken(token);
        
        if (!user || user.id !== userId) {
            return next(new Error('Invalid token'));
        }
        
        // Attach user data to socket
        socket.userId = userId;
        socket.userName = user.name;
        socket.userGender = user.gender;
        
        next();
    } catch (error) {
        next(new Error('Authentication failed'));
    }
});

// ========================================
// SOCKET.IO EVENT HANDLERS
// ========================================

io.on('connection', (socket) => {
    console.log(`✅ User connected: ${socket.userId} (${socket.userName})`);
    
    // Register user
    connectedUsers.set(socket.userId, socket.id);
    
    // Broadcast online status to friends (optional)
    socket.broadcast.emit('user:online', {
        userId: socket.userId,
        timestamp: Date.now()
    });
    
    // =====================================
    // EVENT: call:initiate
    // =====================================
    socket.on('call:initiate', async (data, callback) => {
        try {
            const { receiverId, callId, callType, channelName, agoraToken } = data;
            
            console.log(`📞 Call initiated: ${socket.userId} → ${receiverId} (Type: ${callType})`);
            
            // Check if receiver is connected
            const receiverSocketId = connectedUsers.get(receiverId);
            
            if (!receiverSocketId) {
                // Receiver not connected - fallback to FCM
                callback({ 
                    success: false, 
                    error: 'User offline - FCM notification sent',
                    useFcmFallback: true 
                });
                return;
            }
            
            // Check if receiver is already in a call
            const isReceiverBusy = Array.from(activeCalls.values())
                .some(call => 
                    (call.callerId === receiverId || call.receiverId === receiverId) &&
                    call.status === 'ringing'
                );
            
            if (isReceiverBusy) {
                callback({ 
                    success: false, 
                    error: 'User is busy',
                    busy: true 
                });
                
                // Notify caller immediately
                socket.emit('call:busy', { callId });
                return;
            }
            
            // Store call info
            activeCalls.set(callId, {
                callId,
                callerId: socket.userId,
                callerName: socket.userName,
                receiverId,
                callType,
                channelName,
                status: 'ringing',
                startTime: Date.now()
            });
            
            // Send to receiver INSTANTLY via WebSocket
            io.to(receiverSocketId).emit('call:incoming', {
                callId,
                callerId: socket.userId,
                callerName: socket.userName,
                callType,
                channelName,
                agoraToken, // Receiver needs this to join
                timestamp: Date.now()
            });
            
            console.log(`✅ Call signal sent to receiver: ${receiverId}`);
            
            // Send success response to caller
            callback({
                success: true,
                callId,
                message: 'Call initiated successfully'
            });
            
            // Auto-timeout after 30 seconds
            setTimeout(() => {
                const call = activeCalls.get(callId);
                if (call && call.status === 'ringing') {
                    // Call not answered - notify both parties
                    const callerSocketId = connectedUsers.get(call.callerId);
                    
                    if (callerSocketId) {
                        io.to(callerSocketId).emit('call:timeout', {
                            callId,
                            reason: 'No answer'
                        });
                    }
                    
                    if (receiverSocketId) {
                        io.to(receiverSocketId).emit('call:cancelled', {
                            callId,
                            reason: 'Timeout'
                        });
                    }
                    
                    activeCalls.delete(callId);
                    console.log(`⏱️ Call ${callId} timed out`);
                }
            }, 30000);
            
        } catch (error) {
            console.error('Error in call:initiate:', error);
            callback({ success: false, error: 'Server error' });
        }
    });
    
    // =====================================
    // EVENT: call:accept ⭐ CRITICAL
    // =====================================
    socket.on('call:accept', (data) => {
        try {
            const { callId } = data;
            const call = activeCalls.get(callId);
            
            if (!call) {
                console.log(`❌ Call ${callId} not found`);
                return;
            }
            
            console.log(`✅ Call accepted: ${callId}`);
            
            // Update call status
            call.status = 'accepted';
            call.acceptedAt = Date.now();
            activeCalls.set(callId, call);
            
            // Notify caller INSTANTLY
            const callerSocketId = connectedUsers.get(call.callerId);
            if (callerSocketId) {
                io.to(callerSocketId).emit('call:accepted', {
                    callId,
                    timestamp: Date.now()
                });
                
                console.log(`✅ Caller ${call.callerId} notified: call accepted`);
            }
            
        } catch (error) {
            console.error('Error in call:accept:', error);
        }
    });
    
    // =====================================
    // EVENT: call:reject ⭐⭐ MOST CRITICAL
    // =====================================
    socket.on('call:reject', (data) => {
        try {
            const { callId, reason } = data;
            const call = activeCalls.get(callId);
            
            if (!call) {
                console.log(`❌ Call ${callId} not found`);
                return;
            }
            
            console.log(`❌ Call rejected: ${callId} - Reason: ${reason || 'User declined'}`);
            
            // Notify caller INSTANTLY (0.05 seconds!)
            const callerSocketId = connectedUsers.get(call.callerId);
            if (callerSocketId) {
                io.to(callerSocketId).emit('call:rejected', {
                    callId,
                    reason: reason || 'User declined',
                    timestamp: Date.now()
                });
                
                console.log(`✅ Caller ${call.callerId} notified INSTANTLY: call rejected`);
            }
            
            // Remove call from active calls
            activeCalls.delete(callId);
            
            // Send confirmation to receiver
            socket.emit('call:reject:confirmed', { callId });
            
        } catch (error) {
            console.error('Error in call:reject:', error);
        }
    });
    
    // =====================================
    // EVENT: call:end
    // =====================================
    socket.on('call:end', (data) => {
        try {
            const { callId } = data;
            const call = activeCalls.get(callId);
            
            if (!call) return;
            
            console.log(`📴 Call ended: ${callId}`);
            
            // Determine other user
            const otherUserId = call.callerId === socket.userId 
                ? call.receiverId 
                : call.callerId;
            
            const otherSocketId = connectedUsers.get(otherUserId);
            
            // Notify other user
            if (otherSocketId) {
                io.to(otherSocketId).emit('call:ended', {
                    callId,
                    endedBy: socket.userId,
                    reason: 'Remote user ended call',
                    timestamp: Date.now()
                });
            }
            
            // Remove call
            activeCalls.delete(callId);
            
        } catch (error) {
            console.error('Error in call:end:', error);
        }
    });
    
    // =====================================
    // EVENT: disconnect
    // =====================================
    socket.on('disconnect', () => {
        console.log(`❌ User disconnected: ${socket.userId}`);
        
        // Remove from connected users
        connectedUsers.delete(socket.userId);
        
        // End any active calls
        activeCalls.forEach((call, callId) => {
            if (call.callerId === socket.userId || call.receiverId === socket.userId) {
                // Notify other party
                const otherUserId = call.callerId === socket.userId 
                    ? call.receiverId 
                    : call.callerId;
                
                const otherSocketId = connectedUsers.get(otherUserId);
                if (otherSocketId) {
                    io.to(otherSocketId).emit('call:ended', {
                        callId,
                        reason: 'User disconnected',
                        timestamp: Date.now()
                    });
                }
                
                activeCalls.delete(callId);
            }
        });
        
        // Broadcast offline status
        socket.broadcast.emit('user:offline', {
            userId: socket.userId,
            timestamp: Date.now()
        });
    });
});

// ========================================
// HTTP ENDPOINTS (For Laravel Integration)
// ========================================

// Health check
app.get('/health', (req, res) => {
    res.json({
        status: 'OK',
        connectedUsers: connectedUsers.size,
        activeCalls: activeCalls.size,
        timestamp: Date.now()
    });
});

// Check if user is online
app.get('/api/users/:userId/online', (req, res) => {
    const { userId } = req.params;
    const isOnline = connectedUsers.has(userId);
    
    res.json({
        userId,
        isOnline,
        socketId: isOnline ? connectedUsers.get(userId) : null
    });
});

// Get all connected users
app.get('/api/connected-users', (req, res) => {
    const users = Array.from(connectedUsers.keys());
    res.json({
        count: users.length,
        users
    });
});

// Start server
const PORT = process.env.PORT || 3001;
server.listen(PORT, () => {
    console.log(`🚀 Socket.io server running on port ${PORT}`);
    console.log(`📡 Environment: ${process.env.NODE_ENV}`);
    console.log(`🔗 Laravel API: ${process.env.LARAVEL_API_URL}`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('SIGTERM received, closing server...');
    server.close(() => {
        console.log('Server closed');
        process.exit(0);
    });
});
```

---

### 1.6 Start Socket.io Server

```bash
cd /var/www/onlycare-socket

# Start with PM2
pm2 start server.js --name onlycare-socket

# Save PM2 configuration
pm2 save

# Setup PM2 to start on boot
pm2 startup
```

**Verify it's running:**

```bash
pm2 status
# Should show "onlycare-socket" as "online"

curl http://localhost:3001/health
# Should return: {"status":"OK","connectedUsers":0,"activeCalls":0,...}
```

---

## 📍 STEP 2: Configure Nginx (Proxy for WebSocket)

### 2.1 Update Nginx Configuration

**File:** `/etc/nginx/sites-available/onlycare.in`

Add WebSocket proxy configuration:

```nginx
server {
    listen 80;
    server_name onlycare.in www.onlycare.in;
    
    # Existing Laravel configuration
    root /var/www/onlycare/public;
    index index.php index.html;
    
    # Laravel routes
    location / {
        try_files $uri $uri/ /index.php?$query_string;
    }
    
    # PHP-FPM configuration (existing)
    location ~ \.php$ {
        fastcgi_pass unix:/var/run/php/php8.1-fpm.sock;
        fastcgi_index index.php;
        fastcgi_param SCRIPT_FILENAME $realpath_root$fastcgi_script_name;
        include fastcgi_params;
    }
    
    # ⭐ NEW: WebSocket proxy
    location /socket.io/ {
        proxy_pass http://localhost:3001;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket timeouts
        proxy_read_timeout 86400;
        proxy_send_timeout 86400;
    }
}
```

**Test and reload Nginx:**

```bash
nginx -t
systemctl reload nginx
```

---

### 2.2 Test WebSocket Connection

From your local machine:

```bash
# Test WebSocket endpoint is accessible
curl http://onlycare.in/socket.io/?EIO=4&transport=polling
# Should return socket.io handshake data
```

---

## 📍 STEP 3: Laravel Integration (Optional)

### 3.1 Add Helper Function to Check User Online Status

**File:** `app/Services/WebSocketService.php` (Create new)

```php
<?php

namespace App\Services;

use Illuminate\Support\Facades\Http;

class WebSocketService
{
    protected $socketUrl;

    public function __construct()
    {
        $this->socketUrl = config('websocket.url', 'http://localhost:3001');
    }

    /**
     * Check if user is connected to WebSocket
     */
    public function isUserOnline(string $userId): bool
    {
        try {
            $response = Http::timeout(2)->get("{$this->socketUrl}/api/users/{$userId}/online");
            
            if ($response->successful()) {
                return $response->json('isOnline', false);
            }
            
            return false;
        } catch (\Exception $e) {
            \Log::warning("Failed to check WebSocket status: " . $e->getMessage());
            return false;
        }
    }

    /**
     * Get all connected users
     */
    public function getConnectedUsers(): array
    {
        try {
            $response = Http::timeout(2)->get("{$this->socketUrl}/api/connected-users");
            
            if ($response->successful()) {
                return $response->json('users', []);
            }
            
            return [];
        } catch (\Exception $e) {
            \Log::warning("Failed to get connected users: " . $e->getMessage());
            return [];
        }
    }
}
```

---

### 3.2 Update Calls Controller to Check WebSocket Status

**File:** `app/Http/Controllers/Api/CallController.php`

```php
<?php

namespace App\Http\Controllers\Api;

use App\Services\WebSocketService;
use Illuminate\Http\Request;

class CallController extends Controller
{
    protected $webSocketService;

    public function __construct(WebSocketService $webSocketService)
    {
        $this->webSocketService = $webSocketService;
    }

    public function initiateCall(Request $request)
    {
        // Your existing validation...
        $receiverId = $request->input('receiver_id');
        $callType = $request->input('call_type');
        
        // Check if receiver is online via WebSocket
        $isOnlineViaWebSocket = $this->webSocketService->isUserOnline($receiverId);
        
        // Your existing logic to create call in database...
        $call = Call::create([...]);
        
        // Generate Agora token
        $agoraToken = $this->generateAgoraToken($call->channel_name, 0);
        
        // Return response
        return response()->json([
            'success' => true,
            'call' => $call,
            'agora_token' => $agoraToken,
            'channel_name' => $call->channel_name,
            'receiver_online_via_websocket' => $isOnlineViaWebSocket, // Android will use this
        ]);
        
        // NOTE: Android app will handle:
        // - If receiver_online_via_websocket = true → Use WebSocket (instant)
        // - If receiver_online_via_websocket = false → Use FCM (fallback)
    }
}
```

---

### 3.3 Add WebSocket Configuration

**File:** `config/websocket.php` (Create new)

```php
<?php

return [
    'url' => env('WEBSOCKET_URL', 'http://localhost:3001'),
];
```

**File:** `.env`

Add this line:

```env
WEBSOCKET_URL=http://localhost:3001
```

---

## 📍 STEP 4: Testing Guide

### 4.1 Test Socket.io Server

**Test 1: Health Check**

```bash
curl http://localhost:3001/health

# Expected response:
# {"status":"OK","connectedUsers":0,"activeCalls":0,"timestamp":1700000000000}
```

**Test 2: Check User Online (should be false initially)**

```bash
curl http://localhost:3001/api/users/123/online

# Expected response:
# {"userId":"123","isOnline":false,"socketId":null}
```

---

### 4.2 Test with Android App

1. **Build Android app** with WebSocket integration (we'll provide code separately)
2. **Open app on Phone 1** (User A)
   - App connects to Socket.io
   - Check server logs: Should see "User connected: A"
3. **Open app on Phone 2** (User B)
   - App connects to Socket.io
   - Check server logs: Should see "User connected: B"
4. **Test call rejection:**
   - Phone 1 calls Phone 2
   - Phone 2 receives call instantly (check logs: <100ms)
   - Phone 2 clicks "Reject"
   - Phone 1 sees "Call rejected" instantly (check logs: <100ms)

---

### 4.3 Monitor Server Logs

```bash
# Real-time logs
pm2 logs onlycare-socket

# Check for errors
pm2 logs onlycare-socket --err

# Monitor server resources
pm2 monit
```

---

## 📍 STEP 5: Security & Production Considerations

### 5.1 Add SSL Certificate (IMPORTANT)

WebSocket over HTTPS requires SSL:

```bash
# Install Certbot
apt install certbot python3-certbot-nginx

# Get SSL certificate
certbot --nginx -d onlycare.in -d www.onlycare.in

# Certificates will be in:
# /etc/letsencrypt/live/onlycare.in/
```

**Update Nginx to use HTTPS:**

```nginx
server {
    listen 443 ssl http2;
    server_name onlycare.in www.onlycare.in;
    
    ssl_certificate /etc/letsencrypt/live/onlycare.in/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/onlycare.in/privkey.pem;
    
    # WebSocket over HTTPS
    location /socket.io/ {
        proxy_pass http://localhost:3001;
        # ... same proxy settings ...
    }
}
```

---

### 5.2 Firewall Configuration

```bash
# Allow only necessary ports
ufw allow 22    # SSH
ufw allow 80    # HTTP
ufw allow 443   # HTTPS
ufw enable

# Block direct access to Socket.io port
ufw deny 3001   # Only accessible via Nginx proxy
```

---

### 5.3 Rate Limiting

Add to Socket.io server (in `server.js`):

```javascript
// Add after io initialization
const rateLimit = new Map();

io.use((socket, next) => {
    const userId = socket.handshake.auth.userId;
    
    // Check rate limit
    const now = Date.now();
    const userRequests = rateLimit.get(userId) || [];
    const recentRequests = userRequests.filter(time => now - time < 60000); // Last minute
    
    if (recentRequests.length > 100) {
        return next(new Error('Rate limit exceeded'));
    }
    
    rateLimit.set(userId, [...recentRequests, now]);
    next();
});
```

---

## 📍 STEP 6: Monitoring & Maintenance

### 6.1 PM2 Monitoring

```bash
# Check status
pm2 status

# View logs
pm2 logs onlycare-socket --lines 100

# Restart if needed
pm2 restart onlycare-socket

# Check memory usage
pm2 info onlycare-socket
```

---

### 6.2 Setup Log Rotation

```bash
pm2 install pm2-logrotate

# Configure
pm2 set pm2-logrotate:max_size 10M
pm2 set pm2-logrotate:retain 7
```

---

### 6.3 Setup Monitoring Alerts (Optional)

**Install PM2 Plus (free tier):**

```bash
pm2 link your_public_key your_secret_key
```

Get dashboard at: https://app.pm2.io

---

## 📊 Performance Expectations

| Metric | Expected Value |
|--------|----------------|
| **Message Latency** | 10-50ms (vs FCM's 1-5 seconds) |
| **Concurrent Connections** | 1000+ users on 1GB RAM |
| **Memory Usage** | ~100-200MB for 1000 users |
| **CPU Usage** | <5% on idle, <20% under load |
| **Reconnection Time** | <500ms |

---

## 🆘 Troubleshooting

### Issue 1: Socket.io server not starting

```bash
# Check logs
pm2 logs onlycare-socket --err

# Check if port is in use
lsof -i :3001

# Kill process if needed
kill -9 <PID>

# Restart
pm2 restart onlycare-socket
```

---

### Issue 2: Android app can't connect

**Check 1: Server is running**
```bash
pm2 status
curl http://localhost:3001/health
```

**Check 2: Nginx proxy working**
```bash
curl http://onlycare.in/socket.io/?EIO=4&transport=polling
```

**Check 3: Firewall**
```bash
ufw status
# Make sure port 80/443 are allowed
```

---

### Issue 3: High memory usage

```bash
# Check PM2 stats
pm2 info onlycare-socket

# If memory > 500MB, restart
pm2 restart onlycare-socket

# Enable auto-restart on high memory
pm2 start server.js --name onlycare-socket --max-memory-restart 500M
```

---

## 📝 Summary

### What You Built:

✅ **Node.js Socket.io server** running on port 3001  
✅ **Nginx proxy** for WebSocket over HTTPS  
✅ **Real-time call signaling** (10-50ms latency)  
✅ **Instant call rejection/acceptance** notification  
✅ **Laravel integration** (optional status checking)  
✅ **FCM fallback** (if WebSocket unavailable)  
✅ **PM2 monitoring** and auto-restart  
✅ **SSL security** for production

---

### Android App Will:

1. Connect to Socket.io on app start
2. For calls:
   - If receiver online via WebSocket → Use WebSocket (instant)
   - If receiver offline → Use FCM (fallback)
3. Listen for real-time events:
   - `call:incoming` - Receive call
   - `call:accepted` - Caller knows receiver accepted
   - `call:rejected` - **Caller knows INSTANTLY receiver rejected**
   - `call:ended` - Other user ended call

---

### Cost:

- **$0 extra** (runs on your existing DigitalOcean server)
- **Minimal resources** (~100MB RAM for 1000 users)

---

### Support:

For issues, check:
1. PM2 logs: `pm2 logs onlycare-socket`
2. Nginx logs: `tail -f /var/log/nginx/error.log`
3. Laravel logs: `tail -f storage/logs/laravel.log`

---

**Next Step:** Once server is running, Android team will integrate WebSocket client. We'll provide complete Android code separately.

**Questions?** Share this document with your team and let us know if you need help with any step!

---

**Document Version:** 1.0  
**Last Updated:** November 22, 2025  
**Status:** Ready for Implementation ✅









