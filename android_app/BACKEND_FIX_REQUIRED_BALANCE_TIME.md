# 🚨 URGENT: Backend Fix Required - Missing `balance_time` Field

## 📋 Issue Summary

**Problem**: Countdown timer and coin balance not showing on creator/receiver side during calls.

**Root Cause**: Backend is NOT sending `balance_time` field in:
1. ❌ FCM Push Notifications (incoming call notifications)
2. ❌ `/api/v1/calls/incoming` API endpoint response

**Impact**: 
- Creator cannot see how much time remains in the call
- No warning when call is about to end due to insufficient balance
- Poor user experience - call ends abruptly without warning

---

## 🔍 Evidence from Logs

### Current Backend Response (MISSING balance_time):

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
      "channel_name": "call_CALL_17639079312159"
      ⚠️ MISSING: "balance_time" field
    }
  ]
}
```

### FCM Notification Data (MISSING balance_time):

```
Data payload:
  - callId: CALL_17639079312159
  - callType: AUDIO
  - callerId: USR_17637424324851
  - callerName: User_5555
  - channelId: call_CALL_17639079312159
  - agoraAppId: 63783c2ad2724b839b1e58714bfc2629
  - agoraToken: 
  ⚠️ MISSING: balanceTime field
```

---

## ✅ REQUIRED FIXES

### Fix #1: Add `balance_time` to `/api/v1/calls/incoming` Response

**Endpoint**: `GET /api/v1/calls/incoming`

**Current Response Structure**:
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "caller_id": "string",
      "caller_name": "string",
      "caller_image": "string|null",
      "call_type": "AUDIO|VIDEO",
      "status": "string",
      "created_at": "string",
      "agora_app_id": "string",
      "agora_token": "string",
      "agora_uid": 0,
      "channel_name": "string"
    }
  ]
}
```

**REQUIRED: Add `balance_time` field**:
```json
{
  "success": true,
  "data": [
    {
      "id": "string",
      "caller_id": "string",
      "caller_name": "string",
      "caller_image": "string|null",
      "call_type": "AUDIO|VIDEO",
      "status": "string",
      "created_at": "string",
      "agora_app_id": "string",
      "agora_token": "string",
      "agora_uid": 0,
      "channel_name": "string",
      "balance_time": "25:00"  ← ADD THIS FIELD
    }
  ]
}
```

**Field Specification**:
- **Field Name**: `balance_time`
- **Data Type**: `string`
- **Format**: `"MM:SS"` or `"HH:MM:SS"` for calls longer than 1 hour
- **Example Values**:
  - `"25:00"` - 25 minutes
  - `"1:30:00"` - 1 hour 30 minutes
  - `"0:45"` - 45 seconds
  - `"90:00"` - 90 minutes (1.5 hours)

**Calculation Logic**:
```php
// Example calculation
$caller = User::find($call->caller_id);
$callerBalance = $caller->coin_balance; // e.g., 500 coins

// Audio call rate: 10 coins per minute
// Video call rate: 20 coins per minute
$callRate = ($call->call_type == 'AUDIO') ? 10 : 20;

// Calculate maximum call duration in minutes
$maxMinutes = floor($callerBalance / $callRate);

// Convert to MM:SS format
$hours = floor($maxMinutes / 60);
$minutes = $maxMinutes % 60;

if ($hours > 0) {
    $balanceTime = sprintf("%d:%02d:00", $hours, $minutes);
} else {
    $balanceTime = sprintf("%d:00", $minutes);
}

// Add to response
'balance_time' => $balanceTime
```

---

### Fix #2: Add `balanceTime` to FCM Push Notifications

**Where**: FCM notification payload when sending incoming call notification

**Current Payload**:
```php
$data = [
    'type' => 'incoming_call',
    'callId' => $call->id,
    'callerId' => $caller->id,
    'callerName' => $caller->name,
    'callerPhoto' => $caller->profile_image,
    'callType' => $call->call_type,
    'channelId' => $call->channel_name,
    'agoraAppId' => config('agora.app_id'),
    'agoraToken' => $call->agora_token,
];
```

**REQUIRED: Add `balanceTime`**:
```php
$data = [
    'type' => 'incoming_call',
    'callId' => $call->id,
    'callerId' => $caller->id,
    'callerName' => $caller->name,
    'callerPhoto' => $caller->profile_image,
    'callType' => $call->call_type,
    'channelId' => $call->channel_name,
    'agoraAppId' => config('agora.app_id'),
    'agoraToken' => $call->agora_token,
    'balanceTime' => $balanceTime,  ← ADD THIS
];
```

**Calculation**: Same as above (caller's balance ÷ call rate)

---

### Fix #3: Add `balance_time` to `/api/v1/calls/initiate` Response

**Endpoint**: `POST /api/v1/calls/initiate`

**Current Response**:
```json
{
  "success": true,
  "message": "Call initiated successfully",
  "call": {
    "id": "CALL_xxx",
    "caller_id": "USR_xxx",
    "receiver_id": "USR_xxx",
    "call_type": "AUDIO",
    "status": "CONNECTING"
  },
  "agora_app_id": "xxx",
  "agora_token": "xxx",
  "channel_name": "call_CALL_xxx"
}
```

**REQUIRED: Add `balance_time`**:
```json
{
  "success": true,
  "message": "Call initiated successfully",
  "call": {
    "id": "CALL_xxx",
    "caller_id": "USR_xxx",
    "receiver_id": "USR_xxx",
    "call_type": "AUDIO",
    "status": "CONNECTING"
  },
  "agora_app_id": "xxx",
  "agora_token": "xxx",
  "channel_name": "call_CALL_xxx",
  "balance_time": "25:00"  ← ADD THIS
}
```

---

## 📊 Balance Time Calculation Reference

### Formula:
```
balance_time_minutes = floor(caller_coin_balance / call_rate_per_minute)
```

### Call Rates:
- **Audio Call**: 10 coins/minute
- **Video Call**: 20 coins/minute

### Examples:

| Caller Balance | Call Type | Calculation | Result |
|---------------|-----------|-------------|--------|
| 500 coins | Audio | 500 ÷ 10 = 50 minutes | `"50:00"` |
| 500 coins | Video | 500 ÷ 20 = 25 minutes | `"25:00"` |
| 150 coins | Audio | 150 ÷ 10 = 15 minutes | `"15:00"` |
| 150 coins | Video | 150 ÷ 20 = 7 minutes | `"7:00"` |
| 1000 coins | Audio | 1000 ÷ 10 = 100 minutes | `"1:40:00"` |
| 0 coins | Any | 0 ÷ rate = 0 minutes | `"0:00"` |
| 5 coins | Audio | 5 ÷ 10 = 0 minutes | `"0:00"` |

### Edge Cases:

1. **Zero Balance**: Return `"0:00"`
2. **Insufficient Balance** (< 1 minute): Return `"0:00"` or reject call
3. **Very Long Duration** (> 1 hour): Use format `"HH:MM:SS"` (e.g., `"2:30:00"`)

---

## 🧪 Testing Instructions

### Test Case 1: User with Sufficient Balance

**Setup**:
```sql
UPDATE users SET coin_balance = 500 WHERE id = 'USR_17637424324851';
```

**Expected Response** (Audio Call):
```json
{
  "balance_time": "50:00"
}
```

**Expected Response** (Video Call):
```json
{
  "balance_time": "25:00"
}
```

---

### Test Case 2: User with Low Balance

**Setup**:
```sql
UPDATE users SET coin_balance = 25 WHERE id = 'USR_17637424324851';
```

**Expected Response** (Audio Call):
```json
{
  "balance_time": "2:00"
}
```

---

### Test Case 3: User with Zero Balance

**Setup**:
```sql
UPDATE users SET coin_balance = 0 WHERE id = 'USR_17637424324851';
```

**Expected Behavior**: 
- Option 1: Return `"balance_time": "0:00"` and let app reject call
- Option 2: Return error `"insufficient_balance"` (recommended)

---

## 🔧 Implementation Locations

### Files to Modify (Estimated):

1. **Incoming Calls API Controller**
   - File: `app/Http/Controllers/API/CallController.php`
   - Method: `getIncomingCalls()`
   - Action: Add `balance_time` to response

2. **Initiate Call API Controller**
   - File: `app/Http/Controllers/API/CallController.php`
   - Method: `initiateCall()`
   - Action: Add `balance_time` to response

3. **FCM Notification Service**
   - File: `app/Services/NotificationService.php` or similar
   - Method: `sendCallNotification()`
   - Action: Add `balanceTime` to FCM data payload

4. **Call Resource/Transformer** (if using)
   - File: `app/Http/Resources/CallResource.php` or similar
   - Action: Add `balance_time` field

---

## ✅ Acceptance Criteria

**Fix is complete when**:

1. ✅ `/api/v1/calls/incoming` returns `balance_time` field for all calls
2. ✅ `/api/v1/calls/initiate` returns `balance_time` in response
3. ✅ FCM notifications include `balanceTime` in data payload
4. ✅ `balance_time` format is correct: `"MM:SS"` or `"HH:MM:SS"`
5. ✅ Calculation is accurate: `caller_balance ÷ call_rate`
6. ✅ Works for both AUDIO and VIDEO call types
7. ✅ Handles edge cases (0 balance, insufficient balance)
8. ✅ Android app displays countdown timer correctly

---

## 📱 Android App Will Display

Once backend sends `balance_time`:

### Caller Side:
```
┌─────────────────────────┐
│   👤 User Name          │
│                         │
│   ⏱️  25:00             │
│   Time Remaining        │
│                         │
│   💰 500 coins          │
│                         │
│   🔇  📞  🔊           │
└─────────────────────────┘
```

### Receiver Side:
```
┌─────────────────────────┐
│   👤 Caller Name        │
│   ⏱️  25:00             │
│   💰 500 coins          │
│   (Caller's balance)    │
│                         │
│   🔇  📞  🔊           │
└─────────────────────────┘
```

### Timer Features:
- ✅ Countdown every second
- ⚠️ **Warning** when < 2 minutes (orange color)
- 🔴 **Critical** when < 1 minute (red color, pulsing)
- 🛑 **Auto-end** when time reaches 0:00

---

## 🚨 URGENT: Test User Issue

**Current Problem**: Test user `USR_17637424324851` (User_5555) has **0 coins**.

**Quick Fix for Testing**:
```sql
UPDATE users 
SET coin_balance = 500 
WHERE id = 'USR_17637424324851';
```

This will allow:
- Audio calls: 50 minutes
- Video calls: 25 minutes

---

## 📞 Contact

**If you have questions about**:
- Format of `balance_time` → Check "Field Specification" section
- Calculation logic → Check "Balance Time Calculation Reference" section
- Testing → Check "Testing Instructions" section

**Android team is ready to test** once backend changes are deployed! 🚀

---

## 📋 Checklist for Backend Team

- [ ] Add `balance_time` to `/api/v1/calls/incoming` response
- [ ] Add `balance_time` to `/api/v1/calls/initiate` response
- [ ] Add `balanceTime` to FCM notification data
- [ ] Implement balance calculation logic (balance ÷ rate)
- [ ] Handle edge cases (0 balance, insufficient balance)
- [ ] Test with user who has coins (500+ recommended)
- [ ] Add test user coins: `UPDATE users SET coin_balance = 500 WHERE id = 'USR_17637424324851'`
- [ ] Deploy changes
- [ ] Notify Android team for testing

---

**Priority**: 🔴 **CRITICAL** - Feature is non-functional without this fix

**Estimated Effort**: 2-4 hours

**Target Completion**: ASAP



