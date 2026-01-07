# 🚀 Countdown Timer - Quick Start Guide

## ✅ Implementation Status: COMPLETE

The countdown timer feature is **fully implemented** and ready to use!

---

## 📱 For Mobile Developers

### What You Get from the API

When you call `POST /api/v1/calls/initiate`, the response now includes:

```json
{
  "success": true,
  "call": {
    "id": "CALL_1234567890",
    "balance_time": "25:00"  // ⭐ THIS IS NEW!
  },
  "balance_time": "25:00"  // ⭐ Also here for convenience
}
```

### What `balance_time` Means

- Shows how much time user can afford based on their coin balance
- Format: `"MM:SS"` (e.g., "25:00") or `"HH:MM:SS"` (e.g., "1:40:00")
- Ready to parse and display as countdown timer

---

## 🎯 3-Step Integration

### Step 1: Parse the Time String

```javascript
function parseBalanceTime(balanceTime) {
  const parts = balanceTime.split(':');
  
  if (parts.length === 2) {
    // MM:SS format
    const [min, sec] = parts.map(Number);
    return min * 60 + sec;
  } else {
    // HH:MM:SS format
    const [hr, min, sec] = parts.map(Number);
    return hr * 3600 + min * 60 + sec;
  }
}

// Example:
parseBalanceTime("25:00")    // Returns: 1500 seconds
parseBalanceTime("13:30")    // Returns: 810 seconds
parseBalanceTime("1:40:00")  // Returns: 6000 seconds
```

### Step 2: Display Countdown Timer

```javascript
const response = await initiateCall(creatorId, callType);
let remainingSeconds = parseBalanceTime(response.balance_time);

// Update every second
const interval = setInterval(() => {
  remainingSeconds--;
  
  // Format and display
  const mins = Math.floor(remainingSeconds / 60);
  const secs = remainingSeconds % 60;
  displayTime(`${mins}:${String(secs).padStart(2, '0')}`);
  
  // End call when time runs out
  if (remainingSeconds <= 0) {
    clearInterval(interval);
    endCall();
  }
}, 1000);
```

### Step 3: That's It! ✅

The timer will count down from your balance time to 0:00, then auto-end the call.

---

## 📊 Examples You'll See

| User Balance | Call Type | `balance_time` | Means |
|--------------|-----------|----------------|-------|
| 250 coins | Audio (10/min) | `"25:00"` | 25 minutes |
| 135 coins | Audio (10/min) | `"13:30"` | 13 min 30 sec |
| 300 coins | Video (60/min) | `"5:00"` | 5 minutes |
| 1000 coins | Audio (10/min) | `"1:40:00"` | 1 hr 40 min |

---

## 🔄 Refresh Balance (Optional)

If user adds coins during call, you can refresh:

```javascript
// Call this endpoint during an ongoing call
const status = await fetch(`/api/v1/calls/${callId}/status`);
const data = await status.json();

// Get updated balance time
const newBalanceTime = data.data.balance_time; // "30:00"

// Update your countdown with new time
remainingSeconds = parseBalanceTime(newBalanceTime);
```

---

## 🧪 Test It

### Test 1: Standard Audio Call
```bash
curl -X POST "http://your-api/api/v1/calls/initiate" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO"
  }'

# If user has 250 coins:
# Response will include: "balance_time": "25:00"
```

### Test 2: Video Call
```bash
curl -X POST "http://your-api/api/v1/calls/initiate" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "VIDEO"
  }'

# If user has 300 coins:
# Response will include: "balance_time": "5:00"
```

---

## 🎨 UI Recommendations

### Display Options

**Option 1: Simple Timer (Recommended)**
```
┌─────────────────────┐
│   Time Remaining    │
│      25:00          │
└─────────────────────┘
```

**Option 2: Timer with Context**
```
┌─────────────────────┐
│ 🕐 Time Left: 25:00 │
└─────────────────────┘
```

**Option 3: Visual Progress**
```
┌─────────────────────┐
│      25:00          │
│ [===============   ]│
└─────────────────────┘
```

### Color Coding

- **Green:** > 5 minutes remaining
- **Yellow:** 2-5 minutes remaining
- **Red:** < 2 minutes remaining (show warning)

### Low Balance Warning

```javascript
if (remainingSeconds <= 120) {  // 2 minutes
  showWarning("⚠️ Low balance! Add coins to continue call");
}
```

---

## 🐛 Troubleshooting

### Q: What if `balance_time` is missing?
**A:** Old API version. Update backend to latest version.

### Q: What if I see `"0:00"`?
**A:** User has insufficient balance. Call should have been rejected - check error response.

### Q: Timer is off by a few seconds?
**A:** Normal due to network latency. The backend tracks actual time precisely.

### Q: How do I test with different balances?
**A:** Use admin panel to adjust user's coin balance for testing.

---

## 📚 Full Documentation

For complete details, see:

- **📱_COUNTDOWN_TIMER_REQUIREMENTS.md** - Full requirements
- **🧪_COUNTDOWN_TIMER_TEST_EXAMPLES.md** - Test cases
- **✅_COUNTDOWN_TIMER_IMPLEMENTATION_COMPLETE.md** - Complete summary
- **CALL_INITIATION_API_GUIDE.md** - API documentation

---

## 🎉 That's All!

The feature is ready. Just:
1. Parse `balance_time` string to seconds
2. Display countdown timer
3. Auto-end at 0:00

Simple and effective! 🚀

---

**Questions?** Check the documentation files or contact backend team.

**Version:** 1.0  
**Date:** November 23, 2025  
**Status:** ✅ Production Ready





