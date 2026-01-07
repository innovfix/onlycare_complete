# 🔄 Balance Time Calculation - Visual Flowchart

## 📱 Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    USER INITIATES CALL                          │
│                  POST /api/calls/initiate                       │
│              { receiver_id, call_type: "AUDIO" }                │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND PROCESSING                           │
│                                                                 │
│  1. Get User: SELECT * FROM users WHERE id = :caller_id         │
│                                                                 │
│  2. Extract Balance: coin_balance = 250                         │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│              DETERMINE PRICING (Switch on call_type)            │
│                                                                 │
│  ┌─────────────────┐              ┌─────────────────┐          │
│  │  AUDIO CALL?    │              │  VIDEO CALL?    │          │
│  │ coins_per_min=10│              │ coins_per_min=60│          │
│  └─────────────────┘              └─────────────────┘          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                 VALIDATION CHECK                                │
│                                                                 │
│           Is coin_balance >= coins_per_minute?                  │
│              (Do they have at least 1 minute?)                  │
│                                                                 │
│  ┌─────────────┐                           ┌─────────────┐     │
│  │    NO       │                           │    YES      │     │
│  │  Balance=5  │                           │  Balance=250│     │
│  │  Need=10    │                           │             │     │
│  └──────┬──────┘                           └──────┬──────┘     │
└─────────┼──────────────────────────────────────────┼───────────┘
          │                                          │
          ▼                                          ▼
┌──────────────────────┐              ┌──────────────────────────┐
│   RETURN ERROR       │              │   CALCULATE TIME         │
│                      │              │                          │
│  {                   │              │  available_minutes =     │
│    "success": false, │              │    250 / 10 = 25.0       │
│    "message":        │              │                          │
│      "Insufficient   │              │  hours = 25 // 60 = 0    │
│       coins",        │              │  mins  = 25 % 60 = 25    │
│    "balance_time":   │              │  secs  = 0.0 * 60 = 0    │
│      "0:00",         │              │                          │
│    "required": 10,   │              │  Format:                 │
│    "current": 5      │              │    "25:00"               │
│  }                   │              │                          │
└──────────────────────┘              └──────────┬───────────────┘
                                                 │
                                                 ▼
                                      ┌──────────────────────────┐
                                      │   CREATE CALL RECORD     │
                                      │   + AGORA CREDENTIALS    │
                                      └──────────┬───────────────┘
                                                 │
                                                 ▼
                                      ┌──────────────────────────┐
                                      │   RETURN SUCCESS         │
                                      │                          │
                                      │  {                       │
                                      │    "success": true,      │
                                      │    "call": {...},        │
                                      │    "agora_app_id": "...",│
                                      │    "agora_token": "...", │
                                      │    "channel_name": "...",│
                                      │    "balance_time":"25:00"│
                                      │  }                       │
                                      └──────────────────────────┘
```

---

## 🧮 Calculation Examples - Step by Step

### Example 1: Audio Call - Perfect Division
```
INPUT:
  User Balance: 300 coins
  Call Type: AUDIO
  Coins Per Minute: 10

CALCULATION:
  Step 1: 300 ÷ 10 = 30.0 minutes
  Step 2: hours = 30 ÷ 60 = 0
          mins  = 30 % 60 = 30
          secs  = 0.0 * 60 = 0
  Step 3: Format = "30:00"

OUTPUT: "30:00"
```

---

### Example 2: Audio Call - Fractional Minutes
```
INPUT:
  User Balance: 135 coins
  Call Type: AUDIO
  Coins Per Minute: 10

CALCULATION:
  Step 1: 135 ÷ 10 = 13.5 minutes
  Step 2: hours = 13.5 ÷ 60 = 0
          mins  = 13.5 % 60 = 13
          secs  = 0.5 * 60 = 30
  Step 3: Format = "13:30"

OUTPUT: "13:30"
```

---

### Example 3: Video Call - Over 1 Hour
```
INPUT:
  User Balance: 7200 coins
  Call Type: VIDEO
  Coins Per Minute: 60

CALCULATION:
  Step 1: 7200 ÷ 60 = 120.0 minutes
  Step 2: hours = 120 ÷ 60 = 2
          mins  = 120 % 60 = 0
          secs  = 0.0 * 60 = 0
  Step 3: Format = "2:00:00"

OUTPUT: "2:00:00"
```

---

### Example 4: Insufficient Balance
```
INPUT:
  User Balance: 5 coins
  Call Type: AUDIO
  Coins Per Minute: 10

VALIDATION:
  5 < 10 → FAIL!

OUTPUT: Error Response
{
  "success": false,
  "message": "Insufficient coins. Need at least 10 coins.",
  "balance_time": "0:00",
  "required_coins": 10,
  "current_balance": 5
}
```

---

## 🔢 Format Decision Tree

```
                 Calculate: available_minutes
                            |
                            ▼
                    ┌───────────────┐
                    │  hours > 0?   │
                    └───────┬───────┘
                            |
              ┌─────────────┴─────────────┐
              │                           │
           YES│                           │NO
              ▼                           ▼
    ┌─────────────────────┐    ┌─────────────────────┐
    │  Use HH:MM:SS       │    │  Use MM:SS          │
    │                     │    │                     │
    │  Format:            │    │  Format:            │
    │  f"{h}:{m:02d}:     │    │  f"{m}:{s:02d}"     │
    │     {s:02d}"        │    │                     │
    │                     │    │                     │
    │  Example:           │    │  Example:           │
    │  "2:30:00"          │    │  "45:30"            │
    └─────────────────────┘    └─────────────────────┘
```

---

## 📊 Pricing Table Reference

| Call Type | Coins/Min | Example Balance | Available Time | Formatted |
|-----------|-----------|-----------------|----------------|-----------|
| Audio | 10 | 10 | 1 min | `"1:00"` |
| Audio | 10 | 50 | 5 min | `"5:00"` |
| Audio | 10 | 100 | 10 min | `"10:00"` |
| Audio | 10 | 250 | 25 min | `"25:00"` |
| Audio | 10 | 600 | 60 min | `"1:00:00"` |
| Audio | 10 | 15 | 1.5 min | `"1:30"` |
| Video | 60 | 60 | 1 min | `"1:00"` |
| Video | 60 | 300 | 5 min | `"5:00"` |
| Video | 60 | 600 | 10 min | `"10:00"` |
| Video | 60 | 3600 | 60 min | `"1:00:00"` |
| Video | 60 | 90 | 1.5 min | `"1:30"` |

---

## 🎯 Decision Matrix

```
┌──────────────────┬──────────────┬──────────────┬─────────────┐
│   User Balance   │   Call Type  │   Action     │   Result    │
├──────────────────┼──────────────┼──────────────┼─────────────┤
│   >= 10 coins    │   AUDIO      │   ALLOW      │  "MM:SS"    │
│   >= 60 coins    │   VIDEO      │   ALLOW      │  "MM:SS"    │
│   < 10 coins     │   AUDIO      │   REJECT     │  Error      │
│   < 60 coins     │   VIDEO      │   REJECT     │  Error      │
│   0 coins        │   ANY        │   REJECT     │  Error      │
└──────────────────┴──────────────┴──────────────┴─────────────┘
```

---

## 🔄 Real-Time Update Flow (Optional Future Feature)

```
DURING CALL:
┌─────────────────────────────────────────────────────────┐
│  Every Minute:                                          │
│                                                         │
│  1. Calculate coins_spent = elapsed_min * coins_per_min│
│  2. remaining_balance = original_balance - coins_spent │
│  3. remaining_minutes = remaining_balance / coins_per_ │
│  4. balance_time = format_time(remaining_minutes)      │
│                                                         │
│  Example (Audio, started with 250 coins):              │
│  ┌──────────┬────────────┬──────────┬──────────────┐  │
│  │ Minute 0 │ Spent: 0   │ Left:250 │ Time:"25:00" │  │
│  │ Minute 1 │ Spent: 10  │ Left:240 │ Time:"24:00" │  │
│  │ Minute 2 │ Spent: 20  │ Left:230 │ Time:"23:00" │  │
│  │ Minute 3 │ Spent: 30  │ Left:220 │ Time:"22:00" │  │
│  │   ...    │    ...     │   ...    │     ...      │  │
│  │ Minute 25│ Spent: 250 │ Left: 0  │ Time:"0:00"  │  │
│  └──────────┴────────────┴──────────┴──────────────┘  │
│                                                         │
│  When balance_time reaches "0:00" → END CALL           │
└─────────────────────────────────────────────────────────┘
```

---

## ⚙️ Code Template (Python/Django Example)

```python
from math import floor

def calculate_balance_time(user_balance: int, call_type: str) -> dict:
    """
    Calculate available call time based on user balance.
    
    Args:
        user_balance: User's coin balance
        call_type: "AUDIO" or "VIDEO"
    
    Returns:
        dict with success status and balance_time
    """
    # Step 1: Determine pricing
    coins_per_minute = 10 if call_type == "AUDIO" else 60
    
    # Step 2: Validate minimum balance
    if user_balance < coins_per_minute:
        return {
            "success": False,
            "message": f"Insufficient coins. Need at least {coins_per_minute}.",
            "balance_time": "0:00",
            "required_coins": coins_per_minute,
            "current_balance": user_balance
        }
    
    # Step 3: Calculate available time
    total_minutes = user_balance / coins_per_minute
    hours = floor(total_minutes / 60)
    minutes = floor(total_minutes % 60)
    seconds = floor((total_minutes % 1) * 60)
    
    # Step 4: Format time string
    if hours > 0:
        balance_time = f"{hours}:{minutes:02d}:{seconds:02d}"
    else:
        balance_time = f"{minutes}:{seconds:02d}"
    
    # Step 5: Return success
    return {
        "success": True,
        "balance_time": balance_time
    }


# Usage in your initiate_call endpoint:
def initiate_call(request):
    caller = request.user
    receiver_id = request.data.get('receiver_id')
    call_type = request.data.get('call_type')
    
    # Calculate balance time
    result = calculate_balance_time(caller.coin_balance, call_type)
    
    if not result["success"]:
        return Response(result, status=400)
    
    # Create call record, generate Agora credentials, etc.
    call = Call.objects.create(...)
    agora_creds = generate_agora_credentials(call.id)
    
    return Response({
        "success": True,
        "call": CallSerializer(call).data,
        "agora_app_id": agora_creds["app_id"],
        "agora_token": agora_creds["token"],
        "channel_name": agora_creds["channel"],
        "balance_time": result["balance_time"]  # ✅ CRITICAL
    })
```

---

## 📝 SQL Query Example

```sql
-- Get user balance and calculate available time
SELECT 
    u.id,
    u.coin_balance,
    CASE 
        WHEN :call_type = 'AUDIO' THEN 
            FLOOR(u.coin_balance / 10)  -- Minutes for audio
        WHEN :call_type = 'VIDEO' THEN 
            FLOOR(u.coin_balance / 60)  -- Minutes for video
    END as available_minutes,
    CASE 
        WHEN :call_type = 'AUDIO' THEN 
            CONCAT(
                FLOOR(u.coin_balance / 10), 
                ':',
                LPAD(FLOOR((u.coin_balance % 10) * 6), 2, '0')
            )
        WHEN :call_type = 'VIDEO' THEN 
            CONCAT(
                FLOOR(u.coin_balance / 60), 
                ':',
                LPAD(FLOOR((u.coin_balance % 60) * 1), 2, '0')
            )
    END as balance_time
FROM users u
WHERE u.id = :caller_id;
```

---

## ✅ Final Checklist

```
Before deploying, verify:

[ ] balance_time is NEVER null
[ ] Format is consistent (MM:SS or HH:MM:SS)
[ ] Calculations are accurate
[ ] Validation rejects insufficient balance
[ ] Tested with 10+ scenarios
[ ] API docs are updated
[ ] Mobile team has been notified
[ ] Staging environment tested
[ ] Ready for production
```

---

**Questions?** See `BACKEND_COUNTDOWN_TIMER_REQUIREMENTS.md` for full details.



