package com.onlycare.app.websocket

import android.util.Log
import com.onlycare.app.data.local.SessionManager
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket Manager
 * 
 * Manages WebSocket connection to server for real-time call signaling
 * Server URL: https://onlycare.in (port 3002 handled by Nginx proxy)
 */
@Singleton
class WebSocketManager @Inject constructor(
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "WebSocketManager"
        // Socket.IO endpoint should be exposed by Nginx at /socket.io/ over HTTPS.
        private const val SERVER_URL = "https://onlycare.in"
    }
    
    private var socket: Socket? = null
    private var isConnecting: Boolean = false
    
    // Connection state
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    // Call events
    private val _callEvents = MutableSharedFlow<WebSocketEvent>(
        replay = 0,
        extraBufferCapacity = 10
    )
    val callEvents: SharedFlow<WebSocketEvent> = _callEvents.asSharedFlow()
    
    /**
     * Connect to WebSocket server
     */
    fun connect() {
        if (socket?.connected() == true) {
            Log.d(TAG, "Already connected to WebSocket")
            return
        }
        if (isConnecting) {
            Log.d(TAG, "Already connecting to WebSocket - skipping duplicate connect()")
            return
        }
        
        val token = sessionManager.getAuthToken()
        val userId = sessionManager.getUserId()
        
        if (token.isNullOrBlank() || userId.isNullOrBlank()) {
            Log.e(TAG, "Cannot connect: Missing auth token or user ID")
            _connectionState.value = ConnectionState.Error("Missing authentication")
            return
        }
        
        try {
            Log.d(TAG, "Connecting to WebSocket server: $SERVER_URL")
            Log.d(TAG, "User ID: $userId")
            _connectionState.value = ConnectionState.Connecting
            isConnecting = true
            
            val options = IO.Options().apply {
                // Reconnection settings
                reconnection = true
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                reconnectionAttempts = 5
                
                // Timeout settings
                timeout = 10000

                // Prefer polling first (works reliably behind Nginx/proxies), then upgrade to websocket
                // If websocket is blocked, polling will still keep signaling working.
                transports = arrayOf("polling", "websocket")
                upgrade = true
                forceNew = true
                
                // Auth credentials
                auth = mapOf(
                    "token" to token,
                    "userId" to userId
                )
                
                // Use HTTPS
                secure = true
                
                // Path to socket.io (handled by Nginx)
                path = "/socket.io/"
            }
            
            socket = IO.socket(SERVER_URL, options)
            
            setupEventListeners()
            socket?.connect()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to WebSocket", e)
            _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
            isConnecting = false
        }
    }
    
    /**
     * Setup event listeners
     */
    private fun setupEventListeners() {
        socket?.apply {
            // =====================================
            // Connection Events
            // =====================================
            
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Connected to WebSocket server")
                _connectionState.value = ConnectionState.Connected
                isConnecting = false
                Log.d(TAG, "✅ WebSocket transport connected (websocket/polling fallback enabled)")
            }
            
            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "❌ Disconnected from WebSocket server")
                _connectionState.value = ConnectionState.Disconnected
                isConnecting = false
            }
            
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.getOrNull(0)
                Log.e(TAG, "Connection error: $error args=${args.joinToString()}")
                _connectionState.value = ConnectionState.Error(error?.toString() ?: "Connection error")
                isConnecting = false
            }
            
            on("reconnect") { args ->
                val attempt = args.getOrNull(0) ?: 0
                Log.d(TAG, "Reconnected (attempt: $attempt)")
                _connectionState.value = ConnectionState.Connected
                isConnecting = false
            }
            
            on("reconnecting") { args ->
                val attempt = args.getOrNull(0) ?: 0
                Log.d(TAG, "Reconnecting... (attempt: $attempt)")
                _connectionState.value = ConnectionState.Reconnecting
                isConnecting = true
            }
            
            on("reconnect_error") { args ->
                val error = args.getOrNull(0)
                Log.e(TAG, "Reconnect error: $error args=${args.joinToString()}")
            }
            
            on("reconnect_failed") {
                Log.e(TAG, "Reconnection failed")
                _connectionState.value = ConnectionState.Error("Reconnection failed")
                isConnecting = false
            }
            
            // =====================================
            // Call Events
            // =====================================
            
            on("call:incoming") { args ->
                handleIncomingCall(args.getOrNull(0) as? JSONObject)
            }
            
            on("call:accepted") { args ->
                handleCallAccepted(args.getOrNull(0) as? JSONObject)
            }
            
            on("call:rejected") { args ->
                handleCallRejected(args.getOrNull(0) as? JSONObject)
            }
            
            on("call:ended") { args ->
                handleCallEnded(args.getOrNull(0) as? JSONObject)
            }
            
            on("call:timeout") { args ->
                handleCallTimeout(args.getOrNull(0) as? JSONObject)
            }
            
            on("call:busy") { args ->
                handleCallBusy(args.getOrNull(0) as? JSONObject)
            }
            
            on("call:cancelled") { args ->
                handleCallCancelled(args.getOrNull(0) as? JSONObject)
            }

            // Switch Audio -> Video upgrade flow
            // New backend (preferred): call:upgrade + call:upgrade:request/response
            on("call:upgrade:request") { args ->
                handleSwitchToVideoRequested(args.getOrNull(0) as? JSONObject)
            }
            on("call:upgrade:response") { args ->
                handleUpgradeSwitchToVideoResponse(args.getOrNull(0) as? JSONObject)
            }
            // Backward-compatible legacy events
            on("call:switch_video_request") { args ->
                handleSwitchToVideoRequested(args.getOrNull(0) as? JSONObject)
            }
            on("call:switch_video_accept") { args ->
                handleSwitchToVideoAccepted(args.getOrNull(0) as? JSONObject)
            }
            on("call:switch_video_decline") { args ->
                handleSwitchToVideoDeclined(args.getOrNull(0) as? JSONObject)
            }
        }
    }
    
    // =====================================
    // Event Handlers (Incoming)
    // =====================================
    
    private fun handleIncomingCall(data: JSONObject?) {
        try {
            if (data == null) {
                Log.e(TAG, "❌ handleIncomingCall: data is NULL!")
                return
            }
            
            // Log RAW JSON data to see what backend is sending
            Log.d(TAG, "========================================")
            Log.d(TAG, "📥 RAW WEBSOCKET MESSAGE RECEIVED")
            Log.d(TAG, "   Event: call:incoming")
            Log.d(TAG, "   Raw JSON: $data")
            Log.d(TAG, "========================================")
            
            // Extract fields
            val callId = data.getString("callId")
            val callerId = data.getString("callerId")
            val callerName = data.getString("callerName")
            val callType = data.getString("callType")
            val channelName = data.getString("channelName")
            val agoraToken = data.getString("agoraToken")
            val timestamp = data.getLong("timestamp")
            
            // Extract optional fields (may not be present in older backend versions)
            val agoraAppId = if (data.has("agoraAppId")) {
                data.getString("agoraAppId")
            } else {
                Log.w(TAG, "⚠️ WARNING: agoraAppId NOT found in WebSocket message!")
                null
            }
            
            val balanceTime = if (data.has("balanceTime")) {
                data.getString("balanceTime")
            } else {
                Log.e(TAG, "❌ CRITICAL: balanceTime NOT found in WebSocket message!")
                Log.e(TAG, "❌ This is why timer is not showing on receiver side!")
                null
            }
            
            Log.d(TAG, "")
            Log.d(TAG, "📊 PARSED FIELDS:")
            Log.d(TAG, "   callId: $callId")
            Log.d(TAG, "   callerId: $callerId")
            Log.d(TAG, "   callerName: $callerName")
            Log.d(TAG, "   callType: $callType")
            Log.d(TAG, "   channelName: $channelName")
            Log.d(TAG, "   agoraToken: ${if (agoraToken.isEmpty()) "EMPTY" else "${agoraToken.take(20)}... (${agoraToken.length} chars)"}")
            Log.d(TAG, "   agoraAppId: ${agoraAppId ?: "❌ MISSING"}")
            Log.d(TAG, "   💰 balanceTime: ${balanceTime ?: "❌ MISSING (BACKEND ISSUE!)"}")
            Log.d(TAG, "   timestamp: $timestamp")
            Log.d(TAG, "========================================")
            
            val event = WebSocketEvent.IncomingCall(
                callId = callId,
                callerId = callerId,
                callerName = callerName,
                callType = callType,
                channelName = channelName,
                agoraToken = agoraToken,
                agoraAppId = agoraAppId,
                balanceTime = balanceTime,
                timestamp = timestamp
            )
            
            Log.d(TAG, "📞 Emitting IncomingCall event: ${event.callerName} (${event.callType})")
            Log.d(TAG, "   Event.balanceTime: ${event.balanceTime ?: "NULL"}")
            _callEvents.tryEmit(event)
            
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ ERROR PARSING INCOMING CALL")
            Log.e(TAG, "   Exception: ${e.message}")
            Log.e(TAG, "   Stack trace:")
            e.printStackTrace()
            Log.e(TAG, "========================================")
        }
    }
    
    private fun handleCallAccepted(data: JSONObject?) {
        try {
            if (data == null) return
            
            val event = WebSocketEvent.CallAccepted(
                callId = data.getString("callId"),
                timestamp = data.getLong("timestamp")
            )
            
            Log.d(TAG, "✅ Call accepted: ${event.callId}")
            _callEvents.tryEmit(event)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing call accepted", e)
        }
    }
    
    private fun handleCallRejected(data: JSONObject?) {
        try {
            if (data == null) {
                Log.e(TAG, "⚠️ handleCallRejected: data is NULL!")
                return
            }
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "📥 RECEIVED call:rejected from server")
            Log.d(TAG, "Raw data: $data")
            Log.d(TAG, "========================================")
            
            val callId = data.optString("callId", null)
            val reason = data.optString("reason", "User declined")
            val timestamp = data.optLong("timestamp", System.currentTimeMillis())
            
            if (callId.isNullOrEmpty()) {
                Log.e(TAG, "❌ handleCallRejected: callId is NULL/EMPTY!")
                return
            }
            
            val event = WebSocketEvent.CallRejected(
                callId = callId,
                reason = reason,
                timestamp = timestamp
            )
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "❌ EMITTING CallRejected EVENT:")
            Log.d(TAG, "   Call ID: ${event.callId}")
            Log.d(TAG, "   Reason: ${event.reason}")
            Log.d(TAG, "   Timestamp: ${event.timestamp}")
            Log.d(TAG, "========================================")
            
            val emitted = _callEvents.tryEmit(event)
            if (emitted) {
                Log.d(TAG, "✅ CallRejected event emitted successfully")
            } else {
                Log.e(TAG, "❌ FAILED to emit CallRejected event!")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error parsing call rejected data", e)
            Log.e(TAG, "   Exception: ${e.message}")
            Log.e(TAG, "   Stack trace: ${e.stackTraceToString()}")
        }
    }
    
    private fun handleCallEnded(data: JSONObject?) {
        try {
            if (data == null) return
            
            val event = WebSocketEvent.CallEnded(
                callId = data.getString("callId"),
                endedBy = data.optString("endedBy", null),
                reason = data.getString("reason"),
                timestamp = data.getLong("timestamp")
            )
            
            Log.d(TAG, "📴 Call ended: ${event.callId} - ${event.reason}")
            _callEvents.tryEmit(event)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing call ended", e)
        }
    }
    
    private fun handleCallTimeout(data: JSONObject?) {
        try {
            if (data == null) return
            
            val event = WebSocketEvent.CallTimeout(
                callId = data.getString("callId"),
                reason = data.getString("reason")
            )
            
            Log.d(TAG, "⏱️ Call timeout: ${event.callId}")
            _callEvents.tryEmit(event)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing call timeout", e)
        }
    }

    private fun handleSwitchToVideoRequested(data: JSONObject?) {
        try {
            if (data == null) return
            val callId = data.optString("callId", "").ifBlank { data.optString("call_id", "") }
            val timestamp = data.optLong("timestamp", System.currentTimeMillis())
            if (callId.isBlank()) return
            Log.d(TAG, "📹 Switch-to-video requested for callId=$callId")
            _callEvents.tryEmit(WebSocketEvent.SwitchToVideoRequested(callId = callId, timestamp = timestamp))
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing switch_video_request", e)
        }
    }

    private fun handleSwitchToVideoAccepted(data: JSONObject?) {
        try {
            if (data == null) return
            val callId = data.optString("callId", "").ifBlank { data.optString("call_id", "") }
            val timestamp = data.optLong("timestamp", System.currentTimeMillis())
            if (callId.isBlank()) return
            Log.d(TAG, "✅ Switch-to-video accepted for callId=$callId")
            _callEvents.tryEmit(WebSocketEvent.SwitchToVideoAccepted(callId = callId, timestamp = timestamp))
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing switch_video_accept", e)
        }
    }

    private fun handleSwitchToVideoDeclined(data: JSONObject?) {
        try {
            if (data == null) return
            val callId = data.optString("callId", "").ifBlank { data.optString("call_id", "") }
            val reason = data.optString("reason", "Not now")
            val timestamp = data.optLong("timestamp", System.currentTimeMillis())
            if (callId.isBlank()) return
            Log.d(TAG, "🚫 Switch-to-video declined for callId=$callId reason=$reason")
            _callEvents.tryEmit(WebSocketEvent.SwitchToVideoDeclined(callId = callId, reason = reason, timestamp = timestamp))
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing switch_video_decline", e)
        }
    }

    /**
     * New backend response handler for audio -> video upgrade.
     *
     * Expected payload example:
     * {
     *   "callId": "...",
     *   "accepted": true/false,
     *   "reason": "Not now",
     *   "timestamp": 123
     * }
     */
    private fun handleUpgradeSwitchToVideoResponse(data: JSONObject?) {
        try {
            if (data == null) return

            val callId = data.optString("callId", "").ifBlank { data.optString("call_id", "") }
            if (callId.isBlank()) return

            val timestamp = data.optLong("timestamp", System.currentTimeMillis())

            // Support different backend field names
            val accepted: Boolean? = when {
                data.has("accepted") -> data.optBoolean("accepted")
                data.has("isAccepted") -> data.optBoolean("isAccepted")
                data.has("status") -> {
                    val s = data.optString("status", "").lowercase().trim()
                    when (s) {
                        "accepted", "approve", "approved", "ok", "success", "true", "1" -> true
                        "declined", "rejected", "reject", "false", "0" -> false
                        else -> null
                    }
                }
                else -> null
            }

            if (accepted == true) {
                Log.d(TAG, "✅ Upgrade (switch-to-video) accepted for callId=$callId")
                _callEvents.tryEmit(WebSocketEvent.SwitchToVideoAccepted(callId = callId, timestamp = timestamp))
            } else {
                val reason = data.optString("reason", "Not now")
                Log.d(TAG, "🚫 Upgrade (switch-to-video) declined for callId=$callId reason=$reason accepted=$accepted")
                _callEvents.tryEmit(WebSocketEvent.SwitchToVideoDeclined(callId = callId, reason = reason, timestamp = timestamp))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing call:upgrade:response", e)
        }
    }
    
    private fun handleCallBusy(data: JSONObject?) {
        try {
            if (data == null) return
            
            val event = WebSocketEvent.UserBusy(
                callId = data.getString("callId")
            )
            
            Log.d(TAG, "🔕 User busy: ${event.callId}")
            _callEvents.tryEmit(event)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing user busy", e)
        }
    }
    
    private fun handleCallCancelled(data: JSONObject?) {
        try {
            if (data == null) return
            
            val callId = data.getString("callId")
            val reason = data.optString("reason", "Caller cancelled")
            val timestamp = data.optLong("timestamp", System.currentTimeMillis())
            
            val event = WebSocketEvent.CallCancelled(
                callId = callId,
                reason = reason,
                timestamp = timestamp
            )
            
            Log.d(TAG, "🚫 Call cancelled: ${event.callId}, reason: ${event.reason}, timestamp: ${event.timestamp}")
            _callEvents.tryEmit(event)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing call cancelled", e)
        }
    }
    
    // =====================================
    // Event Emitters (Outgoing)
    // =====================================
    
    /**
     * Notify server when initiating a call
     * 
     * Call this AFTER Laravel API returns success with callId, token, channel
     */
    fun initiateCall(
        receiverId: String,
        callId: String,
        callType: String,
        channelName: String,
        agoraToken: String,
        callback: (success: Boolean, error: String?) -> Unit
    ) {
        if (socket?.connected() != true) {
            Log.w(TAG, "WebSocket not connected, cannot initiate call via WebSocket")
            callback(false, "WebSocket not connected")
            return
        }
        
        try {
            val data = JSONObject().apply {
                put("receiverId", receiverId)
                put("callId", callId)
                put("callType", callType)
                put("channelName", channelName)
                put("agoraToken", agoraToken)
            }
            
            Log.d(TAG, "📤 Emitting call:initiate for callId: $callId")
            
            socket?.emit("call:initiate", data, Ack { args ->
                try {
                    val response = args.getOrNull(0) as? JSONObject
                    val success = response?.optBoolean("success", false) ?: false
                    
                    if (success) {
                        Log.d(TAG, "✅ Call initiated via WebSocket")
                        callback(true, null)
                    } else {
                        val error = response?.optString("error", "Unknown error")
                        Log.e(TAG, "❌ Call initiation failed: $error")
                        callback(false, error)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing initiate response", e)
                    callback(false, e.message)
                }
            })
            
        } catch (e: Exception) {
            Log.e(TAG, "Error emitting call:initiate", e)
            callback(false, e.message)
        }
    }
    
    /**
     * Accept a call
     */
    fun acceptCall(callId: String) {
        if (socket?.connected() != true) {
            Log.w(TAG, "WebSocket not connected, cannot accept call via WebSocket")
            return
        }
        
        try {
            val data = JSONObject().apply {
                put("callId", callId)
            }
            
            Log.d(TAG, "📤 Emitting call:accept for callId: $callId")
            socket?.emit("call:accept", data)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error emitting call:accept", e)
        }
    }
    
    /**
     * Reject a call
     */
    fun rejectCall(callId: String, reason: String = "User declined") {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🚫 rejectCall() called")
        Log.d(TAG, "   Call ID: $callId")
        Log.d(TAG, "   Reason: $reason")
        Log.d(TAG, "========================================")
        
        if (socket?.connected() != true) {
            Log.w(TAG, "⚠️ WebSocket NOT connected - cannot reject call via WebSocket")
            Log.w(TAG, "   Socket: ${socket}")
            Log.w(TAG, "   Connected: ${socket?.connected()}")
            return
        }
        
        try {
            val data = JSONObject().apply {
                put("callId", callId)
                put("reason", reason)
            }
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "📤 EMITTING call:reject to server")
            Log.d(TAG, "   Data: $data")
            Log.d(TAG, "========================================")
            
            socket?.emit("call:reject", data)
            
            Log.d(TAG, "✅ call:reject emitted successfully")
            Log.d(TAG, "   ⏰ Server should send call:rejected back to caller")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error emitting call:reject", e)
            Log.e(TAG, "   Exception: ${e.message}")
            Log.e(TAG, "   Stack trace: ${e.stackTraceToString()}")
        }
    }
    
    /**
     * Cancel a call (when caller ends before receiver accepts)
     * This notifies the receiver immediately via WebSocket
     */
    fun cancelCall(callId: String, reason: String = "Caller cancelled") {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🚫 cancelCall() called")
        Log.d(TAG, "   Call ID: $callId")
        Log.d(TAG, "   Reason: $reason")
        Log.d(TAG, "========================================")
        
        if (socket?.connected() != true) {
            Log.w(TAG, "⚠️ WebSocket NOT connected - cannot cancel call via WebSocket")
            Log.w(TAG, "   Socket: ${socket}")
            Log.w(TAG, "   Connected: ${socket?.connected()}")
            return
        }
        
        try {
            val data = JSONObject().apply {
                put("callId", callId)
                put("reason", reason)
            }
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "📤 EMITTING call:cancel to server")
            Log.d(TAG, "   Data: $data")
            Log.d(TAG, "========================================")
            
            socket?.emit("call:cancel", data)
            
            Log.d(TAG, "✅ call:cancel emitted successfully")
            Log.d(TAG, "   ⏰ Server should send call:cancelled to receiver")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error emitting call:cancel", e)
            Log.e(TAG, "   Exception: ${e.message}")
            Log.e(TAG, "   Stack trace: ${e.stackTraceToString()}")
        }
    }
    
    /**
     * End a call (when call is already ongoing)
     */
    fun endCall(callId: String) {
        if (socket?.connected() != true) {
            Log.w(TAG, "WebSocket not connected, cannot end call via WebSocket")
            return
        }
        
        try {
            val data = JSONObject().apply {
                put("callId", callId)
            }
            
            Log.d(TAG, "📤 Emitting call:end for callId: $callId")
            socket?.emit("call:end", data)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error emitting call:end", e)
        }
    }

    // =============================
    // Switch Audio -> Video (Outgoing)
    // =============================
    fun requestSwitchToVideo(callId: String, receiverId: String) {
        if (socket?.connected() != true) {
            Log.w(TAG, "WebSocket not connected, cannot request switch-to-video")
            return
        }
        try {
            val data = JSONObject().apply {
                put("callId", callId)
                // Important for server routing (receiver must receive the event)
                put("receiverId", receiverId)
                put("senderId", sessionManager.getUserId())
                put("timestamp", System.currentTimeMillis())
            }
            // New backend expects call:upgrade (aliases: call:switchToVideo / call:switch_to_video)
            Log.d(TAG, "📤 Emitting call:upgrade for callId=$callId")
            socket?.emit("call:upgrade", data, Ack { args ->
                val response = args.getOrNull(0) as? JSONObject
                Log.d(TAG, "📥 Ack call:upgrade response=${response?.toString() ?: args.joinToString()}")
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error emitting call:upgrade", e)
        }
    }

    fun acceptSwitchToVideo(callId: String, receiverId: String) {
        if (socket?.connected() != true) {
            Log.w(TAG, "WebSocket not connected, cannot accept switch-to-video")
            return
        }
        try {
            val data = JSONObject().apply {
                put("callId", callId)
                // Important for server routing back to the other peer
                put("receiverId", receiverId)
                put("senderId", sessionManager.getUserId())
                put("accepted", true)
                put("timestamp", System.currentTimeMillis())
            }
            Log.d(TAG, "📤 Emitting call:upgrade:response (accepted) for callId=$callId")
            socket?.emit("call:upgrade:response", data, Ack { args ->
                val response = args.getOrNull(0) as? JSONObject
                Log.d(TAG, "📥 Ack call:upgrade:response response=${response?.toString() ?: args.joinToString()}")
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error emitting call:upgrade:response (accepted)", e)
        }
    }

    fun declineSwitchToVideo(callId: String, receiverId: String, reason: String = "Not now") {
        if (socket?.connected() != true) {
            Log.w(TAG, "WebSocket not connected, cannot decline switch-to-video")
            return
        }
        try {
            val data = JSONObject().apply {
                put("callId", callId)
                // Important for server routing back to the other peer
                put("receiverId", receiverId)
                put("senderId", sessionManager.getUserId())
                put("accepted", false)
                put("reason", reason)
                put("timestamp", System.currentTimeMillis())
            }
            Log.d(TAG, "📤 Emitting call:upgrade:response (declined) for callId=$callId")
            socket?.emit("call:upgrade:response", data, Ack { args ->
                val response = args.getOrNull(0) as? JSONObject
                Log.d(TAG, "📥 Ack call:upgrade:response response=${response?.toString() ?: args.joinToString()}")
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error emitting call:upgrade:response (declined)", e)
        }
    }
    
    /**
     * Disconnect from WebSocket server
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting from WebSocket")
        socket?.disconnect()
        socket?.off()
        socket = null
        _connectionState.value = ConnectionState.Disconnected
    }
    
    /**
     * Check if connected
     */
    fun isConnected(): Boolean = socket?.connected() == true
}

