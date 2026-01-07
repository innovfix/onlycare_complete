# 🚀 Incoming Call Setup Guide - Quick Start

## ⚠️ CRITICAL: Required Before Building

### Step 1: Get google-services.json from Firebase

**YOU MUST DO THIS BEFORE THE APP WILL COMPILE!**

The app will **NOT build** without this file.

#### Option A: Get from Your Backend Team
Ask your backend team for the `google-services.json` file from your Firebase project.

#### Option B: Download from Firebase Console Yourself
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select your project (or create a new one)
3. Click the gear icon ⚙️ → Project Settings
4. Scroll down to "Your apps"
5. Click on your Android app (or add a new Android app)
   - Package name: `com.onlycare.app`
6. Click "Download google-services.json"
7. Place it here: `/Users/bala/Desktop/App Projects/onlycare_app/app/google-services.json`

**File Location:**
```
onlycare_app/
├── app/
│   ├── google-services.json  ⬅️ PUT IT HERE
│   ├── build.gradle.kts
│   └── src/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📱 Step 2: Build and Test

Once you have the `google-services.json` file:

```bash
# Clean build
./gradlew clean

# Build the app
./gradlew assembleDebug

# Install on device
./gradlew installDebug
```

---

## 🧪 Step 3: Test the Implementation

### Test 1: Basic Permission Check
1. Launch the app
2. You should see a permission request for notifications (Android 13+)
3. Grant the permission

### Test 2: Check FCM Token
1. Open the app
2. Check logcat:
   ```bash
   adb logcat | grep FCM
   ```
3. You should see: "FCM Token retrieved: ..."
4. Copy this token - you'll need it for testing

### Test 3: Test with Firebase Console (Manual Test)
1. Go to Firebase Console → Cloud Messaging
2. Click "Send test message"
3. Add your FCM token
4. Click "Test"
5. You should see a notification

### Test 4: Test Incoming Call Flow
**Prerequisites:** Backend must implement FCM notification sending (see FULL_SCREEN_INCOMING_CALL_IMPLEMENTATION.md)

1. **App in foreground:**
   - Have someone call you
   - Full-screen incoming call should appear
   - Accept/Reject should work

2. **App in background:**
   - Press Home button
   - Have someone call you
   - Full-screen incoming call should appear over other apps

3. **App killed (MOST IMPORTANT):**
   - Force kill the app (swipe away from recents)
   - Have someone call you
   - Full-screen incoming call should STILL appear!
   - This is the key feature!

4. **Screen locked:**
   - Lock your phone (screen off)
   - Have someone call you
   - Screen should turn on
   - Full-screen incoming call should appear over lock screen

---

## 🔍 Debugging

### View All Logs
```bash
adb logcat | grep -E "CallNotification|IncomingCall|FCM|OnlyCareApplication"
```

### Check if Services are Running
```bash
adb shell dumpsys activity services | grep onlycare
```

### Check Permissions
```bash
adb shell dumpsys package com.onlycare.app | grep permission
```

---

## 🎯 What's Different Now

### Before (Old Dialog)
- ❌ Small dialog box
- ❌ Only works when app is open
- ❌ No ringtone
- ❌ No screen wake
- ❌ Easy to miss

### After (Full-Screen)
- ✅ Full-screen native phone call UI
- ✅ Works even when app is killed
- ✅ Plays phone ringtone
- ✅ Vibrates
- ✅ Wakes screen
- ✅ Shows over lock screen
- ✅ Beautiful UI with caller info

---

## 📝 Backend Team TODO

Share the file `FULL_SCREEN_INCOMING_CALL_IMPLEMENTATION.md` with your backend team.

They need to:
1. ✅ Provide google-services.json
2. ⏳ Store user FCM tokens in database
3. ⏳ Implement FCM notification sending
4. ⏳ Send notifications when calls are initiated

---

## ⚠️ Common Issues

### Issue: "google-services.json not found"
**Solution:** Download and place the file as shown in Step 1.

### Issue: "No FCM token in logs"
**Solution:** 
1. Check if google-services.json is valid
2. Rebuild the app completely
3. Check Firebase Console for errors

### Issue: "Notification not showing"
**Solution:**
1. Check if notification permission is granted
2. Check battery optimization settings
3. Check if backend is sending notifications correctly

### Issue: "Full-screen not working"
**Solution:**
1. Check Android version (works best on Android 10+)
2. Grant overlay permission if requested
3. Some manufacturers restrict this feature

---

## 📞 Testing Checklist

Before considering it complete, test:

- [ ] App receives notification when killed
- [ ] Screen turns on when locked
- [ ] Full-screen UI appears over lock screen
- [ ] Ringtone plays
- [ ] Phone vibrates
- [ ] Accept button works and starts call
- [ ] Reject button dismisses and notifies caller
- [ ] No crashes during any scenario

---

## 🎉 You're Done!

Once everything is working:
1. The old dialog-based incoming call is REPLACED
2. Users get a professional native phone call experience
3. Calls won't be missed even when app is closed
4. Much better user experience overall

**Next:** Get google-services.json and start testing! 🚀



