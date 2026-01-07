# ✅ HIMA vs ONLYCARE - Simple Checklist

Fill this out by checking hima's code. Just check YES/NO for each item.

---

## 📋 CHECKLIST

### 🔧 Build Configuration

**File:** `hima/app/build.gradle.kts`

```
minSdk = _____  (onlycare: 24)
targetSdk = _____  (onlycare: 35)
compileSdk = _____  (onlycare: 35)
Agora SDK version = _____  (onlycare: 4.5.0)
```

---

### 📱 Manifest Permissions

**File:** `hima/app/src/main/AndroidManifest.xml`

- [ ] `USE_FULL_SCREEN_INTENT` (onlycare: ✅ YES)
- [ ] `SYSTEM_ALERT_WINDOW` (onlycare: ✅ YES)
- [ ] `WAKE_LOCK` (onlycare: ✅ YES)
- [ ] `FOREGROUND_SERVICE` (onlycare: ✅ YES)
- [ ] `FOREGROUND_SERVICE_MICROPHONE` (onlycare: ✅ YES)

---

### 🎬 IncomingCallActivity Manifest

**File:** `hima/app/src/main/AndroidManifest.xml`

```xml
<activity
    android:name=".???.IncomingCallActivity"
    android:showWhenLocked="____"  (onlycare: true)
    android:turnScreenOn="____"  (onlycare: true)
    android:excludeFromRecents="____"  (onlycare: true)
    android:launchMode="____"  (onlycare: singleTop)
/>
```

---

### 🚨 IncomingCallService Behavior (CRITICAL!)

**File:** `hima/.../IncomingCallService.kt`

#### Question 1: Does service manually call `startActivity()`?
- [ ] **YES** - Hima manually launches activity from service
- [ ] **NO** - Hima relies only on notification's full-screen intent

**If YES, copy the code here:**
```kotlin
// PASTE HIMA's CODE:


```

**If NO, what does hima do instead?**
```kotlin
// PASTE what hima does after startForeground():


```

#### Question 2: Service return type?
```kotlin
return START_______  (onlycare: START_NOT_STICKY)
```
Options: `START_STICKY`, `START_NOT_STICKY`, `START_REDELIVER_INTENT`

---

### 🔔 Notification Setup (CRITICAL!)

**File:** `hima/.../CallNotificationManager.kt` or similar

#### Notification Channel:
```kotlin
NotificationChannel(
    id = "_____",
    name = "_____",
    importance = IMPORTANCE_______  (onlycare: IMPORTANCE_HIGH)
)

channel.setSound(null, null) = _____  (onlycare: true)
channel.enableVibration(false) = _____  (onlycare: true)
channel.setBypassDnd(true) = _____  (onlycare: true)
```

#### Notification Builder:
```kotlin
NotificationCompat.Builder(context, channelId)
    .setPriority(PRIORITY_______)  (onlycare: PRIORITY_MAX)
    .setCategory(CATEGORY_______)  (onlycare: CATEGORY_CALL)
    .setFullScreenIntent(intent, _______)  (onlycare: true)
    .setSilent(_______)  (onlycare: true)
```

#### PendingIntent Flags:
```kotlin
PendingIntent.getActivity(
    context,
    0,
    fullScreenIntent,
    PendingIntent.FLAG_______  (onlycare: FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT)
)
```

---

### 🔊 Ringtone Manager (CRITICAL!)

**File:** `hima/.../CallRingtoneManager.kt` or similar

#### Question: Does hima request audio focus?
- [ ] **YES** - Hima requests audio focus BEFORE playing ringtone
- [ ] **NO** - Hima doesn't request audio focus (onlycare: NO)

**If YES, copy the code here:**
```kotlin
// PASTE HIMA's audio focus request:


```

#### Question: Does hima check ringer mode?
- [ ] **YES** - Hima checks if phone is silent/vibrate
- [ ] **NO** - Hima doesn't check ringer mode (onlycare: NO)

**If YES, copy the code here:**
```kotlin
// PASTE HIMA's ringer mode check:


```

---

### 🔐 Permission Checks

**File:** Search hima for "canUseFullScreenIntent" or permission checks

#### Question: Does hima check full-screen intent permission?
- [ ] **YES** - Hima checks the permission
- [ ] **NO** - Hima doesn't check (onlycare: YES, checks Android 14+)

**If YES, for which Android versions?**
```kotlin
// PASTE HIMA's permission check:


```

#### Question: Does hima request permission from user?
- [ ] **YES** - Shows dialog to user
- [ ] **NO** - Doesn't request (onlycare: NO)

---

## 🎯 KEY DIFFERENCES (Fill After Checking)

| Feature | HIMA | ONLYCARE | Same? |
|---------|------|----------|-------|
| Manual `startActivity()` in service | ☐ YES ☐ NO | ✅ YES | ☐ |
| Audio focus requested | ☐ YES ☐ NO | ❌ NO | ☐ |
| Full-screen intent 2nd param | `___` | `true` | ☐ |
| Notification importance | `___` | `HIGH` | ☐ |
| targetSdk version | `___` | `35` | ☐ |

---

## 📸 FILES TO SHARE (If Possible)

If you can share these files from hima, it will help:

1. [ ] `hima/app/build.gradle.kts`
2. [ ] `hima/app/src/main/AndroidManifest.xml`
3. [ ] `hima/.../IncomingCallService.kt`
4. [ ] `hima/.../CallNotificationManager.kt`
5. [ ] `hima/.../CallRingtoneManager.kt` (or similar ringtone file)

---

## ⚡ FASTEST WAY TO GET ANSWERS

### Option A: Quick Search (5 minutes)
1. Open hima project in Android Studio
2. Press `Ctrl+Shift+F` (or `Cmd+Shift+F` on Mac)
3. Search for these terms one by one:
   - `startActivity(`  (in IncomingCallService)
   - `requestAudioFocus`
   - `setFullScreenIntent`
   - `IMPORTANCE_`
   - `targetSdk`

4. For each search result, note YES/NO and copy the code

### Option B: Share Files (2 minutes)
1. Copy the 5 files listed above
2. Send them to me
3. I'll do the comparison

---

**After filling this out, we'll know EXACTLY what's different between hima and onlycare! 🎯**



