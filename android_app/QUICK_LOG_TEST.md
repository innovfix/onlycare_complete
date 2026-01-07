# 🚀 Quick Log Test - Copy & Paste These Commands

## Run These Commands

### Terminal 1 (Male Device):
```bash
adb logcat -c && adb logcat | grep -E "endCall.*Waiting|cancelCall|call:cancel"
```

### Terminal 2 (Female Device):
```bash
adb logcat -c && adb logcat | grep -E "call:cancelled|CALL CANCELLED EVENT"
```

---

## Perform Test

1. Male calls Female
2. Female's phone rings (incoming call screen shows)
3. **Male taps "End Call"**
4. **Watch both terminals**

---

## What You Should See

### Male Terminal (Should Always Show This):

```
AudioCallViewModel:    Waiting for receiver: true
AudioCallViewModel: 🚫 Caller ending call before receiver accepts
WebSocketManager: 📤 EMITTING call:cancel to server
WebSocketManager: ✅ call:cancel emitted successfully
```

### Female Terminal (Should Show This If Backend Works):

```
IncomingCallActivity: 🚫 CALL CANCELLED EVENT RECEIVED (WebSocket)
IncomingCallActivity: ✅ MATCH! Caller cancelled this call
IncomingCallActivity: 🛑 STOPPING RINGING
```

---

## Results

### ✅ If Female Terminal Shows Logs:
**Status:** Backend is working!  
**Result:** Female's screen closes immediately

### ❌ If Female Terminal Shows Nothing:
**Status:** Backend NOT working  
**Problem:** Backend is not forwarding `call:cancelled` event  
**Fix:** Send `MESSAGE_TO_BACKEND_DEVELOPER.md` to backend team

---

That's it! Run the test and see what happens.









