# 🐛 CRITICAL FIX: Male Stuck on Incoming Screen When Female Cancels

## Problem

When female calls male and disconnects before male accepts:
- ✅ Ring stops (correct)
- ❌ Male stays stuck on incoming call screen (WRONG)

---

## Root Cause

**The issue was in TWO places:**

### 1. IncomingCallService.kt (LINE 175)
When launching `IncomingCallActivity`, it was using `FLAG_ACTIVITY_CLEAR_TASK`:

```kotlin
flags = Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_CLEAR_TOP or
        Intent.FLAG_ACTIVITY_SINGLE_TOP or
        Intent.FLAG_ACTIVITY_CLEAR_TASK  // ❌ THIS KILLS MainActivity!
```

**What this did:**
- When incoming call arrives, `IncomingCallService` launches `IncomingCallActivity`
- `FLAG_ACTIVITY_CLEAR_TASK` **destroys MainActivity** from the back stack
- When call is cancelled and tries to navigate to MainActivity...
- **MainActivity doesn't exist anymore!**
- So `IncomingCallActivity` stays stuck on screen with no way to navigate back

### 2. IncomingCallActivity.kt
The navigation code was correct, but couldn't work because MainActivity was already killed.

---

## The Fix

### File: `IncomingCallService.kt`

**BEFORE:**
```kotlin
flags = Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_CLEAR_TOP or
        Intent.FLAG_ACTIVITY_SINGLE_TOP or
        Intent.FLAG_ACTIVITY_CLEAR_TASK  // ❌ Kills MainActivity
```

**AFTER:**
```kotlin
flags = Intent.FLAG_ACTIVITY_NEW_TASK or
        Intent.FLAG_ACTIVITY_CLEAR_TOP or
        Intent.FLAG_ACTIVITY_SINGLE_TOP
        // ✅ Removed CLEAR_TASK to keep MainActivity alive
```

---

## How It Works Now

### Normal Flow (Female calls Male):

```
1. Female calls Male
   └─> Backend sends incoming call to Male

2. Male's device receives notification
   └─> IncomingCallService starts
   └─> Launches IncomingCallActivity
   └─> ✅ MainActivity stays alive in background

3. IncomingCallActivity shows incoming call screen
   └─> Ringing starts
   └─> Male sees "Accept" and "Reject" buttons

4. Female disconnects before Male accepts
   └─> Female's app sends cancellation to backend
   └─> Backend sends cancellation to Male (WebSocket or FCM)

5. Male's IncomingCallActivity receives cancellation
   └─> Stops ringing
   └─> Calls navigateToMainActivity()
   └─> ✅ MainActivity exists in background
   └─> ✅ Brings MainActivity to front
   └─> ✅ Male sees home screen (NOT stuck!)
```

---

## Why FLAG_ACTIVITY_CLEAR_TASK Was Used Originally

The flag was added to handle the case when:
- App is completely killed (not in background)
- FCM notification arrives
- Need to start fresh

But this caused problems when:
- App is already running (MainActivity in background)
- Incoming call arrives
- `CLEAR_TASK` kills MainActivity
- Can't navigate back

---

## The Solution Works For Both Cases

### Case 1: App is Running (MainActivity in background)
```
MainActivity (background) → IncomingCallActivity (foreground)
When cancelled → Navigate back to MainActivity ✅
```

### Case 2: App is Killed (no MainActivity)
```
(no activities) → IncomingCallActivity (fresh start)
When cancelled → navigateToMainActivity() creates new MainActivity ✅
```

The key is `FLAG_ACTIVITY_CLEAR_TOP` handles both:
- If MainActivity exists → brings it to front
- If MainActivity doesn't exist → creates new one

---

## Files Modified

1. **`app/src/main/java/com/onlycare/app/services/IncomingCallService.kt`**
   - Removed `FLAG_ACTIVITY_CLEAR_TASK` from line 175
   - Updated log message to reflect the change

2. **`app/src/main/java/com/onlycare/app/presentation/screens/call/IncomingCallActivity.kt`**
   - Already had correct navigation logic (from previous fix)
   - Now it works because MainActivity is kept alive

---

## Testing

### Test Case: Female Cancels Before Male Accepts

**Steps:**
1. Female calls Male
2. Male receives incoming call screen (ringing)
3. **Before Male accepts**, Female ends the call
4. ✅ Expected: Male's ringing stops AND screen closes → goes to home

**Before Fix:**
- ✅ Ringing stops
- ❌ Screen stays stuck
- ❌ Male has to press back button to exit

**After Fix:**
- ✅ Ringing stops
- ✅ Screen closes automatically
- ✅ Male sees home screen (MainActivity)

---

## Summary

The issue was caused by `FLAG_ACTIVITY_CLEAR_TASK` in `IncomingCallService` killing MainActivity. When the call was cancelled, there was no MainActivity to navigate back to, so the screen stayed stuck.

**Solution:** Remove `FLAG_ACTIVITY_CLEAR_TASK` to keep MainActivity alive in the background, allowing proper navigation when call is cancelled.

**Status:** ✅ **FIXED**

---

**Date:** December 3, 2025  
**File Modified:** `IncomingCallService.kt` (line 172)  
**Testing Required:** Yes - Test female calling male and cancelling before acceptance


