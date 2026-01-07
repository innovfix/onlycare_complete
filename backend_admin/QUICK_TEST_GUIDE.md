# 🚀 Quick Test Guide - Home Screen Creators API

## ✅ What Was Fixed

The **Registration API** now returns an `access_token` in the response!

## 📝 Step-by-Step Testing

### Option 1: Using API Docs Page (Easiest!)

1. **Open API Docs in Browser:**
   ```
   http://localhost/only_care_admin/public/api-docs
   ```

2. **Click "Register" in the left sidebar**

3. **Fill in the form:**
   - Phone: `9876543210`
   - Name: `TestUser`
   - Age: `25`
   - Gender: `MALE`
   - Language: `HINDI`

4. **Click "Send Request"**

5. **Copy the `access_token` from the response** 
   - Look for: `"access_token": "1|xxxxxx..."`
   - Copy the entire token value

6. **Click "Home Screen - Get Creators" in the sidebar**

7. **Paste the token** in the "Access Token" field

8. **Click "Send Request"** to see the creators list! 🎉

---

### Option 2: Using cURL (Direct Testing)

#### Step 1: Register User (Get Token)
```bash
curl -X POST http://localhost/only_care_admin/public/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "9876543210",
    "gender": "MALE",
    "language": "HINDI"
  }'
```

**Response will include:**
```json
{
  "success": true,
  "message": "Registration successful",
  "access_token": "1|abcd1234efgh5678...",
  "user": { ... }
}
```

**→ Copy the `access_token` value!**

#### Step 2: Get Creators List
```bash
curl -X GET "http://localhost/only_care_admin/public/api/v1/users/females?limit=10" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json"
```

**Replace `YOUR_ACCESS_TOKEN_HERE` with the token from Step 1!**

---

## 🎯 Expected Response

You should now see a list of female creators:

```json
{
  "success": true,
  "data": [
    {
      "id": "USR_xxx",
      "name": "Ananya798",
      "age": 24,
      "gender": "FEMALE",
      "profile_image": "...",
      "bio": "D. boss all movies",
      "language": "Kannada",
      "interests": ["Travel", "Movies"],
      "is_online": true,
      "rating": 4.5,
      "audio_call_enabled": true,
      "video_call_enabled": true,
      "audio_call_rate": 10,
      "video_call_rate": 60
    }
  ],
  "pagination": { ... }
}
```

---

## 🔧 Troubleshooting

### Issue: "No female users found" or empty data array

**Solution:** You need to create some female users first!

#### Quick Fix - Create a Female Creator:

```bash
curl -X POST http://localhost/only_care_admin/public/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "9999999999",
    "country_code": "+91"
  }'
```

Then verify OTP (use `123456`):
```bash
curl -X POST http://localhost/only_care_admin/public/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "9999999999",
    "otp": "123456",
    "otp_id": "OTP_FROM_PREVIOUS_RESPONSE"
  }'
```

Then register as FEMALE creator:
```bash
curl -X POST http://localhost/only_care_admin/public/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "9999999999",
    "gender": "FEMALE",
    "age": 24,
    "interests": ["Travel", "Movies", "Music"],
    "description": "Love talking and meeting new people!",
    "language": "Malayalam"
  }'
```

Now your male user will see this creator in the list! 🎉

---

## 🎨 Using the Token in Your App

### React Native / JavaScript
```javascript
const token = "1|abcd1234..."; // Token from registration

const fetchCreators = async () => {
  const response = await fetch(
    'http://localhost/only_care_admin/public/api/v1/users/females?limit=20',
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  
  const data = await response.json();
  console.log(data.data); // Array of creators
};
```

### Store Token Securely
```javascript
// After registration
import AsyncStorage from '@react-native-async-storage/async-storage';

// Save token
await AsyncStorage.setItem('access_token', token);

// Retrieve token
const token = await AsyncStorage.getItem('access_token');
```

---

## ✅ Summary

**Before Fix:** ❌ Registration didn't return token
**After Fix:** ✅ Registration returns `access_token`

**Now you can:**
1. Register user → Get token ✓
2. Use token → Get creators list ✓
3. Build your home screen UI ✓

---

## 🎉 Test Again Now!

Try registering again in the API docs and you'll see the `access_token` in the response!

```
http://localhost/only_care_admin/public/api-docs
```

Click **"Register"** → Fill form → Get token → Use for **"Get Creators"**! 🚀







