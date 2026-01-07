# 🚨 URGENT: Ringing Screen Not Showing - Quick Fix Guide

**Date:** November 23, 2025  
**Issue:** FCM notifications arrive but full-screen ringing activity doesn't launch  
**Fix Time:** 5-10 minutes  
**Location:** Backend FCM sender

---

## 🎯 The Problem

Your Android logs show:
```
Notification(channel=incoming_calls ...) ✅ FCM notification delivered
```

But no ringing screen appears because:

1. ❌ Backend sends **notification payload** (`notification` field in FCM)
2. ✅ Android shows notification in status bar automatically
3. ❌ `CallNotificationService.onMessageReceived()` is **NOT called**
4. ❌ Ringing screen never launches

---

## ✅ The Solution (Backend Only)

### Remove the `notification` field from your FCM payload!

### Before (WRONG ❌)
```php
$fcmPayload = [
    'to' => $receiverFcmToken,
    'notification' => [  // ❌ DELETE THIS ENTIRE BLOCK!
        'title' => 'Incoming Call from ' . $caller->name,
        'body' => 'Audio call',
        'channelId' => 'incoming_calls'
    ],
    'data' => [
        'type' => 'incoming_call',
        'callId' => $call->id,
        // ...
    ]
];
```

### After (CORRECT ✅)
```php
$fcmPayload = [
    'to' => $receiverFcmToken,
    // ✅ NO notification field!
    'data' => [
        'type' => 'incoming_call',
        'callId' => (string) $call->id,
        'callerId' => (string) $caller->id,
        'callerName' => $caller->name,
        'callerPhoto' => $caller->profile_photo_url ?? '',
        'channelId' => $call->channel_name,
        'agoraToken' => $call->agora_token,
        'agoraAppId' => config('services.agora.app_id'),
        'callType' => strtoupper($call->call_type),
        'balanceTime' => $this->calculateBalanceTime($receiver),
        'timestamp' => (string) (now()->timestamp * 1000)
    ],
    'priority' => 'high',
    'content_available' => true
];
```

---

## 📝 Field Requirements

### Required Fields (All must be strings)
```php
'type' => 'incoming_call',              // Identifies this as a call
'callId' => (string) $call->id,         // Call database ID
'callerId' => (string) $caller->id,     // Caller user ID
'callerName' => $caller->name,          // Caller display name
'channelId' => $call->channel_name,     // Agora channel name
'agoraToken' => $call->agora_token,     // Agora RTC token
'agoraAppId' => 'your_agora_app_id',    // Agora app ID
'callType' => 'AUDIO',                  // 'AUDIO' or 'VIDEO'
```

### Optional but Recommended
```php
'callerPhoto' => $caller->profile_photo_url ?? '',
'balanceTime' => '90:00',               // Format: "minutes:seconds"
'timestamp' => (string) (time() * 1000) // Current timestamp in ms
```

---

## 🧪 How to Test

### 1. Make the backend change
Remove `notification` field, keep only `data` field

### 2. Kill the app completely
Swipe OnlyCare app from recent apps

### 3. Send a test call
Use another device to initiate a call

### 4. Expected Result
✅ Full-screen ringing UI appears immediately  
✅ Ringtone plays  
✅ Accept/Reject buttons work  

### 5. Check Logs (if still not working)
```bash
adb logcat -s CallNotificationService IncomingCallService IncomingCallActivity
```

You should see:
```
CallNotificationService: 📨 FCM MESSAGE RECEIVED!
CallNotificationService: ✅ Data payload found
CallNotificationService: Starting IncomingCallService...
IncomingCallService: Incoming call from: John Doe
IncomingCallActivity: IncomingCallActivity created
```

---

## ⚠️ Common Mistakes to Avoid

1. **Don't include `notification` field** - It bypasses the app's handler
2. **All data values must be strings** - Convert numbers to strings
3. **Set `priority: "high"`** - Ensures immediate delivery
4. **Include all required fields** - Missing fields will cause rejection

---

## 📚 For More Details

See `RINGING_SCREEN_NOT_SHOWING_FIX.md` for:
- Complete Laravel example
- Complete Node.js example
- Detailed explanation of FCM message types
- Troubleshooting guide

---

## ✅ Quick Checklist

- [ ] Remove `notification` field from FCM payload
- [ ] Keep only `data` field with all required fields
- [ ] Set `priority: "high"`
- [ ] Convert all data values to strings
- [ ] Test with app killed
- [ ] Verify ringing screen appears

---

**Need Help?** Check the full documentation in `RINGING_SCREEN_NOT_SHOWING_FIX.md`
