# Switch-to-Video Debugging Guide

## Issue Reported

When user clicks the switch-to-video button in audio call:
1. ✅ Dialog appears
2. ✅ User clicks "Yes"
3. ❌ **Nothing happens**
4. ℹ️ User has 130 coins (sufficient balance)

## Changes Made (Build Required)

### 1. Comprehensive Logging Added

**File:** `AudioCallViewModel.kt`

Added extensive logging at every step:
- ✅ When `requestSwitchToVideo()` is called
- ✅ Call ID and remote user ID verification
- ✅ Backend API request/response
- ✅ New video call details received
- ✅ WebSocket connection check
- ✅ WebSocket request sent
- ✅ Error handling with stack traces

**Log Format:**
```
╔════════════════════════════════════════════════════════════
║ 📹 SWITCH TO VIDEO REQUEST STARTED
╠════════════════════════════════════════════════════════════
║ Old Call ID: CALL_xxxxx
║ Remote User ID: USR_xxxxx
╚════════════════════════════════════════════════════════════
```

### 2. User Feedback Added

**File:** `AudioCallScreen.kt`

**Toast Messages:**
- ✅ "Requesting switch to video..." (when user clicks "Yes")
- ✅ Error messages (if backend or WebSocket fails)
- ✅ "Waiting for response..." (after sending request)

**LaunchedEffect:**
```kotlin
LaunchedEffect(state.switchToVideoDeclinedMessage) {
    state.switchToVideoDeclinedMessage?.let { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
```

## Testing Instructions

### Step 1: Install Updated APK
```bash
cd /Users/rishabh/OnlyCareProject/android_app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: Start Audio Call
1. Login as male user (130 coins)
2. Start an audio call with a female user
3. Wait for call to connect

### Step 3: Request Switch-to-Video
1. Click the **video camera button** (switch-to-video)
2. Dialog appears: "Switch to Video Call?"
3. Click **"Yes"**
4. 🔍 **Watch for Toast message:** "Requesting switch to video..."

### Step 4: Capture Logs

**Filter for switch-to-video logs:**
```bash
adb logcat | grep -E "(SWITCH TO VIDEO|📹|requestSwitchToVideo|SwitchToVideo)"
```

**Full AudioCallViewModel logs:**
```bash
adb logcat AudioCallViewModel:D *:S
```

**Full AudioCallScreen logs:**
```bash
adb logcat AudioCallScreen:E *:S
```

## What to Look For

### ✅ Success Flow (Expected)
```
AudioCallScreen: 📹 User clicked YES - requesting switch to video
AudioCallViewModel: ╔════════════════════════════════════════════════════════════
AudioCallViewModel: ║ 📹 SWITCH TO VIDEO REQUEST STARTED
AudioCallViewModel: ║ Old Call ID: CALL_xxxxx
AudioCallViewModel: ║ Remote User ID: USR_xxxxx
AudioCallViewModel: 📤 Calling backend API to create new video call...
AudioCallViewModel: 📡 Backend API response received
AudioCallViewModel: ║ ✅ BACKEND API SUCCESS
AudioCallViewModel: ║ 📹 NEW VIDEO CALL DETAILS
AudioCallViewModel: ║ New Call ID: CALL_yyyyy
AudioCallViewModel: ║ 📤 SENDING WEBSOCKET REQUEST
AudioCallScreen: 📹 Switch-to-video message: Waiting for response...
```

### ❌ Error Flows (Possible Issues)

#### Issue 1: Null Call ID
```
AudioCallViewModel: ❌ Cannot request switch: callId is null
```
**Fix:** Ensure call is properly initialized

#### Issue 2: Backend Validation Failed
```
AudioCallViewModel: ║ ❌ BACKEND API FAILED
AudioCallViewModel: ║ Error: Call not found / Insufficient coins / etc.
AudioCallScreen: 📹 Switch-to-video message: [Error message]
```
**Fix:** Check backend logs, call status, coin balance

#### Issue 3: WebSocket Not Connected
```
AudioCallViewModel: ❌ WebSocket not connected - cannot send request
AudioCallScreen: 📹 Switch-to-video message: Connection issue. Please try again.
```
**Fix:** Check WebSocket connection status

#### Issue 4: Silent Failure (No Logs)
```
[No logs after "User clicked YES"]
```
**Fix:** ViewModel not receiving the call - check activity/fragment lifecycle

## Backend Validation Checks

The backend performs these validations before creating a video call:

1. ✅ Call ID is valid
2. ✅ Call exists in database
3. ✅ Requesting user is part of the call (caller or receiver)
4. ✅ Call type is AUDIO (not already video)
5. ✅ Call status is ONGOING (not ended)
6. ✅ Male user has sufficient coins for video call rate

**Backend Log Location:**
```bash
ssh root@64.227.163.211
tail -f /var/www/onlycare_admin/storage/logs/laravel.log | grep -i "switch"
```

## Common Issues & Solutions

### Issue: "Nothing happens" after clicking Yes

**Possible Causes:**
1. ViewModel method not called (check lifecycle)
2. Backend API call failing silently (check network/response)
3. WebSocket not connected (check connection status)
4. Call status not ONGOING (call already ended or not started)
5. Insufficient coins (but user reported 130 coins)

**Debugging Steps:**
1. ✅ Check logcat for "User clicked YES"
2. ✅ Check if "SWITCH TO VIDEO REQUEST STARTED" appears
3. ✅ Check backend API response
4. ✅ Check WebSocket connection status
5. ✅ Check if Toast messages appear

### Issue: Backend returns "Call not found"

**Cause:** Call ID format mismatch

**Fix:** Backend prepends "CALL_" if missing:
```php
if (!str_starts_with($oldCallId, 'CALL_')) {
    $oldCallId = 'CALL_' . $oldCallId;
}
```

### Issue: Backend returns "Insufficient coins"

**Cause:** Male user doesn't have enough coins for video call

**Check:**
1. Video call rate in `app_settings` table
2. Male user's `coin_balance` in `users` table
3. Compare: `coin_balance >= video_call_rate`

## Expected Result

After clicking "Yes":
1. ✅ Toast: "Requesting switch to video..."
2. ✅ Backend creates new video call
3. ✅ WebSocket sends request to receiver
4. ✅ Toast: "Waiting for response..."
5. ⏳ Receiver sees dialog: "User wants to switch to video call"
6. ✅ If accepted: Both users navigate to video call screen
7. ❌ If declined: Toast shows decline reason

## Next Steps

1. **Install updated APK** with new logging
2. **Test the flow** again
3. **Capture logs** using `adb logcat`
4. **Share logs** to identify the exact failure point

---

**Status:** ✅ Build successful, ready for testing
**Build:** `./gradlew assembleDebug` completed successfully
**Date:** January 10, 2026
