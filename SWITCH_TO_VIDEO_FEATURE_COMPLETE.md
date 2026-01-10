# Switch-to-Video Feature Implementation - Complete

## Overview
Implemented a seamless switch-to-video feature that allows users to upgrade from an audio call to a video call without ending the current conversation. The new video call is created immediately when the request is sent, making the switch instantaneous.

## Implementation Date
January 10, 2026

## Features

### 1. **UI Components**
- **Switch to Video Button**: Added in AudioCallScreen near the mute button (only visible in audio calls, not video calls)
- **Confirmation Dialog**: Shows when user clicks the button to confirm they want to switch
- **Request Dialog**: Shows to the receiver when they get a switch request
- **Seamless Navigation**: Automatically navigates to video call screen when accepted

### 2. **Backend API** (`CallController.php`)

#### New Endpoint: `POST /api/v1/calls/switch-to-video`
Validates and creates a new video call immediately:
- Checks if current call is AUDIO and ONGOING
- Validates male user has sufficient coins (video rate)
- Creates new VIDEO call with status PENDING
- Generates Agora token for new video channel
- Returns new call details (callId, channel, token, etc.)

```php
Route::post('/calls/switch-to-video', [CallController::class, 'requestSwitchToVideo']);
```

### 3. **Database Changes**
- Added `upgraded_from_call_id` column to `calls` table
- Tracks which audio call was upgraded to video
- Indexed for performance

Migration: `2026_01_10_120000_add_upgraded_from_call_id_to_calls_table.php`

### 4. **Android Implementation**

#### AudioCallViewModel
- `requestSwitchToVideo()`: Calls backend API → stores pending video call details → sends WebSocket request
- `acceptSwitchToVideo()`: Accepts new video call → sends WebSocket acceptance → navigates to video
- `declineSwitchToVideo()`: Cancels pre-created video call → sends WebSocket decline
- `endOldAudioCallInBackground()`: Ends audio call after switching to video

#### AudioCallScreen
- Added switch-to-video button in controls row
- Confirmation dialog for sender
- Request dialog for receiver
- LaunchedEffect to handle navigation when accepted

#### State Management
New state fields:
- `showSwitchToVideoRequestDialog`: Show dialog to receiver
- `switchToVideoAccepted`: Track if switch was accepted
- `pendingVideoCallId`: New video call ID
- `pendingVideoChannel`: New video channel
- `pendingVideoToken`: New video token
- `pendingVideoAppId`: Agora app ID
- `pendingVideoBalanceTime`: Balance time for video call
- `pendingVideoReceiverId`: Receiver user ID

### 5. **WebSocket Integration**

#### Events
- `call:upgrade` (request): Sender requests switch with new call details
- `call:upgrade:response` (accept/decline): Receiver responds

#### Updated WebSocket Methods
- `requestSwitchToVideo(oldCallId, newCallId, receiverId)`
- `acceptSwitchToVideo(oldCallId, newCallId, receiverId)`
- `declineSwitchToVideo(oldCallId, newCallId, receiverId, reason)`

#### Updated Event Classes
- `SwitchToVideoRequested`: Now includes new call details (callId, channel, token, etc.)
- `SwitchToVideoAccepted`: Includes both old and new call IDs
- `SwitchToVideoDeclined`: Includes both old and new call IDs

### 6. **API & Repository**

#### CallApiService
```kotlin
@POST("calls/switch-to-video")
fun requestSwitchToVideo(@Body request: SwitchToVideoRequest): Call<SwitchToVideoResponse>
```

#### DTOs
- `SwitchToVideoRequest`: Contains call_id
- `SwitchToVideoResponse`: Contains success, message, data, error
- `SwitchToVideoData`: Contains all new call details
- `ApiError`: For detailed error responses

#### ApiDataRepository
```kotlin
suspend fun requestSwitchToVideo(callId: String): Result<SwitchToVideoResponse>
```

## User Flow

### Happy Path (Both Users Accept)

1. **User A** (in audio call) clicks "Switch to Video" button
2. **Confirmation dialog** appears: "Switch to Video Call?"
3. User A clicks "Yes"
4. **Backend creates** new VIDEO call immediately (status: PENDING)
5. **Backend returns** new call details (callId, channel, token, etc.)
6. **WebSocket request** sent to User B with new call details
7. **User B** sees dialog: "User A wants to switch to video call"
8. User B clicks "Accept"
9. **Backend updates** new call status: PENDING → ONGOING
10. **WebSocket acceptance** sent back to User A
11. **Both users** navigate to VideoCallScreen with new call ID
12. **Old audio call** ends in background
13. **Both users** continue conversation in video call

### Decline Path

1. Steps 1-6 same as above
2. User B clicks "Decline"
3. **Backend cancels** the pre-created video call
4. **WebSocket decline** sent to User A
5. **User A sees** message: "User declined video call request"
6. **Both users** continue audio call

## Validation & Checks

### Backend Validations (`requestSwitchToVideo`)
1. ✅ Call exists
2. ✅ Requesting user is part of call
3. ✅ Call type is AUDIO
4. ✅ Call status is ONGOING (not ended)
5. ✅ Male user has sufficient coins (video_call_rate)

### No Rate Limiting
- Per user requirements, no rate limiting on switch requests
- No check if receiver's video is enabled
- Male coin balance is ALWAYS checked (regardless of who requests)

## Files Modified

### Backend
- `backend_admin/app/Http/Controllers/Api/CallController.php` - Added `requestSwitchToVideo` endpoint
- `backend_admin/routes/api.php` - Added route
- `backend_admin/database/migrations/2026_01_10_120000_add_upgraded_from_call_id_to_calls_table.php` - New migration

### Android
- `android_app/app/src/main/java/com/onlycare/app/data/remote/api/CallApiService.kt` - Added API method
- `android_app/app/src/main/java/com/onlycare/app/data/remote/dto/CallDto.kt` - Added DTOs
- `android_app/app/src/main/java/com/onlycare/app/data/repository/ApiDataRepository.kt` - Added repository method
- `android_app/app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallViewModel.kt` - Added switch logic
- `android_app/app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallScreen.kt` - Added UI button & dialogs
- `android_app/app/src/main/java/com/onlycare/app/websocket/WebSocketManager.kt` - Updated WebSocket methods
- `android_app/app/src/main/java/com/onlycare/app/websocket/WebSocketEvents.kt` - Updated event classes

## Deployment Status

### Backend (✅ Deployed)
- Files uploaded to server: `/var/www/onlycare_admin/`
- Migration run successfully
- Caches cleared (config, route, cache)
- PHP-FPM restarted (opcache cleared)

### Android (⏳ Pending)
- Code changes complete
- No linter errors
- Ready to build and deploy

## Testing Checklist

- [ ] Male initiates switch from audio to video
- [ ] Female initiates switch from audio to video
- [ ] Female accepts switch request
- [ ] Female declines switch request
- [ ] Male has insufficient coins (should show error)
- [ ] Switch during active audio call (both users talking)
- [ ] Video call continues after switch (no interruption)
- [ ] Old audio call properly ended
- [ ] Button only shows in audio calls (not in video calls)
- [ ] WebSocket fallback if one user's WebSocket is disconnected

## Known Limitations
- Requires both users to have active WebSocket connections
- If WebSocket fails, the switch won't work (no FCM fallback for switch feature)
- The pre-created video call will remain in PENDING status if receiver never responds

## Future Enhancements (Optional)
- Add notification sound when switch request is received
- Add animation for seamless transition
- Add rate limiting (if needed later)
- Add check for receiver's video enabled status (if needed later)
- Add FCM fallback for switch requests (if WebSocket issues persist)

## Notes
- Button is intentionally hidden in video calls (as requested)
- No rate limiting implemented (as requested)
- Male coin balance is always checked, regardless of who initiates (as requested)
- The switch creates a completely new call with new ID for clean separation
