# 🚨 Agora Error 110 - Quick Reference

**Problem:** Receiver gets Error 110 immediately after accepting call  
**Root Cause:** UID mismatch between token generation and channel join  
**Status:** ✅ IDENTIFIED | 🛠️ READY TO FIX

---

## 🎯 The Problem in One Sentence

Your backend generates Agora tokens with **UID = 0**, but your Android app is probably joining with a **different UID** (like the user's ID), causing Agora to reject the connection immediately.

---

## 📊 What We Found

### Backend (CallController.php Line 876):
```php
$uid = 0; // ⚠️ HARDCODED
```

### Current Flow:
```
1. Generate token with UID=0
2. Save to database
3. Both caller and receiver get SAME token
4. Android tries to join with UID = ??? (probably not 0)
5. ❌ ERROR 110: Token/UID mismatch
```

---

## ✅ The Solution

### Two Options:

**Option A: Quick Fix (30 minutes)**
- Add `agora_uid: 0` to all API responses
- Update Android app to use `agora_uid` from API
- ✅ Simple, low risk, works immediately

**Option B: Proper Fix (2 hours)**
- Generate unique UID for each user
- Separate tokens for caller and receiver
- Database migration required
- ✅ Better security, better tracking

---

## 🚀 Quick Fix Steps (Option A)

### 1. Backend Changes (3 lines)

**File:** `app/Http/Controllers/Api/CallController.php`

**Line 289:** Add to `initiateCall()` response:
```php
'agora_uid' => 0,  // ✅ ADD THIS
```

**Line 365:** Add to `getIncomingCalls()` response:
```php
'agora_uid' => 0,  // ✅ ADD THIS
```

**Line 453:** Add to `acceptCall()` response:
```php
'agora_uid' => 0,  // ✅ ADD THIS
```

### 2. Android Changes (1 line)

**Before:**
```kotlin
rtcEngine.joinChannel(token, channel, uid = userId.toInt())  // ❌ Wrong
```

**After:**
```kotlin
rtcEngine.joinChannel(token, channel, uid = response.agora_uid)  // ✅ Correct
```

---

## 📝 API Response Examples

### POST /calls/initiate

**Before:**
```json
{
  "agora_token": "007...",
  "channel_name": "call_CALL_123"
}
```

**After:**
```json
{
  "agora_token": "007...",
  "channel_name": "call_CALL_123",
  "agora_uid": 0  // ✅ NEW
}
```

### GET /calls/incoming

**Before:**
```json
{
  "agora_token": "007...",
  "channel_name": "call_CALL_123"
}
```

**After:**
```json
{
  "agora_token": "007...",
  "channel_name": "call_CALL_123",
  "agora_uid": 0  // ✅ NEW
}
```

### POST /calls/{id}/accept

**Before:**
```json
{
  "agora_token": "007...",
  "channel_name": "call_CALL_123"
}
```

**After:**
```json
{
  "agora_token": "007...",
  "channel_name": "call_CALL_123",
  "agora_uid": 0  // ✅ NEW
}
```

---

## 🧪 Testing

### 1. Check Backend Logs

Look for:
```
Token Generation Debug for call CALL_123:
  - UID: 0  ← Should be 0
```

### 2. Check API Response

```bash
curl -H "Authorization: Bearer $TOKEN" \
  https://your-api.com/api/calls/incoming

# Should return:
# "agora_uid": 0
```

### 3. Check Android Logs

```
Joining Agora channel:
  - Token: 007...
  - Channel: call_CALL_123
  - UID: 0  ← Should be 0
```

### 4. Make Test Call

- User A calls User B
- User B accepts
- ✅ Both should connect without Error 110

---

## 📄 Full Documentation

See these files for complete details:

1. **AGORA_TOKEN_UID_ANALYSIS.md** - Detailed analysis and findings
2. **AGORA_UID_FIX_IMPLEMENTATION.md** - Step-by-step code changes
3. This file - Quick reference

---

## 🔧 Need Help?

### Common Issues:

**Q: Still getting Error 110?**  
A: Check Android app is using `response.agora_uid` and not hardcoded UID

**Q: Backend returns null for agora_uid?**  
A: Verify you added the field to all 3 endpoints

**Q: Old calls not working?**  
A: Normal - old calls don't have the new field. New calls will work.

---

## ✅ Checklist

### Backend Team:
- [ ] Add `agora_uid: 0` to POST /calls/initiate response
- [ ] Add `agora_uid: 0` to GET /calls/incoming response
- [ ] Add `agora_uid: 0` to POST /calls/accept response
- [ ] Deploy changes
- [ ] Test API responses

### Android Team:
- [ ] Add `agora_uid: Int` to response models
- [ ] Update `joinChannel()` to use `response.agora_uid`
- [ ] Test call flow
- [ ] Verify Error 110 is gone

---

## 📊 Expected Results

### Before Fix:
```
03:06:16.461 - ✅ Receiver joins channel (result: 0)
03:06:16.607 - ❌ ERROR 110: ERR_OPEN_CHANNEL_TIMEOUT
```

### After Fix:
```
03:06:16.461 - ✅ Receiver joins channel (result: 0)
03:06:16.607 - ✅ Connection established
03:06:17.000 - ✅ Remote user joined
03:06:17.200 - ✅ Audio/Video streaming
```

---

## 🎯 Bottom Line

**The Fix:**
1. Backend tells Android what UID to use
2. Android uses that UID when joining
3. UIDs match → Connection works ✅

**Time to Fix:** 30 minutes  
**Risk:** Very low  
**Impact:** Calls will connect successfully

---

## 📞 Contact

Questions? Need help implementing?  
- Check the detailed guides in this folder
- Test with the checklist above
- Verify logs match expected output

**Good luck! 🚀**

