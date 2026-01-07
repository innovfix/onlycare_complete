## Backend Firebase FCM setup (OnlyCare-Prod)

Your Android app now uses Firebase project **OnlyCare-Prod** (`project_id: onlycare-prod`).
To make **incoming call push notifications** work, the backend must send FCM using the same project.

### 1) Create & download Firebase service account JSON
Firebase Console → **OnlyCare-Prod** → Project settings (gear) → **Service accounts**

- Click **Generate new private key**
- Download the JSON (keep it private)

### 2) Upload JSON to backend server
Upload the JSON to:

`/var/www/onlycare_admin/storage/app/firebase-credentials.json`

### 3) Set backend env var
In `/var/www/onlycare_admin/.env` add:

`FIREBASE_CREDENTIALS=/var/www/onlycare_admin/storage/app/firebase-credentials.json`

### 4) Install PHP SDK (required)
Backend uses Kreait Firebase SDK. On the server:

```bash
cd /var/www/onlycare_admin
composer update kreait/firebase-php --no-interaction
php artisan config:clear && php artisan cache:clear
```

### 5) Verify push works
- Install the new app build on a phone, login, ensure it sends `fcm_token` to backend.
- Trigger an incoming call to that user (when app is background/killed).
- Check server logs for `FCM: incoming_call push sent`.







