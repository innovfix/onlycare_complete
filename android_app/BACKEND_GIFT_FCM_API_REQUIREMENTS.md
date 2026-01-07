# 🎁 Backend API Requirements - Gift FCM Notification

**Date:** December 2024  
**Priority:** HIGH  
**Feature:** Send FCM notification when gift is sent  
**Endpoint:** `POST /api/v1/auth/send_gift_notification`

---

## 📋 Overview

When a male user sends a gift to a female user during a call, the mobile app calls this API to trigger an FCM notification. The backend should send the FCM notification to the female user's device, which will display a gift animation.

---

## 🔌 API Endpoint

### Request

**URL:** `POST /api/v1/auth/send_gift_notification`

**Content-Type:** `application/x-www-form-urlencoded`

**Headers:**
```
Authorization: Bearer {access_token}
Content-Type: application/x-www-form-urlencoded
```

**Form Data Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `sender_id` | String | Yes | User ID of the gift sender (male user) |
| `receiver_id` | String | Yes | User ID of the gift receiver (female user) |
| `gift_id` | Integer | Yes | ID of the gift being sent |
| `gift_icon` | String (URL) | Yes | Full URL of the gift icon image |
| `gift_coins` | Integer | Yes | Cost of the gift in coins |
| `call_type` | String | Yes | Type of call: `"audio"` or `"video"` |

**Example Request:**
```http
POST /api/v1/auth/send_gift_notification HTTP/1.1
Host: onlycare.in
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGc...
Content-Type: application/x-www-form-urlencoded

sender_id=USR_17645953894710&receiver_id=USR_17646557417788&gift_id=1&gift_icon=https://onlycare.in/storage/gifts/fKerEyvc0CwaDNbwW0fDq1RxNQmalgqW21NSQ0ht.png&gift_coins=50&call_type=audio
```

### Response

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Gift notification sent successfully",
  "data": "Notification sent"
}
```

**Error Response (400/500):**
```json
{
  "success": false,
  "message": "Error message here",
  "error": {
    "code": "ERROR_CODE",
    "message": "Detailed error message"
  }
}
```

---

## 📨 FCM Notification Format

After receiving the API call, the backend should send an FCM notification to the **receiver's** device.

### FCM Payload Structure

**⚠️ CRITICAL: Use DATA-ONLY payload (NO notification field!)**

```json
{
  "token": "receiver_fcm_token_from_database",
  "data": {
    "type": "gift_sent",
    "sender_id": "USR_17645953894710",
    "sender_name": "John Doe",
    "receiver_id": "USR_17646557417788",
    "gift_id": "1",
    "gift_icon": "https://onlycare.in/storage/gifts/fKerEyvc0CwaDNbwW0fDq1RxNQmalgqW21NSQ0ht.png",
    "gift_coins": "50",
    "call_type": "audio"
  },
  "android": {
    "priority": "high"
  },
  "priority": "high"
}
```

### Required FCM Data Fields

| Field | Type | Description |
|-------|------|-------------|
| `type` | String | **MUST be:** `"gift_sent"` |
| `sender_id` | String | User ID of sender |
| `sender_name` | String | Name of sender (for display) |
| `receiver_id` | String | User ID of receiver |
| `gift_id` | String | Gift ID (convert integer to string) |
| `gift_icon` | String | Full URL of gift icon image |
| `gift_coins` | String | Gift cost in coins (convert integer to string) |
| `call_type` | String | `"audio"` or `"video"` |

### ⚠️ Important Notes

1. **ALL values MUST be strings** (even numbers)
   ```php
   // ✅ CORRECT
   'gift_id' => (string) $giftId,
   'gift_coins' => (string) $giftCoins,
   
   // ❌ WRONG
   'gift_id' => $giftId,  // Integer will cause issues
   ```

2. **NO notification field** - Use data-only payload
   ```php
   // ✅ CORRECT
   $payload = [
       'token' => $fcmToken,
       'data' => [...],
       'priority' => 'high'
   ];
   
   // ❌ WRONG - Don't include notification field
   $payload = [
       'token' => $fcmToken,
       'notification' => [...],  // ❌ DELETE THIS!
       'data' => [...]
   ];
   ```

3. **High priority** - Set priority to "high" for immediate delivery

---

## 🔄 Implementation Flow

### Step-by-Step Process

1. **Mobile app calls API:**
   ```
   POST /api/v1/auth/send_gift_notification
   ```

2. **Backend receives request:**
   - Validate all parameters
   - Get receiver's FCM token from database
   - Get sender's name from database

3. **Backend sends FCM:**
   - Build FCM payload with all required fields
   - Send to Firebase Cloud Messaging
   - Return success response to mobile app

4. **Female user's device receives FCM:**
   - `CallNotificationService` receives notification
   - Extracts gift data
   - Sends broadcast to calling screen
   - Calling screen shows gift animation

---

## 💻 Code Examples

### PHP/Laravel Example

```php
public function sendGiftNotification(Request $request)
{
    $validated = $request->validate([
        'sender_id' => 'required|string',
        'receiver_id' => 'required|string',
        'gift_id' => 'required|integer',
        'gift_icon' => 'required|url',
        'gift_coins' => 'required|integer',
        'call_type' => 'required|in:audio,video'
    ]);
    
    // Get receiver's FCM token
    $receiver = User::find($validated['receiver_id']);
    if (!$receiver || !$receiver->fcm_token) {
        return response()->json([
            'success' => false,
            'message' => 'Receiver FCM token not found'
        ], 400);
    }
    
    // Get sender's name
    $sender = User::find($validated['sender_id']);
    $senderName = $sender ? $sender->name : 'Someone';
    
    // Build FCM payload
    $fcmPayload = [
        'token' => $receiver->fcm_token,
        'data' => [
            'type' => 'gift_sent',
            'sender_id' => (string) $validated['sender_id'],
            'sender_name' => $senderName,
            'receiver_id' => (string) $validated['receiver_id'],
            'gift_id' => (string) $validated['gift_id'],
            'gift_icon' => $validated['gift_icon'],
            'gift_coins' => (string) $validated['gift_coins'],
            'call_type' => $validated['call_type']
        ],
        'android' => [
            'priority' => 'high'
        ],
        'priority' => 'high'
    ];
    
    // Send FCM notification
    try {
        $response = Http::withHeaders([
            'Authorization' => 'key=' . config('services.fcm.server_key'),
            'Content-Type' => 'application/json'
        ])->post('https://fcm.googleapis.com/fcm/send', $fcmPayload);
        
        if ($response->successful()) {
            return response()->json([
                'success' => true,
                'message' => 'Gift notification sent successfully',
                'data' => 'Notification sent'
            ]);
        } else {
            return response()->json([
                'success' => false,
                'message' => 'Failed to send FCM notification'
            ], 500);
        }
    } catch (\Exception $e) {
        return response()->json([
            'success' => false,
            'message' => 'Error sending notification: ' . $e->getMessage()
        ], 500);
    }
}
```

### Node.js Example

```javascript
const admin = require('firebase-admin');

async function sendGiftNotification(req, res) {
    const { sender_id, receiver_id, gift_id, gift_icon, gift_coins, call_type } = req.body;
    
    // Get receiver's FCM token
    const receiver = await User.findById(receiver_id);
    if (!receiver || !receiver.fcm_token) {
        return res.status(400).json({
            success: false,
            message: 'Receiver FCM token not found'
        });
    }
    
    // Get sender's name
    const sender = await User.findById(sender_id);
    const senderName = sender ? sender.name : 'Someone';
    
    // Build FCM message
    const message = {
        token: receiver.fcm_token,
        data: {
            type: 'gift_sent',
            sender_id: String(sender_id),
            sender_name: senderName,
            receiver_id: String(receiver_id),
            gift_id: String(gift_id),
            gift_icon: gift_icon,
            gift_coins: String(gift_coins),
            call_type: call_type
        },
        android: {
            priority: 'high'
        },
        priority: 'high'
    };
    
    try {
        const response = await admin.messaging().send(message);
        return res.json({
            success: true,
            message: 'Gift notification sent successfully',
            data: 'Notification sent'
        });
    } catch (error) {
        return res.status(500).json({
            success: false,
            message: 'Failed to send notification: ' + error.message
        });
    }
}
```

---

## 🧪 Testing

### Test Request

```bash
curl -X POST https://onlycare.in/api/v1/auth/send_gift_notification \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "sender_id=USR_123&receiver_id=USR_456&gift_id=1&gift_icon=https://onlycare.in/storage/gifts/gift1.png&gift_coins=50&call_type=audio"
```

### Expected Behavior

1. ✅ API returns success response
2. ✅ FCM notification sent to receiver's device
3. ✅ Female user's calling screen shows gift animation
4. ✅ Toast message appears: "🎁 Gift received from [Sender Name]!"

---

## 📊 Database Requirements

### User Table

Ensure the `users` table has:
- `fcm_token` column (VARCHAR/TEXT) - Stores FCM token
- `name` column - For sender name in notification

### Example Query

```sql
SELECT fcm_token, name FROM users WHERE id = 'receiver_id';
```

---

## ⚠️ Error Handling

### Common Errors

1. **Receiver FCM token not found:**
   ```json
   {
     "success": false,
     "message": "Receiver FCM token not found"
   }
   ```

2. **Invalid parameters:**
   ```json
   {
     "success": false,
     "message": "Validation failed",
     "error": {
       "gift_id": ["The gift id must be an integer."]
     }
   }
   ```

3. **FCM send failure:**
   ```json
   {
     "success": false,
     "message": "Failed to send FCM notification"
   }
   ```

---

## ✅ Checklist

- [ ] API endpoint created: `POST /api/v1/auth/send_gift_notification`
- [ ] All parameters validated
- [ ] Receiver FCM token fetched from database
- [ ] Sender name fetched from database
- [ ] FCM payload built with all required fields
- [ ] All values converted to strings
- [ ] NO notification field in FCM payload
- [ ] Priority set to "high"
- [ ] Error handling implemented
- [ ] Success response returned

---

## 📝 Summary

**What the backend needs to do:**

1. ✅ Create API endpoint: `POST /api/v1/auth/send_gift_notification`
2. ✅ Accept form data: `sender_id`, `receiver_id`, `gift_id`, `gift_icon`, `gift_coins`, `call_type`
3. ✅ Get receiver's FCM token from database
4. ✅ Get sender's name from database
5. ✅ Send FCM notification with `type: "gift_sent"` and all gift data
6. ✅ Return success/error response

**FCM Payload Requirements:**
- ✅ Use **data-only** payload (NO notification field)
- ✅ All values must be **strings**
- ✅ Set priority to **"high"**
- ✅ Include all required fields

---

**Questions?** Contact the mobile development team for clarification.





