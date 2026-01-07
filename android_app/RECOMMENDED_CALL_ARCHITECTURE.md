# 🏗️ Recommended Call Architecture for OnlyCare App

## 🎯 Hybrid Approach: Firebase Realtime Database + FCM

### Why This Combination?

1. **Firebase Realtime Database** - For call signaling (SDP, ICE candidates, real-time state)
2. **FCM** - For wake-up notifications when app is killed/backgrounded
3. **StateFlow** - For reactive state management (better than LiveData for this use case)
4. **Sealed Classes** - For type-safe call events

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         SIGNALING LAYER                              │
│  ┌────────────────────┐         ┌──────────────────────┐           │
│  │ Firebase Realtime  │◄────────┤  FCM (Wake-up only)  │           │
│  │    Database        │         │  - App killed        │           │
│  │  - SDP Exchange    │         │  - Background wakeup │           │
│  │  - ICE Candidates  │         └──────────────────────┘           │
│  │  - Call State      │                                             │
│  └────────┬───────────┘                                             │
│           │                                                          │
│           ▼                                                          │
│  ┌────────────────────────────────────────────────────┐            │
│  │         CallSignalingRepository                    │            │
│  │  - observeCallSignals()                            │            │
│  │  - sendOffer/Answer()                              │            │
│  │  - sendICECandidate()                              │            │
│  │  - updateCallState()                               │            │
│  └────────┬───────────────────────────────────────────┘            │
└───────────┼──────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                                    │
│  ┌────────────────────────────────────────────────────┐            │
│  │         CallStateManager (StateFlow)               │            │
│  │                                                     │            │
│  │  sealed class CallEvent {                          │            │
│  │    data class IncomingCall                         │            │
│  │    data class CallAccepted                         │            │
│  │    data class CallRejected                         │            │
│  │    data class CallEnded                            │            │
│  │    data class RemoteUserJoined                     │            │
│  │    data class RemoteUserLeft                       │            │
│  │    data class Error                                │            │
│  │  }                                                  │            │
│  │                                                     │            │
│  │  data class CallState {                            │            │
│  │    val callId: String                              │            │
│  │    val status: Status                              │            │
│  │    val participants: List<User>                    │            │
│  │    val startTime: Long?                            │            │
│  │  }                                                  │            │
│  └────────┬───────────────────────────────────────────┘            │
└───────────┼──────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                              │
│  ┌────────────────────┐    ┌─────────────────────┐                 │
│  │  CallViewModel     │    │  CallScreen         │                 │
│  │  - Observes events │◄───┤  - Composable UI    │                 │
│  │  - Updates UI      │    │  - User actions     │                 │
│  └────────────────────┘    └─────────────────────┘                 │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Implementation

### **1. Call Signaling Repository**

```kotlin
interface CallSignalingRepository {
    fun observeIncomingCalls(userId: String): Flow<CallSignal>
    suspend fun sendCallOffer(callId: String, receiverId: String, sdp: String, callType: CallType)
    suspend fun sendCallAnswer(callId: String, callerId: String, sdp: String)
    suspend fun sendICECandidate(callId: String, userId: String, candidate: String)
    suspend fun updateCallState(callId: String, state: CallState)
    suspend fun endCall(callId: String)
}

class FirebaseCallSignalingRepository(
    private val database: FirebaseDatabase,
    private val fcmRepository: FcmNotificationRepository
) : CallSignalingRepository {
    
    private val callsRef = database.getReference("calls")
    
    override fun observeIncomingCalls(userId: String): Flow<CallSignal> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { callSnapshot ->
                    val call = callSnapshot.getValue(CallData::class.java)
                    if (call?.receiverId == userId && call.state == "ringing") {
                        trySend(CallSignal.IncomingCall(
                            callId = call.callId,
                            callerId = call.callerId,
                            callerName = call.callerName,
                            callType = call.callType,
                            channelName = call.channelName
                        ))
                    }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        callsRef.orderByChild("receiverId").equalTo(userId)
            .addValueEventListener(listener)
        
        awaitClose { callsRef.removeEventListener(listener) }
    }
    
    override suspend fun sendCallOffer(
        callId: String, 
        receiverId: String, 
        sdp: String, 
        callType: CallType
    ) {
        val callData = mapOf(
            "callId" to callId,
            "callerId" to getCurrentUserId(),
            "receiverId" to receiverId,
            "sdp" to sdp,
            "callType" to callType.name,
            "state" to "ringing",
            "timestamp" to ServerValue.TIMESTAMP
        )
        
        // Write to Realtime Database
        callsRef.child(callId).setValue(callData).await()
        
        // Send FCM wake-up notification (if receiver app is killed)
        fcmRepository.sendWakeUpNotification(
            receiverId = receiverId,
            title = "Incoming ${callType.name} call",
            callId = callId
        )
    }
    
    override suspend fun sendCallAnswer(callId: String, callerId: String, sdp: String) {
        callsRef.child(callId).child("answerSdp").setValue(sdp).await()
        callsRef.child(callId).child("state").setValue("accepted").await()
    }
    
    override suspend fun sendICECandidate(callId: String, userId: String, candidate: String) {
        val candidateRef = callsRef.child(callId).child("iceCandidates").child(userId).push()
        candidateRef.setValue(candidate).await()
    }
    
    override suspend fun updateCallState(callId: String, state: CallState) {
        callsRef.child(callId).child("state").setValue(state.value).await()
    }
    
    override suspend fun endCall(callId: String) {
        callsRef.child(callId).removeValue().await()
    }
}
```

---

### **2. Call State Manager with StateFlow**

```kotlin
@Singleton
class CallStateManager @Inject constructor(
    private val signalingRepository: CallSignalingRepository,
    private val agoraManager: AgoraCallManager
) {
    // Use StateFlow instead of LiveData for better coroutine support
    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall.asStateFlow()
    
    private val _callEvents = MutableSharedFlow<CallEvent>(
        replay = 0,
        extraBufferCapacity = 10
    )
    val callEvents: SharedFlow<CallEvent> = _callEvents.asSharedFlow()
    
    sealed class CallEvent {
        data class IncomingCall(
            val callId: String,
            val callerId: String,
            val callerName: String,
            val callerAvatar: String?,
            val callType: CallType,
            val channelName: String
        ) : CallEvent()
        
        data class CallAccepted(val callId: String) : CallEvent()
        data class CallRejected(val callId: String, val reason: String) : CallEvent()
        data class CallEnded(val callId: String, val reason: EndReason) : CallEvent()
        data class RemoteUserJoined(val userId: String) : CallEvent()
        data class RemoteUserLeft(val userId: String) : CallEvent()
        data class CallTimeout(val callId: String) : CallEvent()
        data class Error(val callId: String, val message: String) : CallEvent()
    }
    
    data class ActiveCall(
        val callId: String,
        val channelName: String,
        val callType: CallType,
        val isIncoming: Boolean,
        val remoteUserId: String,
        val remoteUserName: String,
        val status: CallStatus,
        val startTime: Long? = null
    )
    
    enum class CallStatus {
        RINGING,
        CONNECTING,
        CONNECTED,
        ENDED
    }
    
    enum class EndReason {
        USER_HANGUP,
        REMOTE_HANGUP,
        TIMEOUT,
        ERROR,
        REJECTED,
        BUSY
    }
    
    // Start observing incoming calls for current user
    fun startObserving(userId: String) {
        viewModelScope.launch {
            signalingRepository.observeIncomingCalls(userId)
                .collect { signal ->
                    when (signal) {
                        is CallSignal.IncomingCall -> handleIncomingCall(signal)
                        is CallSignal.CallAccepted -> handleCallAccepted(signal)
                        is CallSignal.CallRejected -> handleCallRejected(signal)
                        is CallSignal.CallEnded -> handleCallEnded(signal)
                        is CallSignal.ICECandidate -> handleICECandidate(signal)
                    }
                }
        }
    }
    
    private suspend fun handleIncomingCall(signal: CallSignal.IncomingCall) {
        // Check if user is already in a call
        if (_activeCall.value != null) {
            // Reject with "busy" reason
            signalingRepository.updateCallState(
                signal.callId, 
                CallState.BUSY
            )
            return
        }
        
        // Update active call state
        _activeCall.value = ActiveCall(
            callId = signal.callId,
            channelName = signal.channelName,
            callType = signal.callType,
            isIncoming = true,
            remoteUserId = signal.callerId,
            remoteUserName = signal.callerName,
            status = CallStatus.RINGING
        )
        
        // Emit event for UI
        _callEvents.emit(CallEvent.IncomingCall(
            callId = signal.callId,
            callerId = signal.callerId,
            callerName = signal.callerName,
            callerAvatar = signal.callerAvatar,
            callType = signal.callType,
            channelName = signal.channelName
        ))
        
        // Start timeout timer (30 seconds)
        startCallTimeout(signal.callId)
    }
    
    suspend fun acceptCall(callId: String) {
        val call = _activeCall.value ?: return
        if (call.callId != callId) return
        
        try {
            // Join Agora channel
            val token = agoraManager.getToken(call.channelName)
            agoraManager.joinChannel(call.channelName, token)
            
            // Send answer via signaling
            val sdp = agoraManager.getLocalSDP() // If using WebRTC
            signalingRepository.sendCallAnswer(callId, call.remoteUserId, sdp)
            
            // Update state
            _activeCall.value = call.copy(
                status = CallStatus.CONNECTED,
                startTime = System.currentTimeMillis()
            )
            
            _callEvents.emit(CallEvent.CallAccepted(callId))
            
        } catch (e: Exception) {
            _callEvents.emit(CallEvent.Error(callId, e.message ?: "Failed to accept call"))
        }
    }
    
    suspend fun rejectCall(callId: String, reason: String = "User declined") {
        val call = _activeCall.value ?: return
        if (call.callId != callId) return
        
        // Update signaling state
        signalingRepository.updateCallState(callId, CallState.REJECTED)
        
        // Clear active call
        _activeCall.value = null
        
        // Emit event
        _callEvents.emit(CallEvent.CallRejected(callId, reason))
    }
    
    suspend fun endCall(callId: String) {
        val call = _activeCall.value ?: return
        if (call.callId != callId) return
        
        // Leave Agora channel
        agoraManager.leaveChannel()
        
        // Update signaling
        signalingRepository.endCall(callId)
        
        // Clear state
        _activeCall.value = null
        
        // Emit event
        _callEvents.emit(CallEvent.CallEnded(callId, EndReason.USER_HANGUP))
    }
    
    private fun startCallTimeout(callId: String) {
        viewModelScope.launch {
            delay(30_000) // 30 seconds
            
            val call = _activeCall.value
            if (call?.callId == callId && call.status == CallStatus.RINGING) {
                signalingRepository.updateCallState(callId, CallState.TIMEOUT)
                _activeCall.value = null
                _callEvents.emit(CallEvent.CallTimeout(callId))
            }
        }
    }
}
```

---

### **3. ViewModel for Call Screen**

```kotlin
@HiltViewModel
class CallViewModel @Inject constructor(
    private val callStateManager: CallStateManager,
    private val userRepository: UserRepository
) : ViewModel() {
    
    val activeCall = callStateManager.activeCall.asStateFlow()
    val callEvents = callStateManager.callEvents
    
    // UI State
    private val _uiState = MutableStateFlow<CallUiState>(CallUiState.Idle)
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()
    
    sealed class CallUiState {
        object Idle : CallUiState()
        data class Ringing(val call: CallStateManager.ActiveCall) : CallUiState()
        data class InCall(val call: CallStateManager.ActiveCall) : CallUiState()
        data class Ended(val reason: String) : CallUiState()
        data class Error(val message: String) : CallUiState()
    }
    
    init {
        // Observe call events and update UI state
        viewModelScope.launch {
            callEvents.collect { event ->
                handleCallEvent(event)
            }
        }
        
        // Observe active call changes
        viewModelScope.launch {
            activeCall.collect { call ->
                _uiState.value = when {
                    call == null -> CallUiState.Idle
                    call.status == CallStateManager.CallStatus.RINGING -> 
                        CallUiState.Ringing(call)
                    call.status == CallStateManager.CallStatus.CONNECTED -> 
                        CallUiState.InCall(call)
                    else -> _uiState.value
                }
            }
        }
    }
    
    private fun handleCallEvent(event: CallStateManager.CallEvent) {
        when (event) {
            is CallStateManager.CallEvent.CallRejected -> {
                _uiState.value = CallUiState.Ended("Call rejected")
            }
            is CallStateManager.CallEvent.CallEnded -> {
                val reason = when (event.reason) {
                    CallStateManager.EndReason.USER_HANGUP -> "Call ended"
                    CallStateManager.EndReason.REMOTE_HANGUP -> "Remote user ended call"
                    CallStateManager.EndReason.TIMEOUT -> "Call timeout"
                    CallStateManager.EndReason.REJECTED -> "Call rejected"
                    CallStateManager.EndReason.BUSY -> "User is busy"
                    CallStateManager.EndReason.ERROR -> "Call error"
                }
                _uiState.value = CallUiState.Ended(reason)
            }
            is CallStateManager.CallEvent.Error -> {
                _uiState.value = CallUiState.Error(event.message)
            }
            else -> { /* Handle other events */ }
        }
    }
    
    fun acceptCall() {
        viewModelScope.launch {
            activeCall.value?.let { call ->
                callStateManager.acceptCall(call.callId)
            }
        }
    }
    
    fun rejectCall() {
        viewModelScope.launch {
            activeCall.value?.let { call ->
                callStateManager.rejectCall(call.callId)
            }
        }
    }
    
    fun endCall() {
        viewModelScope.launch {
            activeCall.value?.let { call ->
                callStateManager.endCall(call.callId)
            }
        }
    }
}
```

---

### **4. Composable Call Screen**

```kotlin
@Composable
fun CallScreen(
    viewModel: CallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    
    when (val state = uiState) {
        is CallViewModel.CallUiState.Idle -> {
            // Show nothing or navigate back
        }
        
        is CallViewModel.CallUiState.Ringing -> {
            IncomingCallScreen(
                call = state.call,
                onAccept = { viewModel.acceptCall() },
                onReject = { viewModel.rejectCall() }
            )
        }
        
        is CallViewModel.CallUiState.InCall -> {
            ActiveCallScreen(
                call = state.call,
                onEndCall = { viewModel.endCall() }
            )
        }
        
        is CallViewModel.CallUiState.Ended -> {
            LaunchedEffect(Unit) {
                // Show toast or dialog
                delay(2000)
                // Navigate back to home
            }
        }
        
        is CallViewModel.CallUiState.Error -> {
            ErrorDialog(message = state.message)
        }
    }
}

@Composable
fun IncomingCallScreen(
    call: CallStateManager.ActiveCall,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = call.remoteUserName,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Incoming ${call.callType.name} call",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            FloatingActionButton(
                onClick = onReject,
                containerColor = Color.Red
            ) {
                Icon(Icons.Default.CallEnd, "Reject")
            }
            
            FloatingActionButton(
                onClick = onAccept,
                containerColor = Color.Green
            ) {
                Icon(Icons.Default.Call, "Accept")
            }
        }
    }
}
```

---

## 🔄 Complete Flow

### **Incoming Call Flow:**

```
1. Caller initiates call
   └─> CallViewModel.initiateCall()
       └─> CallStateManager.initiateOutgoingCall()
           └─> FirebaseCallSignalingRepository.sendCallOffer()
               ├─> Write to Firebase Realtime Database
               └─> Send FCM wake-up notification

2. Receiver app processes
   ├─> If app is killed: FCM wakes up app
   └─> Firebase Realtime Database listener triggers
       └─> CallStateManager.handleIncomingCall()
           ├─> Check if user is already in call (send "busy" if yes)
           ├─> Update _activeCall StateFlow
           ├─> Emit IncomingCall event
           └─> Start 30-second timeout

3. Receiver sees incoming call UI
   └─> CallScreen observes uiState
       └─> Shows IncomingCallScreen

4a. User accepts call
    └─> CallViewModel.acceptCall()
        └─> CallStateManager.acceptCall()
            ├─> Join Agora channel
            ├─> Send answer SDP to Firebase
            ├─> Update state to CONNECTED
            └─> Emit CallAccepted event

4b. User rejects call
    └─> CallViewModel.rejectCall()
        └─> CallStateManager.rejectCall()
            ├─> Update Firebase state to "rejected"
            ├─> Clear _activeCall
            └─> Emit CallRejected event
                └─> Caller's Firebase listener receives "rejected"
                    └─> Caller's UI shows "Call rejected"
```

---

## 📊 Comparison: Current vs Recommended

| Aspect | Current (FCM + Singleton LiveData) | Recommended (Firebase RTDB + StateFlow) |
|--------|-------------------------------------|----------------------------------------|
| **Real-time signaling** | ❌ FCM has delays (1-5 seconds) | ✅ RTDB updates in <100ms |
| **State management** | ❌ Single LiveData for all calls | ✅ StateFlow with proper state machine |
| **Concurrent calls** | ❌ Can't handle multiple calls | ✅ Properly handles call queue |
| **Type safety** | ❌ String-based status | ✅ Sealed classes |
| **Testability** | ⚠️ Difficult (singleton) | ✅ Dependency injection friendly |
| **Memory leaks** | ⚠️ Singleton holds references | ✅ Lifecycle-aware StateFlow |
| **WebRTC support** | ❌ No SDP/ICE exchange | ✅ Full WebRTC signaling support |
| **Offline resilience** | ⚠️ Limited | ✅ RTDB handles offline/online |

---

## 🎯 Migration Strategy

### **Phase 1: Add Firebase Realtime Database (Keep FCM)**
- Set up RTDB structure
- Implement CallSignalingRepository
- Keep existing FCM for wake-up notifications

### **Phase 2: Replace Singleton with StateFlow**
- Create CallStateManager with StateFlow
- Replace FcmUtils usage gradually
- Add proper sealed classes for events

### **Phase 3: Update UI Layer**
- Create ViewModels
- Move to Jetpack Compose (optional)
- Remove activity-based navigation

### **Phase 4: Remove Gender Logic**
- Abstract differences into roles (caller/receiver)
- Single code path for all users

---

## 📚 Additional Resources

- [Firebase Realtime Database Best Practices](https://firebase.google.com/docs/database/android/structure-data)
- [StateFlow vs LiveData](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [WebRTC Signaling](https://webrtc.org/getting-started/overview#signaling)
- [Agora + Firebase RTDB Example](https://github.com/AgoraIO-Community/Agora-Flutter-SDK/tree/main/example)

---

## ✅ Summary

Your current implementation works but has architectural limitations. For a production calling app:

1. ✅ **Keep FCM** for wake-up notifications when app is killed
2. ✅ **Add Firebase Realtime Database** for actual call signaling
3. ✅ **Replace Singleton LiveData with StateFlow** in a proper repository/manager
4. ✅ **Use sealed classes** for type-safe call events
5. ✅ **Remove gender-specific logic** - abstract into roles
6. ✅ **Add proper state machine** for call lifecycle

The recommended architecture scales better, handles edge cases, and is more maintainable.



