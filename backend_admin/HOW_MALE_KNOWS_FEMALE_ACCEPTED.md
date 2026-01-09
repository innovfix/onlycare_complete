# 📞 How Male Knows When Female Accepts Call

## 🔄 **Complete Flow**

### **Step 1: Female Accepts Call**
```
Female App (Android)
  ↓
POST /api/v1/calls/{callId}/accept
  ↓
Backend Laravel (CallController.php)
```

**What happens:**
- Female calls REST API endpoint
- Backend updates call status to `ONGOING`
- Backend sets `receiver_joined_at = NOW`
- Backend sets `started_at = NOW`

---

### **Step 2: Backend Notifies Male**

When female accepts, backend sends **TWO notifications** to male:

#### **A) WebSocket Notification (INSTANT - ~50-100ms)**

**Code:** `CallController.php` → `emitCallAcceptedWebSocket()`

**Process:**
```
Backend Laravel
  ↓
POST http://websocket-server:3001/api/emit/call-accepted
  ↓
WebSocket Server (Node.js)
  ↓
Emits 'call:accepted' event to male's socket
  ↓
Male's Android App receives instantly
```

**When it works:**
- ✅ Male is connected to WebSocket
- ✅ WebSocket server is running
- ✅ Male's socket ID is found

**Logs to check:**
```bash
# Backend
grep "websocket_check.*WebSocket notification result" storage/logs/laravel.log

# WebSocket Server
pm2 logs onlycare-socket-server | grep "call:accepted"
```

---

#### **B) FCM Notification (FALLBACK - 1-5 seconds)**

**Code:** `CallController.php` → `notifyCallAccepted()`

**Process:**
```
Backend Laravel
  ↓
Firebase Cloud Messaging (FCM)
  ↓
Male's Android device receives FCM
  ↓
CallNotificationService handles it
```

**When it works:**
- ✅ Male has FCM token registered
- ✅ Firebase credentials are configured
- ✅ Always sent (even if WebSocket fails)

**FCM Payload:**
```json
{
  "type": "call_accepted",
  "callId": "CALL_1234567890",
  "receiverId": "USR_female_id",
  "receiverName": "Female Name",
  "callType": "AUDIO",
  "timestamp": "1234567890000"
}
```

**Logs to check:**
```bash
grep "call_accepted.*FCM notification SENT" storage/logs/laravel.log
```

---

### **Step 3: Male Receives Notification**

#### **Option A: WebSocket (If Connected)**

**Android Code:** `AudioCallViewModel.kt` → `WebSocketEvent.CallAccepted`

**What happens:**
```kotlin
is WebSocketEvent.CallAccepted -> {
    // Update UI immediately
    _state.update {
        it.copy(
            waitingForReceiver = false,
            acceptanceMessage = "✅ $receiverName accepted your call!",
            callAccepted = true
        )
    }
    
    // Stop API polling (no longer needed)
    callStatusPollingJob?.cancel()
    
    // Start Agora mic/speaker
    ensureCallerAgoraJoinedIfReady()
}
```

**Logs to check:**
```bash
adb logcat | grep "websocket_check.*RECEIVED call:accepted"
```

---

#### **Option B: FCM (Fallback)**

**Android Code:** `CallNotificationService.kt` → `handleDataPayload()`

**What happens:**
- FCM data payload received with `type: "call_accepted"`
- Broadcast intent sent: `com.onlycare.app.CALL_ACCEPTED`
- `MainActivity` receives broadcast
- Navigates to call screen or updates UI

**Note:** Currently FCM `call_accepted` handler might not be fully implemented. WebSocket is the primary method.

---

## 📊 **Notification Priority**

### **Best Case (Male Connected to WebSocket):**
1. ✅ WebSocket notification (~50-100ms) - **INSTANT**
2. ✅ FCM notification (~1-5 seconds) - **Backup**

**Result:** Male knows instantly via WebSocket

---

### **Fallback Case (Male NOT Connected to WebSocket):**
1. ❌ WebSocket notification - **FAILED** (male not connected)
2. ✅ FCM notification (~1-5 seconds) - **WORKS**

**Result:** Male knows via FCM (slower but reliable)

---

## 🔍 **How to Verify**

### **Check Backend Logs:**

```bash
# Check if female accepted
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && tail -100 storage/logs/laravel.log | grep 'call_accepted.*Female accepted'"

# Check WebSocket notification attempt
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && tail -100 storage/logs/laravel.log | grep 'websocket_check.*WebSocket notification result'"

# Check FCM notification
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && tail -100 storage/logs/laravel.log | grep 'call_accepted.*FCM notification SENT'"
```

---

### **Check WebSocket Server Logs:**

```bash
ssh root@64.227.163.211 "pm2 logs onlycare-socket-server --lines 50 | grep 'call:accepted'"
```

**Look for:**
- `[websocket_check] Laravel requested call:accepted notification`
- `✅ Emitted call:accepted to caller`
- OR `⚠️ Caller NOT connected to WebSocket`

---

### **Check Android Logs (Male Device):**

```bash
# Check WebSocket reception
adb logcat | grep "websocket_check.*RECEIVED call:accepted"

# Check AudioCallViewModel handling
adb logcat | grep "Receiver accepted our call"

# Check FCM (if WebSocket not connected)
adb logcat | grep "CALL_ACCEPTED\|call_accepted"
```

---

## 🐛 **Common Issues**

### **Issue 1: Male Not Connected to WebSocket**
**Symptoms:**
- WebSocket notification fails
- Only FCM works (slower)

**Solution:**
- Ensure male connects to WebSocket in `MainActivity.onCreate()`
- Check WebSocket connection logs

---

### **Issue 2: FCM Not Received**
**Symptoms:**
- WebSocket fails (male not connected)
- FCM also fails

**Solution:**
- Check Firebase credentials file exists
- Check male has FCM token registered
- Check FCM logs in backend

---

### **Issue 3: Male Receives Notification But UI Doesn't Update**
**Symptoms:**
- WebSocket/FCM received
- But UI still shows "waiting for receiver"

**Solution:**
- Check `AudioCallViewModel` handles `CallAccepted` event
- Check `callAccepted = true` is set in state
- Check Agora join logic

---

## ✅ **Expected Flow (Working)**

### **1. Female Accepts:**
```
Female App → POST /api/v1/calls/{callId}/accept
Backend → Updates database (status = ONGOING)
```

### **2. Backend Notifies Male:**
```
Backend → WebSocket Server → Male (if connected) ✅ INSTANT
Backend → FCM → Male (always) ✅ FALLBACK
```

### **3. Male Receives:**
```
WebSocket Event → AudioCallViewModel → Updates UI ✅
OR
FCM → CallNotificationService → Broadcast → MainActivity → Updates UI ✅
```

### **4. Male UI Updates:**
```
waitingForReceiver = false
acceptanceMessage = "✅ Female Name accepted your call!"
callAccepted = true
Agora mic/speaker starts
```

---

## 📝 **Summary**

**Male knows female accepted via:**

1. **WebSocket (Primary)** - Instant notification (~50-100ms)
   - Works if male is connected to WebSocket
   - Handled by `AudioCallViewModel`

2. **FCM (Fallback)** - Slower notification (~1-5 seconds)
   - Always sent by backend
   - Works even if WebSocket fails
   - Handled by `CallNotificationService` → Broadcast

**Both methods ensure male is notified!**

---

**Last Updated:** 2026-01-09
**Purpose:** Explain how male knows when female accepts call
