# 🧪 Credential Swap Test: hima (Working) → Only Care

## Purpose

Test if the issue is with **Only Care's Agora project settings** by temporarily using credentials from the **working "hima" project**.

---

## 📋 Original Credentials (Only Care)

**Save these to revert back later:**

```
App ID: 8b5e9417f15a48ae929783f32d3d33d4
Primary Certificate: 03e9b06b303e47a9b93e71aed9faac63
Secondary Certificate: 8b5a6bae5d87426b920a2758d2e398eb
```

---

## 🔄 Test Credentials (hima - WORKING)

```
App ID: a41e9245489d44a2ac9af9525f1b508c
Primary Certificate: 9565a122acba4144926a12214064fd57
```

---

## ✅ What We Changed

### 1. Android App
- **File:** `app/src/main/java/com/onlycare/app/utils/AgoraConfig.kt`
- **Changed:** `APP_ID` from Only Care to hima

### 2. Backend (You Need to Do This)
- **File:** `/var/www/onlycare_admin/.env`
- **Change:**
  ```env
  AGORA_APP_ID=a41e9245489d44a2ac9af9525f1b508c
  AGORA_APP_CERTIFICATE=9565a122acba4144926a12214064fd57
  ```
- **Then run:**
  ```bash
  php artisan config:clear
  ```

---

## 🎯 Expected Results

### ✅ If hima Credentials WORK:

**Meaning:** The issue is with **Only Care's Agora project settings** (IP whitelist, geo-fence, etc.)

**Action:** Compare Agora Console settings between "hima" and "Only Care" projects.

---

### ❌ If hima Credentials ALSO Fail:

**Meaning:** Issue is **network/device-specific**, not Agora project settings.

**Action:** Check firewall, VPN, or device network restrictions.

---

## 🔙 How to Revert Back

### Android App:
Revert `AgoraConfig.kt` to original App ID: `8b5e9417f15a48ae929783f32d3d33d4`

### Backend:
```bash
# Revert .env
sed -i 's/AGORA_APP_ID=a41e9245489d44a2ac9af9525f1b508c/AGORA_APP_ID=8b5e9417f15a48ae929783f32d3d33d4/' /var/www/onlycare_admin/.env
sed -i 's/AGORA_APP_CERTIFICATE=9565a122acba4144926a12214064fd57/AGORA_APP_CERTIFICATE=03e9b06b303e47a9b93e71aed9faac63/' /var/www/onlycare_admin/.env
php artisan config:clear
```

---

## 📊 Test on: November 22, 2025



