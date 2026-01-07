# Call Accept Navigation Fix

## 🐛 Problem

When accepting an incoming call, the `IncomingCallActivity` would close but wouldn't navigate to the actual call screen (AudioCallScreen/VideoCallScreen). The user would just see the MainActivity instead of being connected to the call.

### What Was Happening:
1. ✅ Incoming call appears with ringing screen
2. ✅ User clicks "Accept" button
3. ❌ Ringing screen closes
4. ❌ **NO navigation to call screen**
5. ❌ User sees MainActivity (home screen) instead
6. ❌ Call is not connected

---

## 🔍 Root Cause

The issue was **missing call data** in the broadcast communication between `IncomingCallActivity` and `FemaleHomeScreen`:

### Data Flow Problem:
```
IncomingCallService
  ↓ (missing: callId, agoraAppId, callType)
IncomingCallActivity
  ↓ (missing: callId, agoraAppId, callType)
Broadcast to FemaleHomeScreen
  ↓ (missing data = can't navigate!)
❌ Navigation fails
```

### Specific Issues:

1. **IncomingCallService** was only passing 5 fields:
   - ✅ caller_id
   - ✅ caller_name
   - ✅ caller_photo
   - ✅ channel_id
   - ✅ agora_token
   - ❌ Missing: call_id, agora_app_id, call_type

2. **IncomingCallActivity** wasn't extracting or broadcasting:
   - ❌ call_id (needed for API calls)
   - ❌ agora_app_id (needed for Agora initialization)
   - ❌ call_type (needed to decide Audio vs Video screen)

3. **FemaleHomeScreen** broadcast receiver tried to get missing data from state:
   - Line 117: `val call = state.incomingCall ?: return`
   - Problem: State might be null or stale after activity transition

---

## ✅ Solution

### Step 1: Added Missing Constants
**File: `IncomingCallService.kt`**

Added three new constants:
```kotlin
const val EXTRA_CALL_ID = "call_id"
const val EXTRA_AGORA_APP_ID = "agora_app_id"
const val EXTRA_CALL_TYPE = "call_type"
```

### Step 2: Updated Service to Pass All Data
**File: `IncomingCallService.kt`**

Now extracts and forwards all 8 required fields:
```kotlin
private fun handleIncomingCall(intent: Intent) {
    val callerId = intent.getStringExtra(EXTRA_CALLER_ID) ?: return
    val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Unknown"
    val callerPhoto = intent.getStringExtra(EXTRA_CALLER_PHOTO)
    val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: return
    val agoraToken = intent.getStringExtra(EXTRA_AGORA_TOKEN) ?: return
    val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return          // ✅ NEW
    val agoraAppId = intent.getStringExtra(EXTRA_AGORA_APP_ID) ?: return // ✅ NEW
    val callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "AUDIO"     // ✅ NEW
    
    // ... rest of the code
}
```

### Step 3: Updated FemaleHomeScreen to Send All Data
**File: `FemaleHomeScreen.kt`**

Now passes all required fields when starting the service:
```kotlin
val serviceIntent = Intent(context, IncomingCallService::class.java).apply {
    action = IncomingCallService.ACTION_INCOMING_CALL
    putExtra(IncomingCallService.EXTRA_CALLER_ID, call.callerId)
    putExtra(IncomingCallService.EXTRA_CALLER_NAME, call.callerName)
    putExtra(IncomingCallService.EXTRA_CALLER_PHOTO, call.callerImage)
    putExtra(IncomingCallService.EXTRA_CHANNEL_ID, call.channelName ?: "")
    putExtra(IncomingCallService.EXTRA_AGORA_TOKEN, call.agoraToken ?: "")
    putExtra(IncomingCallService.EXTRA_CALL_ID, call.id)              // ✅ NEW
    putExtra(IncomingCallService.EXTRA_AGORA_APP_ID, call.agoraAppId ?: "") // ✅ NEW
    putExtra(IncomingCallService.EXTRA_CALL_TYPE, call.callType)      // ✅ NEW
}
```

### Step 4: Updated IncomingCallActivity
**File: `IncomingCallActivity.kt`**

Now extracts all fields and includes them in the broadcast:
```kotlin
// Added new fields
private var callId: String? = null
private var agoraAppId: String? = null
private var callType: String? = null

private fun extractCallerInfo() {
    callerId = intent.getStringExtra(IncomingCallService.EXTRA_CALLER_ID)
    callerName = intent.getStringExtra(IncomingCallService.EXTRA_CALLER_NAME)
    callerPhoto = intent.getStringExtra(IncomingCallService.EXTRA_CALLER_PHOTO)
    channelId = intent.getStringExtra(IncomingCallService.EXTRA_CHANNEL_ID)
    agoraToken = intent.getStringExtra(IncomingCallService.EXTRA_AGORA_TOKEN)
    callId = intent.getStringExtra(IncomingCallService.EXTRA_CALL_ID)          // ✅ NEW
    agoraAppId = intent.getStringExtra(IncomingCallService.EXTRA_AGORA_APP_ID) // ✅ NEW
    callType = intent.getStringExtra(IncomingCallService.EXTRA_CALL_TYPE)      // ✅ NEW
}

private fun navigateToCallScreen() {
    val acceptIntent = Intent("com.onlycare.app.CALL_ACCEPTED").apply {
        putExtra("caller_id", callerId)
        putExtra("caller_name", callerName)
        putExtra("caller_photo", callerPhoto)
        putExtra("channel_id", channelId)
        putExtra("agora_token", agoraToken)
        putExtra("call_id", callId)              // ✅ NEW
        putExtra("agora_app_id", agoraAppId)     // ✅ NEW
        putExtra("call_type", callType)          // ✅ NEW
    }
    sendBroadcast(acceptIntent)
}
```

### Step 5: Updated Broadcast Receiver
**File: `FemaleHomeScreen.kt`**

Now reads all data from the broadcast intent with state as fallback:
```kotlin
val callAcceptedReceiver = object : android.content.BroadcastReceiver() {
    override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
        if (intent?.action == "com.onlycare.app.CALL_ACCEPTED") {
            // Get data from intent (primary source)
            val callerId = intent.getStringExtra("caller_id")
            val callerName = intent.getStringExtra("caller_name")
            val channelId = intent.getStringExtra("channel_id")
            val agoraToken = intent.getStringExtra("agora_token")
            val callId = intent.getStringExtra("call_id")          // ✅ NEW
            val agoraAppId = intent.getStringExtra("agora_app_id") // ✅ NEW
            val callType = intent.getStringExtra("call_type")      // ✅ NEW
            
            // Fallback to state if intent data is missing
            val call = state.incomingCall
            
            val finalCallId = callId ?: call?.id
            val finalAgoraAppId = agoraAppId ?: call?.agoraAppId
            val finalCallType = callType ?: call?.callType ?: "AUDIO"
            
            // ... navigate with all required data
        }
    }
}
```

---

## 🎯 Expected Behavior Now

### ✅ Complete Flow:
1. ✅ Incoming call arrives
2. ✅ `IncomingCallService` starts with **ALL 8 fields**
3. ✅ `IncomingCallActivity` shows ringing screen
4. ✅ User clicks "Accept"
5. ✅ Broadcast sent with **ALL 8 fields**
6. ✅ `FemaleHomeScreen` receives broadcast
7. ✅ Has all required data (callId, agoraAppId, callType)
8. ✅ Calls API to accept call
9. ✅ **Navigates to AudioCallScreen/VideoCallScreen**
10. ✅ Agora initializes and connects
11. ✅ Call works! 🎉

---

## 🧪 Testing

### What to Look For:

1. **Accept a call** - Click the green "Answer" button

2. **Check logs** - You should see:
```
IncomingCallService: Call details - Call ID: CALL_xxx, Type: AUDIO, App ID: 63783c...
IncomingCallActivity: Call details - Call ID: CALL_xxx, Type: AUDIO, App ID: 63783c..., Token: Present
IncomingCallActivity: Call accepted broadcast sent with all call data
FemaleHomeScreen: 📞 Accepting call with data:
FemaleHomeScreen:   - Call ID: CALL_xxx
FemaleHomeScreen:   - Call Type: AUDIO
FemaleHomeScreen:   - Agora App ID: 63783c...
FemaleHomeScreen: 🚀 Navigating to: audio_call/...
```

3. **Visual behavior**:
   - ✅ Ringing screen appears
   - ✅ Click "Answer"
   - ✅ Ringing screen closes
   - ✅ **Call screen appears immediately** (black screen with controls)
   - ✅ After 2-3 seconds, shows "Connected"
   - ✅ Can hear audio/see video
   - ✅ Controls work (mute, speaker, end call)

### ❌ If Navigation Still Fails:

Check for these log errors:
```
FemaleHomeScreen: ❌ Missing required call data!
FemaleHomeScreen:   - callerId: false/true
FemaleHomeScreen:   - channelId: false/true
FemaleHomeScreen:   - callId: false/true      ← Should be true now
FemaleHomeScreen:   - agoraAppId: false/true  ← Should be true now
```

---

## 📝 Summary

### Files Changed:
1. ✅ `IncomingCallService.kt` - Added 3 new constants, updated data handling
2. ✅ `IncomingCallActivity.kt` - Added 3 new fields, extract and broadcast them
3. ✅ `FemaleHomeScreen.kt` - Pass 3 new extras to service, read from broadcast

### Data Flow (Fixed):
```
FemaleHomeScreen (has all 8 fields from API)
  ↓ Pass all 8 fields
IncomingCallService (receives all 8 fields)
  ↓ Forward all 8 fields
IncomingCallActivity (extracts all 8 fields)
  ↓ Broadcast all 8 fields
FemaleHomeScreen Receiver (reads all 8 fields)
  ↓ Has everything needed!
✅ Navigate to Call Screen
```

### Required Fields for Navigation:
1. ✅ caller_id - Who's calling
2. ✅ caller_name - Display name
3. ✅ channel_id - Agora channel
4. ✅ agora_token - Authentication token
5. ✅ **call_id** - For API calls (accept/reject/end)
6. ✅ **agora_app_id** - For Agora SDK initialization
7. ✅ **call_type** - To choose Audio vs Video screen
8. ⚠️ caller_photo - Optional

---

## 🚀 Next Steps

The call accept navigation is now fixed! When you accept a call, you should:
1. See the ringing screen close smoothly
2. **Immediately see the call screen open**
3. Connect to the call within 2-3 seconds
4. Be able to communicate with the other user

Test this fix and verify that accepting calls now properly navigates to the call screen!



