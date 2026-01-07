# 🧪 Test with Agora Console Generated Token

## Why This Test?

Both your Android app and backend are **correctly configured**, but you're still getting Error 110.

This test will **prove** whether the issue is:
- ❌ Agora project settings (IP whitelist, geo-fence, etc.)
- ❌ Network/Firewall blocking Agora
- ❌ Some other Agora console restriction

---

## 📋 Step-by-Step Test

### Step 1: Generate Token from Agora Console

1. **Go to:** [Agora Console](https://console.agora.io/)
2. **Click on:** Your project "Only Care"
3. **Click on:** "Generate Temp Token" button (top right)
4. **Fill in:**
   - **Channel Name:** `test_manual_001`
   - **UID:** `0`
   - **Expiration:** `24 hours`
5. **Click:** "Generate"
6. **Copy** the generated token

---

### Step 2: Hardcode Token in Android App (Temporary)

Open: `AudioCallScreen.kt`

Find the `LaunchedEffect` where `initializeAndJoinCall` is called:

```kotlin
LaunchedEffect(userId, callId, token, channel, role, audioPermission.status) {
    // ... existing code ...
    
    // 🧪 TEMPORARY TEST: Use hardcoded values
    val testToken = "PASTE_YOUR_CONSOLE_TOKEN_HERE"
    val testChannel = "test_manual_001"
    
    android.util.Log.d("AudioCallScreen", "🧪 TESTING WITH CONSOLE TOKEN")
    viewModel.initializeAndJoinCall(testToken, testChannel, false)
}
```

---

### Step 3: Run Test Call

1. **Build and run** the app
2. **Try to initiate an audio call**
3. **Watch the logs** for Agora errors

---

## 📊 Expected Results

### ✅ If Console Token WORKS:

**Meaning:** Your backend token generation has an issue we haven't caught.

**Next Step:** Compare console token vs backend token character-by-character.

---

### ❌ If Console Token ALSO Fails (Error 110):

**Meaning:** Issue is in Agora project settings or network.

**Check these in Agora Console:**

#### A. IP Whitelist

Go to: **Project Settings → Security → IP Whitelist**
- If **enabled**, add `0.0.0.0/0` temporarily to allow all IPs
- Or add your device's public IP

#### B. Geo-Fencing

Go to: **Project Settings → Advanced → Geo-Fencing**
- Should be **DISABLED**
- Or include your region (e.g., Asia, North America)

#### C. Project Status

Go to: **Dashboard → Project Overview**
- Status should be **ACTIVE**
- Not suspended or disabled

#### D. Domain Restrictions

Go to: **Project Settings → Security**
- Check if **domain restrictions** are enabled
- Mobile apps should NOT have domain restrictions

---

## 🔧 Alternative: Check Network Connectivity

If console token also fails, test network:

### Test if Agora Servers are Reachable:

```bash
# Test from your computer (not Android)
ping sd-rtn.com
nslookup sd-rtn.com

# Test Agora API
curl -I https://api.agora.io/dev/v1/projects
```

If these fail, you have a **firewall/network issue** blocking Agora.

---

## 📱 Quick Test Code (Copy-Paste Ready)

Replace the entire `LaunchedEffect` in `AudioCallScreen.kt`:

```kotlin
LaunchedEffect(Unit) {
    // Grant audio permission first
    if (audioPermission.status.isGranted) {
        // 🧪 TEST WITH AGORA CONSOLE TOKEN
        val consoleToken = "PASTE_YOUR_CONSOLE_TOKEN_HERE"
        val consoleChannel = "test_manual_001"
        
        android.util.Log.d("AudioCallScreen", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        android.util.Log.d("AudioCallScreen", "🧪 TESTING WITH AGORA CONSOLE TOKEN")
        android.util.Log.d("AudioCallScreen", "Token: ${consoleToken.take(30)}...")
        android.util.Log.d("AudioCallScreen", "Channel: $consoleChannel")
        android.util.Log.d("AudioCallScreen", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
        delay(500) // Small delay
        viewModel.initializeAndJoinCall(consoleToken, consoleChannel, false)
    }
}
```

---

## 🎯 What This Proves

| Result | Meaning | Action |
|--------|---------|--------|
| ✅ Console token works | Backend issue | Compare tokens |
| ❌ Console token fails (Error 110) | Agora project settings | Check console restrictions |
| ❌ Console token fails (Error 1005) | Invalid token format | Regenerate token |
| ❌ Console token fails (Error 17) | No permission | Check Agora SDK setup |

---

## 📞 After Test: Report Back

After running the test, share:

1. **Did console token work?** (YES/NO)
2. **What error did you get?** (if any)
3. **Screenshot of Agora Console "Generate Token" page**
4. **Logcat output** showing the test

This will give us the **definitive answer** on where the issue is! 🎯



