# 📞 How to Fix Incoming Calls Not Showing Full Screen

## The Problem
You're seeing a small notification instead of a full-screen incoming call screen when the app is closed.

## Quick Fix - Enable Full-Screen Notifications

### For Android 14 and Above (Most Important!)

#### Option 1: Through App Settings
1. Open your phone's **Settings**
2. Go to **Apps** > **Only Care** (or tap "App info" from notification)
3. Tap **Notifications**
4. Scroll down and find **"Full screen intent"** or **"Display pop-up while in use"**
5. **Toggle it ON** ✅

#### Option 2: Through Notification Settings
1. When you receive the incoming call notification, **long-press** on it
2. Tap the **Settings icon** ⚙️
3. Find and enable **"Full screen intent"**

### For All Android Versions - Disable Battery Optimization

This ensures you receive calls even when the app is completely closed:

1. Open your phone's **Settings**
2. Go to **Apps** > **Only Care**
3. Tap **Battery**
4. Select **"Unrestricted"** or **"Don't optimize"**
5. Confirm

## Manufacturer-Specific Settings

Different phone manufacturers have additional restrictions. Here's how to fix them:

### Xiaomi / Redmi / POCO
1. **Settings** > **Apps** > **Manage Apps** > **Only Care**
2. Enable **Autostart** ✅
3. Go to **Battery saver** > Select **"No restrictions"**
4. Go to **Other permissions** > Enable **"Display pop-up windows"**

### Samsung
1. **Settings** > **Apps** > **Only Care** > **Battery**
2. Enable **"Allow background activity"**
3. **Settings** > **Device care** > **Battery** > **Background usage limits**
4. Add **Only Care** to **"Never sleeping apps"**

### Huawei / Honor
1. **Settings** > **Apps** > **Apps** > **Only Care**
2. Tap **Launch** > Select **"Manage manually"**
3. Enable all three options (Auto-launch, Secondary launch, Run in background)
4. **Settings** > **Battery** > **App launch** > **Only Care** > Manage manually

### OnePlus / Oppo / Realme
1. **Settings** > **Apps** > **Only Care**
2. **Battery** > **Battery optimization** > **Don't optimize**
3. **Advanced** > **Background activity** > Select **"Unrestricted"**

### Vivo
1. **Settings** > **Battery** > **Background power consumption management**
2. Find **Only Care** > Select **"Allow high background power consumption"**
3. **Settings** > **More Settings** > **Applications** > **Autostart**
4. Enable for **Only Care**

## Alternative: Use Notification Buttons

Even without full-screen permission, you can still answer calls:

1. When you receive the notification, you'll see two buttons:
   - ❌ **Reject** - Decline the call
   - ✅ **Answer** - Accept the call

2. Tap **Answer** or tap the notification itself to open the full call screen

## How to Test If It's Working

### Test 1: App Closed
1. **Close Only Care completely** (swipe it away from recent apps)
2. Wait 10 seconds
3. Ask someone to **call you from another device**
4. **Expected Result:**
   - ✅ If permission is granted: Full-screen incoming call appears
   - ⚠️ If permission not granted: Notification with Answer/Reject buttons appears

### Test 2: Screen Locked
1. **Lock your phone**
2. Ask someone to **call you**
3. **Expected Result:**
   - Screen should turn on
   - Full-screen call appears over lock screen

### Test 3: Do Not Disturb Mode
1. Enable **Do Not Disturb** mode
2. Ask someone to **call you**
3. **Expected Result:**
   - Call notification should still appear (it bypasses DND)

## Still Not Working?

If you've enabled all permissions and it's still not working, try these steps:

### Step 1: Restart Your Phone
Sometimes settings don't take effect until you restart.

### Step 2: Reinstall the App
1. Uninstall Only Care
2. Reinstall from Play Store
3. Grant all permissions again

### Step 3: Check Do Not Disturb Settings
1. **Settings** > **Sound & vibration** > **Do Not Disturb**
2. Tap **Apps** or **Exceptions**
3. Make sure **Only Care** is allowed

### Step 4: Clear App Cache
1. **Settings** > **Apps** > **Only Care**
2. Tap **Storage**
3. Tap **Clear cache** (not Clear data!)
4. Restart the app

### Step 5: Check Notification Channel Settings
1. **Settings** > **Apps** > **Only Care** > **Notifications**
2. Tap **"Incoming Calls"** channel
3. Make sure:
   - Importance is set to **"Urgent"** or **"High"**
   - Notification sound is enabled
   - Pop on screen is enabled
   - Show badge is enabled

## Understanding the Notification

When you receive a call notification, here's what you'll see:

```
┌─────────────────────────────────────┐
│  📞 John Doe                        │
│  Incoming video call                │
│                                     │
│  [❌ Reject]  [✅ Answer]           │
└─────────────────────────────────────┘
```

- **Tap the notification**: Opens full-screen call UI
- **Tap Reject**: Declines the call
- **Tap Answer**: Accepts the call and opens video call screen

## Why This Happens

Starting from **Android 12**, Google requires apps to explicitly request permission for full-screen notifications (called "Full screen intent"). This is for:
- **User privacy**: Prevents apps from taking over your screen without permission
- **Battery saving**: Reduces unnecessary wake-ups
- **Security**: Ensures only trusted apps can show full-screen content

Our app requests this permission, but **you need to manually enable it** in settings.

## Quick Checklist

Use this checklist to ensure everything is set up correctly:

- [ ] Full-screen intent permission enabled (Android 14+)
- [ ] Battery optimization disabled
- [ ] Autostart enabled (if on Xiaomi/Oppo/Vivo/etc.)
- [ ] Background activity allowed
- [ ] Notification permission granted
- [ ] App has internet access
- [ ] You're logged in to the app
- [ ] Your phone is connected to internet (WiFi or mobile data)

## Need More Help?

If you're still having issues after following all these steps:

1. **Check app logs**: Open the app and look for any error messages
2. **Test with app open**: Does it work when app is open? If not, there might be another issue
3. **Contact support**: Provide your:
   - Phone model and Android version
   - Whether app is open/closed when testing
   - Screenshot of notification settings
   - Any error messages you see

---

**TL;DR:**
1. Settings > Apps > Only Care > Notifications > Enable "Full screen intent"
2. Settings > Apps > Only Care > Battery > Select "Unrestricted"
3. If your phone is Xiaomi/Oppo/Vivo/etc., enable Autostart
4. Test by closing app and asking someone to call you

That's it! 🎉



