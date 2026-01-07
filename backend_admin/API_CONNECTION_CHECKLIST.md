# API Connection Checklist & Troubleshooting Guide

## ✅ Issues Fixed

### 1. **Transactions API** ✅ FIXED
- **Issue:** Response key mismatch (`transactions` vs `data`)
- **Fix:** Updated `WalletController.php` to return `data` key
- **Status:** ✅ Ready to test

### 2. **Earnings Dashboard API** ✅ FIXED
- **Issue:** Missing required fields in response
- **Fix:** Added `today_calls`, `audio_calls_count`, `video_calls_count`, `average_call_duration`, `average_earnings_per_call`
- **Status:** ✅ Ready to test

### 3. **FemaleHomeScreen** ✅ INTEGRATED
- **Issue:** Using hardcoded data
- **Fix:** Fully integrated with API (coin balance, call availability, today's stats)
- **Status:** ✅ Ready to test

---

## 🔧 Backend Server Status

### Check if Server is Running:
```powershell
Get-Process | Where-Object { $_.ProcessName -like "*php*" }
```

### Start Laravel Server:
```bash
cd C:\xampp\htdocs\only_care\onlycare_admin
C:\xampp\php\php.exe artisan serve --host=0.0.0.0 --port=8000
```

**Expected Output:**
```
Laravel development server started: http://0.0.0.0:8000
```

### Verify Server is Accessible:
```powershell
Invoke-WebRequest -Uri "http://localhost:8000" -Method GET
```

---

## 📱 Android App Configuration

### Check API Base URL

**File:** `onlycare_app/app/src/main/java/com/onlycare/app/data/remote/api/ApiConfig.kt`

**Should be:**
```kotlin
const val BASE_URL = "http://YOUR_IP_ADDRESS:8000/api/"
```

**Examples:**
- Local: `http://192.168.1.100:8000/api/`
- Emulator accessing host: `http://10.0.2.2:8000/api/`

### Find Your IP Address:
```powershell
# Windows
ipconfig | Select-String -Pattern "IPv4"

# Output example:
# IPv4 Address. . . . . . . . . . . : 192.168.1.100
```

---

## 🧪 Testing API Endpoints

### 1. Test Earnings Dashboard (Female Users Only)

**Endpoint:** `GET /api/earnings/dashboard`

**Test with PowerShell:**
```powershell
$token = "YOUR_AUTH_TOKEN_HERE"
$headers = @{
    "Authorization" = "Bearer $token"
    "Accept" = "application/json"
}
Invoke-RestMethod -Uri "http://localhost:8000/api/earnings/dashboard" -Headers $headers -Method GET
```

**Expected Response:**
```json
{
  "success": true,
  "dashboard": {
    "total_earnings": 5430.50,
    "today_earnings": 150.00,
    "week_earnings": 890.50,
    "month_earnings": 3200.75,
    "available_balance": 5430.50,
    "pending_withdrawals": 0.00,
    "total_withdrawals": 0.00,
    "total_calls": 47,
    "today_calls": 3,
    "audio_calls_count": 32,
    "video_calls_count": 15,
    "average_call_duration": 420,
    "average_earnings_per_call": 115.54,
    "total_duration": 19740
  }
}
```

### 2. Test Transactions

**Endpoint:** `GET /api/wallet/transactions`

**Test with PowerShell:**
```powershell
$token = "YOUR_AUTH_TOKEN_HERE"
$headers = @{
    "Authorization" = "Bearer $token"
    "Accept" = "application/json"
}
Invoke-RestMethod -Uri "http://localhost:8000/api/wallet/transactions" -Headers $headers -Method GET
```

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "TXN_1",
      "type": "PURCHASE",
      "coins": 100,
      "is_credit": true,
      "amount": 99.00,
      "status": "SUCCESS",
      "description": "Purchased 100 coins",
      "payment_method": "PhonePe",
      "created_at": "2025-01-17T10:30:00Z"
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 1,
    "total_items": 1,
    "per_page": 20
  }
}
```

### 3. Test Current User

**Endpoint:** `GET /api/users/me`

**Test with PowerShell:**
```powershell
$token = "YOUR_AUTH_TOKEN_HERE"
$headers = @{
    "Authorization" = "Bearer $token"
    "Accept" = "application/json"
}
Invoke-RestMethod -Uri "http://localhost:8000/api/users/me" -Headers $headers -Method GET
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": "USR_123",
    "name": "Jane Doe",
    "username": "jane123",
    "age": 25,
    "gender": "FEMALE",
    "profile_image": "👩",
    "coin_balance": 5430,
    "audio_call_enabled": true,
    "video_call_enabled": true,
    "is_verified": true,
    "total_ratings": 120,
    "rating": 4.5
  }
}
```

### 4. Test Call Availability Update (Female Only)

**Endpoint:** `POST /api/users/me/call-availability`

**Test with PowerShell:**
```powershell
$token = "YOUR_AUTH_TOKEN_HERE"
$headers = @{
    "Authorization" = "Bearer $token"
    "Accept" = "application/json"
    "Content-Type" = "application/json"
}
$body = @{
    "audio_call_enabled" = $true
    "video_call_enabled" = $false
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8000/api/users/me/call-availability" -Headers $headers -Method POST -Body $body
```

**Expected Response:**
```json
{
  "success": true,
  "data": "Call availability updated",
  "message": "Call availability updated"
}
```

---

## 🐛 Common Issues & Solutions

### Issue 1: "Failed to get earnings dashboard"

**Causes:**
1. ❌ Laravel server not running
2. ❌ Wrong IP address in Android app
3. ❌ User is not FEMALE type
4. ❌ Authentication token expired

**Solutions:**
1. ✅ Start Laravel server: `php artisan serve --host=0.0.0.0 --port=8000`
2. ✅ Update `BASE_URL` in `ApiConfig.kt` with correct IP
3. ✅ Login as FEMALE user
4. ✅ Re-login to get fresh token

### Issue 2: "Failed to get transactions"

**Causes:**
1. ❌ Backend response format mismatch
2. ❌ No transactions in database

**Solutions:**
1. ✅ Backend now returns correct `data` key (fixed)
2. ✅ Empty transactions will show "No Transactions" message

### Issue 3: Network timeout or connection refused

**Causes:**
1. ❌ Firewall blocking port 8000
2. ❌ Android app on different network
3. ❌ Emulator can't reach host

**Solutions:**
1. ✅ Allow port 8000 in Windows Firewall
2. ✅ Ensure phone and PC on same WiFi network
3. ✅ Use `10.0.2.2` for emulator accessing host machine

### Issue 4: 401 Unauthorized

**Causes:**
1. ❌ No authentication token
2. ❌ Expired token
3. ❌ Invalid token

**Solutions:**
1. ✅ Re-login in the app
2. ✅ Check token in SessionManager
3. ✅ Verify token in Laravel: `php artisan sanctum:prune-expired`

### Issue 5: 403 Forbidden (Earnings Dashboard)

**Cause:**
❌ User type is MALE (only FEMALE users can access earnings)

**Solution:**
✅ Login with a FEMALE user account

---

## 📊 Database Requirements

### Verify Required Tables Exist:

```sql
-- Check users table
SHOW COLUMNS FROM users;

-- Check calls table
SHOW COLUMNS FROM calls;

-- Check transactions table
SHOW COLUMNS FROM transactions;
```

### Required Columns:

#### Users Table:
- `id` (primary key)
- `name`
- `username`
- `user_type` (MALE/FEMALE)
- `coin_balance` (integer)
- `total_earnings` (integer/decimal)
- `audio_call_enabled` (boolean)
- `video_call_enabled` (boolean)
- `total_ratings` (integer)
- `rating` (decimal)

#### Calls Table:
- `id` (primary key)
- `receiver_id` (foreign key to users)
- `call_type` (AUDIO/VIDEO)
- `status` (ENDED for completed calls)
- `duration` (integer, seconds)
- `coins_earned` (integer)
- `created_at` (timestamp)

#### Transactions Table:
- `id` (primary key)
- `user_id` (foreign key to users)
- `type` (PURCHASE, CALL_SPENT, WITHDRAWAL, etc.)
- `coins` (integer, positive/negative)
- `amount` (decimal, nullable)
- `status` (PENDING, SUCCESS, FAILED)
- `created_at` (timestamp)

---

## 🔍 Android Logcat Monitoring

### Filter by Tags:
```
FemaleHomeViewModel
ApiDataRepository
EarningsViewModel
TransactionsViewModel
ProfileViewModel
```

### Look For:
- ✅ "Call availability updated successfully"
- ✅ "Earnings dashboard loaded"
- ❌ "Failed to load earnings"
- ❌ "Network error"
- ❌ "HTTP 401/403/404/500"

---

## ✅ Final Checklist

### Backend:
- [ ] Laravel server is running on port 8000
- [ ] Database migrations are up to date
- [ ] At least one FEMALE user exists in database
- [ ] Test endpoints return correct responses

### Android App:
- [ ] `BASE_URL` configured with correct IP
- [ ] App compiled successfully (no build errors)
- [ ] Logged in as FEMALE user
- [ ] Network permissions granted

### Testing:
- [ ] FemaleHomeScreen loads without errors
- [ ] Earnings dashboard displays (or shows 0 if no data)
- [ ] Call availability toggles work
- [ ] Transactions page loads (or shows empty state)
- [ ] Profile screen shows real-time data

---

## 🚀 Quick Start Commands

### 1. Start Backend:
```bash
cd C:\xampp\htdocs\only_care\onlycare_admin
C:\xampp\php\php.exe artisan serve --host=0.0.0.0 --port=8000
```

### 2. Build Android App:
```bash
cd C:\xampp\htdocs\only_care\onlycare_app
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

### 3. Install on Device:
```bash
.\gradlew.bat installDebug
```

### 4. Monitor Logs:
```bash
adb logcat -s FemaleHomeViewModel:D ApiDataRepository:D
```

---

## 📞 Support

If errors persist after following this checklist:

1. **Check Laravel logs:**
   ```
   C:\xampp\htdocs\only_care\onlycare_admin\storage\logs\laravel.log
   ```

2. **Check Android Logcat** for detailed error messages

3. **Verify database** has required tables and data

4. **Test endpoints** manually with Postman or PowerShell

---

**All API endpoints are now fixed and ready to use!** 🎉

