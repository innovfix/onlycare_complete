#!/bin/bash

# ==================================================
# ONLYCARE ADMIN - DEPLOYMENT VERIFICATION SCRIPT
# ==================================================
# This script verifies that deployment was successful

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_success() { echo -e "${GREEN}✓ $1${NC}"; }
print_error() { echo -e "${RED}✗ $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠ $1${NC}"; }

ERRORS=0

echo "=========================================="
echo "  DEPLOYMENT VERIFICATION"
echo "=========================================="
echo ""

# Check .env file
print_info "Checking .env file..."
if [ -f ".env" ]; then
    print_success ".env file exists"
    
    # Check critical env variables
    if grep -q "APP_KEY=base64:" .env; then
        print_success "APP_KEY is set"
    else
        print_error "APP_KEY is not properly set"
        ((ERRORS++))
    fi
    
    if grep -q "APP_ENV=production" .env; then
        print_success "APP_ENV is set to production"
    else
        print_warning "APP_ENV is not set to production"
    fi
    
    if grep -q "APP_DEBUG=false" .env; then
        print_success "APP_DEBUG is disabled"
    else
        print_warning "APP_DEBUG should be false in production"
    fi
else
    print_error ".env file not found"
    ((ERRORS++))
fi

# Check directories
print_info "Checking required directories..."
DIRS=("storage" "storage/app" "storage/framework" "storage/logs" "bootstrap/cache")
for dir in "${DIRS[@]}"; do
    if [ -d "$dir" ]; then
        print_success "$dir exists"
    else
        print_error "$dir not found"
        ((ERRORS++))
    fi
done

# Check permissions
print_info "Checking directory permissions..."
if [ -w "storage" ]; then
    print_success "storage directory is writable"
else
    print_error "storage directory is not writable"
    ((ERRORS++))
fi

if [ -w "bootstrap/cache" ]; then
    print_success "bootstrap/cache directory is writable"
else
    print_error "bootstrap/cache directory is not writable"
    ((ERRORS++))
fi

# Check storage symlink
print_info "Checking storage symlink..."
if [ -L "public/storage" ]; then
    print_success "Storage symlink exists"
else
    print_warning "Storage symlink not found (run: php artisan storage:link)"
fi

# Check vendor directory
print_info "Checking composer dependencies..."
if [ -d "vendor" ] && [ -f "vendor/autoload.php" ]; then
    print_success "Composer dependencies installed"
else
    print_error "Composer dependencies not found"
    ((ERRORS++))
fi

# Check database connection
print_info "Checking database connection..."
if php artisan db:show &> /dev/null; then
    print_success "Database connection successful"
    
    # Check if migrations ran
    print_info "Checking database migrations..."
    TABLES=$(php artisan db:table --json 2>/dev/null | grep -c '"table"' || echo "0")
    if [ "$TABLES" -gt "0" ]; then
        print_success "Database tables exist ($TABLES tables found)"
    else
        print_warning "No database tables found. Run migrations."
    fi
else
    print_error "Cannot connect to database"
    ((ERRORS++))
fi

# Check cache files
print_info "Checking optimization caches..."
if [ -f "bootstrap/cache/config.php" ]; then
    print_success "Config cache exists"
else
    print_warning "Config cache not found (run: php artisan config:cache)"
fi

if [ -f "bootstrap/cache/routes-v7.php" ]; then
    print_success "Route cache exists"
else
    print_warning "Route cache not found (run: php artisan route:cache)"
fi

# Check log file
print_info "Checking log files..."
if [ -f "storage/logs/laravel.log" ]; then
    LOG_SIZE=$(du -h "storage/logs/laravel.log" | cut -f1)
    print_success "Log file exists (Size: $LOG_SIZE)"
    
    # Check for recent errors
    RECENT_ERRORS=$(tail -n 100 storage/logs/laravel.log 2>/dev/null | grep -c "ERROR" || echo "0")
    if [ "$RECENT_ERRORS" -gt "0" ]; then
        print_warning "Found $RECENT_ERRORS recent errors in log"
    fi
else
    print_info "No log file yet (will be created on first request)"
fi

# Check critical files
print_info "Checking critical files..."
CRITICAL_FILES=(
    "artisan"
    "composer.json"
    "routes/web.php"
    "routes/api.php"
    "app/Http/Kernel.php"
)

for file in "${CRITICAL_FILES[@]}"; do
    if [ -f "$file" ]; then
        print_success "$file exists"
    else
        print_error "$file not found"
        ((ERRORS++))
    fi
done

# Check API routes
print_info "Checking API routes..."
ROUTES=$(php artisan route:list --path=api 2>/dev/null | wc -l || echo "0")
if [ "$ROUTES" -gt "10" ]; then
    print_success "API routes registered ($ROUTES routes)"
else
    print_warning "Limited API routes found"
fi

# Test PHP syntax
print_info "Checking PHP syntax..."
PHP_ERRORS=$(find app -name "*.php" -exec php -l {} \; 2>&1 | grep -c "Parse error" || echo "0")
if [ "$PHP_ERRORS" -eq "0" ]; then
    print_success "No PHP syntax errors"
else
    print_error "Found $PHP_ERRORS PHP syntax errors"
    ((ERRORS++))
fi

# Memory and PHP configuration
print_info "Checking PHP configuration..."
PHP_VERSION=$(php -r 'echo PHP_VERSION;')
MEMORY_LIMIT=$(php -r 'echo ini_get("memory_limit");')
print_info "PHP Version: $PHP_VERSION"
print_info "Memory Limit: $MEMORY_LIMIT"

# Check required PHP extensions
print_info "Checking required PHP extensions..."
REQUIRED_EXTENSIONS=("pdo" "pdo_mysql" "mbstring" "openssl" "json" "curl" "redis")
for ext in "${REQUIRED_EXTENSIONS[@]}"; do
    if php -m | grep -q "^$ext$"; then
        print_success "$ext extension loaded"
    else
        print_warning "$ext extension not found"
    fi
done

echo ""
echo "=========================================="
if [ "$ERRORS" -eq "0" ]; then
    print_success "VERIFICATION PASSED - No critical errors!"
else
    print_error "VERIFICATION FAILED - Found $ERRORS critical errors!"
fi
echo "=========================================="
echo ""

exit $ERRORS

