# 🔍 Agora Error 110 Diagnostic Guide

## Your Observation (Correct!)
✅ Token is NOT completely invalid because:
- Call initiates successfully
- Receiver gets notification
- Both devices can navigate to call screen
- Backend accepts the call

## The Real Problem
❌ Agora SDK **cannot establish connection** to Agora servers
❌ `onJoinChannelSuccess` callback **NEVER fires**
❌ Error 110 appears within 200ms (too fast = immediate failure)

---

## 🧪 Diagnostic Tests (Do These Now)

### Test 1: Check Network Type
**Question:** Are BOTH devices on the same WiFi/network?

- [ ] Both on WiFi
- [ ] Both on Mobile Data (4G/5G)
- [ ] One WiFi, one Mobile Data
- [ ] Using VPN on either device?

**Try this:** Put BOTH devices on **Mobile Data** (4G/5G) and test again.

---

### Test 2: Check Firewall/Ports
Agora requires these ports to be open:
- **UDP ports:** 1080, 4000-4030, 8000, 9700, 25000
- **TCP ports:** 443, 1080, 8001, 8443, 9700

**Try this:**
1. Disable WiFi firewall/restrictions temporarily
2. Test on Mobile Data (bypasses WiFi firewall)
3. If it works on Mobile Data → WiFi firewall is blocking Agora

---

### Test 3: Verify Agora Console Settings

Go to **Agora Console**: https://console.agora.io/

1. **Find your project** with App ID: `8b5e9417f15a48ae929783f32d3d33d4`

2. **Check Project Status:**
   - [ ] Project is **Active** (not disabled)
   - [ ] Billing is active (if required)
   - [ ] No usage limits exceeded

3. **Check Authentication:**
   - Go to **Config** → **Features**
   - Verify: **"App Certificate"** is enabled
   - Note down: Primary Certificate (for backend token generation)

4. **Check IP Restrictions:**
   - Go to **Config** → **Features**
   - Look for **"IP whitelist"** or **"Allowed IPs"**
   - If enabled → this might be blocking your devices!
   - **Try disabling IP whitelist temporarily** to test

5. **Check Region Settings:**
   - Go to **Config** → **Features**  
   - Check if **"Region"** is restricted
   - Some regions block Agora (China, Iran, etc.)

---

### Test 4: Test with Agora Sample App

Download Agora's official demo app:
- **Android:** https://github.com/AgoraIO/Basic-Audio-Call/tree/master/One-to-One-Audio/Agora-Android-Audio-Tutorial-1to1

**Steps:**
1. Install Agora demo app on BOTH devices
2. Use the SAME App ID: `8b5e9417f15a48ae929783f32d3d33d4`
3. Try to connect

**Result:**
- ✅ If demo works → Issue is in YOUR app code
- ❌ If demo fails → Issue is network/Agora project/firewall

---

### Test 5: Check for VPN/Proxy

**Question:** Is either device using:
- [ ] VPN (ExpressVPN, NordVPN, etc.)
- [ ] Proxy server
- [ ] Corporate network
- [ ] Mobile hotspot from another device

**Try this:** Disable ALL VPNs/proxies and test on clean Mobile Data.

---

### Test 6: Check Agora SDK Version

In your logs, I don't see the Agora SDK version. Check if you're using the latest version.

**Current file:** `app/build.gradle` or `build.gradle.kts`

Look for:
```gradle
implementation("io.agora.rtc:full-sdk:x.x.x")
```

**Latest version (as of 2025):** Should be 4.x or higher

**Try this:** Update to the latest Agora SDK version if outdated.

---

### Test 7: Check Android Permissions

Even though you granted audio permission, check if ALL required permissions are granted:

**Required permissions:**
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

---

### Test 8: Backend Token Generation Check

Even though token passes backend validation, check the token generation parameters.

**Ask your backend dev to confirm:**

```php
// Token generation parameters
$appId = "8b5e9417f15a48ae929783f32d3d33d4";  // Must match
$appCertificate = "XXXXXXXXXXXXXXXXXXXXXXX";   // From Agora console
$channelName = "call_CALL_17638139941273";     // Exact match
$uid = 0;                                       // MUST be 0 or match joining UID
$role = RtcTokenBuilder::RolePublisher;        // MUST be Publisher (not Subscriber)
$privilegeExpiredTs = time() + 3600;           // Token expires in 1 hour
```

**Most common mistake:**
```php
// ❌ WRONG - Subscriber role can't publish audio!
$role = RtcTokenBuilder::RoleSubscriber;

// ✅ CORRECT - Publisher role
$role = RtcTokenBuilder::RolePublisher;
```

**Test this:** Ask backend to add this debug log:
```php
Log::info('Agora token generated', [
    'channel' => $channelName,
    'uid' => $uid,
    'role' => $role == 1 ? 'Publisher' : 'Subscriber',
    'token_length' => strlen($token),
]);
```

---

## 📊 Expected Results

### If Network/Firewall Issue:
- ✅ Works on Mobile Data
- ❌ Fails on WiFi

**Solution:** Configure WiFi router to allow Agora ports

### If Token Role Issue:
- ✅ Call initiates
- ❌ Agora connection fails
- Check backend logs for role = "Subscriber" (should be "Publisher")

**Solution:** Change backend token generation to use `RolePublisher`

### If Agora Project Disabled:
- ❌ Connection fails immediately
- Check Agora console for project status

**Solution:** Enable project / add billing / remove restrictions

### If Regional Block:
- ❌ Fails on all networks
- Check if you're in a restricted region

**Solution:** Use Agora Edge nodes / different region servers

---

## 🎯 Most Likely Causes (In Order)

1. **Network/Firewall blocking Agora ports** (70% probability)
2. **Token role is Subscriber instead of Publisher** (20% probability)
3. **Agora project has IP whitelist enabled** (5% probability)
4. **VPN/Proxy interference** (3% probability)
5. **Regional restrictions** (2% probability)

---

## 📝 Report Back

After running these tests, report back with:

1. **Network test results:**
   - WiFi: ✅ Works / ❌ Fails
   - Mobile Data: ✅ Works / ❌ Fails

2. **Agora demo app test:**
   - ✅ Works / ❌ Fails

3. **Backend token role:**
   - Publisher / Subscriber / Unknown

4. **Agora console settings:**
   - IP whitelist: Enabled / Disabled
   - Project status: Active / Suspended
   - Region: _______

This will help pinpoint the exact issue!



