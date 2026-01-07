# 🐛 Background Call Accept Shows Splash Screen Bug - ROOT CAUSE FOUND

## 📋 Problem Description

**User Report:**
> "When I attend calls in background, app opens but calls not connected. Ringing screen appeared but when I attend it, opens splash screen apps but normally not call connected."

**Exact Behavior:**
1. ✅ App is in background or killed
2. ✅ Incoming call notification appears (ringing screen)
3. ✅ User clicks "Answer" button
4. ❌ **App opens to splash screen instead of call screen**
5. ❌ **Call is NOT connected**
6. ❌ User stuck on splash screen or home screen

---

## 🔍 Root Cause Analysis

### The Problem: Race Condition in Navigation

When a call is accepted from background/killed state, here's what happens:

```
CURRENT FLOW (BROKEN):
1. IncomingCallActivity: User clicks "Answer"
   ↓
2. handleAcceptCall() called
   ↓
3. navigateToCallScreen() launches MainActivity with intent extras
   ↓
4. MainActivity.onCreate() runs
   ↓
5. Shows splash screen (startDestination = Screen.Splash.route)
   ↓
6. handleCallNavigationFromIntent() sets pendingCallNavigation
   ↓
7. setContent {} renders NavGraph
   ↓
8. ❌ PROBLEM: pendingCallNavigation check happens BEFORE splash screen finishes
   ↓
9. ❌ Navigation gets lost or ignored
   ↓
10. ❌ User sees splash → home screen, call never connects
```

### Technical Details

**File: `MainActivity.kt`**

#### Issue 1: Splash Screen Delay Interferes with Navigation

```kotlin
// Line 69-82: Splash screen kept visible for 450ms
override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition { keepSplashScreen }
    
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // ❌ PROBLEM: 450ms delay before app is interactive
    window.decorView.postDelayed({
        keepSplashScreen = false
    }, 450) // This blocks everything!
    
    // ... rest of code
}
```

#### Issue 2: Pending Navigation Timing Issue

```kotlin
// Line 109-140: Pending navigation check
val pending = pendingCallNavigation.value
if (pending != null) {
    // ❌ PROBLEM: This runs DURING splash screen
    // Navigation happens but gets overridden by startDestination
    navCtrl.navigate(route)
    pendingCallNavigation.value = null
}

// Line 142-152: Start destination
NavGraph(
    navController = navCtrl,
    startDestination = startDestination  // ❌ Goes to Splash.route
)
```

**What happens:**
1. `pendingCallNavigation` is set with call data
2. Compose recomposes
3. Navigation to call screen is attempted
4. **BUT** `NavGraph` immediately navigates to `Screen.Splash.route` (start destination)
5. Call screen navigation gets lost
6. User ends up on splash → home flow

#### Issue 3: onNewIntent() and onResume() Race Condition

```kotlin
// Line 158-169: onNewIntent
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    setIntent(intent)
    
    if (intent?.getStringExtra("navigate_to") == "call_screen") {
        handleCallNavigationFromIntent(intent)
    }
}

// Line 171-186: onResume
override fun onResume() {
    super.onResume()
    
    if (intent?.getStringExtra("navigate_to") == "call_screen") {
        handleCallNavigationFromIntent(intent)
        intent.removeExtra("navigate_to")
    }
}
```

**Problem:**
- `onNewIntent()` only called if MainActivity is already running
- If app is killed, `onCreate()` runs first
- Intent extras are processed in `onCreate()`, but navigation fails due to splash screen
- `onResume()` tries again, but by then navigation context is lost

---

## 🎯 Why This Breaks Call Flow

### Expected Flow (What Should Happen):
```
User clicks Answer
  ↓
IncomingCallActivity sends intent to MainActivity
  ↓
MainActivity receives intent with:
  - navigate_to = "call_screen"
  - call_id, caller_id, channel_id, etc.
  ↓
Skip splash screen, go directly to AudioCallScreen/VideoCallScreen
  ↓
User sees call connecting screen
  ↓
Agora connects
  ↓
Call works! ✅
```

### Actual Flow (What's Happening):
```
User clicks Answer
  ↓
IncomingCallActivity sends intent to MainActivity
  ↓
MainActivity.onCreate() runs
  ↓
Shows splash screen (450ms delay)
  ↓
pendingCallNavigation set, but navigation fails
  ↓
NavGraph starts at Splash.route
  ↓
Splash → checks auth → navigates to Home
  ↓
User sees home screen
  ↓
Call data lost
  ↓
No call connection ❌
```

---

## 📊 Evidence from Code

### File: `IncomingCallActivity.kt`

#### Lines 216-234: Intent Creation
```kotlin
private fun navigateToCallScreen() {
    // ✅ All data is correctly passed
    val mainIntent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        putExtra("navigate_to", "call_screen")
        putExtra("caller_id", callerId)
        putExtra("call_id", callId)
        putExtra("agora_app_id", agoraAppId)
        putExtra("agora_token", agoraToken ?: "")
        putExtra("channel_id", channelId)
        putExtra("call_type", callType ?: "AUDIO")
        putExtra("balance_time", balanceTime ?: "")
    }
    
    startActivity(mainIntent)
    // ✅ Intent is launched correctly
}
```

### File: `MainActivity.kt`

#### Lines 308-346: Intent Processing
```kotlin
private fun handleCallNavigationFromIntent(intent: Intent) {
    // ✅ Data extraction works
    val callerId = intent.getStringExtra("caller_id")
    val callId = intent.getStringExtra("call_id")
    // ... all data extracted correctly
    
    // ✅ Validation passes
    if (callerId == null || callId == null || agoraAppId == null || channelId == null) {
        return
    }
    
    // ✅ Pending navigation is set
    pendingCallNavigation.value = PendingCallNavigation(...)
    
    // ❌ BUT: Navigation doesn't happen because of splash screen timing
}
```

---

## 💡 Root Causes Summary

### 1. **Splash Screen Timing**
- Splash screen shown for 450ms minimum
- Blocks all user interaction and navigation
- Call screen navigation attempt happens during splash
- Gets overridden by normal splash → auth → home flow

### 2. **Start Destination Override**
- `NavGraph` always starts at `Screen.Splash.route`
- Even with pending navigation, splash screen runs first
- Call navigation intent gets lost in the process

### 3. **No Special Handling for Call Intents**
- App doesn't check intent extras in `onCreate()` before showing splash
- No "fast path" for incoming call acceptance
- Treats all app launches the same way

### 4. **Compose Recomposition Timing**
- `pendingCallNavigation.value` set triggers recomposition
- But recomposition happens AFTER `NavGraph` is created with splash destination
- By the time navigation is attempted, wrong screen is already showing

---

## ✅ The Fix Required

### Strategy: Skip Splash Screen for Call Intents

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    
    // ✅ CHECK INTENT FIRST!
    val isCallIntent = intent?.getStringExtra("navigate_to") == "call_screen"
    
    if (isCallIntent) {
        // ✅ Skip splash screen immediately for call intents
        splashScreen.setKeepOnScreenCondition { false }
        keepSplashScreen = false
    } else {
        // Normal splash behavior for regular app launch
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        window.decorView.postDelayed({
            keepSplashScreen = false
        }, 450)
    }
    
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // ... rest of code
}
```

### Strategy 2: Override Start Destination for Call Intents

```kotlin
setContent {
    OnlyCareTheme {
        Surface(...) {
            val navCtrl = rememberNavController()
            navController = navCtrl
            
            // ✅ Determine start destination based on intent
            val startDestination = when {
                // Priority 1: Call intent
                intent?.getStringExtra("navigate_to") == "call_screen" -> {
                    // Extract call data and navigate directly
                    handleCallNavigationFromIntent(intent)
                    // Start on call screen instead of splash
                    if (pendingCallNavigation.value?.callType == "VIDEO") {
                        Screen.VideoCall.route
                    } else {
                        Screen.AudioCall.route
                    }
                }
                // Priority 2: Diagnostics mode
                AppConfig.START_WITH_DIAGNOSTICS -> Screen.AgoraDiagnostics.route
                // Priority 3: Normal splash
                else -> Screen.Splash.route
            }
            
            NavGraph(
                navController = navCtrl,
                startDestination = startDestination
            )
        }
    }
}
```

### Strategy 3: Use LaunchedEffect for Navigation

```kotlin
setContent {
    OnlyCareTheme {
        Surface(...) {
            val navCtrl = rememberNavController()
            navController = navCtrl
            
            // ✅ Handle call navigation after NavGraph is ready
            val isCallIntent = intent?.getStringExtra("navigate_to") == "call_screen"
            
            LaunchedEffect(isCallIntent) {
                if (isCallIntent) {
                    // Wait for NavGraph to be ready
                    delay(100)
                    
                    // Extract and validate call data
                    handleCallNavigationFromIntent(intent)
                    
                    // Navigate to call screen
                    val pending = pendingCallNavigation.value
                    if (pending != null) {
                        val route = if (pending.callType == "VIDEO") {
                            Screen.VideoCall.createRoute(...)
                        } else {
                            Screen.AudioCall.createRoute(...)
                        }
                        
                        navCtrl.navigate(route) {
                            // Clear splash screen from back stack
                            popUpTo(Screen.Splash.route) {
                                inclusive = true
                            }
                        }
                        
                        pendingCallNavigation.value = null
                        intent?.removeExtra("navigate_to")
                    }
                }
            }
            
            NavGraph(
                navController = navCtrl,
                startDestination = Screen.Splash.route
            )
        }
    }
}
```

---

## 🎯 Recommended Solution

**Combination of Strategy 1 + Strategy 3:**

1. ✅ **Detect call intent in `onCreate()`**
2. ✅ **Skip splash screen animation for call intents**
3. ✅ **Use `LaunchedEffect` to navigate after NavGraph is ready**
4. ✅ **Clear splash from back stack so user can't go back to it**

This ensures:
- Fast app launch for incoming calls (no 450ms delay)
- Navigation happens at the right time (after NavGraph is initialized)
- User goes directly from accept button → call screen
- No splash screen or home screen in between

---

## 📝 Files to Modify

### 1. `MainActivity.kt`
- Line 69-82: Add intent check before splash delay
- Line 99-155: Add `LaunchedEffect` for call intent navigation
- Line 180-185: Remove redundant intent handling in `onResume()`

### 2. Testing Required After Fix
1. ✅ Accept call when app is in foreground
2. ✅ Accept call when app is in background
3. ✅ **Accept call when app is killed** (main test case)
4. ✅ Normal app launch (splash should still work)
5. ✅ Back button behavior (shouldn't go back to splash from call)

---

## ⚠️ Impact if Not Fixed

**User Experience:**
- Users miss calls because they can't connect
- Poor app rating and reviews
- Frustration: "I clicked answer but nothing happened"
- Loss of revenue (missed calls = no coins)

**Technical Debt:**
- Workarounds in other parts of code
- Confusion about navigation flow
- Duplicate navigation logic
- Hard to debug and maintain

---

## 🚀 Next Steps

1. **DO NOT EXECUTE CODE YET** - User requested diagnosis only
2. Review this document with user
3. Confirm the root cause matches observed behavior
4. Get approval to implement the fix
5. Test thoroughly on both foreground and background scenarios

---

**Status:** ✅ ROOT CAUSE IDENTIFIED  
**Ready for Fix:** ⏳ Awaiting user confirmation  
**Estimated Fix Time:** 15-20 minutes  
**Test Time:** 10-15 minutes  
**Total Time:** 30-35 minutes  

---

**Created:** November 23, 2025  
**Severity:** 🔴 HIGH - Core functionality broken  
**Priority:** 🚨 URGENT - Affects all incoming calls from background


