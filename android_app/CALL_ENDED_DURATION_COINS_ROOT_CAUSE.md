# Call Ended Screen - Duration & Coins Mismatch - ROOT CAUSE ANALYSIS

## 🐛 REPORTED ISSUE

**What User Reported:**
- Call Ended screen shows Duration: **3:45** (3 minutes 45 seconds)
- Call Ended screen shows Coins Spent: **22**
- But user only spoke for **2 minutes** actual call time

## 🔍 ROOT CAUSE FINDINGS

### 1️⃣ **PRIMARY ISSUE: Hardcoded Values in UI**

**Location:** `CallEndedScreen.kt`

```kotlin
// Line 77 - HARDCODED DURATION
Text(
    "3:45",  // ❌ HARDCODED VALUE
    color = White,
    fontWeight = FontWeight.Bold
)

// Line 99 - HARDCODED COINS
Text(
    "22",  // ❌ HARDCODED VALUE
    color = White,
    fontWeight = FontWeight.Bold
)
```

**Impact:** 
- Screen ALWAYS shows 3:45 and 22 coins regardless of actual call duration
- No real data is being displayed to the user

---

### 2️⃣ **MISSING DATA FLOW**

#### ✅ Backend RETURNS Correct Data

`EndCallResponse.kt` (lines 119-134):
```kotlin
data class EndCallResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("call")
    val call: CallDto? = null,  // ✅ Contains duration and coinsSpent
    
    @SerializedName("updated_balance")
    val updatedBalance: Int? = null
)

data class CallDto(
    @SerializedName("duration")
    val duration: Int = 0,  // ✅ Backend calculates this
    
    @SerializedName("coins_spent")
    val coinsSpent: Int = 0,  // ✅ Backend calculates this
)
```

#### ✅ ViewModel RECEIVES The Data

`AudioCallViewModel.kt` (lines 620-650):
```kotlin
fun endCall(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
        val result = repository.endCall(callId, duration)
        result.onSuccess { response ->
            // ✅ response.call contains duration and coinsSpent
            val endedCallId = response.call?.id ?: callId
            onSuccess(endedCallId)  // ❌ Only passing callId, not the data!
        }
    }
}
```

#### ❌ Data NEVER Reaches CallEndedScreen

`AudioCallScreen.kt` (lines 117-129):
```kotlin
viewModel.endCall(
    onSuccess = { callId ->
        // ❌ Only callId is received, not duration/coins
        navController.navigate(Screen.CallEnded.createRoute(callId)) {
            popUpTo(Screen.Main.route)
        }
    }
)
```

`Screen.kt` (lines 70-72):
```kotlin
object CallEnded : Screen("call_ended/{callId}") {
    fun createRoute(callId: String) = "call_ended/$callId"
    // ❌ Only accepts callId, no duration or coins parameters
}
```

`CallEndedScreen.kt` (line 18):
```kotlin
fun CallEndedScreen(navController: NavController) {
    // ❌ Receives no parameters except navController
    // ❌ Never fetches call data using callId
    // ❌ Just shows hardcoded values
}
```

---

### 3️⃣ **DURATION MISMATCH ANALYSIS**

#### Why is Backend Duration Different from Client?

**Client-side Duration Tracking:**
```kotlin
// AudioCallScreen.kt (lines 103-111)
LaunchedEffect(state.remoteUserJoined) {
    if (state.remoteUserJoined) {  // ✅ Starts ONLY when receiver joins
        while (true) {
            delay(1000)
            viewModel.updateDuration(state.duration + 1)
        }
    }
}
```

**Client logic:**
- ✅ Starts counting ONLY when `remoteUserJoined = true`
- ✅ This means actual talk time is counted
- ✅ Ringing time is NOT included

**Backend Duration Calculation:**
The backend likely calculates duration as:
```
duration = ended_at - started_at
```

**Possible Backend Issues:**

1. **🔴 Backend starts timer on `initiateCall` API call**
   - Includes ringing time
   - Includes acceptance delay
   - Includes network latency
   
2. **🔴 Backend may have timezone/timestamp issues**
   - Server timestamp vs client timestamp mismatch
   
3. **🔴 Backend may not track when receiver actually joins**
   - No `receiver_joined_at` timestamp in database
   - Only has `started_at` (when call initiated) and `ended_at`

---

### 4️⃣ **COINS CALCULATION ISSUE**

Backend calculates coins based on its own duration:
```
coins_spent = backend_duration * rate_per_minute
```

**Example:**
- User spoke for 2 minutes (actual talk time)
- Backend duration: 3:45 (225 seconds)
- Rate: ~6 coins/minute
- **Coins charged: 22 coins (for 3:45 minutes)**
- **Should be charged: 12 coins (for 2 minutes)**

**Result:** User is overcharged because backend counts extra time!

---

## 📊 DATA FLOW COMPARISON

### Current (Broken) Flow:
```
Call Ends
  ↓
Client sends duration (120s) → Backend receives but ignores
  ↓
Backend calculates own duration (225s) from timestamps
  ↓
Backend calculates coins (22) based on 225s
  ↓
Backend returns EndCallResponse { duration: 225, coins: 22 }
  ↓
ViewModel receives data but doesn't pass it
  ↓
CallEndedScreen shows hardcoded "3:45" and "22"
```

### What Should Happen:
```
Call Ends (remoteUserJoined = true)
  ↓
Client tracks actual talk time (120s)
  ↓
Client sends duration (120s) → Backend USES this value
  ↓
Backend calculates coins (12) based on client's 120s
  ↓
Backend returns EndCallResponse { duration: 120, coins: 12 }
  ↓
ViewModel passes data to CallEndedScreen
  ↓
CallEndedScreen displays actual "2:00" and "12"
```

---

## 🔧 SOLUTION PLAN

### Option 1: Quick Fix (Display Backend Data)
**Pros:** Shows actual charged amount to user
**Cons:** Doesn't fix the root cause (wrong duration calculation)

**Steps:**
1. Modify `Screen.CallEnded` to accept duration & coins parameters
2. Modify `endCall()` to pass response.call.duration and response.call.coinsSpent
3. Modify `CallEndedScreen` to receive and display these values

### Option 2: Proper Fix (Fix Backend Duration Logic)
**Pros:** Fixes the root cause, fair billing
**Cons:** Requires backend changes

**Backend Requirements:**
1. Add new field: `receiver_joined_at` timestamp in calls table
2. When receiver accepts call, set this timestamp
3. Calculate duration as: `ended_at - receiver_joined_at` (not started_at)
4. OR: Trust client-sent duration value if within reasonable bounds

### Option 3: Hybrid Approach (Recommended)
1. **Frontend Fix:**
   - Fetch and display actual backend values (not hardcoded)
   - This ensures transparency - user sees what they're charged
   
2. **Backend Fix:**
   - Track `receiver_joined_at` timestamp
   - Use this for duration calculation
   - Add validation: if client duration differs by >30s, log for investigation

---

## 🚨 IMMEDIATE ACTION REQUIRED

### For Frontend Team (You):
1. Remove hardcoded values from `CallEndedScreen`
2. Fetch call data using `callId` parameter
3. Display actual backend values (even if they're wrong)
4. This gives transparency to users

### For Backend Team:
1. **URGENT:** Investigate duration calculation logic
2. Check when `started_at` timestamp is set (should be when receiver joins)
3. Add logging to compare client vs server duration
4. Consider adding `receiver_joined_at` field

---

## 📝 FILES THAT NEED CHANGES

### Frontend (Client):
- ✅ `CallEndedScreen.kt` - Remove hardcoded values, fetch real data
- ✅ `Screen.kt` - Add duration/coins parameters OR use callId to fetch
- ✅ `AudioCallViewModel.kt` - Pass EndCallResponse data to navigation
- ✅ `NavGraph.kt` - Update route parameters if needed

### Backend (Server):
- 🔴 Duration calculation logic in `/calls/:callId/end` endpoint
- 🔴 Database schema - add `receiver_joined_at` timestamp
- 🔴 Call tracking logic - set timestamp when receiver accepts

---

## 🎯 ACCEPTANCE CRITERIA

After fixes, Call Ended screen should show:
- **Duration:** Actual talk time (when both users connected)
- **Coins:** Fair calculation based on actual talk time
- **Not** including ringing time, connection delays, etc.

---

## 📚 TECHNICAL REFERENCE

### Key Files:
1. `CallEndedScreen.kt` - UI with hardcoded values
2. `AudioCallViewModel.kt:620-650` - endCall() function
3. `AudioCallScreen.kt:103-111` - Duration tracking logic
4. `CallDto.kt:89-93` - Backend response structure
5. `Screen.kt:70-72` - Navigation route definition

### API Endpoints:
- `POST /calls/{callId}/end` - Returns duration & coins
- `GET /calls/{callId}` - Can fetch call details afterwards

### State Variables:
- `state.duration` - Client-tracked duration (correct)
- `response.call.duration` - Backend-calculated duration (might be wrong)
- `response.call.coinsSpent` - Backend-calculated coins (based on backend duration)



