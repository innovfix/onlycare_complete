# Debug Plan: Balance Time & Coin Display Issue

## Problem
- Creator side shows "0 coins" during call
- Countdown timer not visible on creator side

## Comprehensive Logs Added

### 1. WebSocketManager (First Point of Entry)
**File**: `app/src/main/java/com/onlycare/app/websocket/WebSocketManager.kt`

**What to Look For**:
```
========================================
📥 RAW WEBSOCKET MESSAGE RECEIVED
   Event: call:incoming
   Raw JSON: {... full JSON ...}
========================================

📊 PARSED FIELDS:
   callId: xxx
   callerId: xxx
   callerName: xxx
   callType: AUDIO/VIDEO
   channelName: xxx
   agoraToken: xxx... (xxx chars)
   agoraAppId: xxx OR ❌ MISSING
   💰 balanceTime: xxx OR ❌ MISSING (BACKEND ISSUE!)
   timestamp: xxx
========================================
```

**KEY CHECK**: 
- ❓ Does the RAW JSON contain `balanceTime` field?
- ❓ Does the RAW JSON contain `agoraAppId` field?

**If Missing**: **BACKEND ISSUE** - Backend is not sending these fields via WebSocket

---

### 2. FemaleHomeViewModel (Converting WebSocket to DTO)
**File**: `app/src/main/java/com/onlycare/app/presentation/screens/main/FemaleHomeViewModel.kt`

**What to Look For**:
```
========================================
⚡ INSTANT INCOMING CALL VIA WEBSOCKET
========================================
📞 Call Details:
   Caller: User_xxx
   Caller ID: xxx
   Type: AUDIO
   Call ID: xxx
   Status: ringing (WebSocket)

🔐 Agora Details:
   Channel: xxx
   Token: ✅ xxx... (xxx chars) OR ⚠️ EMPTY
   App ID: xxx OR ⚠️ NULL/MISSING

💰 BALANCE TIME (CRITICAL):
   Value: xxx OR ❌ NULL/MISSING FROM BACKEND!
   Is Null: true/false
   Is Empty: true/false

⚡ Latency: <100ms (INSTANT!)
========================================

📦 IncomingCallDto created with:
   agoraAppId: xxx OR NULL
   balanceTime: xxx OR NULL
========================================
```

**KEY CHECK**:
- ❓ Is `event.balanceTime` NULL?
- ❓ Is `incomingCall.balanceTime` NULL?

**If NULL**: The WebSocketEvent didn't have balanceTime (see step 1)

---

### 3. FemaleHomeScreen (Accepting Call)
**File**: Already has logs

**What to Look For**:
```
========================================
📞 ACCEPTING CALL
Call ID: xxx
Caller ID: xxx
...
Balance Time from IncomingCallDto: xxx OR NULL/EMPTY
========================================

Navigating to call screen with:
  - userId: xxx
  - callId: xxx
  - appId: xxx OR EMPTY!
  - token: OK (xxx chars) OR EMPTY!
  - channel: xxx OR EMPTY!
  - balanceTime: xxx OR EMPTY!

Navigation route: audio_call/...&balanceTime=xxx
========================================
```

**KEY CHECK**:
- ❓ Is `balanceTime` EMPTY in navigation route?
- ❓ What is the actual value of `balanceTime`?

**If EMPTY**: The IncomingCallDto didn't have balanceTime

---

### 4. AudioCallScreen (Receiving Parameters)
**File**: Already has logs

**What to Look For**:
```
🔍 Screen parameters:
  - userId: xxx
  - callId: xxx
  - appId (from backend): xxx OR EMPTY
  - channel (from backend): xxx OR EMPTY
  - balanceTime (from backend): xxx OR EMPTY
  - permission granted: true/false
  🔐 Using backend-provided App ID and credentials
```

**KEY CHECK**:
- ❓ Is `balanceTime` parameter EMPTY?

**If EMPTY**: Not passed in navigation

---

### 5. AudioCallViewModel - setBalanceTime()
**File**: `app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallViewModel.kt`

**What to Look For**:
```
========================================
⏱️ SET BALANCE TIME CALLED
   Input balanceTime: xxx OR NULL
   Is null: true/false
   Is empty: true/false
   Parsed maxDuration: xxx seconds
   Formatted time: MM:SS
   Is low time: true/false

📊 STATE AFTER BALANCE TIME SET:
   state.maxCallDuration: xxx
   state.remainingTime: xxx
   state.isLowTime: true/false

✅ Balance time set successfully
✅ Call can last up to MM:SS
✅ TIMER SHOULD BE VISIBLE NOW

OR

⚠️ WARNING: No balance time available
⚠️ TIMER WILL NOT BE DISPLAYED (maxCallDuration = 0)
========================================
```

**KEY CHECK**:
- ❓ Is `maxCallDuration` = 0?
- ❓ Is input `balanceTime` NULL or EMPTY?

**If maxCallDuration = 0**: Timer won't display because of condition in AudioCallScreen line 370:
```kotlin
if (state.maxCallDuration > 0) {
    LabeledCallCountdownTimer(...)
}
```

---

### 6. AudioCallViewModel - loadUser()
**File**: `app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallViewModel.kt`

**What to Look For**:
```
========================================
👤 LOADING USER DATA
   User ID: xxx
========================================

✅ USER DATA LOADED SUCCESSFULLY
   User ID: xxx
   User Name: User_xxx
   User Username: xxx
   🪙 COIN BALANCE: xxx
   Profile Image: xxx OR EMPTY
========================================

📊 STATE AFTER USER LOAD:
   state.user?.name: User_xxx
   state.user?.coinBalance: xxx
   state.maxCallDuration: xxx
   state.remainingTime: xxx
```

**KEY CHECK**:
- ❓ Is `COIN BALANCE` = 0?
- ❓ Is user data loading successfully?

**If coinBalance = 0**: 
- Either the caller genuinely has 0 coins
- OR wrong user is being loaded (check User ID matches caller)

---

## Diagnostic Flow

### Step 1: Check WebSocket Raw Message
**Look for**: `📥 RAW WEBSOCKET MESSAGE RECEIVED`

**Questions**:
1. Does the raw JSON contain `"balanceTime": "xx:xx"` field?
2. Does the raw JSON contain `"agoraAppId": "xxx"` field?

**If NO** → **BACKEND ISSUE**: Backend is not sending these fields via WebSocket

**If YES** → Go to Step 2

---

### Step 2: Check WebSocket Parsing
**Look for**: `📊 PARSED FIELDS` in WebSocketManager

**Questions**:
1. Is `balanceTime` showing as `❌ MISSING`?
2. Is there any parsing error?

**If Missing** → Parsing issue (but unlikely now with updated code)

**If Present** → Go to Step 3

---

### Step 3: Check IncomingCallDto
**Look for**: `📦 IncomingCallDto created with` in FemaleHomeViewModel

**Questions**:
1. Is `balanceTime: NULL`?

**If NULL** → WebSocket event didn't have it (go back to Step 1)

**If Present** → Go to Step 4

---

### Step 4: Check Navigation
**Look for**: `Navigation route:` in FemaleHomeScreen

**Questions**:
1. Does the route contain `balanceTime=xxx` with actual value?
2. Or does it show `balanceTime=` (empty)?

**If Empty** → Value got lost in navigation

**If Present** → Go to Step 5

---

### Step 5: Check AudioCallScreen Reception
**Look for**: `🔍 Screen parameters` in AudioCallScreen

**Questions**:
1. Is `balanceTime (from backend): EMPTY`?

**If EMPTY** → Not received from navigation

**If Present** → Go to Step 6

---

### Step 6: Check setBalanceTime()
**Look for**: `⏱️ SET BALANCE TIME CALLED` in AudioCallViewModel

**Questions**:
1. What is the `Input balanceTime` value?
2. What is `Parsed maxDuration`?
3. Is it 0 seconds?

**If 0** → Parsing issue or empty input

**If > 0** → Timer should be visible!

---

### Step 7: Check User Data
**Look for**: `✅ USER DATA LOADED SUCCESSFULLY` in AudioCallViewModel

**Questions**:
1. What is `🪙 COIN BALANCE`?
2. Is it 0?
3. Does the User ID match the caller's ID?

**If 0** → Either:
- Caller genuinely has 0 coins (check database)
- Wrong user loaded (check User ID in logs)

---

## Expected Root Causes

### Most Likely: Backend Not Sending balanceTime via WebSocket

**Evidence to Confirm**:
- WebSocketManager shows: `❌ MISSING (BACKEND ISSUE!)`
- Raw JSON doesn't contain `balanceTime` field

**Solution**: Backend must include `balanceTime` in WebSocket `call:incoming` event

---

### Second Most Likely: Caller Has 0 Coins

**Evidence to Confirm**:
- User loads successfully
- `🪙 COIN BALANCE: 0` in logs
- User ID matches caller

**Solution**: Test with a user who has coins, or add coins to test user

---

### Less Likely: Parsing/Navigation Issue

**Evidence to Confirm**:
- WebSocket has balanceTime
- But gets lost somewhere in the flow

**Solution**: Follow logs to find where value disappears

---

## Next Steps

1. **Run the app on creator/receiver side**
2. **Accept an incoming call**
3. **Copy ALL logs** from Logcat
4. **Filter by tags**:
   - `WebSocketManager`
   - `FemaleHome`
   - `FemaleHomeScreen`
   - `AudioCallScreen`
   - `AudioCallViewModel`
5. **Share logs** to diagnose the exact point of failure

---

## Quick Check Commands

### Filter Logcat for Critical Logs:
```bash
# All balance time related logs
adb logcat | grep -i "balance"

# WebSocket raw messages
adb logcat | grep "RAW WEBSOCKET"

# Incoming call flow
adb logcat | grep -E "(INCOMING CALL|ACCEPTING CALL|SET BALANCE TIME|USER DATA)"
```

---

## Summary

The comprehensive logs will show us:
1. ✅ What the backend is sending (raw JSON)
2. ✅ If balanceTime is in the WebSocket message
3. ✅ How it's being parsed
4. ✅ How it's being passed through navigation
5. ✅ If it's reaching the ViewModel
6. ✅ What the parsed maxCallDuration is
7. ✅ What the caller's actual coin balance is

**Share the logs and we'll pinpoint the exact issue!** 🎯



