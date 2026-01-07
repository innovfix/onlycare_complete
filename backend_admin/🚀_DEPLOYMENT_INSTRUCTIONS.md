# 🚀 DEPLOYMENT INSTRUCTIONS - Enhanced WebSocket Server

**Date:** November 23, 2025  
**Changes:** Enhanced debugging, database fallback, format detection

---

## ✅ **WHAT'S BEEN ENHANCED**

1. ✅ Comprehensive debug logging for `call:initiate`
2. ✅ Comprehensive debug logging for `call:reject`
3. ✅ Automatic CallId format detection (CALL_xxx vs xxx)
4. ✅ Database fallback for race conditions
5. ✅ Enhanced error messages and diagnostics
6. ✅ Changed `call:reject` handler to `async` for database queries

---

## 🔧 **FILE CHANGES**

**Modified File:** `/var/www/onlycare_admin/socket-server/server.js`

**Changes:**
- Lines 126-220: Enhanced `call:initiate` logging
- Lines 267-400: Enhanced `call:reject` with debugging and fallback
- Handler signature: `socket.on('call:reject', async (data) => ...)`

**No Breaking Changes** - All existing functionality preserved!

---

## 🚀 **DEPLOYMENT STEPS**

### Method 1: Simple Restart (Recommended)

```bash
# Step 1: Stop old server
pkill -f "/var/www/onlycare_admin/socket-server/server.js"

# Step 2: Wait a moment
sleep 2

# Step 3: Start enhanced server
cd /var/www/onlycare_admin/socket-server
node server.js > /tmp/websocket.log 2>&1 &

# Step 4: Verify it's running
ps aux | grep "server.js" | grep -v grep

# Step 5: Test health (should return JSON)
curl http://localhost:3001/health

# Step 6: Watch logs
tail -f /tmp/websocket.log
```

---

### Method 2: Using PM2 (If Available)

```bash
# If using PM2
pm2 restart socket-server

# Or reload (zero-downtime)
pm2 reload socket-server

# Check logs
pm2 logs socket-server --lines 50
```

---

### Method 3: Using Systemd (If Configured)

```bash
# Restart service
sudo systemctl restart socket-server

# Check status
sudo systemctl status socket-server

# View logs
sudo journalctl -u socket-server -f
```

---

## 🔍 **POST-DEPLOYMENT VERIFICATION**

### Step 1: Check Process is Running

```bash
ps aux | grep "node.*server.js" | grep -v grep

# Expected output:
# root  123456  0.2  0.2  11525140  69728  ?  Ssl  09:30  0:00  node /var/www/onlycare_admin/socket-server/server.js
```

### Step 2: Check Health Endpoint

```bash
curl http://localhost:3001/health

# Expected output:
# {"status":"OK","connectedUsers":0,"activeCalls":0,"timestamp":1732358400000}
```

### Step 3: Check Logs for Startup Message

```bash
tail -20 /tmp/websocket.log

# Expected output:
# 🚀 Socket.io server running on port 3001
# 📡 Environment: production
# 🔗 Laravel API: http://localhost/api/v1
```

### Step 4: Test WebSocket Connection (Optional)

```bash
# Open test client in browser
# File: /var/www/onlycare_admin/socket-server/test-client.html

# Or use wscat (if installed)
wscat -c "ws://localhost:3001?token=YOUR_TEST_TOKEN"
```

---

## 📋 **TESTING THE ENHANCEMENTS**

### Test 1: Call Initiation Logging

```bash
# Terminal 1: Watch logs
tail -f /tmp/websocket.log | grep "🔍"

# Terminal 2: Initiate a call from Android app

# Expected logs:
🔍 ========================================
🔍 call:initiate received
🔍 Caller userId: USR_123456
🔍 Receiver userId: USR_789012
🔍 CallId: CALL_17326748932
✅ Call added to activeCalls successfully
🔍 After: activeCalls size = 1
🔍 All keys in activeCalls: [ 'CALL_17326748932' ]
```

---

### Test 2: Call Rejection Logging

```bash
# Terminal 1: Watch logs
tail -f /tmp/websocket.log | grep "🔍"

# Terminal 2: Reject the call from Android app

# Expected logs (if working):
🔍 call:reject received
🔍 Received callId: CALL_17326748932
🔍 activeCalls size: 1
🔍 Call found? YES ✅
✅ Caller USR_123456 notified INSTANTLY: call rejected
```

---

### Test 3: Database Fallback (If Call Not Found)

```bash
# Expected logs (if fallback kicks in):
🔍 Call found? NO ❌
🔍 Attempting database fallback...
✅ Found call in database!
✅ Using database fallback to notify caller
```

---

## ⚠️ **TROUBLESHOOTING**

### Issue 1: Port Already in Use

**Error:**
```
Error: listen EADDRINUSE: address already in use :::3001
```

**Solution:**
```bash
# Find and kill process on port 3001
lsof -i :3001
kill -9 <PID>

# Or use fuser
fuser -k 3001/tcp

# Then restart server
cd /var/www/onlycare_admin/socket-server
node server.js > /tmp/websocket.log 2>&1 &
```

---

### Issue 2: Server Crashes on Start

**Check logs:**
```bash
cat /tmp/websocket.log | tail -50
```

**Common causes:**
- Missing environment variables in `.env`
- Laravel API URL not configured
- Node modules not installed

**Solution:**
```bash
# Check .env exists
ls -la /var/www/onlycare_admin/socket-server/.env

# Reinstall dependencies if needed
cd /var/www/onlycare_admin/socket-server
npm install

# Start server
node server.js
```

---

### Issue 3: No Logs Appearing

**Solution:**
```bash
# Check log file location
ls -la /tmp/websocket.log

# If empty, server might not be starting
# Run in foreground to see errors
cd /var/www/onlycare_admin/socket-server
node server.js

# Press Ctrl+C to stop, then fix errors, then run in background
```

---

### Issue 4: Database Fallback Not Working

**Symptoms:**
```
❌ Database fallback failed: getaddrinfo ENOTFOUND
```

**Solution:**
```bash
# Check Laravel API URL in .env
cat /var/www/onlycare_admin/socket-server/.env | grep LARAVEL_API_URL

# Should be something like:
# LARAVEL_API_URL=http://localhost/api/v1
# Or
# LARAVEL_API_URL=https://api.onlycare.app/api/v1

# Test if Laravel API is accessible
curl http://localhost/api/v1/settings/app
```

---

## 📊 **MONITORING**

### Real-time Log Monitoring

```bash
# All logs
tail -f /tmp/websocket.log

# Debug logs only
tail -f /tmp/websocket.log | grep "🔍"

# Errors only
tail -f /tmp/websocket.log | grep "❌"

# Success messages only
tail -f /tmp/websocket.log | grep "✅"
```

### Connection Stats

```bash
# Check connected users
curl http://localhost:3001/api/connected-users

# Check active calls
curl http://localhost:3001/health
```

---

## 🎯 **ROLLBACK INSTRUCTIONS** (If Needed)

If you need to rollback to the previous version:

```bash
# 1. Stop current server
pkill -f "server.js"

# 2. Restore from git (if you have version control)
cd /var/www/onlycare_admin/socket-server
git checkout HEAD~1 server.js

# 3. Restart server
node server.js > /tmp/websocket.log 2>&1 &
```

**Note:** You probably won't need to rollback. The enhancements are additive and don't break existing functionality.

---

## ✅ **SUCCESS CRITERIA**

After deployment, you should see:

- [✅] Server process running (check with `ps aux | grep server.js`)
- [✅] Health endpoint responding (check with `curl http://localhost:3001/health`)
- [✅] Logs showing startup message (check with `tail /tmp/websocket.log`)
- [✅] Enhanced debug logs appearing when calls are made
- [✅] Format detection working (try with/without CALL_ prefix)
- [✅] Database fallback working (test by rejecting before initiation)

---

## 📞 **SUPPORT**

If you encounter issues:

1. **Check logs first:** `tail -100 /tmp/websocket.log`
2. **Verify environment:** `.env` file has correct Laravel API URL
3. **Test Laravel API:** Ensure GET `/api/v1/calls/{callId}` endpoint works
4. **Check network:** Ensure Node.js server can reach Laravel API
5. **Review documentation:**
   - `🔍_CALL_REJECTION_DIAGNOSTIC_REPORT.md` - Complete analysis
   - `✅_QUESTIONNAIRE_ANSWERS.md` - Q&A format
   - `🎯_QUICK_DIAGNOSTIC_SUMMARY.md` - Quick reference

---

## 🎉 **DEPLOYMENT COMPLETE**

Once deployed, you're ready to:

1. ✅ Test call rejection with enhanced logging
2. ✅ Identify if Android is using WebSocket or REST API
3. ✅ Handle format mismatches automatically
4. ✅ Handle race conditions with database fallback
5. ✅ Debug any issues with comprehensive logs

**Next step:** Test with Android app and monitor logs!

---

**Deployed By:** Backend Team  
**Deployment Date:** November 23, 2025  
**Version:** Enhanced with Diagnostics v1.0  
**Status:** ✅ Ready for Production Testing






