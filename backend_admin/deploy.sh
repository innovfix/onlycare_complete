#!/bin/bash

# ==================================================
# ONLYCARE ADMIN - AUTOMATED DEPLOYMENT SCRIPT
# ==================================================
# This script automates the deployment process for production

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() { echo -e "${GREEN}✓ $1${NC}"; }
print_error() { echo -e "${RED}✗ $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠ $1${NC}"; }

echo "=========================================="
echo "  ONLYCARE ADMIN - DEPLOYMENT SCRIPT"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then 
    print_warning "Running as root. Consider running as a regular user with sudo privileges."
fi

# Step 1: Check PHP version
print_info "Checking PHP version..."
PHP_VERSION=$(php -r 'echo PHP_VERSION;' 2>/dev/null || echo "0")
if [ "$PHP_VERSION" = "0" ]; then
    print_error "PHP is not installed!"
    exit 1
fi
print_success "PHP $PHP_VERSION detected"

# Step 2: Check Composer
print_info "Checking Composer..."
if ! command -v composer &> /dev/null; then
    print_error "Composer is not installed!"
    exit 1
fi
print_success "Composer is installed"

# Step 3: Pull latest code from repository
print_info "Pulling latest code from repository..."
if [ -d ".git" ]; then
    git pull origin main
    print_success "Code updated from repository"
else
    print_warning "Not a git repository. Skipping git pull."
fi

# Step 4: Install/Update Composer dependencies
print_info "Installing Composer dependencies..."
composer install --optimize-autoloader --no-dev --no-interaction
print_success "Composer dependencies installed"

# Step 5: Check if .env file exists
if [ ! -f ".env" ]; then
    print_warning ".env file not found!"
    if [ -f ".env.production.example" ]; then
        print_info "Copying .env.production.example to .env..."
        cp .env.production.example .env
        print_warning "Please configure .env file with your production settings!"
        print_info "Edit .env and then run this script again."
        exit 1
    else
        print_error ".env.production.example not found!"
        exit 1
    fi
fi
print_success ".env file exists"

# Step 6: Generate Application Key if not set
print_info "Checking application key..."
if grep -q "APP_KEY=$\|APP_KEY=base64:GENERATE" .env; then
    print_info "Generating new application key..."
    php artisan key:generate --force
    print_success "Application key generated"
else
    print_success "Application key already set"
fi

# Step 7: Clear and optimize caches
print_info "Clearing all caches..."
php artisan config:clear
php artisan cache:clear
php artisan route:clear
php artisan view:clear
print_success "Caches cleared"

# Step 8: Database migrations
print_info "Running database migrations..."
read -p "Do you want to run fresh migrations (WARNING: This will delete all data)? [y/N]: " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_warning "Running fresh migrations..."
    php artisan migrate:fresh --force
    print_success "Fresh migrations completed"
else
    print_info "Running incremental migrations..."
    php artisan migrate --force
    print_success "Migrations completed"
fi

# Step 9: Seed database
read -p "Do you want to seed the database? [y/N]: " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_info "Seeding database..."
    php artisan db:seed --force
    print_success "Database seeded"
fi

# Step 10: Create storage symlink
print_info "Creating storage symlink..."
php artisan storage:link
print_success "Storage symlink created"

# Step 11: Set correct permissions
print_info "Setting directory permissions..."
chmod -R 775 storage bootstrap/cache
print_success "Permissions set"

# Step 12: Optimize for production
print_info "Optimizing for production..."
php artisan config:cache
php artisan route:cache
php artisan view:cache
php artisan event:cache
print_success "Application optimized"

# Step 13: Run post-deployment tests
print_info "Running post-deployment verification..."
if [ -f "verify-deployment.sh" ]; then
    bash verify-deployment.sh
fi

echo ""
echo "=========================================="
print_success "DEPLOYMENT COMPLETED SUCCESSFULLY!"
echo "=========================================="
echo ""
print_info "Next Steps:"
echo "  1. Verify .env configuration"
echo "  2. Test the application at: $APP_URL"
echo "  3. Login to admin panel with configured credentials"
echo "  4. Check logs: storage/logs/laravel.log"
echo ""
print_warning "Important: Make sure to configure your web server (Apache/Nginx) properly!"
echo ""

