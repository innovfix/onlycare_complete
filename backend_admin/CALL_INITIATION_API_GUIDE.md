# Call Initiation API - Integration Guide

## Overview
This guide explains how to integrate the audio and video call functionality in your mobile app. When users click the call buttons (audio or video) on a creator's profile, this endpoint initiates the call connection.

---

## API Endpoint

### Initiate Audio/Video Call
**Endpoint:** `POST /api/v1/calls/initiate`

**Authentication:** Required (Bearer Token)

**Description:** Initiates an audio or video call with a creator. This endpoint validates user balance, creator availability, creates a call session, and returns Agora credentials for WebRTC connection.

---

## Request Format

### Headers
```
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json
Accept: application/json
```

### Request Body
```json
{
  "receiver_id": "USR_1234567890",
  "call_type": "AUDIO"
}
```

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `receiver_id` | string | Yes | Creator's user ID (format: USR_xxxxx) |
| `call_type` | string | Yes | Type of call: "AUDIO" or "VIDEO" |

---

## Response Format

### Success Response (200 OK)
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
    "agora_token": "007eJxTYBBaM2fN8gVz5ixZsmTJkiVLlixZsmTJ...",
    "channel_name": "call_1234567890",
    "balance_time": "25:00"
  },
  "agora_app_id": "abc123xyz",
  "agora_token": "007eJxTYBBaM2fN8gVz5ixZsmTJkiVLlixZsmTJ...",
  "channel_name": "call_1234567890",
  "balance_time": "25:00",
  "receiver": {
    "name": "Ananya798",
    "profile_image": "https://cdn.onlycare.app/profiles/ananya798.jpg"
  }
}
```

### Response Fields

| Field | Description |
|-------|-------------|
| `call.id` | Unique call identifier |
| `call.caller_id` | User ID of the caller |
| `call.receiver_id` | User ID of the creator (receiver) |
| `call.call_type` | AUDIO or VIDEO |
| `call.status` | Call status: CONNECTING |
| `call.agora_token` | Agora RTC token for WebRTC connection |
| `call.channel_name` | Agora channel name for this call |
| `call.balance_time` | ⭐ **NEW** Time remaining based on balance (e.g., "25:00", "13:30", "1:40:00") |
| `agora_app_id` | Agora application ID for SDK initialization |
| `balance_time` | ⭐ **NEW** Same as call.balance_time (for convenience) |
| `receiver.name` | Creator's display name |
| `receiver.profile_image` | Creator's profile image URL |

---

## 🕐 Balance Time Feature (NEW)

The `balance_time` field shows how much call time the user can afford based on their current coin balance.

### How It Works

**Calculation:**
```
available_time = user_coin_balance / coins_per_minute
```

**Format:**
- Less than 1 hour: `"MM:SS"` (e.g., "25:00", "13:30")
- 1 hour or more: `"HH:MM:SS"` (e.g., "1:40:00", "2:00:00")

### Examples

| Coin Balance | Call Type | Cost/Min | Available Time | balance_time |
|--------------|-----------|----------|----------------|--------------|
| 250 | AUDIO | 10 | 25.0 min | `"25:00"` |
| 135 | AUDIO | 10 | 13.5 min | `"13:30"` |
| 1000 | AUDIO | 10 | 100 min | `"1:40:00"` |
| 300 | VIDEO | 60 | 5.0 min | `"5:00"` |
| 90 | VIDEO | 60 | 1.5 min | `"1:30"` |
| 7200 | VIDEO | 60 | 120 min | `"2:00:00"` |

### Mobile App Implementation

```javascript
// Parse balance_time string to seconds
function parseBalanceTime(balanceTime) {
  const parts = balanceTime.split(':');
  
  if (parts.length === 2) {
    // MM:SS format
    const [minutes, seconds] = parts.map(Number);
    return minutes * 60 + seconds;
  } else if (parts.length === 3) {
    // HH:MM:SS format
    const [hours, minutes, seconds] = parts.map(Number);
    return hours * 3600 + minutes * 60 + seconds;
  }
  
  return 0;
}

// Example: Display countdown timer
const balanceTime = response.balance_time; // "25:00"
const totalSeconds = parseBalanceTime(balanceTime); // 1500

// Start countdown
let remainingSeconds = totalSeconds;
const interval = setInterval(() => {
  remainingSeconds--;
  
  // Update UI
  const mins = Math.floor(remainingSeconds / 60);
  const secs = remainingSeconds % 60;
  updateDisplay(`${mins}:${secs.toString().padStart(2, '0')}`);
  
  // Auto-end call when time runs out
  if (remainingSeconds <= 0) {
    clearInterval(interval);
    endCall();
  }
}, 1000);
```

### Refresh Balance During Call

Use the call status endpoint to refresh balance time if user adds coins mid-call:

```
GET /api/v1/calls/{call_id}/status
```

Response includes updated `balance_time` for ongoing calls.

---

## Error Responses

### 1. Insufficient Coins (400 Bad Request)
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

**Action:** Show coin recharge screen to the user.

---

### 2. User Offline (400 Bad Request)
```json
{
  "success": false,
  "error": {
    "code": "USER_OFFLINE",
    "message": "User is not online"
  }
}
```

**Action:** Show message that creator is currently offline.

---

### 3. Call Not Available (400 Bad Request)
```json
{
  "success": false,
  "error": {
    "code": "CALL_NOT_AVAILABLE",
    "message": "Video call not available"
  }
}
```

**Action:** Show message that this call type is not available. Suggest audio call if video is unavailable.

---

### 4. Creator Not Found (404 Not Found)
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "User not found"
  }
}
```

**Action:** Show error message that creator profile no longer exists.

---

### 5. Unauthorized (401)
```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Unauthenticated"
  }
}
```

**Action:** Redirect user to login screen.

---

## Integration Flow

### Step 1: User Clicks Call Button
When user clicks on the audio (10/min) or video (60/min) call button on a creator's profile:

```javascript
// Example: React Native
const initiateCall = async (creatorId, callType) => {
  try {
    const response = await fetch('https://api.onlycare.app/v1/calls/initiate', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify({
        receiver_id: creatorId,  // e.g., "USR_1234567890"
        call_type: callType      // "AUDIO" or "VIDEO"
      })
    });
    
    const data = await response.json();
    
    if (data.success) {
      // Call initiated successfully
      connectToCall(data.call);
    } else {
      // Handle error
      handleCallError(data.error);
    }
  } catch (error) {
    console.error('Call initiation failed:', error);
  }
};
```

---

### Step 2: Connect to Agora
Use the returned `agora_token` and `channel_name` to connect to Agora RTC:

```javascript
import AgoraRTC from 'react-native-agora';

const connectToCall = async (callData) => {
  const {
    id,
    agora_token,
    channel_name,
    call_type
  } = callData;
  
  // Initialize Agora client
  const client = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' });
  
  // Join channel
  await client.join(
    AGORA_APP_ID,
    channel_name,
    agora_token,
    null
  );
  
  // Create and publish local tracks
  if (call_type === 'AUDIO') {
    const audioTrack = await AgoraRTC.createMicrophoneAudioTrack();
    await client.publish([audioTrack]);
  } else if (call_type === 'VIDEO') {
    const [audioTrack, videoTrack] = await AgoraRTC.createMicrophoneAndCameraTracks();
    await client.publish([audioTrack, videoTrack]);
  }
  
  // Navigate to call screen
  navigation.navigate('CallScreen', {
    callId: id,
    client: client,
    callType: call_type
  });
};
```

---

### Step 3: Handle Call Events

```javascript
// When receiver accepts the call
client.on('user-published', async (user, mediaType) => {
  await client.subscribe(user, mediaType);
  
  if (mediaType === 'audio') {
    const remoteAudioTrack = user.audioTrack;
    remoteAudioTrack.play();
  }
  
  if (mediaType === 'video') {
    const remoteVideoTrack = user.videoTrack;
    remoteVideoTrack.play('remote-video-container');
  }
});

// When call ends
client.on('user-left', (user) => {
  // Receiver left the call
  endCall();
});
```

---

### Step 4: End Call
When call ends, send duration to backend:

```javascript
const endCall = async (callId, duration) => {
  try {
    const response = await fetch(`https://api.onlycare.app/v1/calls/${callId}/end`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify({
        duration: duration  // in seconds
      })
    });
    
    const data = await response.json();
    
    if (data.success) {
      // Show call summary
      showCallSummary(data.call);
    }
  } catch (error) {
    console.error('Failed to end call:', error);
  }
};
```

---

## Validations Performed

The API performs the following validations before initiating a call:

1. ✅ **User Authentication**: Validates the bearer token
2. ✅ **Creator Existence**: Verifies the creator exists in the system
3. ✅ **Online Status**: Checks if creator is currently online
4. ✅ **Call Availability**: Ensures audio/video calls are enabled for the creator
5. ✅ **Coin Balance**: Validates user has sufficient coins
   - Audio: 10 coins/minute (typical rate)
   - Video: 60 coins/minute (typical rate)
6. ✅ **Agora Token Generation**: Creates secure token for WebRTC connection

---

## Call Rates

Default call rates (configurable in admin panel):

| Call Type | Rate | Example |
|-----------|------|---------|
| Audio | 10 coins/min | 5 minutes = 50 coins |
| Video | 60 coins/min | 5 minutes = 300 coins |

**Note:** Rates are per minute, rounded up. A 61-second call will be charged for 2 minutes.

---

## UI/UX Recommendations

### Before Calling
1. Show creator's online status (green dot for online)
2. Display call rates clearly (10/min for audio, 60/min for video)
3. Show user's current coin balance
4. Disable call buttons if creator is offline or calls are disabled

### During Call Initiation
1. Show loading indicator with "Connecting..." message
2. Play ringing sound
3. Show creator's name and profile picture
4. Provide cancel button to abort call

### Error Handling
1. **Insufficient coins**: Show "Recharge Now" button → Navigate to wallet screen
2. **User offline**: Show "Creator is offline" → Suggest trying later
3. **Call unavailable**: Show available call types
4. **Network error**: Show retry button

### During Call
1. Display call timer
2. Show coin consumption in real-time (optional)
3. Provide mute/unmute controls
4. Provide end call button (red, prominent)
5. For video: Camera flip and video off/on buttons

### After Call
1. Show call summary:
   - Duration
   - Coins spent
   - Creator name
   - Rating prompt (1-5 stars)
2. Option to call again
3. Add to favorites option

---

## Testing the API

### Using the Web Interface
1. Navigate to: `http://your-domain.com/api-docs`
2. Click on "Initiate Call" in the sidebar
3. Fill in the test form:
   - Authorization Token (from login/registration)
   - Creator ID (e.g., USR_1234567890)
   - Call Type (AUDIO or VIDEO)
4. Click "Initiate Call"
5. View the response in the panel

### Using cURL
```bash
curl -X POST https://api.onlycare.app/v1/calls/initiate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO"
  }'
```

### Using Postman
1. Create new POST request
2. URL: `https://api.onlycare.app/v1/calls/initiate`
3. Headers:
   - `Authorization`: `Bearer YOUR_ACCESS_TOKEN`
   - `Content-Type`: `application/json`
   - `Accept`: `application/json`
4. Body (raw JSON):
```json
{
  "receiver_id": "USR_1234567890",
  "call_type": "AUDIO"
}
```

---

## Complete Call Flow Sequence

```
User clicks call button
    ↓
POST /calls/initiate
    ↓
Backend validates:
  - User authentication
  - Creator exists & online
  - Call type available
  - Sufficient coin balance
    ↓
Backend creates call record
Backend generates Agora token
    ↓
Returns call details & Agora credentials
    ↓
App connects to Agora channel
App shows "Calling..." screen
    ↓
Receiver gets notification (WebSocket)
Receiver accepts/rejects call
    ↓
If accepted: POST /calls/{callId}/accept
  - Both users connected
  - Call timer starts
    ↓
Call in progress...
    ↓
Either party ends call
    ↓
POST /calls/{callId}/end (with duration)
    ↓
Backend calculates coins:
  - Deduct from caller
  - Add to receiver
  - Create transaction records
    ↓
Returns call summary
    ↓
App shows call summary screen
User can rate the call
```

---

## Related API Endpoints

### Accept Call (for receiver)
```
POST /api/v1/calls/{callId}/accept
```

### Reject Call (for receiver)
```
POST /api/v1/calls/{callId}/reject
```

### End Call
```
POST /api/v1/calls/{callId}/end
```
Body: `{ "duration": 180 }` (in seconds)

### Rate Call
```
POST /api/v1/calls/{callId}/rate
```
Body: `{ "rating": 5, "feedback": "Great conversation!" }`

### Get Call History
```
GET /api/v1/calls/history?page=1&limit=20
```

### Get Recent Sessions
```
GET /api/v1/calls/recent-sessions?page=1&limit=20
```

---

## Troubleshooting

### Issue: "INSUFFICIENT_COINS" error
**Solution:** Check user's coin balance before showing call buttons. Provide quick recharge option.

### Issue: "USER_OFFLINE" error
**Solution:** Show real-time online status. Refresh status before initiating call.

### Issue: Agora connection fails
**Solution:** Verify Agora App ID is configured correctly. Check Agora token is not expired.

### Issue: Call initiated but receiver doesn't receive notification
**Solution:** Implement WebSocket connection for real-time notifications. Fallback to FCM/APNS push notifications.

### Issue: Call quality is poor
**Solution:** Use Agora's network quality callback to show connection quality indicator to users.

---

## Security Considerations

1. **Token Security**: Never expose access tokens in logs or client-side code
2. **Agora Token**: Token is single-use and time-limited (default 24 hours)
3. **Rate Limiting**: API has rate limits to prevent abuse
4. **Coin Verification**: Server validates coin balance before call, and again at end
5. **Call Recording**: Ensure proper consent and privacy policy compliance if recording calls

---

## Support & Documentation

- **Full API Documentation**: `http://your-domain.com/api-docs`
- **Backend Code**: `/app/Http/Controllers/Api/CallController.php`
- **Route Definition**: `/routes/api.php` (line 87)
- **Database Model**: `/app/Models/Call.php`

---

## Summary

The call initiation endpoint is ready to use! Simply:

1. Get the creator's ID from the home screen/profile
2. POST to `/api/v1/calls/initiate` with receiver_id and call_type
3. Use the returned agora_token and channel_name to connect via Agora SDK
4. Handle the call lifecycle (accept, ongoing, end, rate)

The endpoint handles all validations, coin checking, and returns everything needed to establish the WebRTC connection.

---

**Last Updated:** November 4, 2024  
**Version:** 1.0  
**Status:** Production Ready ✅







