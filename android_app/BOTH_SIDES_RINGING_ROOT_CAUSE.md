# BOTH Sides Stuck on "Ringing" Screen - ROOT CAUSE ANALYSIS

## 🐛 Problem Description

**Issue**: When the receiver accepts an incoming call, **BOTH** the caller AND receiver remain stuck on the "Ringing" screen. Neither side transitions to the "Connected" screen with call controls.

**User Report**: "Receiver accepts call → receiver sees Ringing → caller ALSO sees Ringing"

---

## 🔍 ROOT CAUSE FOUND

### The Critical Missing Data: Agora Token & Channel Name

The backend's `GET /calls/incoming` API endpoint is **NOT returning** the `agora_token` and `channel_name` fields that the receiver needs to join the call!

### Data Flow Breakdown

#### Step 1: Caller Initiates Call

**File**: `CallConnectingViewModel.kt` → `repository.initiateCall()`

```kotlin
POST /calls/initiate
Response: InitiateCallResponse {
    success: true,
    call: CallDto { id: "123", ... },
    agora_token: "abc123xyz...",  // ✅ Caller gets token
    channel_name: "call_123",      // ✅ Caller gets channel
    balance_time: "90:00"
}
```

✅ **Caller has token & channel** → Joins Agora successfully → Shows "Ringing" screen waiting for receiver

---

#### Step 2: Receiver Gets Incoming Call Notification

**File**: `FemaleHomeViewModel.kt` line 185 → `repository.getIncomingCalls()`

```kotlin
GET /calls/incoming
Response: ApiResponse<List<IncomingCallDto>> {
    success: true,
    data: [
        {
            id: "123",
            caller_id: "user_1",
            caller_name: "John Doe",
            call_type: "AUDIO",
            status: "ringing",
            agora_token: null,      // ❌ MISSING!
            channel_name: null      // ❌ MISSING!
        }
    ]
}
```

**IncomingCallDto Structure** (`CallDto.kt` lines 164-191):
```kotlin
data class IncomingCallDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("caller_id")
    val callerId: String,
    
    @SerializedName("caller_name")
    val callerName: String,
    
    @SerializedName("caller_image")
    val callerImage: String? = null,
    
    @SerializedName("call_type")
    val callType: String, // "AUDIO" or "VIDEO"
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("agora_token")
    val agoraToken: String? = null,     // ⚠️ NULLABLE - often NULL from backend!
    
    @SerializedName("channel_name")
    val channelName: String? = null     // ⚠️ NULLABLE - often NULL from backend!
)
```

❌ **Receiver has NO token & NO channel** → Cannot join Agora!

---

#### Step 3: Receiver Accepts Call

**File**: `FemaleHomeScreen.kt` lines 82-101

```kotlin
onClick = {
    viewModel.acceptIncomingCall(
        onSuccess = {
            val route = Screen.AudioCall.createRoute(
                userId = call.callerId,
                callId = call.id,
                token = call.agoraToken ?: "",      // ❌ NULL becomes ""
                channel = call.channelName ?: ""    // ❌ NULL becomes ""
            )
            navController.navigate(route)
        }
    )
}
```

❌ **Empty token and channel passed to AudioCallScreen!**

---

#### Step 4: AudioCallScreen Validation FAILS

**File**: `AudioCallScreen.kt` lines 62-67

```kotlin
// Validate we have token and channel before proceeding
if (token.isEmpty() || channel.isEmpty()) {
    android.util.Log.e("AudioCallScreen", "❌ Missing credentials...")
    viewModel.setError("❌ Invalid Call Setup\n\nMissing call credentials...")
    return@LaunchedEffect  // ⚠️ STOPS HERE! Never joins Agora!
}

// This code is never reached:
if (audioPermission.status.isGranted) {
    viewModel.initializeAndJoinCall(token, channel)
}
```

❌ **Receiver NEVER joins Agora channel** because validation fails!

---

#### Step 5: Caller Waits Forever

**File**: `AudioCallViewModel.kt` lines 174-180

```kotlin
override fun onUserJoined(uid: Int) {
    Log.i(TAG, "👤 Remote user joined: $uid")
    connectionTimeoutJob?.cancel()
    _state.update { 
        it.copy(
            remoteUserJoined = true,  // This changes UI from "Ringing" to "Connected"
            waitingForReceiver = false
        ) 
    }
}
```

❌ **This callback NEVER fires** because receiver never joined the channel!

---

## 📊 Visual Flow Diagram

```
CALLER SIDE                    BACKEND                      RECEIVER SIDE
────────────                   ────────                     ─────────────

1. POST /calls/initiate
   ────────────────────────────────>
                               Creates call in DB
                               Generates Agora token
                               Creates channel name
   <────────────────────────────────
   ✅ Gets: token, channel

2. Join Agora channel
   (token: "abc123", channel: "call_123")
   ✅ Joined successfully
   📱 Shows "Ringing" screen
   ⏳ Waiting for receiver...

                               3. GET /calls/incoming (polling)
                               <────────────────────────────────
                               Returns: IncomingCallDto
                               ❌ agora_token: null
                               ❌ channel_name: null
                               ────────────────────────────────>
                                                              ❌ Receives incomplete data

                                                              4. User clicks "Accept"
                                                              POST /calls/{id}/accept
                                                              ────────────────────────────────>
                               Updates call status to "accepted"
                               ❌ Still doesn't send token/channel!
                               <────────────────────────────────
                               Returns: success
                                                              
                                                              5. Navigate to AudioCallScreen
                                                              token = ""  ❌ EMPTY!
                                                              channel = "" ❌ EMPTY!
                                                              
                                                              6. Validation FAILS
                                                              ❌ Shows error dialog
                                                              ❌ NEVER joins Agora
                                                              📱 Shows "Ringing" screen (error state)

⏳ Still waiting...
📱 Still shows "Ringing"
❌ onUserJoined() never fires                                 
❌ STUCK FOREVER                                              ❌ STUCK FOREVER
```

---

## 💡 The Complete Problem Chain

1. **Backend Issue**: `GET /calls/incoming` endpoint doesn't include `agora_token` and `channel_name` in the response
2. **Missing Data**: `IncomingCallDto` has null values for token and channel
3. **Navigation with Empty Strings**: FemaleHomeScreen passes `""` for token and channel
4. **Validation Failure**: AudioCallScreen rejects empty credentials and shows error
5. **No Agora Join**: Receiver never calls `initializeAndJoinCall()`
6. **No Callback**: Caller's `onUserJoined()` never fires
7. **Both Stuck**: Both sides remain on "Ringing" screen indefinitely

---

## ✅ SOLUTION OPTIONS

### Option 1: Fix Backend API (RECOMMENDED) ⭐

**What**: Update the backend's `GET /calls/incoming` endpoint to include `agora_token` and `channel_name`.

**Backend Change Required**:
```javascript
// Backend: /api/calls/incoming endpoint
app.get('/calls/incoming', async (req, res) => {
    const calls = await Call.find({
        receiver_id: req.user.id,
        status: 'ringing'
    });
    
    // ✅ Include token and channel for each call
    const enrichedCalls = calls.map(call => ({
        id: call._id,
        caller_id: call.caller_id,
        caller_name: call.caller_name,
        caller_image: call.caller_image,
        call_type: call.call_type,
        status: call.status,
        created_at: call.created_at,
        agora_token: call.agora_token,      // ✅ ADD THIS!
        channel_name: call.channel_name      // ✅ ADD THIS!
    }));
    
    res.json({ success: true, data: enrichedCalls });
});
```

**Why This is Best**:
- ✅ Fixes the root cause
- ✅ No app changes needed
- ✅ Works for all clients (iOS, Android, Web)
- ✅ Proper data flow

---

### Option 2: Fetch Call Details After Accept (WORKAROUND)

**What**: After accepting the call, make an additional API call to get the full call details including token and channel.

**App Changes Required**:

#### 2a. Add New API Endpoint (Backend)
```javascript
// Backend: GET /api/calls/:callId endpoint
app.get('/calls/:callId', async (req, res) => {
    const call = await Call.findById(req.params.callId);
    
    res.json({
        success: true,
        data: {
            id: call._id,
            caller_id: call.caller_id,
            receiver_id: call.receiver_id,
            call_type: call.call_type,
            status: call.status,
            agora_token: call.agora_token,
            channel_name: call.channel_name,
            // ... other fields
        }
    });
});
```

#### 2b. Update Accept Flow (App)
**File**: `FemaleHomeViewModel.kt`

```kotlin
fun acceptIncomingCall(onSuccess: (token: String, channel: String) -> Unit, onError: (String) -> Unit) {
    val call = _state.value.incomingCall ?: return
    val callId = call.id
    
    Log.d("FemaleHome", "✅ Accepting call: $callId")
    
    viewModelScope.launch {
        // Step 1: Accept the call
        val acceptResult = repository.acceptCall(callId)
        
        acceptResult.onSuccess {
            Log.d("FemaleHome", "✅ Call accepted, fetching call details...")
            
            // Step 2: Fetch full call details (including token & channel)
            val callDetailsResult = repository.getCallById(callId)
            
            callDetailsResult.onSuccess { callDto ->
                val token = callDto.agoraToken ?: ""
                val channel = callDto.channelName ?: ""
                
                if (token.isEmpty() || channel.isEmpty()) {
                    onError("Missing call credentials from server")
                    return@onSuccess
                }
                
                // Dismiss dialog
                _state.update {
                    it.copy(
                        incomingCall = null,
                        hasIncomingCall = false,
                        processedCallIds = it.processedCallIds + callId
                    )
                }
                
                // Return token and channel
                onSuccess(token, channel)
                
            }.onFailure { error ->
                onError(error.message ?: "Failed to get call details")
            }
            
        }.onFailure { error ->
            onError(error.message ?: "Failed to accept call")
        }
    }
}
```

#### 2c. Update UI (App)
**File**: `FemaleHomeScreen.kt`

```kotlin
onClick = {
    viewModel.acceptIncomingCall(
        onSuccess = { token, channel ->
            // Now we have token and channel!
            val route = if (call.callType == "VIDEO") {
                Screen.VideoCall.createRoute(
                    userId = call.callerId,
                    callId = call.id,
                    token = token,          // ✅ Real token!
                    channel = channel       // ✅ Real channel!
                )
            } else {
                Screen.AudioCall.createRoute(
                    userId = call.callerId,
                    callId = call.id,
                    token = token,
                    channel = channel
                )
            }
            navController.navigate(route)
        },
        onError = { error ->
            Log.e("FemaleHomeScreen", "Failed to accept call: $error")
        }
    )
}
```

**Why This Works**:
- ✅ Receiver gets real token & channel
- ✅ Can join Agora successfully
- ✅ Caller's `onUserJoined()` fires
- ✅ Both transition to "Connected"

**Downsides**:
- ❌ Extra API call (slower)
- ❌ Backend still needs changes (add GET endpoint)
- ❌ More complex flow

---

### Option 3: Return Token/Channel in Accept Response

**What**: Have the backend return `agora_token` and `channel_name` in the accept call response.

**Backend Change**:
```javascript
// Backend: POST /api/calls/:callId/accept
app.post('/calls/:callId/accept', async (req, res) => {
    const call = await Call.findByIdAndUpdate(
        req.params.callId,
        { status: 'accepted' },
        { new: true }
    );
    
    // ✅ Return token and channel in accept response
    res.json({
        success: true,
        message: "Call accepted",
        data: {
            id: call._id,
            agora_token: call.agora_token,
            channel_name: call.channel_name
        }
    });
});
```

**App Changes**:

#### File: `ApiDataRepository.kt`
```kotlin
suspend fun acceptCall(callId: String): Result<CallDto> {  // Change return type
    return try {
        Log.d(TAG, "Accepting call: $callId")
        val response = callApiService.acceptCall(callId)
        
        if (response.isSuccessful && response.body()?.success == true) {
            val callDto = response.body()?.data
            if (callDto != null) {
                Log.d(TAG, "Call accepted: token=${callDto.agoraToken}, channel=${callDto.channelName}")
                Result.success(callDto)
            } else {
                Result.failure(Exception("No data in accept response"))
            }
        } else {
            val errorMsg = response.errorBody()?.string() ?: response.message()
            Result.failure(Exception("Failed to accept call: $errorMsg"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

#### File: `FemaleHomeViewModel.kt`
```kotlin
fun acceptIncomingCall(onSuccess: (token: String, channel: String) -> Unit, onError: (String) -> Unit) {
    val call = _state.value.incomingCall ?: return
    val callId = call.id
    
    viewModelScope.launch {
        val result = repository.acceptCall(callId)
        result.onSuccess { callDto ->
            val token = callDto.agoraToken ?: ""
            val channel = callDto.channelName ?: ""
            
            if (token.isEmpty() || channel.isEmpty()) {
                onError("Missing credentials in accept response")
                return@onSuccess
            }
            
            // Dismiss dialog
            _state.update {
                it.copy(
                    incomingCall = null,
                    hasIncomingCall = false,
                    processedCallIds = it.processedCallIds + callId
                )
            }
            
            onSuccess(token, channel)
        }.onFailure { error ->
            onError(error.message ?: "Failed to accept call")
        }
    }
}
```

**Why This is Good**:
- ✅ Single API call
- ✅ Fast response
- ✅ Clean flow

---

## 🎯 RECOMMENDED SOLUTION

**Option 1 (Fix Backend GET /calls/incoming) is the BEST** because:

1. ✅ **Fixes root cause**: Data is available when receiver first sees the incoming call
2. ✅ **No app logic changes**: Existing code will work once backend sends the data
3. ✅ **Simplest**: Just add 2 fields to backend response
4. ✅ **Most efficient**: No extra API calls
5. ✅ **Future-proof**: Works for all scenarios (accept, auto-answer, etc.)

---

## 🧪 Testing After Fix

Once the backend includes `agora_token` and `channel_name` in the incoming calls response:

1. ✅ Caller initiates call → sees "Ringing" → waiting
2. ✅ Receiver gets notification → sees token & channel in IncomingCallDto
3. ✅ Receiver clicks Accept → navigates with real credentials
4. ✅ Receiver joins Agora channel successfully
5. ✅ Caller's `onUserJoined()` fires
6. ✅ **BOTH see "Connected" screen** ✨
7. ✅ Call timer starts
8. ✅ Mute/Speaker controls work
9. ✅ Both can hear each other

---

## 📝 Backend API Contract Required

```json
GET /api/calls/incoming

Response:
{
  "success": true,
  "data": [
    {
      "id": "call_12345",
      "caller_id": "user_abc",
      "caller_name": "John Doe",
      "caller_image": "https://...",
      "call_type": "AUDIO",
      "status": "ringing",
      "created_at": "2024-01-20T10:30:00Z",
      "agora_token": "006abc123xyz...",           // ✅ MUST BE PRESENT!
      "channel_name": "call_12345"                 // ✅ MUST BE PRESENT!
    }
  ]
}
```

**Critical**: The `agora_token` and `channel_name` must be the **SAME** values that were returned to the caller when they initiated the call. Both users must join the same channel with compatible tokens!

---

## ✅ Summary

**ROOT CAUSE**: Backend's `GET /calls/incoming` endpoint doesn't return `agora_token` and `channel_name`, causing receiver to navigate to call screen with empty credentials, fail validation, never join Agora, and leaving both sides stuck on "Ringing" screen.

**FIX**: Backend must include `agora_token` and `channel_name` in the incoming calls response.

**Files Affected**: Backend only (no app changes needed if Option 1 is used).




