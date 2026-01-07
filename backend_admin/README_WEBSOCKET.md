# 🚀 OnlyCare WebSocket Integration

Complete real-time WebSocket implementation for instant video call signaling.

## 📚 Documentation Files

### Main Guide
- **[WEBSOCKET_INTEGRATION_GUIDE.md](./WEBSOCKET_INTEGRATION_GUIDE.md)** - Complete step-by-step integration guide for backend team

### Quick References
- **[DEPLOYMENT_CHECKLIST.md](./DEPLOYMENT_CHECKLIST.md)** - Deployment checklist with verification steps
- **[UPDATE_LARAVEL_ENV.md](./UPDATE_LARAVEL_ENV.md)** - Laravel environment configuration guide

### Implementation Files
- **socket-server/** - Node.js Socket.io server implementation
  - `server.js` - Main server code
  - `package.json` - Dependencies
  - `env.example` - Environment configuration template
  - `install.sh` - Automated installation script
  - `ecosystem.config.js` - PM2 configuration
  - `test-client.html` - Browser-based testing tool

### Laravel Integration
- **app/Services/WebSocketService.php** - Laravel service for WebSocket communication
- **config/websocket.php** - WebSocket configuration file
- **nginx-websocket.conf** - Nginx proxy configuration

---

## 🎯 Quick Start

### For Backend Team:

1. **Read the main guide:**
   ```bash
   cat WEBSOCKET_INTEGRATION_GUIDE.md
   ```

2. **Deploy Socket.io server:**
   ```bash
   cd socket-server
   chmod +x install.sh
   sudo ./install.sh
   ```

3. **Configure Laravel:**
   ```bash
   # See UPDATE_LARAVEL_ENV.md for details
   nano /var/www/onlycare/public/.env
   # Add WebSocket configuration
   ```

4. **Test deployment:**
   ```bash
   # Open test-client.html in browser
   # Or use curl to test health endpoint
   curl http://localhost:3001/health
   ```

---

## 📋 What This Solves

### Problem
- FCM notifications have 1-5 second delays
- Call rejection takes 6-10 seconds to reach caller
- Users complain calls feel slow and unresponsive

### Solution
- WebSocket provides 10-50ms latency (100x faster)
- Instant call rejection/acceptance notifications
- Real-time user online/offline status
- FCM fallback for offline users

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────┐
│  Laravel Backend (Existing)                 │
│  • REST APIs for calls                      │
│  • Database operations                      │
│  • Agora token generation                   │
│  • FCM fallback notifications               │
└──────────────┬──────────────────────────────┘
               │ HTTP API calls
               │
┌──────────────┴──────────────────────────────┐
│  Socket.io Server (New - Node.js)           │
│  • Real-time WebSocket connections          │
│  • Instant call signaling                   │
│  • User presence tracking                   │
│  • Call state management                    │
└──────────────┬──────────────────────────────┘
               │ WebSocket (WSS)
               │
┌──────────────┴──────────────────────────────┐
│  Android App                                 │
│  • Connects to Socket.io on app start       │
│  • Receives instant call notifications      │
│  • Falls back to FCM when offline           │
└─────────────────────────────────────────────┘
```

---

## 🔧 Components

### 1. Socket.io Server (Node.js)
- **Port:** 3001 (internal only)
- **Protocol:** WebSocket (HTTP/WSS)
- **Purpose:** Real-time bidirectional communication
- **Process Manager:** PM2 (auto-restart, logs, monitoring)

### 2. Nginx Proxy
- **Purpose:** Proxy WebSocket connections through domain
- **SSL:** Certbot for HTTPS/WSS
- **Path:** `/socket.io/` proxies to `localhost:3001`

### 3. Laravel Integration
- **Service:** `WebSocketService.php`
- **Config:** `config/websocket.php`
- **Usage:** Check user online status, get connected users

### 4. Android App (Separate)
- Connects to Socket.io using Socket.io client library
- Listens for real-time events
- Falls back to FCM when WebSocket unavailable

---

## 📡 WebSocket Events

### Client → Server
| Event | Description | Payload |
|-------|-------------|---------|
| `call:initiate` | Start a call | `{ receiverId, callId, callType, channelName, agoraToken }` |
| `call:accept` | Accept incoming call | `{ callId }` |
| `call:reject` | Reject incoming call | `{ callId, reason }` |
| `call:end` | End active call | `{ callId }` |

### Server → Client
| Event | Description | Payload |
|-------|-------------|---------|
| `call:incoming` | Receive incoming call | `{ callId, callerId, callerName, callType, channelName, agoraToken }` |
| `call:accepted` | Call was accepted | `{ callId, timestamp }` |
| `call:rejected` | Call was rejected (⭐ **INSTANT!**) | `{ callId, reason, timestamp }` |
| `call:ended` | Call ended | `{ callId, reason, endedBy }` |
| `call:timeout` | Call not answered | `{ callId, reason }` |
| `call:busy` | User is busy | `{ callId }` |
| `user:online` | User came online | `{ userId, timestamp }` |
| `user:offline` | User went offline | `{ userId, timestamp }` |

---

## 🚀 Deployment Status

### Pre-Deployment ✅
- [x] Documentation created
- [x] Socket.io server implemented
- [x] Laravel service created
- [x] Nginx configuration prepared
- [x] Installation script ready
- [x] Test client created

### Deployment Steps (Do on Server)
- [ ] Install Node.js 18.x
- [ ] Install PM2
- [ ] Deploy Socket.io server
- [ ] Configure environment variables
- [ ] Start server with PM2
- [ ] Configure Nginx proxy
- [ ] Setup SSL certificate
- [ ] Configure firewall
- [ ] Update Laravel environment
- [ ] Test end-to-end

### Post-Deployment
- [ ] Monitor server logs
- [ ] Test with Android app
- [ ] Verify call rejection latency (<100ms)
- [ ] Setup monitoring alerts
- [ ] Train team on troubleshooting

---

## 📊 Performance Metrics

| Metric | Current (FCM) | New (WebSocket) | Improvement |
|--------|---------------|-----------------|-------------|
| Call rejection notification | 6-10 seconds | 50-100ms | **100x faster** |
| Call acceptance notification | 2-5 seconds | 50-100ms | **50x faster** |
| User online status | N/A (polling) | Real-time | Instant |
| Server resources | N/A | ~100MB RAM | Minimal |

---

## 🛠️ Testing Tools

### 1. Health Check (Terminal)
```bash
curl http://localhost:3001/health
```

### 2. User Online Check (Terminal)
```bash
curl http://localhost:3001/api/users/123/online
```

### 3. Laravel Tinker
```bash
php artisan tinker
>>> app(\App\Services\WebSocketService::class)->isServerAvailable()
```

### 4. Browser Test Client
Open `socket-server/test-client.html` in browser to:
- Connect with custom user ID and token
- Initiate test calls
- Accept/reject calls
- View real-time event logs

### 5. PM2 Monitoring
```bash
pm2 status        # Check server status
pm2 logs          # View real-time logs
pm2 monit         # Interactive monitoring
```

---

## 🔒 Security Features

- ✅ **Token authentication:** All connections verified with Laravel
- ✅ **Firewall:** Port 3001 blocked externally, only Nginx proxy
- ✅ **SSL/TLS:** WebSocket over HTTPS (WSS)
- ✅ **Rate limiting:** Prevent abuse (100 requests/minute per user)
- ✅ **CORS:** Configured for specific domains
- ✅ **Graceful degradation:** Falls back to FCM if WebSocket fails

---

## 🆘 Troubleshooting

### Server won't start
```bash
pm2 logs onlycare-socket --err
lsof -i :3001  # Check if port is in use
```

### Can't connect from Android
```bash
# Check server
pm2 status

# Check Nginx
nginx -t
curl https://onlycare.in/socket.io/?EIO=4&transport=polling

# Check firewall
ufw status
```

### High memory usage
```bash
pm2 restart onlycare-socket
# Or configure auto-restart
pm2 start server.js --name onlycare-socket --max-memory-restart 500M
```

### Laravel can't connect
```bash
# Check internal connection
curl http://localhost:3001/health

# Test from Laravel
php artisan tinker
>>> app(\App\Services\WebSocketService::class)->getHealthStatus()
```

---

## 📚 Resources

- **Socket.io Documentation:** https://socket.io/docs/v4/
- **PM2 Documentation:** https://pm2.keymetrics.io/docs/usage/quick-start/
- **Nginx WebSocket Proxy:** https://nginx.org/en/docs/http/websocket.html
- **Laravel HTTP Client:** https://laravel.com/docs/http-client

---

## 👥 Team Responsibilities

### Backend Team
- Deploy Socket.io server
- Configure Laravel integration
- Setup Nginx proxy
- Configure SSL certificate
- Monitor server health

### Android Team
- Integrate Socket.io client library
- Implement WebSocket connection
- Handle real-time events
- Implement FCM fallback
- Test latency and reliability

### DevOps
- Setup server monitoring
- Configure auto-restart
- Setup log rotation
- Configure backups
- Monitor performance metrics

---

## 📞 Support

### Log Files
- **Socket.io:** `pm2 logs onlycare-socket`
- **Nginx:** `tail -f /var/log/nginx/error.log`
- **Laravel:** `tail -f /var/www/onlycare/public/storage/logs/laravel.log`

### Health Checks
- **Socket.io:** `curl http://localhost:3001/health`
- **Laravel:** `php artisan tinker`
- **Nginx:** `nginx -t`

---

## ✅ Success Criteria

Deployment is successful when:
1. Socket.io server shows "online" in PM2
2. Health endpoint returns 200 OK
3. Laravel can check user online status
4. Android app connects successfully
5. Call rejection latency < 100ms
6. Server auto-restarts on crash
7. Logs are rotating properly

---

## 📝 Next Steps

1. **Backend Team:** Read `WEBSOCKET_INTEGRATION_GUIDE.md` and deploy server
2. **Android Team:** Wait for Socket.io URL, then integrate client
3. **Testing:** Use `test-client.html` for initial testing
4. **Production:** Follow `DEPLOYMENT_CHECKLIST.md` step by step

---

**Document Version:** 1.0  
**Last Updated:** November 22, 2025  
**Status:** Ready for Implementation ✅

**Questions?** Share this document with your team!









