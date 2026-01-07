# Backend Fix - Quick Start Guide

## 🎯 PROBLEM
Users are being overcharged because backend calculates duration from call initiation (including ringing time) instead of from when receiver picks up.

---

## ⚡ QUICK FIX (3 Steps)

### Step 1: Database Migration (2 minutes)

```sql
ALTER TABLE calls ADD COLUMN receiver_joined_at TIMESTAMP NULL;
CREATE INDEX idx_calls_receiver_joined_at ON calls(receiver_joined_at);
```

---

### Step 2: Update Accept Call Endpoint (5 minutes)

**When receiver accepts call, set the timestamp:**

```javascript
// In POST /calls/:callId/accept
await db.query(
    `UPDATE calls 
     SET status = 'ONGOING', 
         receiver_joined_at = NOW()  -- ⭐ ADD THIS
     WHERE id = $1`,
    [callId]
);
```

---

### Step 3: Update End Call Endpoint (10 minutes)

**Calculate duration from receiver_joined_at, not started_at:**

```javascript
// In POST /calls/:callId/end

// ❌ OLD (WRONG):
const duration = (ended_at - started_at) / 1000;

// ✅ NEW (CORRECT):
const duration = call.receiver_joined_at 
    ? (ended_at - receiver_joined_at) / 1000  // Only talk time
    : clientDuration || 0;  // Fallback if never answered
```

---

## 📊 BEFORE vs AFTER

### Before (Wrong):
```
User clicks "Call"     → started_at = 08:34:30
Ringing... (30s)
Receiver picks up      → (not tracked)
Talk for 2 minutes
Call ends              → ended_at = 08:37:00

Duration = 08:37:00 - 08:34:30 = 2:30 (150 seconds)
Coins = 150s ÷ 60 × 6 = 15 coins ❌
```

### After (Correct):
```
User clicks "Call"     → started_at = 08:34:30
Ringing... (30s)
Receiver picks up      → receiver_joined_at = 08:35:00 ⭐
Talk for 2 minutes
Call ends              → ended_at = 08:37:00

Duration = 08:37:00 - 08:35:00 = 2:00 (120 seconds) ✅
Coins = 120s ÷ 60 × 6 = 12 coins ✅
```

---

## 🧪 TESTING

### Quick Test:
```bash
# 1. Make a test call
curl -X POST /api/calls/initiate -d '{"receiver_id":"user2","call_type":"AUDIO"}'

# 2. Wait 30 seconds (simulate ringing)

# 3. Accept call
curl -X POST /api/calls/CALL_ID/accept

# Check database:
SELECT started_at, receiver_joined_at FROM calls WHERE id = 'CALL_ID';
# receiver_joined_at should be ~30s after started_at ✅

# 4. Wait 2 minutes (simulate talking)

# 5. End call
curl -X POST /api/calls/CALL_ID/end -d '{"duration":120}'

# Check result:
SELECT duration, coins_spent FROM calls WHERE id = 'CALL_ID';
# duration should be ~120s (NOT ~150s) ✅
# coins_spent should be ~12 (NOT ~15) ✅
```

---

## ⚠️ EDGE CASES

### Case 1: Receiver Never Picks Up
```javascript
if (!call.receiver_joined_at) {
    // Use client duration (will be 0)
    duration = clientDuration || 0;
    // User not charged ✅
}
```

### Case 2: Call Rejected
- `receiver_joined_at` stays NULL
- Duration = 0
- No coins charged ✅

### Case 3: Client Duration Differs from Server
```javascript
if (Math.abs(clientDuration - serverDuration) > 30) {
    console.warn('Duration mismatch:', {
        client: clientDuration,
        server: serverDuration
    });
    // Use server duration (more reliable)
}
```

---

## 📈 VALIDATION

Add this validation code to compare client vs server:

```javascript
// In end call endpoint
const clientDuration = req.body.duration;
const serverDuration = (ended_at - receiver_joined_at) / 1000;

if (Math.abs(clientDuration - serverDuration) > 30) {
    // Log for investigation
    await logDurationMismatch({
        callId,
        clientDuration,
        serverDuration,
        diff: Math.abs(clientDuration - serverDuration)
    });
}

// Use server duration for billing
call.duration = serverDuration;
```

---

## 📊 MONITORING

Create a simple monitoring table:

```sql
CREATE TABLE duration_mismatch_logs (
    id SERIAL PRIMARY KEY,
    call_id VARCHAR(36),
    client_duration INT,
    server_duration INT,
    difference INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Check for issues:
SELECT 
    COUNT(*) as mismatches,
    AVG(difference) as avg_diff
FROM duration_mismatch_logs
WHERE created_at >= NOW() - INTERVAL '24 hours'
  AND difference > 30;
```

---

## ✅ CHECKLIST

Backend team should:

- [ ] Run database migration
- [ ] Update accept call endpoint (add receiver_joined_at)
- [ ] Update end call endpoint (calculate from receiver_joined_at)
- [ ] Add duration validation
- [ ] Test in staging
- [ ] Deploy to production
- [ ] Monitor for 24 hours

---

## 🆘 SUPPORT

**Questions? Contact:**
- Frontend team (Android) for client duration tracking logic
- Database admin for migration support
- DevOps for deployment

**Files to reference:**
- Full code examples: `BACKEND_CODE_DURATION_FIX.md`
- Root cause analysis: `CALL_ENDED_DURATION_COINS_ROOT_CAUSE.md`
- Implementation plan: `CALL_ENDED_FIX_PLAN.md`

---

## 🎯 EXPECTED RESULTS

After deployment:
- ✅ Duration matches actual talk time
- ✅ No overcharging for ringing time
- ✅ Reduced user complaints about billing
- ✅ Fair and transparent pricing

**Estimated time: 15-30 minutes to implement**
**Impact: High (affects all users' billing)**
**Priority: Urgent (users being overcharged)**



