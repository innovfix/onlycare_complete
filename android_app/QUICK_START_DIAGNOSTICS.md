# ⚡ QUICK START - Diagnostic Screen

## 🎯 What is This?

Your app now launches with an **Agora diagnostic screen** that:
- Tests Agora integration automatically
- Shows results visually
- Helps identify network vs code issues

---

## 🚀 Quick Start (30 Seconds)

### Just build and run:

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**That's it!** Diagnostic screen appears first and tests run automatically.

---

## ⚙️ Toggle On/Off

**File:** `app/src/main/java/com/onlycare/app/utils/AppConfig.kt`

```kotlin
// Show diagnostics on launch:
const val START_WITH_DIAGNOSTICS = true   // ⬅️ For testing

// Normal app flow:
const val START_WITH_DIAGNOSTICS = false  // ⬅️ For production
```

---

## 📊 What You'll See

### On Launch:
1. Diagnostic screen appears
2. 7 tests run automatically (5-10 seconds)
3. Results shown with ✅ (pass) or ❌ (fail)
4. Two buttons: "Run Tests" | "Continue"

### If All Pass ✅:
```
✅ 6 / 7 Tests Passed
🎉 Agora works perfectly!
```
Click "Continue" to use app.

### If Network Fails ❌:
```
❌ Network Connectivity FAILED
⚠️ WiFi is blocking Agora
```
**Solution:** Test on mobile data (4G/5G)

---

## 🧪 Test on Mobile Data

1. Turn OFF WiFi on device
2. Turn ON Mobile Data (4G/5G)
3. Click "Run Tests" in app
4. Compare results

**Expected:** Network test will PASS on mobile data!

---

## 🎯 Quick Decision Guide

### Tests Pass on Mobile Data?
✅ **Your code is perfect!**  
⚠️ **WiFi is blocking Agora**

### Tests Pass on WiFi?
✅ **Everything works!**  
🎉 **You're ready for production!**

### Tests Fail Everywhere?
❌ **Check credentials**  
📝 **Verify App ID and Certificate**

---

## 📱 Visual Guide

```
App Launch
    ↓
Diagnostic Screen Appears
    ↓
Tests Run (5-10 seconds)
    ↓
Results Shown
    ↓
Click "Continue"
    ↓
Normal App Flow
```

---

## 💡 Pro Tips

**Skip Tests:** Just click "Continue" immediately  
**Re-run Tests:** Switch network, click "Run Tests"  
**For Production:** Set `START_WITH_DIAGNOSTICS = false`

---

## 📞 Need Help?

Check logs:
```bash
adb logcat | grep "AgoraDiagnostics"
```

Share screenshot of results screen with team.

---

**Ready!** Build the app and launch to see diagnostics! 🚀



