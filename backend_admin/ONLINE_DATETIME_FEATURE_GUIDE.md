# 📞 Online DateTime Feature - Complete Guide

## 🎯 Purpose
This feature ensures MALE users are available to receive calls from FEMALE users. MALE users must update their `online_datetime` within the last hour to be available for calls.

---

## 🔄 How It Works

### Step 1: MALE User Updates Online Status
**MALE user calls this API to mark themselves as available:**

```
POST https://onlycare.in/api/v1/users/me/update-online-datetime
Authorization: Bearer {male_user_token}
```

**Response:**
```json
{
    "success": true,
    "message": "Online datetime updated successfully",
    "data": {
        "online_datetime": "2025-12-02T05:47:01+00:00",
        "online_datetime_timestamp": 1764654421
    }
}
```

**What happens:**
- Sets `online_datetime` in database to current time
- MALE user is now "available" for 1 hour

---

### Step 2: FEMALE User Tries to Call MALE User

**FEMALE user initiates call:**

```
POST https://onlycare.in/api/v1/calls/initiate
Authorization: Bearer {female_user_token}
Content-Type: application/json

{
    "receiver_id": "USR_{male_user_id}",
    "call_type": "AUDIO"
}
```

**System checks:**
1. ✅ Is MALE user's `online_datetime` set?
2. ✅ Is `online_datetime` less than 1 hour old?

**If VALID (within 1 hour):**
```json
{
    "success": true,
    "message": "Call initiated successfully",
    "call": {
        "id": "CALL_1234567890",
        "status": "CONNECTING",
        ...
    }
}
```
**→ Call connects! ✅**

**If INVALID (older than 1 hour OR not set):**
```json
{
    "success": false,
    "error": {
        "code": "USER_BUSY",
        "message": "User is not available to receive calls"
    }
}
```
**→ Call rejected! ❌**

---

## 📋 Complete Test Flow

### Test Case 1: MALE Updates Status → FEMALE Calls (SUCCESS)

1. **MALE user updates online datetime:**
   ```
   POST /api/v1/users/me/update-online-datetime
   Token: {male_token}
   ```
   ✅ Returns success

2. **FEMALE user calls MALE immediately:**
   ```
   POST /api/v1/calls/initiate
   Token: {female_token}
   Body: {
       "receiver_id": "USR_{male_id}",
       "call_type": "AUDIO"
   }
   ```
   ✅ Call connects (online_datetime is fresh)

---

### Test Case 2: MALE Updates Status → Wait 1+ Hours → FEMALE Calls (FAILURE)

1. **MALE user updates online datetime:**
   ```
   POST /api/v1/users/me/update-online-datetime
   ```
   ✅ Success

2. **Wait 1 hour and 1 minute**

3. **FEMALE user tries to call:**
   ```
   POST /api/v1/calls/initiate
   ```
   ❌ Returns "User is not available to receive calls"
   (online_datetime expired)

---

### Test Case 3: MALE Never Updates → FEMALE Calls (FAILURE)

1. **MALE user never calls update-online-datetime**
   (online_datetime is NULL in database)

2. **FEMALE user tries to call:**
   ```
   POST /api/v1/calls/initiate
   ```
   ❌ Returns "User is not available to receive calls"
   (online_datetime not set)

---

## 🔍 Database Check

**Check MALE user's online_datetime:**
```sql
SELECT id, name, user_type, online_datetime, 
       TIMESTAMPDIFF(MINUTE, online_datetime, NOW()) as minutes_ago
FROM users 
WHERE user_type = 'MALE' 
AND id = 'USR_{user_id}';
```

**Check if MALE is available (within 1 hour):**
```sql
SELECT id, name, 
       online_datetime,
       CASE 
           WHEN online_datetime IS NULL THEN 'NOT SET'
           WHEN online_datetime > DATE_SUB(NOW(), INTERVAL 1 HOUR) THEN 'AVAILABLE'
           ELSE 'EXPIRED'
       END as status
FROM users 
WHERE user_type = 'MALE';
```

---

## 📝 Important Notes

1. **Only MALE users** can update `online_datetime`
   - FEMALE users calling the endpoint get 403 Forbidden

2. **Validation only applies to FEMALE → MALE calls**
   - MALE → FEMALE calls don't check `online_datetime`
   - FEMALE → FEMALE calls don't check `online_datetime`
   - MALE → MALE calls don't check `online_datetime`

3. **1 Hour Window**
   - MALE must update `online_datetime` every hour to stay available
   - After 1 hour, they become "unavailable"

4. **Error Code**
   - Always returns `USER_BUSY` code
   - Message: "User is not available to receive calls"

---

## 🧪 Quick Test Commands

### Test Update Endpoint (MALE user):
```bash
curl -X POST https://onlycare.in/api/v1/users/me/update-online-datetime \
  -H "Authorization: Bearer {male_token}" \
  -H "Content-Type: application/json"
```

### Test Call Initiation (FEMALE user):
```bash
curl -X POST https://onlycare.in/api/v1/calls/initiate \
  -H "Authorization: Bearer {female_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "receiver_id": "USR_{male_user_id}",
    "call_type": "AUDIO"
  }'
```

---

## ✅ Summary

- **MALE users** must call `/api/v1/users/me/update-online-datetime` to be available
- **FEMALE users** can only call MALE users whose `online_datetime` is within 1 hour
- **After 1 hour**, MALE users become unavailable until they update again
- This ensures MALE users are actively online before receiving calls from FEMALE users








