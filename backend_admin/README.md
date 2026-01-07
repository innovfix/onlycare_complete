# Only Care - Professional Dark Mode Admin Panel

![Laravel](https://img.shields.io/badge/Laravel-10-red)
![PHP](https://img.shields.io/badge/PHP-8.1+-blue)
![Tailwind](https://img.shields.io/badge/Tailwind-3.0-cyan)
![License](https://img.shields.io/badge/License-MIT-green)

> **Professional dark mode admin panel for the Only Care dating & calling application. Built with Laravel 10, Tailwind CSS, Alpine.js, and Chart.js.**

---

## 🌟 Features

### ✨ Core Features
- ✅ **Dark Mode Design** - Professional black & white theme
- ✅ **Fully Responsive** - Mobile-first design
- ✅ **User Management** - Complete CRUD operations
- ✅ **Call Management** - View and analyze calls
- ✅ **Financial Management** - Transactions, withdrawals, coin packages
- ✅ **KYC Verification** - Document review and approval
- ✅ **Content Moderation** - Reports and user suspensions
- ✅ **Analytics Dashboard** - Charts and statistics
- ✅ **Role-Based Access** - Admin permissions system

### 📊 Dashboard
- Real-time statistics
- User growth charts
- Revenue analytics
- Pending actions overview
- Recent activity feed

### 👥 User Management
- List all users with filters
- View detailed user profiles
- Edit user information
- Block/unblock users
- Delete user accounts
- Track user activity

### 💰 Financial Management
- Transaction history
- Coin package management
- Withdrawal approvals
- Revenue reports
- Payment tracking

### 🔐 KYC Verification
- Document upload review
- Approve/reject KYC
- Verification workflow
- User verification status

### 🚨 Content Moderation
- User reports management
- Report resolution
- User suspension system
- Content monitoring

---

## 🚀 Quick Start

### Prerequisites
- PHP >= 8.1
- Composer
- Node.js & NPM
- MySQL 5.7+

### Installation

1. **Install Dependencies**
```bash
composer install
npm install
```

2. **Configure Environment**
```bash
# Copy .env file (if needed)
cp .env.example .env

# Generate application key
php artisan key:generate
```

3. **Setup Database**
```bash
# Create database
mysql -u root -p
CREATE DATABASE only_care_db;
EXIT;

# Run migrations
php artisan migrate
```

4. **Create Admin User**
```bash
php artisan make:seeder AdminSeeder
```

Add this to `database/seeders/AdminSeeder.php`:
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

5. **Build Assets**
```bash
npm run build
# Or for development
npm run dev
```

6. **Start Server**
```bash
php artisan serve
```

7. **Access Admin Panel**
```
http://localhost:8000/login
```

**Default Login:**
- Email: `admin@onlycare.app`
- Password: `admin123`

---

## 📁 Project Structure

```
only_care_admin/
├── app/
│   ├── Http/
│   │   ├── Controllers/          # All controllers
│   │   └── Middleware/           # Authentication middleware
│   └── Models/                   # 15 Eloquent models
├── config/
│   └── auth.php                  # Admin authentication
├── database/
│   ├── migrations/               # 15 database tables
│   └── seeders/                  # Database seeders
├── resources/
│   ├── css/
│   │   └── app.css              # Dark mode styles
│   ├── js/
│   │   └── app.js               # Alpine.js & Chart.js
│   └── views/
│       ├── layouts/             # Base layout
│       ├── auth/                # Login
│       └── dashboard/           # Dashboard
├── routes/
│   └── web.php                  # All routes
├── composer.json                # PHP dependencies
├── package.json                 # Node dependencies
└── tailwind.config.js           # Tailwind config
```

---

## 🎨 UI Components

### Dark Mode Color Scheme
- Background: `#0F172A`
- Surface: `#1E293B`
- Border: `#334155`
- Text: `#F1F5F9`
- Primary: `#3B82F6`
- Success: `#10B981`
- Warning: `#F59E0B`
- Danger: `#EF4444`

### Available Components
- Buttons (primary, success, danger, warning, secondary)
- Badges (all status types)
- Cards (header, body)
- Forms (inputs, selects, labels)
- Tables (responsive)
- Alerts (success, danger, warning, info)
- Charts (Line, Bar, Pie)

---

## 💾 Database Schema

### Tables (15)
1. **users** - User accounts
2. **calls** - Call records
3. **coin_packages** - Coin packages
4. **transactions** - Financial transactions
5. **bank_accounts** - Bank details
6. **withdrawals** - Withdrawal requests
7. **kyc_documents** - KYC documents
8. **messages** - Chat messages
9. **friendships** - Friend connections
10. **referrals** - Referral tracking
11. **reports** - User reports
12. **notifications** - Push notifications
13. **app_settings** - App configuration
14. **blocked_users** - User blocks
15. **admins** - Admin users

---

## 🔐 Admin Roles

| Role | Permissions |
|------|------------|
| **SUPER_ADMIN** | Full access to everything |
| **ADMIN** | User management, content moderation |
| **MODERATOR** | Content moderation only |
| **FINANCE** | Financial operations |
| **SUPPORT** | Read-only, support tickets |

---

## 📊 Routes

### Authentication
- `GET /login` - Login form
- `POST /login` - Process login
- `POST /logout` - Logout

### Dashboard
- `GET /` - Dashboard
- `GET /dashboard` - Dashboard

### Users
- `GET /users` - List users
- `GET /users/{id}` - View user
- `PUT /users/{id}` - Update user
- `POST /users/{id}/block` - Block user
- `DELETE /users/{id}` - Delete user

### Calls
- `GET /calls` - List calls
- `GET /calls/{id}` - View call

### Transactions
- `GET /transactions` - List transactions
- `GET /transactions/{id}` - View transaction

### Withdrawals
- `GET /withdrawals` - List withdrawals
- `POST /withdrawals/{id}/approve` - Approve
- `POST /withdrawals/{id}/reject` - Reject

### KYC
- `GET /kyc` - List pending KYC
- `POST /kyc/{userId}/approve` - Approve
- `POST /kyc/{userId}/reject` - Reject

### Reports
- `GET /reports` - List reports
- `POST /reports/{id}/resolve` - Resolve

### Coin Packages
- `GET /coin-packages` - List packages
- `POST /coin-packages` - Create package
- `PUT /coin-packages/{id}` - Update package

### Settings
- `GET /settings` - View settings
- `POST /settings` - Update settings

---

## 🛠️ Tech Stack

- **Backend**: Laravel 10
- **Frontend**: Tailwind CSS 3
- **JavaScript**: Alpine.js
- **Charts**: Chart.js
- **Database**: MySQL
- **Build Tool**: Vite
- **Authentication**: Laravel Guards

---

## 📖 Documentation

- `INSTALLATION_GUIDE.md` - Installation instructions
- `PROJECT_SUMMARY.md` - Complete project overview
- `ADMIN_PANEL_DOCUMENTATION.md` - Database schema
- `ADMIN_PANEL_REQUIREMENTS.md` - Feature requirements
- `BUSINESS_LOGIC_AND_FEATURES.md` - Business rules
- `API_DOCUMENTATION.md` - API endpoints

---

## 🎯 Key Features Implemented

### ✅ Authentication System
- Admin login/logout
- Session management
- Remember me
- Role-based access

### ✅ Dashboard
- Statistics cards
- Charts (user growth, revenue)
- Pending actions
- Recent activity

### ✅ User Management
- CRUD operations
- Search and filters
- Block/unblock
- View statistics

### ✅ Financial Management
- Transaction viewing
- Withdrawal approval workflow
- Coin package management
- Revenue tracking

### ✅ KYC Verification
- Document review
- Approval workflow
- Rejection with reason
- Status tracking

### ✅ Content Moderation
- Report management
- User suspension
- Report resolution
- Moderation actions

---

## 🔧 Configuration

### Database
Edit `.env`:
```env
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=only_care_db
DB_USERNAME=root
DB_PASSWORD=
```

### Admin Guard
Configured in `config/auth.php`:
```php
'guards' => [
    'admin' => [
        'driver' => 'session',
        'provider' => 'admins',
    ],
],
```

---

## 🐛 Troubleshooting

### Issue: Assets not loading
```bash
npm run build
php artisan cache:clear
```

### Issue: Login fails
```bash
# Make sure admin user exists
php artisan db:seed --class=AdminSeeder
```

### Issue: Migrations fail
```bash
# Check database exists
mysql -u root -p
SHOW DATABASES;

# If not, create it
CREATE DATABASE only_care_db;
```

---

## 📱 Mobile Responsive

- ✅ Collapsible sidebar
- ✅ Touch-friendly interface
- ✅ Responsive tables
- ✅ Mobile-optimized forms
- ✅ Adaptive layouts

---

## 🚀 Performance

- Optimized queries
- Lazy loading
- Asset minification
- Database indexing
- Caching support

---

## 🔒 Security

- CSRF protection
- XSS prevention
- SQL injection protection
- Password hashing
- Session security
- Role-based permissions

---

## 📈 Future Enhancements

- [ ] Email notifications
- [ ] Activity logs
- [ ] Advanced analytics
- [ ] Data export (CSV/PDF)
- [ ] Two-factor authentication
- [ ] Real-time updates

---

## 🤝 Contributing

This is a private project for Only Care application.

---

## 📝 License

MIT License - See LICENSE file for details

---

## 👨‍💻 Developer

Created with ❤️ for Only Care App

---

## 📞 Support

For issues or questions:
1. Check the documentation files
2. Review installation guide
3. Check project summary

---

## ✨ Screenshots

### Login Page
![Login](https://via.placeholder.com/800x400?text=Dark+Mode+Login)

### Dashboard
![Dashboard](https://via.placeholder.com/800x400?text=Dashboard+with+Charts)

### User Management
![Users](https://via.placeholder.com/800x400?text=User+Management)

---

## 🎉 Status

**✅ Production Ready**

- All core features implemented
- Dark mode design complete
- Fully responsive
- Database schema complete
- Authentication working
- Controllers implemented
- Ready to use

---

**Built with Laravel 10 • Tailwind CSS • Alpine.js • Chart.js**

**Version:** 1.0.0  
**Last Updated:** November 4, 2025  
**Status:** ✅ Complete

