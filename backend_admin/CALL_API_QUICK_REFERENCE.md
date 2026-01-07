# Call Initiation API - Quick Reference

## 🎯 Purpose
Endpoint to initiate audio/video calls when user clicks call buttons on creator profiles.

---

## 📍 Endpoint
```
POST /api/v1/calls/initiate
```

---

## 🔑 Authentication
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

---

## 📤 Request Body
```json
{
  "receiver_id": "USR_1234567890",
  "call_type": "AUDIO"
}
```

**Call Types:**
- `AUDIO` - Audio call (10 coins/min)
- `VIDEO` - Video call (60 coins/min)

---

## ✅ Success Response (200)
```json
{
  "success": true,
  "call": {
    "id": "CALL_1234567890",
    "caller_id": "USR_9876543210",
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO",
    "status": "CONNECTING",
    "created_at": "2024-11-04T10:30:00Z",
    "agora_token": "007eJxTYBBaM2fN8gVz...",
    "channel_name": "call_1234567890",
    "balance_time": "25:00"
  },
  "agora_app_id": "abc123xyz",
  "agora_token": "007eJxTYBBaM2fN8gVz...",
  "channel_name": "call_1234567890",
  "balance_time": "25:00",
  "receiver": {
    "name": "Ananya798",
    "profile_image": "https://cdn.onlycare.app/profiles/ananya798.jpg"
  }
}
```

**New Field - `balance_time`:** Time remaining based on user's coin balance (format: "MM:SS" or "HH:MM:SS")  
Examples: `"25:00"` (25 minutes), `"13:30"` (13 min 30 sec), `"1:40:00"` (1 hour 40 minutes)

---

## ❌ Common Errors

### Insufficient Coins (400)
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_COINS",
    "message": "You don't have enough coins to make this call",
    "details": {
      "required": 10,
      "available": 5
    }
  }
}
```
**Action:** Show recharge screen

---

### User Offline (400)
```json
{
  "success": false,
  "error": {
    "code": "USER_OFFLINE",
    "message": "User is not online"
  }
}
```
**Action:** Show "Creator is offline" message

---

### Call Not Available (400)
```json
{
  "success": false,
  "error": {
    "code": "CALL_NOT_AVAILABLE",
    "message": "Video call not available"
  }
}
```
**Action:** Suggest audio call instead

---

### Creator Not Found (404)
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "User not found"
  }
}
```
**Action:** Show error message

---

## 🔍 What Gets Validated

1. ✅ User authentication (Bearer token)
2. ✅ Creator exists in database
3. ✅ Creator is online
4. ✅ Call type is enabled (audio/video)
5. ✅ User has sufficient coin balance
6. ✅ Generates Agora token for WebRTC

---

## 💡 Integration Example

### React Native / JavaScript
```javascript
const initiateCall = async (creatorId, callType) => {
  const response = await fetch('https://api.onlycare.app/v1/calls/initiate', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    body: JSON.stringify({
      receiver_id: creatorId,
      call_type: callType
    })
  });
  
  const data = await response.json();
  
  if (data.success) {
    // Connect to Agora with data.call.agora_token
    connectToAgora(data.call);
  } else {
    // Handle error
    handleError(data.error);
  }
};
```

---

## 🎬 Complete Call Flow

1. **User clicks call button** → `POST /calls/initiate`
2. **Backend validates** → Returns Agora credentials
3. **App connects to Agora** → Establishes WebRTC connection
4. **Receiver gets notification** → Can accept/reject
5. **If accepted** → `POST /calls/{callId}/accept`
6. **Call in progress** → Agora handles audio/video
7. **Call ends** → `POST /calls/{callId}/end` (with duration)
8. **Coins deducted** → Transaction created
9. **Show summary** → User can rate call

---

## 🧪 Testing

### Web Interface
```
http://your-domain.com/api-docs
→ Click "Initiate Call" in sidebar
→ Fill form and test
```

### cURL
```bash
curl -X POST http://your-domain.com/api/v1/calls/initiate \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"receiver_id":"USR_1234567890","call_type":"AUDIO"}'
```

---

## 📋 Related Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/calls/initiate` | POST | Start call |
| `/calls/{id}/accept` | POST | Accept incoming call |
| `/calls/{id}/reject` | POST | Reject incoming call |
| `/calls/{id}/end` | POST | End ongoing call |
| `/calls/{id}/rate` | POST | Rate completed call |
| `/calls/history` | GET | Get call history |
| `/calls/recent-sessions` | GET | Get recent sessions |

---

## 📦 Files Modified

1. ✅ **Backend Controller** - Already exists
   - `/app/Http/Controllers/Api/CallController.php`
   - Method: `initiateCall()`

2. ✅ **API Routes** - Already registered
   - `/routes/api.php` (line 87)

3. ✅ **Web API Documentation** - Updated
   - `/resources/views/api-docs/index-dark.blade.php`
   - Added sidebar link
   - Added documentation section
   - Added code examples
   - Added test form with JavaScript handler

4. ✅ **Documentation Files** - Created
   - `CALL_INITIATION_API_GUIDE.md` (Complete guide)
   - `CALL_API_QUICK_REFERENCE.md` (This file)

---

## 🚀 Status

**✅ READY TO USE**

The endpoint is fully functional and documented. No additional backend work needed. Just integrate the API call in your mobile app when user clicks the audio/video call buttons!

---

## 💬 Support

For issues or questions:
- Full docs: `/CALL_INITIATION_API_GUIDE.md`
- Web docs: `http://your-domain.com/api-docs`
- Controller: `/app/Http/Controllers/Api/CallController.php`







