# 🔍 Questions to Ask About HIMA Project (Working Implementation)

## 📋 PURPOSE
Compare the **WORKING "hima" project** with the **BROKEN "onlycare" project** to identify differences in incoming call implementation.

---

## ✅ GENERAL INFORMATION

### Q1: Project Overview
- [ ] What Android versions does hima support? (minSdk, targetSdk, compileSdk)
- [ ] What Agora SDK version does hima use?
- [ ] What Firebase/FCM version does hima use?
- [ ] What devices have you tested hima on? (brands, Android versions)

**Get from:** `hima/app/build.gradle.kts`
```kotlin
minSdk = ?
targetSdk = ?
compileSdk = ?
```

---

## 📱 MANIFEST CONFIGURATION

### Q2: Permissions in Manifest
**Get from:** `hima/app/src/main/AndroidManifest.xml`

Does hima have these permissions?
- [ ] `USE_FULL_SCREEN_INTENT` - YES/NO?
- [ ] `SYSTEM_ALERT_WINDOW` - YES/NO?
- [ ] `WAKE_LOCK` - YES/NO?
- [ ] `FOREGROUND_SERVICE` - YES/NO?
- [ ] `FOREGROUND_SERVICE_MICROPHONE` - YES/NO?
- [ ] `POST_NOTIFICATIONS` - YES/NO?
- [ ] `VIBRATE` - YES/NO?

**Copy the exact permissions section from hima's AndroidManifest.xml**
```xml
<!-- PASTE HIMA'S PERMISSIONS HERE -->
```

---

### Q3: IncomingCallActivity Declaration
**Get from:** `hima/app/src/main/AndroidManifest.xml`

How is IncomingCallActivity declared in hima?

```xml
<!-- PASTE HIMA'S ACTIVITY DECLARATION HERE -->
<activity
    android:name=".???.IncomingCallActivity"
    android:exported="?"
    android:theme="?"
    android:launchMode="?"
    android:showWhenLocked="?"
    android:turnScreenOn="?"
    android:excludeFromRecents="?" />
```

**Important:** Check if it has these attributes:
- [ ] `showWhenLocked="true"` - YES/NO?
- [ ] `turnScreenOn="true"` - YES/NO?
- [ ] `excludeFromRecents="true"` - YES/NO?

---

### Q4: Service Declarations
**Get from:** `hima/app/src/main/AndroidManifest.xml`

How are services declared in hima?

```xml
<!-- PASTE HIMA'S SERVICE DECLARATIONS HERE -->
<service
    android:name=".???.IncomingCallService"
    android:foregroundServiceType="?"
    android:exported="?" />

<service
    android:name=".???.CallNotificationService"
    android:exported="?">
    <intent-filter>
        <action android:name="?" />
    </intent-filter>
</service>
```

---

## 🔔 NOTIFICATION SETUP

### Q5: Notification Channel Configuration
**Get from:** `hima/.../CallNotificationManager.kt` or similar file

What are the notification channel settings in hima?

```kotlin
// PASTE HIMA'S NOTIFICATION CHANNEL CREATION CODE HERE

// Look for:
NotificationChannel(
    channelId = "?",
    channelName = "?",
    importance = ?  // What importance level?
)

// Does it have:
channel.setSound(?, ?)  // What sound settings?
channel.enableVibration(?)  // true or false?
channel.lockscreenVisibility = ?
channel.setBypassDnd(?)  // true or false?
```

**Key Questions:**
- [ ] What is the channel importance? (IMPORTANCE_HIGH, IMPORTANCE_MAX, etc.)
- [ ] Is sound null or set?
- [ ] Is vibration enabled or disabled?
- [ ] Does it bypass DND?

---

### Q6: Notification Building
**Get from:** `hima/.../CallNotificationManager.kt` or similar file

How does hima build the incoming call notification?

```kotlin
// PASTE HIMA'S NOTIFICATION BUILDING CODE HERE

NotificationCompat.Builder(context, channelId)
    .setContentTitle(?)
    .setContentText(?)
    .setSmallIcon(?)
    .setPriority(?)  // What priority?
    .setCategory(?)  // What category?
    .setFullScreenIntent(?, ?)  // Second parameter true or false?
    .setContentIntent(?)
    .setAutoCancel(?)
    .setOngoing(?)
    .setTimeoutAfter(?)
    .setSound(?)
    .setVibrate(?)
    .setSilent(?)
    .setOnlyAlertOnce(?)
    .setVisibility(?)
    .build()
```

**Critical Questions:**
- [ ] What is `setFullScreenIntent(pendingIntent, ?)` - second parameter true or false?
- [ ] What is `setPriority(?)`? (PRIORITY_MAX, PRIORITY_HIGH, etc.)
- [ ] Is `setSilent(true)` or `setSilent(false)`?
- [ ] What is the notification category? (CATEGORY_CALL, etc.)

---

### Q7: Full-Screen Intent PendingIntent
**Get from:** `hima/.../CallNotificationManager.kt` or similar file

How is the full-screen intent PendingIntent created in hima?

```kotlin
// PASTE HIMA'S FULL-SCREEN INTENT CODE HERE

val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
    flags = ?  // What flags?
    // What extras are passed?
}

val fullScreenPendingIntent = PendingIntent.getActivity(
    context,
    requestCode = ?,
    fullScreenIntent,
    flags = ?  // What flags? (FLAG_IMMUTABLE, FLAG_UPDATE_CURRENT, etc.)
)
```

**Key Questions:**
- [ ] What Intent flags are used?
- [ ] What PendingIntent flags are used?
- [ ] What is the request code?

---

## 🚀 SERVICE IMPLEMENTATION

### Q8: IncomingCallService - Starting Foreground
**Get from:** `hima/.../IncomingCallService.kt`

How does hima start the foreground service?

```kotlin
// PASTE HIMA'S startForeground() CODE HERE

override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // ...
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        startForeground(
            notificationId = ?,
            notification = ?,
            foregroundServiceType = ?  // What type?
        )
    } else {
        startForeground(?, ?)
    }
    
    return ?  // START_STICKY, START_NOT_STICKY, etc.
}
```

**Key Questions:**
- [ ] What is the foreground service type?
- [ ] What does it return? (START_STICKY, START_NOT_STICKY)

---

### Q9: IncomingCallService - Activity Launch
**Get from:** `hima/.../IncomingCallService.kt`

**CRITICAL QUESTION:** Does hima's IncomingCallService manually call `startActivity()`?

```kotlin
// PASTE HIMA'S ACTIVITY LAUNCH CODE HERE (if it exists)

// Does hima have something like this?
private fun launchFullScreenActivity() {
    val intent = Intent(this, IncomingCallActivity::class.java).apply {
        flags = ?
        // extras
    }
    startActivity(intent)  // ⬅️ DOES THIS EXIST IN HIMA?
}
```

**Key Questions:**
- [ ] YES - Hima DOES manually call `startActivity()` from service
- [ ] NO - Hima DOES NOT manually call `startActivity()` (relies only on notification)

**If YES, copy the entire function here:**
```kotlin
// PASTE THE COMPLETE FUNCTION HERE
```

---

### Q10: FCM Message Handling
**Get from:** `hima/.../CallNotificationService.kt` (or Firebase service)

How does hima handle FCM messages?

```kotlin
// PASTE HIMA'S onMessageReceived() CODE HERE

override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    
    // What does it do with the data?
    // Does it start IncomingCallService?
    // Does it show notification directly?
}
```

---

## 🔊 RINGTONE HANDLING

### Q11: Ringtone Manager Implementation
**Get from:** `hima/.../CallRingtoneManager.kt` or similar file

How does hima handle ringtone playback?

```kotlin
// PASTE HIMA'S RINGTONE CODE HERE

fun startRinging() {
    // Does it request audio focus?
    audioManager?.requestAudioFocus(?)  // ⬅️ DOES THIS EXIST?
    
    // How does it play ringtone?
    ringtone = RingtoneManager.getRingtone(?)
    ringtone?.audioAttributes = ?
    ringtone?.isLooping = ?
    ringtone?.play()
    
    // Does it start vibration?
}
```

**Critical Questions:**
- [ ] Does hima request audio focus BEFORE playing ringtone? YES/NO?
- [ ] If YES, copy the audio focus request code:
```kotlin
// PASTE AUDIO FOCUS REQUEST CODE HERE
```

---

### Q12: Audio Manager Setup
**Get from:** `hima/.../CallRingtoneManager.kt`

Does hima initialize AudioManager?

```kotlin
// PASTE HIMA'S AudioManager INITIALIZATION

private var audioManager: AudioManager? = null

init {
    audioManager = context.getSystemService(?) as? AudioManager
}
```

**Questions:**
- [ ] Does hima have an AudioManager instance? YES/NO?
- [ ] Does it check ringer mode before playing? YES/NO?

---

## 📲 ACTIVITY IMPLEMENTATION

### Q13: IncomingCallActivity - Window Flags
**Get from:** `hima/.../IncomingCallActivity.kt`

How does hima set up window flags?

```kotlin
// PASTE HIMA'S setupWindowFlags() or onCreate() CODE HERE

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // How does it set window flags?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(?)
        setTurnScreenOn(?)
    } else {
        window.addFlags(?)
    }
    
    // Does it dismiss keyguard?
    // Does it keep screen on?
}
```

---

### Q14: IncomingCallActivity - Lifecycle
**Get from:** `hima/.../IncomingCallActivity.kt`

What launch mode and behavior does hima use?

**Questions:**
- [ ] What is the launch mode? (singleTop, singleTask, standard)
- [ ] Does it finish itself after accept/reject? YES/NO?
- [ ] Does it send any broadcasts? YES/NO?

---

## 🔐 PERMISSION HANDLING

### Q15: Full-Screen Intent Permission Check
**Get from:** Search hima project for "canUseFullScreenIntent" or "USE_FULL_SCREEN_INTENT"

Does hima check or request full-screen intent permission?

```kotlin
// PASTE HIMA'S PERMISSION CHECK CODE HERE (if exists)

fun canUseFullScreenIntent(context: Context): Boolean {
    // What does hima do here?
}
```

**Questions:**
- [ ] Does hima check this permission? YES/NO?
- [ ] If YES, for which Android versions?
- [ ] Does hima request this permission from user? YES/NO?
- [ ] If YES, copy the request code:
```kotlin
// PASTE PERMISSION REQUEST CODE HERE
```

---

### Q16: Battery Optimization
**Get from:** Search hima project for "battery" or "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"

Does hima handle battery optimization?

**Questions:**
- [ ] Does hima request battery optimization exemption? YES/NO?
- [ ] If YES, copy the code:
```kotlin
// PASTE BATTERY OPTIMIZATION CODE HERE
```

---

## 🎯 KEY DIFFERENCES TO IDENTIFY

### Q17: Main Differences Checklist

After going through hima's code, fill this out:

**Notification:**
- [ ] Hima uses importance: `_____________` vs onlycare uses: `IMPORTANCE_HIGH`
- [ ] Hima full-screen intent second param: `_____` vs onlycare uses: `true`
- [ ] Hima priority: `_____________` vs onlycare uses: `PRIORITY_MAX`

**Service:**
- [ ] Hima manually launches activity: `YES/NO` vs onlycare: `YES`
- [ ] Hima service return: `_____________` vs onlycare uses: `START_NOT_STICKY`

**Ringtone:**
- [ ] Hima requests audio focus: `YES/NO` vs onlycare: `NO`
- [ ] Hima checks ringer mode: `YES/NO` vs onlycare: `NO`

**Permissions:**
- [ ] Hima checks permission for Android: `_____+` vs onlycare: `14+`
- [ ] Hima requests permission: `YES/NO` vs onlycare: `NO`

---

## 📝 FILES TO SHARE FROM HIMA

Please share these files from hima project:

### Critical Files:
1. **AndroidManifest.xml**
   - Path: `hima/app/src/main/AndroidManifest.xml`
   
2. **build.gradle.kts**
   - Path: `hima/app/build.gradle.kts`

3. **IncomingCallService.kt** (or similar)
   - Path: `hima/app/src/main/java/.../IncomingCallService.kt`

4. **CallNotificationManager.kt** (or similar)
   - Path: `hima/app/src/main/java/.../CallNotificationManager.kt`

5. **CallRingtoneManager.kt** (or similar)
   - Path: `hima/app/src/main/java/.../RingtoneManager.kt`

6. **IncomingCallActivity.kt**
   - Path: `hima/app/src/main/java/.../IncomingCallActivity.kt`

7. **FCM Service** (Firebase messaging)
   - Path: `hima/app/src/main/java/.../FirebaseMessagingService.kt`

---

## 🔍 HOW TO GET ANSWERS

### Option 1: Manual Comparison
1. Open hima project in Android Studio
2. Go through each question above
3. Find the relevant code
4. Copy and paste it into this document

### Option 2: Share Files
1. Copy the 7 files listed above from hima
2. Send them to me
3. I'll do the comparison automatically

### Option 3: Quick Search
Search hima project for these keywords:
```
- "setFullScreenIntent"
- "startForeground"
- "startActivity" (inside service)
- "requestAudioFocus"
- "IMPORTANCE_"
- "PRIORITY_"
- "canUseFullScreenIntent"
- "setShowWhenLocked"
```

For each search result, note the file path and copy the surrounding code.

---

## 🎯 MOST CRITICAL QUESTIONS

If you can only answer a few, answer these:

### TOP 5 CRITICAL QUESTIONS:

**#1:** Does hima's `IncomingCallService` manually call `startActivity()`? (Q9)
- [ ] YES - Copy the code
- [ ] NO - Relies only on notification

**#2:** Does hima request audio focus before playing ringtone? (Q11)
- [ ] YES - Copy the code
- [ ] NO

**#3:** What is the full-screen intent PendingIntent setup in hima? (Q7)
- Copy the entire code block

**#4:** What is the notification importance level in hima? (Q5)
- `IMPORTANCE_______`

**#5:** What Android SDK versions does hima target? (Q1)
- minSdk: `___`
- targetSdk: `___`
- compileSdk: `___`

---

## 📊 COMPARISON TABLE (Fill This Out)

| Feature | HIMA (Working) | ONLYCARE (Broken) | Different? |
|---------|----------------|-------------------|------------|
| **minSdk** | ? | 24 | ? |
| **targetSdk** | ? | 35 | ? |
| **Notification Importance** | ? | IMPORTANCE_HIGH | ? |
| **Full-screen intent (2nd param)** | ? | true | ? |
| **Manual startActivity() in service** | ? | YES | ? |
| **Audio focus requested** | ? | NO | ? |
| **Permission check version** | ? | Android 14+ only | ? |
| **Service return type** | ? | START_NOT_STICKY | ? |
| **Foreground service type** | ? | MICROPHONE | ? |

---

## ✅ NEXT STEPS

1. **Fill out this questionnaire** by checking hima's code
2. **Pay special attention** to Q9 (manual startActivity) and Q11 (audio focus)
3. **Share the answers** or share the actual files from hima
4. **I will identify** the exact differences causing the issue
5. **We'll fix** onlycare based on hima's working implementation

---

**Priority Questions:** Focus on Q1, Q5, Q7, Q9, Q11 first - these are the most likely culprits!

**Estimated Time:** 15-20 minutes to go through hima's code and answer these questions



