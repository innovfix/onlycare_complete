# 🔥 Backend Team Requirements - Incoming Call Feature

## ⚠️ CRITICAL: 3 Things Backend Team MUST Do

---

## 1️⃣ URGENT: Provide google-services.json File

**Status:** ⏳ **BLOCKING - APP WON'T BUILD WITHOUT THIS!**

### What is it?
Firebase configuration file needed for push notifications.

### Where to get it:
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project (or create one if none exists)
3. Go to: ⚙️ Project Settings → General
4. Scroll to "Your apps" section
5. Click on Android app (or add new Android app)
   - Package name: `com.onlycare.app`
6. Click "Download google-services.json"

### Where to send it:
**Send this file to mobile team:** `google-services.json`

**They will place it here:**
```
onlycare_app/
├── app/
│   ├── google-services.json  ⬅️ HERE
│   └── build.gradle.kts
```

---

## 2️⃣ Store User FCM Tokens in Database

**Status:** ⏳ **REQUIRED**

### What to do:
Add a new field to your User model/table to store Firebase Cloud Messaging tokens.

### Database Change:
```sql
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(255);
```

**OR in your User model:**
```javascript
// MongoDB/Mongoose
{
  userId: String,
  name: String,
  phone: String,
  fcmToken: String,  // ⬅️ ADD THIS FIELD
  ...
}
```

### API Endpoint Needed:
Create an endpoint to receive and update FCM tokens from mobile app:

```
POST /api/users/update-fcm-token
```

**Request Body:**
```json
{
  "userId": "123",
  "fcmToken": "dXJ5dmVyc2lvbjphcHA6MTo6MzI4OTY..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "FCM token updated successfully"
}
```

**Example Implementation (Node.js/Express):**
```javascript
router.post('/update-fcm-token', async (req, res) => {
  const { userId, fcmToken } = req.body;
  
  try {
    await User.findByIdAndUpdate(userId, { 
      fcmToken: fcmToken 
    });
    
    res.json({ 
      success: true, 
      message: 'FCM token updated successfully' 
    });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: error.message 
    });
  }
});
```

---

## 3️⃣ Send FCM Push Notifications When Call is Initiated

**Status:** ⏳ **REQUIRED**

### What to do:
When User A calls User B, send a push notification to User B's device using Firebase Cloud Messaging.

### Step A: Install Firebase Admin SDK

**Node.js:**
```bash
npm install firebase-admin
```

**Python:**
```bash
pip install firebase-admin
```

### Step B: Initialize Firebase Admin SDK

**Node.js:**
```javascript
const admin = require('firebase-admin');

// Initialize with service account
const serviceAccount = require('./path/to/serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});
```

**Python:**
```python
import firebase_admin
from firebase_admin import credentials, messaging

cred = credentials.Certificate('path/to/serviceAccountKey.json')
firebase_admin.initialize_app(cred)
```

**Where to get serviceAccountKey.json:**
1. Firebase Console → Project Settings → Service Accounts
2. Click "Generate new private key"
3. Download and keep it secure on your server

### Step C: Send Notification When Call is Initiated

**When User A calls User B:**

**Node.js Implementation:**
```javascript
async function sendIncomingCallNotification(receiverUser, callerUser, callData) {
  // Get receiver's FCM token from database
  const receiverFCMToken = receiverUser.fcmToken;
  
  if (!receiverFCMToken) {
    console.error('Receiver has no FCM token');
    return false;
  }
  
  const message = {
    token: receiverFCMToken,
    data: {
      type: 'incoming_call',
      callerId: callerUser.id.toString(),
      callerName: callerUser.name,
      callerPhoto: callerUser.profilePhoto || '',
      channelId: callData.channelId,
      agoraToken: callData.agoraToken
    },
    android: {
      priority: 'high'
    }
  };
  
  try {
    const response = await admin.messaging().send(message);
    console.log('✅ Notification sent successfully:', response);
    return true;
  } catch (error) {
    console.error('❌ Error sending notification:', error);
    return false;
  }
}

// Usage in your call initiation endpoint:
router.post('/calls/initiate', async (req, res) => {
  const { callerId, receiverId, callType } = req.body;
  
  // 1. Get users from database
  const caller = await User.findById(callerId);
  const receiver = await User.findById(receiverId);
  
  // 2. Generate Agora token and channel
  const channelId = `call_${Date.now()}`;
  const agoraToken = generateAgoraToken(channelId, receiverId);
  
  // 3. Create call record
  const call = await Call.create({
    callerId,
    receiverId,
    channelId,
    agoraToken,
    callType,
    status: 'ringing'
  });
  
  // 4. Send FCM notification to receiver
  await sendIncomingCallNotification(receiver, caller, {
    channelId,
    agoraToken
  });
  
  // 5. Return to caller
  res.json({
    success: true,
    call: call
  });
});
```

**Python Implementation:**
```python
from firebase_admin import messaging

def send_incoming_call_notification(receiver_user, caller_user, call_data):
    """Send incoming call notification via FCM"""
    
    receiver_fcm_token = receiver_user.get('fcm_token')
    
    if not receiver_fcm_token:
        print('Receiver has no FCM token')
        return False
    
    message = messaging.Message(
        data={
            'type': 'incoming_call',
            'callerId': str(caller_user['id']),
            'callerName': caller_user['name'],
            'callerPhoto': caller_user.get('profile_photo', ''),
            'channelId': call_data['channel_id'],
            'agoraToken': call_data['agora_token']
        },
        token=receiver_fcm_token,
        android=messaging.AndroidConfig(
            priority='high'
        )
    )
    
    try:
        response = messaging.send(message)
        print(f'✅ Notification sent successfully: {response}')
        return True
    except Exception as error:
        print(f'❌ Error sending notification: {error}')
        return False
```

---

## 📋 EXACT FCM Payload Format

**IMPORTANT:** Your FCM notification MUST use this EXACT format:

```json
{
  "token": "receiver_fcm_token_here",
  "data": {
    "type": "incoming_call",
    "callerId": "123",
    "callerName": "Hima Poojary",
    "callerPhoto": "https://example.com/photo.jpg",
    "channelId": "channel_12345",
    "agoraToken": "agora_token_here"
  },
  "android": {
    "priority": "high"
  }
}
```

### Field Descriptions:
- `type` (required): Must be `"incoming_call"`
- `callerId` (required): ID of the person calling
- `callerName` (required): Display name shown to receiver
- `callerPhoto` (optional): URL to caller's profile picture
- `channelId` (required): Agora channel ID for the call
- `agoraToken` (required): Agora RTC token for the call

---

## 🔄 Optional: Call Cancellation

If caller cancels before receiver answers, send this notification:

```json
{
  "token": "receiver_fcm_token_here",
  "data": {
    "type": "call_cancelled",
    "callerId": "123"
  },
  "android": {
    "priority": "high"
  }
}
```

This will stop the ringing and dismiss the incoming call screen.

---

## 🧪 How to Test

### Step 1: Test Firebase Setup
1. Send test notification from Firebase Console
2. Go to: Cloud Messaging → Send test message
3. Add a test FCM token
4. Click "Test"
5. Device should receive notification

### Step 2: Test Your Implementation
1. Mobile app generates FCM token (check logs)
2. Mobile app sends token to your API
3. Check database - token should be saved
4. Trigger a call from User A to User B
5. Your backend should send FCM notification
6. User B's device should show full-screen incoming call

---

## 📊 Complete Flow

```
User A initiates call
         ↓
Mobile app → POST /api/calls/initiate
         ↓
Your Backend:
  1. Get User B from database
  2. Get User B's fcmToken
  3. Generate Agora token and channel
  4. Send FCM notification to User B
  5. Return call data to User A
         ↓
User B's device receives FCM
         ↓
Full-screen incoming call appears!
```

---

## ✅ Checklist for Backend Team

- [ ] Provide `google-services.json` to mobile team
- [ ] Add `fcmToken` field to User model/database
- [ ] Create API endpoint: `POST /api/users/update-fcm-token`
- [ ] Install Firebase Admin SDK
- [ ] Get `serviceAccountKey.json` from Firebase Console
- [ ] Initialize Firebase Admin SDK in your backend
- [ ] Modify call initiation endpoint to send FCM notifications
- [ ] Test FCM notification sending
- [ ] Implement call cancellation notification (optional)
- [ ] Test end-to-end with mobile team

---

## 🆘 Need Help?

### Firebase Console Access:
- URL: https://console.firebase.google.com
- Need: Google account with admin access to your Firebase project

### Common Issues:

**Issue:** "Don't have Firebase project"
**Solution:** Create one at Firebase Console, it's free

**Issue:** "Can't find serviceAccountKey.json"
**Solution:** Firebase Console → Project Settings → Service Accounts → Generate new private key

**Issue:** "FCM notification not received"
**Solutions:**
1. Check if token is valid and saved correctly
2. Check if notification format is correct
3. Check Firebase Console for errors
4. Verify Android priority is set to "high"

---

## 📞 Questions?

If you have questions, check:
- `FULL_SCREEN_INCOMING_CALL_IMPLEMENTATION.md` (detailed guide)
- Firebase Admin SDK docs: https://firebase.google.com/docs/admin/setup

---

## 🎯 Timeline

**Critical Path:**
1. ⏰ **Today:** Provide google-services.json (URGENT - blocks mobile testing)
2. ⏰ **This week:** Add fcmToken field and API endpoint
3. ⏰ **This week:** Implement FCM notification sending
4. ✅ **Next week:** Test and deploy

---

**Once these 3 things are done, incoming calls will work perfectly! 🚀**



