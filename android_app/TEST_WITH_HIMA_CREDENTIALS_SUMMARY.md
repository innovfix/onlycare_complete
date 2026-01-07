# 🧪 Test with hima Credentials - Complete Guide

## 🎯 What We're Testing

Using **working "hima" project credentials** in the Only Care app to determine if the Error 110 issue is caused by **Agora Console project settings** or something else.

---

## ✅ Changes Made

### 1. Android App ✅ DONE
**File:** `app/src/main/java/com/onlycare/app/utils/AgoraConfig.kt`

```kotlin
// Changed from Only Care App ID:
// const val APP_ID = "8b5e9417f15a48ae929783f32d3d33d4"

// To hima App ID (working project):
const val APP_ID = "a41e9245489d44a2ac9af9525f1b508c"
```

### 2. Backend ⏳ YOU NEED TO DO THIS

**Follow this guide:** `BACKEND_SWAP_TO_HIMA_CREDENTIALS.md`

**Quick commands:**
```bash
cd /var/www/onlycare_admin
cp .env .env.backup.onlycare

sed -i 's/AGORA_APP_ID=8b5e9417f15a48ae929783f32d3d33d4/AGORA_APP_ID=a41e9245489d44a2ac9af9525f1b508c/' .env
sed -i 's/AGORA_APP_CERTIFICATE=03e9b06b303e47a9b93e71aed9faac63/AGORA_APP_CERTIFICATE=9565a122acba4144926a12214064fd57/' .env

php artisan config:clear
php artisan cache:clear
```

---

## 📋 Testing Steps

### Step 1: Build Updated Android App
```bash
# In Android Studio or via command line
./gradlew clean build
./gradlew installDebug
```

### Step 2: Update Backend
Run the commands above to update backend credentials.

### Step 3: Make a Test Call
1. Open the Only Care app on **both devices**
2. Initiate a call (audio or video)
3. Accept the call on receiver side
4. **Watch for Error 110**

### Step 4: Check Logs

#### Android Logs (Logcat):
```
Look for:
- "JOIN CHANNEL DETAILS" (shows App ID and token)
- "Join channel result code: 0" (success)
- "onJoinChannelSuccess" (GOOD - no Error 110!)
- "onError: 110" (BAD - still failing)
```

#### Backend Logs:
```bash
tail -f /var/www/onlycare_admin/storage/logs/laravel.log
```

Look for:
- "Agora token generated" with new App ID
- Token starting with `007a41e9245489d44a2ac9af9525f1b508c`

---

## 🎯 Expected Results

### ✅ SCENARIO A: Calls Work with hima Credentials

**Meaning:**
- ✅ Your Android code is correct
- ✅ Your backend code is correct
- ❌ **"Only Care" Agora project has restrictions**

**What to Check Next:**
1. Go to Agora Console → "Only Care" project
2. Check **Security Settings** → IP Whitelist (should be disabled)
3. Check **Advanced Settings** → Geo-Fencing (should be disabled)
4. Compare all settings with "hima" project

**Solution:**
- Disable IP Whitelist in Only Care project
- OR use "hima" project permanently
- OR create a new Agora project with correct settings

---

### ❌ SCENARIO B: Calls Still Fail with Error 110

**Meaning:**
- ✅ Both Agora projects are fine
- ❌ **Network/device issue** blocking Agora servers

**What to Check Next:**
1. Test from different WiFi network
2. Test from mobile data (not WiFi)
3. Check if VPN is enabled (disable it)
4. Check corporate firewall settings

**Solution:**
- Test connectivity to Agora servers
- Check firewall rules
- Try different network

---

### ❌ SCENARIO C: Different Error (Not 110)

**Meaning:** New issue discovered.

**What to Do:**
- Share the new error code
- Share complete logs from both devices
- We'll debug the new issue

---

## 📊 Quick Credential Reference

| Project | App ID | Certificate | Status |
|---------|--------|-------------|--------|
| **Only Care** | `8b5e9417f15a48ae929783f32d3d33d4` | `03e9b06b303e47a9b93e71aed9faac63` | ❌ Error 110 |
| **hima** | `a41e9245489d44a2ac9af9525f1b508c` | `9565a122acba4144926a12214064fd57` | ✅ Working |

---

## 🔙 How to Revert Back

**After testing, follow this guide:**
`REVERT_TO_ONLYCARE_CREDENTIALS.md`

**Important:** Revert back so you don't use hima's Agora quota!

---

## 📞 Report Results

After testing, report:

1. **Did calls work?** (YES/NO)
2. **What happened?** (Connected, Error 110, Other error)
3. **Logs from both devices** (if still failing)
4. **Network used** (WiFi, mobile data, VPN?)

---

## 📁 Related Files

- `CREDENTIAL_SWAP_TEST.md` - Test plan overview
- `BACKEND_SWAP_TO_HIMA_CREDENTIALS.md` - Backend update instructions
- `REVERT_TO_ONLYCARE_CREDENTIALS.md` - How to revert changes
- `BACKEND_VERIFY_AGORA_CONFIG.md` - Original diagnostic doc

---

## 🎉 This Test Will Prove Everything!

This test will **definitively answer** whether the issue is:
- Agora Console project settings (most likely!)
- Network/firewall issues
- Code issues (unlikely, since everything checks out)

Good luck with the test! 🚀



