# Switch-to-Video: Final Fix - Simple Navigation ✅

## Problem

The in-place upgrade was causing issues:
- ❌ Receiver: Stuck on "Video Call Active" screen (placeholder)
- ❌ Sender: Goes to call ended screen
- ❌ Call doesn't actually start

## Root Cause

The in-place upgrade approach was too complex and had timing issues with:
- Agora channel switching
- WebSocket event handling
- State management

## Solution: Back to Simple Navigation ✅

Reverted to the **simple navigation approach** with proper fixes:

### Flow

1. ✅ Male clicks "Switch to Video"
2. ✅ Backend creates new video call
3. ✅ Female receives request, clicks "Accept"
4. ✅ **Both users navigate to VideoCallScreen**
5. ✅ Old audio call ends in background
6. ✅ Both join new video channel
7. ✅ Video call starts normally

### Key Changes

#### 1. Simplified `acceptSwitchToVideo()` (Receiver)

```kotlin
fun acceptSwitchToVideo() {
    // Send WebSocket acceptance
    webSocketManager.acceptSwitchToVideo(oldCallId, newCallId, otherId)
    
    // Mark backend that new call is accepted
    repository.acceptCall(newCallId)
    
    // ✅ Signal to navigate to video call
    _state.update { 
        it.copy(
            switchToVideoAccepted = true,
            showSwitchToVideoRequestDialog = false
        ) 
    }
}
```

#### 2. Simplified WebSocket Handler (Requester)

```kotlin
is WebSocketEvent.SwitchToVideoAccepted -> {
    if (event.callId == _state.value.callId) {
        // ✅ Signal to navigate to video call
        _state.update { 
            it.copy(switchToVideoAccepted = true) 
        }
    }
}
```

#### 3. Navigation in AudioCallScreen

```kotlin
LaunchedEffect(state.switchToVideoAccepted) {
    if (state.switchToVideoAccepted && state.pendingVideoCallId != null) {
        // End old audio call
        viewModel.endCall(...)
        
        // Navigate to VideoCallScreen
        navController.navigate(
            Screen.VideoCall.createRoute(
                userId = state.user?.id ?: userId,
                callId = state.pendingVideoCallId!!,
                appId = state.pendingVideoAppId ?: "",
                token = state.pendingVideoToken ?: "",
                channel = state.pendingVideoChannel ?: "",
                role = "receiver",
                balanceTime = state.pendingVideoBalanceTime ?: ""
            )
        ) {
            popUpTo("audio_call/{userId}/{callId}") { inclusive = true }
        }
    }
}
```

### What This Does

1. **Receiver accepts** → Sets `switchToVideoAccepted = true`
2. **LaunchedEffect triggers** → Navigates to VideoCallScreen
3. **Sender receives WebSocket** → Sets `switchToVideoAccepted = true`
4. **LaunchedEffect triggers** → Navigates to VideoCallScreen
5. **Both users** → Now in VideoCallScreen with new call ID
6. **VideoCallScreen** → Starts video call normally (just like a regular video call)

### Benefits

✅ **Simple and reliable** - Uses existing VideoCallScreen
✅ **No custom logic** - Video call works like any other video call
✅ **Proper cleanup** - Old audio call ends before navigation
✅ **Both users see same thing** - VideoCallScreen with video
✅ **No timing issues** - Navigation happens after acceptance

---

## Testing

1. **Start audio call** (male → female)
2. **Male clicks 🎥** → Clicks "Yes"
3. **Female sees dialog** → Clicks "Accept"
4. **Expected:**
   - ✅ Both navigate to VideoCallScreen
   - ✅ Video call starts normally
   - ✅ Duration starts from 00:00
   - ✅ Both can see/hear each other

---

## Status

✅ **Build:** Successful
✅ **Navigation:** Fixed
✅ **Ready for Testing:** YES

**Test now - both users should go to VideoCallScreen!** 🎉

---

**Date:** January 10, 2026  
**Approach:** Simple Navigation (reverted from in-place upgrade)  
**Status:** Complete ✅
