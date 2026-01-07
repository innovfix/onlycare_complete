# 30-Second Timeout Fix for Call Connections

## Problem
When clicking the audio/video call button, if the receiver didn't join within ~10 seconds, Agora SDK would show error 110 (Connection Timeout) immediately. This was too fast and didn't give the receiver enough time to:
- Receive the notification
- Open the app
- Accept the call
- Join the channel

## Solution Implemented

### Changed Timeout from 10 seconds → 30 seconds

**Files Modified:**
1. `AudioCallViewModel.kt`
2. `VideoCallViewModel.kt`

### How It Works

#### Before Fix (Old Behavior):
```
User clicks call button
       ↓
Join Agora channel
       ↓
Wait for receiver...
       ↓
⏰ After ~10 seconds
       ↓
❌ Agora Error 110: Connection Timeout
       ↓
Show error dialog immediately
```

#### After Fix (New Behavior):
```
User clicks call button
       ↓
Join Agora channel
       ↓
Start 30-second custom timeout ⏰
       ↓
Wait for receiver...
       ↓
If receiver joins within 30 seconds:
  ✅ Cancel timeout
  ✅ Call connects successfully
       ↓
If receiver doesn't join within 30 seconds:
  ❌ Show timeout error
  ❌ Explain why (offline/unavailable)
```

## Technical Implementation

### 1. Added New State Variable
```kotlin
// Both AudioCallState and VideoCallState
val waitingForReceiver: Boolean = false
```

### 2. Added Timeout Job Variables
```kotlin
private var connectionTimeoutJob: kotlinx.coroutines.Job? = null
private var hasShownTimeoutError = false
```

### 3. Start 30-Second Timer When Joining
```kotlin
// In initializeAndJoinCall()
_state.update { it.copy(waitingForReceiver = true) }
hasShownTimeoutError = false

connectionTimeoutJob?.cancel()

connectionTimeoutJob = viewModelScope.launch {
    kotlinx.coroutines.delay(30000) // 30 seconds
    
    if (!_state.value.remoteUserJoined && !hasShownTimeoutError) {
        Log.w(TAG, "⏰ 30-second timeout reached")
        hasShownTimeoutError = true
        _state.update {
            it.copy(
                waitingForReceiver = false,
                error = "❌ Connection Timeout..."
            )
        }
    }
}
```

### 4. Cancel Timer When User Joins
```kotlin
override fun onUserJoined(uid: Int) {
    connectionTimeoutJob?.cancel() // ✅ Cancel timeout
    hasShownTimeoutError = false
    _state.update { 
        it.copy(
            remoteUserJoined = true, 
            waitingForReceiver = false, 
            error = null
        ) 
    }
}
```

### 5. Suppress Agora's 10-Second Error
```kotlin
override fun onError(errorCode: Int) {
    // Ignore error 110 - let our 30-second timeout handle it
    if (errorCode == 110) {
        Log.w(TAG, "⏰ Agora timeout (110) - waiting up to 30 seconds")
        return
    }
    
    // Show other errors immediately
    // ...
}
```

### 6. Cleanup on ViewModel Destroy
```kotlin
override fun onCleared() {
    super.onCleared()
    connectionTimeoutJob?.cancel() // Cleanup
    // ...
}
```

## Benefits

| Before | After |
|--------|-------|
| 10-second timeout | **30-second timeout** |
| Too fast for receiver | Reasonable time to join |
| Many false timeouts | Fewer failed connections |
| Poor user experience | Better success rate |

## Timeline Comparison

### Before Fix:
```
0s  - Call initiated
3s  - Still waiting...
5s  - Still waiting...
8s  - Still waiting...
10s - ❌ ERROR! (receiver might still be opening app!)
```

### After Fix:
```
0s  - Call initiated
5s  - Still waiting...
10s - Still waiting... (Agora timeout ignored)
15s - Still waiting...
20s - Still waiting...
25s - Still waiting...
30s - ❌ Timeout (receiver truly unavailable)
```

## User Experience

### Scenario 1: Receiver joins in 15 seconds
- **Before:** ❌ Error shown at 10s (failed unnecessarily)
- **After:** ✅ Call connects successfully (waited 30s)

### Scenario 2: Receiver joins in 8 seconds  
- **Before:** ✅ Success
- **After:** ✅ Success (no change)

### Scenario 3: Receiver offline (never joins)
- **Before:** ❌ Error at 10s
- **After:** ❌ Error at 30s (gave more time)

## Testing Checklist

- [ ] Audio call - receiver joins within 30s → Should succeed
- [ ] Video call - receiver joins within 30s → Should succeed
- [ ] Audio call - receiver never joins → Error at 30s
- [ ] Video call - receiver never joins → Error at 30s
- [ ] Test with slow network → Should wait full 30s
- [ ] Test canceling call before timeout → Should cleanup properly

## Configuration

**Current timeout:** 30 seconds (30000 milliseconds)

To adjust timeout, change this line in both ViewModels:
```kotlin
kotlinx.coroutines.delay(30000) // Change to 45000 for 45 seconds
```

## Logs to Watch

When testing, look for these log messages:

✅ **Success Case:**
```
⏰ Starting 30-second timeout...
👤 Remote user joined: 12345
✅ Timeout cancelled - user joined successfully
```

❌ **Timeout Case:**
```
⏰ Starting 30-second timeout...
⏰ Agora timeout (110) detected - but waiting up to 30 seconds
⏰ 30-second timeout reached - receiver did not join
❌ Showing timeout error to user
```

---

**Date:** November 21, 2025  
**Status:** ✅ Implemented  
**Timeout:** 30 seconds (configurable)




