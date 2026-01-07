# 🧪 START TESTING - Quick Setup Guide

## Step-by-Step Installation (5 Minutes)

### ✅ **Step 1: Open Terminal**
```bash
cd /Applications/XAMPP/xamppfiles/htdocs/only_care_admin
```

### ✅ **Step 2: Install Dependencies** (Takes 2-3 minutes)
```bash
# Install PHP dependencies
composer install

# Install Node dependencies
npm install
```

### ✅ **Step 3: Setup Database**
```bash
# Start XAMPP MySQL
# OR create database via command line:
mysql -u root -p
CREATE DATABASE only_care_db;
EXIT;
```

### ✅ **Step 4: Run Migrations**
```bash
php artisan migrate
```

### ✅ **Step 5: Create Admin User**

Create file: `database/seeders/AdminSeeder.php`

```php
<?php
namespace Database\Seeders;

use App\Models\Admin;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;

class AdminSeeder extends Seeder
{
    public function run(): void
    {
        Admin::create([
            'id' => 'ADM_' . Str::random(10),
            'username' => 'admin',
            'email' => 'admin@onlycare.app',
            'password' => Hash::make('admin123'),
            'role' => 'SUPER_ADMIN',
            'is_active' => true,
        ]);
    }
}
```

Then run:
```bash
php artisan db:seed --class=AdminSeeder
```

### ✅ **Step 6: Build Assets**
```bash
# For production (recommended for testing)
npm run build

# OR for development (with hot reload)
npm run dev
```

### ✅ **Step 7: Start Laravel Server**
```bash
php artisan serve
```

Server will start at: **http://localhost:8000**

---

## 🔐 **Login Credentials**

```
URL:      http://localhost:8000/login
Email:    admin@onlycare.app
Password: admin123
```

---

## ⚡ **Quick Test Commands**

```bash
# Clear all caches
php artisan cache:clear
php artisan config:clear
php artisan route:clear
php artisan view:clear

# Check routes
php artisan route:list

# Check database connection
php artisan tinker
>>> DB::connection()->getPdo();
```

---

## 🐛 **Troubleshooting**

### Problem: Composer command not found
```bash
# Install Composer first
# Download from: https://getcomposer.org/
```

### Problem: npm command not found
```bash
# Install Node.js first
# Download from: https://nodejs.org/
```

### Problem: Database connection error
```bash
# Check .env file database settings:
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=only_care_db
DB_USERNAME=root
DB_PASSWORD=
```

### Problem: Assets not loading
```bash
# Rebuild assets
npm run build

# Clear browser cache
# Ctrl+Shift+R (Windows/Linux)
# Cmd+Shift+R (Mac)
```

---

## ✅ **Verification Checklist**

- [ ] Composer installed
- [ ] Node.js & npm installed
- [ ] XAMPP MySQL running
- [ ] Database created (`only_care_db`)
- [ ] `composer install` completed
- [ ] `npm install` completed
- [ ] Migrations run successfully
- [ ] Admin user seeded
- [ ] Assets built (`npm run build`)
- [ ] Server running (`php artisan serve`)
- [ ] Can access http://localhost:8000

---

## 🎯 **Ready to Test!**

Once all steps are complete:

1. ✅ Open browser: http://localhost:8000
2. ✅ Should see login page
3. ✅ Login with: admin@onlycare.app / admin123
4. ✅ Should see dashboard

---

## 📝 **Testing Checklist**

Use the **TESTING_CHECKLIST.md** file to systematically test all features:

1. Authentication (login/logout)
2. Dashboard (stats, charts)
3. User Management
4. Call Management  
5. Transactions
6. Withdrawals
7. KYC Verification
8. Reports
9. Coin Packages
10. Settings

Each section has detailed checkpoints!

---

**Need Help?** 
- Check logs: `storage/logs/laravel.log`
- Run: `php artisan tinker` for database testing
- Check routes: `php artisan route:list`

**Happy Testing! 🚀**







