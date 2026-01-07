# Receiver Side Stuck on "Ringing" Screen - Root Cause Analysis

## 🐛 Problem Description

**Issue**: When the receiver accepts an incoming call, their screen shows "Ringing" instead of transitioning to the "Connected" screen with call controls. The caller side may transition correctly, but the receiver side remains stuck showing:
- "Ringing" status
- "Waiting for User_XXXX to answer..."
- Only a hang-up button (no mute/speaker controls)

**Expected Behavior**: After accepting, BOTH caller AND receiver should see the "Connected" screen with:
- Call duration timer
- Coins spent counter
- Mute, Speaker, and End Call buttons

---

## 🔍 Root Cause Analysis

### The Core Problem: Agora `onUserJoined()` Callback Asymmetry

The issue stems from how Agora's WebRTC callbacks work differently for the caller vs receiver:

#### 📱 Current Flow

```
CALLER SIDE:
1. User clicks "Call"
2. Navigate to AudioCallScreen
3. Join Agora channel (caller joins FIRST)
4. Shows "Ringing" screen (waiting for receiver)
5. Receiver accepts call
6. Receiver joins Agora channel
7. ✅ Agora fires onUserJoined() on CALLER'S device
8. ✅ Caller's screen transitions to "Connected"

RECEIVER SIDE:
1. Incoming call dialog appears
2. User clicks "Accept"
3. acceptIncomingCall() API called
4. Navigate to AudioCallScreen
5. Join Agora channel (caller ALREADY in channel)
6. ✅ Agora fires onJoinChannelSuccess()
7. ❌ Agora DOES NOT fire onUserJoined() (caller was already there!)
8. ❌ remoteUserJoined stays FALSE
9. ❌ Screen stuck showing "Ringing"
```

### Why `onUserJoined()` Doesn't Fire on Receiver Side

**Agora's `onUserJoined()` callback behavior:**
- Fires when a **REMOTE** user joins the channel **AFTER** you're already in it
- If someone was **ALREADY** in the channel when you joined, the callback **DOES NOT** fire
- Instead, you only get `onJoinChannelSuccess()`

**The consequence:**
```kotlin
// AudioCallScreen.kt line 139
if (!state.remoteUserJoined) {
    // RINGING STATE - Waiting for receiver to accept
    RingingCallUI(...)
} else {
    // CONNECTED STATE - Show full call UI
    ConnectedCallUI(...)
}
```

Since `remoteUserJoined` remains `false` on the receiver side, they're stuck in the ringing state forever.

---

## 🔬 Technical Deep Dive

### State Management Issue

**AudioCallState.kt (line 21-34):**
```kotlin
data class AudioCallState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val callId: String? = null,
    val duration: Int = 0,
    val coinsSpent: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isConnected: Boolean = false,
    val remoteUserJoined: Boolean = false,  // ⚠️ THIS STAYS FALSE ON RECEIVER SIDE!
    val isCallEnded: Boolean = false,
    val waitingForReceiver: Boolean = false
)
```

### Agora Callback Flow

**AudioCallViewModel.kt (line 168-180):**
```kotlin
val eventListener = object : AgoraEventListener {
    override fun onJoinChannelSuccess(channel: String, uid: Int) {
        Log.i(TAG, "✅ Joined channel successfully: $channel")
        _state.update { it.copy(isConnected = true, error = null) }
        // ⚠️ Sets isConnected but NOT remoteUserJoined!
    }
    
    override fun onUserJoined(uid: Int) {
        Log.i(TAG, "👤 Remote user joined: $uid")
        connectionTimeoutJob?.cancel()
        hasShownTimeoutError = false
        _state.update { it.copy(remoteUserJoined = true, waitingForReceiver = false, error = null) }
        // ✅ This callback ONLY fires on CALLER'S side, not RECEIVER'S!
    }
}
```

### Agora SDK Behavior

**AgoraManager.kt (line 367-378):**
```kotlin
private fun createEventHandler(eventListener: AgoraEventListener?) = object : IRtcEngineEventHandler() {
    
    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        Log.i(TAG, "✅ onJoinChannelSuccess: channel=$channel, uid=$uid")
        eventListener?.onJoinChannelSuccess(channel ?: "", uid)
        // ✅ Fires on BOTH caller and receiver when they join
    }
    
    override fun onUserJoined(uid: Int, elapsed: Int) {
        Log.i(TAG, "👤 onUserJoined: uid=$uid")
        this@AgoraManager.onUserJoined?.invoke(uid)
        eventListener?.onUserJoined(uid)
        // ✅ Fires on CALLER when receiver joins
        // ❌ Does NOT fire on RECEIVER (because caller was already there)
    }
}
```

---

## 💡 Solution Plan

### Option 1: Detect Receiver Role and Set State Immediately ⭐ RECOMMENDED

**Approach**: Detect if the user is joining as a RECEIVER (accepting an incoming call) and immediately set `remoteUserJoined = true` after `onJoinChannelSuccess()`.

**Implementation:**
1. Add a `isReceiver` parameter to `initializeAndJoinCall()` in `AudioCallViewModel`
2. When `isReceiver = true` and `onJoinChannelSuccess()` fires, also set `remoteUserJoined = true`
3. Pass `isReceiver = true` from the accept call flow in `FemaleHomeScreen`

**Pros:**
- ✅ Minimal code changes
- ✅ Logically correct (receiver knows caller is already there)
- ✅ No timing issues
- ✅ Maintains existing callback structure

**Cons:**
- ❌ Requires tracking call initiation context

### Option 2: Check Remote Users on Join

**Approach**: After joining channel, query Agora SDK for existing users in the channel and update state.

**Implementation:**
1. After `onJoinChannelSuccess()`, query channel for remote users
2. If any remote users exist, set `remoteUserJoined = true`

**Pros:**
- ✅ Works regardless of role
- ✅ No need to track caller/receiver distinction

**Cons:**
- ❌ Agora SDK may not have reliable API for this
- ❌ Timing issues (query might happen before Agora updates remote user list)
- ❌ More complex implementation

### Option 3: Use `onRemoteAudioStateChanged` Callback

**Approach**: Listen to `onRemoteAudioStateChanged()` to detect when remote user starts publishing audio.

**Implementation:**
1. Add logic in `onRemoteAudioStateChanged()` to set `remoteUserJoined = true`
2. This fires when remote user's audio stream becomes active

**Pros:**
- ✅ Agora provides this callback
- ✅ Works for both caller and receiver

**Cons:**
- ❌ Depends on audio stream state (might have delays)
- ❌ Could miss silent users or timing issues

---

## ✅ Recommended Solution: Option 1

### Implementation Steps:

#### Step 1: Add `isReceiver` Parameter to ViewModel

**File**: `AudioCallViewModel.kt`

Modify `initializeAndJoinCall()`:
```kotlin
fun initializeAndJoinCall(token: String, channelName: String, isReceiver: Boolean = false) {
    // ... existing code ...
    
    val eventListener = object : AgoraEventListener {
        override fun onJoinChannelSuccess(channel: String, uid: Int) {
            Log.i(TAG, "✅ Joined channel successfully: $channel")
            
            if (isReceiver) {
                // Receiver knows caller is already in channel
                Log.i(TAG, "👤 Receiver joined - caller already present")
                _state.update { 
                    it.copy(
                        isConnected = true, 
                        remoteUserJoined = true,  // ✅ Set immediately for receiver!
                        waitingForReceiver = false,
                        error = null
                    ) 
                }
            } else {
                // Caller waits for onUserJoined callback
                _state.update { it.copy(isConnected = true, error = null) }
            }
        }
        
        override fun onUserJoined(uid: Int) {
            // This still fires for caller when receiver joins
            Log.i(TAG, "👤 Remote user joined: $uid")
            connectionTimeoutJob?.cancel()
            hasShownTimeoutError = false
            _state.update { 
                it.copy(
                    remoteUserJoined = true, 
                    waitingForReceiver = false, 
                    error = null
                ) 
            }
        }
    }
}
```

#### Step 2: Pass `isReceiver = true` from Accept Call Flow

**File**: `FemaleHomeScreen.kt`

In the accept button's `onSuccess` callback:
```kotlin
onClick = {
    viewModel.acceptIncomingCall(
        onSuccess = {
            val route = Screen.AudioCall.createRoute(
                userId = call.callerId,
                callId = call.id,
                token = call.agoraToken ?: "",
                channel = call.channelName ?: ""
            )
            navController.navigate(route)
        },
        onError = { error ->
            Log.e("FemaleHomeScreen", "Failed to accept call: $error")
        }
    )
}
```

Then in `AudioCallScreen.kt`:
```kotlin
LaunchedEffect(userId, callId, token, channel, audioPermission.status) {
    // ... existing checks ...
    
    if (audioPermission.status.isGranted) {
        val isReceiver = callId.isNotEmpty() && token.isNotEmpty()
        // If we have callId and token from navigation, we're the receiver
        
        Log.d("AudioCallScreen", "✅ Joining as ${if (isReceiver) "RECEIVER" else "CALLER"}")
        viewModel.initializeAndJoinCall(token, channel, isReceiver = isReceiver)
    }
}
```

**Better Alternative**: Add a `role` parameter to the navigation route:
```kotlin
// In Screen.kt
object AudioCall : Screen("audio_call/{userId}/{callId}/{token}/{channel}/{role}") {
    fun createRoute(
        userId: String, 
        callId: String, 
        token: String, 
        channel: String,
        role: String = "caller"  // "caller" or "receiver"
    ): String
}
```

---

## 📊 Expected Flow After Fix

### ✅ CALLER SIDE (No change)
```
1. Initiate call
2. Join Agora channel (isReceiver = false)
3. onJoinChannelSuccess() → isConnected = true, but remoteUserJoined = false
4. Show "Ringing" screen
5. Receiver accepts and joins
6. onUserJoined() fires → remoteUserJoined = true
7. ✅ Screen transitions to "Connected"
```

### ✅ RECEIVER SIDE (Fixed!)
```
1. Accept incoming call
2. Join Agora channel (isReceiver = true)
3. onJoinChannelSuccess() → isConnected = true AND remoteUserJoined = true
4. ✅ IMMEDIATELY show "Connected" screen
5. No waiting, no stuck "Ringing" screen!
```

---

## 🧪 Testing Checklist

After implementing the fix:

1. **Test Normal Audio Call**:
   - ✅ Caller initiates call → sees "Ringing"
   - ✅ Receiver accepts call → IMMEDIATELY sees "Connected"
   - ✅ Caller sees "Connected" when receiver joins
   - ✅ Both see call timer and controls

2. **Test Video Call**:
   - ✅ Same behavior as audio call
   - ✅ Both see video streams and controls

3. **Test Edge Cases**:
   - ✅ Receiver accepts but has no internet → shows error
   - ✅ Caller hangs up before receiver accepts → receiver sees error
   - ✅ Both users in channel → both see "Connected"

---

## 📝 Files to Modify

1. **AudioCallViewModel.kt**
   - Add `isReceiver` parameter to `initializeAndJoinCall()`
   - Modify `onJoinChannelSuccess()` to set `remoteUserJoined = true` for receivers

2. **AudioCallScreen.kt**
   - Detect role (caller vs receiver) based on navigation parameters
   - Pass `isReceiver` to `viewModel.initializeAndJoinCall()`

3. **VideoCallViewModel.kt** (if exists)
   - Apply same fix for video calls

4. **VideoCallScreen.kt** (if exists)
   - Apply same fix for video calls

---

## 🎯 Summary

**Root Cause**: Agora's `onUserJoined()` callback doesn't fire when joining a channel where someone is already present. Receivers join AFTER the caller, so they never get the callback that sets `remoteUserJoined = true`, causing the UI to stay stuck on "Ringing".

**Solution**: Detect when the user is a RECEIVER (accepting an incoming call) and immediately set `remoteUserJoined = true` in the `onJoinChannelSuccess()` callback, since the receiver KNOWS the caller is already in the channel waiting.

**Impact**: Minimal code changes, fixes the issue completely for all receiver-side users, maintains existing caller flow.




