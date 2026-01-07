# 🔙 Revert to Only Care Credentials

## ⚠️ IMPORTANT: Run This After Testing with hima Credentials

After testing with "hima" credentials, revert back to "Only Care" credentials.

---

## 📋 Original Only Care Credentials

```
App ID: 8b5e9417f15a48ae929783f32d3d33d4
Primary Certificate: 03e9b06b303e47a9b93e71aed9faac63
Secondary Certificate: 8b5a6bae5d87426b920a2758d2e398eb
```

---

## 🔧 Revert Instructions

### 1. Android App

**File:** `/Users/bala/Desktop/App Projects/onlycare_app/app/src/main/java/com/onlycare/app/utils/AgoraConfig.kt`

Change:
```kotlin
const val APP_ID = "a41e9245489d44a2ac9af9525f1b508c"  // hima (TESTING)
```

Back to:
```kotlin
const val APP_ID = "8b5e9417f15a48ae929783f32d3d33d4"
```

---

### 2. Backend

```bash
# SSH to server
cd /var/www/onlycare_admin

# Restore from backup
cp .env.backup.onlycare .env

# OR manually update
sed -i 's/AGORA_APP_ID=a41e9245489d44a2ac9af9525f1b508c/AGORA_APP_ID=8b5e9417f15a48ae929783f32d3d33d4/' .env
sed -i 's/AGORA_APP_CERTIFICATE=9565a122acba4144926a12214064fd57/AGORA_APP_CERTIFICATE=03e9b06b303e47a9b93e71aed9faac63/' .env

# Clear cache
php artisan config:clear
php artisan cache:clear
```

---

## ✅ Verify Revert

### Android:
Check `AgoraConfig.kt` shows: `8b5e9417f15a48ae929783f32d3d33d4`

### Backend:
```bash
grep "AGORA_APP_ID" /var/www/onlycare_admin/.env
```
Should show: `AGORA_APP_ID=8b5e9417f15a48ae929783f32d3d33d4`

---

## 📝 After Reverting

If hima credentials **worked** and Only Care credentials **don't work**, then the issue is confirmed to be in the **Only Care Agora Console project settings**.

Next step: Compare Agora Console settings between "hima" and "Only Care" projects.



