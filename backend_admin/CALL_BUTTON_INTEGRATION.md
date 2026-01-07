# Call Button Integration - Visual Guide

## 📱 Your App UI (From Screenshot)

```
┌──────────────────────────────────────────────┐
│  HI ma                              💰 0     │
│  Where Feelings Connect                      │
├──────────────────────────────────────────────┤
│                                              │
│  ┌────────────────────────────────────────┐ │
│  │  [Creator Photo]    Ananya798          │ │
│  │                     📍 Kannada         │ │
│  │                     ✈️ Travel          │ │
│  │                     D. boss all movies │ │
│  │                                        │ │
│  │  [🟣 📞 10/min]  [⚪ 📹 60/min]      │ │
│  └────────────────────────────────────────┘ │
│                                              │
│  ┌────────────────────────────────────────┐ │
│  │  [Creator Photo]    Nandini043         │ │
│  │                     📍 Kannada         │ │
│  │                     🎵 Music           │ │
│  │                     i like talking     │ │
│  │                                        │ │
│  │  [⚪ 📞 10/min]  [🟢 📹 60/min]      │ │
│  └────────────────────────────────────────┘ │
│                                              │
└──────────────────────────────────────────────┘
```

---

## 🎯 When User Clicks Audio Button (📞 10/min)

### Step 1: User Action
```javascript
// User taps the purple audio call button
onPressAudioCall(creatorId) {
  initiateCall(creatorId, 'AUDIO');
}
```

### Step 2: API Call
```javascript
const initiateCall = async (creatorId, callType) => {
  // Show loading indicator
  setLoading(true);
  
  try {
    const response = await fetch('https://api.onlycare.app/v1/calls/initiate', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${userAccessToken}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify({
        receiver_id: creatorId,      // "USR_1234567890"
        call_type: callType           // "AUDIO"
      })
    });
    
    const data = await response.json();
    
    if (data.success) {
      // Got Agora credentials!
      connectToCall(data.call);
    } else {
      // Handle error
      handleCallError(data.error);
    }
  } catch (error) {
    Alert.alert('Error', 'Network error. Please try again.');
  } finally {
    setLoading(false);
  }
};
```

### Step 3: Backend Response
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
    "channel_name": "call_1234567890"
  },
  "receiver": {
    "name": "Ananya798",
    "profile_image": "https://cdn.onlycare.app/profiles/ananya798.jpg"
  }
}
```

### Step 4: Connect to Agora
```javascript
const connectToCall = async (callData) => {
  // Navigate to calling screen first
  navigation.navigate('CallingScreen', {
    callId: callData.id,
    creatorName: callData.receiver.name,
    creatorImage: callData.receiver.profile_image,
    callType: callData.call_type
  });
  
  // Connect to Agora
  const client = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' });
  
  await client.join(
    AGORA_APP_ID,
    callData.channel_name,
    callData.agora_token,
    null
  );
  
  // Create and publish audio track
  const audioTrack = await AgoraRTC.createMicrophoneAudioTrack();
  await client.publish([audioTrack]);
  
  // Save client for later use
  setAgoraClient(client);
};
```

---

## 🎬 When User Clicks Video Button (📹 60/min)

Same flow as audio, but:
```javascript
onPressVideoCall(creatorId) {
  initiateCall(creatorId, 'VIDEO');  // Changed to VIDEO
}
```

API call with `call_type: "VIDEO"` and creates video tracks:
```javascript
const [audioTrack, videoTrack] = await AgoraRTC.createMicrophoneAndCameraTracks();
await client.publish([audioTrack, videoTrack]);
```

---

## ⚠️ Error Handling in UI

### Error 1: Not Enough Coins
```javascript
const handleCallError = (error) => {
  if (error.code === 'INSUFFICIENT_COINS') {
    Alert.alert(
      'Insufficient Coins',
      `You need ${error.details.required} coins but only have ${error.details.available}.`,
      [
        { text: 'Cancel', style: 'cancel' },
        { 
          text: 'Recharge', 
          onPress: () => navigation.navigate('Wallet')
        }
      ]
    );
  }
};
```

### Error 2: Creator Offline
```javascript
if (error.code === 'USER_OFFLINE') {
  Alert.alert(
    'Creator Offline',
    'This creator is not online right now. Please try again later.',
    [{ text: 'OK' }]
  );
}
```

### Error 3: Call Type Not Available
```javascript
if (error.code === 'CALL_NOT_AVAILABLE') {
  Alert.alert(
    'Call Unavailable',
    error.message,
    [
      { text: 'OK' },
      { 
        text: 'Try Audio Call', 
        onPress: () => initiateCall(creatorId, 'AUDIO')
      }
    ]
  );
}
```

---

## 🎨 UI States for Call Buttons

### State 1: Creator Online & Calls Enabled
```javascript
<View style={styles.callButtons}>
  {/* Audio Button - Purple, Enabled */}
  <TouchableOpacity
    style={[styles.callButton, styles.audioButton]}
    onPress={() => onPressAudioCall(creator.id)}
    disabled={!creator.is_online || !creator.audio_call_enabled}
  >
    <Icon name="phone" />
    <Text>10/min</Text>
  </TouchableOpacity>
  
  {/* Video Button - Green, Enabled */}
  <TouchableOpacity
    style={[styles.callButton, styles.videoButton]}
    onPress={() => onPressVideoCall(creator.id)}
    disabled={!creator.is_online || !creator.video_call_enabled}
  >
    <Icon name="video" />
    <Text>60/min</Text>
  </TouchableOpacity>
</View>
```

### State 2: Creator Offline
```javascript
// Gray out both buttons
<View style={styles.callButtons}>
  <View style={[styles.callButton, styles.disabledButton]}>
    <Icon name="phone" color="#666" />
    <Text style={styles.disabledText}>10/min</Text>
  </View>
  
  <View style={[styles.callButton, styles.disabledButton]}>
    <Icon name="video" color="#666" />
    <Text style={styles.disabledText}>60/min</Text>
  </View>
</View>
```

### State 3: Only Audio Available
```javascript
// Audio enabled, Video grayed out
<View style={styles.callButtons}>
  <TouchableOpacity
    style={[styles.callButton, styles.audioButton]}
    onPress={() => onPressAudioCall(creator.id)}
  >
    <Icon name="phone" />
    <Text>10/min</Text>
  </TouchableOpacity>
  
  <View style={[styles.callButton, styles.disabledButton]}>
    <Icon name="video" color="#666" />
    <Text style={styles.disabledText}>Unavailable</Text>
  </View>
</View>
```

---

## 🔔 Real-time Updates (Recommended)

### WebSocket for Instant Notifications
```javascript
// Connect to WebSocket when app starts
const ws = new WebSocket('ws://api.onlycare.app/ws?token=' + accessToken);

ws.onmessage = (event) => {
  const data = JSON.parse(event.data);
  
  if (data.event === 'incoming_call') {
    // Show incoming call screen
    showIncomingCallScreen(data.data);
  }
  
  if (data.event === 'call_status') {
    // Update call status (accepted, rejected, ended)
    updateCallStatus(data.data);
  }
  
  if (data.event === 'user_status') {
    // Update creator online status in real-time
    updateCreatorStatus(data.data.user_id, data.data.is_online);
  }
};
```

---

## 📱 Complete Flow Diagram

```
┌─────────────────────┐
│  User sees creator  │
│  profile card with  │
│  call buttons       │
└──────────┬──────────┘
           │
           │ Clicks 📞 10/min or 📹 60/min
           │
           ▼
┌─────────────────────┐
│  POST /calls/       │
│  initiate           │
│  {                  │
│    receiver_id,     │
│    call_type        │
│  }                  │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Backend validates: │
│  ✓ User has coins   │
│  ✓ Creator online   │
│  ✓ Calls enabled    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Returns:           │
│  • call_id          │
│  • agora_token      │
│  • channel_name     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  App connects to    │
│  Agora channel      │
│  Shows "Calling..." │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Creator receives   │
│  notification       │
│  Can accept/reject  │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Call connected!    │
│  Audio/Video        │
│  streaming via      │
│  Agora              │
└─────────────────────┘
```

---

## 🧪 Quick Test

### Test with cURL
```bash
# Replace YOUR_ACCESS_TOKEN with a real token from login
# Replace USR_1234567890 with a real creator ID

curl -X POST http://your-domain.com/api/v1/calls/initiate \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO"
  }'
```

### Test with Web Interface
```
1. Open: http://your-domain.com/api-docs
2. Click: "Initiate Call" in sidebar
3. Fill form with real token and creator ID
4. Click "Initiate Call" button
5. See response immediately
```

---

## 📋 Implementation Checklist

### Frontend (Your Work)
- [ ] Add call button UI to creator profile cards
- [ ] Show online status indicators (green dot)
- [ ] Implement `initiateCall()` function
- [ ] Add error handling for all error codes
- [ ] Setup Agora SDK
- [ ] Create calling screen UI
- [ ] Create active call screen UI
- [ ] Implement call end functionality
- [ ] Add call rating UI
- [ ] Test with real users

### Backend (Already Done ✅)
- [x] Endpoint exists: `POST /calls/initiate`
- [x] Validations implemented
- [x] Agora token generation
- [x] Database call record creation
- [x] Error responses properly formatted
- [x] API documented
- [x] Test interface available

---

## 🎯 Key Points to Remember

1. **Always check online status** before enabling call buttons
2. **Show current coin balance** near call buttons
3. **Validate on frontend** before API call (save API calls)
4. **Handle all error codes** with user-friendly messages
5. **Store call_id** for subsequent operations (end, rate)
6. **Track call duration** accurately for billing
7. **Test thoroughly** with different scenarios

---

## 📞 API Quick Reference

```javascript
// Initiate Call
POST /api/v1/calls/initiate
Body: { receiver_id, call_type }
Returns: { call: { id, agora_token, channel_name } }

// Accept Call (for receiver)
POST /api/v1/calls/{callId}/accept
Returns: { call: { agora_token, channel_name } }

// End Call
POST /api/v1/calls/{callId}/end
Body: { duration: 180 }
Returns: { call: { coins_spent, duration } }

// Rate Call
POST /api/v1/calls/{callId}/rate
Body: { rating: 5, feedback: "Great!" }
Returns: { success: true }
```

---

## 🚀 Ready to Integrate!

Everything is set up on the backend. The endpoint is production-ready and fully tested. Just integrate it into your mobile app following the examples above!

For complete details, see:
- **Full Guide**: `CALL_INITIATION_API_GUIDE.md`
- **Quick Ref**: `CALL_API_QUICK_REFERENCE.md`
- **Web Docs**: `http://your-domain.com/api-docs`

---

**Status:** ✅ Production Ready  
**Last Updated:** November 4, 2024







