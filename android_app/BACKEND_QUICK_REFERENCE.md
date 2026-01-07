# ⚡ Quick Reference - Balance Time Feature

## 🎯 What Backend Needs to Do

**ONE SIMPLE CHANGE:** Make `balance_time` field return actual calculated time instead of `null`

---

## 📝 Formula

```python
balance_time = format_time(user_coin_balance / coins_per_minute)

# Where:
# - Audio calls: coins_per_minute = 10
# - Video calls: coins_per_minute = 60
```

---

## 📊 Quick Examples

| User Balance | Call Type | Calculation | Result |
|--------------|-----------|-------------|--------|
| 250 coins | Audio (10/min) | 250 ÷ 10 = 25 min | `"25:00"` |
| 300 coins | Video (60/min) | 300 ÷ 60 = 5 min | `"5:00"` |
| 600 coins | Audio (10/min) | 600 ÷ 10 = 60 min | `"1:00:00"` |
| 135 coins | Audio (10/min) | 135 ÷ 10 = 13.5 min | `"13:30"` |
| 5 coins | Audio (10/min) | Not enough | ❌ Error |

---

## 🔧 Implementation (Pseudocode)

```python
def calculate_balance_time(user_balance, call_type):
    # Step 1: Get pricing
    coins_per_min = 10 if call_type == "AUDIO" else 60
    
    # Step 2: Calculate minutes
    total_minutes = user_balance / coins_per_min
    
    # Step 3: Format as time string
    hours = int(total_minutes // 60)
    mins = int(total_minutes % 60)
    secs = int((total_minutes % 1) * 60)
    
    # Step 4: Return formatted string
    if hours > 0:
        return f"{hours}:{mins:02d}:{secs:02d}"  # "1:30:00"
    else:
        return f"{mins}:{secs:02d}"              # "25:00"
```

---

## ✅ Before/After Comparison

### ❌ BEFORE (Current - Wrong)
```json
{
  "success": true,
  "call": { "id": "call_123", ... },
  "agora_app_id": "abc123",
  "agora_token": "token_here",
  "channel_name": "channel_123",
  "balance_time": null           // ← NULL = Bad!
}
```

### ✅ AFTER (Required - Correct)
```json
{
  "success": true,
  "call": { "id": "call_123", ... },
  "agora_app_id": "abc123",
  "agora_token": "token_here",
  "channel_name": "channel_123",
  "balance_time": "25:00"        // ← Calculated = Good!
}
```

---

## ⚠️ Important Rules

1. **Never return `null`** - Always calculate and return a string
2. **Format:** Use `"MM:SS"` or `"HH:MM:SS"` format
3. **Validation:** Reject calls if balance < coins_per_minute (need at least 1 min)
4. **Decimals:** Handle fractional minutes (e.g., 13.5 min = "13:30")

---

## 🧪 Test These Cases

```
✅ User has 500 coins → Audio call → "50:00"
✅ User has 600 coins → Video call → "10:00"  
✅ User has 15 coins → Audio call → "1:30"
❌ User has 5 coins → Audio call → Error (need 10 minimum)
✅ User has 0 coins → Any call → Error
```

---

## 📞 Questions?

See full detailed document: `BACKEND_COUNTDOWN_TIMER_REQUIREMENTS.md`

---

**That's it!** Super simple change, huge UX improvement. 🚀



