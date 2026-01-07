# Registration Flow - Quick Reference

## 📋 Summary

| User Type | Gender | Required Fields | Auto-Generated |
|-----------|--------|----------------|----------------|
| **USER** | MALE | • gender<br>• avatar<br>• language | name: `User_XXXX` |
| **CREATOR** | FEMALE | • gender<br>• avatar<br>• age (18-100)<br>• interests (1-4)<br>• description (10-500 chars)<br>• language | name: `Creator_XXXX` |

---

## 🚀 API Endpoint

```
POST /api/v1/auth/register
Authorization: Bearer {token_from_otp_verification}
Content-Type: application/json
```

---

## 📤 Request Examples

### USER (Male)
```json
{
  "phone": "9876543210",
  "gender": "MALE",
  "avatar": "base64_or_url",
  "language": "HINDI"
}
```

### CREATOR (Female)
```json
{
  "phone": "9876543210",
  "gender": "FEMALE",
  "avatar": "base64_or_url",
  "age": 25,
  "interests": ["Music", "Travel", "Movies", "Dancing"],
  "description": "Professional dancer and content creator.",
  "language": "HINDI"
}
```

---

## ✅ Success Response
```json
{
  "success": true,
  "message": "Registration successful",
  "user": { ... }
}
```

---

## ❌ Common Errors

### Missing Creator Fields
```json
{
  "success": false,
  "message": "Validation error",
  "errors": {
    "age": ["Age is required for creators"],
    "interests": ["At least 1 interest is required"],
    "description": ["Description is required for creators"]
  }
}
```

### Invalid Interests Count
```json
{
  "errors": {
    "interests": ["Maximum 4 interests are allowed"]
  }
}
```

### Invalid Age
```json
{
  "errors": {
    "age": ["Age must be at least 18"]
  }
}
```

---

## 🎯 Key Differences

### USER (Male) ✓
- ✅ Quick registration (3 fields)
- ✅ No age verification
- ✅ No interests required
- ✅ No bio/description
- 🎯 Focus: Browse and call creators

### CREATOR (Female) ✓
- ✅ Detailed profile (7 fields)
- ✅ Age verification (18+)
- ✅ Interests showcase (1-4 tags)
- ✅ Bio/description required
- 🎯 Focus: Attract users, earn money

---

## 🔄 Complete Flow

```
1. Send OTP → Phone verification
2. Verify OTP → Get access token
3. Register → Choose gender
   ├─ MALE → Simple form
   └─ FEMALE → Detailed form
4. Success → User profile created
```

---

## 📱 Frontend Implementation

```javascript
// Example registration function
async function register(userData) {
  const endpoint = '/api/v1/auth/register';
  
  // Prepare payload based on gender
  const payload = {
    phone: userData.phone,
    gender: userData.gender,
    avatar: userData.avatar,
    language: userData.language
  };
  
  // Add creator-specific fields if FEMALE
  if (userData.gender === 'FEMALE') {
    payload.age = userData.age;
    payload.interests = userData.interests; // Array with 1-4 items
    payload.description = userData.description; // 10-500 chars
  }
  
  const response = await fetch(endpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`
    },
    body: JSON.stringify(payload)
  });
  
  return response.json();
}
```

---

## 🧪 Test Cases

### ✅ Valid USER Registration
```json
{
  "phone": "9876543210",
  "gender": "MALE",
  "avatar": "https://example.com/avatar.jpg",
  "language": "HINDI"
}
```
**Expected:** ✅ Success

### ✅ Valid CREATOR Registration
```json
{
  "phone": "9876543211",
  "gender": "FEMALE",
  "avatar": "https://example.com/avatar2.jpg",
  "age": 24,
  "interests": ["Music", "Dancing"],
  "description": "Professional dancer specializing in Bollywood and contemporary styles.",
  "language": "TAMIL"
}
```
**Expected:** ✅ Success

### ❌ Invalid: Creator without age
```json
{
  "phone": "9876543212",
  "gender": "FEMALE",
  "avatar": "url",
  "language": "HINDI"
}
```
**Expected:** ❌ Validation Error

### ❌ Invalid: Too many interests
```json
{
  "phone": "9876543213",
  "gender": "FEMALE",
  "avatar": "url",
  "age": 25,
  "interests": ["Music", "Dance", "Travel", "Movies", "Art"],
  "description": "Multi-talented creator",
  "language": "HINDI"
}
```
**Expected:** ❌ Validation Error (max 4 interests)

---

## 🔧 Implementation Files

| File | Purpose |
|------|---------|
| `app/Http/Controllers/Api/AuthController.php` | Registration logic with conditional validation |
| `resources/views/api-docs/index.blade.php` | API documentation UI |
| `REGISTRATION_FLOW.md` | Detailed documentation |
| `REGISTRATION_QUICK_REF.md` | This quick reference |

---

## 📞 Questions?

Check the full documentation: `REGISTRATION_FLOW.md`
View API docs: `http://localhost/only_care_admin/public/api-docs`







