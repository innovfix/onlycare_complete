# ⚡ QUICK QUESTIONS - Call Duration Issue

## 🔴 PROBLEM
App shows **0:00 duration** and **0 coins** after every call.

---

## ❓ 5 CRITICAL QUESTIONS

### Q1: Does `receiver_joined_at` column exist in `calls` table?
```sql
SHOW COLUMNS FROM calls LIKE 'receiver_joined_at';
```
- [ ] Yes, exists
- [ ] No, doesn't exist

---

### Q2: In `acceptCall()`, do you set `receiver_joined_at`?
```php
$call->receiver_joined_at = now(); // ← Is this in your code?
```
- [ ] Yes, we set it
- [ ] No, we don't

---

### Q3: In `endCall()`, how do you calculate duration?
```php
// Which formula are you using?

// Option A: (WRONG - includes ringing time)
$duration = ended_at - started_at;

// Option B: (CORRECT - only talk time)
$duration = ended_at - receiver_joined_at;
```
- [ ] Using Option A (started_at)
- [ ] Using Option B (receiver_joined_at)
- [ ] Other: ___________

---

### Q4: Run this query and tell us the values:
```sql
SELECT 
    id, status, started_at, receiver_joined_at, ended_at, duration, coins_spent
FROM calls 
ORDER BY created_at DESC 
LIMIT 1;
```

**Paste result here:**
```
id: ?
started_at: ?
receiver_joined_at: ?
ended_at: ?
duration: ?
coins_spent: ?
```

---

### Q5: Show us your current `endCall()` code:
```php
// Paste just the duration calculation part:
// (the lines where you calculate duration before saving)
```

---

## ✅ WHAT WE NEED

**Just answer these 5 questions** and we can immediately tell you exactly what to fix!

**Estimated time:** 5 minutes to answer  
**Urgency:** 🔴 HIGH (all calls broken)

---

## 📎 Reference Document

Full details in: `BACKEND_REQUIREMENTS_DURATION_FIX.md`



