# ✅ READY TO TEST - Complete Flow

## Date: November 23, 2025

## Status: 🟢 **READY FOR END-TO-END TESTING**

---

## ✅ Backend Changes: COMPLETE

### What Backend Fixed:

1. ✅ Added `balance_time` to `/api/v1/calls/incoming` response
2. ✅ Added `balanceTime` to FCM push notifications  
3. ✅ `/api/v1/calls/initiate` already had `balance_time`
4. ⏳ Test user coins to be updated (500 coins recommended)

---

## ✅ Android Changes: COMPLETE

### What We Already Implemented:

1. ✅ Extract `balanceTime` from all navigation sources:
   - From `IncomingCallActivity`
   - From `FemaleHomeScreen` 
   - From `MainActivity` intent
   - From WebSocket events

2. ✅ Parse and display countdown timer:
   - `TimeUtils.parseBalanceTime()` - converts "50:00" to seconds
   - `TimeUtils.formatTime()` - displays as MM:SS
   - `TimeUtils.isLowTime()` - detects < 2 minutes
   - `TimeUtils.isTimeUp()` - detects when time runs out

3. ✅ Display caller's coin balance:
   - Shows `state.user?.coinBalance` instead of `coinsSpent`
   - Works on both audio and video call screens

4. ✅ Timer components:
   - `CallCountdownTimer` - full timer with warnings
   - `CompactCallCountdownTimer` - compact version
   - `LabeledCallCountdownTimer` - with label
   - Color changes: Green → Orange (< 2 min) → Red (< 1 min)
   - Pulsing animation when critically low

5. ✅ Comprehensive logging:
   - WebSocket message parsing
   - Balance time extraction
   - User data loading
   - Timer state updates

---

## 🧪 TESTING INSTRUCTIONS

### Prerequisites

1. **Backend must add coins to test user first**:
```sql
UPDATE users 
SET coin_balance = 500 
WHERE id = 'USR_17637424324851';
```

2. **Verify backend is deployed** with the balance_time fix

3. **Have 2 devices ready**:
   - Device 1: Caller (User_5555 - USR_17637424324851)
   - Device 2: Receiver/Creator (your female account)

---

## 📱 Test Scenario 1: Audio Call Flow

### Step 1: Caller Initiates Call

**Device 1 (Caller)**:
1. Open app, login as User_5555
2. Navigate to a female creator's profile
3. Tap "Audio Call" button

**Expected Logs (Caller)**:
```
CallConnectingScreen: Initiating call...
API Response: balance_time: "50:00"
AudioCallScreen: balanceTime (from backend): 50:00
AudioCallViewModel: ⏱️ SET BALANCE TIME CALLED
   Input balanceTime: 50:00
   Parsed maxDuration: 3000 seconds
   ✅ TIMER SHOULD BE VISIBLE NOW
```

**Expected UI (Caller - Ringing State)**:
```
┌─────────────────────────┐
│   ← [Back]              │
│                         │
│   👤 Creator Name       │
│                         │
│   Ringing...            │
│                         │
│   🔴 [End Call]        │
└─────────────────────────┘
```

---

### Step 2: Receiver Accepts Call

**Device 2 (Receiver)**:
1. Incoming call notification appears
2. Tap "Accept"

**Expected Logs (Receiver)**:
```
📨 FCM MESSAGE RECEIVED!
✅ Data payload found:
  - balanceTime: 50:00  ← SHOULD BE PRESENT

IncomingCallService:
  Balance Time: 50:00  ← NO LONGER NULL

MainActivity:
  - Balance Time: 50:00  ← NO LONGER EMPTY

AudioCallScreen:
  - balanceTime (from backend): 50:00  ← NO LONGER EMPTY

AudioCallViewModel: ⏱️ SET BALANCE TIME CALLED
   Input balanceTime: 50:00
   Parsed maxDuration: 3000 seconds
   ✅ TIMER SHOULD BE VISIBLE NOW

AudioCallViewModel: 👤 LOADING USER DATA
   🪙 COIN BALANCE: 500  ← SHOULD BE 500, NOT 0
```

**Expected UI (Receiver - Connected State)**:
```
┌─────────────────────────┐
│   ← [Back]              │
│                         │
│   👤 User_5555          │
│                         │
│   ⏱️  50:00            │
│   Time Remaining        │
│                         │
│   💰 500 coins          │
│                         │
│   🔇  🔴  🔊          │
└─────────────────────────┘
```

---

### Step 3: Both Sides During Call

**Device 1 (Caller - Connected State)**:
```
┌─────────────────────────┐
│   ← [Back]              │
│                         │
│   👤 Creator Name       │
│                         │
│   ⏱️  49:45            │
│   Time Remaining        │
│                         │
│   💰 500 coins          │
│                         │
│   🔇  🔴  🔊          │
└─────────────────────────┘
```

**Both sides should show**:
- ✅ Same countdown timer (synchronized within 1 second)
- ✅ Caller's coin balance (500 coins)
- ✅ Timer counting down every second
- ✅ Green color (> 2 minutes remaining)

---

### Step 4: Low Time Warning Test

**Fast-forward to < 2 minutes remaining** (optional):

**Expected Behavior**:
- ⚠️ Timer turns **ORANGE** at 1:59
- Timer shows clear warning color

**Expected UI**:
```
┌─────────────────────────┐
│   👤 User Name          │
│                         │
│   ⏱️  1:45  ⚠️         │  ← Orange color
│   Time Remaining        │
│                         │
│   💰 500 coins          │
└─────────────────────────┘
```

---

### Step 5: Critical Time Warning Test

**Fast-forward to < 1 minute remaining** (optional):

**Expected Behavior**:
- 🔴 Timer turns **RED** at 0:59
- 🔔 Timer **PULSES** (fading in/out animation)
- Very noticeable warning

**Expected UI**:
```
┌─────────────────────────┐
│   👤 User Name          │
│                         │
│   ⏱️  0:45  ⚠️         │  ← RED, pulsing
│   Time Remaining        │
│                         │
│   💰 500 coins          │
└─────────────────────────┘
```

---

## 📱 Test Scenario 2: Video Call Flow

Same as Audio Call, but:
- Timer should show **25:00** (500 coins ÷ 20 = 25 minutes)
- Displayed in top info bar (compact format)

---

## 🔍 What to Check in Logs

### 1. Backend Response (Caller Side)

**Filter**: `CallConnectingViewModel` or `API`

**Look for**:
```
→ REQUEST: POST /api/v1/calls/initiate
← RESPONSE: 
  "balance_time": "50:00"  ← MUST BE PRESENT
```

### 2. FCM Notification (Receiver Side)

**Filter**: `CallNotificationService` or `FCM`

**Look for**:
```
📨 FCM MESSAGE RECEIVED!
✅ Data payload found:
  - balanceTime: 50:00  ← MUST BE PRESENT (not null)
```

### 3. Balance Time Parsing

**Filter**: `AudioCallViewModel`

**Look for**:
```
⏱️ SET BALANCE TIME CALLED
   Input balanceTime: 50:00  ← NOT NULL/EMPTY
   Parsed maxDuration: 3000 seconds  ← > 0
   ✅ TIMER SHOULD BE VISIBLE NOW
```

### 4. User Data Loading

**Filter**: `AudioCallViewModel`

**Look for**:
```
✅ USER DATA LOADED SUCCESSFULLY
   🪙 COIN BALANCE: 500  ← SHOULD BE 500, NOT 0
```

---

## ✅ Success Criteria

### Must Pass:

- [ ] **Backend Response**: `balance_time` present in API response
- [ ] **FCM Notification**: `balanceTime` present (not null)
- [ ] **Caller Side**: Timer displays "50:00" immediately after connecting
- [ ] **Receiver Side**: Timer displays "50:00" immediately after accepting
- [ ] **Both Sides**: Same timer countdown (within 1 second)
- [ ] **Coin Display**: Shows "500 coins" on both sides
- [ ] **Timer Countdown**: Decreases every second (50:00 → 49:59 → 49:58...)
- [ ] **Timer Color**: Green when > 2 minutes
- [ ] **Low Warning**: Orange when < 2 minutes (optional to wait)
- [ ] **Critical Warning**: Red + pulsing when < 1 minute (optional to wait)

---

## ❌ Failure Scenarios

### If Timer Shows "0:00" or Not Visible:

**Possible Causes**:

1. **Backend not deployed** or still sending null
   - Check API response logs
   - Verify `balance_time` field present

2. **Test user still has 0 coins**
   - Run SQL: `SELECT coin_balance FROM users WHERE id = 'USR_17637424324851'`
   - Should return 500

3. **Backend calculation wrong**
   - Check logs: What `balance_time` value is returned?
   - Should be "50:00" for 500 coins audio call

4. **Android not receiving parameter**
   - Check logs: `balanceTime (from backend): ???`
   - Should NOT be EMPTY

---

### If Coin Balance Shows "0":

**Possible Causes**:

1. **User actually has 0 coins**
   - Check database
   - Add 500 coins

2. **Wrong user loaded**
   - Check logs: Which User ID is being loaded?
   - Should match caller ID

---

## 📊 Expected Log Flow (Success)

### Caller Side:
```
1. CallConnectingViewModel: Initiating call
2. API Response: balance_time: "50:00" ✅
3. Navigate to AudioCallScreen with balanceTime="50:00"
4. AudioCallScreen: balanceTime (from backend): 50:00 ✅
5. AudioCallViewModel: setBalanceTime("50:00")
6. AudioCallViewModel: maxDuration = 3000 seconds ✅
7. AudioCallViewModel: loadUser(receiverId)
8. AudioCallViewModel: COIN BALANCE: 500 ✅
9. UI: Timer displays "50:00" ✅
10. UI: Coins display "500 coins" ✅
```

### Receiver Side:
```
1. FCM: balanceTime: "50:00" ✅
2. IncomingCallService: Balance Time: 50:00 ✅
3. Accept call
4. MainActivity: Balance Time: 50:00 ✅
5. Navigate to AudioCallScreen with balanceTime="50:00"
6. AudioCallScreen: balanceTime (from backend): 50:00 ✅
7. AudioCallViewModel: setBalanceTime("50:00")
8. AudioCallViewModel: maxDuration = 3000 seconds ✅
9. AudioCallViewModel: loadUser(callerId)
10. AudioCallViewModel: COIN BALANCE: 500 ✅
11. UI: Timer displays "50:00" ✅
12. UI: Coins display "500 coins" ✅
```

---

## 🚀 Quick Test Commands

### Check Backend is Ready:

```bash
# Test initiate call endpoint
curl -X POST https://onlycare.in/api/v1/calls/initiate \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"receiver_id": "RECEIVER_ID", "call_type": "AUDIO"}' | jq '.balance_time'

# Should output: "50:00"
```

### Check User Has Coins:

```bash
# Check test user balance
mysql -u root -p onlycare_db -e "SELECT coin_balance FROM users WHERE id = 'USR_17637424324851';"

# Should output: 500
```

---

## 📞 Support

**If Issues Occur**:

1. **Collect Logs**: 
   ```bash
   adb logcat | grep -E "(AudioCallViewModel|AudioCallScreen|CallNotificationService|balance)"
   ```

2. **Check Backend Response**:
   ```bash
   adb logcat | grep -A 20 "calls/initiate"
   ```

3. **Verify FCM Notification**:
   ```bash
   adb logcat | grep -A 20 "FCM MESSAGE RECEIVED"
   ```

---

## ✨ What Success Looks Like

### Both Devices Show:

```
┌──────────────────────────┐
│   User_5555             │
│                          │
│   ⏱️  49:45             │  ← Counting down
│   Time Remaining         │
│                          │
│   💰 500 coins          │  ← Caller's balance
│                          │
│   🔇  🔴  🔊           │
└──────────────────────────┘
```

**Timer Updates Every Second**:
- 50:00 → 49:59 → 49:58 → ... → 0:00

**Colors Change Automatically**:
- 🟢 Green: 50:00 → 2:00
- 🟠 Orange: 1:59 → 1:00  
- 🔴 Red (pulsing): 0:59 → 0:00

---

**Ready to Test!** 🎉

Once backend confirms:
1. ✅ Changes deployed
2. ✅ Test user has 500 coins

Then run Test Scenario 1 and verify all checkboxes! 📱



