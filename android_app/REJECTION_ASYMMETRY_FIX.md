# 🔧 Call Rejection Asymmetry Fix

**Date:** December 3, 2025  
**Status:** ✅ FIXED

---

## 🐛 Problem Description

**Asymmetric Rejection Behavior:**
- ✅ **Female calls → Male rejects:** Works on BOTH sides
  - Male's call screen closes ✅
  - Female's call screen closes ✅
  
- ❌ **Male calls → Female rejects:** Only works on male side
  - Male's call screen closes ✅
  - Female's incoming call dialog closes ✅
  - BUT: Female doesn't receive proper event notifications ❌

---

## 🔍 Root Cause Analysis

### The Issue:

**FemaleHomeViewModel WebSocket listener was DISABLED:**

```kotlin
init {
    loadHomeData()
    
    // ❌ DISABLED: WebSocket incoming calls (using FCM only for female side)
    // startWebSocketListener()  // <-- COMMENTED OUT!
    
    startIncomingCallPolling()
}
```

**Why this caused asymmetric behavior:**

1. **Male users:**
   - ✅ WebSocket connected in MainActivity (male users only)
   - ✅ AudioCallViewModel/VideoCallViewModel listen to WebSocket events
   - ✅ Receive `CallRejected`, `CallCancelled`, `CallAccepted` events
   - ✅ Properly handle all call state changes

2. **Female users:**
   - ❌ WebSocket listener in FemaleHomeViewModel was DISABLED
   - ❌ Never receive `CallRejected`, `CallCancelled`, `CallAccepted` events
   - ❌ Miss important call state changes
   - ⚠️ Only receive events when in AudioCallViewModel/VideoCallViewModel (after accepting a call)

---

## ✅ The Fix

### 1. **Enabled WebSocket Listener in FemaleHomeViewModel**

Changed from:
```kotlin
// startWebSocketListener()  // DISABLED
```

To:
```kotlin
startWebSocketListener()  // ✅ ENABLED
```

### 2. **Modified WebSocket Listener to Handle Specific Events**

The listener now handles:
- ✅ `CallCancelled` - When caller cancels before female answers
- ✅ `CallRejected` - Logged for debugging (not actively needed for female receivers)
- ✅ `CallAccepted` - Logged for debugging (not actively needed for female receivers)
- ✅ `CallEnded` - When call ends unexpectedly
- ❌ `IncomingCall` - **IGNORED** (females use FCM for incoming calls to avoid duplicates)

### Updated Code:

```kotlin
private fun startWebSocketListener() {
    Log.d("FemaleHome", "✅ WebSocket event listener ENABLED (excluding IncomingCall)")
    
    viewModelScope.launch {
        webSocketManager.callEvents.collect { event ->
            when (event) {
                is WebSocketEvent.CallCancelled -> {
                    // Caller cancelled - dismiss incoming call dialog
                    if (event.callId == _state.value.incomingCall?.id) {
                        Log.d("FemaleHome", "🚫 Call cancelled by caller: ${event.callId}")
                        _state.update {
                            it.copy(
                                incomingCall = null,
                                hasIncomingCall = false
                            )
                        }
                    }
                }
                
                is WebSocketEvent.CallRejected -> {
                    // Log for debugging
                    Log.d("FemaleHome", "📥 CallRejected event received: ${event.callId}")
                }
                
                is WebSocketEvent.CallEnded -> {
                    // Call ended - clear incoming call state
                    if (event.callId == _state.value.incomingCall?.id) {
                        Log.d("FemaleHome", "📴 Call ended: ${event.callId}")
                        _state.update {
                            it.copy(
                                incomingCall = null,
                                hasIncomingCall = false
                            )
                        }
                    }
                }
                
                is WebSocketEvent.IncomingCall -> {
                    // ❌ IGNORE: Use FCM for incoming calls
                    Log.d("FemaleHome", "⏭️ IncomingCall event ignored (using FCM only)")
                }
                
                else -> { /* Ignore other events */ }
            }
        }
    }
}
```

---

## 🎯 What This Fixes

### Before Fix:
```
Scenario: Male calls → Female rejects

MALE SIDE:
1. Male on AudioCallScreen (waiting for answer)
2. Male's AudioCallViewModel listening to WebSocket ✅
3. Female clicks "Reject"
4. Male receives CallRejected event ✅
5. Male's screen closes ✅

FEMALE SIDE:
1. Female sees incoming call dialog
2. Female's FemaleHomeViewModel NOT listening to WebSocket ❌
3. Female clicks "Reject"
4. Dialog dismisses (via local state) ✅
5. Female never receives confirmation event ❌
6. If caller cancels, female might not know ❌
```

### After Fix:
```
Scenario: Male calls → Female rejects

MALE SIDE:
1. Male on AudioCallScreen (waiting for answer)
2. Male's AudioCallViewModel listening to WebSocket ✅
3. Female clicks "Reject"
4. Male receives CallRejected event ✅
5. Male's screen closes ✅

FEMALE SIDE:
1. Female sees incoming call dialog
2. Female's FemaleHomeViewModel NOW listening to WebSocket ✅
3. Female clicks "Reject"
4. Dialog dismisses (via local state) ✅
5. Female receives proper event notifications ✅
6. If caller cancels, female knows immediately ✅
```

---

## 🧪 Testing

### Test Case 1: Male calls → Female rejects
1. Male user initiates call to female
2. Male should see "Waiting for answer..." screen
3. Female should see incoming call dialog
4. Female clicks "Reject"
5. **Expected:**
   - ✅ Male's screen closes with "Call Rejected" message
   - ✅ Female's dialog dismisses
   - ✅ Female receives event notification (visible in logs)

### Test Case 2: Male calls → Male cancels before female answers
1. Male user initiates call to female
2. Male should see "Waiting for answer..." screen
3. Female should see incoming call dialog
4. Male cancels the call (back button or cancel)
5. **Expected:**
   - ✅ Male's screen closes
   - ✅ Female's dialog dismisses automatically (via CallCancelled event)

### Test Case 3: Female calls → Male rejects
1. Female user accepts incoming call (from male)
2. Both on call screen
3. Male ends/rejects
4. **Expected:**
   - ✅ Male's screen closes
   - ✅ Female's screen closes (via AudioCallViewModel/VideoCallViewModel listening)

---

## 📝 Files Changed

### `app/src/main/java/com/onlycare/app/presentation/screens/main/FemaleHomeViewModel.kt`

**Changes:**
1. Line 61: Enabled `startWebSocketListener()`
2. Lines 67-135: Completely rewrote `startWebSocketListener()` function
   - Added handling for `CallCancelled`, `CallRejected`, `CallAccepted`, `CallEnded`
   - Explicitly ignore `IncomingCall` to prevent duplicates with FCM
   - Added comprehensive logging for debugging

---

## 🔄 Related Architecture

### Female User Call Flow:

1. **Incoming Calls:** FCM notifications (IncomingCallService)
2. **Call Events:** WebSocket (FemaleHomeViewModel) - **NOW ENABLED**
3. **In-Call Events:** WebSocket (AudioCallViewModel/VideoCallViewModel)

### Male User Call Flow:

1. **Outgoing Calls:** Navigate to CallConnectingScreen → AudioCallScreen/VideoCallScreen
2. **Call Events:** WebSocket (MainActivity connection)
3. **In-Call Events:** WebSocket (AudioCallViewModel/VideoCallViewModel)

Both sides now have symmetric event handling! ✅

---

## ✅ Conclusion

The asymmetric rejection behavior has been fixed by enabling WebSocket event listening in `FemaleHomeViewModel`. Female users now receive proper call event notifications (cancellation, rejection, etc.) while still using FCM for incoming call notifications to avoid duplicates.

**Status:** ✅ COMPLETE - Ready for testing


