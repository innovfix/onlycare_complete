# 🧪 Test Call Cancellation - Step by Step

## 📋 Issue

When **male ends call before female accepts**, female's incoming call screen **keeps ringing** and doesn't close.

---

## 🔍 Diagnosis: Find Where the Problem Is

Run this test to identify if the issue is in the **app** or **backend**.

---

## Test Steps

### Step 1: Male Calls Female

1. Open app on Male device (Device A)
2. Open app on Female device (Device B)
3. Male initiates call to Female
4. Female receives incoming call notification (ringing)

**Expected:** ✅ Both devices working

---

### Step 2: Male Ends Call Before Female Accepts

**On Male Device (Device A):**
1. While Female's phone is ringing
2. Male taps "End Call" button
3. **Watch the logs**

**Expected logs on Male device:**

```
AudioCallViewModel: ========================================
AudioCallViewModel: 📞 endCall() called
AudioCallViewModel:    Call ID: CALL_123
AudioCallViewModel:    Duration: 5
AudioCallViewModel:    Waiting for receiver: true
AudioCallViewModel: ========================================
AudioCallViewModel: 🚫 Caller ending call before receiver accepts - sending cancellation
AudioCallViewModel:    This will notify receiver immediately via WebSocket
WebSocketManager: ========================================
WebSocketManager: 🚫 cancelCall() called
WebSocketManager:    Call ID: CALL_123
WebSocketManager:    Reason: Caller ended call
WebSocketManager: ========================================
WebSocketManager: 📤 EMITTING call:cancel to server
WebSocketManager:    Data: {"callId":"CALL_123","reason":"Caller ended call"}
WebSocketManager: ========================================
WebSocketManager: ✅ call:cancel emitted successfully
WebSocketManager:    ⏰ Server should send call:cancelled to receiver
```

---

### Step 3: Check Female Device

**On Female Device (Device B):**

**Watch the logs immediately after male ends call:**

#### ✅ **If Working (Backend is correct):**

You should see these logs within 100ms:

```
WebSocketManager: 📥 RECEIVED call:cancelled from server
IncomingCallActivity: ========================================
IncomingCallActivity: 🚫 CALL CANCELLED EVENT RECEIVED (WebSocket)
IncomingCallActivity: ========================================
IncomingCallActivity:    Event Call ID: CALL_123
IncomingCallActivity:    Current Call ID: CALL_123
IncomingCallActivity:    Reason: Caller ended call
IncomingCallActivity:    Match: true
IncomingCallActivity: ========================================
IncomingCallActivity: ✅ MATCH! Caller cancelled this call
IncomingCallActivity: 🛑 STOPPING RINGING - Closing IncomingCallActivity...
IncomingCallActivity: ✅ Navigating to MainActivity due to WebSocket cancellation
IncomingCallActivity: ========================================
```

**Result:** Incoming call screen closes, ringing stops ✅

---

#### ❌ **If NOT Working (Backend issue):**

You see **NO logs** like above. The screen keeps ringing.

**This means:** Backend is NOT sending `call:cancelled` event to female ❌

---

## 🔧 How to Get Logs

### Android Studio Logcat:

1. Connect device via USB
2. Open Android Studio → Logcat
3. Filter: `WebSocketManager|IncomingCallActivity|AudioCallViewModel`
4. Perform the test
5. Check logs

### Terminal (ADB):

```bash
# Male device logs (check if sending call:cancel)
adb -s MALE_DEVICE_ID logcat | grep -E "cancelCall|call:cancel"

# Female device logs (check if receiving call:cancelled)
adb -s FEMALE_DEVICE_ID logcat | grep -E "call:cancelled|CALL CANCELLED EVENT"
```

---

## 📊 Test Results Analysis

### Scenario 1: Male Sends, Female Receives ✅

**Male logs:**
```
✅ call:cancel emitted successfully
```

**Female logs (within 100ms):**
```
🚫 CALL CANCELLED EVENT RECEIVED (WebSocket)
✅ MATCH! Caller cancelled this call
🛑 STOPPING RINGING
```

**Conclusion:** Everything working! ✅

---

### Scenario 2: Male Sends, Female Does NOT Receive ❌

**Male logs:**
```
✅ call:cancel emitted successfully
⏰ Server should send call:cancelled to receiver
```

**Female logs:**
```
(No logs about cancellation)
```

**Conclusion:** Backend is NOT forwarding `call:cancelled` event ❌

**Fix Required:** Backend team needs to implement (see below)

---

### Scenario 3: Male Does NOT Send ❌

**Male logs:**
```
📞 endCall() called
Waiting for receiver: false  ← WRONG!
(No cancellation logs)
```

**Conclusion:** Male app not detecting "waiting for receiver" state ❌

**Fix Required:** App-side issue (check `waitingForReceiver` state)

---

## 🔧 Backend Fix Required

If **Scenario 2** (backend not forwarding):

### Backend Code Needed:

```javascript
// In your WebSocket server code
socket.on('call:cancel', async (data) => {
  const { callId, reason } = data;
  
  console.log(`📥 Received call:cancel from caller`);
  console.log(`   Call ID: ${callId}`);
  console.log(`   Reason: ${reason}`);
  
  // 1. Update database
  await updateCallStatus(callId, 'cancelled');
  
  // 2. Get call to find receiver
  const call = await Call.findById(callId);
  
  if (!call) {
    console.error(`❌ Call not found: ${callId}`);
    return;
  }
  
  // 3. ⚡ SEND call:cancelled to RECEIVER (REQUIRED!)
  const receiverSocketId = getUserSocketId(call.receiverId);
  
  if (receiverSocketId) {
    io.to(receiverSocketId).emit('call:cancelled', {
      callId: callId,
      reason: reason || 'Caller cancelled',
      timestamp: Date.now()
    });
    
    console.log(`✅ Sent call:cancelled to receiver ${call.receiverId}`);
  }
  
  // 4. Also send FCM as backup
  const receiverFcmToken = await getFCMToken(call.receiverId);
  
  if (receiverFcmToken) {
    await sendFCMNotification(receiverFcmToken, {
      type: 'call_cancelled',
      callId: callId,
      callerId: call.callerId
    });
  }
});
```

---

## 🎯 Quick Test Checklist

Run this test and check the boxes:

### Male Device (Caller):
- [ ] Shows "📞 endCall() called"
- [ ] Shows "Waiting for receiver: true"
- [ ] Shows "🚫 Caller ending call before receiver accepts"
- [ ] Shows "📤 EMITTING call:cancel to server"
- [ ] Shows "✅ call:cancel emitted successfully"

### Female Device (Receiver):
- [ ] Shows "🚫 CALL CANCELLED EVENT RECEIVED (WebSocket)"
- [ ] Shows "✅ MATCH! Caller cancelled this call"
- [ ] Shows "🛑 STOPPING RINGING"
- [ ] Incoming call screen closes
- [ ] Ringing stops

### If All Checked:
✅ **Working perfectly!**

### If Male Checks Pass, But Female Doesn't:
❌ **Backend issue** - Backend not sending `call:cancelled` event

### If Male First 2 Check, But Rest Fail:
❌ **App issue** - Not detecting "waiting for receiver" correctly

---

## 📞 What to Tell Backend Developer

If the test shows backend issue:

**Message:**
```
Hi,

When caller sends "call:cancel" event via WebSocket, the backend needs to 
forward a "call:cancelled" event to the receiver.

Currently:
- ✅ Caller sends: call:cancel
- ❌ Backend does NOT forward: call:cancelled to receiver
- ❌ Receiver's incoming call screen keeps ringing

Please implement the backend code in TEST_CALL_CANCELLATION_NOW.md 
(Section: Backend Fix Required)

This is the same pattern as rejection:
- Receiver sends: call:reject
- Backend forwards: call:rejected to caller ✅ (this works)

We need the reverse:
- Caller sends: call:cancel
- Backend forwards: call:cancelled to receiver (needs implementation)

Thanks!
```

---

## 🔍 Compare with Working Rejection Flow

For reference, here's how rejection works (which is already working):

### Rejection (Female rejects → Male gets notified) ✅

1. Female taps "Reject"
2. Female sends `call:reject` to backend
3. **Backend forwards `call:rejected` to Male** ✅
4. Male receives event and stops ringing ✅

### Cancellation (Male ends → Female should get notified) ❌

1. Male taps "End Call"
2. Male sends `call:cancel` to backend ✅
3. **Backend should forward `call:cancelled` to Female** ❌ (NOT IMPLEMENTED)
4. Female should receive event and stop ringing ❌

**Pattern is exactly the same, just reversed direction!**

---

Run the test now and report which scenario you're seeing!









