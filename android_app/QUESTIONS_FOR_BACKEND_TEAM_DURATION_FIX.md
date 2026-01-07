# Questions for Backend Team - Call Duration Fix

## 🔴 URGENT: Current Issue

The app is showing **0:00 duration and 0 coins** for all calls because backend returns:
```json
{
  "duration": 0,
  "coins_spent": 0,
  "started_at": null,
  "receiver_joined_at": null
}
```

---

## ❓ QUESTIONS FOR BACKEND TEAM

### 1. Database Schema

**Q1:** Does the `calls` table have a `receiver_joined_at` column?
- [ ] Yes, column exists
- [ ] No, not added yet

**Q2:** What is the exact database schema for the `calls` table?
```
Please show: DESCRIBE calls; or SHOW COLUMNS FROM calls;
```

---

### 2. Accept Call Endpoint

**Q3:** In the `/api/v1/calls/{callId}/accept` endpoint, do you set the `receiver_joined_at` timestamp?
- [ ] Yes, we set it
- [ ] No, not implemented yet

**Q4:** What timestamps are currently being set when a call is accepted?
```
Please show the code that runs when accept is called:
- Do you set started_at?
- Do you set receiver_joined_at?
- What value is set?
```

**Q5:** Can you share the current code for the accept call endpoint?
```php
// Please paste the acceptCall() function code here
```

---

### 3. End Call Endpoint

**Q6:** In the `/api/v1/calls/{callId}/end` endpoint, how do you calculate duration?

Please check your code and tell us which formula you use:
- [ ] A) `duration = ended_at - started_at` (WRONG - includes ringing time)
- [ ] B) `duration = ended_at - receiver_joined_at` (CORRECT - only talk time)
- [ ] C) `duration = client-sent value` (using what client sends)
- [ ] D) Other: _______________

**Q7:** Can you share the current code for the end call endpoint?
```php
// Please paste the endCall() function code here
// Specifically the part that calculates duration
```

---

### 4. Timestamps

**Q8:** When we make a call and accept it, what are the actual values in the database?

Please run this query after making a test call:
```sql
SELECT 
    id,
    status,
    started_at,
    receiver_joined_at,
    ended_at,
    duration,
    coins_spent,
    created_at
FROM calls 
WHERE id = 'CALL_17639124091962'  -- Use the latest call ID
LIMIT 1;
```

**Expected result format:**
```
id: CALL_17639124091962
status: ENDED
started_at: 2025-11-23 15:40:09 or NULL?
receiver_joined_at: 2025-11-23 15:40:11 or NULL?
ended_at: 2025-11-23 15:40:16
duration: 0 or some number?
coins_spent: 0 or some number?
```

---

### 5. Current Implementation Status

**Q9:** Have you implemented the duration fix from `BACKEND_REQUIREMENTS_DURATION_FIX.md`?
- [ ] Yes, fully implemented
- [ ] Partially implemented (which parts?)
- [ ] Not started yet
- [ ] Don't know about this requirement

**Q10:** When did you last modify the calls endpoints?
```
Last modified date:
- acceptCall endpoint: ?
- endCall endpoint: ?
```

---

### 6. Testing

**Q11:** Can you test this manually and tell us what you see?

**Test steps:**
1. User A calls User B
2. Wait 30 seconds (ringing)
3. User B accepts call
4. Talk for 2 minutes
5. End call
6. Check database: What is the duration value?

**Expected:** duration should be ~120 seconds (2 minutes talk time)  
**NOT:** ~150 seconds (2 min + 30 sec ringing)

**Q12:** After making the test call above, what values do you see in the database?
```
duration = ?
coins_spent = ?
started_at = ?
receiver_joined_at = ?
ended_at = ?
```

---

### 7. Error Investigation

**Q13:** Why is `receiver_joined_at` NULL in the response?

Looking at our logs:
```json
{
  "receiver_joined_at": null,  // ← Why is this NULL?
  "started_at": null           // ← Why is this NULL?
}
```

Possible reasons:
- [ ] Column doesn't exist in database
- [ ] Column exists but we're not setting it
- [ ] Column exists and we set it, but API doesn't return it
- [ ] Other reason: _______________

---

### 8. Quick Check

**Q14:** Can you run this query RIGHT NOW and tell us the result?

```sql
-- Check if receiver_joined_at column exists
SHOW COLUMNS FROM calls LIKE 'receiver_joined_at';
```

**If it returns empty:** Column doesn't exist (needs to be added)  
**If it returns a row:** Column exists

**Q15:** If column exists, please run:
```sql
-- Check current calls
SELECT 
    COUNT(*) as total_calls,
    COUNT(receiver_joined_at) as calls_with_joined_timestamp,
    COUNT(CASE WHEN receiver_joined_at IS NULL THEN 1 END) as calls_without_joined_timestamp
FROM calls
WHERE created_at >= CURDATE();
```

This tells us how many calls have the timestamp set.

---

## 🎯 WHAT WE NEED FROM YOU

**Please answer all questions above and provide:**

1. ✅ Current database schema for `calls` table
2. ✅ Current code for `acceptCall()` function
3. ✅ Current code for `endCall()` function (duration calculation part)
4. ✅ SQL query results showing actual call data
5. ✅ Confirmation of implementation status

---

## 📋 REFERENCE

**What needs to be done (if not done yet):**

### Step 1: Add Column (if missing)
```sql
ALTER TABLE calls ADD COLUMN receiver_joined_at TIMESTAMP NULL;
```

### Step 2: Update Accept Call (if not setting timestamp)
```php
// In acceptCall() function:
$call->receiver_joined_at = now();
$call->status = 'ONGOING';
$call->save();
```

### Step 3: Update End Call (if calculating wrong)
```php
// In endCall() function:
if ($call->receiver_joined_at) {
    $duration = now()->diffInSeconds($call->receiver_joined_at);
} else {
    $duration = 0; // Call never answered
}
$call->duration = $duration;
```

---

## ⏱️ URGENCY

**Priority:** 🔴 HIGH

**Impact:** All calls showing 0:00 duration and 0 coins in the app

**Users affected:** 100% of call users

**Expected fix time:** 30-60 minutes once we understand current state

---

## 📞 CONTACT

Please answer these questions as soon as possible so we can:
1. Understand what's currently implemented
2. Identify what's missing
3. Provide exact fix instructions
4. Test and verify the fix

**Thank you!** 🙏



