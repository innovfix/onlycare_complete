#!/bin/bash

# OnlyCare WebSocket Server Installation Script
# Run this script on your DigitalOcean server

set -e

echo "🚀 OnlyCare WebSocket Server Installation"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}❌ Please run as root (use sudo)${NC}"
    exit 1
fi

echo -e "${YELLOW}Step 1: Installing Node.js 18.x...${NC}"
# Install Node.js 18.x
if ! command -v node &> /dev/null; then
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
    echo -e "${GREEN}✅ Node.js installed${NC}"
else
    NODE_VERSION=$(node -v)
    echo -e "${GREEN}✅ Node.js already installed: $NODE_VERSION${NC}"
fi

echo ""
echo -e "${YELLOW}Step 2: Installing PM2...${NC}"
# Install PM2
if ! command -v pm2 &> /dev/null; then
    npm install -g pm2
    echo -e "${GREEN}✅ PM2 installed${NC}"
else
    PM2_VERSION=$(pm2 -v)
    echo -e "${GREEN}✅ PM2 already installed: $PM2_VERSION${NC}"
fi

echo ""
echo -e "${YELLOW}Step 3: Creating directory structure...${NC}"
# Create directory
mkdir -p /var/www/onlycare-socket
cd /var/www/onlycare-socket

echo ""
echo -e "${YELLOW}Step 4: Installing Node.js dependencies...${NC}"
# Copy files (assuming they're in the same directory as this script)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -f "$SCRIPT_DIR/package.json" ]; then
    cp "$SCRIPT_DIR/package.json" .
    cp "$SCRIPT_DIR/server.js" .
    cp "$SCRIPT_DIR/.env.example" .
    cp "$SCRIPT_DIR/.gitignore" .
    
    # Install dependencies
    npm install
    echo -e "${GREEN}✅ Dependencies installed${NC}"
else
    echo -e "${RED}❌ package.json not found. Please ensure all files are in the same directory.${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}Step 5: Setting up environment file...${NC}"
if [ ! -f .env ]; then
    cp .env.example .env
    echo -e "${YELLOW}⚠️  Please edit .env file and configure:${NC}"
    echo "   - LARAVEL_API_URL"
    echo "   - LARAVEL_API_SECRET"
    echo ""
    read -p "Press Enter to edit .env file now..."
    nano .env
else
    echo -e "${GREEN}✅ .env file already exists${NC}"
fi

echo ""
echo -e "${YELLOW}Step 6: Starting server with PM2...${NC}"
# Start with PM2
pm2 start server.js --name onlycare-socket
pm2 save
pm2 startup

echo ""
echo -e "${GREEN}✅ Installation complete!${NC}"
echo ""
echo "=========================================="
echo "📊 Server Status:"
pm2 status
echo ""
echo "🔍 Test health endpoint:"
echo "   curl http://localhost:3001/health"
echo ""
echo "📝 View logs:"
echo "   pm2 logs onlycare-socket"
echo ""
echo "🔄 Restart server:"
echo "   pm2 restart onlycare-socket"
echo ""
echo "⚠️  Next steps:"
echo "   1. Configure Nginx proxy (see WEBSOCKET_INTEGRATION_GUIDE.md)"
echo "   2. Setup SSL certificate"
echo "   3. Update Laravel .env with WEBSOCKET_URL"
echo "   4. Test with Android app"
echo ""
echo "=========================================="









