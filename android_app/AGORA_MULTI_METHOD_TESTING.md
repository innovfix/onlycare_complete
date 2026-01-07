# 🔍 Agora Multi-Method Testing Guide

**Created:** November 22, 2025  
**Purpose:** Test Agora from multiple angles to identify exactly what's working

---

## 🎯 What I've Created

### New Diagnostic Tool: `AgoraDiagnostics`

A comprehensive testing utility that checks Agora from **7 different angles**:

1. ✅ **Credentials Verification** - App ID format and validity
2. ✅ **Token Generation** - Client-side token creation
3. ✅ **Test Mode** - No-token mode (if App Certificate disabled)
4. ✅ **Network Connectivity** - Can we reach Agora servers?
5. ✅ **SDK Initialization** - Does RtcEngine create successfully?
6. ✅ **Join with Token** - Can we join a channel with generated token?
7. ✅ **Join without Token** - Can we join in test mode?

---

## 📱 Files Created

### 1. `AgoraDiagnostics.kt`
**Location:** `app/src/main/java/com/onlycare/app/agora/diagnostics/`

**Features:**
- Runs 7 comprehensive tests
- Detailed logging for each test
- Returns structured results
- Identifies specific failure points

---

### 2. `AgoraDiagnosticsScreen.kt`
**Location:** `app/src/main/java/com/onlycare/app/presentation/screens/diagnostics/`

**UI Features:**
- Visual test results
- Pass/fail indicators
- Detailed error messages
- One-click test execution

---

### 3. `AgoraDiagnosticsViewModel.kt`
**Location:** `app/src/main/java/com/onlycare/app/presentation/screens/diagnostics/`

**Features:**
- Manages test execution
- State management for UI
- Async test running

---

## 🧪 How to Run Tests

### Method 1: Via Code (Quick Test)

Add this to any existing screen or create a test button:

```kotlin
import com.onlycare.app.agora.diagnostics.AgoraDiagnostics

// In a coroutine scope:
val diagnostics = AgoraDiagnostics(context)
val results = diagnostics.runAllTests()

// Results will be logged automatically
// Check logcat for detailed output
```

---

### Method 2: Via UI Screen (Coming Soon)

1. Add to navigation graph
2. Add menu item to access diagnostics
3. Run tests from UI
4. View visual results

---

### Method 3: Via Terminal Command (Fastest for now)

Since the diagnostic screen needs navigation setup, **run tests via code first**:

**Add to your `MaleHomeScreen` or `FemaleHomeScreen` temporarily:**

```kotlin
// At the top of the file
import com.onlycare.app.agora.diagnostics.AgoraDiagnostics

// Inside the Composable, add a test button:
Button(
    onClick = {
        viewModelScope.launch {
            val diagnostics = AgoraDiagnostics(context)
            diagnostics.runAllTests()
            // Check logcat for results
        }
    }
) {
    Text("Test Agora (Check Logs)")
}
```

---

## 📊 What Each Test Checks

### TEST 1: Credentials Verification ✅

**Checks:**
- App ID is 32 characters
- App ID contains only alphanumeric chars
- Credentials are configured

**Expected Result:**
```
✅ App ID is valid (32 characters)
App ID: a41e9245489d44a2ac9af9525f1b508c
```

**If Fails:**
- App ID is malformed
- Check `AgoraConfig.APP_ID`

---

### TEST 2: Token Generation ✅

**Checks:**
- Can generate token client-side
- Token has correct format (starts with "007")
- Token length is reasonable

**Expected Result:**
```
✅ Token generated successfully
Length: 167, Prefix: 007a41e924...
```

**If Fails:**
- Token generation logic broken
- App Certificate issue
- Check `AgoraTokenProvider.kt`

---

### TEST 3: Test Mode Check ℹ️

**Checks:**
- Whether App Certificate is enabled in Agora Console

**Expected Result:**
```
ℹ️ Test mode available (will try later)
Requires: App Certificate disabled in Agora Console
```

**Note:** This is informational, will be tested in TEST 7

---

### TEST 4: Network Connectivity 🌐

**Checks:**
- Can reach `api.agora.io`
- Can reach `api-us.agora.io`
- Can reach `api-eu.agora.io`
- Can reach `api-ap.agora.io`

**Expected Result (Working Network):**
```
✅ Can reach Agora servers
Reachable: https://api.agora.io, https://api-ap.agora.io
```

**Expected Result (Blocked Network):**
```
❌ Cannot reach any Agora servers
Tested: api.agora.io, api-us.agora.io, ...
```

**If Fails:**
- ❌ **Network/firewall is blocking Agora**
- ✅ **Test on mobile data immediately**

---

### TEST 5: SDK Initialization 🔧

**Checks:**
- Can create RtcEngine instance
- SDK is properly included in project
- Context is valid

**Expected Result:**
```
✅ RTC Engine created successfully
Engine class: RtcEngineImpl
```

**If Fails:**
- Agora SDK not included in gradle
- Context is null
- Critical SDK issue

---

### TEST 6: Join Channel with Token 🔑

**Checks:**
- Can join channel with client-generated token
- UID matching is correct
- Token is valid

**Expected Result (Working):**
```
✅ Successfully joined channel with token
Channel: agora_test_channel_12345, UID: 0
```

**Expected Result (Network Issue):**
```
❌ Failed to join channel
Error code: 110, Join result: 0
```

**If Fails with Error 110:**
- Network is blocking (confirmed)
- **Test on mobile data**

**If Fails with Error 109:**
- Token is invalid
- UID mismatch
- Check token generation

---

### TEST 7: Join Channel without Token 🧪

**Checks:**
- Can join in test mode (no token)
- App Certificate status in Console

**Expected Result (Certificate Enabled):**
```
ℹ️ Token required (App Certificate enabled)
Error 109 is expected when certificate is enabled
```

**Expected Result (Certificate Disabled):**
```
✅ Test mode works! (No token required)
App Certificate is NOT enabled in Console
```

**If Fails:**
- Check Agora Console settings
- May need to enable/disable certificate

---

## 📋 Interpreting Results

### Scenario 1: All Tests Pass ✅

```
7 / 7 tests passed
🎉 ALL TESTS PASSED! Agora is working correctly!
```

**Conclusion:** Your Agora integration is **PERFECT!**

**Action:** Deploy to production with confidence!

---

### Scenario 2: Network Tests Fail ❌

```
TEST 1: ✅ Credentials Check
TEST 2: ✅ Token Generation  
TEST 3: ℹ️ Test Mode Check
TEST 4: ❌ Network Connectivity ← FAILED
TEST 5: ✅ SDK Initialization
TEST 6: ❌ Join with Token ← FAILED (Error 110)
TEST 7: ❌ Join without Token ← FAILED (Error 110)
```

**Conclusion:** Network is blocking Agora servers

**Action:**
1. ✅ **Test on mobile data (4G/5G)**
2. Configure WiFi router
3. Use VPN

---

### Scenario 3: Token Tests Fail ❌

```
TEST 1: ❌ Credentials Check ← FAILED
TEST 2: ❌ Token Generation ← FAILED
TEST 4: ✅ Network Connectivity
TEST 5: ✅ SDK Initialization
TEST 6: ❌ Join with Token ← FAILED (Error 109)
```

**Conclusion:** App ID or Certificate issue

**Action:**
1. Verify App ID in `AgoraConfig.kt`
2. Verify App Certificate in `AgoraTokenProvider.kt`
3. Check Agora Console settings

---

### Scenario 4: SDK Tests Fail ❌

```
TEST 5: ❌ SDK Initialization ← FAILED
```

**Conclusion:** Agora SDK not properly integrated

**Action:**
1. Check `build.gradle.kts` has `implementation("io.agora.rtc:full-sdk:4.5.0")`
2. Sync gradle
3. Clean and rebuild

---

## 🚀 Quick Testing Steps

### Step 1: Run Diagnostics

```bash
# Build and install app
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

# Open app
# Run diagnostics (via temporary button or code)

# View logs
adb logcat | grep "AgoraDiagnostics"
```

---

### Step 2: Check Results

Look for this in logs:

```
AgoraDiagnostics: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
AgoraDiagnostics: 📊 DIAGNOSTIC SUMMARY
AgoraDiagnostics: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
AgoraDiagnostics: ✅ Credentials Check: ✅ App ID is valid
AgoraDiagnostics: ✅ Token Generation: ✅ Token generated successfully
AgoraDiagnostics: ℹ️ Test Mode Availability: ℹ️ Test mode available
AgoraDiagnostics: ❌ Network Connectivity: ❌ Cannot reach any Agora servers
AgoraDiagnostics: ✅ SDK Initialization: ✅ RTC Engine created successfully
AgoraDiagnostics: ❌ Join with Token: ❌ Failed to join channel
AgoraDiagnostics: ❌ Join without Token: ❌ Failed to join without token
AgoraDiagnostics: 
AgoraDiagnostics: 📈 RESULTS: 3 / 7 tests passed
AgoraDiagnostics: ⚠️ Some tests failed. Check details above.
```

---

### Step 3: Test on Mobile Data

If network tests fail:

```bash
# Turn OFF WiFi
# Turn ON Mobile Data (4G/5G)
# Run diagnostics again
# Compare results
```

**Expected on Mobile Data:**
```
✅ Network Connectivity: ✅ Can reach Agora servers
✅ Join with Token: ✅ Successfully joined channel
```

---

## 📝 Adding Diagnostics Button (Temporary)

### Quick Way to Access Diagnostics:

**In `MaleHomeScreen.kt` or `FemaleHomeScreen.kt`:**

```kotlin
// Add this import at top
import com.onlycare.app.agora.diagnostics.AgoraDiagnostics
import kotlinx.coroutines.launch

// Inside your Composable, add button in a visible location:
val scope = rememberCoroutineScope()
val context = LocalContext.current

FloatingActionButton(
    onClick = {
        scope.launch {
            val diagnostics = AgoraDiagnostics(context)
            diagnostics.runAllTests()
            // Results will be in logcat
            android.util.Log.i("HomeScreen", "Diagnostics complete! Check logcat.")
        }
    },
    modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
) {
    Icon(Icons.Default.Build, contentDescription = "Test Agora")
}
```

---

## 🎯 Expected Test Results by Network

### On WiFi (Blocking):
```
✅ Credentials: PASS
✅ Token Gen: PASS
✅ SDK Init: PASS
❌ Network: FAIL ← Key indicator
❌ Join (Token): FAIL (Error 110)
❌ Join (No Token): FAIL (Error 110)
```

### On Mobile Data:
```
✅ Credentials: PASS
✅ Token Gen: PASS
✅ SDK Init: PASS
✅ Network: PASS ← Now working!
✅ Join (Token): PASS ← Now working!
ℹ️ Join (No Token): PASS or FAIL (Error 109 expected)
```

---

## 📊 Summary

### What This Testing Reveals:

1. **Credentials** - Are they configured correctly?
2. **Token Generation** - Is our client-side logic working?
3. **Network** - Can we reach Agora at all?
4. **SDK** - Is Agora properly integrated?
5. **Join (Token)** - Does the complete flow work?
6. **Join (No Token)** - Is test mode available?

### Next Steps Based on Results:

**If 6-7 tests pass:**
- ✅ Agora is working!
- ✅ Deploy with confidence

**If network tests fail:**
- ⚠️ Test on mobile data
- ⚠️ Fix WiFi configuration

**If token tests fail:**
- ⚠️ Check credentials
- ⚠️ Verify UID matching

**If SDK tests fail:**
- ⚠️ Check gradle dependencies
- ⚠️ Rebuild project

---

## 🔧 Troubleshooting Commands

### View All Diagnostic Logs:
```bash
adb logcat -c  # Clear logs
adb logcat | grep -E "AgoraDiagnostics|AgoraTokenProvider|AgoraManager"
```

### Filter by Test:
```bash
# Credentials
adb logcat | grep "TEST 1"

# Token Generation  
adb logcat | grep "TEST 2"

# Network
adb logcat | grep "TEST 4"

# Join Tests
adb logcat | grep "TEST 6\|TEST 7"
```

---

## ✅ Checklist

Before considering Agora broken, verify:

- [ ] Ran diagnostics on WiFi
- [ ] Ran diagnostics on mobile data
- [ ] Compared results between networks
- [ ] Checked all 7 test results
- [ ] Read error messages carefully
- [ ] Verified App ID and Certificate

**If tests pass on mobile data but fail on WiFi:**
- ✅ Your code is PERFECT
- ❌ WiFi network is blocking Agora
- 🎯 Fix network or use mobile data

---

**Created by:** Comprehensive Diagnostic Tool  
**Purpose:** Multi-angle Agora verification  
**Status:** Ready for testing  
**Next:** Run diagnostics and share results!



