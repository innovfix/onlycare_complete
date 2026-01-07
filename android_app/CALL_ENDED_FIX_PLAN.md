# Call Ended Screen Fix - DETAILED IMPLEMENTATION PLAN

## 🎯 GOAL
Fix the Call Ended screen to show actual duration and coins spent instead of hardcoded values.

---

## 📋 IMPLEMENTATION OPTIONS

### ⭐ OPTION 1: Pass Data via Navigation (Recommended)

**Advantages:**
- ✅ Immediate display (no API call needed)
- ✅ Data already available in ViewModel
- ✅ No loading state needed
- ✅ Works offline after call ends

**Steps:**

#### Step 1: Modify Navigation Route
**File:** `app/src/main/java/com/onlycare/app/presentation/navigation/Screen.kt`

**Current (Line 70-72):**
```kotlin
object CallEnded : Screen("call_ended/{callId}") {
    fun createRoute(callId: String) = "call_ended/$callId"
}
```

**Change To:**
```kotlin
object CallEnded : Screen("call_ended/{callId}/{duration}/{coinsSpent}") {
    fun createRoute(
        callId: String,
        duration: Int,  // in seconds
        coinsSpent: Int
    ) = "call_ended/$callId/$duration/$coinsSpent"
}
```

---

#### Step 2: Update AudioCallViewModel to Pass Data
**File:** `app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallViewModel.kt`

**Current (Lines 620-650):**
```kotlin
fun endCall(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    val callId = _state.value.callId
    val duration = _state.value.duration
    
    // ... leave channel ...
    
    viewModelScope.launch {
        val result = repository.endCall(callId, duration)
        result.onSuccess { response ->
            val endedCallId = response.call?.id ?: callId
            onSuccess(endedCallId)  // ❌ Only passing callId
        }.onFailure { error ->
            onError(error.message ?: "Failed to end call")
            onSuccess(callId)
        }
    }
}
```

**Change To:**
```kotlin
fun endCall(
    onSuccess: (callId: String, duration: Int, coinsSpent: Int) -> Unit,
    onError: (String) -> Unit
) {
    val callId = _state.value.callId
    val duration = _state.value.duration
    
    // Stop foreground service
    stopCallService()

    // Leave Agora channel first
    agoraManager?.leaveChannel()
    
    if (callId == null || callId.isEmpty()) {
        Log.w(TAG, "No callId to end call - using duration only")
        // Use local values if no backend call
        onSuccess("", duration, 0)
        return
    }
    
    viewModelScope.launch {
        val result = repository.endCall(callId, duration)
        result.onSuccess { response ->
            val endedCallId = response.call?.id ?: callId
            val backendDuration = response.call?.duration ?: duration
            val coinsSpent = response.call?.coinsSpent ?: 0
            
            Log.d(TAG, "✅ Call ended - Duration: $backendDuration, Coins: $coinsSpent")
            
            // Pass all three values
            onSuccess(endedCallId, backendDuration, coinsSpent)
        }.onFailure { error ->
            Log.e(TAG, "Failed to end call: ${error.message}")
            // Still navigate with local values
            onSuccess(callId, duration, 0)
        }
    }
}
```

---

#### Step 3: Update AudioCallScreen Navigation Calls
**File:** `app/src/main/java/com/onlycare/app/presentation/screens/call/AudioCallScreen.kt`

**Find All Instances (5 places):**

**Instance 1 - Lines 117-129:**
```kotlin
viewModel.endCall(
    onSuccess = { callId ->
        navController.navigate(Screen.CallEnded.createRoute(callId)) {
            popUpTo(Screen.Main.route)
        }
    },
    onError = {
        navController.navigate(Screen.CallEnded.createRoute(state.callId ?: "1")) {
            popUpTo(Screen.Main.route)
        }
    }
)
```

**Change To:**
```kotlin
viewModel.endCall(
    onSuccess = { callId, duration, coinsSpent ->
        navController.navigate(
            Screen.CallEnded.createRoute(callId, duration, coinsSpent)
        ) {
            popUpTo(Screen.Main.route)
        }
    },
    onError = { error ->
        // Use current state values if API fails
        navController.navigate(
            Screen.CallEnded.createRoute(
                state.callId ?: "1",
                state.duration,
                0
            )
        ) {
            popUpTo(Screen.Main.route)
        }
    }
)
```

**Instance 2 - Lines 176-188 (in RingingCallUI):**
```kotlin
viewModel.endCall(
    onSuccess = { callId ->
        navController.navigate(Screen.CallEnded.createRoute(callId)) {
            popUpTo(Screen.Main.route)
        }
    },
    onError = {
        navController.navigate(Screen.CallEnded.createRoute(state.callId ?: "1")) {
            popUpTo(Screen.Main.route)
        }
    }
)
```

**Change To:** (Same as above)

**Instance 3 - Lines 210-222 (in ConnectedCallUI end button):**
Same change as above.

---

#### Step 4: Update NavGraph Route Definition
**File:** `app/src/main/java/com/onlycare/app/presentation/navigation/NavGraph.kt`

**Current (Lines 239-244):**
```kotlin
composable(
    route = Screen.CallEnded.route,
    arguments = listOf(navArgument("callId") { type = NavType.StringType })
) {
    CallEndedScreen(navController = navController)
}
```

**Change To:**
```kotlin
composable(
    route = Screen.CallEnded.route,
    arguments = listOf(
        navArgument("callId") { type = NavType.StringType },
        navArgument("duration") { type = NavType.IntType },
        navArgument("coinsSpent") { type = NavType.IntType }
    )
) { backStackEntry ->
    val callId = backStackEntry.arguments?.getString("callId") ?: ""
    val duration = backStackEntry.arguments?.getInt("duration") ?: 0
    val coinsSpent = backStackEntry.arguments?.getInt("coinsSpent") ?: 0
    
    CallEndedScreen(
        navController = navController,
        callId = callId,
        duration = duration,
        coinsSpent = coinsSpent
    )
}
```

---

#### Step 5: Update CallEndedScreen to Receive and Display Data
**File:** `app/src/main/java/com/onlycare/app/presentation/screens/call/CallEndedScreen.kt`

**Current (Lines 17-18):**
```kotlin
@Composable
fun CallEndedScreen(navController: NavController) {
```

**Change To:**
```kotlin
@Composable
fun CallEndedScreen(
    navController: NavController,
    callId: String,
    duration: Int,  // in seconds
    coinsSpent: Int
) {
```

**Current (Lines 75-80) - Duration Display:**
```kotlin
Text("Duration:", color = TextGray)
Text(
    "3:45",  // ❌ HARDCODED
    color = White,
    fontWeight = FontWeight.Bold
)
```

**Change To:**
```kotlin
Text("Duration:", color = TextGray)
Text(
    formatDuration(duration),  // ✅ Use actual duration
    color = White,
    fontWeight = FontWeight.Bold
)
```

**Current (Lines 89-102) - Coins Display:**
```kotlin
Text("Coins Spent:", color = TextGray)
Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = Icons.Default.Circle,
        contentDescription = null,
        tint = androidx.compose.ui.graphics.Color(0xFFFFC107),
        modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
        "22",  // ❌ HARDCODED
        color = White,
        fontWeight = FontWeight.Bold
    )
}
```

**Change To:**
```kotlin
Text("Coins Spent:", color = TextGray)
Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = Icons.Default.Circle,
        contentDescription = null,
        tint = androidx.compose.ui.graphics.Color(0xFFFFC107),
        modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
        "$coinsSpent",  // ✅ Use actual coins
        color = White,
        fontWeight = FontWeight.Bold
    )
}
```

**Add Helper Function at Bottom:**
```kotlin
/**
 * Format duration in seconds to MM:SS format
 */
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}
```

---

#### Step 6: Update VideoCallViewModel & VideoCallScreen (Same Changes)

Apply the same changes to:
- `VideoCallViewModel.kt`
- `VideoCallScreen.kt`

---

### ⚙️ OPTION 2: Fetch Data Using CallId

**Advantages:**
- ✅ Always shows latest backend data
- ✅ No need to pass data through navigation

**Disadvantages:**
- ❌ Requires API call (loading state)
- ❌ Slower display
- ❌ Needs internet connection

**Steps:**

#### Step 1: Create ViewModel for CallEndedScreen
```kotlin
class CallEndedViewModel(
    private val repository: ApiDataRepository,
    private val callId: String
) : ViewModel() {
    
    private val _state = MutableStateFlow(CallEndedState())
    val state = _state.asStateFlow()
    
    init {
        loadCallData()
    }
    
    private fun loadCallData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val result = repository.getCallStatus(callId)
            result.onSuccess { call ->
                _state.update {
                    it.copy(
                        duration = call.duration,
                        coinsSpent = call.coinsSpent,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class CallEndedState(
    val duration: Int = 0,
    val coinsSpent: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

#### Step 2: Update CallEndedScreen
```kotlin
@Composable
fun CallEndedScreen(
    navController: NavController,
    callId: String,
    viewModel: CallEndedViewModel = viewModel(
        factory = CallEndedViewModelFactory(callId)
    )
) {
    val state by viewModel.state.collectAsState()
    
    if (state.isLoading) {
        // Show loading
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Rest of UI using state.duration and state.coinsSpent
}
```

---

## 🎯 RECOMMENDED APPROACH

**Use OPTION 1** (Pass data via navigation) because:
1. ✅ Faster (no extra API call)
2. ✅ Simpler implementation
3. ✅ Data already available from endCall response
4. ✅ Works offline
5. ✅ Better UX (instant display)

---

## 🔄 ALSO NEEDED: Backend Investigation

Even after frontend fix, backend team should investigate:

### Backend Checklist:
1. **When does `started_at` get set?**
   - Should be when receiver ACCEPTS call, not when caller initiates

2. **How is duration calculated?**
   - Current: `ended_at - started_at` ❌
   - Should be: `ended_at - receiver_joined_at` ✅

3. **Database changes needed:**
```sql
ALTER TABLE calls ADD COLUMN receiver_joined_at TIMESTAMP;
```

4. **Update `/calls/:callId/accept` endpoint:**
```javascript
// When receiver accepts call
call.receiver_joined_at = new Date();
await call.save();
```

5. **Update `/calls/:callId/end` endpoint:**
```javascript
// Calculate duration from when receiver joined
const duration = Math.floor(
    (call.ended_at - call.receiver_joined_at) / 1000
);
// Not from started_at!
```

6. **Add validation:**
```javascript
// Compare client vs server duration
if (Math.abs(clientDuration - serverDuration) > 30) {
    logger.warn('Duration mismatch', {
        callId,
        clientDuration,
        serverDuration,
        diff: Math.abs(clientDuration - serverDuration)
    });
}
```

---

## ✅ TESTING CHECKLIST

After implementing fixes:

### Frontend Testing:
- [ ] Make a 2-minute call
- [ ] End the call
- [ ] Call Ended screen shows correct duration (~2:00)
- [ ] Call Ended screen shows correct coins spent
- [ ] No hardcoded values visible
- [ ] Test with different call durations (30s, 5min, 10min)
- [ ] Test when backend API fails (should show local duration)

### Backend Testing:
- [ ] Check logs for duration calculation
- [ ] Compare client vs server duration
- [ ] Verify coins calculation is based on actual talk time
- [ ] Test timezone handling
- [ ] Verify database timestamps are correct

---

## 📊 BEFORE vs AFTER

### Before (Current):
```
User speaks for 2 minutes
Backend calculates 3:45 (unknown reason)
Coins: 22 (based on 3:45)
Screen shows: "3:45" and "22" (hardcoded, happens to match backend's wrong value)
```

### After (Frontend Fix):
```
User speaks for 2 minutes
Backend calculates 3:45 (still wrong, but now visible)
Coins: 22 (based on 3:45)
Screen shows: Actual backend values (3:45 and 22)
User sees they're being overcharged → Complains to support
```

### After (Backend Fix):
```
User speaks for 2 minutes
Backend calculates 2:00 (correct - from receiver_joined_at)
Coins: 12 (based on 2:00)
Screen shows: 2:00 and 12
User is charged fairly ✅
```

---

## 🚀 IMPLEMENTATION ORDER

1. **First:** Frontend fix (Option 1) - Shows actual data
2. **Second:** Backend investigation - Logs duration calculation
3. **Third:** Backend fix - Correct duration calculation
4. **Fourth:** Validation - Compare client vs server duration

---

## 📝 FILES TO MODIFY (Summary)

### Frontend (Option 1 - Recommended):
1. ✅ `Screen.kt` - Add duration & coinsSpent parameters
2. ✅ `AudioCallViewModel.kt` - Modify endCall() signature
3. ✅ `AudioCallScreen.kt` - Update all endCall() calls (5 instances)
4. ✅ `NavGraph.kt` - Update CallEnded route definition
5. ✅ `CallEndedScreen.kt` - Receive and display actual data
6. ✅ `VideoCallViewModel.kt` - Same changes as Audio
7. ✅ `VideoCallScreen.kt` - Same changes as Audio

### Backend:
1. 🔴 Database migration - Add `receiver_joined_at` column
2. 🔴 `/calls/:callId/accept` - Set receiver_joined_at timestamp
3. 🔴 `/calls/:callId/end` - Use receiver_joined_at for duration
4. 🔴 Add logging - Compare client vs server duration

---

## ⏱️ ESTIMATED TIME

- Frontend Fix (Option 1): **2-3 hours**
- Backend Investigation: **1-2 hours**
- Backend Fix: **3-4 hours**
- Testing: **2 hours**

**Total: 8-11 hours** for complete fix



