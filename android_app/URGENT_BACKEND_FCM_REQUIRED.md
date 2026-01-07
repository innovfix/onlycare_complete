# 🚨 URGENT: FCM Integration Required - Incoming Calls Not Working When App Closed

## ⚠️ Current Problem

**Incoming calls ONLY work when app is OPEN** ❌

When app is closed/background → User misses calls → Bad user experience!

---

## ✅ Solution: Send FCM Push Notifications

Mobile app is **100% ready**. We just need backend to send FCM notifications.

---

## 🎯 What Backend Team Must Do

### Step 1: Install Firebase Admin SDK (5 minutes)

**Node.js:**
```bash
npm install firebase-admin
```

**Python:**
```bash
pip install firebase-admin
```

---

### Step 2: Initialize Firebase (5 minutes)

**Get Service Account Key:**
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Project Settings → Service Accounts
3. Click "Generate new private key"
4. Download `serviceAccountKey.json`
5. Put it on your server (keep it secret!)

**Initialize in your backend:**

**Node.js:**
```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});
```

**Python:**
```python
import firebase_admin
from firebase_admin import credentials

cred = credentials.Certificate('path/to/serviceAccountKey.json')
firebase_admin.initialize_app(cred)
```

---

### Step 3: Send FCM Notification When Call Initiated (10 minutes)

**When User A calls User B, send this:**

**Node.js:**
```javascript
const admin = require('firebase-admin');

async function sendIncomingCallNotification(receiverUser, callerUser, callData) {
  const message = {
    token: receiverUser.fcmToken,  // Get from your database
    data: {
      type: 'incoming_call',
      callerId: callerUser.id,
      callerName: callerUser.name,
      callerPhoto: callerUser.profileImage || '',
      channelId: callData.channelName,
      agoraToken: callData.agoraToken || ''
    },
    android: {
      priority: 'high'
    }
  };
  
  try {
    const response = await admin.messaging().send(message);
    console.log('✅ FCM notification sent:', response);
    return true;
  } catch (error) {
    console.error('❌ FCM error:', error);
    return false;
  }
}

// Use it in your call initiation endpoint
router.post('/calls/initiate', async (req, res) => {
  const { callerId, receiverId } = req.body;
  
  // 1. Get users
  const caller = await User.findById(callerId);
  const receiver = await User.findById(receiverId);
  
  // 2. Create call record
  const call = await Call.create({
    callerId,
    receiverId,
    channelName: `call_${Date.now()}`,
    agoraToken: generateToken(...),
    status: 'CONNECTING'
  });
  
  // 3. 🔥 SEND FCM NOTIFICATION (NEW!)
  await sendIncomingCallNotification(receiver, caller, call);
  
  // 4. Return to caller
  res.json({ success: true, call });
});
```

**Python:**
```python
from firebase_admin import messaging

def send_incoming_call_notification(receiver_user, caller_user, call_data):
    message = messaging.Message(
        data={
            'type': 'incoming_call',
            'callerId': str(caller_user['id']),
            'callerName': caller_user['name'],
            'callerPhoto': caller_user.get('profile_image', ''),
            'channelId': call_data['channel_name'],
            'agoraToken': call_data.get('agora_token', '')
        },
        token=receiver_user['fcm_token'],  # Get from database
        android=messaging.AndroidConfig(
            priority='high'
        )
    )
    
    try:
        response = messaging.send(message)
        print(f'✅ FCM notification sent: {response}')
        return True
    except Exception as error:
        print(f'❌ FCM error: {error}')
        return False
```

---

## 🎯 Exact FCM Payload Format (CRITICAL!)

**You MUST send this exact format:**

```json
{
  "token": "receiver_fcm_token_from_database",
  "data": {
    "type": "incoming_call",
    "callerId": "USR_123",
    "callerName": "John Doe",
    "callerPhoto": "https://example.com/photo.jpg",
    "channelId": "call_CALL_12345",
    "agoraToken": "agora_token_here_or_empty_string"
  },
  "android": {
    "priority": "high"
  }
}
```

**⚠️ IMPORTANT:**
- ✅ Use `data` payload (NOT `notification`)
- ✅ Set `priority: "high"`
- ✅ All fields required except `callerPhoto` (can be empty string)
- ✅ `agoraToken` can be empty string if generating on client

---

## 📊 Where to Get FCM Token

**The mobile app already sends FCM token to your backend:**

**Endpoint:** `POST /api/v1/users/update-fcm-token`

**Request:**
```json
{
  "fcm_token": "eifKhal2QvyKtCCkyofk4w:APA91bG..."
}
```

**Your database should have:**
```javascript
User {
  id: "USR_123",
  name: "John Doe",
  phone: "1234567890",
  fcmToken: "eifKhal2QvyKtCCkyofk4w:APA91bG...",  // ⬅️ Store this!
  ...
}
```

---

## 🧪 How to Test

### Test 1: Check if FCM Token is Being Saved

**Check your database:**
```sql
SELECT id, name, fcm_token FROM users WHERE id = 'USR_123';
```

**Expected:**
```
USR_123 | John Doe | eifKhal2QvyKtCCkyofk4w:APA91bG...
```

If `fcm_token` is NULL → Mobile app is sending but backend not saving!

---

### Test 2: Send Test FCM Notification

**Node.js Test:**
```javascript
// Quick test script
const admin = require('firebase-admin');

admin.initializeApp({
  credential: admin.credential.cert(require('./serviceAccountKey.json'))
});

const testToken = 'eifKhal2QvyKtCCkyofk4w:APA91bG...'; // Get from database

admin.messaging().send({
  token: testToken,
  data: {
    type: 'incoming_call',
    callerId: 'TEST_123',
    callerName: 'Test Caller',
    callerPhoto: '',
    channelId: 'test_channel',
    agoraToken: ''
  },
  android: { priority: 'high' }
}).then(response => {
  console.log('✅ Success:', response);
}).catch(error => {
  console.error('❌ Error:', error);
});
```

**Expected result on mobile:**
- Full-screen incoming call appears
- Ringtone plays
- Phone vibrates
- **Even if app is completely closed!** 🎉

---

## ⏱️ Time Estimate

| Task | Time |
|------|------|
| Install Firebase Admin SDK | 5 min |
| Initialize Firebase | 5 min |
| Add FCM sending to call endpoint | 10 min |
| Test | 10 min |
| **TOTAL** | **30 minutes** |

---

## 🎯 Expected Result After Implementation

### Before (Current):
```
User A calls User B
    ↓
User B's app OPEN → ✅ Call received
User B's app CLOSED → ❌ Call NOT received (MISSED CALL!)
```

### After (With FCM):
```
User A calls User B
    ↓
Backend sends FCM notification
    ↓
User B's app OPEN → ✅ Call received
User B's app CLOSED → ✅ Call STILL received! 🎉
User B's app KILLED → ✅ Call STILL received! 🎉
Screen LOCKED → ✅ Call STILL received! 🎉
```

---

## 🚨 Priority: HIGH

**Why this is urgent:**
- Users are missing calls when app is closed
- Bad user experience
- May lose customers
- **Mobile app is 100% ready, just waiting for this!**

**Implementation time:** 30 minutes ⏱️

---

## 📞 Questions?

Check the complete documentation:
- `BACKEND_REQUIREMENTS_FOR_INCOMING_CALL.md` (full guide)
- `FCM_INCOMING_CALL_DIAGNOSTIC_REPORT.md` (technical details)

---

## ✅ Checklist for Backend Team

- [ ] Install Firebase Admin SDK
- [ ] Download serviceAccountKey.json from Firebase Console
- [ ] Initialize Firebase in backend
- [ ] Verify `fcm_token` is being saved in user database
- [ ] Add FCM notification sending to call initiation endpoint
- [ ] Test: Send test FCM notification
- [ ] Test: Make actual call with app closed
- [ ] Verify: User receives full-screen incoming call
- [ ] Deploy to production

---

**Once this is done, incoming calls will work perfectly even when the app is closed!** 🎉

**Status:** ⏳ WAITING FOR BACKEND IMPLEMENTATION

**Mobile Status:** ✅ 100% COMPLETE AND READY



