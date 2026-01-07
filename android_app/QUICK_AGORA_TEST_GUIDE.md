# ⚡ QUICK AGORA TESTING - 3 Methods

**Choose the easiest method for you:**

---

## 🎯 Method 1: Terminal Command (EASIEST - 30 seconds)

### Just run this command while app is running:

```bash
adb shell am broadcast -a com.onlycare.app.TEST_AGORA
```

**Then check logs:**

```bash
adb logcat | grep "AgoraDiagnostics"
```

**That's it!** Results will show in logs within 5-10 seconds.

---

## 🎯 Method 2: Add Test Button (RECOMMENDED - 2 minutes)

### 1. Open any screen file (like `MaleHomeScreen.kt`)

### 2. Add these imports at the top:

```kotlin
import com.onlycare.app.agora.diagnostics.AgoraDiagnostics
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
```

### 3. Add this button inside your Composable:

```kotlin
val scope = rememberCoroutineScope()
val context = LocalContext.current

// Add button anywhere visible:
Button(
    onClick = {
        scope.launch {
            AgoraDiagnostics(context).runAllTests()
        }
    }
) {
    Text("🔍 Test Agora")
}
```

### 4. Build, install, and click button

### 5. Check logcat:

```bash
adb logcat | grep "AgoraDiagnostics"
```

---

## 🎯 Method 3: Automatic on App Start (FASTEST - 1 minute)

### 1. Open `HimaApplication.kt` (or your Application class)

### 2. Add to `onCreate()`:

```kotlin
import com.onlycare.app.agora.diagnostics.AgoraDiagnostics
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OnlyCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Add this:
        GlobalScope.launch {
            delay(3000) // Wait for app to fully start
            AgoraDiagnostics(this@OnlyCareApplication).runAllTests()
        }
    }
}
```

### 3. Build, install, launch app

### 4. Wait 3-5 seconds, then check logs:

```bash
adb logcat | grep "AgoraDiagnostics"
```

---

## 📊 What You'll See in Logs

### Example Output:

```
AgoraDiagnostics: 🔍 AGORA COMPREHENSIVE DIAGNOSTICS
AgoraDiagnostics: 
AgoraDiagnostics: ━━━ TEST 1: Verify Credentials ━━━
AgoraDiagnostics: ✅ App ID is valid: a41e9245489d44a2ac9af9525f1b508c
AgoraDiagnostics: 
AgoraDiagnostics: ━━━ TEST 2: Client-Side Token Generation ━━━
AgoraDiagnostics: ✅ Token generated: 007a41e9245489d44a2a... (167 chars)
AgoraDiagnostics: 
AgoraDiagnostics: ━━━ TEST 3: Test Mode Check ━━━
AgoraDiagnostics: ℹ️ Test mode: Join channel with empty token
AgoraDiagnostics: 
AgoraDiagnostics: ━━━ TEST 4: Network Connectivity ━━━
AgoraDiagnostics: ✅ Reachable: https://api.agora.io
AgoraDiagnostics: ❌ Not reachable: https://api-us.agora.io
AgoraDiagnostics: 
AgoraDiagnostics: ━━━ TEST 5: SDK Initialization ━━━
AgoraDiagnostics: ✅ RTC Engine created: RtcEngineImpl
AgoraDiagnostics: 
AgoraDiagnostics: ━━━ TEST 6: Join Channel (With Client Token) ━━━
AgoraDiagnostics: 📊 joinChannel() result: 0
AgoraDiagnostics: ✅ onJoinChannelSuccess: channel=agora_test_channel_12345, uid=0
AgoraDiagnostics: 
AgoraDiagnostics: ━━━ TEST 7: Join Channel (Without Token - Test Mode) ━━━
AgoraDiagnostics: ⚠️ Error 109: Token required (App Certificate enabled)
AgoraDiagnostics: 
AgoraDiagnostics: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
AgoraDiagnostics: 📊 DIAGNOSTIC SUMMARY
AgoraDiagnostics: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
AgoraDiagnostics: ✅ Credentials Check: ✅ App ID is valid (32 characters)
AgoraDiagnostics: ✅ Token Generation: ✅ Token generated successfully
AgoraDiagnostics: ℹ️ Test Mode Availability: ℹ️ Test mode available
AgoraDiagnostics: ✅ Network Connectivity: ✅ Can reach Agora servers
AgoraDiagnostics: ✅ SDK Initialization: ✅ RTC Engine created successfully
AgoraDiagnostics: ✅ Join with Token: ✅ Successfully joined channel with token
AgoraDiagnostics: ℹ️ Join without Token: ℹ️ Token required (App Certificate enabled)
AgoraDiagnostics: 
AgoraDiagnostics: 📈 RESULTS: 6 / 7 tests passed
AgoraDiagnostics: 🎉 Agora is working! (1 informational result)
```

---

## ✅ Quick Interpretation

### All Green ✅ = Agora Works!

```
✅ Credentials Check
✅ Token Generation
✅ Network Connectivity
✅ SDK Initialization
✅ Join with Token
```

**Conclusion:** Your Agora integration is perfect! 🎉

---

### Network Red ❌ = WiFi Blocking

```
✅ Credentials Check
✅ Token Generation
❌ Network Connectivity ← Problem here
✅ SDK Initialization
❌ Join with Token ← Fails due to network
```

**Conclusion:** Network is blocking Agora servers

**Solution:** Test on mobile data (4G/5G)

---

### Token Red ❌ = Configuration Issue

```
❌ Credentials Check ← Problem here
❌ Token Generation ← Problem here
✅ Network Connectivity
✅ SDK Initialization
❌ Join with Token
```

**Conclusion:** App ID or Certificate incorrect

**Solution:** Check `AgoraConfig.kt` and `AgoraTokenProvider.kt`

---

## 🚀 Fastest Test Right Now

### Copy-paste this into terminal:

```bash
# Build app
cd "/Users/bala/Desktop/App Projects/onlycare_app"
./gradlew assembleDebug

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app (replace with your main activity)
adb shell am start -n com.onlycare.app/.presentation.MainActivity

# Wait 5 seconds for app to start
sleep 5

# Watch logs (press Ctrl+C to stop)
adb logcat -c  # Clear old logs first
adb logcat | grep "AgoraDiagnostics"
```

**Note:** You'll need to trigger the test using one of the 3 methods above.

---

## 📱 Test on Mobile Data

### Step 1: Run on WiFi

```bash
# Run test while connected to WiFi
# Note results
```

### Step 2: Switch to Mobile Data

```bash
# On device:
# - Turn OFF WiFi
# - Turn ON Mobile Data (4G/5G)
```

### Step 3: Run test again

```bash
# Run same test on mobile data
# Compare results
```

### Expected Difference:

**WiFi:**
```
❌ Network Connectivity: Cannot reach Agora
❌ Join with Token: Failed (Error 110)
```

**Mobile Data:**
```
✅ Network Connectivity: Can reach Agora
✅ Join with Token: Success!
```

**If this happens:** Your code is PERFECT, WiFi is blocking Agora!

---

## 🎯 Choose Your Method

**Easiest:** Method 3 (Auto on start)  
**Most Control:** Method 2 (Button)  
**No Code Change:** Method 1 (ADB command - requires broadcast receiver)

**Recommendation:** Use **Method 2 (Button)** - gives you full control!

---

## 🔧 If You Need Help

### Share these logs:

```bash
# Get full diagnostic output
adb logcat -d | grep -A 100 "AGORA COMPREHENSIVE DIAGNOSTICS" > agora_test.txt

# Share agora_test.txt
```

This will show exactly which tests passed/failed!

---

**Time Required:** 1-2 minutes  
**Difficulty:** Easy  
**Result:** Know exactly what's working with Agora!



