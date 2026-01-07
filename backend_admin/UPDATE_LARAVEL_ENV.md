# 📝 Laravel Environment Configuration

## Add These Lines to Your Laravel `.env` File

After deploying the WebSocket server, add these configuration lines to your Laravel `.env` file:

### Location: `/var/www/onlycare/public/.env` (or your Laravel root)

```env
# ========================================
# WebSocket Configuration
# ========================================

# Internal URL - Used by Laravel to communicate with Socket.io server
WEBSOCKET_URL=http://localhost:3001

# Public URL - Sent to Android clients for WebSocket connection
WEBSOCKET_PUBLIC_URL=https://onlycare.in

# Timeout for HTTP requests to WebSocket server (in seconds)
WEBSOCKET_TIMEOUT=2

# Enable/Disable WebSocket features
WEBSOCKET_ENABLED=true
```

---

## Configuration Explanation

### `WEBSOCKET_URL`
- **Purpose:** Laravel uses this to check if users are online
- **Value:** `http://localhost:3001` (internal communication)
- **Why localhost?** Both Laravel and Socket.io run on the same server

### `WEBSOCKET_PUBLIC_URL`
- **Purpose:** Android app uses this to connect to WebSocket
- **Value:** Your domain with HTTPS (e.g., `https://onlycare.in`)
- **Why HTTPS?** Secure WebSocket (WSS) requires SSL certificate

### `WEBSOCKET_TIMEOUT`
- **Purpose:** Prevent Laravel from hanging if Socket.io is down
- **Value:** `2` seconds (recommended)
- **Why 2 seconds?** Fast enough to check status, but won't block Laravel

### `WEBSOCKET_ENABLED`
- **Purpose:** Toggle WebSocket features on/off
- **Value:** `true` to enable, `false` to disable
- **Use case:** Disable during maintenance or testing

---

## After Adding Configuration

### 1. Clear Laravel Cache

```bash
cd /var/www/onlycare/public
php artisan config:cache
php artisan cache:clear
```

### 2. Verify Configuration

```bash
php artisan tinker
```

In Tinker:
```php
config('websocket.url');          // Should return: http://localhost:3001
config('websocket.public_url');   // Should return: https://onlycare.in
config('websocket.enabled');      // Should return: true
```

### 3. Test WebSocket Service

```bash
php artisan tinker
```

In Tinker:
```php
$ws = app(\App\Services\WebSocketService::class);

// Check if server is available
$ws->isServerAvailable();  // Should return: true

// Get health status
$ws->getHealthStatus();    // Should return array with 'status' => 'OK'

// Check if a user is online
$ws->isUserOnline('123');  // Should return: true or false
```

---

## API Response Format

When Android app calls your API to initiate a call, include WebSocket info:

### Example: `/api/v1/calls/initiate` Response

```json
{
  "success": true,
  "call": {
    "id": "call-123",
    "caller_id": "user-1",
    "receiver_id": "user-2",
    "call_type": "video",
    "status": "ringing"
  },
  "agora_token": "agora-token-here",
  "channel_name": "channel-123",
  "websocket": {
    "enabled": true,
    "url": "https://onlycare.in",
    "receiver_online": true
  }
}
```

Android app will use:
- `websocket.enabled` - Check if WebSocket is available
- `websocket.url` - Connect to WebSocket server
- `websocket.receiver_online` - Decide whether to use WebSocket or FCM

---

## Troubleshooting

### Issue: `config('websocket.url')` returns `null`

**Solution:**
```bash
# Clear config cache
php artisan config:clear

# Re-cache config
php artisan config:cache
```

### Issue: Laravel can't connect to Socket.io

**Check 1:** Is Socket.io running?
```bash
pm2 status
curl http://localhost:3001/health
```

**Check 2:** Is firewall blocking?
```bash
# Port 3001 should be accessible internally but blocked externally
curl http://localhost:3001/health  # Should work
```

**Check 3:** Test from Laravel
```bash
php artisan tinker
>>> app(\App\Services\WebSocketService::class)->getHealthStatus();
```

### Issue: Android app can't connect

**Check 1:** Is HTTPS configured?
```bash
curl https://onlycare.in/socket.io/?EIO=4&transport=polling
# Should return socket.io handshake data
```

**Check 2:** Is Nginx proxy configured?
```bash
nginx -t
systemctl status nginx
```

**Check 3:** Check Android logs for connection errors

---

## Environment-Specific Configuration

### Development/Local
```env
WEBSOCKET_URL=http://localhost:3001
WEBSOCKET_PUBLIC_URL=http://localhost:3001
WEBSOCKET_ENABLED=true
```

### Staging
```env
WEBSOCKET_URL=http://localhost:3001
WEBSOCKET_PUBLIC_URL=https://staging.onlycare.in
WEBSOCKET_ENABLED=true
```

### Production
```env
WEBSOCKET_URL=http://localhost:3001
WEBSOCKET_PUBLIC_URL=https://onlycare.in
WEBSOCKET_ENABLED=true
```

---

## Security Notes

1. **Never expose internal URL:** `WEBSOCKET_URL` should always be `localhost:3001`
2. **Always use HTTPS:** `WEBSOCKET_PUBLIC_URL` must use HTTPS in production
3. **Shared secret:** Keep `LARAVEL_API_SECRET` (in Socket.io `.env`) secure and match it in both places
4. **Firewall:** Block external access to port 3001

---

**Next Steps:**
1. ✅ Add configuration to Laravel `.env`
2. ✅ Clear Laravel cache
3. ✅ Test with Tinker
4. ✅ Update Android app to use WebSocket
5. ✅ Test end-to-end with actual devices

---

**Questions?** Check the main guide: `WEBSOCKET_INTEGRATION_GUIDE.md`









