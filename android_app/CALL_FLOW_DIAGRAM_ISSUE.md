# 📊 Call Flow Diagram: Current vs Fixed

## 🔴 CURRENT FLOW (BROKEN - App Killed Scenario)

```
┌─────────────────────────────────────────────────────────────────┐
│  BACKEND: Incoming Call                                          │
│  ├─ Creates call record in database                              │
│  └─ Sends FCM push notification                                  │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  DEVICE: Firebase Cloud Messaging (FCM)                          │
│  ├─ ✅ Receives push notification (even when app killed)         │
│  └─ ✅ Wakes up CallNotificationService                          │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  CallNotificationService.onMessageReceived()                     │
│  ├─ ✅ Extracts call data (callId, callerId, callerName, etc)    │
│  └─ ✅ Calls startIncomingCallService()                          │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  IncomingCallService.onStartCommand()                            │
│  ├─ ✅ Creates foreground service                                │
│  ├─ ✅ Builds notification (with full-screen intent)             │
│  ├─ ✅ Shows notification                                        │
│  ├─ ✅ Starts CallRingtoneManager                                │
│  │   ├─ ❌ Plays ringtone WITHOUT audio focus                    │
│  │   └─ ❌ Ringtone is ducked/ignored by Android                 │
│  └─ ❌ Calls launchFullScreenActivity()                          │
│      └─ ❌ startActivity() FAILS! (Android 10+ restriction)      │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  NOTIFICATION SYSTEM                                              │
│  ├─ ✅ Notification visible in status bar                        │
│  ├─ ✅ Has full-screen intent attached                           │
│  └─ ❌ Full-screen intent is IGNORED                             │
│      ├─ Reason 1: User didn't grant permission (Android 12+)     │
│      ├─ Reason 2: Service already tried to launch (conflict)     │
│      └─ Reason 3: Battery optimization blocking                  │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  RESULT (USER SEES)                                               │
│  ├─ ✅ Notification appears (silent)                             │
│  ├─ ❌ NO full-screen ringing activity                           │
│  ├─ ❌ NO ringtone sound                                         │
│  └─ ❌ User misses the call                                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🟢 FIXED FLOW (WORKING - App Killed Scenario)

```
┌─────────────────────────────────────────────────────────────────┐
│  BACKEND: Incoming Call                                          │
│  ├─ Creates call record in database                              │
│  └─ Sends FCM push notification                                  │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  DEVICE: Firebase Cloud Messaging (FCM)                          │
│  ├─ ✅ Receives push notification (even when app killed)         │
│  └─ ✅ Wakes up CallNotificationService                          │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  CallNotificationService.onMessageReceived()                     │
│  ├─ ✅ Extracts call data (callId, callerId, callerName, etc)    │
│  └─ ✅ Calls startIncomingCallService()                          │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  IncomingCallService.onStartCommand()                            │
│  ├─ ✅ Creates foreground service                                │
│  ├─ ✅ Builds notification (with full-screen intent)             │
│  ├─ ✅ Shows notification                                        │
│  ├─ ✅ Starts CallRingtoneManager                                │
│  │   ├─ ✅ Requests audio focus FIRST                            │
│  │   ├─ ✅ Checks ringer mode (silent/vibrate/normal)            │
│  │   └─ ✅ Plays ringtone with proper audio attributes           │
│  └─ ✅ REMOVED launchFullScreenActivity() call                   │
│      └─ Let notification system handle it automatically          │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  NOTIFICATION SYSTEM (Automatic)                                 │
│  ├─ ✅ Notification visible                                      │
│  ├─ ✅ Detects screen is off/locked                              │
│  ├─ ✅ Checks full-screen intent permission                      │
│  │   └─ ✅ Permission granted (we prompted user on app launch)   │
│  ├─ ✅ Turns screen on                                           │
│  └─ ✅ Launches IncomingCallActivity via full-screen intent      │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  IncomingCallActivity                                             │
│  ├─ ✅ Shows full-screen UI over lock screen                     │
│  ├─ ✅ Displays caller name, photo                               │
│  ├─ ✅ Shows Accept/Reject buttons                               │
│  └─ ✅ Ringtone playing in background (from service)             │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  RESULT (USER SEES)                                               │
│  ├─ ✅ Screen turns ON automatically                             │
│  ├─ ✅ Full-screen ringing activity visible                      │
│  ├─ ✅ Ringtone playing with vibration                           │
│  └─ ✅ User can answer the call                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔍 KEY DIFFERENCES

| Component | ❌ Current (Broken) | ✅ Fixed (Working) |
|-----------|-------------------|-------------------|
| **CallRingtoneManager** | Plays without audio focus | ✅ Requests audio focus first |
| **IncomingCallService** | Calls `startActivity()` | ✅ Removed manual launch |
| **Activity Launch** | Service tries (fails) | ✅ Notification system handles |
| **Permission Check** | Only Android 14+ | ✅ Android 12+ |
| **Permission Request** | Never asks user | ✅ Prompts on app launch |
| **Battery Optimization** | Not handled | ✅ Requests exemption |
| **Result** | Silent notification only | ✅ Full-screen + sound |

---

## 📱 PERMISSION STATES

### Android 10-11 (No Permission Required)
```
Full-Screen Intent: Auto-granted ✅
Battery Optimization: May block ⚠️
Result: Works if battery not optimized
```

### Android 12-13 (Permission Required)
```
Full-Screen Intent: Must check ⚠️
Permission Status: No direct API ⚠️
Workaround: Check notifications enabled
Result: Works if notifications enabled
```

### Android 14-15 (Stricter Permission)
```
Full-Screen Intent: Explicit permission required ⚠️
Permission API: canUseFullScreenIntent() ✅
Settings Path: Apps > Only Care > Notifications
Result: Works only if permission granted
```

---

## 🛠️ CODE CHANGES SUMMARY

### 1. CallNotificationManager.kt
```kotlin
// BEFORE (❌ Wrong)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    // Only checks Android 14+
}

// AFTER (✅ Correct)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    // Checks Android 12+
}
```

### 2. IncomingCallService.kt
```kotlin
// BEFORE (❌ Wrong)
ringtoneManager?.startRinging()
launchFullScreenActivity(...)  // ❌ This fails!

// AFTER (✅ Correct)
ringtoneManager?.startRinging()
// Activity launched by notification system automatically
```

### 3. CallRingtoneManager.kt
```kotlin
// BEFORE (❌ Wrong)
ringtone?.play()  // No audio focus

// AFTER (✅ Correct)
requestAudioFocus()  // ✅ Request first
ringtone?.play()     // Then play
```

### 4. MainActivity.kt
```kotlin
// BEFORE (❌ Missing)
// Nothing

// AFTER (✅ Added)
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Request full-screen intent permission
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.canUseFullScreenIntent()) {
            // Show dialog to open settings
        }
    }
}
```

---

## 🚦 TESTING SCENARIOS

### Scenario 1: App in Foreground
```
Before Fix: ✅ Works (app handles it)
After Fix:  ✅ Works (no change)
```

### Scenario 2: App in Background
```
Before Fix: ⚠️ Partial (notification only)
After Fix:  ✅ Works (full-screen + sound)
```

### Scenario 3: App Killed (Swiped from Recents)
```
Before Fix: ❌ Broken (notification only, silent)
After Fix:  ✅ Works (full-screen + sound)
```

### Scenario 4: Screen Off/Locked
```
Before Fix: ❌ Broken (notification only, no screen wake)
After Fix:  ✅ Works (screen turns on, full-screen + sound)
```

### Scenario 5: Battery Saver ON
```
Before Fix: ❌ May not work (service killed)
After Fix:  ✅ Works (exempted from optimization)
```

---

## 🔧 IMPLEMENTATION CHECKLIST

- [ ] **Step 1:** Update `canUseFullScreenIntent()` to check Android 12+
- [ ] **Step 2:** Remove `launchFullScreenActivity()` call from service
- [ ] **Step 3:** Add `requestAudioFocus()` in CallRingtoneManager
- [ ] **Step 4:** Add `audioManager` initialization in CallRingtoneManager
- [ ] **Step 5:** Add `abandonAudioFocus()` in stopRinging()
- [ ] **Step 6:** Add permission request dialog in MainActivity
- [ ] **Step 7:** Test on Android 12 device
- [ ] **Step 8:** Test on Android 14 device
- [ ] **Step 9:** Test with battery saver ON
- [ ] **Step 10:** Test with screen off

---

## 📊 EXPECTED RESULTS AFTER FIX

| Test Case | Android 10-11 | Android 12-13 | Android 14-15 |
|-----------|---------------|---------------|---------------|
| App killed | ✅ Works | ✅ Works* | ✅ Works* |
| Screen off | ✅ Works | ✅ Works* | ✅ Works* |
| Battery saver | ✅ Works | ✅ Works | ✅ Works |
| DND mode | ⚠️ Silent** | ⚠️ Silent** | ⚠️ Silent** |

*Requires user to grant permission (we prompt them)
**By design - respects user's DND settings

---

## 🎯 PRIORITY

**CRITICAL** - Affects 90%+ of users on modern Android (10+)

**Implementation Time:** 30-45 minutes  
**Testing Time:** 15-20 minutes  
**Total:** ~1 hour

---

**Status:** 🔴 **AWAITING IMPLEMENTATION**  
**Document Version:** 1.0  
**Date:** November 23, 2025



