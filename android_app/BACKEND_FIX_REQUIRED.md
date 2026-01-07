# 🚨 URGENT: Backend API Fix Required - Incoming Calls Missing Agora Credentials

**Priority**: CRITICAL (App is broken without this fix)  
**Impact**: All incoming calls are failing - Both caller and receiver stuck on "Ringing" screen  
**Estimated Fix Time**: 10-15 minutes

---

## 📋 Executive Summary

The app's incoming call feature is currently **NOT WORKING** because the `GET /calls/incoming` API endpoint is missing two critical fields (`agora_token` and `channel_name`) in its response. Without these credentials, receivers cannot join the Agora voice/video channel, causing both users to remain stuck on the "Ringing" screen indefinitely.

---

## 🔍 The Problem

### Current Situation

When a receiver gets an incoming call notification and clicks "Accept", they need the **same** Agora credentials (token and channel name) that were given to the caller when they initiated the call. However, the incoming calls API is not providing these credentials.

### What's Happening Now

```
1. Caller initiates call
   ↓
2. POST /calls/initiate → Returns agora_token ✅ and channel_name ✅
   ↓
3. Caller joins Agora channel successfully
   ↓
4. Receiver polls GET /calls/incoming → Returns agora_token ❌ NULL and channel_name ❌ NULL
   ↓
5. Receiver clicks "Accept"
   ↓
6. Receiver tries to join Agora with empty credentials
   ↓
7. ❌ FAILS - Both users stuck on "Ringing" screen forever
```

---

## 🎯 API Endpoint That Needs Fixing

### Full Endpoint Details

```
Method:  GET
URL:     https://onlycare.in/api/v1/calls/incoming
Headers: Authorization: Bearer {user_token}
```

### Current Response (INCORRECT ❌)

```json
{
  "success": true,
  "data": [
    {
      "id": "67895d8d9c9e12a1b4e3f7c8",
      "caller_id": "67889abc1234567890abcdef",
      "caller_name": "John Doe",
      "caller_image": "https://onlycare.in/storage/profiles/john.jpg",
      "call_type": "AUDIO",
      "status": "ringing",
      "created_at": "2024-01-20T10:30:00.000Z",
      "agora_token": null,        ⚠️ THIS IS THE PROBLEM!
      "channel_name": null        ⚠️ THIS IS THE PROBLEM!
    }
  ]
}
```

### Required Response (CORRECT ✅)

```json
{
  "success": true,
  "data": [
    {
      "id": "67895d8d9c9e12a1b4e3f7c8",
      "caller_id": "67889abc1234567890abcdef",
      "caller_name": "John Doe",
      "caller_image": "https://onlycare.in/storage/profiles/john.jpg",
      "call_type": "AUDIO",
      "status": "ringing",
      "created_at": "2024-01-20T10:30:00.000Z",
      "agora_token": "006abc123xyz789...longtokenstring",     ✅ MUST INCLUDE THIS!
      "channel_name": "call_67895d8d9c9e12a1b4e3f7c8"         ✅ MUST INCLUDE THIS!
    }
  ]
}
```

---

## 🛠️ What Needs to Be Changed

### Backend Code Changes Required

**Location**: Your Laravel/Node.js backend - wherever the `GET /calls/incoming` endpoint is defined

### Option 1: Using Laravel (if PHP backend)

```php
// Example: app/Http/Controllers/CallController.php

public function getIncomingCalls(Request $request)
{
    $userId = $request->user()->id;
    
    $calls = Call::where('receiver_id', $userId)
        ->where('status', 'ringing')
        ->with('caller:id,name,profile_image')
        ->orderBy('created_at', 'desc')
        ->get();
    
    $formattedCalls = $calls->map(function ($call) {
        return [
            'id' => $call->id,
            'caller_id' => $call->caller_id,
            'caller_name' => $call->caller->name,
            'caller_image' => $call->caller->profile_image,
            'call_type' => $call->call_type,
            'status' => $call->status,
            'created_at' => $call->created_at->toISOString(),
            
            // ✅ ADD THESE TWO FIELDS:
            'agora_token' => $call->agora_token,        // CRITICAL!
            'channel_name' => $call->channel_name,      // CRITICAL!
        ];
    });
    
    return response()->json([
        'success' => true,
        'data' => $formattedCalls
    ]);
}
```

### Option 2: Using Node.js/Express (if JavaScript backend)

```javascript
// Example: routes/calls.js

router.get('/incoming', authenticate, async (req, res) => {
    try {
        const userId = req.user.id;
        
        const calls = await Call.find({
            receiver_id: userId,
            status: 'ringing'
        })
        .populate('caller_id', 'name profile_image')
        .sort({ created_at: -1 });
        
        const formattedCalls = calls.map(call => ({
            id: call._id,
            caller_id: call.caller_id._id,
            caller_name: call.caller_id.name,
            caller_image: call.caller_id.profile_image,
            call_type: call.call_type,
            status: call.status,
            created_at: call.created_at.toISOString(),
            
            // ✅ ADD THESE TWO FIELDS:
            agora_token: call.agora_token,        // CRITICAL!
            channel_name: call.channel_name,      // CRITICAL!
        }));
        
        res.json({
            success: true,
            data: formattedCalls
        });
        
    } catch (error) {
        res.status(500).json({
            success: false,
            message: error.message
        });
    }
});
```

---

## ⚠️ CRITICAL Requirements

### 1. Token and Channel MUST Match

The `agora_token` and `channel_name` returned in this endpoint **MUST BE THE EXACT SAME VALUES** that were:
- Generated when the call was initiated (POST /calls/initiate)
- Stored in the `calls` table in your database
- Given to the caller

**Both users MUST join the same Agora channel with compatible tokens!**

### 2. Fields Are Required, Not Optional

These fields are **NOT optional**:
- ❌ Do NOT return `null`
- ❌ Do NOT return empty string `""`
- ✅ MUST return the actual token and channel name strings

### 3. Database Schema Verification

Make sure your `calls` table has these columns and they are being populated:
```sql
calls
  ├─ id
  ├─ caller_id
  ├─ receiver_id
  ├─ call_type (AUDIO/VIDEO)
  ├─ status (ringing/accepted/rejected/ended)
  ├─ agora_token      ← MUST be saved when call is initiated
  ├─ channel_name     ← MUST be saved when call is initiated
  ├─ created_at
  └─ updated_at
```

If these columns don't exist, you need to:
1. Add them to your database schema
2. Update the `POST /calls/initiate` endpoint to save these values

---

## 🧪 How to Test the Fix

### Step 1: Make the Backend Change
Update your code as shown above and deploy/restart your server.

### Step 2: Test with API Client (Postman/Insomnia)

```bash
# Request
GET https://onlycare.in/api/v1/calls/incoming
Authorization: Bearer YOUR_TEST_USER_TOKEN

# Expected Response
{
  "success": true,
  "data": [
    {
      "id": "some_call_id",
      "caller_id": "some_user_id",
      "caller_name": "Test User",
      "caller_image": "https://...",
      "call_type": "AUDIO",
      "status": "ringing",
      "created_at": "2024-01-20T10:30:00.000Z",
      "agora_token": "006abc123xyz...",    ← VERIFY THIS IS PRESENT AND NOT NULL!
      "channel_name": "call_xyz123"         ← VERIFY THIS IS PRESENT AND NOT NULL!
    }
  ]
}
```

### Step 3: Test in the App

1. **Device A (Caller)**: Log in as a male user, initiate a call to a female user
2. **Device B (Receiver)**: Should see incoming call dialog
3. **Device B**: Click "Accept"
4. **Expected Result**: 
   - ✅ Receiver's screen should show "Connected" with timer and controls
   - ✅ Caller's screen should transition from "Ringing" to "Connected"
   - ✅ Both users can hear each other

---

## 📊 Related Endpoints for Reference

Here are the other call endpoints that ARE working correctly (for your reference):

### POST /calls/initiate (Already working ✅)
```json
Response:
{
  "success": true,
  "call": { "id": "...", "status": "ringing", ... },
  "agora_token": "006abc123...",      ← Correctly returns token
  "channel_name": "call_xyz123",       ← Correctly returns channel
  "balance_time": "90:00"
}
```

### POST /calls/{callId}/accept (Already working ✅)
```json
Response:
{
  "success": true,
  "message": "Call accepted",
  "data": { ... }
}
```

**Note**: The accept endpoint doesn't need to return token/channel because if GET /calls/incoming is fixed, the receiver already has them.

---

## 🔒 Security Considerations

### Are Agora Tokens Secure to Send?

**YES** - Agora tokens are meant to be sent to clients. They are:
- ✅ Time-limited (expire after a set duration)
- ✅ Scoped to specific channels
- ✅ Cannot be reused for other calls
- ✅ Standard practice in WebRTC applications

### Should These Be in the Response?

**ABSOLUTELY YES** - This is how all real-time communication apps work:
- Zoom, Google Meet, WhatsApp calls all send channel credentials to clients
- Agora's official documentation recommends this pattern
- Without these credentials, the receiver cannot join the call

---

## 📝 Complete Data Flow (After Fix)

```
CALLER DEVICE                    BACKEND                     RECEIVER DEVICE
─────────────                    ───────                     ───────────────

1. POST /calls/initiate
   ──────────────────────────>
                              Generate Agora token
                              Create channel name
                              Save to database:
                              - agora_token = "006abc..."
                              - channel_name = "call_123"
   <──────────────────────────
   ✅ Receive: token, channel

2. Join Agora channel
   (token, channel)
   ✅ Successfully joined
   📱 Show "Ringing" screen

                              3. GET /calls/incoming (polling)
                              <──────────────────────────────
                              Query database:
                              SELECT id, caller_id, 
                                     agora_token,     ← Include!
                                     channel_name     ← Include!
                              FROM calls
                              WHERE receiver_id = X
                              AND status = 'ringing'
                              ──────────────────────────────>
                              ✅ Return: token, channel
                                                          ✅ Receive complete data!

                                                          4. User clicks "Accept"
                                                          POST /calls/123/accept
                                                          ──────────────────────────────>
                              Update status = 'accepted'
                              <──────────────────────────────
                                                          ✅ Success response

                                                          5. Join Agora channel
                                                          (using token & channel from step 3)
                                                          ✅ Successfully joined!

6. Agora callback fires:
   onUserJoined(uid)
   ✅ Transition to "Connected"
   📱 Show call controls
                                                          📱 Show "Connected" screen
                                                          📱 Show call controls

7. ✅ BOTH USERS CONNECTED!
   🎧 Audio/Video working
   ⏱️ Call timer running
   🔇 Mute/Speaker controls active
```

---

## 🆘 Need Help?

If you encounter any issues implementing this fix, please check:

1. **Is the data in your database?**
   ```sql
   SELECT agora_token, channel_name 
   FROM calls 
   WHERE id = 'some_call_id';
   ```
   If these are NULL, the problem is in your `POST /calls/initiate` endpoint.

2. **Is your JSON serialization correct?**
   Make sure your backend framework isn't filtering out these fields.

3. **Are you using the right column names?**
   The database columns might be named differently (snake_case vs camelCase).

---

## ✅ Checklist for Backend Developer

- [ ] Located the `GET /calls/incoming` endpoint in your codebase
- [ ] Verified `agora_token` and `channel_name` exist in database
- [ ] Added these two fields to the API response
- [ ] Tested with API client (Postman) - verified fields are present and not null
- [ ] Deployed to server
- [ ] Tested with the app - incoming calls work end-to-end
- [ ] Verified both users can see "Connected" screen
- [ ] Verified audio/video works between both users

---

## 📞 Testing Credentials (If Needed)

If you need test accounts to verify the fix works:

**Test User 1 (Caller - Male)**:
- Will need credentials from your system

**Test User 2 (Receiver - Female)**:
- Will need credentials from your system

---

## 🎯 Expected Timeline

- **Understanding the issue**: 5 minutes
- **Making code changes**: 5 minutes
- **Testing**: 5 minutes
- **Deployment**: As per your process

**Total estimated time: 15-20 minutes**

---

## ❓ Questions?

If you have questions or need clarification:

1. Check if the `agora_token` and `channel_name` exist in your `calls` table
2. Review how `POST /calls/initiate` generates and saves these values
3. Make sure the GET endpoint is reading from the same database table

**The fix is simple: Just include the two missing fields in your response!** 🚀




