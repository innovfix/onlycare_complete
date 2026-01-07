# 🚀 MANUAL SETUP GUIDE - Only Care Admin Panel

## ⚠️ IMPORTANT: Run These Commands One by One

### Step 1: Open Terminal
```bash
cd /Applications/XAMPP/xamppfiles/htdocs/only_care_admin
```

---

## 📦 **INSTALLATION STEPS**

### Step 2: Install Composer (if not installed)
```bash
# Download Composer
curl -sS https://getcomposer.org/installer | php

# Move to global location
sudo mv composer.phar /usr/local/bin/composer

# Verify installation
composer --version
```

### Step 3: Install Node.js/npm (if not installed)
Download from: https://nodejs.org/

Or using Homebrew:
```bash
brew install node
```

### Step 4: Install PHP Dependencies
```bash
composer install
```

### Step 5: Install Node Dependencies
```bash
npm install
```

### Step 6: Generate Application Key
```bash
php artisan key:generate
```

### Step 7: Create Database
**Option A: Using phpMyAdmin**
1. Open XAMPP
2. Start MySQL
3. Open http://localhost/phpmyadmin
4. Create new database: `only_care_db`

**Option B: Using Terminal**
```bash
/Applications/XAMPP/xamppfiles/bin/mysql -u root -p
```
Then run:
```sql
CREATE DATABASE only_care_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### Step 8: Run Migrations
```bash
php artisan migrate
```

### Step 9: Seed Database
```bash
# Create admin user
php artisan db:seed --class=AdminSeeder

# Create sample data
php artisan db:seed --class=SampleDataSeeder
```

### Step 10: Build Assets
```bash
npm run build
```

### Step 11: Clear Caches
```bash
php artisan cache:clear
php artisan config:clear
php artisan route:clear
php artisan view:clear
```

### Step 12: Start Server
```bash
php artisan serve
```

---

## ✅ **VERIFICATION**

After setup, you should see:
```
Starting Laravel development server: http://127.0.0.1:8000
```

**Then open browser and go to:**
```
http://localhost:8000/login
```

**Login with:**
- Email: `admin@onlycare.app`
- Password: `admin123`

---

## 📊 **WHAT WAS CREATED**

### Database Tables (15)
✅ users, calls, coin_packages, transactions, withdrawals, bank_accounts, kyc_documents, messages, friendships, referrals, reports, notifications, app_settings, blocked_users, admins

### Sample Data
✅ 30 Users (20 male, 10 female)
✅ 50 Call records
✅ Multiple transactions
✅ Withdrawal requests (pending & approved)
✅ KYC documents (pending review)
✅ User reports
✅ 4 Coin packages
✅ App settings

---

## 🐛 **TROUBLESHOOTING**

### Problem: "php: command not found"
```bash
# Add XAMPP PHP to PATH
export PATH="/Applications/XAMPP/xamppfiles/bin:$PATH"

# Or use full path
/Applications/XAMPP/xamppfiles/bin/php artisan serve
```

### Problem: "composer: command not found"
Install Composer first (see Step 2 above)

### Problem: "npm: command not found"
Install Node.js first (see Step 3 above)

### Problem: Database connection error
Check `.env` file:
```
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=only_care_db
DB_USERNAME=root
DB_PASSWORD=
```

### Problem: "Class AdminSeeder not found"
```bash
composer dump-autoload
```

---

## 🎯 **READY TO TEST**

Once server is running:
1. ✅ Open http://localhost:8000
2. ✅ Login with credentials above
3. ✅ Explore the dashboard
4. ✅ Test all features using TESTING_CHECKLIST.md

---

**Need Help? Check:**
- `storage/logs/laravel.log` for errors
- Run `php artisan route:list` to see all routes
- Run `php artisan tinker` for database testing

**Happy Testing! 🚀**







