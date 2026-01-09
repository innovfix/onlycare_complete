package com.onlycare.app.presentation.screens.call

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlycare.app.data.local.SessionManager
import com.onlycare.app.data.repository.ApiDataRepository
import com.onlycare.app.domain.model.CallType
import com.onlycare.app.domain.model.User
import com.onlycare.app.websocket.WebSocketEvent
import com.onlycare.app.websocket.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class RandomCallConnectInfo(
    val receiverId: String,
    val callId: String,
    val appId: String,
    val token: String,
    val channel: String,
    val balanceTime: String
)

data class RandomCallState(
    val callType: String = "audio",
    val totalCandidates: Int = 0,
    val currentAttempt: Int = 0, // 1-based
    val user: User? = null,
    val isStarting: Boolean = false,
    val isRinging: Boolean = false,
    val secondsLeft: Int = 10,
    val error: String? = null,
    val finished: Boolean = false,
    val localProfileImage: String? = null,
    val connectInfo: RandomCallConnectInfo? = null
)

@HiltViewModel
class RandomCallViewModel @Inject constructor(
    private val repository: ApiDataRepository,
    private val webSocketManager: WebSocketManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        private const val TAG = "RandomCallVM"
        private const val RING_SECONDS = 10
    }

    private val _state = MutableStateFlow(RandomCallState())
    val state: StateFlow<RandomCallState> = _state.asStateFlow()

    private var started = false
    private var cancelRequested = false
    private var callAccepted = false // Flag to stop queue when call is accepted

    private var candidateIds: List<String> = emptyList()
    private var queueJob: Job? = null
    private var pollingJob: Job? = null

    private var currentCallId: String? = null
    private var currentConnectInfo: RandomCallConnectInfo? = null
    private var attemptDeferred: CompletableDeferred<AttemptOutcome>? = null

    private sealed class AttemptOutcome {
        data object Accepted : AttemptOutcome()
        data class Failed(val reason: String) : AttemptOutcome()
    }

    init {
        // Local avatar for "You"
        val localImage = sessionManager.getProfileImage().ifBlank { null }
        _state.update { it.copy(localProfileImage = localImage) }

        // Listen for call signaling events (accept/reject/busy/timeout)
        viewModelScope.launch {
            webSocketManager.callEvents.collect { event ->
                val callId = currentCallId ?: return@collect
                val deferred = attemptDeferred ?: return@collect

                when (event) {
                    is WebSocketEvent.CallAccepted -> {
                        if (event.callId == callId) {
                            Log.d(TAG, "✅ Call accepted for callId=$callId")
                            callAccepted = true
                            deferred.complete(AttemptOutcome.Accepted)
                        }
                    }

                    is WebSocketEvent.CallRejected -> {
                        if (event.callId == callId) {
                            Log.d(TAG, "🚫 Call rejected for callId=$callId reason=${event.reason}")
                            deferred.complete(AttemptOutcome.Failed("Rejected"))
                        }
                    }

                    is WebSocketEvent.UserBusy -> {
                        if (event.callId == callId) {
                            Log.d(TAG, "⛔ User busy for callId=$callId")
                            deferred.complete(AttemptOutcome.Failed("Busy"))
                        }
                    }

                    is WebSocketEvent.CallTimeout -> {
                        if (event.callId == callId) {
                            Log.d(TAG, "⏱️ Server timeout for callId=$callId reason=${event.reason}")
                            deferred.complete(AttemptOutcome.Failed("No answer"))
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    fun startQueue(callType: String, candidateUserIds: List<String>) {
        if (started) return
        started = true
        cancelRequested = false
        callAccepted = false // Reset acceptance flag

        // Best-effort: ensure websocket is connected (do NOT depend on it)
        if (!webSocketManager.isConnected()) {
            try {
                webSocketManager.connect()
            } catch (_: Throwable) {
            }
        }

        // Deduplicate + keep order + ignore blanks
        candidateIds = candidateUserIds
            .filter { it.isNotBlank() }
            .distinct()

        _state.update {
            it.copy(
                callType = callType,
                totalCandidates = candidateIds.size,
                currentAttempt = 0,
                user = null,
                isStarting = true,
                isRinging = false,
                secondsLeft = RING_SECONDS,
                error = null,
                finished = false,
                connectInfo = null
            )
        }

        queueJob?.cancel()
        queueJob = viewModelScope.launch {
            runQueue()
        }
    }

    fun cancel() {
        cancelRequested = true
        queueJob?.cancel()
        queueJob = null
        pollingJob?.cancel()
        pollingJob = null

        val callId = currentCallId
        currentCallId = null
        attemptDeferred?.cancel()
        attemptDeferred = null

        if (!callId.isNullOrBlank()) {
            cancelCallInternal(callId, reason = "Caller cancelled")
        }
    }

    private suspend fun runQueue() {
        try {
            if (candidateIds.isEmpty()) {
                _state.update {
                    it.copy(
                        isStarting = false,
                        finished = true,
                        error = "No online creators available right now."
                    )
                }
                return
            }

            // Safety: check balance once
            val requiredCoins = if (_state.value.callType.lowercase() == "audio") 10 else 60
            repository.getWalletBalance().onFailure { err ->
                _state.update {
                    it.copy(
                        isStarting = false,
                        finished = true,
                        error = err.message ?: "Failed to check wallet balance"
                    )
                }
                return
            }.onSuccess { balance ->
                if (balance < requiredCoins) {
                    _state.update {
                        it.copy(
                            isStarting = false,
                            finished = true,
                            error = "Insufficient coins. Please recharge your wallet."
                        )
                    }
                    return
                }
            }

            _state.update { it.copy(isStarting = false) }

            for ((index, receiverId) in candidateIds.withIndex()) {
                if (cancelRequested || callAccepted) {
                    Log.d(TAG, "Queue stopped: cancelRequested=$cancelRequested, callAccepted=$callAccepted")
                    return
                }

                _state.update {
                    it.copy(
                        currentAttempt = index + 1,
                        user = null,
                        isRinging = false,
                        secondsLeft = RING_SECONDS,
                        error = null
                    )
                }

                // Load user for UI and validate call toggle
                val userResult = repository.getUserById(receiverId)
                val user = userResult.getOrNull()
                if (user == null) {
                    Log.w(TAG, "Skip receiverId=$receiverId (failed to load user)")
                    continue
                }

                val callTypeLower = _state.value.callType.lowercase()
                val callEnabled = when (callTypeLower) {
                    "audio" -> user.audioCallEnabled
                    "video" -> user.videoCallEnabled
                    else -> user.audioCallEnabled
                }
                if (!callEnabled) {
                    Log.d(TAG, "Skip receiverId=$receiverId (callType=$callTypeLower disabled)")
                    continue
                }

                _state.update { it.copy(user = user) }

                // Initiate call via backend
                val callTypeEnum = when (callTypeLower) {
                    "audio" -> CallType.AUDIO
                    "video" -> CallType.VIDEO
                    else -> CallType.AUDIO
                }

                _state.update { it.copy(isRinging = true) }

                val initResult = repository.initiateCall(receiverId, callTypeEnum)
                val initResp = initResult.getOrNull()
                val callId = initResp?.call?.id
                val appId = initResp?.agoraAppId ?: initResp?.call?.agoraAppId ?: ""
                val token = initResp?.call?.agoraToken ?: initResp?.agoraToken ?: ""
                val channel = initResp?.call?.channelName ?: initResp?.channelName ?: ""
                val balanceTime = initResp?.balanceTime ?: initResp?.call?.balanceTime ?: ""

                Log.i(TAG, "========================================")
                Log.i(TAG, "📞 MALE USER - CALL INITIATED")
                Log.i(TAG, "========================================")
                Log.i(TAG, "CALL_ID: $callId")
                Log.i(TAG, "RECEIVER_ID: $receiverId")
                Log.i(TAG, "CALL_TYPE: $callTypeEnum")
                Log.i(TAG, "BALANCE_TIME: $balanceTime")
                Log.i(TAG, "")
                Log.i(TAG, "🔑 AGORA CREDENTIALS FROM API (MALE SIDE):")
                Log.i(TAG, "========================================")
                Log.i(TAG, "AGORA_APP_ID: $appId")
                Log.i(TAG, "CHANNEL_NAME: $channel")
                Log.i(TAG, "AGORA_TOKEN: $token")
                Log.i(TAG, "TOKEN_LENGTH: ${token.length}")
                Log.i(TAG, "TOKEN_EMPTY: ${token.isBlank()}")
                Log.i(TAG, "========================================")

                if (callId.isNullOrBlank() || appId.isBlank() || channel.isBlank()) {
                    Log.w(TAG, "Skip receiverId=$receiverId (initiateCall missing credentials)")
                    _state.update { it.copy(isRinging = false) }
                    continue
                }

                if (cancelRequested) {
                    cancelCallInternal(callId, reason = "Caller cancelled")
                    return
                }

                currentCallId = callId
                currentConnectInfo = RandomCallConnectInfo(
                    receiverId = receiverId,
                    callId = callId,
                    appId = appId,
                    token = token,
                    channel = channel,
                    balanceTime = balanceTime
                )
                attemptDeferred = CompletableDeferred()

                // Instant notify via WebSocket (fallback to FCM if not connected)
                if (webSocketManager.isConnected()) {
                    webSocketManager.initiateCall(
                        receiverId = receiverId,
                        callId = callId,
                        callType = callTypeEnum.name,
                        channelName = channel,
                        agoraToken = token
                    ) { success, error ->
                        if (success) {
                            Log.d(TAG, "✅ call:initiate sent via WebSocket (callId=$callId)")
                        } else {
                            Log.w(TAG, "⚠️ call:initiate WebSocket failed callId=$callId error=$error (FCM fallback)")
                        }
                    }
                }

                // Safety-net: poll backend status during this 10s ring window.
                // This handles cases where caller misses WebSocket call:accepted.
                pollingJob?.cancel()
                pollingJob = viewModelScope.launch {
                    try {
                        while (!cancelRequested && !callAccepted) {
                            val deferred = attemptDeferred ?: return@launch
                            if (deferred.isCompleted) return@launch

                            val result = repository.getCallStatus(callId)
                            result.onSuccess { call ->
                                val status = call.status?.uppercase()?.trim().orEmpty()
                                when (status) {
                                    // Accepted/connected-ish statuses
                                    "ONGOING", "ACCEPTED", "IN_PROGRESS", "CONNECTED" -> {
                                        Log.d(TAG, "✅ Call accepted via API polling callId=$callId status=$status")
                                        callAccepted = true
                                        if (!deferred.isCompleted) deferred.complete(AttemptOutcome.Accepted)
                                        return@onSuccess
                                    }

                                    // Terminal statuses
                                    "REJECTED", "DECLINED", "ENDED", "CANCELLED", "CANCELED" -> {
                                        Log.d(TAG, "🚫 Call terminal via API polling callId=$callId status=$status")
                                        if (!deferred.isCompleted) deferred.complete(AttemptOutcome.Failed(status))
                                        return@onSuccess
                                    }
                                }
                            }

                            delay(700)
                        }
                    } catch (_: CancellationException) {
                    } catch (t: Throwable) {
                        Log.w(TAG, "Polling error (ignored): ${t.message}")
                    }
                }

                // Countdown UI
                val countdownJob = viewModelScope.launch {
                    for (s in RING_SECONDS downTo 1) {
                        _state.update { it.copy(secondsLeft = s) }
                        delay(1_000)
                    }
                }

                val outcome = withTimeoutOrNull(RING_SECONDS * 1_000L) {
                    attemptDeferred?.await()
                }

                countdownJob.cancel()
                pollingJob?.cancel()
                pollingJob = null

                when (outcome) {
                    AttemptOutcome.Accepted -> {
                        val info = currentConnectInfo
                        if (info != null) {
                            Log.d(TAG, "✅ Call accepted - setting connectInfo and STOPPING queue")
                            _state.update { it.copy(connectInfo = info, isRinging = false) }
                            // Don't cancel the call - it's connected!
                            // Just stop the queue loop
                            return
                        }
                    }
                    is AttemptOutcome.Failed -> {
                        Log.d(TAG, "Attempt failed callId=$callId reason=${outcome.reason} -> next")
                    }
                    null -> {
                        Log.d(TAG, "⏱️ 10s no answer for callId=$callId -> cancel and next")
                    }
                }

                // Check again if call was accepted (race condition protection)
                if (callAccepted) {
                    Log.d(TAG, "Call accepted detected after timeout check - stopping queue")
                    return
                }

                // No accept within 10s (or rejected/busy/timeout) → cancel and move next
                cancelCallInternal(callId, reason = "No answer")
                currentCallId = null
                currentConnectInfo = null
                attemptDeferred = null

                _state.update { it.copy(isRinging = false) }
            }

            _state.update {
                it.copy(
                    finished = true,
                    isRinging = false,
                    error = "No online creator answered. Please try again."
                )
            }
        } catch (ce: CancellationException) {
            // normal when user cancels/back
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "Random call queue error", e)
            _state.update {
                it.copy(
                    finished = true,
                    isRinging = false,
                    error = e.message ?: "Random call failed"
                )
            }
        }
    }

    private fun cancelCallInternal(callId: String, reason: String) {
        try {
            webSocketManager.cancelCall(callId, reason)
        } catch (_: Throwable) {
        }

        viewModelScope.launch {
            try {
                repository.cancelCall(callId)
            } catch (_: Throwable) {
            }
        }
    }
}

