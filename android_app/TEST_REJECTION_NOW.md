# 🧪 TEST CALL REJECTION NOW

## ⚡ QUICK TEST (5 minutes)

### Setup:
- Device A: Caller (Male user - User_5555)
- Device B: Receiver (Female user - User_1111)

### Test Steps:

1. **Start the test:**
   ```bash
   # On Device B, watch logs:
   adb -s <Device_B_serial> logcat | grep -E "(IncomingCallActivity|WebSocket|FemaleHome)"
   ```

2. **Device A:** Initiate call to Device B

3. **Device B:** Wait for incoming call screen to appear

4. **Device B:** Tap "Reject" button (red button)

5. **Check Device B logs immediately:**

### ✅ SUCCESS - You should see:

```
IncomingCallActivity: 🚫 REJECTING CALL IN ACTIVITY
IncomingCallActivity: CallId: CALL_xxxxx
IncomingCallActivity: WebSocket connected: true
IncomingCallActivity: ✅ WebSocket rejection sent successfully
IncomingCallActivity: ⚡ Caller will be notified in <100ms!
IncomingCallActivity: ✅ REJECTION COMPLETE
```

### ✅ SUCCESS - Device A should:
- Stop ringing **IMMEDIATELY** (within 1 second)
- Show "Call Rejected" message
- Return to previous screen

---

## ❌ FAILURE INDICATORS

### If Device B shows:
```
IncomingCallActivity: WebSocket connected: false
```
**Problem:** WebSocket not connected  
**Fix:** Check MainActivity WebSocket initialization

### If Device A keeps ringing:
**Problem:** Backend not emitting `call:rejected`  
**Action:** Check backend logs (see CALL_REJECTION_FIX_COMPLETE.md)

### If you see NO logs at all:
**Problem:** Code not deployed  
**Action:** Rebuild and reinstall app:
```bash
./gradlew clean
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📊 EXPECTED TIMELINE

| Event | Time |
|-------|------|
| User taps "Reject" | 0ms |
| Log appears | +10ms |
| WebSocket sends | +50ms |
| Backend receives | +100ms |
| Caller notified | +150ms |
| Caller stops ringing | +200ms |

**Total time: ~200ms (instant!)**

---

## 🎯 NEXT STEPS

1. **If test passes ✅:**
   - Test with WebSocket disconnected (should still work via API)
   - Test rapid rejections
   - Deploy to production

2. **If test fails ❌:**
   - Share the logs from Device B
   - Check backend is running
   - Verify WebSocket server is up

---

**Good luck! The fix is solid - it should work!** 🚀



