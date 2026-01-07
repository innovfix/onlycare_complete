# 🚨 ROOT CAUSE: No Ringing Screen When App is Closed/Killed/Mobile Off

## 📊 Issue Summary

**Problem:** When the app is:
- Closed (killed/swiped from recents)
- Mobile screen is off
- App is in background for long time

**What Happens:**
- ✅ Notification appears (FCM working)
- ❌ NO full-screen ringing activity (IncomingCallActivity not launching)
- ❌ NO ringtone sound (CallRingtoneManager not starting)

**What Should Happen:**
- ✅ Notification appears
- ✅ Full-screen ringing activity launches automatically
- ✅ Ringtone plays with vibration

---

## 🔍 ROOT CAUSE ANALYSIS

### ✅ What's Working (Already Implemented Correctly)

1. **FCM Service** (`CallNotificationService.kt`)
   - ✅ Receives push notifications even when app is killed
   - ✅ Properly extracts call data
   - ✅ Starts IncomingCallService as foreground service

2. **IncomingCallService** (`IncomingCallService.kt`)
   - ✅ Runs as foreground service with notification
   - ✅ Initializes CallRingtoneManager
   - ✅ Attempts to start ringtone
   - ✅ Calls `startActivity()` to launch IncomingCallActivity

3. **IncomingCallActivity** (`IncomingCallActivity.kt`)
   - ✅ Configured with proper flags (showWhenLocked, turnScreenOn)
   - ✅ Has Accept/Reject UI
   - ✅ Handles window flags correctly

4. **CallNotificationManager** (`CallNotificationManager.kt`)
   - ✅ Creates high-importance notification channel
   - ✅ Uses `setFullScreenIntent()` with priority MAX
   - ✅ Builds notification with proper category (CALL)
   - ✅ Sets silent notification (ringtone handled separately)

5. **Permissions in Manifest** (`AndroidManifest.xml`)
   - ✅ `USE_FULL_SCREEN_INTENT` declared
   - ✅ `SYSTEM_ALERT_WINDOW` declared
   - ✅ `WAKE_LOCK` declared
   - ✅ `VIBRATE` declared
   - ✅ IncomingCallActivity has proper attributes

---

## ❌ What's NOT Working (Root Causes)

### **PRIMARY ROOT CAUSE: Android 10+ Full-Screen Intent Restrictions**

Starting from **Android 10 (API 29)** and stricter in **Android 12+ (API 31)**, full-screen intents have severe restrictions:

#### 1. **Android 12+ (API 31+): Full-Screen Intent Permission Required**

**Issue:**
- On Android 12+, apps MUST have `USE_FULL_SCREEN_INTENT` **runtime permission**
- This is NOT auto-granted by manifest declaration
- User must manually grant it in Settings

**Current State:**
```kotlin
// CallNotificationManager.kt - Line 62-70
fun canUseFullScreenIntent(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.canUseFullScreenIntent()  // ✅ Checks permission on Android 14+
    } else {
        // Android 13 and below - permission is auto-granted from manifest
        true  // ❌ WRONG! Android 12-13 also need runtime check!
    }
}
```

**The Bug:** Android 12 (API 31) and Android 13 (API 33) are NOT being checked! They return `true` but permission might not be granted.

**Fix Required:**
```kotlin
fun canUseFullScreenIntent(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+ (API 31+)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Android 14+ has direct check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            notificationManager.canUseFullScreenIntent()
        } else {
            // Android 12-13: Check if notification policy access is granted
            // This is a workaround - user needs to enable in Settings manually
            notificationManager.areNotificationsEnabled()
        }
    } else {
        // Android 11 and below - auto-granted
        true
    }
}
```

---

#### 2. **Android 14+ (API 34+): Even Stricter Requirements**

**Issue:**
- Android 14 introduced `NotificationManager.canUseFullScreenIntent()` API
- User must explicitly grant "Display over other apps" or "Full screen intent" permission
- This is found in: **Settings > Apps > Only Care > Notifications > Allow full screen notifications**

**Current State:**
- ✅ Code checks for Android 14+ (UPSIDE_DOWN_CAKE)
- ✅ Logs warning if permission not granted
- ❌ But doesn't request permission from user!

---

#### 3. **Battery Optimization Killing the Service**

**Issue:**
- Modern Android (8.0+) aggressively kills background services to save battery
- Even foreground services can be killed if battery optimization is enabled for the app
- Manufacturer-specific battery savers (Xiaomi, Oppo, Vivo, OnePlus, Samsung) are even more aggressive

**Current State:**
```kotlin
// IncomingCallService.kt - Line 94-107
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    startForeground(
        CallNotificationManager.INCOMING_CALL_NOTIFICATION_ID,
        notification,
        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE  // ✅ Correct type
    )
}
```

**Problem:**
- ✅ Service is started as foreground (correct)
- ❌ But if battery optimization is ON, Android can still kill it
- ❌ App doesn't check or request battery optimization exemption

---

#### 4. **Service Trying to Launch Activity (Android 10+ Restrictions)**

**Issue:**
- On Android 10+ (API 29+), background services **CANNOT** start activities directly
- Exception: Activities launched from notifications (via full-screen intent) are allowed
- But if notification is suppressed (DND mode, battery saver), activity won't launch

**Current State:**
```kotlin
// IncomingCallService.kt - Line 132-153
private fun launchFullScreenActivity(...) {
    val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        // ... extras
    }
    
    try {
        startActivity(fullScreenIntent)  // ❌ FAILS on Android 10+ when app is killed!
        Log.d(TAG, "Full-screen activity launched")
    } catch (e: Exception) {
        Log.e(TAG, "Error launching full-screen activity", e)  // ❌ This is likely throwing!
    }
}
```

**The Problem:**
- Service tries to call `startActivity()` directly
- This is BLOCKED by Android 10+ background activity launch restrictions
- The activity NEVER launches because of this exception

**Why the notification appears:**
- Notification is shown by foreground service (always works)
- But full-screen intent in notification is IGNORED because permission not granted
- Result: User sees notification but NO full-screen activity

---

#### 5. **Ringtone Not Playing (Separate Issue)**

**Issue:**
- Ringtone is started in `IncomingCallService.onCreate()`
- But if service is killed quickly or audio focus is not acquired, ringtone stops

**Current State:**
```kotlin
// IncomingCallService.kt - Line 110-112
// Start ringing
ringtoneManager?.startRinging()  // ✅ Calls this

// CallRingtoneManager.kt - Line 52-97
fun startRinging() {
    // Get system default ringtone
    val ringtoneUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
    
    // Set audio attributes
    ringtone?.audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)  // ✅ Correct usage
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    
    ringtone?.isLooping = true  // ✅ Correct
    ringtone?.play()  // ❌ Might fail if audio focus not acquired!
}
```

**Problems:**
1. **No audio focus request** - On modern Android, you MUST request audio focus before playing sound
2. **No check for DND mode** - If phone is in Do Not Disturb mode, ringtone is suppressed
3. **No check for ringer mode** - If phone is in Silent/Vibrate mode, ringtone won't play

---

### 6. **Manufacturer-Specific Battery Optimization**

**Issue:**
- Xiaomi (MIUI), Oppo (ColorOS), Vivo (FuntouchOS), OnePlus (OxygenOS), Samsung (OneUI) have aggressive battery optimization
- These kill background services even when marked as foreground
- They require special permissions or user to whitelist the app

**Current State:**
- ❌ No detection of manufacturer
- ❌ No request to whitelist app from battery optimization
- ❌ No guidance to user on how to disable battery optimization

---

## 📋 COMPLETE FIX PLAN (DO NOT EXECUTE - PLAN ONLY)

### **Phase 1: Fix Full-Screen Intent Permission (CRITICAL)**

#### 1.1 Update `canUseFullScreenIntent()` Function
**File:** `app/src/main/java/com/onlycare/app/utils/CallNotificationManager.kt`
**Lines:** 62-70

**Fix:**
```kotlin
fun canUseFullScreenIntent(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+ (API 31+)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            notificationManager.canUseFullScreenIntent()
        } else {
            // Android 12-13: Fallback check (not perfect but better than nothing)
            // The permission exists but there's no direct API to check it
            // We check if notifications are enabled as a proxy
            notificationManager.areNotificationsEnabled()
        }
    } else {
        // Android 11 and below - auto-granted from manifest
        true
    }
}
```

#### 1.2 Request Full-Screen Intent Permission on App Launch
**New File:** `app/src/main/java/com/onlycare/app/utils/FullScreenIntentHelper.kt`

**Create:**
```kotlin
object FullScreenIntentHelper {
    
    fun requestFullScreenIntentPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (!notificationManager.canUseFullScreenIntent()) {
                // Open settings to let user enable it
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                
                AlertDialog.Builder(activity)
                    .setTitle("Enable Full Screen Notifications")
                    .setMessage("To show incoming call screen when app is closed, please enable 'Full screen notifications' in settings.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        activity.startActivity(intent)
                    }
                    .setNegativeButton("Later", null)
                    .show()
            }
        }
    }
}
```

#### 1.3 Check Permission in MainActivity
**File:** `app/src/main/java/com/onlycare/app/presentation/MainActivity.kt`

**Add in `onCreate()`:**
```kotlin
// Check full-screen intent permission
FullScreenIntentHelper.requestFullScreenIntentPermission(this)
```

---

### **Phase 2: Fix Activity Launch from Service (CRITICAL)**

#### 2.1 Remove Direct `startActivity()` Call from Service
**File:** `app/src/main/java/com/onlycare/app/services/IncomingCallService.kt`
**Lines:** 114-153

**Problem:** The service currently calls `startActivity()` directly, which FAILS on Android 10+.

**Fix:** DON'T launch activity from service. Instead, rely ONLY on notification's full-screen intent.

**Current Code (WRONG):**
```kotlin
// Start ringing
ringtoneManager?.startRinging()

// Launch full-screen activity  ❌ THIS FAILS!
launchFullScreenActivity(...)
```

**Fixed Code:**
```kotlin
// Start ringing
ringtoneManager?.startRinging()

// DON'T launch activity manually
// Let the notification's fullScreenIntent handle it automatically
// This works even on Android 10+ because it's triggered by notification system, not by our service
```

**Explanation:**
- The notification we create already has `setFullScreenIntent(fullScreenPendingIntent, true)`
- Android notification system will launch the activity automatically when:
  1. Notification is posted
  2. Screen is off or locked
  3. App has full-screen intent permission
- Our manual `startActivity()` call is REDUNDANT and BLOCKED by Android 10+

---

### **Phase 3: Fix Ringtone with Audio Focus (HIGH PRIORITY)**

#### 3.1 Request Audio Focus Before Playing Ringtone
**File:** `app/src/main/java/com/onlycare/app/services/CallRingtoneManager.kt`
**Lines:** 52-97

**Problem:** No audio focus request, so ringtone might not play or might be ducked.

**Fix:**
```kotlin
private var audioManager: AudioManager? = null
private var audioFocusRequest: AudioFocusRequest? = null

init {
    setupVibrator()
    setupAudioManager()  // NEW
}

private fun setupAudioManager() {
    audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
}

fun startRinging() {
    if (isRinging) {
        Log.d(TAG, "Already ringing, ignoring start request")
        return
    }
    
    try {
        Log.d(TAG, "Starting ringtone and vibration")
        
        // REQUEST AUDIO FOCUS (NEW)
        requestAudioFocus()
        
        // Check ringer mode
        val ringerMode = audioManager?.ringerMode
        if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            Log.w(TAG, "Phone is in silent mode, skipping ringtone but vibrating")
            startVibration()
            isRinging = true
            return
        }
        
        // Get system default ringtone URI
        val ringtoneUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        
        if (ringtoneUri != null) {
            ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
            
            // Set audio attributes for ringtone
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                ringtone?.streamType = AudioManager.STREAM_RING
            }
            
            // Start playing ringtone in loop
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()
            
            Log.d(TAG, "Ringtone started playing")
        } else {
            Log.w(TAG, "Could not get default ringtone URI")
        }
        
        // Start vibration
        startVibration()
        
        isRinging = true
        
    } catch (e: Exception) {
        Log.e(TAG, "Error starting ringtone", e)
    }
}

private fun requestAudioFocus() {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0+
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .setWillPauseWhenDucked(false)
                .setOnAudioFocusChangeListener { focusChange ->
                    Log.d(TAG, "Audio focus changed: $focusChange")
                }
                .build()
            
            val result = audioManager?.requestAudioFocus(audioFocusRequest!!)
            Log.d(TAG, "Audio focus request result: $result")
        } else {
            // Legacy API
            @Suppress("DEPRECATION")
            val result = audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_RING,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            Log.d(TAG, "Audio focus request result (legacy): $result")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error requesting audio focus", e)
    }
}

fun stopRinging() {
    if (!isRinging) {
        Log.d(TAG, "Not ringing, ignoring stop request")
        return
    }
    
    try {
        Log.d(TAG, "Stopping ringtone and vibration")
        
        // Stop ringtone
        ringtone?.stop()
        ringtone = null
        
        // Stop vibration
        vibrator?.cancel()
        
        // ABANDON AUDIO FOCUS (NEW)
        abandonAudioFocus()
        
        isRinging = false
        
        Log.d(TAG, "Ringtone and vibration stopped")
        
    } catch (e: Exception) {
        Log.e(TAG, "Error stopping ringtone", e)
    }
}

private fun abandonAudioFocus() {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager?.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
        Log.d(TAG, "Audio focus abandoned")
    } catch (e: Exception) {
        Log.e(TAG, "Error abandoning audio focus", e)
    }
}
```

---

### **Phase 4: Fix Battery Optimization (MEDIUM PRIORITY)**

#### 4.1 Request Battery Optimization Exemption
**File:** `app/src/main/java/com/onlycare/app/utils/BatteryOptimizationHelper.kt`

**Create:**
```kotlin
object BatteryOptimizationHelper {
    
    private const val TAG = "BatteryOptimization"
    
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true  // Not applicable on older versions
        }
    }
    
    fun requestBatteryOptimizationExemption(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isIgnoringBatteryOptimizations(activity)) {
                AlertDialog.Builder(activity)
                    .setTitle("Disable Battery Optimization")
                    .setMessage("For reliable incoming call notifications, please disable battery optimization for this app.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${activity.packageName}")
                        }
                        try {
                            activity.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error opening battery optimization settings", e)
                            // Fallback to general battery settings
                            val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            activity.startActivity(fallbackIntent)
                        }
                    }
                    .setNegativeButton("Later", null)
                    .show()
            }
        }
    }
    
    fun getManufacturerSpecificInstructions(): String? {
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        return when {
            manufacturer.contains("xiaomi") -> {
                "Xiaomi/MIUI:\n" +
                "1. Go to Settings > Apps > Manage apps > Only Care\n" +
                "2. Set 'Autostart' to ON\n" +
                "3. Set 'Battery saver' to 'No restrictions'\n" +
                "4. Lock the app in recent apps (drag down and tap lock icon)"
            }
            manufacturer.contains("oppo") -> {
                "Oppo/ColorOS:\n" +
                "1. Go to Settings > Apps > App Management > Only Care\n" +
                "2. Enable 'Allow auto-start'\n" +
                "3. Set 'Battery optimization' to 'Don't optimize'\n" +
                "4. Set 'Allow background activity'"
            }
            manufacturer.contains("vivo") -> {
                "Vivo/FuntouchOS:\n" +
                "1. Go to Settings > Apps & notifications > App management > Only Care\n" +
                "2. Enable 'High background power consumption'\n" +
                "3. Enable 'Auto-start'\n" +
                "4. Disable 'Background restriction'"
            }
            manufacturer.contains("oneplus") -> {
                "OnePlus/OxygenOS:\n" +
                "1. Go to Settings > Apps > Only Care > Battery\n" +
                "2. Set to 'Don't optimize'\n" +
                "3. Go to Settings > Apps > Only Care > App battery usage\n" +
                "4. Enable 'Allow background activity'"
            }
            manufacturer.contains("samsung") -> {
                "Samsung/OneUI:\n" +
                "1. Go to Settings > Apps > Only Care > Battery\n" +
                "2. Set to 'Unrestricted'\n" +
                "3. Add to 'Never sleeping apps' in Settings > Battery"
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                "Huawei/Honor:\n" +
                "1. Go to Settings > Apps > Apps > Only Care\n" +
                "2. Enable 'AutoLaunch'\n" +
                "3. Set 'Battery' to 'Manage manually'\n" +
                "4. Enable all toggles (Auto-launch, Secondary launch, Run in background)"
            }
            else -> null
        }
    }
    
    fun showManufacturerSpecificGuide(activity: Activity) {
        val instructions = getManufacturerSpecificInstructions()
        
        if (instructions != null) {
            AlertDialog.Builder(activity)
                .setTitle("${Build.MANUFACTURER} Battery Optimization")
                .setMessage("To ensure incoming calls work reliably:\n\n$instructions")
                .setPositiveButton("Got it", null)
                .show()
        }
    }
}
```

#### 4.2 Call in MainActivity
**File:** `app/src/main/java/com/onlycare/app/presentation/MainActivity.kt`

**Add in `onCreate()`:**
```kotlin
// Check battery optimization
BatteryOptimizationHelper.requestBatteryOptimizationExemption(this)

// Show manufacturer-specific guide
BatteryOptimizationHelper.showManufacturerSpecificGuide(this)
```

---

### **Phase 5: Add Permission Check Screen (LOW PRIORITY)**

#### 5.1 Create Permissions Check Screen
**New File:** `app/src/main/java/com/onlycare/app/presentation/screens/settings/IncomingCallPermissionsScreen.kt`

**Create a screen that shows:**
1. ✅/❌ Full-screen intent permission status
2. ✅/❌ Battery optimization status
3. ✅/❌ Notification permission status
4. ✅/❌ Manufacturer-specific optimizations
5. Button to fix each issue

---

## 🎯 PRIORITY ORDER (MUST FIX IN THIS ORDER)

### **CRITICAL (Must fix first):**
1. ✅ Fix `canUseFullScreenIntent()` to check Android 12-13
2. ✅ Request full-screen intent permission from user on app launch
3. ✅ REMOVE `launchFullScreenActivity()` call from service (let notification system handle it)

### **HIGH PRIORITY (Fix second):**
4. ✅ Add audio focus request in CallRingtoneManager
5. ✅ Check ringer mode before playing ringtone
6. ✅ Abandon audio focus when stopping ringtone

### **MEDIUM PRIORITY (Fix third):**
7. ✅ Request battery optimization exemption
8. ✅ Show manufacturer-specific battery optimization guide

### **LOW PRIORITY (Nice to have):**
9. ✅ Create dedicated permissions check screen
10. ✅ Add in-app troubleshooting guide

---

## 📱 USER INSTRUCTIONS (TEMPORARY FIX UNTIL CODE IS FIXED)

### For Android 12+ Users:

#### 1. Enable Full-Screen Notifications
1. Open **Settings** on your phone
2. Go to **Apps** > **Only Care**
3. Tap **Notifications**
4. Enable **Allow full screen notifications** or **Display over other apps**

#### 2. Disable Battery Optimization
1. Open **Settings** on your phone
2. Go to **Apps** > **Only Care** > **Battery**
3. Set to **Unrestricted** or **Don't optimize**

#### 3. Manufacturer-Specific (if above doesn't work)

**Xiaomi/MIUI:**
1. Settings > Apps > Manage apps > Only Care
2. Enable **Autostart**
3. Set **Battery saver** to **No restrictions**
4. Lock app in recent apps

**Samsung:**
1. Settings > Apps > Only Care > Battery
2. Set to **Unrestricted**
3. Settings > Battery > Never sleeping apps > Add Only Care

**Oppo/ColorOS:**
1. Settings > Apps > App Management > Only Care
2. Enable **Allow auto-start**
3. Set **Battery optimization** to **Don't optimize**

---

## 🧪 TESTING CHECKLIST (After Fixes)

### Test 1: App Closed (Swiped from Recents)
1. Open app and log in
2. Swipe app from recent apps (kill it)
3. Send incoming call from another device
4. **Expected:** Full-screen ringing activity appears with sound

### Test 2: Screen Off
1. Open app and log in
2. Turn screen off (lock phone)
3. Send incoming call from another device
4. **Expected:** Screen turns on, full-screen ringing activity appears with sound

### Test 3: Battery Saver Mode
1. Enable battery saver mode
2. Repeat Test 1 and Test 2
3. **Expected:** Still works (because battery optimization exempted)

### Test 4: Do Not Disturb Mode
1. Enable Do Not Disturb
2. Repeat Test 1 and Test 2
3. **Expected:** Notification appears (may be silent depending on DND settings)

### Test 5: Android 14+ Device
1. Repeat all tests on Android 14+ device
2. **Expected:** All tests pass if full-screen intent permission granted

---

## 📝 SUMMARY

**The main issues are:**

1. ❌ **Full-screen intent permission not checked/requested on Android 12-13** (API 31-33)
2. ❌ **Service trying to launch activity directly** (blocked by Android 10+ background restrictions)
3. ❌ **No audio focus request before playing ringtone**
4. ❌ **No battery optimization exemption request**
5. ❌ **No manufacturer-specific battery optimization handling**

**Once these are fixed:**
- ✅ Full-screen ringing activity will appear even when app is killed
- ✅ Ringtone will play reliably
- ✅ Works on all Android versions (7.0 to 15)
- ✅ Works on all manufacturers (with user setup)

---

## 🔗 REFERENCES

- [Android Background Activity Launch Restrictions](https://developer.android.com/guide/components/activities/background-starts)
- [Android Full-Screen Intent](https://developer.android.com/training/notify-user/time-sensitive#full-screen)
- [Android Audio Focus](https://developer.android.com/guide/topics/media-apps/audio-focus)
- [Battery Optimization Best Practices](https://developer.android.com/training/monitoring-device-state/doze-standby)
- [Don't Kill My App](https://dontkillmyapp.com/) - Manufacturer-specific guides

---

**Document Version:** 1.0  
**Date:** November 23, 2025  
**Status:** ROOT CAUSE IDENTIFIED - AWAITING FIX IMPLEMENTATION



