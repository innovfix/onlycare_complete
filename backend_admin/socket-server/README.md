# OnlyCare WebSocket Server

Real-time WebSocket server for instant video call signaling using Socket.io.

## 🚀 Quick Start

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Environment

```bash
cp .env.example .env
nano .env
```

Update the following variables:
- `LARAVEL_API_URL` - Your Laravel API endpoint
- `LARAVEL_API_SECRET` - Shared secret between Node.js and Laravel

### 3. Start Server

**Development:**
```bash
npm run dev
```

**Production with PM2:**
```bash
npm run pm2:start
```

## 📋 PM2 Commands

```bash
# Start server
npm run pm2:start

# Stop server
npm run pm2:stop

# Restart server
npm run pm2:restart

# View logs
npm run pm2:logs

# Monitor server
npm run pm2:monit
```

## 🔧 API Endpoints

### Health Check
```bash
GET /health
```

### Check User Online Status
```bash
GET /api/users/:userId/online
```

### Get Connected Users
```bash
GET /api/connected-users
```

## 📡 WebSocket Events

### Client → Server

- `call:initiate` - Initiate a call
- `call:accept` - Accept incoming call
- `call:reject` - Reject incoming call
- `call:end` - End active call

### Server → Client

- `call:incoming` - Receive incoming call
- `call:accepted` - Call was accepted
- `call:rejected` - Call was rejected
- `call:ended` - Call ended by other party
- `call:timeout` - Call timed out (no answer)
- `call:busy` - User is busy
- `user:online` - User came online
- `user:offline` - User went offline

## 🔒 Authentication

Clients must authenticate with:
```javascript
{
  auth: {
    token: "Bearer token from Laravel",
    userId: "user_id"
  }
}
```

## 📊 Performance

- **Latency:** 10-50ms
- **Concurrent Users:** 1000+ on 1GB RAM
- **Memory Usage:** ~100-200MB for 1000 users

## 🆘 Troubleshooting

Check logs:
```bash
pm2 logs onlycare-socket
```

Check server status:
```bash
pm2 status
```

Test health endpoint:
```bash
curl http://localhost:3001/health
```

## 📚 Documentation

See `WEBSOCKET_INTEGRATION_GUIDE.md` in the parent directory for complete setup instructions.









