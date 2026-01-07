# Accept Call "No Data Returned" Error - Fix

## 🐛 The Error

```
Error
Failed to accept call: Call accepted but no data returned
```

**When it happened:** After clicking "Accept" on an incoming call dialog.

---

## 🔍 Root Cause

### The Problem

The `acceptCall()` method was expecting the backend to return a full `CallDto` object:

```kotlin
// BEFORE - Line 639 in ApiDataRepository.kt
suspend fun acceptCall(callId: String): Result<CallDto> {
    // ...
    if (response.isSuccessful && response.body()?.success == true) {
        val callDto = response.body()?.data
        if (callDto != null) {  // ❌ FAILED HERE
            Result.success(callDto)
        } else {
            // ❌ Error: "Call accepted but no data returned"
            Result.failure(Exception("Call accepted but no data returned"))
        }
    }
}
```

**What the backend actually returns:**
```json
{
  "success": true,
  "data": null,    // ❌ No data returned
  "message": "Call accepted"
}
```

### Why This Is Wrong

1. **Receiver already has all the needed information** from `IncomingCallDto`:
   - ✅ `callId`
   - ✅ `callerId`
   - ✅ `agoraToken`
   - ✅ `channelName`

2. **The backend doesn't need to return this data again** - it just needs to update the call status to "accepted"

3. **Inconsistent with similar methods** like `rejectCall()` which returns `Result<String>` and handles null data gracefully

---

## ✅ The Fix

### Changed Method Signature

Changed from returning `CallDto` to returning `Unit`:

```kotlin
// BEFORE
suspend fun acceptCall(callId: String): Result<CallDto>

// AFTER
suspend fun acceptCall(callId: String): Result<Unit>
```

### Simplified Logic

**File:** `app/src/main/java/com/onlycare/app/data/repository/ApiDataRepository.kt`

```kotlin
suspend fun acceptCall(callId: String): Result<Unit> {
    return try {
        Log.d(TAG, "Accepting call: $callId")
        val response = callApiService.acceptCall(callId)
        
        if (response.isSuccessful && response.body()?.success == true) {
            Log.d(TAG, "Call accepted successfully: $callId")
            // ✅ No need to return call data - receiver already has it
            Result.success(Unit)
        } else {
            val errorMsg = response.errorBody()?.string() ?: response.message()
            Log.e(TAG, "Failed to accept call: $errorMsg")
            Result.failure(Exception("Failed to accept call: $errorMsg"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "acceptCall error", e)
        Result.failure(e)
    }
}
```

### Updated ViewModel

**File:** `app/src/main/java/com/onlycare/app/presentation/screens/main/FemaleHomeViewModel.kt`

```kotlin
// BEFORE
result.onSuccess { callDto ->  // ❌ Expected CallDto
    // ...
}

// AFTER
result.onSuccess {  // ✅ No data expected
    // ...
}
```

---

## 📊 Comparison: Similar Methods

| Method | Return Type | Requires Data? | Pattern |
|--------|-------------|----------------|---------|
| `rejectCall()` | `Result<String>` | ❌ No (uses default) | ✅ Correct |
| `acceptCall()` (before) | `Result<CallDto>` | ✅ Yes (fails if null) | ❌ Wrong |
| `acceptCall()` (after) | `Result<Unit>` | ❌ No | ✅ Correct |

---

## 🧪 Testing

### Test Case 1: Accept Audio Call ✅
1. Caller initiates audio call
2. Receiver sees dialog
3. Receiver clicks "Accept"
4. **Expected:** No error, navigates to AudioCallScreen
5. **Result:** ✅ Works!

### Test Case 2: Accept Video Call ✅
1. Caller initiates video call
2. Receiver accepts
3. **Expected:** No error, navigates to VideoCallScreen
4. **Result:** ✅ Works!

### Test Case 3: Backend Returns Error ✅
1. Simulate backend error (e.g., call already ended)
2. **Expected:** Clear error message shown
3. **Result:** ✅ Proper error handling

---

## 📝 Files Modified

1. **ApiDataRepository.kt**
   - Changed `acceptCall()` return type from `Result<CallDto>` to `Result<Unit>`
   - Removed null check for data
   - Added comment explaining why data isn't needed

2. **FemaleHomeViewModel.kt**
   - Updated `acceptIncomingCall()` to handle `Result<Unit>`
   - Changed `onSuccess { callDto ->` to `onSuccess {`

---

## 🎯 Key Takeaway

**The Fix:** Don't require data from backend when the client already has all the information it needs. The `acceptCall` API just needs to update the backend state - it doesn't need to return data.

**Pattern to Follow:**
- ✅ If client has all needed info → Return `Result<Unit>`
- ✅ If client needs response data → Return `Result<DataType>`
- ✅ Always handle null data gracefully with defaults

---

**Date:** November 21, 2025  
**Status:** ✅ Fixed and Tested  
**Related:** RINGING_SCREEN_FIX.md




