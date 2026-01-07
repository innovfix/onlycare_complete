# 🧪 Countdown Timer Feature - Test Examples

## 📋 Document Overview

**Purpose:** Comprehensive test examples for balance_time feature  
**Date:** November 23, 2025  
**Status:** Ready for Testing

---

## 🎯 Quick Reference Table

| Balance | Call Type | Coins/Min | Available Time | balance_time | Notes |
|---------|-----------|-----------|----------------|--------------|-------|
| 10 | AUDIO | 10 | 1.0 min | `"1:00"` | Minimum for audio |
| 15 | AUDIO | 10 | 1.5 min | `"1:30"` | Fractional |
| 25 | AUDIO | 10 | 2.5 min | `"2:30"` | Fractional |
| 100 | AUDIO | 10 | 10.0 min | `"10:00"` | Even number |
| 135 | AUDIO | 10 | 13.5 min | `"13:30"` | Fractional |
| 155 | AUDIO | 10 | 15.5 min | `"15:30"` | Fractional |
| 250 | AUDIO | 10 | 25.0 min | `"25:00"` | Common case |
| 500 | AUDIO | 10 | 50.0 min | `"50:00"` | Large balance |
| 1000 | AUDIO | 10 | 100.0 min | `"1:40:00"` | Over 1 hour |
| 10000 | AUDIO | 10 | 1000.0 min | `"16:40:00"` | Very large |
| 60 | VIDEO | 60 | 1.0 min | `"1:00"` | Minimum for video |
| 90 | VIDEO | 60 | 1.5 min | `"1:30"` | Fractional |
| 300 | VIDEO | 60 | 5.0 min | `"5:00"` | Common case |
| 600 | VIDEO | 60 | 10.0 min | `"10:00"` | Common case |
| 1200 | VIDEO | 60 | 20.0 min | `"20:00"` | Large balance |
| 7200 | VIDEO | 60 | 120.0 min | `"2:00:00"` | Over 1 hour |
| 5 | AUDIO | 10 | 0.5 min | ERROR | Insufficient |
| 0 | AUDIO | 10 | 0 min | ERROR | Zero balance |

---

## 📝 Detailed Test Cases

### Test Case 1: Minimum Audio Call Balance

**Scenario:** User has exactly minimum required for audio call

**Setup:**
- User coin balance: `10 coins`
- Call type: `AUDIO`
- Rate: `10 coins/minute`

**Expected Calculation:**
```
available_time = 10 / 10 = 1.0 minutes
hours = 0
minutes = 1
seconds = 0
format = "1:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "balance_time": "1:00"
  }
}
```

**Mobile App Behavior:**
- Display countdown starting at 1:00
- Count down: 1:00 → 0:59 → 0:58 → ... → 0:01 → 0:00
- End call when reaches 0:00

---

### Test Case 2: Fractional Minutes (Audio)

**Scenario:** Balance results in fractional minutes

**Setup:**
- User coin balance: `135 coins`
- Call type: `AUDIO`
- Rate: `10 coins/minute`

**Expected Calculation:**
```
available_time = 135 / 10 = 13.5 minutes
hours = 0
minutes = 13
seconds = 30 (0.5 * 60)
format = "13:30"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "balance_time": "13:30"
  }
}
```

**Mobile App Behavior:**
- Display countdown starting at 13:30
- Count down: 13:30 → 13:29 → ... → 0:01 → 0:00
- Total available: 13 minutes 30 seconds = 810 seconds

---

### Test Case 3: Standard Audio Call

**Scenario:** Typical audio call with good balance

**Setup:**
- User coin balance: `250 coins`
- Call type: `AUDIO`
- Rate: `10 coins/minute`

**Expected Calculation:**
```
available_time = 250 / 10 = 25.0 minutes
hours = 0
minutes = 25
seconds = 0
format = "25:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "id": "CALL_1732357890",
    "call_type": "AUDIO",
    "balance_time": "25:00"
  }
}
```

**Mobile App Behavior:**
- Display countdown starting at 25:00
- Total available: 25 minutes = 1500 seconds

---

### Test Case 4: Over 1 Hour (Audio)

**Scenario:** Very large balance resulting in hours

**Setup:**
- User coin balance: `1000 coins`
- Call type: `AUDIO`
- Rate: `10 coins/minute`

**Expected Calculation:**
```
available_time = 1000 / 10 = 100.0 minutes
hours = 1 (100 / 60)
minutes = 40 (100 % 60)
seconds = 0
format = "1:40:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "balance_time": "1:40:00"
  }
}
```

**Mobile App Behavior:**
- Display countdown starting at 1:40:00
- Count down: 1:40:00 → 1:39:59 → ... → 0:00:01 → 0:00:00
- Total available: 100 minutes = 6000 seconds

---

### Test Case 5: Minimum Video Call Balance

**Scenario:** User has exactly minimum required for video call

**Setup:**
- User coin balance: `60 coins`
- Call type: `VIDEO`
- Rate: `60 coins/minute`

**Expected Calculation:**
```
available_time = 60 / 60 = 1.0 minutes
hours = 0
minutes = 1
seconds = 0
format = "1:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "call_type": "VIDEO",
    "balance_time": "1:00"
  }
}
```

---

### Test Case 6: Fractional Minutes (Video)

**Scenario:** Video call with fractional minutes

**Setup:**
- User coin balance: `90 coins`
- Call type: `VIDEO`
- Rate: `60 coins/minute`

**Expected Calculation:**
```
available_time = 90 / 60 = 1.5 minutes
hours = 0
minutes = 1
seconds = 30 (0.5 * 60)
format = "1:30"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "call_type": "VIDEO",
    "balance_time": "1:30"
  }
}
```

---

### Test Case 7: Standard Video Call

**Scenario:** Typical video call

**Setup:**
- User coin balance: `300 coins`
- Call type: `VIDEO`
- Rate: `60 coins/minute`

**Expected Calculation:**
```
available_time = 300 / 60 = 5.0 minutes
hours = 0
minutes = 5
seconds = 0
format = "5:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "call_type": "VIDEO",
    "balance_time": "5:00"
  }
}
```

---

### Test Case 8: Over 1 Hour (Video)

**Scenario:** Very large balance for video call

**Setup:**
- User coin balance: `7200 coins`
- Call type: `VIDEO`
- Rate: `60 coins/minute`

**Expected Calculation:**
```
available_time = 7200 / 60 = 120.0 minutes
hours = 2 (120 / 60)
minutes = 0 (120 % 60)
seconds = 0
format = "2:00:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "call_type": "VIDEO",
    "balance_time": "2:00:00"
  }
}
```

---

### Test Case 9: Insufficient Balance (Audio)

**Scenario:** User doesn't have minimum required coins

**Setup:**
- User coin balance: `5 coins`
- Call type: `AUDIO`
- Required minimum: `10 coins`

**Expected Behavior:**
- Call should be REJECTED
- balance_time should NOT be calculated

**Expected API Response:**
```json
{
  "success": false,
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

**Mobile App Behavior:**
- Show error message
- Redirect to recharge page

---

### Test Case 10: Zero Balance

**Scenario:** User has no coins

**Setup:**
- User coin balance: `0 coins`
- Call type: `AUDIO`

**Expected API Response:**
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_COINS",
    "message": "Insufficient coins for audio call. Minimum 10 coins required.",
    "details": {
      "required": 10,
      "available": 0
    }
  }
}
```

---

### Test Case 11: Insufficient Balance (Video)

**Scenario:** User doesn't have minimum for video call

**Setup:**
- User coin balance: `30 coins`
- Call type: `VIDEO`
- Required minimum: `60 coins`

**Expected API Response:**
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_COINS",
    "message": "Insufficient coins for video call. Minimum 60 coins required.",
    "details": {
      "required": 60,
      "available": 30
    }
  }
}
```

---

### Test Case 12: Edge Case - Exactly 59 Minutes

**Scenario:** Just under 1 hour threshold

**Setup:**
- User coin balance: `590 coins`
- Call type: `AUDIO`
- Rate: `10 coins/minute`

**Expected Calculation:**
```
available_time = 590 / 10 = 59.0 minutes
hours = 0 (59 / 60)
minutes = 59
seconds = 0
format = "59:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "balance_time": "59:00"
  }
}
```

**Note:** Still uses MM:SS format, not HH:MM:SS

---

### Test Case 13: Edge Case - Exactly 60 Minutes

**Scenario:** Exactly 1 hour

**Setup:**
- User coin balance: `600 coins`
- Call type: `AUDIO`
- Rate: `10 coins/minute`

**Expected Calculation:**
```
available_time = 600 / 10 = 60.0 minutes
hours = 1 (60 / 60)
minutes = 0 (60 % 60)
seconds = 0
format = "1:00:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "balance_time": "1:00:00"
  }
}
```

**Note:** Switches to HH:MM:SS format at exactly 1 hour

---

### Test Case 14: Very Large Balance

**Scenario:** User has massive balance

**Setup:**
- User coin balance: `10000 coins`
- Call type: `AUDIO`
- Rate: `10 coins/minute`

**Expected Calculation:**
```
available_time = 10000 / 10 = 1000.0 minutes
hours = 16 (1000 / 60)
minutes = 40 (1000 % 60)
seconds = 0
format = "16:40:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "call": {
    "balance_time": "16:40:00"
  }
}
```

**Mobile App Note:** 
- Might want to cap display at 99:59:59 or show "99+"
- Or just display the full time

---

## 🔄 Refresh Balance Time Tests

### Test Case 15: Ongoing Call - Balance Refresh

**Scenario:** User is on call and wants to check remaining time

**Setup:**
- Call started with 250 coins (25:00)
- 3 minutes have elapsed
- Coins spent so far: 30 coins
- Current balance: 250 coins (unchanged, will deduct at end)

**Request:**
```
GET /api/v1/calls/CALL_1732357890/status
```

**Expected Calculation:**
```
elapsed_time = 3 minutes
coins_spent = ceil(3) * 10 = 30 coins
remaining_coins = 250 - 30 = 220 coins
remaining_time = 220 / 10 = 22.0 minutes
format = "22:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "data": {
    "id": "CALL_1732357890",
    "status": "ONGOING",
    "duration": 0,
    "coins_spent": 0,
    "balance_time": "22:00",
    "started_at": "2025-11-23T10:30:00Z"
  }
}
```

---

### Test Case 16: Mid-Call with User Adding Coins

**Scenario:** User adds coins during call

**Setup:**
- Call started with 100 coins (10:00)
- 5 minutes elapsed
- User adds 500 coins via recharge
- New balance: 600 coins

**Request:**
```
GET /api/v1/calls/CALL_1732357890/status
```

**Expected Calculation:**
```
elapsed_time = 5 minutes
coins_spent = ceil(5) * 10 = 50 coins
remaining_coins = 600 - 50 = 550 coins
remaining_time = 550 / 10 = 55.0 minutes
format = "55:00"
```

**Expected API Response:**
```json
{
  "success": true,
  "data": {
    "balance_time": "55:00"
  }
}
```

**Mobile App Behavior:**
- Countdown timer updates from low time to 55:00
- User can continue call with extended time

---

## 🧮 Calculation Verification

### Formula:
```
available_minutes = coin_balance / coins_per_minute

hours = floor(available_minutes / 60)
minutes = floor(available_minutes % 60)
seconds = round((available_minutes - floor(available_minutes)) * 60)

if hours > 0:
    format = "H:MM:SS"
else:
    format = "M:SS"
```

### Edge Cases to Handle:
1. ✅ Seconds rounding to 60 → Add to minutes
2. ✅ Minutes rounding to 60 → Add to hours
3. ✅ Zero balance → Error response
4. ✅ Insufficient balance → Error response
5. ✅ Very large balance → Hours format
6. ✅ Fractional minutes → Proper seconds

---

## 🔨 Manual Testing Commands

### Test Audio Call (250 coins)

```bash
# Create a test user with 250 coins
curl -X POST "http://localhost:8000/api/v1/calls/initiate" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO"
  }'

# Expected balance_time: "25:00"
```

### Test Video Call (300 coins)

```bash
curl -X POST "http://localhost:8000/api/v1/calls/initiate" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "VIDEO"
  }'

# Expected balance_time: "5:00"
```

### Test Fractional Minutes (135 coins)

```bash
# First, update user's balance to 135 coins
# Then initiate audio call

# Expected balance_time: "13:30"
```

### Test Insufficient Balance (5 coins)

```bash
# Update user's balance to 5 coins
# Then try to initiate audio call

# Expected: Error response with INSUFFICIENT_COINS code
```

### Test Call Status

```bash
curl -X GET "http://localhost:8000/api/v1/calls/CALL_1732357890/status" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Expected: balance_time field showing remaining time (if ONGOING)
```

---

## 📱 Mobile App Integration Examples

### Example 1: Parse and Display Balance Time

```javascript
// Kotlin/Android
fun parseBalanceTime(balanceTime: String): Int {
    val parts = balanceTime.split(":")
    
    return when (parts.size) {
        2 -> {
            // MM:SS format
            val minutes = parts[0].toInt()
            val seconds = parts[1].toInt()
            minutes * 60 + seconds
        }
        3 -> {
            // HH:MM:SS format
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            val seconds = parts[2].toInt()
            hours * 3600 + minutes * 60 + seconds
        }
        else -> 0
    }
}

// Usage
val balanceTime = "13:30"
val totalSeconds = parseBalanceTime(balanceTime) // 810 seconds
startCountdown(totalSeconds)
```

### Example 2: Countdown Timer

```javascript
// React Native / JavaScript
import { useState, useEffect } from 'react';

function CallScreen({ balanceTime }) {
  const [seconds, setSeconds] = useState(parseBalanceTime(balanceTime));
  
  useEffect(() => {
    const interval = setInterval(() => {
      setSeconds(prev => {
        if (prev <= 0) {
          endCall();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    
    return () => clearInterval(interval);
  }, []);
  
  const formatTime = (totalSeconds) => {
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const secs = totalSeconds % 60;
    
    if (hours > 0) {
      return `${hours}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
    }
    return `${minutes}:${String(secs).padStart(2, '0')}`;
  };
  
  return (
    <div>
      <h2>Time Remaining: {formatTime(seconds)}</h2>
    </div>
  );
}
```

---

## ✅ Testing Checklist

### Backend Tests
- [ ] Test with 10 coins (audio) → "1:00"
- [ ] Test with 60 coins (video) → "1:00"
- [ ] Test with 135 coins (audio) → "13:30"
- [ ] Test with 250 coins (audio) → "25:00"
- [ ] Test with 300 coins (video) → "5:00"
- [ ] Test with 1000 coins (audio) → "1:40:00"
- [ ] Test with 7200 coins (video) → "2:00:00"
- [ ] Test with 5 coins (audio) → Error
- [ ] Test with 0 coins → Error
- [ ] Test call status endpoint during ongoing call
- [ ] Test balance refresh after adding coins mid-call

### Mobile App Tests
- [ ] Parse MM:SS format correctly
- [ ] Parse HH:MM:SS format correctly
- [ ] Display countdown timer
- [ ] Auto-end call at 0:00
- [ ] Handle balance refresh
- [ ] Show low balance warning at 1:00 or 0:30
- [ ] Test with various balance amounts

---

## 📊 Expected vs Actual Results Table

| Coins | Type | Expected | Status | Notes |
|-------|------|----------|--------|-------|
| 10 | AUDIO | "1:00" | ⏳ Pending | Minimum audio |
| 60 | VIDEO | "1:00" | ⏳ Pending | Minimum video |
| 135 | AUDIO | "13:30" | ⏳ Pending | Fractional |
| 250 | AUDIO | "25:00" | ⏳ Pending | Standard |
| 300 | VIDEO | "5:00" | ⏳ Pending | Standard |
| 1000 | AUDIO | "1:40:00" | ⏳ Pending | Hours format |
| 5 | AUDIO | Error | ⏳ Pending | Insufficient |

Fill in "Status" column with:
- ✅ Pass
- ❌ Fail
- ⏳ Pending

---

**Document Version:** 1.0  
**Last Updated:** November 23, 2025  
**Status:** Ready for Testing





