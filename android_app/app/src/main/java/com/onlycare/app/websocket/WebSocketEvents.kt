package com.onlycare.app.websocket

/**
 * WebSocket Events
 * 
 * Events received from and sent to the WebSocket server for real-time call signaling
 */

/**
 * Events received from server (Server → App)
 */
sealed class WebSocketEvent {
    
    /**
     * Incoming call from another user
     */
    data class IncomingCall(
        val callId: String,
        val callerId: String,
        val callerName: String,
        val callType: String, // "AUDIO" or "VIDEO"
        val channelName: String,
        val agoraToken: String,
        val agoraAppId: String? = null,
        val balanceTime: String? = null, // Caller's available balance time (e.g., "25:00")
        val timestamp: Long
    ) : WebSocketEvent()
    
    /**
     * Other user accepted your call
     */
    data class CallAccepted(
        val callId: String,
        val timestamp: Long
    ) : WebSocketEvent()
    
    /**
     * Other user rejected your call
     */
    data class CallRejected(
        val callId: String,
        val reason: String,
        val timestamp: Long
    ) : WebSocketEvent()
    
    /**
     * Call ended by other user
     */
    data class CallEnded(
        val callId: String,
        val endedBy: String?,
        val reason: String,
        val timestamp: Long
    ) : WebSocketEvent()
    
    /**
     * Call timed out (no answer after 30 seconds)
     */
    data class CallTimeout(
        val callId: String,
        val reason: String
    ) : WebSocketEvent()
    
    /**
     * User is busy (already in another call)
     */
    data class UserBusy(
        val callId: String
    ) : WebSocketEvent()
    
    /**
     * Call was cancelled
     */
    data class CallCancelled(
        val callId: String,
        val reason: String,
        val timestamp: Long = System.currentTimeMillis() // Optional, defaults to current time
    ) : WebSocketEvent()

    /**
     * Caller requested to switch this ongoing audio call to video
     */
    data class SwitchToVideoRequested(
        val callId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : WebSocketEvent()

    /**
     * Receiver accepted switch-to-video request
     */
    data class SwitchToVideoAccepted(
        val callId: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : WebSocketEvent()

    /**
     * Receiver declined switch-to-video request
     */
    data class SwitchToVideoDeclined(
        val callId: String,
        val reason: String = "Not now",
        val timestamp: Long = System.currentTimeMillis()
    ) : WebSocketEvent()
}

/**
 * Connection states
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
    object Reconnecting : ConnectionState()
}

