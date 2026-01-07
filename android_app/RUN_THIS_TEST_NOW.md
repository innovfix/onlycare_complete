# ⚡ Run This Test NOW - Find the Exact Issue in 5 Minutes

## 🎯 Goal

Find out **exactly** why the perfectly-coded backend and Android app aren't communicating.

---

## 🧪 Test Steps (Do This Right Now!)

### Step 1: Check Backend Logs (1 minute)

Open backend logs:
```bash
pm2 logs onlycare-socket --lines 100
```

Or if using different process manager:
```bash
tail -f /path/to/your/websocket-server.log
```

**Keep this terminal open!**

---

### Step 2: Check Android Caller Logs (1 minute)

Connect Device A (caller) and run:
```bash
adb logcat -c  # Clear logs
adb logcat | grep -E "WebSocketManager|VideoCallViewModel|AudioCallViewModel"
```

**Keep this terminal open!**

---

### Step 3: Check Android Receiver Logs (1 minute)

Connect Device B (receiver) and run:
```bash
adb logcat -c  # Clear logs
adb logcat | grep -E "WebSocketManager|FemaleHome"
```

**Keep this terminal open!**

---

### Step 4: Make Test Call (30 seconds)

1. Device A: Open app, make a call to Device B
2. Device B: Wait for incoming call notification
3. Device B: Tap "Reject"

---

### Step 5: Analyze Logs (2 minutes)

Now check each terminal and answer these questions:

---

## ✅ Checklist - What Do You See?

### Backend Logs - During Call Initiation

**Question 1:** Do you see this when call is made?
```
📞 Call initiated: [caller] → [receiver]
✅ Call signal sent to receiver
```

- [ ] YES - Backend received call:initiate ✅
- [ ] NO - Backend never got call:initiate ❌ **(Issue: Android not emitting call:initiate)**

---

**Question 2:** Do you see call being stored?
```
✅ Call stored in activeCalls: CALL_xxxxx
```

- [ ] YES - Call is stored ✅
- [ ] NO - Call not stored ❌ **(This shouldn't happen if code is as you described)**

---

### Backend Logs - During Call Rejection

**Question 3:** Do you see this when receiver taps Reject?
```
❌ Call rejected: CALL_xxxxx - Reason: User declined
```

- [ ] YES - Backend received call:reject ✅
- [ ] NO - Backend never got call:reject ❌ **(Issue: WebSocket not connected on receiver!)**

---

**Question 4:** Do you see caller notification?
```
✅ Caller [userId] notified INSTANTLY: call rejected
```

- [ ] YES - Backend sent call:rejected to caller ✅
- [ ] NO - Backend couldn't notify caller ❌ **(Issue: Caller socket not found)**

---

**Question 5:** If you see "Call not found", what's the exact message?
```
❌ Call CALL_xxxxx not found
```

- [ ] I see this - **(Issue: Call was stored but removed/not found)**
- [ ] I don't see this - Good ✅

---

### Android Caller Logs - During Call

**Question 6:** Does caller show WebSocket connected?
```
✅ Connected to WebSocket server
```

- [ ] YES - Connected ✅
- [ ] NO - Disconnected ❌ **(Issue: Fix WebSocket connection)**

---

**Question 7:** Does caller emit call:initiate?
```
📤 Emitting call:initiate for callId: CALL_xxxxx
✅ Call initiated via WebSocket
```

- [ ] YES - Emitted ✅
- [ ] NO - Not emitted ❌ **(Issue: WebSocket not connected or error)**

---

**Question 8:** Does caller receive call:rejected?
```
📥 RECEIVED call:rejected from server
```

- [ ] YES - Received ✅ **(But not processing it?)**
- [ ] NO - Never received ❌ **(Backend didn't send or caller socket wrong)**

---

### Android Receiver Logs - During Rejection

**Question 9:** Does receiver show WebSocket connected?
```
✅ Connected to WebSocket server
```

- [ ] YES - Connected ✅
- [ ] NO - Disconnected ❌ **(Issue: Fix WebSocket connection)**

---

**Question 10:** Does receiver emit call:reject?
```
📤 EMITTING call:reject to server
✅ call:reject emitted successfully
```

- [ ] YES - Emitted ✅
- [ ] NO - Not emitted ❌ **(Issue: WebSocket not connected)**
- [ ] Shows "WebSocket NOT connected" ❌ **(Issue: Connection lost)**

---

## 🎯 Diagnosis Guide

Based on your answers, here's what's wrong:

### Scenario A: WebSocket Not Connected
**Symptoms:**
- Question 6 or 9: NO
- Question 10: Shows "WebSocket NOT connected"

**Issue:** WebSocket disconnects before rejection  
**Fix:** Add auto-reconnection or ensure connection stays alive

---

### Scenario B: Backend Never Receives call:reject
**Symptoms:**
- Question 10: YES (Android emits)
- Question 3: NO (Backend doesn't receive)

**Issue:** WebSocket connection broken or IDs wrong  
**Fix:** Check userId in auth, check server logs for connection errors

---

### Scenario C: Backend Can't Find Caller Socket
**Symptoms:**
- Question 3: YES (Backend receives rejection)
- Question 4: NO (Backend can't notify caller)

**Issue:** Caller's userId format in `activeCalls` doesn't match `connectedUsers`  
**Fix:** Check userId format consistency (e.g., "123" vs "USER_123")

---

### Scenario D: Android Caller Receives But Doesn't Process
**Symptoms:**
- Question 8: YES (Caller receives call:rejected)
- But ringing doesn't stop

**Issue:** Event not processed in ViewModel  
**Fix:** Check `VideoCallViewModel`/`AudioCallViewModel` is collecting `callEvents`

---

### Scenario E: Call Not Found
**Symptoms:**
- Question 5: YES (Backend says "Call not found")

**Issue:** Call expired/removed before rejection OR callId mismatch  
**Fix:** Check callId format or increase timeout

---

## 📝 Report Template

After running the test, copy this and fill it out:

```
## Test Results

Date: [DATE]
Time: [TIME]

### Backend Logs
- Q1 (Call initiated): [ ] YES / [ ] NO
- Q2 (Call stored): [ ] YES / [ ] NO
- Q3 (Rejection received): [ ] YES / [ ] NO
- Q4 (Caller notified): [ ] YES / [ ] NO
- Q5 (Call not found): [ ] YES / [ ] NO

### Android Caller Logs
- Q6 (Connected): [ ] YES / [ ] NO
- Q7 (Emit initiate): [ ] YES / [ ] NO
- Q8 (Receive rejected): [ ] YES / [ ] NO

### Android Receiver Logs
- Q9 (Connected): [ ] YES / [ ] NO
- Q10 (Emit reject): [ ] YES / [ ] NO

### Exact Error Messages
[Paste any error messages here]

### Diagnosis
Based on answers above, the issue is: [Scenario A/B/C/D/E]
```

---

## 🚀 Next Steps

1. **Run the test above** ✅
2. **Fill out the report** ✅
3. **Identify the scenario** ✅
4. **Apply the fix** ✅
5. **Test again** ✅

---

**This will take 5 minutes and tell you EXACTLY what's wrong!** 🎯

Don't skip any questions - each one eliminates possibilities and narrows down the issue!



