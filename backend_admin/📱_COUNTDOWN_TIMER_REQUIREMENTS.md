# 📱 Countdown Timer Feature - Backend Requirements

## 📋 Document Overview

**Feature:** Real-time countdown timer on call screen  
**Purpose:** Show caller how much time remains based on their coin balance  
**Priority:** High  
**Date:** November 23, 2025  
**Status:** ✅ IMPLEMENTED

---

## 🎯 Feature Description

When a male user (caller) initiates a call, they need to see a countdown timer showing how much call time they can afford based on their current coin balance.

**Current Behavior:**
- Call screen shows elapsed time (counting UP): 00:00 → 05:23 → 10:45
- User doesn't know when call will end due to insufficient balance

**Required Behavior:**
- Call screen shows remaining time (counting DOWN): 45:00 → 44:59 → 44:58
- User knows exactly how much time they have left
- Call auto-ends when time reaches 00:00

---

## 💰 Pricing Rules (Reminder)

| Call Type | Cost per Minute | Example |
|-----------|----------------|---------|
| **Audio Call** | 10 coins/minute | 100 coins = 10 minutes |
| **Video Call** | 60 coins/minute | 600 coins = 10 minutes |

---

## ✅ IMPLEMENTATION COMPLETE

### Changes Made:

1. **Enhanced Balance Time Calculation**
   - File: `/app/Http/Controllers/Api/CallController.php`
   - Added `calculateBalanceTime()` method
   - Handles fractional minutes (e.g., 13.5 min = "13:30")
   - Supports hours format for long calls (e.g., "1:30:00")

2. **Updated Endpoints:**
   - `POST /api/v1/calls/initiate` - Returns `balance_time` field
   - `GET /api/v1/calls/{id}/status` - Returns updated `balance_time` for ongoing calls

---

## 📝 Implementation Details

### Balance Time Calculation Algorithm

```php
private function calculateBalanceTime($coinBalance, $coinsPerMinute)
{
    // Calculate total available minutes (including fractional minutes)
    $availableMinutes = $coinBalance / $coinsPerMinute;
    
    // Extract hours, minutes, and seconds
    $hours = floor($availableMinutes / 60);
    $minutes = floor($availableMinutes % 60);
    $seconds = round(($availableMinutes - floor($availableMinutes)) * 60);
    
    // Handle edge case: if seconds round to 60, add to minutes
    if ($seconds >= 60) {
        $seconds = 0;
        $minutes++;
        if ($minutes >= 60) {
            $minutes = 0;
            $hours++;
        }
    }
    
    // Format based on duration
    if ($hours > 0) {
        return sprintf("%d:%02d:%02d", $hours, $minutes, $seconds);
    } else {
        return sprintf("%d:%02d", $minutes, $seconds);
    }
}
```

---

## 📤 API Response Examples

### Example 1: Audio Call with 250 Coins

**Request:**
```json
POST /api/v1/calls/initiate
{
  "receiver_id": "USR_1234567890",
  "call_type": "AUDIO"
}
```

**Calculation:**
```
User balance: 250 coins
Call type: AUDIO (10 coins/min)
Available time: 250 / 10 = 25 minutes
Format: "25:00"
```

**Response:**
```json
{
  "success": true,
  "message": "Call initiated successfully",
  "call": {
    "id": "CALL_1732357890",
    "caller_id": "USR_9876543210",
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO",
    "status": "CONNECTING",
    "agora_app_id": "abc123xyz",
    "agora_token": "007eJxT...",
    "channel_name": "call_CALL_1732357890",
    "balance_time": "25:00"
  },
  "agora_app_id": "abc123xyz",
  "agora_token": "007eJxT...",
  "channel_name": "call_CALL_1732357890",
  "balance_time": "25:00"
}
```

---

### Example 2: Video Call with 300 Coins

**Request:**
```json
POST /api/v1/calls/initiate
{
  "receiver_id": "USR_1234567890",
  "call_type": "VIDEO"
}
```

**Calculation:**
```
User balance: 300 coins
Call type: VIDEO (60 coins/min)
Available time: 300 / 60 = 5 minutes
Format: "5:00"
```

**Response:**
```json
{
  "success": true,
  "call": {
    "id": "CALL_1732357891",
    "call_type": "VIDEO",
    "balance_time": "5:00"
  }
}
```

---

### Example 3: Audio Call with 135 Coins (Fractional Minutes)

**Request:**
```json
POST /api/v1/calls/initiate
{
  "receiver_id": "USR_1234567890",
  "call_type": "AUDIO"
}
```

**Calculation:**
```
User balance: 135 coins
Call type: AUDIO (10 coins/min)
Available time: 135 / 10 = 13.5 minutes
Format: "13:30" (13 minutes 30 seconds)
```

**Response:**
```json
{
  "success": true,
  "call": {
    "balance_time": "13:30"
  }
}
```

---

### Example 4: Video Call with 7200 Coins (Over 1 Hour)

**Request:**
```json
POST /api/v1/calls/initiate
{
  "receiver_id": "USR_1234567890",
  "call_type": "VIDEO"
}
```

**Calculation:**
```
User balance: 7200 coins
Call type: VIDEO (60 coins/min)
Available time: 7200 / 60 = 120 minutes = 2 hours
Format: "2:00:00" (HH:MM:SS format)
```

**Response:**
```json
{
  "success": true,
  "call": {
    "balance_time": "2:00:00"
  }
}
```

---

### Example 5: Insufficient Balance

**Request:**
```json
POST /api/v1/calls/initiate
{
  "receiver_id": "USR_1234567890",
  "call_type": "AUDIO"
}
```

**Scenario:** User has 5 coins (needs minimum 10)

**Response:**
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

---

## 🔄 Refresh Balance Time During Call

### Endpoint: `GET /api/v1/calls/{call_id}/status`

**Purpose:** Allow user to refresh balance during ongoing call (e.g., if they add coins mid-call)

**Request:**
```
GET /api/v1/calls/CALL_1732357890/status
```

**Response for Ongoing Call:**
```json
{
  "success": true,
  "data": {
    "id": "CALL_1732357890",
    "status": "ONGOING",
    "duration": 180,
    "coins_spent": 30,
    "balance_time": "22:00",
    "started_at": "2025-11-23T10:30:00Z"
  }
}
```

**Calculation Logic:**
```
Elapsed: 3 minutes (180 seconds)
Coins spent: 3 × 10 = 30 coins
Original balance: 250 coins
Remaining: 250 - 30 = 220 coins
Remaining time: 220 / 10 = 22 minutes = "22:00"
```

---

## 🧪 Test Scenarios

### Test Case 1: Normal Audio Call
```
Given: User has 500 coins
When: User initiates audio call
Then: balance_time = "50:00"
```

### Test Case 2: Normal Video Call
```
Given: User has 600 coins
When: User initiates video call
Then: balance_time = "10:00"
```

### Test Case 3: Low Balance (Audio)
```
Given: User has 25 coins
When: User initiates audio call
Then: balance_time = "2:30"
```

### Test Case 4: Insufficient Balance
```
Given: User has 5 coins
When: User initiates audio call (needs 10)
Then: success = false, error message returned
```

### Test Case 5: Exact Balance for 1 Minute
```
Given: User has 10 coins (audio) or 60 coins (video)
When: User initiates call
Then: balance_time = "1:00"
```

### Test Case 6: Large Balance
```
Given: User has 10,000 coins
When: User initiates audio call
Then: balance_time = "16:40:00" (1000 minutes = 16h 40m)
```

### Test Case 7: Fractional Minutes
```
Given: User has 155 coins
When: User initiates audio call
Then: balance_time = "15:30" (155/10 = 15.5 minutes)
```

### Test Case 8: Very Small Balance
```
Given: User has 15 coins
When: User initiates audio call
Then: balance_time = "1:30" (1 minute 30 seconds)
```

---

## 📊 Expected Data Flow

```
1. Mobile app → POST /api/v1/calls/initiate
                  { receiver_id, call_type }
                  
2. Backend:
   a. Get caller's coin_balance from database
   b. Calculate available_time = balance / coins_per_minute
   c. Format as "MM:SS" or "HH:MM:SS"
   
3. Backend → Mobile app response
              { ..., balance_time: "25:00" }
              
4. Mobile app:
   - Parse "25:00" → 1500 seconds
   - Start countdown: 25:00 → 24:59 → 24:58...
   - Auto-end call when reaches 0:00
```

---

## ✅ Acceptance Criteria

- [x] `balance_time` field is always returned (never null)
- [x] Format is consistent: "MM:SS" or "HH:MM:SS"
- [x] Calculation is accurate: `balance / coins_per_minute`
- [x] Handles fractional minutes correctly (e.g., 13.5 min = "13:30")
- [x] Rejects calls with insufficient balance (< 1 minute worth)
- [x] Tested with audio calls (10 coins/min)
- [x] Tested with video calls (60 coins/min)
- [x] Added to call status endpoint for refreshing during calls
- [ ] Updated API documentation (pending)
- [ ] Tested with mobile app (pending)

---

## 🚀 Deployment Checklist

- [x] Update API endpoint: `/api/calls/initiate`
- [x] Add `balance_time` calculation logic
- [x] Add to call status endpoint
- [x] Test all scenarios (see test cases above)
- [ ] Update API documentation
- [ ] Deploy to staging environment
- [ ] Test with mobile app (Android)
- [ ] Deploy to production

---

## 📞 Integration Guide for Mobile Team

### Parsing Balance Time

```javascript
// Example balance_time values from API:
// "25:00" = 25 minutes
// "13:30" = 13 minutes 30 seconds
// "1:30:00" = 1 hour 30 minutes

function parseBalanceTime(balanceTime) {
  const parts = balanceTime.split(':');
  
  if (parts.length === 2) {
    // MM:SS format
    const minutes = parseInt(parts[0]);
    const seconds = parseInt(parts[1]);
    return minutes * 60 + seconds; // Return total seconds
  } else if (parts.length === 3) {
    // HH:MM:SS format
    const hours = parseInt(parts[0]);
    const minutes = parseInt(parts[1]);
    const seconds = parseInt(parts[2]);
    return hours * 3600 + minutes * 60 + seconds; // Return total seconds
  }
  
  return 0;
}

// Usage:
const balanceTime = "25:00";
const totalSeconds = parseBalanceTime(balanceTime); // 1500
// Start countdown from totalSeconds
```

### Countdown Implementation

```javascript
let remainingSeconds = parseBalanceTime(response.balance_time);

const countdownInterval = setInterval(() => {
  remainingSeconds--;
  
  // Update UI
  const minutes = Math.floor(remainingSeconds / 60);
  const seconds = remainingSeconds % 60;
  updateTimerDisplay(`${minutes}:${seconds.toString().padStart(2, '0')}`);
  
  // End call when time runs out
  if (remainingSeconds <= 0) {
    clearInterval(countdownInterval);
    endCall(); // Your end call function
  }
}, 1000);
```

### Refreshing Balance (Optional)

```javascript
// If user adds coins during call, refresh balance time
async function refreshBalanceTime(callId) {
  const response = await fetch(`/api/v1/calls/${callId}/status`);
  const data = await response.json();
  
  if (data.success && data.data.balance_time) {
    remainingSeconds = parseBalanceTime(data.data.balance_time);
    // Countdown will continue with updated time
  }
}
```

---

## 📎 Related Files

- **Controller:** `/app/Http/Controllers/Api/CallController.php`
- **Route:** `/routes/api.php` (line 87)
- **Model:** `/app/Models/Call.php`
- **Documentation:** This file

---

## 🔍 Testing Endpoints

### Test with cURL:

```bash
# Test audio call with 250 coins
curl -X POST "http://your-domain.com/api/v1/calls/initiate" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO"
  }'

# Expected: balance_time = "25:00"
```

```bash
# Test video call with 300 coins
curl -X POST "http://your-domain.com/api/v1/calls/initiate" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "VIDEO"
  }'

# Expected: balance_time = "5:00"
```

```bash
# Test call status (during ongoing call)
curl -X GET "http://your-domain.com/api/v1/calls/CALL_1732357890/status" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Expected: balance_time will show remaining time
```

---

## 🎯 Summary (TL;DR)

**What was done:**
1. Added `calculateBalanceTime()` method that properly formats time with hours, minutes, and seconds
2. Updated `initiateCall()` to return accurate `balance_time` 
3. Updated `getCallStatus()` to return refreshed `balance_time` during ongoing calls
4. Handles all edge cases: fractional minutes, hours format, insufficient balance

**Format:**
- Less than 1 hour: `"MM:SS"` (e.g., "25:00", "13:30")
- 1 hour or more: `"HH:MM:SS"` (e.g., "1:30:00", "2:00:00")

**Examples:**
- 250 coins, audio call → `"25:00"`
- 300 coins, video call → `"5:00"`
- 135 coins, audio call → `"13:30"` (fractional)
- 7200 coins, video call → `"2:00:00"` (hours)
- 5 coins, audio call → Error (insufficient)

**Next Steps:**
1. Test with mobile app
2. Update API documentation pages
3. Deploy to production

---

**Document Version:** 1.0  
**Last Updated:** November 23, 2025  
**Status:** ✅ Implementation Complete





