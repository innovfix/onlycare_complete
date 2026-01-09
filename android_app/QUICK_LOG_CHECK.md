# Quick Log Check Guide

## Steps to See Logs

### 1. Rebuild the App
The logging code was just added, so you need to rebuild:

**In Android Studio:**
- Click **Build** → **Clean Project**
- Then **Build** → **Rebuild Project**
- Wait for build to finish
- Run the app again

### 2. Open Logcat in Android Studio

1. At the bottom of Android Studio, click the **Logcat** tab
2. Make sure your device/emulator is selected in the dropdown
3. Select **No Filters** first to see all logs

### 3. Filter by Tag

In the Logcat search box at the top, enter:
```
tag:RateUserScreen
```

You should see logs like:
```
RateUserScreen: ════════════════════════════════════════════════════════════
RateUserScreen: RateUserScreen COMPOSABLE CALLED
RateUserScreen: userId: USR_xxxxx
RateUserScreen: callId: CALL_xxxxx
```

### 4. If No Logs Appear

Try these filters one by one:

**Filter 1 - Just RateUserScreen:**
```
tag:RateUserScreen
```

**Filter 2 - Multiple tags:**
```
tag:RateUserScreen|CallActivity|AudioCallScreen
```

**Filter 3 - Package name:**
```
package:com.onlycare.onlycareapp
```

**Filter 4 - No filter (see everything):**
Clear the search box completely

### 5. Check Log Level

At the top right of Logcat, there's a dropdown that says "Verbose" - make sure it's set to:
- **Verbose** (to see all logs including Debug and Error)

### 6. Alternative: Use adb Command

Open Terminal and run:
```bash
adb logcat | grep -E "RateUserScreen|CallActivity|AudioCallScreen"
```

Or to save to a file:
```bash
adb logcat > call_logs.txt
```

Then search for "RateUserScreen" in the file.

### 7. Test That Logging Works

Add this simple test:
1. Open the rating screen after a call
2. You should immediately see:
   ```
   RateUserScreen: ════════════════════════════════════════════════════════════
   RateUserScreen: RateUserScreen COMPOSABLE CALLED
   ```

If you don't see this, the app might not be rebuilt yet.

## Troubleshooting

### "I don't see any logs at all"

1. **Check device connection:**
   ```bash
   adb devices
   ```
   Should show your device/emulator

2. **Restart adb:**
   ```bash
   adb kill-server
   adb start-server
   ```

3. **Check app is running:**
   - Make sure the app is actually running on the device
   - Check the package name matches: `com.onlycare.onlycareapp`

### "I see other app logs but not RateUserScreen"

1. **Rebuild the app** (most common issue)
2. **Check you're on the rating screen** - logs only appear when that screen is shown
3. **Clear Logcat** and try again (click the trash icon in Logcat)

### "Logcat is overwhelming with too many logs"

Use this filter to only see our logs:
```
package:mine tag:RateUserScreen|CallActivity|AudioCallScreen|AudioCallViewModel|IncomingCallActivity
```

## What You Should See

### When Rating Screen Opens:
```
RateUserScreen: ════════════════════════════════════════════════════════════
RateUserScreen: RateUserScreen COMPOSABLE CALLED
RateUserScreen: userId: USR_17677720014836
RateUserScreen: callId: CALL_17679642226895
RateUserScreen: ════════════════════════════════════════════════════════════
RateUserScreen: ╔════════════════════════════════════════════════════════════
RateUserScreen: ║ 📝 RateUserScreen ENTERED
RateUserScreen: ╚════════════════════════════════════════════════════════════
```

### When New Call Arrives:
```
IncomingCallActivity: ╔════════════════════════════════════════════════════════════
IncomingCallActivity: ║ 🚀 LAUNCHING CallActivity
```

### When You Accept:
```
CallActivity: 🔄 CallActivity.onNewIntent() - NEW CALL WHILE RUNNING!
```

If you're not seeing these logs, **rebuild the app first**!
