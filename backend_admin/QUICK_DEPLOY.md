# 🚀 OnlyCare Admin - Quick Deploy Guide

## One-Command Deployment

```bash
# Clone, setup, and deploy in one go
git clone https://github.com/innovfix/onlycare_admin.git && \
cd onlycare_admin && \
chmod +x deploy.sh database-setup.sh verify-deployment.sh && \
./deploy.sh
```

## What Happens Automatically

✅ **System Check** - Verifies PHP, Composer, MySQL  
✅ **Dependencies** - Installs all packages  
✅ **Environment** - Creates .env file  
✅ **Security** - Generates app key  
✅ **Database** - Runs migrations  
✅ **Storage** - Sets up directories & symlinks  
✅ **Optimization** - Caches config, routes, views  
✅ **Verification** - Checks deployment  

## Manual Configuration Required

After automated deployment, configure these in `.env`:

```env
# Your Domain
APP_URL=https://yourdomain.com

# Database
DB_DATABASE=onlycare_production
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Agora (Video Calls)
AGORA_APP_ID=your_app_id
AGORA_APP_CERTIFICATE=your_certificate

# Email (Optional)
MAIL_HOST=smtp.mailtrap.io
MAIL_USERNAME=your_username
MAIL_PASSWORD=your_password
```

## Access Admin Panel

1. **URL**: `https://yourdomain.com/login`
2. **Email**: `admin@example.com`
3. **Password**: `admin123`
4. **⚠️ CHANGE PASSWORD IMMEDIATELY!**

## Test API

```bash
curl https://yourdomain.com/api/health
```

## Need Help?

See `LIVE_DEPLOYMENT_GUIDE.md` for detailed instructions.

## Server Requirements

- PHP 8.1+
- MySQL 8.0+
- 2GB RAM
- Apache/Nginx

## Support

- 📖 Full Docs: `LIVE_DEPLOYMENT_GUIDE.md`
- 🔧 Troubleshooting: See main guide
- 🌐 API Docs: `/api-docs`

---

**That's it! Your app is live! 🎉**

