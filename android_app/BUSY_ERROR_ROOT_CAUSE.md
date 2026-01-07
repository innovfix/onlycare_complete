# Root Cause Analysis: "User is currently on another call" Error

## 🔍 Summary
**This is an API ERROR, NOT an Agora error**

The error occurs BEFORE Agora is even initialized. The backend API is rejecting the call request.

---

## 📋 Error Flow

```
User clicks "Call" button
         ↓
Navigate to CallConnectingScreen
         ↓
Load user data (isOnline, audioCallEnabled, etc.)
         ↓
✅ Check if user is online (local validation)
         ↓
✅ Check if call type is enabled (local validation)
         ↓
✅ Check caller's wallet balance (API call)
         ↓
🚨 API CALL: POST /calls/initiate
   Request: { receiverId, callType }
         ↓
❌ Backend API Response: ERROR
   Error message contains "busy"
         ↓
App parses error and shows:
"❌ Failed to Initiate Call
User is currently on another call
Please try again or contact support."
         ↓
AGORA IS NEVER INITIALIZED ❌
```

---

## 🔧 Technical Details

### 1. **API Endpoint**
```
POST https://onlycare.in/api/v1/calls/initiate
```

**Request Body:**
```json
{
  "receiverId": "user_123",
  "callType": "AUDIO" // or "VIDEO"
}
```

### 2. **Error Detection Code**

**File:** `CallConnectingViewModel.kt` (Lines 209-241)

```kotlin
}.onFailure { error ->
    // Parse error message for specific issues
    val errorMsg = error.message ?: "Unknown error"
    val detailedError = when {
        errorMsg.contains("busy", ignoreCase = true) -> 
            "❌ User is Busy\n\nThe receiver is currently on another call.\n\nPlease try again in a few minutes."
        
        // ... other error cases ...
        
        else -> 
            "❌ Failed to Initiate Call\n\n$errorMsg\n\nPlease try again or contact support."
    }
    
    _state.update {
        it.copy(
            isConnecting = false,
            error = detailedError
        )
    }
}
```

### 3. **API Implementation**

**File:** `ApiDataRepository.kt` (Lines 499-601)

```kotlin
suspend fun initiateCall(receiverId: String, callType: CallType): Result<InitiateCallResponse> {
    return try {
        val request = InitiateCallRequest(
            receiverId = receiverId,
            callType = callType.name
        )
        
        Log.d(TAG, "Initiating call - receiver: $receiverId, type: ${callType.name}")
        
        val response = callApiService.initiateCall(request)
        
        if (response.isSuccessful) {
            // Handle success...
            Result.success(body)
        } else {
            // Parse error response from backend
            val errorBodyString = response.errorBody()?.string() ?: ""
            // Extract error message from JSON response
            Result.failure(Exception(errorMsg))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Failed to initiate call: ${e.message}"))
    }
}
```

---

## 🎯 What's Actually Happening

### Backend API Checks (Server-Side)

When you call `POST /calls/initiate`, the **backend server** is performing these checks:

1. ✅ **Authentication** - Is the caller authenticated?
2. ✅ **Receiver exists** - Does the receiver user exist?
3. ✅ **Receiver is online** - Is the receiver currently online?
4. ✅ **Call type enabled** - Does receiver have audio/video calls enabled?
5. ✅ **Sufficient balance** - Does caller have enough coins?
6. 🚨 **Receiver availability** - Is the receiver ALREADY on another active call?

**If ANY of these checks fail, the API returns an error response.**

---

## 🔍 Root Cause: Backend Busy Check

The backend API is detecting that the receiver is **already on an active call** and returning an error response like:

```json
{
  "success": false,
  "error": {
    "message": "User is busy on another call"
  }
}
```

OR

```json
{
  "success": false,
  "message": "Receiver is busy"
}
```

The app detects the word **"busy"** in the error message and displays:
> "User is currently on another call"

---

## ✅ Is This Check Correct?

**YES!** This is actually a GOOD validation that prevents:
- ❌ Creating multiple simultaneous calls to the same user
- ❌ Wasting Agora resources
- ❌ Confusing the receiver with multiple incoming calls
- ❌ Call state conflicts

---

## 🐛 Potential Issues

### Issue 1: **Stale Call State on Backend**

If the backend thinks a call is still active when it's not, it will block new calls.

**Possible causes:**
- Previous call didn't end properly (network disconnect, app crash)
- Call end API (`POST /calls/{callId}/end`) was never called
- Backend call timeout mechanism not working
- Database has stale "active" call records

### Issue 2: **Race Condition**

If the previous call JUST ended but the backend hasn't updated the call status yet, a new call attempt might fail.

### Issue 3: **Testing with Same Account**

If you're testing by calling from one device to another while the receiver is already on a call, this error is EXPECTED behavior.

---

## 🔬 Debugging Steps

### Step 1: Check Backend Logs

Look at the backend server logs when this error occurs:

```bash
# What to look for:
- POST /calls/initiate request
- Receiver ID
- "busy" or "already on call" error messages
- Active call records for that receiver
```

### Step 2: Check Database Call Records

Query the database for active calls for this receiver:

```sql
SELECT * FROM calls 
WHERE receiver_id = 'USER_ID' 
AND status IN ('ringing', 'active', 'connecting')
ORDER BY created_at DESC;
```

### Step 3: Add More Detailed Logging

Modify the app to log the FULL error response:

**In `ApiDataRepository.kt` line ~537:**

```kotlin
} else {
    // Parse error response
    val errorBodyString = response.errorBody()?.string() ?: ""
    Log.e(TAG, "❌ FULL ERROR RESPONSE:")
    Log.e(TAG, "   HTTP Code: ${response.code()}")
    Log.e(TAG, "   Error Body: $errorBodyString")
    Log.e(TAG, "   Headers: ${response.headers()}")
    
    // ... rest of error handling
}
```

### Step 4: Test Call End Flow

Ensure calls are properly ended:

1. Make a call
2. End the call normally
3. Check that `POST /calls/{callId}/end` is called
4. Wait 5 seconds
5. Try making another call

If it still fails, the backend call cleanup logic has issues.

---

## 🛠️ Potential Solutions

### Solution 1: Backend Call Timeout (Recommended)

**On the backend**, implement automatic call cleanup:

```javascript
// Pseudo-code for backend
async function cleanupStaleCalls() {
  const staleTimeout = 2 * 60 * 1000; // 2 minutes
  
  const staleCalls = await Call.find({
    status: { $in: ['ringing', 'active', 'connecting'] },
    updatedAt: { $lt: Date.now() - staleTimeout }
  });
  
  for (const call of staleCalls) {
    call.status = 'ended';
    call.endedAt = Date.now();
    await call.save();
    console.log(`Auto-ended stale call: ${call._id}`);
  }
}

// Run every 30 seconds
setInterval(cleanupStaleCalls, 30000);
```

### Solution 2: Force End Previous Call (Client-Side)

Before initiating a new call, force end any active calls for this receiver:

```kotlin
// In CallConnectingViewModel.kt
private suspend fun forceEndActiveCalls(receiverId: String) {
    try {
        // Get active calls for this receiver
        val activeCalls = repository.getActiveCallsForReceiver(receiverId)
        
        // End each one
        activeCalls.forEach { call ->
            repository.endCall(call.id, duration = 0)
            Log.d(TAG, "Force ended stale call: ${call.id}")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to force end calls: ${e.message}")
    }
}

// Call before initiating new call
suspend fun checkBalanceAndInitiateCall(...) {
    // ... existing validation ...
    
    // Force end any stale calls
    forceEndActiveCalls(receiverId)
    
    // Now initiate new call
    initiateCallInternal(receiverId, callType, onSuccess)
}
```

### Solution 3: Better Error Message

Improve the error message to help with debugging:

```kotlin
errorMsg.contains("busy", ignoreCase = true) -> 
    """
    ❌ User is Busy
    
    The receiver is currently on another call.
    
    What to do:
    • Wait a few minutes and try again
    • Ask them to end their current call
    • Contact support if this persists
    
    Technical details:
    Receiver: $receiverId
    Error: $errorMsg
    """.trimIndent()
```

---

## 📊 Quick Answer to Your Questions

| Question | Answer |
|----------|--------|
| **Is this API error or Agora error?** | **API ERROR** - Backend rejects the call request |
| **When does it happen?** | BEFORE Agora initialization |
| **Where is the check?** | Backend API: `POST /calls/initiate` |
| **What is being checked?** | If receiver is already on an active call |
| **Is this a bug?** | NO - It's a valid backend validation |
| **Why does it fail?** | Backend database has active call record for receiver |
| **How to fix?** | Backend needs call cleanup/timeout mechanism |

---

## 🎬 Next Steps

1. **Check backend logs** when error occurs - What exact error is returned?
2. **Query database** for active call records for that receiver
3. **Test call end flow** - Ensure calls are properly ended in database
4. **Implement backend timeout** - Auto-end stale calls after 2 minutes
5. **Add detailed logging** - Log full API error responses for debugging

---

## 📝 Conclusion

**This error is WORKING AS DESIGNED.** 

The backend API is correctly preventing multiple simultaneous calls to the same receiver. The issue is likely:

1. **Stale call records** in the backend database (previous calls didn't end properly)
2. **Missing call cleanup** mechanism on the backend
3. **Testing scenario** where receiver is actually on another call

**Fix Location:** Backend server (Node.js/PHP/Python - wherever your API is hosted)

**App Code:** Working correctly - it's just displaying the error from the backend




