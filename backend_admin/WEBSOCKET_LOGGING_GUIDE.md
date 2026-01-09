# 🔍 WebSocket Logging Guide - Call Acceptance Flow

## 📋 Purpose
This guide explains how to check logs to diagnose why calls don't start after female accepts.

---

## 🔍 **Log Tags to Search For**

### **1. Android App Logs (Logcat)**

Search for: `[websocket_check]`

#### **Key Log Points:**

**a) WebSocket Connection:**
```
[websocket_check] ✅ CONNECTED to WebSocket server
User ID: USR_xxx
Socket ID: xxx
```

**b) Female Accepts Call:**
```
[websocket_check] 📤 acceptCall() called
Call ID: CALL_xxx
WebSocket connected: true/false
[websocket_check] 📤 Emitting call:accept event
[websocket_check] ✅ call:accept event emitted successfully
```

**c) Male Receives Acceptance:**
```
[websocket_check] 📥 RECEIVED call:accepted event
Call ID: CALL_xxx
[websocket_check] ✅ CallAccepted event emitted successfully
```

**d) AudioCallViewModel Handles Acceptance:**
```
[websocket_check] ⚡ RECEIVED call:accepted via WebSocket
Call ID from event: CALL_xxx
Current call ID in state: CALL_xxx
[websocket_check] ✅ Receiver accepted our call! 🎉
```

---

### **2. Backend Laravel Logs**

**Location:** `/var/www/onlycare_admin/storage/logs/laravel.log`

#### **Search Commands:**

```bash
# Check if female accepted call
grep "call_accepted.*Female accepted" storage/logs/laravel.log | tail -20

# Check WebSocket notification attempt
grep "websocket_check" storage/logs/laravel.log | tail -30

# Check FCM notification
grep "call_accepted.*FCM" storage/logs/laravel.log | tail -20

# Check all call acceptance logs
grep "call_accepted" storage/logs/laravel.log | tail -50
```

#### **Key Log Points:**

**a) Female Accepts (REST API):**
```
[call_accepted] Female accepted call - Notifying male
call_id: CALL_xxx
caller_id: USR_xxx
receiver_id: USR_xxx
```

**b) WebSocket Notification Attempt:**
```
[websocket_check] Attempting to notify male via WebSocket
caller_id: USR_xxx
call_id: CALL_xxx
socket_url: http://localhost:3001
has_secret: true
```

**c) WebSocket Result:**
```
[websocket_check] WebSocket notification result
emitted: true/false
reason: Caller not connected (if false)
```

**d) FCM Notification:**
```
[call_accepted] FCM notification SENT to male - Call accepted
OR
[call_accepted] FCM notification FAILED to notify male
```

---

### **3. WebSocket Server Logs**

**Location:** PM2 logs: `pm2 logs onlycare-socket-server`

#### **Search Commands:**

```bash
# Check WebSocket server logs
pm2 logs onlycare-socket-server --lines 100 | grep websocket_check

# Check call:accept events
pm2 logs onlycare-socket-server --lines 100 | grep "call:accept"

# Check call:accepted emissions
pm2 logs onlycare-socket-server --lines 100 | grep "call:accepted"
```

#### **Key Log Points:**

**a) Female Sends call:accept:**
```
[websocket_check] Received call:accept event
Socket user ID: USR_xxx
Call ID: CALL_xxx
Active calls count: X
✅ Call found in activeCalls: CALL_xxx
Caller ID: USR_xxx
Receiver ID: USR_xxx
```

**b) WebSocket Emits to Male:**
```
✅ [websocket_check] Emitted call:accepted to caller
Caller ID: USR_xxx
Socket ID: xxx
Event data: {...}
```

**c) Laravel Requests call:accepted (HTTP endpoint):**
```
[websocket_check] Laravel requested call:accepted notification
Caller ID: USR_xxx
Call ID: CALL_xxx
Total connected users: X
Connected user IDs: [...]
Caller socket ID: xxx (or NOT FOUND)
```

---

## 🔍 **Diagnostic Flow**

### **Step 1: Check if Female Sent call:accept**

**Android Logcat:**
```
[websocket_check] 📤 Emitting call:accept event
[websocket_check] ✅ call:accept event emitted successfully
```

**WebSocket Server:**
```
[websocket_check] Received call:accept event
```

**If NOT found:**
- ❌ WebSocket not connected on female side
- Check: `[websocket_check] WebSocket connected: false`

---

### **Step 2: Check if WebSocket Server Received call:accept**

**WebSocket Server:**
```
[websocket_check] Received call:accept event
Call ID: CALL_xxx
✅ Call found in activeCalls
```

**If NOT found:**
- ❌ Call not in activeCalls (might be normal if accepted via REST API only)
- Check if call was initiated via WebSocket

---

### **Step 3: Check if Laravel Called WebSocket Endpoint**

**Backend Logs:**
```
[websocket_check] Attempting to notify male via WebSocket
[websocket_check] WebSocket notification result
emitted: true/false
```

**If `emitted: false`:**
- Check reason: `reason: Caller not connected`
- ❌ Male not connected to WebSocket

---

### **Step 4: Check if WebSocket Server Emitted to Male**

**WebSocket Server:**
```
✅ [websocket_check] Emitted call:accepted to caller
Caller socket ID: xxx
```

**If NOT found:**
- Check: `⚠️ Caller NOT connected to WebSocket`
- ❌ Male not connected

---

### **Step 5: Check if Male Received call:accepted**

**Android Logcat (Male Device):**
```
[websocket_check] 📥 RECEIVED call:accepted event
[websocket_check] ✅ CallAccepted event emitted successfully
```

**AudioCallViewModel:**
```
[websocket_check] ⚡ RECEIVED call:accepted via WebSocket
[websocket_check] ✅ Receiver accepted our call! 🎉
```

**If NOT found:**
- ❌ Male not connected to WebSocket
- OR Male's app not listening for event
- Check: `[websocket_check] ✅ CONNECTED to WebSocket server` on male device

---

## 🐛 **Common Issues & Solutions**

### **Issue 1: Female Not Connected to WebSocket**
**Symptoms:**
- Android: `[websocket_check] WebSocket connected: false`
- No `call:accept` event sent

**Solution:**
- Check WebSocket connection on female device
- Ensure WebSocketManager.connect() is called

---

### **Issue 2: Male Not Connected to WebSocket**
**Symptoms:**
- Backend: `emitted: false, reason: Caller not connected`
- WebSocket Server: `⚠️ Caller NOT connected`

**Solution:**
- Check WebSocket connection on male device
- Ensure WebSocketManager.connect() is called before making call

---

### **Issue 3: Call Not in activeCalls**
**Symptoms:**
- WebSocket Server: `❌ Call CALL_xxx not found in activeCalls`

**Solution:**
- This is normal if call was initiated via REST API only
- Laravel HTTP endpoint will handle notification instead

---

### **Issue 4: Male Receives call:accepted but Call Doesn't Start**
**Symptoms:**
- Android: `[websocket_check] ✅ Receiver accepted our call! 🎉`
- But Agora channel not joined

**Solution:**
- Check AudioCallViewModel logic after receiving CallAccepted
- Ensure `callAccepted = true` triggers Agora join
- Check if `pendingAppId`, `pendingToken`, `pendingChannelName` are set

---

## 📊 **Quick Check Commands**

### **Backend:**
```bash
# Check recent call acceptance
ssh root@64.227.163.211 "cd /var/www/onlycare_admin && tail -200 storage/logs/laravel.log | grep -E 'call_accepted|websocket_check' | tail -30"

# Check WebSocket server
ssh root@64.227.163.211 "pm2 logs onlycare-socket-server --lines 50 | grep websocket_check"
```

### **Android:**
```bash
# Filter Logcat for WebSocket events
adb logcat | grep websocket_check

# Filter for call acceptance
adb logcat | grep -E "call:accept|call:accepted|acceptCall"
```

---

## ✅ **Expected Flow (Working)**

1. **Female accepts call:**
   - Android: `[websocket_check] 📤 Emitting call:accept`
   - WebSocket: `[websocket_check] Received call:accept event`
   - Backend: `[call_accepted] Female accepted call`

2. **Backend notifies male:**
   - Backend: `[websocket_check] Attempting to notify male via WebSocket`
   - WebSocket: `✅ Emitted call:accepted to caller`
   - Backend: `[websocket_check] WebSocket notification result: emitted: true`

3. **Male receives notification:**
   - Android: `[websocket_check] 📥 RECEIVED call:accepted event`
   - Android: `[websocket_check] ✅ Receiver accepted our call! 🎉`
   - Android: Agora channel join starts

4. **Call starts:**
   - Both users join Agora channel
   - Audio/video connection established

---

**Last Updated:** 2026-01-09
**Purpose:** Diagnose call acceptance flow issues
