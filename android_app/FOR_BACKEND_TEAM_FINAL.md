# 🔧 Backend Fix Required - FCM Notification Format

**Date:** November 23, 2025  
**Priority:** URGENT  
**Estimated Fix Time:** 5-10 minutes  
**Impact:** Incoming call ringing screen not appearing

---

## ✅ CONFIRMED: This is 100% Safe to Fix

### Mobile App Analysis
✅ **The mobile app ONLY logs the notification field** (line 98-100 in `CallNotificationService.kt`)  
✅ **It does NOT use it for any functionality**  
✅ **Removing it will NOT break anything**  

Here's what the app does with it:
```kotlin
// This is the ONLY place the notification field is used:
remoteMessage.notification?.let {
    Log.d(TAG, "📬 Notification payload: ${it.title} - ${it.body}")
}
```

It's just a log message! Nothing else uses it.

---

## 🎯 What You Need to Change

### Find Your FCM Sender Code

Look for where you send push notifications for incoming calls. It's probably in a file like:
- `app/Services/FCMService.php` (Laravel)
- `services/fcm.service.js` (Node.js)
- `services/notification_service.py` (Python)

### Current Code (WRONG ❌)

```php
// Example in Laravel/PHP
$fcmPayload = [
    'to' => $receiverFcmToken,
    
    // ❌ DELETE THIS ENTIRE BLOCK!
    'notification' => [
        'title' => 'Incoming Call from ' . $caller->name,
        'body' => 'Audio call',
        'channelId' => 'incoming_calls',
        'sound' => 'default'
    ],
    
    'data' => [
        'type' => 'incoming_call',
        'callId' => $call->id,
        // ... other data
    ]
];
```

### Fixed Code (CORRECT ✅)

```php
// ✅ ONLY DATA FIELD - NO NOTIFICATION FIELD!
$fcmPayload = [
    'to' => $receiverFcmToken,
    
    // ✅ Only keep the data field
    'data' => [
        // REQUIRED FIELDS (all must be strings!)
        'type' => 'incoming_call',
        'callId' => (string) $call->id,
        'callerId' => (string) $caller->id,
        'callerName' => $caller->name,
        'channelId' => $call->channel_name,          // Agora channel
        'agoraToken' => $call->agora_token,
        'agoraAppId' => config('services.agora.app_id'),
        'callType' => strtoupper($call->call_type),  // 'AUDIO' or 'VIDEO'
        
        // OPTIONAL BUT RECOMMENDED
        'callerPhoto' => $caller->profile_photo_url ?? '',
        'balanceTime' => $this->calculateBalanceTime($receiver),  // e.g. "90:00"
        'timestamp' => (string) (now()->timestamp * 1000)  // milliseconds
    ],
    
    // IMPORTANT: High priority for immediate delivery
    'priority' => 'high',
    'content_available' => true
];
```

---

## 📋 Complete Example (Laravel)

```php
<?php

namespace App\Services;

use App\Models\Call;
use Illuminate\Support\Facades\Http;

class FCMService
{
    public function sendIncomingCallNotification(Call $call, $receiverFcmToken)
    {
        $caller = $call->caller;
        
        $payload = [
            'to' => $receiverFcmToken,
            // NO notification field!
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
                'balanceTime' => '90:00',  // Calculate based on user coins
                'timestamp' => (string) (now()->timestamp * 1000)
            ],
            'priority' => 'high',
            'content_available' => true
        ];
        
        $response = Http::withHeaders([
            'Authorization' => 'key=' . config('services.fcm.server_key'),
            'Content-Type' => 'application/json'
        ])->post('https://fcm.googleapis.com/fcm/send', $payload);
        
        return $response->successful();
    }
}
```

---

## 📋 Complete Example (Node.js with Firebase Admin SDK)

```javascript
const admin = require('firebase-admin');

async function sendIncomingCallNotification(call, receiverFcmToken) {
  const message = {
    token: receiverFcmToken,
    // NO notification field!
    data: {
      type: 'incoming_call',
      callId: call.id.toString(),
      callerId: call.caller_id.toString(),
      callerName: call.caller.name,
      callerPhoto: call.caller.profile_photo_url || '',
      channelId: call.channel_name,
      agoraToken: call.agora_token,
      agoraAppId: process.env.AGORA_APP_ID,
      callType: call.call_type.toUpperCase(),
      balanceTime: '90:00',
      timestamp: Date.now().toString()
    },
    android: {
      priority: 'high',
      ttl: 20000  // 20 seconds
    }
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('FCM sent:', response);
    return true;
  } catch (error) {
    console.error('FCM failed:', error);
    return false;
  }
}
```

---

## ⚠️ Important Notes

### 1. All Data Values MUST Be Strings
```php
// ❌ WRONG
'callId' => $call->id,  // This might be an integer

// ✅ CORRECT
'callId' => (string) $call->id,  // Convert to string
```

### 2. Remove `notification` Field Completely
```php
// ❌ WRONG - Even empty notification field causes issues
'notification' => [],

// ✅ CORRECT - No notification field at all
// (just don't include it)
```

### 3. Set High Priority
```php
'priority' => 'high',  // ✅ This is required!
```

---

## 🧪 How to Test

### 1. Make the Change
Remove `notification` field from your FCM sender

### 2. Send a Test Call
- User A calls User B
- User B's app should be KILLED (swipe from recent apps)

### 3. Expected Result
✅ Full-screen ringing UI appears on User B's phone  
✅ Ringtone plays  
✅ Accept/Reject buttons work  

### 4. Check Server Logs
Your FCM request should look like:
```json
{
  "to": "fcm_token_here...",
  "data": {
    "type": "incoming_call",
    "callId": "123",
    ...
  },
  "priority": "high"
}
```

**No `notification` field should be present!**

---

## ❓ Why This Works

### Problem
When FCM payload includes `notification` field:
- Android shows notification automatically in status bar
- BUT doesn't wake up the app's message handler
- App never receives the call data
- Ringing screen never appears

### Solution
When FCM payload has ONLY `data` field:
- Android wakes up the app's message handler
- App receives all call data
- App can show custom ringing screen
- Full control over notification and UI

---

## ✅ Final Checklist

Before deploying:
- [ ] Removed `notification` field completely
- [ ] Kept only `data` field
- [ ] All data values are strings
- [ ] Set `priority: "high"`
- [ ] Included all required fields (callId, callerId, callerName, channelId, agoraToken, agoraAppId, callType)
- [ ] Tested with app killed
- [ ] Confirmed ringing screen appears

---

## 📞 Questions?

If you have any questions or issues, contact the mobile team. This is a straightforward change that should work immediately.

**Deployment:** This can be deployed to production as soon as testing is complete. It's a safe change with no side effects.

