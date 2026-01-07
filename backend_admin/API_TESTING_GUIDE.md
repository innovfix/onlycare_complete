# 🚀 API Testing Interface - Quick Start Guide

## 📍 Access the API Documentation

Open your browser and navigate to:

```
http://localhost/only_care_admin/public/api-docs
```

Or if you're using a different domain:

```
https://yourdomain.com/api-docs
```

---

## ✨ Features

### 1. **Interactive Testing Interface**
- Test APIs directly from the browser
- No need for Postman or other tools
- Real-time request/response display

### 2. **Complete Documentation**
- All request parameters with descriptions
- Request/response examples
- Response status codes
- Error handling examples

### 3. **Developer Friendly**
- Copy response with one click
- Pre-filled example values
- Color-coded responses (success/error)
- Expandable/collapsible sections

---

## 🧪 How to Test Login APIs

### Step 1: Send OTP
1. Open the API Documentation page
2. Find "Send OTP" endpoint
3. Click to expand it
4. Click **"Test This API"**
5. Enter phone number (e.g., `9876543210`)
6. Enter country code (e.g., `+91`)
7. Click **"Send Request"** button
8. See the response below (you'll get OTP in debug mode)

### Step 2: Verify OTP
1. Find "Verify OTP" endpoint
2. Click to expand it
3. Enter the same phone number
4. Enter the OTP you received (from previous response)
5. Enter the `otp_id` from previous response
6. Click **"Send Request"**
7. You'll get an `access_token` in response
8. **Save this token** - you'll need it for other APIs!

### Step 3: Complete Registration (If New User)
1. If `user_exists` was `false` in Step 2, complete registration
2. Find "Complete Registration" endpoint
3. Fill in all details (name, age, gender, etc.)
4. Click **"Send Request"**
5. Registration complete!

---

## 📋 What You'll See

### Request Section
- All required and optional parameters
- Parameter types and descriptions
- Pre-filled example values

### Response Section
- Real-time response after clicking "Send Request"
- Status code (200, 400, 422, etc.)
- JSON response formatted beautifully
- Success/Error highlighted with colors

### Examples Section
- Success response example
- Error response examples
- All possible response codes

---

## 🎯 Response Status Codes

| Code | Meaning | Color |
|------|---------|-------|
| 200 | Success | 🟢 Green |
| 400 | Bad Request | 🟡 Yellow |
| 401 | Unauthorized | 🔴 Red |
| 404 | Not Found | 🟡 Yellow |
| 422 | Validation Error | 🟡 Yellow |
| 500 | Server Error | 🔴 Dark Red |

---

## 💡 Tips for Developers

### 1. **Testing Flow**
```
Send OTP → Verify OTP → Complete Registration (if new user)
```

### 2. **Save Important Data**
- Save the `otp_id` from Send OTP response
- Save the `access_token` from Verify OTP response
- You'll need the token for all protected APIs

### 3. **Debug Mode**
In development, the OTP is returned in the response:
```json
{
  "otp": "123456"  // Only in debug mode
}
```

### 4. **Copy Responses**
Click the "Copy" button to copy the entire response to clipboard

---

## 🔐 Using Access Token (For Other APIs)

Once you have the access token from login:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

Use this header for all protected endpoints like:
- `/api/v1/users/me`
- `/api/v1/calls/initiate`
- `/api/v1/wallet/balance`
- etc.

---

## 📱 Example Testing Scenario

### Complete Login Flow Test:

**1. Send OTP:**
```
Phone: 9876543210
Country Code: +91
```

**Response:**
```json
{
  "success": true,
  "otp_id": "OTP_1730880123456",
  "otp": "123456"
}
```

**2. Verify OTP:**
```
Phone: 9876543210
OTP: 123456
OTP ID: OTP_1730880123456
```

**Response:**
```json
{
  "success": true,
  "user_exists": false,
  "access_token": "1|abcd1234..."
}
```

**3. Complete Registration:**
```
Phone: 9876543210
Name: John Doe
Age: 25
Gender: MALE
Language: HINDI
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "user": { ... }
}
```

---

## 🐛 Common Issues & Solutions

### Issue 1: "CORS Error"
**Solution:** Make sure you're accessing from the same domain

### Issue 2: "OTP Expired"
**Solution:** OTP is valid for 10 minutes. Request a new one

### Issue 3: "User Not Found" during registration
**Solution:** Verify OTP first before completing registration

### Issue 4: "Invalid OTP"
**Solution:** Check the OTP from the send-otp response

---

## 📞 Support

If you encounter any issues:
1. Check the browser console for errors
2. Verify your request parameters match the documentation
3. Check response status codes for hints
4. Contact the backend team

---

## 🎉 Next Steps

After confirming the login APIs work:
1. We'll add all other API categories
2. User Management APIs
3. Call Management APIs
4. Wallet & Payment APIs
5. And all 52 endpoints!

---

**Current Status:** ✅ Login/Auth APIs (3 endpoints) - Ready to Test!

**URL:** http://localhost/only_care_admin/public/api-docs







