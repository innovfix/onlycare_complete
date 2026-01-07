# 🚨 BACKEND QUICK FIX CARD

## What's Broken
❌ Countdown timer not showing on receiver/creator side  
❌ Coin balance shows 0

## Why It's Broken
1. Backend NOT sending `balance_time` field
2. Test user has 0 coins

## What We Need (3 Simple Fixes)

### Fix 1: Add to `/api/v1/calls/incoming` Response
```json
{
  "balance_time": "50:00"  ← ADD THIS
}
```

### Fix 2: Add to FCM Notification Data
```php
'balanceTime' => '50:00'  ← ADD THIS
```

### Fix 3: Give Test User Coins
```sql
UPDATE users SET coin_balance = 500 WHERE id = 'USR_17637424324851';
```

## Format
- **Type**: String
- **Format**: `"MM:SS"` (e.g., "50:00" for 50 minutes)
- **Calculation**: `caller_balance ÷ call_rate`
  - Audio: 500 coins ÷ 10 = 50 min = `"50:00"`
  - Video: 500 coins ÷ 20 = 25 min = `"25:00"`

## Example Response
```json
{
  "success": true,
  "data": [{
    "id": "CALL_xxx",
    "caller_id": "USR_xxx",
    "call_type": "AUDIO",
    "agora_app_id": "xxx",
    "agora_token": "xxx",
    "channel_name": "call_xxx",
    "balance_time": "50:00"  ← THIS
  }]
}
```

## Test
1. Add 500 coins to User_5555
2. Initiate call → Check response has `balance_time`
3. Accept call → Timer should display!

## Full Details
See: `BACKEND_FIX_REQUIRED_BALANCE_TIME.md`

---

**Priority**: 🔴 CRITICAL  
**Time**: 2-4 hours  
**Impact**: Feature completely broken without this



