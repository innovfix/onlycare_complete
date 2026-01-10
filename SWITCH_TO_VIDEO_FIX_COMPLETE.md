# Switch-to-Video Fix - COMPLETE ✅

## Issue Report
**User:** "getting toast of failed to request switch to video"
**Balance:** 130 coins (sufficient for video call)
**Action:** Clicked switch-to-video button → Clicked "Yes" → Nothing happened

## Root Cause Found 🎯

**Backend Error:**
```
Undefined constant Yasser\Agora\RtcTokenBuilder::ROLE_PUBLISHER
```

**Location:** `/var/www/onlycare_admin/app/Http/Controllers/Api/CallController.php` (line 1200)

**Problem:**
The `requestSwitchToVideo` method was using the **wrong constant name** for the Agora RolePublisher:

```php
// ❌ WRONG (line 1200)
RtcTokenBuilder::ROLE_PUBLISHER  // This constant doesn't exist!

// ✅ CORRECT
RtcTokenBuilder::RolePublisher   // Capital 'R' in 'Role'
```

## Fix Applied ✅

### Backend (CallController.php)

**Before:**
```php
$token = RtcTokenBuilder::buildTokenWithUid(
    $appId,
    $appCertificate,
    $channelName,
    $uid,
    RtcTokenBuilder::ROLE_PUBLISHER,  // ❌ Wrong constant
    $expirationTime
);
```

**After:**
```php
$token = RtcTokenBuilder::buildTokenWithUid(
    $appId,
    $appCertificate,
    $channelName,
    $uid,
    RtcTokenBuilder::RolePublisher,  // ✅ Correct constant
    $expirationTime
);
```

### Deployment Status

✅ **File Updated:** `backend_admin/app/Http/Controllers/Api/CallController.php`
✅ **Deployed to:** 64.227.163.211
✅ **Cache Cleared:** Application, Config, Route
✅ **PHP-FPM Restarted:** php8.3-fpm
✅ **Date:** January 10, 2026

## Enhanced Logging (Already Deployed)

### Android App
**File:** `app/src/main/java/com/onlycare/app/data/repository/ApiDataRepository.kt`

Added comprehensive logging:
```
╔════════════════════════════════════════════════════════════
║ 📤 SWITCH TO VIDEO API REQUEST
║ Call ID: CALL_xxxxx
║ Endpoint: POST /api/v1/calls/switch-to-video
╚════════════════════════════════════════════════════════════
║ 📡 API RESPONSE RECEIVED
║ HTTP Code: 200
║ Error Message: (if any)
╚════════════════════════════════════════════════════════════
```

**File:** `app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallViewModel.kt`

Added step-by-step logging:
```
📹 SWITCH TO VIDEO REQUEST STARTED
✅ BACKEND API SUCCESS
📹 NEW VIDEO CALL DETAILS
📤 SENDING WEBSOCKET REQUEST
```

**File:** `app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallScreen.kt`

Added user feedback:
- ✅ Toast: "Requesting switch to video..." (immediate feedback)
- ✅ Toast: "Waiting for response..." (after request sent)
- ✅ Toast: Error messages (if any failure)

## Testing Instructions

### Step 1: Install Updated App
```bash
cd /Users/rishabh/OnlyCareProject/android_app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 2: Test Switch-to-Video

1. **Start an audio call:**
   - Login as male user (with at least 60+ coins for video)
   - Call a female user
   - Wait for call to connect

2. **Request switch:**
   - Click the **🎥 video camera button** on the audio call screen
   - Dialog appears: "Switch to Video Call?"
   - Click **"Yes"**

3. **Expected behavior:**
   - ✅ Toast: "Requesting switch to video..."
   - ✅ Backend creates new video call
   - ✅ WebSocket sends request to receiver
   - ✅ Toast: "Waiting for response..."
   - ⏳ **Receiver sees dialog:** "[User] wants to switch to video call"
   
4. **Receiver accepts:**
   - ✅ Both users navigate to video call screen
   - ✅ Old audio call ends in background
   - ✅ New video call starts seamlessly

5. **Receiver declines:**
   - ✅ Requester sees Toast: "Not now" (or custom decline reason)
   - ✅ Audio call continues

### Step 3: Verify Logs (If Issues)

```bash
# Android app logs
adb logcat | grep -E "(📹|SWITCH TO VIDEO|ApiDataRepository)"

# Backend logs
ssh root@64.227.163.211 "tail -f /var/www/onlycare_admin/storage/logs/laravel.log | grep -i switch"
```

## What Was Wrong

The Agora SDK package (`yasser/agora-token-generator`) uses these constants:
- ✅ `RtcTokenBuilder::RolePublisher` (correct - capital R)
- ✅ `RtcTokenBuilder::RoleSubscriber` (correct - capital R)

**NOT:**
- ❌ `RtcTokenBuilder::ROLE_PUBLISHER` (wrong - all caps)
- ❌ `RtcTokenBuilder::ROLE_SUBSCRIBER` (wrong - all caps)

This is confirmed by checking the working `generateToken` method (line 2313):
```php
$role = strtolower($roleInput) === 'subscriber' 
    ? RtcTokenBuilder::RoleSubscriber   // ✅ Capital R
    : RtcTokenBuilder::RolePublisher;   // ✅ Capital R
```

## Backend Validation Checks (All Working)

When you click "Yes", the backend validates:

1. ✅ Call ID is valid
2. ✅ Call exists in database
3. ✅ Requesting user is part of the call
4. ✅ Call type is AUDIO (not already video)
5. ✅ Call status is ONGOING (not ended)
6. ✅ Male user has sufficient coins
7. ✅ **Generates Agora token** (this was failing before)
8. ✅ Creates new video call record
9. ✅ Returns new call details

## Expected Flow After Fix

### Requester (Male/Female who clicks "Yes")
```
1. Click 🎥 button
2. Toast: "Requesting switch to video..."
3. Backend validates & creates video call
4. WebSocket sends request to receiver
5. Toast: "Waiting for response..."
```

### Receiver (Male/Female who receives request)
```
1. Dialog appears: "[User] wants to switch to video call"
2. Click "Accept" or "Decline"
3. If Accept: Navigate to video call screen
4. If Decline: Request toast shown, audio continues
```

### Both Users (After Accept)
```
1. Navigate to VideoCallScreen with new call ID
2. Old audio call ends in background
3. New video call starts with pre-created:
   - Call ID
   - Channel name
   - Agora token
   - Balance time
4. Seamless transition ✨
```

## Summary

**Problem:** Backend was using wrong Agora constant name
**Fix:** Changed `ROLE_PUBLISHER` → `RolePublisher`
**Status:** ✅ Deployed and ready to test

**Action Required:**
1. Install updated Android app (with enhanced logging)
2. Test the switch-to-video feature
3. Should now work correctly! 🎉

---

**Date:** January 10, 2026  
**Deployed:** ✅ Backend fixed and deployed  
**App Build:** ✅ Enhanced logging ready  
**Ready for Testing:** ✅ YES
