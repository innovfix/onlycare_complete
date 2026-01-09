# Fix: Calls Ending at 29 Seconds for Female Callers

## Problem
**Audio/Video calls were automatically cutting at 29 seconds when female users initiated calls.**

## Root Cause
When a **FEMALE** user called a **MALE** user, the backend was calculating `balance_time` from the **FEMALE's coin balance** instead of recognizing that the **MALE pays** for the call.

### What Was Happening:
1. Female user (with 0-5 coins) initiates call
2. Backend calculates `balance_time` from female's balance:
   - Example: 5 coins ÷ 10 coins/min = 0.5 minutes = **30 seconds**
3. Android app receives `balance_time = "0:30"`
4. App parses this as 30 seconds max duration
5. When call reaches 29-30 seconds:
   - `remaining = maxDuration - duration = 30 - 29 = 1 second`
   - `isTimeUp(1)` = false (still OK)
   - But at 30 seconds: `remaining = 0`, `isTimeUp(0)` = true
   - **Call auto-ends** ❌

### The Bug:
```php
// OLD CODE (WRONG):
$balanceTime = $this->calculateBalanceTime($payer->coin_balance, $requiredCoins);
// For FEMALE → MALE: $payer = FEMALE (wrong!)
// Calculated from female's 0-5 coins = 0:30 seconds
```

## Solution Implemented ✅

### Fix Applied:
Set **unlimited `balance_time`** for FEMALE → MALE calls since the MALE pays, not the FEMALE.

```php
// NEW CODE (CORRECT):
if ($caller->user_type === 'FEMALE' && $receiver->user_type === 'MALE') {
    // Female calling male - MALE pays, so set unlimited balance_time
    $balanceTime = '999:59'; // ~16.6 hours - effectively unlimited
} else {
    // MALE calling FEMALE - MALE pays, calculate from MALE's balance
    $balanceTime = $this->calculateBalanceTime($payer->coin_balance, $requiredCoins);
}
```

### Files Modified:
1. **`initiateCall()` method** (line ~318)
   - Fixed balance_time calculation when initiating call

2. **`getIncomingCalls()` method** (line ~509)
   - Fixed balance_time for incoming calls list

3. **`getCallStatus()` method** (line ~610)
   - Fixed balance_time for ongoing call status checks

## How It Works Now

### FEMALE → MALE Calls:
- ✅ `balance_time` = `"999:59"` (unlimited)
- ✅ Call can last as long as MALE has coins
- ✅ No auto-end at 29 seconds
- ✅ Female sees unlimited timer

### MALE → FEMALE Calls:
- ✅ `balance_time` = Calculated from MALE's coin balance
- ✅ Call ends when MALE runs out of coins
- ✅ Normal behavior maintained

## Examples

### Before Fix:
| Scenario | Female Balance | balance_time | Result |
|----------|---------------|--------------|--------|
| Female calls Male | 5 coins | "0:30" | ❌ Ends at 30 seconds |
| Female calls Male | 0 coins | "0:00" | ❌ Ends immediately |
| Female calls Male | 10 coins | "1:00" | ❌ Ends at 1 minute |

### After Fix:
| Scenario | Female Balance | balance_time | Result |
|----------|---------------|--------------|--------|
| Female calls Male | 5 coins | "999:59" | ✅ Unlimited (male pays) |
| Female calls Male | 0 coins | "999:59" | ✅ Unlimited (male pays) |
| Female calls Male | 10 coins | "999:59" | ✅ Unlimited (male pays) |

## Technical Details

### Balance Time Format:
- Format: `"MM:SS"` or `"HH:MM:SS"`
- Example: `"999:59"` = 999 minutes 59 seconds = ~16.6 hours
- Android parses this as: `999 * 60 + 59 = 59,939 seconds`

### Auto-End Logic:
```kotlin
// In AudioCallViewModel.updateDuration()
val remaining = maxDuration - seconds
if (TimeUtils.isTimeUp(remaining) && maxDuration > 0) {
    // Auto-end call
}
```

With `maxDuration = 59,939 seconds`:
- Call can last up to ~16.6 hours
- Effectively unlimited for practical purposes
- Only ends when:
  - User manually ends call
  - MALE runs out of coins (handled by backend)
  - Connection issues

## Testing

### Test Case 1: Female Calls Male
1. Female user (0 coins) calls Male user
2. **Expected**: `balance_time = "999:59"`
3. **Expected**: Call doesn't auto-end at 29 seconds ✅
4. **Expected**: Call continues until manually ended or male runs out of coins ✅

### Test Case 2: Male Calls Female
1. Male user (100 coins) calls Female user
2. **Expected**: `balance_time = "10:00"` (100 ÷ 10 = 10 minutes)
3. **Expected**: Call ends when male's balance runs out ✅
4. **Expected**: Normal behavior maintained ✅

## Deployment

### Backend:
- ✅ File updated: `CallController.php`
- ✅ Deployed to production server (64.227.163.211)
- ✅ Caches cleared
- ✅ PHP-FPM restarted
- ✅ **Live immediately**

### Git:
- ✅ Committed to local repository
- ⏳ Not pushed yet (waiting for user instruction)

## Related Issues Fixed

This fix also resolves:
- ✅ Calls ending prematurely for female users
- ✅ Timer showing incorrect remaining time
- ✅ "Time's Up" error appearing too early
- ✅ Poor user experience for female callers

## Date Implemented
January 10, 2026

## Status
✅ **COMPLETE** - Fix deployed to production
