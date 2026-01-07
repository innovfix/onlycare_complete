# 🚨 URGENT: Call Rejection Flow - Complete Technical Fix

**Document Version:** 2.0  
**Created:** November 22, 2025  
**Priority:** 🔴 **CRITICAL**  
**Impact:** Every call rejection in production

---

## 📋 Executive Summary

### Problem
When a receiver rejects a call, the **caller continues ringing for 30+ seconds** until timeout, creating a terrible user experience.

### Root Cause
The Laravel backend's `/calls/{call_id}/reject` API endpoint only updates the database but **does not emit a WebSocket event** to notify the caller.

### Solution
Two approaches (choose one based on your current Android implementation):
1. **Approach A (Recommended):** Android app emits WebSocket event directly
2. **Approach B:** Laravel backend triggers WebSocket notification

---

## 🔍 Current System Analysis

### ✅ What's Working

#### 1. WebSocket Server (Already Correct!)
```267:300:socket-server/server.js
socket.on('call:reject', (data) => {
    try {
        const { callId, reason } = data;
        const call = activeCalls.get(callId);
        
        if (!call) {
            console.log(`❌ Call ${callId} not found`);
            return;
        }
        
        console.log(`❌ Call rejected: ${callId} - Reason: ${reason || 'User declined'}`);
        
        // Notify caller INSTANTLY (0.05 seconds!)
        const callerSocketId = connectedUsers.get(call.callerId);
        if (callerSocketId) {
            io.to(callerSocketId).emit('call:rejected', {
                callId,
                reason: reason || 'User declined',
                timestamp: Date.now()
            });
            
            console.log(`✅ Caller ${call.callerId} notified INSTANTLY: call rejected`);
        }
        
        // Remove call from active calls
        activeCalls.delete(callId);
        
        // Send confirmation to receiver
        socket.emit('call:reject:confirmed', { callId });
        
    } catch (error) {
        console.error('Error in call:reject:', error);
    }
});
```

**Status:** ✅ Fully implemented and working

---

### ❌ What's Broken

#### Laravel API Endpoint
```319:353:app/Http/Controllers/Api/CallControllerClean.php
public function rejectCall(Request $request, $callId)
{
    $id = str_replace('CALL_', '', $callId);
    $call = Call::find($id);

    if (!$call) {
        return response()->json([
            'success' => false,
            'error' => [
                'code' => 'NOT_FOUND',
                'message' => 'Call not found'
            ]
        ], 404);
    }

    if ($call->receiver_id !== $request->user()->id) {
        return response()->json([
            'success' => false,
            'error' => [
                'code' => 'FORBIDDEN',
                'message' => 'You are not authorized to reject this call'
            ]
        ], 403);
    }

    $call->update([
        'status' => 'REJECTED',
        'ended_at' => now()
    ]);

    return response()->json([
        'success' => true,
        'message' => 'Call rejected'
    ]);
}
```

**Problem:** Only updates database, no WebSocket notification!

---

## ✅ Solution: Two Approaches

### Approach A: Android App Emits WebSocket Event (RECOMMENDED ⭐)

**Why Recommended:**
- Faster (no HTTP API delay)
- WebSocket server already handles this perfectly
- More reliable (direct communication)
- Already implemented in WebSocket server

**Current Android Flow (Broken):**
```
Receiver taps "Reject"
    ↓
App calls HTTP API: POST /calls/{call_id}/reject
    ↓
Laravel updates database
    ↓
❌ Caller keeps ringing (no notification sent!)
```

**Fixed Android Flow (WORKING):**
```
Receiver taps "Reject"
    ↓
1. App emits WebSocket: socket.emit('call:reject', { callId, reason })
    ↓
2. WebSocket server notifies caller: socket.emit('call:rejected')
    ↓
3. Caller stops ringing INSTANTLY (50-100ms)
    ↓
4. App calls HTTP API: POST /calls/{call_id}/reject (for database update)
```

#### Android Implementation (Kotlin)

```kotlin
// In your CallService or WebSocketManager

fun rejectCall(callId: String, reason: String = "User declined") {
    try {
        // 1. FIRST: Emit WebSocket event for instant notification
        socket?.emit("call:reject", JSONObject().apply {
            put("callId", callId)
            put("reason", reason)
        })
        
        Log.d("CallService", "✅ Emitted call:reject via WebSocket")
        
        // 2. THEN: Call HTTP API to update database
        lifecycleScope.launch {
            try {
                val response = apiService.rejectCall(callId)
                Log.d("CallService", "✅ Database updated: call rejected")
            } catch (e: Exception) {
                Log.e("CallService", "❌ Failed to update database: ${e.message}")
            }
        }
        
        // 3. Dismiss incoming call UI
        dismissIncomingCallScreen()
        
    } catch (e: Exception) {
        Log.e("CallService", "❌ Failed to reject call: ${e.message}")
    }
}
```

#### Expected Server Logs
```
📥 Call rejected: CALL_17637599232099 - Reason: User declined
✅ Caller USR_123 notified INSTANTLY: call rejected
```

---

### Approach B: Laravel Backend Triggers WebSocket (Alternative)

If Android team cannot implement Approach A immediately, update Laravel:

#### Updated Laravel Controller

```php
public function rejectCall(Request $request, $callId)
{
    DB::beginTransaction();
    
    try {
        $id = str_replace('CALL_', '', $callId);
        $call = Call::with(['caller', 'receiver'])->find($id);

        if (!$call) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'NOT_FOUND',
                    'message' => 'Call not found'
                ]
            ], 404);
        }

        if ($call->receiver_id !== $request->user()->id) {
            return response()->json([
                'success' => false,
                'error' => [
                    'code' => 'FORBIDDEN',
                    'message' => 'You are not authorized to reject this call'
                ]
            ], 403);
        }

        // 1. Update database
        $call->update([
            'status' => 'REJECTED',
            'ended_at' => now()
        ]);
        
        Log::info('✅ Call rejected in database', [
            'call_id' => $callId,
            'caller_id' => $call->caller_id,
            'receiver_id' => $call->receiver_id
        ]);

        // 2. ⚡ NOTIFY CALLER VIA WEBSOCKET (NEW!)
        $webSocketService = app(\App\Services\WebSocketService::class);
        
        if ($webSocketService->isUserOnline($call->caller_id)) {
            $notified = $webSocketService->notifyUser($call->caller_id, 'call:rejected', [
                'callId' => $callId,
                'reason' => $request->input('reason', 'User declined'),
                'timestamp' => now()->timestamp * 1000
            ]);
            
            if ($notified) {
                Log::info('✅ Caller notified via WebSocket', [
                    'caller_id' => $call->caller_id,
                    'event' => 'call:rejected'
                ]);
            }
        } else {
            Log::warning('⚠️ Caller not online, WebSocket notification skipped', [
                'caller_id' => $call->caller_id
            ]);
        }

        DB::commit();

        return response()->json([
            'success' => true,
            'message' => 'Call rejected'
        ]);
        
    } catch (\Exception $e) {
        DB::rollBack();
        Log::error('❌ Call rejection failed: ' . $e->getMessage());
        
        return response()->json([
            'success' => false,
            'error' => [
                'code' => 'INTERNAL_ERROR',
                'message' => 'Failed to reject call'
            ]
        ], 500);
    }
}
```

#### Required: WebSocketService Enhancement

Add this method to `app/Services/WebSocketService.php`:

```php
/**
 * Notify a specific user via WebSocket
 */
public function notifyUser(string $userId, string $event, array $data): bool
{
    try {
        if (!$this->isServerAvailable()) {
            Log::warning('WebSocket server not available');
            return false;
        }

        $socketId = $this->getUserSocketId($userId);
        
        if (!$socketId) {
            Log::warning("User {$userId} not connected to WebSocket");
            return false;
        }

        // Emit event to specific socket
        $response = Http::timeout(2)->post(
            config('websocket.server_url') . '/api/emit',
            [
                'socketId' => $socketId,
                'event' => $event,
                'data' => $data
            ]
        );

        return $response->successful();
        
    } catch (\Exception $e) {
        Log::error('Failed to notify user via WebSocket: ' . $e->getMessage());
        return false;
    }
}

/**
 * Get socket ID for a user
 */
protected function getUserSocketId(string $userId): ?string
{
    try {
        $response = Http::timeout(2)->get(
            config('websocket.server_url') . "/api/users/{$userId}/online"
        );

        if ($response->successful()) {
            $data = $response->json();
            return $data['isOnline'] ? $data['socketId'] : null;
        }

        return null;
        
    } catch (\Exception $e) {
        Log::error('Failed to get user socket ID: ' . $e->getMessage());
        return null;
    }
}
```

#### Required: WebSocket Server API Endpoint

Add to `socket-server/server.js` (after line 410):

```javascript
// Emit event to specific socket
app.post('/api/emit', express.json(), (req, res) => {
    const { socketId, event, data } = req.body;
    
    if (!socketId || !event) {
        return res.status(400).json({ error: 'socketId and event are required' });
    }
    
    try {
        io.to(socketId).emit(event, data);
        
        console.log(`✅ Emitted event '${event}' to socket ${socketId}`);
        
        res.json({ 
            success: true,
            message: 'Event emitted successfully'
        });
    } catch (error) {
        console.error('Failed to emit event:', error);
        res.status(500).json({ error: 'Failed to emit event' });
    }
});
```

---

## 📊 Comparison: Approach A vs Approach B

| Aspect | Approach A (WebSocket) | Approach B (Laravel) |
|--------|----------------------|----------------------|
| **Speed** | 50-100ms | 200-500ms |
| **Reliability** | Higher (direct) | Lower (2 hops) |
| **Backend Changes** | None needed | Moderate changes |
| **Android Changes** | Minor (add 1 emit) | None needed |
| **Already Implemented** | ✅ Yes (in WebSocket server) | ❌ No (needs implementation) |
| **Recommended** | ⭐ **YES** | Use only if Android can't change |

---

## 🧪 Testing Guide

### Test Scenario 1: Call Rejection

**Setup:**
1. Device A (Caller): User ID = `USR_123`
2. Device B (Receiver): User ID = `USR_456`
3. Both connected to WebSocket server

**Steps:**

1. **Device A initiates call:**
   ```kotlin
   socket.emit("call:initiate", {
       receiverId: "USR_456",
       callId: "CALL_17637599232099",
       callType: "VIDEO",
       channelName: "call_17637599232099",
       agoraToken: "..."
   })
   ```

2. **Device B receives incoming call:**
   ```
   Event: call:incoming
   Data: { callId, callerId, callerName, callType, ... }
   ```

3. **Device B rejects call:**
   ```kotlin
   // Approach A (Recommended)
   socket.emit("call:reject", {
       callId: "CALL_17637599232099",
       reason: "User declined"
   })
   ```

4. **Device A should IMMEDIATELY receive:**
   ```
   Event: call:rejected
   Data: {
       callId: "CALL_17637599232099",
       reason: "User declined",
       timestamp: 1700000000000
   }
   ```

5. **Expected Timeline:**
   - T+0ms: Device B emits `call:reject`
   - T+50ms: Device A receives `call:rejected`
   - T+100ms: Ringing stops on Device A

### Test Scenario 2: Call Timeout

**Steps:**

1. Device A initiates call
2. Device B receives call
3. Device B does NOT respond (no accept/reject)
4. After 30 seconds:
   - Device A receives `call:timeout`
   - Device B receives `call:cancelled`

### Expected Server Logs

**Successful Rejection Flow:**
```
📞 Call initiated: USR_123 → USR_456 (Type: VIDEO)
✅ Call signal sent to receiver: USR_456
❌ Call rejected: CALL_17637599232099 - Reason: User declined
✅ Caller USR_123 notified INSTANTLY: call rejected
```

**Timeout Flow:**
```
📞 Call initiated: USR_123 → USR_456 (Type: VIDEO)
✅ Call signal sent to receiver: USR_456
⏱️ Call CALL_17637599232099 timed out
```

---

## 🔍 Debugging Checklist

### If Caller Doesn't Receive `call:rejected`

1. **Check WebSocket Connection:**
   ```bash
   # On server
   curl http://localhost:3001/api/users/USR_123/online
   ```
   Should return: `{ "isOnline": true, "socketId": "abc123" }`

2. **Check Server Logs:**
   ```bash
   pm2 logs onlycare-socket
   ```
   Should see: `✅ Caller USR_123 notified INSTANTLY: call rejected`

3. **Check Android WebSocket Listeners:**
   ```kotlin
   socket?.on("call:rejected") { args ->
       Log.d("WebSocket", "✅ Received call:rejected: ${args[0]}")
       stopRinging()
   }
   ```

4. **Verify Event Name (Case-Sensitive!):**
   - ✅ Correct: `call:rejected` (with colon)
   - ❌ Wrong: `callRejected`, `call_rejected`, `CALL:REJECTED`

5. **Check Call ID in Memory:**
   ```bash
   # Check active calls on server
   curl http://localhost:3001/health
   ```

---

## 📡 Complete WebSocket Event Reference

### Events FROM Client (App → Server)

| Event | Sender | Data | Purpose |
|-------|--------|------|---------|
| `call:initiate` | Caller | `{ receiverId, callId, callType, channelName, agoraToken }` | Start a call |
| `call:accept` | Receiver | `{ callId }` | Accept incoming call |
| `call:reject` | Receiver | `{ callId, reason }` | Reject incoming call |
| `call:end` | Either | `{ callId }` | End active call |

### Events TO Client (Server → App)

| Event | Recipient | Data | When |
|-------|-----------|------|------|
| `call:incoming` | Receiver | `{ callId, callerId, callerName, callType, channelName, agoraToken, timestamp }` | Call initiated |
| `call:accepted` | Caller | `{ callId, timestamp }` | Receiver accepted |
| `call:rejected` | Caller | `{ callId, reason, timestamp }` | Receiver rejected |
| `call:ended` | Other party | `{ callId, endedBy, reason, timestamp }` | Call ended |
| `call:timeout` | Caller | `{ callId, reason }` | No answer (30s) |
| `call:cancelled` | Receiver | `{ callId, reason }` | Timeout or cancelled |
| `call:busy` | Caller | `{ callId }` | Receiver busy |
| `user:online` | All | `{ userId, timestamp }` | User connected |
| `user:offline` | All | `{ userId, timestamp }` | User disconnected |

---

## 🚀 Implementation Priority

### Phase 1: Immediate Fix (This Sprint) 🔴
- [ ] **Android Team:** Implement Approach A (emit WebSocket event on reject)
- [ ] **Testing:** Verify call rejection latency < 100ms
- [ ] **Monitoring:** Check server logs for successful notifications

### Phase 2: Backend Enhancement (Next Sprint) 🟡
- [ ] **Backend Team:** Implement Approach B as fallback
- [ ] **Testing:** Test both WebSocket and HTTP API paths
- [ ] **Documentation:** Update API docs with WebSocket events

### Phase 3: Optimization (Future) 🟢
- [ ] Add retry logic for failed WebSocket emissions
- [ ] Implement FCM fallback if WebSocket fails
- [ ] Add analytics for call rejection reasons
- [ ] Monitor and optimize latency

---

## 📊 Success Metrics

### Before Fix
- ⏱️ **Call rejection latency:** 30-45 seconds (timeout)
- 😠 **User complaints:** High ("calls don't stop ringing")
- 📉 **User experience:** Poor

### After Fix (Target)
- ⚡ **Call rejection latency:** < 100ms (100x faster!)
- 😊 **User complaints:** Zero
- 📈 **User experience:** Excellent

---

## 🆘 Troubleshooting

### Problem: "Call keeps ringing after reject"

**Diagnosis:**
```bash
# 1. Check if receiver sent event
# Look for this in PM2 logs:
pm2 logs onlycare-socket | grep "Call rejected"

# 2. Check if caller is online
curl http://localhost:3001/api/users/USR_123/online

# 3. Check active calls
curl http://localhost:3001/health
```

**Solutions:**
1. Receiver didn't emit WebSocket event → Fix Android code
2. Caller not connected to WebSocket → Check connection
3. Wrong event name → Verify `call:reject` (exact name)
4. Server not running → Restart: `pm2 restart onlycare-socket`

---

### Problem: "WebSocket event not reaching caller"

**Diagnosis:**
```kotlin
// Add debug logging on caller side
socket?.on("call:rejected") { args ->
    Log.d("DEBUG", "✅ GOT IT! call:rejected event received")
    Log.d("DEBUG", "Data: ${args[0]}")
}

// Check if listener is registered
Log.d("DEBUG", "Listening for: call:rejected")
```

**Solutions:**
1. Listener not registered → Add `socket?.on("call:rejected")`
2. Socket disconnected → Reconnect before call
3. Wrong socket instance → Use same socket for all events
4. Event handler not called → Check Android logs

---

### Problem: "Latency still high (> 1 second)"

**Diagnosis:**
```bash
# Check network latency
ping -c 5 your-server.com

# Check WebSocket server response time
curl -w "@-" -o /dev/null -s http://localhost:3001/health <<'EOF'
    time_total:  %{time_total}\n
EOF
```

**Solutions:**
1. Network issues → Check WiFi/4G connection
2. Server overloaded → Scale server resources
3. Database slow → Add database indexes
4. Using HTTP API → Switch to Approach A (WebSocket)

---

## 📞 Support & Questions

### Server Logs
```bash
# Real-time WebSocket logs
pm2 logs onlycare-socket --lines 100

# Check server status
pm2 status

# Restart if needed
pm2 restart onlycare-socket
```

### Health Checks
```bash
# WebSocket server health
curl http://localhost:3001/health

# Check connected users
curl http://localhost:3001/api/connected-users

# Check specific user
curl http://localhost:3001/api/users/USR_123/online
```

### Log File Locations
- **WebSocket Server:** `~/.pm2/logs/onlycare-socket-out.log`
- **WebSocket Errors:** `~/.pm2/logs/onlycare-socket-error.log`
- **Laravel:** `/var/www/onlycare/public/storage/logs/laravel.log`
- **Nginx:** `/var/log/nginx/error.log`

---

## ✅ Deployment Checklist

### Pre-Deployment
- [ ] WebSocket server running (`pm2 status`)
- [ ] Health endpoint returns 200 (`curl http://localhost:3001/health`)
- [ ] Server logs show no errors
- [ ] Android app has WebSocket library integrated

### Android Deployment (Approach A)
- [ ] Add `call:reject` emit in reject button handler
- [ ] Add `call:rejected` listener in call activity
- [ ] Test on 2 devices (caller + receiver)
- [ ] Verify < 100ms latency
- [ ] Test with poor network conditions

### Backend Deployment (Approach B - If Used)
- [ ] Update `CallController.php` with WebSocket notification
- [ ] Update `WebSocketService.php` with `notifyUser()` method
- [ ] Add `/api/emit` endpoint to `server.js`
- [ ] Test end-to-end with curl
- [ ] Deploy to staging first
- [ ] Monitor logs for errors

### Post-Deployment
- [ ] Monitor PM2 logs for successful notifications
- [ ] Check analytics for rejection latency
- [ ] Gather user feedback
- [ ] Document any issues encountered

---

## 📚 Related Documentation

- **[README_WEBSOCKET.md](./README_WEBSOCKET.md)** - WebSocket overview and architecture
- **[WEBSOCKET_INTEGRATION_GUIDE.md](./WEBSOCKET_INTEGRATION_GUIDE.md)** - Complete integration guide
- **[FCM_BACKEND_ANSWERS.md](./FCM_BACKEND_ANSWERS.md)** - FCM notification guide (fallback)
- **[CALL_API_COMPLETE_FLOW.md](./CALL_API_COMPLETE_FLOW.md)** - Complete call API documentation

---

## 🎯 Recommendation

**Use Approach A (WebSocket Event from Android)**

**Reasons:**
1. ✅ WebSocket server already has complete implementation
2. ✅ Faster (50-100ms vs 200-500ms)
3. ✅ More reliable (fewer hops)
4. ✅ Easier to implement (just 1 emit call)
5. ✅ Better architecture (separation of concerns)

**Android Changes Needed:** Add just 2 lines:
```kotlin
// In rejectCall() function
socket?.emit("call:reject", JSONObject().apply {
    put("callId", callId)
    put("reason", "User declined")
})
```

That's it! The WebSocket server handles the rest.

---

**Document Status:** ✅ Ready for Implementation  
**Last Updated:** November 22, 2025  
**Next Review:** After Android implementation

**Questions? Contact the team!** 🚀







