# Home Screen Creator Listing API - Testing Guide

## ✅ What Has Been Implemented

### 1. **API Endpoint Enhanced** ✓
- Updated `GET /api/v1/users/females` endpoint
- Now includes `audio_call_rate` and `video_call_rate` in the response
- Call rates are fetched dynamically from app settings

### 2. **API Documentation Added** ✓
- Full documentation added to: `http://localhost/only_care_admin/public/api-docs`
- Interactive testing interface included
- Located under "Users / Creators" section in the sidebar

### 3. **Standalone Documentation** ✓
- Comprehensive markdown documentation created
- File: `API_HOME_SCREEN_DOCUMENTATION.md`
- Includes code examples for React Native, Swift, Kotlin

---

## 🚀 How to Test

### Option 1: Using the Interactive API Docs (Recommended)

1. **Open Your Browser**
   ```
   http://localhost/only_care_admin/public/api-docs
   ```

2. **Get an Access Token First**
   - Click on "Send OTP" in the sidebar
   - Enter a phone number (e.g., `9876543210`)
   - Click "Send Request"
   - Copy the OTP from the response (it will be `123456`)
   
   - Click on "Verify OTP" in the sidebar
   - Enter the phone number and OTP
   - Click "Send Request"
   - **Copy the `access_token` from the response**

3. **Test the Creator Listing API**
   - Click on "Home Screen - Get Creators" in the sidebar
   - Paste your access token in the "Access Token" field
   - Configure filters (optional):
     - Limit: 10
     - Page: 1
     - Language: Malayalam (or any other)
     - Check "Online Only" if you want
   - Click "Send Request"
   - View the response with creator details

### Option 2: Using cURL

#### Step 1: Send OTP
```bash
curl -X POST http://localhost/only_care_admin/public/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "9876543210",
    "country_code": "+91"
  }'
```

#### Step 2: Verify OTP
```bash
curl -X POST http://localhost/only_care_admin/public/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "9876543210",
    "otp": "123456",
    "otp_id": "OTP_ID_FROM_STEP_1"
  }'
```

**Save the access_token from the response!**

#### Step 3: Get Creators (Basic)
```bash
curl -X GET "http://localhost/only_care_admin/public/api/v1/users/females?limit=10&page=1" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json"
```

#### Step 4: Get Creators (With Filters)
```bash
curl -X GET "http://localhost/only_care_admin/public/api/v1/users/females?online=true&language=Malayalam&limit=10" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json"
```

### Option 3: Using Postman

1. **Import the following request:**
   - Method: `GET`
   - URL: `http://localhost/only_care_admin/public/api/v1/users/females`
   - Headers:
     - `Authorization`: `Bearer YOUR_ACCESS_TOKEN`
     - `Content-Type`: `application/json`
   - Query Params (optional):
     - `limit`: 10
     - `page`: 1
     - `online`: true
     - `language`: Malayalam

2. **Send the request and check response**

---

## 📋 Response Format

### Success Response (200 OK)

```json
{
  "success": true,
  "data": [
    {
      "id": "USR_1730736000000",
      "name": "Ananya798",
      "age": 24,
      "gender": "FEMALE",
      "profile_image": "https://cdn.example.com/profiles/ananya.jpg",
      "bio": "D. boss all movies",
      "language": "Kannada",
      "interests": ["Travel", "Movies", "Music"],
      "is_online": true,
      "last_seen": 1730736123456,
      "rating": 4.5,
      "total_ratings": 127,
      "audio_call_enabled": true,
      "video_call_enabled": true,
      "is_verified": true,
      "audio_call_rate": 10,
      "video_call_rate": 60
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 5,
    "total_items": 94,
    "per_page": 20,
    "has_next": true,
    "has_prev": false
  }
}
```

### Key Fields for Home Screen UI

| Field | Description | UI Usage |
|-------|-------------|----------|
| `name` | Creator name | Display as heading |
| `profile_image` | Avatar URL | Display in image view |
| `language` | Language | Show with 📍 icon |
| `interests[0]` | First interest | Show with 🎯 icon |
| `bio` | Description | Show as subtitle |
| `audio_call_rate` | Audio cost | "📞 10/min" button |
| `video_call_rate` | Video cost | "🎥 60/min" button |
| `is_online` | Online status | Show green/gray indicator |
| `is_verified` | KYC status | Show verification badge |

---

## 🔍 Available Filters

| Parameter | Type | Example | Description |
|-----------|------|---------|-------------|
| `limit` | integer | 20 | Items per page (max: 50) |
| `page` | integer | 1 | Page number |
| `online` | boolean | true | Show only online creators |
| `language` | string | Malayalam | Filter by language |
| `verified` | boolean | true | Show only verified creators |

### Supported Languages
- Hindi
- English
- Tamil
- Telugu
- Malayalam
- Kannada
- Bengali
- Marathi

---

## 🎯 Test Scenarios

### Scenario 1: Get All Creators (Default)
```bash
GET /api/v1/users/females
Expected: List of 20 creators (default pagination)
```

### Scenario 2: Get Online Malayalam Creators
```bash
GET /api/v1/users/females?online=true&language=Malayalam
Expected: Only online Malayalam-speaking creators
```

### Scenario 3: Get Verified Creators with Pagination
```bash
GET /api/v1/users/females?verified=true&limit=5&page=2
Expected: Page 2 with 5 verified creators per page
```

### Scenario 4: Test Without Authorization
```bash
GET /api/v1/users/females
(without Bearer token)
Expected: 401 Unauthorized error
```

### Scenario 5: Test with Female User Token
```bash
GET /api/v1/users/females
(with female user token)
Expected: 403 Forbidden error
```

---

## ⚙️ Call Rates Configuration

Call rates are stored in the `app_settings` table and can be modified from the admin panel:

1. **Access Admin Panel**
   ```
   http://localhost/only_care_admin/public/settings
   ```

2. **Update Call Rates**
   - Navigate to Settings
   - Update "Audio Call Rate per Minute"
   - Update "Video Call Rate per Minute"
   - Save changes

3. **Default Rates**
   - Audio: 10 coins/minute
   - Video: 60 coins/minute (based on screenshot)

---

## 🐛 Troubleshooting

### Issue: "Unauthorized" Error
**Solution:** Make sure you're including the Bearer token in the Authorization header
```bash
-H "Authorization: Bearer YOUR_TOKEN"
```

### Issue: "Forbidden - Only male users can access"
**Solution:** Register as a MALE user during the registration step

### Issue: Empty Data Array
**Solution:** 
1. Check if you have female users in the database
2. Try removing filters (online, language, verified)
3. Run the database seeder to create sample data

### Issue: Call Rates Not Showing
**Solution:**
1. Check if app_settings table has records
2. Run: `php artisan db:seed --class=AppSettingsSeeder`
3. Verify rates are set in admin panel

---

## 📚 Additional Documentation

1. **Full API Documentation**: `API_HOME_SCREEN_DOCUMENTATION.md`
2. **Interactive Docs**: `http://localhost/only_care_admin/public/api-docs`
3. **Complete API Reference**: `API_DOCUMENTATION.md`

---

## 🎉 Summary

✅ **API Updated**: Call rates now included in creator listing
✅ **Documentation Added**: Interactive docs at `/api-docs`
✅ **Testing Ready**: Use web interface or cURL
✅ **Filtering Supported**: Language, online status, verification
✅ **Mobile Ready**: Response format matches screenshot requirements

---

## 📝 Notes

- The API automatically excludes blocked users from results
- Only active female users are returned
- Pagination is automatic with `limit` and `page` parameters
- Call rates are consistent across all creators (fetched from settings)
- Requires authentication with valid Bearer token
- Only accessible by MALE user accounts

---

**Need Help?**
- Check the interactive documentation at `/api-docs`
- Read the full documentation in `API_HOME_SCREEN_DOCUMENTATION.md`
- Test using the built-in testing form in the API docs interface







