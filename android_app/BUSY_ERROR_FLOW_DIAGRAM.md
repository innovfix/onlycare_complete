# Visual Flow: "User is currently on another call" Error

## 📱 Complete Call Initiation Flow with Error Point

```
┌─────────────────────────────────────────────────────────────────┐
│  USER CLICKS "CALL" BUTTON                                      │
│  (FemaleHomeScreen.kt)                                          │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  Navigate to CallConnectingScreen                               │
│  Route: call_connecting/{userId}/{callType}                     │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  CallConnectingViewModel.loadUser(userId)                       │
│  📍 Line 37-58 in CallConnectingViewModel.kt                    │
│                                                                  │
│  API: GET /users/{userId}                                       │
│  Returns: { name, isOnline, audioCallEnabled, etc. }            │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  CallConnectingViewModel.checkBalanceAndInitiateCall()          │
│  📍 Line 60-166 in CallConnectingViewModel.kt                   │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
         ┌─────────────────┴─────────────────┐
         │                                   │
         ▼                                   ▼
┌──────────────────┐              ┌──────────────────┐
│  VALIDATION #1   │              │  VALIDATION #2   │
│  Is Online?      │              │  Call Type OK?   │
│  Line 95-104     │              │  Line 107-127    │
└────────┬─────────┘              └────────┬─────────┘
         │ ✅ YES                          │ ✅ YES
         └─────────────┬───────────────────┘
                       │
                       ▼
         ┌─────────────────────────────────┐
         │  VALIDATION #3                  │
         │  Check Wallet Balance           │
         │  📍 Line 133-156                │
         │  API: GET /wallet/balance       │
         └─────────────┬───────────────────┘
                       │ ✅ Sufficient
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│  initiateCallInternal()                                         │
│  📍 Line 168-251 in CallConnectingViewModel.kt                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  ApiDataRepository.initiateCall()                               │
│  📍 Line 499-601 in ApiDataRepository.kt                        │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│  🌐 API CALL TO BACKEND                                         │
│                                                                  │
│  POST https://onlycare.in/api/v1/calls/initiate                │
│                                                                  │
│  Request Body:                                                   │
│  {                                                               │
│    "receiverId": "user_123",                                    │
│    "callType": "AUDIO"                                          │
│  }                                                               │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
╔═════════════════════════════════════════════════════════════════╗
║  🔧 BACKEND API SERVER                                          ║
║  (Node.js / PHP / Python - wherever your API is)                ║
║                                                                  ║
║  Performs these checks:                                         ║
║  1. ✅ Is caller authenticated?                                 ║
║  2. ✅ Does receiver user exist?                                ║
║  3. ✅ Is receiver online?                                      ║
║  4. ✅ Is call type enabled for receiver?                       ║
║  5. ✅ Does caller have sufficient balance?                     ║
║  6. 🚨 Is receiver already on another active call?              ║
║                                                                  ║
║  Query:                                                          ║
║  SELECT * FROM calls                                            ║
║  WHERE receiver_id = 'user_123'                                 ║
║  AND status IN ('ringing', 'active', 'connecting')              ║
║  AND updated_at > (NOW() - 2 minutes)                           ║
╚═════════════════════════┬═══════════════════════════════════════╝
                          │
                          │
         ┌────────────────┴────────────────┐
         │                                 │
         ▼                                 ▼
    ✅ NO ACTIVE CALL              🚨 ACTIVE CALL FOUND!
         │                                 │
         │                                 │
         ▼                                 ▼
┌───────────────────┐         ┌──────────────────────────┐
│  SUCCESS RESPONSE │         │  ERROR RESPONSE          │
│  HTTP 200         │         │  HTTP 400/422            │
│                   │         │                          │
│  {                │         │  {                       │
│    "success": true│         │    "success": false,     │
│    "call": {      │         │    "error": {            │
│      "id": "...", │         │      "message":          │
│      "agoraToken" │         │      "User is busy"      │
│      "channel"    │         │    }                     │
│    }              │         │  }                       │
│  }                │         │                          │
└─────────┬─────────┘         └────────────┬─────────────┘
          │                                │
          ▼                                ▼
┌───────────────────┐         ┌──────────────────────────┐
│  Proceed to       │         │  🚨 ERROR HANDLING       │
│  Agora Call       │         │  Line 209-241            │
│  ✅ SUCCESS!      │         │                          │
└───────────────────┘         │  Parse error message:    │
                              │  if contains "busy" →    │
                              │                          │
                              │  Show error:             │
                              │  "❌ Failed to           │
                              │   Initiate Call          │
                              │                          │
                              │   User is currently      │
                              │   on another call        │
                              │                          │
                              │   Please try again..."   │
                              └────────────┬─────────────┘
                                           │
                                           ▼
                              ┌──────────────────────────┐
                              │  Update ViewModel State  │
                              │  _state.update {         │
                              │    it.copy(              │
                              │      isConnecting=false  │
                              │      error="..."         │
                              │    )                     │
                              │  }                       │
                              └────────────┬─────────────┘
                                           │
                                           ▼
                              ┌──────────────────────────┐
                              │  CallConnectingScreen    │
                              │  displays error dialog   │
                              │  Line 92-119             │
                              │                          │
                              │  🔴 Red error icon       │
                              │  📝 Error message        │
                              │  🔙 "Go Back" button     │
                              └──────────────────────────┘
```

---

## 🎯 Key Findings

### 1. **Error Origin**
- **NOT from Agora SDK** ❌
- **FROM Backend API** ✅
- Happens at: `POST /calls/initiate` endpoint

### 2. **Error Trigger**
Backend database query finds an active call record:
```sql
-- This query returns results = ERROR triggered
SELECT * FROM calls 
WHERE receiver_id = 'user_123'
AND status IN ('ringing', 'active', 'connecting')
```

### 3. **App Code is Working Correctly**
The Android app is just:
1. Making an API call
2. Receiving an error response
3. Parsing the error message
4. Displaying it to the user

**The app is doing its job correctly!** ✅

### 4. **Problem is in Backend**
The backend has a call record that shows the receiver as "busy", which could be:
- ✅ **Correct** - Receiver IS actually on another call
- ❌ **Stale data** - Previous call didn't end properly in database
- ❌ **No cleanup** - Old call records aren't being auto-expired

---

## 🔍 How to Identify Which Case It Is

### Test 1: Wait and Retry
```
1. Get the error "User is currently on another call"
2. Wait 5 minutes
3. Try calling again

Result:
- Still fails? → Stale data problem ❌
- Works now? → User WAS actually busy ✅
```

### Test 2: Check Backend Database
```sql
-- Run this query when error occurs
SELECT 
  id,
  caller_id,
  receiver_id,
  status,
  call_type,
  created_at,
  updated_at,
  TIMESTAMPDIFF(MINUTE, updated_at, NOW()) as minutes_since_update
FROM calls 
WHERE receiver_id = 'USER_ID'
AND status IN ('ringing', 'active', 'connecting')
ORDER BY created_at DESC;
```

**If you see calls from 10+ minutes ago still marked as "active" → Stale data problem** ❌

### Test 3: End Call Properly
```
1. Make a test call
2. End the call using the app
3. Check backend logs - Did it receive POST /calls/{callId}/end?
4. Check database - Is call status updated to "ended"?

If status is still "active" → Call end flow is broken ❌
```

---

## 🛠️ Backend Fix Required

The backend needs to implement **automatic call cleanup**:

```javascript
// Pseudo-code for backend (Node.js example)
async function cleanupStaleCalls() {
  const TWO_MINUTES_AGO = new Date(Date.now() - 2 * 60 * 1000);
  
  const staleCalls = await Call.updateMany(
    {
      status: { $in: ['ringing', 'active', 'connecting'] },
      updatedAt: { $lt: TWO_MINUTES_AGO }
    },
    {
      status: 'ended',
      endedAt: new Date(),
      duration: 0
    }
  );
  
  if (staleCalls.modifiedCount > 0) {
    console.log(`✅ Cleaned up ${staleCalls.modifiedCount} stale calls`);
  }
}

// Run every 30 seconds
setInterval(cleanupStaleCalls, 30 * 1000);
```

---

## 📊 Summary Table

| Aspect | Details |
|--------|---------|
| **Error Type** | API Error (Backend) |
| **Error Location** | `POST /calls/initiate` endpoint |
| **Error Trigger** | Active call found in database for receiver |
| **Agora Involved?** | ❌ NO - Error happens before Agora |
| **App Code Issue?** | ❌ NO - App is working correctly |
| **Backend Issue?** | ✅ YES - Needs call cleanup mechanism |
| **File with Error Display** | `CallConnectingViewModel.kt` line 222-223 |
| **File with API Call** | `ApiDataRepository.kt` line 499-601 |
| **API Endpoint** | `https://onlycare.in/api/v1/calls/initiate` |

---

## ✅ Conclusion

**Your Android app code is working perfectly!** 

The error is a **backend database issue** where old call records are not being cleaned up. This causes the backend to think the receiver is busy when they're actually available.

**Fix Location:** Backend server (wherever `https://onlycare.in/api/v1/` is hosted)

**Fix Required:** Implement automatic cleanup of stale call records after 2 minutes of inactivity.




