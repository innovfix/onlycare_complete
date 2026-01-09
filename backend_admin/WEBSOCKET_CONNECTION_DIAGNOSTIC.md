# 🔍 WebSocket Connection Diagnostic Guide

## 📋 Issue
WebSocket shows `isConnected() = false` repeatedly.

---

## 🔍 **Root Cause Analysis**

### **Current Behavior:**
1. **MALE users:** Should connect to WebSocket in `MainActivity.onCreate()`
2. **FEMALE users:** Should NOT connect to WebSocket (use FCM only)

### **Why WebSocket Might Not Be Connected:**

#### **1. Female User (Expected Behavior)**
- Female users don't connect to WebSocket by design
- They use FCM for incoming calls
- This is **NORMAL** and **EXPECTED**

**Check logs for:**
```
[websocket_check] ℹ️ Female user - skipping WebSocket (using FCM only)
```

#### **2. Male User - Connection Failed**
- Male user should connect but connection failed
- Check for connection errors

**Check logs for:**
```
[websocket_check] ❌ CONNECTION ERROR
[websocket_check] ❌ EXCEPTION during WebSocket connection
```

#### **3. Missing Credentials**
- Auth token or user ID missing
- Connection cannot proceed

**Check logs for:**
```
[websocket_check] ❌ Cannot connect: Missing auth token or user ID
```

---

## 🔍 **Diagnostic Steps**

### **Step 1: Check User Gender**

**Android Logcat:**
```bash
adb logcat | grep -E "websocket_check.*Gender|websocket_check.*Female|websocket_check.*Male"
```

**Look for:**
- `Gender: FEMALE` → WebSocket not connected is **EXPECTED**
- `Gender: MALE` → WebSocket should be connected

---

### **Step 2: Check Connection Attempts**

**Android Logcat:**
```bash
adb logcat | grep -E "websocket_check.*connect\(\)|websocket_check.*CONNECTING|websocket_check.*CONNECTED"
```

**Look for:**
- `[websocket_check] connect() called` → Connection attempt started
- `[websocket_check] 🔌 Starting WebSocket connection` → Connection in progress
- `[websocket_check] ✅ CONNECTED` → Successfully connected
- `[websocket_check] ❌ CONNECTION ERROR` → Connection failed

---

### **Step 3: Check Connection Errors**

**Android Logcat:**
```bash
adb logcat | grep -E "websocket_check.*ERROR|websocket_check.*EXCEPTION|Connection error"
```

**Common Errors:**

**a) Missing Credentials:**
```
[websocket_check] ❌ Cannot connect: Missing auth token or user ID
  Token: MISSING
  User ID: MISSING
```

**b) Connection Error:**
```
[websocket_check] ❌ CONNECTION ERROR
Error: [error message]
Server URL: https://onlycare.in
```

**c) Exception:**
```
[websocket_check] ❌ EXCEPTION during WebSocket connection
Exception: [exception type]
Message: [error message]
```

---

### **Step 4: Check MainActivity Connection**

**Android Logcat:**
```bash
adb logcat | grep -E "MainActivity.*websocket_check|MainActivity.*WebSocket"
```

**Look for:**
- `[websocket_check] connectWebSocket() called` → MainActivity trying to connect
- `[websocket_check] ⚡ Attempting WebSocket connection (Male user)` → Connection started
- `[websocket_check] ✅ WebSocket connected successfully!` → Success
- `[websocket_check] ⚠️ WebSocket connection attempt finished but NOT connected` → Failed

---

## 🐛 **Common Issues & Solutions**

### **Issue 1: Female User Checking Connection**
**Symptoms:**
- `isConnected() = false` repeatedly
- Logs show: `Gender: FEMALE`

**Solution:**
- ✅ **This is EXPECTED behavior**
- Female users don't need WebSocket
- They use FCM for notifications
- No action needed

---

### **Issue 2: Male User Not Connecting**
**Symptoms:**
- `Gender: MALE`
- `isConnected() = false`
- No connection attempt logs

**Solution:**
- Check if `MainActivity.connectWebSocket()` is called
- Check if user is logged in
- Check if gender is correctly set

---

### **Issue 3: Connection Error**
**Symptoms:**
- `[websocket_check] ❌ CONNECTION ERROR`
- Error message in logs

**Solution:**
- Check server URL: `https://onlycare.in`
- Check network connectivity
- Check WebSocket server status
- Check firewall/proxy settings

---

### **Issue 4: Missing Credentials**
**Symptoms:**
- `[websocket_check] ❌ Cannot connect: Missing auth token or user ID`

**Solution:**
- Ensure user is logged in
- Check `SessionManager.getAuthToken()`
- Check `SessionManager.getUserId()`
- Re-login if needed

---

## 📊 **Expected Log Flow (Male User)**

### **1. App Starts:**
```
MainActivity: [websocket_check] connectWebSocket() called
MainActivity: Gender: MALE
MainActivity: [websocket_check] ⚡ Attempting WebSocket connection (Male user)
```

### **2. Connection Starts:**
```
WebSocketManager: [websocket_check] connect() called
WebSocketManager: [websocket_check] 🔌 Starting WebSocket connection
WebSocketManager: Server URL: https://onlycare.in
WebSocketManager: User ID: USR_xxx
```

### **3. Connection Success:**
```
WebSocketManager: [websocket_check] ✅ CONNECTED to WebSocket server
WebSocketManager: User ID: USR_xxx
WebSocketManager: Socket ID: xxx
MainActivity: [websocket_check] ✅ WebSocket connected successfully!
```

### **4. Connection Check:**
```
WebSocketManager: [websocket_check] isConnected() = TRUE
```

---

## 📊 **Expected Log Flow (Female User)**

### **1. App Starts:**
```
MainActivity: [websocket_check] connectWebSocket() called
MainActivity: Gender: FEMALE
MainActivity: [websocket_check] ℹ️ Female user - skipping WebSocket (using FCM only)
```

### **2. Connection Check:**
```
WebSocketManager: [websocket_check] isConnected() = FALSE
WebSocketManager: User ID: USR_xxx
WebSocketManager: Socket: NULL
```

**Note:** This is **EXPECTED** - Female users don't connect to WebSocket.

---

## ✅ **Quick Diagnostic Commands**

### **Check User Gender:**
```bash
adb logcat | grep -E "Gender:|websocket_check.*Female|websocket_check.*Male" | tail -10
```

### **Check Connection Status:**
```bash
adb logcat | grep "websocket_check.*isConnected\|websocket_check.*CONNECTED" | tail -20
```

### **Check Connection Errors:**
```bash
adb logcat | grep "websocket_check.*ERROR\|websocket_check.*EXCEPTION" | tail -20
```

### **Check All WebSocket Logs:**
```bash
adb logcat | grep "websocket_check" | tail -50
```

---

## 🎯 **Summary**

1. **Female users:** WebSocket not connected is **EXPECTED** ✅
2. **Male users:** Should connect to WebSocket on app start
3. **Check logs** to see why connection failed (if male user)
4. **Common issues:** Missing credentials, connection errors, network issues

---

**Last Updated:** 2026-01-09
**Purpose:** Diagnose WebSocket connection issues
