# Incoming Call Dialog Fix

## Problem
The incoming call dialog was showing repeatedly even after the user accepted or rejected the call. It would keep appearing every 3 seconds on the same call.

## Root Cause
1. **Missing API Call**: When rejecting a call, the app was only dismissing the dialog locally without notifying the backend
2. **Polling Issue**: The `FemaleHomeViewModel` polls for incoming calls every 3 seconds
3. **Backend Not Updated**: Since the backend was never told the call was rejected, it kept returning the same call as "incoming"
4. **Loop Created**: Dialog dismissed → Poll runs → Same call still "incoming" → Dialog shows again

## Code Issues Found

### 1. FemaleHomeScreen.kt (Line 114-115)
```kotlin
onClick = {
    // TODO: Reject call API  ← This was never implemented!
    viewModel.dismissIncomingCall()
}
```

### 2. Missing Repository Method
The `ApiDataRepository` didn't have a `rejectCall()` method, even though the API endpoint existed.

### 3. No Call Tracking
No mechanism to prevent showing the same call multiple times if API fails.

## Solution Implemented

### 1. Added `processedCallIds` Tracking
**File**: `FemaleHomeViewModel.kt`

Added a Set to track which calls have been processed:

```kotlin
data class FemaleHomeState(
    // ... other fields ...
    val processedCallIds: Set<String> = emptySet()
)
```

### 2. Updated Polling Logic
**File**: `FemaleHomeViewModel.kt`

Modified `startIncomingCallPolling()` to skip already processed calls:

```kotlin
val latestCall = incomingCalls.firstOrNull { call ->
    !_state.value.processedCallIds.contains(call.id)
}
```

### 3. Implemented `rejectIncomingCall()`
**File**: `FemaleHomeViewModel.kt`

Created proper reject function that:
- Dismisses dialog immediately (better UX)
- Marks call as processed
- Calls backend API to reject

```kotlin
fun rejectIncomingCall() {
    val call = _state.value.incomingCall ?: return
    val callId = call.id
    
    // Dismiss dialog immediately
    _state.update {
        it.copy(
            incomingCall = null,
            hasIncomingCall = false,
            processedCallIds = it.processedCallIds + callId
        )
    }
    
    // Call backend API
    viewModelScope.launch {
        val result = repository.rejectCall(callId)
        // Handle result...
    }
}
```

### 4. Updated `dismissIncomingCall()` for Accept
**File**: `FemaleHomeViewModel.kt`

When accepting, also mark call as processed:

```kotlin
fun dismissIncomingCall() {
    val callId = _state.value.incomingCall?.id
    _state.update {
        it.copy(
            incomingCall = null,
            hasIncomingCall = false,
            processedCallIds = if (callId != null) it.processedCallIds + callId else it.processedCallIds
        )
    }
}
```

### 5. Added Repository Method
**File**: `ApiDataRepository.kt`

Implemented the missing `rejectCall()` method:

```kotlin
suspend fun rejectCall(callId: String): Result<String> {
    return try {
        Log.d(TAG, "Rejecting call: $callId")
        val response = callApiService.rejectCall(callId)
        
        if (response.isSuccessful && response.body()?.success == true) {
            val message = response.body()?.data ?: "Call rejected"
            Result.success(message)
        } else {
            Result.failure(Exception("Failed to reject call"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "rejectCall error", e)
        Result.failure(e)
    }
}
```

### 6. Updated UI to Use New Function
**File**: `FemaleHomeScreen.kt`

Changed reject button to call the proper function:

```kotlin
dismissButton = {
    Button(
        onClick = {
            // Reject the call via API and dismiss dialog
            viewModel.rejectIncomingCall()
        },
        // ... styling ...
    ) {
        // ... UI ...
    }
}
```

## Benefits of This Fix

1. **No Repeated Dialogs**: Once a call is rejected/accepted, it won't show again
2. **Backend Synced**: Server knows the call was rejected
3. **Better UX**: Dialog dismisses immediately, API call happens in background
4. **Fault Tolerant**: Even if API call fails, dialog won't repeat (thanks to `processedCallIds`)
5. **Proper State Management**: Call state is properly tracked throughout the app lifecycle

## Testing Recommendations

1. **Accept Call**: Verify dialog doesn't reappear after accepting
2. **Reject Call**: Verify dialog doesn't reappear after rejecting
3. **Network Failure**: Test what happens if reject API fails - dialog should still not reappear
4. **Multiple Calls**: If multiple calls come in, each should show once only
5. **Server State**: Verify backend marks call as rejected/accepted

## Files Modified

1. `app/src/main/java/com/onlycare/app/presentation/screens/main/FemaleHomeViewModel.kt`
   - Added `processedCallIds` to state
   - Updated polling logic to skip processed calls
   - Implemented `rejectIncomingCall()` method
   - Updated `dismissIncomingCall()` to track processed calls

2. `app/src/main/java/com/onlycare/app/presentation/screens/main/FemaleHomeScreen.kt`
   - Changed reject button to call `rejectIncomingCall()`

3. `app/src/main/java/com/onlycare/app/data/repository/ApiDataRepository.kt`
   - Added `rejectCall()` method

## Notes

- The `acceptCall` API endpoint exists but isn't currently used when accepting calls
- Consider implementing proper accept API call in the future for better backend tracking
- The polling interval (3 seconds) could be made configurable if needed




