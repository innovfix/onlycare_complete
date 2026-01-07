# ⚡ Backend Team - Quick Action Items

## 🔥 3 CRITICAL TASKS (Must Complete Today!)

---

## ✅ Task 1: Provide `google-services.json` to Mobile Team

### Steps:
1. Go to: https://console.firebase.google.com
2. Select your OnlyCare project
3. Click ⚙️ → **Project Settings** → **General**
4. Scroll to **"Your apps"** section
5. Click Android app (or add new)
   - Package name: `com.onlycare.app`
6. Click **"Download google-services.json"**
7. **SEND THIS FILE TO MOBILE TEAM**

**⏰ Priority:** URGENT - Mobile team cannot build app without this!

---

## ✅ Task 2: Run These Commands on Server

Copy and paste these commands one by one:

```bash
# 1. Navigate to project
cd /var/www/onlycare_admin

# 2. Run database migration (adds fcm_token column)
php artisan migrate

# 3. Install Firebase Admin SDK
composer require kreait/firebase-php

# 4. Clear cache
php artisan config:clear
php artisan cache:clear
```

**Expected Output:**
```
Migration table created successfully.
Migrating: 2025_11_22_000001_add_fcm_token_to_users_table
Migrated:  2025_11_22_000001_add_fcm_token_to_users_table

Installing kreait/firebase-php (7.x.x)
```

---

## ✅ Task 3: Upload Firebase Service Account Key

### Step 3.1: Download Service Account Key

1. Go to: https://console.firebase.google.com
2. Select your project
3. Click ⚙️ → **Project Settings** → **Service Accounts**
4. Click **"Generate New Private Key"**
5. Click **"Generate Key"** (confirm)
6. Save the downloaded JSON file

### Step 3.2: Upload to Server

**Option A: Using SCP (from your local machine)**
```bash
scp /path/to/downloaded-key.json root@your-server:/var/www/onlycare_admin/storage/app/firebase-credentials.json
```

**Option B: Create file directly on server**
```bash
nano /var/www/onlycare_admin/storage/app/firebase-credentials.json
# Paste the entire JSON content
# Press Ctrl+X, then Y, then Enter to save
```

### Step 3.3: Set Permissions
```bash
chmod 600 /var/www/onlycare_admin/storage/app/firebase-credentials.json
chown www-data:www-data /var/www/onlycare_admin/storage/app/firebase-credentials.json
```

### Step 3.4: Update .env File
```bash
echo "" >> /var/www/onlycare_admin/.env
echo "# Firebase Configuration" >> /var/www/onlycare_admin/.env
echo "FIREBASE_CREDENTIALS=/var/www/onlycare_admin/storage/app/firebase-credentials.json" >> /var/www/onlycare_admin/.env

# Clear cache
php artisan config:clear
```

---

## 🧪 Verification Commands

### 1. Verify Migration Ran Successfully
```bash
mysql -u root -p -e "USE onlycare_db; DESCRIBE users;" | grep fcm_token
```
**Expected:** You should see `fcm_token` column listed

### 2. Verify Firebase Package Installed
```bash
php artisan tinker
```
Then type:
```php
$firebase = (new \Kreait\Firebase\Factory)->withServiceAccount(config('firebase.credentials'));
echo "✅ Firebase initialized successfully!";
exit
```

### 3. Verify Configuration
```bash
grep FIREBASE /var/www/onlycare_admin/.env
```
**Expected Output:**
```
FIREBASE_CREDENTIALS=/var/www/onlycare_admin/storage/app/firebase-credentials.json
```

### 4. Check File Exists
```bash
ls -la /var/www/onlycare_admin/storage/app/firebase-credentials.json
```
**Expected:** File should exist with 600 permissions

---

## 📋 What Was Already Implemented (No Action Needed)

✅ Database migration file created  
✅ User model updated with fcm_token field  
✅ Firebase config file created  
✅ API route added: `POST /api/v1/users/update-fcm-token`  
✅ UserController method implemented  
✅ CallController FCM notification logic fully implemented  
✅ Error handling and logging added  

**You just need to run the commands above!**

---

## 🔍 New API Endpoint (Automatically Available)

After completing the tasks above, this endpoint will work:

**Endpoint:** `POST /api/v1/users/update-fcm-token`

**Headers:**
```
Authorization: Bearer {user_auth_token}
Content-Type: application/json
```

**Request:**
```json
{
  "fcm_token": "dXJ5dmVyc2lvbjphcHA6MTE6MzI4OTY4..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "FCM token updated successfully"
}
```

**Usage:** Mobile app will call this when it starts or FCM token refreshes.

---

## 📞 How FCM Notifications Work Now

### Automatic Flow:
```
1. User A calls User B
   ↓
2. Mobile app: POST /api/v1/calls/initiate
   ↓
3. Backend automatically:
   ✓ Creates call
   ✓ Generates Agora token
   ✓ Sends FCM push notification to User B
   ↓
4. User B's device receives notification
   ↓
5. Full-screen incoming call appears!
```

**No changes needed to existing `/calls/initiate` endpoint!**  
It now automatically sends push notifications.

---

## ⏰ Timeline

### Today (Must Complete):
- [ ] Download and send `google-services.json` to mobile team
- [ ] Run migration command
- [ ] Install Firebase package
- [ ] Upload Firebase service account key
- [ ] Update .env file
- [ ] Run verification commands

**Estimated Time:** 15-20 minutes

### Tomorrow:
- [ ] Test with mobile team
- [ ] Verify notifications work on real devices

---

## ❓ Need Help?

### If migration fails:
```bash
php artisan migrate:status
php artisan migrate --force
```

### If composer install fails:
```bash
composer clear-cache
composer update
composer require kreait/firebase-php
```

### If Firebase credentials don't work:
1. Re-download from Firebase Console
2. Check file path in .env matches actual location
3. Check file permissions (should be 600)

### Check logs:
```bash
tail -50 /var/www/onlycare_admin/storage/logs/laravel.log
```

---

## 📚 Full Documentation

For detailed information, see:
- `FCM_INCOMING_CALLS_SETUP_GUIDE.md` - Complete setup guide
- `BACKEND_TEAM_REQUIREMENTS.md` - Requirements document (in your message)

---

## ✅ Completion Checklist

Once all tasks are done, verify with mobile team:

- [ ] Mobile team has `google-services.json`
- [ ] Migration ran successfully (fcm_token column exists)
- [ ] Firebase package installed
- [ ] Service account key uploaded and configured
- [ ] Verification commands passed
- [ ] Mobile team can test `/update-fcm-token` endpoint
- [ ] End-to-end test: Make a call and verify push notification works

---

**Status:** Ready to Execute! 🚀  
**Contact:** Share this document with your DevOps/Backend team







