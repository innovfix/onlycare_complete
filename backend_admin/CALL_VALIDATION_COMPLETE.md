# Call API Validation Flow - Complete Guide

## ✅ Updated: Creator Verification Check Added

### Overview
The Call API now implements comprehensive validation to ensure that only verified creators with active call settings can receive paid calls.

---

## 🔒 Complete Validation Flow

When a user initiates a call, the following validations are performed **in order**:

### 1. **Request Validation**
```json
{
  "receiver_id": "required|string",
  "call_type": "required|in:AUDIO,VIDEO"
}
```
- Ensures required fields are present
- Validates call type is either AUDIO or VIDEO

**Error Code:** `VALIDATION_ERROR` (422)

---

### 2. **User Existence Check**
- Verifies both caller and receiver exist in the system
- Handles receiver_id with or without `USR_` prefix

**Error Code:** `NOT_FOUND` (404)

---

### 3. **Self-Call Prevention** ⚠️ CRITICAL
- Prevents users from calling themselves
- System integrity check

**Error Code:** `INVALID_REQUEST` (400)
```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "You cannot call yourself"
  }
}
```

---

### 4. **Blocking Check** ⚠️ CRITICAL
- Checks if the receiver has blocked the caller
- Privacy protection

**Error Code:** `USER_UNAVAILABLE` (400)
```json
{
  "error": {
    "code": "USER_UNAVAILABLE",
    "message": "User is not available"
  }
}
```

---

### 5. **Online Status Check**
- Verifies receiver is currently online
- Prevents calls to offline users

**Error Code:** `USER_OFFLINE` (400)
```json
{
  "error": {
    "code": "USER_OFFLINE",
    "message": "User is not online"
  }
}
```

---

### 6. **Busy Status Check** ⚠️ CRITICAL
- Ensures receiver is not already on another call
- Prevents concurrent calls

**Error Code:** `USER_BUSY` (400)
```json
{
  "error": {
    "code": "USER_BUSY",
    "message": "User is currently on another call"
  }
}
```

---

### 7. **Creator Verification Check** ⚠️ CRITICAL ✨ NEW
**Only verified creators can receive paid calls**

```php
if ($receiver->user_type === 'FEMALE' && !$receiver->is_verified) {
    return error('USER_NOT_VERIFIED');
}
```

**Why This Is Important:**
- Ensures only legitimate, verified creators earn money
- Protects platform integrity
- Prevents fraud
- Builds user trust

**Error Code:** `USER_NOT_VERIFIED` (400)
```json
{
  "error": {
    "code": "USER_NOT_VERIFIED",
    "message": "This creator is not verified and cannot receive calls"
  }
}
```

**Verification Criteria:**
- Creator must have `user_type = 'FEMALE'`
- Creator must have `is_verified = true`
- Admin verifies creators through admin panel

---

### 8. **Call Type Availability Check** ⚠️ CRITICAL

#### Audio Call Check:
```php
if (call_type === 'AUDIO' && !receiver->audio_call_enabled) {
    return error('CALL_NOT_AVAILABLE');
}
```

#### Video Call Check:
```php
if (call_type === 'VIDEO' && !receiver->video_call_enabled) {
    return error('CALL_NOT_AVAILABLE');
}
```

**Why Both Checks Are Required:**
1. **Verification (`is_verified`)**: Ensures creator is legitimate
2. **Call Availability (`audio_call_enabled`/`video_call_enabled`)**: Allows creator to control when they want to receive calls

**Example Scenarios:**
- ✅ Verified creator with audio_call_enabled = true → Can receive audio calls
- ❌ Verified creator with audio_call_enabled = false → Cannot receive audio calls (creator's choice)
- ❌ Unverified creator with audio_call_enabled = true → Cannot receive audio calls (not verified)
- ❌ Unverified creator with audio_call_enabled = false → Cannot receive audio calls (both conditions fail)

**Error Code:** `CALL_NOT_AVAILABLE` (400)
```json
{
  "error": {
    "code": "CALL_NOT_AVAILABLE",
    "message": "Audio call not available"
  }
}
```

---

### 9. **Coin Balance Check**
- Verifies caller has sufficient coins for at least 1 minute
- Rates from `app_settings` table:
  - Audio: 10 coins/minute
  - Video: 60 coins/minute

**Error Code:** `INSUFFICIENT_COINS` (400)
```json
{
  "error": {
    "code": "INSUFFICIENT_COINS",
    "message": "Insufficient coins for audio call. Minimum 10 coins required.",
    "details": {
      "required": 10,
      "available": 5
    }
  }
}
```

---

### 10. **Balance Time Calculation** (Good UX)
- Calculates how long the user can talk with current balance
- Displayed to user before call starts

```php
$balanceMinutes = floor($caller->coin_balance / $requiredCoins);
$balanceTime = sprintf("%d:00", $balanceMinutes);
```

**Example:**
- User has 150 coins
- Audio call rate = 10 coins/minute
- Balance time = "15:00" (15 minutes)

---

## 📊 Validation Summary Table

| # | Validation | Type | Error Code | HTTP Code |
|---|------------|------|------------|-----------|
| 1 | Request Parameters | Required | VALIDATION_ERROR | 422 |
| 2 | User Existence | Required | NOT_FOUND | 404 |
| 3 | Self-Call Prevention | CRITICAL | INVALID_REQUEST | 400 |
| 4 | Blocking Check | CRITICAL | USER_UNAVAILABLE | 400 |
| 5 | Online Status | Required | USER_OFFLINE | 400 |
| 6 | Busy Status | CRITICAL | USER_BUSY | 400 |
| 7 | Creator Verification | **CRITICAL ✨** | **USER_NOT_VERIFIED** | **400** |
| 8 | Call Type Availability | CRITICAL | CALL_NOT_AVAILABLE | 400 |
| 9 | Coin Balance | Required | INSUFFICIENT_COINS | 400 |

---

## 🔄 Complete Call Flow

### 1. **Call Initiation** (CONNECTING)
```
POST /api/calls/initiate
{
  "receiver_id": "USR_1234",
  "call_type": "AUDIO"
}
```

**Validations Applied:**
- ✓ All 9 validations above
- ✓ Creator must be verified
- ✓ Call type must be enabled

**State Changes:**
- Call created with status `CONNECTING`
- Push notification sent to receiver
- No coins deducted yet

---

### 2. **Call Acceptance** (ONGOING)
```
POST /api/calls/{callId}/accept
```

**State Changes:**
- Call status → `ONGOING`
- Both users → `is_busy = true`
- `started_at` timestamp recorded
- Call timer starts

---

### 3. **Call Rejection**
```
POST /api/calls/{callId}/reject
```

**State Changes:**
- Call status → `REJECTED`
- `ended_at` timestamp recorded
- No coins involved
- No transaction records

---

### 4. **Call End** (ENDED)
```
POST /api/calls/{callId}/end
{
  "duration": 125  // seconds
}
```

**Coin Calculation:**
```php
$minutes = ceil($duration / 60);  // Round UP
$coinsSpent = $minutes * $coin_rate_per_minute;
```

**Examples:**
| Duration | Calc | Audio (10/min) | Video (60/min) |
|----------|------|----------------|----------------|
| 30 sec | ceil(30/60) = 1 | 10 coins | 60 coins |
| 60 sec | ceil(60/60) = 1 | 10 coins | 60 coins |
| 61 sec | ceil(61/60) = 2 | 20 coins | 120 coins |
| 125 sec | ceil(125/60) = 3 | 30 coins | 180 coins |

**State Changes:**
1. Call status → `ENDED`
2. Duration recorded
3. **Caller:** Coins deducted from `coin_balance`
4. **Receiver:** Coins added to `coin_balance` AND `total_earnings`
5. Both users → `is_busy = false`
6. Two transaction records created:
   - `CALL_SPENT` for caller
   - `CALL_EARNED` for receiver

---

## 💰 Transaction Flow

### Caller Transaction
```json
{
  "id": "TXN_...",
  "user_id": "USR_caller",
  "type": "CALL_SPENT",
  "amount": 30,
  "coins": 30,
  "status": "SUCCESS",
  "reference_id": "CALL_...",
  "reference_type": "CALL"
}
```

### Receiver Transaction
```json
{
  "id": "TXN_...",
  "user_id": "USR_receiver",
  "type": "CALL_EARNED",
  "amount": 30,
  "coins": 30,
  "status": "SUCCESS",
  "reference_id": "CALL_...",
  "reference_type": "CALL"
}
```

---

## 🎯 How Creators Get Verified

### Admin Panel Process:
1. Creator registers with `user_type = 'FEMALE'`
2. Creator is initially `is_verified = false`
3. Admin reviews creator profile
4. Admin verifies creator → sets `is_verified = true`
5. Creator can now receive calls

### Database Update:
```sql
UPDATE users 
SET is_verified = true 
WHERE id = 'USR_xxx' AND user_type = 'FEMALE';
```

---

## 📱 Frontend Implementation

### Check Before Showing Call Button
```javascript
const canCall = (creator) => {
  return creator.is_verified &&           // Must be verified ✨
         creator.is_online &&              // Must be online
         !creator.is_busy &&               // Not on another call
         (creator.audio_call_enabled ||    // At least one call type
          creator.video_call_enabled);     // must be enabled
};

// Show/hide call buttons
<AudioCallButton 
  disabled={!canCall(creator) || !creator.audio_call_enabled}
/>
<VideoCallButton 
  disabled={!canCall(creator) || !creator.video_call_enabled}
/>
```

### Display Verification Badge
```javascript
{creator.is_verified && (
  <VerifiedBadge icon="✓" color="blue" />
)}
```

---

## 🧪 Testing the Complete Flow

Run the test script:
```bash
php test_call_flow.php
```

**Tests Cover:**
1. ✓ All validation scenarios
2. ✓ Call rejection (no coins involved)
3. ✓ Audio call complete flow
4. ✓ Video call complete flow
5. ✓ Time calculation edge cases (30s, 60s, 61s)
6. ✓ Coin deduction (caller)
7. ✓ Coin credit (receiver)
8. ✓ Transaction record verification
9. ✓ Call history verification

---

## ✨ What Changed

### Before:
- ❌ Unverified creators could receive calls
- ❌ Only checked `audio_call_enabled` / `video_call_enabled`
- ❌ Potential for fraud

### After:
- ✅ Only verified creators can receive calls
- ✅ Additional `is_verified` check added
- ✅ Better platform integrity
- ✅ Protection against fraud
- ✅ Builds user trust

---

## 🔍 Database Schema

### Users Table
```sql
users:
  - is_verified: BOOLEAN DEFAULT FALSE      ← Verification status
  - audio_call_enabled: BOOLEAN DEFAULT TRUE ← Audio availability
  - video_call_enabled: BOOLEAN DEFAULT TRUE ← Video availability
  - is_online: BOOLEAN DEFAULT FALSE
  - is_busy: BOOLEAN DEFAULT FALSE
  - coin_balance: INTEGER DEFAULT 0
  - total_earnings: INTEGER DEFAULT 0
```

### Calls Table
```sql
calls:
  - id: STRING (CALL_xxx)
  - caller_id: STRING (USR_xxx)
  - receiver_id: STRING (USR_xxx)
  - call_type: ENUM('AUDIO', 'VIDEO')
  - status: ENUM('CONNECTING', 'ONGOING', 'ENDED', 'REJECTED', 'MISSED')
  - duration: INTEGER (seconds)
  - coins_spent: INTEGER
  - coins_earned: INTEGER
  - coin_rate_per_minute: INTEGER
  - started_at: TIMESTAMP
  - ended_at: TIMESTAMP
```

---

## 📞 API Endpoints

### Initiate Call
```http
POST /api/calls/initiate
Authorization: Bearer {token}

{
  "receiver_id": "USR_1234567890",
  "call_type": "AUDIO"
}
```

### Accept Call
```http
POST /api/calls/{callId}/accept
Authorization: Bearer {token}
```

### Reject Call
```http
POST /api/calls/{callId}/reject
Authorization: Bearer {token}
```

### End Call
```http
POST /api/calls/{callId}/end
Authorization: Bearer {token}

{
  "duration": 125
}
```

---

## 🎉 Summary

The Call API now has **complete validation** including:

1. ✅ **9 comprehensive validation checks**
2. ✅ **Creator verification requirement**
3. ✅ **Call type availability control**
4. ✅ **Accurate time tracking** (ceil to minute)
5. ✅ **Correct coin calculations**
6. ✅ **Proper user deductions**
7. ✅ **Proper creator credits**
8. ✅ **Transaction record creation**
9. ✅ **Busy state management**

All validations are working correctly to ensure:
- **Platform integrity**
- **User protection**
- **Fair compensation**
- **Fraud prevention**
- **Great user experience**

---

**Last Updated:** November 5, 2025
**Status:** ✅ Production Ready



