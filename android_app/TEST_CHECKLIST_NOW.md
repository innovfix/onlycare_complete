# ✅ TEST NOW - Quick Checklist

## 🎯 Backend Confirmed: balance_time Fix COMPLETE!

---

## 📋 Quick Pre-Test Checklist

### 1. Backend Must Run This SQL First:
```sql
UPDATE users 
SET coin_balance = 500 
WHERE id = 'USR_17637424324851';
```

**Verify**:
```bash
mysql -u root -p onlycare_db -e "SELECT id, name, coin_balance FROM users WHERE id = 'USR_17637424324851';"
```

**Expected Output**:
```
+----------------------+-----------+--------------+
| id                   | name      | coin_balance |
+----------------------+-----------+--------------+
| USR_17637424324851   | User_5555 | 500          |
+----------------------+-----------+--------------+
```

---

## 🧪 Simple 5-Minute Test

### Setup:
- **Device 1**: Caller (User_5555 account)
- **Device 2**: Receiver (Creator/Female account)
- **Both**: Connected to internet
- **Both**: Latest app version installed

---

### Test Steps:

#### Step 1: Start Call (Device 1 - Caller)
```
1. Login as User_5555
2. Find a creator
3. Tap "Audio Call"
4. Wait for ringing...
```

**✅ Check**: Screen shows ringing state

---

#### Step 2: Accept Call (Device 2 - Receiver)
```
1. Notification appears
2. Tap "Accept"
3. Call connects
```

**✅ Check**: Call screen appears

---

#### Step 3: Verify Timer (Both Devices)
```
Look at the screen - you should see:

┌─────────────────────┐
│   👤 User Name      │
│                     │
│   ⏱️  50:00        │ ← THIS SHOULD APPEAR
│   Time Remaining    │
│                     │
│   💰 500 coins     │ ← THIS SHOULD SHOW 500
│                     │
│   🔇  🔴  🔊      │
└─────────────────────┘
```

**✅ Check These**:
- [ ] Timer shows "50:00" (not blank, not "0:00")
- [ ] Coins show "500" (not "0")
- [ ] Timer counts down (50:00 → 49:59 → 49:58...)
- [ ] Same on BOTH devices

---

## 🎯 Pass/Fail Criteria

### ✅ TEST PASSED IF:
1. Timer displays immediately after call connects
2. Shows "50:00" on both devices
3. Shows "500 coins" on both devices  
4. Timer counts down every second
5. Both devices show same time (within 1 second)

### ❌ TEST FAILED IF:
1. No timer appears
2. Timer shows "0:00"
3. Coins show "0"
4. Timer doesn't count down

---

## 🐛 If Test Fails

### Run This Command to Get Logs:
```bash
adb logcat -c && adb logcat | grep -E "(balance|COIN BALANCE|SET BALANCE TIME|FCM MESSAGE)"
```

### Look For:

**Success Logs** (what you WANT to see):
```
✅ balance_time: "50:00"
✅ balanceTime: 50:00
✅ Parsed maxDuration: 3000 seconds
✅ COIN BALANCE: 500
✅ TIMER SHOULD BE VISIBLE NOW
```

**Failure Logs** (problems):
```
❌ balance_time: null
❌ balanceTime: EMPTY
❌ Parsed maxDuration: 0 seconds
❌ COIN BALANCE: 0
❌ TIMER WILL NOT BE DISPLAYED
```

---

## 📞 Quick Verification

### Check Backend Deployed Correctly:

**Test API endpoint**:
```bash
curl -X POST https://onlycare.in/api/v1/calls/initiate \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"receiver_id": "RECEIVER_ID", "call_type": "AUDIO"}' \
  | grep balance_time
```

**Should Output**: `"balance_time": "50:00"`

---

## 🎉 Expected Result

### What You Should See on Both Devices:

**Before Fix** (OLD):
```
┌─────────────────────┐
│   👤 User_5555      │
│                     │
│   [NO TIMER]        │ ← Missing!
│                     │
│   💰 0 coins       │ ← Wrong!
│                     │
│   🔇  🔴  🔊      │
└─────────────────────┘
```

**After Fix** (NEW - You should see this):
```
┌─────────────────────┐
│   👤 User_5555      │
│                     │
│   ⏱️  50:00        │ ← Working! ✅
│   Time Remaining    │
│                     │
│   💰 500 coins     │ ← Correct! ✅
│                     │
│   🔇  🔴  🔊      │
└─────────────────────┘
```

---

## 📊 Test Results

After testing, fill this out:

```
Test Date: _____________
Tester: _____________

Device 1 (Caller):
[ ] Timer appeared: YES / NO
[ ] Shows "50:00": YES / NO
[ ] Shows "500 coins": YES / NO
[ ] Counts down: YES / NO

Device 2 (Receiver):
[ ] Timer appeared: YES / NO
[ ] Shows "50:00": YES / NO  
[ ] Shows "500 coins": YES / NO
[ ] Counts down: YES / NO

Overall Result: PASS / FAIL

Notes:
_________________________
_________________________
```

---

## 🚀 Ready?

1. ✅ Backend deployed fix
2. ✅ Test user has 500 coins
3. ✅ 2 devices ready
4. ✅ App installed on both

**GO TEST NOW!** 📱

Expected time: **5 minutes**

---

**If it works**: 🎉 Feature complete! Document and close ticket.

**If it fails**: 📋 Share logs from the command above.



