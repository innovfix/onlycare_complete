# Switch-to-Video: Prevent Call Ended Screen ✅

## Problem

When receiver accepted the switch-to-video request:
- ❌ **Sender:** Goes to CallEndedScreen (instead of VideoCallScreen)
- ✅ **Receiver:** Correctly navigates to VideoCallScreen

### Root Cause

When the sender receives "switch-to-video accepted" WebSocket event:
1. ✅ Sets `switchToVideoAccepted = true` 
2. ✅ Navigation to VideoCallScreen starts
3. ✅ Old audio call is ended (`viewModel.endCall()`)
4. ❌ **This triggers `isCallEnded = true`**
5. ❌ `LaunchedEffect(isCallEnded)` sees the flag
6. ❌ **Navigates to CallEndedScreen** (before VideoCallScreen navigation completes)

## Solution: Add `isSwitchingToVideo` Flag ✅

Added a new state flag to prevent navigation to CallEndedScreen when switching to video.

### Changes

#### 1. Added State Flag

**File:** `AudioCallViewModel.kt`

```kotlin
data class AudioCallState(
    // ... existing fields ...
    
    // ✅ NEW: Prevent navigation to call ended screen when switching to video
    val isSwitchingToVideo: Boolean = false  // Don't show call ended screen when this is true
)
```

#### 2. Set Flag When Switch is Accepted (Sender Side)

**File:** `AudioCallViewModel.kt`

```kotlin
is WebSocketEvent.SwitchToVideoAccepted -> {
    if (event.callId == _state.value.callId) {
        _state.update { 
            it.copy(
                switchToVideoAccepted = true,
                isSwitchingToVideo = true  // ✅ Prevent call ended screen
            ) 
        }
    }
}
```

#### 3. Set Flag When Switch is Accepted (Receiver Side)

**File:** `AudioCallViewModel.kt`

```kotlin
fun acceptSwitchToVideo() {
    // ... send acceptance ...
    
    acceptResult.onSuccess {
        _state.update { 
            it.copy(
                switchToVideoAccepted = true,
                isSwitchingToVideo = true  // ✅ Prevent call ended screen
            ) 
        }
    }
}
```

#### 4. Check Flag Before Navigating to CallEndedScreen

**File:** `AudioCallScreen.kt`

```kotlin
LaunchedEffect(state.isCallEnded, state.callId, state.callReallyStarted, state.isSwitchingToVideo) {
    // ✅ GUARD: Only process if:
    // 1. Call has ended
    // 2. Call ID is set
    // 3. Call was really started
    // 4. NOT switching to video ✅ NEW
    if (state.isCallEnded && 
        !state.callId.isNullOrEmpty() && 
        state.callReallyStarted && 
        !state.isSwitchingToVideo) {  // ✅ Prevent navigation during video upgrade
        
        // Navigate to CallEndedScreen
        viewModel.endCall(...)
    }
}
```

---

## Flow After Fix

### Sender (Requester) Flow

```
1. Male clicks 🎥 "Switch to Video"
2. Backend creates new video call
3. Female accepts
4. Male receives WebSocket "switch-to-video accepted"
   ↓
5. ✅ Set isSwitchingToVideo = true
6. ✅ Set switchToVideoAccepted = true
   ↓
7. LaunchedEffect(switchToVideoAccepted) triggers
   ↓
8. End old audio call
   ↓
9. ✅ Navigate to VideoCallScreen
   ↓
10. LaunchedEffect(isCallEnded) triggers
    ↓
11. ✅ Check: isSwitchingToVideo = true
12. ✅ SKIP navigation to CallEndedScreen
    ↓
✅ SUCCESS: Sender is now in VideoCallScreen
```

### Receiver (Accepter) Flow

```
1. Female sees dialog
2. Female clicks "Accept"
   ↓
3. ✅ Set isSwitchingToVideo = true
4. ✅ Set switchToVideoAccepted = true
5. Send WebSocket acceptance
   ↓
6. LaunchedEffect(switchToVideoAccepted) triggers
   ↓
7. End old audio call
   ↓
8. ✅ Navigate to VideoCallScreen
   ↓
9. LaunchedEffect(isCallEnded) triggers
    ↓
10. ✅ Check: isSwitchingToVideo = true
11. ✅ SKIP navigation to CallEndedScreen
    ↓
✅ SUCCESS: Receiver is now in VideoCallScreen
```

---

## Comparison

### ❌ Before (Sender goes to CallEndedScreen)

```
Sender Flow:
AudioCallScreen 
    ↓ Accept WebSocket
    ↓ isCallEnded = true
    ↓ ❌ Navigate to CallEndedScreen
    ✗ Never reaches VideoCallScreen

Receiver Flow:
AudioCallScreen
    ↓ Click Accept
    ↓ ✅ Navigate to VideoCallScreen
    ✓ Success
```

### ✅ After (Both go to VideoCallScreen)

```
Sender Flow:
AudioCallScreen 
    ↓ Accept WebSocket
    ↓ isSwitchingToVideo = true ✅
    ↓ isCallEnded = true
    ↓ ✅ SKIP CallEndedScreen (isSwitchingToVideo = true)
    ↓ ✅ Navigate to VideoCallScreen
    ✓ Success

Receiver Flow:
AudioCallScreen
    ↓ Click Accept
    ↓ isSwitchingToVideo = true ✅
    ↓ isCallEnded = true
    ↓ ✅ SKIP CallEndedScreen (isSwitchingToVideo = true)
    ↓ ✅ Navigate to VideoCallScreen
    ✓ Success
```

---

## Testing

### Test Scenario

1. **Start audio call** (male → female)
2. **Male clicks 🎥 "Switch to Video"**
3. **Male clicks "Yes"**
4. **Female sees dialog, clicks "Accept"**

### Expected Result

✅ **Sender (Male):**
- Stays in AudioCallScreen briefly
- Navigates to VideoCallScreen
- **Does NOT see CallEndedScreen**

✅ **Receiver (Female):**
- Stays in AudioCallScreen briefly
- Navigates to VideoCallScreen
- **Does NOT see CallEndedScreen**

✅ **Both:**
- End up in VideoCallScreen together
- Video call starts normally
- Duration shows 00:00 (new call)

### Verification Logs

```bash
adb logcat | grep -E "(isSwitchingToVideo|isCallEnded|ENDING CALL)"
```

**Expected Output:**
```
🔍 LaunchedEffect(isCallEnded) CHECKING:
   state.isCallEnded = true
   state.isSwitchingToVideo = true  ✅
   ✅ SKIP navigation (switching to video)
```

---

## Summary

### Problem
- Sender went to CallEndedScreen instead of VideoCallScreen

### Root Cause
- Old audio call ending triggered `isCallEnded = true`
- This triggered navigation to CallEndedScreen

### Solution
- Added `isSwitchingToVideo` flag
- Set flag to `true` when switch-to-video is accepted
- Check flag before navigating to CallEndedScreen
- Skip navigation when `isSwitchingToVideo = true`

### Result
✅ **Both users now correctly navigate to VideoCallScreen**

---

**Date:** January 10, 2026  
**Issue:** Sender goes to CallEndedScreen during switch-to-video  
**Fix:** Added `isSwitchingToVideo` flag  
**Status:** Complete ✅
