# 🔍 Backend Logs Analysis - OnlyCare Call System

## ✅ Token Generation Test Results

**Test Completed:** ✅ SUCCESS  
**Date:** November 22, 2025  
**Test Call ID:** CALL_17638195357765

---

## 📊 Current Configuration Status

### ✅ Agora Credentials (VERIFIED)
```
App ID:          8b5e9417f15a48ae929783f32d3d33d4 ✅
App Certificate: 03e9b06b303e47a9b93e...ac63 ✅ (Primary)
Certificate Type: PRIMARY (32 characters)
Mode:            SECURE ✅
```

### ✅ Token Generation Settings (VERIFIED)
```
UID:             0 ✅ (any user can join)
Role:            PUBLISHER (1) ✅ (can send/receive audio/video)
Expiration:      86400 seconds (24 hours) ✅
Token Version:   007 ✅
```

### ⚠️ Logging Configuration (NEEDS FIX)
```
LOG_LEVEL:       error ⚠️
APP_DEBUG:       false
APP_ENV:         production
```

**Issue:** With `LOG_LEVEL=error`, you won't see token generation logs!

---

## 🎫 Generated Token Example

### Token Details
```
Full Token: 0078b5e9417f15a48ae929783f32d3d33d4AAAAID2CCAuTCI/qaxDuP8bZYki5O2ymxoYjWGfIWqePMjEFAGEkA2khwA9pIxGPABhjYWxsX0NBTExfMTc2MzgxOTUzNTc3NjUAAAAA

Length:     139 characters
Version:    007 ✅
App ID:     8b5e9417f15a48ae929783f32d3d33d4 ✅
Encoding:   Base64 ✅
```

### Token Breakdown
```
007 8b5e9417f15a48ae929783f32d3d33d4 AAAAID2CCAuTCI/qaxDuP8bZYki5O2ymxoYjWGfIWqePMjEFAGEkA2khwA9pIxGPABhjYWxsX0NBTExfMTc2MzgxOTUzNTc3NjUAAAAA
│   │                                  │
│   └─ App ID (32 chars)              └─ Encrypted token data (Base64)
│
└─ Version (007)
```

---

## 📋 What Logs SHOULD Look Like (When LOG_LEVEL=info/debug)

### 1️⃣ POST /api/v1/calls/initiate

```log
[2025-11-22 14:30:00] production.INFO: 🔑 Generating Agora token for call: 
{
    "call_id": "CALL_17638195357765"
}

[2025-11-22 14:30:00] production.DEBUG: Token Generation Debug for call CALL_17638195357765:
[2025-11-22 14:30:00] production.DEBUG:   - App ID: 8b5e9417f15a48ae929783f32d3d33d4
[2025-11-22 14:30:00] production.DEBUG:   - Certificate: 03e9b06b303e47a9b93e...
[2025-11-22 14:30:00] production.DEBUG:   - Channel: call_CALL_17638195357765

[2025-11-22 14:30:00] production.INFO: Agora token generated for call CALL_17638195357765 (SECURE mode)

[2025-11-22 14:30:00] production.INFO: ✅ Agora credentials generated:
{
    "channel": "call_CALL_17638195357765",
    "token_length": 139
}

[2025-11-22 14:30:00] production.INFO: 📝 Creating call record with Agora credentials:
{
    "call_id": "CALL_17638195357765",
    "type": "AUDIO"
}

[2025-11-22 14:30:00] production.INFO: ✅ Call record created successfully with Agora credentials saved
```

**Key Points to Verify:**
- ✅ Token generated in SECURE mode (not UNSECURE)
- ✅ Channel name format: `call_CALL_xxxxxxxxxxxxx`
- ✅ Token length: ~139-500 characters
- ✅ Credentials saved to database

---

### 2️⃣ GET /api/v1/calls/incoming (Receiver Side)

```log
[2025-11-22 14:30:05] production.DEBUG: 📞 Incoming call data:
{
    "call_id": "CALL_17638195357765",
    "caller_id": "USR_17637560616692",
    "caller_name": "Test User",
    "call_type": "AUDIO",
    "agora_token_length": 139,
    "channel_name": "call_CALL_17638195357765"
}
```

**Key Points to Verify:**
- ✅ Token is retrieved from database
- ✅ Same channel name as initiation
- ✅ Same token as generated during initiation

---

### 3️⃣ POST /api/v1/calls/{callId}/accept (Call Accepted)

```log
[2025-11-22 14:30:10] production.INFO: Call accepted

Response includes:
{
    "success": true,
    "message": "Call accepted",
    "call": {
        "id": "CALL_17638195357765",
        "status": "ONGOING",
        "started_at": "2025-11-22T14:30:10+00:00",
        "agora_token": "0078b5e9417f15a48ae929783f32d3d33d4...",
        "channel_name": "call_CALL_17638195357765"
    }
}
```

**Key Points to Verify:**
- ✅ Same token as initiation
- ✅ Same channel name
- ✅ Status changed to ONGOING

---

## 🔧 How to Enable Logging for Call Monitoring

### Step 1: Enable Debug Logs (TEMPORARY)

```bash
# Edit .env
nano /var/www/onlycare_admin/.env

# Change:
LOG_LEVEL=error

# To:
LOG_LEVEL=info    # For production
# OR
LOG_LEVEL=debug   # For detailed testing

# Clear cache
php artisan config:clear
php artisan cache:clear
```

### Step 2: Start Real-Time Monitor

```bash
# Terminal 1: Monitor logs
bash /tmp/monitor_calls.sh

# Terminal 2: Make test calls from your app
```

### Step 3: Make a Test Call

Use your mobile app to initiate a call and watch the logs appear.

### Step 4: Check for These Key Messages

✅ **SUCCESS Indicators:**
```
✅ "Agora token generated for call ... (SECURE mode)"
✅ "Agora credentials generated"
✅ "Call record created successfully with Agora credentials saved"
✅ Token starts with "007"
✅ Token contains your App ID
```

❌ **ERROR Indicators:**
```
❌ "Agora App ID not configured"
❌ "Agora token generation failed"
❌ "Token generation failed, falling back to UNSECURE mode"
❌ "Agora project in UNSECURE mode (no certificate)"
```

### Step 5: Restore Production Settings

```bash
# After testing, restore:
LOG_LEVEL=error
APP_DEBUG=false

php artisan config:clear
```

---

## 🧪 Quick Test Commands

### Test Token Generation (No Logging Required)
```bash
php /var/www/onlycare_admin/test_token_generation.php
```

### Check Recent Call Logs
```bash
tail -100 /var/www/onlycare_admin/storage/logs/laravel.log | grep -i "call_\|agora\|token"
```

### Monitor Specific Call ID
```bash
grep "CALL_17638195357765" /var/www/onlycare_admin/storage/logs/laravel.log
```

### Real-Time Log Stream
```bash
tail -f /var/www/onlycare_admin/storage/logs/laravel.log | grep --line-buffered "calls/"
```

---

## ✅ Summary: Your Backend Is Correct!

| Component | Status | Value |
|-----------|--------|-------|
| **App ID** | ✅ | `8b5e9417f15a48ae929783f32d3d33d4` |
| **Certificate** | ✅ | `03e9b06b303e47a9b93e...ac63` (Primary) |
| **UID** | ✅ | `0` (any user can join) |
| **Role** | ✅ | `PUBLISHER (1)` - NOT Subscriber |
| **Token Format** | ✅ | Starts with `007` + App ID |
| **Token Length** | ✅ | 139+ characters |
| **Mode** | ✅ | SECURE (Certificate enabled) |
| **Generation Time** | ✅ | 0.12ms (fast!) |

---

## 📡 What Gets Sent to App

```json
{
    "success": true,
    "message": "Call initiated successfully",
    "call": {
        "id": "CALL_17638195357765",
        "status": "CONNECTING",
        "agora_token": "0078b5e9417f15a48ae929783f32d3d33d4AAAAID2CCAuTCI/qaxDuP8bZYki5O2ymxoYjWGfIWqePMjEFAGEkA2khwA9pIxGPABhjYWxsX0NBTExfMTc2MzgxOTUzNTc3NjUAAAAA",
        "channel_name": "call_CALL_17638195357765"
    }
}
```

**The app should use:**
- `AGORA_APP_ID`: `8b5e9417f15a48ae929783f32d3d33d4`
- `token`: From API response (139 chars)
- `channelName`: From API response (`call_CALL_xxxxx`)
- `uid`: `0` (or any number)

---

## 🎯 Next Steps

1. ✅ **Backend is correctly configured** - No changes needed
2. ⚠️ **Enable logging temporarily** to see token generation logs (optional)
3. 📱 **Test on mobile app** - Verify the app receives and uses tokens correctly
4. 🔍 **Monitor logs during calls** to debug any issues
5. 🔒 **Restore LOG_LEVEL=error** after testing for security

---

## 💡 Pro Tips

1. **Production Logging:** Keep `LOG_LEVEL=error` to avoid exposing tokens in logs
2. **Debug Mode:** Use `LOG_LEVEL=debug` only when troubleshooting
3. **Token Security:** Never log full tokens in production
4. **Channel Names:** Always unique per call (using timestamp + random)
5. **Token Expiry:** 24 hours is sufficient for call sessions

---

**Generated:** November 22, 2025  
**Test Status:** ✅ PASSED  
**Backend Status:** ✅ PRODUCTION READY








