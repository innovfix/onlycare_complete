# 🚨 URGENT: Firebase Credentials File Missing

## Problem
FCM notifications are **FAILING** because the Firebase credentials file is missing.

**Error:**
```
Firebase credentials file not found: /var/www/onlycare_admin/storage/app/firebase-credentials.json
```

---

## ✅ Solution: Upload Firebase Credentials

### Step 1: Download Firebase Service Account Key

1. Go to: **https://console.firebase.google.com**
2. Select your project: **OnlyCare-Prod** (or your project name)
3. Click ⚙️ **Settings** → **Project Settings**
4. Go to **Service Accounts** tab
5. Click **"Generate New Private Key"**
6. Click **"Generate Key"** in the confirmation dialog
7. A JSON file will download (e.g., `onlycare-prod-firebase-adminsdk-xxxxx.json`)

### Step 2: Upload to Server

**Option A: Using SCP (from your local machine)**
```bash
scp /path/to/downloaded-key.json root@64.227.163.211:/var/www/onlycare_admin/storage/app/firebase-credentials.json
```

**Option B: Create file directly on server**
```bash
ssh root@64.227.163.211
cd /var/www/onlycare_admin
nano storage/app/firebase-credentials.json
# Paste the entire JSON content from downloaded file
# Press Ctrl+X, then Y, then Enter to save
```

### Step 3: Set Correct Permissions

```bash
ssh root@64.227.163.211
cd /var/www/onlycare_admin
chmod 600 storage/app/firebase-credentials.json
chown www-data:www-data storage/app/firebase-credentials.json
```

### Step 4: Verify

```bash
ssh root@64.227.163.211
cd /var/www/onlycare_admin
ls -lah storage/app/firebase-credentials.json
# Should show: -rw------- 1 www-data www-data [size] firebase-credentials.json
```

### Step 5: Clear Cache

```bash
ssh root@64.227.163.211
cd /var/www/onlycare_admin
php artisan config:clear
php artisan cache:clear
```

---

## ✅ After Upload - Test FCM

Once the file is uploaded, test by making a call. You should see in logs:

```
[fcm_check] FCM notification SENT successfully to female
```

Instead of:

```
[fcm_check] FCM notification FAILED - Firebase credentials file missing
```

---

## 📍 File Location

**Required Path:** `/var/www/onlycare_admin/storage/app/firebase-credentials.json`

**Current Status:** ❌ File does not exist

---

**Created:** January 9, 2026
**Priority:** 🔥 URGENT - FCM notifications not working without this file
