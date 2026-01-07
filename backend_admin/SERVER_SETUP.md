# 🖥️ OnlyCare Admin - Server Setup Guide

Complete guide to prepare a fresh server for OnlyCare Admin deployment.

## 📦 For Ubuntu/Debian Servers

### 1. Update System

```bash
sudo apt update && sudo apt upgrade -y
```

### 2. Install Apache

```bash
# Install Apache
sudo apt install apache2 -y

# Enable required modules
sudo a2enmod rewrite ssl headers

# Start Apache
sudo systemctl start apache2
sudo systemctl enable apache2
```

### 3. Install PHP 8.1

```bash
# Add PHP repository
sudo apt install software-properties-common -y
sudo add-apt-repository ppa:ondrej/php -y
sudo apt update

# Install PHP and extensions
sudo apt install php8.1 php8.1-fpm php8.1-cli php8.1-common php8.1-mysql \
    php8.1-zip php8.1-gd php8.1-mbstring php8.1-curl php8.1-xml \
    php8.1-bcmath php8.1-redis php8.1-intl php8.1-opcache -y

# Verify installation
php -v
```

### 4. Install MySQL

```bash
# Install MySQL
sudo apt install mysql-server -y

# Secure installation
sudo mysql_secure_installation

# Start MySQL
sudo systemctl start mysql
sudo systemctl enable mysql
```

### 5. Install Composer

```bash
cd ~
curl -sS https://getcomposer.org/installer -o composer-setup.php
sudo php composer-setup.php --install-dir=/usr/local/bin --filename=composer

# Verify
composer --version
```

### 6. Install Redis (Optional but Recommended)

```bash
sudo apt install redis-server -y
sudo systemctl start redis-server
sudo systemctl enable redis-server

# Test Redis
redis-cli ping
# Should return: PONG
```

### 7. Install Git

```bash
sudo apt install git -y
git --version
```

### 8. Install Node.js (Optional - for frontend assets)

```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y
node --version
npm --version
```

### 9. Configure PHP

```bash
# Edit PHP configuration
sudo nano /etc/php/8.1/apache2/php.ini
```

Update these values:

```ini
upload_max_filesize = 100M
post_max_size = 100M
max_execution_time = 300
memory_limit = 256M
date.timezone = Asia/Kolkata
```

Restart Apache:

```bash
sudo systemctl restart apache2
```

### 10. Create Application Directory

```bash
# Create directory
sudo mkdir -p /var/www/onlycare_admin

# Set ownership
sudo chown -R $USER:www-data /var/www/onlycare_admin

# Set permissions
sudo chmod -R 755 /var/www
```

---

## 🔴 For CentOS/RHEL Servers

### 1. Update System

```bash
sudo yum update -y
```

### 2. Install Apache

```bash
sudo yum install httpd -y
sudo systemctl start httpd
sudo systemctl enable httpd
```

### 3. Install PHP 8.1

```bash
# Add EPEL and Remi repositories
sudo yum install epel-release -y
sudo yum install https://rpms.remirepo.net/enterprise/remi-release-8.rpm -y

# Enable PHP 8.1
sudo dnf module reset php -y
sudo dnf module enable php:remi-8.1 -y

# Install PHP
sudo yum install php php-cli php-fpm php-mysqlnd php-zip php-gd \
    php-mbstring php-curl php-xml php-pear php-bcmath php-json -y
```

### 4. Install MySQL

```bash
sudo yum install mysql-server -y
sudo systemctl start mysqld
sudo systemctl enable mysqld
sudo mysql_secure_installation
```

### 5. Install Composer

```bash
cd ~
curl -sS https://getcomposer.org/installer | php
sudo mv composer.phar /usr/local/bin/composer
chmod +x /usr/local/bin/composer
```

### 6. Configure Firewall

```bash
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

---

## 🔒 SSL Certificate Setup (Let's Encrypt)

### For Apache

```bash
# Install Certbot
sudo apt install certbot python3-certbot-apache -y

# Get certificate
sudo certbot --apache -d yourdomain.com -d www.yourdomain.com

# Test auto-renewal
sudo certbot renew --dry-run
```

### For Nginx

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx -y

# Get certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

---

## 🗄️ Database Setup

```bash
# Login to MySQL
sudo mysql -u root -p

# Create database and user
CREATE DATABASE onlycare_production CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'onlycare_user'@'localhost' IDENTIFIED BY 'YourSecurePassword123!';
GRANT ALL PRIVILEGES ON onlycare_production.* TO 'onlycare_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

---

## 🌐 Apache Virtual Host Setup

Create configuration file:

```bash
sudo nano /etc/apache2/sites-available/onlycare.conf
```

Add this configuration:

```apache
<VirtualHost *:80>
    ServerName yourdomain.com
    ServerAlias www.yourdomain.com
    ServerAdmin admin@yourdomain.com
    DocumentRoot /var/www/onlycare_admin/public

    <Directory /var/www/onlycare_admin/public>
        Options -Indexes +FollowSymLinks
        AllowOverride All
        Require all granted
    </Directory>

    ErrorLog ${APACHE_LOG_DIR}/onlycare_error.log
    CustomLog ${APACHE_LOG_DIR}/onlycare_access.log combined
</VirtualHost>
```

Enable site:

```bash
sudo a2ensite onlycare.conf
sudo a2dissite 000-default.conf
sudo systemctl reload apache2
```

---

## 🔐 Security Hardening

### 1. Configure UFW Firewall

```bash
# Install UFW
sudo apt install ufw -y

# Configure rules
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow http
sudo ufw allow https

# Enable firewall
sudo ufw enable
sudo ufw status
```

### 2. Disable Root SSH Login

```bash
sudo nano /etc/ssh/sshd_config
```

Set:

```
PermitRootLogin no
PasswordAuthentication no  # If using SSH keys
```

Restart SSH:

```bash
sudo systemctl restart sshd
```

### 3. Install Fail2Ban

```bash
sudo apt install fail2ban -y
sudo systemctl start fail2ban
sudo systemctl enable fail2ban
```

---

## 📊 Performance Optimization

### 1. Enable OPcache

```bash
sudo nano /etc/php/8.1/apache2/conf.d/10-opcache.ini
```

Add:

```ini
opcache.enable=1
opcache.memory_consumption=128
opcache.interned_strings_buffer=8
opcache.max_accelerated_files=10000
opcache.revalidate_freq=2
opcache.fast_shutdown=1
```

### 2. Configure Apache for Performance

```bash
sudo nano /etc/apache2/mods-available/mpm_prefork.conf
```

Adjust:

```apache
<IfModule mpm_prefork_module>
    StartServers             5
    MinSpareServers          5
    MaxSpareServers          10
    MaxRequestWorkers        150
    MaxConnectionsPerChild   3000
</IfModule>
```

Enable compression:

```bash
sudo a2enmod deflate
sudo systemctl restart apache2
```

---

## ✅ Verification Checklist

After server setup, verify:

- [ ] PHP version 8.1+ installed
- [ ] All required PHP extensions loaded
- [ ] MySQL running and accessible
- [ ] Apache/Nginx configured correctly
- [ ] Composer installed globally
- [ ] SSL certificate active
- [ ] Firewall configured
- [ ] Proper directory permissions
- [ ] Redis running (if using)
- [ ] Git installed

Check with:

```bash
# PHP
php -v
php -m

# MySQL
sudo systemctl status mysql

# Apache
sudo systemctl status apache2

# Composer
composer --version

# Redis
redis-cli ping

# Git
git --version

# Firewall
sudo ufw status
```

---

## 🚀 Ready for Deployment

Once your server is set up, proceed to deployment:

```bash
cd /var/www/onlycare_admin
git clone https://github.com/innovfix/onlycare_admin.git .
chmod +x deploy.sh
./deploy.sh
```

See `LIVE_DEPLOYMENT_GUIDE.md` for detailed deployment instructions.

---

## 📞 Common Server Issues

### Issue: PHP module not loaded

```bash
# Check installed modules
php -m

# Install missing module
sudo apt install php8.1-modulename

# Restart Apache
sudo systemctl restart apache2
```

### Issue: Permission denied

```bash
# Fix ownership
sudo chown -R www-data:www-data /var/www/onlycare_admin

# Fix permissions
sudo chmod -R 755 /var/www/onlycare_admin
sudo chmod -R 775 /var/www/onlycare_admin/storage
sudo chmod -R 775 /var/www/onlycare_admin/bootstrap/cache
```

### Issue: Database connection refused

```bash
# Check MySQL status
sudo systemctl status mysql

# Start MySQL
sudo systemctl start mysql

# Check MySQL port
sudo netstat -tlnp | grep 3306
```

---

**Server setup complete! Ready for application deployment. 🎉**

