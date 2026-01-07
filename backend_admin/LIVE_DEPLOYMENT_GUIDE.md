# 🚀 Live Server Deployment Guide - Only Care Admin Panel

This guide provides step-by-step instructions for deploying the Only Care Admin Panel to a live server (e.g., Hostinger, GoDaddy, AWS, DigitalOcean).

---

## 📋 Prerequisites

1.  **Domain Name** (e.g., `admin.onlycare.app`)
2.  **Hosting Server** (Shared Hosting, VPS, or Dedicated)
3.  **PHP 8.1 or higher**
4.  **MySQL Database**
5.  **SSH Access** (Recommended) or **cPanel File Manager**

---

## 📦 1. File Preparation (Local Machine)

Before uploading, prepare your project files.

1.  **Zip the Project:**
    Select all files in your project folder (excluding `node_modules`, `vendor`, `.git`, and `storage/framework/cache`) and zip them into `only_care_admin.zip`.

    *Important:* Make sure to include hidden files like `.env.example`.

2.  **Export Database (Optional but Recommended):**
    If you have local data you want to keep, export your local database using phpMyAdmin or command line:
    ```bash
    mysqldump -u root -p only_care_db > only_care_db.sql
    ```
    *If you want a fresh start on live, skip this step.*

---

## ☁️ 2. Upload & Extract (Live Server)

1.  **Upload Zip:**
    Upload `only_care_admin.zip` to your server's public folder (usually `public_html` or a subdomain folder) via File Manager or FTP (FileZilla).

2.  **Extract:**
    Extract the zip file content.

3.  **Set Permissions:**
    Ensure the following folders are writable (775 or 777):
    *   `storage/` (and all subfolders)
    *   `bootstrap/cache/`

---

## 🗄️ 3. Database Setup

1.  **Create Database:**
    Go to your hosting panel (cPanel -> MySQL Databases) and create:
    *   **Database Name:** `only_care_live_db`
    *   **User:** `only_care_user`
    *   **Password:** `StrongPassword123!`
    *   **Privileges:** Grant ALL privileges to the user for this database.

2.  **Import Tables:**
    Open phpMyAdmin, select your new database, and Import the `only_care_db.sql` file (if you exported it).

    *OR*

    **Run Migrations (SSH Recommended):**
    If you have SSH access, run:
    ```bash
    php artisan migrate --force
    ```

---

## ⚙️ 4. Configuration (.env)

1.  **Create .env File:**
    Rename `.env.example` to `.env` (or create a new `.env` file).

2.  **Update Settings:**
    Edit `.env` with your live server details:

    ```ini
    APP_NAME="Only Care Admin"
    APP_ENV=production
    APP_KEY=base64:... (copy from your local .env or generate new)
    APP_DEBUG=false
    APP_URL=https://admin.yourdomain.com

    DB_CONNECTION=mysql
    DB_HOST=127.0.0.1
    DB_PORT=3306
    DB_DATABASE=only_care_live_db  <-- Your Live DB Name
    DB_USERNAME=only_care_user     <-- Your Live DB User
    DB_PASSWORD=StrongPassword123! <-- Your Live DB Password

    BROADCAST_DRIVER=log
    CACHE_DRIVER=file
    FILESYSTEM_DISK=public
    QUEUE_CONNECTION=sync
    SESSION_DRIVER=file
    SESSION_LIFETIME=120
    ```

3.  **Generate Key (if needed):**
    If you didn't copy the key, run via SSH:
    ```bash
    php artisan key:generate
    ```

---

## 🔗 5. Web Server Configuration (Important)

**For Apache (cPanel/Shared Hosting):**
Ensure there is an `.htaccess` file in your root folder with this content to point requests to the `public` folder:

```apache
<IfModule mod_rewrite.c>
    RewriteEngine On
    RewriteRule ^(.*)$ public/$1 [L]
</IfModule>
```

**OR (Better Method):**
Point your domain's "Document Root" directly to the `public` folder in your hosting settings.

---

## 🧹 6. Final Steps

1.  **Install Dependencies (SSH):**
    ```bash
    composer install --optimize-autoloader --no-dev
    ```

2.  **Link Storage:**
    ```bash
    php artisan storage:link
    ```
    *(If no SSH: Create a symlink from `public/storage` to `storage/app/public` manually via PHP script)*

3.  **Clear Caches:**
    ```bash
    php artisan config:cache
    php artisan route:cache
    php artisan view:cache
    ```

---

## 🛡️ Required Database Columns Checklist

Ensure your live database has these critical columns in the `users` table for the app to work:

| Column Name | Type | Description |
| :--- | :--- | :--- |
| `referral_code` | VARCHAR(20) | Unique referral code |
| `country_code` | VARCHAR(5) | e.g., +91 |
| `user_type` | VARCHAR(10) | MALE / FEMALE |
| `is_active` | BOOLEAN | 1 = Active, 0 = Inactive |
| `username` | VARCHAR(10) | Unique username |

If any are missing, run:
```sql
ALTER TABLE users ADD COLUMN referral_code VARCHAR(20) UNIQUE AFTER phone;
ALTER TABLE users ADD COLUMN country_code VARCHAR(5) DEFAULT '+91' AFTER phone;
ALTER TABLE users ADD COLUMN user_type VARCHAR(10) DEFAULT 'MALE' AFTER gender;
ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT 1 AFTER is_blocked;
ALTER TABLE users ADD COLUMN username VARCHAR(10) UNIQUE AFTER name;
```

---

## 🆘 Troubleshooting

*   **500 Server Error:** Check `storage/logs/laravel.log` and permissions.
*   **404 Not Found:** Ensure `.htaccess` is correct and `mod_rewrite` is enabled.
*   **Database Error:** Check `.env` credentials and ensure user has privileges.
