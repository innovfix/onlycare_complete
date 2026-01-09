# 📋 How to Read Logs for Call Issue Debugging

## What to Look For

When you're on the rating screen and accept a new call, filter the logs by these tags:

```bash
# In Android Studio Logcat, filter by:
RateUserScreen|CallActivity|AudioCallScreen|AudioCallViewModel|IncomingCallActivity
```

## Expected Flow (CORRECT)

### 0. When Rating Screen Is Shown (After Previous Call)

```
RateUserScreen: ╔════════════════════════════════════════════════════════════
RateUserScreen: ║ 📝 RateUserScreen ENTERED
RateUserScreen: ╚════════════════════════════════════════════════════════════
RateUserScreen: 📞 Call ID: CALL_12345 (previous call)
RateUserScreen: 👤 User ID: USR_xxxxx
RateUserScreen: ════════════════════════════════════════════════════════════
RateUserScreen: 🧹 Clearing call state from CallStateManager...
RateUserScreen: 📊 BEFORE clearing:
RateUserScreen:    wasInCall: true (from previous call)
RateUserScreen:    wasInIncomingScreen: false
RateUserScreen:    previousCallId: CALL_12345
RateUserScreen: ✅ Call state cleared - ready for new incoming calls
```

### 1. When You Accept the New Call

```
IncomingCallActivity: ╔════════════════════════════════════════════════════════════
IncomingCallActivity: ║ 🚀 LAUNCHING CallActivity
IncomingCallActivity: ╚════════════════════════════════════════════════════════════
IncomingCallActivity:   - App State: ALIVE (MainActivity in background)
IncomingCallActivity:   - Call ID: CALL_67890 (NEW CALL)
IncomingCallActivity:   - Intent flags: CLEAR_TOP | SINGLE_TOP
```

### 2. CallActivity Receives the New Call

**If CallActivity already exists (you're on rating screen):**
```
CallActivity: ╔════════════════════════════════════════════════════════════
CallActivity: ║ 🔄 CallActivity.onNewIntent() - NEW CALL WHILE RUNNING!
CallActivity: ╚════════════════════════════════════════════════════════════
CallActivity:    Old intent callId: CALL_12345 (previous call)
CallActivity:    New intent callId: CALL_67890 (new call)
CallActivity: 🔄 Calling recreate() NOW to fully restart activity...
CallActivity: ✅ recreate() called - activity will restart
```

**Then onCreate() should be called:**
```
CallActivity: 🎬 CallActivity.onCreate() - STARTING
CallActivity:    savedInstanceState: not null (restored)
CallActivity: 📞 CallActivity INITIALIZED
CallActivity:   - Call ID: CALL_67890 (new call ID)
```

### 3. AudioCallScreen Initializes

```
AudioCallScreen: ========================================
AudioCallScreen: 🔄 AudioCallScreen LaunchedEffect TRIGGERED
AudioCallScreen: ========================================
AudioCallScreen: 📞 Call ID: CALL_67890
AudioCallScreen: 👤 User ID: USR_xxxxx
AudioCallScreen: 🎯 Role: receiver
AudioCallScreen: 🧹 Resetting ViewModel state for this call...
```

### 4. ViewModel Resets

```
AudioCallViewModel: ========================================
AudioCallViewModel: 🔄 resetForNewCall() - Clearing ALL stale state
AudioCallViewModel: ========================================
AudioCallViewModel: 📊 BEFORE RESET:
AudioCallViewModel:    isCallEnded: true (from previous call)
AudioCallViewModel:    callId: CALL_12345 (old call)
AudioCallViewModel:    callReallyStarted: true (old call)
AudioCallViewModel: ========================================
AudioCallViewModel: ✅ State reset complete - ready for new call
AudioCallViewModel: 📊 AFTER RESET:
AudioCallViewModel:    isCallEnded: false ✅
AudioCallViewModel:    callId: CALL_12345 (will be updated)
AudioCallViewModel:    callReallyStarted: false ✅
```

### 5. Call Initializes

```
AudioCallScreen: ========================================
AudioCallScreen: 🚀 INITIALIZING CALL
AudioCallScreen: ========================================
AudioCallScreen: 📞 Call ID: CALL_67890
AudioCallScreen: 📺 Channel: channel_name
AudioCallViewModel: 🔄 Initializing and joining call...
AudioCallViewModel: ✅ Reset isCallEnded=false and set callReallyStarted=true
```

### 6. Check for Premature Ending

```
AudioCallScreen: ========================================
AudioCallScreen: 🔍 LaunchedEffect(isCallEnded) CHECKING:
AudioCallScreen:    state.isCallEnded = false ✅
AudioCallScreen:    state.callId = CALL_67890 ✅
AudioCallScreen:    state.callReallyStarted = true ✅
AudioCallScreen: ========================================
```

**If call ends prematurely, you'll see:**
```
AudioCallScreen:    state.isCallEnded = true ❌ (BAD!)
AudioCallScreen: 🚨 ALL CONDITIONS MET - ENDING CALL
```

## What Causes Premature Ending

If you see `isCallEnded = true` before the call connects, look for:

```
AudioCallViewModel: ========================================
AudioCallViewModel: 🚨 SETTING isCallEnded = true
AudioCallViewModel: ========================================
AudioCallViewModel: Reason: <REASON HERE>
AudioCallViewModel: Current callId: CALL_xxxxx
AudioCallViewModel: callReallyStarted: <true/false>
AudioCallViewModel: Stack trace:
```

### Common Reasons:

1. **"API Polling - Call Rejected/Declined/Ended"**
   - Old polling job from previous call is still running
   - Should be cancelled in `resetForNewCall()`

2. **"Agora - Remote User Left Channel"**
   - Agora detected remote user left
   - Could be from previous call's Agora session

3. **"WebSocket - Call Ended by Other User"**
   - WebSocket event from previous call
   - Should be ignored if callId doesn't match

## What to Share

When reporting the issue, share logs containing:

0. **The rating screen entry:**
   ```
   RateUserScreen: 📝 RateUserScreen ENTERED
   RateUserScreen: 🧹 Clearing call state
   ```

1. **The accept moment:**
   ```
   IncomingCallActivity: 🚀 LAUNCHING CallActivity
   ```

2. **Whether onNewIntent() was called:**
   ```
   CallActivity: 🔄 CallActivity.onNewIntent()
   ```
   **OR**
   ```
   CallActivity: 🎬 CallActivity.onCreate()
   ```

3. **The ViewModel reset:**
   ```
   AudioCallViewModel: 📊 BEFORE RESET:
   AudioCallViewModel: 📊 AFTER RESET:
   ```

4. **If call ends, the reason:**
   ```
   AudioCallViewModel: 🚨 SETTING isCallEnded = true
   AudioCallViewModel: Reason: <REASON>
   ```

5. **The LaunchedEffect check:**
   ```
   AudioCallScreen: 🔍 LaunchedEffect(isCallEnded) CHECKING:
   ```

## How to Capture Logs

### In Android Studio:

1. Open **Logcat** tab
2. Select your device
3. In the filter box, enter:
   ```
   package:mine tag:CallActivity|AudioCallScreen|AudioCallViewModel
   ```
4. Reproduce the issue
5. Right-click in Logcat → **Copy** → **Copy All**

### Using adb:

```bash
adb logcat -s RateUserScreen:* CallActivity:* AudioCallScreen:* AudioCallViewModel:* IncomingCallActivity:*
```

## Quick Diagnosis

| Symptom | Likely Cause |
|---------|--------------|
| No `onNewIntent()` log | CallActivity not being reused (check intent flags) |
| `onNewIntent()` but no `onCreate()` after | `recreate()` not working |
| `isCallEnded = true` in BEFORE RESET | Old state not cleared |
| `isCallEnded = true` in AFTER RESET | Reset not working |
| `isCallEnded = true` before `callReallyStarted = true` | Premature ending from old job/event |
| Stack trace shows "API Polling" | Old polling job not cancelled |
| Stack trace shows "Agora" | Old Agora session not destroyed |

Share these logs and we can pinpoint exactly what's happening!
