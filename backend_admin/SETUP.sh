#!/bin/bash

echo "🚀 Only Care Admin Panel - Automated Setup"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if composer is installed
if ! command -v composer &> /dev/null; then
    echo -e "${RED}❌ Composer not found. Please install Composer first.${NC}"
    echo "Download from: https://getcomposer.org/"
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo -e "${RED}❌ npm not found. Please install Node.js first.${NC}"
    echo "Download from: https://nodejs.org/"
    exit 1
fi

echo -e "${GREEN}✓${NC} Composer found"
echo -e "${GREEN}✓${NC} npm found"
echo ""

# Install PHP dependencies
echo "📦 Installing PHP dependencies..."
composer install --no-interaction
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} PHP dependencies installed"
else
    echo -e "${RED}❌ Failed to install PHP dependencies${NC}"
    exit 1
fi
echo ""

# Install Node dependencies
echo "📦 Installing Node dependencies..."
npm install
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Node dependencies installed"
else
    echo -e "${RED}❌ Failed to install Node dependencies${NC}"
    exit 1
fi
echo ""

# Generate application key
echo "🔑 Generating application key..."
php artisan key:generate --force
echo ""

# Check if database exists
echo "🗄️  Setting up database..."
echo -e "${YELLOW}⚠️  Make sure MySQL is running in XAMPP${NC}"
echo ""
echo "Creating database..."
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS only_care_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Database created"
else
    echo -e "${YELLOW}⚠️  Could not create database automatically${NC}"
    echo "Please create it manually in phpMyAdmin or MySQL:"
    echo "  CREATE DATABASE only_care_db;"
    read -p "Press Enter when database is created..."
fi
echo ""

# Run migrations
echo "📊 Running database migrations..."
php artisan migrate --force
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Migrations completed"
else
    echo -e "${RED}❌ Failed to run migrations${NC}"
    exit 1
fi
echo ""

# Seed database
echo "🌱 Seeding database with sample data..."
php artisan db:seed --class=AdminSeeder --force
php artisan db:seed --class=SampleDataSeeder --force
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Database seeded"
else
    echo -e "${YELLOW}⚠️  Failed to seed database (non-critical)${NC}"
fi
echo ""

# Build assets
echo "🎨 Building assets..."
npm run build
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Assets built"
else
    echo -e "${RED}❌ Failed to build assets${NC}"
    exit 1
fi
echo ""

# Clear caches
echo "🧹 Clearing caches..."
php artisan cache:clear
php artisan config:clear
php artisan route:clear
php artisan view:clear
echo -e "${GREEN}✓${NC} Caches cleared"
echo ""

echo "=========================================="
echo -e "${GREEN}✅ Setup Complete!${NC}"
echo "=========================================="
echo ""
echo "📝 Admin Login Credentials:"
echo "   URL:      http://localhost:8000/login"
echo "   Email:    admin@onlycare.app"
echo "   Password: admin123"
echo ""
echo "🚀 Start the server with:"
echo "   php artisan serve"
echo ""
echo "📊 Sample Data Created:"
echo "   - 30 Users (20 male, 10 female)"
echo "   - 50 Calls"
echo "   - Multiple Transactions"
echo "   - Withdrawal Requests"
echo "   - KYC Documents"
echo "   - User Reports"
echo "   - 4 Coin Packages"
echo ""
echo "Happy Testing! 🎉"







