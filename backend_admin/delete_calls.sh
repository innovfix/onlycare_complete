#!/bin/bash

# Script to delete user calls from admin panel
# Usage: ./delete_calls.sh

echo ""
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║    DELETE USER CALLS - OnlyCare Admin Panel              ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# Change to script directory
cd "$(dirname "$0")"

# Check if PHP is installed
if ! command -v php &> /dev/null; then
    echo "❌ Error: PHP is not installed or not in PATH"
    echo "   Please install PHP to run this script"
    exit 1
fi

# Check if vendor directory exists (Composer dependencies)
if [ ! -d "vendor" ]; then
    echo "⚠️  Warning: vendor/ directory not found"
    echo "   Running composer install..."
    composer install
    if [ $? -ne 0 ]; then
        echo "❌ Failed to install dependencies"
        exit 1
    fi
fi

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "❌ Error: .env file not found"
    echo "   Please create .env file with database credentials"
    exit 1
fi

echo "🔍 Running deletion script for user with phone: 6203224780"
echo ""

# Run the PHP script
php delete_user_calls_6203224780.php

# Check exit status
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Script completed successfully!"
else
    echo ""
    echo "❌ Script encountered an error"
    exit 1
fi

echo ""
