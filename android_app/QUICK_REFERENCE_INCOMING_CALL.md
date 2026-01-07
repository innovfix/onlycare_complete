# 📱 Quick Reference - Full-Screen Incoming Call

## 🚨 BEFORE YOU BUILD

### CRITICAL: You need this file!
```
app/google-services.json
```

**Where to get it:**
- Firebase Console → Project Settings → Download
- OR ask your backend team

**⚠️ App won't build without it!**

---

## 📂 What Was Created

### New Files (7 code files)
```
app/src/main/java/com/onlycare/app/
├── services/
│   ├── CallNotificationService.kt       (FCM receiver)
│   ├── IncomingCallService.kt          (Foreground service)
│   └── CallRingtoneManager.kt          (Ringtone handler)
├── presentation/screens/call/
│   └── IncomingCallActivity.kt         (Full-screen UI)
└── utils/
    ├── CallNotificationManager.kt       (Notification helper)
    ├── FCMTokenManager.kt              (Token management)
    └── IncomingCallPermissions.kt      (Permissions)
```

### Modified Files (5 files)
```
✏️ app/build.gradle.kts          (Firebase dependencies)
✏️ build.gradle.kts              (Firebase plugin)
✏️ AndroidManifest.xml           (Permissions + services)
✏️ OnlyCareApplication.kt        (Initialize FCM)
✏️ FemaleHomeScreen.kt           (Request permissions)
```

---

## 🔧 Quick Build & Test

### 1. Get google-services.json
```bash
# Place it here:
app/google-services.json
```

### 2. Build
```bash
./gradlew clean assembleDebug
```

### 3. Install
```bash
./gradlew installDebug
```

### 4. Check FCM Token
```bash
adb logcat | grep FCM
# Should see: "FCM Token retrieved: ..."
```

---

## 🧪 Quick Test

### Test Full-Screen Incoming Call

**Method 1: Manual FCM Test (No backend needed)**
1. Get FCM token from logs
2. Go to Firebase Console → Cloud Messaging
3. Send test notification with this data:
```json
{
  "type": "incoming_call",
  "callerId": "123",
  "callerName": "Test Caller",
  "channelId": "test_channel",
  "agoraToken": "test_token"
}
```

**Method 2: With Backend Integration**
1. Make sure backend sends FCM notifications
2. Have someone call you
3. Should see full-screen incoming call

---

## 🎯 Test Checklist

Quick tests to verify it works:

- [ ] Notification permission granted
- [ ] FCM token generated (check logs)
- [ ] App in foreground → call appears
- [ ] App in background → call appears
- [ ] **App killed → call still appears** ⭐
- [ ] Screen locked → call wakes screen
- [ ] Ringtone plays
- [ ] Vibration works
- [ ] Accept button works
- [ ] Reject button works

---

## 🐛 Quick Debug

### Check FCM Token
```bash
adb logcat | grep -E "FCM|CallNotification"
```

### Check Services Running
```bash
adb shell dumpsys activity services | grep onlycare
```

### Check Permissions
```bash
adb shell dumpsys package com.onlycare.app | grep permission
```

---

## 🔗 Documentation

**Start here:**
1. `INCOMING_CALL_SETUP_GUIDE.md` - Quick setup instructions

**Complete reference:**
2. `FULL_SCREEN_INCOMING_CALL_IMPLEMENTATION.md` - Everything you need to know

**Overview:**
3. `INCOMING_CALL_IMPLEMENTATION_SUMMARY.md` - What was done

---

## 💬 Backend Team Needs

Share with backend team:
- [ ] `google-services.json` file
- [ ] `FULL_SCREEN_INCOMING_CALL_IMPLEMENTATION.md`

Backend must implement:
- [ ] Store user FCM tokens
- [ ] Send FCM notifications when calls initiated
- [ ] Handle call cancellation

---

## ✅ What You Get

### Before (Dialog)
- Small dialog box
- Only when app is open
- No ringtone/vibration
- Easy to miss

### After (Full-Screen)
- **Full-screen native phone call UI**
- **Works even when app is killed** ⭐
- **Plays ringtone + vibrates**
- **Wakes screen automatically**
- **Shows over lock screen**
- **Professional appearance**

---

## 🚀 Status

✅ Implementation: **COMPLETE**  
⏳ Testing: **Waiting for google-services.json**  
⏳ Backend: **Integration needed**

---

## 📞 Next Step

**→ Get google-services.json and place it in app/ directory!**

Then run:
```bash
./gradlew clean assembleDebug
```

🎉 **You're ready to test!**



