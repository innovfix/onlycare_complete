# Agora Token Logging - Android App

## Summary
Added comprehensive logging throughout the Android app to display Agora credentials (App ID, Channel Name, Token) whenever they are received or used.

---

## Files Modified

### 1. **IncomingCallActivity.kt**
**Location:** `android_app/app/src/main/java/com/onlycare/app/presentation/screens/call/IncomingCallActivity.kt`

**Method:** `extractCallerInfo()` (Line ~362)

**Added Logging:**
```kotlin
Log.d(TAG, "========================================")
Log.d(TAG, "📞 INCOMING CALL DATA RECEIVED")
Log.d(TAG, "========================================")
Log.d(TAG, "Caller info:")
Log.d(TAG, "  - Caller ID: $callerId")
Log.d(TAG, "  - Caller Name: $callerName")
Log.d(TAG, "  - Call ID: $callId")
Log.d(TAG, "  - Call Type: $callType")
Log.d(TAG, "  - Balance Time: $balanceTime")
Log.d(TAG, "")
Log.d(TAG, "🔑 AGORA CREDENTIALS RECEIVED:")
Log.d(TAG, "========================================")
Log.d(TAG, "App ID: $agoraAppId")
Log.d(TAG, "Channel Name: $channelId")
Log.d(TAG, "Token: ${agoraToken ?: "NULL"}")
Log.d(TAG, "Token Length: ${agoraToken?.length ?: 0}")
Log.d(TAG, "Token Empty: ${agoraToken.isNullOrEmpty()}")
Log.d(TAG, "========================================")
```

**When it logs:** When an incoming call activity is opened (female receiving call)

---

### 2. **CallNotificationService.kt**
**Location:** `android_app/app/src/main/java/com/onlycare/app/services/CallNotificationService.kt`

**Method:** `handleIncomingCall()` (Line ~199)

**Added Logging:**
```kotlin
Log.d(TAG, "  - Caller Photo: ${callerPhoto ?: "NULL"}")
Log.d(TAG, "  - Call Type: ${callType ?: "NULL"}")
Log.d(TAG, "  - Balance Time: ${balanceTime ?: "NULL"}")
Log.d(TAG, "")
Log.d(TAG, "🔑 AGORA CREDENTIALS FROM FCM:")
Log.d(TAG, "========================================")
Log.d(TAG, "App ID: ${agoraAppId ?: "NULL"}")
Log.d(TAG, "Channel Name: ${channelId ?: "NULL"}")
Log.d(TAG, "Token: ${agoraToken ?: "NULL"}")
Log.d(TAG, "Token Length: ${agoraToken?.length ?: 0}")
Log.d(TAG, "Token Empty: ${agoraToken.isNullOrEmpty()}")
Log.d(TAG, "========================================")
```

**When it logs:** When FCM push notification is received with incoming call data

---

### 3. **RandomCallViewModel.kt**
**Location:** `android_app/app/src/main/java/com/onlycare/app/presentation/screens/call/RandomCallViewModel.kt`

**Method:** `initiateRandomCall()` (Line ~269)

**Added Logging:**
```kotlin
Log.d(TAG, "========================================")
Log.d(TAG, "📞 CALL INITIATED - API RESPONSE")
Log.d(TAG, "========================================")
Log.d(TAG, "Call ID: $callId")
Log.d(TAG, "Receiver ID: $receiverId")
Log.d(TAG, "Call Type: $callTypeEnum")
Log.d(TAG, "Balance Time: $balanceTime")
Log.d(TAG, "")
Log.d(TAG, "🔑 AGORA CREDENTIALS FROM API:")
Log.d(TAG, "========================================")
Log.d(TAG, "App ID: $appId")
Log.d(TAG, "Channel Name: $channel")
Log.d(TAG, "Token: $token")
Log.d(TAG, "Token Length: ${token.length}")
Log.d(TAG, "Token Empty: ${token.isBlank()}")
Log.d(TAG, "========================================")
```

**When it logs:** When male user initiates a random call and receives API response

---

### 4. **ApiDataRepository.kt**
**Location:** `android_app/app/src/main/java/com/onlycare/app/data/repository/ApiDataRepository.kt`

**Method:** `getIncomingCalls()` (Line ~1372)

**Added Logging:**
```kotlin
// Log Agora credentials for each incoming call
body.data.forEachIndexed { index, call ->
    Log.d(TAG, "")
    Log.d(TAG, "========================================")
    Log.d(TAG, "📞 INCOMING CALL #${index + 1} FROM API")
    Log.d(TAG, "========================================")
    Log.d(TAG, "Call ID: ${call.id}")
    Log.d(TAG, "Caller: ${call.callerName} (${call.callerId})")
    Log.d(TAG, "Call Type: ${call.callType}")
    Log.d(TAG, "Status: ${call.status}")
    Log.d(TAG, "Balance Time: ${call.balanceTime ?: "NULL"}")
    Log.d(TAG, "")
    Log.d(TAG, "🔑 AGORA CREDENTIALS FROM API:")
    Log.d(TAG, "========================================")
    Log.d(TAG, "App ID: ${call.agoraAppId ?: "NULL"}")
    Log.d(TAG, "Channel Name: ${call.channelName ?: "NULL"}")
    Log.d(TAG, "Token: ${call.agoraToken ?: "NULL"}")
    Log.d(TAG, "Token Length: ${call.agoraToken?.length ?: 0}")
    Log.d(TAG, "Token Empty: ${call.agoraToken.isNullOrEmpty()}")
    Log.d(TAG, "========================================")
}
```

**When it logs:** When fetching incoming calls from API (female polling for calls)

---

## What Gets Logged

For each location, the following Agora credentials are logged:

1. **App ID** - The Agora Application ID
2. **Channel Name** - The channel name (e.g., `call_CALL_17679595916078`)
3. **Token** - The complete Agora RTC token (139+ characters)
4. **Token Length** - Length of the token string
5. **Token Empty** - Whether the token is null or empty

Additional context logged:
- Call ID
- Caller/Receiver information
- Call Type (AUDIO/VIDEO)
- Balance Time
- Call Status

---

## Log Tags to Search

When checking Android logs (Logcat), search for:
- `IncomingCallActivity` - For incoming call data
- `CallNotificationService` - For FCM notification data
- `RandomCallViewModel` - For call initiation data
- `ApiDataRepository` - For API response data

**Search pattern:** Look for lines containing `🔑 AGORA CREDENTIALS`

---

## Example Log Output

```
========================================
📞 INCOMING CALL DATA RECEIVED
========================================
Caller info:
  - Caller ID: USR_17677720014836
  - Caller Name: User_4780
  - Call ID: CALL_17679595916078
  - Call Type: AUDIO
  - Balance Time: 90:00

🔑 AGORA CREDENTIALS RECEIVED:
========================================
App ID: 8b5e9417f15a48ae929783f32d3d33d4
Channel Name: call_CALL_17679595916078
Token: 0078b5e9417f15a48ae929783f32d3d33d4AAAAICWjdRS3CgtWf01SthPNkCqKUebs8CAA2VzAzG74fxkYBYaBqWlg7CdpYj2nABhjYWxsX0NBTExfMTc2Nzk1OTU5MTYwNzgAAAAA
Token Length: 139
Token Empty: false
========================================
```

---

## How to View Logs

### Using Android Studio:
1. Open Logcat tab
2. Select your device
3. Filter by package: `com.onlycare.app`
4. Search for: `AGORA CREDENTIALS`

### Using ADB:
```bash
adb logcat | grep "AGORA CREDENTIALS"
```

Or for specific tags:
```bash
adb logcat IncomingCallActivity:D CallNotificationService:D RandomCallViewModel:D ApiDataRepository:D *:S
```

---

**Generated:** 2026-01-09
**Purpose:** Debug and verify Agora token generation and transmission to Android users
