#!/bin/bash

# ==================================================
# ONLYCARE ADMIN - DATABASE SETUP SCRIPT
# ==================================================
# This script sets up the database for production

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

echo "=========================================="
echo "  DATABASE SETUP & CONFIGURATION"
echo "=========================================="
echo ""

# Load environment variables
if [ -f ".env" ]; then
    export $(cat .env | grep -v '^#' | xargs)
    print_success "Environment variables loaded"
else
    print_error ".env file not found!"
    exit 1
fi

# Database credentials
DB_NAME="${DB_DATABASE:-onlycare_production}"
DB_USER="${DB_USERNAME:-root}"
DB_PASS="${DB_PASSWORD}"
DB_HOST="${DB_HOST:-127.0.0.1}"

print_info "Database Configuration:"
echo "  Host: $DB_HOST"
echo "  Database: $DB_NAME"
echo "  User: $DB_USER"
echo ""

# Ask for MySQL root password
read -sp "Enter MySQL root password: " MYSQL_ROOT_PASS
echo ""

# Test MySQL connection
print_info "Testing MySQL connection..."
if mysql -h"$DB_HOST" -uroot -p"$MYSQL_ROOT_PASS" -e "SELECT 1;" &> /dev/null; then
    print_success "MySQL connection successful"
else
    print_error "Failed to connect to MySQL!"
    exit 1
fi

# Create database if not exists
print_info "Creating database if not exists..."
mysql -h"$DB_HOST" -uroot -p"$MYSQL_ROOT_PASS" <<MYSQL_SCRIPT
CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
MYSQL_SCRIPT
print_success "Database '$DB_NAME' ready"

# Create/Update database user
print_info "Setting up database user..."
mysql -h"$DB_HOST" -uroot -p"$MYSQL_ROOT_PASS" <<MYSQL_SCRIPT
CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASS';
GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'localhost';
CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASS';
GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'%';
FLUSH PRIVILEGES;
MYSQL_SCRIPT
print_success "Database user configured"

# Verify database connection with app user
print_info "Verifying application database connection..."
if mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" -e "USE $DB_NAME; SELECT 1;" &> /dev/null; then
    print_success "Application can connect to database"
else
    print_error "Application cannot connect to database!"
    exit 1
fi

# Run migrations
print_info "Running database migrations..."
php artisan migrate --force
print_success "Migrations completed"

# Ask if user wants to seed
read -p "Do you want to seed the database with initial data? [y/N]: " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_info "Seeding database..."
    
    # Seed admin user
    php artisan db:seed --class=AdminSeeder --force
    print_success "Admin user created"
    
    # Seed sample data
    read -p "Do you want to seed sample/demo data? [y/N]: " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        php artisan db:seed --class=FemaleCreatorsSeeder --force
        php artisan db:seed --class=SampleDataSeeder --force
        print_success "Sample data seeded"
    fi
fi

# Display database info
echo ""
echo "=========================================="
print_success "DATABASE SETUP COMPLETED!"
echo "=========================================="
echo ""
print_info "Database Information:"
echo "  Database Name: $DB_NAME"
echo "  Tables Created: $(mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" -D"$DB_NAME" -e "SHOW TABLES;" | wc -l) tables"
echo ""

# Get admin credentials
print_info "Admin Credentials:"
echo "  Email: admin@example.com (check AdminSeeder for exact email)"
echo "  Password: admin123 (CHANGE THIS IMMEDIATELY)"
echo ""
print_warning "IMPORTANT: Change the admin password after first login!"
echo ""

