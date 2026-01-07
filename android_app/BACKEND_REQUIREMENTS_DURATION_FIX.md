# Backend Requirements - Call Duration Fix

## 🐛 ISSUE DESCRIPTION

**Problem:** Users are being overcharged because call duration includes ringing time instead of only actual talk time.

**Example:**
- User clicks "Call" button → Timer starts on backend
- Phone rings for 45 seconds
- Receiver picks up call
- They talk for 2 minutes
- Call ends
- **Backend calculates:** 2 minutes + 45 seconds = 2:45 duration
- **User is charged for:** 2:45 (165 seconds) = ~16-17 coins
- **Should be charged for:** 2:00 (120 seconds) = 12 coins
- **Result:** User overcharged by 40%

---

## 🎯 ROOT CAUSE

Backend currently calculates duration as:
```
Duration = Call End Time - Call Start Time
```

But **Call Start Time** is set when caller initiates the call (before receiver picks up).

This means duration includes:
- ❌ Ringing time (30-45 seconds)
- ❌ Connection delays
- ❌ Time before receiver accepts

**It should only include actual talk time** (when both users are connected).

---

## ✅ SOLUTION REQUIRED

Track when receiver **actually accepts** the call, not just when caller initiates it.

Calculate duration as:
```
Duration = Call End Time - Receiver Joined Time
```

This ensures users are only charged for actual conversation time.

---

## 📋 CHANGES NEEDED

### 1. DATABASE CHANGE

**Add new column to `calls` table:**

| Column Name | Type | Nullable | Description |
|-------------|------|----------|-------------|
| `receiver_joined_at` | TIMESTAMP | YES | Timestamp when receiver accepts/picks up the call |

**Notes:**
- Make it nullable to handle backward compatibility
- Existing calls can have NULL value
- Add index on this column for performance

---

### 2. API ENDPOINT: Accept Call

**Endpoint:** `POST /api/calls/{callId}/accept`

**Current Behavior:**
- Updates call status to "ONGOING"
- Sends notification to caller

**Required Change:**
- **SET** `receiver_joined_at` = current timestamp when receiver accepts the call
- This marks the exact moment receiver picked up

**When to set:**
- Only when receiver successfully accepts the call
- Not when rejected, missed, or timed out

---

### 3. API ENDPOINT: End Call

**Endpoint:** `POST /api/calls/{callId}/end`

**Request Body:**
```json
{
  "duration": 120  // Client-tracked duration in seconds
}
```

**Current Behavior:**
- Calculates duration = `ended_at - started_at`
- Calculates coins based on this duration
- Updates user balance

**Required Changes:**

**A) Duration Calculation:**
- **IF** `receiver_joined_at` exists (not NULL):
  - Calculate: `duration = ended_at - receiver_joined_at`
  - This gives actual talk time
- **ELSE** (call never answered):
  - Set `duration = 0`
  - No coins should be charged

**B) Validation:**
- Compare server-calculated duration with client-sent duration
- If difference is more than 30 seconds → Log for investigation
- Use server duration for billing (more reliable)

**C) Billing:**
- Calculate coins based on **server duration** (from `receiver_joined_at`)
- Apply same pricing logic as before (6 coins/min for audio, 10 coins/min for video)

---

## 📊 RESPONSE FORMAT

**Accept Call Response:** (No change needed)
```json
{
  "success": true,
  "message": "Call accepted",
  "data": {
    "id": "call-123",
    "status": "ONGOING",
    "receiver_joined_at": "2025-11-23T08:35:00.000Z",
    "started_at": "2025-11-23T08:34:30.000Z"
  }
}
```

**End Call Response:**
```json
{
  "success": true,
  "message": "Call ended",
  "call": {
    "id": "call-123",
    "status": "ENDED",
    "duration": 120,
    "coins_spent": 12,
    "coins_earned": 8,
    "started_at": "2025-11-23T08:34:30.000Z",
    "receiver_joined_at": "2025-11-23T08:35:00.000Z",
    "ended_at": "2025-11-23T08:37:00.000Z"
  },
  "updated_balance": 488
}
```

**Key Point:** Duration should be ~120 seconds (2 minutes), NOT ~150 seconds (2.5 minutes with ringing)

---

## 🧪 TESTING REQUIREMENTS

### Test Case 1: Normal Call Flow
1. User A calls User B
2. Phone rings for 30 seconds
3. User B accepts call → `receiver_joined_at` should be set
4. Talk for 2 minutes
5. End call
6. **Verify:** `duration` = ~120 seconds (NOT ~150)
7. **Verify:** Coins calculated based on 2 minutes only

### Test Case 2: Call Not Answered
1. User A calls User B
2. Phone rings for 30 seconds
3. Call times out (not answered)
4. **Verify:** `receiver_joined_at` = NULL
5. **Verify:** `duration` = 0
6. **Verify:** Coins = 0 (no charge)

### Test Case 3: Call Rejected
1. User A calls User B
2. User B rejects immediately
3. **Verify:** `receiver_joined_at` = NULL
4. **Verify:** `duration` = 0
5. **Verify:** Coins = 0 (no charge)

### Test Case 4: Duration Validation
1. Make a 2-minute call
2. Client sends `duration: 120`
3. Server calculates duration from `receiver_joined_at`
4. **If difference > 30 seconds:** Log warning
5. **Use server duration** for billing

---

## ⚠️ EDGE CASES TO HANDLE

### Case 1: Old Calls (Before Fix)
- Existing calls don't have `receiver_joined_at`
- Handle NULL gracefully
- Use old logic (or client duration) for these calls

### Case 2: Client Duration Missing
- If client doesn't send duration
- Calculate from `receiver_joined_at`
- If that's also NULL → duration = 0

### Case 3: Receiver Never Joins
- Call accepted but receiver never joins Agora channel
- `receiver_joined_at` is set but no actual connection
- Still use server calculation (fair approach)

### Case 4: Network Issues During Call
- Client duration might differ due to disconnections
- Server duration is more reliable (use it)
- Log significant mismatches for investigation

---

## 📈 SUCCESS CRITERIA

After implementation, verify:

1. ✅ All new accepted calls have `receiver_joined_at` timestamp
2. ✅ Duration calculated from `receiver_joined_at`, not `started_at`
3. ✅ Users charged only for talk time, not ringing time
4. ✅ Calls that are never answered have 0 duration and 0 coins
5. ✅ Client vs server duration comparison logged
6. ✅ Billing is fair and transparent

---

## 📊 EXPECTED IMPACT

### Before Fix:
- Average call duration: Inflated by 30-45 seconds
- User overcharged by: 40-80% per call
- User complaints: "Why so expensive?"

### After Fix:
- Average call duration: Accurate (only talk time)
- User charged fairly: 100% accurate
- User satisfaction: Improved

---

## 🚨 PRIORITY & URGENCY

**Priority:** 🔴 **CRITICAL**

**Reason:** This is a billing issue affecting all users. Users are being overcharged for every call.

**Impact:** 
- Affects 100% of calls
- Financial impact on users
- Trust and retention issue
- Potential refund requests

**Timeline:**
- Review: 1-2 days
- Implementation: 2-3 days
- Testing: 1-2 days
- Deployment: 1 day
- **Total: 5-8 days**

---

## 🔍 MONITORING REQUIREMENTS

### What to Monitor:

1. **Duration Accuracy**
   - Track difference between client and server duration
   - Alert if average difference > 15 seconds

2. **Null Values**
   - Monitor how many calls have NULL `receiver_joined_at`
   - Should decrease to near-zero after fix

3. **Billing Fairness**
   - Compare average duration before and after fix
   - Should decrease by 20-40% (removed ringing time)

4. **User Feedback**
   - Monitor support tickets about billing
   - Should decrease significantly

### Logging Needed:

For each call end:
```
LOG: Call {callId} ended
- Started at: {started_at}
- Receiver joined at: {receiver_joined_at}
- Ended at: {ended_at}
- Client duration: {client_duration}s
- Server duration: {server_duration}s
- Difference: {difference}s
- Coins charged: {coins_spent}
```

---

## 📞 COORDINATION REQUIRED

### With Frontend Team:
- Frontend tracks duration correctly (already confirmed)
- Frontend will update UI to show accurate data
- Coordinate testing between client and server

### With Database Team:
- Add new column with index
- No data migration needed for existing calls
- Backup before deployment

### With DevOps:
- Plan deployment window
- Set up monitoring alerts
- Prepare rollback plan if needed

---

## ✅ ACCEPTANCE CRITERIA

This ticket is DONE when:

1. ✅ Database column `receiver_joined_at` added
2. ✅ Accept call endpoint sets the timestamp
3. ✅ End call endpoint uses new calculation logic
4. ✅ All test cases pass
5. ✅ Deployed to staging and verified
6. ✅ Deployed to production
7. ✅ Monitoring shows correct duration calculation
8. ✅ User complaints about billing reduced

---

## 📝 ADDITIONAL NOTES

### Backward Compatibility:
- Existing calls with NULL `receiver_joined_at` should work
- No breaking changes to API
- Response format remains the same

### Performance:
- Add database index on `receiver_joined_at`
- No additional API calls needed
- Minimal performance impact

### Security:
- No security concerns
- Same authentication/authorization as before

### Documentation:
- Update API documentation with new field
- Add note about duration calculation change
- Update billing documentation

---

## 🎯 SUMMARY

**What:** Fix call duration calculation to exclude ringing time

**Why:** Users are being overcharged 40-80% per call

**How:** 
1. Track when receiver picks up (new timestamp)
2. Calculate duration from pickup to end (not from initiation)
3. Bill based on actual talk time only

**Impact:** Fair billing, improved user trust, reduced complaints

**Timeline:** 5-8 days from review to production

**Priority:** Critical (affects all users' billing)

---

## 📧 QUESTIONS?

If you need clarification on:
- Requirements → Contact Product Team
- Technical implementation → Contact Frontend Team (for duration tracking logic)
- Database changes → Contact Database Team
- Deployment → Contact DevOps Team

---

**This fix will make billing fair and transparent for all users! 🎯**



