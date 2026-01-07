# 📱 Android Team - Balance Time Integration Guide

## 🎯 Overview

**Status**: ✅ Backend changes complete  
**Feature**: Countdown timer showing remaining call duration based on caller's coin balance

---

## ✅ What's Been Fixed (Backend)

The backend now sends `balance_time` field in:

1. ✅ **`/api/v1/calls/initiate`** - Caller receives this when initiating a call
2. ✅ **`/api/v1/calls/incoming`** - Receiver receives this when checking for incoming calls
3. ✅ **FCM Push Notifications** - Receiver gets `balanceTime` in notification data payload

---

## 📊 Field Specification

### Field Name: `balance_time` (API) / `balanceTime` (FCM)

**Data Type**: `String`

**Format**: 
- `"MM:SS"` for calls < 1 hour (e.g., `"25:00"`)
- `"HH:MM:SS"` for calls ≥ 1 hour (e.g., `"1:30:00"`)

**Examples**:
- `"50:00"` = 50 minutes (500 coins, audio call)
- `"25:00"` = 25 minutes (500 coins, video call)
- `"1:40:00"` = 1 hour 40 minutes (1000 coins, audio call)
- `"0:00"` = No balance (0 coins)

**Calculation**:
```
Audio calls: balance_time = (caller_coins ÷ 10) minutes
Video calls: balance_time = (caller_coins ÷ 20) minutes
```

---

## 📱 API Response Examples

### 1. Initiate Call Response (Caller Side)

**Endpoint**: `POST /api/v1/calls/initiate`

```json
{
  "success": true,
  "message": "Call initiated successfully",
  "call": {
    "id": "CALL_17639079312159",
    "caller_id": "USR_17637424324851",
    "caller_name": "User_5555",
    "receiver_id": "USR_17637424324852",
    "receiver_name": "Creator_Name",
    "call_type": "AUDIO",
    "status": "CONNECTING",
    "agora_app_id": "63783c2ad2724b839b1e58714bfc2629",
    "agora_token": "",
    "agora_uid": 0,
    "channel_name": "call_CALL_17639079312159",
    "balance_time": "50:00"  ← ✅ USE THIS FOR COUNTDOWN
  },
  "agora_app_id": "63783c2ad2724b839b1e58714bfc2629",
  "agora_token": "",
  "channel_name": "call_CALL_17639079312159",
  "balance_time": "50:00"  ← ✅ ALSO AVAILABLE HERE
}
```

### 2. Incoming Calls Response (Receiver Side)

**Endpoint**: `GET /api/v1/calls/incoming`

```json
{
  "success": true,
  "data": [
    {
      "id": "CALL_17639079312159",
      "caller_id": "USR_17637424324851",
      "caller_name": "User_5555",
      "caller_image": null,
      "call_type": "AUDIO",
      "status": "CONNECTING",
      "created_at": "2025-11-23 14:25:31",
      "agora_app_id": "63783c2ad2724b839b1e58714bfc2629",
      "agora_token": "",
      "agora_uid": 0,
      "channel_name": "call_CALL_17639079312159",
      "balance_time": "50:00"  ← ✅ USE THIS FOR COUNTDOWN
    }
  ]
}
```

### 3. FCM Push Notification (Receiver - Background/Killed App)

**FCM Data Payload**:

```json
{
  "type": "incoming_call",
  "callId": "CALL_17639079312159",
  "callType": "AUDIO",
  "callerId": "USR_17637424324851",
  "callerName": "User_5555",
  "callerPhoto": "",
  "channelId": "call_CALL_17639079312159",
  "agoraAppId": "63783c2ad2724b839b1e58714bfc2629",
  "agoraToken": "",
  "balanceTime": "50:00"  ← ✅ USE THIS FOR COUNTDOWN
}
```

---

## 🎨 UI Implementation Examples

### Caller Screen (During Call)

```
┌─────────────────────────────────────┐
│  ☎️  Audio Call                     │
├─────────────────────────────────────┤
│                                     │
│         👤 Creator Name             │
│                                     │
│         ⏱️  50:00                   │
│      Time Remaining                 │
│                                     │
│         💰 500 coins                │
│                                     │
│    🔇    📞    🔊                   │
│   Mute  End  Speaker                │
│                                     │
└─────────────────────────────────────┘
```

### Receiver Screen (During Call)

```
┌─────────────────────────────────────┐
│  ☎️  Audio Call                     │
├─────────────────────────────────────┤
│                                     │
│         👤 User_5555                │
│                                     │
│         ⏱️  50:00                   │
│   (Caller's remaining time)         │
│                                     │
│         💰 500 coins                │
│     (You'll earn this)              │
│                                     │
│    🔇    📞    🔊                   │
│   Mute  End  Speaker                │
│                                     │
└─────────────────────────────────────┘
```

---

## 🎨 Timer States & Colors

### Normal State (> 2 minutes remaining)

- **Color**: White/Default
- **Display**: `"50:00"`
- **Icon**: ⏱️ Normal timer icon

### Warning State (< 2 minutes remaining)

- **Color**: 🟠 Orange (`#FF9800`)
- **Display**: `"1:30"` (flashing)
- **Icon**: ⏰ Warning icon
- **Behavior**: Flash every second

### Critical State (< 1 minute remaining)

- **Color**: 🔴 Red (`#F44336`)
- **Display**: `"0:45"` (pulsing)
- **Icon**: ⚠️ Critical warning icon
- **Behavior**: Pulse animation (scale 1.0 → 1.1 → 1.0)
- **Alert**: Show snackbar: "Call will end in less than 1 minute!"

### Zero Balance (0:00 reached)

- **Action**: Auto-end call
- **Message**: "Call ended: Insufficient balance"

---

## 💻 Sample Android Code

### 1. Parse `balance_time` from API Response

```kotlin
data class CallResponse(
    val success: Boolean,
    val message: String,
    val call: CallData,
    val balance_time: String  // ← Parse this field
)

data class CallData(
    val id: String,
    val caller_id: String,
    val receiver_id: String,
    val call_type: String,
    val status: String,
    val agora_app_id: String,
    val agora_token: String,
    val channel_name: String,
    val balance_time: String  // ← Also available here
)
```

### 2. Convert `balance_time` String to Seconds

```kotlin
fun parseBalanceTime(balanceTime: String): Int {
    // Format: "MM:SS" or "HH:MM:SS"
    val parts = balanceTime.split(":")
    
    return when (parts.size) {
        2 -> {
            // Format: "MM:SS"
            val minutes = parts[0].toIntOrNull() ?: 0
            val seconds = parts[1].toIntOrNull() ?: 0
            (minutes * 60) + seconds
        }
        3 -> {
            // Format: "HH:MM:SS"
            val hours = parts[0].toIntOrNull() ?: 0
            val minutes = parts[1].toIntOrNull() ?: 0
            val seconds = parts[2].toIntOrNull() ?: 0
            (hours * 3600) + (minutes * 60) + seconds
        }
        else -> 0
    }
}

// Example usage:
val balanceTime = "50:00"
val totalSeconds = parseBalanceTime(balanceTime)  // Returns: 3000 seconds
```

### 3. Countdown Timer Implementation

```kotlin
class CallViewModel : ViewModel() {
    private val _remainingSeconds = MutableLiveData<Int>()
    val remainingSeconds: LiveData<Int> = _remainingSeconds
    
    private var countdownTimer: CountDownTimer? = null
    
    fun startCountdown(balanceTime: String) {
        val totalSeconds = parseBalanceTime(balanceTime)
        _remainingSeconds.value = totalSeconds
        
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(
            (totalSeconds * 1000).toLong(),
            1000  // Update every second
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                _remainingSeconds.value = secondsRemaining
                
                // Check for warning states
                when {
                    secondsRemaining == 60 -> {
                        // Show critical warning at 1 minute
                        showWarning("Call will end in 1 minute!")
                    }
                    secondsRemaining == 120 -> {
                        // Show warning at 2 minutes
                        showWarning("2 minutes remaining")
                    }
                    secondsRemaining == 0 -> {
                        // Auto-end call
                        endCall()
                    }
                }
            }
            
            override fun onFinish() {
                _remainingSeconds.value = 0
                endCall()
            }
        }.start()
    }
    
    fun stopCountdown() {
        countdownTimer?.cancel()
    }
    
    private fun showWarning(message: String) {
        // Show snackbar or toast
    }
    
    private fun endCall() {
        // End the call and navigate back
    }
}
```

### 4. UI Display with Color States

```kotlin
@Composable
fun CallTimerDisplay(remainingSeconds: Int) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeString = String.format("%d:%02d", minutes, seconds)
    
    // Determine color based on remaining time
    val (textColor, iconAlpha) = when {
        remainingSeconds > 120 -> Color.White to 1f
        remainingSeconds > 60 -> Color(0xFFFF9800) to 0.8f  // Orange
        else -> Color(0xFFF44336) to 1f  // Red
    }
    
    // Pulsing animation for critical state
    val scale by animateFloatAsState(
        targetValue = if (remainingSeconds <= 60) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(if (remainingSeconds <= 60) scale else 1f)
    ) {
        Icon(
            imageVector = when {
                remainingSeconds > 120 -> Icons.Default.Timer
                remainingSeconds > 60 -> Icons.Default.Warning
                else -> Icons.Default.Error
            },
            contentDescription = "Timer",
            tint = textColor,
            modifier = Modifier.size(32.dp)
        )
        
        Text(
            text = timeString,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Text(
            text = "Time Remaining",
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}
```

### 5. Parse FCM Notification Data

```kotlin
// In your FCM Service
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        
        if (data["type"] == "incoming_call") {
            val callId = data["callId"] ?: return
            val callType = data["callType"] ?: "AUDIO"
            val callerId = data["callerId"] ?: return
            val callerName = data["callerName"] ?: "Unknown"
            val balanceTime = data["balanceTime"] ?: "0:00"  // ← Parse this
            val channelId = data["channelId"] ?: return
            val agoraToken = data["agoraToken"] ?: ""
            val agoraAppId = data["agoraAppId"] ?: return
            
            // Show incoming call UI with balance time
            showIncomingCallScreen(
                callId = callId,
                callerName = callerName,
                callType = callType,
                balanceTime = balanceTime,  // ← Pass to UI
                channelId = channelId,
                agoraToken = agoraToken,
                agoraAppId = agoraAppId
            )
        }
    }
}
```

---

## 🧪 Testing Instructions

### Backend Testing (Already Done ✅)

```bash
# 1. Add coins to test user
cd /var/www/onlycare_admin
mysql -u root -p onlycare_db < test_user_coins_update.sql

# 2. Run API test script
./test_balance_time_api.sh
```

### Android Testing

#### Test Case 1: User with 500 Coins - Audio Call

**Expected**:
- `balance_time: "50:00"` in API response
- Countdown starts at 50:00
- Timer counts down every second
- Normal color (white) for 48+ minutes

**Steps**:
1. Login as test user (USR_17637424324851)
2. Initiate audio call
3. Verify balance_time is displayed as "50:00"
4. Verify timer counts down correctly

#### Test Case 2: User with 500 Coins - Video Call

**Expected**:
- `balance_time: "25:00"` in API response
- Countdown starts at 25:00

**Steps**:
1. Login as test user
2. Initiate video call
3. Verify balance_time is displayed as "25:00"

#### Test Case 3: Warning State (< 2 minutes)

**Setup**: User with 150 coins (15 minutes audio)

**Expected**:
- Timer shows orange at 1:59
- Timer flashes every second

**Steps**:
1. Login with user having 150 coins
2. Initiate call
3. Fast-forward timer to 1:59 (or wait)
4. Verify orange color appears

#### Test Case 4: Critical State (< 1 minute)

**Setup**: User with 50 coins (5 minutes audio)

**Expected**:
- Timer shows red at 0:59
- Timer pulses (scale animation)
- Snackbar warning appears

**Steps**:
1. Login with user having 50 coins
2. Initiate call
3. Fast-forward timer to 0:59
4. Verify red color and pulsing

#### Test Case 5: Zero Balance (Call Auto-Ends)

**Expected**:
- Call ends automatically at 0:00
- Message: "Call ended: Insufficient balance"

**Steps**:
1. Let timer reach 0:00
2. Verify call ends automatically
3. Verify proper message is shown

#### Test Case 6: FCM Notification (Receiver)

**Expected**:
- Incoming call notification shows balanceTime
- Receiver can see caller's balance time

**Steps**:
1. User A calls User B (A has 500 coins)
2. User B (receiver) gets FCM notification
3. Verify notification data contains `balanceTime: "50:00"`
4. Verify incoming call screen shows "50:00"

---

## 🐛 Troubleshooting

### Issue: `balance_time` is "0:00"

**Possible Causes**:
1. Caller has 0 coins
2. Backend not calculating correctly

**Solution**:
```bash
# Check user's coin balance
mysql -u root -p onlycare_db -e "SELECT coin_balance FROM users WHERE id = 'USR_xxx';"

# Add test coins
mysql -u root -p onlycare_db -e "UPDATE users SET coin_balance = 500 WHERE id = 'USR_xxx';"
```

### Issue: `balance_time` field missing

**Possible Causes**:
1. Using old cached API response
2. Backend not deployed

**Solution**:
1. Clear app cache
2. Check API version
3. Verify backend deployment

### Issue: Timer not counting down

**Possible Causes**:
1. Parsing error
2. CountDownTimer not started

**Solution**:
1. Add logging to `parseBalanceTime()` function
2. Verify timer is started in `onCallAccepted()`

---

## 📊 Testing Checklist

**Backend** (Complete ✅):
- [x] `/api/v1/calls/initiate` returns `balance_time`
- [x] `/api/v1/calls/incoming` returns `balance_time`
- [x] FCM notification includes `balanceTime`
- [x] Calculation is accurate
- [x] Handles 0 balance edge case

**Android** (Pending):
- [ ] Parse `balance_time` from API response
- [ ] Parse `balanceTime` from FCM notification
- [ ] Display countdown timer on caller screen
- [ ] Display countdown timer on receiver screen
- [ ] Implement color states (white/orange/red)
- [ ] Implement warning at 2 minutes
- [ ] Implement critical warning at 1 minute
- [ ] Auto-end call at 0:00
- [ ] Test with different balance amounts
- [ ] Test audio and video calls

---

## 📞 Contact

**Backend Team**: Changes complete ✅  
**Android Team**: Ready for integration 🚀

**Questions?**
- Check `BALANCE_TIME_FIX_SUMMARY.md` for detailed backend changes
- Check Laravel logs: `tail -f /var/www/onlycare_admin/storage/logs/laravel.log`

---

## 🎉 Summary

**What You Get**:
1. ✅ `balance_time` field in all relevant API responses
2. ✅ `balanceTime` field in FCM notifications
3. ✅ Accurate calculation based on coin balance and call rate
4. ✅ Format: `"MM:SS"` or `"HH:MM:SS"`
5. ✅ Ready for countdown timer implementation

**What You Need to Build**:
1. Parse `balance_time` string to seconds
2. Implement CountDownTimer
3. Display timer in call UI
4. Implement color states (white → orange → red)
5. Show warnings at 2 min and 1 min
6. Auto-end call at 0:00

**Let's make it happen!** 🚀




