## Deploying your local changes to `onlycare.in` (Admin Panel)

Editing files in `C:\Only care\onlycare_admin` **will not change** the live website until you deploy those changes to the server.

### What I need from you (pick one)

- **Option A (SSH)**: server **host**, **username**, **port** (usually 22), and the **path** where the Laravel project is deployed (example: `/var/www/onlycare_admin`).
- **Option B (cPanel/FTP)**: confirm you deploy by uploading files (no SSH). I’ll tell you exactly which folders to upload + how to clear caches.

---

## Option A — SSH deploy (recommended)

### 1) Run the Windows deploy helper

From `C:\Only care\onlycare_admin`:

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy_windows.ps1 `
  -Host "onlycare.in" `
  -User "root" `
  -Path "/var/www/onlycare_admin" `
  -Branch "main"
```

If you use a different port:

```powershell
powershell -ExecutionPolicy Bypass -File .\deploy_windows.ps1 `
  -Host "onlycare.in" `
  -User "root" `
  -Port 2222 `
  -Path "/var/www/onlycare_admin"
```

### 2) Hard refresh

Open the admin in browser and do **Ctrl + F5**.

---

## Option B — cPanel / FTP (no SSH)

### Upload

Upload the changed files/folders to the server (same paths). Common ones:
- `resources/views/...` (Blade UI changes)
- `app/Http/Controllers/...` (backend logic changes)
- `routes/...` (route changes)

### Clear caches (critical)

If you can’t run Artisan, you must clear these:
- delete all files in `storage/framework/views/`
- delete all files in `bootstrap/cache/` (keep the folders)

Then hard refresh the browser.

---

## Quick verification

- Admin login page should load: `https://onlycare.in/login`
- API test endpoint: `https://onlycare.in/api/v1/test-connection`



