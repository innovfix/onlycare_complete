# Registration Flow Documentation

## Overview
The Only Care app has **two distinct registration flows** based on user type:

1. **USER Registration** (Male) - Simplified flow for male users
2. **CREATOR Registration** (Female) - Comprehensive flow for female content creators

---

## 🔄 Registration Process

### Step 1: Send OTP
```http
POST /api/v1/auth/send-otp
```

**Request:**
```json
{
  "phone": "9876543210",
  "country_code": "+91"
}
```

### Step 2: Verify OTP
```http
POST /api/v1/auth/verify-otp
```

**Request:**
```json
{
  "phone": "9876543210",
  "otp": "123456",
  "otp_id": "OTP_1234567890"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "user_exists": false,
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "user": null
}
```

### Step 3: Complete Registration

---

## 👨 USER Registration (Male)

### Required Fields:
- ✅ `gender` (MALE)
- ✅ `avatar` (profile image)
- ✅ `language` (preferred language)

### Optional Fields:
- `phone` (already verified)

### API Endpoint:
```http
POST /api/v1/auth/register
```

### Request Example:
```json
{
  "phone": "9876543210",
  "gender": "MALE",
  "avatar": "base64_encoded_image_or_url",
  "language": "HINDI"
}
```

### What Happens:
- Name is auto-generated: `User_3210` (last 4 digits of phone)
- Age: `null`
- Bio: `null`
- Interests: `null`

### Success Response:
```json
{
  "success": true,
  "message": "Registration successful",
  "user": {
    "id": "USR_1234567890",
    "phone": "9876543210",
    "name": "User_3210",
    "age": null,
    "gender": "MALE",
    "profile_image": "https://cdn.onlycare.app/avatars/...",
    "bio": null,
    "language": "HINDI",
    "interests": [],
    "coin_balance": 0,
    "total_earnings": 0
  }
}
```

---

## 👩 CREATOR Registration (Female)

### Required Fields:
- ✅ `gender` (FEMALE)
- ✅ `avatar` (profile image)
- ✅ `age` (18-100)
- ✅ `interests` (array, min: 1, max: 4)
- ✅ `description` (bio, 10-500 characters)
- ✅ `language` (preferred language)

### Optional Fields:
- `phone` (already verified)

### API Endpoint:
```http
POST /api/v1/auth/register
```

### Request Example:
```json
{
  "phone": "9876543210",
  "gender": "FEMALE",
  "avatar": "base64_encoded_image_or_url",
  "age": 25,
  "interests": ["Music", "Travel", "Movies", "Dancing"],
  "description": "Love music and travel. Professional dancer and content creator.",
  "language": "HINDI"
}
```

### Validation Rules:
- **Age:** Must be between 18-100
- **Interests:** Minimum 1, Maximum 4
- **Description:** Minimum 10 characters, Maximum 500 characters

### What Happens:
- Name is auto-generated: `Creator_3210` (last 4 digits of phone)
- All provided fields are saved

### Success Response:
```json
{
  "success": true,
  "message": "Registration successful",
  "user": {
    "id": "USR_1234567890",
    "phone": "9876543210",
    "name": "Creator_3210",
    "age": 25,
    "gender": "FEMALE",
    "profile_image": "https://cdn.onlycare.app/avatars/...",
    "bio": "Love music and travel. Professional dancer and content creator.",
    "language": "HINDI",
    "interests": ["Music", "Travel", "Movies", "Dancing"],
    "coin_balance": 0,
    "total_earnings": 0,
    "kyc_status": "PENDING"
  }
}
```

---

## ⚠️ Error Responses

### Validation Error (422)
```json
{
  "success": false,
  "message": "Validation error",
  "errors": {
    "age": ["Age is required for creators"],
    "interests": ["At least 1 interest is required"],
    "description": ["Description must be at least 10 characters"]
  }
}
```

### User Not Found (404)
```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "Please verify OTP first"
  }
}
```

---

## 🔍 Comparison Table

| Field | USER (Male) | CREATOR (Female) |
|-------|-------------|------------------|
| **phone** | ✅ Required (verified) | ✅ Required (verified) |
| **gender** | ✅ MALE | ✅ FEMALE |
| **avatar** | ✅ Required | ✅ Required |
| **language** | ✅ Required | ✅ Required |
| **age** | ❌ Not needed | ✅ Required (18-100) |
| **interests** | ❌ Not needed | ✅ Required (1-4) |
| **description** | ❌ Not needed | ✅ Required (10-500 chars) |
| **Auto-generated name** | `User_XXXX` | `Creator_XXXX` |

---

## 📱 Mobile Implementation Notes

### For Frontend Developers:

1. **After OTP verification**, check `user_exists` flag
2. **If false**, show registration screen
3. **Show different forms** based on selected gender:
   - **MALE** → Simple form (avatar + language)
   - **FEMALE** → Detailed form (avatar + age + interests + description + language)
4. **Interests picker** should allow 1-4 selections
5. **Avatar upload** should handle image compression before sending
6. **Language** should be a dropdown with supported languages:
   - Hindi, Tamil, Bengali, Kannada, Malayalam, Telugu

---

## 🧪 Testing

### Test USER Registration:
```bash
curl -X POST http://localhost/only_care_admin/public/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "phone": "9876543210",
    "gender": "MALE",
    "avatar": "test_avatar_url",
    "language": "HINDI"
  }'
```

### Test CREATOR Registration:
```bash
curl -X POST http://localhost/only_care_admin/public/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "phone": "9876543210",
    "gender": "FEMALE",
    "avatar": "test_avatar_url",
    "age": 25,
    "interests": ["Music", "Travel"],
    "description": "Professional content creator",
    "language": "HINDI"
  }'
```

---

## 🔒 Security Notes

1. Phone must be verified via OTP before registration
2. Token is generated after OTP verification
3. Registration endpoint requires authentication token
4. Age verification enforced (minimum 18 years)
5. Input sanitization on all fields

---

## 📝 Database Changes

No new migrations needed. Existing `users` table supports both flows:
- `age`, `bio`, `interests` can be `NULL` for male users
- These fields are required/populated for female creators

---

## 🚀 Next Steps

After registration is complete:
- Users can browse creators and initiate calls
- Creators need to complete KYC for receiving payments
- Creators can set their call rates and availability

---

## 📞 Support

For API issues or questions:
- Check API documentation: `http://localhost/only_care_admin/public/api-docs`
- Review error messages for validation details
- Contact backend team for integration support







