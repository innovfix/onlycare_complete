# ✅ REQUEST & RESPONSE EXAMPLES ADDED

## Updated to Match Login/Register Documentation Style

---

## What Was Added:

### 🎨 **New Two-Panel Layout**

**Left Panel:** Documentation
- Parameters
- Validations
- Response codes
- Coin rates

**Right Panel:** Code Examples
- Request examples
- Response examples
- Copy buttons

---

## 📝 **Request Example Added**

```bash
curl -X POST http://localhost/api/v1/calls/initiate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO"
  }'
```

✅ **Copy button included**

---

## 📊 **Response Examples Added (9 Different Scenarios)**

### 1. ✅ **200 OK - Success**
```json
{
  "success": true,
  "message": "Call initiated successfully",
  "data": {
    "call_id": 123,
    "caller_id": "USR_987654321",
    "caller_name": "John Doe",
    "receiver_id": "USR_1234567890",
    "receiver_name": "Ananya798",
    "call_type": "AUDIO",
    "status": "CONNECTING",
    "balance_time": "15:00",
    "agora_token": "007eJx...",
    "channel_name": "call_123"
  }
}
```

### 2. ⚠️ **400 - Self-Call Error**
```json
{
  "success": false,
  "error": {
    "code": "INVALID_REQUEST",
    "message": "You cannot call yourself"
  }
}
```

### 3. ⚠️ **400 - Blocked Error**
```json
{
  "success": false,
  "error": {
    "code": "USER_UNAVAILABLE",
    "message": "User is not available"
  }
}
```

### 4. ⚠️ **400 - User Busy**
```json
{
  "success": false,
  "error": {
    "code": "USER_BUSY",
    "message": "User is currently on another call"
  }
}
```

### 5. ⚠️ **400 - Insufficient Coins**
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_COINS",
    "message": "Insufficient coins for audio call. Minimum 10 coins required.",
    "details": {
      "required": 10,
      "available": 5
    }
  }
}
```

### 6. ⚠️ **400 - User Offline**
```json
{
  "success": false,
  "error": {
    "code": "USER_OFFLINE",
    "message": "User is not online"
  }
}
```

### 7. ⚠️ **400 - Call Type Disabled**
```json
{
  "success": false,
  "error": {
    "code": "CALL_NOT_AVAILABLE",
    "message": "Audio call not available"
  }
}
```

### 8. ⚠️ **404 - User Not Found**
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "User not found"
  }
}
```

### 9. ⚠️ **422 - Validation Error**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": {
      "receiver_id": ["The receiver id field is required."],
      "call_type": ["The call type must be AUDIO or VIDEO."]
    }
  }
}
```

---

## 🎨 **Features Added**

✅ **Two-panel layout** (like login/register)
✅ **9 response examples** covering all error scenarios
✅ **Copy buttons** for all code blocks
✅ **Synchronized scrolling** between panels
✅ **Color-coded responses** (green=success, yellow=error)
✅ **Status code headers** (200, 400, 404, 422)
✅ **cURL request examples**
✅ **Privacy notes** (blocked message)

---

## 🔄 **Refresh Your Browser**

Visit:
```
http://localhost/only_care_admin/public/api-docs/calls
```

You'll now see:
- ✅ Split-screen layout
- ✅ Request example on right
- ✅ 9 response examples on right
- ✅ Copy buttons work
- ✅ Professional format like login/register

---

## 📊 **Before vs After**

| Feature | Before | After |
|---------|--------|-------|
| Layout | Single column | Two-panel split |
| Request examples | In docs | Separate panel with copy |
| Response examples | Basic text | 9 scenarios with copy |
| Copy buttons | None | All code blocks |
| Scroll sync | No | Yes |
| Format | Different | Same as login/register |

---

## ✅ **Status: COMPLETE**

**Documentation now matches the login/register style perfectly!** 🎉

**Date:** November 6, 2024  
**Updated:** Call APIs Documentation  
**Style:** Same as Authentication APIs  
**Status:** ✅ **READY**







