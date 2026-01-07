# API Documentation - Only Care App

## Base URL
```
Production: https://api.onlycare.app/v1
Development: https://dev-api.onlycare.app/v1
```

## Authentication
All API requests require authentication token in header:
```
Authorization: Bearer {access_token}
```

---

## 1. Authentication APIs

### 1.1 Send OTP
```http
POST /auth/send-otp
```

**Request:**
```json
{
  "phone": "9876543210",
  "country_code": "+91"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent successfully",
  "otp_id": "OTP_1234567890",
  "expires_in": 600
}
```

### 1.2 Verify OTP
```http
POST /auth/verify-otp
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
  "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
  "user": null
}
```

### 1.3 Complete Registration
```http
POST /auth/register
```

**Request:**
```json
{
  "phone": "9876543210",
  "name": "Priya Sharma",
  "age": 24,
  "gender": "FEMALE",
  "language": "HINDI",
  "bio": "Love music and travel",
  "interests": ["Music", "Travel", "Movies"],
  "profile_image": "base64_encoded_image_or_url"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "user": {
    "id": "USR_1234567890",
    "phone": "9876543210",
    "name": "Priya Sharma",
    "age": 24,
    "gender": "FEMALE",
    "profile_image": "https://cdn.onlycare.app/profiles/...",
    "coin_balance": 0,
    "total_earnings": 0,
    "is_verified": false,
    "created_at": "2024-01-20T10:30:00Z"
  }
}
```

### 1.4 Refresh Token
```http
POST /auth/refresh-token
```

**Request:**
```json
{
  "refresh_token": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response:**
```json
{
  "success": true,
  "access_token": "eyJhbGciOiJIUzI1NiIs...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIs..."
}
```

### 1.5 Logout
```http
POST /auth/logout
```

**Response:**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

## 2. User APIs

### 2.1 Get Current User Profile
```http
GET /users/me
```

**Response:**
```json
{
  "success": true,
  "user": {
    "id": "USR_1234567890",
    "phone": "9876543210",
    "name": "Priya Sharma",
    "age": 24,
    "gender": "FEMALE",
    "profile_image": "https://cdn.onlycare.app/profiles/...",
    "bio": "Love music and travel",
    "language": "HINDI",
    "interests": ["Music", "Travel", "Movies"],
    "is_online": true,
    "last_seen": 1705745400000,
    "rating": 4.5,
    "total_ratings": 127,
    "coin_balance": 234,
    "total_earnings": 5430,
    "audio_call_enabled": true,
    "video_call_enabled": true,
    "is_verified": true,
    "kyc_status": "APPROVED",
    "created_at": "2024-01-15T10:30:00Z"
  }
}
```

### 2.2 Update Profile
```http
PUT /users/me
```

**Request:**
```json
{
  "name": "Priya Sharma",
  "age": 25,
  "bio": "Updated bio...",
  "interests": ["Music", "Travel", "Movies", "Food"],
  "profile_image": "base64_or_url"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "user": { /* updated user object */ }
}
```

### 2.3 Get Female Users (Male users only)
```http
GET /users/females?page=1&limit=20&online=true
```

**Query Parameters:**
- `page`: Page number (default: 1)
- `limit`: Items per page (default: 20, max: 50)
- `online`: Filter online users (true/false)
- `verified`: Filter verified users (true/false)
- `language`: Filter by language

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "USR_1234567890",
      "name": "Priya",
      "age": 24,
      "profile_image": "https://...",
      "bio": "Love music and travel",
      "interests": ["Music", "Travel"],
      "is_online": true,
      "rating": 4.5,
      "audio_call_enabled": true,
      "video_call_enabled": true,
      "is_verified": true
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 10,
    "total_items": 200,
    "per_page": 20
  }
}
```

### 2.4 Get User by ID
```http
GET /users/:userId
```

**Response:**
```json
{
  "success": true,
  "user": { /* user object */ }
}
```

### 2.5 Update Online Status
```http
POST /users/me/status
```

**Request:**
```json
{
  "is_online": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "Status updated"
}
```

### 2.6 Update Call Availability (Female only)
```http
POST /users/me/call-availability
```

**Request:**
```json
{
  "audio_call_enabled": true,
  "video_call_enabled": false
}
```

**Response:**
```json
{
  "success": true,
  "message": "Call availability updated"
}
```

### 2.7 Block User
```http
POST /users/:userId/block
```

**Response:**
```json
{
  "success": true,
  "message": "User blocked successfully"
}
```

### 2.8 Unblock User
```http
POST /users/:userId/unblock
```

**Response:**
```json
{
  "success": true,
  "message": "User unblocked successfully"
}
```

### 2.9 Get Blocked Users
```http
GET /users/me/blocked
```

**Response:**
```json
{
  "success": true,
  "blocked_users": [
    {
      "id": "USR_9876543210",
      "name": "Rahul",
      "profile_image": "https://...",
      "blocked_at": "2024-01-20T10:30:00Z"
    }
  ]
}
```

---

## 3. Call APIs

### 3.1 Initiate Call
```http
POST /calls/initiate
```

**Request:**
```json
{
  "receiver_id": "USR_1234567890",
  "call_type": "AUDIO"
}
```

**Response:**
```json
{
  "success": true,
  "call": {
    "id": "CALL_1234567890",
    "caller_id": "USR_9876543210",
    "receiver_id": "USR_1234567890",
    "call_type": "AUDIO",
    "status": "CONNECTING",
    "created_at": "2024-01-20T10:30:00Z",
    "agora_token": "007eJxTYBBa...",
    "channel_name": "call_1234567890"
  },
  "receiver": {
    "name": "Priya",
    "profile_image": "https://..."
  }
}
```

### 3.2 Accept Call
```http
POST /calls/:callId/accept
```

**Response:**
```json
{
  "success": true,
  "call": {
    "id": "CALL_1234567890",
    "status": "ONGOING",
    "started_at": "2024-01-20T10:30:15Z",
    "receiver_joined_at": "2024-01-20T10:30:15Z",
    "agora_token": "007eJxTYBBa...",
    "channel_name": "call_1234567890"
  }
}
```

**Note:** `receiver_joined_at` timestamp marks when receiver actually picks up the call. This is used for accurate billing to exclude ringing time.

### 3.3 Reject Call
```http
POST /calls/:callId/reject
```

**Response:**
```json
{
  "success": true,
  "message": "Call rejected"
}
```

### 3.4 End Call
```http
POST /calls/:callId/end
```

**Request:**
```json
{
  "duration": 312
}
```

**Response:**
```json
{
  "success": true,
  "call": {
    "id": "CALL_1234567890",
    "status": "ENDED",
    "duration": 312,
    "coins_spent": 52,
    "coins_earned": 52,
    "started_at": "2024-01-20T10:30:00Z",
    "receiver_joined_at": "2024-01-20T10:30:15Z",
    "ended_at": "2024-01-20T10:35:27Z"
  },
  "caller_balance": 182,
  "receiver_earnings": 5482
}
```

**Important Notes:**
- **Duration Calculation:** Server calculates duration from `receiver_joined_at` to `ended_at`, NOT from `started_at`
- **Fair Billing:** Users are only charged for actual talk time, excluding ringing time
- **Client Duration:** The duration sent by client is compared with server duration; server duration is used for billing
- **Validation:** If client and server duration differ by more than 30 seconds, it's logged for investigation

### 3.5 Rate Call
```http
POST /calls/:callId/rate
```

**Request:**
```json
{
  "rating": 4,
  "feedback": "Good conversation"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Rating submitted"
}
```

### 3.6 Get Call History
```http
GET /calls/history?page=1&limit=20
```

**Response:**
```json
{
  "success": true,
  "calls": [
    {
      "id": "CALL_1234567890",
      "other_user": {
        "id": "USR_1234567890",
        "name": "Priya",
        "profile_image": "https://..."
      },
      "call_type": "AUDIO",
      "status": "ENDED",
      "duration": 312,
      "coins_spent": 52,
      "coins_earned": 52,
      "rating": 4,
      "created_at": "2024-01-20T10:30:00Z"
    }
  ],
  "pagination": { /* pagination object */ }
}
```

### 3.7 Get Recent Sessions
```http
GET /calls/recent-sessions?page=1&limit=20
```

**Description:**
Get recent call sessions for the current user. Shows all recent calls (ended, rejected, or missed) with detailed information about the other user. This is the main endpoint for the "Recent" tab in the app.

**Query Parameters:**
- `page` (optional): Page number, default: 1
- `limit` (optional): Number of items per page (max 50), default: 20

**Response:**
```json
{
  "success": true,
  "sessions": [
    {
      "id": "CALL_1234567890",
      "user": {
        "id": "USR_1234567890",
        "name": "Anushrma09",
        "username": "anushrma09",
        "age": 24,
        "profile_image": "https://cdn.onlycare.app/profiles/...",
        "rating": 4.5,
        "is_online": true,
        "audio_call_enabled": true,
        "video_call_enabled": true
      },
      "call_type": "AUDIO",
      "status": "ENDED",
      "duration": 180,
      "duration_formatted": "3 min",
      "is_incoming": false,
      "is_outgoing": true,
      "coins_spent": 30,
      "coins_earned": null,
      "rating": 5,
      "created_at": "2024-01-20T21:48:00Z",
      "created_at_formatted": "Yesterday 09:48 PM"
    },
    {
      "id": "CALL_9876543210",
      "user": {
        "id": "USR_1234567890",
        "name": "Anushrma09",
        "username": "anushrma09",
        "age": 24,
        "profile_image": "https://cdn.onlycare.app/profiles/...",
        "rating": 4.5,
        "is_online": true,
        "audio_call_enabled": true,
        "video_call_enabled": true
      },
      "call_type": "VIDEO",
      "status": "ENDED",
      "duration": 900,
      "duration_formatted": "15 min",
      "is_incoming": false,
      "is_outgoing": true,
      "coins_spent": 135,
      "coins_earned": null,
      "rating": null,
      "created_at": "2024-01-20T21:32:00Z",
      "created_at_formatted": "Yesterday 09:32 PM"
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 5,
    "total_items": 98,
    "per_page": 20,
    "has_more": true
  }
}
```

**Key Features:**
- Shows both incoming and outgoing calls
- Includes user online status and call availability
- Provides formatted timestamps (Today/Yesterday)
- Shows duration in human-readable format
- Includes coins spent (for callers) or earned (for receivers)
- Allows quick actions (call/video call buttons) with availability info

**Use Cases:**
- Display recent call activity in the "Recent" tab
- Enable quick re-calling of recent contacts
- Track call history with detailed user information

### 3.8 Get Recent Callers (Female only)
```http
GET /calls/recent-callers?limit=20
```

**Response:**
```json
{
  "success": true,
  "callers": [
    {
      "user": {
        "id": "USR_9876543210",
        "name": "Rahul",
        "age": 28,
        "profile_image": "https://...",
        "rating": 4.2
      },
      "last_call": {
        "call_type": "AUDIO",
        "duration": 180,
        "coins_earned": 30,
        "created_at": "2024-01-20T10:30:00Z"
      },
      "total_calls": 5,
      "total_earnings": 234
    }
  ]
}
```

---

## 4. Wallet & Payment APIs

### 4.1 Get Coin Packages
```http
GET /wallet/packages
```

**Response:**
```json
{
  "success": true,
  "packages": [
    {
      "id": "PKG_1",
      "coins": 100,
      "price": 99.0,
      "original_price": 150.0,
      "discount": 34,
      "is_popular": false,
      "is_best_value": false
    }
  ]
}
```

### 4.2 Initiate Purchase
```http
POST /wallet/purchase
```

**Request:**
```json
{
  "package_id": "PKG_2",
  "payment_method": "PhonePe"
}
```

**Response:**
```json
{
  "success": true,
  "transaction": {
    "id": "TXN_1234567890",
    "package_id": "PKG_2",
    "coins": 500,
    "amount": 399.0,
    "payment_method": "PhonePe",
    "status": "PENDING"
  },
  "payment_gateway_url": "https://payment-gateway.com/...",
  "payment_gateway_data": { /* gateway specific data */ }
}
```

### 4.3 Verify Purchase
```http
POST /wallet/verify-purchase
```

**Request:**
```json
{
  "transaction_id": "TXN_1234567890",
  "payment_gateway_id": "PG_9876543210",
  "status": "SUCCESS"
}
```

**Response:**
```json
{
  "success": true,
  "transaction": {
    "id": "TXN_1234567890",
    "status": "SUCCESS",
    "coins": 500,
    "amount": 399.0
  },
  "new_balance": 734
}
```

### 4.4 Get Transaction History
```http
GET /wallet/transactions?page=1&limit=20&type=CALL_SPENT
```

**Query Parameters:**
- `page` (integer, optional): Page number (default: 1)
- `limit` (integer, optional): Items per page (default: 20, max: 50)
- `type` (string, optional): Filter by transaction type: `PURCHASE`, `CALL_SPENT`, `WITHDRAWAL`

**Response:**
```json
{
  "success": true,
  "transactions": [
    {
      "id": "TXN_3000",
      "type": "PURCHASE",
      "coins": 3000,
      "is_credit": true,
      "status": "SUCCESS",
      "description": null,
      "created_at": "2024-11-04T00:00:00Z",
      "date": "Nov 04",
      "time": "00:00 AM",
      "title": "Wallet Recharge",
      "icon_type": "wallet",
      "amount": 599.0,
      "payment_method": "PhonePe"
    },
    {
      "id": "TXN_840",
      "type": "CALL_SPENT",
      "coins": 840,
      "is_credit": false,
      "status": "SUCCESS",
      "description": null,
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
    },
    {
      "id": "TXN_30",
      "type": "CALL_SPENT",
      "coins": 30,
      "is_credit": false,
      "status": "SUCCESS",
      "description": null,
      "created_at": "2024-11-04T15:00:00Z",
      "date": "Nov 04",
      "time": "15:00 PM",
      "title": "Audio session with Anushrma09",
      "icon_type": "audio",
      "call": {
        "id": "CALL_9876543210",
        "type": "AUDIO",
        "duration": 125,
        "duration_formatted": "2 min 5 sec",
        "partner": {
          "id": "USR_1234567890",
          "name": "Anushrma09",
          "profile_image": "https://cdn.onlycare.app/profiles/...",
          "gender": "FEMALE"
        }
      }
    }
  ],
  "pagination": {
    "current_page": 1,
    "total_pages": 5,
    "total_items": 95,
    "per_page": 20
  }
}
```

**Transaction Types:**
- `PURCHASE` - Coin package purchase (credit transaction with wallet icon)
- `CALL_SPENT` - Coins spent on audio/video call (debit transaction with call details)
- `WITHDRAWAL` - Withdrawal request (debit transaction for female users)

**Icon Types:**
- `wallet` - For PURCHASE transactions
- `audio` - For AUDIO call transactions
- `video` - For VIDEO call transactions
- `withdrawal` - For WITHDRAWAL transactions
- `transaction` - For generic transactions

### 4.5 Get Wallet Balance
```http
GET /wallet/balance
```

**Response:**
```json
{
  "success": true,
  "coin_balance": 734,
  "total_earned": 5430,
  "total_spent": 1234,
  "available_for_withdrawal": 2340
}
```

---

## 5. Earnings & Withdrawal APIs (Female Users)

### 5.1 Get Earnings Dashboard
```http
GET /earnings/dashboard
```

**Response:**
```json
{
  "success": true,
  "total_earnings": 5430,
  "today_earnings": 230,
  "week_earnings": 1240,
  "month_earnings": 3456,
  "available_balance": 2340,
  "pending_withdrawals": 1500,
  "total_withdrawals": 1590,
  "total_calls": 127,
  "average_per_call": 42,
  "total_duration": 151200
}
```

### 5.2 Request Withdrawal
```http
POST /withdrawals/request
```

**Request:**
```json
{
  "amount": 1500,
  "bank_account_id": "BANK_1234567890"
}
```

**Response:**
```json
{
  "success": true,
  "withdrawal": {
    "id": "WD_1234567890",
    "amount": 1500,
    "coins": 1500,
    "status": "PENDING",
    "bank_account": {
      "account_holder": "Priya Sharma",
      "account_number": "XXXX-XXXX-1234",
      "ifsc_code": "SBIN0001234"
    },
    "requested_at": "2024-01-20T10:30:00Z",
    "processing_days": "3-5"
  },
  "new_available_balance": 840
}
```

### 5.3 Get Withdrawal History
```http
GET /withdrawals/history?page=1&limit=20
```

**Response:**
```json
{
  "success": true,
  "withdrawals": [
    {
      "id": "WD_1234567890",
      "amount": 1500,
      "coins": 1500,
      "status": "COMPLETED",
      "bank_account": { /* bank account info */ },
      "requested_at": "2024-01-20T10:30:00Z",
      "completed_at": "2024-01-23T15:45:00Z"
    }
  ],
  "pagination": { /* pagination object */ }
}
```

### 5.4 Get Bank Accounts
```http
GET /bank-accounts
```

**Response:**
```json
{
  "success": true,
  "bank_accounts": [
    {
      "id": "BANK_1234567890",
      "account_holder_name": "Priya Sharma",
      "account_number": "12345678901234",
      "ifsc_code": "SBIN0001234",
      "bank_name": "State Bank of India",
      "branch_name": "Mumbai Main Branch",
      "upi_id": "priya@paytm",
      "is_primary": true,
      "is_verified": true
    }
  ]
}
```

### 5.5 Add Bank Account
```http
POST /bank-accounts
```

**Request:**
```json
{
  "account_holder_name": "Priya Sharma",
  "account_number": "12345678901234",
  "ifsc_code": "SBIN0001234",
  "upi_id": "priya@paytm"
}
```

**Response:**
```json
{
  "success": true,
  "bank_account": { /* bank account object */ }
}
```

### 5.6 Update Bank Account
```http
PUT /bank-accounts/:accountId
```

### 5.7 Delete Bank Account
```http
DELETE /bank-accounts/:accountId
```

---

## 6. KYC APIs (Female Users)

### 6.1 Get KYC Status
```http
GET /kyc/status
```

**Response:**
```json
{
  "success": true,
  "kyc_status": "APPROVED",
  "documents": [
    {
      "type": "AADHAAR",
      "status": "APPROVED",
      "submitted_at": "2024-01-18T10:30:00Z",
      "verified_at": "2024-01-19T14:20:00Z"
    },
    {
      "type": "PAN",
      "status": "APPROVED",
      "submitted_at": "2024-01-18T10:30:00Z",
      "verified_at": "2024-01-19T14:20:00Z"
    }
  ]
}
```

### 6.2 Submit KYC Documents
```http
POST /kyc/submit
```

**Request (multipart/form-data):**
```
aadhaar_number: "123456789012"
aadhaar_front: [File]
aadhaar_back: [File]
pan_number: "ABCDE1234F"
pan_image: [File]
selfie: [File]
```

**Response:**
```json
{
  "success": true,
  "message": "KYC documents submitted successfully",
  "kyc_status": "PENDING"
}
```

---

## 7. Chat APIs

### 7.1 Get Conversations
```http
GET /chat/conversations?page=1&limit=20
```

**Response:**
```json
{
  "success": true,
  "conversations": [
    {
      "user": {
        "id": "USR_1234567890",
        "name": "Priya",
        "profile_image": "https://...",
        "is_online": true
      },
      "last_message": {
        "content": "Hey! How are you?",
        "created_at": "2024-01-20T10:30:00Z",
        "is_from_me": false
      },
      "unread_count": 2
    }
  ],
  "pagination": { /* pagination object */ }
}
```

### 7.2 Get Messages
```http
GET /chat/:userId/messages?page=1&limit=50
```

**Response:**
```json
{
  "success": true,
  "messages": [
    {
      "id": "MSG_1234567890",
      "sender_id": "USR_1234567890",
      "receiver_id": "USR_9876543210",
      "content": "Hey! How are you?",
      "is_read": true,
      "created_at": "2024-01-20T10:30:00Z"
    }
  ],
  "pagination": { /* pagination object */ }
}
```

### 7.3 Send Message
```http
POST /chat/:userId/messages
```

**Request:**
```json
{
  "content": "I'm good! How about you?"
}
```

**Response:**
```json
{
  "success": true,
  "message": {
    "id": "MSG_1234567891",
    "sender_id": "USR_9876543210",
    "receiver_id": "USR_1234567890",
    "content": "I'm good! How about you?",
    "is_read": false,
    "created_at": "2024-01-20T10:31:00Z"
  }
}
```

### 7.4 Mark Messages as Read
```http
POST /chat/:userId/mark-read
```

**Response:**
```json
{
  "success": true,
  "message": "Messages marked as read"
}
```

---

## 8. Friends APIs

### 8.1 Get Friends List
```http
GET /friends?page=1&limit=20
```

**Response:**
```json
{
  "success": true,
  "friends": [
    {
      "id": "USR_1234567890",
      "name": "Priya",
      "age": 24,
      "profile_image": "https://...",
      "is_online": true,
      "last_seen": 1705745400000
    }
  ],
  "pagination": { /* pagination object */ }
}
```

### 8.2 Send Friend Request
```http
POST /friends/:userId/request
```

**Response:**
```json
{
  "success": true,
  "message": "Friend request sent"
}
```

### 8.3 Accept Friend Request
```http
POST /friends/:userId/accept
```

**Response:**
```json
{
  "success": true,
  "message": "Friend request accepted"
}
```

### 8.4 Reject Friend Request
```http
POST /friends/:userId/reject
```

**Response:**
```json
{
  "success": true,
  "message": "Friend request rejected"
}
```

### 8.5 Remove Friend
```http
DELETE /friends/:userId
```

**Response:**
```json
{
  "success": true,
  "message": "Friend removed"
}
```

---

## 9. Referral APIs (Share & Get Coins)

### 9.1 Get Referral Code & Statistics
```http
GET /referral/code
```

Retrieves user's unique referral code, statistics, and shareable links. Perfect for the "Share & Get Coins" screen.

**Response:**
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

**Fields Explanation:**
- `referral_code` - Unique 8-character code (4 letters + 4 numbers, e.g., CTLA8241)
- `my_invites` - Total number of successful referrals (claimed only)
- `per_invite_coins` - Bonus coins earned per referral (default: 10)
- `total_coins_earned` - Total coins earned from all referrals
- `share_message` - Ready-to-share text message
- `whatsapp_share_url` - Direct WhatsApp share link

### 9.2 Apply Referral Code
```http
POST /referral/apply
```

Apply a referral code to receive bonus coins. Both the referrer and referee receive coins.

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

**Error Responses:**
- `400 ALREADY_USED` - User already used a referral code
- `400 INVALID_CODE` - Cannot use your own referral code
- `404 NOT_FOUND` - Referral code doesn't exist

**Rules:**
- Each user can only use ONE referral code (first-time only)
- Cannot use your own referral code
- Both referrer and referee receive coins instantly

### 9.3 Get Referral History
```http
GET /referral/history?page=1&limit=20
```

Get paginated list of users who used your referral code.

**Query Parameters:**
- `page` (integer, optional): Page number (default: 1)
- `limit` (integer, optional): Items per page (default: 20, max: 50)

**Response:**
```json
{
  "success": true,
  "referrals": [
    {
      "id": "REF_1234567890",
      "referred_user": {
        "id": "USR_9876543210",
        "name": "Rahul Sharma",
        "phone": "9876543210",
        "profile_image": "https://cdn.onlycare.app/profiles/..."
      },
      "bonus_coins": 10,
      "is_claimed": true,
      "created_at": "2024-11-03T10:30:00Z",
      "created_at_formatted": "Nov 03, 2024",
      "claimed_at": "2024-11-03T10:30:00Z"
    },
    {
      "id": "REF_1234567891",
      "referred_user": {
        "id": "USR_9876543211",
        "name": "Priya Patel",
        "phone": "9876543211",
        "profile_image": "https://cdn.onlycare.app/profiles/..."
      },
      "bonus_coins": 10,
      "is_claimed": true,
      "created_at": "2024-11-02T15:20:00Z",
      "created_at_formatted": "Nov 02, 2024",
      "claimed_at": "2024-11-02T15:20:00Z"
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

---

## 10. Report APIs

### 10.1 Report User
```http
POST /reports/user
```

**Request:**
```json
{
  "reported_user_id": "USR_9876543210",
  "report_type": "HARASSMENT",
  "description": "User was sending inappropriate messages..."
}
```

**Response:**
```json
{
  "success": true,
  "message": "Report submitted successfully",
  "report_id": "REP_1234567890"
}
```

---

## 11. Notification APIs

### 11.1 Get Notifications
```http
GET /notifications?page=1&limit=20&unread=true
```

**Response:**
```json
{
  "success": true,
  "notifications": [
    {
      "id": "NOTIF_1234567890",
      "title": "New Call",
      "message": "Rahul is calling you",
      "type": "CALL",
      "reference_id": "CALL_1234567890",
      "is_read": false,
      "created_at": "2024-01-20T10:30:00Z"
    }
  ],
  "unread_count": 5,
  "pagination": { /* pagination object */ }
}
```

### 11.2 Mark Notification as Read
```http
POST /notifications/:notificationId/read
```

### 11.3 Mark All as Read
```http
POST /notifications/read-all
```

---

## 12. Settings APIs

### 12.1 Get App Settings
```http
GET /settings/app
```

**Response:**
```json
{
  "success": true,
  "settings": {
    "audio_call_rate": 10,
    "video_call_rate": 15,
    "min_withdrawal_amount": 500,
    "coin_to_inr_rate": 1,
    "referral_bonus_referrer": 100,
    "referral_bonus_referred": 50
  }
}
```

---

## Error Responses

### Standard Error Format
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_COINS",
    "message": "You don't have enough coins to make this call",
    "details": {
      "required": 10,
      "available": 5
    }
  }
}
```

### Common Error Codes
- `UNAUTHORIZED`: Invalid or expired token
- `FORBIDDEN`: Insufficient permissions
- `NOT_FOUND`: Resource not found
- `VALIDATION_ERROR`: Invalid request data
- `INSUFFICIENT_COINS`: Not enough coins
- `USER_OFFLINE`: User is not online
- `USER_BLOCKED`: User has been blocked
- `CALL_NOT_AVAILABLE`: Call type not enabled
- `KYC_NOT_APPROVED`: KYC verification required
- `MIN_WITHDRAWAL_NOT_MET`: Below minimum withdrawal amount
- `RATE_LIMIT_EXCEEDED`: Too many requests

---

## WebSocket Events (Real-time)

### Connection
```javascript
ws://api.onlycare.app/ws?token={access_token}
```

### Events

**Incoming Call:**
```json
{
  "event": "incoming_call",
  "data": {
    "call_id": "CALL_1234567890",
    "caller": {
      "id": "USR_9876543210",
      "name": "Rahul",
      "profile_image": "https://..."
    },
    "call_type": "AUDIO"
  }
}
```

**New Message:**
```json
{
  "event": "new_message",
  "data": {
    "message": { /* message object */ }
  }
}
```

**Call Status Update:**
```json
{
  "event": "call_status",
  "data": {
    "call_id": "CALL_1234567890",
    "status": "ENDED"
  }
}
```

**User Online Status:**
```json
{
  "event": "user_status",
  "data": {
    "user_id": "USR_1234567890",
    "is_online": true
  }
}
```

---

## Rate Limiting

- **Authentication endpoints**: 5 requests per minute per IP
- **General API endpoints**: 100 requests per minute per user
- **WebSocket**: 1 connection per user

---

## Pagination

All list endpoints support pagination with these parameters:
- `page`: Page number (default: 1)
- `limit`: Items per page (default: 20, max: 50)

Response includes:
```json
{
  "pagination": {
    "current_page": 1,
    "total_pages": 10,
    "total_items": 200,
    "per_page": 20,
    "has_next": true,
    "has_prev": false
  }
}
```

---

This API documentation provides all the endpoints required for the Only Care mobile app to function properly.

