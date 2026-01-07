# 🌟 OnlyCare - Complete Monorepo

This repository contains **ALL** OnlyCare project files:
- 📱 **Android App** (Kotlin + Jetpack Compose)
- 🖥️ **Backend Admin Panel** (Laravel PHP)

---

## 📦 Repository Structure

```
only_care_monorepo/
├── android_app/          # Android mobile application
│   ├── app/              # Main app module
│   ├── gradle/           # Gradle wrapper
│   └── build.gradle.kts  # Project build config
│
└── backend_admin/        # Laravel admin panel
    ├── app/              # Laravel application code
    ├── routes/           # API & web routes
    ├── resources/        # Views, assets
    ├── database/         # Migrations, seeders
    └── public/           # Public assets
```

---

## 📱 Android App

**Location:** `android_app/`

**Version:** 3.2.2 (versionCode: 13)

**Tech Stack:**
- Kotlin
- Jetpack Compose
- Agora RTC SDK
- Retrofit + OkHttp
- Room Database
- Hilt DI

**Key Features:**
- Video/Audio calling with Agora
- Real-time notifications (FCM)
- Coin-based payment system
- KYC verification
- Profile management

**Build APK/AAB:**
```bash
cd android_app
./gradlew assembleRelease  # APK
./gradlew bundleRelease    # AAB for Play Store
```

**Output Files:**
- APKs: `android_app/app/build/outputs/apk/release/`
- AAB: `android_app/app/build/outputs/bundle/release/app-release.aab`

---

## 🖥️ Backend Admin Panel

**Location:** `backend_admin/`

**Tech Stack:**
- Laravel 11
- MySQL Database
- Firebase Admin SDK (FCM)
- RESTful API

**Key Features:**
- User management
- Call history & analytics
- Transaction monitoring
- Withdrawal approvals
- KYC verification
- Policy page management
- Coin package management

**Setup:**
```bash
cd backend_admin
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate
php artisan serve
```

**Public Policy Pages:**
- Privacy Policy: `https://onlycare.in/privacy-policy`
- Terms & Conditions: `https://onlycare.in/terms-conditions`
- Community Guidelines: `https://onlycare.in/community-guidelines`
- Refund Policy: `https://onlycare.in/refund-policy`

---

## 🌐 Live Deployment

**Production Server:** `64.227.163.211`  
**Domain:** `https://onlycare.in`  
**API Base:** `https://onlycare.in/api/v1/`

---

## 🔐 Environment Setup

### Android App
1. Add `keystore.properties` in `android_app/` with signing credentials
2. Place `google-services.json` in `android_app/app/`

### Backend Admin
1. Copy `.env.example` to `.env`
2. Configure database credentials
3. Add Firebase service account JSON
4. Set up mail and payment gateway configs

---

## 📝 Recent Updates (v3.2.2)

### Android App
✅ Fixed policy pages crash (Privacy, Terms, Community Guidelines, Refund)  
✅ Removed `READ_MEDIA_IMAGES` permission for Play Store compliance  
✅ Made all policy DTOs nullable with safe fallbacks  
✅ Added `HtmlContentView` component for rendering HTML content  
✅ Fixed Community Guidelines API endpoint call  

### Backend Admin
✅ Created `PublicPageController` with crash-safe fallbacks  
✅ Added public policy routes to `web.php`  
✅ Created beautiful responsive policy page template  
✅ Deployed all files to live server  
✅ Verified all URLs return 200 OK  

---

## 🚀 Quick Start

### Clone Repository
```bash
git clone https://github.com/innovfix/only_care_monorepo.git
cd only_care_monorepo
```

### Build Android App
```bash
cd android_app
./gradlew assembleRelease
```

### Run Backend Locally
```bash
cd backend_admin
composer install
php artisan serve
```

---

## 📞 Support

**Email:** onlycareapp000@gmail.com  
**Developer:** Innovfix Team

---

## 📄 License

Proprietary - All Rights Reserved © 2026 OnlyCare

---

**Last Updated:** January 7, 2026  
**Repository Version:** 1.0.0

