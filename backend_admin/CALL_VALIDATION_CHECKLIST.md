# ✅ Call API Validation Checklist

## 🎯 Question: Should creators be verified AND have active audio/video call settings?

### **Answer: YES! ✅**

Both conditions are now required:

1. **Creator MUST be verified** (`is_verified = true`)
   - Added in: `CallController.php` line 136-144
   - Prevents unverified users from earning money
   - Ensures platform integrity

2. **Call type MUST be enabled** 
   - Audio: `audio_call_enabled = true`
   - Video: `video_call_enabled = true`
   - Allows creators to control when they receive calls

---

## 📋 Complete Validation Checklist

When a call is initiated (`POST /api/calls/initiate`), the following validations occur:

### ✅ 1. Request Parameters
- [x] `receiver_id` is required
- [x] `call_type` is required (AUDIO or VIDEO)
- **Error:** `VALIDATION_ERROR` (422)

### ✅ 2. User Existence
- [x] Caller exists
- [x] Receiver exists
- **Error:** `NOT_FOUND` (404)

### ✅ 3. Self-Call Prevention
- [x] Caller cannot call themselves
- **Error:** `INVALID_REQUEST` (400)

### ✅ 4. Blocking Check
- [x] Receiver has not blocked the caller
- **Error:** `USER_UNAVAILABLE` (400)

### ✅ 5. Online Status
- [x] Receiver is online
- **Error:** `USER_OFFLINE` (400)

### ✅ 6. Busy Status
- [x] Receiver is not on another call
- **Error:** `USER_BUSY` (400)

### ✅ 7. Creator Verification ⭐ NEW
- [x] If receiver is FEMALE (creator), they must be verified
- [x] Check: `user_type === 'FEMALE' && is_verified === true`
- **Error:** `USER_NOT_VERIFIED` (400)
- **Code Location:** `CallController.php:136-144`

```php
// Only verified creators can receive paid calls
if ($receiver->user_type === 'FEMALE' && !$receiver->is_verified) {
    return response()->json([
        'success' => false,
        'error' => [
            'code' => 'USER_NOT_VERIFIED',
            'message' => 'This creator is not verified and cannot receive calls'
        ]
    ], 400);
}
```

### ✅ 8. Call Type Availability
- [x] For AUDIO: `audio_call_enabled === true`
- [x] For VIDEO: `video_call_enabled === true`
- **Error:** `CALL_NOT_AVAILABLE` (400)

### ✅ 9. Coin Balance
- [x] Caller has sufficient coins (at least 1 minute)
- [x] Audio: 10 coins/minute
- [x] Video: 60 coins/minute
- **Error:** `INSUFFICIENT_COINS` (400)

---

## 🔐 Why Both Checks Are Required

### Scenario Matrix:

| is_verified | audio_call_enabled | Can Receive Audio? | Reason |
|-------------|-------------------|-------------------|---------|
| ✅ true | ✅ true | ✅ **YES** | All conditions met |
| ✅ true | ❌ false | ❌ NO | Creator disabled audio |
| ❌ false | ✅ true | ❌ NO | Not verified (security) |
| ❌ false | ❌ false | ❌ NO | Both conditions fail |

### Real-World Examples:

**Example 1: Active Verified Creator** ✅
```json
{
  "user_type": "FEMALE",
  "is_verified": true,
  "audio_call_enabled": true,
  "video_call_enabled": true
}
```
→ Can receive both audio and video calls

**Example 2: Verified But Taking Break** ⚠️
```json
{
  "user_type": "FEMALE",
  "is_verified": true,
  "audio_call_enabled": false,
  "video_call_enabled": false
}
```
→ Cannot receive calls (creator's choice to take a break)

**Example 3: New Unverified Creator** ❌
```json
{
  "user_type": "FEMALE",
  "is_verified": false,
  "audio_call_enabled": true,
  "video_call_enabled": true
}
```
→ Cannot receive calls (not verified by admin yet)

**Example 4: Regular Male User** ✅
```json
{
  "user_type": "MALE",
  "is_verified": false,
  "audio_call_enabled": false,
  "video_call_enabled": false
}
```
→ Verification check doesn't apply to male users (they are callers, not receivers)

---

## 💰 Coin Flow (Time-Based Deduction)

### When Call Ends:

```php
// Calculate minutes (round UP)
$minutes = ceil($duration / 60);
$coinsSpent = $minutes * $coin_rate_per_minute;
```

### Example Calculations:

| Duration | Minutes (ceil) | Audio Cost | Video Cost |
|----------|---------------|------------|------------|
| 1 sec | 1 | 10 coins | 60 coins |
| 30 sec | 1 | 10 coins | 60 coins |
| 60 sec | 1 | 10 coins | 60 coins |
| 61 sec | 2 | 20 coins | 120 coins |
| 90 sec | 2 | 20 coins | 120 coins |
| 125 sec | 3 | 30 coins | 180 coins |
| 300 sec | 5 | 50 coins | 300 coins |

### Money Flow:

1. **Caller:**
   - `coin_balance` decreased by `coinsSpent`
   - Transaction: `CALL_SPENT`

2. **Creator (Receiver):**
   - `coin_balance` increased by `coinsSpent`
   - `total_earnings` increased by `coinsSpent`
   - Transaction: `CALL_EARNED`

3. **Both Users:**
   - `is_busy` set to `false`

---

## 📊 Validation Flow Diagram

```
User initiates call
    ↓
[1] Valid parameters? → NO → Error 422
    ↓ YES
[2] Users exist? → NO → Error 404
    ↓ YES
[3] Self-call? → YES → Error 400
    ↓ NO
[4] Blocked? → YES → Error 400
    ↓ NO
[5] Online? → NO → Error 400
    ↓ YES
[6] Busy? → YES → Error 400
    ↓ NO
[7] Verified? (if FEMALE) → NO → Error 400 ⭐ NEW
    ↓ YES
[8] Call type enabled? → NO → Error 400
    ↓ YES
[9] Sufficient coins? → NO → Error 400
    ↓ YES
Create call (CONNECTING)
    ↓
Send push notification
    ↓
Return success ✅
```

---

## 🧪 How to Test

### Test 1: Verified Creator with Calls Enabled
```bash
POST /api/calls/initiate
{
  "receiver_id": "USR_123",  # Verified creator
  "call_type": "AUDIO"
}
```
**Expected:** ✅ Success (200)

### Test 2: Unverified Creator
```bash
POST /api/calls/initiate
{
  "receiver_id": "USR_456",  # Unverified creator
  "call_type": "AUDIO"
}
```
**Expected:** ❌ `USER_NOT_VERIFIED` (400)

### Test 3: Verified Creator with Audio Disabled
```bash
POST /api/calls/initiate
{
  "receiver_id": "USR_123",  # audio_call_enabled = false
  "call_type": "AUDIO"
}
```
**Expected:** ❌ `CALL_NOT_AVAILABLE` (400)

### Test 4: Self-Call
```bash
POST /api/calls/initiate
{
  "receiver_id": "USR_789",  # Same as caller
  "call_type": "AUDIO"
}
```
**Expected:** ❌ `INVALID_REQUEST` (400)

---

## 🔧 Admin Panel: How to Verify Creators

### Step 1: View Users
1. Go to Admin Panel → Users
2. Filter by `user_type = FEMALE`

### Step 2: Verify Creator
1. Click on creator profile
2. Check their details, KYC documents
3. Click "Verify" button
4. Sets `is_verified = true`

### Database Query (if needed):
```sql
-- Verify a creator
UPDATE users 
SET is_verified = 1 
WHERE id = 'USR_xxx' AND user_type = 'FEMALE';

-- Unverify a creator
UPDATE users 
SET is_verified = 0 
WHERE id = 'USR_xxx';

-- Check verification status
SELECT id, name, user_type, is_verified, 
       audio_call_enabled, video_call_enabled
FROM users 
WHERE user_type = 'FEMALE';
```

---

## 📱 Frontend Recommendations

### Display Verification Badge
```javascript
{creator.is_verified && (
  <VerifiedBadge 
    icon="✓" 
    text="Verified" 
    color="blue" 
  />
)}
```

### Disable Call Button Logic
```javascript
const canAudioCall = 
  creator.is_verified && 
  creator.is_online && 
  !creator.is_busy && 
  creator.audio_call_enabled;

const canVideoCall = 
  creator.is_verified && 
  creator.is_online && 
  !creator.is_busy && 
  creator.video_call_enabled;

<AudioCallButton disabled={!canAudioCall} />
<VideoCallButton disabled={!canVideoCall} />
```

### Show Helpful Messages
```javascript
if (!creator.is_verified) {
  return <Message>This creator is not verified yet</Message>;
}

if (!creator.audio_call_enabled) {
  return <Message>Audio calls are currently unavailable</Message>;
}

if (!creator.is_online) {
  return <Message>Creator is offline</Message>;
}

if (creator.is_busy) {
  return <Message>Creator is on another call</Message>;
}
```

---

## ✅ Summary

### What's Required for a Successful Call?

**For the Caller:**
- ✅ Has sufficient coin balance
- ✅ Not calling themselves
- ✅ Not blocked by receiver

**For the Creator (Receiver):**
- ✅ Is verified (`is_verified = true`)
- ✅ Is online (`is_online = true`)
- ✅ Not busy (`is_busy = false`)
- ✅ Has call type enabled (`audio_call_enabled` or `video_call_enabled`)
- ✅ Hasn't blocked the caller

### Coin Calculation:
- ✅ Time-based (per minute)
- ✅ Rounded UP (`ceil`)
- ✅ Caller pays
- ✅ Creator earns the same amount

### Transaction Records:
- ✅ `CALL_SPENT` for caller
- ✅ `CALL_EARNED` for creator
- ✅ Both linked to call ID

---

## 🎉 All Validations Are Working!

The Call API now has **comprehensive validation** ensuring:
- **Only verified creators can earn**
- **Creators control their availability**
- **Fair coin calculations**
- **Accurate time tracking**
- **Proper money flow**
- **Platform integrity**

**Status:** ✅ Production Ready
**Last Updated:** November 5, 2025



