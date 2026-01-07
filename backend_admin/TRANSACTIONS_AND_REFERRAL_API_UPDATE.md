# Transactions & Referral API Updates

## Date: November 4, 2025

This document summarizes the updates made to the Transactions and Referral APIs based on the mobile app UI requirements.

---

## 1. Transaction History API Enhancement

### Updated Endpoint
```
GET /api/v1/wallet/transactions?page=1&limit=20&type=CALL_SPENT
```

### Key Features Added

#### 1.1 Enhanced Transaction Response
- **Detailed Call Information**: For `CALL_SPENT` transactions, the API now includes:
  - Call type (AUDIO/VIDEO)
  - Call duration (formatted and in seconds)
  - Partner details (name, profile image, gender)
  - User-friendly title (e.g., "Audio session with Anushrma09")

#### 1.2 New Query Parameters
- `type` (optional): Filter transactions by type (PURCHASE, CALL_SPENT, WITHDRAWAL)

#### 1.3 User-Friendly Fields
- `title`: Human-readable transaction description
- `icon_type`: Icon indicator (wallet, audio, video, withdrawal, transaction)
- `date`: Formatted date (e.g., "Nov 04")
- `time`: Formatted time (e.g., "14:30 PM")
- `is_credit`: Boolean indicating credit/debit
- `coins`: Always positive value (use `is_credit` for direction)

### Example Response
```json
{
  "success": true,
  "transactions": [
    {
      "id": "TXN_840",
      "type": "CALL_SPENT",
      "coins": 840,
      "is_credit": false,
      "status": "SUCCESS",
      "created_at": "2024-11-04T14:30:00Z",
      "date": "Nov 04",
      "time": "14:30 PM",
      "title": "Video session with Anushrma09",
      "icon_type": "video",
      "call": {
        "id": "CALL_1234567890",
        "type": "VIDEO",
        "duration": 800,
        "duration_formatted": "13 min 20 sec",
        "partner": {
          "id": "USR_1234567890",
          "name": "Anushrma09",
          "profile_image": "https://cdn.onlycare.app/profiles/...",
          "gender": "FEMALE"
        }
      }
    }
  ]
}
```

### Model Updates
- **Transaction.php**: Added relationships for calls and polymorphic references
- **WalletController.php**: Enhanced with call details, duration formatting, and partner information

---

## 2. Referral API Enhancement (Share & Get Coins)

### Updated Endpoints

#### 2.1 Get Referral Code & Statistics
```
GET /api/v1/referral/code
```

**Response includes:**
- `referral_code`: 8-character unique code (e.g., CTLA8241)
- `my_invites`: Total successful referrals count
- `per_invite_coins`: Bonus per referral (default: 10)
- `total_coins_earned`: Total coins from all referrals
- `share_message`: Ready-to-share text
- `whatsapp_share_url`: Direct WhatsApp share link

**Example Response:**
```json
{
  "success": true,
  "referral_code": "CTLA8241",
  "referral_url": "https://onlycare.app/invite/CTLA8241",
  "my_invites": 0,
  "per_invite_coins": 10,
  "total_coins_earned": 0,
  "share_message": "Join me on Only Care! Use my referral code CTLA8241 and we both get 10 coins! Download now: https://onlycare.app/invite/CTLA8241",
  "whatsapp_share_url": "https://wa.me/?text=Join%20me%20on%20Only%20Care..."
}
```

#### 2.2 Apply Referral Code
```
POST /api/v1/referral/apply
```

**Request:**
```json
{
  "referral_code": "CTLA8241"
}
```

**Success Response:**
```json
{
  "success": true,
  "message": "Referral code applied successfully! You received 10 coins",
  "bonus_coins": 10,
  "referrer_bonus": 10,
  "new_balance": 110
}
```

**Business Rules:**
- Each user can only use ONE referral code (first-time only)
- Cannot use your own referral code
- Both referrer and referee receive coins instantly
- Coins are added to wallet balance immediately

#### 2.3 Get Referral History
```
GET /api/v1/referral/history?page=1&limit=20
```

**Features:**
- Paginated list of referred users
- Shows user details, coins earned, and claim status
- Formatted dates for better UX

**Example Response:**
```json
{
  "success": true,
  "referrals": [
    {
      "id": "REF_1234567890",
      "referred_user": {
        "id": "USR_9876543210",
        "name": "Rahul Sharma",
        "profile_image": "https://cdn.onlycare.app/profiles/..."
      },
      "bonus_coins": 10,
      "is_claimed": true,
      "created_at": "2024-11-03T10:30:00Z",
      "created_at_formatted": "Nov 03, 2024",
      "claimed_at": "2024-11-03T10:30:00Z"
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 1,
    "total_items": 2,
    "per_page": 20
  }
}
```

### Controller Updates
- **ReferralController.php**: 
  - Fixed model field inconsistencies (referred_id → referred_user_id)
  - Enhanced response with WhatsApp integration
  - Added shareable message generation
  - Updated referral code format to match UI (4 letters + 4 numbers)
  - Added pagination to history endpoint

---

## 3. Documentation Updates

### 3.1 Web-Based API Documentation
- **File**: `resources/views/api-docs/index-dark.blade.php`
- **Changes**:
  - Added "Referral & Rewards" section to sidebar
  - Created documentation pages for all 3 referral endpoints
  - Added comprehensive code examples with cURL
  - Updated transaction history response examples
  - Added type parameter documentation

### 3.2 Markdown API Documentation
- **File**: `API_DOCUMENTATION.md`
- **Changes**:
  - Enhanced "Transaction History" section with detailed examples
  - Updated "Referral APIs (Share & Get Coins)" section
  - Added field explanations and business rules
  - Included error response documentation

---

## 4. Files Modified

### Backend Controllers
1. `/app/Http/Controllers/Api/WalletController.php` - Enhanced transaction history API
2. `/app/Http/Controllers/Api/ReferralController.php` - Updated referral endpoints

### Models
1. `/app/Models/Transaction.php` - Added call relationship and polymorphic reference support

### Documentation
1. `/resources/views/api-docs/index-dark.blade.php` - Web-based API docs with new sections
2. `/API_DOCUMENTATION.md` - Updated markdown documentation

---

## 5. Mobile App Integration

### Transaction Screen
The enhanced API now provides all data needed for the mobile "Transactions" screen:
- Audio/Video call sessions with duration and partner info
- Wallet recharges with payment details
- Proper icons and formatting
- Negative coins displayed in red with minus sign
- Positive coins displayed in green with plus sign

### Share & Get Coins Screen
The referral API provides complete data for the referral UI:
- My Invites count (0 initially)
- Per Invite coins (10)
- Total Coins Earned (0 initially)
- Referral code (CTLA8241 format)
- WhatsApp share integration
- Shareable link and message

---

## 6. Testing the APIs

### Test Transaction History
```bash
curl -X GET "http://localhost/api/v1/wallet/transactions?limit=20&type=CALL_SPENT" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test Get Referral Code
```bash
curl -X GET "http://localhost/api/v1/referral/code" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test Apply Referral Code
```bash
curl -X POST "http://localhost/api/v1/referral/apply" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"referral_code": "CTLA8241"}'
```

### Test Get Referral History
```bash
curl -X GET "http://localhost/api/v1/referral/history?page=1&limit=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 7. Next Steps

1. **Test the APIs** using Postman or the web-based documentation at `/api-docs`
2. **Integrate with Mobile App** using the enhanced response structures
3. **Monitor Performance** especially for transaction history with call data
4. **Configure Settings** for referral bonus amounts in the admin panel

---

## Summary

✅ **Transaction History API** - Now includes detailed call session information with partner details and formatted durations

✅ **Referral System API** - Complete "Share & Get Coins" functionality with WhatsApp integration

✅ **Documentation** - Both web-based and markdown documentation fully updated with examples

✅ **Mobile-Ready** - All responses formatted to match the mobile app UI requirements

The APIs are now ready for mobile app integration and testing!







