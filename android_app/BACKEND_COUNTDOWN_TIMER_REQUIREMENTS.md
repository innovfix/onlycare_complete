# 📱 Countdown Timer Feature - Backend Requirements

## 📋 Document Overview
**Feature:** Real-time countdown timer on call screen  
**Purpose:** Show caller how much time remains based on their coin balance  
**Priority:** High  
**Date:** November 23, 2025  
**For:** Backend Development Team

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

## 🔧 Current API Response (Initiate Call)

### Endpoint: `POST /api/calls/initiate`

**Current Response:**
```json
{
  "success": true,
  "message": "Call initiated successfully",
  "call": {
    "id": "call_12345",
    "caller_id": "user_789",
    "receiver_id": "user_456",
    "call_type": "AUDIO",
    "status": "CONNECTING"
  },
  "agora_app_id": "abc123xyz",
  "agora_token": "token_string_here",
  "channel_name": "channel_12345",
  "balance_time": null    // ⚠️ CURRENTLY NULL OR MISSING
}
```

---

## ✅ Required API Changes

### 1. **`balance_time` Field - MUST BE PROVIDED**

**Field Name:** `balance_time`  
**Type:** String  
**Format:** `"MM:SS"` or `"HH:MM:SS"`  
**Required:** Yes (cannot be null or empty)

**Examples:**
```json
"balance_time": "45:00"      // 45 minutes, 0 seconds
"balance_time": "1:30:00"    // 1 hour, 30 minutes, 0 seconds
"balance_time": "5:30"       // 5 minutes, 30 seconds
"balance_time": "0:45"       // 0 minutes, 45 seconds (less than 1 min)
```

---

## 🧮 Calculation Logic (Backend Must Implement)

### Formula:
```
available_time_minutes = user_coin_balance / coins_per_minute
balance_time = format_as_MM_SS(available_time_minutes)
```

### Detailed Steps:

#### **Step 1: Get User's Current Coin Balance**
```sql
SELECT coin_balance FROM users WHERE id = :caller_id
```
Example: `coin_balance = 250`

#### **Step 2: Determine Coins Per Minute**
```python
if call_type == "AUDIO":
    coins_per_minute = 10
elif call_type == "VIDEO":
    coins_per_minute = 60
```

#### **Step 3: Calculate Available Minutes**
```python
available_minutes = user_coin_balance / coins_per_minute
```

**Examples:**
- Audio call: `250 / 10 = 25 minutes`
- Video call: `250 / 60 = 4.16 minutes` (4 min 10 sec)

#### **Step 4: Format as String**
```python
def format_balance_time(minutes):
    hours = int(minutes // 60)
    mins = int(minutes % 60)
    secs = int((minutes % 1) * 60)
    
    if hours > 0:
        return f"{hours}:{mins:02d}:{secs:02d}"
    else:
        return f"{mins}:{secs:02d}"

# Examples:
# 25.0 minutes → "25:00"
# 4.16 minutes → "4:10"
# 90.5 minutes → "1:30:30"
```

---

## 📝 Complete Implementation Examples

### Example 1: Audio Call with 250 Coins

**Input:**
```json
{
  "receiver_id": "user_456",
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
  "call": { "id": "call_12345", ... },
  "agora_app_id": "abc123xyz",
  "agora_token": "token_string",
  "channel_name": "channel_12345",
  "balance_time": "25:00"    // ✅ NEW
}
```

---

### Example 2: Video Call with 300 Coins

**Input:**
```json
{
  "receiver_id": "user_456",
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
  "call": { "id": "call_12345", ... },
  "agora_app_id": "abc123xyz",
  "agora_token": "token_string",
  "channel_name": "channel_12345",
  "balance_time": "5:00"    // ✅ NEW
}
```

---

### Example 3: Audio Call with 135 Coins (Non-round Number)

**Input:**
```json
{
  "receiver_id": "user_456",
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
  "call": { "id": "call_12345", ... },
  "balance_time": "13:30"    // ✅ 13 minutes 30 seconds
}
```

---

### Example 4: Video Call with 1200 Coins (Over 1 Hour)

**Input:**
```json
{
  "receiver_id": "user_456",
  "call_type": "VIDEO"
}
```

**Calculation:**
```
User balance: 1200 coins
Call type: VIDEO (60 coins/min)
Available time: 1200 / 60 = 20 minutes
Format: "20:00"
```

**Alternative:** If over 60 minutes:
```
User balance: 7200 coins
Available time: 7200 / 60 = 120 minutes = 2 hours
Format: "2:00:00" (HH:MM:SS format)
```

**Response:**
```json
{
  "success": true,
  "call": { "id": "call_12345", ... },
  "balance_time": "2:00:00"    // ✅ 2 hours format
}
```

---

## ⚠️ Edge Cases & Error Handling

### Case 1: Insufficient Balance

**Scenario:** User has 5 coins, tries audio call (needs 10 coins minimum)

**Current Behavior:** Call initiates anyway (wrong!)

**Required Behavior:** Reject call with error

**Response:**
```json
{
  "success": false,
  "message": "Insufficient coins",
  "balance_time": "0:00",
  "required_coins": 10,
  "current_balance": 5
}
```

---

### Case 2: Zero Balance

**Scenario:** User has 0 coins

**Response:**
```json
{
  "success": false,
  "message": "Insufficient coins. Please recharge your wallet.",
  "balance_time": "0:00",
  "required_coins": 10,    // or 60 for video
  "current_balance": 0
}
```

---

### Case 3: Partial Minute Available

**Scenario:** User has 15 coins for audio call

**Calculation:**
```
15 coins / 10 coins per min = 1.5 minutes
Format: "1:30" (1 minute 30 seconds)
```

**Response:**
```json
{
  "success": true,
  "balance_time": "1:30"    // ✅ Allow partial minutes
}
```

---

### Case 4: Very Long Call Duration

**Scenario:** User has 100,000 coins for audio call

**Calculation:**
```
100,000 / 10 = 10,000 minutes = 166.67 hours = ~7 days
Format: "166:40:00" or "10000:00"
```

**Recommendation:** Use HH:MM:SS format or cap display

---

## 🔄 Additional API Requirements (Optional but Recommended)

### 1. Get Call Status (Refresh Balance Time)

**Endpoint:** `GET /api/calls/:call_id/status`

**Purpose:** Allow user to refresh balance during call (if they add coins mid-call)

**Response:**
```json
{
  "success": true,
  "call": {
    "id": "call_12345",
    "status": "ONGOING",
    "duration": 180,           // 3 minutes elapsed
    "coins_spent": 30,         // 3 * 10 = 30 coins
    "balance_time": "22:00"    // Recalculated: 250 - 30 = 220 coins = 22 min
  }
}
```

**Calculation Logic:**
```python
elapsed_minutes = call_duration_seconds / 60
coins_spent = elapsed_minutes * coins_per_minute
remaining_balance = user_balance - coins_spent
remaining_minutes = remaining_balance / coins_per_minute
balance_time = format_balance_time(remaining_minutes)
```

---

### 2. Minimum Balance Validation

**Recommendation:** Add validation in initiate call endpoint

```python
# Before creating call
minimum_required = coins_per_minute  # At least 1 minute

if user_balance < minimum_required:
    return error_response(
        message=f"Insufficient coins. Need at least {minimum_required} coins.",
        required=minimum_required,
        current=user_balance
    )
```

---

## 🧪 Testing Scenarios

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

---

## 🔍 Database Schema Check

**Please verify your `users` table has:**

```sql
CREATE TABLE users (
    id VARCHAR PRIMARY KEY,
    coin_balance INTEGER NOT NULL DEFAULT 0,
    ...
);
```

**Index recommendation:**
```sql
CREATE INDEX idx_users_coin_balance ON users(coin_balance);
```

---

## 📊 Expected Data Flow

```
1. Mobile app → POST /api/calls/initiate
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

## 🐛 Common Pitfalls to Avoid

### ❌ DON'T:
1. Return `null` or empty string for `balance_time`
2. Return negative values like `"-5:00"`
3. Use inconsistent format (sometimes "MM:SS", sometimes seconds)
4. Forget to handle decimal minutes (e.g., 13.5 minutes)
5. Allow calls with 0 or insufficient balance

### ✅ DO:
1. Always return `balance_time` as a formatted string
2. Return `"0:00"` for zero/insufficient balance (with error)
3. Consistently use "MM:SS" or "HH:MM:SS" format
4. Round fractional seconds (not fractional minutes)
5. Validate minimum balance before creating call

---

## 📞 Implementation Pseudocode

```python
def initiate_call(caller_id, receiver_id, call_type):
    # 1. Get caller's balance
    user = get_user_by_id(caller_id)
    coin_balance = user.coin_balance
    
    # 2. Determine pricing
    if call_type == "AUDIO":
        coins_per_minute = 10
    elif call_type == "VIDEO":
        coins_per_minute = 60
    else:
        return error("Invalid call type")
    
    # 3. Validate minimum balance (at least 1 minute)
    if coin_balance < coins_per_minute:
        return {
            "success": false,
            "message": "Insufficient coins",
            "balance_time": "0:00",
            "required_coins": coins_per_minute,
            "current_balance": coin_balance
        }
    
    # 4. Calculate available time
    available_minutes = coin_balance / coins_per_minute
    hours = int(available_minutes // 60)
    minutes = int(available_minutes % 60)
    seconds = int((available_minutes % 1) * 60)
    
    # 5. Format balance_time
    if hours > 0:
        balance_time = f"{hours}:{minutes:02d}:{seconds:02d}"
    else:
        balance_time = f"{minutes}:{seconds:02d}"
    
    # 6. Create call record
    call = create_call_record(caller_id, receiver_id, call_type)
    
    # 7. Generate Agora credentials
    agora_credentials = generate_agora_credentials(call.id)
    
    # 8. Return response
    return {
        "success": true,
        "call": call,
        "agora_app_id": agora_credentials.app_id,
        "agora_token": agora_credentials.token,
        "channel_name": agora_credentials.channel,
        "balance_time": balance_time    # ✅ CRITICAL
    }
```

---

## ✅ Acceptance Criteria

Before marking this as complete, please verify:

- [ ] `balance_time` field is always returned (never null)
- [ ] Format is consistent: "MM:SS" or "HH:MM:SS"
- [ ] Calculation is accurate: `balance / coins_per_minute`
- [ ] Handles fractional minutes correctly (e.g., 13.5 min = "13:30")
- [ ] Rejects calls with insufficient balance (< 1 minute worth)
- [ ] Tested with audio calls (10 coins/min)
- [ ] Tested with video calls (60 coins/min)
- [ ] Tested with edge cases (0 coins, huge balance, etc.)
- [ ] Updated API documentation
- [ ] Database indexes are optimized

---

## 🚀 Deployment Checklist

1. ✅ Update API endpoint: `/api/calls/initiate`
2. ✅ Add `balance_time` calculation logic
3. ✅ Add minimum balance validation
4. ✅ Test all scenarios (see Testing Scenarios section)
5. ✅ Update API documentation
6. ✅ Deploy to staging environment
7. ✅ Test with mobile app (Android)
8. ✅ Deploy to production

---

## 📞 Questions or Issues?

**Contact:** [Your Name / Mobile Team Lead]  
**Email:** [your-email@company.com]  
**Slack:** #backend-development

**Expected Timeline:** Please implement within [X days/weeks]

---

## 📎 Related Documents

- API Documentation: [Link to your API docs]
- Pricing Rules Document: [Link if exists]
- Database Schema: [Link to schema docs]
- Mobile App Call Flow: See `CALL_FLOW_DIAGRAM.md` in mobile repo

---

**Document Version:** 1.0  
**Last Updated:** November 23, 2025  
**Status:** Pending Implementation

---

## 🎯 Summary (TL;DR for Backend Team)

**What to do:**
1. Make `balance_time` field return actual calculated time (not null)
2. Formula: `balance_time = format_time(user_balance / coins_per_minute)`
3. Format: `"MM:SS"` (e.g., "25:00" for 25 minutes)
4. Add validation: Reject if balance < minimum (1 minute worth)

**Examples:**
- User has 250 coins, audio call → `"25:00"`
- User has 300 coins, video call → `"5:00"`
- User has 15 coins, audio call → `"1:30"`
- User has 5 coins, audio call → Error (need minimum 10)

**That's it!** Simple change, big UX improvement. 🚀



