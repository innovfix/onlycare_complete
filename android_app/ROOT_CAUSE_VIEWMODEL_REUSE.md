# 🔴 ROOT CAUSE: ViewModel Being Reused From Previous Call

## The Real Problem

The ViewModel (`AudioCallViewModel`) is **scoped to the Activity/Navigation** and is **NOT being cleared** between calls.

When you:
1. Complete a call → ViewModel has `isCallEnded = true`
2. Go to rating screen → ViewModel **STILL EXISTS** with stale state
3. Accept new call → **SAME ViewModel instance** is reused
4. Even though we reset `isCallEnded` in `setCallId()`, the `LaunchedEffect` might fire BEFORE `setCallId()` is called

## The Flow

```
Previous Call:
├─ AudioCallScreen opens
├─ AudioCallViewModel created (Hilt scoped)
├─ Call ends: isCallEnded = true
└─ Navigate to RateUserScreen (ViewModel still alive!)

Rating Screen:
├─ RateUserScreen displayed
└─ AudioCallViewModel STILL IN MEMORY with isCallEnded = true

New Call Accepted:
├─ Navigate to AudioCallScreen
├─ SAME AudioCallViewModel instance (Hilt reuses it!)
├─ LaunchedEffect(state.isCallEnded) fires IMMEDIATELY
│   └─ Sees isCallEnded = true (from previous call!)
│   └─ Calls endCall() immediately!
├─ setCallId() called AFTER LaunchedEffect
│   └─ Too late! Call already ending
└─ Call ends before connecting
```

## Why Our Previous Fix Didn't Work

We added:
```kotlin
fun setCallId(callId: String) {
    _state.update { it.copy(
        callId = callId,
        isCallEnded = false  // Reset here
    ) }
}
```

But the problem is:
1. `LaunchedEffect(state.isCallEnded)` fires when AudioCallScreen composes
2. It sees `isCallEnded = true` (from previous call)
3. It immediately calls `endCall()`
4. THEN `setCallId()` is called (too late!)

## The Real Fix

We need to reset the state **BEFORE** the LaunchedEffect checks it.

### Option 1: Reset in AudioCallScreen (BEST)

Add a LaunchedEffect that runs FIRST and resets the state:

```kotlin
// ✅ FIX: Reset ViewModel state when screen opens (before other LaunchedEffects)
LaunchedEffect(Unit) {
    android.util.Log.d("AudioCallScreen", "🔄 Resetting ViewModel state for new call")
    viewModel.resetForNewCall()
}
```

Then in ViewModel:
```kotlin
fun resetForNewCall() {
    Log.d(TAG, "🔄 resetForNewCall() called - clearing all stale state")
    _state.update {
        it.copy(
            isCallEnded = false,
            error = null,
            duration = 0,
            coinsSpent = 0,
            remoteUserJoined = false,
            isConnected = false,
            waitingForReceiver = false,
            wasEverConnected = false
        )
    }
}
```

### Option 2: Use ViewModelStoreOwner

Scope the ViewModel to the NavBackStackEntry instead of the Activity:

```kotlin
val viewModel: AudioCallViewModel = hiltViewModel(
    viewModelStoreOwner = navController.getBackStackEntry(navController.currentDestination?.route ?: "")
)
```

This creates a NEW ViewModel for each navigation.

### Option 3: Clear ViewModel on Rating Screen

When entering rating screen, clear the ViewModel:

```kotlin
LaunchedEffect(Unit) {
    // Clear ViewModel
    viewModelStore.clear()
}
```

But this is hacky and not recommended.

## Recommended Solution

**Use Option 1** - Add explicit reset in AudioCallScreen:

1. Add `resetForNewCall()` to ViewModel
2. Call it in AudioCallScreen's first LaunchedEffect
3. This ensures state is clean BEFORE other LaunchedEffects check it
