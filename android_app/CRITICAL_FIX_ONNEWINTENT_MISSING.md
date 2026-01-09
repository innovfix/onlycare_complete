# 🔴 CRITICAL ROOT CAUSE: CallActivity Missing onNewIntent()

## The Real Problem (Finally Found!)

When user is on **rating screen** and accepts a new call, the call **immediately ends** without starting.

### Why This Happens

```
Previous Call Flow:
1. User in call → Call ends
2. Navigate to CallEndedScreen (inside CallActivity)
3. Navigate to RateUserScreen (inside CallActivity)
4. CallActivity is still running with OLD call data

New Call Arrives:
5. User accepts new call
6. IncomingCallActivity launches CallActivity with:
   - FLAG_ACTIVITY_SINGLE_TOP (reuse existing activity)
   - FLAG_ACTIVITY_CLEAR_TOP (clear activities on top)
   
7. ❌ PROBLEM: CallActivity already exists (on rating screen)
8. ❌ Android REUSES the existing CallActivity
9. ❌ onCreate() is NOT called
10. ❌ onNewIntent() is NOT implemented - so new call data is IGNORED!
11. ❌ Old rating screen stays visible with OLD ViewModel
12. ❌ Old ViewModel has stale state → call ends immediately
```

### The Intent Flags Explained

```kotlin
// From IncomingCallActivity.kt
Intent(context, CallActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or 
            Intent.FLAG_ACTIVITY_SINGLE_TOP
}
```

**FLAG_ACTIVITY_SINGLE_TOP**:
- If CallActivity is already on top → **reuse it** (call `onNewIntent()`)
- Don't call `onCreate()`, just update the intent

**FLAG_ACTIVITY_CLEAR_TOP**:
- Clear any activities on top of CallActivity in the back stack
- When combined with SINGLE_TOP, it reuses the activity

Result: **CallActivity is reused, but it never gets the new call data!**

## The Fix

### 1. Add `onNewIntent()` to CallActivity

```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    
    Log.d(TAG, "🔄 CallActivity.onNewIntent() - NEW CALL WHILE ACTIVITY RUNNING!")
    Log.d(TAG, "⚠️ User was on rating/ended screen and accepted a new call")
    Log.d(TAG, "   We need to FULLY RESTART the activity to clear ALL stale state")
    
    // Update the intent for recreate()
    setIntent(intent)
    
    // ✅ CRITICAL: Use recreate() to fully restart the activity
    // This will:
    // 1. Destroy current activity (clears ViewModels, navigation state, everything)
    // 2. Create new activity with the new intent
    // 3. Call onCreate() with fresh state
    recreate()
}
```

This ensures that when CallActivity is reused, it:
1. Updates the intent with new call data
2. Calls `recreate()` to FULLY restart the activity
3. Destroys all ViewModels, navigation state, and Compose state
4. Calls `onCreate()` again with the new intent
5. Creates completely fresh state for the new call

### 2. Make ViewModel Reset Depend on callId

```kotlin
// In AudioCallScreen.kt
LaunchedEffect(callId) {  // ✅ Changed from LaunchedEffect(Unit)
    Log.d(TAG, "🔄 Resetting ViewModel for new call: $callId")
    viewModel.resetForNewCall()
}
```

This ensures that:
1. When Compose tree is recreated with new callId
2. The effect runs again (because callId changed)
3. ViewModel state is reset for the new call

### 3. Triple Guard in UI

```kotlin
LaunchedEffect(state.isCallEnded, state.callId, state.callReallyStarted) {
    // Only end call if ALL THREE conditions are true:
    if (state.isCallEnded && !state.callId.isNullOrEmpty() && state.callReallyStarted) {
        // End the call
    }
}
```

This prevents stale `isCallEnded=true` from ending a new call that hasn't really started yet.

## How It Works Now

```
Previous Call:
1. User in call → Call ends
2. Navigate to RateUserScreen (inside CallActivity)
3. CallActivity running with old data

New Call Arrives:
4. User accepts new call
5. IncomingCallActivity launches CallActivity with SINGLE_TOP
6. ✅ CallActivity.onNewIntent() is called
7. ✅ setIntent(newIntent) - Update intent with new call data
8. ✅ recreate() - Fully restart the activity
9. ✅ Activity destroyed (ViewModels cleared, navigation reset)
10. ✅ onCreate() called again with new intent
11. ✅ Fresh ViewModel created
12. ✅ Fresh NavController created
13. ✅ AudioCallScreen with new callId
14. ✅ LaunchedEffect(callId) runs → resetForNewCall()
15. ✅ Call initialization starts fresh
16. ✅ Call proceeds normally!
```

## Why Previous Fixes Didn't Work

1. **Resetting state in ViewModel**: Didn't help because new call data never reached the ViewModel
2. **Cancelling jobs in resetForNewCall()**: Didn't help because the function wasn't being called
3. **Adding callReallyStarted flag**: Helped but wasn't enough - new call data still missing
4. **Guards in LaunchedEffect**: Helped but weren't the root cause

The **real root cause** was that **CallActivity wasn't receiving new call data** at all!

## Files Changed

1. **CallActivity.kt**:
   - Added `onNewIntent()` override
   - Refactored `onCreate()` to use `extractCallDataAndSetupUI()`

2. **AudioCallScreen.kt**:
   - Changed `LaunchedEffect(Unit)` to `LaunchedEffect(callId)`
   - Ensures reset runs for every new call

3. **AudioCallViewModel.kt**:
   - Added `callReallyStarted` flag
   - Enhanced `resetForNewCall()` with job cancellation
   - Added guards in polling job

## Testing

To test this fix:
1. Start a call
2. Let it end normally
3. Go to rating screen
4. Have someone call you
5. Accept the call
6. ✅ Call should start normally (not end immediately)

## Lessons Learned

- **Always check Activity lifecycle methods** when using SINGLE_TOP
- **onNewIntent()** is critical for activities that can be reused
- **Don't assume onCreate() is always called** - it's not with SINGLE_TOP
- **LaunchedEffect(Unit)** might not rerun when you expect - use specific keys
- **Root cause analysis** requires understanding the entire flow, not just the symptoms

This was a **classic Activity reuse issue** that's easy to miss!
