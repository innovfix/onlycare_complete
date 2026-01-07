# Call Initiation API - Implementation Complete ✅

## Summary
The audio/video call initiation endpoint is **fully functional and documented**. When users click the call buttons on creator profiles in your mobile app, use this endpoint to establish the connection.

---

## 🎯 What Was Done

### ✅ Backend (Already Existed)
The endpoint was already implemented in the backend:
- **Controller:** `/app/Http/Controllers/Api/CallController.php`
- **Method:** `initiateCall(Request $request)`
- **Route:** `POST /api/v1/calls/initiate` (line 87 in routes/api.php)
- **Status:** Fully functional, production-ready

### ✅ Web-Based API Documentation (Updated)
Added complete documentation to the web interface:
- **File:** `/resources/views/api-docs/index-dark.blade.php`
- **Changes:**
  - Added "Initiate Call" link to sidebar
  - Added full API documentation section with:
    - Request parameters table
    - Response codes explanation
    - Validations performed
    - Interactive test form
  - Added code examples (cURL, success, and all error responses)
  - Added JavaScript form handler for live testing
- **Access:** Navigate to `http://your-domain.com/api-docs` and click "Initiate Call"

### ✅ Comprehensive Documentation Files (Created)
1. **`CALL_INITIATION_API_GUIDE.md`** (7 KB)
   - Complete integration guide
   - Request/response formats
   - Error handling
   - Integration flow with code examples
   - UI/UX recommendations
   - Troubleshooting guide
   - Security considerations

2. **`CALL_API_QUICK_REFERENCE.md`** (4 KB)
   - Quick reference for developers
   - Request/response examples
   - Common errors with solutions
   - Integration code snippets
   - Related endpoints table

---

## 📍 API Details

### Endpoint
```
POST /api/v1/calls/initiate
```

### Request
```json
{
  "receiver_id": "USR_1234567890",
  "call_type": "AUDIO"
}
```

### Success Response
```json
{
  "success": true,
  "call": {
    "id": "CALL_1234567890",
    "agora_token": "007eJxTYBBaM2fN8gVz...",
    "channel_name": "call_1234567890",
    "call_type": "AUDIO",
    "status": "CONNECTING"
  },
  "receiver": {
    "name": "Ananya798",
    "profile_image": "https://..."
  }
}
```

---

## 🔍 Validations Performed

The endpoint automatically validates:
1. ✅ User authentication (Bearer token)
2. ✅ Creator exists and is valid
3. ✅ Creator is currently online
4. ✅ Call type (audio/video) is enabled for creator
5. ✅ User has sufficient coin balance
6. ✅ Generates secure Agora token for WebRTC

---

## 💰 Call Rates

| Call Type | Rate | UI Display |
|-----------|------|------------|
| Audio | 10 coins/min | 10/min |
| Video | 60 coins/min | 60/min |

*Note: Rates are configurable in admin panel*

---

## 🎬 Integration Steps

### 1. When User Clicks Call Button
```javascript
// Get creator ID from the profile
const creatorId = "USR_1234567890";
const callType = "AUDIO"; // or "VIDEO"

// Call the API
const response = await fetch('https://api.onlycare.app/v1/calls/initiate', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${userToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    receiver_id: creatorId,
    call_type: callType
  })
});

const data = await response.json();
```

### 2. Handle Response
```javascript
if (data.success) {
  // Use Agora credentials to connect
  const { agora_token, channel_name, id } = data.call;
  
  // Connect to Agora
  await connectToAgoraChannel(agora_token, channel_name);
  
  // Navigate to call screen
  navigation.navigate('CallScreen', { 
    callId: id,
    creatorName: data.receiver.name,
    creatorImage: data.receiver.profile_image
  });
} else {
  // Handle errors
  switch (data.error.code) {
    case 'INSUFFICIENT_COINS':
      navigation.navigate('Recharge');
      break;
    case 'USER_OFFLINE':
      Alert.alert('Offline', 'Creator is not online');
      break;
    case 'CALL_NOT_AVAILABLE':
      Alert.alert('Unavailable', 'This call type is not available');
      break;
    default:
      Alert.alert('Error', data.error.message);
  }
}
```

### 3. Connect to Agora (Example)
```javascript
import AgoraRTC from 'react-native-agora';

const connectToAgoraChannel = async (token, channelName) => {
  const client = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' });
  
  await client.join(
    AGORA_APP_ID,
    channelName,
    token,
    null
  );
  
  // Create local tracks
  const [audioTrack, videoTrack] = await AgoraRTC.createMicrophoneAndCameraTracks();
  
  // Publish tracks
  await client.publish([audioTrack, videoTrack]);
  
  return client;
};
```

---

## 🧪 Testing

### Option 1: Web Interface (Recommended)
1. Open browser: `http://your-domain.com/api-docs`
2. Click **"Initiate Call"** in the left sidebar
3. Fill in the test form:
   - **Authorization Token**: Paste your access token
   - **Creator ID**: Enter a valid creator ID (e.g., USR_1234567890)
   - **Call Type**: Select AUDIO or VIDEO
4. Click **"Initiate Call"** button
5. View response in the panel below

### Option 2: cURL Command
```bash
curl -X POST http://your-domain.com/api/v1/calls/initiate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO"
  }'
```

### Option 3: Postman
1. Create new POST request
2. URL: `http://your-domain.com/api/v1/calls/initiate`
3. Headers:
   - `Authorization`: `Bearer YOUR_ACCESS_TOKEN`
   - `Content-Type`: `application/json`
4. Body (raw JSON):
```json
{
  "receiver_id": "USR_1234567890",
  "call_type": "AUDIO"
}
```

---

## 📱 UI Elements on Creator Profile

Based on the screenshot you provided, the UI shows:

### Creator Card Elements
- **Creator Name**: Ananya798, Nandini043, etc.
- **Location**: Kannada
- **Interest Tag**: Travel, Music, Art
- **Bio/Description**: Short text
- **Call Buttons**:
  - 🟣 **Purple Audio Button**: "📞 10/min"
  - 🟢 **Green Video Button**: "📹 60/min"

### When to Show Buttons
```javascript
// Show buttons only if:
if (creator.is_online && 
    creator.audio_call_enabled && 
    creator.video_call_enabled) {
  // Show both buttons enabled
} else if (creator.is_online && creator.audio_call_enabled) {
  // Show only audio button
} else {
  // Show disabled/grayed out buttons
}
```

---

## 🔄 Complete Call Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│ 1. User clicks call button on creator profile          │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 2. POST /calls/initiate                                 │
│    - Validates user has coins                           │
│    - Checks creator is online                           │
│    - Creates call record                                │
│    - Generates Agora token                              │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 3. Returns agora_token + channel_name                   │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 4. App connects to Agora channel                        │
│    Shows "Calling..." screen                            │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 5. Creator receives notification (WebSocket/Push)       │
│    Can accept or reject                                 │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 6. If accepted: POST /calls/{id}/accept                 │
│    Both users see active call screen                    │
│    Timer starts                                         │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 7. Call in progress (Agora handles audio/video)         │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 8. Either party ends call                               │
│    POST /calls/{id}/end with duration                   │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 9. Backend:                                             │
│    - Calculates coins (duration × rate)                 │
│    - Deducts from caller                                │
│    - Adds to creator                                    │
│    - Creates transaction records                        │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────────┐
│ 10. App shows call summary                              │
│     - Duration                                          │
│     - Coins spent                                       │
│     - Rating prompt                                     │
└─────────────────────────────────────────────────────────┘
```

---

## ⚠️ Error Scenarios

### 1. Insufficient Coins
```json
{ "error": { "code": "INSUFFICIENT_COINS" } }
```
**Action**: Show recharge screen with current balance

### 2. Creator Offline
```json
{ "error": { "code": "USER_OFFLINE" } }
```
**Action**: Alert "Creator is offline. Try again later."

### 3. Call Type Disabled
```json
{ "error": { "code": "CALL_NOT_AVAILABLE" } }
```
**Action**: Alert "Video calls not available. Try audio call."

### 4. Creator Not Found
```json
{ "error": { "code": "NOT_FOUND" } }
```
**Action**: Alert "Creator profile not found"

---

## 📚 Documentation Files

| File | Description | Size |
|------|-------------|------|
| `CALL_INITIATION_API_GUIDE.md` | Complete integration guide | 7 KB |
| `CALL_API_QUICK_REFERENCE.md` | Quick reference | 4 KB |
| `CALL_INITIATION_COMPLETE_SUMMARY.md` | This summary | 8 KB |
| Web docs: `/api-docs` | Interactive documentation | Web UI |

---

## 🔗 Related Endpoints

| Endpoint | Purpose |
|----------|---------|
| `POST /calls/initiate` | **Start call** |
| `POST /calls/{id}/accept` | Accept incoming call |
| `POST /calls/{id}/reject` | Reject incoming call |
| `POST /calls/{id}/end` | End ongoing call |
| `POST /calls/{id}/rate` | Rate completed call |
| `GET /calls/history` | Get call history |
| `GET /calls/recent-sessions` | Get recent sessions |
| `GET /calls/recent-callers` | Get recent callers (female only) |

All endpoints are documented at: `http://your-domain.com/api-docs`

---

## 🎯 Next Steps for Mobile App Development

1. **Implement UI**:
   - Add call buttons to creator profile cards
   - Show online status indicators
   - Display call rates (10/min, 60/min)

2. **Integrate API**:
   - Call `/calls/initiate` when user clicks call button
   - Handle all error responses
   - Store call ID for subsequent operations

3. **Setup Agora**:
   - Install Agora SDK
   - Configure with your Agora App ID
   - Use returned token and channel name

4. **Build Call Screen**:
   - Show creator info
   - Display call timer
   - Mute/unmute controls
   - Camera controls (for video)
   - End call button

5. **Handle Call End**:
   - Send duration to `/calls/{id}/end`
   - Show call summary
   - Prompt for rating

6. **Test Thoroughly**:
   - Test with insufficient coins
   - Test with offline creators
   - Test call quality
   - Test reconnection scenarios

---

## ✅ Verification Checklist

- [x] Backend endpoint exists and is functional
- [x] Route is registered in `/routes/api.php`
- [x] Web API documentation is complete
- [x] Interactive test form works
- [x] Code examples are provided
- [x] Error responses are documented
- [x] Integration guide is comprehensive
- [x] Quick reference is created
- [x] No linting errors
- [x] All validations are in place

---

## 🎉 Status: COMPLETE

The call initiation endpoint is **production-ready** and fully documented. You can now integrate it into your mobile app. All backend work is complete!

---

## 📞 Support

If you need help with integration:
1. Check the comprehensive guide: `CALL_INITIATION_API_GUIDE.md`
2. Use the quick reference: `CALL_API_QUICK_REFERENCE.md`
3. Test using web interface: `http://your-domain.com/api-docs`
4. Review controller code: `/app/Http/Controllers/Api/CallController.php`

---

**Created:** November 4, 2024  
**Status:** Production Ready ✅  
**Backend Version:** 1.0  
**Documentation Version:** 1.0







