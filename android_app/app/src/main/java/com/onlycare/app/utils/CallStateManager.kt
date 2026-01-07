package com.onlycare.app.utils

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton to track if user is currently in a call or incoming call screen
 * Used to prevent multiple incoming calls at the same time
 */
object CallStateManager {
    private const val TAG = "CallStateManager"
    
    @Volatile
    private var _isInCall = false
    
    @Volatile
    private var _isInIncomingCallScreen = false
    
    // Track processed call IDs to prevent duplicate notifications
    private val processedCallIds = ConcurrentHashMap.newKeySet<String>()
    
    // Track cancelled call IDs (for when cancellation arrives before IncomingCallActivity starts)
    private val cancelledCallIds = ConcurrentHashMap.newKeySet<String>()
    
    // Track current active call ID
    @Volatile
    private var _currentCallId: String? = null
    
    /**
     * Returns true if user is currently in an active call (AudioCallScreen or VideoCallScreen)
     */
    val isInCall: Boolean
        get() = _isInCall
    
    /**
     * Returns true if user is currently in the incoming call activity screen
     */
    val isInIncomingCallScreen: Boolean
        get() = _isInIncomingCallScreen
    
    /**
     * Returns true if user is busy (either in a call or viewing incoming call screen)
     */
    val isBusy: Boolean
        get() = _isInCall || _isInIncomingCallScreen
    
    /**
     * Get current active call ID
     */
    val currentCallId: String?
        get() = _currentCallId
    
    /**
     * Mark that user entered an active call screen
     */
    fun setInCall(inCall: Boolean) {
        Log.d(TAG, "setInCall: $inCall (was: $_isInCall)")
        _isInCall = inCall
    }
    
    /**
     * Mark that user entered/exited incoming call screen
     */
    fun setInIncomingCallScreen(inScreen: Boolean) {
        Log.d(TAG, "setInIncomingCallScreen: $inScreen (was: $_isInIncomingCallScreen)")
        _isInIncomingCallScreen = inScreen
    }
    
    /**
     * Set the current active call ID
     */
    fun setCurrentCallId(callId: String?) {
        Log.d(TAG, "setCurrentCallId: $callId (was: $_currentCallId)")
        _currentCallId = callId
        if (callId != null) {
            markAsProcessed(callId)
        }
    }
    
    /**
     * Mark a call ID as processed to prevent duplicate notifications
     */
    fun markAsProcessed(callId: String) {
        processedCallIds.add(callId)
        Log.d(TAG, "markAsProcessed: $callId (total processed: ${processedCallIds.size})")
    }
    
    /**
     * Check if a call ID has already been processed
     */
    fun isProcessed(callId: String): Boolean {
        val processed = processedCallIds.contains(callId)
        if (processed) {
            Log.d(TAG, "isProcessed: $callId = true (duplicate call)")
        }
        return processed
    }
    
    /**
     * Alias for isProcessed (for compatibility)
     */
    fun isCallProcessed(callId: String): Boolean = isProcessed(callId)
    
    /**
     * Clear processed call IDs (useful for cleanup)
     * Keeps only recent entries to prevent memory leak
     */
    fun clearOldProcessedCalls() {
        if (processedCallIds.size > 100) {
            Log.d(TAG, "clearOldProcessedCalls: clearing ${processedCallIds.size} entries")
            processedCallIds.clear()
        }
        if (cancelledCallIds.size > 100) {
            Log.d(TAG, "clearOldCancelledCalls: clearing ${cancelledCallIds.size} entries")
            cancelledCallIds.clear()
        }
    }
    
    /**
     * Mark a call as cancelled (when caller disconnects before receiver accepts)
     * This is used to handle the case where cancellation FCM arrives before IncomingCallActivity starts
     */
    fun markCallAsCancelled(callId: String) {
        cancelledCallIds.add(callId)
        Log.d(TAG, "========================================")
        Log.d(TAG, "🚫 markCallAsCancelled: $callId")
        Log.d(TAG, "   Total cancelled calls tracked: ${cancelledCallIds.size}")
        Log.d(TAG, "========================================")
    }
    
    /**
     * Check if a call has been cancelled
     * Used by IncomingCallActivity to check if its call was cancelled before it started
     */
    fun isCallCancelled(callId: String): Boolean {
        val cancelled = cancelledCallIds.contains(callId)
        if (cancelled) {
            Log.d(TAG, "🚫 isCallCancelled: $callId = true (call was cancelled by caller)")
        }
        return cancelled
    }
    
    /**
     * Remove a call from cancelled list (after it's been handled)
     */
    fun clearCancelledCall(callId: String) {
        cancelledCallIds.remove(callId)
        Log.d(TAG, "clearCancelledCall: $callId removed from cancelled list")
    }
    
    /**
     * Reset all states (useful when app starts fresh)
     */
    fun reset() {
        Log.d(TAG, "reset() called")
        _isInCall = false
        _isInIncomingCallScreen = false
        _currentCallId = null
        processedCallIds.clear()
        cancelledCallIds.clear()
    }
}



