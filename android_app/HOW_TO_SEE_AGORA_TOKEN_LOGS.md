# How to See Agora Token Logs in Android App

## Updated Log Format (Now using Log.i for better visibility)

---

## For MALE USER (Caller)

**TAG:** `RandomCallViewModel`

**Search for:** `MALE USER - CALL INITIATED` or `AGORA CREDENTIALS FROM API (MALE SIDE)`

**Example Output:**
```
RandomCallViewModel: ========================================
RandomCallViewModel: 📞 MALE USER - CALL INITIATED
RandomCallViewModel: ========================================
RandomCallViewModel: CALL_ID: CALL_17679605661819
RandomCallViewModel: RECEIVER_ID: USR_17677720438040
RandomCallViewModel: CALL_TYPE: AUDIO
RandomCallViewModel: BALANCE_TIME: 90:00
RandomCallViewModel: 
RandomCallViewModel: 🔑 AGORA CREDENTIALS FROM API (MALE SIDE):
RandomCallViewModel: ========================================
RandomCallViewModel: AGORA_APP_ID: 8b5e9417f15a48ae929783f32d3d33d4
RandomCallViewModel: CHANNEL_NAME: call_CALL_17679605661819
RandomCallViewModel: AGORA_TOKEN: 0068b5e9417f15a48ae929783f32d3d33d4IAAl...
RandomCallViewModel: TOKEN_LENGTH: 139
RandomCallViewModel: TOKEN_EMPTY: false
RandomCallViewModel: ========================================
```

---

## For FEMALE USER (Receiver)

### Option 1: FCM Notification
**TAG:** `CallNotificationService`

**Search for:** `AGORA CREDENTIALS FROM FCM`

### Option 2: Incoming Call Screen
**TAG:** `IncomingCallActivity`

**Search for:** `AGORA CREDENTIALS RECEIVED`

**Example Output:**
```
IncomingCallActivity: ========================================
IncomingCallActivity: 📞 INCOMING CALL DATA RECEIVED
IncomingCallActivity: ========================================
IncomingCallActivity: CALLER_ID: USR_17677720014836
IncomingCallActivity: CALLER_NAME: User_4780
IncomingCallActivity: CALL_ID: CALL_17679605661819
IncomingCallActivity: CALL_TYPE: AUDIO
IncomingCallActivity: BALANCE_TIME: 90:00
IncomingCallActivity: 
IncomingCallActivity: 🔑 AGORA CREDENTIALS RECEIVED:
IncomingCallActivity: ========================================
IncomingCallActivity: AGORA_APP_ID: 8b5e9417f15a48ae929783f32d3d33d4
IncomingCallActivity: CHANNEL_NAME: call_CALL_17679605661819
IncomingCallActivity: AGORA_TOKEN: 0068b5e9417f15a48ae929783f32d3d33d4IAAl...
IncomingCallActivity: TOKEN_LENGTH: 139
IncomingCallActivity: TOKEN_EMPTY: false
IncomingCallActivity: ========================================
```

---

## How to Filter Logs in Android Studio

### Method 1: Log Level (RECOMMENDED)
1. In Logcat, set Log Level to **"Info"** or higher
2. Search for: `AGORA_TOKEN` or `AGORA CREDENTIALS`

### Method 2: By TAG
In Logcat filter, enter:
```
tag:RandomCallViewModel|IncomingCallActivity|CallNotificationService
```

### Method 3: By Text
Just search for:
- `AGORA_TOKEN:`
- `AGORA_APP_ID:`
- `CHANNEL_NAME:`
- `MALE USER - CALL INITIATED`
- `INCOMING CALL DATA RECEIVED`

---

## Using ADB Command Line

```bash
# See all Agora logs (Info level)
adb logcat *:I | grep "AGORA"

# Filter by specific tags
adb logcat RandomCallViewModel:I IncomingCallActivity:I CallNotificationService:I *:S

# Save to file
adb logcat *:I | grep "AGORA" > agora_token_logs.txt

# See only male side (caller)
adb logcat RandomCallViewModel:I *:S

# See only female side (receiver)
adb logcat IncomingCallActivity:I CallNotificationService:I *:S
```

---

## Troubleshooting

### If you don't see logs:

1. **Check Log Level:** Set to "Info" (I) or higher in Logcat
2. **Clear Filters:** Remove any text filters temporarily
3. **Check Package:** Make sure `com.onlycare.app` is selected
4. **Restart App:** Completely close and reopen the app
5. **Check ADB Connection:** `adb devices` should show your device

### If male side shows nothing:

- **TAG is:** `RandomCallViewModel`
- **Make sure you're filtering for INFO level (I)**
- Search for: `MALE USER - CALL INITIATED`

### If female side shows only headers:

- **Change log level from Debug (D) to Info (I)**
- The data lines use Log.i() now instead of Log.d()

---

## Quick Search Terms

Copy and paste these into Logcat search:

**For Male:**
```
MALE USER - CALL INITIATED
```

**For Female:**
```
INCOMING CALL DATA RECEIVED
```

**For Both:**
```
AGORA_TOKEN:
```

---

**Updated:** 2026-01-09
**Log Level:** INFO (Log.i)
**Format:** CAPITAL_LETTERS_WITH_UNDERSCORES for better visibility
