# 🚨 CRITICAL: Ringing Screen Not Appearing - FCM Payload Issue

**Status:** 🔴 CRITICAL BUG  
**Issue:** Incoming call notifications appear in status bar but full-screen ringing activity doesn't launch  
**Root Cause:** Backend is sending FCM **notification payload** instead of **data payload**

---

## 🐛 Problem Analysis

### What's Happening

From your Android logs:
```
2025-11-23 21:58:16.623  3986-3986  ExpandableNotifRow  com.android.systemui
0|com.onlycare.app|0|FCM-Notification:16223553|10451
Notification(channel=incoming_calls ...)
```

✅ **FCM notification IS being delivered**  
✅ **Android is showing notification in status bar**  
❌ **`CallNotificationService.onMessageReceived()` is NOT being called**  
❌ **`IncomingCallService` is NOT starting**  
❌ **`IncomingCallActivity` is NOT launching**  
❌ **No ringing screen appears**

---

## 🔍 Root Cause

### The Two Types of FCM Messages

Firebase Cloud Messaging supports two types of payloads:

#### 1. Notification Payload (What Backend is Sending ❌)
```json
{
  "to": "fcm_token_here",
  "notification": {
    "title": "Incoming Call from John",
    "body": "Audio call",
    "channelId": "incoming_calls"
  },
  "data": {
    "type": "incoming_call",
    "callId": "123"
  }
}
```

**Behavior:**
- ✅ Shows notification in status bar automatically
- ❌ **DOES NOT** call `onMessageReceived()` when app is killed/background
- ❌ Cannot show custom UI (ringing screen)
- ❌ Cannot play custom ringtone
- ❌ Cannot launch full-screen activity

#### 2. Data-Only Payload (What We Need ✅)
```json
{
  "to": "fcm_token_here",
  "data": {
    "type": "incoming_call",
    "callId": "123",
    "callerId": "user_456",
    "callerName": "John Doe",
    "callerPhoto": "https://...",
    "channelId": "call_123",
    "agoraToken": "006abc...",
    "agoraAppId": "your_app_id",
    "callType": "AUDIO",
    "balanceTime": "90:00",
    "timestamp": "1732405096000"
  }
}
```

**Behavior:**
- ✅ **ALWAYS** calls `onMessageReceived()` (even when app is killed!)
- ✅ App controls notification display
- ✅ Can show custom UI (full-screen ringing activity)
- ✅ Can play custom ringtone
- ✅ Can launch full-screen activity

---

## 🎯 The Fix: Backend Must Send Data-Only Payload

### Required Backend Changes

**Location:** Your backend FCM notification sender (Laravel/Node.js/Python)

### ⚠️ Remove the `notification` field completely!

```php
// ❌ WRONG (Current Backend Code)
$fcmPayload = [
    'to' => $receiverFcmToken,
    'notification' => [  // ❌ Remove this entire block!
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

```php
// ✅ CORRECT (Fixed Backend Code)
$fcmPayload = [
    'to' => $receiverFcmToken,
    // ✅ NO notification field at all!
    'data' => [
        'type' => 'incoming_call',
        'callId' => (string) $call->id,
        'callerId' => (string) $caller->id,
        'callerName' => $caller->name,
        'callerPhoto' => $caller->profile_photo_url ?? '',
        'channelId' => $call->channel_name,  // Agora channel name
        'agoraToken' => $call->agora_token,
        'agoraAppId' => config('services.agora.app_id'),
        'callType' => strtoupper($call->call_type),  // 'AUDIO' or 'VIDEO'
        'balanceTime' => $this->calculateBalanceTime($receiver), // e.g. "90:00"
        'timestamp' => (string) now()->timestamp * 1000  // Current time in milliseconds
    ],
    'priority' => 'high',  // ✅ Important for immediate delivery
    'content_available' => true  // ✅ Wakes up app even when killed
];
```

---

## 📝 Complete Backend Example (Laravel)

```php
<?php

namespace App\Services;

use App\Models\Call;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class FCMService
{
    /**
     * Send incoming call notification via FCM
     * This will trigger IncomingCallActivity full-screen UI
     */
    public function sendIncomingCallNotification(Call $call, $receiverFcmToken)
    {
        $caller = $call->caller;
        
        // Build data-only payload (NO notification field!)
        $payload = [
            'to' => $receiverFcmToken,
            'data' => [
                // Required fields
                'type' => 'incoming_call',
                'callId' => (string) $call->id,
                'callerId' => (string) $caller->id,
                'callerName' => $caller->name,
                'callType' => strtoupper($call->call_type), // 'AUDIO' or 'VIDEO'
                'channelId' => $call->channel_name,
                'agoraToken' => $call->agora_token,
                'agoraAppId' => config('services.agora.app_id'),
                
                // Optional fields
                'callerPhoto' => $caller->profile_photo_url ?? '',
                'balanceTime' => $this->calculateBalanceTime($call->receiver),
                'timestamp' => (string) (now()->timestamp * 1000)
            ],
            
            // High priority for immediate delivery
            'priority' => 'high',
            'content_available' => true,
            
            // Android-specific config
            'android' => [
                'priority' => 'high',
                'ttl' => '20s'  // Expire after 20 seconds
            ]
        ];
        
        Log::info('Sending FCM call notification', [
            'call_id' => $call->id,
            'receiver' => $call->receiver_id,
            'token' => substr($receiverFcmToken, 0, 20) . '...'
        ]);
        
        // Send to FCM
        $response = Http::withHeaders([
            'Authorization' => 'key=' . config('services.fcm.server_key'),
            'Content-Type' => 'application/json'
        ])->post('https://fcm.googleapis.com/fcm/send', $payload);
        
        if ($response->successful()) {
            Log::info('FCM notification sent successfully', ['call_id' => $call->id]);
            return true;
        } else {
            Log::error('FCM notification failed', [
                'call_id' => $call->id,
                'status' => $response->status(),
                'body' => $response->body()
            ]);
            return false;
        }
    }
    
    /**
     * Calculate balance time based on user's coins
     */
    private function calculateBalanceTime($receiver)
    {
        $coins = $receiver->coins ?? 0;
        $coinsPerMinute = config('app.coins_per_minute', 10);
        $minutes = floor($coins / $coinsPerMinute);
        return sprintf('%d:%02d', $minutes, 0); // e.g. "90:00"
    }
    
    /**
     * Send call cancelled notification
     */
    public function sendCallCancelledNotification($callId, $receiverFcmToken, $callerId)
    {
        $payload = [
            'to' => $receiverFcmToken,
            'data' => [
                'type' => 'call_cancelled',
                'callId' => (string) $callId,
                'callerId' => (string) $callerId
            ],
            'priority' => 'high'
        ];
        
        Http::withHeaders([
            'Authorization' => 'key=' . config('services.fcm.server_key'),
            'Content-Type' => 'application/json'
        ])->post('https://fcm.googleapis.com/fcm/send', $payload);
    }
}
```

---

## 📝 Complete Backend Example (Node.js)

```javascript
const admin = require('firebase-admin');

/**
 * Send incoming call notification via FCM
 * Data-only payload to trigger IncomingCallActivity
 */
async function sendIncomingCallNotification(call, receiverFcmToken) {
  const message = {
    token: receiverFcmToken,
    // ✅ NO notification field!
    data: {
      type: 'incoming_call',
      callId: call.id.toString(),
      callerId: call.caller_id.toString(),
      callerName: call.caller.name,
      callerPhoto: call.caller.profile_photo_url || '',
      channelId: call.channel_name,
      agoraToken: call.agora_token,
      agoraAppId: process.env.AGORA_APP_ID,
      callType: call.call_type.toUpperCase(), // 'AUDIO' or 'VIDEO'
      balanceTime: calculateBalanceTime(call.receiver),
      timestamp: Date.now().toString()
    },
    android: {
      priority: 'high',
      ttl: 20000 // 20 seconds
    }
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('FCM notification sent:', response);
    return true;
  } catch (error) {
    console.error('FCM notification failed:', error);
    return false;
  }
}

function calculateBalanceTime(receiver) {
  const coins = receiver.coins || 0;
  const coinsPerMinute = 10;
  const minutes = Math.floor(coins / coinsPerMinute);
  return `${minutes}:00`;
}
```

---

## ✅ How to Verify the Fix

### 1. Check Backend Logs
After making the change, send a test call and check your backend logs. You should see:
```
Sending FCM call notification: {
  "to": "fcm_token...",
  "data": {
    "type": "incoming_call",
    "callId": "123",
    ...
  },
  "priority": "high"
}
```

**No `notification` field should be present!**

### 2. Check Android Logs (Logcat)
After the fix, when a call comes in, you should see:
```
CallNotificationService: 📨 FCM MESSAGE RECEIVED!
CallNotificationService: ✅ Data payload found:
CallNotificationService:   - type: incoming_call
CallNotificationService:   - callId: 123
CallNotificationService:   - callerName: John Doe
CallNotificationService: ✅ Required fields present. Starting IncomingCallService...
IncomingCallService: Service created
IncomingCallService: Incoming call from: John Doe (ID: user_456)
IncomingCallService: Full-screen activity launched with all call data
IncomingCallActivity: IncomingCallActivity created for caller: John Doe
```

### 3. Visual Test
- Kill the app completely (swipe from recent apps)
- Send a test call from another device
- **Expected Result:** Full-screen ringing UI should appear immediately with ringtone playing

---

## 🎯 Key Points

1. ✅ **Remove `notification` field completely** from FCM payload
2. ✅ **Only use `data` field** with all call details
3. ✅ **Set `priority: "high"`** for immediate delivery
4. ✅ **Include all required fields:** callId, callerId, callerName, channelId, agoraToken, agoraAppId, callType
5. ✅ **All data values must be strings** (convert numbers/booleans to strings)

---

## 📚 References

- [FCM Message Types (Official Docs)](https://firebase.google.com/docs/cloud-messaging/concept-options#notifications_and_data_messages)
- [Data-Only Messages](https://firebase.google.com/docs/cloud-messaging/android/receive#handling_messages)
- `app/src/main/java/com/onlycare/app/services/CallNotificationService.kt` (Mobile app FCM handler)

---

## ⚡ Quick Summary

**Problem:** Backend sends FCM notification payload → Android shows notification in status bar → App's `onMessageReceived()` not called → Ringing screen doesn't appear

**Solution:** Backend sends data-only FCM payload (NO notification field) → Android wakes app → `onMessageReceived()` called → `IncomingCallService` starts → Ringing screen appears

**Action Required:** Update backend FCM sender to remove `notification` field and only send `data` payload.

