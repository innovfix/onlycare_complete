# Coin Balance and Timer Display Fix

## Problem Summary

On the creator (receiver) side during a call:
1. **Coin balance was showing as 0** - The screen was displaying `coinsSpent` (accumulating during call) instead of the caller's actual `coinBalance`
2. **Remaining time was not showing** - The `balanceTime` parameter was not being passed from the receiver's side when accepting calls

## Root Causes

### Issue 1: Wrong Coin Display
- Both `AudioCallScreen` and `VideoCallScreen` were showing `state.coinsSpent` (coins being spent during the call)
- They should have been showing `state.user?.coinBalance` (the caller's actual coin balance from their profile)

### Issue 2: Missing Balance Time Parameter
- When the receiver (creator) accepted a call through `FemaleHomeScreen`, the `balanceTime` was not being extracted from `IncomingCallDto` and passed to the call screen
- When navigating from `IncomingCallActivity` notification, the `balanceTime` was not being passed to `MainActivity`
- `MainActivity` was not extracting and passing `balanceTime` to the call screen navigation

## Changes Made

### 1. AudioCallScreen.kt
**Location**: Line 380-394 (ConnectedCallUI)

**Changed From**:
```kotlin
// Coins Spent
Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = Icons.Default.Paid,
        contentDescription = null,
        tint = androidx.compose.ui.graphics.Color(0xFFFFC107),
        modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
        text = "${state.coinsSpent} coins",
        style = MaterialTheme.typography.bodyLarge,
        color = White
    )
}
```

**Changed To**:
```kotlin
// User's Coin Balance (Caller's balance)
Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = Icons.Default.Paid,
        contentDescription = null,
        tint = androidx.compose.ui.graphics.Color(0xFFFFC107),
        modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
        text = "${state.user?.coinBalance ?: 0} coins",
        style = MaterialTheme.typography.bodyLarge,
        color = White
    )
}
```

### 2. VideoCallScreen.kt
**Location**: Line 276-290 (Top Info Bar)

**Changed From**:
```kotlin
Text(
    text = "${state.coinsSpent}",
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold,
    color = White
)
```

**Changed To**:
```kotlin
Text(
    text = "${state.user?.coinBalance ?: 0}",
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold,
    color = White
)
```

### 3. FemaleHomeScreen.kt
**Location**: Line 189-237 (Accept call handler)

**Added**:
```kotlin
val balanceTime = call.balanceTime ?: "" // Get caller's balance time
```

**Updated navigation**:
```kotlin
Screen.AudioCall.createRoute(
    userId = callerId,
    callId = callId,
    appId = agoraAppId,
    token = agoraToken,
    channel = channelName,
    role = "receiver",
    balanceTime = balanceTime  // ✅ Now passed
)
```

### 4. IncomingCallActivity.kt
**Location**: Line 214-224 (navigateToCallScreen)

**Added**:
```kotlin
putExtra("balance_time", balanceTime ?: "")
```

### 5. MainActivity.kt

#### a) PendingCallNavigation data class
**Location**: Line 59-67

**Added**:
```kotlin
data class PendingCallNavigation(
    val callerId: String,
    val callId: String,
    val agoraAppId: String,
    val agoraToken: String,
    val channelId: String,
    val callType: String,
    val balanceTime: String = ""  // ✅ Added
)
```

#### b) Navigation route creation
**Location**: Line 112-130

**Updated**:
```kotlin
Screen.AudioCall.createRoute(
    userId = pending.callerId,
    callId = pending.callId,
    appId = pending.agoraAppId,
    token = pending.agoraToken,
    channel = pending.channelId,
    role = "receiver",
    balanceTime = pending.balanceTime  // ✅ Now passed
)
```

#### c) handleCallAccepted (broadcast receiver)
**Location**: Line 264-300

**Added**:
```kotlin
val balanceTime = intent.getStringExtra("balance_time")
```

**Updated PendingCallNavigation**:
```kotlin
pendingCallNavigation.value = PendingCallNavigation(
    callerId = callerId,
    callId = callId,
    agoraAppId = agoraAppId,
    agoraToken = agoraToken ?: "",
    channelId = channelId,
    callType = callType ?: "AUDIO",
    balanceTime = balanceTime ?: ""  // ✅ Now included
)
```

#### d) handleCallNavigationFromIntent
**Location**: Line 305-343

**Added**:
```kotlin
val balanceTime = intent.getStringExtra("balance_time")
```

**Updated PendingCallNavigation**:
```kotlin
pendingCallNavigation.value = PendingCallNavigation(
    callerId = callerId,
    callId = callId,
    agoraAppId = agoraAppId,
    agoraToken = agoraToken ?: "",
    channelId = channelId,
    callType = callType ?: "AUDIO",
    balanceTime = balanceTime ?: ""  // ✅ Now included
)
```

## Expected Behavior After Fix

### For Caller Side:
1. ✅ Coin balance shows the caller's actual coin balance (e.g., "500 coins")
2. ✅ Countdown timer shows remaining call time based on their balance
3. ✅ Timer updates every second and shows warnings when time is running low

### For Receiver/Creator Side:
1. ✅ Coin balance shows the **caller's** coin balance (not the receiver's)
2. ✅ Countdown timer shows the same remaining time as the caller sees
3. ✅ Both sides see the same countdown timer in sync
4. ✅ Timer is properly displayed from the moment the call is accepted

## Testing Checklist

- [ ] **Caller Side - Audio Call**
  - [ ] Coin balance displays caller's actual balance
  - [ ] Timer countdown shows and updates
  - [ ] Timer shows correct format (MM:SS)
  - [ ] Warning appears when < 2 minutes remaining
  
- [ ] **Caller Side - Video Call**
  - [ ] Coin balance displays caller's actual balance
  - [ ] Timer countdown shows in top info bar
  - [ ] Timer updates correctly
  
- [ ] **Receiver Side - Audio Call (via in-app dialog)**
  - [ ] Accept call from FemaleHomeScreen incoming call dialog
  - [ ] Coin balance shows **caller's** balance
  - [ ] Timer countdown appears immediately
  
- [ ] **Receiver Side - Video Call (via in-app dialog)**
  - [ ] Accept call from FemaleHomeScreen incoming call dialog
  - [ ] Caller's coin balance shows in top info bar
  - [ ] Timer appears and counts down
  
- [ ] **Receiver Side - Accept from Notification**
  - [ ] Accept call from notification (IncomingCallActivity)
  - [ ] Navigate to call screen with correct balance time
  - [ ] Coin balance and timer display correctly

## Technical Notes

### Coin Balance vs Coins Spent
- **Coin Balance** (`user.coinBalance`): The user's total available coins
- **Coins Spent** (`state.coinsSpent`): Accumulating counter of coins spent during current call
- The UI now correctly shows the **coin balance**, which is what users expect to see

### Balance Time Flow
```
Backend (Initiate Call) 
  → IncomingCallDto.balanceTime 
    → FemaleHomeScreen extracts and passes to Screen.AudioCall.createRoute
      → AudioCallScreen receives balanceTime parameter
        → viewModel.setBalanceTime(balanceTime)
          → Parsed into maxCallDuration
            → Timer displays countdown
```

### Why Show Caller's Coins on Receiver Side?
- The **caller** is the one spending coins (paying for the call)
- The **receiver/creator** earns coins but doesn't spend them
- Both sides need to see the same countdown timer showing when the call will end
- The receiver needs to know how much time the caller has left (to avoid abrupt disconnections)

## Files Modified

1. ✅ `app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallScreen.kt`
2. ✅ `app/src/main/java/com/onlycare/app/presentation/screens/call/VideoCallScreen.kt`
3. ✅ `app/src/main/java/com/onlycare/app/presentation/screens/main/FemaleHomeScreen.kt`
4. ✅ `app/src/main/java/com/onlycare/app/presentation/screens/call/IncomingCallActivity.kt`
5. ✅ `app/src/main/java/com/onlycare/app/presentation/MainActivity.kt`

## Verification

✅ No linter errors
✅ All changes compile successfully
✅ Proper null safety with `?: 0` and `?: ""`
✅ Logging added for debugging balance_time flow



