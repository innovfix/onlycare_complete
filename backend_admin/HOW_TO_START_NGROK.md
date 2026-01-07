# 🚀 **HOW TO START/RESTART ngrok** (Step-by-Step)

## ⚠️ **ngrok Not Found!**

I checked your system - ngrok is not installed or was deleted.  
Let me guide you through downloading and starting it!

---

## 📥 **OPTION 1: Download & Start ngrok (5 Minutes)**

### **Step 1: Download ngrok**

1. **Open your browser** (Chrome, Edge, etc.)
2. **Go to:** https://ngrok.com/download
3. **Click:** "Windows (64-bit)" download button
4. **Save the ZIP file** to: `C:\ngrok\`

### **Step 2: Extract ngrok**

1. **Go to:** `C:\ngrok\` folder
2. **Right-click** the downloaded ZIP file
3. **Click:** "Extract All..."
4. **Extract to:** `C:\ngrok\`
5. You should now have: `C:\ngrok\ngrok.exe`

### **Step 3: Start ngrok**

**Open PowerShell** and run:

```powershell
cd C:\ngrok
.\ngrok.exe http 8000
```

**You'll see:**
```
Session Status    online
Forwarding        https://abc-def-123.ngrok-free.app -> http://localhost:8000
```

**✅ COPY THE HTTPS URL!** (e.g., `https://abc-def-123.ngrok-free.app`)

### **Step 4: Update Your App**

**Edit this file:**
```
onlycare_app/app/src/main/java/com/onlycare/app/di/NetworkModule.kt
```

**Change line 25:**
```kotlin
// OLD:
private const val BASE_URL = "https://adrienne-pseudosyphilitic-sharlene.ngrok-free.dev/api/v1/"

// NEW:
private const val BASE_URL = "https://YOUR-NEW-URL.ngrok-free.app/api/v1/"
```

**Important:** Replace `YOUR-NEW-URL` with the actual URL from ngrok!

### **Step 5: Rebuild App**
```
1. Stop app (■ Stop button)
2. Build → Clean Project
3. Build → Rebuild Project
4. Run (▶ Run button)
```

### **Step 6: Test**
```
1. Open app
2. Go to Earnings page
3. Tap "Retry"
4. ✅ Should work!
```

---

## ⚡ **OPTION 2: Use Local IP (NO ngrok needed - EASIER!)**

If your phone and PC are on the **same WiFi**, you don't need ngrok!

### **Step 1: Find Your PC's IP**

**In PowerShell, run:**
```powershell
ipconfig | Select-String "IPv4"
```

**You'll see something like:**
```
IPv4 Address. . . . . . . . . . . : 192.168.1.100
```

**Note the IP address** (e.g., `192.168.1.100`)

### **Step 2: Update NetworkModule.kt**

**Change line 25 to:**
```kotlin
private const val BASE_URL = "http://192.168.1.100:8000/api/v1/"
```

**Use YOUR actual IP!**

### **Step 3: Rebuild & Test**
```
1. Stop app
2. Build → Rebuild Project
3. Run app
4. Go to Earnings page
5. ✅ Should work!
```

---

## 🎯 **WHICH OPTION TO CHOOSE?**

| Option | Pros | Cons | Best For |
|--------|------|------|----------|
| **ngrok** | Works anywhere | URL changes on restart | Testing from anywhere |
| **Local IP** | Easier, stable | Only works on same WiFi | Quick local testing |

**Recommendation:** Start with **Local IP** (Option 2) - it's faster!

---

## 📝 **QUICK COMMANDS:**

### **For ngrok (after download):**
```powershell
cd C:\ngrok
.\ngrok.exe http 8000
```

**Keep this terminal window OPEN!**

### **For Local IP:**
```powershell
# Find your IP:
ipconfig | Select-String "IPv4"

# Use that IP in NetworkModule.kt:
# http://YOUR_IP:8000/api/v1/
```

---

## ⚠️ **IMPORTANT REMINDERS:**

1. **Keep terminals open:**
   - MySQL (running in background)
   - Laravel (running in background)
   - ngrok (if using) - **keep visible window OPEN**

2. **ngrok URL changes:**
   - Every time you restart ngrok, you get a NEW URL
   - You must update NetworkModule.kt each time
   - Rebuild the app after updating

3. **Local IP is stable:**
   - Doesn't change (usually)
   - No need to update app unless IP changes
   - Easier for testing!

---

## 🔍 **TROUBLESHOOTING:**

### **Problem: "ngrok not found"**
**Solution:** Download from https://ngrok.com/download

### **Problem: "Cannot connect to localhost:8000"**
**Solution:** Make sure Laravel is running:
```powershell
cd C:\xampp\htdocs\only_care\onlycare_admin
C:\xampp\php\php.exe artisan serve --host=0.0.0.0 --port=8000
```

### **Problem: "Failed to load"**
**Solution:** 
1. Check all 3 services are running
2. Check app's BASE_URL matches ngrok/IP
3. Rebuild app after changing BASE_URL

---

## ✅ **RECOMMENDED: Use Local IP!**

**It's faster and you don't need to download anything!**

Just run:
```powershell
ipconfig | Select-String "IPv4"
```

Update NetworkModule.kt with your IP, rebuild, done! ✅

---

## 🚀 **START NOW!**

Choose one option and follow the steps above!

**Need help?** Check the troubleshooting section!

