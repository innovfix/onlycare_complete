# 🚀 OnlyCare Admin - Complete Deployment Package

## 📦 What's Included

This deployment package contains everything you need to deploy OnlyCare Admin to production:

### Automated Scripts
- ✅ `deploy.sh` - Main deployment automation script
- ✅ `database-setup.sh` - Database setup and migration script
- ✅ `verify-deployment.sh` - Post-deployment verification script

### Configuration Files
- ✅ `env.production.template` - Production environment template
- ✅ `.htaccess.production` - Production Apache configuration

### Documentation
- ✅ `LIVE_DEPLOYMENT_GUIDE.md` - Complete deployment guide (READ THIS FIRST)
- ✅ `QUICK_DEPLOY.md` - Quick start guide
- ✅ `SERVER_SETUP.md` - Server preparation guide

---

## 🎯 Quick Start (3 Steps)

### Step 1: Prepare Server
```bash
# Follow SERVER_SETUP.md to prepare your server
# Ensure PHP 8.1+, MySQL, Apache/Nginx are installed
```

### Step 2: Deploy Application
```bash
# Clone and deploy
git clone https://github.com/innovfix/onlycare_admin.git
cd onlycare_admin
./deploy.sh
```

### Step 3: Configure & Verify
```bash
# Edit .env with your settings
nano .env

# Run verification
./verify-deployment.sh

# Access: https://yourdomain.com/login
```

---

## 📖 Documentation Overview

### For First-Time Deployment
1. **Start Here**: `SERVER_SETUP.md`
   - Prepare fresh server
   - Install dependencies
   - Configure services

2. **Then Read**: `LIVE_DEPLOYMENT_GUIDE.md`
   - Complete deployment instructions
   - Web server configuration
   - Security setup
   - Troubleshooting

3. **Quick Reference**: `QUICK_DEPLOY.md`
   - One-command deployment
   - Essential configuration
   - Quick access info

### For Experienced Users
- Jump directly to `QUICK_DEPLOY.md`
- Run `./deploy.sh`
- Configure `.env`
- Done!

---

## 🔧 Deployment Scripts Explained

### deploy.sh
Main deployment script that:
- ✅ Checks system requirements
- ✅ Installs dependencies
- ✅ Configures environment
- ✅ Runs migrations
- ✅ Sets permissions
- ✅ Optimizes application

**Usage:**
```bash
chmod +x deploy.sh
./deploy.sh
```

### database-setup.sh
Database configuration script that:
- ✅ Creates database
- ✅ Creates database user
- ✅ Grants permissions
- ✅ Runs migrations
- ✅ Seeds initial data

**Usage:**
```bash
chmod +x database-setup.sh
./database-setup.sh
```

### verify-deployment.sh
Verification script that checks:
- ✅ Environment configuration
- ✅ File permissions
- ✅ Database connection
- ✅ Required extensions
- ✅ Critical files
- ✅ Cache optimization

**Usage:**
```bash
chmod +x verify-deployment.sh
./verify-deployment.sh
```

---

## ⚙️ Configuration Files

### env.production.template
Production environment template with:
- Database configuration
- Security settings
- API keys (Agora, etc.)
- Mail settings
- Cache configuration
- Feature flags

**Usage:**
```bash
cp env.production.template .env
nano .env  # Edit with your values
```

### .htaccess.production
Production Apache configuration with:
- Security headers
- Directory protection
- File access restrictions
- Rewrite rules

**Usage:**
```bash
cp .htaccess.production .htaccess
```

---

## 🎨 Project Structure

```
onlycare_admin/
├── app/                          # Application code
│   ├── Http/Controllers/Api/     # API controllers
│   ├── Models/                   # Database models
│   └── ...
├── database/
│   ├── migrations/               # Database migrations
│   └── seeders/                  # Data seeders
├── public/                       # Web root
├── resources/
│   └── views/                    # Blade templates
├── routes/
│   ├── web.php                   # Web routes
│   └── api.php                   # API routes
├── storage/                      # Storage directory
│
├── deploy.sh                     # 🚀 Main deployment script
├── database-setup.sh             # 🗄️ Database setup script
├── verify-deployment.sh          # ✅ Verification script
├── env.production.template       # ⚙️ Environment template
├── .htaccess.production          # 🔒 Apache config
│
├── LIVE_DEPLOYMENT_GUIDE.md      # 📖 Complete guide
├── QUICK_DEPLOY.md               # ⚡ Quick start
├── SERVER_SETUP.md               # 🖥️ Server preparation
└── DEPLOYMENT_README.md          # 📋 This file
```

---

## 🔑 Default Credentials

After deployment, access admin panel with:

- **URL**: `https://yourdomain.com/login`
- **Email**: `admin@example.com`
- **Password**: `admin123`

**⚠️ CRITICAL**: Change password immediately after first login!

---

## 📊 Features

### Admin Panel Features
- ✅ User Management (Users & Creators)
- ✅ Call History & Analytics
- ✅ Transaction Management
- ✅ Coin Package Management
- ✅ Withdrawal Processing
- ✅ KYC Verification
- ✅ Content Moderation
- ✅ Reports & Analytics
- ✅ Settings Management
- ✅ Dark Mode UI

### API Features
- ✅ User Authentication (Sanctum)
- ✅ Registration & Login
- ✅ User Profiles
- ✅ Video/Audio Calls (Agora)
- ✅ Chat Messaging
- ✅ Content Management
- ✅ Coin Purchases
- ✅ Wallet & Transactions
- ✅ Withdrawal System
- ✅ KYC Verification
- ✅ Referral System
- ✅ Notifications
- ✅ Search & Filtering

---

## 🌍 Supported Platforms

### Server OS
- ✅ Ubuntu 20.04+
- ✅ Debian 10+
- ✅ CentOS 7+
- ✅ RHEL 8+

### Web Servers
- ✅ Apache 2.4+
- ✅ Nginx 1.18+

### Databases
- ✅ MySQL 8.0+
- ✅ MariaDB 10.5+

### PHP
- ✅ PHP 8.1+
- ✅ PHP 8.2

---

## 🔒 Security Features

- ✅ Environment-based configuration
- ✅ Sanctum API authentication
- ✅ CSRF protection
- ✅ XSS prevention
- ✅ SQL injection protection
- ✅ Rate limiting
- ✅ Secure password hashing
- ✅ SSL/TLS encryption
- ✅ Security headers
- ✅ File upload validation

---

## 📈 Performance Features

- ✅ Route caching
- ✅ Config caching
- ✅ View caching
- ✅ OPcache optimization
- ✅ Redis caching support
- ✅ Database query optimization
- ✅ Asset compression
- ✅ Lazy loading

---

## 🧪 Testing

### Test Endpoints
```bash
# Health check
curl https://yourdomain.com/api/health

# Test login
curl -X POST https://yourdomain.com/api/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

### Access API Documentation
- URL: `https://yourdomain.com/api-docs`
- Complete API reference with examples

---

## 📦 Deployment Checklist

### Pre-Deployment
- [ ] Server meets requirements
- [ ] Domain configured and pointing to server
- [ ] SSL certificate obtained
- [ ] Database credentials ready
- [ ] SMTP credentials ready (optional)
- [ ] Agora credentials ready
- [ ] Backup strategy planned

### During Deployment
- [ ] Clone repository
- [ ] Run `deploy.sh`
- [ ] Configure `.env`
- [ ] Run `database-setup.sh`
- [ ] Configure web server
- [ ] Run `verify-deployment.sh`

### Post-Deployment
- [ ] Change admin password
- [ ] Test login
- [ ] Test API endpoints
- [ ] Configure app settings
- [ ] Add coin packages
- [ ] Setup cron jobs
- [ ] Configure backups
- [ ] Monitor logs

---

## 🆘 Getting Help

### Documentation
- **Main Guide**: `LIVE_DEPLOYMENT_GUIDE.md`
- **Server Setup**: `SERVER_SETUP.md`
- **Quick Start**: `QUICK_DEPLOY.md`
- **API Docs**: `/api-docs` on your server

### Common Issues
See **Troubleshooting** section in `LIVE_DEPLOYMENT_GUIDE.md`

### Support
- GitHub Issues: https://github.com/innovfix/onlycare_admin/issues
- API Documentation: https://yourdomain.com/api-docs

---

## 🔄 Updates & Maintenance

### Update Application
```bash
cd /var/www/onlycare_admin
git pull origin main
composer install --no-dev
php artisan migrate --force
php artisan config:cache
php artisan route:cache
php artisan view:cache
```

### Clear Cache
```bash
php artisan cache:clear
php artisan config:clear
php artisan route:clear
php artisan view:clear
```

### View Logs
```bash
tail -f storage/logs/laravel.log
```

---

## 💡 Tips & Best Practices

### Security
1. Always use HTTPS in production
2. Keep `APP_DEBUG=false`
3. Use strong passwords
4. Regular security updates
5. Configure firewall properly
6. Enable fail2ban
7. Regular backups

### Performance
1. Enable Redis for caching
2. Use OPcache
3. Enable compression
4. Optimize images
5. Use CDN for static assets
6. Monitor server resources
7. Regular database optimization

### Maintenance
1. Monitor logs regularly
2. Keep Laravel updated
3. Update dependencies
4. Test before deploying updates
5. Maintain database backups
6. Monitor disk space
7. Check SSL certificate expiry

---

## 📊 System Requirements Summary

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| PHP | 8.1 | 8.2 |
| MySQL | 8.0 | 8.0+ |
| RAM | 2GB | 4GB+ |
| Storage | 20GB | 50GB+ |
| CPU | 1 Core | 2+ Cores |

---

## 🎉 Success!

If you've followed this guide, your OnlyCare Admin is now:
- ✅ Deployed to production
- ✅ Configured securely
- ✅ Optimized for performance
- ✅ Ready for use

### Next Steps
1. Login to admin panel
2. Change default password
3. Configure app settings
4. Add coin packages
5. Test all features
6. Connect mobile app
7. Go live!

---

## 📞 Contact & Credits

- **Project**: OnlyCare Admin Panel
- **Version**: 1.0
- **Framework**: Laravel 10
- **License**: MIT
- **Repository**: https://github.com/innovfix/onlycare_admin

---

**Happy Deploying! 🚀**

Last Updated: November 2025

