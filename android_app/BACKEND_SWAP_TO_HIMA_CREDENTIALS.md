# 🔄 Backend: Swap to hima Credentials (TEST)

## ⚠️ This is a TEMPORARY TEST

We're testing if the issue is with "Only Care's" Agora project settings by using the working "hima" project credentials.

---

## 📋 Step-by-Step Instructions

### Step 1: Backup Current Credentials

```bash
# SSH to your server
cd /var/www/onlycare_admin

# Backup current .env
cp .env .env.backup.onlycare

echo "✅ Backup created: .env.backup.onlycare"
```

---

### Step 2: Update Agora Credentials

```bash
# Update App ID
sed -i 's/AGORA_APP_ID=8b5e9417f15a48ae929783f32d3d33d4/AGORA_APP_ID=a41e9245489d44a2ac9af9525f1b508c/' .env

# Update App Certificate
sed -i 's/AGORA_APP_CERTIFICATE=03e9b06b303e47a9b93e71aed9faac63/AGORA_APP_CERTIFICATE=9565a122acba4144926a12214064fd57/' .env

echo "✅ Credentials updated to hima project"
```

---

### Step 3: Clear Laravel Cache

```bash
php artisan config:clear
php artisan cache:clear

echo "✅ Cache cleared"
```

---

### Step 4: Verify Changes

```bash
# Check the .env file
grep "AGORA_APP_ID" .env
grep "AGORA_APP_CERTIFICATE" .env
```

**Expected output:**
```
AGORA_APP_ID=a41e9245489d44a2ac9af9525f1b508c
AGORA_APP_CERTIFICATE=9565a122acba4144926a12214064fd57
```

---

### Step 5: Test Token Generation

```bash
# Test if backend can generate tokens with new credentials
php /var/www/onlycare_admin/test_token_generation.php
```

**Expected:** Token should start with `007a41e9245489d44a2ac9af9525f1b508c`

---

## ✅ Test Complete - Now Try a Call!

1. **Build and install** the updated Android app
2. **Make a test call**
3. **Check if Error 110 is gone**

---

## 🔙 How to Revert Back to Only Care Credentials

### Option A: Quick Revert

```bash
cd /var/www/onlycare_admin

# Restore from backup
cp .env.backup.onlycare .env

# Clear cache
php artisan config:clear
php artisan cache:clear

echo "✅ Reverted to Only Care credentials"
```

---

### Option B: Manual Revert

```bash
# Update back to Only Care credentials
sed -i 's/AGORA_APP_ID=a41e9245489d44a2ac9af9525f1b508c/AGORA_APP_ID=8b5e9417f15a48ae929783f32d3d33d4/' .env
sed -i 's/AGORA_APP_CERTIFICATE=9565a122acba4144926a12214064fd57/AGORA_APP_CERTIFICATE=03e9b06b303e47a9b93e71aed9faac63/' .env

php artisan config:clear
php artisan cache:clear
```

---

## 📊 Quick Reference

| Project | App ID | Certificate |
|---------|--------|-------------|
| **Only Care** (Original) | `8b5e9417f15a48ae929783f32d3d33d4` | `03e9b06b303e47a9b93e71aed9faac63` |
| **hima** (Testing) | `a41e9245489d44a2ac9af9525f1b508c` | `9565a122acba4144926a12214064fd57` |

---

## 🎯 What This Test Proves

### If Calls Work with hima Credentials:
- ✅ Your code is CORRECT
- ❌ "Only Care" Agora project has restrictions (IP whitelist, geo-fence, etc.)
- **Action:** Compare Agora Console settings between projects

### If Calls Still Fail with hima Credentials:
- ✅ Both Agora projects are fine
- ❌ Network/firewall issue on device/server
- **Action:** Check network connectivity to Agora servers

---

## ⚠️ Remember to Revert!

After testing, **revert back to Only Care credentials** so you don't accidentally use hima's quota!



