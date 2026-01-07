# Quick Start: Python Voice Gender Detection API

## 🚀 How to Run the API

### Step 1: Navigate to Directory
```bash
cd /var/www/onlycare_admin/voice-gender-detection-test
```

### Step 2: Install Dependencies (First Time Only)
```bash
# Create virtual environment
python3 -m venv venv

# Activate it
source venv/bin/activate

# Install packages (this will take 2-5 minutes first time)
pip install -r requirements.txt
```

### Step 3: Start the API
```bash
# Make sure you're in the directory
cd /var/www/onlycare_admin/voice-gender-detection-test

# Activate virtual environment
source venv/bin/activate

# Start the service
python3 api_service.py
```

**OR use the start script:**
```bash
cd /var/www/onlycare_admin/voice-gender-detection-test
./START_SERVICE.sh
```

## ✅ How to Check if API is Working

### Method 1: Using PHP Test Script (From Laravel Root)
```bash
cd /var/www/onlycare_admin
php test_python_api.php
```

### Method 2: Using Bash Test Script
```bash
cd /var/www/onlycare_admin/voice-gender-detection-test
./test_api.sh
```

### Method 3: Using curl
```bash
curl http://localhost:5002/health
```

**Expected Response:**
```json
{
  "status": "healthy",
  "service": "Voice Gender Detection API",
  "model": "Hugging Face Wav2Vec2",
  "accuracy": "98.46%",
  "detector_loaded": true
}
```

## 🔍 Check if Port 5002 is in Use

```bash
# Check if port is listening
ss -tuln | grep 5002
# or
lsof -i :5002
```

## 📝 Run in Background

```bash
# Using nohup
cd /var/www/onlycare_admin/voice-gender-detection-test
nohup python3 api_service.py > api_service.log 2>&1 &

# Check if running
ps aux | grep api_service.py
```

## 🛑 Stop the API

```bash
# Find the process
ps aux | grep api_service.py

# Kill it
kill <PID>
# or
pkill -f api_service.py
```

## 📊 View Logs

```bash
tail -f /var/www/onlycare_admin/voice-gender-detection-test/api_service.log
```

## ⚠️ Troubleshooting

**If API doesn't start:**
1. Check Python version: `python3 --version` (needs 3.10+)
2. Check if dependencies installed: `pip list | grep transformers`
3. Check logs: `cat api_service.log`

**If port 5002 is busy:**
```bash
# Find what's using it
lsof -i :5002
# Kill it if needed
kill -9 <PID>
```

**If model download fails:**
- Check internet connection
- Model will auto-download on first run (takes 2-5 minutes)
- Check disk space: `df -h`
